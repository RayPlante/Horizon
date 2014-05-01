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
import ncsa.horizon.util.Metavector;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.*;
import FITSWCS.Projection;
import FITSWCS.projections.*;
import FITSWCS.TrigD;
import FITSWCS.exceptions.*;

/**
 * an object that projects positions from a spherical coordinate 
 * system onto a rectilinear system.
 *
 * In the documentation below, the terms "old" and "new" refer the to 
 * two systems relative to the direction of this transform; that is,
 * the forward() method transforms a position in the "old" system to 
 * a position in the new system. <p>
 *
 * This class is a Horizon wrapper around the FITSWCS.Projection
 * class and its various sub-classes in FITSWCS.projections. <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: FITSProjectionCoordTransform.java,v 1.1 1997/08/07 07:43:42 rplante Exp $
 */
public class FITSProjectionCoordTransform extends CoordTransform {

    protected Projection proj = null;
    protected String[] name = new String[2];

    private String pcode = null;
    private double[] projparm = null;

    public FITSProjectionCoordTransform(String pcode, double[] projparm,
					String[] name) 
	throws IllegalArgumentException
    {
	setProjection(pcode, projparm);
	setName(name);
    }

    public FITSProjectionCoordTransform(String pcode, double[] projparm) 
	throws IllegalArgumentException
    {
	setProjection(pcode, projparm);
    }

    public FITSProjectionCoordTransform(String[] name) {
	setName(name);
    }

    public FITSProjectionCoordTransform() { }

    public void setProjection(String pcode, double[] projparm) 
	throws IllegalArgumentException
    {
	try {
	    proj = Projection.getProjection(pcode, projparm);
	}
	catch (UnsupportedProjectionException ex) {
	    throw new IllegalArgumentException("pcode: unrecognized as " +
					       "supported projection code");
	}
	catch (BadProjectionParameterException ex) {
	    throw new IllegalArgumentException("projparm: bad set of " +
					       "projection parameters for " + 
					       "requested projection");
	}

	this.pcode = pcode;
	this.projparm = new double[projparm.length];
	System.arraycopy(projparm, 0, this.projparm, 0, projparm.length);
    }

    /**
     * project a position on a sphere onto a plane
     * @param position    an array giving the position in the system of the
     *                    sphere
     * @param axisIndices a 2-elementarray containing the indices of the 
     *                    position array that should be used in the 
     *                    tranformation.  The first index should be for the 
     *                    longitude axis and the second, the latitude axis.
     */
    public double[] forward(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException
    {
	double[] lnglat;
	try {
	    lnglat = proj.fwd(position[axisIndices[0]], 
			      position[axisIndices[1]]);
	}
	catch (PixelBeyondProjectionException ex) {
	    throw new PositionBeyondDomainException(ex.getMessage());
	}

	position[axisIndices[0]] = lnglat[0];
	position[axisIndices[1]] = lnglat[1];

	return position;
    }

    /**
     * project a position on a sphere onto a plane.  The first axis is 
     * assumed to be the longitude axis, and the second, the latitude axis.
     * @param position    an array giving the position in the system of the
     *                    sphere
     */
    public double[] forward(double[] position) 
	throws PositionBeyondDomainException
    {
	double[] lnglat;
	try {
	    lnglat = proj.fwd(position[0], position[1]);
	}
	catch (PixelBeyondProjectionException ex) {
	    throw new PositionBeyondDomainException(ex.getMessage());
	}

	position[0] = lnglat[0];
	position[1] = lnglat[1];

	return position;
    }

    /**
     * de-project a position on a plane onto a sphere.  
     * @param position    an array giving the position in the coordinate system 
     *                    of the plane
     * @param axisIndices a 2-element array containing the indices of the 
     *                    position array that should be used in the 
     *                    tranformation.  The first index should be for the 
     *                    longitude axis and the second, the latitude axis.
     */
    public double[] reverse(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException
    {
	double[] xy;
	try {
	    xy = proj.rev( position[axisIndices[0]], position[axisIndices[1]] );
	}
	catch (PixelBeyondProjectionException ex) {
	    throw new PositionBeyondDomainException(ex.getMessage());
	}

	position[axisIndices[0]] = xy[0];
	position[axisIndices[1]] = xy[1];

	return position;
    }

    /**
     * de-project a position on a plane onto a sphere.  The first axis is 
     * assumed to be the longitude axis, and the second, the latitude axis.
     * @param position    an array giving the position in the coordinate system 
     *                    of the plane
     */
    public double[] reverse(double[] position) 
	throws PositionBeyondDomainException
    {
	double[] xy;
	try {
	    xy = proj.rev(position[0], position[1]);
	}
	catch (PixelBeyondProjectionException ex) {
	    throw new PositionBeyondDomainException(ex.getMessage());
	}

	position[0] = xy[0];
	position[1] = xy[1];

	return position;
    }

    /**
     * return the minimum number of axes that the forward transform operates 
     * on.  This value is equal to the minimum number of axes that results 
     * from the reverse transform.  This value is often equal to the that 
     * returned by getOutNaxes(), but is not required to.
     */
    public int getInNaxes() { return 2; }

    /**
     * return the minimum number of axes that results from the forward 
     * transform.  This value is equal to the minimum number of axes that 
     * the reverse transform operates on.  This value is often equal to the 
     * that returned by getInNaxes(), but is not required to.
     */
    public int getOutNaxes() { return 2; } 

    /**
     * return the maximum number of axes this transform operates on
     */
    public int getMaxNaxes() { return 2; }

    /**
     * make an educated guess as to the proper way to apply this transform
     * to a coordinate system with the specified Metadata.  This is done 
     * by finding the first axes with types matching "longitude" and 
     * "latitude".
     */
    public CoordTransformConstraints determineConstraints(Metadata in,
							  boolean forwards) 
    {
	int i;
	int[] ail = new int[2];

	if (forwards) {
	    if (proj == null) return null;

	    Integer longax = 
		CoordMetadata.firstAxisMatchingType(in, "longitude");
	    Integer latax = 
		CoordMetadata.firstAxisMatchingType(in, "latitude");
	    if (longax == null || latax == null) return null;

	    ail[0] = longax.intValue();
	    ail[1] = latax.intValue();
	}
	else {
	    Integer[] linaxes = 
		CoordMetadata.axesMatchingType(in, "linear", 0);
	    if (linaxes == null || linaxes.length < 2) return null;

// 	    // check the projection type
// 	    String projtype;
// 	    try {
// 		projtype = (String) in.getMetadatum("projection");
// 	    } catch (ClassCastException ex) { projtype = null; }

// 	    // if projection type not found make sure we have one in place 
// 	    // already
// 	    if (projtype == null && proj == null) return null;

// 	    // if projection type found, make sure it's one we recognize
// 	    if (projtype != null) {
// 		for (i=1; i < ProjectionType.NTYPES && 
// 			  ! projtype.equalsIgnoreCase(ProjectionType.code[i]); 
// 		     i++);
// 		if (i >= ProjectionType.NTYPES) return null;
// 	    }

	    if (proj == null) return null;

	    ail[0] = linaxes[0].intValue();
	    ail[1] = linaxes[1].intValue();
	}

	return new CoordTransformConstraints(ail, forwards);
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
	String[] out = new String[2];
	System.arraycopy(name, 0, out, 0, 2);
	return out;
    }

    /**
     * create a copy of this Transform
     */
    public synchronized Object clone() {
	FITSProjectionCoordTransform out = 
	    (FITSProjectionCoordTransform) super.clone();
	if (proj != null) out.setProjection(pcode, projparm);
	out.setName(name);
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

	try {
	    Integer N = (Integer) in.getMetadatum("naxes");
	    n = (N == null) ? 0 : N.intValue();
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("naxes: not of Integer type");
	}
	if (n <= 0) return in;

	double[] rv = new double[n];

	// get the values for the reference value and stepsize
	for(i=0; i < n; i++) {
	    try {
		R = (Double) in.getMetadatum("Axes[" + i + "].refvalue");
		rv[i] = (R == null) ? 0.0 : R.doubleValue();
	    } catch (ClassCastException ex) {
		throw new CorruptedMetadataException("refvalue: not of " +
						     "Double type");
	    }
	}

	// determine the new reference value by sending it through
	// the transform
	try {
	    if (forward) {
		rv = forward(rv, axisIndices);
	    }
	    else {
		rv = reverse(rv, axisIndices);
	    }
	} catch (PositionBeyondDomainException ex) { }

	// Now update with the new values
	for(i=0; i < axisIndices.length; i++) {
	    if (axisIndices[i] < n) {
		CoordMetadata.setAxisRefvalue(in, axisIndices[i],  
					      rv[axisIndices[i]] );
		// update the axis name if necessary
		if (name[i] != null) 
		    CoordMetadata.setAxisName(in, axisIndices[i], name[i]);
	    }
	}

	// Now update the projection type
	if (! forward) {
	    in.put("projection", pcode);
	    if (projparm != null) {
		Metavector Pparms = new Metavector(projparm.length);
		for(i=0; i < projparm.length; i++) 
		    Pparms.addElement(new Double(projparm[i]));
		in.put("Projparameters", Pparms);
	    }
	}

	return in;
    }

}
