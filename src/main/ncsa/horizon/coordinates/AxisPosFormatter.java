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
package ncsa.horizon.coordinates;

/**
 * support for formatting a position along an axis in a coordinate system <p>
 *
 * Note that valueOf(String s) is not guaranteed to produce closure with 
 * valStr().  For example, valStr might convert a continuous value into the
 * a timezone or the name of a color; in this case, valueOf() might have to
 * return an "average" or canonical value associated the string.  <p>
 */
public interface AxisPosFormatter extends Cloneable {

    /**
     * convert a value to a string
     */
    public abstract String toString(double val);

    /**
     * convert a value to a string with a given precision.  Implementations
     * may interpret the precision in different ways
     */
    public abstract String toString(double val, int precision);

    /**
     * parse the string into a double value as best as possible.  Note that
     * this method is not guaranteed to give closure with valStr().
     */
    public abstract double valueOf(String s) 
	throws NumberFormatException;

    public abstract Object clone();
}

