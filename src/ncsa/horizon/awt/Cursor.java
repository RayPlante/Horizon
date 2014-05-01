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
* Cursor.java - Implement a draggable cursor for a Component.
*
* Modification history:
*    18-Sep-1996 plutchak     Initial version.
*/
package ncsa.horizon.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

/**
* A draggable cursor for a Component.  This allows a distinction to be
* made between the cursor attached to a component and the cursor used
* by the windowing system.  The cursor can be moved and placed by pressing
* the mouse button while the window system cursor is "inside" the component
* cursor, dragging it to a new location, and releasing the mouse button.<p>
* The Cursor neither generates nor receives events.  It can be thought of
* as a simple decorational widget than knows its location and state, can
* draw itself given a graphics context, and can be manipulated and queried
* through method calls.  It is the responsibility of the parent component
* to ask the Cursor to draw/redraw itself.
*
* @version 0.1 alpha
* @author Horizon Team, University of Illinois at Urbana-Champaign
* @author <br>Joel Plutchak &ltplutchak@uiuc.edu&gt
*/
public class Cursor {
   /** * The x location of the cursor. */
   public int        x;                            // x location
   /** * The y location of the cursor. */
   public int        y;                            // y location

   // user-selectable attributes
   protected int       height, width;                // dimensions
   protected int       heightInc, widthInc;          // convenience values
   protected int       heightDec, widthDec;
   protected int       thickness = 2;
   protected int       style;                        // shape/appearance
   protected Color     color = Color.red;            // color

   // controlled attributes
   protected int       state = STATE_VISIBLE;        // visible/hidden, etc.
   protected Component component = null;             // component to attach to
   protected Point     grabLoc = null;               // where cursor was grabbed

   // constants
   /** * A cross (plus-sign) cursor. */
   public static final int    STYLE_CROSS = 1;
   /** * A solid block cursor. */
   public static final int    STYLE_DOT = 2;
   /** * A box cursor. */
   public static final int    STYLE_BOX = 3;
   /** * A box cursor with central dot. */
   public static final int    STYLE_BULLSEYE = 4;
   /** * A cross cursor with open hotspot. */
   public static final int    STYLE_OPENCROSS = 5;
   /** * An cross cursor that spans the display. */
   public static final int    STYLE_SPANNINGCROSS = 6;
   /** * An X-shaped cursor.
            (Not yet implemented-- awaiting richer graphics API.) */
   public static final int    STYLE_X = 7;
   /** * A monochrome user-supplied cursor.
            (Not yet implemented-- awaiting richer graphics API.) */
   public static final int    STYLE_USER_ICON = 8;
   /** * A fixed-color user-supplied cursor.
            (Not yet implemented-- awaiting richer graphics API.) */
   public static final int    STYLE_USER_PIXMAP = 9;
   // this should also be set to the highest possible cursor style value
   protected static final int   STYLE_TERMINUS = 6;

   protected static final int   STATE_VISIBLE = 0x0001;

   /**
   * Create a Cursor in the default style and size.  The current
   * default cursor is a small cross.
   */
   public Cursor ( ) {
      width = 16;
      height = width;
      calcDimensions();

      style = 0x0001;        // use the first defined style as default
   }

   /**
   * Create a Cursor with a given size and style.
   *
   * @param _size	The size of the cursor (height and width are equal)
   * @param _style      The style of the cursor
   */
   public Cursor ( int _size, int _style ) {
      width = (int) Math.abs( (double)_size );  // quietly ignore negative size
      height = width;
      calcDimensions();

      style = _style;

      // set to default style if style out of bounds
      if (style <= 0) style = 1;
      if (style > STYLE_TERMINUS) style = 1;
   }

   /**
   * Create a Cursor for a given component.
   *
   * @param _component	The component to attach the Cursor to.
   */
   public Cursor ( Component _component ) {
      width = 16;
      height = width;
      calcDimensions();

      style = 0x0001;

      component = _component;

      x = component.size().width/2;
      y = component.size().height/2;
   }

   /**
   * calculate convenience values for use by other methods.
   */
   protected void calcDimensions ( ) {
      widthInc = widthDec = width/2;
      widthInc += (width % 2);

      heightInc = heightDec = height/2;
      heightInc += (height % 2);
   }

   /**
   * Draw the cursor at its current location.  It is the responsibility of
   * the applet/application to draw the cursor at the appropriate times.
   * This gives maximum flexibility for double buffering, etc.
   * @param The Graphics context on which to draw the Cursor.
   */
   public void draw ( Graphics g ) {

      if (component == null) return;

      if (isVisible() == false) return;

      Color saveColor = g.getColor();
      g.setColor( color );

      switch (style) {
         case STYLE_OPENCROSS:
            g.fillRect( x-widthDec, y-thickness/2, widthInc-thickness/2,
                  thickness );
            g.fillRect( x+thickness/2, y-thickness/2, widthDec-thickness/2,
                  thickness );
            g.fillRect( x-thickness/2, y-heightDec, thickness,
                  heightDec-thickness/2 );
            g.fillRect( x-thickness/2, y+thickness/2, thickness,
                  heightInc-thickness/2 );
            break;
         case STYLE_CROSS:
            g.fillRect( x-thickness/2, y-heightDec, thickness, height );
            g.fillRect( x-widthDec, y-thickness/2, width, thickness );
            break;
         case STYLE_X:
            // not yet implemented (awaiting richer graphics API)
            break;
         case STYLE_BULLSEYE:
            g.fillRect( x-thickness/2, y-thickness/2, thickness, thickness );
         case STYLE_BOX:
            g.fillRect( x-widthDec, y-heightDec, width, thickness );
            g.fillRect( x-widthDec, y+heightInc-thickness, width, thickness );
            g.fillRect( x-widthDec, y-heightDec, thickness, height );
            g.fillRect( x+widthInc-thickness, y-heightDec, thickness, height );
            break;
         case STYLE_DOT:
            g.fillRect( x-widthDec, y-heightDec, width, height );
            break;
         case STYLE_SPANNINGCROSS:
            Dimension size = component.size();

            g.fillRect( x-thickness/2, 0, thickness, size.height );
            g.fillRect( 0, y-thickness/2, size.width, thickness );
            break;
      }
      g.setColor( saveColor );
   }

   /**
   * Grab this cursor in preparation for dragging or placing.
   */
   public void grab ( ) {
      //System.out.println( "(grab) at "+x+", "+y );
      grabLoc = new Point( x, y );
   }

   /**
   * Drag this cursor to a given location.
   * @param _x		the x coordinate.
   * @param _y		the y coordinate.
   */
   public void drag ( int x, int y ) {
      // need to erase the old cursor and draw the new one
      move( x, y );
   }

   /**
   * Drop this cursor.
   * @param _x		The x coordinate.
   * @param _y		The y coordinate.
   */
   public void drop ( int _x, int _y ) {
      //System.out.println( "(drop) at "+x+", "+y );

      if (!isGrabbed())
         return;

      if (component == null)
         return;

      // if dropped off the component, set it to the grabbed location
      if ((_x < 0) || (_x > component.size().width) ||
          (_y < 0) || (_y > component.size().height))
         move( grabLoc.x, grabLoc.y );
      else
         move( _x, _y );

      // depending on how mouse up is implemented, may need to do a final drag()
      grabLoc = null;
   }

   /**
   * Move this Cursor to the coordinate (x,y) in the parent's coordinate
   * space.  This calls the repaint() method of the Cursor's component,
   * who is ultimately responsible for calling the Cursor's draw() method.
   * 
   * @param _x		the x coordinate
   * @param _y		the y coordinate
   */
   public void move ( int _x, int _y ) {
      int   oldx = x;
      int   oldy = y;

      x = _x;
      y = _y;

      if (component != null) {
         // need to just repaint the necessary areas
         if (style == STYLE_SPANNINGCROSS) {
            Dimension size = component.size();

            component.repaint( oldx-thickness/2, 0, thickness, size.height );
            component.repaint( 0, oldy-thickness/2, size.width, thickness );
            component.repaint( x-thickness/2, 0, thickness, size.height );
            component.repaint( 0, y-thickness/2, size.width, thickness );
         }
         else {
            component.repaint( oldx-widthDec, oldy-heightDec, width, height );
            component.repaint( x-widthDec, y-heightDec, width, height );
         }
      }
   }

   /**
   * Cause a redisplay of the cursor (by calling the component's repaint()
   * method).
   */
   protected void redraw ( ) {
      if (component != null) {
         // need to just repaint the necessary area
         if (style != STYLE_SPANNINGCROSS) {
            component.repaint( x-widthDec, y-heightDec, width, height );
         }
         else {
            component.repaint();
         }
      }
   }

   /**
   * The location of this Cursor in its parent's coordinate space.
   * @return The location of this Cursor in its parent's coordinate space.
   * @see move
   */
   public Point location ( ) {
      //System.out.println( "(location) at "+x+", "+y );
      return( new Point( x, y ));
   }

   /**
   * Hide the cursor from view.
   */
   public void hide ( ) {
      state = state & (~STATE_VISIBLE);
      redraw();
   }

   /**
   * Show the cursor.
   */
   public void show ( ) {
      state = state | STATE_VISIBLE;
      redraw();
   }

   /**
   * If the boolean argument is true, makes the Cursor visible. If false,
   * makes the Cursor invisible. 
   * @param cond	if true, show the Cursor; if false, hide the Cursor. 
   */
   public void show ( boolean cond ) {
      if (cond) show(); else hide();
   }

   /**
   * Resize the Cursor to the width and height specified by the dimension
   * @return	The current size of this Cursor.
   */
   public Dimension size ( ) {
      return( new Dimension( width, height ));
   }

   /**
   * Resize the Cursor to the width and height specified by the dimension
   * argument.
   * @param _dim	The new dimension of this Cursor.
   */
   public void resize ( Dimension _dim ) {
      resize( _dim.width, _dim.height );
   }

   /**
   * Resize the Cursor to the width and height specified.
   * @param _width	The new width of this Cursor.
   * @param _height	The new height of this Cursor.
   */
   public void resize ( int _width, int _height ) {

      // make sure cursor isn't bigger than the component
      //if (component != null) {
      //   Dimension   max = component.size();

      //   _width = (max.width < _width) ? max.width : _width;
      //   _height = (max.height < _height) ? max.height : _height;
      //}

      hide();
      width = _width;
      height = _height;
      calcDimensions();
      show();
   }

   /**
   * Determines if the specified (x,y) location is inside this component.
   * @param _x	The x coordinate
   * @param _y	The y coordinate
   * @return true if the specified (x,y) location lies within this component;
   *     false otherwise.
   * @see locate
   */
   public boolean inside ( int _x, int _y ) {

      if ((_x < (x-widthDec)) || (_x > (x+widthInc)) ||
          (_y < (y-heightDec)) || (_y > (y+heightInc)))
         return( false );

      return( true );
   }

   /**
   * Determines if the specified (x,y) location is inside or close to this component.
   * @param _x	The x coordinate
   * @param _y	The y coordinate
   * @param _pad	The pad value.  This amount is added to each side of
   *    the bounding box of the cursor for determining insideness.
   * @return true if the specified (x,y) location lies within this component;
   *     false otherwise.
   * @see locate
   */
   public boolean inside ( int _x, int _y, int _pad ) {

      if ((_x < (x-widthDec)-_pad) || (_x > (x+widthInc)+_pad) ||
          (_y < (y-heightDec)-_pad) || (_y > (y+heightInc)+_pad))
         return( false );

      return( true );
   }

   /**
   * Indicate whether the cursor is visible.
   * @return boolean true if this Cursor is showing; false if it is hidden.
   * @see show
   */
   public boolean isVisible ( ) {
      return( (state & STATE_VISIBLE) == 1 );
   }

   /**
   * Indicate whether the cursor has been grabbed.
   * @return boolean true if this Cursor has been grabbed; otherwise false
   * @see grab
   * @see drop
   */
   public boolean isGrabbed ( ) {
      return( (grabLoc != null) );
   }

   /**
   * Set the component to attach this Cursor to.
   * @param _component	The component to attach the Cursor to.
   */
   public void setComponent ( Component _component ) {
      component = _component;

      // make sure the cursor isn't too big
      resize( width, height );
   }

   /**
   * Set the style for this Cursor.
   * @param _style	The style to use for the cursor.
   */
   public void setStyle ( int _style ) {
      if ((_style < 0) || (_style > STYLE_TERMINUS)) return;

      hide();
      style = _style;
      show();
      //redraw();
   }

   /**
   * Get the style of this Cursor.
   * @return 	The style of the cursor.
   */
   public int getStyle ( ) {
      return( style );
   }

   /**
   * Set the color for this Cursor.
   * @param _color	The Color to use to draw the cursor.
   */
   public void setColor ( Color _color ) {
      color = _color;
      redraw();
   }

   /**
   * Get the color of this Cursor.
   * @return 	The Color used to draw the cursor.
   */
   public java.awt.Color getColor ( ) {
      return( color );
   }

   /**
   * Set the line thickness to use.  This controls the width of the lines
   * that make up box- and cross-style cursors, and the size of the center
   * portion of open- and bullseye-style cursors.
   * @param int		The thickness of the cursor elements.
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
   * Get the current cursor line thickness.
   * @return The thicknes of the cursor elements.
   */
   public int getThickness ( ) {
      return( thickness );
   }
}
