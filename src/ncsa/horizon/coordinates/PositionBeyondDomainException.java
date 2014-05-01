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
 * An attempt was made to transform a position that is outside the domain
 * of the CoordTransform object used.
 */
public class PositionBeyondDomainException extends CoordTransformException {
    public PositionBeyondDomainException() { super(); }
    public PositionBeyondDomainException(String s, double[] pos) { 
	super(s, pos);
    }
    public PositionBeyondDomainException(double[] pos) { super(pos); }
    public PositionBeyondDomainException(String s) { super(s); }
}

