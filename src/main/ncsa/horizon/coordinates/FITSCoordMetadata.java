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
 *-------------------------------------------------------------------------
 * History: 
 *  96       rlp  Original version 
 *  97jul    rlp  Updated for new coordinat model, added FITS header parser
 *  97sep05  rlp  EQUINOX and EPOCH metadata types changed to Double
 *  97sep30  rlp  correct bug in scanHeader() caused by above change;
 *                allow 80-char header card to be followed by a \n;
 *                fixed bug in scanHeaderCard using setVeloStandard()
 *  97oct23  rlp  correct stack overflow bug in setCRPIX(int, double) 
 */
package ncsa.horizon.coordinates;

import java.util.*;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.PushbackInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.MetadataTypeException;
import ncsa.horizon.util.Metavector;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.coordinates.formatters.*;
import ncsa.horizon.coordinates.systems.SphLinCoordinateSystem;
import FITSWCS.TrigD;

/**
 * a Metadata class for loading coordinate metadata from a FITS image file. <p>
 *
 * This class provides a variety of methods that makes it easy load FITS 
 * metadata with the correct type.  As the are loaded, they are converted 
 * values and types defined by the horizon schema so that they can be used
 * by the horizon coordinate classes. <p>
 *
 * One should note that the FITS metadata format/schema uses the convention 
 * of numbering axes beginning with 1, in contrast to the horizon schema 
 * which numbers axes beginning with 0.  Methods in this class that take an 
 * axis number as an argument assume the FITS convention (subtracting 1 from
 * the input value when converting to the horizon schema). <p>
 * 
 * The majority of methods provided by this class give the user a way of 
 * saving individual metadata values; these are methods like
 * <code>setNAXIS()</code> and <code>setCTYPE()</code>.  It also
 * provides a few methods for parsing out FITS headers for
 * coordinate-related data.  These include <code>scanHeaderLine()</code>
 * which parses a single line from the header, and <code>scanHeader</code>
 * for parsing an entire header from an input stream.  The
 * <code>modernize()</code> method is used for converting depricated FITS
 * keywords into their current alternatives.  Finally, after the metadata
 * list is fully loaded, one can use the <code>createCoordSys()</code> method
 * to create the <code>CoordinateSystem</code> object the metadata describe.
 * <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: FITSCoordMetadata.java,v 1.2 1997/12/05 02:05:46 rplante Exp $
 */
public class FITSCoordMetadata extends CoordMetadata {

    public final static 
    AxisPosFormatter defaultRAFormatter = new HHMMSSAxisPosFormatter();
    
    public final static 
    AxisPosFormatter defaultDecFormatter = new DDMMSSAxisPosFormatter();
    
    public final static 
    AxisPosFormatter defaultLinearFormatter = new GenericAxisPosFormatter();

    /**
     * Construct a CoordMetadata list for a FITS image
     */
    public FITSCoordMetadata() {
	super(2);
    }

    /**
     * Creates an metadatum list for a coordinate system with naxes axes
     * @param naxes the number of axes in the coordinate system
     * @param defaults the defaults (can be null)
     */
    public FITSCoordMetadata(int naxes, Metadata defaults) 
	throws ArrayIndexOutOfBoundsException
    {
	super(naxes, defaults);
    }

    /**
     * Creates an empty metadatum list with specified defaults.
     * @param defaults the defaults
     */
    public FITSCoordMetadata(int naxes) 
	throws ArrayIndexOutOfBoundsException
    {
	super(naxes);
    }

    /**
     * Creates an empty metadatum list with specified defaults.
     * @param defaults the defaults
     */
    public FITSCoordMetadata(Metadata defaults) 
	throws ArrayIndexOutOfBoundsException
    {
	super(defaults);
    }

    /**
     * return the value of FITS keyword NAXIS, the number of axes in the 
     * dataset
     * @param md  the metadata list to search
     */
    public static int getNAXIS(Metadata md) { 
	return getNaxes(md); 
    }

    /**
     * return the value of FITS keyword NAXIS, the number of axes in the 
     * dataset
     */
    public int getNAXIS() { return super.getNaxes(); }

    /**
     * insert the number of axes into a metadata list
     * @param md   the Metadata list to insert value into
     * @param nax  the value of the NAXIS keyword
     */
    public static void setNAXIS(Metadata md, int nax) { 
	setNaxes(md, nax); 
    }

    /**
     * insert the number of axes into this metadata list
     * @param nax  the value of the NAXIS keyword
     */
    public void setNAXIS(int nax) { super.setNaxes(this,nax); }

    /**
     * insert the name and type information into a metadata list
     * @param md   the Metadata list to insert value into
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CTYPE keyword
     */
    public static void setCTYPE(Metadata md, int axis, String value) { 
	boolean isSpherical = false;
	String tmp;
	AxisPosFormatter frmtr;

	if (axis < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested FITS axis=" + 
					   axis + " < 1");
	String in = value.trim();

	// save the value as its label
	setAxisLabel(md, axis-1, in); 

	// determine its type
	if (in.startsWith("RA-")   || in.equals("RA") ) {
	    isSpherical = true;
	    setAxisName(md, axis-1, "right ascension");
	    setAxisType(md, axis-1, "longitude");
	    setAxisFormatter(md, axis-1, defaultRAFormatter);
	}
	else if (in.startsWith("DEC-")  || in.equals("DEC")) {
	    isSpherical = true;
	    setAxisName(md, axis-1, "declination");
	    setAxisType(md, axis-1, "latitude");
	    setAxisFormatter(md, axis-1, defaultDecFormatter);
	}
	else if (in.startsWith("LON-", 1) || 
		 (in.length() == 4 && in.endsWith("LON")) ||
	         in.equals("LONG"))
	{
	    isSpherical = true;
	    setAxisType(md, axis-1, "longitude");
	    setAxisFormatter(md, axis-1, defaultLinearFormatter);
	    tmp = getLONLATName(in.charAt(0));
	    if (tmp == null) tmp = in;
	    setAxisName(md, axis-1, tmp);
	}
	else if (in.startsWith("LAT-", 1) || 
		 ((in.length() == 4 || in.length() == 3) && 
		  in.endsWith("LAT")) )
	{
	    isSpherical = true;
	    setAxisType(md, axis-1, "latitude");
	    setAxisFormatter(md, axis-1, defaultLinearFormatter);
	    tmp = getLONLATName(in.charAt(0));
	    if (tmp == null) tmp = in;
	    setAxisName(md, axis-1, tmp);
	}
	else {
	    setAxisType(md, axis-1, "linear");
	    if (in.startsWith("VELO") || in.startsWith("FELO")) {
		setAxisName(md, axis-1, "velocity");
		frmtr = new MetricVelocityFormatter();
		setAxisFormatter(md, axis-1, frmtr);
		if ((tmp = parseAfterDash(in)) != null) {
		    setVeloStandard(md, axis, tmp);
		}
	    }
	    else if (in.startsWith("FREQ")) {
		setAxisName(md, axis-1, "frequency");
		frmtr = new FreqAxisPosFormatter();
		setAxisFormatter(md, axis-1, frmtr);
	    }
	    else {
		setAxisName(md, axis-1, in);
		setAxisFormatter(md, axis-1, defaultLinearFormatter);
	    }
	}

	// determine the projection if necessary
	if (isSpherical && (tmp = parseAfterDash(in)) != null) {
	    String prev = null;
	    try {
		prev = (String) md.getMetadatum("projection");
	    } catch (ClassCastException ex) { prev = null; }
	    if (tmp != null) {
		if (prev != null && ! prev.equals(tmp)) 
		    System.err.println("Warning: overriding previous " +
				       "projection type");
		md.put("projection", tmp);
	    } 
	}
    }	

    /**
     * insert the name and type information into this metadata list
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CTYPE keyword
     */
    public void setCTYPE(int axis, String in) { setCTYPE(this, axis, in); }

    /**
     * return the name horizon gives to various kinds of longitude and 
     * latitude based on the first letter of the CTYPE string.  This
     * method assumes that the CTYPE is of the form "xLON-..." or 
     * "xLAT-...", where x is one of { G, E, H, S }.
     */
    protected static String getLONLATName(char c) {
	String name = null;

	switch (c) {
	case 'G':
	    name = "Galactic longitude";
	    break;
	case 'E':
	    name = "Ecliptic longitude";
	    break;
	case 'H':
	    name = "helioelcliptic longitude";
	    break;
	case 'S':
	    name = "supergalactic longitude";
	    break;
	default:
	    break;
	}

	return name;
    }

    /**
     * return the string appearing after one or more dashes in a string
     * @param ctype  the string to parse, usually the value of the 
     *               CTYPE keyword
     */
    public static String parseAfterDash(String ctype) {
	int cp = ctype.indexOf('-');

	if (cp < 0) return null;

	while(cp < ctype.length() && ctype.charAt(cp) == '-') cp++;
	return ((cp < ctype.length()) ? ctype.substring(cp) : null);
    }	

    /**
     * insert an axis reference value into a metadata list
     * @param md   the Metadata list to insert value into
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CRVAL keyword
     */
    public static void setCRVAL(Metadata md, int axis, double in) { 
	if (axis < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested FITS axis=" + 
					   axis + " < 1");

	setAxisRefvalue(md, axis-1, in); 
    }

    /**
     * insert an axis reference value into this metadata list
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CRVAL keyword
     */
    public void setCRVAL(int axis, double in) { setCRVAL(this, axis, in); }

    /**
     * insert an axis reference data position into a metadata list
     * @param md   the Metadata list to insert value into
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CRPIX keyword
     */
    public static void setCRPIX(Metadata md, int axis, double in) { 
	if (axis < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested FITS axis=" + 
					   axis + " < 1");
	setAxisRefposition(md, axis-1, in); 
    }

    /**
     * insert an axis reference data position into this metadata list
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CRPIX keyword
     */
    public void setCRPIX(int axis, double in) { setCRPIX(this, axis, in); }

    /**
     * insert a voxel width into a metadata list
     * @param md   the Metadata list to insert value into
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CDELT keyword
     */
    public static void setCDELT(Metadata md, int axis, double in) { 
	if (axis < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested FITS axis=" + 
					   axis + " < 1");
	setAxisStepsize(md, axis-1, in);
    }

    /**
     * insert a voxel width into this metadata list
     * @param axis the axis number (first axis is 1)
     * @param in   the value of the CDELT keyword
     */
    public void setCDELT(int axis, double in) { setCDELT(this, axis, in); }

    /**
     * insert an axis rotation into a metadata list; since the CROTA
     * keyword is being phased out in favor of a PC Matrix, one should
     * run modernize() on the metadata list after all metadata are loaded
     * if this method was called.
     * @param md   the Metadata list to insert value into
     * @param axis the axis the standard is associated with
     * @param in   the value of the CROTA keyword
     */
    public static void setCROTA(Metadata md, int axis, double in) {
	if (axis < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested FITS axis=" + 
					   axis + " < 1");
	setAxisObject(md, axis-1, "CROTA", new Double(in));
    }

    /**
     * insert an axis rotation into this metadata list; since the CROTA
     * keyword is being phased out in favor of a PC Matrix, one should
     * run modernize() on the metadata list after all metadata are loaded
     * if this method was called.
     * @param md   the Metadata list to insert value into
     * @param axis the axis the standard is associated with
     * @param in   the value of the CROTA keyword
     */
    public void setCROTA(int axis, double in) { setCROTA(this, axis, in); }

    /**
     * set the value of the velocity standard in use for specific axis
     * @param md   the Metadata list to insert value into
     * @param axis the axis the standard is associated with
     * @param in   the code for the standard; e.g. "LSR", "HEL", "OBS".
     */
    public static void setVeloStandard(Metadata md, int axis, String in) {
	if (axis < 1) throw new 
	    ArrayIndexOutOfBoundsException("Requested FITS axis=" + 
					   axis + " < 1");
	setAxisObject(md, axis-1, "velocityStandard", in);
    }

    /**
     * set the value of the velocity standard in use for specific axis
     * @param axis the axis the standard is associated with
     * @param in   the code for the standard; e.g. "LSR", "HEL", "OBS".
     */
    public void setVeloStandard(int axis, String in) {
	setVeloStandard(this, axis, in);
    }

    /**
     * set the value of the default velocity standard for the entire
     * coordinate system
     * @param md   the Metadata list to insert value into
     * @param in   the code for the standard; e.g. "LSR", "HEL", "OBS".
     */
    public static void setVeloStandard(Metadata md, String in) {
	md.put("velocityStandard", in);
    }

    /**
     * set the value of the default velocity standard for the entire
     * coordinate system
     * @param in   the code for the standard; e.g. "LSR", "HEL", "OBS".
     */
    public void setVeloStandard(String in) {
	setVeloStandard(this, in);
    }

    /**
     * a the value of FITS keyword LONGPOLE into a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   value of the FITS keyword LONGPOLE
     */
    public static void setLONGPOLE(Metadata md, double longpole) {
	md.put("longpole", new Double(longpole));
    }

    /**
     * insert the value of FITS keyword LONGPOLE into this metadata list
     * @param in   value of the FITS keyword LONGPOLE
     */
    public void setLONGPOLE(double longpole) { setLONGPOLE(this, longpole); }

    /**
     * a the value of FITS keyword LATPOLE into a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   value of the FITS keyword LATPOLE
     */
    public static void setLATPOLE(Metadata md, double latpole) {
	md.put("latpole", new Double(latpole));
    }

    /**
     * insert the value of FITS keyword LATPOLE into this metadata list
     * @param in   value of the FITS keyword LATPOLE
     */
    public void setLATPOLE(double latpole) { setLATPOLE(this, latpole); }

    /**
     * insert a PC matrix into a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   an array of NAXISxNAXIS elements, in order of 
     *             (PC001001, PC001002, ..., PC002001, PC002002, ...)
     * @exception IllegalArgumentException if in.length is not a perfect
     *                                     square
     */
    public static void setPCMatrix(Metadata md, double[] in) {
	double order = Math.sqrt(in.length);
	if (order != Math.floor(order)) 
	    throw new IllegalArgumentException("PCMatrix is not square");
	int nax = getNaxes(md);
	if (nax != order) 
	    System.err.println("Warning: incorrect size of PC matrix = " +
			       order + " for number of NAXIS=" + nax);

	Metavector pcv = new Metavector(in.length);
	pcv.setSize(in.length);
	for(int i=0; i < in.length; i++) 
	    pcv.setElementAt(new Double(in[i]), i);

	md.put("SkewRotate", pcv);
    }
	
    /**
     * insert a PC matrix into this metadata list
     * @param in   an array of NAXISxNAXIS elements, in order of 
     *             (PC001001, PC001002, ..., PC002001, PC002002, ...)
     * @exception IllegalArgumentException if in.length is not a perfect
     *                                     square
     */
    public void setPCMatrix(double[] in) { setPCMatrix(this, in); }
    
    /**
     * insert an element of the PC matrix into a metadata list.  The 
     * proper number of axes should be set before calling this method
     * @param md   the Metadata list to insert value into
     * @param i    the matrix row; e.g. 3 for PC003002
     * @param j    the matrix column; e.g. 2 for PC003002
     * @param in   the PC matrix element
     * @exception ArrayIndexOutOfBoundsException i or j < 1 or > NAXIS
     */
    public static void setPCMatrix(Metadata md, int i, int j, double in) {
	Metavector pcv;

	if (i < 1) throw new 
		       ArrayIndexOutOfBoundsException("row " + i + " < 0");
	if (j < 1) throw new 
		       ArrayIndexOutOfBoundsException("column " + j + " < 0");

	int nax = getNaxes(md);
	if (i > nax) throw new ArrayIndexOutOfBoundsException(
	                                      "row " + i + " > NAXIS=" + nax);
	if (j > nax) throw new ArrayIndexOutOfBoundsException(
	                                   "column " + j + " > NAXIS=" + nax);

	try {
	    pcv = (Metavector) md.getMetadatum("SkewRotate");
	}
	catch (ClassCastException ex) { pcv = null; }

	if (pcv != null && pcv.size() != nax*nax) {
	    System.err.println("Warning: replacing previous matrix of " +
			       "incorrect size");
	    pcv = null;
	}

	if (pcv == null) pcv = getIdentityPCMatrix(nax);

	pcv.setElementAt(new Double(in), (i-1)*nax+(j-1));
	md.put("SkewRotate", pcv);
    }

    /**
     * insert an element of the PC matrix into a metadata list.  The 
     * proper number of axes should be set before calling this method
     * @param i    the matrix row; e.g. 3 for PC003002
     * @param j    the matrix column; e.g. 2 for PC003002
     * @param in   the PC matrix element
     * @exception ArrayIndexOutOfBoundsException i or j < 1 or > NAXIS
     */
    public void setPCMatrix(int i, int j, double in) { 
	setPCMatrix(this, i, j, in);
    }

    /**
     * return a Metavector object containing a PCMatrix whose values
     * are equal to 1 on the diagonal and 0 otherwise.
     * @param n    the dimensionality of the matrix; output matrix 
     *             will be n x n in size.
     */
    public static Metavector getIdentityPCMatrix(int n) {
	if (n <= 0) throw new ArrayIndexOutOfBoundsException(n + "<= 0");

	Double zero = new Double(0.0),
	       one  = new Double(1.0);
	int i, sz = n*n;
	Metavector out = new Metavector(sz);
	out.setSize(sz);

	for(i=0; i < sz; i++) out.setElementAt(zero, i);
	for(i=0; i < n;  i++) out.setElementAt(one, i*n+i);

	return out;
    }

    /**
     * insert an array of projection parameters in a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   an array containing the projection parameters
     */
    public static void setPROJP(Metadata md, double[] in) {
	Metavector prjp = new Metavector(in.length);
	prjp.setSize(in.length);
	for(int i=0; i < in.length; i++) 
	    prjp.setElementAt(new Double(in[i]), i);
	md.put("ProjectionParameters", prjp);
    }

    /**
     * insert an array of projection parameters in this metadata list
     * @param in   an array containing the projection parameters
     */
    public void setPROJP(double[] in) { setPROJP(this, in); }

    /**
     * insert an element of the array of projection parameters
     * @param md   the Metadata list to insert value into
     * @param i    the array element to insert; e.g. 3 for PROJP3
     * @param in   the value of the parameter
     * @exception ArrayIndexOutOfBoundsException i < 1
     */
    public static void setPROJP(Metadata md, int i, double in) 
	throws ArrayIndexOutOfBoundsException
    { 
	Metavector projp;

	int nax = getNaxes(md);
	if (i < 1) throw new ArrayIndexOutOfBoundsException("i=" + i + " < 1");

	try {
	    projp = (Metavector) md.getMetadatum("ProjectionParameters");
	}
	catch (ClassCastException ex) { projp = null; }

	if (projp == null) {
	    projp = new Metavector(i);
	    Double zero = new Double(0.0);
	    for(int j=0; j<i-1; j++) projp.setElementAt(zero, j);
	}

	projp.setElementAt(new Double(in), i-1);
	md.put("ProjectionParameters", projp);
    }

    /**
     * insert an element of the array of projection parameters
     * @param md   the Metadata list to insert value into
     * @param i    the array element to insert; e.g. 3 for PROJP3
     * @param in   the value of the parameter
     * @exception ArrayIndexOutOfBoundsException i < 1
     */
    public void setPROJP(int i, double in) { setPROJP(this, i, in); }

    /**
     * insert the value of the RADECSYS keyword into a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   the value of the RADECSYS keyword
     */
    public static void setRADECSYS(Metadata md, String in) {
	md.put("RADECSYS", in);
    }
	
    /**
     * insert the value of the RADECSYS keyword into this metadata list
     * @param md   the Metadata list to insert value into
     * @param in   the value of the RADECSYS keyword
     */
    public void setRADECSYS(String in) { setRADECSYS(this, in); }

    /**
     * insert the value of the EQUINOX keyword into a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   the value of the EQUINOX keyword
     */
    public static void setEQUINOX(Metadata md, double in) {
	md.put("EQUINOX", new Double(in));
    }
	
    /**
     * insert the value of the EQUINOX keyword into this metadata list
     * @param md   the Metadata list to insert value into
     * @param in   the value of the EQUINOX keyword
     */
    public void setEQUINOX(double in) { setEQUINOX(this, in); }

    /**
     * insert the value of the EPOCH keyword into a metadata list
     * @param md   the Metadata list to insert value into
     * @param in   the value of the EPOCH keyword
     */
    public static void setEPOCH(Metadata md, double in) {
	md.put("EPOCH", new Double(in));
    }
	
    /**
     * insert the value of the EPOCH keyword into this metadata list
     * @param md   the Metadata list to insert value into
     * @param in   the value of the EPOCH keyword
     */
    public void setEPOCH(double in) { setEPOCH(this, in); }

    /**
     * convert deprecated metadata into their modern alternatives.  
     * Currently, this includes converting CROTAi values into a PC matrix
     * and converting the NCP projection to SIN.  Note that if NCP is found 
     * to be the projection type, any previously existing projection 
     * parameters will be overriden.  On the other hand, if both a PC matrix 
     * and CROTA values are found, the PC matrix will not be overriden unless
     * it is found to be of the wrong dimensionality (i.e. != NAXIS^2).
     * @param md   the Metadata list to be updated
     */
    public static void modernize(Metadata md) {

	// this is needed for both conversions
	Integer latax = axisExactlyMatchingType(md, "latitude"),
	        longax = axisExactlyMatchingType(md, "longitude");
	int ltax=0, lnax=0;
	if (longax != null) lnax = longax.intValue();
	if (latax != null)  ltax =  latax.intValue();

	// convert CROTA to PC matrix
	//
	// check out old PC matrix if we have one
	Metavector opcm, pcm;
	Double crota, dtmp;
	double cdelti, cdeltj, val;
	int i, nax = getNaxes(md);
	int sz = nax*nax;

	try {
	    opcm = pcm = (Metavector) md.getMetadatum("SkewRotate");
	} catch (ClassCastException ex) { opcm = pcm = null; }
	if (pcm != null && pcm.size() < sz) pcm = null;

	// look for CROTA keywords
	if (pcm == null) {
	    int found = 0;
	    Vector crv = new Vector(nax);
	    crv.setSize(nax);
	    for(i=0; i < nax; i++) {
		try {
		    crota = (Double) md.getMetadatum("Axes[" + i + "].CROTA");
		} catch (ClassCastException ex) { crota = null; }
		if (crota != null) {
		    crv.setElementAt(crota, i);
		    found++;
		}
	    }

	    // create the proper PC matrix; right now we only understand
	    // CROTA values associated with longitude and latitude axes
	    if (found > 0 && longax != null && latax != null) {
		if ((crota = (Double) crv.elementAt(ltax)) == null) {
		    crota = (Double) crv.elementAt(lnax);
		    if (crota != null) 
			crota = new Double(0.0 - crota.doubleValue());
		}
		if (crota != null && crota.doubleValue() == 0) crota = null;

		if (crota != null) {
		    try {
			dtmp = (Double) md.getMetadatum("Axes[" + longax + 
							"].stepsize");
			cdelti = dtmp.doubleValue();
		    } catch (ClassCastException ex) { cdelti = 1.0; }
		    try {
			dtmp = (Double) md.getMetadatum("Axes[" + latax + 
							"].stepsize");
			cdeltj = dtmp.doubleValue();
		    } catch (ClassCastException ex) { cdeltj = 1.0; }

		    pcm = getIdentityPCMatrix(nax);
		    val = TrigD.cos(crota.doubleValue());
		    pcm.setElementAt(new Double(val), lnax*nax+ltax);
		    val = 0.0 - TrigD.sin(crota.doubleValue()) * cdeltj/cdelti;
		    pcm.setElementAt(new Double(val), lnax*nax+ltax);
		    val = TrigD.sin(crota.doubleValue()) * cdelti/cdeltj;
		    pcm.setElementAt(new Double(val), ltax*nax+lnax);
		    val = TrigD.cos(crota.doubleValue());
		    pcm.setElementAt(new Double(val), ltax*nax+ltax);

		    found--;
		    if (opcm != null) 
			System.err.println("Warning: overriding previous " + 
					   "PC matrix");
		    md.put("SkewRotate", pcm);
		}
		if (found > 0) 
		    System.err.println("Warning: ignoring extra CROTA values");
	    }
	}

	// convert NCP to SIN
	//
	String proj;
	try {
	    proj = (String) md.getMetadatum("projection");
	} catch (ClassCastException ex) { proj = null; }

	if (proj != null && proj.equals("NCP")) {
	    double[] parms = { 0, 0 };
	    Double latncp=null;
	    Integer axis = latax;
	    if (axis != null) {
		try {
		    latncp = (Double) md.getMetadatum("Axes[" + axis + 
						      "].refvalue");
		} 
		catch (ClassCastException ex) {
	          throw new MetadataTypeException("Axes[" + axis + "].refvalue",
						  "Double");
		}
	    }
	    if (latncp != null) {
		parms[1] = 1.0/TrigD.tan(latncp.doubleValue());
		proj = "SIN";
		md.put("projection", proj);
		setPROJP(md, parms);
	    }
	    else {
		System.err.println("Warning: unable to find reference " +
				   " latitude NCP projection;\n" +
				   "         no conversion to SIN done.");
	    }
	}
    }

    /**
     * convert deprecated metadata into their modern alternatives.  
     * Currently, this includes converting CROTAi values into a PC matrix
     * and converting the NCP projection to SIN.  Note that if NCP is found 
     * to be the projection type, any previously existing projection 
     * parameters will be overriden.  On the other hand, if both a PC matrix 
     * and CROTA values are found, the PC matrix will not be overriden unless
     * it is found to be of the wrong dimensionality (i.e. != NAXIS^2).
     * @param md   the Metadata list to be updated
     */
    public void modernize() { modernize(this); }

    /**
     * scan an 80-character line of a FITS header and load the data in 
     * the metadata list if it is recognized as pertaining to the 
     * coordinate system.  The input line need not be strictly 80 characters 
     * long, as long as it contains the contents of only one FITS header 
     * card.  The card is allowed to end in characters indicating the end 
     * of a line (i.e. \n, \r, \m\r).  Since the line may have contained a 
     * deprecated FITS keyword, it is a good idea to run modernize on the 
     * metadata list after all metadata have been loaded.
     * @param md   the Metadata list to insert value into
     * @param  line    the FITS header line
     * @return String  the keyword that was extracted, or null data was not
     *                 saved to the metadata list
     */
    public static String scanHeaderCard(Metadata md, String line) {
	String dums, savedKey;
	Double dumd;
	int i, dumi, dumj;

	// reject any short lines or lines not containing a key-value pair
	if (line.length() < 10 && line.charAt(8) != '=') return null;
	savedKey = line.substring(0,8).trim();

	try {
	    if ((line.substring(0,6)).equals("NAXIS ")) {
		dumi = Integer.parseInt(FITSkeyval(line));
		setNAXIS(md, dumi);
	    }
	    else if ((line.substring(0,5)).equals("CRPIX")) {
		dumi = Integer.parseInt(line.substring(5,8).trim());
		dumd = Double.valueOf(FITSkeyval(line));
		setCRPIX(md, dumi, dumd.doubleValue());
	    }
	    else if ((line.substring(0,5)).equals("CTYPE")) {
		dumi = Integer.parseInt(line.substring(5,8).trim());
		setCTYPE(md, dumi, FITSkeyval(line));
	    }
	    else if ((line.substring(0,5)).equals("CDELT")) {
		dumi = Integer.parseInt(line.substring(5,8).trim());
		dumd = Double.valueOf(FITSkeyval(line));
		setCDELT(md, dumi, dumd.doubleValue());
	    }
	    else if ((line.substring(0,5)).equals("CRVAL")) {
		dumi = Integer.parseInt(line.substring(5,8).trim());
		dumd = Double.valueOf(FITSkeyval(line));
		setCRVAL(md, dumi, dumd.doubleValue());
	    }
	    else if ((line.substring(0,5)).equals("CROTA")) {
		dumi = Integer.parseInt(line.substring(5,8).trim());
		dumd = Double.valueOf(FITSkeyval(line));
		setCROTA(md, dumi, dumd.doubleValue());
	    }
	    else if ((line.substring(0,5)).equals("PROJP")) {
		dumi = Integer.parseInt(line.substring(5,8).trim());
		dumd = Double.valueOf(FITSkeyval(line));
		setPROJP(md, dumi, dumd.doubleValue());
	    }
	    else if ((line.substring(0,2)).equals("PC")) {
		dums = "0123456789";
		for(i=2; i < 8; i++) {
		    if (dums.indexOf(line.charAt(i)) < 0) return null;
		}
		for(i=2; i < 5 && line.charAt(i) != '0'; i++);
		if (i >= 5) return null;
		dumi = Integer.parseInt(line.substring(i,5));
		for(i=5; i < 8 && line.charAt(i) != '0'; i++);
		if (i >= 8) return null;
		dumj = Integer.parseInt(line.substring(i,8));
		dumd = Double.valueOf(FITSkeyval(line));
		setPCMatrix(md, dumi, dumj, dumd.doubleValue());
	    }
	    else if ((line.substring(0,8)).equals("RADECSYS")) {
		setRADECSYS(md, FITSkeyval(line));
	    }
	    else if ((line.substring(0,8)).equals("EQUINOX ")) {
		dumd = Double.valueOf(FITSkeyval(line));
		setEQUINOX(md, dumd.doubleValue());
	    }
	    else if ((line.substring(0,8)).equals("EPOCH   ")) {
		dumd = Double.valueOf(FITSkeyval(line));
		setEPOCH(md, dumd.doubleValue());
	    }
	    else {
//		System.err.println(savedKey + " (ignored)");
		savedKey = null;
	    }
	}
	catch (NumberFormatException ex) { 
	    System.err.println("Failed to parse: " + line);
	    savedKey = null;
	}

//	if (savedKey != null) System.err.println("Saving " + savedKey);
	return savedKey;
    }	
    
    /**
     * scan an 80-character line of a FITS header and load the data in 
     * this metadata list if it is recognized as pertaining to the 
     * coordinate system.  The input line need not be strictly 80 characters 
     * long, as long as it contains the contents of only one FITS header 
     * card.  The card is allowed to end in characters indicating the end 
     * of a line (i.e. \n, \r, \m\r).  Since the line may have contained a 
     * deprecated FITS keyword, it is a good idea to run modernize on the 
     * metadata list after all metadata have been loaded.
     * @param  card    the FITS header line
     * @return String  the keyword that was extracted, or null data was not
     *                 saved to the metadata list
     */
    public String scanHeaderCard(String card) { 
	return scanHeaderCard(this, card); 
    }

    /**
     * scan FITS header and loads all the data recognized as pertaining to 
     * the coordinate system.  Note that this method will run modernize()
     * on the metadata list after all the data have been loaded.
     * @param md       the Metadata list to insert value into
     * @param header   the stream containing the header
     * @return int     the number of lines recognized as containing needed data
     */
    public static int scanHeader(Metadata md, InputStream header) 
	throws IOException
    {
	int found=0, nread;
	int nextByte = 0;
	byte[] buf = new byte[80];
	String line;
	PushbackInputStream strm = new PushbackInputStream(header);

	while ((nread = strm.read(buf)) >= 0) {
	    if (nread == 0) continue;
	    if (nread == 1) nread += strm.read(buf, 1, buf.length-1);

	    line = new String(buf, 0, 0, nread);
//	    System.err.println(line);
	    if (line.startsWith("END") &&
		(line.length() == 3 || 
		 line.charAt(3) == '/' || line.charAt(3) == ' ')) break;
	    if (scanHeaderCard(md, line) != null) found++;

	    // these next two lines support the existance of a carriage
	    // return following an 80-char card; if it is encountered,
	    // it is thrown away.  Note that this is not a standard FITS 
	    // feature
	    nextByte = strm.read();
	    if (nextByte != '\n') strm.unread(nextByte);
	}

	if (found > 0) modernize(md);
	return found;
    }

    /**
     * scan FITS header and loads all the data recognized as pertaining to 
     * the coordinate system.  Note that this method will run modernize()
     * on the metadata list after all the data have been loaded.
     * @param header   the stream containing the header
     * @return int     the number of lines recognized as containing needed data
     */
    public int scanHeader(InputStream header) throws IOException
    { return scanHeader(this, header); }

    /**
     * return the value portion of a key-value pair from FITS header line.
     * If the value is a string, the quotes will be removed.
     */
    public static String FITSkeyval(String line) {
	if (line.charAt(8) != '=') return null;

	String out, use;
	int i, l = line.length();

	// get rid of "key = " portion
	for(i=9; i < l && line.charAt(i) <= ' '; i++);
	if (i >= l) return null;
	use = line.substring(i).trim();
	
	if (use.charAt(0) == '\'') {
	    i = use.indexOf('\'', 1);
	    if (i < 0) 
		out = use.substring(1);
	    else 
		out = use.substring(1,i);
	}
	else {
	    i = use.indexOf('/');
	    if (i >= 0) 
		out = use.substring(0,i).trim();
	    else
		out = use;
	}

	return out;
    }

    /**
     * create the coordinate system described by a metadata set.
     * Currently, the real type of the returned system is 
     * ncsa.horizon.coordinates.systems.SphLinCoordinateSystem.
     * @param md   the Metadata describing the system
     */
    public static CoordinateSystem createCoordSys(Metadata md) 
	throws IllegalTransformException
    {
	modernize(md);
	return new SphLinCoordinateSystem(md);
    }

    /**
     * create the coordinate system described by this metadata set.
     * Currently, the real type of the returned system is 
     * ncsa.horizon.coordinates.systems.SphLinCoordinateSystem.
     */
    public CoordinateSystem createCoordSys() throws IllegalTransformException
    { return createCoordSys(this); }

    public static void main(String args[]) {

	FITSCoordMetadata md = new FITSCoordMetadata();
	String filename = "img.hdr";
	DataInputStream hdr;
	String line;

	try { 
	    hdr = new DataInputStream( new FileInputStream(filename) );
	} catch (FileNotFoundException ex) {
	    throw new InternalError(ex.getMessage());
	}

	try {
	    while ((line = hdr.readLine()) != null) {
		if (line.startsWith("END") &&
		    (line.length() == 3 || 
		     line.charAt(3) == '/' || line.charAt(3) == ' ')) break;

		md.scanHeaderCard(line);
	    }
	}
	catch (IOException e) {
	    throw new InternalError(e.getMessage());
	}

	System.out.println("FITS metadata: " + md);
    }
}
