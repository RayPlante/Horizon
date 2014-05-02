/**
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1998, Board of Trustees of the University of Illinois
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
 *-------------------------------------------------------------------------
 * History: 
 *  96dec10  rlp  Original version
 */
package ncsa.horizon.awt.image;

import ncsa.horizon.data.TransferFunction;
import ncsa.horizon.data.NumericTransferFunction;
import ncsa.horizon.data.NdArrayData;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.JavaType;
import ncsa.horizon.util.ArrayTypeConverter;

import java.awt.image.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * an ImageProducer that uses a TransferFunction to create images
 */
public class TransferFunctionImageSource extends SliceImageSource {

    /**
     * the transfer function to use
     */
    protected TransferFunction tf = null;

    public TransferFunctionImageSource(Slice slice, NdArrayData data) {
	this(slice, data, null);
    }

    public TransferFunctionImageSource(Slice slice, NdArrayData data,
					 ColorModel cm) 
    {
	super(slice, data, cm);
	JavaType type = data.getType(); 
// 	if (type != JavaType.DOUBLE && type != JavaType.FLOAT &&
// 	    type != JavaType.LONG   && type != JavaType.INT     ) return;
// 	tf = new NumericTransferFunction();
    }
					 
    public TransferFunctionImageSource(Slice slice, NdArrayData data,
				       ColorModel cm, TransferFunction f) 
    {
	super(slice, data, cm);
	tf = f;
    }

    public synchronized TransferFunction getTransferFunction() { return tf; }
    public synchronized void setTransferFunction(TransferFunction f) { 
	tf = f; 
    }

    public synchronized void run() {

	if (tf == null) {
	    super.run();
	    return;
	}

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

	// begin sending data
	boolean in_loop = true;
	while(in_loop) {

	    synchronized(flag_complete) {
		if(flag_update | flag_complete.booleanValue()) {

		    // fill out the pixels array
		    double[] location = slice.getLocation();
		    int[] startCoord = 
			ArrayTypeConverter.arrayDoubleToInt(location);
		    int[] sliceSize = 
			ArrayTypeConverter.arrayDoubleToInt(slice.getSize());

		    int[] pixels = getLevels(startCoord, sliceSize);

		  if (pixels == null) {
		      System.err.println(getClass().getName() + 
			       " required slice out of range of volume");
		  } else {
		    for (e = v.elements(); e.hasMoreElements(); ) {
			ic = (ImageConsumer)e.nextElement();  
			if(consumers.contains(ic))
			    ic.setPixels(0, 0, width, height, colorModel, 
					 pixels, 0, width);
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
		} 
	    }
	}
	thread = null;
    } 

    /**
     * map data to an array of integers using the attached TransferFunction
     */
    public int[] getLevels(int[] start, int[] sz) {
	JavaType type = dataVolume.getType();
	Object values = dataVolume.getValue(start, sz);
	if (values == null) return null;

	try {
	    return tf.getLevels(values, type);
	}
	catch (IllegalArgumentException ex) {
	    return null;
	}
    }
}
					 
