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
package ncsa.horizon.coordinates.formatters;

import ncsa.horizon.coordinates.*;
import Acme.Fmt;

/**
 * support for printing out double values as floating point numbers.
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: GenericAxisPosFormatter.java,v 1.1 1997/08/07 07:30:50 rplante Exp $
 */
public class GenericAxisPosFormatter implements AxisPosFormatter, Cloneable {
    public GenericAxisPosFormatter() { super(); }

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     */
    public String toString(double val) { return Double.toString(val); }

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     * @param prec the number of places right of the decimal point
     */
    public String toString(double val, int prec) { 

	// determine number of places to appear left of decimal
	String tmp = Fmt.fmt(Math.abs(val), 0, 0, Fmt.LJ);
	int p = tmp.indexOf('.');
	int e = tmp.indexOf('E');
	if (p < 0) {
	    p = (e < 0) ? tmp.length() : e;
	} 

	int sigfigs = p + prec;
	int minwidth = sigfigs + 1;
	return Fmt.fmt(val, minwidth, sigfigs, Fmt.ZF);
    }

    /**
     * parse the String representation of a floating point number
     */
    public double valueOf(String s) throws NumberFormatException {
	Double out = Double.valueOf(s);
	return out.doubleValue();
    }

    public Object clone() { 
	try {
	    return super.clone(); 
	} catch (CloneNotSupportedException ex) {
	    // should not happen
	    throw new InternalError(ex.getMessage());
	}
    }

    public static void main(String args[]) {
	int i;
	AxisPosFormatter cap = new GenericAxisPosFormatter();

	double mine = 152.2345;
	System.out.println("My position: " + cap.toString(mine));

	double yours;
	for (i=0; i < args.length; i++) {
	    yours = new Double(args[i]).doubleValue();
	    System.out.println("Your position: " + cap.toString(yours));
	}
    }

    protected static final String myname = 
        "Generic Coordinate Axis Position Formatter";
    public String toString() { return myname; }
};

