/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996, Board of Trustees of the University of Illinois
 *
 * NCSA Horizon software, both binary and source (hereafter, Software) is
 * copyrighted by The Board of Trustees of the University of Illinois
 * (UI), and ownership remains with the UI.
 *
 * You should have received a full statement of copyright and
 * conditions for use with this package; if not, a copy may be
 * obtained from the above address.  Please see this statement
 * for more details.
 *
 */
/*
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    19-Jan-1998 Ray Plante  moved from ncsa.horizon.util to ncsa.horizon.data
 *    02-Feb-1998 Ray Plante  now converts data values using BSCALE, BZERO
 *                            FITS metadata; made less of a memory hog by 
 *                            forgetting original data read from FITS reader
 */

package ncsa.horizon.data;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import ncsa.fits.fits.*;
import ncsa.fits.util.*;
import ncsa.horizon.coordinates.FITSCoordMetadata;
import ncsa.horizon.util.*;

/**
 * NdArrayReader implementation for on-disk FITS file.
 */
public class FITSNdArrayReader extends NdArrayReader {

  private InputStream inputStream;
  private Fits fits;
  private HDU primaryHdu;
  private Header primaryHeader;
  private Data primaryData;
  private Object convertedData = null;
  private JavaType javaType, storedType;

  private Double bzero, bscale;
  private Integer blank;
  private Metadata md = null;

  /**
   * Disabled.  No argument constructor, do nothing. It is protected
   * so cannot be instanticated this way.
   */
  protected FITSNdArrayReader() {
  }

  /**
   * Construct a NdArrayReader to read the local file Filename: name.
   * Exception throws if the file is not find or not in FITS format.
   */
  public FITSNdArrayReader(String name) throws InstantiationException {
    try {
      inputStream = new FileInputStream(name);
      fits = new Fits(name);
      constructVariables();
    } catch (IOException e) { // can't open file
      System.err.println(getClass().getName() + ": " + e);
      throw new InstantiationException(getClass().getName() + ": " + e +
				       "Maybe FileNotFound: " + name);
    } catch (SecurityException e) { // not allowed to read the file
      System.err.println(getClass().getName() + ": " + e);
      throw new InstantiationException(getClass().getName() + ": " + e + 
				       "Maybe you don't have the access to file: " +
				       name);
    } catch (NullPointerException e) { // maybe file is not a good FITS file
      System.err.println(getClass().getName() + ": " + e);
      throw new InstantiationException(getClass().getName() + e + 
			  " Maybe file is not a good FITS file.");
    } catch (OutOfMemoryError e) {  /* not enough memory to create Fits
				     * object with data */
      System.err.println(getClass().getName() + ": " + e);
      throw new InstantiationException(getClass().getName() + e + 
			  " Maybe not enough memory for Fits with data.");
    }
  }

  /**
   * Construct a NdArrayReader to read through URL.
   * Exception throws for bad network connection or the request
   * file is not found or not in FITS format.
   */
  public FITSNdArrayReader(URL url) throws InstantiationException {
    try {
      inputStream = url.openStream();
      fits = new Fits(url);
      constructVariables();
    }
    catch(IOException e) {
      System.err.println(getClass().getName() + ": " + e);
      throw new InstantiationException(getClass().getName() +
				       " bad network connection: " + 
				       url.getRef());
    }
    catch (NullPointerException e) {
      System.err.println(getClass().getName() + e);
      throw new InstantiationException(getClass().getName() + ": " + e + 
				       " Maybe remote file is not found or not a good FITS file.");
    } catch (OutOfMemoryError e) {  /* not enough memory to create Fits
				     * object with data */
      System.err.println(getClass().getName() + ": " + e);
      throw new InstantiationException(getClass().getName() + e + 
			  " Maybe not enough memory for Fits with data.");
    }
  }

  // works only for constructor
  private void constructType() {
    int type = primaryData.getType();
    switch(type) {
    case 1:
      storedType = JavaType.BOOLEAN;
      break;
    case 2:
      storedType = JavaType.BYTE;
      break;
    case 3:
      storedType = JavaType.CHAR;
      break;
    case 7:
      storedType = JavaType.FLOAT;
      break;
    case 8:
      storedType = JavaType.DOUBLE;
      break;
    case 4:
      storedType = JavaType.SHORT;
      break;
    case 5:
      storedType = JavaType.INT;
      break;
    case 6:
      storedType = JavaType.LONG;
      break;
    }
      
    int sc = storedType.code();
    if (sc >= 4 && sc <= 6) {
	if (md == null) getMetadata(); 
	try {
	    bzero  = (Double) md.getMetadatum("NativeMetadata.BZERO");
	    bscale = (Double) md.getMetadatum("NativeMetadata.BSCALE");
	} catch (ClassCastException ex) { bzero = bscale = null; }
	try {
	    blank = (Integer) md.getMetadatum("NativeMetadata.BLANK");
	} catch (ClassCastException ex) { blank = null; }
    }
    if (sc >= 4 && sc <= 6 && 
	bscale != null && bzero != null) 
    {
//	System.err.println("bscale=" + bscale + ", bzero=" + bzero);
	javaType = (sc == 4) ? JavaType.FLOAT : JavaType.DOUBLE;
    } else {
	javaType = storedType;
    }
  }    

  /**
   * Works only for constructors
   */
  private void constructVariables() {
    primaryHdu = fits.readHDU();
    primaryHeader = primaryHdu.getHeader();
    primaryData = primaryHdu.getData();
    constructType();
  }

  
  /**
   * Return how many nd arrays is available
   */
  public int NumberOfNdarray() {
    return 1;
  }

  /**
   * Return the data size in byte for the 
   * first Nd array.  For example,
   * if data type is unsigned integers,
   * dataSize will return 8.
   * If no such information from the fits file,
   * -1 will be returned.
   */
  public int dataSize() {
    return (int) primaryHeader.getLValue("BITPIX", -1);
  }

  /**
   * Return a NdArrayData.  Since we currently only support
   * on-memory NdArrayData instance created from a FITS file,
   * this method may return null if there is not enough memory 
   * to hold all the data.
   */
  public NdArrayData getNdArrayData() {
    int naxes = getNaxes();
    double[] size = ArrayTypeConverter.arrayIntToDouble(getSize());
    double[] loc = new double[naxes];
    double[] sampling = new double[naxes];
    for (int i = 0; i < naxes; i++) {
      loc[i] = 1.0;
      sampling[i] = 1.0;
    }
    Volume aVolume = new Volume(naxes, loc, size, sampling);
    NdArrayData aNdArrayData = null;
    try {
      aNdArrayData = new InMemoryData(aVolume, getType());
      aNdArrayData.setValues(getValue());
    } catch (InstantiationException e) {
      System.err.println(getClass().getName() + e);
      return null;
    }
    if(isFinal()) {
      aNdArrayData.setComplete();
    } else {
      NdArrayDataUpdater dvu = getUpdater();
      dvu.update(aNdArrayData);
    }
    return aNdArrayData;
  }

  public Metadata getMetadata() {
      if (md == null) {
	  
	  FITSMetadata aFITSmd = new FITSMetadata();
	  // FITSCoordMetadata horizonmd = new FITSCoordMetadata();
	  int ncards = (int) primaryHeader.getCardSize();
	  for (int i = 0; i < ncards; i++) {
	      aFITSmd.scanHeaderCard(primaryHeader.getCard(i), true);
	  }
	  // FITSMetadata.convertToHorizon(aFITSmd, horizonmd);
	  aFITSmd.setHorizonMetadata();

	  md = aFITSmd;
      }

      return md;
  }

  /**
   * Return the number of axes for the first Nd array.
   * If no such information from the fits file,
   * -1 will be returned.
   */
  public int getNaxes() {
    return (int) primaryHeader.getLValue("NAXIS", -1);
  }

  /**
   * Return the axis sizes of the first Nd-array
   */
  public int[] getSize() {
    int naxes = getNaxes();
    int[] size = new int[naxes];
    for(int i = 0; i < naxes; i++) {
      String key = "NAXIS" + (i + 1);
      size[i] = (int) primaryHeader.getLValue(key, -1);
    }
    return size;
  }

  /**
   * Return stream of the reading content
   */
  public InputStream getStream() {
    return inputStream;
  }

  /**
   * return data type of the first nd array
   */
  public JavaType getType() {
    return javaType;
  }
    
  /**
   * Return the double value at location coord 
   * of the first nd array.
   * IllegalArgumentException will be thrown if
   * coord is out of bound of int[] getSize.
   */
  public double getValue(int[] coord) throws
  IllegalArgumentException {
    int dim = primaryData.getDimensions().length;
    switch(dim){
    case 1:
      break;
    case 2: {
      float[][] data = (float[][])primaryData.getData();
      return data[coord[1]][coord[0]]; }
    case 3: {
      float[][][] data = (float[][][])primaryData.getData();
      return data[coord[2]][coord[1]][coord[0]]; }
    case 4: {
      float[][][][] data = (float[][][][])primaryData.getData();
      return data[coord[3]][coord[2]][coord[1]][coord[0]]; }
    case 5: {
      float[][][][][] data = (float[][][][][])primaryData.getData();
      return data[coord[4]][coord[3]][coord[2]][coord[1]][coord[0]]; }
    }
    return 0;
  }

  /**
   * Return the 1d array representation
   * of the first nd array.
   * Likely to throw OutOfMemoryError if the first nd array is too large
   */
  public Object getValue() {
      if (convertedData == null) {
	  if (primaryData == null) 
	      throw new InternalError("Forgot primary data too soon!");

	  convertedData = 
	      javaType.allocateArray((int) NdArrayMath.size(getSize()));
	  int dim = primaryData.getDimensions().length;
	  int[] size = getSize();

	  Object inarray = primaryData.getData();
	  fillArray(inarray, storedType, dim, 
		    convertedData, javaType, 0, size);

	  primaryData = null;
	  System.gc();
      }

      return convertedData;
  }

  private void fillArray(Object ndarray, JavaType intype, int dim,
			 Object outarray, JavaType outtype, int start, 
			 int[] size) 
  {
      int i;

      if (dim >= 2) {
	  int[] subsz = new int[size.length-1];
	  System.arraycopy(size, 0, subsz, 0, subsz.length);
	  int rest = subsz[0];
	  for(i=1; i < subsz.length; i++) rest *= subsz[i];

	  for(i=0; i < size[size.length-1]; i++) {
	      fillArray(subArray(ndarray, intype, dim, i), intype, dim-1,
			outarray, outtype, start+(i*rest), subsz);
	  }
      }
      else if (intype == outtype) {
	  System.arraycopy(ndarray, 0, outarray, start, size[0]);
      }
      else if (intype == JavaType.SHORT && outtype == JavaType.FLOAT) {
	  short[] ina = (short[]) ndarray;
	  float[] outa = (float[]) outarray;

	  if (bzero == null || bscale == null) {
	      for(i=0; i < size[0]; i++) outa[i+start] = (float) ina[i];
	  }
	  else {
	      float bs = bscale.floatValue(),
                    bz = bzero.floatValue();
	      int blnk = (blank == null) ? -40000 : blank.intValue();

	      for(i=0; i < size[0]; i++) 
		  outa[i+start] = (ina[i] == blnk) ? Float.NaN : 
                                                     bz + bs*ina[i];
	  }
      }
      else if ((intype == JavaType.INT || intype == JavaType.LONG) &&
	       outtype == JavaType.DOUBLE) {
	  double[] outa = (double[]) outarray;

	  if (bzero == null || bscale == null) {
	      if (intype == JavaType.INT) {
		  int[] ina = (int[]) ndarray;
		  for(i=0; i < size[0]; i++) outa[i+start] = (double) ina[i];
	      }
	      else {
		  long[] ina = (long[]) ndarray;
		  for(i=0; i < size[0]; i++) outa[i+start] = (double) ina[i];
	      }
	  }
	  else {
	      double bs = bscale.doubleValue(),
                     bz = bzero.doubleValue();
	      long blnk = (blank == null) ? Long.MIN_VALUE : blank.longValue();

	      if (intype == JavaType.INT) {
		  int[] ina = (int[]) ndarray;
		  for(i=0; i < size[0]; i++) 
		      outa[i+start] = (ina[i] == blnk) ? 
			                 Double.NaN : (double) ina[i];
	      }
	      else {
		  long[] ina = (long[]) ndarray;
		  for(i=0; i < size[0]; i++) 
		      outa[i+start] = (ina[i] == blnk) ? 
			                 Double.NaN : (double) ina[i];
	      }
	  }
      }
      else {
	  throw new ClassCastException("bad intype-outtype combo");
      }
  }

  private Object subArray(Object ndarray, JavaType type, int dim, int which) {
      switch(type.code()) {
      case 1:
	  switch(dim) {
	  case 2:
	      return ((boolean[][])ndarray)[which];
	  case 3:
	      return ((boolean[][][])ndarray)[which];
	  case 4:
	      return ((boolean[][][][])ndarray)[which];
	  case 5:
	      return ((boolean[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 2:
	  switch(dim) {
	  case 2:
	      return ((char[][])ndarray)[which];
	  case 3:
	      return ((char[][][])ndarray)[which];
	  case 4:
	      return ((char[][][][])ndarray)[which];
	  case 5:
	      return ((char[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 3:
	  switch(dim) {
	  case 2:
	      return ((byte[][])ndarray)[which];
	  case 3:
	      return ((byte[][][])ndarray)[which];
	  case 4:
	      return ((byte[][][][])ndarray)[which];
	  case 5:
	      return ((byte[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 4:
	  switch(dim) {
	  case 2:
	      return ((short[][])ndarray)[which];
	  case 3:
	      return ((short[][][])ndarray)[which];
	  case 4:
	      return ((short[][][][])ndarray)[which];
	  case 5:
	      return ((short[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 5:
	  switch(dim) {
	  case 2:
	      return ((int[][])ndarray)[which];
	  case 3:
	      return ((int[][][])ndarray)[which];
	  case 4:
	      return ((int[][][][])ndarray)[which];
	  case 5:
	      return ((int[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 6:
	  switch(dim) {
	  case 2:
	      return ((long[][])ndarray)[which];
	  case 3:
	      return ((long[][][])ndarray)[which];
	  case 4:
	      return ((long[][][][])ndarray)[which];
	  case 5:
	      return ((long[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 7:
	  switch(dim) {
	  case 2:
	      return ((float[][])ndarray)[which];
	  case 3:
	      return ((float[][][])ndarray)[which];
	  case 4:
	      return ((float[][][][])ndarray)[which];
	  case 5:
	      return ((float[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      case 8:
	  switch(dim) {
	  case 2:
	      return ((double[][])ndarray)[which];
	  case 3:
	      return ((double[][][])ndarray)[which];
	  case 4:
	      return ((double[][][][])ndarray)[which];
	  case 5:
	      return ((double[][][][][])ndarray)[which];
	  default:
	      throw new InternalError("cast algorithm logic error");
	  }
      default:
	  throw new InternalError("unknown JavaType");
      }
  }


}
