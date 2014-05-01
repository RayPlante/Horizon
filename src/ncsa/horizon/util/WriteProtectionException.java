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

public class WriteProtectionException extends Exception {

    private String classnm;

    public WriteProtectionException() { super(); }

    /**
     * @param classname object reference name, class name, or method name 
     *        of for object throwing the exception
     */
    public WriteProtectionException(String classname) { 
	super("Attempt to alter write-protected data"); 
	classnm = new String(classname);
    }

    /**
     * @param classname object reference name, class name, or method name 
     *        of for object throwing the exception
     * @param detailed message
     */
    public WriteProtectionException(String classname, String message) { 
	super(message); 
	classnm = new String(classname);
    }

    public String toString() { return classnm; }

    public String getMessage() { 
	StringBuffer out = new StringBuffer();
	String msg = toString();

	if (classnm != null) {
	    out.append(classnm);
	    if (msg != null && msg.length() > 0) out.append(": ");
	}
	if (msg != null && msg.length() > 0) out.append(msg);
	
	return out.toString();
    }
}

