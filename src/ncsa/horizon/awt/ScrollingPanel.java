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
* A panel containing a scrolling image canvas and scrollbars.
*/
public class ScrollingPanel extends Panel {
  /** the canvas upon which the image will be painted. */
  ScrollerImageCanvas       imageCanvas = null;
  
  //Viewable          viewable = null;
  Image             view;
  Dimension         preferred = null;
   /** Scrollbar of the image canvas */
   Scrollbar         hScroll, vScroll;
  
  /** Create a separate panel based on the image canvas.
   *  This panel will contain the image canvas and scrollbars
   */
  public ScrollingPanel ( Image image ) {  
    super();
    // image canvas
    imageCanvas = new ScrollerImageCanvas();
    setImage( image );
    // set layout manager
    setLayout( new BorderLayout() );
    // create the scrollbar
    vScroll = new Scrollbar();
    hScroll = new Scrollbar( Scrollbar.HORIZONTAL );
    this.add("Center", imageCanvas );
    this.add("East",   vScroll);
    this.add("South",  hScroll);
    // set scrollbars
    imageCanvas.setScrollbars( hScroll, vScroll );
  }

  /** Create a separate panel based on the image canvas.
   *  This panel will contain the image canvas and scrollbars
   */
  public ScrollingPanel () {  
    super();
    // image canvas
    imageCanvas = new ScrollerImageCanvas();
    //setImage( null );
    // set layout manager
    setLayout( new BorderLayout() );
    // create the scrollbar
    vScroll = new Scrollbar();
    hScroll = new Scrollbar( Scrollbar.HORIZONTAL );
    this.add("Center", imageCanvas );
    this.add("East",   vScroll);
    this.add("South",  hScroll);
    // set scrollbars
    imageCanvas.setScrollbars( hScroll, vScroll );
  }

  /** Create a separate panel based on the image canvas.
   *  This panel will contain the image canvas and scrollbars
   */
  public ScrollingPanel ( int width, int height ) {
    super();
    preferred = new Dimension( width, height );
    // image canvas
    imageCanvas = new ScrollerImageCanvas();
    // set layout manager
    setLayout( new BorderLayout() );
    // create the scrollbar
    vScroll = new Scrollbar();
    hScroll = new Scrollbar( Scrollbar.HORIZONTAL );
    this.add("Center", imageCanvas );
    this.add("East",   vScroll);
    this.add("South",  hScroll);
    // set scrollbars
    imageCanvas.setScrollbars( hScroll, vScroll );
    this.resize( width, height );
  }

  /**
   * Set the image to be displayed on the panel.
   * <p>
   * @param java.awt.Image	The image object to display
   */
  public void setImage ( Image image ) {
    if (image != null) {
      // set image specifications, or load the image and defer specs
      if (prepareImage( image, this )) {
	int w = image.getWidth(this);
	int h = image.getHeight(this);
	imageCanvas.setImageSize( w, h );
	imageCanvas.setImage( image );
	//-- System.out.println( "(Panel.setImage) w x h = "+w+" x "+h );
      }
      else {
	//-- System.out.println( "(Panel.setImage) deferring image acquisition" );
	image.getWidth( this );
	image.getHeight( this );
      }
      view = image;
    }
  }

  public boolean imageUpdate ( Image img, int infoflags, int x, int y,
			       int width, int height ) {
    //System.out.println( "(Panel.imageUpdate) flag = "+infoflags );
    // process image event notification
    if ((infoflags & ImageObserver.ERROR) != 0) {
      view = null;
      System.out.println( "(Panel.imageUpdate) image acquisition failed" );
      repaint();
      return false;
    }
    if ((infoflags & ImageObserver.ABORT) != 0) {
      view = null;   
      System.out.println( "(Panel.imageUpdate) image acquisition aborted" );
      repaint();
      return false;
    }
    if ((infoflags & ImageObserver.ALLBITS) != 0) {
      imageCanvas.setImage( img );
      view = img;   
      //-- System.out.println( "(Panel.imageUpdate) image acquired" );
      repaint();
      return true;
    }
    if ((infoflags & ImageObserver.WIDTH) != 0) {
      //-- System.out.println( "(Panel.imageUpdate) width= "+width );
      imageCanvas.setImageWidth( width );
    }
    if ((infoflags & ImageObserver.HEIGHT) != 0) {
      //-- System.out.println( "(Panel.imageUpdate) height= "+height );
      imageCanvas.setImageHeight( height );
    }
    return true;
  }

  /**
   * Handles the event. Returns true if the event is handled and
   * should not be passed to the parent of this component. The default
   * event handler calls some helper methods to make life easier
   * on the programmer.
   * @param evt the event
   * @see java.awt.Component#handleEvent
   */
   public boolean handleEvent ( Event evt ) {
     //  detect the scrollbar event
     switch (evt.id) {
     case Event.PGUP:
     case Event.SCROLL_PAGE_UP:
     case Event.UP:
     case Event.SCROLL_LINE_UP:
     case Event.SCROLL_ABSOLUTE:
     case Event.DOWN:
     case Event.SCROLL_LINE_DOWN:
     case Event.PGDN:
     case Event.SCROLL_PAGE_DOWN:
       // detect the vertical scrollbar
       if (evt.target ==  vScroll) {
	 // get translated value
	 imageCanvas.ty = vScroll.getValue();
	 // repaint the graphics
	 imageCanvas.repaint();
       }
       // detect the horizontal scrollbar
       if (evt.target ==  hScroll) {
	 // get translated value
	 imageCanvas.tx = hScroll.getValue();
	 // repaint the graphics
	 imageCanvas.repaint();
       }
     } // switch(evt.id)
     return super.handleEvent(evt);
   }

  /**
   * Called if an action occurs in the Component
   * @param evt the event
   * @param arg the action that's occuring
   * @see java.awt.Component#action
   */
  public boolean action ( Event evt, Object arg ) {
    // return value
    return super.action(evt, arg);
  }

  /**
   * All sizing methods are overridden as debugging aids.
   */
  public void resize ( int width, int height ) {
    //-- System.out.print( "(Panel.resize) w h =" );
    //-- System.out.println( " "+width+" "+height );
    super.resize( width, height );
  }
  
  public void resize ( Dimension d ) {
    //-- System.out.print( "(Panel.resize[d]) w h =" );
    //-- System.out.println( " "+d.width+" "+d.height );
    super.resize( d );
  }

  public void reshape ( int x, int y, int width, int height ) {
    //-- System.out.print( "(Panel.reshape) x y w h =" );
    //-- System.out.println( " "+x+" "+y+" "+width+" "+height );
    super.reshape( x, y, width, height );
  }

  public Dimension minimumSize ( ) {
    Dimension   d = super.preferredSize();
    //-- System.out.print( "(Panel.minimumSize) w h =" );
    //-- System.out.println( " "+d.width+" "+d.height );
    return d;
  }

  public Dimension preferredSize ( ) {
    Dimension   d = super.preferredSize();
    //-- System.out.print( "(Panel.preferredSize[d]) w h =" );
    //-- System.out.println( " "+d.width+" "+d.height );
    if (preferred != null) {
      //-- System.out.print( "(Panel.preferredSize) w h =" );
      //-- System.out.println( " "+preferred.width+" "+preferred.height );
      return preferred;
    }
    return d;
  }
}
