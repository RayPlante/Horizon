/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1997, Board of Trustees of the University of Illinois
 *
 * NCSA Horizon software, both binary and source (hereafter, Software) is
 * copyrighted by The Board of Trustees of the University of Illinois
 * (UI), and ownership remains with the UI.
 *
 * You should have received a full statement of copyright and
 * conditions for use with this package; if not, a copy may be
 * obtained from the above address.  Please see this statement
 * for more details.

 *-------------------------------------------------------------------------
 * The Java Fits code used here was first developed by T. McGlynn
 * (NASA/GSFC/USRA) as public domain software.  Minor changes have 
 * have been made for use with the NCSA Horizon Image Browser package.
 * In general, developers are free to use, update, and redistribute 
 * this source code.
 *-------------------------------------------------------------------------
 * History: 
 *  97            Original version by T. McGlynn
 *
 *  97 Oct.  Wei Xie   Adapted for use with the Horizon package.
 *                     Package renamed to ncsa.fits.
 */

package ncsa.fits.fits;

import java.io.*;
import java.net.*;
import java.util.*;
import ncsa.fits.util.*;

/** Methods to read/write FITS Header/Data unit (HDU).
  */
public class HDU extends Object implements IO {
    
  private boolean dataFilled=false;
  private boolean valid = false;
  private boolean lastHDU = false;
  private Header  myHeader=null;
  private Data    myData=null;
  private long    fileOffset;
  private long    headerSize=0;
  private long    dataSize=0;
  private long    trueDataSize=0;
  private DataInput dis;
  
  /** Null constructor */
  public HDU() {  // Do nothing
  }
    
  /** Standard constructor.
   *
   * @param dis Data stream from which HDU is to be read.
   * @param HDUlist  Vector of HDU's in the FITS file.
   * @param needData Should the data be read or skipped?
   */
  public HDU(DataInput dis, Vector HDUlist, boolean needData) {
    
    this.dis = dis;
    if (HDUlist.size() == 0) {
      fileOffset = 0;
    }
    else {
      HDU h=null;
      try {
	h = (HDU) HDUlist.lastElement();
      }
      catch (NoSuchElementException e) {
	System.err.println("What??");
      }
	    
      fileOffset = h.getFileOffset() + h.getHeaderSize() + h.getDataSize();
    }

    if (readHeader()) {
      if (needData) {
	if (readData()) {
	  dataFilled = true;
	  valid = true;
	}
      }
      else {
	if (skipData()) {
	  valid = true;
	}
      }
    }
    return;
  }
    
  /** Simple method for getting a single HDU.
   * @param file  The file name in which the HDU appears.
   * @param extension The number of the extension (0 for the primary array).
   */
  public static HDU getHDU(String file, int extension) {
    try {
      Fits f;
      HDU h;
      f = new Fits(file);
      h = f.readHDU(extension);
      return h;
    }
    catch (Exception e) {
      System.err.println("Caught exception when generating HDU:"+e);
      return null;
    }
  }
    
    /** Get the Header associated with the HDU */
    public Header getHeader() {
	return myHeader;
    }
    
    /** Get the Data associated with the HDU */
    public Data getData() {
	return myData;
    }
    
    /** Make sure that the Data has been read for this HDU.
      * On exit we skip to the presumed end of file.
      * @param eof Where to place the file pointer when done.
      * @return Did we successfully get the data for this HDU?
      */
    protected boolean fillData(long eof) {
	if (dis instanceof RandomAccessFile) {
	    RandomAccessFile ras = (RandomAccessFile) dis;
	    try {
	        ras.seek(fileOffset);
	        if (readData()) {
		    valid = true;
		    dataFilled = true;
		} else {
		    valid = false;
		    dataFilled = false;
		}
		ras.seek(eof);
	    } catch (IOException e) {
		System.err.println("Exception caught in fillData:"+e);
		return false;
	    }
	    
	    return valid;
	    
	} else {
	    return false;
	}
    }
    
    /** Is this a valid HDU? */
    public boolean isValid(){
	return valid;
    }
    
    /** Get the offset of the beginning of the HDU */
    public long getFileOffset() {
	return fileOffset;
    }
    
    /** Have we read the Data for this HDU? */
    public boolean haveData() {
	return dataFilled;
    }
    
    /** Indicate that this is the last HDU. */
    protected void setLast() {
	lastHDU = true;
    }
    
    /** Set the flag which indicates whether this is the last HDU
      * @param flag indicates whether this is the last HDU
      */
    protected void setLast(boolean flag) {
	lastHDU = flag;
    }
    
    /** Is this the last valid HDU in the FITS file? */
    public boolean isLast() {
	return lastHDU;
    }
    
    /** Get the size of the Header in bytes (including padding) */
    public long getHeaderSize() {
	return headerSize;
    }
    
    
    /** Get the size of the Data in bytes (including padding) */
    public long getDataSize() {
	return dataSize;
    }
    
    /* Write out the HDU */
    public void writeClass(DataOutput dos) throws IOException {
	if (valid) {
	    myHeader.writeClass(dos);
	    if (dataFilled) {
		myData.writeClass(dos);
	    }
	    writePadding(dos);
	}
    }
    
    /** Write out any needing padding bytes */
    protected void writePadding(DataOutput dos) throws IOException {
	if (dataSize  > trueDataSize) {
	    byte[] pad = new byte[(int)(dataSize-trueDataSize)];
	    dos.write(pad);
	}
    }
    
    /** Read the HDU from the input */
    public void readClass(DataInput dis) throws IOException {
	
	this.dis = dis;
	this.fileOffset = 0;
	if (readHeader()) {
	    dataFilled = readData();
	}
	
    }
    
    /** Print the HDU to a printstream */
    public void printClass(PrintStream ps) throws IOException {
	if (valid) {
	    ps.println(">>> Header <<<");
	    myHeader.printClass(ps);
	    if (dataFilled) {
		ps.println(">>> Data <<<");
		myData.printClass(ps);
	    } else {
		ps.println(">>> Data unavailable <<<");
	    }
	} else {
	    ps.println(">>> Invalid HDU <<<");
	}
    }
    
    
    /** Read the Header for the HDU */
    protected boolean readHeader() {
        myHeader = new Header(dis);
	if (myHeader.validHeader()) {
	    dataSize = myHeader.dataSize();
	    headerSize = myHeader.getSize();
	    valid = true;
	} else {
	    valid = false;
	}
	return valid;
    }
	
 
    /** Read the Data for the HDU */
    protected boolean readData() {
	myData = makeData();
	try {
	    myData.readClass(dis);
	    dis.skipBytes((int) (dataSize-trueDataSize));
	    
	} catch (IOException e) {
	    System.out.println("ReadData caught exception:"+e);
	    return false;
	}
	return true;
    }
    
    /** Create a Data object to correspond to the header description */
    protected Data makeData() {
	String card0 = myHeader.getCard(0);
	String key = card0.substring(0,8);
	String val = card0.substring(11,19);
	  
	if (key.equals("SIMPLE  ") || (key.equals("XTENSION") && 
				       val.equals("IMAGE   ") ) ){
	    return imageData();
	} else if (key.equals("XTENSION")) {
	    if (val.equals("TABLE   ") ) {
		return asciiData();
	    } else if (val.equals("BINTABLE")) {
		return binaryData();
	    } else {
		System.err.println("Invalid XTENSION type: "+key+"="+val);
	        return null;
	    }
	} else {
	    System.err.println("Invalid HDU: not SIMPLE or XTENSION:"+val);
	    return null;
	}
    }
    
  /** 
   * Create a Data object for image data 
   *(primary arrays and IMAGE extensions)
   */
  protected Data imageData() {
    
    int bitpix;
    int type;
    int ndim;
    int[] dims;
    long trueSize;
    long cdim;
    int i;
	
	
    if (myHeader.getLValue("GCOUNT",1L) != 1  ||
	myHeader.getLValue("PCOUNT",0L) != 0) {
      System.err.println("Currently unable to handle GROUPed data");
      return null;
    }
	
    bitpix = (int) myHeader.getLValue("BITPIX", 0L);
    if (bitpix == 8) {
      type = Data.BYTE;
    }
    else if (bitpix == 16) {
      type = Data.SHORT;
    }
    else if (bitpix == 32) { 
      type = Data.INT;
    }
    else if (bitpix == 64) {  /* This isn't a standard for FITS yet...*/
      type = Data.LONG;
    }
    else if (bitpix == -32) {
      type = Data.FLOAT;
    }
    else if (bitpix == -64) {
      type = Data.DOUBLE;
    }
    else {
      System.err.println("Unable to parse BITPIX value:"+bitpix);
      return null;
    }
    
    ndim = (int) myHeader.getLValue("NAXIS   ", 0L) ;
    dims = new int[ndim];
    
    if (ndim > 0) {
      trueSize = 1;
    }
    else {
      return new Data();  // Return a null Data object
    }
	
    // Note that we have to invert the order of the axes
    // for the FITS file.
    for (i=1; i<=ndim; i += 1) {
      cdim = myHeader.getLValue("NAXIS"+i, 0L);
      trueSize *= cdim;
      dims[ndim-i] = (int) cdim;
    }
	
    trueSize *= Math.abs(bitpix)/8;
    
    this.trueDataSize = trueSize;
    try {
      return new Data(type, dims);
    }
    catch (Exception e) {
      System.err.println("Unable to create data: exception "+e);
      return null;
    }
  }

  /** Create a Data object for ASCII tables */
  Data asciiData() {
    System.err.println("I cannot read ASCII tables");
    return null;
  }
    
    /** Create a Data object for Binary tables.
      * Currently this does not support variable length fields
      */
    Data binaryData() {
	
	int nfields;
	Vector	binDataVec = new Vector(50);
	int i;
	
	long naxis1 = myHeader.getLValue("NAXIS1", 0);
	long naxis2 = myHeader.getLValue("NAXIS2", 0);
	this.trueDataSize = naxis1*naxis2;
	if (this.trueDataSize <= 0) {
	    return null;
	}
	
	
	nfields = (int) myHeader.getLValue("TFIELDS",0);
	for (i=0; i<nfields; i += 1) {
	    Data column = getColumn(i+1);
	    if (column == null) {
		System.err.println("Invalid TFORM for column "+(i+1));
	    } else {
		binDataVec.addElement(column);
	    }
	}
	Data[] dv = new Data[binDataVec.size()];
	binDataVec.copyInto(dv);
	Data row = new Data(dv);
	
	int[] dims = {(int)naxis2};
	
	Data  table;
	try {
	    table = new Data(Data.OBJECT, dims);
	} catch (Exception e) {
	    System.err.println("Error creating table object");
	    return null;
	}
	for (i=0; i<naxis2; i += 1) {
	    try {
		Object temp;
		temp = (Object) row.clone();
	        ((Object[])table.getData())[i] = temp;
	    } catch (CloneNotSupportedException e) {
		System.err.println("Error cloning rows");
		return null;
	    }
	}
	return new Data(table);
    }
	

    /** Skip the data section of an HDU */
    protected boolean skipData() {
	
	try {
	    dis.skipBytes( (int) dataSize);
	} catch (IOException e) {
	    return false;
	}
	return true;
    }
    
  /** Get the format for a given column */
  protected Data getColumn(int col) {
    
    int i;
    int datatype;
    int arrsiz;
	
    String format = myHeader.getSValue("TFORM"+col);
	
    if (format == null) {
      return null;
    }
	
    // Skip initial white space
    for (i=0; i<format.length(); i += 1) {
      if (!Character.isSpace(format.charAt(i))){
	break;
      }
    }
    // Skip numbers
    for ( ;i<format.length(); i += 1) {
      if (!Character.isDigit(format.charAt(i))) {
	break;
      }
    }
    
    if (i < format.length() ) {
      if (i > 0) {
	arrsiz = Integer.parseInt(format.substring(0,i));
      }
      else {
	arrsiz = 1;
      }
	
      switch (format.charAt(i)){
      case 'X':
	datatype = Data.BYTE;
	arrsiz = (arrsiz+7)/8;
	break;
      case 'B':
      case 'A':
      case 'L':
	datatype = Data.BYTE;
	break;
      case 'I':
	datatype = Data.SHORT;
	break;
      case 'J':
	datatype = Data.INT;
	break;
      case 'E':
	datatype = Data.FLOAT;
	break;
      case 'D':
	datatype = Data.DOUBLE;
	break;
      case 'C':
	datatype = Data.FLOAT;
	arrsiz *= 2;
	break;
      case 'M':
	datatype = Data.DOUBLE;
	arrsiz= 2;
	break;
      case 'P':
	datatype = Data.INT;
	if (arrsiz > 0) {
	  arrsiz = 2;
	}
	else {
	  arrsiz = 0;
	}
      default:
	System.err.println("Invalid column code:"+format.charAt(i));
	return null;
      }
      int[] dims = {arrsiz};
	    
      if (arrsiz > 1) {
	try {
	  return new Data(datatype, dims);
	}
	catch (Exception e) {
	  System.err.println("Unable to create column: exception "+e);
	  return null;
	}
      }
      else if (arrsiz == 1) {
	try {
	  Data temp = new Data(datatype, new int[0]);
	  return temp;
	}
	catch (Exception e) {
	  System.err.println("Unable to create column: exception "+e);
	  return null;
	}
      }
      else {
	return null;
      }
    }
    else {
      return null;
    }
  }
  
}
