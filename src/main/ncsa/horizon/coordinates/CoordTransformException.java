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
 *  96mar26  rlp  Original version;
 */
package ncsa.horizon.coordinates;

/**
 * An illegal or undefined condition has been detected while using
 * a CoordTransform object.
 */
public class CoordTransformException extends Exception {
    public double[] position = null;

    public CoordTransformException() { super(); }
    public CoordTransformException(String s, double[] pos) { 
	super(s);
	position = pos;
    }
    public CoordTransformException(double[] pos) { 
	super();
	position = pos;
    }
    public CoordTransformException(String s) { super(s); }

    public String getMessage() {
	if (position == null) {
	    return super.getMessage();
	}

	StringBuffer out = new StringBuffer(super.getMessage());
	out.append(": ");
	if (position.length > 0) {
	    out.append(Double.toString(position[0]));
	    for(int i=1; i < position.length; i++) 
		out.append(", " + position[1]);
	}
	return out.toString();
    }
}

