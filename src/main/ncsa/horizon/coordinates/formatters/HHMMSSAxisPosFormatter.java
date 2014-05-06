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

import java.util.StringTokenizer;
import java.util.Formatter;
import ncsa.horizon.coordinates.*;

/**
 * support for printing out angles in hours:minutes:seconds format over the
 * circular range 0, 24 hours.
 */
public class HHMMSSAxisPosFormatter implements AxisPosFormatter {

    /** 
     * default precision = 2.  This is default number of digits to 
     * the right of the decimal in the seconds field 
     */
    public final static int DEF_PREC = 2;

    /** 
     * the maximum precision supported = 4.
     */
    public final static int MAX_PREC = 4;

    public HHMMSSAxisPosFormatter() { super(); }

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     * @param prec the number of places right of the decimal point.  
     *             Precision can have the following ranges:
     * <pre>
     *    &lt 2   print with "natural" precision (whatever Java gives it)
     *      -2   only the nearest hour should be printed.  
     *      -1   print value to the nearest minute.
     *       0   print value to the nearest second (with no decimal point).
     *    &gt= 1   print value with prec number of digits right of the
     *           decimal in the seconds field
     * </pre>
     */
    public String toString(double degrees, int prec) { 

	while (degrees < 0) {
	    degrees += 360.0;
	}
	while (degrees >= 360.0) {
	    degrees -= 360.0;
	}
	double hours = degrees/15.0;

	int hh = (int) hours;
	int mm = (int) ( (hours - hh) * 60.0 );
	double ss = (hours - hh - mm/60.0) * 3600.0;

	// round if necessary for requested precision
	if (prec == 0) 
	    ss = Math.rint(ss); 
	else if (prec > 0) 
	    ss = Math.rint(ss * Math.pow(10, prec)) / Math.pow(10, prec);

	// correct for formating errors caused by rounding
	if (ss > 59.99999) { 
	    ss = 0;
	    mm += 1;
	}

	if (ss >= 60 || (prec == -1 && ss >= 30)) { ss -= 60; mm++; }
	if (mm >= 60 || (prec == -2 && mm >= 30)) { mm -= 60; hh++; }
	if (hh >= 24) hh -= 24;

	// format hours
	StringBuffer out = new StringBuffer();
	out.append(hh);
	if (prec == -2) return out.toString();

	// format minutes
	out.append(":");
	if (mm < 10) out.append("0");
	out.append(mm);
	if (prec == -1) return out.toString();

	// format seconds
	out.append(":");
	if (ss < 10) out.append("0");
	if (prec < -2) {
	    if (ss < 0.000099) 
		out.append("0.0000");
	    else
		out.append(ss);
	}
	else if (prec == 0) {
	    out.append(ss);
	}
	else {
            // format the seconds with the desired precision
            String fmt = "%0."+prec+"f";
            Formatter fmtr = new Formatter(out);
            fmtr.format(fmt, ss);
	}

	return out.toString();
    }

    /**
     * format value into a string with default precision
     */
    public String toString(double degrees) { 
	return toString(degrees, -3);
    }

    /**
     * parse a string for a double value.
     */
    public double valueOf(String s) throws NumberFormatException {
	if (s == null) throw new NumberFormatException(s);

	double[] vals = new double[3];
	double out = 0;
	int i, sign;

	StringTokenizer factory = new StringTokenizer(s, ":");
	for(i=0; i<3; i++) vals[i] = 0;
	for(i=0; i<3 && factory.hasMoreTokens(); i++) {
	    vals[i] = Double.valueOf(factory.nextToken()).doubleValue();
	}

	sign = (vals[0] < 0) ? -1 : 1;
	out += vals[0] + sign*(vals[1]/60.0 + vals[2]/3600.0);
	out *= 15.0;
	while (out < 0) {
	    out += 360.0;
	}
	while (out >= 360.0) {
	    out -= 360.0;
	}

	return out;
    }

    public Object clone() { return new HHMMSSAxisPosFormatter(); }

    protected static final String myname = 
        "Time-Angle Coordinate Axis Position Formatter";
    public String toString() { return myname; }

    public static void main(String args[]) {
	int i;
	AxisPosFormatter cap = new HHMMSSAxisPosFormatter();

	double mine = 149.9823;
	System.out.println("My position: " + cap.toString(mine));

	double yours;
	for (i=0; i < args.length; i++) {
	    yours = new Double(args[i]).doubleValue();
	    System.out.println("Your position: " + cap.toString(yours));
	}
    }

};

