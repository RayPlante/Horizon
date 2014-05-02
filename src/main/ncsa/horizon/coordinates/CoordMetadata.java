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
 *  96       rlp  Original version;
 *  97jan20  rlp  added static methods cloneMetadata(Metadata), 
 *             	  getDefaultMetadata(); added method setDefaultMetadata()
 *             	  added static methods formally part of
 *             	  CoordConverter: getAxisNames(Metadata), 
 *             	  matchingAxis(Metadata, int, String, int, int), 
 *             	  matchingAxis(Metadata, int, String, int), 
 *             	  exactMatchingAxis(Metadata, String), 
 *             	  firstMatchingAxis(Metadata, int, String, int, int), 
 *             	  firstMatchingAxis(Metadata, String); changed constructors 
 *             	  to use getDefaultMetadata(), setDefaultMetadata()
 *  97apr01  rlp  updated to match new versions of Metadata, Metavector
 *                and support new Horizon coordinate system metadata schema
 */
package ncsa.horizon.coordinates;

import java.util.*;
import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.Metavector;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.formatters.GenericAxisPosFormatter;

/**
 * a Metadata object with extra help for describing Coordinate Systems. <p>
 *
 * Coordinate-related classes in the Horizon package (e.g. 
 * <a href="ncsa.horizon.coordinates.CoordinateSystem.html">
 * CoordinateSystem</a>, <a href="ncsa.horizon.coordinates.CoordPos.html">
 * CoordPos</a>, etc.) make use of specific set of metadata, the so-called
 * <em>horizon schema</em>, which assumes specific types and meanings for 
 * them.  This class provides special methods for setting values for these 
 * metadata in a way that ensures their proper type.  Constructors provide
 * sensible defaults for values not specifically set. <p>
 * 
 * Below is a list of metadata that have specific meanings for coordinate
 * systems and specific assumed types.  Any that are not specified during
 * construction are set to the default values shown.  
 * <pre>
 *    Key            Type              Default Value
 *    -------------------------------------------------------------
 *    naxes          Integer	       1                            
 *    name           String	       --not set--
 *    Axes           Metavector        Metadata object for each of
 *                                     naxes axes; -see next table-
 *    schema         String            "horizon"
 *    schemaVersion  String            --release-specific--
 *    -------------------------------------------------------------
 * </pre>
 * Since this class is for the horizon schema, the schema and schemaVersion 
 * would not normally need updating later. <p>
 *
 * The Axes object is an array of Metadata objects, each containing 
 * sub-metadata that describes a particular axis.  The Axes that have 
 * specified meanings and types are shown in the next table, along with
 * values set for them by default:
 * <pre>
 *    Axes sub-metadata:  
 *    Key            Type              Default Value
 *    -------------------------------------------------------------
 *    axisSchema     String            "referenced"
 *    name           String            "Pixels"
 *    label          String            -not set-
 *    type           String            "linear"
 *    refposition    Double	       0.0
 *    refvalue       Double	       0.0
 *    stepsize       Double	       0.0
 *    refoffset      Double	       0.0
 *    formatter      AxisPosFormatter  GenericAxisPosFormatter
 *    -------------------------------------------------------------
 * </pre>
 * Note that the "axisSchema" metadatum is similar to "schema" in that it 
 * indicates what metadata are used to parameterize the axis.  A value
 * of "referenced" means that it is defined via a reference pixel using 
 * the above metadata names.  Thus, one would normally not need to update 
 * the "axisSchema" metadatum.  <p>
 *
 * Each of these metadata listed in the first table may be set using a method
 * called set&lt<em>metadatum</em>&gt(), where <em>metadatum</em> is the name 
 * of the metadatum (e.g. <a href="#setName(java.lang.String)">
 * setName(String)</a>).  Sub-metadata for the "Axes" metadatum can be set
 * directly using a method called setAxis&lt<em>metadatum</em>&gt() (e.g. 
 * <a href="#setAxisRefvalue(int,double)">setAxisRefvalue(int,double)</a>   
 * For more information on what each of the above metadata represent, see its 
 * cooresponding set method. <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: CoordMetadata.java,v 1.1 1997/08/07 07:24:11 rplante Exp $
 */
public class CoordMetadata extends Metadata {

    private int numaxes=1;

    public final static String horizonSchema = "horizon";

    /**
     * version of horizon schema supported by this object
     */
    public final static String version     = "v1.2alpha";

    public final static String Axes        = "Axes";
    public final static String naxes       = "naxes";
    public final static String name        = "name";
    public final static String axisSchema  = "axisSchema";
    public final static String type        = "type";
    public final static String label       = "label";
    public final static String formatter   = "formatter";
    public final static String refposition = "refposition";
    public final static String refvalue    = "refvalue";
    public final static String stepsize    = "stepsize";
    public final static String refoffset   = "refoffset";

    /**
     * Construct a CoordMetadata object with one axis with default
     * values for the standard coordinate metadata
     */
    public CoordMetadata() {
	super();
	initMetadata(1);
    }

    /**
     * Create a Metadata object describing a coordinate system with a 
     * given number of axes.  Standard coordinate metadata not found in
     * the given metadata will be set to default values.
     * @param naxes the number of axes in the coordinate system
     * @param defaults the defaults (can be null)
     * @exception ArrayIndexOutOfBoundsException if naxes < 1
     */
    public CoordMetadata(int naxes, Metadata defaults) 
	throws ArrayIndexOutOfBoundsException
    {
	super(defaults);
	initMetadata(naxes);
    }

    /**
     * Creates an empty metadatum list with specified defaults.
     * @param naxes the number of axes in this coordinate system
     * @exception ArrayIndexOutOfBoundsException if naxes < 1
     */
    public CoordMetadata(int naxes) 
	throws ArrayIndexOutOfBoundsException
    {
	super();
        initMetadata(naxes);
    }

    /**
     * Creates an empty metadatum list with specified defaults.
     * @param defaults the defaults
     * @exception ArrayIndexOutOfBoundsException if defaults contains
     *                 a value for "naxes" that is < 1 or not specified.
     */
    public CoordMetadata(Metadata defaults) 
	throws ArrayIndexOutOfBoundsException
    {
	super(defaults);

	Integer nax;
	int ival;
        try {
	    nax = (Integer) defaults.getMetadatum(naxes);
	} catch (ClassCastException e) {
	    nax = null;
	}
	if (nax == null) 
	    ival = 0;
	else
	    ival = nax.intValue();

	initMetadata(ival);
    }

    protected CoordMetadata(boolean setDefaults) {
	super();
	if (setDefaults) initMetadata(1);
    }

    protected void initMetadata(int nax) {
	if (nax < 1) throw new 
	    ArrayIndexOutOfBoundsException("Coordinate Metadatum naxes=" + 
					   nax + " < 1");
	boolean moredef = false;
	setNaxes(nax);

	// set the schema
	setSchema(horizonSchema);
	setSchemaVersion(version);

	// look for a default Axes Metavector; create one if not found
	Metavector axesmv=null;
	if (defaults != null) {
	    try {
		// this Metavector will be protected; thus we can edit it
		axesmv = new Metavector((Metavector) 
					defaults.getMetadatum(Axes));
					
	    } catch (ClassCastException ex) {
		axesmv = null;
	    }
	}
	if (axesmv == null) {
	    moredef = true;
	    axesmv = new Metavector();
	}

	// Make sure enough axes are defined
	int sz = axesmv.size();
	Metadata axmd;
	for(int i=0; i < nax; i++) {

	    // Make sure element (one for each axis) is of type Metadata
	    try {

		// original metadata are protected
		axmd = (Metadata) axesmv.elementAt(i);
	    } catch (ClassCastException ex) {
		axmd = null;
	    }
	    if (axmd == null) {
		moredef = true;
		axmd = new Metadata();
		axesmv.setElementAt(axmd, i);
	    }

	    // Make sure each axis has a minimum of metadata set
	    Double defd = new Double(0.0);
	    String[] keys = { name, schema, schemaVersion, axisSchema, 
			      type, formatter, 
			      refposition, refvalue, stepsize, refoffset };
	    Object[] defs = { "Pixels", horizonSchema, version, "referenced", 
			      "linear", new GenericAxisPosFormatter(),
			      defd, defd, new Double(1.0), defd };
	    for(int j=0; j < keys.length; j++) {
		Object val = axmd.getMetadatum(keys[j]);

		if ( val != null && 
		     ((j < 2 && !(val instanceof String)) || 
		      (j == 2 && !(val instanceof AxisPosFormatter)) || 
		      !(val instanceof Double)) ) val = null;

		if (val == null) {
		    moredef = true;
		    axmd.put(keys[j], defs[j]);
		}
	    }

	}

	// if we had to add any default values, replace the default list
	if (moredef) {
	    if (defaults == null) defaults = new Metadata();
	    defaults.put(Axes, axesmv);
	}
    }

    /**
     * return the current number of axes in the coordinate system 
     * described by the input Metadata
     */
    public static int getNaxes(Metadata md) {
	Integer val;

	try {
	    val = (Integer) md.getMetadatum(naxes);
	} catch (ClassCastException ex) {
	    val = null;
	}
	if (val == null) return 0;

	return val.intValue();
    }

    /**
     * return the current number of axes in the coordinate system
     */
    public int getNaxes() {

	int n = getNaxes(this);

	if (n == 0) {
	    Object val = new Integer(numaxes);
	    super.put(naxes, val);
	    defaults.put(naxes, val);
	}

	return n;
    }

    /**
     * set the number of axes in the coordinate system
     */
    public static void setNaxes(Metadata md, int nax) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (nax < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested naxes=" + nax + " < 1");

	if (md instanceof CoordMetadata) {
	    ((CoordMetadata)md).setNaxes(nax);
	} else {
	    md.put(naxes, new Integer(nax));
	}
    }

    /**
     * set the number of axes in the coordinate system
     */
    public void setNaxes(int nax) throws ArrayIndexOutOfBoundsException {
	numaxes = nax;
	super.put(naxes, new Integer(nax));
    }

    /**
     * set the name of the coordinate system
     */
    public static void setName(Metadata md, String in) {
	if (md instanceof CoordMetadata)
	    ((CoordMetadata)md).setName(in);
	else 
	    md.put(name, in);
    }

    /**
     * set the name of the coordinate system
     */
    public void setName(String in) {
	super.put(name, in);
    }

    /**
     * set the Axes object
     */
    protected static void setAxes(Metadata md, Metavector axmv) {  
	if (md instanceof CoordMetadata)
	    ((CoordMetadata)md).setAxes(axmv);
	else
	    md.put(Axes, axmv); 
    }

    /**
     * set the Axes object
     */
    protected void setAxes(Metavector mv) {  super.put(Axes, mv); }

    /**
     * set an Axes metadatum
     * @param axis   the index of the axis to be set (first axis has index 0)
     * @param mdname name of metadatum to be updated
     * @param in     Object to be set
     */
    protected static void setAxisObject(Metadata md, int axis, String mdname, 
					Object in)
    {
	Metavector axesmv;
	Metadata axismd;

	synchronized (md) {

	    // extract the Axes Metavector
	    try {
		axesmv = (Metavector) md.getMetadatum(Axes);
	    } catch (ClassCastException ex) {
		axesmv = null;
	    }
	    if (axesmv == null) axesmv = new Metavector(axis+1);

	    // extract the Metadata for the requested axis
	    try {
		axismd = (Metadata) axesmv.elementAt(axis);
	    } catch (ClassCastException ex) {
		axismd = null;
	    }
	    if (axismd == null) axismd = new Metadata();

	    axismd.put(mdname, in);

	    axesmv.setElementAt(axismd, axis);
	    setAxes(md, axesmv);
	}
    }

    /**
     * set the axis label for the specified axis.  Axis Labels are usually 
     * used when printing out coordinate positions.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   String to be used as a label
     */
    public static void setAxisLabel(Metadata md, int axis, String in) {
	setAxisObject(md, axis, label, in);
    }

    /**
     * set the axis labels which are usually 
     * used when printing out coordinate positions.
     * @param in   array of Strings to be used as labels 
     */
    public static void setAxisLabel(Metadata md, String[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, label, in[i]);
    }

    /**
     * set the axis labels which are usually 
     * used when printing out coordinate positions.
     * @param in   array of Strings to be used as labels 
     */
    public void setAxisLabel(String[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, label, in[i]);
    }

    /**
     * set the axis label for the specified axis.  Axis Labels are usually 
     * used when printing out coordinate positions.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   String to be used as a label
     */
    public void setAxisLabel(int axis, Object in) {
	setAxisObject(this, axis, label, in);
    }

    /**
     * set the axis name for the specified axis.  Axis names are usually 
     * to identify an axis.  It is used as a label if an axis label is 
     * not set.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   array of Strings to be used as names
     */
    public static void setAxisName(Metadata md, int axis, String in) {
	setAxisObject(md, axis, name, in);
    }

    /**
     * set the axis names.  Axis names are usually 
     * to identify an axis.  It is used as a label if an axis label is 
     * not set.
     * @param in   array of Strings to be used as names
     */
    public void setAxisName(String[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, name, in[i]);
    }

    /**
     * set the axis names.  Axis names are usually 
     * to identify an axis.  It is used as a label if an axis label is 
     * not set.
     * @param in   String to be used as a name
     */
    public static void setAxisName(Metadata md, String[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, name, in[i]);
    }

    /**
     * set the axis name for the specified axis.  Axis names are usually 
     * to identify an axis.  It is used as a label if an axis label is 
     * not set.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   String to be used as a name
     */
    public void setAxisName(int axis, String in) {
	setAxisObject(this, axis, name, in);
    }

    /**
     * set the axis type for the specified axis.  Axis types can be used 
     * to determine how to apply a coordinate transform
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   String to be used as a name
     */
    public static void setAxisType(Metadata md, int axis, String in) {
	setAxisObject(md, axis, type, in);
    }

    /**
     * set the axis types.  Axis types can be used 
     * to determine how to apply a coordinate transform
     * @param in   String to be used as a name
     */
    public static void setAxisType(Metadata md, String[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, type, in[i]);
    }

    /**
     * set the axis types.  Axis types can be used 
     * to determine how to apply a coordinate transform
     * @param in   String to be used as a name
     */
    public void setAxisType(String[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, type, in[i]);
    }

    /**
     * set the axis type for the specified axis.  Axis types can be used 
     * to determine how to apply a coordinate transform
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   String to be used as a name
     */
    public void setAxisType(int axis, String in) {
	setAxisObject(this, axis, type, in);
    }

    /**
     * set the axis reference value for the specified axis.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public static void setAxisRefvalue(Metadata md, int axis, double in) {
	setAxisObject(md, axis, refvalue, new Double(in));
    }

    /**
     * set the axis reference values for all axes.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param in   values to be set
     */
    public static void setAxisRefvalue(Metadata md, double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, refvalue, new Double(in[i]));
    }

    /**
     * set the axis reference values for all axes.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param in   values to be set
     */
    public void setAxisRefvalue(double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, refvalue, new Double(in[i]));
    }

    /**
     * set the axis reference value for the specified axis.  The reference
     * value is position along a world coordinate axis that corresponds to
     * the data location given by the "refposition" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public void setAxisRefvalue(int axis, double in) {
	setAxisObject(this, axis, refvalue, new Double(in));
    }

    /**
     * set the axis reference position for the specified axis.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public static void setAxisRefposition(Metadata md, int axis, double in) {
	setAxisObject(md, axis, refposition, new Double(in));
    }

    /**
     * set the axis reference position for all axes.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param in   values to be set
     */
    public static void setAxisRefposition(Metadata md, double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, refposition, new Double(in[i]));
    }

    /**
     * set the axis reference position for all axes.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param in   values to be set
     */
    public void setAxisRefposition(double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, refposition, new Double(in[i]));
    }

    /**
     * set the axis reference position for the specified axis.  The reference
     * position is position along a data set axis that corresponds to
     * the world coordinate position given by the "refvalue" metadatum.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public void setAxisRefposition(int axis, double in) {
	setAxisObject(this, axis, refposition, new Double(in));
    }

    /**
     * set the axis step size for the specified axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public static void setAxisStepsize(Metadata md, int axis, double in) {
	setAxisObject(md, axis, stepsize, new Double(in));
    }

    /**
     * set the axis step size for all axes.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   values to be set
     */
    public static void setAxisStepsize(Metadata md, double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, stepsize, new Double(in[i]));
    }

    /**
     * set the axis step size for all axes.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param in   values to be set
     */
    public void setAxisStepsize(double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, stepsize, new Double(in[i]));
    }

    /**
     * set the axis step size for the specified axis.  The step size
     * is length of a voxel along an axis in world coordinate units.
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   value to be set
     */
    public void setAxisStepsize(int axis, double in) {
	setAxisObject(this, axis, stepsize, new Double(in));
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
    public void setAxisRefoffset(int axis, double in) {
	setAxisObject(this, axis, refoffset, new Double(in));
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
    public static void setAxisRefoffset(Metadata md, int axis, double in) {
	setAxisObject(md, axis, refoffset, new Double(in));
    }

    /**
     * set the axis reference offsets for all axes.  An offset
     * equal to zero indicates that the reference position refers to the 
     * center of the reference voxel.  -0.5 indicates that the position 
     * corresponds to the beginning of the voxel; +0.5, the end.  If the 
     * reference voxel assumes that the first voxel has an index of 1, an 
     * extra 1 should be subtracted from the reference offset.
     * @param in   value to be set
     */
    public static void setAxisRefoffset(Metadata md, double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, refoffset, new Double(in[i]));
    }

    /**
     * set the axis reference offsets for all axes.  An offset
     * equal to zero indicates that the reference position refers to the 
     * center of the reference voxel.  -0.5 indicates that the position 
     * corresponds to the beginning of the voxel; +0.5, the end.  If the 
     * reference voxel assumes that the first voxel has an index of 1, an 
     * extra 1 should be subtracted from the reference offset.
     * @param in   value to be set
     */
    public void setAxisRefoffset(double[] in) {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, refoffset, new Double(in[i]));
    }

    /**
     * set the axis formatter object for a specified axis.  An 
     * AxisPosFormatter object is used to convert a coordinate position 
     * into a formatted string.
     * @param md   the metadata to operate on
     * @param axis the index of the axis to be set (first axis has index 0)
     * @param in   the formatter object to be set
     */
    public static void setAxisFormatter(Metadata md, int axis, 
					AxisPosFormatter in) 
    {
	setAxisObject(md, axis, formatter, in);
    }

    /**
     * set the axis formatter objects for all axes.  An 
     * AxisPosFormatter object is used to convert a coordinate position 
     * into a formatted string.
     * @param md   the metadata to operate on
     * @param in   the formatter objects to be set
     */
    public static void setAxisFormatter(Metadata md, AxisPosFormatter[] in) 
    {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(md, i, formatter, in[i]);
    }

    /**
     * set the axis formatter objects for all axes.  An 
     * AxisPosFormatter object is used to convert a coordinate position 
     * into a formatted string.
     * @param in   the formatter objects to be set
     */
    public void setAxisFormatter(AxisPosFormatter[] in) 
    {
	for(int i=0; i < in.length; i++) 
	    setAxisObject(this, i, formatter, in[i]);
    }

    /**
     * set the axis formatter object for a specified axis.  An 
     * AxisPosFormatter object is used to convert a coordinate position 
     * into a formatted string.
     */
    public void setAxisFormatter(int axis, AxisPosFormatter in) {
	setAxisObject(this, axis, formatter, in);
    }

    /**
     * update the value of a metadatum.  If the key is recognized as a 
     * standard coordinate metadatum name, the value is checked to ensure
     * it has the proper type
     * @param inkey  the key name (should be of type String)
     * @param val    the value to set for the metadatum with key name
     * @returns Object the previous value, or null if either the key is not 
     *               of type string or the value is of an improper type.
     */
    public Object put(Object inkey, Object val) {
	if (! (inkey instanceof String)) return null;
	String key = (String) inkey;

	boolean special = true;
	Object old = getMetadatum(key);
	try {
	    if (key.equals(naxes)) 
		setNaxes( ((Integer) val).intValue() );
	    else if (key.equals(name)) 
		setName( (String) val );
	    else if (key.equals(Axes)) 
		setAxes( (Metavector) val );
	    else
		special = false;
	}
	catch (ClassCastException ex) {
	    return null;
	}

	if (! special) old = super.put(key, val);

	return old;
    }

    /**
     * extract the String array of axis names from the metadatum list 
     * (i.e. the "Axes[n].name" metadatum); null is returned if not found
     * (or is of the wrong type).
     */

    public static String[] getAxisNames(Metadata in) {
	return getAxisStrings(name, in);
    }

    /**
     * extract the String array of axis types from the metadatum list 
     * (i.e. the "Axes[n].type" metadatum); null is returned if not found
     * (or is of the wrong type).
     */
    public static String[] getAxisTypes(Metadata in) {
	return getAxisStrings(type, in);
    }

    protected static String[] getAxisStrings(String mdname, Metadata in) {
	Integer numax;
	String[] out=null;
	Vector use = new Vector(5);
	Metavector axesmv;

	synchronized (in) {
	    try {
		numax = (Integer) in.getMetadatum(naxes);
	    } catch (ClassCastException e) { 
		numax = null;
	    }
	    if (numax == null) numax = new Integer(0);
	    int nax = numax.intValue();

	    try {
		axesmv = (Metavector) in.getMetadatum(Axes);
	    } catch (ClassCastException e) { 
		axesmv = null;
	    }
	    if (axesmv == null) return null;

	    if (nax <= 0) nax = axesmv.size();
	    if (nax <= 0) return null;
	    out = new String[nax];

	    for(int i=0; i < nax; i++) {
		Metadata smd;
		try { smd = (Metadata) axesmv.elementAt(i); }
		catch (ClassCastException e) {  smd = null; }
		if (smd == null) {
		    out[i] = null;
		}
		else {
		    try {
			out[i] = (String) smd.getMetadatum(mdname);
		    } 
		    catch ( ClassCastException e) {  
			out[i] = null;
		    }
		}
	    }
	}

	return out;
    }
		
    protected static Integer[] indicesMatchingString(String[] s, int toffset, 
						     String value, int ooffset,
						     int len) 
    {
	int i, j;
	Integer[] out;
	Vector matches = new Vector();

	for(i=0; i < s.length; i++) {
	    if (s[i] == null) continue;
	    if (len <= 0) {
		if (s[i].equals(value)) 
		    matches.addElement(new Integer(i));
	    }
	    else {
		if (s[i].regionMatches(toffset, value, ooffset, len)) 
		    matches.addElement(new Integer(i));
	    }
	}

	// copy the Vector of matches to an int array 
	j = matches.size();
	if (j == 0) return null;
	out = new Integer[j];
	for(i=0; i < j; i++) {
	    out[i] = (Integer) matches.elementAt(i);
	}

	return out;
    }
	
    /**
     * return the indices for axes in the input Metadata (as an array of
     * Integers) whose types match a particular value.
     * @param in       input Metadata list to search
     * @param toffset  begin search of Metadatum value at position toffset 
     * @param value    metadatum value to compare to
     * @param ooffset  compare to substring of value starting at position 
     *                 ooffset
     * @param len      use first len characters in comparison.  If len=0, 
     *                 compare first value.length() characters; if len<0,
     *                 look for an exact match (of substrings using toffset
     *                 and ooffset).
     * @returns an Integer array containing the key names, or null if 
     *          none were found.
     */
    public static Integer[] axesMatchingType(Metadata in, int toffset, 
					     String value, int ooffset, 
					     int len) {

	String[] types;

	types = getAxisTypes(in);
	if (types == null) return null;
	return indicesMatchingString(types, toffset, value, ooffset, len);
    }

    /**
     * same as Integer[] axesMatchingType(in, 0, value, 0, len)
     */
    public static Integer[] axesMatchingType(Metadata in, String value, 
					     int len) 
    {
	return axesMatchingType(in, 0, value, 0, len);
    }

    /** 
     * return index for the axis in input Metadata with type exactly matching 
     * input value.  This is the like
     * <code>(axisMatchingType(in, 0, value, 0, -1))[0]</code>
     * (assuming AxisMatchingType did not return null).
     */
    public static Integer axisExactlyMatchingType(Metadata in, String value) {
	Integer[] tmp = axesMatchingType(in, 0, value, 0, -1);
	return ( (tmp == null) ? null : tmp[0] );
    }

    /**
     * return index for the first axis in the input Metadata 
     * with a type matching the input value.
     * @param in       input Metadata list to search
     * @param toffset  begin search of Metadatum value at position toffset 
     * @param value    metadatum value to compare to
     * @param ooffset  compare to substring of value starting at position 
     *                 ooffset
     * @param len      use first len characters in comparison.  If len=0, 
     *                 compare first value.length() characters; if len<0,
     *                 look for an exact match (of substrings using toffset
     *                 and ooffset).
     * @returns an Integer containing the index of the axis, or null if a 
     *          was not found.
     */
    public static Integer firstAxisMatchingType(Metadata in, int toffset, 
						String value, int ooffset, 
						int len) 
    {
	Integer[] list = axesMatchingType(in, toffset, value, ooffset, len);
	return ( (list == null) ? null : list[0] );
    }

    /**
     * same as String firstAxisMatchingType(in, 0, value, 0, 0)
     */
    public static Integer firstAxisMatchingType(Metadata in, String value) {
	return firstAxisMatchingType(in, 0, value, 0, 0);
    }

    /**
     * return the indices for axes in the input Metadata (as an array of
     * Integers) whose names match a particular value.
     * @param in       input Metadata list to search
     * @param toffset  begin search of Metadatum value at position toffset 
     * @param value    metadatum value to compare to
     * @param ooffset  compare to substring of value starting at position 
     *                 ooffset
     * @param len      use first len characters in comparison.  If len=0, 
     *                 compare first value.length() characters; if len<0,
     *                 look for an exact match (of substrings using toffset
     *                 and ooffset).
     * @returns an Integer array containing the key names, or null if 
     *          none were found.
     */
    public static Integer[] axesMatchingName(Metadata in, int toffset, 
					     String value, int ooffset, 
					     int len) {

	String[] names=null;

	names = getAxisNames(in);
	if (names == null) return null;
	return indicesMatchingString(names, toffset, value, ooffset, len);
    }

    /**
     * same as Integer[] axesMatchingName(in, 0, value, 0, len)
     */
    public static Integer[] axesMatchingName(Metadata in, String value, 
					     int len) 
    {
	return axesMatchingName(in, 0, value, 0, len);
    }

    /** 
     * return index for the axis in input Metadata with type exactly matching 
     * input value.  This is the like
     * <code>(AxisMatchingName(in, 0, value, 0, -1))[0]</code>
     * (assuming AxisMatchingName did not return null).
     */
    public static Integer axisExactlyMatchingName(Metadata in, String value) {
	Integer[] tmp = axesMatchingName(in, 0, value, 0, -1);
	return ( (tmp == null) ? null : tmp[0] );
    }

    /**
     * return index for the first axis in the input Metadata 
     * with a type matching the input value.
     * @param in       input Metadata list to search
     * @param toffset  begin search of Metadatum value at position toffset 
     * @param value    metadatum value to compare to
     * @param ooffset  compare to substring of value starting at position 
     *                 ooffset
     * @param len      use first len characters in comparison.  If len=0, 
     *                 compare first value.length() characters; if len<0,
     *                 look for an exact match (of substrings using toffset
     *                 and ooffset).
     * @returns an Integer containing the index of the axis, or null if a 
     *          was not found.
     */
    public static Integer firstAxisMatchingName(Metadata in, int toffset, 
						String value, int ooffset, 
						int len) 
    {
	Integer[] list = axesMatchingName(in, toffset, value, ooffset, len);
	return ( (list == null) ? null : list[0] );
    }

    /**
     * same as String firstAxisMatchingName(in, 0, value, 0, 0)
     */
    public static Integer firstAxisMatchingName(Metadata in, String value) {
	return firstAxisMatchingName(in, 0, value, 0, 0);
    }

    /**
     * returns true if Metadata claims to use the "horizon" schema
     */
    public static boolean usesHorizonSchema(Metadata in) {
	String skma;
	try {
	    skma = (String) in.getMetadatum(schema);
	} 
	catch (ClassCastException ex) { skma = null; }
	return (skma != null && skma.equals(horizonSchema));
    }

    /**
     * returns true if Metadata claims to use the "horizon" schema
     */
    public static boolean usesReferencedAxis(Metadata in, int axis) {
	String skma;
	try {
	    skma = (String) in.getMetadatum("Axes[" + axis + "].axisSchema");
	} 
	catch (ClassCastException ex) { skma = null; }
	return (skma != null && skma.equals("referenced"));
    }

}
