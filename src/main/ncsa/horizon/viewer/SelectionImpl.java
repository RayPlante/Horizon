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
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    09-Dec-1997 Ray Plante  replaced deprecated ImageDisplayMap with 
 *                              ImageDisplayMap
 */

package ncsa.horizon.viewer;

import java.awt.*;
import ncsa.horizon.awt.Line;
import ncsa.horizon.util.ImageDisplayMap;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.Voxel;

public class SelectionImpl {
  private final int PIXELSIZE = 8;
  private final Color color1 = Color.gray;
  private final Color color2 = Color.green;
  private Point pixel;
  private Rectangle box;
  private Line line;

  public SelectionImpl() {
    pixel = new Point(0, 0);
    box = new Rectangle();
    line = new Line();
  }

  /** draw the selection widgets
   * @param g the Graphics of the component
   *          on which the widgets are drawn.
   */
  public void draw(Graphics g) {
    drawBox(g);
    drawLine(g);
    drawPix(g);
  }

  /** draw the box selection 
   * @param g the Graphics of the component
   *          on which the box are drawn.
   */
  public void drawBox(Graphics g) {
    Point upleft = new Point(Math.min(box.x, box.x + box.width),
                             Math.min(box.y, box.y + box.height));
    Dimension d = new Dimension(Math.abs(box.width), 
                                Math.abs(box.height));
    Rectangle rec = new Rectangle(upleft, d);
    Color save = g.getColor();
    g.setColor(color1);
    g.drawRect(rec.x, rec.y, rec.width, rec.height);
    g.setColor(color2);
    g.drawRect(rec.x + 1, rec.y + 1, rec.width - 2, rec.height - 2);
    g.setColor(save);
  } // end SelectionImpl.drawBox

  /** draw the line selection 
   * @param g the Graphics of the component
   *          on which the line are drawn.
   */
  public void drawLine(Graphics g) {
    Line line1 = new Line(line.x + 1, line.y + 1,
			  line.width, line.height);
    line.draw(g, color1);
    line1.draw(g, color2);
  }

  /** draw the pixel selection, the mark is a cross
   * @param g the Graphics of the component
   *          on which the pixel selection are drawn.
   */
  public void drawPix(Graphics g) {
    Point left = new Point(pixel.x, pixel.y);
    Point right = new Point(pixel.x, pixel.y);
    Point up = new Point(pixel.x, pixel.y);
    Point down = new Point(pixel.x, pixel.y);
    left.translate(-PIXELSIZE, 0);
    right.translate(PIXELSIZE, 0);
    up.translate(0, -PIXELSIZE);
    down.translate(0, PIXELSIZE);
    Color save = g.getColor();
    g.setColor(color1);
    g.drawLine(left.x, left.y, right.x, right.y);
    g.drawLine(up.x, up.y, down.x, down.y);
    g.setColor(color2);
    g.drawLine(left.x, left.y + 1, right.x, right.y + 1);
    g.drawLine(up.x + 1, up.y, down.x + 1, down.y);
    g.setColor(save);
  } // end SelectionImpl.drawPix

  public synchronized Rectangle getBoxSelection() {
    return new Rectangle(box.x, box.y, box.width, box.height);
  }

  public synchronized Line getLineSelection() {
    return new Line(line.x, line.y, line.width, line.height);
  }

  public synchronized Point getPixelSelection() {
    return new Point(pixel.x, pixel.y);
  }

  public synchronized void setBoxSelection(int x1, int y1, int x2, int y2) {
    box.reshape(x1, y1, x2 - x1, y2 - y1);
  } // end SelectionImpl.setBoxSelection

  public synchronized void setLineSelection(int x1, int y1, int x2, int y2) {
    line.reshape(x1, y1, x2 - x1, y2 - y1);
  }

  // use synchronized to prevent only set x.  If there is another
  // thread runing.  If that thread call getPixelSelection
  // the results maight be one that only x updated.
  public synchronized void setPixelSelection(int x, int y) {
    pixel.move(x, y);
  }

  public synchronized void setVoxelSelection(Voxel voxel, 
					    Slice slice,
					    ImageDisplayMap pixelMap) {
    // project the specified Voxel onto the current slice
    Voxel use = slice.projection(voxel);
    Point pix = pixelMap.getDisplayPixel(use);
    pixel.move(pix.x, pix.y);
  }
}
