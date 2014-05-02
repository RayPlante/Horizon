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
 */
package ncsa.horizon.util;

/**
 * an Exception that can be thrown if an unrecognized option has been
 * encountered.  See the CmdLine class for details.
 * 
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @version $Id: UnrecognizedOptionException.java,v 1.1 1997/12/05 02:00:11 rplante Exp $
 */
public class UnrecognizedOptionException extends Exception {

    private Character c = null;

    /**
     * create an exception indicating that an unrecognized option
     * was encountered
     */
    public UnrecognizedOptionException() { super(); }

    /**
     * create an exception indicating that an unrecognized option
     * was encountered
     */
    public UnrecognizedOptionException(char c) { 
	super(); 
	this.c = new Character(c);
    }

    /**
     * create an exception indicating that an unrecognized option
     * was encountered
     */
    public UnrecognizedOptionException(Character C) { 
	super(); 
	this.c = C;
    }

    /**
     * create an exception indicating that an unrecognized option
     * was encountered
     */
    public UnrecognizedOptionException(String str) { super(str); }

    public String toString() { return getMessage(); }

    public String getMessage() { 
	if (c == null) 
	    return super.getMessage();
	else 
	    return new String("Unrecognized option: -" + c);
    }
}

