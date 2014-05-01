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
 *-------------------------------------------------------------------------
 * History: 
 *  97mar21  rlp  Original version 
 */
package ncsa.horizon.util;

/**
 * A container for holding an Object and integer, as in a return value
 * and error status.
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: ValueAndStatus.java,v 1.1 1997/08/07 06:52:06 rplante Exp $
 */
public class ValueAndStatus {

    public Object value=null;
    public int status=0;

    public ValueAndStatus(Object value, int status) {
	this.value = value;
	this.status = status;
    }
}
