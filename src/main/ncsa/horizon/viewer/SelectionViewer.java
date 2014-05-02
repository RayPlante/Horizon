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
package ncsa.horizon.viewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.util.Volume;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.Voxel;

/**
 * a Viewer Panel (i.e. a Panel for displaying Viewable objects) that 
 * allows the user to select pixels and subregions.
 * 
 * @version Alpha $Id: SelectionViewer.java,v 0.4 1997/01/09 17:08:43 rplante Exp $
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */
abstract public class SelectionViewer extends Viewer implements Cloneable {

    /**
     * get the current selected display pixel.
     */
    public abstract Point getPixelSelection();

    /**
     * set the current selected display pixel.  The location is measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     */
    public abstract void setPixelSelection(int x, int y);

    /**
     * get the current selected display box.
     */
    public abstract Rectangle getBoxSelection();

    /**
     * set the current selected display box.  The locations are measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     * @param x1,y1  the location of one vertex of the selected box
     * @param x2,y2  the location of the vertex of the selected box opposite 
     *               to the one given by x1,y1
     */
    public abstract void setBoxSelection(int x1, int y1, int x2, int y2);

    /**
     * get the current selected display Line.
     */
    public abstract Rectangle getLineSelection();

    /**
     * set the current selected display line.  The locations are measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     * @param x1,y1  the location of the start of the line
     * @param x2,y2  the location of the end of the line
     */
    public abstract void setLineSelection(int x1, int y1, int x2, int y2);

    /**
     * return the current selected Voxel, or null if there is no current 
     * Viewable.
     */
    public abstract Voxel getVoxelSelection();

    /** 
     * set the current selected Voxel to the one given as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public abstract void setVoxelSelection(Voxel vox);

    /**
     * return the current selected Slice, or null if there is no current
     * Viewable;
     */
    public abstract Slice getSliceSelection();

    /**
     * set the current selected Slice to the given Volume as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public abstract void setSliceSelection(Volume vol);
}
