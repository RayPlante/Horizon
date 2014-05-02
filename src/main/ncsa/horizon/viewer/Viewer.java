/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-8, Board of Trustees of the University of Illinois
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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  96       ???  Original version
 *  98jan15  wx   implements Observer with empty update method
 */
package ncsa.horizon.viewer;

/** 
* @version Alpha $Id: Viewer.java,v 0.6 1998/01/19 16:18:04 rplante Exp $
* @author Horizon team, University of Illinois at Urbana-Champaign
* @author Daniel L. Goscha
*/

import java.awt.Panel;
import java.awt.Dimension;
import java.util.Observer;
import java.util.Observable;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.Voxel;

/** 
* Abstract Panel class to display Viewable objects.  Such a Panel usually 
* contains a display area (often a Canvas object) to which Image objects 
* extracted from Viewables are painted.  The Viewer methods assume 
* a single Viewable as being the "current" one; however, it is concievabe 
* that many viewable objects might be visualized in the display area of the
* panel.
*/
abstract public class Viewer extends Panel implements Cloneable, Observer {

    /**
     * This method adds a reference to a viewable object.
     * @param image the Viewable object to be added
     */
    public abstract void addViewable(Viewable image);

    /**
     * Return a reference to the current Viewable object, or null if 
     * none are attached to this Viewer.
     */
    public abstract Viewable getViewable();

    /**
     * display a slice from the current Viewable data, or do nothing if
     * the current Viewable is not set.  A null slice means display the
     * default slice (equivalent to displayViewable());
     */
    public abstract void displaySlice(Slice sl);

    /**
     * display a default slice of the current Viewable
     */
    public void displaySlice() { displaySlice(null); }

    /**
     * return a Slice object describing the data currently being viewed, 
     * or null if there is no Viewable currently being viewed.
     */
    public abstract Slice getViewSlice();
     
    /**
     * This method returns the size of the region that displays a Viewable
     * @return Dimension of the compoonent
     * @see java.awt.Dimension
     * @see java.awt.Component.size()
     */
    public abstract Dimension getDisplaySize();

    /**
     * create a clone of this Viewer Panel
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void update(Observable  o, Object  arg) {
      ;
    }
}
