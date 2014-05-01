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
 *-------------------------------------------------------------------------
 * History: 
 *  98jan14  rlp  Original version
 */
package ncsa.horizon.awt;

/**
 * an Exception thrown due to errors associated with the ModedGUI and 
 * ModedGUIControllable classes.  See these classes for details.
 * 
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class ModedGUIException extends Exception {

    /**
     * create an exception indicating that an unrecognized option
     * was encountered
     */
    public ModedGUIException() { super(); }

    /**
     * create an exception indicating that an unrecognized option
     * was encountered
     */
    public ModedGUIException(String str) { super(str); }

}

