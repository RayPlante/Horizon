/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1997, 1998, Board of Trustees of the University of Illinois
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
 *  97mar    rlp  Original version
 *  97aug07  rlp  Habanero-ized!
 *  97aug19  rlp  updated to use ImageDisplayMap
 *  97aug08  rlp  fixed bug in magview pixel map
 *  97oct12  rlp  updated for changes made to Slice (first axis index)
 *  97dec06  rlp  updated for changes in ImageCanvas: offpaint is now 
 *                  protected; calls replaced with update() or repaint()
 *  97dec07  rlp  made more robust against dataset read errors: aborts display
 *                  request if dataset has 0 size.  
 *  98jan23  rlp  made more robust against multiple display requests by 
 *                  anxious users via us of new ImageCanvas.isLoading() method.
 *                NOTE ABOUT HABANERO:  This method denies update requests if
 *                  magview.isLoading() == true; that is, if this viewer is
 *                  still loading a magnified image.  A false return does not
 *                  necessarily mean that other Habanero participants aren't
 *                  still loading.  As a result, low-end participants may 
 *                  become out of sync with the hi-end ones.  
 *  98feb06  rlp  changed repaint() to magview.repaint() in  updateMagview();
 *                  don't know if it'll make a difference.
 */
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.*;
import java.util.Random;
import java.net.URL;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.awt.ImageCanvas;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.util.*;
import ncsa.horizon.coordinates.*;

/**
 * A SelectionViewer developed for use with the NCSA Astronomy Digital 
 * Image Library (ADIL).  <p>
 *
 * This viewer provides two display areas side-by-side.  The left side 
 * displays a scaled version of the requested slice.  The right side 
 * displays zoomed version of the image.  Clicking positions in the left 
 * display will cause a new zoomed image to appear in the right display 
 * centered over the selected pixel.  The user may control how much the
 * right image is zoomed via buttons along the bottom of the viewer panel.  
 * The user may optionally choose "Region" zooming which allows a box 
 * to be drawn in the left display, representing the region to be shown in 
 * the right display.  The coordinate positions corresponding
 * to the pixel below the mouse are displayed as the mouse is moved about
 * within either display.  Users may display the image's text header by 
 * clicking on the header button.  <p>
 *
 * The ADIL uses this viewer to browse representations of FITS files from 
 * the Library's collection.  The only assumption made about the data being
 * displayed is that data origin is relative to the lower left corner of 
 * the display with the y-axis increasing upward.  (This may be relaxed in 
 * future versions.)  <p>
 *
 * Although the default label on the header button is "FITS Header Text", its 
 * behavior is <em>not</em> FITS-specific.  The text of button can be set
 * during construction.  How the text header is obtained is also publicly 
 * adjustable.  This can be done synchronously by calling the setHeader() 
 * method.  Alternatively, the viewer can be told to set the header text 
 * asynchronously only when the user requests it by clicking the header 
 * button; this might be preferred if setting the header is costly (e.g. it
 * must be downloaded over the network.)  To do this, one provides the viewer 
 * a Runnable object (via the setHeaderFetcher()) that calls setHeader().  <p>
 *
 * <b>Habanero Support</b><p> 
 *
 * This viewer can be used as part of collaborative application within the
 * Habanero environment.  When run as part of Habanero, certain user actions 
 * are mirrored on each version of the application in the collaborative 
 * session.  These sharable events include requests for zoomed images (i.e. 
 * when a user clicks on a point or selects a box in the left image) as well
 * as requests for a change in the zoom factor (i.e. when a user clicks one
 * of the zoom factor buttons or enters a custom zoom factor).  Events that 
 * are not shared include requests to view the image header and coordinate
 * tracking with the mouse.  The viewer also supports late-joiners to the
 * Habanero session.  (See Habanero documentation 
 * (<a href="http://www.ncsa.uiuc.edu/SDG/Software/Habanero/Docs">http://www.ncsa.uiuc.edu/SDG/Software/Habanero/Docs</a>)
 * for more details about Habanero.)  <p>
 *
 * Events are shared by converting physical events (e.g. a button click) 
 * into logical events (e.g. change zoom factor).   All logical events have
 * the type ACTION_EVENT, and their logical type and arguments are encoded
 * together as a String stored in the Event field arg.  Several static
 * methods, <code>isLogical(Event), decodeLogicalEvent(Event), 
 * encodeLogicalEvent(Event),</code> and 
 * <code>getLogicalEventArg(Event)</code>, are thus provided for encoding 
 * and decoding logical events.  
 *
 * @version Alpha $Id: ADILViewer.java,v 1.11 1998/02/06 17:35:00 rplante Exp $
 * @author Raymond L. Plante
 * @author Daniel Goscha
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class ADILViewer extends SelectionViewer implements Cloneable {

    protected AV_ImageCanvas magview;
    protected AV_PositionPanel posview;
    protected AV_MainCanvas mainview;
    protected AV_MagnifierControl magctl;

    protected Point selpix = new Point(0, 0);
    protected Rectangle selbox  = new Rectangle(0, 0, 256, 256), 
	                selline = new Rectangle(0, 0, 0, 0);

    protected Viewable data=null;
    protected Slice slice=null;
    protected Slice magslice=null;
    protected ImageDisplayMap ppixtrx=null;
    protected ImageDisplayMap mpixtrx=null;
    protected ImageDisplayMap cpixtrx=null;
    protected CoordinateSystem coord=null;
    protected Image mainimg=null;
    protected boolean newViewable = false;
    protected Dimension sliceSize = new Dimension(0,0);
    protected int lepid;

    protected String hdrWinButtonText = "FITS Header Text";
    protected Button hdrWinButton;
    protected String header=null;
    protected SimpleFrame hdrWindow=null;
    protected TextArea hdrArea=null;
    protected boolean hdrDisplayed=false;
    protected Runnable fetcher=null;

    private boolean doPix=false, doCoord=true;

//    public final static int SHOW_HEADER  = 2002;
//    public final static int HIDE_HEADER  = 2003;

    /** a logical event type requesting a change in the zoom factor */
    public final static int CHANGE_ZOOM  = 2004;

    /** a logical event type requesting a zoomed view centered on a
     *  specified pixel */
    public final static int ZOOM_POINT   = 2006;

    /** a logical event type requesting a zoomed view of a requested box */
    public final static int ZOOM_BOX     = 2007;

    /**
     * create a viewer with no Viewable to display
     */
    public ADILViewer() {
	this(null);
    }

    /**
     * create a viewer with no Viewable to display
     * @param hdrButtonText text to use on header display request button
     */
    public ADILViewer(String hdrButtonText) {

	if (hdrButtonText != null) hdrWinButtonText = hdrButtonText;

	mainview = new AV_MainCanvas(this);
	magview = new AV_ImageCanvas(this);
	posview = new AV_PositionPanel(this);
	magctl = new AV_MagnifierControl(this);

	init();

	lepid = (new Random(System.currentTimeMillis())).nextInt();
    }

    /**
     * construct a Viewer using specific subcomponents
     */
    public ADILViewer(AV_MainCanvas mainc, AV_ImageCanvas magc, 
		      AV_PositionPanel posp, AV_MagnifierControl magp,
		      String hdrButtonText)
    {
	if (hdrButtonText != null) hdrWinButtonText = hdrButtonText;

	mainview = mainc;
	magview = magc;
	posview = posp;
	magctl = magp;

	mainview.parent = this;
	magview.parent = this;
	posview.parent = this;
	magctl.parent = this;

	init();
	lepid = (new Random(System.currentTimeMillis())).nextInt();
    }

    /**
     * construct a Viewer using specific subcomponents
     */
    public ADILViewer(AV_MainCanvas mainc, AV_ImageCanvas magc, 
		      AV_PositionPanel posp, AV_MagnifierControl magp)
    {
	this(mainc, magc, posp, magp, null);
    }

    /**
     * create a viewer and display in it a slice from a viewable image
     */
    public ADILViewer(Viewable data, Slice slice) {
	this();
	if (data != null) addViewable(data);
	setPixtrans();
	displaySlice(slice);
    }

    /**
     * assemble the components of the Viewer Panel
     */
    protected void init()
    {
	// Set up the layout
	//
	GridBagLayout bag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setFont(new Font("Helvetica", Font.PLAIN, 12));
	setLayout(bag);
	c.insets = new Insets(4,4,4,4);

	// Main Canvas
	Panel dpan = new Panel();
	dpan.setLayout(new BorderLayout(4, 0));
//	mainview.size = new Dimension(256, 256);
	mainview.setBackground(Color.black);
	dpan.add("West", mainview);
	setDrawPoint(true);

	// Magnifier Canvas
	magview.setBackground(Color.black);
	dpan.add("East", magview);
	c.gridx = c.gridy = 0;
	c.gridwidth = 8; c.gridheight = 4;
	c.anchor = c.NORTHWEST;
	bag.setConstraints(dpan, c);
	add(dpan);

	// Position Display
// 	c.gridx = 0;
// 	c.gridy = 4;
// 	c.anchor = c.WEST;
// 	c.fill = c.HORIZONTAL;
// 	c.gridwidth = 12;
// 	c.gridheight = 1;
// 	bag.setConstraints(posview, c);
// 	add(posview);

	// Position Display
	c.gridx = 0;
	c.gridy = 4;
	c.anchor = c.EAST;
	c.fill = c.HORIZONTAL;
	c.gridwidth = 6;
	c.gridheight = 1;
	bag.setConstraints(posview, c);
	add(posview);

	// Header button
	hdrWinButton = new Button(hdrWinButtonText);
	c.gridx = 6;
	c.gridy = 4;
	c.gridheight = 1;
	c.gridwidth = 2;
	c.fill = c.NONE;
	c.anchor = c.WEST;
 	bag.setConstraints(hdrWinButton, c);
 	add(hdrWinButton);

	// Magnifier Control
	c.gridx = 0;
	c.gridy = 5;
	c.gridwidth = 8;
	c.gridheight = 1;
	c.anchor = c.CENTER;
//	mag = magctl.getMagnification();
	bag.setConstraints(magctl, c);
	add(magctl);
    }

    /**
     * return the Viewer's logical event producer id
     */
    public int getID() { return lepid; }

    /**
     * set the Viewer's logical event producer id
     */
    public void setID(int id) { lepid = id; }

    /**
     * return true if event is a logical event
     */
    public static boolean isLogical(Event ev) { 
	return (ev.arg instanceof String && 
		((String)ev.arg).startsWith("LE:"));
    }

    /**
     * decode a Logical Event argument
     */
    public static Event decodeLogicalEvent(Event ev) {
	if (!isLogical(ev)) return null;

	String arg = (String) ev.arg;

	// get the Logical Event id and store as key
	int tab = arg.indexOf('\t');
	if (tab < 0 || arg.length() <= tab+1) return null;
	try {
	    ev.key = Integer.parseInt(arg.substring(3,tab));
	}
	catch (NumberFormatException ex) { 
	    System.err.println("Trouble parsing Logical Event ID: " +
			       ex.getMessage());
	    return null;
	}

	// get the Logical Event Producer's id and store as modifiers
	arg = arg.substring(tab+1);
	tab = arg.indexOf('\t');
	if (tab < 0) return null;
	try {
	    ev.modifiers = Integer.parseInt(arg.substring(0,tab));
	}
	catch (NumberFormatException ex) { 
	    System.err.println("Trouble parsing Logical Event Producer " +
			       "ID: " + ex.getMessage());
	    return null;
	}

	// successfully decoded; set clickCount as flag
	ev.clickCount = -1;

	return ev;
    }

    /**
     * Return a String containing the actual argument of a Logical Event
     */
    public static String getLogicalEventArg(Event ev) {
	if (! isLogical(ev)) return null;

	String arg = (String) ev.arg;
	int tab = arg.indexOf('\t');
	try {
	    arg = arg.substring(tab+1);
	    tab = arg.indexOf('\t');
	    arg = arg.substring(tab+1);
	}
	catch (StringIndexOutOfBoundsException ex) {
	    System.err.println("Error parsing logical event argument");
	    return null;
	}

	return arg;
    }

    /**
     * encode a logical event into the arg field of the given Event
     * @param ev        Event to edit
     * @param id        Logical Event id
     * @param epid      Logical Event producer's id
     * @param argument  String-encoded argument for logical event
     */
    public static void encodeLogicalEvent(Event ev, int id, int epid, 
					  String arg) 
    {
	ev.arg = new String("LE:" + id + "\t" + epid + "\t" + arg);
	ev.id = ev.ACTION_EVENT;
	ev.key = id;
	ev.modifiers = epid;
	ev.clickCount = -1;
    }

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
     * display a slice from the current Viewable data, or do nothing if
     * the current Viewable is not set.
     */
    public synchronized void displaySlice(Slice sl) {
	Slice reqsl = null;
	int[] isz = null;

	// save a copy of input slice
	if (data == null) return;
	if (sl != null) reqsl = new Slice(sl);

	// if no slice was given, come up with a default one
	if (reqsl == null) reqsl = data.getDefaultSlice();

	// if viewable doesn't provide a default slice, come up with one
	if (reqsl == null) {
	    isz = data.getSize();
	    for(int i=0; i < isz.length; i++) {
		if (isz[i] <= 0) {
		    System.err.println("Dataset looks empty; " + 
				       "display request aborted");
		    return;
		}
	    }
	    System.out.println("image size: " + isz[0] + " by " + isz[1]);

	    reqsl = new Slice(Math.max(isz.length, 2), 0, 1);
	    reqsl.setXaxisLength( (isz.length >= 1) ? isz[0] : 1 );
	    reqsl.setYaxisLength( (isz.length >= 2) ? isz[1] : 1 );
	}

	// Now that we are sure we have a usable slice; now we will 
	// save it as the current slice being displayed.
	slice = reqsl;

	isz = slice.getTrueSize();
	sliceSize.width  = isz[slice.getXaxis()]; 
	sliceSize.height = isz[slice.getYaxis()]; 

	// set the mapping from display pixels to data pixels
	if (newViewable) {
	    setPixtrans(mainview,
			slice.getTrueLength(slice.getXaxis()), 
			slice.getTrueLength(slice.getYaxis()));
	    newViewable = false;
	} else {
	    updatePixtrans(mainview,
			   slice.getTrueLength(slice.getXaxis()), 
			   slice.getTrueLength(slice.getYaxis()));
	}

	// update coordinate labels if we can
	if (coord != null) {
	    posview.setXCoordLabel(coord.getAxisLabel(slice.getXaxis()));
	    posview.setYCoordLabel(coord.getAxisLabel(slice.getYaxis()));
	    validate();
	}

	// extract the slice as an Image
	mainimg = data.getView(slice, (ColorModel) null, true);

	// now display the slice
	System.out.println("Loading image...");
	mainview.displayImage(mainimg);

	// update the view in the magnifier canvas
	Dimension sz = mainview.size();
	setPixelSelection(sz.width/2, sz.height/2);
	setBoxSelection(0, 0, sz.width, sz.height);
 	try { Thread.sleep(100); }          // this helps reduce image request
	catch (InterruptedException e) { }  // collisions between two canvases
	updateMagview();
    }

    /**
     * display a default slice of the current Viewable
     */
    public void displaySlice() { displaySlice(null); }

    /**
     * display a default slice of the current Viewable
     */
    public void displayViewable() { displaySlice(null); }

    /**
     * return the size of the display area
     */
    public Dimension getDisplaySize() { return mainview.size(); }

    /**
     * set the current selected display pixel.  The location is measured in
     * real display (i.e. screen) pixels relative to the upper left hand
     * corner.
     */
    public void setPixelSelection(int x, int y) { 

	// set the selected pixel
	selpix.x = x;
	selpix.y = y;

	// make sure mainview's copy is in sync
	mainview.pt.x = x;
	mainview.pt.y = y;

	// update the display in the Position panel
//	updatePosview(true, false, true);

	// update the display in the Magnifier canvas
//	updateMagview();
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

	// make sure mainview has the same copy
	mainview.boxstart.x = selbox.x;
	mainview.boxstart.y = selbox.y;
	mainview.boxend.x = selbox.x + selbox.width;
	mainview.boxend.y = selbox.y + selbox.height;

	// update the display in the Position panel
//	updatePosview(false, true, false);
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

	if (ppixtrx == null) setPixtrans();
	if (ppixtrx != null) 
	    return ppixtrx.getDataVoxel(selpix);
	else 
	    return new Voxel(2);
    }

    /** 
     * set the current selected Voxel to the one given as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public synchronized void setVoxelSelection(Voxel vox) {
	if (data == null) return;
	if (ppixtrx == null) setPixtrans();
	Voxel use = slice.projection(vox);
	Point pix = ppixtrx.getDisplayPixel(use);
	selpix.x = pix.x;
	selpix.y = pix.y;

	// make sure mainview's copy is in sync
	mainview.pt.x = selpix.x;
	mainview.pt.y = selpix.y;

//	updatePosview(true, false, true);
    }

    /**
     * return the current selected Slice, or null if there is no current
     * Viewable;
     */
    public Slice getSliceSelection() {
	if (data == null) return null;
	if (ppixtrx == null) setPixtrans();
	if (ppixtrx != null) 
	    return ppixtrx.getDataSlice(selbox);
	else 
	    return getViewSlice();
    }

    /**
     * return a Slice object describing the data currently being viewed, 
     * or null if there is no Viewable currently being viewed.
     */
    public Slice getViewSlice() {
	return (slice == null) ? null : (Slice) slice.clone();
    }

    /**
     * set the current selected Slice to the given Volume as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public synchronized void setSliceSelection(Volume vol) {
	if (data == null) return;
	if (ppixtrx == null) setPixtrans();
	Slice use = slice.projection(vol);

	selbox = ppixtrx.getDisplayRegion(use);

	// make sure mainview has the same copy
	mainview.boxstart.x = selbox.x;
	mainview.boxstart.y = selbox.y;
	mainview.boxend.x = selbox.x + selbox.width;
	mainview.boxend.y = selbox.y + selbox.height;
    }

    /**
     * update the ImageDisplayMap object, pixtrans (used to convert display
     * pixels into data pixels), to reflect changes in the current Viewable
     */
    private synchronized void setPixtrans(AV_ImageCanvas canvas, 
					  int wd, int ht) 
    {
	if (canvas == mainview) {
	    ppixtrx = new ImageDisplayMap();

	    if (data != null) {
		coord = data.getCoordSys();
	    }
	}
	else {
	    if (ppixtrx == null) setPixtrans(mainview);
	    mpixtrx = (ImageDisplayMap) cpixtrx.clone();
	}
	updatePixtrans(canvas, wd, ht);
    }

    /**
     * set the pixtrans object using the currently displayed image
     */
    private void setPixtrans() { setPixtrans(mainview, -1, -1); }

    /**
     * set the pixtrans object using the currently displayed image
     */
    private void setPixtrans(AV_ImageCanvas canvas) { 
	setPixtrans(canvas, -1, -1); 
    }

    /**
     * update the ImageDisplayMap object, pixtrans (used to convert display
     * pixels into data pixels), to reflect changes in the currently viewed
     * slice.
     */
    private synchronized void updatePixtrans(AV_ImageCanvas canvas, 
					     int wd, int ht) 
    {
	/* Three ImageDisplayMap objects are maintained: one for slice
	 * selection in the preview canvas, one for coordinates display 
	 * in the preview canvas, and one for coordinates display in
	 * the zoom canvas.  Normally only one would be needed for each
	 * canvas; however, in this viewer, we extract slices from the
	 * viewable using the GIF data origin convention but display
	 * coordinates using the FITS data origin convention.
	 */
	if (slice == null) return;

	ImageDisplayMap pixtrx;
	Slice sl;
//	String canstr;
	if (canvas == mainview) {
	    pixtrx = ppixtrx;
	    sl = slice;
//	    canstr = "mainview";
	}
	else {
	    pixtrx = mpixtrx;
	    sl = magslice;
//	    canstr = "magview ";
	}
	if (pixtrx == null) {
	    setPixtrans(canvas, wd, ht);   // this calls updatePixtrans()
	    return;
	}

	Voxel v = sl.getVoxel();
	Dimension dispdim = canvas.viewSize(wd, ht);
	Rectangle disprect = 
	    new Rectangle(0, 0, dispdim.width, dispdim.height);

	pixtrx.setSlice(sl);
	pixtrx.setDisplay(disprect);

	if (canvas == mainview) {
	    cpixtrx = (ImageDisplayMap) pixtrx.clone();
	    cpixtrx.yaxisReversed = true;
	}

	if (coord != null) {
	    posview.setXCoordLabel(coord.getAxisLabel(sl.getXaxis()));
	    posview.setYCoordLabel(coord.getAxisLabel(sl.getYaxis()));
//	    validate();
	}
    }

    /**
     * update the pixtrans object using the currently displayed image
     */
    private void updatePixtrans(AV_ImageCanvas canvas) { 
	updatePixtrans(canvas, -1, -1); 
    }

    /**
     * update the pixtrans object using the currently displayed image
     */
    private void updatePixtrans() { updatePixtrans(mainview, -1, -1); }

    /**
     * This implementation simple issues a repaint() when an image has become
     * available.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y,
			       int w, int h) {

	if ((flags&WIDTH) != 0 || (flags&HEIGHT) != 0) updatePixtrans();

	if ((flags & ALLBITS) == 0) return( true );

	repaint();
	return( false );
    }

    /**
     * update the display of positions in the position panel
     */
    public void updatePosview(AV_ImageCanvas ic, int x, int y) {
	double[] dpix = new double[2];
	Slice sl;
	Voxel vox;
	int xax, yax;

	ImageDisplayMap pixtrx;
	if (ic == mainview) {
	    pixtrx = cpixtrx;
	    sl = slice;
	}
	else {
	    pixtrx = mpixtrx;
	    sl = magslice;
	}
	if (pixtrx == null || sl == null) return;

	vox = pixtrx.getDataVoxel(new Point(x, y));
	xax = sl.getXaxis();
	yax = sl.getYaxis();
	dpix[0] = vox.axisPos(xax);
	dpix[1] = vox.axisPos(yax);

	if (doPix) {
	    posview.setPixelPos(dpix[0], dpix[1]);
	}

	if (doCoord && coord != null) {
	    String xval, yval;
	    try {
		CoordPos cpos = coord.getCoordPos(vox);
		xval = cpos.valueString(xax, 2);
		yval = cpos.valueString(yax, 2);
	    }
	    catch (CoordTransformException ex) {
		xval = yval = "Undefined";
	    }
	    posview.setCoordVal(xval, yval);
	}
    }

    /**
     * update the display of the image that appears in the magnifier canvas
     */
    public void updateMagview() { 

	if (data == null || slice == null) return;

	float mag = magctl.getMagnification();

	if (mag <= 0.0f) {

	    // use selected region
	    magslice = getSliceSelection();
	    setDrawPoint(false);
	    setDrawBox(true);
	}
	else {

	    // create magnified slice from selected pixel
	    Dimension area = magview.size();
	    area.width  = (int) (area.width/mag);
	    area.height = (int) (area.height/mag);

	    Voxel vox = getVoxelSelection();
//	    System.out.println("Getting mag over Voxel: " + vox);
	    magslice = (Slice) slice.clone();
//	    magslice.flags &= ~(magslice.CAN_INTERPOLATE);
// 	    magslice.setXaxisLocation(
// 		vox.axisPos(magslice.getXaxis())-area.width/2);
// 	    magslice.setYaxisLocation(
// 		vox.axisPos(magslice.getYaxis())-area.height/2);
	    magslice.setXaxisLocation(
		Math.floor(vox.axisPos(magslice.getXaxis())-area.width/2));
	    magslice.setYaxisLocation(
		Math.floor(vox.axisPos(magslice.getYaxis())-area.height/2));
	    magslice.setXaxisLength(area.width);
	    magslice.setYaxisLength(area.height);
	    magslice.setXaxisSampling(1.0/mag);
	    magslice.setYaxisSampling(1.0/mag);

	    setDrawPoint(true);
	    setDrawBox(false);
	}

	// get image from slice selection
// 	System.out.println("Using slice: pos(" + 
// 			   magslice.axisPos(magslice.getXaxis()) + ", " +
// 	                   magslice.axisPos(magslice.getYaxis()) + ") len(" + 
// 	                   magslice.getLength(magslice.getXaxis()) + ", " +
// 	                   magslice.getLength(magslice.getYaxis()) + ")" );
	Image magimg = data.getView(magslice, (ColorModel) null, true);

	// update the magslice pixel map
	int xax = magslice.getXaxis(),
	    yax = magslice.getYaxis();
	magslice.setAxisPos(xax, 1 + magslice.axisPos(xax));
	magslice.setAxisPos(yax, 1 + sliceSize.height - 
			    (magslice.axisPos(yax) + magslice.getLength(yax)));
	updatePixtrans(magview, 
		       magslice.getTrueLength(xax), 
		       magslice.getTrueLength(yax));

	// display the subimage
	magview.displayImage(magimg);
	magview.repaint();
    }

    public void setDrawBox(boolean b)   { mainview.drawBox = b;   }
    public void setDrawLine(boolean b)  { mainview.drawLine = b;  }
    public void setDrawPoint(boolean b) { mainview.drawPoint = b; }

    /**
     * set the routine that will fetch the header text when needed.  The
     * Runnable must call this ADILViewer's setHeader();
     */
    public void setHeaderFetcher(Runnable routine) {
	fetcher = routine;
    }

    /**
     * set the string to be used as a header
     */
    public void setHeader(String hdr) {  
	header = hdr; 

	if (hdrArea != null) {
	    hdrArea.setText(header);
	    hdrDisplayed = true;
	}
	else {
	    hdrDisplayed = false;
	}
    }

    /**
     * pop up a Frame with a box containing header text
     */
    public void showHeader() {
	if (hdrWindow == null) {
	    hdrArea = new TextArea("Getting Text...", 15, 80);
	    hdrWindow = new SimpleFrame("FITS Header");
	    hdrWindow.setLayout(new GridLayout(1, 1));
	    hdrWindow.add(hdrArea);
	    hdrWindow.resize(580, 225);
	    hdrWindow.move(400, 10);
	    hdrWindow.pack();
	    hdrDisplayed = false;
	}
	if (! hdrDisplayed) {
	    if (header == null) {
		if (fetcher != null) {
		    System.out.println("Fetching Header...");
		    Thread getHdr = new Thread(fetcher);
		    getHdr.start();
		}
		else {
		    hdrArea.setText("Header not available");
		}
	    }
	    else {
		hdrArea.setText(header);
		hdrDisplayed = true;
	    }
	}
	hdrWindow.show();
    }

    public boolean action(Event ev, Object what) {
	if (ev.target == hdrWinButton) {
	    if (hdrWindow == null || ! hdrWindow.isVisible()) 
		showHeader();
	    else
		hdrWindow.hide();
	}

	return false;
    }
	
    public boolean handleLogicalEvent(Event ev) {

//	System.err.println("ADILViewer: handling logical event: " + ev.arg);
	if (isLogical(ev)) {
	    if (ev.clickCount >= 0 && decodeLogicalEvent(ev) == null) 
		return false;

	    int id = ev.key;
	    String arg = getLogicalEventArg(ev);
//	    System.err.println("ADILViewer: event=" + id + " arg=" + arg);

	    int tab;
	    float mag = magctl.getMagnification();
	    switch (id) {
	    case CHANGE_ZOOM:
		try {
		    mag = Float.valueOf(arg).floatValue();
		}
		catch (NumberFormatException ex) { 
		    System.err.println("Trouble with CHANGE_ZOOM request: " +
				       ex.getMessage());
		    return false;
		}

		magctl.setMagnification(mag);    // update the button display
		if (mag == 0) {
		    setDrawPoint(false);         // enable box drawing
		    setDrawBox(true);
		}
		else if (mag > 0) {    
		    updateMagview();             // update the image display
		}

		break;

	    case ZOOM_POINT:
//		mainview.update();
		tab = arg.indexOf("\t");
		Double vx=null, vy=null;
		try {
		    vx = Double.valueOf(arg.substring(0,tab));
		    arg = arg.substring(tab+1);
		    vy = Double.valueOf(arg);
		}
		catch (StringIndexOutOfBoundsException ex) { 
		    System.err.println("Error parsing logical event " +
				       "argument (syntax); skipping...");
		    return false;
		}
		catch (NumberFormatException ex) {
		    System.err.println("Error parsing logical event " +
			       "argument (number format); skipping...");
		    return false;
		}

		synchronized (this) {
		    Voxel vsel = getVoxelSelection();
		    vsel.setAxisPos(slice.getXaxis(), vx.doubleValue());
		    vsel.setAxisPos(slice.getYaxis(), vy.doubleValue());
		    setVoxelSelection(vsel);
		}		    
		mainview.repaint();

		// update the magview
//		System.err.println("ADILViewer: Now updating mag-view");
		updateMagview();
		break;

	    case ZOOM_BOX:
		tab = arg.indexOf('\t');
		Double sx=null, sy=null, sw=null, sh=null;
		try {
		    sx = Double.valueOf(arg.substring(0,tab));
		    arg = arg.substring(tab+1);
		    tab = arg.indexOf('\t');
		    sy = Double.valueOf(arg.substring(0,tab));
		    arg = arg.substring(tab+1);
		    tab = arg.indexOf('\t');
		    sw = Double.valueOf(arg.substring(0,tab));
		    arg = arg.substring(tab+1);
		    sh = Double.valueOf(arg);
		}
		catch (StringIndexOutOfBoundsException ex) { 
		    System.err.println("Error parsing logical event " +
				       "argument (syntax); skipping...");
		    return false;
		}
		catch (NumberFormatException ex) {
		    System.err.println("Error parsing logical event " +
			       "argument (number format); skipping...");
		    return false;
		}

		synchronized (this) {
		    Slice ssel = getSliceSelection();
		    ssel.setAxisPos(slice.getXaxis(), sx.doubleValue());
		    ssel.setAxisPos(slice.getYaxis(), sy.doubleValue());
		    ssel.setXaxisLength(sw.doubleValue());
		    ssel.setYaxisLength(sh.doubleValue());
		    setSliceSelection(ssel);
		}		    
//		mainview.offpaint();
		mainview.update(mainview.getGraphics());

		// update the magview
//		System.err.println("ADILViewer: Now updating mag-view");
		updateMagview();
		break;

	    default:
		return false;
	    }
	}
	return false;
    }


//    public Dimension preferredSize() { return new Dimension(522, 388); }

    public Dimension minimumSize() { return new Dimension(522, 388); }

    public float getMagnification() { return magctl.getMagnification(); }

    public void setMagnification(float mag) { magctl.setMagnification(mag); }

    /** 
     * produce a copy of this viewer.
     */
    public Object clone() { 
	AV_MainCanvas mainout = (AV_MainCanvas) mainview.clone();
	AV_ImageCanvas magout = (AV_ImageCanvas) magview.clone();
	AV_PositionPanel posout = (AV_PositionPanel) posview.clone();
	AV_MagnifierControl ctlout = (AV_MagnifierControl) magctl.clone();
	ADILViewer out = new ADILViewer(mainout, magout, posout, ctlout, 
					hdrWinButtonText);

	return out;
    }

    public void hide() {
	if (hdrWindow != null && hdrWindow.isVisible()) hdrWindow.hide();
	super.hide();
    }

    /**
     * for use within an Habanero-ized application (similar to 
     * implementing the interface ncsa.habanero.Marshallable, a 
     * form of serialization)
     */
    public void marshallSelf(DataOutput out) throws IOException { 

	// current magnification
	out.writeFloat(magctl.getMagnification());

	// point selection
	out.writeInt(selpix.x);
	out.writeInt(selpix.y);

	// box selection
	out.writeInt(selbox.x);
	out.writeInt(selbox.y);
	out.writeInt(selbox.width);
	out.writeInt(selbox.height);

	// current Slice
//	out.writeInt(slice.getFirstAxisIndex());
	int n = slice.getNaxes();
	out.writeInt(n);
	out.writeInt(slice.getXaxis());
	out.writeInt(slice.getYaxis());
	double[] sldat = slice.getLocation(0);
	int i;
	for(i=0; i<n; i++) 
	    out.writeDouble(sldat[i]);
	sldat = slice.getSize(0);
	for(i=0; i<n; i++) 
	    out.writeDouble(sldat[i]);
	sldat = slice.getSampling(0);
	for(i=0; i<n; i++) 
	    out.writeDouble(sldat[i]);
    }

    /**
     * for use within an Habanero-ized application (similar to 
     * implementing the interface ncsa.habanero.Marshallable, a 
     * form of serialization)
     */
    public void unmarshallSelf(DataInput in) throws IOException { 
	magctl.setMagnification(in.readFloat());

	// point selection
	selpix.x = in.readInt();
	selpix.y = in.readInt();

	// box selection
	selbox.x = in.readInt();
	selbox.y = in.readInt();
	selbox.width = in.readInt();
	selbox.height = in.readInt();

	// current Slice
//	int fa = in.readInt();
	int n = in.readInt();
	if (n <= 0) throw new IOException("naxes = " + n + " < 0");
	int xa = in.readInt();
	if (xa < 0 || xa >= n) 
	    throw new IOException("x axis out of range");
	int ya = in.readInt();
	if (ya < 0 || ya >= n) 
	    throw new IOException("y axis out of range");
	slice = new Slice(n, xa, ya);
	double[] sldat = new double[n];
	int i;
	for(i=0; i<n; i++) 
	   sldat[i] = in.readDouble();
	slice.setLocation(sldat,0);
	for(i=0; i<n; i++) 
	   sldat[i] = in.readDouble();
	slice.setSize(sldat,0);
	for(i=0; i<n; i++) 
	   sldat[i] = in.readDouble();
	slice.setSampling(sldat,0);
    }

    /**
     * shut down this viewer. This method will dispose of the header window, 
     * release data references, and black out the screen.  This method is
     * provided for use as an Habanero applet.  
     */
    public synchronized void shutDown() {

	// dispose of child window(s)
	if (hdrWindow != null) hdrWindow.dispose();

	// forget everything
	data = null;
	slice = magslice =null;
	ppixtrx = mpixtrx = cpixtrx;
	coord = null;
	mainimg = null;
	header = null;
	newViewable = hdrDisplayed = false;
	fetcher = null;

	// paint it black
	mainview.clear();
	magview.clear();
    }
	
}

class AV_MainCanvas extends AV_ImageCanvas {

    boolean doingselect=false;
    Point boxstart = new Point(0,0),
          boxend = new Point(0,0), 
          pt = new Point(0,0);
//    Color lineColor = Color.white;
    Color lineColor = Color.magenta;

    public boolean drawPoint=false;
    public boolean drawBox=false;
    public boolean drawLine=false;

    public AV_MainCanvas(ADILViewer parent) {
	super(parent);
    }

    public AV_MainCanvas(ADILViewer parent, Dimension viewSize) {
	super(parent, viewSize);
    }

    public AV_MainCanvas() {
	super();
    }

    public boolean mouseUp(Event evt, int x, int y) {

	if (parent.magview.isLoading()) {
	    System.err.println("Magnifier updating; please wait...");

	    // don't share this event (when part of a Hablet)
	    return true;
	}

	pt.x = x;
	pt.y = y;
	Voxel vsel = null;
	Slice ssel = null;
	float mag;

	if (parent.data == null) return super.mouseUp(evt, x, y);

	synchronized (parent) {
	    if (parent != null && doingselect) {
		if (drawPoint) parent.setPixelSelection(x, y);
		if (drawBox) parent.setBoxSelection(boxstart.x, boxstart.y, 
						    boxend.x, boxend.y);
		if (drawLine) parent.setLineSelection(boxstart.x, boxstart.y, 
						      boxend.x, boxend.y);
	    }
	    doingselect = false;
	    vsel = parent.getVoxelSelection();
	    ssel = parent.getSliceSelection();
	    mag = parent.getMagnification();
	}

	if (parent != null) {
	    if (mag > 0.0f) {
		ADILViewer.encodeLogicalEvent(evt, ADILViewer.ZOOM_POINT, 
					      parent.lepid,
		  new String(vsel.axisPos(parent.slice.getXaxis()) + "\t" +
			     vsel.axisPos(parent.slice.getYaxis())) );
		parent.handleLogicalEvent(evt);
	    } 
	    else if (boxstart.x != boxend.x && boxstart.y != boxend.y) {
		ADILViewer.encodeLogicalEvent(evt, ADILViewer.ZOOM_BOX, 
					      parent.lepid,
		  new String(ssel.axisPos(parent.slice.getXaxis())   + "\t" +
			     ssel.axisPos(parent.slice.getYaxis())   + "\t" +
			     ssel.getLength(parent.slice.getXaxis()) + "\t" +
			     ssel.getLength(parent.slice.getYaxis())) );
		parent.handleLogicalEvent(evt);
	    }
        }

	return false;
    }

    public boolean mouseDown(Event evt, int x, int y) {
	if (parent.data == null) return super.mouseDown(evt, x, y);

	if (parent.magview.isLoading()) {
	    // don't share this event (when part of a Hablet)
	    return true;
	}

	boxstart.x = boxend.x = pt.x = x;
	boxstart.y = boxend.y = pt.y = y;
	if (drawPoint && parent != null) parent.setPixelSelection(x, y);
	doingselect = true;
	update(getGraphics());

	super.mouseDown(evt, x, y);

	return true;
    }

    public boolean mouseDrag(Event evt, int x, int y) {

	if (parent.data == null) return true;
	if (! drawBox) return false;

	if (parent.magview.isLoading()) {
	    // don't share this event (when part of a Hablet)
	    return true;
	}

	Dimension sz = size();
	if (x < 0) x = 0;
	if (x >= sz.width) x = sz.width-1;
	if (y < 0) y = 0;
	if (y >= sz.height) y = sz.height-1;
	boxend.x = pt.x = x;
	boxend.y = pt.y = y;

	if (! doingselect) {
	    boxstart.x = x;
	    boxstart.y = y;
	    doingselect = true;
	}
	if (parent != null) {
	    if (drawPoint) parent.setPixelSelection(x, y);
	    if (drawBox) parent.setBoxSelection(boxstart.x, boxstart.y, 
						boxend.x, boxend.y);
	    if (drawLine) parent.setLineSelection(boxstart.x, boxstart.y, 
						  boxend.x, boxend.y);
	}
	update(getGraphics());
// 	repaint(Math.min(boxstart.x,boxend.x), 
// 		Math.min(boxstart.y, boxstart.y),
// 		Math.abs(boxend.x-boxstart.x)+1, 
// 		Math.abs(boxend.y-boxstart.y)+1);
	return true;
    }

//    public void paint(Graphics g) {
    public boolean tryPaint(Graphics g) {
	boolean painted = super.tryPaint(g);
	
	g.setColor(lineColor);
	if (drawBox || drawLine) {
	    if (drawBox) g.drawRect(Math.min(boxstart.x, boxend.x), 
				    Math.min(boxstart.y, boxend.y),
				    Math.abs(boxend.x-boxstart.x), 
				    Math.abs(boxend.y-boxstart.y));
	    if (drawLine) g.drawLine(boxstart.x, boxstart.y, 
				     boxend.x, boxend.y);
	}
	else if (drawPoint) {
	    g.drawRect(pt.x, pt.y, 1, 1);
	}

	return painted;
    }

    /**
     * make a copy of this canvas with all the current settings
     */
    public Object clone() {
	AV_MainCanvas out = (AV_MainCanvas) super.clone();
	out.doingselect=doingselect;
	out.boxstart = new Point(boxstart.x, boxstart.y);
	out.boxend = new Point(boxend.x, boxend.y);
	out.pt = new Point(pt.x, pt.y);
	out.lineColor = lineColor;

	out.drawPoint=drawPoint;
	out.drawBox=drawBox;
	out.drawLine=drawLine;

	return out;
    }
}

class AV_ImageCanvas extends ImageCanvas implements Cloneable {

    protected ADILViewer parent=null;

    public AV_ImageCanvas() {
	this(null, null);
    }

    public AV_ImageCanvas(ADILViewer parent) {
	this(parent, null);
    }

    public AV_ImageCanvas(ADILViewer parent, Dimension viewSize) {
	super();
	this.parent = parent;
	if (viewSize != null) {
	    if (viewSize.width <= 0) viewSize.width = 256;
	    if (viewSize.height <= 0) viewSize.width = 256;
	}
	size = (viewSize == null) ? new Dimension(256, 256) : viewSize;
	resize(size);
	setMode(ImageCanvas.SIZE_IMAGE_FLUSH);
    }

    public boolean mouseMove(Event event, int x, int y) {
	if (parent != null) parent.updatePosview(this, x, y);
	return false;
    }

    public boolean mouseDown(Event evt, int x, int y) {

	if (parent.coord != null) {
	    ImageDisplayMap pixtrx = 
		(this instanceof AV_MainCanvas) ? 
		parent.cpixtrx : parent.mpixtrx;
	    Voxel v = pixtrx.getDataVoxel(new Point(x,y));
	    Slice sl = pixtrx.getSlice();
	    int xax = sl.getXaxis(),
		yax = sl.getYaxis();
	    v.setAxisPos(xax, Math.floor(v.axisPos(xax)));
	    v.setAxisPos(yax, Math.floor(v.axisPos(yax)));
	    try {
		CoordPos cp = parent.coord.getCoordPos(v);
		System.out.println("Selected: (" + v.axisPos(xax) + ", " + 
				   v.axisPos(yax) + ") ==> " + 
				   cp.valueString(xax) + ", " + 
				   cp.valueString(yax));
	    }
	    catch (CoordTransformException ex) {
		System.out.println("Selected: (" + v.axisPos(0) + ", " + 
				   v.axisPos(1) + 
				   ") ==> Undefined coordinate position");
	    }
	}

	return false;
    }

    /**
     * make a copy of this canvas with all the current settings
     */
    public Object clone() {
	AV_ImageCanvas out = (AV_ImageCanvas) super.clone();
	out.parent = parent;

	return out;
    }
}

class AV_MagnifierControl extends Panel implements Cloneable {

    float mag = 1.0f;
    Checkbox[] btn;
    CheckboxGroup btns;
    TextField customEntry;
    ADILViewer parent = null;

    public AV_MagnifierControl(ADILViewer parent) { this(-1, parent); }

    public AV_MagnifierControl(int maglev, ADILViewer parent) {
	int maxmag=4;

	this.parent = parent;
	if (maglev < 0 || maglev > maxmag) maglev = 1;
	mag = (float) maglev;
	boolean[] btnon = new boolean[6];
	for(int i=0; i <= maxmag; i++) btnon[i] = false;
	btnon[maglev] = true;

	// Buttons
	btns = new CheckboxGroup();
	btn = new Checkbox[maxmag+2];
	setLayout(new FlowLayout());

	btn[0] = new Checkbox("Region", btns, btnon[0]);
	add(btn[0]);

	btn[1] = new Checkbox("100%", btns, btnon[1]);
	add(btn[1]);
	
	btn[2] = new Checkbox("200%", btns, btnon[2]);
	add(btn[2]);
	
	btn[3] = new Checkbox("300%", btns, btnon[3]);
	add(btn[3]);
	
	btn[4] = new Checkbox("400%", btns, btnon[4]);
	add(btn[4]);

	btn[5] = new Checkbox("Custom %", btns, btnon[5]);
	add(btn[5]);

	customEntry = new TextField("500", 4);
//	customEntry.setEditable(btn[5].getState());
	add(customEntry);
    }

    public boolean action(Event ev, Object obj) {

//	if (ev.target instanceof Checkbox || ev.target instanceof TextField)
//	    System.err.println("Handling physical event: " + ev.id);
	float mag = -1.0f;
	if (ev.target == btn[0]) mag = 0.0f;
	else if (ev.target == btn[1]) mag = 1.0f;
	else if (ev.target == btn[2]) mag = 2.0f;
	else if (ev.target == btn[3]) mag = 3.0f;
	else if (ev.target == btn[4]) mag = 4.0f;
	else if (ev.target == btn[5] || ev.target == customEntry) {
	    Float entry;
	    try {
		entry = Float.valueOf(customEntry.getText());
	    } catch (NumberFormatException ex) {
		System.out.println("Entry not recognized as float value: " +
				   customEntry.getText());
		return false;
	    }
	    mag = entry.floatValue() / 100.0f;
	}
	else {
	    super.action(ev, obj);
	}

	// if this was a physical event requesting a change in the
	// zoom factor, translate into a logical event requesting the
	// same and hand it off to the (parent) Viewer
	//
	if (parent != null && mag >= 0) {
	    ADILViewer.encodeLogicalEvent(ev, ADILViewer.CHANGE_ZOOM,
					  parent.lepid, 
					  (new Float(mag)).toString());
	    parent.handleLogicalEvent(ev);
	}

	return false;
    }

    public float getMagnification() { return mag; }

    public void setMagnification(float mag) {
//	if (mag <= 0) return;
	this.mag = mag;
	if (mag <= 0) btns.setCurrent(btn[0]);
	else if (mag == 1) btns.setCurrent(btn[1]);
	else if (mag == 2) btns.setCurrent(btn[2]);
	else if (mag == 3) btns.setCurrent(btn[3]);
	else if (mag == 4) btns.setCurrent(btn[4]);
	else { 
	    btns.setCurrent(btn[5]);

	    float entry;
	    try {
		entry = Float.valueOf(customEntry.getText()).floatValue();
	    } catch (NumberFormatException ex) {
		entry = -1.0f;
	    }
	    if (entry != mag*100.0f) 
		customEntry.setText(Float.toString(mag*100.0f));
	}
	return;
    }

    public Object clone() {
	AV_MagnifierControl out = new AV_MagnifierControl(parent);
	out.mag = mag;
	for(int i=0; i < btn.length; i++) 
	    out.btn[i].setState(btn[i].getState());
	out.customEntry.setText(customEntry.getText());

	return out;
    }
}	

class AV_PositionPanel extends Panel implements Cloneable { 

    Label xLabel = new Label("X Axis:         ", Label.LEFT);
    Label yLabel = new Label("Y Axis:         ", Label.LEFT);
    Label xPos = new Label("                  ");
    Label yPos = new Label("                  ");

    ADILViewer parent=null;

    public AV_PositionPanel(ADILViewer parent) {

	this.parent = parent;

	setLayout(new FlowLayout(FlowLayout.LEFT));
//	setLayout(new GridLayout(1, 4));
	add(xLabel);
	add(xPos);
	add(yLabel);
	add(yPos);
//	add(parent.hdrWinButton);

    }

    public void setPixelPos(Object x, Object y) { }

    public void setPixelPos(double x, double y) { }

    public synchronized void setCoordVal(Object x, Object y) {
	String use = new String(x.toString());
	if (! use.equals(xPos.getText())) {
	    xPos.setText(use);
//	    xPos.repaint();
	}
	use = new String(y.toString());
	if (! use.equals(yPos.getText())) {
	    yPos.setText(use);
//	    yPos.repaint();
	}
    }

    public synchronized void setXCoordLabel(Object v) {
	String use = new String(v.toString() + ":");
	if (! use.equals(xLabel.getText())) {
	    xLabel.setText(use);
//	    xLabel.repaint();
	}
    }

    public synchronized void setYCoordLabel(Object v) {
	String use = new String(v.toString() + ":");
	if (! use.equals(yLabel.getText())) {
	    yLabel.setText(use);
//	    yLabel.repaint();
	}
    }

    public void setBoxStart(Object x, Object y) { }

    public void setBoxStart(int x, int y) { }

    public synchronized void setBoxEnd(Object x, Object y) { } 

    public synchronized void setBoxEnd(int x, int y) { } 

    public Dimension minimumSize() {
	Dimension out = super.minimumSize();
	out.width = 300;
	return out;
    }

//    public void makeHeaderFrame() { }

    public Object clone() {
	AV_PositionPanel out = new AV_PositionPanel(parent);
	out.xLabel.setText(xLabel.getText());
	out.xPos.setText(xPos.getText());
	out.yLabel.setText(yLabel.getText());
	out.yPos.setText(yPos.getText());

	return out;
    }
}

