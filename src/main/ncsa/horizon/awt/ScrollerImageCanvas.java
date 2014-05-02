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

package ncsa.horizon.awt;
/*
  ScrollingPanel.java - implement panel with scrolling canvas
  

   Modification history:
   17-Jul-1996 plutchak     first version; I modified code by Xinjian Lu
                            (xlu@ncsa.uiuc.edu), changing his Frame to a
                            Panel and adding/changing a few methods

  05-Aug-1996 xlu          Rename "ImageCanvas" into  "ScrollerImageCanvas".
                               Add new constructor for ScrollerImageCanvas.
                               (paint() changed  back)
                               Allow to use "new ScrollingPanel(null)"

*/
//import horizon;

import  java.awt.*;
import  java.awt.image.ImageObserver;

/**
* Handle scrolling image display.
*/
public class ScrollerImageCanvas extends Canvas {

  /** the currently displayed image */
  protected Image                 currentImage;

  /** image size */
  protected int imageWidth  = 0;
  protected int imageHeight = 0;

  /** variables for double buffering  */
  protected Image         offScreenImage = null;
  protected Graphics      offGraphics;

  /** display string */
  protected String infoStr = "No image";

  /** image offset on panel */
  protected int tx=0;
  protected int ty=0;       // offset translate

  /** scrollbar of the image canvas */
  protected Scrollbar      hScroll,      vScroll;

  /** new constructor */
  public ScrollerImageCanvas() {
    super();
  }

  /** Constructor with image
   * @param image the image
   */
  public ScrollerImageCanvas(Image image) {
    this();
    // set image
    setImage(image);	
    // set the default image size
    setImageSize(imageWidth, imageHeight);
    // resize the canvas
    resize(imageWidth, imageHeight);
  }

  /** Constructor with image
   * @param image the image
   * @param v the vertical scrollbar
   * @param h the horizontal scrollbar
   */
  public ScrollerImageCanvas(Image image, Scrollbar v, Scrollbar h) {
    this(image);
    // append the scrollbar
    setScrollbars(h,v);
  }

  /** set the current displayed image
   * @param img the current image
   */
  public void setImage ( Image img ) {
    this.currentImage = img; 
    // recalculate bounds, etc.
    setHScrollValue();
    setVScrollValue();
    repaint();
  }

  /** set the current image size
   * @param width  the width of the image
   * @param height the height of the image
   */
  public void setImageSize ( int width, int height) {
    imageWidth = width;
    imageHeight = height;
    if (currentImage != null) {
      setHScrollValue();
      setVScrollValue();
      repaint();
    }
  }

  /** set the current image width
   * @param width  the width of the image
   */
  public void setImageWidth ( int width ) {
    imageWidth = width;
    if (currentImage != null) setHScrollValue();
  }

  /** set the current image height
   * @param height the height of the image
   */
  public void setImageHeight ( int height) {
    imageHeight = height;
    if (currentImage != null) setVScrollValue();
  }

  /** Reshapes the Component to the specified bounding box. */
  public synchronized void reshape ( int x, int y, int w, int h ) {
    super.reshape( x, y, w, h );
    //-- System.out.print( "(Canvas.reshape) x y w h =" );
    //-- System.out.println( " "+x+" "+y+" "+w+" "+h+" " );
    // resize scrollbars
    setHScrollValue();
    setVScrollValue();
  }

  /**
   * Set scrollbars for the canvas
   * @param h the horizontal scrollbar
   * @param v the vertical scrollbar
   */
  public void setScrollbars ( Scrollbar h, Scrollbar v ) {
    // set scrollbar values
    this.hScroll = h;
    this.vScroll = v;
  }

  /** change the horizontal scrollbar value to the sn appropriate width. */
  void setHScrollValue() {
    // get current canvas size
    int canvasWidth = size().width;
    // canvas is valid?
    if (canvasWidth <= 0) {
      //-- System.out.println( "Canvas has no width; can't resize scrollbar" );
      return;
    }
    if (currentImage == null) {
      //-- System.out.println( "Canvas has no image; can't resize scrollbar");
      return;
    }
    //Shift everything to the right if we're displaying empty space
    //on the right side.
    if ((tx + canvasWidth) > imageWidth) {
      int newtx = imageWidth - canvasWidth;
      if (newtx < 0) {
	newtx = 0;
      }
      tx = newtx;
    }
    //-- System.out.print( "(Canvas.setHScroll) image canvas tx:" );
    //-- System.out.println( " "+imageWidth+" "+ canvasWidth+" "+tx );
    hScroll.setValues(//draw the part of the image that starts at this x:
		      tx,
		      //amount to scroll for a "page":
		      //(int)(canvasWidth * 0.9),
		      canvasWidth,
		      //minimum image x to specify:
		      0,
		      //maximum image x to specify:
		      imageWidth - canvasWidth );
    //"visible" arg to setValues() has no effect after scrollbar is visible.
    //hScroll.setPageIncrement((int)(canvasWidth * 0.9));
    hScroll.setPageIncrement( canvasWidth );
    if ((imageWidth - canvasWidth) <= 0)
      //hScroll.hide();
      hScroll.disable();
    else
      hScroll.enable();
    //hScroll.show();
    return;
  }

  /** Adjust the Vertical Scrollbar value by the specified width. */
  void setVScrollValue() {
    // get current canvas size
    int canvasHeight = size().height;
    // canvas is valid?
    if (canvasHeight <= 0) {
      //-- System.out.println("Canvas has no height; can't resize scrollbar");
      return;
    }
    if (currentImage == null) {
      //-- System.out.println( "Canvas has no image; can't resize scrollbar");
      return;
    }
    //Shift everything to the right if we're displaying empty space
    //on the right side.
    if ((ty + canvasHeight) > imageHeight) {
      int newty = imageHeight - canvasHeight;
      if (newty < 0) {
	newty = 0;
      }
      ty = newty;
    }
    //-- System.out.print( "(Canvas.setVScroll) image canvas ty:" );
    //-- System.out.println( " "+imageHeight+" "+ canvasHeight+" "+ty );
    vScroll.setValues(//draw the part of the image that starts at this y:
		      ty,
		      //amount to scroll for a "page":
		      //(int)(canvasHeight * 0.9),
		      canvasHeight,
		      //minimum image y to specify:
		      0,
		      //maximum image y to specify:
		      imageHeight - canvasHeight);
    //"visible" arg to setValues() has no effect after scrollbar is visible.
    //vScroll.setPageIncrement((int)(canvasHeight * 0.9));
    vScroll.setPageIncrement( canvasHeight );
    if ((imageHeight - canvasHeight) <= 0)
      //vScroll.hide();
      vScroll.disable();
    else
      //vScroll.show();
      vScroll.enable();
    return;
  }

  /** return the current image in the canvas */
  public Image getImage() {
    return currentImage;
  }

  /**
   * Get the dimensions of an image.
   * @return the image's dimensions.
   */
  Dimension getImageDimensions(Image im) {
    return new Dimension(im.getWidth(null), im.getHeight(null));
  }

  /** return the current image width  */
  public int getImageWidth() {
    return imageWidth;
  }

  /** return the current image height  */
  public int getImageHeight() {
    return imageHeight;
  }

  /**
   * Returns the minimum size of this component.
   * @see #preferredSize
   * @see LayoutManager
   */
  public Dimension minimumSize() {
    return new Dimension(20,20);
  }

  /**
   * Paints the component.
   * @param g the specified Graphics window
   * @see java.awt.Component#paint
   */
  public void paint(Graphics g) {  
    // get current Canvas size
    int w = size().width;
    int h = size().height;
    // get the approciate position to display the image
    int startx=0, starty=0;
    // set the specified translated parameters 
    // and the subcomponents will be relative to this origin.
    g.translate(-tx, -ty);
    // If the image size is greater than the Canvas 
    int width;
    int height;
    width = getImageWidth();
    height= getImageHeight();
    if (width>w)
      startx = 0;
    else
      startx = (w-width)/2;
    if (height>h)
      starty = 0;
    else
      starty = (h-height)/2;
    //System.out.println("startx: " + startx);
    if (currentImage == null) {
      super.paint(g);
      // draw a box around the empty canvas
      g.setColor( Color.cyan );
      g.drawRect( 0, 0, size().width, size().height );
      // info
      g.setColor( Color.black );
      FontMetrics fm = g.getFontMetrics();
      int x = (w - fm.stringWidth(infoStr))/2;
      int y = h/2;
      g.drawString( "No image", x, y );
    }
    else {
      /*
	// draw the frame
	g.setColor(Color.red);
	g.drawRect(0,0,size().width, size().height);
	*/
	
      // paint image
      //	g.drawImage(currentImage, startx, starty,width, height, this);
      g.drawImage(currentImage,startx,starty, this);
    }
    // set the specified translated parameters 
    // and the subcomponents will be relative to this origin.
    g.translate(tx, ty);
}

  public void setTx(int tx) {
    this.tx = tx;
  }

  public void setTy(int ty) {
    this.ty = ty;
  }

  /**
   * Updates the component. This method is called in
   * response to a call to repaint. You can assume that
   * the background is not cleared.
   * @param g the specified Graphics window
   * @see java.awt.Component#update
   */
  public void update ( Graphics g ) {
    Dimension d = size();
    if (offScreenImage == null) {
      // offScreenImage not created; create offscreen image of proper size
      if ((d.width*d.height) < 800 * 500)
	offScreenImage = createImage(800, 500);
      else
	offScreenImage = createImage(d.width, d.height);
      // get the off-screen graphics context
      offGraphics    = offScreenImage.getGraphics();
      // set the font for offGraphics
      offGraphics.setFont(getFont());
    }
    // paint the background on the off-screen graphics context
    offGraphics.setColor( getBackground() );
    offGraphics.fillRect( 1, 1, d.width-2, d.height-2 );
    //offGraphics.clearRect( 0, 0, d.width, d.height );
    offGraphics.setColor( getForeground() );
    // draw the current frame to the offscreen image
    paint( offGraphics );
    // draw the (offscreen) image to the current (on-screen) graphics context
    g.drawImage( offScreenImage, 0, 0, null );
  }
}

