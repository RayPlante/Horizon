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
 * Line.java - Line segment.
 *
 * Modification history:
 *    21-Mar-1997 Wei Xie     Initial version.
 */

package ncsa.horizon.awt;

import java.awt.*;
import java.lang.*;

/**
 * Line class is a geometry entity represent a line segment vector
 * with start point and x, y vector components. <p>
 *
 * Now, it just extends java.awt.Rectangle with Rectangle's x, y as
 * start point and width, height as x, y vector components.<p>
 *
 * Since it extends Rectangle, outside objects have access to (x, y),
 * which is the start point of the Line, and width, height which are
 * the x, y components of the line.
 */
public class Line extends Rectangle
{

  /**
   * Constructs a new Line whose start point is at (0, 0),
   * vector componet 0, and 0.
   */
  public Line()
  {
    super();
  }

  /**
   * Constructs a new Line whose start point is at (0, 0),
   * vector componet delta_x, and delta_y
   * @param delta_x the Line's x component
   * @param delta_y the Line's y component
   */
  public Line(int delta_x, int delta_y)
  {
    super(delta_x, delta_y);
  }

  /**
   * Constructs a new Line whose start point is at (x, y),
   * vector componet delta_x, and delta_y
   * @param x the x coordinate of start point
   * @param y the y coordinate of start point
   * @param delta_x the Line's x component
   * @param delta_y the Line's y component
   */
  public Line(int x, int y, int delta_x, int delta_y)
  {
    super(x, y, delta_x, delta_y);
  }

  /**
   * Constructs a new rectangle whose start is the
   * specified point argument and whose x, y components
   * are 0, 0.
   * @param p start point
   */
  public Line(Point p)
  {
    super(p);
  }

  /**
   * Constructs a new rectangle whose start is the
   * specified point argument and whose x, y components
   * are delta_x, delta_y
   * @param p start point
   * @param delta_x the Line's x component
   * @param delta_y the Line's y component
   */
  public Line(Point p, int delta_x, int delta_y)
  {
    super(p.x, p.y, delta_x, delta_y);
  }

  /** draw this line
   */
  public void draw(Graphics g) {
    Point start = startPoint();
    Point end = endPoint();
    g.drawLine(start.x, start.y, end.x, end.y);
  }

  /** draw this line with color
   */
  public void draw(Graphics g, Color color) {
    Color save = g.getColor();
    g.setColor(color);
    draw(g);
    g.setColor(save);
  } // end Line.draw

  /**
   * Return a copy the end point.
   */
  public Point endPoint() {
    return new Point(x + width, y + height);
  }

  /**
   * Return line length.
   * @return length of this Line.
   */
  public int length()
  {
    return ((int) Math.sqrt(width*width + height*height));
  }

  /** return a copy of start point.
   */
  public Point startPoint() {
    return new Point(x, y);
  }
}
