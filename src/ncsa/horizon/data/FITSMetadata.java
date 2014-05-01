/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1997, Board of Trustees of the University of Illinois
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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  97nov19  rlp  Original version
 *  97dec10  rlp  Fixed bug reading header cards that look like PC and
 *                   CD matrix cards.
 *  98jan19  rlp  moved from ncsa.horizon.viewable to ncsa.horizon.data
 *  98feb02  rlp  now looks for BZERO, BSCALE, BLANK by default
 */
package ncsa.horizon.data;

import java.util.*;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import ncsa.horizon.util.*;
import ncsa.horizon.coordinates.FITSCoordMetadata;
import ncsa.horizon.coordinates.CoordMetadata;

/**
 * a Metadata class for loading horizon and FITS  metadata from a FITS 
 * image file. <p>
 *
 * This class allows one to simply scan a FITS header and then (semi-) 
 * automatically have horizon metadata (i.e. metadat in the "horizon" 
 * schema) created based on the FITS header contents.  Among the horzion
 * metadata created is "NativeMetadata", which is a metadata list and which 
 * contains a subset of the FITS header data (which subset depends on how 
 * the header is scanned). <p>
 * 
 * This class offers several ways to scan a header.  First, one can
 * provide the scanHeader() method with an InputStream.  This will read
 * data from the stream until the "END" FITS header card is read, or the
 * end of the stream is reached.  When the method returns, the metadata
 * list will contain all the horizon metadata that can be created from the 
 * header.  Unless the boolean argument to this method is true, the 
 * "NativeMetadata" will only contain the FITS keywords that were needed 
 * to create the horizon metadata.  <p>
 * 
 * Alternatively, one can examine each FITS header card individually using
 * scanHeaderCard().  In this case, one would pass each header card (perhaps
 * within a processing loop) to this method.  Unless the boolean argument
 * to this method is true, the datum in the card will only be loaded (as 
 * native metadata) if it is needed to create horizon metadata.  After all 
 * cards have been read, one should call the setHorizonMetadata() method.  
 * This will use the native metadata that have been loaded to create the 
 * horizon metadata.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: FITSMetadata.java,v 1.4 1998/02/03 03:56:02 rplante Exp $
 */
public class FITSMetadata extends Metadata {

    /**
     * a list of the FITS keywords that are needed to create horizon
     * metadata
     */
    public final static String[] neededKeys = {
	"BITPIX", "BLANK", "BUNIT", "BSCALE", "BZERO", "CD", "CRPIX", 
	"CRVAL", "CTYPE", "EQUINOX ", "EPOCH ", "NAXIS", "PROJP", "PC", 
	"RADECSYS", "SIMPLE"
    };

    protected Metadata fnative = null;

    /**
     * Create an initialized (but otherwise empty) Metadata object
     */
    public FITSMetadata() { initMetadata(); }

    /**
     * Create a Metadata object and load the horizon metadata that can
     * be gleaned from an input stream.
     * @param header         a stream set at the beginning of the FITS header
     * @param loadAllNative  if true, all FITS key-value pairs should be 
     *                       loaded into the "NativeMetadata" sub metadata 
     *                       list; otherwise, only the needed data to create 
     *                       horizon metadata will be loaded.
     */
    public FITSMetadata(InputStream header, boolean loadAllNative) 
	throws IOException
    { 
	initMetadata();
	scanHeader(header, loadAllNative);
    }

    /**
     * Create a Metadata object and load the horizon metadata that can
     * be gleaned from a metadata object containing native FITS metadata
     */ 
    public FITSMetadata(Metadata FITSmd) { 
	initMetadata();
	fnative = (Metadata) FITSmd.clone();
	initNativeMetadata(fnative);
	convertToHorizon(this, FITSmd);
    }

    /** 
     * initialize this Metadata object.  Currently sets the schema and
     * and schemaVersion metadata identifying the use of the "horizon"
     * schema, and sets "yaxisReversed" to true.
     */
    protected void initMetadata() {

	// set the schema
	setSchema(CoordMetadata.horizonSchema);
	setSchemaVersion(CoordMetadata.version);

	// set the "yaxisReversed"
	put("yaxisReversed", Boolean.TRUE);
    }

    /** 
     * initialize the Metadata object that will be used to hold the 
     * native FITS metadata.  Currently sets the schema and
     * metadata identifying the use of the "FITS" schema.
     */
    protected static void initNativeMetadata(Metadata md) {

	// set the schema
	md.setSchema("FITS");
    }

    /**
     * scan an 80-character line of a FITS header and load the data in 
     * the metadata list if it is recognized as necessary for creating 
     * horizon metadata.  The input line need not be strictly 80 characters 
     * long, as long as it contains the contents of only one FITS header 
     * card.  The card is allowed to end in characters indicating the end 
     * of a line (i.e. \n, \r, \m\r).  
     * @param  nativemd  the Metadata list to insert native FITS metadata into
     * @param  line      the FITS header line
     * @param  alwaysLoadAsNative   if true, load the metadatum contained
     *                   in the card as a native metadatum, regardless of 
     *                   whether is needed to create horizon metadata
     * @return String    the keyword that was extracted, or null data if it
     *                   is not needed to create horizon metadata
     */
    public static String scanHeaderCard(Metadata nativemd, String line, 
					boolean alwaysLoadAsNative) {
	String dums, savedKey;
	Double dumd;
	int i, dumi, dumj;
	boolean save = false;

	// ignore an END card
	if (line.startsWith("END") && 
	    (line.length() == 3     || line.charAt(3) == ' ' || 
	     line.charAt(3) == '\n' || line.charAt(3) == '/'   )) return null;

	// assume that any short line or line containting a key-value pair
	// is part of the history
	if (line.length() < 10 && line.charAt(8) != '=') {
	    if (alwaysLoadAsNative) addToHistory(nativemd, line);
	    return null;
	}
	savedKey = line.substring(0,8).trim();

	// load the metadata if it is needed
	save = needFITSKey(savedKey);
	if (alwaysLoadAsNative || save) loadHeaderCard(nativemd, line);

	return ((save) ? savedKey : null);
    }	
    
    /**
     * scan an 80-character line of a FITS header and load the data in 
     * the native metadata list  ("NativeMetadata") of this metadata list 
     * if it is recognized as necessary for creating horizon metadata.
     * The input line need not be strictly 80 characters long, as long as 
     * it contains the contents of only one FITS header card.  The card 
     * is allowed to end in characters indicating the end of a line 
     * (i.e. \n, \r, \m\r).  
     * @param  line      the FITS header line
     * @param  alwaysLoadAsNative   if true, load the metadatum contained
     *                   in the card as a native metadatum, regardless of 
     *                   whether is needed to create horizon metadata
     * @return String    the keyword that was extracted, or null data if it
     *                   is not needed to create horizon metadata
     */
    public String scanHeaderCard(String line, boolean alwaysLoadAsNative) {
	if (fnative == null) checkNativeMetadata();
	return scanHeaderCard(fnative, line, alwaysLoadAsNative);
    }

    /**
     * scan FITS header and convert the relevent information found in it to 
     * horizon metadata.  
     * @param md       the Metadata list to insert values into
     * @param header   the stream containing the header
     * @param loadAllNative   if true, all FITS key-value pairs should be 
     *                 loaded into the "NativeMetadata" sub metadata list;
     *                 otherwise, only the needed data to create horizon 
     *                 metadata will be loaded.
     * @return int     the number of lines recognized as containing needed data
     */
    public static int scanHeader(Metadata md, InputStream header, 
				 boolean loadAllNative) 
	throws IOException
    {
	int found=0, nread;
	int nextByte = 0;
	byte[] buf = new byte[80];
	String line;
	Metadata FITSmd = null;
	PushbackInputStream strm = new PushbackInputStream(header);

	try {
	    FITSmd = (Metadata) md.getMetadatum("NativeMetadata");
	} catch (ClassCastException ex) { FITSmd = null; }
	if (FITSmd == null) {
	    FITSmd = new Metadata();
	    initNativeMetadata(FITSmd);
	}

	while ((nread = strm.read(buf)) >= 0) {
	    if (nread == 0) continue;
	    if (nread == 1) nread += strm.read(buf, 1, buf.length-1);

	    line = new String(buf, 0, 0, nread);
//	    System.err.println(line);
	    if (line.startsWith("END") &&
		(line.length() == 3 || 
		 line.charAt(3) == '/' || line.charAt(3) == ' ')) break;
	    if (scanHeaderCard(FITSmd, line, loadAllNative) != null) found++;

	    // these next two lines support the existance of a carriage
	    // return following an 80-char card; if it is encountered,
	    // it is thrown away.  Note that this is not a standard FITS 
	    // feature
	    nextByte = strm.read();
	    if (nextByte != '\n') strm.unread(nextByte);
	}

//	System.err.println("Found " + found + " keys");
	if (found > 0) convertToHorizon(FITSmd, md);
	return found;
    }

    /**
     * scan FITS header and convert the relevent information found in it to 
     * horizon metadata, storing it in this list.
     * @param header   the stream containing the header
     * @param loadAllNative   if true, all FITS key-value pairs should be 
     *                 loaded into the "NativeMetadata" sub metadata list;
     *                 otherwise, only the needed data to create horizon 
     *                 metadata will be loaded.
     * @return int     the number of lines recognized as containing needed data
     */
    public int scanHeader(InputStream header, boolean loadAllNative) 
	throws IOException
    {  return scanHeader(this, header, loadAllNative); }

    private void checkNativeMetadata() {
	if (fnative == null) {
	    try {
		fnative = (Metadata) getMetadatum("NativeMetadata");
	    } catch (ClassCastException ex) { fnative = null; }
	    if (fnative == null) {
		fnative = new Metadata();
		initNativeMetadata(fnative);
		put("NativeMetadata", fnative);
	    }
	}
    }

    /**
     * load the FITS metadatum found in the given FITS header card into
     * a metadata list as native FITS metadata
     */
    public static void loadHeaderCard(Metadata FITSmd, String line) {
	String key;
	Object val;

	// assume that any short line or line containting a key-value pair
	// is part of the history
	if (line.length() < 10 && line.charAt(8) != '=') {
	    addToHistory(FITSmd, line);
	    return;
	}

	key = line.substring(0,8).trim();
	val = FITSkeyval(line);
//	System.err.println("loading " + key + " with " + val);
	if (val != null) 
	    FITSmd.put(key, val);
	else
	    addToHistory(FITSmd, line);
    }

    /**
     * load the FITS metadatum found in the given FITS header card into
     * the native metadata list ("NativeMetadata") from  this metadata list
     */
    public void loadHeaderCard(String line) { 
	if (fnative == null) checkNativeMetadata();
	loadHeaderCard(fnative, line); 
    }
	
    /**
     * return the value portion of a key-value pair from a FITS header
     * card.  The returned Object will either be a String, Double, Integer,
     * Boolean, or null if a valid key-value pair is not found.
     */
    public static Object FITSkeyval(String line) {
	if (line.charAt(8) != '=') return null;

	String use;
	Object out=null;
	int i, l = line.length();

	// get rid of "key = " portion
	for(i=9; i < l && line.charAt(i) <= ' '; i++);
	if (i >= l) return null;
	use = line.substring(i).trim();

	// is it a string value?
	if (use.charAt(0) == '\'') {
	    i = use.indexOf('\'', 1);
	    if (i < 0) 
		out = use.substring(1);
	    else 
		out = use.substring(1,i);
	}
	else {

	    // get rid of comments 
	    i = use.indexOf('/');
	    if (i >= 0) use = use.substring(0,i).trim();

	    // is it a boolean?
	    if (use.equalsIgnoreCase("T")) {
		out = Boolean.TRUE;
	    }
	    else if (use.equalsIgnoreCase("F")) {
		out = Boolean.FALSE;
	    }

	    // is it an integer or double?
	    else {

		// try it as an Integer
		try {
		    out = new Integer(use);
		} catch (NumberFormatException ie) {

		    // try it as a Double
		    try {
			out = new Double(use);
		    }
		    catch (NumberFormatException de) {

			// no valid value found
			out = null;
		    }
		}
	    }
	}

	return out;
    }

    /**
     * add the contents of the input card to the "historyBuffer" 
     * metadatum of the given metadata list
     * @param nativemd  the metadata list to store the history card into
     * @param card      the history card
     * @exception CorruptedMetadataException thrown if nativemd already
     *      has a "historyBuffer" metadatum of a type other than 
     *      StringBuffer
     */
    public static void addToHistory(Metadata nativemd, String card) 
	throws CorruptedMetadataException
    {
	StringBuffer hbuf = null;
	String use;

	// get the history buffer if it exists
	try {
	    hbuf = (StringBuffer) nativemd.getMetadatum("historyBuffer");
	}
	catch (ClassCastException ex) {
	    throw new CorruptedMetadataException("historyBuffer", 
						 "not of type StringBuffer");
	}
	if (hbuf == null) hbuf = new StringBuffer();

	// Remove preceeding "HISTORY" tag, if it exists
	int len = card.length();
	int st;
	st = (card.startsWith("HISTORY ")) ? 8 : 0;

	// we will put a carriage return at the end of this card
	use = card.substring(st, 79).trim();

	hbuf.append(use);
	hbuf.append("\n");

	nativemd.put("historyBuffer", hbuf);
    }

    /**
     * add the contents of the input card to the "historyBuffer" 
     * metadatum of the native metadata list ("NativeMetadata") within 
     * this metadata.
     * @param card      the history card
     * @exception CorruptedMetadataException thrown if this metadata 
     *      list already has a "historyBuffer" metadatum of a type other 
     *      than StringBuffer
     */
    public void addToHistory(String card) throws CorruptedMetadataException {
	if (fnative == null) checkNativeMetadata();
	addToHistory(fnative, card);
    }

    /**
     * return true if it appears that this FITS keyword is one that is
     * needed to create horizon metadata
     * @param key   the keyword in question
     */
    public static boolean needFITSKey(String key) {
	int i;
	for(i=0; i < neededKeys.length; i++) {
	    if (key.startsWith(neededKeys[i])) break;
	}
	return (i < neededKeys.length);
    }

    /**
     * convert FITS metadata to horizon metadata
     */
    public static void convertToHorizon(Metadata FITSmd, Metadata horizonmd) 
	throws CorruptedMetadataException
    {
	int nax, i, j;
	boolean hasCDELTs = false;
	Double dumd = null;
	Integer dumi;
	String dums;
	FITSCoordMetadata cmdata = null;

	// First query for the number of axes
	try {
	    dumi = (Integer) FITSmd.getMetadatum("NAXIS");
	} catch (ClassCastException ex) {
	    dumi = null;
	}
	if (dumi == null) {
	    System.err.println("Warning: Unable to determine number of axes" +
		               "; assuming 2");
	    dumi = new Integer(2);
	} 

	nax = dumi.intValue();
	if (nax < 1) throw new CorruptedMetadataException("NAXIS < 1");
	horizonmd.put("naxes", dumi);
	cmdata = new FITSCoordMetadata(nax);

	// now determine the size of the image
	double[] sz = new double[nax];
	for(i=1; i <= nax; i++) {
	    dums = new String("NAXIS" + i);
	    try {
		dumi = (Integer) FITSmd.getMetadatum(dums);
	    }
	    catch (ClassCastException ex) {
		dumi = null;
	    }
	    if (dumi == null) dumi = new Integer(1);

	    if (dumi.intValue() < 1) 
		throw new CorruptedMetadataException(dums + " < 1");
	    sz[i-1] = dumi.intValue();
	}
	double[] origin = new double[nax];
	for(i=0; i < nax; i++) origin[i] = 1.0; // data voxels are one-relative
	Volume dv = new Volume(nax, origin, sz, null);
	horizonmd.put("dataVolume", dv);

	// get the measurementUnit = BUNIT
	try {
	    dums = (String) FITSmd.getMetadatum("BUNIT");
	} catch (ClassCastException ex) { dums = null; }
	if (dums != null) horizonmd.put("measurementUnit", dums);

	// The rest are related to the coordinate system
	//
	// reference pixel related data
	for(i=1; i <= nax; i++) {
	    try {
		dums = (String) FITSmd.getMetadatum("CTYPE" + i);
	    } catch (ClassCastException ex) { dums = null; }
	    if (dums != null) cmdata.setCTYPE(i, dums);

	    try {
		dumd = (Double) FITSmd.getMetadatum("CRVAL" + i);
	    } catch (ClassCastException ex) { dumd = null; }
	    if (dumd != null) cmdata.setCRVAL(i, dumd.doubleValue());

	    try {
		dumd = (Double) FITSmd.getMetadatum("CRPIX" + i);
	    } catch (ClassCastException ex) { dumd = null; }
	    if (dumd != null) cmdata.setCRPIX(i, dumd.doubleValue());

	    try {
		dumd = (Double) FITSmd.getMetadatum("CDELT" + i);
		if (dumd != null) hasCDELTs = true;
	    } catch (ClassCastException ex) { dumd = null; }
	    if (dumd == null) dumd = new Double(1.0);
	    cmdata.setCDELT(i, dumd.doubleValue());
	}

	// RADECSYS: the relevent celestial coordinate system
	try {
	    dums = (String) FITSmd.getMetadatum("RADECSYS");
	} catch (ClassCastException ex) { dums = null; }
	if (dums != null) cmdata.setRADECSYS(dums);

/* 
 * the type of these keywords should be double rather than strings
 *
	try {
	    dums = (String) FITSmd.getMetadatum("EQUINOX");
	} catch (ClassCastException ex) { dums = null; }
	if (dums != null) cmdata.setEQUINOX(dums);
 */

	try {
	    dumd = (Double) FITSmd.getMetadatum("EPOCH");
	} catch (ClassCastException ex) { dums = null; }
	if (dumd != null) cmdata.setEPOCH(dumd.doubleValue());

	boolean cdwarnd = false;
	for(Enumeration e = FITSmd.metadatumNames(); e.hasMoreElements(); ) {
	    try {
		dums = (String) e.nextElement();

		// a CD matrix element
		if (dums.startsWith("CD") && dums.length() > 4) {
		    if ((j = dums.indexOf('_')) == 3 && dums.length() == 5) {
			i = Integer.parseInt(dums.substring(2,3));
			i = Integer.parseInt(dums.substring(4,5));
		    } else if (dums.length() == 8) {
			i = Integer.parseInt(dums.substring(2,5));
			j = Integer.parseInt(dums.substring(5,8));
		    } else {
			continue;
		    }

		    if (hasCDELTs) {
			if (! cdwarnd) System.err.println(
			    "Warning: found both CDELTs and CD matrix " +
			    "elements; ignoring CD matrix");
			cdwarnd = true;
			continue;
		    }

		    dumd = (Double) FITSmd.getMetadatum(dums);
		    cmdata.setPCMatrix(i, j, dumd.doubleValue());
		}

		// a PC matrix element
		else if (dums.startsWith("PC") && dums.length() == 8) {
		    i = Integer.parseInt(dums.substring(2,5));
		    j = Integer.parseInt(dums.substring(5,8));
		    dumd = (Double) FITSmd.getMetadatum(dums);
		    cmdata.setPCMatrix(i, j, dumd.doubleValue());
		}

		// a projection parameter
		else if (dums.startsWith("PROJP") && dums.length() > 5) {
		    i = Integer.parseInt(dums.substring(5,dums.length()));
		    dumd = (Double) FITSmd.getMetadatum(dums);
		    cmdata.setPROJP(i, dumd.doubleValue());
		}
	    }
	    catch (ClassCastException ex) { }
	    catch (NumberFormatException ex) { }
	}

	// Now set the coordinate system into place
	cmdata.modernize();
	horizonmd.put("CoordinateSystem", cmdata);

	// Now set the native metadata
	setHistory(FITSmd);
	horizonmd.put("NativeMetadata", FITSmd);
    }

    /**
     * Use the native metadata (stored as the "NativeMetadata" metadatum) 
     * within this list to create the relevant horizon metadata.
     */
    public void setHorizonMetadata() {
	Metadata FITSmd = null;
	try {
	    FITSmd = (Metadata) getMetadatum("NativeMetadata");
	} catch (ClassCastException ex) { FITSmd = null; }
	if (FITSmd != null) fnative = FITSmd;

	if (fnative == null) {
	    fnative = new Metadata();
	    initNativeMetadata(fnative);
	}

	convertToHorizon(fnative, this);
    }

    /**
     * Assume that there is no more History cards to be added and append
     * the contents of the "historyBuffer" (StringBuffer) metadatum to the 
     * "HISTORY" (String) metdatum.
     */
    public static void setHistory(Metadata FITSmd) {
	StringBuffer hbuf;
	String his;

	synchronized (FITSmd) {
	    try {
		hbuf = (StringBuffer) FITSmd.getMetadatum("historyBuffer");
	    } catch (ClassCastException ex) { hbuf = null; }
	    if (hbuf == null) return;

	    try {
		his = (String) FITSmd.getMetadatum("HISTORY");
	    } catch (ClassCastException ex) { his = null; }
	    if (his == null) {
		his = hbuf.toString();
	    } else {
		his = his + hbuf.toString();
	    }

	    FITSmd.put("HISTORY", his);
	    FITSmd.remove("historyBuffer");
	}
    }

    /**
     * Assume that there is no more History cards to be added and append
     * the contents of the "historyBuffer" (StringBuffer) metadatum to the 
     * "HISTORY" (String) metdatum.
     */
    public void setHistory() { setHistory(this); }
}
