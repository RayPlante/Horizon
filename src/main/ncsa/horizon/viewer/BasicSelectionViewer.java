/*
 * $Id: BasicSelectionViewer.java,v 0.7 1998/02/03 06:53:44 rplante Exp $
 *
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
 *---------------------------------------------------------------
 *
 * Author: Ray Plante <rplante@ncsa.uiuc.edu>
 * Implementation of Viewer methods based on SimpleViewer
 *
 * History:
 *  96dec12  rlp  Initial version
 *  96dec12  rlp  updated for changes in Slice, ImageDataOrigin
 *  97oct21  rlp  updated for changes made to Slice (first axis index)
 *  97dec04  rlp  now uses ImageDisplayMap instead of depricated 
 *                  ImageDataOrigin
 *  98feb02  rlp  fixed inifinite loop bug when ImageDisplayMap update is 
 *                  attempted before data is loaded
 */
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.*;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.util.*;
import ncsa.horizon.coordinates.*;

/**
 * This Viewer provides a basic implementation of the methods needed for
 * a SelectionViewer. <p>
 *
 * Programmer's are encouraged to inspect this code for examples of how
 * to implement the various Viewer methods.
 */
public class BasicSelectionViewer extends SelectionViewer 
       implements Cloneable 
{
    // constants
    /** * Fit the image to the Viewer size (default) */
    public final static int SIZE_IMAGE_FIT    = 1;
    /** * Trim the image to the Viewer size (centered) */
    public final static int SIZE_IMAGE_CENTER   = 2;
    /** * Trim the image to the Viewer size (centered) */
    public final static int SIZE_IMAGE_TRUNCATE   = 3;
    /** * Scale the image to the Viewer size (centered) */
    public final static int SIZE_IMAGE_SCALE   = 4;

    /** a flag that is true if the last selected pixel should be drawn */
    public boolean drawPixel = false;

    /** a flag that is true if the last selected box should be drawn */
    public boolean drawBox = false;

    /** a flag that is true if the last selected line should be drawn */
    public boolean drawLine = false;

    /** the current viewable */
    protected Viewable          viewable=null;

    /** the current display mode (FIT, CENTER, TRUNCATE, or SCALE) */
    protected int               mode=SIZE_IMAGE_CENTER;

    /** the last image extracted from the current viewable */
    protected Image             image=null;

    /** the last slice requested from the current viewable */
    protected Slice             slice=null;

    /** the preferred size of this Panel */
    protected Dimension pref_size = new Dimension(256, 256);

    /** ImageDisplayMap object for converting between display pixels and 
     *  data pixels */
    protected ImageDisplayMap pixelMap=null;

    /** CoordinateSystem object for converting between data pixels and 
     *  world coordinates */
    protected CoordinateSystem coord=null;

    /** the last selected display pixel */
    protected Point selpix = new Point(0, 0);

    /** the last selected display rectangle */
    protected Rectangle selbox  = new Rectangle(0, 0, 256, 256);

    /** the last selected display line */
    protected Rectangle selline  = new Rectangle(0, 0, 0, 0);

    /** a flag that is true if a new viewable has been attached */
    protected boolean newViewable = false;

    /**
     * create a BasicSelectionViewer of default preferred size;
     */
    public BasicSelectionViewer() { }

    /**
     * create a BasicSelectionViewer of specified preferred size;
     */
    public BasicSelectionViewer(Dimension size) { 
	this(size.width, size.height);
    }

    /**
     * create a BasicSelectionViewer of specified preferred size;
     */
    public BasicSelectionViewer(int width, int height) { 

	// set preferred size to size given
	pref_size.width  = width;
	pref_size.height = height;

	// set last selected box to be entire window
	selbox.width = width;
	selbox.height = height;

	// resize to size requested
	resize(width, height);
    }

/* ---------------------------------------------------------------
 * Viewer methods
 * --------------------------------------------------------------- */

    /**
     * This method adds a reference to a viewable object.
     * @param image the Viewable object to be added
     */
    public void addViewable ( Viewable dataset ) {
	viewable = dataset;
    }

    /**
     * Return a reference to the current Viewable object, or null if 
     * none are attached to this Viewer.
     * @return The current Viewable object; null if none present.
     */
    public Viewable getViewable ( ) {
	return( viewable );
    }

    /**
     * Display a slice from the current Viewable data, or do nothing if
     * the current Viewable is not set.  A null slice means display the
     * default slice.
     */
    public void displaySlice ( Slice sl ) {

	if (viewable == null) return;
	if ((image = viewable.getView(sl)) == null) return;

	// save a copy of input slice
	if (viewable != null) slice = null;
	if (sl != null) slice = new Slice(sl);

	if (viewable == null) return;

	// if no slice was given, come up with a default one
	if (slice == null) {
	    int[] isz = viewable.getSize();

	    slice = new Slice(Math.max(isz.length, 2), 0, 1);
	    slice.setXaxisLength( (isz.length >= 1) ? isz[0] : 1 );
	    slice.setYaxisLength( (isz.length >= 2) ? isz[1] : 1 );
	}

// 	System.out.println("request slice: [" + 
// 			   slice.axisPos(slice.getXaxis()) +
// 			   ", " + slice.axisPos(slice.getYaxis()) +
// 			   ", " + slice.getLength(slice.getXaxis()) +
// 			   ", " + slice.getLength(slice.getXaxis()) + "]");

	// set the mapping from display pixels to data pixels
	if (newViewable) {
	    setPixelMap();
	    newViewable = false;
	} else {
	    updatePixelMap();
	}

	// now we are ready to display the slice
	repaint();
    }

    /**
     * Display a default slice of the current Viewable.
     */
    public void displaySlice ( ) {
	displaySlice( null );
    }

    /**
     * This method returns the size of the region that displays a Viewable
     * @return Dimension of the compoonent
     * @see java.awt.Dimension
     * @see java.awt.Component.size()
     */
    public Dimension getDisplaySize ( ) {
       return( this.size() );
    }

    /**
     * Create a clone of this Viewer Panel.
     * @return A clone of this object.
     * @exception java.lang.CloneNotSupportedException  not thrown if 
     *            called as a BasicSelectionViewer
     */
    public Object clone() throws CloneNotSupportedException {
	BasicSelectionViewer out = new BasicSelectionViewer(preferredSize());
	out.mode = mode;
	out.addViewable(viewable);
	out.selpix = selpix;
	out.selbox = selbox;
	out.selline = selline;
	out.displaySlice(slice);

	return out;
    }

    /**
     * This implementation simple issues a repaint() when an image has become
     * available.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y,
			       int w, int h) {

	if ((flags&WIDTH) != 0 || (flags&HEIGHT) != 0) updatePixelMap();

	if ((flags & ALLBITS) == 0) return( true );

	repaint();
	return( false );
    }

/* ---------------------------------------------------------------
 * SelectionViewer methods
 * --------------------------------------------------------------- */

    /**
     * return a Slice object describing the data currently being viewed, 
     * or null if there is no Viewable currently being viewed.
     */
    public Slice getViewSlice() {
	return (slice == null) ? null : (Slice) slice.clone();
    }

    /**
     * set the current selected display pixel.  The location is measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     */
    public void setPixelSelection(int x, int y) { 

	// set the selected pixel
	selpix.x = x;
	selpix.y = y;
    }

    /**
     * set the current selected display box.  The locations are measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     * @param x1,y1  the location of one vertex of the selected box
     * @param x2,y2  the location of the vertex of the selected box opposite 
     *               to the one given by x1,y1
     */
    public void setBoxSelection(int x1, int y1, int x2, int y2) { 
	selbox.x = Math.min(x1, x2);
	selbox.y = Math.min(y1, y2);
	selbox.width  = Math.abs(x2 - x1);
	selbox.height = Math.abs(y2 - y1);
    }

    /**
     * set the current selected display line.  The locations are measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     * @param x1,y1  the location of the start of the line
     * @param x2,y2  the location of the end of the line
     */
    public void setLineSelection(int x1, int y1, int x2, int y2) { 
	selline.x = x1;
	selline.y = y1;
	selline.width  = x2 - x1;
	selline.height = y2 - y1;
    }

    /**
     * get the current selected display pixel.
     */
    public Point getPixelSelection() { return new Point(selpix.x, selpix.y); }

    /**
     * get the current selected display box.
     */
    public Rectangle getBoxSelection() { 
	return new Rectangle(selbox.x, selbox.y, selbox.width, selbox.height); 
    }

    /**
     * get the current selected display Line.
     */
    public Rectangle getLineSelection() { 
	return new Rectangle(selline.x, selline.y, 
			     selline.width, selline.height); 
    }

    /**
     * return the current selected Voxel, or null if there is no current 
     * Viewable.
     */
    public Voxel getVoxelSelection() {
	Voxel out;

	// make sure have a display to data pixel converter
	if (pixelMap == null) setPixelMap();

	// use the ImageDisplayMap to convert the currently selected 
	// pixel to a data Voxel
	if (pixelMap != null) 
	    return pixelMap.getDataVoxel(selpix);
	else 
	    return new Voxel(2);
    }

    /** 
     * set the current selected Voxel to the one given as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public synchronized void setVoxelSelection(Voxel vox) {

	if (viewable == null) return;

	// make sure have a display to data pixel converter
	if (pixelMap == null) setPixelMap();

	// project the specified Voxel onto the current slice
	Voxel use = slice.projection(vox);

	// convert the data Voxel to a display pixel, and save it
	Point pix = pixelMap.getDisplayPixel(use);
	selpix.x = pix.x;
	selpix.y = pix.y;
    }

    /**
     * return the current selected Slice, or null if there is no current
     * Viewable;
     */
    public Slice getSliceSelection() {

	if (viewable == null) return null;

	// make sure have a display to data pixel converter
	if (pixelMap == null) setPixelMap();

	// convert the last selected box to a data Slice
	if (pixelMap != null) 
	    return pixelMap.getDataSlice(selbox);
	else 
	    return getViewSlice();
    }

    /**
     * set the current selected Slice to the given Volume as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public synchronized void setSliceSelection(Volume vol) {

	if (viewable == null) return;

	// make sure have a display to data pixel converter
	if (pixelMap == null) setPixelMap();

	// project the specified Volume onto the current slice
	Slice use = slice.projection(vol);

	// convert the Slice to a display rectangle
	selbox = pixelMap.getDisplayRegion(use);
    }

/* ---------------------------------------------------------------
 * New BasicSelectionViewer methods
 * --------------------------------------------------------------- */

    /**
     * Set the display mode for sizing or trimming the viewable image.
     * @param _mode The mode to use.  Valid modes are SIZE_IMAGE_FIT,
     *    SIZE_IMAGE_CENTER, and SIZE_IMAGE_TRUNCATE.
     */
    public void setMode ( int _mode ) {
	switch (_mode) {
	case SIZE_IMAGE_FIT:
	case SIZE_IMAGE_CENTER:
	case SIZE_IMAGE_SCALE:
	case SIZE_IMAGE_TRUNCATE: 
            mode = _mode;
	    updatePixelMap();
            repaint();
	}
   }

/* ---------------------------------------------------------------
 * Methods overriding Component methods
 * --------------------------------------------------------------- */

    /**
     * return the preferred size of this Viewer Panel
     */
    public Dimension preferredSize() { return pref_size; }

    /**
     * Redraw current slice/viewable.
     */
    public void update ( Graphics g ) {
	if (image == null) return;                // there's no image to update
	if (!prepareImage( image, this )) return; // the image isn't yet loaded

	int imwd = image.getWidth(this);
	int imht = image.getHeight(this);
	if (imwd < 0 || imht < 0) return;         // the image is not ready yet

	Rectangle implace = imagePlacement(imwd, imht);  
	Dimension disp = size();
	g.clearRect( 0, 0, disp.width, disp.height );    // clear the panel

	switch (mode) {
	case SIZE_IMAGE_FIT:          // resize the image to the panel size
	case SIZE_IMAGE_SCALE:        // resize the image, preserving aspect
	    g.drawImage( image, implace.x, implace.y, 
			 implace.width, implace.height, this );
	    break;

	case SIZE_IMAGE_CENTER:       // center the image in the panel
	case SIZE_IMAGE_TRUNCATE:     // truncate right/bottom as necessary
	default: 
	    g.drawImage( image, implace.x, implace.y, this );
	    break;
	}

	// add on any line graphics
	g.setColor(Color.magenta);
	if (drawPixel) g.drawRect(selpix.x, selpix.y, 1, 1);
	if (drawBox)   {
	    Rectangle use = new Rectangle(selbox.x, selbox.y, 
					  selbox.width, selbox.height);
	    if (use.width < 0) {
		use.x += use.width;
		use.width *= -1;
	    }
	    if (use.height < 0) {
		use.y += use.height;
		use.height *= -1;
	    }
	    g.drawRect(use.x, use.y, use.width, use.height);
	}
	if (drawLine)  g.drawLine(selline.x, selline.y, 
				  selline.x+selline.width, 
				  selline.y+selline.height);
    }

    /**
     * This implementationm simply calls the update() method.
     * @param g The graphics context to paint.
     */
    public void paint ( Graphics g )  {
	update( g );
    }

    /**
     * process selection request
     */
    public boolean mouseDown(Event evt, int x, int y) {

	if ((evt.modifiers & Event.CTRL_MASK) != 0) {

	    // Control-button was pushed: toggle selection graphics
	    if ((evt.modifiers & Event.META_MASK) != 0) 
		drawBox = !drawBox;              // Control-Right toggle box 
	    else if ((evt.modifiers & (Event.ALT_MASK|Event.SHIFT_MASK)) != 0) 
		drawLine = !drawLine;            // Control-Middle toggle line 
	    else 
		drawPixel = !drawPixel;          // Control-Lef toggle pixel 
	}
	else {
	    if ((evt.modifiers & Event.META_MASK) != 0) {

		// the right button was pushed: start box selection
		drawBox = true;
		selbox.x = x;
		selbox.y = y;
		selbox.width  = 1;
		selbox.height = 1;
	    }
	    if ((evt.modifiers & (Event.ALT_MASK|Event.SHIFT_MASK)) != 0) {

		// the middle button was pushed: start a line selection
		drawLine  = true;
		selline.x = x;
		selline.y = y;
		selline.width  = 1;
		selline.height = 1;
	    }
	    if ((evt.modifiers & 
		 (Event.ALT_MASK|Event.META_MASK|Event.SHIFT_MASK)) == 0) {

		// the left button was pushed: select a pixel
		drawPixel = true;
		selpix.x = x;
		selpix.y = y;
	    }
	}

	repaint();
	return false;
    }

    /**
     * process a box selection request
     */
    public boolean mouseDrag(Event evt, int x, int y) {

	if ((evt.modifiers & Event.META_MASK) != 0 && drawBox) {
	    selbox.width  = x - selbox.x;
	    selbox.height = y - selbox.y;
	}
	if ((evt.modifiers & (Event.ALT_MASK|Event.SHIFT_MASK)) != 0) {

	    // the middle button was pushed: start a line selection
	    drawLine  = true;
	    selline.width  = x - selline.x;
	    selline.height = y - selline.y;
	}
	if ((evt.modifiers & 
	     (Event.ALT_MASK|Event.META_MASK|Event.SHIFT_MASK)) == 0 && 
	    drawPixel) 
	{

	    // the left button is being dragged: move the selected pixel
	    selpix.x = x;
	    selpix.y = y;
	}

	repaint();
	return false;
    }

/* ---------------------------------------------------------------
 * Some Extra private methods
 * --------------------------------------------------------------- */

    /**
     * Return a rectangle that describes where an image of a particular
     * size will be placed.
     */
    private Rectangle imagePlacement(int width, int height) {
	Rectangle out = new Rectangle(0, 0, width, height);
	Dimension disp = size();

	switch (mode) {
	case SIZE_IMAGE_FIT:
	    out.width  = disp.width;
	    out.height = disp.height;
	    break;
	case SIZE_IMAGE_CENTER:
	    out.x = (disp.width - width)/2;
	    out.y = (disp.height - height)/2;
	    break;
	case SIZE_IMAGE_SCALE: {
	    double iaspect = 1.0 * height / width;
	    double daspect = 1.0 * disp.height / disp.width;
	    if (iaspect < daspect) {
		out.width = disp.width;
		out.height = (int) (out.width * iaspect);
		out.y = (disp.height - out.height) / 2;
	    } 
	    else {
		out.height = disp.height;
		out.width = (int) (out.height / iaspect);
		out.x = (disp.width - out.width) / 2;
	    }
	}
	case SIZE_IMAGE_TRUNCATE: 
	default:
	}

	return out;
    }

    /**
     * update the ImageDisplayMap object, pixelMap (used to convert display
     * pixels into data pixels), to reflect changes in the current Viewable
     */
    private synchronized void setPixelMap() {
        setPixelMap(slice, null);
    } 

    /**
     * set the ImageDisplayMap object, pixelMap (used to convert display
     * pixels into data pixels), to reflect changes in the current Viewable
     * @param dataSlice      the slice to be displayed; if null, do not 
     *                       change the slice
     * @param displayRegion  the region of the display being used; if null,
     *                       assume that as much of the display as possible
     *                       will be used (unless dataSlice is also null,
     *                       in which case, nothing is changed).
     */
    protected synchronized void setPixelMap(Slice dataSlice, 
					    Rectangle displayRegion) {

        Boolean xaxisReversed=null, yaxisReversed=null;
	
	if (viewable != null) {

            pixelMap = new ImageDisplayMap();

	    // the attached viewable may give some hints on how the 
	    // data is ordered in the form of "xaxisReversed" and
	    // "yaxisReversed" metadata
	    // 
	    Metadata md = viewable.getMetadata();
	    if (md != null) {
                try {
		    xaxisReversed = (Boolean) md.getMetadatum("xaxisReversed");
		} catch (ClassCastException ex) { xaxisReversed = null; }
		try {
                    yaxisReversed = (Boolean) md.getMetadatum("yaxisReversed");
		} catch (ClassCastException ex) { yaxisReversed = null; }
	    }

	    if (xaxisReversed != null) 
                pixelMap.xaxisReversed = xaxisReversed.booleanValue();
	    if (yaxisReversed != null) 
                pixelMap.yaxisReversed = yaxisReversed.booleanValue();

	    // now set the mapping appropriate for a slice of requested size
	    updatePixelMap(dataSlice, displayRegion);
	}
    }

    /**
     * update the ImageDataOrigin object, pixelMap (used to convert display
     * pixels into data pixels), to reflect changes in the currently viewed
     * slice or viewing mode.
     */
    private synchronized void updatePixelMap() {
        updatePixelMap(slice, null); 
    }

    /**
     * update the ImageDisplayMap object, pixelMap (used to convert display
     * pixels into data pixels), to reflect changes in the currently viewed
     * slice.
     * @param dataSlice      the slice to be displayed; if null, do not 
     *                       change the slice
     * @param displayRegion  the region of the display being used; if null,
     *                       assume that as much of the display as possible
     *                       will be used (unless dataSlice is also null,
     *                       in which case, nothing is changed).
     */
    protected synchronized void updatePixelMap(Slice dataSlice, 
					       Rectangle displayRegion) {
        int dwd=-1, dht=-1;

	if (pixelMap == null) {
            setPixelMap(dataSlice, displayRegion);    
	    return;                 // this calls updatePixelMap(),
	                            //   so we should return
	}
	if (dataSlice == null && displayRegion == null) return;

	if (displayRegion == null) {
            dwd = dataSlice.getTrueLength(slice.getXaxis());
            dht = dataSlice.getTrueLength(slice.getYaxis());

	    // this is how much space an image with an unscaled size of 
	    // wd x ht will take up on the screen
	    //
	    Dimension dispdim = viewSize(dwd, dht);
	    displayRegion = new Rectangle(0, 0, dispdim.width, dispdim.height);
	}

	if (dataSlice != null) pixelMap.setSlice(dataSlice);
	if (displayRegion != null) pixelMap.setDisplay(displayRegion);
    } 

    public Dimension viewSize(int wd, int ht) {
	Dimension mysz = size();
	Dimension imsz = new Dimension(mysz);
    
	if (mysz.width == 0 && mysz.height == 0)
	    mysz = preferredSize();

	if (wd < 0 || ht < 0) {
	    if (image == null) return mysz;
	    if (wd < 0) wd = image.getWidth(this);
	    if (ht < 0) ht = image.getHeight(this);
	}
	if (wd < 0 || ht < 0)
	    return size();

	if (mode == SIZE_IMAGE_FIT) {
	    return mysz;
	}
	else if (mode == SIZE_IMAGE_SCALE) {
	    imsz.width = wd;
	    imsz.height = ht;

	    double aspect = (1.0*wd)/ht;
	    if (imsz.width > imsz.height) {
	        imsz.width = mysz.width;
		imsz.height = (int) (imsz.width/aspect);
	    }
	    else {
	        imsz.height = mysz.height;
		imsz.width = (int) (imsz.height*aspect);
	    } 
    
	    return imsz;
	}
	else {
	    return new Dimension(wd, ht);
	}

    }
}

