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
 *  98jan22  rlp  Original version;
 */
package ncsa.horizon.coordinates.transforms;

import ncsa.horizon.coordinates.IllegalTransformException;
/**
 * This transform cannot be applied in the requested manner due to 
 * bad projection parameters.  This exception is supported by the 
 * LinToSphLinCoordTransform class which will throw it if either 
 * the requested projection type is unsupported or the accompanying 
 * parameters are illegal or missing for a supported projection type.
 */
public class BadProjectionException extends IllegalTransformException {

    /**
     * the specific problem is not explicitly identified 
     */
    public final static byte OTHER = 0;

    /**
     * requested type is unsupported (or not found)
     */
    public final static byte UNSUPPORTED_TYPE = 1;

    /**
     * illegal or missing parameters encountered
     */
    public final static byte ILLEGAL_PARAMETERS = 2;

    protected byte problem = OTHER;

    public BadProjectionException() { super(); }
    public BadProjectionException(String s) { super(s); }

    /** 
     * Construct an exception of a explicit variety
     * @param id    flavor of problem; one of OTHER, UNSUPPORTED_TYPE, or
     *              ILLEGAL_PARAMETERS.
     * @param what  if id=UNSUPPORTED_TYPE, this is the value of the 
     *              "projection" metadatum; otherwise it is a short description
     *              of problem
     */
    public BadProjectionException(byte id, String what) { 
	super(what); 
	problem = id;
    }

    public String getMessage() {
	if (problem == UNSUPPORTED_TYPE) {
	    return new String("Projection type not found: " + 
			      super.getMessage());
	} 
	else if (problem == ILLEGAL_PARAMETERS) {
	    return new String("Illegal Projection Parameters: " +
			      super.getMessage());
	} 
	else {
	    return super.getMessage();
	}
    }
}
