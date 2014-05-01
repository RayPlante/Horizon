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
* GraphicsSelectionImpl.java - SelectionImpl with graphics such as
*                              Cursor, LineSelection, and ROI.
*
* Modification history:
*    19-Aug-1997 Wei Xie     Initial version.
*    19-Aug-1997 Wei Xie     Return cardpanel instead of a button
*                            of method getGui.
*/
package ncsa.horizon.viewer;

import java.awt.*;
import ncsa.horizon.awt.*;
import ncsa.horizon.util.*;

/**
 * This is a subclass of SelectionImpl.  It extends 
 * SelectionImpl to observe SelectionImpl's interface.
 * But it override all its public methods. <p>
 * It has more functionality than SelectionImpl.  It
 * implements Guiedable.  The Guiedable interface's
 * getGui will return a one element component array. The only
 * element is a CardPanel with the three settingpanels.  This button
 * will bring out a setting window when clicked.  
 * User can use the
 * setting window to set the attributes of selection
 * graphics.
 */

public class GraphicsSelectionImpl extends SelectionImpl 
                                   implements Guiedable {
  private Component parent;
  private GuiedCursor cursor;
  private GuiedRoi roi;
  private GuiedLineSelection lineSelection;
  private CardPanel settingPanel;

  public GraphicsSelectionImpl(Component parent) {
    this.parent = parent;
    cursor = new GuiedCursor(parent);
    cursor.hide();
    cursor.setColor(Color.magenta);
    cursor.setThickness(1);
    roi = new GuiedRoi(parent);
    roi.hide();
    roi.setColor(Color.magenta);
    roi.setThickness(1);
    lineSelection = new GuiedLineSelection(parent);
    lineSelection.hide();
    lineSelection.setColor(Color.magenta);
    lineSelection.setThickness(1);
    settingPanel = new CardPanel("Set Cursor", cursor.getGui()[0]);
    settingPanel.addCard("Set LineSelection", lineSelection.getGui()[0]);
    settingPanel.addCard("Set ROI", roi.getGui()[0]);
  }

  /** draw the selection widgets
   * @param g the Graphics of the component
   *          on which the widgets are drawn.
   */
  public void draw(Graphics g) {
    cursor.draw(g);
    lineSelection.draw(g);
    roi.draw(g);
  }

  public synchronized Rectangle getBoxSelection() {
    return roi.getRect();
  }

  public synchronized Line getLineSelection() {
    return lineSelection.getLine();
  }

  public synchronized Point getPixelSelection() {
    return cursor.getLocation();
  }

  public Component[] getGui() {
    Component[] cmp = new Component[1];
    cmp[0] = settingPanel;
    return cmp;
  }

  public boolean isBoxSelectionVisible() {
    return roi.isVisible();
  }

  public boolean isLineSelectionVisible() {
    return lineSelection.isVisible();
  }

  public boolean isPixelSelectionVisible() {
    return cursor.isVisible();
  }
					    
  public synchronized void setBoxSelection(int x1, int y1, int x2, int y2) {
    int width = x2 - x1;
    int height = y2 - y1;
    Rectangle box = roi.getRect();
    // make sure ret roi is necessary, since there are chain reactions
    // if roi is a GuiedRoi.
    if((x1 != box.x) || (y1 != box.y) || (width != box.width)
       || (height != box.height))
      roi.reshape(x1, y1, width, height);
  } // end SelectionImpl.setBoxSelection

  public void setBoxSelection(Rectangle box) {
    setBoxSelection(box.x, box.y, box.x + box.width, box.y + box.height);
  } // end SelectionImpl.setBoxSelection

  public synchronized void setLineSelection(int x1, int y1, int x2, int y2) {
    int delta_x = x2 - x1;
    int delta_y = y2 - y1;
    Line tempLine = lineSelection.getLine();
    if((x1 != tempLine.x) || (y1 != tempLine.y) || (delta_x != tempLine.width)
       || (delta_y != tempLine.height))
      lineSelection.reshape(x1, y1, delta_x, delta_y);
  }

  public void setLineSelection(Line line) {
    setLineSelection(line.x, line.y, line.x + line.width,
		     line.y + line.height);
  }

  // use synchronized to prevent only set x.  If there is another
  // thread runing.  If that thread call getPixelSelection
  // the results maight be one that only x updated.
  public synchronized void setPixelSelection(int x, int y) {
    Point tempLoc = cursor.getLocation();
    if(x != tempLoc.x || y != tempLoc.y)
      cursor.move(x, y);
  }

  // use synchronized to prevent only set x.  If there is another
  // thread runing.  If that thread call getPixelSelection
  // the results maight be one that only x updated.
  public void setPixelSelection(Point pixel) {
    setPixelSelection(pixel.x, pixel.y);
  }

  /**
   * set the selected region via a Slice and an ImageDisplayMap for 
   * translating the Slice to display space.  
   */
  public synchronized void setSliceSelection(Slice slice,
                                             ImageDisplayMap pixelMap) {

    // project the specified Voxel onto the current slice
    Slice use = pixelMap.getSlice().projection(slice);
    Rectangle box = pixelMap.getDisplayRegion(use);
    setBoxSelection(box.x, box.y, box.x + box.width, box.y + box.height);
  }

  /**
   * set the selected pixel via a Voxel and an ImageDisplayMap for 
   * translating the Voxel to display space.  
   */
  public synchronized void setVoxelSelection(Voxel voxel,
					     ImageDisplayMap pixelMap) {

    // project the specified Voxel onto the current slice
    Voxel use = pixelMap.getSlice().projection(voxel);
    Point pix = pixelMap.getDisplayPixel(use);
    setPixelSelection(pix.x, pix.y);
  }

  public void showBoxSelection(boolean b) {
    roi.show(b);
    settingPanel.show(roi.getGui()[0]);
  }

  public void showLineSelection(boolean b) {
    lineSelection.show(b);
    settingPanel.show(lineSelection.getGui()[0]);
  }

  public void showPixelSelection(boolean b) {
    cursor.show(b);
    settingPanel.show(cursor.getGui()[0]);
  }
					    
  public void toggleBoxSelection() {
    showBoxSelection(!isBoxSelectionVisible());
  }

  public void toggleLineSelection() {
    showLineSelection(!isLineSelectionVisible());
  }

  public void togglePixelSelection() {
    showPixelSelection(!isPixelSelectionVisible());
  }

}
