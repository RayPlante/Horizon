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
 *  97aug08  rlp  extra documentation, made a RuntimeException
 *
 */
package ncsa.horizon.util;

/**
 * an exception indicating that a value from a 
 * <a href="ncsa.horizon.util.Metadata.html">Metadata</a> list was of an
 * unexpected type.
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: MetadataTypeException.java,v 0.5 1997/08/07 09:15:02 rplante Exp $
 */
public class MetadataTypeException extends RuntimeException {

    private String item, type_expected, type_found;

    public MetadataTypeException() { super("Incorrect type for Metadatum"); }

    /**
     * @param mdname metadatum name
     */
    public MetadataTypeException(String mdname) { 
	super("Incorrect type for Metadatum"); 
	item = mdname;
    }

    /**
     * @param mdname metadatum name
     * @param expected the type expected
     */
    public MetadataTypeException(String mdname, String expected) {
	super("Incorrect type for Metadatum"); 
	item = mdname;
	type_expected = expected;
    }

    /**
     * @param mdname metadatum name
     * @param expected the type expected
     */
    public MetadataTypeException(String mdname, String expected, String found) {
	super("Incorrect type for Metadatum"); 
	item = mdname;
	type_expected = expected;
	type_found = found;
    }

    public String toString() { return item; }

    public String getMessage() {
	StringBuffer out = new StringBuffer();
	out.append(super.getMessage());

	if (item != null) out.append(" ").append(item);
	if (type_expected != null) 
	    out.append("; expected ").append(type_expected);
	if (type_found != null) 
	    out.append(", found ").append(type_found);
	out.append(".");

	return out.toString();
    }
}
