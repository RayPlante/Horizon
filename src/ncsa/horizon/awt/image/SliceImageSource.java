/**
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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 */

/*
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    19-Jan-1998 Ray Plante  updated to NdArray* move to ncsa.horizon.data
 *
 * version 2
 */
package ncsa.horizon.awt.image;

import java.awt.image.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import ncsa.horizon.util.ArrayTypeConverter;
import ncsa.horizon.util.JavaType;
import ncsa.horizon.util.Slice;
import ncsa.horizon.data.NdArrayMath;
import ncsa.horizon.data.NdArrayData;

/**
 * An image producer.  It produce a image represent a slice.
 */
public class SliceImageSource implements ImageProducer, Observer, Runnable {
  /**
   * The source slice.
   */
  protected Slice slice;
  /**
   * A vector of ImageConsumers
   */
  protected Vector consumers;
  /**
   * The source data.
   */
  protected NdArrayData dataVolume;
  protected Thread thread;
  /**
   * The flag indicating that whether the source data
   * is just updated.
   */
  protected boolean flag_update;
  /**
   * The flag used to get argument from the Observance.
   */
  protected Boolean flag_complete;
  protected Hashtable properties;
  /**
   * The color model, with which this ImageProducer produces image
   */
  protected ColorModel colorModel;

  /**
   * 
   */
  public SliceImageSource(Slice slice, NdArrayData volume) {
    this(slice, volume, null);
  }

  /**
   * 
   */
  public SliceImageSource(Slice slice, NdArrayData volume, ColorModel cm) {
    this.slice = slice;
    dataVolume = volume;
    consumers = new Vector();
    flag_update = true; // volume has just been updated
    flag_complete = new Boolean(dataVolume.isComplete());
    properties = new Hashtable();
    if (cm == null) {
      Lut aLut = Lut.getRainbowLut();
      colorModel = new IndexColorModel(8, 256, aLut.getRed(),
			       aLut.getGreen(), aLut.getBlue());
    } else {
      colorModel = cm;
    }
  }

  /**
   * The following methods handle the addition and removal of
   * image consumers.
   */
  /* Consumer such as ImageRepresentation can be added this way:
   * java.awt.Component.prepareImage 756 
   * sun.awt.motif.MComponentPeer.prepareImage 166
   * sun.awt.motif.MToolkit.prepareScrImage: 221 
   * sun.awt.image.ImageRepresentation.prepare: 304
   * sun.awt.image.ImageRepresentation.startProduction: 266
   * ncsa.horizon.awt.image.SliceImageSource.startProduction: 71 
   */
  public synchronized void addConsumer(ImageConsumer ic) {
    if(isConsumer(ic) == false)
      consumers.addElement(ic);
  }

  public boolean isConsumer(ImageConsumer ic) {
    return consumers.indexOf(ic) > -1;
  }

  /* It could be called this way:
   * SliceImageSource.run
   * sun.awt.image.ImageInfoGrabber.setProperties: 387
   * sun.awt.image.Image.setProperties: 286
   * sun.awt.image.Image.addInfo: 271
   * sun.awt.image.ImageInfoGrabber.stopInfo:376
   * then this method is called with ic = 
   *    sun.awt.image.ImageInfoGrabber
   */
  public void removeConsumer(ImageConsumer ic) {
    if(isConsumer(ic) == true)
      consumers.removeElement(ic);
  }

  /**
   * We don't observer this request.
   */
  public void requestTopDownLeftRightResend(ImageConsumer ic) {
    ;
  }
 
  /**
   * Image production starts here.  Image production occurs in a
   * separate thread.  The application itself is free to go on
   * with life.
   */
  /* startProduction must be mutual-excluded from run.
   * This is because if run is runing addComumer will
   * not take effect.   In the run method, comsumers'
   * clone v is used to represent consumer vector.
   * Also, thread != null.  So, startProduction won't start a
   * thread to produce.
   */
  public synchronized void startProduction(ImageConsumer ic) {
    addConsumer(ic);

    if (thread == null) {
      thread = new Thread(this, "sliceimageproducer thread");
      thread.start();
    }
  }

  public synchronized void run() {
    int naxis = slice.getNaxes();
    int xaxis = slice.getXaxis();
    int yaxis = slice.getYaxis();
    int width = (int) slice.getLength(xaxis);
    int height = (int) slice.getLength(yaxis);

    ImageConsumer ic;
    Vector v = (Vector)consumers.clone();
    // Vector v = consumers;
    Enumeration e;

    /* Tell each image consumer the dimensions of the image
     * and the color model we will use.  Since we don't say
     * otherwise, they won't expect pixels to arrive in
     * any particular order (RANDOMPIXELORDER is the
     * default).
     */
    for(e = v.elements(); e.hasMoreElements(); ) {
      ic = (ImageConsumer)e.nextElement();
      
      if(consumers.contains(ic))
	ic.setDimensions(width, height);
      if(consumers.contains(ic))
	ic.setProperties(properties);
      if(consumers.contains(ic))
	ic.setColorModel(colorModel);
      if(consumers.contains(ic)) {
	if(flag_complete.booleanValue())
	  ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
		      ImageConsumer.COMPLETESCANLINES |
		      ImageConsumer.SINGLEPASS |
		      ImageConsumer.SINGLEFRAME);
	else
	  ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
		      ImageConsumer.COMPLETESCANLINES);
      }
    }
    boolean in_loop = true;
    while(in_loop) {
      synchronized(flag_complete) {
	if(flag_update | flag_complete.booleanValue()) {
	  // fill out the pixels array
	  double[] location = slice.getLocation();
	  int[] startCoord = ArrayTypeConverter.arrayDoubleToInt(location);
	  int[] sliceSize = ArrayTypeConverter.arrayDoubleToInt(slice.getSize());
	  Object values = dataVolume.getValue(startCoord, sliceSize);
	  if (values == null) {
	    System.err.println(getClass().getName() + "required slice out of range of volume");
	  } else {
	    JavaType javaType = dataVolume.getType();
	    int[] pixels = NdArrayMath.scaleTo(values, 0,
					       ((IndexColorModel) colorModel).getMapSize() - 1, javaType);
	    for (e = v.elements(); e.hasMoreElements(); ) {
	      ic = (ImageConsumer)e.nextElement();  
	      if(consumers.contains(ic))
		ic.setPixels(0, 0, width, height, colorModel, pixels, 0, width);
	    }
	  }
	  
	  // Tell each image consumer that a frame or the 
	  // whole image has been delivered.
	  
	  for (e = v.elements(); e.hasMoreElements(); ) {
	    ic = (ImageConsumer)e.nextElement();
            if(consumers.contains(ic)) {
	      if(flag_complete.booleanValue()) {
	        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
	      }
	      else {
	        ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
	        thread.setPriority(Thread.MIN_PRIORITY);
	      }
            }
	  }
	  flag_update = false;
	  
	  if(flag_complete.booleanValue())
	    in_loop = false;
	} // end synchronized(flag_complete)
      }
    }
    thread = null;
  } // end SliceImageSource.run

  public void update(Observable o, Object arg) {
    if(o == dataVolume) {
      if(dataVolume.isComplete()) {
	synchronized (flag_complete) {
	  flag_complete = Boolean.TRUE;
	}
      }
      flag_update = true;
      if(thread != null) {
	thread.setPriority(Thread.NORM_PRIORITY);
      }
    }
  } // end SliceImageSource.update

} // end SliceImageSource

