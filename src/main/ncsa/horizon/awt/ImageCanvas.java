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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  96dec14  rlp  Original version
 *  96dec17  rlp  added specific calls to resize() in constructor when 
 *                  size is given in viewSize(), added call to 
 *                  preferredSize() if size() returns 0 (will probabaly 
 *                  need to think more about this.)
 *  96dec24  rlp  fixed bu in tryPaint() for FLUSH images
 *  97jan16  rlp  changed use of MediaTracker for better performance
 *  97aug08  rlp  performance tweaking
 *  97jul11  wx   made changes so that double-buffer painting is always
 *                  used when calls to repaint are made.  Specific changes
 *                  include overriding update to call offpaint, making 
 *                  tryPaint() protected, and replacing second 
 *                  paint(getGraphics()) call in displayImage() with repaint().
 *  97dec04  rlp  general cleanup of code and documentation; no major changes
 *  98jan23  rlp  added use of IC_LockReservation & isLoading() to guard 
 *                  against multiple display requests by anxious users from 
 *                  clobbering each other.  Added some use of "synchronized".  
 *                  Use of tryPaint() and/or MediaTracker may now be obsolete.
 *                  Added clear() method to reset this display to a null state.
 *                  Fixed update bug associated with a resized display.
 *  98feb06  rlp  removed some "synchronized" tags from certain methods as 
 *                  there can sometimes be a deadlock
 */
package ncsa.horizon.awt;

import java.awt.*;
import java.awt.image.*;

/** 
 * A Canvas object for displaying images.  Features include
 * <ul>
 *    <li> double-buffer painting
 *    <li> mode describing how to squeez image into Canvas area
 * </ul>
 *
 * @version Alpha $Id: ImageCanvas.java,v 0.10 1998/02/06 19:52:53 rplante Exp $
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */
public class ImageCanvas extends Canvas implements ImageObserver, Cloneable {

    // constants
    /** * Fit the image to the Canvas size (default) */
    public final static int SIZE_IMAGE_FIT      = 1;
    /** * Trim the image to the Canvas size (centered) */
    public final static int SIZE_IMAGE_CENTER   = 2;
    /** * Trim the image to the Canvas size (flush top/left) */
    public final static int SIZE_IMAGE_TRUNCATE = 3;
    /** * Scale the image to the Canvas size (centered) */
    public final static int SIZE_IMAGE_SCALE    = 4;
    /** * Scale the image to the Canvas size (centered) */
    public final static int SIZE_IMAGE_FLUSH    = 5;

    protected Image view = null;
    protected int mode = SIZE_IMAGE_CENTER;
    protected Image offscreen=null;
    protected MediaTracker tracker=null;
    protected static Font msgfont = new Font("Helvetica", Font.PLAIN, 12);
    protected boolean doclear=false;

    protected Dimension size = null;
    private IC_LockReservation loading = new IC_LockReservation(false);
    int ndi=0;

    public ImageCanvas() {
	super();
	tracker=new MediaTracker(this);
    }

    public ImageCanvas(int w, int h) {
	super();
	size = new Dimension(w, h);
	resize(w, h);
	tracker=new MediaTracker(this);
    }

    public ImageCanvas(Dimension sz) {
	super();
	size = new Dimension(sz.width, sz.height);
	resize(sz.width, sz.height);
	tracker=new MediaTracker(this);
    }

    public Dimension preferredSize() {
	if (size == null) 
	    return super.preferredSize();
	else 
	    return new Dimension(size.width, size.height);
    }
    public void setPreferredSize(int w, int h) { 
	size = new Dimension(w, h); 
	offscreen = null;
    }
    public void setPreferredSize(Dimension sz) { 
	size = new Dimension(sz.width, sz.height); 
	offscreen = null;
    }

    public void displayImage(Image im) {
	if (loading.getValue()) return;

	synchronized(loading) {
	    loading.setValue(true);
	    if (view != null) doclear = true;
	    view = im;
	    tracker.addImage(im, 0);
	    setBackground(Color.white);

	    paint(getGraphics());

	    // wait until the image is finished loading or encournters 
	    // an error.
	    if (! tracker.checkID(0,true)) {
		try { tracker.waitForID(0); }
		catch (InterruptedException e) { }
	    }

	    int stat = tracker.statusID(0, false);
	    if ((stat&tracker.COMPLETE) != 0) {
		repaint();
	    }
	    if ((stat&tracker.LOADING) != 0) {
		System.out.println("Something is still loading.");
	    }
	    if ((stat&(tracker.ERRORED|tracker.ABORTED)) != 0) {
		System.out.println("Error loading image");
		loading.setValue(false);
	    }
	}
	    
    }

    /**
     * clear the canvas.  This method also releases references to images
     * being displayed
     */
    public synchronized void clear() {
	view = null;
	offscreen = null;
	doclear = false;
	loading.setValue(false);
	Graphics g = getGraphics();
	if (g != null) {
	    setBackground(Color.black);
	    g.clearRect(0,0, size().width, size().height);
	}
	repaint();
    }

    /**
     * paint the image with a double-buffering technique.  This method first
     * creates an offscreen Image, paints the real image to it (via 
     * tryPaint()), and then paints that copy to the canvas.  
     */
    protected void offpaint(Graphics g) {
	Graphics og;
	Dimension mysz = size();

	if (offscreen == null) 
	    offscreen = createImage(mysz.width, mysz.height);
	og = offscreen.getGraphics();
	og.setFont((g == null) ? msgfont : g.getFont());
	if (tryPaint(og)) {
	    if (! g.drawImage(offscreen, 0, 0, this)) 
		paintMessage(g, "Image is loading");
	}
    }

    /**
     * this calls tryPaint(Graphics)
     */
    public void paint(Graphics g) {
	tryPaint(g);
    }

    /**
     * print a message on the canvas.  Message is placed over a white 
     * rectangle to that it can be seen.
     */
    protected void paintMessage(Graphics g, String msg) {
	Font f = g.getFont();
	if (f == null) throw new InternalError("No font. ");
	FontMetrics fm = getFontMetrics(f);
	Dimension sz = new Dimension(fm.stringWidth(msg), fm.getHeight());
	sz.height = (int) (sz.height * 1.2);
	sz.width += 2*fm.charWidth('M');

	// draw a white rectangle to write on
	Dimension dsz = size();
	int x = dsz.width/2 - sz.width/2;
	int y = dsz.height/2 - sz.height/2;
	g.clearRect(x, y, sz.width, sz.height);

	// print the message
	g.drawChars(msg.toCharArray(), 0, msg.length(), 
		    (int) (x + 0.5*fm.charWidth('M')), 
		    (int) (y + sz.height - 0.2*sz.height));
    }

    public boolean imageUpdate(Image img, int flags, int x, int y, 
			       int width, int height) 
    {
	if ((flags&(ERROR|ABORT)) != 0) {
	    System.out.println("Image experiencing problems: (flags = " +
		               flags + ")");
	}

	boolean drawable=false;
// 	if (img != view && img != offscreen) {
// 	    System.out.println("img = " + img + " != " + view + ", " + 
// 			       offscreen);
// 	    System.out.println("stat: " + flags);
// 	}

	drawable = ((flags&ALLBITS) != 0);

	if (drawable) { 
	    synchronized (this) {
		if (offscreen != null && doclear) {
		    offscreen.getGraphics().clearRect(0,0, 
						      size().width, 
						      size().height);
		    doclear = false;
		}
		offpaint(getGraphics());
	    }
	}
	return (!drawable);
    }

    /**
     * like paint() except that it returns a boolean indicating whether 
     * it was successful in painting the image onto the canvas
     */
    protected boolean tryPaint(Graphics g) {
	if (view != null) {
	    Dimension imsz = size();

	    switch (mode) {
	    case SIZE_IMAGE_FIT:    // resize the image to the canvas size
		if (prepareImage(view, imsz.width, imsz.height, this)) {
		    if (! g.drawImage(view, 0, 0, 
				      imsz.width, imsz.height, this)) {
			paintMessage(g, "Image is loading");
			return false;
		    }
		}
		else {
		    paintMessage(g, "Image is loading");
//		    paintMessage(g, "patience...");
		    return false;
		}
		break;

	    case SIZE_IMAGE_CENTER: { // center the image in the panel
		int iht = view.getHeight(this);
		int iwd = view.getWidth(this);
		int  xloc = (imsz.width - iwd)/2;
		int  yloc = (imsz.height - iht)/2;
		if (iht > 0 && iwd > 0 && prepareImage(view, this)) {
		    if (! g.drawImage(view, xloc, yloc, this)) {
			paintMessage(g, "Image is loading");
			return false;
		    }
		}
		else {
		    paintMessage(g, "Image is loading");
		    return false;
		}
		break;
	    }

	    case SIZE_IMAGE_SCALE: { 
		int xloc, yloc, xsz, ysz;
		int iht = view.getHeight(this);
		int iwd = view.getWidth(this);
		double iaspect = 1.0 * iht / iwd;
		double daspect = 1.0 * imsz.height / imsz.width;
		if (iaspect < daspect) {
		    xsz = imsz.width;
		    ysz = (int) (xsz * iaspect);
		    xloc = 0;
		    yloc = (imsz.height - ysz) / 2;
		} 
		else {
		    ysz = imsz.height;
		    xsz = (int) (ysz / iaspect);
		    xloc = (imsz.width - xsz) / 2;
		    yloc = 0;
		}

		if (iht > 0 && iwd > 0 && prepareImage(view, xsz, ysz, this)) {
		    if (! g.drawImage( view, xloc, yloc, xsz, ysz, this )) {
			paintMessage(g, "Image is loading");
			return false;
		    }
		}
		else {
		    paintMessage(g, "Image is loading");
		    return false;
		}
		break;
	    }

	    case SIZE_IMAGE_FLUSH: { 
		int xsz, ysz;
		int iht = view.getHeight(this);
		int iwd = view.getWidth(this);
		double iaspect = 1.0 * iht / iwd;
		double daspect = 1.0 * imsz.height / imsz.width;
		if (iaspect < daspect) {
		    xsz = imsz.width;
		    ysz = (int) (xsz * iaspect);
		} 
		else {
		    ysz = imsz.height;
		    xsz = (int) (ysz / iaspect);
		}

		if (iht > 0 && iwd > 0 && 
		    prepareImage(view, xsz, ysz, this)) {

		    if (! g.drawImage( view, 0, 0, xsz, ysz, this )) {
			paintMessage(g, "Image is loading");
			return false;
		    }
		}
		else {
		    paintMessage(g, "Image is loading");
		    return false;
		}
		break;
	    }

	    case SIZE_IMAGE_TRUNCATE:      // truncate right/bottom 
	    default: 
		if (prepareImage(view, this)) {
		    if (! g.drawImage( view, 0, 0, this )) {
			paintMessage(g, "Image is loading");
			return false;
		    }
		}
		else {
		    paintMessage(g, "Image is loading");
		    return false;
		}
		break;
	    }
	    loading.setValue(false);
	}

	return true;
    }

    /**
     * calls offpaint()
     */
    public void update(Graphics g) {
	offpaint(g);
    }

    /**
     * determine the dimesions necessary to fit an image of width wd and 
     * height ht into the display canvas (while preserving the aspect ratio).
     * An input dimension < 0 means use the corresponding dimension of the 
     * current image being displayed.
     */
    public Dimension viewSize(int wd, int ht) {
	Dimension mysz = size();
	Dimension imsz = new Dimension(mysz);

	if (mysz.width == 0 && mysz.height == 0) mysz = preferredSize();

	if (wd < 0 || ht < 0) {
	    if (view == null) return mysz;
	    if (wd < 0) wd = view.getWidth(this);
	    if (ht < 0) ht = view.getHeight(this);
	}
	if (wd < 0 || ht < 0) return size();
	imsz.width = wd;
	imsz.height = ht;

	double iaspect = (1.0*wd)/ht,
	       daspect = (1.0*mysz.width)/mysz.height;
	if (iaspect > daspect) {
	    imsz.width = mysz.width;
	    imsz.height = (int) (imsz.width/iaspect);
	}
	else {
	    imsz.height = mysz.height;
	    imsz.width = (int) (imsz.height*iaspect);
	} 

	return imsz;
    }

    /**
     * determine the dimesions necessary to fit the current image into
     * the display canvas (while preserving the aspect ratio.)
     */
    public Dimension viewSize() { return viewSize(-1, -1); }

    /**
     * indicate whether there is an image being loaded at this time
     */
    public boolean isLoading() { return loading.getValue(); }

    /**
     * make a copy of this canvas in an efficient manner
     */
    public synchronized Object clone() {
	ImageCanvas out = new ImageCanvas();
	out.size = new Dimension(size);
	out.loading.setValue(loading.getValue());
//	out.width = width;
//	out.height = height;
	out.view = view;
	out.tracker.addImage(out.view, 0);

	return out;
    }

    /**
     * Set the display mode for sizing or trimming the viewable image.
     * @param _mode The mode to use.  Valid modes are SIZE_IMAGE_FIT,
     *    SIZE_IMAGE_CENTER, and SIZE_IMAGE_TRUNCATE.
     */
    public void setMode ( int _mode ) {
	switch (_mode) {
	    case SIZE_IMAGE_FIT:
            case SIZE_IMAGE_SCALE:
            case SIZE_IMAGE_FLUSH:
		doclear = true;
	    case SIZE_IMAGE_CENTER:
            case SIZE_IMAGE_TRUNCATE: {
		mode = _mode;
		repaint();
	    }
	}
    }
}

class IC_LockReservation {    
    private boolean b = true;

    public IC_LockReservation(boolean val) { b = val; }
    public IC_LockReservation() { super(); }

    public boolean getValue() {  return b; }

    public synchronized void setValue(boolean val) {  b = val; }
}
