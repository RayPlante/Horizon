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
* ZoomControl.java - A GUI to control the Cursor Setting.
*
* Modification history:
*    19-Mar-1997 Ray Plante     Initial version.
*    21-Aug-1997 Wei Xie        Second version.
*    06-Dec-1997 Ray Plante     moved from depricated control package
*                                 to the module package
*    08-Dec-1997 Ray Plante     changed zoomOverPoint() to make better
*                                 use of available display area
*/

package ncsa.horizon.modules;

import java.awt.*;
import java.awt.image.*;
import java.applet.Applet;
import java.util.Vector;
import java.awt.Dimension;
import ncsa.horizon.awt.Separator;
import ncsa.horizon.awt.LayoutRearrangeable;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.viewer.SelectionViewer;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.Voxel;

/**
 * ZoomControl is a control panel.  It provides graphical interfaces
 * for user to set zoom parameters and to send control commands
 * to a SelectionViewer.
 */

public class ZoomControl extends Panel implements LayoutRearrangeable {
  protected SelectionViewer viewer=null;

  /**
   * The title Label with "Zoom Control" string
   */
  protected Label title;

  /**
   * The label with "Zoom over selected Point" string
   */
  protected Label zpLabel;

  /**
   * The button with "Zoom" string which is below
   * "Zoom over selected Point" Label in the default layout.<BR>
   * This button triggers a zoom-over-point action when pressed.
   */
  protected Button zoomOverPointButton;

  /**
   * The button labeled "unZoom" which is below
   * "Zoom over selected Point" Label.<BR>
   * This button triggers a unzoom-over-point action when pressed.
   * "unzoom" means zoom with reversed zoom factor.  The zoom factor
   * is shown in the customZm TextField.
   * @see #customZm
   */
  protected Button unZoomOverPointButton;

  /**
   * The Choice for choose 
   * Zoom over selected Point factor
   */
  protected Choice zpChoice;

  /**
   * The TextField for user to
   * type in custom zooming factor and
   * show the current zooming factor.
   */
  protected TextField customZm;

  /**
   * The "Zoom to Box" button
   */
  protected Button ztbButton;
  /**
   * The "Zoom Back" Button
   */
  protected Button zoomBackButton;

  // latest slice to be zoom back
  private Slice slice;

  // latest selected slice
  private Slice sliceSelection;

  // latest selected voxel
  private Voxel voxelSelection;

  // latest custom selected zoom factor, default is 5
  double customFactor = 5.0;

  public ZoomControl() {
    init();
    // make sure every component is instantiated, because
    // this method(ZoomControl) is always called as default
    // by a subclass. We don't want layoutComponents() be called
    // before some new component is instantiated.
    if(getClass().getName().compareTo
       ("ncsa.horizon.modules.ZoomControl") == 0)
      layoutComponents();
  }

  public ZoomControl(SelectionViewer viewer) {
    this.viewer = viewer;
    init();
    if(getClass().getName().compareTo
       ("ncsa.horizon.modules.ZoomControl") == 0)
      layoutComponents();
  }

  private void init() {
    setFont(new Font("Helvetica", Font.PLAIN, 14));
    Font titleFont =  new Font("Helvetica", Font.BOLD, 16);
    Font bold = new Font("Helvetica", Font.BOLD, 14);

    // title "Zoom Control"
    title = new Label("Zoom Control");
    title.setFont(titleFont);

    // Zoom over point Label
    zpLabel = new Label("  Zoom over Selected Point");
    zpLabel.setFont(bold);

    // Zoom over point button
    zoomOverPointButton = new Button("Zoom");

    // Unzoom over point button
    unZoomOverPointButton = new Button("Unzoom");

    // Point zoom selection:
    zpChoice = new Choice();
    zpChoice.addItem("x 1");
    zpChoice.addItem("x 2");
    zpChoice.addItem("x 3");
    zpChoice.addItem("x 4");
    zpChoice.addItem("Custom");
    zpChoice.select(0);

    // Point zoom factor TextField
    customZm = new TextField("" + getCurrentZoom(), 3);
    customZm.setEditable(false);

    // Zoom to region Button
    ztbButton = new Button("Zoom to Box");

    // Zoom back Button
    zoomBackButton = new Button("Zoom Back");
    zoomBackButton.disable();
  } // end init

  public boolean action(Event evt, Object obj) {
    boolean handled = super.action(evt, obj);

    if(evt.target == zpChoice) {
      String choice = (String) obj;
      if(choice.equals("Custom")) {
	customZm.setText("" + customFactor);
        customZm.setEditable(true);
      }
      else {
	customZm.setEditable(false);
	customZm.setText("" + getCurrentZoom());
      }
    }
    else if(evt.target == customZm) {
      Double d = new Double(customZm.getText());
      customFactor = d.doubleValue();
    }
    else if (evt.target == ztbButton) {
      zoomToBox();
    }
    else if (evt.target == zoomOverPointButton) {
      zoomOverPoint();
    }
    else if (evt.target == unZoomOverPointButton) {
      unzoomOverPoint();
    }
    else if (evt.target == zoomBackButton) {
      zoomBack();
    }
    else
      return handled;
	
    return true;
  } // end action

  /**
   * get currentZoom factor over selected point.
   * @return current zooming factor over selected point.
   */
  public double getCurrentZoom() {
    int choice_idx = zpChoice.getSelectedIndex();
    if(choice_idx > 3) {
      return customFactor;
    }
    else {
      return 1.0 * (choice_idx+1);
    }
  } // end getCurrentZoom

  public void layoutComponents() {
    layoutComponents(this);
  }

  /**
   * layout those components to Container parent:
   * title, zpLabel, zoomOverPointButton, unZoomOverPointButton, 
   * zpChoice, ztbButton, zoomBackButton
   * @param parent Container above components are layouted
   *
   * here is the source code of default layout.  User can modify 
   * it to get what he/she want. 
   * <pre>
   *  public void layoutComponents(Container parent) 
   *  {
   *    GridBagLayout bag = new GridBagLayout();
   *    GridBagConstraints c = new GridBagConstraints();
   *    parent.setLayout(bag);
   *    //
   *    // layout title
   *    c.gridwidth = GridBagConstraints.REMAINDER;
   *    bag.setConstraints(title, c);
   *    parent.add(title);
   *    //
   *    // layout the horizontal Separator
   *    Separator separator = new Separator();
   *    c.fill = GridBagConstraints.HORIZONTAL;
   *    bag.setConstraints(separator, c);
   *    parent.add(separator);
   *    //
   *    // layout zpLabel
   *    c.fill = GridBagConstraints.NONE;
   *    bag.setConstraints(zpLabel, c);
   *    parent.add(zpLabel);
   *    //
   *    // layout zoomOverPointButton
   *    c.gridwidth = GridBagConstraints.RELATIVE;
   *    bag.setConstraints(zoomOverPointButton, c);
   *    parent.add(zoomOverPointButton);
   *    //
   *    // layout unZoomOverPointButton
   *    c.gridwidth = GridBagConstraints.REMAINDER;
   *    bag.setConstraints(unZoomOverPointButton, c);
   *    parent.add(unZoomOverPointButton);
   *    //
   *    // layout zpChoice
   *    c.gridwidth = GridBagConstraints.RELATIVE;
   *    c.anchor = GridBagConstraints.EAST;
   *    bag.setConstraints(zpChoice, c);
   *    parent.add(zpChoice);
   *    //
   *    // layout customZm
   *    c.gridwidth = GridBagConstraints.REMAINDER;
   *    c.anchor = GridBagConstraints.CENTER;
   *    bag.setConstraints(customZm, c);
   *    parent.add(customZm);
   *    //
   *    // layout next horizontal Separator
   *    c.anchor = GridBagConstraints.CENTER;
   *    c.weightx = 550;
   *    c.weighty = 550;
   *    Separator separator1 = new Separator();
   *    c.fill = GridBagConstraints.HORIZONTAL;
   *    bag.setConstraints(separator1, c);
   *    parent.add(separator1);
   *    //
   *    // layout ztbButton
   *    c.fill = GridBagConstraints.NONE;
   *    bag.setConstraints(ztbButton, c);
   *    parent.add(ztbButton);
   *    //
   *    // layout next horizontal Separator
   *    Separator separator2 = new Separator();
   *    c.fill = GridBagConstraints.HORIZONTAL;
   *    bag.setConstraints(separator2, c);
   *    parent.add(separator2);
   *    //
   *    // layout zoomBackButton
   *    c.fill = GridBagConstraints.NONE;
   *    bag.setConstraints(zoomBackButton, c);
   *    parent.add(zoomBackButton);
   *  }
   * </pre>
   */

  public void layoutComponents(Container parent) {
    // lay out graphic components
    GridBagLayout bag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    parent.setLayout(bag);

    c.gridwidth = GridBagConstraints.REMAINDER;
    bag.setConstraints(title, c);
    parent.add(title);

    Separator separator = new Separator();
    c.fill = GridBagConstraints.HORIZONTAL;
    bag.setConstraints(separator, c);
    parent.add(separator);

    c.fill = GridBagConstraints.NONE;
    bag.setConstraints(zpLabel, c);
    parent.add(zpLabel);

    c.gridwidth = GridBagConstraints.RELATIVE;
    bag.setConstraints(zoomOverPointButton, c);
    parent.add(zoomOverPointButton);

    c.gridwidth = GridBagConstraints.REMAINDER;
    bag.setConstraints(unZoomOverPointButton, c);
    parent.add(unZoomOverPointButton);

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.anchor = GridBagConstraints.EAST;
    bag.setConstraints(zpChoice, c);
    parent.add(zpChoice);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.CENTER;
    bag.setConstraints(customZm, c);
    parent.add(customZm);

    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 550;
    c.weighty = 550;
    Separator separator1 = new Separator();
    c.fill = GridBagConstraints.HORIZONTAL;
    bag.setConstraints(separator1, c);
    parent.add(separator1);

    c.fill = GridBagConstraints.NONE;
    bag.setConstraints(ztbButton, c);
    parent.add(ztbButton);

    Separator separator2 = new Separator();
    c.fill = GridBagConstraints.HORIZONTAL;
    bag.setConstraints(separator2, c);
    parent.add(separator2);

    c.fill = GridBagConstraints.NONE;
    bag.setConstraints(zoomBackButton, c);
    parent.add(zoomBackButton);
  } // end layoutComponents(Container parent)

  /**
   * set currentZoom factor over selected point.
   * @return current zooming factor over selected point.
   */
  public void setCurrentZoom(double zoom) {
    if(zoom <= 0)
      return;

    if((zoom != (int)zoom) || zoom > 4) {
      zpChoice.select("Custom");
      customFactor = zoom;
      customZm.setText("" + customFactor);
      customZm.setEditable(true);
    }

    else {
      zpChoice.select((int)(zoom - 1.0));
      customZm.setText("" + (zpChoice.getSelectedIndex() + 1));
      customZm.setEditable(false);
    }
  } // end setCurrentZoom

  /**
   * Set viewer.  If viewer already exists, detach this
   * ZoomControl from old viewer and attach it to new viewer.
   */
  public void setViewer(SelectionViewer newViewer) {
    viewer = newViewer;
    widgetDefault();
  }

  /**
   * Zoom out with current zooming factor with reference to
   * the selected pixel.
   */
  public void unzoomOverPoint() {
    double zm = 1.0/getCurrentZoom();
    zoomOverPoint(zm);
  }

  private void widgetDefault() {
    zoomBackButton.disable();
    zpChoice.select(0);
    customZm.setText(zpChoice.getItem(0));
  }

  /**
   * Zoom back.  The display will return to the state
   * just before last zooming action.
   */
  public void zoomBack() {
    // ZoomControl is not at state 1, 
    // can't do zoomBack operation
    if(!zoomBackButton.isEnabled())
      return;
    // get the current slice before zoomBack in order to set
    // back the SliceSelection
    zoomToSlice(slice);
    zoomBackButton.disable();
    viewer.setSliceSelection(sliceSelection);
    viewer.setVoxelSelection(voxelSelection);
  }

  /**
   * Zoom in with current zooming factor with reference to
   * the selected pixel.
   */
  public void zoomOverPoint() {
    double zm = getCurrentZoom();
    zoomOverPoint(zm);
  }

  /**
   * Zoom in with given number factor with reference to
   * the selected pixel.
   */
  public void zoomOverPoint(double zm) {
    // initial state 2
    if(viewer == null)
      return;

    Slice cursl = viewer.getViewSlice();
    cursl.makeLengthsPositive();

    // enable zooming back
    slice = (Slice) cursl.clone();
    zoomBackButton.enable();

    sliceSelection = viewer.getSliceSelection();
    voxelSelection = viewer.getVoxelSelection();
    Dimension size = viewer.getDisplaySize();
    int xax = cursl.getXaxis();
    int yax = cursl.getYaxis();

    // get the size with the right zoom factor and which make best
    // use of available display area (without expanding beyond the bounds
    // of the previous slice).
    double xlen = cursl.getLength(xax);
    double ylen = cursl.getLength(yax);
    double saspect = xlen/ylen,
	   daspect = (1.0*size.width)/size.height;

    if(zm != 1.0) {
	if (saspect == daspect) {
	    cursl.setXaxisLength( xlen / zm );
	    cursl.setYaxisLength( ylen / zm );
	}
	else if (saspect > daspect) {
	    saspect /= zm;        //  = (xlen / zm) / ylen
	    xlen /= zm;
	    cursl.setXaxisLength(xlen);
	    if (saspect < daspect) cursl.setYaxisLength( xlen / daspect );
	}
	else {
	    saspect *= zm;        //  = xlen / (ylen / zm)
	    ylen /= zm;
	    cursl.setYaxisLength(ylen);
	    if (saspect > daspect) cursl.setXaxisLength( ylen * daspect );
	}
    }

    cursl.setXaxisLocation(voxelSelection.axisPos(xax) - 
			   cursl.getLength(xax)/2.0);
    cursl.setYaxisLocation(voxelSelection.axisPos(yax) - 
			   cursl.getLength(yax)/2.0);
    zoomToSlice(cursl);
    viewer.setSliceSelection(sliceSelection);
    viewer.setVoxelSelection(voxelSelection);
  } // end zoomOverPoint(double zm)

  /**
   * zoom in to fit the selected box.
   */
  public void zoomToBox() {
    // initial state 2
    if(viewer == null)
      return;

    // enable zooming back
    Slice cursl = viewer.getViewSlice();
    slice =  (Slice) cursl.clone();
    voxelSelection = viewer.getVoxelSelection();

    zoomBackButton.enable();

    sliceSelection = viewer.getSliceSelection();
    zoomToSlice(sliceSelection);
    viewer.setSliceSelection(sliceSelection);
    viewer.setVoxelSelection(voxelSelection);
  }

  /**
   * This method is the core method that I ask viewer(s) to
   * display slice.  Overwrite this method, if you want more
   * viewer to zoom simutaniously.  see testZoomControl3 for
   * example.
   */
  protected void zoomToSlice(Slice slice) {
    viewer.displaySlice(slice);
  }

}

       
