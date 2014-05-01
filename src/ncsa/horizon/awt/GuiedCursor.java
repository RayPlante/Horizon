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
* GuiedCursor.java - Implement a draggable cursor for a Component.
*
* Modification history:
*    22-Aug-1997 Wei Xie     Initial version.
*    23-Dec-1997 Plante      fix for CursorSettingPanel's move to modules 
*                               package
*/

package ncsa.horizon.awt;

import ncsa.horizon.modules.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
/**
 * This class is a subclass of ncsa.horizon.awt.Cursor.
 * It adds the functionality to have a setting panel.
 * Client can use setting panel to set the attributes 
 * of the Cursor.
 * It implements ncsa.horizon.awt.Guiedable interface.
 * Client of this class can call getGui to get a reference
 * to the setting panel.
 * It extends Cursor class to observe its interface but
 * overrides all of its public methods.
 */

public class GuiedCursor extends Cursor implements Guiedable {
  // Hide x, y to prevent client access cursor location directly.
  // This enforce the coincidence of cursor position and setting panel
  // display
  private int x, y;

  private Cursor cursor;
  private CursorSettingPanel cursorSettingPanel;

  public GuiedCursor() {
    cursor = new Cursor();
  }

  public GuiedCursor(Component parent) {
    cursor = new Cursor(parent);
    cursorSettingPanel = new CursorSettingPanel(cursor, parent);
  }

  /**
   * Create a Cursor with a given size and style.
   *
   * @param _size       The size of the cursor (height and width are equal)
   * @param _style      The style of the cursor
   */
   public GuiedCursor ( int size, int style ) {
     cursor = new Cursor(size, style);
   }

   /**
   * Draw the cursor at its current location.  It is the responsibility of
   * the applet/application to draw the cursor at the appropriate times.
   * This gives maximum flexibility for double buffering, etc.
   * @param The Graphics context on which to draw the Cursor.
   */
   public void draw ( Graphics g ) {
     cursor.draw(g);
   }

   /**
   * Drag this cursor to a given location.
   * @param _x          the x coordinate.
   * @param _y          the y coordinate.
   */
   public void drag ( int x, int y ) {
     cursor.drag( x, y );
   }

   /**
   * Drop this cursor.
   * @param _x          The x coordinate.
   * @param _y          The y coordinate.
   */
   public void drop ( int x, int y ) {
     cursor.drop(x, y);
   }

  /**
   * Return one element component array.  The only element is
   * the reference of the CursorSettingPanel
   */
  public Component[] getGui() {
    Component[] cmp = new Component[1];
    cmp[0] = cursorSettingPanel;
    return cmp;
  }

   /**
   * Get the color of this Cursor.
   * @return    The Color used to draw the cursor.
   */
   public java.awt.Color getColor ( ) {
     return cursor.getColor();
   }

  /**
   * Return cursor's location.
   */
  public Point getLocation() {
    return new Point(cursor.x, cursor.y);
  }

   /**
   * Get the style of this Cursor.
   * @return    The style of the cursor.
   */
   public int getStyle ( ) {
     return cursor.getStyle();
   }

   /**
   * Get the current cursor line thickness.
   * @return The thicknes of the cursor elements.
   */
   public int getThickness ( ) {
     return cursor.getThickness();
   }

   /**
   * Grab this cursor in preparation for dragging or placing.
   */
   public void grab ( ) {
     cursor.grab();
   }

  /**
   * Hide the cursor from view.
   */
   public void hide ( ) {
     cursorSettingPanel.setShowbox(false);
   }

   /**
   * Determines if the specified (x,y) location is inside this component.
   * @param _x  The x coordinate
   * @param _y  The y coordinate
   * @return true if the specified (x,y) location lies within this component;
   *     false otherwise.
   * @see locate
   */
   public boolean inside(int x, int y) {
     return cursor.inside(x, y);
   }

  /**
   * Determines if the specified (x,y) location is inside or 
   * close to this component.
   * @param _x  The x coordinate
   * @param _y  The y coordinate
   * @param _pad        The pad value.  This amount is added to each side of
   *    the bounding box of the cursor for determining insideness.
   * @return true if the specified (x,y) location lies within this component;
   *     false otherwise.
   * @see locate
   */
   public boolean inside(int x, int y, int pad) {
     return cursor.inside(x, y, pad);
   }

   /**
   * Indicate whether the cursor has been grabbed.
   * @return boolean true if this Cursor has been grabbed; otherwise false
   * @see grab
   * @see drop
   */
   public boolean isGrabbed ( ) {
     return cursor.isGrabbed();
   }

   /**
   * Indicate whether the cursor is visible.
   * @return boolean true if this Cursor is showing; false if it is hidden.
   * @see show
   */
   public boolean isVisible ( ) {
     return cursor.isVisible();
   }

  /**
   * The location of this Cursor in its parent's coordinate space.
   * @return The location of this Cursor in its parent's coordinate space.
   * @see move
   */
   public Point location ( ) {
     return cursor.location();
   }

  /**
   * Move this Cursor to the coordinate (x,y) in the parent's coordinate
   * space.  This calls the repaint() method of the Cursor's component,
   * who is ultimately responsible for calling the Cursor's draw() method.
   * 
   * @param _x          the x coordinate
   * @param _y          the y coordinate
   */
  public void move(int x, int y) {
    cursorSettingPanel.setFields(x, y);
  }

  /**
   * Resize the Cursor to the width and height specified by the dimension
   * argument.
   * @param _dim        The new dimension of this Cursor.
   */
  public void resize (Dimension dim) {
    resize(dim.width, dim.height);
  }

  /**
   * Resize the Cursor to the width and height specified.
   * @param _width      The new width of this Cursor.
   * @param _height     The new height of this Cursor.
   */
   public void resize ( int _width, int _height ) {
     cursorSettingPanel.setScrolls(width, height, 
				  cursor.getThickness());
   }

   /**
   * Set the color for this Cursor.
   * @param _color      The Color to use to draw the cursor.
   */
   public void setColor(Color color) {
     cursorSettingPanel.setColorchoice(color);
   }

   /**
   * Set the component to attach this Cursor to.
   * @param _component  The component to attach the Cursor to.
   */
   public void setComponent ( Component component ) {
     cursor.setComponent(component);
     cursorSettingPanel = new CursorSettingPanel(cursor, component);
   }

   /**
   * Set the style for this Cursor.
   * @param _style      The style to use for the cursor.
   */
   public void setStyle(int style) {
     cursorSettingPanel.setStylechoice(style);
   }

   /**
   * Set the line thickness to use.  This controls the width of the lines
   * that make up box- and cross-style cursors, and the size of the center
   * portion of open- and bullseye-style cursors.
   * @param int         The thickness of the cursor elements.
   */
   public void setThickness ( int thickness ) {
     Dimension dim = size();
     cursorSettingPanel.setScrolls(dim.width, dim.height, thickness);
   }

  /**
   * Show the cursor.
   */
   public void show ( ) {
     cursorSettingPanel.setShowbox(true);
   }

   /**
   * If the boolean argument is true, makes the Cursor visible. If false,
   * makes the Cursor invisible. 
   * @param cond        if true, show the Cursor; if false, hide the Cursor. 
   */
   public void show ( boolean cond ) {
     cursorSettingPanel.setShowbox(cond);
   }

  /**
   * Return the size of Cursor 
   */
   public Dimension size ( ) {
      return cursor.size();
   }

}
