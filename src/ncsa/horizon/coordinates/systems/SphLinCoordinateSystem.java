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
 *  98jan22  rlp  added support for BadProjectionException, allowing system
 *                to recover from unrecognized projections when strict=false.
 */
package ncsa.horizon.coordinates.systems;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.Metavector;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.util.Voxel;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.coordinates.transforms.LinToSphLinCoordTransform;
import ncsa.horizon.coordinates.transforms.BadProjectionException;
import java.util.*;

/**
 * a description of a world coordinate system that can contain up to one 
 * pair of spherical (longitude/latitude) axes and any number of additional
 * linear axes. <p>
 *
 * Two of the constructors support a boolean input, <code>beStrict</code>, 
 * that when false (which is the default) will cause the system to try to
 * recover from certain illegal metadata values by overriding them with 
 * default values.  Currently, this only applies to the "projection" 
 * metadatum.  If the value is unsupported, unrecognized, or accompanied 
 * by illegal (or missing) projection parameters (the "ProjectionParamters" 
 * metadatum), this class will override the projection type with the 
 * Sanson-Flamsteed projection (GLS, also called the global sinusoid 
 * projection).  With this projection, longitude positions are projected via
 * a simple factor of cosine(latitude).  <p>
 *
 * @author Raymond L. Plante
 * @author the NCSA Horizon Development Team
 */
public class SphLinCoordinateSystem extends CoordinateSystem {

    private boolean strict = false;

    /**
     * construct a coordinate system with the specified number of 
     * (linear) axes.  
     */
    public SphLinCoordinateSystem(int naxes) { 
	super(naxes);
    }

    /**
     * construct a coordinate system with the specified number of axes
     * and described by the specified set of Metadata.  The Metadata
     * is examined to determine the paramters of the system, including
     * which of the axes define the spherical portion of the system.
     */
    public SphLinCoordinateSystem(int naxes, Metadata md) 
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
    public SphLinCoordinateSystem(Metadata md) 
	throws IllegalTransformException
    { 
	super(md);
	init();
    }

    /**
     * construct a coordinate system described by the specified set of 
     * Metadata.  The Metadata is examined to determine the paramters 
     * of the system, including which of the axes define the spherical 
     * portion of the system.
     * @param md        the metadata describing the system
     * @param beStrict  if true, be strict in respecting the parameters of
     *                  the system; if false, this class will override
     *                  certain illegal metadata values.  Currently, only 
     *                  the "projection" code will be overridden with "GLS"
     *                  if original code is unsupported or is accompanied 
     *                  by illegal parameters.
     */
    public SphLinCoordinateSystem(Metadata md, boolean beStrict) 
	throws IllegalTransformException
    { 
	super(md);
	strict = beStrict;
	init();
    }

    /**
     * create a SphLinCoordinateSystem object specifying all internal
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
     * @param matrix   a linear tranformation matrix for correcting for 
     *                 skew and/or rotation.  A null value means no matrix
     *                 should be applied.
     * @param longaxis the index (relative to zero) of the longitude axis,
     *                 or <0 if such an axis does not exist.
     * @param lataxis  the index (relative to zero) of the latitude axis,
     *                 or <0 if such an axis does not exist.
     * @param pcode    a 3-character string representing the projection type;
     *                 currently supported types include those listed in
     *                 <a href="FITSWCS.ProjectionType.html">ProjectionType</a>
     * @param prjparms an array containing the projection parameters (or
     *                 null if none available).  The number of elements needed
     *                 depends on the value of pcode.
     * @param longpole The world longitude of the reference system's pole, or
     *                 null if the default should be used.  A value of 999.0
     *                 is also taken to mean that the default should be used.  
     * @param latpole  The world latitude of the reference system's pole, or
     *                 null if the default should be used.  A value of 999.0
     *                 is also taken to mean that the default should be used.  
     * @param beStrict  if true, be strict in respecting the parameters of
     *                  the system; if false, this class will override
     *                  certain illegal metadata values.  Currently, only 
     *                  the "projection" code will be overridden if it is
     *                  unsupported or is accompanied by illegal parameters.
     * @exception IllegalTransformException if the parameters do not define
     *                 a valid transformation.
     */
    public SphLinCoordinateSystem(int naxes, Metadata md,
				  double[] refpos, double[] refoff, 
				  double[] refval, double[] stepsz, 
				  String[] names, double[] matrix, 
				  int longaxis, int lataxis, 
				  String pcode, double[] prjparms, 
				  Double longpole, Double latpole, 
				  boolean beStrict)
	throws IllegalTransformException
    { 
	super(naxes, md);
	strict = beStrict;

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

	// the skew/rotation matrix
	if (matrix != null) {
	    Metavector srmat = new Metavector(matrix.length);
	    for(i=0; i < matrix.length; i++) 
		srmat.addElement(new Double(matrix[i]));
	    cmdata.put("SkewRotate", srmat);
	}

	// identification of the longitude/latitude axes
	if (longaxis > 0) {
	    if (longaxis >= naxes) 
		throw new ArrayIndexOutOfBoundsException("longaxis index = " + 
						   longaxis + " >= naxes = " +
						   naxes);
	    CoordMetadata.setAxisType(cmdata, longaxis, "longitude");
	}       
	if (lataxis > 0) {
	    if (lataxis >= naxes) 
		throw new ArrayIndexOutOfBoundsException("lataxis index = " + 
						   lataxis + " >= naxes = " +
						   naxes);
	    CoordMetadata.setAxisType(cmdata, lataxis, "latitude");
	}

	// Projection code/parameters
	if (pcode != null) cmdata.put("projection", pcode);
	if (prjparms != null) {
	    Metavector pparms = new Metavector(prjparms.length);
	    for(i=0; i < matrix.length; i++) 
		pparms.addElement(new Double(prjparms[i]));
	    cmdata.put("ProjectionParameters", pparms);
	}

	// longpole/latpole
	if (longpole != null) cmdata.put("longpole", longpole);
	if (latpole != null) cmdata.put("latpole", latpole);
	    
	// now initialize this system
	init();
    }


    private void init() throws IllegalTransformException {
	CoordTransform t;

	try {
	    try {
		t = new LinToSphLinCoordTransform(cmdata);
	    } catch (BadProjectionException ex) {
		if (strict) {
		    throw new 
			IllegalTransformException("Illegal/Unsupported " +
						  "projection requested: " +
						  ex.getMessage());
		} else {

		    System.err.println("Warning: " + ex.getMessage() +
				       "\n  switching to GLS projection.");
		    cmdata.put("projection", "GLS");
		    t = new LinToSphLinCoordTransform(cmdata);
		}
	    }
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
