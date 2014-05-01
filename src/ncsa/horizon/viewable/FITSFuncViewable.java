/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@atmos.ncsa.uiuc.edu
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
 *    24-Dec-1997 Wei Xie     Initial version.
 *    13-Jan-1998 Ray Plante  added setRange to set the TransferFunction to
 *                               to the range of the data.
 *    19-Jan-1998 Ray Plante  updated to NdArray* move to ncsa.horizon.data
 *    02-Feb-1998 Ray Plante  now handles short and byte data types
 */

// Classes in this file:
// FITSFuncViewable

package ncsa.horizon.viewable;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import ncsa.horizon.awt.ROI;
import ncsa.horizon.awt.image.SliceImageSource;
import ncsa.horizon.awt.image.TransferFunctionImageSource;
import ncsa.horizon.awt.image.FlippingFilter;
import ncsa.horizon.util.*;
import ncsa.horizon.data.*;
import ncsa.horizon.coordinates.CoordinateSystem;
import ncsa.horizon.coordinates.FITSCoordMetadata;

public class FITSFuncViewable extends FITSViewable {

  protected TransferFunction transferFunction;
  /**
   * Can't instantiate a FITSFuncViewable without argument.
   */
  protected FITSFuncViewable() {
    ;
  }

  /** 
   * New a Viewable to read local FITS file.
   * If a reader can't be created for this file. Exception throws.
   */
  public FITSFuncViewable(String filename) throws InstantiationException {
    super(filename);
  } // end FITSFuncViewable(String name)

  /** 
   * New a Viewable to read through a URL.
   * If a reader can't be created for this file. Exception throws.
   */
  public FITSFuncViewable(URL url) throws InstantiationException {
    super(url);
  } // end FITSFuncViewable(String name)

  /** 
   * New a Viewable to read local FITS file.
   * If a reader can't be created for this file. Exception throws.
   */
  public FITSFuncViewable(String filename,
			  TransferFunction transferFunction)
    throws InstantiationException {
    super(filename);
    this.transferFunction = transferFunction;
    setRange();
  } // end FITSFuncViewable(String name)

  /** 
   * New a Viewable to read through a URL.
   * If a reader can't be created for this file. Exception throws.
   */
  public FITSFuncViewable(URL url, 
			  TransferFunction transferFunction)
    throws InstantiationException {
    super(url);
    this.transferFunction = transferFunction;
    setRange();
  } // end FITSFuncViewable(String name)

  private void setRange() {
      if (transferFunction == null) return;

      NdArrayData d = getData();
      JavaType jt = d.getType();
      Object da = d.getValue();
      Object min=null, max=null;
      try {
	  if (jt == JavaType.DOUBLE) {
	      double[] dda = (double[]) da;
	      double[] mnmx = NdArrayMath.minMax1d(dda);
	      min = jt.wrap(mnmx[0]);
	      max = jt.wrap(mnmx[1]);
	  }
	  else if (jt == JavaType.FLOAT) {
	      float[] dda = (float[]) da;
	      float[] mnmx = NdArrayMath.minMax1d(dda);
	      min = jt.wrap(mnmx[0]);
	      max = jt.wrap(mnmx[1]);
	  }
	  else if (jt == JavaType.INT) {
	      int[] dda = (int[]) da;
	      int[] mnmx = NdArrayMath.minMax1d(dda);
	      min = jt.wrap(mnmx[0]);
	      max = jt.wrap(mnmx[1]);
	  }
	  else if (jt == JavaType.LONG) {
	      long[] dda = (long[]) da;
	      long[] mnmx = NdArrayMath.minMax1d(dda);
	      min = jt.wrap(mnmx[0]);
	      max = jt.wrap(mnmx[1]);
	  } 
	  else if (jt == JavaType.SHORT) {
	      short[] dda = (short[]) da;
	      short[] mnmx = NdArrayMath.minMax1d(dda);
	      min = JavaType.INT.wrap((int) mnmx[0]);
	      max = JavaType.INT.wrap((int) mnmx[1]);
	  } 
	  else if (jt == JavaType.BYTE) {
	      byte[] dda = (byte[]) da;
	      byte[] mnmx = NdArrayMath.minMax1d(dda);
	      min = JavaType.INT.wrap((int) mnmx[0]);
	      max = JavaType.INT.wrap((int) mnmx[1]);
	  } 
	  else { 
	      System.err.println("Warning: unsupported (non-numeric) " + 
				 "data type");
	      return;
	  }
      } catch (ClassCastException ex) {
	  throw new InternalError("JavaType implementation error");
      }

      System.err.println("Range: " + min + ", " + max);
      if (min != null) transferFunction.setMinimum(min);
      if (max != null) transferFunction.setMaximum(max);
  }
	  

  protected Image createImage(Slice sl, ColorModel cm) {
    SliceImageSource sis = null;
    if (cm == null) {
      sis = new TransferFunctionImageSource(sl, dataVolume);
    } else if (transferFunction == null) {
      sis = new TransferFunctionImageSource(sl, dataVolume, cm);
    } else {
      sis = new TransferFunctionImageSource(sl, dataVolume,
					    cm, transferFunction);
    }
    if(!dataVolume.isComplete()) {
      dataVolume.addObserver(sis);
    }
    image = Toolkit.getDefaultToolkit().createImage(sis);
    return flip(image);
  }

  /**
   * Not alway create a new image.
   */
  // public Image getView(Slice slice) {

  // }

  /**
   * Create a (2-D) view from a slice into the image data and a given color
   * model.  
   * @param slice       region of data to make into an image.  Null is allowed.
   *                    If slice is null, I will use default slice instead.
   * @param colorModel  a java.awt.image.ColorModel to apply to the returned 
   *                    Image object, if possible.  If this is null, use
   *                    the Viewable's default colormodel.
   * @param makeDefault if true make this slice be the viewable's default view,
   *                    if possible
   * <p>
   * @return   A java.awt.Image object (null on failure)
   */
  public Image getView(Slice slice, 
                       ColorModel colorModel, boolean makeDefault) {
    if (slice == null) {
      slice = getDefaultSlice();
    } else if (makeDefault) {
      makeDefaultSlice(slice);
    }
    Volume volume = dataVolume.getVolume();

    this.slice = slice.projection(volume);
    image = createImage(this.slice, colorModel);

    // this.slice and slice are in the same plane
    // should be changed to be slice.equals(this.slice), 
    // but I don't have equal method right now
    if (this.slice == slice) {
      return image;
    } else {
      return cropImage(slice);
    }
  }

}
