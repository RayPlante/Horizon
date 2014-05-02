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
 *  98jan22  rlp  added support for BadProjectionException when bad projection
 *                parameters are encountered.  
 */
package ncsa.horizon.coordinates.transforms;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.Metavector;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.*;
import FITSWCS.SphericalTransform;
import FITSWCS.Projection;
import FITSWCS.projections.*;
import FITSWCS.TrigD;
import FITSWCS.exceptions.*;

/**
 * an object that transforms positions in a linear, multi-dimensional space 
 * (often the data space) into a world coordinate space that contains zero or
 * one pair of axes representing longitude and latitude.  The longitude and 
 * latitude axes are de-projected from the linear space via one of the 
 * projections given in 
 * <a href="FITSWCS.ProjectionType.html">FITSWCS.ProjectionType</a>.
 * Prior to deprojection, all axes may be optionally sent through a 
 * skewing/rotating transform.  
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: LinToSphLinCoordTransform.java,v 1.2 1998/01/22 19:42:26 rplante Exp $
 */
public class LinToSphLinCoordTransform extends CoordTransform {

    protected int naxes;
    protected SkewRotateCoordTransform skew = null;
    protected LinearCoordTransform lin = null;
    protected SphericalTransform sph = null;
    protected Projection proj = null;
    protected int longax;
    protected int latax;
    protected String[] name = null;

    private String pcode = null;
    private double[] projparm = null;

    /**
     * create a Transform based on the specified metadata 
     * @param md        the metadata to examine for data defining the 
     *                  transform
     * @param longaxis 	the index (relative to zero) of the longitude axis,
     *                 	or <0 if the axis should be determined from the 
     *                  metadata
     * @param lataxis  	the index (relative to zero) of the latitude axis,
     *                 	or <0 if the axis should be determined from the 
     *                  metadata
     */
    public LinToSphLinCoordTransform(Metadata md) 
	throws IllegalTransformException, BadProjectionException
    {
	this(md, -1, -1);
    }

    /**
     * create a Transform based on the specified metadata 
     * @param md        the metadata to examine for data defining the 
     *                  transform
     * @param longaxis 	the index (relative to zero) of the longitude axis,
     *                 	or <0 if the axis should be determined from the 
     *                  metadata
     * @param lataxis  	the index (relative to zero) of the latitude axis,
     *                 	or <0 if the axis should be determined from the 
     *                  metadata
     */
    public LinToSphLinCoordTransform(Metadata md, int longaxis, int lataxis) 
	throws IllegalTransformException, BadProjectionException
    {
	String prjcode;
	int nax;
	double[] prjparms=null;
	Double longpole = null, latpole = null;

	try {
	    Integer N = (Integer) md.getMetadatum("naxes");
	    nax = (N == null) ? 0 : N.intValue();
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("naxes: not of Integer type");
	}
	if (nax <= 0) throw new IllegalTransformException("naxes = " + 
							  nax + " <= 0");

	// look for projection code, parameters
	try {
	    prjcode = (String) md.getMetadatum("projection");
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("projection: not of String " +
						 "type");
	}
	try {
	    Metavector Pparm = (Metavector) 
		md.getMetadatum("ProjectionParameters");
	    if (Pparm != null) {
		projparm = new double[Pparm.size()];
		for(int j=0; j < Pparm.size(); j++) 
		    projparm[j] = ((Double)Pparm.elementAt(j)).doubleValue();
	    }
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("ProjectionParameters: " + 
						 "not a Metavector of " +
						 "Doubles");
	}

	try {
	    longpole = (Double) md.getMetadatum("longpole");
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("longpole: not of Double " +
						 "type");
	}
	try {
	    latpole = (Double) md.getMetadatum("latpole");
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("latpole: not of Double " +
						 "type");
	}

	// look for Skew/Rotate matrix
	double[] matrix = null;
	try {
	    Metavector SRmat = (Metavector) md.getMetadatum("SkewRotate");
	    if (SRmat != null) {
		matrix = new double[SRmat.size()];
		for(int j=0; j < SRmat.size(); j++) 
		    matrix[j] = ((Double)SRmat.elementAt(j)).doubleValue();
	    }
	} catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("SkewRotate: not a " + 
						 "Metavector of Doubles");
	}

	// Look for spherical types
	Integer N;
	if (longaxis < 0) {
	    N = CoordMetadata.firstAxisMatchingType(md, 0, "longitude", 0, 9);
	    if (N != null) longaxis = N.intValue();
	}
	if (lataxis < 0) {
	    N = CoordMetadata.firstAxisMatchingType(md, 0, "latitude", 0, 8);
	    if (N != null) lataxis = N.intValue();
	}

	// get axis names
	String[] names = CoordMetadata.getAxisNames(md);

	// Now get coordinate definition values
	String[] mdnm = { "refposition", "refoffset", "refvalue", "stepsize" };
	double[] defd = { 0.0, 0.0, 0.0, 1.0 };
	double[][] val = new double[4][];
	for(int i=0; i < 4; i++) {
	    val[i] = new double[nax];
	    for(int j=0; j < nax; j++) {
		try {
		    Double d = (Double) md.getMetadatum("Axes[" + j + "]." + 
							mdnm[i]);
		    val[i][j] = (d != null) ? d.doubleValue(): defd[i];
		} catch (ClassCastException ex) {
		    throw new 
			CorruptedMetadataException(mdnm[i] + ": not of " + 
						   "Double type");
		}
	    }
	}

	initialize(nax, val[0], val[1], val[2], val[3], names, matrix,
		   longaxis, lataxis, prjcode, prjparms, longpole, latpole);

    }

    /**
     * create a LinToSphLinCoordTransform object specifying all internal
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
     * @exception IllegalTransformException if the parameters do not define
     *                 a valid transformation.
     */
    public LinToSphLinCoordTransform(int naxes, 
				     double[] refpos, double[] refoff, 
				     double[] refval, double[] stepsz, 
				     String[] names, double[] matrix, 
				     int longaxis, int lataxis, 
				     String pcode, double[] prjparms, 
				     Double longpole, Double latpole)
	throws IllegalTransformException, BadProjectionException
    {
	initialize(naxes, refpos, refoff, refval, stepsz, names, matrix, 
		   longaxis, lataxis, pcode, prjparms, longpole, latpole);
    }

    /**
     * create a LinToSphLinCoordTransform object specifying internal
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
     * @param refoff   An extra offset that gets added to the reference
     *                 position.  Default equals 0;
     * @param stepsz   the size of a voxel of the "output" system (the
     *                 system that forward() transforms to) in units of 
     *                 the "input" system
     * @param names    names to assign to transformed axes.  A null element
     *                 of null array means do not reassign name.  These 
     *                 values only affect the behavior of getMetadata();
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
     * @exception IllegalTransformException if the parameters do not define
     *                 a valid transformation.
     */
    public LinToSphLinCoordTransform(int naxes, 
				     double[] refpos, double[] refoff, 
				     double[] refval, double[] stepsz, 
				     String[] names, int longaxis, int lataxis,
				     String pcode, double[] prjparms)
	throws IllegalTransformException, BadProjectionException
    {
	initialize(naxes, refpos, refoff, refval, stepsz, names, null, 
		   longaxis, lataxis, pcode, prjparms, null, null);
    }

    /**
     * create a LinToSphLinCoordTransform object specifying internal
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
     * @param refoff   An extra offset that gets added to the reference
     *                 position.  Default equals 0;
     * @param stepsz   the size of a voxel of the "output" system (the
     *                 system that forward() transforms to) in units of 
     *                 the "input" system
     * @param names    names to assign to transformed axes.  A null element
     *                 of null array means do not reassign name.  These 
     *                 values only affect the behavior of getMetadata();
     * @param longaxis the index (relative to zero) of the longitude axis,
     *                 or <0 if such an axis does not exist.
     * @param lataxis  the index (relative to zero) of the latitude axis,
     *                 or <0 if such an axis does not exist.
     * @param pcode    a 3-character string representing the projection type;
     *                 currently supported types include those listed in
     *                 <a href="FITSWCS.ProjectionType.html">ProjectionType</a>
     * @exception IllegalTransformException if the parameters do not define
     *                 a valid transformation.
     */
    public LinToSphLinCoordTransform(int naxes, 
				     double[] refpos, double[] refoff, 
				     double[] refval, double[] stepsz, 
				     String[] names, int longaxis, int lataxis,
				     String pcode)
	throws IllegalTransformException, BadProjectionException
    {
	initialize(naxes, refpos, refoff, refval, stepsz, names, null, 
		   longaxis, lataxis, pcode, null, null, null);
    }

    /**
     * initialize all internal data.  Missing values in the input array 
     * are set to default values.
     * @param naxes    the maximum number of axes this transform can
     *                 operate on
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
     * @exception IllegalTransformException if the parameters do not define
     *                 a valid transformation.
     */
    private void initialize(int naxes, double[] refpos, double[] refoff, 
			    double[] refval, double[] stepsz, String[] names,
			    double[] matrix, int longaxis, int lataxis, 
			    String pcode, double[] prjparms, 
			    Double longpole, Double latpole)
	throws IllegalTransformException, BadProjectionException
    {
	if (naxes <= 0) throw new 
	    ArrayIndexOutOfBoundsException("naxes = " + naxes + " <= 0");
	this.naxes = naxes;

	// ensure sufficient values in axis description values
	double[][] inarrays = { refpos, refval, refoff, stepsz };
	double[][] outarrays = new double[4][];
	double[] defs = { 0.0, 0.0, 0.0, 1.0 };
	int l, j;
	for(int i=0; i < inarrays.length; i++) {
	    if (inarrays[i] == null || inarrays[i].length < naxes) {
		outarrays[i] = new double[naxes];

		if (inarrays[i] != null) {
		    l = (naxes < inarrays[i].length) ? naxes 
			                             : inarrays[i].length;
		    System.arraycopy(inarrays[i], 0, outarrays[i], 0, l);
		}
		else {
		    l = 0;
		}
		for(j=l; j < naxes; j++) outarrays[i][j] = defs[i];
	    }
	    else {
		outarrays[i] = inarrays[i];
	    }
	}

	refpos = outarrays[0];
	refval = outarrays[1];
	refoff = outarrays[2];
	stepsz = outarrays[3];

	boolean dosph = (pcode != null && longaxis >= 0 && lataxis >= 0);
	double longval = 0, latval = 0;
	if (dosph) {
	    longax = longaxis;
	    latax = lataxis;
	    longval = refval[longax];
	    latval = refval[latax];
	    double[] tmpda = new double[refval.length];
	    System.arraycopy(refval, 0, tmpda, 0, refval.length);
	    refval = tmpda;
	    refval[longax] = refval[latax] = 0.0;
	}

	// create a skew/rotate and linear shifting transforms as necessary
	if (matrix != null) {
	    try {
		skew = new SkewRotateCoordTransform(naxes, matrix);
	    }
	    catch (SingularMatrixException ex) {
		throw new IllegalTransformException("SkewRotate: singular " +
						    "matrix");
	    }
	    lin  = new LinearCoordTransform(naxes, refpos, refval, refoff, 
					    null, null);
					    
	}
	else {
	    lin  = new LinearCoordTransform(naxes, refpos, refval, refoff, 
					    stepsz, null);
	}

	if (dosph) {

	    // create a Projection transform
	    this.pcode = pcode;
	    projparm = prjparms;
	    try { 
		proj = Projection.getProjection(pcode, projparm);
	    }
	    catch (UnsupportedProjectionException ex) {
		throw new BadProjectionException(
		    BadProjectionException.UNSUPPORTED_TYPE, pcode);
	    }
	    catch (BadProjectionParameterException ex) {
		throw new BadProjectionException(
		    BadProjectionException.ILLEGAL_PARAMETERS, ex.getMessage());
	    }
	    catch (ArrayIndexOutOfBoundsException ex) {
		throw new BadProjectionException(
		    BadProjectionException.ILLEGAL_PARAMETERS,
		    "need more than " + ex.getMessage() + " parameters");
	    }
	
	    // create a Spherical transform
	    try { 
		double theta0 = SphericalTransform.getTheta0(pcode);
		boolean islongpole = (longpole != null || latpole == null);
		double poleref = (!islongpole) ? latpole.doubleValue() 
		    : (longpole == null) ? 999.0 : longpole.doubleValue();
		sph = new SphericalTransform(longval, latval, 
					     poleref, islongpole, theta0);
	    }
	    catch (FITSWCSException ex) {
		throw new IllegalTransformException(ex.getMessage());
	    }
	}

	name = new String[naxes];
	System.arraycopy(names, 0, name, 0, 
			 (names.length < naxes) ? names.length : naxes);
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
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	double[] out;

	// rotate/skew
	if (skew != null) {
	    position = skew.forward(position, axisIndices);
	}

	// translate/scale
	if (lin != null) {
	    position = lin.forward(position, axisIndices);
	}

	// de-project
	if (proj != null) {
	    double[] xy;
	    try {
		xy = proj.rev( position[axisIndices[longax]], 
			       position[axisIndices[latax]] );
	    }
	    catch (PixelBeyondProjectionException ex) {
		throw new PositionBeyondDomainException(ex.getMessage());
	    }

	    position[axisIndices[longax]] = xy[0];
	    position[axisIndices[latax]] = xy[1];
	}

	// spherically rotate
	if (sph != null) {
	    while(position[axisIndices[longax]] < 0) 
		position[axisIndices[longax]] += 360.0;
	    double[] lnglat = sph.rev(position[axisIndices[longax]],
				      position[axisIndices[latax]]);

	    position[axisIndices[longax]] = lnglat[0];
	    position[axisIndices[latax]] = lnglat[1];
	}

	return position;
    }

    /**
     * apply a forward tranform on an input position.  
     * @param position    an array giving the input position to transform
     */
    public synchronized double[] forward(double[] position) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	double[] out;

	// rotate/skew
	if (skew != null) {
	    position = skew.forward(position);
	}

	// translate/scale
	if (lin != null) {
	    position = lin.forward(position);
	}

	// de-project
	if (proj != null) {
	    double[] xy;
	    try {
		xy = proj.rev( position[longax], position[latax] );
	    }
	    catch (PixelBeyondProjectionException ex) {
		throw new PositionBeyondDomainException(ex.getMessage());
	    }

	    position[longax] = xy[0];
	    position[latax] = xy[1];
	}

	// spherically rotate
	if (sph != null) {
	    while(position[longax] < 0) position[longax] += 360.0;
	    double[] lnglat = sph.rev(position[longax], position[latax]);

	    position[longax] = lnglat[0];
	    position[latax] = lnglat[1];
	}

	return position;
    }

    /**
     * apply a reverse tranform on an input position.  
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public synchronized double[] reverse(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	double[] out;

	// spherically rotate
	if (sph != null) {
	    while(position[axisIndices[longax]] < 0) 
		position[axisIndices[longax]] += 360.0;
	    double[] lnglat = sph.fwd(position[axisIndices[longax]],
				      position[axisIndices[latax]]);

	    position[axisIndices[longax]] = lnglat[0];
	    position[axisIndices[latax]] = lnglat[1];
	}

	// project onto sphere
	if (proj != null) {
	    double[] xy;
	    try {
		xy = proj.fwd( position[axisIndices[longax]], 
			       position[axisIndices[latax]] );
	    }
	    catch (PixelBeyondProjectionException ex) {
		throw new PositionBeyondDomainException(ex.getMessage());
	    }

	    position[axisIndices[longax]] = xy[0];
	    position[axisIndices[latax]] = xy[1];
	}

	// translate/scale
	if (lin != null) {
	    position = lin.reverse(position, axisIndices);
	}

	// rotate/skew
	if (skew != null) {
	    position = skew.forward(position, axisIndices);
	}

	return position;
    }

    /**
     * apply a reverse tranform on an input position.  
     * @param position    an array giving the input position to transform
     */
    public synchronized double[] reverse(double[] position) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	double[] out;

	// spherically rotate
	if (sph != null) {
	    while(position[longax] < 0) position[longax] += 360.0;
	    double[] lnglat = sph.fwd(position[longax],
				      position[latax]);

	    position[longax] = lnglat[0];
	    position[latax] = lnglat[1];
	}

	// project onto sphere
	if (proj != null) {
	    double[] xy;
	    try {
		xy = proj.fwd( position[longax], 
			       position[latax] );
	    }
	    catch (PixelBeyondProjectionException ex) {
		throw new PositionBeyondDomainException(ex.getMessage());
	    }

	    position[longax] = xy[0];
	    position[latax] = xy[1];
	}

	// translate/scale
	if (lin != null) {
	    position = lin.reverse(position);
	}

	// rotate/skew
	if (skew != null) {
	    position = skew.forward(position);
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
    public int getMaxNaxes() { return naxes; }

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
	String[] out = new String[naxes];
	System.arraycopy(name, 0, out, 0, naxes);
	return out;
    }

    /**
     * create a copy of this Transform
     */
    public synchronized Object clone() {
	LinToSphLinCoordTransform out = 
	    (LinToSphLinCoordTransform) super.clone();
	if (skew != null) out.skew = (SkewRotateCoordTransform) skew.clone();
	if (lin != null) out.lin = (LinearCoordTransform) lin.clone();
	try {
	    if (proj != null) 
		out.proj = Projection.getProjection(pcode, projparm);
	} catch (FITSWCSException ex) {
	    // should not happen
	    throw new InternalError("Bad projection found during cloning");
	}
	if (sph != null) out.sph = new SphericalTransform(sph.getEuler());
	out.setName(name);
	return out;
    }

    /**
     * make an educated guess as to the proper way to apply this transform
     * to a coordinate system with the specified Metadata.  This method
     * returns a default CoordTransformConstraints object that is 
     * consistant with the system description that was provided to this
     * transform at construction; the input Metadata are ignored.
     */
    public CoordTransformConstraints determineConstraints(Metadata in,
							  boolean forwards) 
    {
	CoordTransformConstraints out = new CoordTransformConstraints(naxes);
	out.forward = forwards;
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

	if (in == null) in = new CoordMetadata(naxes);
	super.getMetadata(in, forward, axisIndices);

	if (forward) {
	    if (skew != null) in = skew.getMetadata(in, true, axisIndices);
	    if (lin != null)  in = lin.getMetadata(in, true, axisIndices);
	}

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
	    double[] spos = { rv[longax], rv[latax] };
	    if (forward) {
		if (proj != null) spos = proj.rev(spos);
		if (sph != null) spos = sph.rev(spos);
	    }
	    else {
		if (sph != null) spos = sph.rev(spos);
		if (proj != null) spos = proj.rev(spos);
	    }
	    rv[longax] = spos[0];
	    rv[latax] = spos[1]; 
	}
	catch (FITSWCSException ex) { }

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

	if (! forward) {
	    if (skew != null) in = lin.getMetadata(in, false, axisIndices);
	    if (lin != null)  in = skew.getMetadata(in, false, axisIndices);
	}

	return in;
    }
}

	
