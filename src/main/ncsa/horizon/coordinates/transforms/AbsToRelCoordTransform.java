/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-7, Board of Trustees of the University of Illinois
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
 *-------------------------------------------------------------------------
 * History: 
 *  97jul14  rlp  Original version;
 */
package ncsa.horizon.coordinates.transforms;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.MetadataTypeException;
import ncsa.horizon.coordinates.*;

/**
 * an object for transforming positions from absolute positions to positions 
 * relative to a reference position. <p>
 *
 * See the documentation for the super class, 
 * <a href="ncsa.horizon.coordinates.CoordTransform.html">CoordTransform</a>,
 * for a general description of how a transform is used.  <p>
 *
 * The transform applied by this class is governed by two basic pieces of 
 * data: a reference position and a stepsize.  The stepsize, a scale factor 
 * to be applied to width of a voxel, is usually left to its default value 
 * of 1.0.  The reference position is the absolute position that the relative 
 * positions are referred to; that is, the reference position in the old 
 * system will be shifted to 0.0 in the new, transformed system.  <p>
 *
 * When one instantiates this class, one usually indicates whether the 
 * transform can adapt its reference position and stepsize according to the
 * <code>CoordinateSystem</code> it gets attached to.  If this transform
 * is adaptable (e.g. willAdapt() returns true), then when the transform is 
 * attached to a <code>CoordinateSystem</code>, it will change its reference
 * position to that found in the system's metadata, and the stepsize will
 * be set to 1.0.  If it is not adaptable, it will use the reference position
 * and stepsize set at construction.  <p>
 *
 * For example, if one wants to make a CoordinateSystem give positions in 
 * relative coordinates, one can:
 * <pre>
 *     CoordinateSystem csys;
 *     int numaxes;
 *     ...             // csys and numaxes are set
 *     CoordTransform t = new AbsToRelCoordTransform(numaxes);
 *     csys.attachTransform(t);
 * </pre>
 * The <code>csys</code> system will now print out its coordinate positions 
 * relative to its native reference position.  However, if we want positions
 * be be relative to some other reference, we can instead:
 * <pre>
 *     double[] ref = { 20.0, 31.53, -15.8 };
 *     CoordTransform t = new AbsToRelCoordTransform(ref.length, ref, false);
 *     csys.attachTransform(t); 
 * </pre>
 * Now suppose the system already prints out relative coordinate positions;
 * We can change the positions to absolute ones by attaching this transform
 * in reverse:
 * <pre>
 *     double[] ref = { 20.0, 31.53, -15.8 };
 *     CoordTransform t = new AbsToRelCoordTransform(ref.length, ref, false);
 *     csys.attachTransform(t, false); 
 * </pre>
 * In this case, we needed to provide the reference position, since the 
 * native reference (if the positions really are relative coordinates) is 
 * likely to be zero.  However, it may not be, and you can allow the transform
 * use that reference when attached in reverse:
 * <pre>
 *     CoordTransform t = new AbsToRelCoordTransform(numaxes, null, true);
 *     csys.attachTransform(t, false); 
 * </pre>
 * This will cause the zero position in the native system to be shifted to
 * the native reference position; in addition, -1.0 will be used as the 
 * stepsize.  This turns about to be exactly equivalent to our first example. 
 * <p>
 *
 * @author Raymond Plante
 * @author the Horizon Java Team 
 * @version $Id: AbsToRelCoordTransform.java,v 1.1 1997/08/07 07:43:42 rplante Exp $
 */
public class AbsToRelCoordTransform extends CoordTransform {

    /**
     * if true, this transform--when attached to a CoordinateSystem--will
     * replace its reference position data with that of the CoordinateSystem 
     * being attached to.
     */
    protected boolean adapt = true;
    protected LinearCoordTransform delegate;

    /**
     * create a AbsToRelCoordTransform object with default values, 
     * assuming it will operate on up to 2 axes.  By
     * default, willAdapt() will return true.
     */
    public AbsToRelCoordTransform() {
	delegate = new LinearCoordTransform(2);
    }

    /**
     * create a AbsToRelCoordTransform object with default values.  By
     * default, willAdapt() will return true.
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     */
    public AbsToRelCoordTransform(int naxes) {
	delegate = new LinearCoordTransform(naxes);
    }

    /**
     * create a AbsToRelCoordTransform object specifying all internal
     * data.  Missing values in the input array are set to zero
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     * @param refval   the position in the "output" system (the system
     *                 forward() transforms to) that is equal to the
     *                 the refpos position.  Default equals 0.
     * @param adapt    if false, these values should not be updated when
     *                 attached to a CoordinateSystem.  (If one intends
     *                 intend to attach this AbsToRelCoordTransform to a 
     *                 CoordinateSystem, this value will not be used.)
     *                 
     */
    public AbsToRelCoordTransform(int naxes, double[] refval, boolean adapt)
    {
	this(naxes, refval, null, adapt);
    }

    /**
     * create a AbsToRelCoordTransform object specifying all internal
     * data.  Missing values in the input arrays are set to default
     * values: 0.0 for refval and 1.0 for stepsize.  
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     * @param refval   the position in the "output" system (the system
     *                 forward() transforms to) that is equal to the
     *                 the refpos position.  Default equals 0.
     * @param stepsize the size of a voxel of the "output" system (the
     *                 system that forward() transforms to) in units of 
     *                 the "input" system
     * @param adapt    if false, these values should not be updated when
     *                 attached to a CoordinateSystem.  (If one intends
     *                 intend to attach this AbsToRelCoordTransform to a 
     *                 CoordinateSystem, this value will not be used.)
     *                 
     */
    public AbsToRelCoordTransform(int naxes, double[] refval, 
				  double[] stepsize, boolean adapt)
    {
	delegate = new LinearCoordTransform(naxes, null, refval, 
					    null, stepsize);
    }

    /** 
     * create a linear transform based on the specified metadata using the 
     * horizon schema.
     */
    public AbsToRelCoordTransform(Metadata md) 
	throws IllegalTransformException
    {
	delegate = new LinearCoordTransform(md);
	int nop = delegate.getMaxNaxes();
	for(int i=0; i < nop; i++) {
	    delegate.setRefposition(i, 0.0);
	    delegate.setRefoffset(i, 0.0);
	    delegate.setName(i, null);
	}
    }

    /**
     * apply a forward tranform on an input position.  
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public synchronized double[] forward(double[] position, int[] axisIndices) 
    {
	return delegate.reverse(position, axisIndices);
    }

    /**
     * apply a forward tranform on an input position. 
     * @param position    an array giving the input position to transform
     */
    public synchronized double[] forward(double[] position) {
	return delegate.reverse(position);
    }

    /**
     * apply a reverse tranform on an input position
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public synchronized double[] reverse(double[] position, int[] axisIndices) 
    {
	return delegate.forward(position, axisIndices);
    }

    /**
     * apply a reverse tranform on an input position.  A list of axis indices
     * is assumed (usually { 0, ... getOutNAxes()-1 }).
     * @param position    an array giving the input position to transform
     */
    public synchronized double[] reverse(double[] position) {
	return delegate.forward(position);
    }

    /**
     * return the minimum number of axes that the forward transform operates 
     * on.  This value is equal to the minimum number of axes that results 
     * from the reverse transform.  This value is often equal to the that 
     * returned by getOutNaxes(), but is not required to.
     */
    public int getInNaxes() { return 0; }

    /**
     * return the minimum number of axes that results from the forward 
     * transform.  This value is equal to the minimum number of axes that 
     * the reverse transform operates on.  This value is often equal to the 
     * that returned by getInNaxes(), but is not required to.
     */
    public int getOutNaxes() { return 0; } 

    /**
     * return the maximum number of axes this transform operates on
     */
    public int getMaxNaxes() { return delegate.getMaxNaxes(); }

    /**
     * set the maximum number of axes this transform operates on
     */
    public synchronized void setMaxNaxes(int naxes) 
	throws ArrayIndexOutOfBoundsException
    { 
	try {
	    delegate.setMaxNaxes(naxes);
	}
	catch (ArrayIndexOutOfBoundsException ex) {
	    throw new ArrayIndexOutOfBoundsException(ex.getMessage());
	}
	hasChanged();
	notifyObservers();
    }
    
    /**
     * set the axis reference value for the specified axis.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized void setRefvalue(int axis, double in) 
	throws ArrayIndexOutOfBoundsException
    {
	try {
	    delegate.setRefvalue(axis, in);
	}
	catch (ArrayIndexOutOfBoundsException ex) {
	    throw new ArrayIndexOutOfBoundsException(ex.getMessage());
	}
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis reference value for the specified axis.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     */
    public synchronized double getRefvalue(int axis) 
	throws ArrayIndexOutOfBoundsException
    {
	try {
	    return delegate.getRefvalue(axis);
	}
	catch (ArrayIndexOutOfBoundsException ex) {
	    throw new ArrayIndexOutOfBoundsException(ex.getMessage());
	}
    }

    /**
     * set the axis reference value for each axis.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param in   values to be set; missing or extra values are ignored
     */
    public synchronized void setRefvalue(double[] in) 
    {
	delegate.setRefvalue(in);
	hasChanged();
	notifyObservers();
    }

    /**
     * return a copy of the axis reference values for all axes.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     */
    public synchronized double[] getRefvalue() {
	return delegate.getRefvalue();
    }

    /**
     * set the axis step size for the specified axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized void setStepsize(int axis, double in) 
	throws ArrayIndexOutOfBoundsException
    {
	try {
	    delegate.setStepsize(axis, in);
	}
	catch (ArrayIndexOutOfBoundsException ex) {
	    throw new ArrayIndexOutOfBoundsException(ex.getMessage());
	}
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis step size for the specified axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     */
    public synchronized double getStepsize(int axis) 
	throws ArrayIndexOutOfBoundsException
    {
	try {
	    return delegate.getStepsize(axis);
	}
	catch (ArrayIndexOutOfBoundsException ex) {
	    throw new ArrayIndexOutOfBoundsException(ex.getMessage());
	}
    }

    /**
     * set the axis step size for each axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized void setStepsize(double[] in) 
    {
	delegate.setStepsize(in);
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis step size for each axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized double[] getStepsize() {
	return delegate.getStepsize();
    }

    /**
     * create a copy of this Transform
     */
    public synchronized Object clone() {
	AbsToRelCoordTransform out = (AbsToRelCoordTransform) super.clone();
	out.delegate = (LinearCoordTransform) delegate.clone();
	return out;
    }

    /**
     * update the input Metadata object to reflect the changes that this
     * tranform makes to a coordinate position.  In general, this method 
     * will actually edit the contents of the input Metadata when changes
     * are necessary.  
     */
    public synchronized Metadata getMetadata(Metadata in, boolean forward, 
					     int[] axisIndices) 
    { 
	Metadata out = in;

	if (adapt) {

	    // we should use the reference position information stored
	    // in the metadata object to derive the parameters of this
	    // transformation
	    int n;
	    delegate.resetWarn();
	    try {
		Integer N = (Integer) in.getMetadatum("naxes");
		n = (N == null) ? 0 : N.intValue();
	    } catch (ClassCastException ex) {
		delegate.checkSchema(in);
		throw new MetadataTypeException("naxes", "Integer");
	    }
	    if (n <= 0) { delegate.checkSchema(in); return in; }

	    // set the the reference value for the requested axes
	    applyNamesAndFormatters(in, forward, axisIndices);
	    if (forward) {
		for(int i=0; i < axisIndices.length; i++) 
		    CoordMetadata.setAxisRefvalue(in,axisIndices[i],0.0);
	    }
	}
	else {

	    // convert the reference position info in the given Metadata 
	    // list via our internal paramters
	    out = delegate.getMetadata(in, !forward, axisIndices);
	}

	return out;
    }

    /**
     * set whether this transform should adapt its internal data 
     * according to the reference position data of the CoordinateSystem
     * it gets attached to.  If the input is true, then when this transform
     * is attached to a CoordinateSystem object, it will ignore its own
     * reference position information; instead, it will look up the 
     * current reference position of the system and then reset its internal
     * parameters to convert positions in that system to positions relative
     * to that reference position.
     */
    public void setToAdapt(boolean doadapt) {
	adapt = doadapt;
    }

    /**
     * return whether the data internal to this object will get update
     * upon attachment to a CoordinateSystem.
     */
    public boolean willAdapt() { return adapt; }

    /**
     * initialize this transform according to the system it is to be
     * applied to.  This method is usually called by a CoordinateSystem 
     * object when the transform is attached to it.  
     * @exception IllegalTransformException if the csys has a non-positive
     *                                      number of axes
     */
    public void init(CoordinateSystem csys, boolean forward, 
		     int[] axisIndices) 
	throws IllegalTransformException
    { 
	if (adapt) {
	    Metadata md = csys.getMetadata();
	    int i, axis, n;
	    Double R;

	    delegate.resetWarn();
	    try {
		Integer N = (Integer) md.getMetadatum("naxes");
		n = (N == null) ? 0 : N.intValue();
	    } catch (ClassCastException ex) {
		delegate.checkSchema(md);
		throw new MetadataTypeException("naxes", "Integer");
	    }
	    if (n <= 0) {
		delegate.checkSchema(md);
		throw new IllegalTransformException("naxes = " + n + " <= 0");
	    }

	    // get the values for the reference value 
	    double[] rv = new double[n];
	    for(i=0; i < n; i++) {
		try {
		    R = (Double) md.getMetadatum("Axes[" + i + "].refvalue");
		    rv[i] = (R == null) ? 0.0 : R.doubleValue();
		}
		catch (ClassCastException ex) {
		    delegate.checkAxSchema(md, n);
		    throw new MetadataTypeException("refvalue", "Double");
		}
	    }

	    // now set our internal data
	    if (axisIndices.length > delegate.getMaxNaxes()) 
		setMaxNaxes(axisIndices.length);
	    for(i=0; i < axisIndices.length; i++) {
		axis = axisIndices[i];
		delegate.setStepsize(i, ( (forward)  ? 1.0      : -1.0 ));
		delegate.setRefvalue(i, ( (axis < n) ? rv[axis] :  0.0 ));
	    }
	}
    }

}


    
