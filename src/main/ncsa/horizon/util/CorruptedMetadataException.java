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
 */
package ncsa.horizon.util;

public class CorruptedMetadataException extends RuntimeException {
    public CorruptedMetadataException(String s) {
	super(s);
    }

    public CorruptedMetadataException(String s, String m) {
	super(s + ": " +  m);
    }

    public CorruptedMetadataException() {
	super();
    }
}
