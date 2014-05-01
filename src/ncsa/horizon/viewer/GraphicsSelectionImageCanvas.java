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

import java.awt.*;
import ncsa.horizon.awt.ImageCanvas;
import ncsa.horizon.awt.Guiedable;
import ncsa.horizon.awt.Line;
import ncsa.horizon.util.*;
/**
 * I function as a SelectionArea which listens to mouse moving
 * and selection events.  I keep a reference to SelectionData and update
 * it if new mouse events come.  The obervers of the SelectionData
 * will act upon being notified.  <p>
 * I am a decorator of GraphcisSelectionImpl.  So, I implement all the
 * methods of GraphcisSelectionImpl.
 * Also, I can share the same SelectionData with other SelectionArea.
 * But, I am not a SelectionData observer.  So, my SelectionData is altered
 * by other SelectionArea, some SelectionData observer such as a 
 * SelectionViewershould notify me, ie, it should call my setBoxSelection
 * method and etc.
 */
public class GraphicsSelectionImageCanvas extends ImageCanvas
implements Guiedable {
  private GraphicsSelectionImpl selectionImpl;
  private SelectionData selection;

  
  public GraphicsSelectionImageCanvas(int width, int height) {
    super(width, height);
    selection = new SelectionData();
    selectionImpl = new GraphicsSelectionImpl(this);

    setMode(SIZE_IMAGE_SCALE);
  } // end GraphicsSelectionCanvas:constructor
  
  public Component[] getGui() {
    return selectionImpl.getGui();
  }
  
  public SelectionData getSelectionData() {
    return selection;
  }

  public Rectangle getBoxSelection() {
    return selection.getBoxSelection();
  }

  public Line getLineSelection() {
    return selection.getLineSelection();
  }

  public Point getPixelSelection() {
    return selection.getPixelSelection();
  }

  public boolean isBoxSelectionVisible() {
    return selectionImpl.isBoxSelectionVisible();
  }

  public boolean isLineSelectionVisible() {
    return selectionImpl.isLineSelectionVisible();
  }

  public boolean isPixelSelectionVisible() {
    return selectionImpl.isPixelSelectionVisible();
  }

  /**
   * process selection requests
   */
  public boolean mouseDown(Event evt, int x, int y) {
    if((evt.modifiers & Event.CTRL_MASK) != 0) {
      // Control-button was pushed: toggle selection graphics
      if ((evt.modifiers & Event.META_MASK) != 0) {
        // Control-Right toggle box 
        selectionImpl.toggleBoxSelection();
      }
      else if((evt.modifiers & (Event.ALT_MASK|Event.SHIFT_MASK)) != 0) {
        // Control-Middle toggle line
        selectionImpl.toggleLineSelection();
      }
      else {
        // Control-Lef toggle pixel 
        selectionImpl.togglePixelSelection();
      }
    }
    else {
      if((evt.modifiers & Event.META_MASK) != 0) {
	// the right button was pushed: start box selection
	selectionImpl.showBoxSelection(true);
	selection.setBoxSelection(x, y, x + 1, y + 1);
      }
      if ((evt.modifiers & (Event.ALT_MASK|Event.SHIFT_MASK)) != 0) {
	// the middle button was pushed: start a line selection
	selection.setLineSelection(x, y, x + 1, y + 1);
	selectionImpl.showLineSelection(true);
      }
      if((evt.modifiers & 
	  (Event.ALT_MASK|Event.META_MASK|Event.SHIFT_MASK)) == 0) {
	// the left button was pushed: select a pixel
	selection.setPixelSelection(x, y);
	selectionImpl.showPixelSelection(true);
      }
    }
    selection.notifyObservers();
    repaint();
    return false;
  } //end GraphicsSelectionCanvas.mouseDown

  /**
   * process a box selection request
   */
  public boolean mouseDrag(Event evt, int x, int y) {
    if((evt.modifiers & Event.META_MASK) != 0) {
      Rectangle rec = selection.getBoxSelection();
      selection.setBoxSelection(rec.x, rec.y, x, y);
    }
    if ((evt.modifiers & (Event.ALT_MASK|Event.SHIFT_MASK)) != 0) {
      // the middle button was pushed
      Point start = selection.getLineSelection().startPoint();
      selection.setLineSelection(start.x, start.y, x, y);
    }
    if ((evt.modifiers & 
             (Event.ALT_MASK|Event.META_MASK|Event.SHIFT_MASK)) == 0) {
      // the left button is being dragged: move the selected pixel
      selection.setPixelSelection(x, y);
    }
    selection.notifyObservers();
    repaint();
    return true;
  } // end GraphicsSelectionCanvas.mouseDrag

  public boolean mouseMove(Event event, int x, int y) {
    selection.setMousePosition(x, y);
    selection.notifyObservers();
    return true;
  } // end GraphicsSelectionCanvas.mouseMove

  /**
   * mouse position changed.
   */
  public boolean mouseUp(Event event, int x, int y) {
    selection.setMousePosition(x, y);
    selection.notifyObservers();
    return true;
  } // end GraphicsSelectionCanvas.mouseUp
	       
  /**
   */
  public void setBoxSelection(int x1, int y1, int x2, int y2) {
    selectionImpl.setBoxSelection(x1, y1, x2, y2);
    selection.setBoxSelection(x1, y1, x2, y2);
  }

  /**
   */
  public void setBoxSelection(Rectangle box) {
    selectionImpl.setBoxSelection(box);
    selection.setBoxSelection(box);
  }

  public void setLineSelection(int x1, int y1, int x2, int y2) {
    selectionImpl.setLineSelection(x1, y1, x2, y2);
    selection.setLineSelection(x1, y1, x2, y2);
  }

  public void setLineSelection(Line line) {
    selectionImpl.setLineSelection(line);
    selection.setLineSelection(line);
  }

  public void setPixelSelection(int x, int y) {
    selectionImpl.setPixelSelection(x, y);
    selection.setPixelSelection(x, y);
  }

  public void setPixelSelection(Point pixel) {
    selectionImpl.setPixelSelection(pixel);
    selection.setPixelSelection(pixel);
  }

  public void setSliceSelection(Slice slice, ImageDisplayMap pixelMap) {
    selectionImpl.setSliceSelection(slice, pixelMap);
    Rectangle box = selectionImpl.getBoxSelection();
    selection.setBoxSelection(box.x, box.y, box.x + box.width,
			      box.y + box.height);
  }

  public synchronized void setVoxelSelection(Voxel voxel,
                                             ImageDisplayMap pixelMap) {
    selectionImpl.setVoxelSelection(voxel, pixelMap);
    Point pixel = selectionImpl.getPixelSelection();
    selection.setPixelSelection(pixel.x, pixel.y);
  }

  public void showBoxSelection(boolean b) {
    selectionImpl.showBoxSelection(b);
  }

  public void showLineSelection(boolean b) {
    selectionImpl.showLineSelection(b);
  }

  public void showPixelSelection(boolean b) {
    selectionImpl.showPixelSelection(b);
  }

  public void toggleBoxSelection() {
    selectionImpl.toggleBoxSelection();
  }

  public void toggleLineSelection() {
    selectionImpl.toggleLineSelection();
  }

  public void togglePixelSelection() {
    selectionImpl.togglePixelSelection();
  }

  /**
   * Redraw.
   */
  protected boolean tryPaint( Graphics g ) {
    boolean flag;
    flag = super.tryPaint(g);
    selectionImpl.draw(g);
    selection.setPixelSelection(selectionImpl.getPixelSelection());
    selection.setBoxSelection(selectionImpl.getBoxSelection());
    selection.setLineSelection(selectionImpl.getLineSelection());
    selection.notifyObservers();
    return flag;
  } // end GraphicsSelectionCanvas.tryPaint

}
