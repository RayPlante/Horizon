/** 
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-97 Board of Trustees of the University of Illinois
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
 *  96summer rlp  original version
 *  96dec    rlp  uses ImageCanvas
 *  97jan    rlp  uses ImageDataOrigin
 *  97aug06  rlp  updated for new coordinate classes; switch to ImageDisplayMap
 *  97oct21  rlp  updated for changes made to Slice (first axis index)
 *  97dec06  rlp  updated for changes in ImageCanvas: offpaint is now 
 *                  protected; calls replaced with update() or repaint()
 *  97dec07  rlp  made more robust to dataset read errors: aborts display
 *                  request if dataset has 0 size.  repaint(): also sends
 *                  repaint() to subcanvases.  remove repaint()s to label
 *                  components (which caused Win to throw exceptions)
 */
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.*;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.awt.ImageCanvas;
import ncsa.horizon.util.*;
import ncsa.horizon.coordinates.*;

/*
 * an example of a viewer for scientific data browsing
 * 
 * @version Alpha $Id: SciDat1Viewer.java,v 0.13 1997/12/08 16:30:52 rplante Exp $
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */

public class SciDat1Viewer extends SelectionViewer implements Cloneable {

    protected float mag;
    protected MagnifierPanel magview;
    protected PositionPanel posview;
    protected MainCanvas mainview;

    protected Point selpix = new Point(0, 0);
    protected Rectangle selbox  = new Rectangle(0, 0, 256, 256), 
	                selline = new Rectangle(0, 0, 0, 0);

    protected Viewable data=null;
    protected Slice slice=null;
    protected ImageDisplayMap pixtrx=null;
    protected CoordinateSystem coord=null;
    protected Image mainimg=null;
    protected boolean newViewable = false;

    /**
     * create a viewer with no Viewable to display
     */
    public SciDat1Viewer() {
	mainview = new MainCanvas(this);
	magview = new MagnifierPanel(this);
	posview = new PositionPanel(this);

	init();
    }

    /**
     * construct a Viewer using specific subcomponents
     */
    public SciDat1Viewer(MainCanvas mainc, MagnifierPanel magp, 
			 PositionPanel posp)
    {
	mainview = mainc;
	magview = magp;
	posview = posp;

	mainview.parent = this;
	magview.parent = this;
	posview.parent = this;

	init();
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
	setFont(new Font("Helvetica", Font.PLAIN, 14));
	setLayout(bag);
	c.insets = new Insets(4,4,4,4);

	// Main Canvas
//	mainview.size = new Dimension(256, 256);
	mainview.setBackground(Color.black);
	c.gridx = c.gridy = 0;
	c.gridwidth = c.gridheight = 4;
	bag.setConstraints(mainview, c);
	add(mainview);

	// Magnifier
	c.gridx = 4;
	c.gridy = 0;
	c.gridwidth = 3;
	c.gridheight = 2;
	c.anchor = c.NORTHWEST;
	bag.setConstraints(magview, c);
	add(magview);
	mag = magview.getMagnification();

	// Position Display
	c.gridx = 4;
	c.gridy = 2;
	c.gridwidth = 3;
	c.gridheight = 2;
	bag.setConstraints(posview, c);
	add(posview);
    }

    /**
     * create a viewer and display in it a slice from a viewable image
     */
    public SciDat1Viewer(Viewable data, Slice slice) {
	this();
	if (data != null) addViewable(data);
	setPixtrans();
	displaySlice(slice);
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

	// save a copy of input slice
	if (sl != null) reqsl = (Slice) sl.clone();
	if (data == null) return;

	// if no slice was given, use the viewable's default slice
	if (reqsl == null) reqsl = data.getDefaultSlice();

	// if viewable doesn't provide a default slice, come up with one
	if (reqsl == null) {
	    int[] isz = data.getSize();
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

	// set the mapping from display pixels to data pixels
	if (newViewable) {
	    setPixtrans();
	    newViewable = false;
	} else {
	    updatePixtrans();
	}

	// extract the slice as an Image
	mainimg = data.getView(slice, (ColorModel) null, true);

	// now display the slice
	System.out.println("Loading image...");
	mainview.displayImage(mainimg);

	// update the view in the magnifier canvas
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

	// update the display in the Position panel
	updatePosview(true, false, true);
	mainview.updateGraphics(selpix, null);

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

	// update the display in the Position panel
	updatePosview(false, true, false);
	mainview.updateGraphics(null, selbox);
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
	if (pixtrx == null) setPixtrans();
	return pixtrx.getDataVoxel(selpix);
    }

    /** 
     * set the current selected Voxel to the one given as projected onto 
     * the currently displayed Slice, or do nothing if there is no current 
     * Viewable.
     */
    public synchronized void setVoxelSelection(Voxel vox) {
	if (data == null) return;
	if (pixtrx == null) setPixtrans();
	selpix = pixtrx.getDisplayPixel(vox);
	updatePosview(true, false, true);
	mainview.updateGraphics(selpix, null);
    }

    /**
     * return the current selected Slice, or null if there is no current
     * Viewable;
     */
    public Slice getSliceSelection() {
	if (data == null) return null;
	if (pixtrx == null) setPixtrans();
	return pixtrx.getDataSlice(selbox);
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
	if (pixtrx == null) setPixtrans();
	Slice use = slice.projection(vol);
	selbox = pixtrx.getDisplayRegion(use);
	updatePosview(true, false, true);   // once for one end of the box
	updatePosview(false, true, false);    // once for the other
	mainview.updateGraphics(null, selbox);
    }

    /**
     * update the ImageDisplayMap object, pixtrans (used to convert display
     * pixels into data pixels), to reflect changes in the current Viewable
     */
    private synchronized void setPixtrans() {

	Boolean xaxisReversed=null, yaxisReversed=null;
	pixtrx = new ImageDisplayMap();

	if (data != null) {
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
		pixtrx.xaxisReversed = xaxisReversed.booleanValue();
	    if (yaxisReversed != null) 
		pixtrx.yaxisReversed = yaxisReversed.booleanValue();

	    coord = data.getCoordSys();
	}
	updatePixtrans();
    }

    /**
     * update the ImageDisplayMap object, pixtrans (used to convert display
     * pixels into data pixels), to reflect changes in the currently viewed
     * slice.
     */
    private synchronized void updatePixtrans() {

	if (slice == null) return;
	if (pixtrx == null) {
	    setPixtrans();
	    return;
	}

	Dimension imgdim = new Dimension(slice.getTrueLength(slice.getXaxis()),
					 slice.getTrueLength(slice.getYaxis()));
	Dimension dispdim = mainview.viewSize(imgdim.width, imgdim.height);
	Rectangle dispreg = new Rectangle(dispdim);

	pixtrx.setSlice(slice);
	pixtrx.setDisplay(dispreg);

	if (coord != null) {
	    posview.setXCoordLabel(coord.getAxisLabel(slice.getXaxis()));
	    posview.setYCoordLabel(coord.getAxisLabel(slice.getYaxis()));
	}
    }

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
    public void updatePosview(boolean doPix, boolean doBox, boolean doCoord) {
	int fa, xax, yax;

	Voxel vox = getVoxelSelection();
	if (slice != null) {
	    xax = slice.getXaxis(); 
	    yax = slice.getYaxis();
	}
	else {
	    xax = 0;
	    yax = 1;
	}

	if (doPix) {
	    posview.setPixelPos((int) vox.axisPos(xax), 
				(int) vox.axisPos(yax));
	}

	if (doCoord && coord != null) {
	    try {
		CoordPos cpos = coord.getCoordPos(vox.getValues(0));
		posview.setCoordVal(cpos.valueString(xax), 
				    cpos.valueString(yax));
	    } catch (CoordTransformException ex) {
		String undef = "Undefined";
		posview.setCoordVal(undef, undef);
	    }
	}

	if (doBox) {
	    if (pixtrx == null) setPixtrans();
	    Slice selsl = pixtrx.getDataSlice(selbox);
	    double[] use = { selsl.axisPos(xax), 
			     selsl.axisPos(yax) };
	    posview.setBoxStart((int) use[0], (int) use[1]);
	    use[0] += selsl.getLength(xax);
	    use[1] += selsl.getLength(yax);
				
	    posview.setBoxEnd((int) use[0], (int) use[1]);
	}
    }

    /**
     * update the display of the image that appears in the magnifier canvas
     */
    public void updateMagview() { 

	if (data == null || slice == null || !magview.getState()) return;

	float mag = magview.getMagnification();

	// OO way
	//
	// get subimage
	Dimension area = magview.displaySize();
	area.width  = (int) (area.width/mag);
	area.height = (int) (area.height/mag);

	ncsa.horizon.util.Voxel vox = getVoxelSelection();
	Slice magsl = (Slice) slice.clone();
	magsl.flags &= ~(magsl.CAN_INTERPOLATE);
	magsl.setXaxisLocation(vox.axisPos(magsl.getXaxis())-area.width/2);
	magsl.setYaxisLocation(vox.axisPos(magsl.getYaxis())-area.height/2);
	magsl.setXaxisLength(area.width);
	magsl.setYaxisLength(area.height);
	magsl.setXaxisSampling(1.0/mag);
	magsl.setYaxisSampling(1.0/mag);

	Image magimg = data.getView(magsl, (ColorModel) null, true);

	// display the subimage
	magview.displayImage(magimg);
    }

    public void setDrawBox(boolean b)   { mainview.drawBox = b;   }
    public void setDrawLine(boolean b)  { mainview.drawLine = b;  }
    public void setDrawPoint(boolean b) { mainview.drawPoint = b; }

    public void repaint() {
	// repaint canvases as well
	super.repaint();
	mainview.repaint(20);
	magview.repaint();
    }

    /** 
     * produce a copy of this viewer.
     */
    public Object clone() { 
	MainCanvas mainout = (MainCanvas) mainview.clone();
	MagnifierPanel magout = (MagnifierPanel) magview.clone();
	PositionPanel posout = (PositionPanel) posview.clone();
	SciDat1Viewer out = new SciDat1Viewer(mainout, magout, posout);

	return out;
    }
	
}

class MainCanvas extends ImageCanvas {

    SciDat1Viewer parent=null;
    boolean doingselect=false, internalSelect=false;
    Point boxstart = new Point(0,0),
          boxend = new Point(0,0), 
          pt = new Point(0,0);
//    Color lineColor = Color.white;
    Color lineColor = Color.magenta;

    public boolean drawPoint=false;
    public boolean drawBox=false;
    public boolean drawLine=false;

    public MainCanvas(SciDat1Viewer parent) {
	super();
	this.parent = parent;
	size = new Dimension(256, 256);
	resize(size);
	setMode(ImageCanvas.SIZE_IMAGE_FLUSH);
    }

    public MainCanvas() {
	super();
	size = new Dimension(256, 256);
	resize(size);
	setMode(ImageCanvas.SIZE_IMAGE_FLUSH);
    }

    public void displayImage(Image im) {
	super.displayImage(im);
	System.out.println("Image is loaded");
    }

    public void updateGraphics(Point selpt, Rectangle selbox) {
	if (internalSelect) {
	    internalSelect = false;
	    return;
	}

	if (selpt != null) {
	    pt.x = selpt.x;
	    pt.y = selpt.y;
	}
	if (selbox != null) {
	    if (selbox.width < 0) {
		boxstart.x = selbox.x+selbox.width-1;
		boxend.x = selbox.x;
	    } else {
		boxstart.x = selbox.x;
		boxend.x = selbox.x+selbox.width-1;
	    }	    
	    if (selbox.height < 0) {
		boxstart.y = selbox.y+selbox.height-1;
		boxend.y = selbox.y;
	    } else {
		boxstart.y = selbox.y;
		boxend.y = selbox.y+selbox.height-1;
	    }	    
	}
	update(getGraphics());
    }

    public boolean mouseUp(Event evt, int x, int y) {
	pt.x = x;
	pt.y = y;
	if (parent != null && doingselect) {
	    internalSelect = true;
	    if (drawPoint) parent.setPixelSelection(x, y);
	    if (drawBox) parent.setBoxSelection(boxstart.x, boxstart.y, 
						boxend.x, boxend.y);
	    if (drawLine) parent.setLineSelection(boxstart.x, boxstart.y, 
						  boxend.x, boxend.y);
	    internalSelect = false;
	}
	doingselect = false;
	update(getGraphics());

	// update the magview
	parent.updateMagview();
	return true;
    }

    public boolean mouseDown(Event evt, int x, int y) {
	boxstart.x = boxend.x = pt.x = x;
	boxstart.y = boxend.y = pt.y = y;
	internalSelect = true;
	if (drawPoint && parent != null) parent.setPixelSelection(x, y);
	internalSelect = false;
	doingselect = true;
	update(getGraphics());
	return true;
    }

    public boolean mouseDrag(Event evt, int x, int y) {
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
	    internalSelect = true;
	    if (drawPoint) parent.setPixelSelection(x, y);
	    if (drawBox) parent.setBoxSelection(boxstart.x, boxstart.y, 
						boxend.x, boxend.y);
	    if (drawLine) parent.setLineSelection(boxstart.x, boxstart.y, 
						  boxend.x, boxend.y);
	    internalSelect = false;
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
	MainCanvas out = (MainCanvas) super.clone();
	out.parent = parent;
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

class MagnifierPanel extends Panel implements Cloneable {

    float mag = 1.0f;
    ImageCanvas disp;
    Checkbox[] btn;
    SciDat1Viewer parent = null;

    public MagnifierPanel(SciDat1Viewer parent) {
	this(0, parent);
    }

    public MagnifierPanel(int maglev, SciDat1Viewer parent) {

	int maxmag=4;
	CheckboxGroup btns;

	this.parent = parent;
	if (maglev < 1 || maglev > maxmag) maglev = 1;
	mag = (float) maglev;
	boolean[] btnon = new boolean[5];
	for(int i=0; i <= maxmag; i++) btnon[i] = false;
	btnon[maglev] = true;
//	System.out.println("maglev=" + maglev);

	GridBagLayout bag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setFont(new Font("Helvetica", Font.PLAIN, 14));
	setLayout(bag);
	c.weightx = c.weighty = 1.0;
	c.insets = new Insets(0,2,2,2);

	// Magnifier Canvas
	disp = new ImageCanvas(128,128);
	disp.setMode(ImageCanvas.SIZE_IMAGE_FLUSH);
//	disp.size = new Dimension(128,128);
	disp.setBackground(Color.black);
//	disp.show(true);
	c.gridx = c.gridy = 0;
	c.gridwidth = c.gridheight = 5;
	c.anchor = c.WEST;
	bag.setConstraints(disp, c);
	add(disp);

	// Buttons
	c.gridx = 5;
	c.gridwidth = 3;
	c.gridheight = 1;
	c.anchor = c.CENTER;
//	c.ipadx = c.ipady = 2;
//	c.anchor = GridBagConstraints.WEST;
	btns = new CheckboxGroup();
	btn = new Checkbox[maxmag+1];

	c.gridy = 0;
	btn[0] = new Checkbox("Magnifier", null, btnon[0]);
	bag.setConstraints(btn[0], c);
	add(btn[0]);

	c.gridy = 1;
	btn[1] = new Checkbox("x1", btns, btnon[1]);
	bag.setConstraints(btn[1], c);
	add(btn[1]);
	
	c.gridy = 2;
	btn[2] = new Checkbox("x2", btns, btnon[2]);
	bag.setConstraints(btn[2], c);
	add(btn[2]);
	
	c.gridy = 3;
	btn[3] = new Checkbox("x3", btns, btnon[3]);
	bag.setConstraints(btn[3], c);
	add(btn[3]);
	
	c.gridy = 4;
	btn[4] = new Checkbox("x4", btns, btnon[4]);
	bag.setConstraints(btn[4], c);
	add(btn[4]);
    }

    public boolean action(Event event, Object obj) {
	boolean handled = super.action(event, obj);

	if (event.target == btn[0]);
	else if (event.target == btn[1]) mag = 1.0f;
	else if (event.target == btn[2]) mag = 2.0f;
	else if (event.target == btn[3]) mag = 3.0f;
	else if (event.target == btn[4]) mag = 4.0f;
	else return handled;

	if (parent != null && btn[0].getState()) {
//	    System.out.println("user wants a new magnification");
	    parent.updateMagview();
	}
	return true;
    }

    public float getMagnification() { return mag; }

    public void setMagnification(float mag) {
	if (mag <= 0) return;
	this.mag = mag;
	return;
    }

    public void displayImage(Image im) { 
	if (btn[0].getState()) disp.displayImage(im); 
    }

    public void setState(boolean on) { btn[0].setState(on); }
    public boolean getState() { return btn[0].getState(); }

    public Dimension displaySize() { return disp.size(); }

    public void repaint() {
	// be sure to repaint canvas as well
	super.repaint();
	disp.repaint(20);
    }

    public Object clone() {
	MagnifierPanel out = new MagnifierPanel(parent);
	out.mag = mag;
	out.disp = (ImageCanvas) disp.clone();
	for(int i=0; i < btn.length; i++) 
	    out.btn[i].setState(btn[i].getState());

	return out;
    }
}	

class PositionPanel extends Panel implements Cloneable { 

    SciDat1Viewer parent=null;
    Label pixpos, xcoordlab, xcoordval, ycoordlab, ycoordval;
    Checkbox drawbox;
    Label boxstart, boxend;

    public PositionPanel(SciDat1Viewer parent) {

	Label statlab;
	this.parent = parent;
	GridBagLayout bag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setFont(new Font("Helvetica", Font.PLAIN, 14));
	setLayout(bag);
//	c.weightx = c.weighty = 1.0;

	// Pixel Position Label
	statlab = new Label("Data Pixel: ", Label.LEFT);
	c.gridx = c.gridy = 0;
	c.gridwidth = 4;
	c.gridheight = 1;
	c.anchor = c.NORTHWEST;
	bag.setConstraints(statlab, c);
	add(statlab);

	pixpos = new Label("(0,0)      ", Label.LEFT);
	c.gridx = 4;
	c.gridy = 0;
	bag.setConstraints(pixpos, c);
	add(pixpos);

	// Coordinates Labels
	xcoordlab = new Label("XCoord:   ", Label.LEFT);
	c.gridx = 0;
	c.gridy = 3;
	c.gridwidth = 3;
	bag.setConstraints(xcoordlab, c);
	add(xcoordlab);

	ycoordlab = new Label("YCoord:   ", Label.LEFT);
	c.gridx = 0;
	c.gridy = 4;
	c.gridwidth = 3;
	bag.setConstraints(ycoordlab, c);
	add(ycoordlab);

	xcoordval = new Label("              ", Label.LEFT);
	c.gridx = 3;
	c.gridy = 3;
	c.gridwidth = 5;
	bag.setConstraints(xcoordval, c);
	add(xcoordval);

	ycoordval = new Label("              ", Label.LEFT);
	c.gridx = 3;
	c.gridy = 4;
	c.gridwidth = 5;
	bag.setConstraints(ycoordval, c);
	add(ycoordval);

	// Box Button and Label
	drawbox = new Checkbox("Box: ");
	c.gridx = 0;
	c.gridy = 6;
	c.gridwidth = 2;
	bag.setConstraints(drawbox, c);
	add(drawbox);

	boxstart = new Label("(0,0)       ", Label.LEFT);
	c.gridx = 2;
	c.gridy = 6;
	c.gridwidth = 3;
	bag.setConstraints(boxstart, c);
	add(boxstart);

	boxend = new Label("(0,0)       ", Label.LEFT);
	c.gridx = 5;
	c.gridy = 6;
	bag.setConstraints(boxend, c);
	add(boxend);
    }

    public synchronized void setPixelPos(Object x, Object y) {
	String use = new String("(" + x + "," + y + ")");
	if (! use.equals(pixpos.getText())) {
	    pixpos.setText(use);
//	    pixpos.repaint();
	}
    }

    public synchronized void setPixelPos(int x, int y) {
	String use = new String("(" + x + "," + y + ")");
	if (! use.equals(pixpos.getText())) {
	    pixpos.setText(use);
//	    pixpos.repaint();
	}	    
    }

    public synchronized void setCoordVal(Object x, Object y) {
	String use = new String(x.toString());
	if (! use.equals(xcoordval.getText())) {
	    xcoordval.setText(use);
//	    xcoordval.repaint();
	}
	use = new String(y.toString());
	if (! use.equals(ycoordval.getText())) {
	    ycoordval.setText(use);
//	    ycoordval.repaint();
	}
    }

    public synchronized void setXCoordLabel(Object v) {
	String use = new String(v.toString());
	if (! use.equals(xcoordlab.getText())) {
	    xcoordlab.setText(use);
//	    xcoordlab.repaint();
	}
    }

    public synchronized void setYCoordLabel(Object v) {
	String use = new String(v.toString());
	if (! use.equals(ycoordlab.getText())) {
	    ycoordlab.setText(use);
//	    ycoordlab.repaint();
	}
    }

    public synchronized void setBoxStart(Object x, Object y) {
	if (drawbox.getState()) {
	    String use = new String("(" + x + "," + y + ")");
	    if (! use.equals(boxstart.getText())) {
		boxstart.setText(use);
//		boxstart.repaint();
	    }
	}
    }

    public synchronized void setBoxStart(int x, int y) {
	if (drawbox.getState()) {
	    String use = new String("(" + x + "," + y + ")");
	    if (! use.equals(boxstart.getText())) {
		boxstart.setText(use);
//		boxstart.repaint();
	    }
	}
    }

    public synchronized void setBoxEnd(Object x, Object y) {
	if (drawbox.getState()) {
	    String use = new String("(" + x + "," + y + ")");
	    if (! use.equals(boxend.getText())) {
		boxend.setText(use);
//		boxend.repaint();
	    }
	}
    }

    public synchronized void setBoxEnd(int x, int y) {
	if (drawbox.getState()) {
	    String use = new String("(" + x + "," + y + ")");
	    if (! use.equals(boxend.getText())) {
		boxend.setText(use);
//		boxend.repaint();
	    }
	}
    }

    public synchronized boolean getDrawBoxState() { return drawbox.getState(); }

    public synchronized void setDrawBoxState(boolean b) { drawbox.setState(b); }

    public boolean action(Event evt, Object o) {
	boolean handled = super.action(evt, o);
	if ((evt.id != Event.MOUSE_UP || evt.id != Event.MOUSE_UP) &&
	    evt.target == drawbox) {

	    if (parent != null) parent.setDrawBox(drawbox.getState());
	    return true;
	}
	else {
	    return handled;
	}
    }

    public Object clone() {
	PositionPanel out = new PositionPanel(parent);
	out.pixpos.setText(pixpos.getText());
	out.xcoordlab.setText(xcoordlab.getText());
	out.xcoordval.setText(xcoordval.getText());
	out.ycoordlab.setText(ycoordlab.getText());
	out.ycoordval.setText(ycoordval.getText());
	out.boxstart.setText(boxstart.getText());
	out.boxend.setText(boxend.getText());
	out.drawbox.setState(drawbox.getState());

	return out;
    }
}

