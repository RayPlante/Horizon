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
* GuiedRoi.java - Implement a draggable Roi for a Component.
*
* Modification history:
*    22-Aug-1997 Wei Xie     Initial version.
*/

package ncsa.horizon.awt;

import java.awt.*;
import ncsa.horizon.modules.ROISettingPanel;

/**
 * This class is a subclass of ncsa.horizon.awt.ROI.
 * It adds the functionality to have a setting panel.
 * Client can use setting panel to set the attribute 
 * of the ROI.
 * It implements ncsa.horizon.awt.Guiedable interface.
 * Client of this class can call getGui to get a reference
 * to the setting panel.
 */

public class GuiedRoi extends ROI implements Guiedable {
  // hide x, y, width, height to prevent client access them directly.
  // This enforce the coincidence of LineSelection attribute and their
  // setting panel display.
  private int x, y, width, height;

  private ROI roi;
  private ROISettingPanel roiSettingPanel;

  public GuiedRoi() {
    roi = new ROI();
  }

  public GuiedRoi(Component component) {
    roi = new ROI(component);
    roiSettingPanel = new ROISettingPanel(roi, component);
  }

  /**
   * Create a ROI beginning at the specified point.
   * @param p The point defining a vertex of the region.
   * @param _component  The component to attach the ROI to.
   */
  public GuiedRoi(Point p, Component component ) {
    roi = new ROI(p, component);
    roiSettingPanel = new ROISettingPanel(roi, component);
   }

   /**
   * Create a ROI beginning at the specified point.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param _component  The component to attach the ROI to.
   */
   public GuiedRoi( int x, int y, Component component ) {
    roi = new ROI(x, y, component);
    roiSettingPanel = new ROISettingPanel(roi, component);
   }

   /**
   * Create a ROI beginning with the specified vertex and dimensions.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param width The width of the region.
   * @param height The height of the region.
   * @param _component  The component to attach the ROI to.
   */
   public GuiedRoi( int x,int y, int width,int height, Component component ) {
    roi = new ROI(x, y, width, height, component);
    roiSettingPanel = new ROISettingPanel(roi, component);
   }

  /**
   * Adds a point to a the roi. This results in the smallest
   * roi that contains both the rectangle and the point.
   */
  public void add(int newx, int newy) {
    roi.add(newx, newy);
  }

  /**
   * Adds a point to a roi. This results in the smallest
   * roi that contains both the roi and the point.
   */
  public void add(Point pt) {
    roi.add(pt.x, pt.y);
  }

  /**
   * Adds a rectangle to a roi. This results in the union roi
   * of the roi and the rectangle.
   */
  public void add(Rectangle r) {
    roi.add(r);
  }

  /**
   * Translate a direction integer to a String representation.
   * @param _direction An integer representation of a direction.
   * @return A String describing the direction; "NONE" if out of bounds.
   */
   public String directionString(int direction) {
     return roi.directionString(direction);
   }

  /**
   * Drag this ROI to a given location.
   * @param _x          the x coordinate.
   * @param _y          the y coordinate.
   */
  public void drag ( int x, int y ) {
    roi.drag(x, y);
  }

  /**
   * Draw the ROI at its current location.  It is the responsibility of
   * the applet/application to draw the ROI at the appropriate times, e.g.,
   * inside the component's update() and/or paint() method.  This gives
   * maximum flexibility for double buffering, etc.
   * @param g The Graphics context to use for drawing.
   */
  public void draw(Graphics g) {
    roi.draw(g);
  }

  /**
   * Drop this Region Of Interest.  Depending on region state, this can
   * mean either finish creating a new region, ending a region move, or
   * ending a resize operation.
   * @param _x          The x coordinate.
   * @param _y          The y coordinate.
   */
  public void drop ( int x, int y ) {
    roi.drop(x, y);
  }

  /**
   * Get the color of this ROI.
   * @return    The Color used to draw the ROI.
   */
   public java.awt.Color getColor ( ) {
     return roi.getColor();
   }

  /**
   * Return one element component array.  The only element is
   * the reference of the ROISettingPanel
   */
  public Component[] getGui() {
    Component[] cmp = new Component[1];
    cmp[0] = roiSettingPanel;
    return cmp;
  }

  /**
   * return a Rectangle to represent the Roi's 
   * location and dimension.
   */
  public Rectangle getRect() {
    return new Rectangle(roi.x, roi.y, roi.width, roi.height);
  }

  /**
   * Get the style of this ROI.
   * @return    The style of the ROI.
   */
   public int getStyle() {
     return roi.getStyle();
   }

  /**
   * Get the current ROI line thickness.
   * @return The thicknes of the ROI elements.
   */
   public int getThickness() {
     return roi.getThickness();
   }

  /**
   * Checks whether two rois are equal.
   */
  public boolean equals(Object obj) {
    return roi.equals(obj);
  }

  /**
   * Grab this ROI in preparation for dragging or resizing.
   */
  public void grab ( int x, int y ) {
    roi.grab(x, y);
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
   * Hide the ROI from view.
   */
  public void hide() {
    roiSettingPanel.setShowbox(false);
  }

  /**
   * Checks if the specified point lies inside a the roi.
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public boolean inside(int x, int y) {
    return roi.inside(x, y);
  }

  /**
   * Checks if this roi intersect with rectangle r.
   */
  public boolean intersects(Rectangle r) {
    return roi.intersects(r);
  }

  /**
   * Computes the intersection of this roi and rectangle r.
   */
  public Rectangle intersection(Rectangle r) {
    return roi.intersection(r);
  }

  /**
   * Indicate whether the ROI has been completed.
   * @return boolean true if this ROI has been completed.
   */
   public boolean isActive ( ) {
     return roi.isActive();
   }

  /**
   * Indicate whether the ROI has been grabbed.
   * @return boolean true if this ROI has been grabbed; otherwise false
   * @see grab
   * @see drop
   */
   public boolean isGrabbed ( ) {
     return roi.isGrabbed();
   }

  /**
   * Indicate whether the ROI has been initialized but not completed.
   * @return boolean true if this ROI has been initialized but not completed.
   */
   public boolean isInitialized ( ) {
     return roi.isInitialized();
   }

  /**
   * Indicates whether this ROI is resizable. By default, an ROI is resizable.
   * @return true if this ROI can be resized; false otherwise
   */
   public boolean isResizable() {
     return roi.isResizable();
   }

  /**
   * Indicate whether the ROI is visible.
   * @return boolean true if this ROI is showing; false if it is hidden.
   * @see show
   */
   public boolean isVisible ( ) {
     return roi.isVisible();
   }

   /**
   * Move this ROI to the coordinate (x,y) in the parent's coordinate
   * space.  This calls the repaint() method of the ROI's component,
   * which is ultimately responsible for calling the ROI's draw() method.
   * 
   * @param _x          the x coordinate
   * @param _y          the y coordinate
   */
   public void move (int x, int y) {
     roiSettingPanel.setXfield(x);
     roiSettingPanel.setYfield(y);
   }

   /**
   * Determines if the specified (x,y) location is on or inside this
   * component.  This method is used for resize or move operations on
   * the region.
   * @param _x  The x coordinate
   * @param _y  The y coordinate
   * @param _pad        The pad value.  This amount is added to each side of
   *    the bounding box of the ROI for determining on-ness insideness.
   * @return A direction constant indicating the postion of the (x,y)
   *    location of the region box; otherwise 0.
   */
  public int on ( int x, int y, int pad ) {
    return roi.on(x, y, pad);
  }

  /**
   * Reshapes the roi.
   */
  public void reshape(int x, int y, int width, int height) {
    roiSettingPanel.setFields(x, y, width, height);
  }

  /**
   * Resize the ROI to the width and height specified by the dimension
   * argument.
   * @param _dim        The new dimension of this ROI.
   */
   public void resize ( Dimension _dim ) {
     resize( _dim.width, _dim.height );
   }

   /**
   * Resize the ROI to the width and height specified.
   * @param _width      The new width of this ROI.
   * @param _height     The new height of this ROI.
   */
   public void resize ( int width, int height ) {
     roiSettingPanel.setWfield(width);
     roiSettingPanel.setHfield(height);
   }

  /**
   * Set the color for this ROI.
   * @param _color      The Color to use to draw the ROI.
   */
   public void setColor(Color color) {
     roiSettingPanel.setColorchoice(color);
   }

  /**
   * Set the component to attach this ROI to.
   * @param _component  The component to attach the ROI to.
   */
   public void setComponent (Component component) {
     roi.setComponent(component);
     roiSettingPanel = new ROISettingPanel(roi, component);
   }

  /**
   * Determines whether this ROI should be resizable. By default, an ROI
   * is resizable.  
   * @param resizable   true if this ROI should be resizable; false otherwise
   */
   public void setResizable ( boolean resizable ) {
     roi.setResizable(resizable);
   }

  /**
   * Set the style for this ROI.
   * @param _style      The style to use for the ROI.
   */
   public void setStyle(int style) {
     roi.setStyle(style);
   }

  /**
   * Set the line thickness to use.  This controls the width of the lines
   * that make up box- and cross-style ROIs, and the size of the center
   * portion of open- and bullseye-style ROIs.
   * @param _thickness  The thickness of the ROI elements.
   */
   public void setThickness(int thickness) {
     roiSettingPanel.setScroll(thickness);
   }

  /**
   * Show the ROI.
   */
  public void show() {
    roiSettingPanel.setShowbox(true);
  }

   /**
   * If the boolean argument is true, makes the ROI visible. If false,
   * makes the ROI invisible. 
   * @param cond        if true, show the ROI; if false, hide the ROI. 
   */
   public void show ( boolean cond ) {
      if (cond) show(); else hide();
   }

   /**
   * return size of the ROI
   * @return    The current size of this ROI.
   */
  public Dimension size ( ) {
    return roi.size();
  }

  /**
   * Returns the String representation of this Rectangle's values.
   */
  public String toString() {
    return getClass().getName() + "[x=" + roi.x + ",y=" + 
           roi.y + ",width=" + roi.width + ",height=" + roi.height + "]";
    }

}
