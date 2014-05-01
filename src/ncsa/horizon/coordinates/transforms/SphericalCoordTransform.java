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
 *  96mar26  rlp  Original version;
 *  97jul15  rlp  added hasChanged() calls prior to all notifyObservers().
 */
package ncsa.horizon.coordinates.transforms;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.*;
import FITSWCS.SphericalTransform;
import FITSWCS.TrigD;
import FITSWCS.exceptions.*;

/**
 * an object for transforming positions from one spherical coordinate 
 * system to another by shifting the pole and rotating.  <p>
 *
 * In the documentation below, the terms "old" and "new" refer the to 
 * two systems relative to the direction of this transform; that is,
 * the forward() method transforms a position in the "old" system to 
 * a position in the new system. <p>
 *
 * This class is a Horizon wrapper around the FITSWCS.SphericalTransform
 * class.
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: SphericalCoordTransform.java,v 1.1 1997/08/07 07:43:42 rplante Exp $
 */
public class SphericalCoordTransform extends CoordTransform {

    protected SphericalTransform sph = null;
    protected String[] name = new String[2];

    /**
     * Specify the transformation between two spherical coordinate systems
     * using one of two descriptions:  "Dip-and-Spin" or "Pole-Positioning".
     * <dl>
     * <dt> Dip-and-Spin
     * <dd> The transformation is described as a two step process of first
     *      shifting or "dipping" the pole by some angle, and then rotating
     *      the system around it new pole by another angle.  
     * @param angle1    longitude in old system to dip the pole towards, 
     *                  in degrees
     * @param angle2    angle to dip the pole by, in degrees.
     * @param angle3    angle to rotate the system by after dipping.
     * <dt> Pole-Positioning
     * <dd> The transformation is described by giving the location of the 
     *      poles of the old and new systems with coordinates of the other
     *      system.
     * @param angle1    longitude of the new pole in the old system
     * @param angle2    latitude of the new pole in the old system
     * @param angle3    longitude of the old pole in the new system
     * </dl> <p>
     * @param dipAndSpin  true if the three angles given refer to the 
     *                    "dip-and-spin" method.
     */
    public SphericalCoordTransform(double angle1, double angle2, 
				   double angle3, boolean dipAndSpin)
    {
	try {
	    if (dipAndSpin) {

		// "Dip-and-Spin" specification:
		//    angle1 = longitude of dip
		//    angle2 = angle of dip
		//    angle3 = rotation angle after dip
		//
		double[] euler = { angle1, angle2, angle1-180.0-angle3, 
				   TrigD.cos(angle2), TrigD.sin(angle2) };
		while (euler[2] < 0) { euler[2] += 360.0; }

		sph = new SphericalTransform(euler);
	    }
	    else {

		// Pole specification:
		//    angle1 = longitude of new pole in old system
		//    angle2 = latitude of new pole in old system
		//    angle3 = longitude of old pole in new system
		//    
		sph = new SphericalTransform(angle1, angle2, 
					     angle3, true, 90.0);
	    }
	}
	catch (BadReferenceParameterException ex) {
	    throw new IllegalArgumentException(ex.getMessage());
	}

    }

    /**
     * Specify the transformation between two spherical coordinate systems
     * using the "pole-positioning" method.
     * @param newpolelong  longitude of the new pole in the old system
     * @param newpolelat   latitude of the new pole in the old system
     * @param oldpolelong  longitude of the old pole in the new system
     */
    public SphericalCoordTransform(double newpolelong, double newpolelat, 
				   double oldpolelong)
    { 
	this(newpolelong, newpolelat, oldpolelong, false);
    }

    /**
     * Specify the transformation between two spherical coordinate systems
     * using the "pole-positioning" method.  The longitude of the old pole
     * in the new system is assumed to be 180 degrees.
     * @param newpolelong  longitude of the new pole in the old system
     * @param newpolelat   latitude of the new pole in the old system
     */
    public SphericalCoordTransform(double newpolelong, double newpolelat)
    {
	try {
	    sph = new SphericalTransform(newpolelong, newpolelat);
	}
	catch (BadReferenceParameterException ex) {
	    throw new IllegalArgumentException(ex.getMessage());
	}
    }

    /**
     * Specify the transformation between two spherical coordinate systems
     * using a reference position of a point in the new system located 
     * else where along 0 degrees longitude other than the pole.
     * @param reflong  longitude of reference point in the old system, 
     *                 in degrees
     * @param reflat   latitude of reference point in the old system, 
     *                 in degrees
     * @param poleref  either the longitude or latitude of the old pole 
     *                 in the new system, in degrees; a value of 999.0 
     *                 indicates that this should be set to a default value.
     * @param islongpole true if poleref is a longitude;
     * @param theta0   latitude of reference point in the new system.
     */
    public SphericalCoordTransform(double reflong, double reflat,
				   double poleref, boolean islongpole,
				   double theta0) 
	throws IllegalArgumentException
    {
	try {
	    sph = new SphericalTransform(reflong, reflat, 
					 poleref, islongpole, theta0);
	}
	catch (BadReferenceParameterException ex) {
	    throw new IllegalArgumentException(ex.getMessage());
	}
    }

    /**
     * apply a forward tranform on an input position
     * @param position    an array giving the input position in the old system
     * @param axisIndices a 2-elementarray containing the indices of the 
     *                    position array that should be used in the 
     *                    tranformation.  The first index should be for the 
     *                    longitude axis and the second, the latitude axis.
     */
    public double[] forward(double[] position, int[] axisIndices) 
	throws TransformUndefinedException
    {
	if (axisIndices.length < 2) 
	    throw new TransformUndefinedException("SphericalCoordTransform: " +
						  "must operate on two axes");

	while(position[axisIndices[0]] < 0) position[axisIndices[0]] += 360.0;
	double[] lnglat = sph.fwd(position[axisIndices[0]],
				  position[axisIndices[1]]);
	position[axisIndices[0]] = lnglat[0];
	position[axisIndices[1]] = lnglat[1];

	return position;
    }

    /**
     * apply a forward tranform on an input position.  The first axis is 
     * assumed to be the longitude axis, and the second, the latitude axis
     * @param position    an array giving the input position in the old system
     */
    public double[] forward(double[] position) 
	throws TransformUndefinedException
    {
	if (position.length < 2) 
	    throw new TransformUndefinedException("SphericalCoordTransform: " +
						  "must operate on two axes");

	while(position[0] < 0) position[0] += 360.0;
	double[] lnglat = sph.fwd(position[0],
				  position[1]);
	position[0] = lnglat[0];
	position[1] = lnglat[1];

	return position;
    }

    /**
     * apply a reverse tranform on an input position
     * @param position    an array giving the input position in the new system
     * @param axisIndices a 2-element array containing the indices of the 
     *                    position array that should be used in the 
     *                    tranformation.  The first index should be for the 
     *                    longitude axis and the second, the latitude axis.
     */
    public double[] reverse(double[] position, int[] axisIndices) 
	throws TransformUndefinedException
    {
	if (axisIndices.length < 2) 
	    throw new TransformUndefinedException("SphericalCoordTransform: " +
						  "must operate on two axes");

	while(position[axisIndices[0]] < 0) position[axisIndices[0]] += 360.0;
	double[] lnglat = sph.rev(position[axisIndices[0]],
				  position[axisIndices[1]]);
	position[axisIndices[0]] = lnglat[0];
	position[axisIndices[1]] = lnglat[1];

	return position;
    }

    /**
     * apply a forward tranform on an input position.  The first axis is 
     * assumed to be the longitude axis, and the second, the latitude axis
     * @param position    an array giving the input position in the new system
     */
    public double[] reverse(double[] position) 
	throws TransformUndefinedException
    {
	if (position.length < 2) 
	    throw new TransformUndefinedException("SphericalCoordTransform: " +
						  "must operate on two axes");

	while(position[0] < 0) position[0] += 360.0;
	double[] lnglat = sph.rev(position[0],
				  position[1]);
	position[0] = lnglat[0];
	position[1] = lnglat[1];

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
     * by finding the first axes with types beginning with "longitude" and 
     * "latitude".
     */
    public CoordTransformConstraints determineConstraints(Metadata in,
							  boolean forwards) 
    {
	Integer longax = 
	    CoordMetadata.firstAxisMatchingType(in, 0, "longitude", 0, 9);
	Integer latax = 
	    CoordMetadata.firstAxisMatchingType(in, 0, "latitude", 0, 8);
	if (longax == null || latax == null) return null;

	int[] ail = { longax.intValue(), latax.intValue() };
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
	SphericalCoordTransform out = (SphericalCoordTransform) super.clone();
	out.sph = new SphericalTransform(sph.getEuler());
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
	} catch (TransformUndefinedException ex) { }

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

	return in;
    }

}




