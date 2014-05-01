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
/*
* ROI.java - Implement an interactive Region Of Interest.
*
* Modification history:
*    18-Oct-1996 plutchak     Initial version.
*    19-Nov-1997 Wei Xie      put a static method getTureRectangle()
*/
package ncsa.horizon.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Rectangle;

/**
* A user-selected  Region of Interest for a Component.
* The ROI can be created, moved, resized, etc. by calling the appropriate
* methods.  It is up to the calling application to implement the user
* action interface to the routines.  E.g., create the ROI on mouse button
* down, extend the size on mouse drag, and return a completed ROI on
* mouse up.<p>
* The ROI neither generates nor receives events.  It can be thought of
* as a simple decorational widget than knows its location and state, can
* draw itself given a graphics context, and can be manipulated and queried
* through method calls.
*
* @version 0.1 alpha
* @author Horizon Team, University of Illinois at Urbana-Champaign
* @author <br>Joel Plutchak <plutchak@uiuc.edu>
*/
public class ROI extends Rectangle {
   // user-selectable attributes
   private int       thickness = 2;
   private int       style;                        // shape/appearance
   private Color     color = Color.red;            // color

   // controlled attributes
   private int       state = STATE_VISIBLE;        // visible/hidden, etc.
   private Component component = null;             // component to attach to
   private Point     grabLoc = null;               // cursor loc when grabbed
   private Rectangle startLoc = null;              // region when grabbed
   private int       direction = CENTER;           // area of region grabbed

   // state constants
   private static final int   STATE_CLEAR = 0x0000;
   private static final int   STATE_INITIALIZED = 0x0001;
   private static final int   STATE_ACTIVE = 0x0002;
   private static final int   STATE_VISIBLE = 0x0010;
   private static final int   STATE_RESIZABLE = 0x0020;

   // style constants
   private static final int   STYLE_NORMAL = 0x0001;
   private static final int   STYLE_TERMINUS = 0x0001;

   /**
   * // Values returned by the on() method.<br>
   */
   /** * The inside area of the bounding rectangle.  */
   public final static int CENTER = 10;
   /** * The top edge of the bounding rectangle.  */
   public final static int NORTH = 11;
   /** * The top right vertex of the bounding rectangle.  */
   public final static int NORTHEAST = 12;
   /** * The right edge of the bounding rectangle.  */
   public final static int EAST = 13;
   /** * The bottom right vertex of the bounding rectangle.  */
   public final static int SOUTHEAST = 14;
   /** * The bottom edge of the bounding rectangle.  */
   public final static int SOUTH = 15;
   /** * The bottom left vertex of the bounding rectangle.  */
   public final static int SOUTHWEST = 16;
   /** * The left edge of the bounding rectangle.  */
   public final static int WEST = 17;
   /** * The top left vertex of the bounding rectangle.  */
   public final static int NORTHWEST = 18;

   /**
   * Create a ROI in the default style and size.
   */
   public ROI ( ) {
      super();
      style = 0x0001;        // use the first defined style as default
   }

   /**
   * Create a ROI beginning at the specified point.
   * @param p The point defining a vertex of the region.
   * @param _component	The component to attach the ROI to.
   */
   public ROI ( Point p, Component _component ) {
      super( p );
      state |= (STATE_INITIALIZED | STATE_RESIZABLE );
      style = 0x0001;        // use the first defined style as default
      component = _component;
   }

   /**
   * Create a ROI beginning at the specified point.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param _component	The component to attach the ROI to.
   */
   public ROI ( int _x, int _y, Component _component ) {
      super( _x, _y, 0, 0 );
      state |= STATE_INITIALIZED;
      style = 0x0001;        // use the first defined style as default
      component = _component;
   }

   /**
   * Create a ROI beginning with the specified vertex and dimensions.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param width The width of the region.
   * @param height The height of the region.
   * @param _component	The component to attach the ROI to.
   */
   public ROI ( int _x,int _y, int _width,int _height, Component _component ) {
      super( _x, _y, _width, _height );
      state |= STATE_ACTIVE;
      style = 0x0001;        // use the first defined style as default
      component = _component;
   }

   /**
   * Create a ROI for a given component.
   *
   * @param _component	The component to attach the ROI to.
   */
   public ROI ( Component _component ) {
      super();
      style = 0x0001;
      component = _component;
   }

   /**
   * Draw the ROI at its current location.  It is the responsibility of
   * the applet/application to draw the ROI at the appropriate times, e.g.,
   * inside the component's update() and/or paint() method.  This gives
   * maximum flexibility for double buffering, etc.
   * @param g The Graphics context to use for drawing.
   */
   public void draw ( Graphics g ) {

      //System.out.println( "(ROI.draw) "+x+", "+y+", "+width+", "+height );
      if (((state & (STATE_VISIBLE)) == 0) || (width == 0) || (height == 0)) {
         //System.out.println("(draw) "+component+", "+isVisible()+", "+state);
         return;
      }

      int _x = x;
      int _y = y;
      int _width = width;
      int _height = height;

      Color saveColor = g.getColor();
      g.setColor( color );

      // adjust for case where width or height is negative
      if (width < 0) {
         _width = -width;
         _x = x + width;
      }
      if (height < 0) {
         _height = -height;
         _y = y + height;
      }
      //System.out.println( "(draw) "+_x+", "+_y+", "+_width+", "+_height );
      g.drawRect( _x, _y, _width, _height ); 

    g.setColor( saveColor );
   }

   /**
   * Reset Rectangle components in order to preserve the "upper left" rule.
   */
   private void recalculate ( ) {
      if (width < 0) {
         x += width;
         width = -width;
      }

      if (height < 0) {
         y += height;
         height = -height;
      }
      //System.out.println( "(recalc) ("+x+", "+y+", "+width+", "+height+")" );
   }

   /**
   * Grab this ROI in preparation for dragging or resizing.
   */
   public void grab ( int _x, int _y ) {
      //System.out.println( "(grab) at "+_x+", "+_y );
      grabLoc = new Point( _x, _y );
      startLoc = new Rectangle( x, y, width, height );
   }

   /**
   * Drag this ROI to a given location.
   * @param _x		the x coordinate.
   * @param _y		the y coordinate.
   */
   public void drag ( int _x, int _y ) {
      // Need to erase the old ROI and draw the new one.  This is
      // a drag with coordinates relative to where the region was
      // and where the cursor was when grabbed.

      move( startLoc.x + _x - grabLoc.x, startLoc.y + _y - grabLoc.y );
   }

   /**
   * Drop this Region Of Interest.  Depending on region state, this can
   * mean either finish creating a new region, ending a region move, or
   * ending a resize operation.
   * @param _x		The x coordinate.
   * @param _y		The y coordinate.
   */
   public void drop ( int _x, int _y ) {
      //System.out.println( "(drop) at "+_x+", "+_y+" ("+width+", "+height+")");

      // ignore spurious drops
      if (!isGrabbed() && !isInitialized())
         return;

      // sanity check: if grabbed, should be either active or initialized
      if ((component == null) || (!isActive() && !isInitialized())) {
         grabLoc = null;
         return;
      }

      // handle drop if action was a move
      // if dropped off the component, set it to the grabbed location
      if ((_x < 0) || (_x > component.size().width) ||
          (_y < 0) || (_y > component.size().height)) {
         if (isInitialized()) {            // ended a new region offscreen
            width = 0;   height = 0;
            x = 0;       y = 0;
            state &= ((~STATE_INITIALIZED) & (~STATE_ACTIVE));
            grabLoc = null;

            redraw();
            return; 
         }
         // was being dragged/resized; reset to initial region
         x = startLoc.x;   y = startLoc.y;
         width = startLoc.width;   height = startLoc.height;
         //System.out.println("(drop) at "+_x+", "+_y+" ("+width+", "+height+")");
         redraw();

         return;
      }

      if (isGrabbed()) {
         if (isActive())
            drag( _x, _y );
         else
            grow( _x, _y );
      }

      // depending on how mouse up is implemented, may need to do a final drag()
      state = (state & (~STATE_INITIALIZED)) | STATE_ACTIVE;
      grabLoc = null;

      recalculate();
      return;
   }

   /**
   * Move this ROI to the coordinate (x,y) in the parent's coordinate
   * space.  This calls the repaint() method of the ROI's component,
   * which is ultimately responsible for calling the ROI's draw() method.
   * 
   * @param _x		the x coordinate
   * @param _y		the y coordinate
   */
   public void move ( int _x, int _y ) {
      int oldx = x;
      int oldy = y;

      x = _x;
      y = _y;

      if (component != null) {
         // just need to repaint the necessary areas
         component.repaint( oldx, oldy, width+1, height+1 );
         component.repaint( x, y, width+1, height+1 );
      }
   }

   /**
   * Cause a redisplay of the ROI (by calling the component's repaint()
   * method).
   */
   private void redraw ( ) {
      if (component != null) {
         // need to just repaint the necessary area
         component.repaint();
      }
   }

   /**
   * Hide the ROI from view.
   */
   public void hide ( ) {
      state = state & (~STATE_VISIBLE);
      redraw();
   }

   /**
   * Show the ROI.
   */
   public void show ( ) {
      state = state | STATE_VISIBLE;
      redraw();
   }

   /**
   * If the boolean argument is true, makes the ROI visible. If false,
   * makes the ROI invisible. 
   * @param cond	if true, show the ROI; if false, hide the ROI. 
   */
   public void show ( boolean cond ) {
      if (cond) show(); else hide();
   }

   /**
   * Adjust the width and height in the current direction.  Overrides
   * the superclass method to handle single-direction growth and
   * negative width/height.
   * @param x The x value of the new location.
   * @param y The y value of the new  location.
   */
   public void grow ( int _x, int _y ) {
      resize( _x - x + 1, _y - y + 1 );
   }

   /**
   * Resize the ROI to the width and height specified by the dimension
   * @return	The current size of this ROI.
   */
   public Dimension size ( ) {
      return( new Dimension( width, height ));
   }

   /**
   * Resize the ROI to the width and height specified by the dimension
   * argument.
   * @param _dim	The new dimension of this ROI.
   */
   public void resize ( Dimension _dim ) {
      resize( _dim.width, _dim.height );
   }

   /**
   * Resize the ROI to the width and height specified.
   * @param _width	The new width of this ROI.
   * @param _height	The new height of this ROI.
   */
   public void resize ( int _width, int _height ) {

      hide();
      width = _width;
      height = _height;
      //calcDimensions();
      show();
   }

   /**
   * Determines if the specified (x,y) location is on or inside this
   * component.  This method is used for resize or move operations on
   * the region.
   * @param _x	The x coordinate
   * @param _y	The y coordinate
   * @param _pad	The pad value.  This amount is added to each side of
   *    the bounding box of the ROI for determining on-ness insideness.
   * @return A direction constant indicating the postion of the (x,y)
   *    location of the region box; otherwise 0.
   */
   public int on ( int _x, int _y, int _pad ) {

      // return false if outside the region (plus pad)
      if ((_x < (x - _pad)) || (_x > (x + width + _pad)) ||
          (_y < (y - _pad)) || (_y > (y + height + _pad)))
         return( 0 );

      // return CENTER if inside the region (minus pad)
      if ((_x > (x + _pad)) && (_x < (x + width - _pad)) &&
          (_y > (y + _pad)) && (_y < (y + height - _pad)))
         return( CENTER );

      // check all vertices and sides
      if (_x < (x + _pad)) {                        // right side and vertices
          if (_y < (y + _pad)) return( NORTHWEST ); // upper right vertex
          if (_y > (y + width - _pad)) return( SOUTHWEST );// lower right vertex
          return( WEST );                           // right side
      }
      else if (_x > (x + width - _pad)) {           // left side & vertices
          if (_y < (y + _pad)) return( NORTHEAST ); // upper left vertex
          if (_y > (y + width - _pad)) return( SOUTHEAST );// lower left vertex
          return( EAST );                           // left side
      }
      else if (_y > (y + width - _pad))
         return( SOUTH );
      else if (_y < (y + _pad))
         return( NORTH );

      // This code should never be reached
      System.out.println( "(ROI) Internal consistency error: ("+
            x+","+y+","+width+","+height+") <> ("+_x+","+_y+")" );
      return( 0 );
   }

   /** 
   * return the Rectangle described by this region.
   * @return The Rectangle described by this region.
   */
   public Rectangle getRect ( ) {
      return( new Rectangle( x, y, width, height ));
   }

   /**
   * Indicate whether the ROI has been initialized but not completed.
   * @return boolean true if this ROI has been initialized but not completed.
   */
   public boolean isInitialized ( ) {
      return( (state & STATE_INITIALIZED) != 0 );
   }

   /**
   * Indicate whether the ROI has been completed.
   * @return boolean true if this ROI has been completed.
   */
   public boolean isActive ( ) {
      return( (state & STATE_ACTIVE) != 0 );
   }

   /**
   * Indicate whether the ROI is visible.
   * @return boolean true if this ROI is showing; false if it is hidden.
   * @see show
   */
   public boolean isVisible ( ) {
      return( (state & STATE_VISIBLE) != 0 );
   }

   /**
   * Indicate whether the ROI has been grabbed.
   * @return boolean true if this ROI has been grabbed; otherwise false
   * @see grab
   * @see drop
   */
   public boolean isGrabbed ( ) {
      return( (grabLoc != null) );
   }

   /**
   * Set the component to attach this ROI to.
   * @param _component	The component to attach the ROI to.
   */
   public void setComponent ( Component _component ) {
      component = _component;

      // make sure the ROI isn't too big
      resize( width, height );
   }

   /**
   * Set the style for this ROI.
   * @param _style	The style to use for the ROI.
   */
   public void setStyle ( int _style ) {
      if ((_style < 0) || (_style > STYLE_TERMINUS)) return;

      hide();
      style = _style;
      show();
   }

   /**
   * Get the style of this ROI.
   * @return 	The style of the ROI.
   */
   public int getStyle ( ) {
      return( style );
   }

   /**
   * Set the color for this ROI.
   * @param _color	The Color to use to draw the ROI.
   */
   public void setColor ( Color _color ) {
      color = _color;
      redraw();
   }

   /**
   * Get the color of this ROI.
   * @return 	The Color used to draw the ROI.
   */
   public java.awt.Color getColor ( ) {
      return( color );
   }

   /**
   * Set the line thickness to use.  This controls the width of the lines
   * that make up box- and cross-style ROIs, and the size of the center
   * portion of open- and bullseye-style ROIs.
   * @param _thickness	The thickness of the ROI elements.
   */
   public void setThickness ( int _thickness ) {
      int   min = (width > height) ? height : width;

      // only use a positive thickness
      if (_thickness < 0) _thickness = -_thickness;

      // force thickness to be less than width and height
      if (_thickness > min-2) _thickness = min-2;

      thickness = _thickness;
      redraw();
   }

   /**
   * Get the current ROI line thickness.
   * @return The thicknes of the ROI elements.
   */
   public int getThickness ( ) {
      return( thickness );
   }

   /**
   * Indicates whether this ROI is resizable. By default, an ROI is resizable.
   * @return true if this ROI can be resized; false otherwise
   */
   public boolean isResizable ( ) {
      return( (state & STATE_RESIZABLE) != 0 );
   }

   /**
   * Determines whether this ROI should be resizable. By default, an ROI
   * is resizable.  
   * @param resizable	true if this ROI should be resizable; false otherwise
   */
   public void setResizable ( boolean resizable ) {
      if (resizable)
         state |= STATE_RESIZABLE;
      else
         state &= (~STATE_RESIZABLE);
   }

   /**
   * Translate a direction integer to a String representation.
   * @param _direction An integer representation of a direction.
   * @return A String describing the direction; "NONE" if out of bounds.
   */
   public String directionString ( int _direction ) {
      switch (_direction) {
         case CENTER: return( "CENTER" );
         case NORTH: return( "NORTH" );
         case NORTHEAST: return( "NORTHEAST" );
         case EAST: return( "EAST" );
         case SOUTHEAST: return( "SOUTHEAST" );
         case SOUTH: return( "SOUTH" );
         case SOUTHWEST: return( "SOUTHWEST" );
         case WEST: return( "WEST" );
         case NORTHWEST: return( "NORTHWEST" );
      }

      return( "NONE" );
   }

  // strict java Rectangle doesn't allow negative width, or hight
  // convert a rectangle, maybe with negative w or h, to be strict java Rectangle
  public static Rectangle getTrueRectangle(Rectangle rec) {
    int x = rec.x;
    int y = rec.y;
    int w = rec.width;
    int h = rec.height;
    if (rec.width < 0) {
      x += w;
      w = -w;
    }
    if (rec.height < 0) {
      y += h;
      h = -h;
    }
    return new Rectangle(x, y, w, h);
  }
}
