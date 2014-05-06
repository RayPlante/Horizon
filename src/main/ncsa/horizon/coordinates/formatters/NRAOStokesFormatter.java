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
 */
package ncsa.horizon.coordinates.formatters;

import java.util.*;
import ncsa.horizon.coordinates.*;

/**
 * support for printing out Stokes axis positions by their two-character
 * polarization codes, according to the NRAO Stokes Paramters Convention.
 * <p>
 *
 * The convention is as follows:
 * <pre>
 * pixel position:  -7  -6  -5  -4  -3  -2  -1   0   1   2   3   4
 * Stokes code:     YX  XY  YY  XX  LR  RL  LL  RR   I   Q   U   V
 * </pre>
 *
 * The I, Q, U, and V indicate the standard Stokes optical polarizations. 
 * That is, the values a the pixel position 
 * The other codes indicate 
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: NRAOStokesFormatter.java,v 1.1 1997/08/07 07:30:50 rplante Exp $
 */
public class NRAOStokesFormatter extends GenericAxisPosFormatter {

    /**
     * the supported polarization codes:
     */
    public final static String[] polcodes = { "Unknown", 
					      "YX", "XY", "YY", "XX", 
					      "LR", "RL", "LL", "RR", 
					      "I",   "Q",  "U",  "V"  };

    public NRAOStokesFormatter() { super(); }

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     */
    public String toString(double val) { 
	int ci = (int) Math.floor(val + 8.5);
	if (ci < 0 || ci >= polcodes.length) ci = 0;
	return polcodes[ci];
    }

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     * @param prec the number of places right of the decimal point
     */
    public String toString(double val, int prec) { 
	return toString(val);
    }

    /**
     * parse the String representation of a floating point number
     */
    public double valueOf(String s) throws NumberFormatException {
	int i;

	for(i=0; i < polcodes.length && ! s.equals(polcodes[i]); i++);
	if (i >= polcodes.length) 
	    throw new NumberFormatException("No polarization code matching " +
					    s);
	    
	return (i-8.5);
    }

    public Object clone() { return super.clone(); }

    protected static final String myname = 
        "NRAO Stokes Coordinate Axis Position Formatter";
    public String toString() { return myname; }
};

