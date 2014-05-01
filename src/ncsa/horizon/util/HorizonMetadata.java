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
 *  97jul31  rlp  Original version;
 */
package ncsa.horizon.util;

/**
 * a Metadata class with extra help to support the Horizon schema
 */
public class HorizonMetadata extends Metadata {

    public final static String horizonSchema = "horizon";

    /**
     * version of horizon schema supported by this object
     */
    public final static String version     = "v1.2alpha";

    public final static String aCoordinateSystem = "CoordinateSystem";
    public final static String nativeSchema      = "nativeSchema";
    public final static String SchemaSet         = "SchemaSet";
    public final static String naxes             = "naxes";

    /** 
     * Construct a HorizonMetadata object for holding Viewable-level
     * metadata.  The dataset will be assumed to have 1 axis
     */
    public HorizonMetadata() {
	super();
	initMetadata(1);
    }

    /**
     * Create a Metadata object describing a Viewable dataset with a
     * given number of axes.  
     * @param nax       the number of axes in the dataset
     * @param defaults  the defaults (can be null)
     * @exception ArrayIndexOutOfBoundsException if naxes < 1
     */
    public HorizonMetadata(int nax, Metadata defaults)
	throws ArrayIndexOutOfBoundsException
    {
	super(defaults);
	if (nax < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested naxes=" + nax + " < 1");
	initMetadata(nax);
    }

    /**
     * Creates an empty metadatum list with defaults.
     * @param nax   the number of axes in the dataset
     * @exception ArrayIndexOutOfBoundsException if naxes < 1
     */
    public HorizonMetadata(int nax) 
	throws ArrayIndexOutOfBoundsException
    {
	super();
	if (nax < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested naxes=" + nax + " < 1");
        initMetadata(nax);
    }

    /**
     * Creates an empty metadatum list with specified defaults.
     * @param viewableDefaults   the default dataset metadata; can be
     *                           null;
     * @param coordMetadata      the coordinate system metadata; these
     *                           will be saved under the primary (i.e.
     *                           updatable) list with the name 
     *                           "CoordinateSystem"; can be null.
     * @exception ArrayIndexOutOfBoundsException if viewableDefaults contains
     *                 a value for "naxes" that is < 1 or not specified.
     */
    public HorizonMetadata(Metadata viewableDefaults, Metadata coordMetadata) 
	throws ArrayIndexOutOfBoundsException
    {
	super(viewableDefaults);

	Integer nax;
	int ival;
	Metadata use = (defaults == null) ? coordMetadata : defaults;
	if (use == null) {
	    ival = 1;
	}
	else {
	    try {
		nax = (Integer) use.getMetadatum(naxes);
	    } catch (ClassCastException e) {
		nax = null;
	    }
	    if (nax == null) 
		ival = 0;
	    else
		ival = nax.intValue();
	}

	if (ival < 1) throw new 
	    ArrayIndexOutOfBoundsException("default metadatum naxes=" + 
					   ival + " < 1");

	initMetadata(ival);
	setCoordinateSystem(coordMetadata);
    }

    protected void initMetadata(int nax) {

	if (nax <= 0) {
	    try {
		Integer ival = (Integer) getMetadatum(naxes);
		if (ival == null) 
		    ival = (Integer) getMetadatum("CoordinateSystem.naxes");
		if (ival != null) {
		    nax = ival.intValue();
		    if (nax <= 0) throw new 
		        CorruptedMetadataException("default naxes=" + 
						   nax + " < 1");
		}
	    } catch (ClassCastException ex) {
		throw new MetadataTypeException("naxes", "Integer");
	    }
	}
	if (nax <= 0) nax = 1;

	setNaxes(nax);

	// set the schema
	setSchema(horizonSchema);
	setSchemaVersion(version);
    }

    /**
     * set the number of axes in a dataset 
     * @param md   the metadata to update
     * @param nax  the number of axes
     */
    public static void setNaxes(Metadata md, int nax) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (nax < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested naxes=" + nax + " < 1");

	md.put(naxes, new Integer(nax));
    }

    /**
     * set the number of axes for the dataset described by this Metadata
     * @param nax  the number of axes
     */
    public void setNaxes(int i) throws ArrayIndexOutOfBoundsException 
    {  setNaxes(this, i); }

    /**
     * set the Coordinate System metadata for a dataset
     * @param md       the metadata to update
     * @param coordmd  the Coordinate System metadata to insert
     */
    public static void setCoordinateSystem(Metadata md, Metadata coordmd) {
	md.put(aCoordinateSystem, coordmd);
    }

    /**
     * set the Coordinate System metadata for a dataset
     * @param coordmd  the Coordinate System metadata to insert
     */
    public void setCoordinateSystem(Metadata coordmd) {
	setCoordinateSystem(this, coordmd);
    }
}

