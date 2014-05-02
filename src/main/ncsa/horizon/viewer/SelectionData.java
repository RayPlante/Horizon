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
 */

package ncsa.horizon.viewer;

import java.util.*;
import java.awt.*;

import ncsa.horizon.awt.*;

/**
 * SelectionData class is used to be a model in MVC
 * pattern.  Here view will be SelectionViewer and
 * control will be the mouse event listener.
 * This way, two viewers can react to one mouse control.
 * For example, if mouse is moved to select a pixel, the
 * spreadsheet's corresponding cell will be highlighted.
 * The same holds vice versa.
 */
public class SelectionData extends Observable {
  /**
   * MOUSE_ASPECT = 1
   */
  public static final int MOUSE_ASPECT = 1;  // 0001
  /**
   * PIXEL_ASPECT = 2
   */
  public static final int PIXEL_ASPECT = 2;  // 0010
  /**
   * BOX_ASPECT = 4
   */
  public static final int BOX_ASPECT = 4;    // 0100
  /**
   * LINE_ASPECT = 8
   */
  public static final int LINE_ASPECT = 8;   // 1000

  // current mouse position
  private Point mouse;
  private Point pixel;
  private Rectangle box;
  private Line line;
  private int changeAspect = 0;

  public SelectionData() {
    mouse = new Point(0, 0);
    pixel = new Point(0, 0);
    box = new Rectangle();
    line = new Line();
  }

  public synchronized Rectangle getBoxSelection() {
    return new Rectangle(box.x, box.y, box.width, box.height);
  }

  public synchronized Line getLineSelection() {
    return new Line(line.x, line.y, line.width, line.height);
  }

  public synchronized Point getMousePosition() {
    return new Point(mouse.x, mouse.y);
  }

  public synchronized Point getPixelSelection() {
    return new Point(pixel.x, pixel.y);
  }

  /**
   * Notifies all observers if an observable change occurs.
   */
  public void notifyObservers() {
    notifyObservers(new Integer(changeAspect));
    changeAspect = 0;
  }

  /**
   * The third bit of changedAspect will be set.
   */
  public void setBoxSelection(Rectangle box) {
    setBoxSelection(box.x, box.y, box.x + box.width, box.y + box.height);
  } // end SelectionImpl.setBoxSelection

  /**
   * The third bit of changedAspect will be set.
   */
  public synchronized void setBoxSelection(int x1, int y1, int x2, int y2) {
    int width = x2 - x1;
    int height = y2 - y1;
    if((box.x != x1) || (box.y != y1) || (box.width != width)
       || (box.height != height)) {
    box.reshape(x1, y1, width, height);
    setChanged(BOX_ASPECT);
    }
  } // end SelectionImpl.setBoxSelection

  private void setChanged(int aspect) {
    setChanged();
    changeAspect |= aspect;
  }

  /**
   * The fourth bit of changedAspect will be set.
   */
  public synchronized void setLineSelection(int x1, int y1, int x2, int y2) {
    int width = x2 - x1;
    int height = y2 - y1;
    if((box.x != x1) || (box.y != y1) || (box.width != width)
       || (box.height != height)) {    
      line.reshape(x1, y1, x2 - x1, y2 - y1);
      setChanged(LINE_ASPECT);
    }
  }

  /**
   * The fourth bit of changedAspect will be set.
   */
  public void setLineSelection(Line line) {
    setLineSelection(line.x, line.y, line.x + line.width,
		     line.y + line.height);
  }

  /**
   * The first bit of changedAspect will be set.
   */
  public synchronized void setMousePosition(int x, int y) {
    if((mouse.x != x) || (mouse.y != y)) {
      mouse.move(x, y);
      setChanged(MOUSE_ASPECT);
    }
  }

  /**
   * The first bit of changedAspect will be set.
   */
  public void setMousePosition(Point mouse) {
    setMousePosition(mouse.x, mouse.y);
  }

  // use synchronized to prevent only set x.  If there is another
  // thread runing.  If that thread call getPixelSelection
  // the results maight be one that only x updated.
  /**
   * The  second bit of changedAspect will be set.
   */
  public synchronized void setPixelSelection(int x, int y) {
    if((pixel.x != x) || (pixel.y != y)) {
      pixel.move(x, y);
      setChanged(PIXEL_ASPECT);
    }
  }

  /**
   * The  second bit of changedAspect will be set.
   */
  public void setPixelSelection(Point pixel) {
    setPixelSelection(pixel.x, pixel.y);
  }

}
