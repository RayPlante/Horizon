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
* GuiedLineSelection.java - Implement a draggable line for a Component.
*
* Modification history:
*    22-Aug-1997 Wei Xie     Initial version.
*/

package ncsa.horizon.awt;

import ncsa.horizon.modules.LineSelectionSettingPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
/**
 * This class is a subclass of ncsa.horizon.awt.LineSelection.
 * It adds the functionality to have a setting panel.
 * Client can use setting panel to set the attribute 
 * of the LineSelection.
 * It implements ncsa.horizon.awt.Guiedable interface.
 * Client of this class can call getGui to get a reference
 * to the setting panel.
 */

public class GuiedLineSelection extends LineSelection implements Guiedable {
  // hide x, y, width, height to prevent client access them directly.
  // This enforce the coincidence of LineSelection attribute and their
  // setting panel display.
  private int x, y, width, height;
  private LineSelection lineSelection;
  private LineSelectionSettingPanel lineSelectionSettingPanel;

  /**
   * Create a LineSelection in the default style and size.
   * The Line selected is start at (0. 0), with x, y component
   * 0, 0.
   * @param _component  The component to attach the LineSelection to.
   */
  public GuiedLineSelection(Component component) {
    lineSelection = new LineSelection(component);
    lineSelectionSettingPanel = new LineSelectionSettingPanel(lineSelection, component);
  }

  /**
   * Create a LineSelection beginning at the specified point.
   * @param p The point defining a vertex of the region.
   * @param _component  The component to attach the LineSelection to.
   */
  public GuiedLineSelection(Point p, Component component) {
    lineSelection = new LineSelection(p, component);
    lineSelectionSettingPanel = new LineSelectionSettingPanel(lineSelection, component);
  }

  /**
   * Create a LineSelection beginning at the specified point.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param _component  The component to attach the LineSelection to.
   */
  public GuiedLineSelection(int x, int y, Component component) {
    lineSelection = new LineSelection(x, y, component);
    lineSelectionSettingPanel = new LineSelectionSettingPanel(lineSelection, component);
  }

  /**
   * Create a LineSelection beginning with the specified vertex and dimensions.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param width The width of the region.
   * @param height The height of the region.
   * @param _component  The component to attach the LineSelection to.
   */
  public GuiedLineSelection(int x,int y, int width,
		       int height, Component component ) {
    lineSelection = new LineSelection(x, y, width, height, component);
    lineSelectionSettingPanel = new LineSelectionSettingPanel(lineSelection, component);
  }

  /**
   * Draw the LineSelection at its current location.  It is the responsibility of
   * the applet/application to draw the LineSelection at the appropriate times, e.g.,
   * inside the component's update() and/or paint() method.  This gives
   * maximum flexibility for double buffering, etc.
   * @param g The Graphics context to use for drawing.
   */
  public void draw( Graphics g ) {
    lineSelection.draw(g);
  }

  /**
   * Return a copy the end point.
   */
  public Point endPoint() {
    return lineSelection.endPoint();
  }

  /**
   * Get the color of this LineSelection.
   * @return    The Color used to draw the LineSelection.
   */
  public java.awt.Color getColor() {
    return lineSelection.getColor();
  }

  /**
   * Return one element component array.  The only element is
   * the reference of the LineSelectionSettingPanel
   */
  public Component[] getGui() {
    Component[] cmp = new Component[1];
    cmp[0] = lineSelectionSettingPanel;
    return cmp;
  }

  /**
   * return this LineSelection as a Line to show its
   * geographic attributes.
   */
  public Line getLine() {
    return lineSelection.getLine();
  }

  /**
   * Get the current LineSelection's line thickness.
   * @return The thicknes of the LineSelection.
   */
  public int getThickness() {
    return lineSelection.getThickness();
  }

  public void hide() {
    show(false);
  }

  /**
   * Tell if the LineSelection is visible
   */
  public boolean isVisible() {
    return lineSelection.isVisible();
  }

  /**
   * Return line length.
   * @return length of this Line.
   */
  public int length() {
    return lineSelection.length();
  }

  /**
   * Move this LineSelection to the coordinate (x,y) in the parent's coordinate
   * space.  This calls the repaint() method of the LineSelection's component,
   * which is ultimately responsible for calling the LineSelectio's draw() method.
   * 
   * @param _x          the x coordinate
   * @param _y          the y coordinate
   */
   public void move(int x, int y) {
     lineSelectionSettingPanel.setXfield(x);
     lineSelectionSettingPanel.setYfield(y);
   }

  /**
   * Reset start point and delta_x, delta_y of the LineSelection
   */
  public void reshape(int x, int y, int delta_x, int delta_y) {
    lineSelectionSettingPanel.setFields(x, y, delta_x, delta_y);
  }

  /**
   * Reset delta_x, and delta_y of the LineSelection.
   */
  public void resize(int delta_x, int delta_y) {
    lineSelectionSettingPanel.setDelta_xfield(delta_x);
    lineSelectionSettingPanel.setDelta_yfield(delta_y);
  }

  /**
   * Set the color for this LineSelection.  The associated component 
   * will repaint.
   * @param color      The Color to use to draw the LineSelection.
   */
  public void setColor(Color color) {
    lineSelectionSettingPanel.setColorchoice(color);
  }

  /**
   * Set the thickness for this LineSelection.  The associated component 
   * will repaint.
   * @param thickness Thickness to set.
   */
  public void setThickness(int thickness) {
    lineSelectionSettingPanel.setScroll(thickness);
  }

  /**
   * Show or hide this LineSelection according t
   * @param t true to show, false to hide
   */
  public void show(boolean b) {
    lineSelectionSettingPanel.setShowbox(b);
  }

  /** return a copy of start point.
   */
  public Point startPoint() {
    return lineSelection.startPoint();
  }

  /**
   * Translates this LineSelection by dx to 
   * the right and dy downward so that its
   * top- left corner is now point (x+dx, y+dy),
   * where it had been the point (x, y). 
   */
  public void translate(int dx, int dy) {
    move(x + dx, y + dy);
  }
}
