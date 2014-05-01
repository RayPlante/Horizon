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
 * The transform for a position is undefined using this CoordTransform object.
 */
public class TransformUndefinedException extends CoordTransformException {
    public TransformUndefinedException() { super(); }
    public TransformUndefinedException(String s, double[] pos) { 
	super(s, pos);
    }
    public TransformUndefinedException(double[] pos) { super(pos); }
    public TransformUndefinedException(String s) { super(s); }
}
