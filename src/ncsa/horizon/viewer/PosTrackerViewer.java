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
 *  97jan21  rlp  original version
 *  97aug    rlp  made resizable, performance improvements; now uses new
 *                coordinates and metadata model
 *  97oct21  rlp  updated for changes made to Slice (first axis index)
 *  98feb02  rlp  bug fix: pixelMap incorrectly updated after resize.
 */
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.awt.ImageCanvas;
import ncsa.horizon.util.*;
import ncsa.horizon.coordinates.*;

/**
 * A Viewer that displays the current position under the mouse. <p>
 *
 * This Viewer contains a display area of a configurable size and below it
 * a region for displaying the current coordinate position.  When the
 * mouse is placed within the display area, the position beneath the mouse
 * is displayed at the bottom of the viewer.  Moving the mouse updates
 * the position display in real time.  <p>
 *
 * This class is meant to serve as an example of how to implement a Viewer,
 * a Viewable, and related coordinate classes. <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: PosTrackerViewer.java,v 1.6 1998/02/03 06:53:44 rplante Exp $
 */
public class PosTrackerViewer extends Viewer implements Cloneable {

    protected Viewable data=null;
    protected Slice slice=null;
    protected ImageDisplayMap pixelMap=null;
    protected CoordinateSystem coordSys=null;
    protected Image view=null;
//    protected Dimension sliceSize = new Dimension(0,0);

    protected ImageCanvas display=null;
    protected Label xDataPos, yDataPos, xLabel, yLabel, xCoordPos, yCoordPos;
    protected Panel positionDisplay=null;

    protected boolean newViewable = false;

    /**
     * default width of display area if not specified in constructor
     */
    public final static int defautlDisplayWidth  = 256;

    /**
     * default height of display area if not specified in constructor
     */
    public final static int defautlDisplayHeight = 256;

    /**
     * create a viewer with no Viewable to display
     */
    public PosTrackerViewer() {
	this(0, 0);
    }

    /**
     * create a viewer with a given display size 
     * @param width width of the display area
     */
    public PosTrackerViewer(int width, int height) {

	// Set up the layout
//	setLayout(new BorderLayout(4,4));
	GridBagConstraints bc = new GridBagConstraints();
	GridBagLayout gbag = new GridBagLayout();
	setLayout(gbag);
//	bc.insets = new Insets(0,4,4,4);
	bc.weightx = bc.weighty = 0.0;
	bc.anchor = bc.NORTHWEST;
	bc.gridx = bc.gridy = bc.RELATIVE;  
	bc.gridwidth = bc.REMAINDER;
	bc.fill = bc.NONE;
	setFont(new Font("Helvetica", Font.PLAIN, 14));

	// Set up the display area with the requested size
	//
	if (width <= 0)  width  = defautlDisplayWidth;
	if (height <= 0) height = defautlDisplayHeight;
	display = new ImageCanvas(width, height);
	
	// Set the mode of the ImageCanvas so that images are scaled
	// to fit within the display area (preserving aspect ratio)
	// and place flush against the upper-left corner.  This will
	// keep pixel tracking simple
	display.setMode(display.SIZE_IMAGE_FLUSH);

	display.setBackground(Color.black);
	gbag.setConstraints(display, bc);
	add(display);
//	add("Center", display);

	// Set the position panel
	//
	positionDisplay = new Panel();
	GridBagLayout bag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
//	positionDisplay.setFont(new Font("Helvetica", Font.PLAIN, 14));
	positionDisplay.setLayout(bag);

	// Data Pixel Position Label
	Label alabel = new Label("Data Pixel: ", Label.LEFT);
	c.insets = new Insets(4,0,0,0);
	c.gridx = c.gridy = 0;
	c.gridwidth = 4;
	c.gridheight = 1;
	c.anchor = c.NORTHWEST;
	bag.setConstraints(alabel, c);
	positionDisplay.add(alabel);
	
	// Data Pixel Position
	xDataPos = new Label("      0", Label.RIGHT);
	c.gridx = 4;
	c.gridwidth = 2;
	c.gridy = 0;
	bag.setConstraints(xDataPos, c);
	positionDisplay.add(xDataPos);
	
	alabel = new Label(", ", Label.LEFT);
	c.gridx = 6;
	c.gridwidth = 1;
	bag.setConstraints(alabel, c);
	positionDisplay.add(alabel);
	
	yDataPos = new Label("      0", Label.RIGHT);
	c.gridx = 7;
	c.gridwidth = 2;
	bag.setConstraints(yDataPos, c);
	positionDisplay.add(yDataPos);
	
	// Coordinate Position Labels
	xLabel = new Label("XCoord:   ", Label.LEFT);
	c.insets = bc.insets;
	c.gridx = 0;
	c.gridy = 3;
	c.gridwidth = 3;
	bag.setConstraints(xLabel, c);
	positionDisplay.add(xLabel);

	yLabel = new Label("YCoord:   ", Label.LEFT);
	c.gridx = 0;
	c.gridy = 4;
	c.gridwidth = 3;
	bag.setConstraints(yLabel, c);
	positionDisplay.add(yLabel);

	xCoordPos = new Label("                    ", Label.RIGHT);
	c.gridx = 3;
	c.gridy = 3;
	c.gridwidth = 6;
	bag.setConstraints(xCoordPos, c);
	positionDisplay.add(xCoordPos);

	yCoordPos = new Label("                    ", Label.RIGHT);
	c.gridx = 3;
	c.gridy = 4;
	c.gridwidth = 6;
	bag.setConstraints(yCoordPos, c);
	positionDisplay.add(yCoordPos);

//	bc.gridy = bc.RELATIVE;
	gbag.setConstraints(positionDisplay, bc);
	add(positionDisplay);
//	add("South", positionDisplay);

    }

/* ---------------------------------------------------------------
 * Viewer methods
 * --------------------------------------------------------------- */

    /**
     * replace the current Viewable object with a new one; the display 
     * will not be affected until displaySlice() is called.
     */
    public synchronized void addViewable(Viewable data) {
	this.data = data;
	newViewable = true;
    }

    /**
     * Return a reference to the current Viewable object, or null if 
     * none are attached to this Viewer.
     */
    public synchronized Viewable getViewable() {  return data;  }

    /**
     * return a Slice object describing the data currently being viewed, 
     * or null if there is no Viewable currently being viewed.
     */
    public Slice getViewSlice() {
	return (slice == null) ? null : (Slice) slice.clone();
    }

    /**
     * display a slice from the current Viewable data, or do nothing if
     * the current Viewable is not set.
     */
    public synchronized void displaySlice(Slice sl) {
	Slice requestedSlice = null;

	// save a copy of input slice.  Making a copy prevents the object
	// that gave us this slice from updating it while we are trying
	// to use it.
	//
	// (Note that we like to number axes beginning with zero.)
	//
	if (sl != null) requestedSlice = new Slice(sl);
	if (data == null) return;

	// if no slice was given, use the viewable's default slice
	if (requestedSlice == null) requestedSlice = data.getDefaultSlice();

	// if no slice was provided, come up with a default one.
	// 
	// (Note: this is a guess for the default slice of this viewable.
	// If it does not properly describe the slice, our positions 
	// may be incorrect.)
	if (requestedSlice == null) {
	    int[] isz = data.getSize();
	    for(int i=0; i < isz.length; i++) {
		if (isz[i] <= 0) {
		    System.err.println("Dataset looks empty; " + 
				       "display request aborted");
		    return;
		}
	    }

	    requestedSlice = new Slice(Math.max(isz.length, 2), 0, 1);
	    requestedSlice.setXaxisLength( (isz.length >= 1) ? isz[0] : 1 );
	    requestedSlice.setYaxisLength( (isz.length >= 2) ? isz[1] : 1 );

	    System.err.println("Warning: guessing a default view; displayed " +
			       "positions may be incorrect");
	}

	// Now that we are sure we have a usable slice; now we will 
	// save it as the current slice being displayed.
	slice = requestedSlice;

	// set the mapping between display pixels and data pixels as well
	// as between data pixels and world coordinate positions
	//
	if (newViewable) {

	    // this is a new data set, so we need to update our coordinate
	    // system
	    coordSys = data.getCoordSys();

	    // we also need to reset our ImageDisplayMap object that defines
	    // the mapping between display and data pixels.
/*	    setPixelMap(slice.getTrueLength(slice.getXaxis()), 
			slice.getTrueLength(slice.getYaxis()));*/
	    setPixelMap(slice, null);
	    newViewable = false;
	} 
	else {

	    // this is just a new slice from an already attached Viewable;
	    // thus, we need only update the pixel mapper.
	    updatePixelMap();
	}

	// Since the user may have chosen a different pair of axes to
	// display, we should update our axis labels.
	if (coordSys != null) {
	    xLabel.setText( coordSys.getAxisLabel(slice.getXaxis()) + 
			    ": " );
	    yLabel.setText( coordSys.getAxisLabel(slice.getYaxis()) + 
			    ": " );
	}

	// extract the slice as an Image
	view = data.getView(slice, (ColorModel) null, true);

	// now display the slice
	display.displayImage(view);

    }

    /**
     * display a default slice of the current Viewable
     */
    public void displaySlice() { displaySlice(null); }

    /**
     * return the size of the display area
     */
    public Dimension getDisplaySize() { return display.size(); }

    /**
     * set the size of the display area
     */
    public synchronized void setDisplaySize(int width, int height) {
	int dwd, dht;
	Dimension old = size();
	if (width != old.width || height != old.height) {
	    display.setPreferredSize(width, height);
	    if (pixelMap != null) {
		Rectangle dispreg = pixelMap.getDisplay();
		Slice datsl = pixelMap.getSlice();
		dwd = datsl.getTrueLength(datsl.getXaxis());
		dht = datsl.getTrueLength(datsl.getYaxis());
		Dimension dim = display.viewSize(dwd, dht);
		dispreg.width = dim.width;
		dispreg.height = dim.height;
		pixelMap.setDisplay(dispreg);
	    }
	    display.resize(width, height);
	}
    }

    /**
     * set the size of the display area
     */
    public void setDisplaySize(Dimension sz) {
	setDisplaySize(sz.width, sz.height);
    }

    /**
     * resize the viewer so that the display area takes up as much of
     * the available space as possible.
     */
    public synchronized void reshape(int x, int y, int width, int height) {
	Dimension psz=null;
	Dimension old = size();

	if (old.width > 0 && old.height > 0 &&
	    (width != old.width || height != old.height)) 
	{
	    psz = positionDisplay.size();
	    if (psz.width > 0 && psz.height > 0) {
		if (width < 2) width = 2;
		if (height < psz.height + 2) height = psz.height + 2;
		setDisplaySize(width, height - psz.height);
	    }

	    // update the pixelMap for the viewer
	    if (slice != null && pixelMap != null) {
		Dimension newdim = 
		    display.viewSize(slice.getTrueLength(slice.getXaxis()),
				     slice.getTrueLength(slice.getYaxis()));
		updatePixelMap(slice, new Rectangle(0, 0, newdim.width,
						    newdim.height));
	    }
	}
	super.reshape(x, y, width, height);
    }

/* ---------------------------------------------------------------
 * Methods overriding Component methods
 * --------------------------------------------------------------- */

    public boolean mouseMove(Event event, int x, int y) {

	// only handle event if it occurred within the ImageCanvas
	// display; update the position display using a display 
	// pixel that is relative to the ImageCanvas display's origin
        Component c = locate(x, y);
	if (display == c) {
	    Point displayOrigin = display.location();
	    updatePosDisplay(x-displayOrigin.x, y-displayOrigin.y);
	}
	return false;
    }

    public Dimension preferredSize() {
	Dimension out = display.preferredSize();
	if (isVisible()) {
	    Dimension psz = positionDisplay.preferredSize();
	    out.height += psz.height;
	    if (psz.width > out.width) out.width = psz.width;
	}
	else {
	    out.height += 5*getFontMetrics(getFont()).getHeight() + 4;
	}
	return out;
    }
    
/* ---------------------------------------------------------------
 * New PosTrackerViewer methods
 * --------------------------------------------------------------- */

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
	if (data != null) {

	    pixelMap = new ImageDisplayMap();

	    // the attached viewable may give some hints on how the 
	    // data is ordered in the form of "xaxisReversed" and
	    // "yaxisReversed" metadata
	    // 
	    Metadata md = data.getMetadata();
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
	}

	// now set the mapping appropriate for a slice of requested size
	updatePixelMap(dataSlice, displayRegion);
    }

    /**
     * update the ImageDisplayMap object, pixtrans (used to convert display
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
	    Dimension dispdim = display.viewSize(dwd, dht);
	    displayRegion = new Rectangle(0, 0, dispdim.width, dispdim.height);
	}

	if (dataSlice != null) pixelMap.setSlice(dataSlice);
	if (displayRegion != null) pixelMap.setDisplay(displayRegion);

    }

    /**
     * update the ImageDisplayMap object, pixtrans (used to convert display
     * pixels into data pixels), to reflect changes in the currently viewed
     * slice.
     */
    protected synchronized void updatePixelMap() { 
	updatePixelMap(slice, null); 
    }

    /**
     * update the position display for a given display pixel
     */
    protected void updatePosDisplay(int x, int y) {

	if (pixelMap == null) return;

	// First translate the display pixel to a data pixel; 
	Voxel dvox = pixelMap.getDataVoxel(new Point(x, y));
	
	// display the selected data pixel.  Here we will display the
	// the selection as an integer.
	double dxpos = Math.floor(pixelMap.getXDataPos(dvox));
	double dypos = Math.floor(pixelMap.getYDataPos(dvox));
	xDataPos.setText(Double.toString(dxpos));
	yDataPos.setText(Double.toString(dypos));

	// Now translate the data pixels to coordinate positions
	if (coordSys != null) {

	    // convert to a coordinate position and display it.
	    try {
		CoordPos cpos = coordSys.getCoordPos(dvox);
		xCoordPos.setText(cpos.valueString(slice.getXaxis(), 2));
		yCoordPos.setText(cpos.valueString(slice.getYaxis(), 2));
	    } 
	    catch (CoordTransformException ex) {
		xCoordPos.setText("Undefined");
		yCoordPos.setText("Undefined");
	    }
	}
    }

/* ---------------------------------------------------------------
 * clone method
 * --------------------------------------------------------------- */

    /** 
     * produce a copy of this viewer.
     */
    public Object clone() { 
	Dimension sz = display.preferredSize();
	PosTrackerViewer out = new PosTrackerViewer(sz.width, sz.height);
	if (data != null) {
	    out.addViewable(data);
	    if (slice != null) out.displaySlice(slice);
	}

	return out;
    }
}



