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
 *  97mar26  rlp  Original version;
 *  97jul15  rlp  added hasChanged() calls prior to all notifyObservers().
 */
package ncsa.horizon.coordinates.transforms;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.MetadataTypeException;
import ncsa.horizon.coordinates.*;

/**
 * an object for transforming positions from one coordinate system to 
 * another via a linear shift and scaling.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: LinearCoordTransform.java,v 1.1 1997/08/07 07:43:42 rplante Exp $
 */
public class LinearCoordTransform extends CoordTransform {

    protected int nop=0;
    protected double[] refpos;
    protected double[] refval;
    protected double[] offset;
    protected double[] stepsize;
    protected String[] name;

    /**
     * create a LinearCoordTransform object with default values.
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     */
    public LinearCoordTransform(int naxes) {
	this(naxes, null, null, null, null, null);
    }

    /**
     * create a LinearCoordTransform object with default values.
     */
    public LinearCoordTransform() {
	this(2, null, null, null, null, null);
    }

    /**
     * create a LinearCoordTransform object specifying all internal
     * data.  Missing values in the input array are set to default
     * values.
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     * @param refpos   the position in the "input" system (the system
     *                 forward() transforms from) that is equal to the
     *                 the refval position.  Default equals 0.
     * @param refval   the position in the "output" system (the system
     *                 forward() transforms to) that is equal to the
     *                 the refpos position.  Default equals 0.
     * @param offset   An extra offset that gets added to the reference
     *                 position.  Default equals 0;
     * @param stepsize the size of a voxel of the "output" system (the
     *                 system that forward() transforms to) in units of 
     *                 the "input" system
     */
    public LinearCoordTransform(int naxes, double[] refpos, double[] refval,
				double[] offset, double[] stepsize) 
    {
	this(naxes, refpos, refval, offset, stepsize, null);
    }

    /**
     * create a LinearCoordTransform object specifying all internal
     * data.  Missing values in the input array are set to default
     * values.
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     * @param refpos   the position in the "input" system (the system
     *                 forward() transforms from) that is equal to the
     *                 the refval position.  Default equals 0.
     * @param refval   the position in the "output" system (the system
     *                 forward() transforms to) that is equal to the
     *                 the refpos position.  Default equals 0.
     * @param offset   An extra offset that gets added to the reference
     *                 position.  Default equals 0;
     * @param stepsize the size of a voxel of the "output" system (the
     *                 system that forward() transforms to) in units of 
     *                 the "input" system
     * @param names    names to assign to transformed axes.  A null element
     *                 of null array means do not reassign name.  These 
     *                 values only affect the behavior of getMetadata();
     */
    public LinearCoordTransform(int naxes, double[] refpos, double[] refval,
				double[] offset, double[] stepsize, 
				String[] names) 
    {
	if (naxes <= 0) throw new 
	    ArrayIndexOutOfBoundsException("naxes = " + naxes + " <= 0");
	nop = naxes;

	double[][] inarrays = { refpos, refval, offset, stepsize };
	double[][] outarrays = new double[4][];
	double[] defs = { 0.0, 0.0, 0.0, 1.0 };
	int l, j;
	for(int i=0; i < inarrays.length; i++) {
	    outarrays[i] = new double[nop];

	    if (inarrays[i] != null) {
		l = (nop < inarrays[i].length) ? nop : inarrays[i].length;
		System.arraycopy(inarrays[i], 0, outarrays[i], 0, l);
	    }
	    else {
		l = 0;
	    }
	    for(j=l; j < nop; j++) outarrays[i][j] = defs[i];
	}

	this.refpos = outarrays[0];
	this.refval = outarrays[1];
	this.offset = outarrays[2];
	this.stepsize = outarrays[3];

	name = new String[nop];
	if (names != null) {
	    for(j=0; j < names.length && j < nop; j++) name[j] = names[j];
	}
    }

    /** 
     * create a linear transform based on the specified metadata using the 
     * horizon schema.
     */
    public LinearCoordTransform(Metadata md) 
	throws IllegalTransformException
    {
	resetWarn();
	try {
	    Integer N = (Integer) md.getMetadatum("naxes");
	    nop = (N == null) ? 0 : N.intValue();
	} catch (ClassCastException ex) {
	    checkSchema(md);
	    throw new MetadataTypeException("naxes", "Integer");
	}
	if (nop <= 0) {
	    checkSchema(md);
	    throw new IllegalTransformException("naxes = " + nop + " <= 0");
	}

	// get axis names
	String[] names = CoordMetadata.getAxisNames(md);
	name = new String[nop];
	for(int i=0; i < nop; i++) {
	    name[i] = (i < names.length) ? names[i] : "unknown";
	}

	// Now get coordinate definition values
	String[] mdnm = { "refposition", "refoffset", "refvalue", "stepsize" };
	double[] defd = { 0.0, 0.0, 0.0, 1.0 };
	double[][] val = new double[4][];
	for(int i=0; i < 4; i++) {
	    val[i] = new double[nop];
	    for(int j=0; j < nop; j++) {
		try {
		    Double d = (Double) md.getMetadatum("Axes[" + j + "]." + 
							mdnm[i]);
		    val[i][j] = (d != null) ? d.doubleValue(): defd[i];
		} catch (ClassCastException ex) {
		    checkAxSchema(md, nop);
		    throw new 
			MetadataTypeException("Axes[" + j + "]." + mdnm[i],
					      "Double");
		}
	    }
	}
	refpos = val[0];
	offset = val[1];
	refval = val[2];
	stepsize = val[3];
	
    }

    Boolean uzngh = null;
    boolean checkSchema(Metadata md) {
	if (uzngh != null) return uzngh.booleanValue();

	uzngh = new Boolean(CoordMetadata.usesHorizonSchema(md));
	if (! uzngh.booleanValue()) 
	    System.err.println("Warning: Metadata object is not using " +
			       "Horizon schema");
	return uzngh.booleanValue();
    }

    Boolean rfdax = null;
    boolean checkAxSchema(Metadata md, int nx) {
	if (rfdax != null) return (rfdax.booleanValue());

	checkSchema(md);
	StringBuffer axlist = new StringBuffer();
	for(int i=0; i < nx; i++) {
	    if (! CoordMetadata.usesReferencedAxis(md, i)) 
		axlist.append(" " + i);
	}
	rfdax = new Boolean(axlist.length() == 0);
	if (! rfdax.booleanValue()) 
	    System.err.println("Warning: metadata object contains axes " + 
                               "using axis schema other than referenced: " + 
			       axlist);
	return rfdax.booleanValue();
    }

    void resetWarn() {  uzngh = null;  rfdax = null; }

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
	int i, maxax=-1;

	for(i=0; i < axisIndices.length; i++) 
	    if (axisIndices[i] > maxax) maxax = axisIndices[i];
	if (maxax > position.length-1) {
	    double[] newpos = new double[maxax+1];
	    System.arraycopy(position, 0, newpos, 0, position.length);
	    position = newpos;
	}

	for(i=0; i < axisIndices.length && i < nop; i++) {
	    position[axisIndices[i]] = refval[i] + 
		(position[axisIndices[i]] - refpos[i] - offset[i]) * 
		stepsize[i];
	}

	return position;
    }

    /**
     * apply a forward tranform on an input position. 
     * @param position    an array giving the input position to transform
     */
    public synchronized double[] forward(double[] position) {
	for(int i=0; i < position.length && i < nop; i++) {
	    position[i] = refval[i] + 
		(position[i] - refpos[i] - offset[i]) * stepsize[i];
	}

	return position;
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
	int i, maxax=-1;

	for(i=0; i < axisIndices.length; i++) 
	    if (axisIndices[i] > maxax) maxax = axisIndices[i];
	if (maxax > position.length-1) {
	    double[] newpos = new double[maxax+1];
	    System.arraycopy(position, 0, newpos, 0, position.length);
	    position = newpos;
	}

	for(i=0; i < axisIndices.length && i < nop; i++) {
	    position[axisIndices[i]] = 
		(position[axisIndices[i]] - refval[i]) / stepsize[i] 
		+ refpos[i] + offset[i];
	}

	return position;
    }

    /**
     * apply a reverse tranform on an input position.  A list of axis indices
     * is assumed (usually { 0, ... getOutNAxes()-1 }).
     * @param position    an array giving the input position to transform
     */
    public synchronized double[] reverse(double[] position) {
	for(int i=0; i < position.length && i < nop; i++) {
	    position[i] = (position[i] - refval[i]) / stepsize[i] 
		+ refpos[i] + offset[i];
	}

	return position;
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
    public int getMaxNaxes() { return nop; }

    /**
     * set the maximum number of axes this transform operates on
     */
    public synchronized void setMaxNaxes(int naxes) 
	throws ArrayIndexOutOfBoundsException
    { 
	if (naxes <= 0) throw new 
	    ArrayIndexOutOfBoundsException("naxes = " + naxes + " <= 0");
	nop = naxes; 
	double[][] inarrays = { refpos, refval, offset, stepsize };
	double[][] outarrays = new double[4][];
	double[] defs = { 0.0, 0.0, 0.0, 1.0 };
	int l, j;
	for(int i=0; i < inarrays.length; i++) {
	    outarrays[i] = new double[nop];

	    if (inarrays[i] != null) {
		l = (nop < inarrays[i].length) ? nop : inarrays[i].length;
		System.arraycopy(inarrays[i], 0, outarrays[i], 0, l);
	    }
	    else {
		l = 0;
	    }
	    for(j=l; j < nop; j++) outarrays[i][j] = defs[i];
	}

	this.refpos = outarrays[0];
	this.refval = outarrays[1];
	this.offset = outarrays[2];
	this.stepsize = outarrays[3];

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
	refval[axis] = in;
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
	return refval[axis];
    }

    /**
     * set the axis reference value for each axis.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param in   values to be set; missing or extra values are ignored
     */
    public synchronized void setRefvalue(double[] in) 
    {
	System.arraycopy(in, 0, refval, 0, 
		 (in.length < refval.length) ? in.length : refval.length);
	hasChanged();
	notifyObservers();
    }

    /**
     * return a copy of the axis reference values for all axes.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     */
    public synchronized double[] getRefvalue() {
	double[] out = new double[nop];
	System.arraycopy(refval, 0, out, 0, nop);
	return out;
    }

    /**
     * set the axis reference position  the specified axis.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized void setRefposition(int axis, double in) 
	throws ArrayIndexOutOfBoundsException
    {
	refpos[axis] = in;
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis reference position for the specified axis.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     */
    public synchronized double getRefposition(int axis) 
	throws ArrayIndexOutOfBoundsException
    {
	return refpos[axis];
    }

    /**
     * set the axis reference position for each axis.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param in   values to be set
     */
    public synchronized void setRefposition(double[] in) 
    {
	System.arraycopy(in, 0, refpos, 0, 
		 (in.length < refpos.length) ? in.length : refpos.length);
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis reference position for all the axes.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     */
    public synchronized double[] getRefposition() 
	throws ArrayIndexOutOfBoundsException
    {
	double[] out = new double[nop];
	System.arraycopy(refpos, 0, out, 0, nop);
	return out;
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
	stepsize[axis] = in;
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis step size for the specified axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to get (first axis has index 0)
     */
    public synchronized double getStepsize(int axis) 
	throws ArrayIndexOutOfBoundsException
    {
	return stepsize[axis];
    }

    /**
     * set the axis step size for each axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param in   values to be set
     */
    public synchronized void setStepsize(double[] in) 
    {
	System.arraycopy(in, 0, stepsize, 0, 
		 (in.length < stepsize.length) ? in.length : stepsize.length);
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis step size for each axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized double[] getStepsize() 
	throws ArrayIndexOutOfBoundsException
    {
	double[] out = new double[nop];
	System.arraycopy(stepsize, 0, out, 0, nop);
	return out;
    }

    /**
     * set the axis reference offset for the specified axis.  An offset
     * equal to zero indicates that the reference position refers to the 
     * center of the reference voxel.  -0.5 indicates that the position 
     * corresponds to the beginning of the voxel; +0.5, the end.  If the 
     * reference voxel assumes that the first voxel has an index of 1, an 
     * extra 1 should be subtracted from the reference offset.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public synchronized void setRefoffset(int axis, double in) 
	throws ArrayIndexOutOfBoundsException
    {
	offset[axis] = in;
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis reference offset for the specified axis.  
     * @param axis the index of the axis to be set (first axis has index 0)
     */
    public synchronized double getRefoffset(int axis) 
	throws ArrayIndexOutOfBoundsException
    {
	return offset[axis];
    }

    /**
     * set the axis reference offset for each axis.  An offset
     * equal to zero indicates that the reference position refers to the 
     * center of the reference voxel.  -0.5 indicates that the position 
     * corresponds to the beginning of the voxel; +0.5, the end.  If the 
     * reference voxel assumes that the first voxel has an index of 1, an 
     * extra 1 should be subtracted from the reference offset.
     * @param in   values to be set
     */
    public synchronized void setRefoffset(double[] in) 
    {
	System.arraycopy(in, 0, offset, 0, 
		 (in.length < offset.length) ? in.length : offset.length);
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis reference offset for each axis.  
     */
    public synchronized double[] getRefoffset() 
	throws ArrayIndexOutOfBoundsException
    {
	double[] out = new double[nop];
	System.arraycopy(offset, 0, out, 0, nop);
	return out;
    }

    /**
     * set the axis name to be given to a transformed axis.  Note that
     * this name is only used by the getMetadatum() method and is applied
     * regardless of the value of the forward argument to that method.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   the name to give to the axis, null means use the name of
     *             the untransformed axis
     */
    public synchronized void setName(int axis, String in) 
	throws ArrayIndexOutOfBoundsException
    {
	name[axis] = in;
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis name to be given to a transformed axis, or null
     * if the name is not updated by getMetadata().
     * @param axis the index of the axis (first axis has index 0)
     */
    public synchronized String getName(int axis) 
	throws ArrayIndexOutOfBoundsException
    {
	return name[axis];
    }

    /**
     * set the axis names to be given to each transformed axis.  Note that
     * these names are only used by the getMetadatum() method and are applied
     * regardless of the value of the forward argument to that method.
     * @param in   values to be set
     */
    public synchronized void setName(String[] in) 
    {
	System.arraycopy(in, 0, name, 0, 
		 (in.length < name.length) ? in.length : name.length);
	hasChanged();
	notifyObservers();
    }

    /**
     * return the axis name to be given to a transformed axis.  Null values
     * in the output array indicate that the name for that axis will not be
     * updated by getMetadata().
     */
    public synchronized String[] getName() 
	throws ArrayIndexOutOfBoundsException
    {
	String[] out = new String[nop];
	System.arraycopy(name, 0, out, 0, nop);
	return out;
    }

    /**
     * create a copy of this Transform
     */
    public synchronized Object clone() {
	LinearCoordTransform out = (LinearCoordTransform) super.clone();
	System.arraycopy(refval, 0, out.refval, 0, refval.length);
	System.arraycopy(refpos, 0, out.refpos, 0, refpos.length);
	System.arraycopy(offset, 0, out.offset, 0, offset.length);
	System.arraycopy(stepsize, 0, out.stepsize, 0, stepsize.length);
	System.arraycopy(name, 0, out.name, 0, name.length);
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
	int i, n;
	Double R;

	super.getMetadata(in, forward, axisIndices);

	resetWarn();
	try {
	    Integer N = (Integer) in.getMetadatum("naxes");
	    n = (N == null) ? 0 : N.intValue();
	} catch (ClassCastException ex) {
	    checkSchema(in);
	    throw new MetadataTypeException("naxes", "Integer");
	}
	if (n <= 0) { checkSchema(in); return in; }

	double[] rv = new double[n];
	double[] ss = new double[n];

	// get the values for the reference value and stepsize
	for(i=0; i < n; i++) {
	    try {
		R = (Double) in.getMetadatum("Axes[" + i + "].refvalue");
		rv[i] = (R == null) ? 0.0 : R.doubleValue();
		R = (Double) in.getMetadatum("Axes[" + axisIndices[i] + 
					     "].stepsize");
		ss[i] = (R == null) ? 1.0 : R.doubleValue();
	    } catch (ClassCastException ex) {
		checkAxSchema(in, nop);
		throw new MetadataTypeException("Axis metadatum " +
						"(refvalue/stepsize): " +
						"not of Double type");
	    }
	}

	// determine the new reference value by sending it through
	// the transform; adjust the stepsize.
	if (forward) {

	    rv = forward(rv, axisIndices);
	    for(i=0; i < axisIndices.length; i++) 
		if (axisIndices[i] < n) ss[axisIndices[i]] *= stepsize[i];
	}
	else {

	    rv = reverse(rv, axisIndices);
	    for(i=0; i < axisIndices.length; i++) 
		if (axisIndices[i] < n) ss[axisIndices[i]] /= stepsize[i];
	}

	// Now update with the new values
	for(i=0; i < axisIndices.length; i++) {
	    if (axisIndices[i] < n) {
		CoordMetadata.setAxisRefvalue(in, axisIndices[i],  
					      rv[axisIndices[i]] );
		CoordMetadata.setAxisStepsize(in, axisIndices[i], 
					      ss[axisIndices[i]] );

		// update the axis name if necessary
		if (name[i] != null) 
		    CoordMetadata.setAxisName(in, axisIndices[i], name[i]);

	    }
	}

	return in;
    }

}


    
