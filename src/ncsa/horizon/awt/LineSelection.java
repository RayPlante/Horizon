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
 * LineSelection.java - Line segment Selection.
 *
 * Modification history:
 *    21-June-1997 Wei Xie     Initial version.
 */

package ncsa.horizon.awt;

import java.awt.*;
import ncsa.horizon.awt.Line;

/**
 * A user-selected  line segment for a Component.  The LineSelection
 *  can be created, moved, resized, etc. by calling the appropriate
 * methods.  It is up to the calling application to implement the user
 * action interface to the routines.  E.g., create the LineSelection  
 * on mouse button down, extend the size on mouse drag,
 *  and return a completed LineSelection on mouse up.<p>
 * The LineSelection neither generates nor receives events. It can be 
 * thought of as a simple decorational widget than knows its location 
 * and state, can draw itself given a graphics context,
 * and can be manipulated and queried
 * through method calls.  It is the responsibility of the parent component
 * to ask the Cursor to draw/redraw itself.

 * @version 0.1 alpha
 * @author Horizon Team, University of Illinois at Urbana-Champaign
 * @author <br>Wei Xie <weixie@lai.ncsa.uiuc.edu>
 */
public class LineSelection extends Line {
  // user-selectable attributes
  private int       thickness = 3;
  private Color     color = Color.red;  // color

  // controlled attributes
  private int       state = STATE_VISIBLE; // visible/hidden, etc.
  private Component component = null; // component to attach to

  // state constants
  private static final int   STATE_INVISIBLE = 0;
  private static final int   STATE_VISIBLE = 1;
  private static final int   STATE_MOVABLE = 2; 
                             // used when it is dragged

  /**
   * Create a LineSelection in the default style and size.
   * The Line selected is start at (0. 0), with x, y component
   * 0, 0.  It is not attached to any Component.
   */
  public LineSelection() {
    component = null;
  }

  /**
   * Create a LineSelection in the default style and size.
   * The Line selected is start at (0. 0), with x, y component
   * 0, 0.
   * @param _component  The component to attach the LineSelection to.
   */
  public LineSelection(Component _component) {
    component = _component;
  }

  /**
   * Create a LineSelection beginning at the specified point.
   * @param p The point defining a vertex of the region.
   * @param _component  The component to attach the LineSelection to.
   */
  public LineSelection(Point p, Component _component) {
    super(p);
    component = _component;
  }

  /**
   * Create a LineSelection beginning at the specified point.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param _component  The component to attach the LineSelection to.
   */
  public LineSelection(int _x, int _y, Component _component) {
    super( _x, _y, 0, 0 );
    component = _component;
  }

  /**
   * Create a LineSelection beginning with the specified vertex and dimensions.
   * @param x The x value of the location of one vertex of the region.
   * @param y The y value of the location of one vertex of the region.
   * @param width The width of the region.
   * @param height The height of the region.
   * @param _component  The component to attach the LineSelection to.
   */
  public LineSelection(int _x,int _y, int _width,
		       int _height, Component _component ) {
    super( _x, _y, _width, _height );
    component = _component;
  }


  /**
   * Draw the LineSelection at its current location.  It is 
   * the responsibility of
   * the applet/application to draw the LineSelection at 
   * the appropriate times, e.g.,
   * inside the component's update() and/or paint() method.  This gives
   * maximum flexibility for double buffering, etc.
   * @param g The Graphics context to use for drawing.
   */
  public void draw( Graphics g ) {

    if(!isVisible()) {
      return;
    }

    Color saveColor = g.getColor();
    g.setColor(color);
    if(thickness > 1) {
      double ratio = ((double) thickness)/((double) length());
      double txdb = ratio * ((double)height) / 2.0;
      int tx = txdb > 0 ? ((int) Math.ceil(txdb)) : ((int) Math.floor(txdb));
      double tydb = - ratio * ((double)width) / 2.0;
      int ty = tydb > 0 ? ((int) Math.ceil(tydb)) : ((int) Math.floor(tydb));
      Point[] poly = new Point[4];
      for(int i = 0; i < 4; i++)
        poly[i] = new Point(x, y);
      poly[0].translate(tx, ty);
      poly[1].translate(-tx, -ty);
      poly[2].translate(width, height);
      poly[2].translate(-tx, -ty);
      poly[3].translate(width, height);
      poly[3].translate(tx, ty);
      Polygon polygon = new Polygon();
      for(int i = 0; i < 4; i++)
        polygon.addPoint(poly[i].x, poly[i].y);
      g.fillPolygon(polygon);
    }
    else
      g.drawLine(x, y, x + width, y + height);
    g.setColor( saveColor );
  } // end draw

  /**
   * Get the color of this LineSelection.
   * @return    The Color used to draw the LineSelection.
   */
   public java.awt.Color getColor()
   {
      return color;
   }

  /**
   * return this LineSelection as a Line to show its
   * geographic attributes.
   */
  public Line getLine() {
    return new Line(x, y, width, height);
  }

  /**
   * Get the current LineSelection's line thickness.
   * @return The thicknes of the LineSelection.
   */
  public int getThickness() {
    return thickness;
  }

  public void hide() {
    show(false);
  }

  /**
   * Tell if the LineSelection is visible
   */
  public boolean isVisible()
  {
    switch(state)
    {
    case STATE_VISIBLE:
    case STATE_MOVABLE:
        return true;
    case STATE_INVISIBLE:
        return false;
    default:
        return false;
    }
  } // end isVisible

   /**
   * Move this LineSelection to the coordinate (x,y) in the parent's coordinate
   * space.  This calls the repaint() method of the LineSelection's component,
   * which is ultimately responsible for calling the LineSelectio's draw() method.
   * 
   * @param _x          the x coordinate
   * @param _y          the y coordinate
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
   } //end move

  /**
   * Cause a redisplay of the LineSelection (by calling the 
   * component's repaint() method).
   */
   private void redraw ( ) {
      if (component != null) {
         // need to just repaint the necessary area
         component.repaint();
      }
   }

  /**
   * Set the color for this LineSelection.  The associated component 
   * will repaint.
   * @param color      The Color to use to draw the LineSelection.
   */
  public void setColor(Color color)
  {
    this.color = color;
    redraw();
  }

  /**
   * Set the component.
   */
  public void setComponent(Component component) {
    this.component = component;
  }

  /**
   * Set the thickness for this LineSelection.  The associated component 
   * will repaint.
   * @param thickness Thickness to set.
   */
  public void setThickness(int thickness)
  {
    this.thickness = thickness;
    redraw();
  }

  /**
   * Show or hide this LineSelection according t
   * @param t true to show, false to hide
   */
  public void show(boolean b)
  {
    if(b)
      state = STATE_VISIBLE;
    else
      state = STATE_INVISIBLE;
    redraw();
  }

}
