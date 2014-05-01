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
 *  96       rlp  Original version (as an abstract class)
 *  97mar28  rlp  Updated in redesign of the coordinates package; now
 *                an instantiatable class
 */
package ncsa.horizon.coordinates;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.formatters.GenericAxisPosFormatter;
import ncsa.horizon.coordinates.formatters.HHMMSSAxisPosFormatter;
import ncsa.horizon.coordinates.formatters.DDMMSSAxisPosFormatter;

/**
 * support for accessing a representation (string or double) of a position
 * in a coordinate system. <P>
 *
 * A CoordPos object represents a position in a coordinate space.  Such an 
 * object is often returned by CoordinateSystem object methods and can be
 * handed back to a Coordinate object for conversion to a data voxel location.
 * One of the most useful features of the CoordPos object is that is knows
 * how to format itself as a string.  Code to print out parts of the 
 * CoordPos's value might look something like this:
 * <pre>
 *    CoordPos wcspos;
 *    ...
 *    wcspos = wcs.getCoordPos(datapos);
 *    System.out.println( wcspos.getAxisLabel(0) +   // print position
 *                        ": " +                     //  along 1st axis
 *                        wcspos.valueString(0) );     
 *    System.out.println( wcspos.getAxisLabel(1) +   // print position
 *                        ": " +                     //  along 2nd axis
 *                        wcspos.valueString(1) );     
 * </pre>
 * which might produce something the following:
 * <pre>
 *    RA: 12:45:39.11
 *    Dec: -30:17:20.2
 * </pre>
 * Note that axes are numbered beginning with 0.
 *
 * Position values as doubles can be both retrieved and set.   <p>
 *
 * <b> Coordinate Metadata </b><p>
 *
 * A CoordPos can also carry along with it information about the coordinate
 * system within which it exists, stored as a Metadata object.  In general,
 * the values within the Metadata list are not actually used by the CoordPos 
 * itself (with the exception of two constructors, CoordPos(Metadata) and 
 * CoordPos(Metadata, double[])).  For this reason, CoordPos allows you
 * to directly manipulate its own copy of its Metadata list.  That is,
 * one can make updates to the list directly and have the changes stick 
 * with the CoordPos object without a call to setMetadata(); e.g:
 * <pre>
 *    CoordPos wcspos;
 *    ...
 *    wcspos.getMetadata().put("name", "Celestial-Velocity");
 * </pre> <p>
 * 
 * See <a href="ncsa.horizon.util.Metadata.html">Metadata</a> for more 
 * information about accesssing and setting metadata.  See also 
 * <a href="ncsa.horizon.coordinates.CoordMetadata.html">CoordMetadata</a> 
 * for information about recognized coordinate metadata names.
 */
public class CoordPos implements Cloneable {

    protected int naxes;
    protected String[] labels;
    protected double[] pos;
    protected AxisPosFormatter[] posPrinter;
    protected static GenericAxisPosFormatter defPosPrinter = 
                                                new GenericAxisPosFormatter();
    protected Metadata cmdata;

    /**
     * Construct a default coordinate position in a system with a specified
     * number of axes.  
     * @param naxes       the number of axes in the coordinate system
     */
    public CoordPos(int naxes) {
	init(naxes, null, 0, null, null, null, null, null);
    }

    /**
     * Construct a default coordinate position in a system with a specified
     * number of axes.  Note that the value of naxes overrides any value
     * that might appear in the Metadata list.
     * @param naxes       the number of axes in the coordinate system
     * @param md          the Metadata describing the coordinate system within
     *                    which this position exists.  Can be null.  A copy 
     *                    of this object (using md.deepClone()) is stored 
     *                    internally.
     */
    public CoordPos(int naxes, Metadata md) {
	init(naxes, null, 0, null, null, null, null, md);
    }

    /**
     * Construct a default coordinate position in a system with a specified
     * number of axes and an initial position.  Note that the value of 
     * naxes overrides any value
     * that might appear in the Metadata list.
     * @param naxes       the number of axes in the coordinate system
     * @param position    a representation of the coordinate position
     *                    as a double array.  Can be null.
     * @param md          the Metadata describing the coordinate system within
     *                    which this position exists.  Can be null.  A copy 
     *                    of this object (using md.deepClone()) is stored 
     *                    internally.
     */
    public CoordPos(int naxes, double[] position, Metadata md) {
	init(naxes, position, 0, null, null, null, null, md);
    }

    /**
     * Construct a default coordinate position in a system with a specified
     * number of axes.  The number of axes, the labels, and the formatters
     * are all obtained from the specified Metadata list.
     * @param md          the Metadata describing the coordinate system within
     *                    which this position exists.  Can be null.  A copy 
     *                    of this object (using md.deepClone()) is stored 
     *                    internally.
     * @exception CorruptedMetadataException if the "naxes" metadatum is 
     *                    not found in md or is of an illegal value
     */
    public CoordPos(Metadata md) throws CorruptedMetadataException {

	Integer nax = null;
	try {
	    nax = (Integer) md.getMetadatum("naxes");
	}
	catch (ClassCastException e) {  nax = null; }
	if (nax == null) throw new 
	    CorruptedMetadataException("No (Integer) naxes metadatum found");
	if (nax.intValue() <= 0) throw new 
	    CorruptedMetadataException("naxes metadatum value <= 0");

	init(nax.intValue(), null, 0, null, null, null, null, md);
    }

    /**
     * Construct a default coordinate position in a system with a specified
     * number of axes.  The number of axes, the labels, and the formatters
     * are all obtained from the specified Metadata list.
     * @param md          the Metadata describing the coordinate system within
     *                    which this position exists.  Can be null.  A copy 
     *                    of this object (using md.deepClone()) is stored 
     *                    internally.
     * @param position    a representation of the coordinate position
     *                    as a double array.  Can be null.
     * @exception CorruptedMetadataException if the "naxes" metadatum is 
     *                    not found in md or is of an illegal value
     */
    public CoordPos(Metadata md, double[] position) 
	throws CorruptedMetadataException 
    {
	Integer nax = null;
	try {
	    nax = (Integer) md.getMetadatum("naxes");
	}
	catch (ClassCastException e) {  nax = null; }
	if (nax == null) throw new 
	    CorruptedMetadataException("No (Integer) naxes metadatum found");
	if (nax.intValue() <= 0) throw new 
	    CorruptedMetadataException("naxes metadatum value <= 0");

	init(nax.intValue(), position, 0, null, null, null, null, md);
    }


    /**
     * Construct a CoordPos object, specifying all of its internal data.  
     * Any metadata of unexpected types are ignored.
     * @param naxes       the number of axes in the coordinate system
     * @param position    a representation of the coordinate position
     *                    as a double array.  Can be null.
     * @param labels      the labels to use for identifying each of the 
     *                    axes.  If a label is missing (or is null), then
     *                    the value of the "Axis[n].label" metadatum is 
     *                    used; failing that, "Axis[n].name" is used followed
     *                    by the string "Pixels".
     * @param formatters  the formatting objects to use when printing a 
     *                    position along each coordinate axis.  If a formatter
     *                    is missing (or is null), then the value of the 
     *                    "Axis[n].formatter" is used; failing that, a 
     *                    GenericAxisPosFormatter object is used.
     * @param md          the Metadata describing the coordinate system within
     *                    which this position exists.  Can be null.  A copy 
     *                    of this object (using md.deepClone()) is stored 
     *                    internally.
     * @exception IllegalArgumentException  if naxes <= 0
     */
    public CoordPos(int naxes, double[] position, String[] labels, 
		    AxisPosFormatter[] formatters, Metadata md) 
	throws IllegalArgumentException
    {
	init(naxes, position, 0, labels, null, formatters, null, md);
    }

    /**
     * Construct a CoordPos object, specifying all of its internal data.  
     * Note that appropriate values for the formatters and labels found
     * in the Metadata object overrides the default given in this 
     * constructor.  Any metadata of unexpected types are ignored.
     * @param naxes       the number of axes in the coordinate system
     * @param position    a representation of the coordinate position
     *                    as a double array.  Can be null.
     * @param defpos      a default value to use for elements that are 
     *                    missing from position (i.e. elements with indices 
     *                    < naxes but >= position.length).
     * @param labels      the labels to use for identifying each of the 
     *                    axes.  If a label is missing (or is null), then
     *                    the value of the "Axis[n].label" metadatum is 
     *                    used; failing that, "Axis[n].name" is used followed
     *                    by the value of deflab.
     * @param deflab      the default label to use if nothing better can be
     *                    found.  If null, "Pixels" is used
     * @param formatters  the formatting objects to use when printing a 
     *                    position along each coordinate axis.  If a formatter
     *                    is missing (or is null), then the value of the 
     *                    "Axis[n].formatter" is used; failing that, deffmtr
     *                    is used.
     * @param deffmtr     the default label to use if nothing better can be
     *                    found.  If null, a GenericAxisPosFormatter object
     *                    is used.
     * @param md          the Metadata describing the coordinate system within
     *                    which this position exists.  Can be null.  A copy 
     *                    of this object (using md.deepClone()) is stored 
     *                    internally.
     * @exception IllegalArgumentException  if naxes <= 0
     */
    public CoordPos(int naxes, double[] position, double defpos, 
		    String[] labels, String deflab,
		    AxisPosFormatter[] formatters, AxisPosFormatter deffmtr, 
		    Metadata md) 
	throws IllegalArgumentException
    {
	init(naxes, position, defpos, labels, deflab, formatters, deffmtr, md);
    }

    /**
     * initialize the internal data of this object
     */
    private void init(int naxes, double[] position, double defpos, 
		    String[] labels, String deflab,
		    AxisPosFormatter[] formatters, AxisPosFormatter deffmtr, 
		    Metadata md) 
	throws IllegalArgumentException
    {
	if (naxes <= 0) 
	    throw new IllegalArgumentException("naxes = " + naxes + " <= 0");

	this.naxes = naxes;
	cmdata = md.deepClone();
	cmdata.put("naxes", new Integer(naxes));

	// set the position values
	int i, j;
	pos = new double[naxes];
	i = 0;
	if (position != null) {
	    for(; i < naxes && i < position.length; i++) 
		pos[i] = position[i];
	}
	for(; i < naxes; i++) pos[i] = defpos;

	// set the formatting objects
	posPrinter = new AxisPosFormatter[naxes];
	if (formatters != null) {
	    for(i=0; i < naxes && i < formatters.length; i++) 
		posPrinter[i] = formatters[i];
	}

	// set the labels
	this.labels = new String[naxes];
	if (labels != null) {
	    for(i=0; i < naxes && i < labels.length; i++) 
		this.labels[i] = labels[i];
	}

	// set default labels and formatters; prefer the value in our
	// Metadata object over the default.
	//
	if (deflab == null) deflab = "Pixels";
	if (deffmtr == null) deffmtr = defPosPrinter;
	for(i=0; i < naxes; i++) {
	    if (posPrinter[i] == null) {
		AxisPosFormatter use = null;
		if (cmdata != null) {
		    try {
			use = (AxisPosFormatter) 
			    cmdata.getMetadatum("Axes[" + i + "].formatter");
		    } catch (ClassCastException e) { use = null; }
		}
		if (use == null) use = deffmtr;

		posPrinter[i] = use;
	    }
	    if (this.labels[i] == null) { 
		String use = null;
		if (cmdata != null) {
		    try {
			use = (String) 
			    cmdata.getMetadatum("Axes[" + i + "].label");
		    } catch (ClassCastException e) { use = null; }
		    try {
			if (use == null) use = (String) 
			    cmdata.getMetadatum("Axes[" + i + "].name");
		    } catch (ClassCastException e) { use = null; }
		}
		if (use == null) use = deflab;

		this.labels[i] = use;
	    }
	}
    }



    /**
     * return the number of axes in this coordinate system
     */
    public int getNaxes() { return naxes; } 

    /**
     * return the currently set label for an axis
     * @param axis the axis of interest; axes are numbered beginning with 0.
     */
    public String getAxisLabel(int axis) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (axis >= naxes) 
	    throw new ArrayIndexOutOfBoundsException("Requested axis=" + axis +
						     " >= naxes=" + naxes);
	return labels[axis];
    }

    /**
     * return the axis labels as an array of strings.  The index of the 
     * label for the first axis is zero.
     */
    public String[] getAxisLabel() { 
	String[] out = new String[labels.length];
	System.arraycopy(labels, 0, out, 0, labels.length);
	return out;
    }

    /**
     * set the axis labels.  Extra labels in the input array are ignored.
     */
    public void setAxisLabel(String[] labs) {
	for(int i=0; i < labs.length && i < labels.length; i++) 
	    labels[i] = labs[i];
    }

    /**
     * set an axis label.  This method will not affect the contents of
     * the Metadata object returned by the getMetadata() method. 
     */
    public void setAxisLabel(int i, String lab) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (i >= naxes) 
	    throw new ArrayIndexOutOfBoundsException("Requested axis=" + i +
						     " >= naxes=" + naxes);
	labels[i] = lab;
    }

    /**
     * return a Metadata object that identifies the coordinate 
     * system within which this coordinate position exists.  
     * @return Metadata  an editable Metadata object (not a copy), or null
     *                   if one is not available.
     *                   Updates made to the returned Metadata's 
     *                   contents will be carried along with this CoordPos
     *                   object even without calls to setMetadata().
     */
    public Metadata getMetadata() { return cmdata; }

    /**
     * set the Metadata object that identifies the coordinate 
     * system within which this coordinate position exists.
     */
    public void setMetadata(Metadata md) { cmdata = md; }

    /**
     * return a double containing the position's projection along
     * an axis
     * @param axis the axis of interest; axes are numbered beginning with 0.
     */
    public double getValue(int axis) throws ArrayIndexOutOfBoundsException {
	if (axis >= naxes) 
	    throw new ArrayIndexOutOfBoundsException("Requested axis=" + axis +
						     " >= naxes=" + naxes);
	return pos[axis];
    }

    /**
     * set the position's projection along an axis to a value
     * @param axis the axis of interest; axes are numbered beginning with 0.
     * @param newval the new value
     * @return the old value
     */
    public void setValue(int axis, double newval) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (axis >= naxes) 
	    throw new ArrayIndexOutOfBoundsException("Requested axis=" + axis +
						     " >= naxes=" + naxes);
	pos[axis] = newval;
    }

    /**
     * return an array of doubles representing the position along each
     * axis
     */
    public double[] getValue() {
	double[] out = new double[pos.length];
	System.arraycopy(pos, 0, out, 0, pos.length);
	return out;
    }

    /**
     * set the position along all axes with the values given in the input
     * array, returning the old values;
     */
    public void setValue(double[] newpos) {
	double[] tmp;

	if (newpos == null) return;
	int length = (newpos.length > pos.length) ? pos.length : newpos.length;
	System.arraycopy(newpos, 0, pos, 0, length);
    }

    /**
     * return a formatted string containing the position's projection along
     * an axis
     * @param axis the axis of interest; axes are numbered beginning with 0.
     */
    public String valueString(int axis) 
	throws ArrayIndexOutOfBoundsException 
    {
	return posPrinter[axis].toString(pos[axis]);
    }

    /**
     * return a formatted string containing the position's projection along
     * an axis
     * @param axis the axis of interest; axes are numbered beginning with 0.
     * @param prec the precision of the string; the interpretation of the
     *             as depends on the AxisPosFormatter object in use for the
     *             requested axis.
     */
    public String valueString(int axis, int prec) 
	throws ArrayIndexOutOfBoundsException 
    {
	return posPrinter[axis].toString(pos[axis], prec);
    }

    /**
     * return an array of formatted strings containing the position's 
     * projection along each axis
     */
    public String[] valueStrings() 
	throws ArrayIndexOutOfBoundsException 
    {
	String[] out = new String[naxes];
	for(int i=0; i < naxes; i++) out[i] = posPrinter[i].toString(pos[i]);

	return out;
    }

    /**
     * return an array of formatted strings containing the position's 
     * projection along each axis
     * @param prec the precision of the string; the interpretation of the
     *             as depends on the AxisPosFormatter object in use for the
     *             requested axis.
     */
    public String[] valueStrings(int prec) 
	throws ArrayIndexOutOfBoundsException 
    {
	String[] out = new String[naxes];
	for(int i=0; i < naxes; i++) 
	    out[i] = posPrinter[i].toString(pos[i], prec);

	return out;
    }

    /**
     * clone this object.  
     */
    public Object clone() {
	CoordPos out;

	try {
	    out = (CoordPos) super.clone();

	    out.labels = new String[labels.length];
	    System.arraycopy(labels, 0, out.labels, 0, labels.length);

	    out.pos = new double[pos.length];
	    System.arraycopy(pos, 0, out.pos, 0, pos.length);

	    out.posPrinter = new AxisPosFormatter[posPrinter.length];
	    System.arraycopy(posPrinter, 0, 
			     out.posPrinter, 0, posPrinter.length);
	}
	catch (CloneNotSupportedException ex) {

	    // should not happen
	    throw new InternalError(ex.getMessage());
	}

	return out;
    }

}

