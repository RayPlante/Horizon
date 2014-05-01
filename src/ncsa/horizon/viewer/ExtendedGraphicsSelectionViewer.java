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
 * GraphicsSectionViewer.java - An implementation of SelectionViewer.
 *
 * Modification history:
 *    21-Aug-1997 Wei Xie     Initial version. 
 *    15-Jan-1998 Ray Plante  added extra constructors to allow hiding of 
 *                            button row.   
 */
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.*;
import java.util.Observable;

import ncsa.horizon.awt.*;
import ncsa.horizon.modules.LutSelectionPanel;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.modules.SliceChooserPanel;
import ncsa.horizon.util.*;
import ncsa.horizon.viewable.Viewable;

/**
 * This Viewer provides an implementation of the methods needed for
 * a SelectionViewer. <p>
 *
 * Programmer's are encouraged to inspect this code for examples of how
 * to implement the various Viewer methods.
 */
public class ExtendedGraphicsSelectionViewer extends GraphicsSelectionViewer
    implements Guiedable 
{

  protected LutSelectionPanel lutSelectionPanel;
  protected SimpleFrame chooserFrame;

  /**
   * create a viewer with a display of a default size (256 by 256).
   */
  public ExtendedGraphicsSelectionViewer() {
    this(0, 0);
  }

  /**
   * create a viewer with a display of a default size (256 by 256).
   * @param showButtons  if true, the Viewer will contain 3 buttons at the
   *                     bottom of its Panel; see showingButtons() for details
   */
  public ExtendedGraphicsSelectionViewer(boolean showButtons) {
      super(0, 0, showButtons);
  }

  /**
   * create a viewer with a given display size 
   * @param width width of the display area
   * @param height height of the display area
   */
  public ExtendedGraphicsSelectionViewer(int width, int height) {
    super(width, height);
  } // end ExtendedGraphicsSelectionViewer:constructor(int, int)

  /**
   * create a viewer with a given display size 
   * @param width        width of the display area
   * @param height       height of the display area
   * @param showButtons  if true, the Viewer will contain 3 buttons at the
   *                     bottom of its Panel; see showingButtons() for details
   */
  public ExtendedGraphicsSelectionViewer(int width, int height, 
					 boolean showButtons) 
  {
      super(width, height, showButtons);
  }

  /**
   * Initialize member variables except display.  This method works only for
   * constructors. It should be called after display has been constructed.
   */
  protected void constructVariables() {
    super.constructVariables();
    lutSelectionPanel = new LutSelectionPanel();
    lutSelectionPanel.register(this);
    chooserFrame = new SimpleFrame("SliceChooser");
  }

  /**
   * replace the current Viewable object with a new one; the display 
   * will not be affected until displaySlice() is called.
   */
  public synchronized void addViewable(Viewable data) {
    super.addViewable(data);
    createSliceChooser();
  } // end ExtendedGraphicsSelectionViewer.addViewable

  /**
   * Create a sliceChooser from viewable.  This method is
   * call when current viewable is replaced with new one, ie,
   * when addViewable(Viewable data) is called.
   */
  protected void createSliceChooser() {
    chooserFrame.removeAll();
    Volume volume = viewable.getData().getVolume();
    SliceChooserPanel sliceChooserPanel;
    if (coord == null) {
      sliceChooserPanel = new SliceChooserPanel(volume);
    } else {
      sliceChooserPanel = new SliceChooserPanel(volume, coord);
    }
    sliceChooserPanel.regist(this);
    chooserFrame.add("Center", sliceChooserPanel);
    chooserFrame.pack();
  }

  /**
   * Delegate displaySlice action to viewerImpl.
   */
  protected void displaySlicebyImpl() {
    viewerImpl.displaySlice(viewable, slice, 
			    lutSelectionPanel.currentColorModel());
  }

  /**
   * Return an java.awt.Component array of length 3. <br>
   * First is Graphics setting panels, which are contained
   * in a CardPanel. <br>
   * Second is the SliceChooserPanel. <br>
   * The last is the LutSelectionPanel. 
   */
  public Component[] getGui() {
    Component[] cmp = new Component[3];
    cmp[0] = display.getGui()[0];
    cmp[1] = chooserFrame;
    cmp[2] = lutSelectionPanel;
    return cmp;
  }


} // end ExtendedGraphicsSelectionViewer

