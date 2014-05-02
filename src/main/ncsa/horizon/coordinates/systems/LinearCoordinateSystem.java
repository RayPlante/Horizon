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
 *-------------------------------------------------------------------------
 * History: 
 *  97apr08  rlp  Original version;
 */
package ncsa.horizon.coordinates.systems;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.Metavector;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.util.Voxel;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.coordinates.transforms.LinearCoordTransform;
import java.util.*;

/**
 * a description of a world coordinate system made up of any number of 
 * simple linear axes.  <p>
 */
public class LinearCoordinateSystem extends CoordinateSystem {

    /**
     * construct a coordinate system with the specified number of 
     * linear axes.  
     */
    public LinearCoordinateSystem(int naxes) { 
	super(naxes);
    }

    /**
     * construct a coordinate system with the specified number of axes
     * and described by the specified set of Metadata.  The Metadata
     * is examined to determine the paramters of the system, including
     * which of the axes define the spherical portion of the system.
     */
    public LinearCoordinateSystem(int naxes, Metadata md) 
	throws IllegalTransformException
    { 
	super(naxes, md);
	init();
    }

    /**
     * construct a coordinate system described by the specified set of 
     * Metadata.  The Metadata is examined to determine the paramters 
     * of the system, including which of the axes define the spherical 
     * portion of the system.
     */
    public LinearCoordinateSystem(Metadata md) 
	throws IllegalTransformException
    { 
	super(md);
	init();
    }

    /**
     * create a LinearCoordinateSystem object specifying all internal
     * data.  Missing values in the input array are set to default
     * values.
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
     * @param md       metadata describing the coordinate system; data
     *                 in this list are overridden by the other data 
     *                 given in this constructor
     * @param refpos   the position in the "input" system (the system
     *                 forward() transforms from) that is equal to the
     *                 the refval position.  Default equals 0.
     * @param refval   the position in the "output" system (the system
     *                 forward() transforms to) that is equal to the
     *                 the refpos position.  Default equals 0.
     * @param refoff   An extra offset that gets added to the reference
     *                 position.  Default equals 0;
     * @param stepsz   the size of a voxel of the "output" system (the
     *                 system that forward() transforms to) in units of 
     *                 the "input" system
     * @param names    names to assign to transformed axes.  A null element
     *                 of null array means do not reassign name.  These 
     *                 values only affect the behavior of getMetadata();
     * @exception IllegalTransformException if the parameters do not define
     *                 a valid transformation.
     */
    public LinearCoordinateSystem(int naxes, Metadata md,
				  double[] refpos, double[] refoff, 
				  double[] refval, double[] stepsz, 
				  String[] names)
	throws IllegalTransformException
    { 
	super(naxes, md);

	int i;

	// update our metadata object with the values given to this
	// constructor
	//
	// first the basic reference data
	if (refpos != null) CoordMetadata.setAxisRefposition(cmdata, refpos);
	if (refval != null) CoordMetadata.setAxisRefvalue(cmdata, refval);
	if (refoff != null) CoordMetadata.setAxisRefoffset(cmdata, refoff);
	if (stepsz != null) CoordMetadata.setAxisStepsize(cmdata, stepsz);
	if (names != null) CoordMetadata.setAxisName(cmdata, names);

	// now initialize this system
	init();
    }


    private void init() throws IllegalTransformException {
	CoordTransform t;

	try {
	    t = new LinearCoordTransform(cmdata);
	} catch (IllegalTransformException ex) {
	    throw new 
		IllegalTransformException("Bad parameters in metadata: " +
					  ex.getMessage());
	}
	CoordTransformConstraints c = new CoordTransformConstraints(naxes);
	attachTransform(t, c);
	glueTransforms(false);
    }


}
