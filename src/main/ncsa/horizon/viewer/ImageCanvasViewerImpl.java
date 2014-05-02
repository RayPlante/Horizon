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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  97       wx   Original version
 *  97dec08  rlp  bug fix: getDisplaySize now returns size of canvas
 */
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.ColorModel;
import ncsa.horizon.awt.ImageCanvas;
import ncsa.horizon.util.Slice;
import ncsa.horizon.viewable.Viewable;

public class ImageCanvasViewerImpl {

  private ImageCanvas canvas;

  public ImageCanvasViewerImpl(ImageCanvas c) {
    canvas = c;
  }

  public void displaySlice(Viewable v) {
    Image image = v.getView();
    canvas.displayImage(image);
  }

  public void displaySlice(Viewable v, Slice s) {
    Image image = v.getView(s);
    canvas.displayImage(image);
  }

  public void displaySlice(Viewable v, Slice s, ColorModel cm) {
    Image image = v.getView(s, cm, false);
    canvas.displayImage(image);
  }

  public Dimension getDisplaySize() {
    return canvas.size();
  }

}
