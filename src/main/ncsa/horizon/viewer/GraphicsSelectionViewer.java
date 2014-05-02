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
*    14-Jan-1998 Wei Xie     Various overhauling, including moving code to 
*                            various delegate classes and implementation of
*                            Guiedable interface.  Removed "Edit Graphics"
*                            button.
*    14-Jan-1998 Ray Plante  updated default slice support to use 
*                            Viewable.getDefaultSlice; changed role/
*                            implementation of createDefaultSlice() (does not
*                            use Viewable.getData()).  Bug fix: removed 
*                            preferredSize() method.  Allow canvas size to 
*                            expand to fill enclosing container.  Added 
*                            "Free Position" and "Position on Select" 
*                            Checkboxes and "Edit Selections" Button; 
*                            integrated use of these with the Guidable 
*                            interface via boolean showButtons and 
*                            registerGUIWindow().  
*    16-Jan-1998 Wei Xie     added in data value display; other updates
*    19-Jan-1998 Ray Plante  moved to ncsa.horizon.data (from util); fixed 
*                            resize/update bug.
*    23-Jan-1998 Wei Xie     ??
*    02-Feb-1998 Ray Plante  bug fix: pixelMap incorrectly updated after 
*                            resize.
*/
package ncsa.horizon.viewer;

import java.awt.*;
import java.awt.image.*;
import java.util.Observable;

import ncsa.horizon.awt.*;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.util.*;
import ncsa.horizon.data.NdArrayData;
import ncsa.horizon.viewable.Viewable;

/**
 * This Viewer provides an implementation of the methods needed for
 * a SelectionViewer. <p>
 *
 * Programmer's are encouraged to inspect this code for examples of how
 * to implement the various Viewer methods.
 */
public class GraphicsSelectionViewer extends SelectionViewer
implements Guiedable {
  /**
   * the current viewable
   */
  protected Viewable viewable;
  
  /**
   * the last slice requested from the current viewable
   */
  protected Slice slice;

  /** ImageDisplayMap object for converting between display pixels and 
   *  data pixels   */
  protected ImageDisplayMap pixelMap=null;

  /** CoordinateSystem object for converting between data pixels and 
   *  world coordinates */
  protected CoordinateSystem coord=null;

  protected ImageCanvasViewerImpl viewerImpl;

  /** a flag that is true if a new viewable has been attached */
  protected boolean newViewable = false;

  /** A canvas used to display the image and hanle the selection
   * events */
  protected GraphicsSelectionImageCanvas display;

  /** SelectionDatas
   */
  protected SelectionData selection;

  /** A panel displaying current mouse data position and coordinates. */
  protected Panel posPanel;
  protected Label xDataPos, yDataPos, xLabel, yLabel, xCoordPos, yCoordPos;
  protected Panel butPanel;
  protected Checkbox freeposBox  = null;
  protected Checkbox onselectBox = null;
  protected Button   editselsBut = null;
  protected Window   editselsWin = null;

  /** if true, the viewer will include 3 buttons at bottom of viewer */
  private boolean showButtons;

  private boolean freepos;
  private boolean onselect;

  /**
   * default width of display area if not specified in constructor
   */
  public final static int defaultDisplayWidth  = 256;

  /**
   * default height of display area if not specified in constructor
   */
  public final static int defaultDisplayHeight = 256;

  /**
   * the NdArrayData corresponding to current slice.
   * It is used to determine the data value for each pixel.
   */
  protected NdArrayData sliceData;

  /**
   * 
   */
  protected Label valueLabel;

  /**
   * create a viewer with a display of a default size (256 by 256).
   */
  public GraphicsSelectionViewer() {
      this(0, 0, true);
  }

  /**
   * create a viewer with a display of a default size (256 by 256).
   * @param showButtons  if true, the Viewer will contain 3 buttons at the
   *                     bottom of its Panel; see showingButtons() for details
   */
  public GraphicsSelectionViewer(boolean showButtons) {
      this(0, 0, showButtons);
  }

  /**
   * create a viewer with a given display size 
   * @param width width of the display area
   * @param height height of the display area
   */
  public GraphicsSelectionViewer(int width, int height) {
      this(width, height, true);
  } // end GraphicsSelectionViewer:constructor(int, int)

  /**
   * create a viewer with a given display size 
   * @param width        width of the display area
   * @param height       height of the display area
   * @param showButtons  if true, the Viewer will contain 3 buttons at the
   *                     bottom of its Panel; see showingButtons() for details
   */
  public GraphicsSelectionViewer(int width, int height, boolean showButtons) {
    this.showButtons = showButtons;
    constructDisplay(width, height);
    constructVariables();
    layoutComponents();
  } // end GraphicsSelectionViewer:constructor(int, int)

  /**
   * Set up the display area with the requested size.
   * Works only for constructors.
   */
  protected void constructDisplay(int width, int height) {
    if (width <= 0)  width  = defaultDisplayWidth;
    if (height <= 0) height = defaultDisplayHeight;

    display = new GraphicsSelectionImageCanvas(width, height);
    // Set the mode of the ImageCanvas so that images are scaled
    // to fit within the display area (preserving aspect ratio)
    // and place flush against the upper-left corner.  This will
    // keep pixel tracking simple
    display.setMode(display.SIZE_IMAGE_FLUSH);
    display.setBackground(Color.black);
  }

  /**
   * Initialize member variables except display.  This method works only for
   * constructors. It should be called after display has been constructed.
   */
  protected void constructVariables() {
    selection = display.getSelectionData();
    selection.addObserver(this);
    viewerImpl = new ImageCanvasViewerImpl(display);
    pixelMap = new ImageDisplayMap();
    posPanel = new Panel();
    xDataPos = new Label("     0", Label.RIGHT);
    yDataPos = new Label("     0", Label.RIGHT);
    xLabel = new Label("XCoord:   ", Label.LEFT);
    yLabel = new Label("YCoord:   ", Label.LEFT);
    xCoordPos = new Label("                           ", Label.LEFT);
    yCoordPos = new Label("                           ", Label.LEFT);

    if (showButtons) {
	freeposBox  = new Checkbox("Free Position");
	onselectBox = new Checkbox("Position On Select");
	editselsBut = new Button("Edit Selections");
    }
    valueLabel = new Label("                      0", Label.RIGHT);
  }

  /**
   * replace the current Viewable object with a new one; the display 
   * will not be affected until displaySlice() is called.
   */
  public synchronized void addViewable(Viewable data) {
    viewable = data;
    newViewable = true;
    slice = null;
    coord = viewable.getCoordSys();
  } // end GraphicsSelectionViewer.addViewable

  /**
   * Given a concrete viewable, create a slice.  This method
   * is used to create a default slice.
   */
  protected Slice createDefaultSlice(Viewable v) {
      Slice out = null;

      int[] isz = viewable.getSize();
      for(int i=0; i < isz.length; i++) {
	  if (isz[i] <= 0) {
	      System.err.println("Warning: Dataset looks empty. ");
	      return null;
	  }
      }
//      System.out.println("image size: " + isz[0] + " by " + isz[1]);

      out = new Slice(Math.max(isz.length, 2), 0, 1);
      out.setXaxisLength( (isz.length >= 1) ? isz[0] : 1 );
      out.setYaxisLength( (isz.length >= 2) ? isz[1] : 1 );

      return out;
  } // end GraphicsSelectionViewer.createDefaultSlice

  /**
   * Display a default slice of the current Viewable.
   */
  public void displaySlice() {
    displaySlice(null);
  }

  /**
   * display a slice from the current Viewable data, or do nothing if
   * the current Viewable is not set.
   * @param sl slice to be displayed.  if sl== null, same as displaySlice()
   */
  public synchronized void displaySlice(Slice sl) {
      Slice reqsl = null;

      // save a copy of input slice.  Making a copy prevents the object
      // that gave us this slice from updating it while we are trying
      // to use it.
      //
      // (Note that we like to number axes beginning with zero.)
      if (sl != null) reqsl = new Slice(sl);

      // if no slice was given, come up with a default one
      try {
	  if (reqsl == null) reqsl = viewable.getDefaultSlice();

	  // if viewable doesn't provide a default slice, come up with one
	  if (reqsl == null) reqsl = createDefaultSlice(viewable);
	  if (reqsl == null) {
	      System.err.println("Unable to get default slice; " + 
				 "display request aborted");
	      return;
	  }
      } catch (NullPointerException ex) {
	  if (viewable == null) 
	      reqsl = null;
	  else 
	      throw ex;
      }

      // Now that we are sure we have a usable slice; now we will 
      // save it as the current slice being displayed.
      setSlice(reqsl);

      // exit if we do not have a viewable attached
      if (viewable == null) {
	  System.err.println("Warning: no dataset to display; " +
			     "display request aborted.");
	  return;
      }

      if (newViewable) { //state 1 enter state 2 or 3
	// we also need to reset our ImageDisplayMap object that defines
	// the mapping between display and data pixels.
	setPixelmap();
	newViewable = false;
      }

      ///// state 2, 3 entered
      
      if (coord != null) { // state 2
	String[] wlabel = coord.getAxisLabel();
	xLabel.setText(wlabel[slice.getXaxis()] + ": ");
	yLabel.setText(wlabel[slice.getYaxis()] + ": ");
      }
      // this is just a new slice from an already attached Viewable;
      // thus, we need only update the pixel mapper.
      updatePixelmap();
      displaySlicebyImpl();
      setSliceData(slice);
  } // end GraphicsSelectionViewer.displaySlice(Slice sl)

  /**
   * Delegate displaySlice action to viewerImpl.
   */
  protected void displaySlicebyImpl() {
    viewerImpl.displaySlice(viewable, slice);
  }

  /**
   * get the current selected display box.
   */
  public Rectangle getBoxSelection() { 
    return selection.getBoxSelection(); 
  } //end GraphicsSelectionViewer.getBoxSelection

  /**
   * This method returns the size in display pixel units of the region 
   * that displays a Viewable
   * @return Dimension of the compoonent
   * @see java.awt.Dimension
   * @see java.awt.Component.size()
   */
  public Dimension getDisplaySize() {
    return viewerImpl.getDisplaySize();
  } //end GraphicsSelectionViewer.getDisplaySize

  /**
   * Return the Selection Editing Panel associated with this panel.  (This 
   * method is part of the Guiedable interface.)  The panel controls are 
   * laid out in CardPanel.  <p>
   *
   * If this Viewer was set to show buttons at the bottom of the viewer
   * (which is the default behavior)--that is,showingButtons() returns 
   * true, it is recommended that the caller of this method also call 
   * registerGUIWindow().  This will prevent the Viewer from displaying the
   * Editing Panel in its own window.  It is not necessary to call 
   * registerGUIWindow() if this Viewer was told at construction not to 
   * show the buttons (via GraphicsSelectionViewer(boolean) or 
   * GraphicsSelectionViewer(int, int, boolean)). <p>
   *
   * @returns Component[]  an array of length 1 where the first (and only)
   *                       element is the Selection Editing Panel.
   */
  public Component[] getGui() {
    Component[] cmp = new Component[1];
    cmp[0] = display.getGui()[0];
    return cmp;
  }

  /**
   * This method tells the Viewer that another object will be managing the 
   * Selection editing panel, returned by the getGui() method.  This will 
   * prevent the Viewer from popping up its own Frame containing the panel.
   * If there is already a window registered and is visible, that window 
   * is disposed of, so that only one window will hold panel at any given 
   * time.  
   * @param container  the top-level container that will hold the Selecition
   *                   editing panel.
   * @returns true if the frame currently holding the panel was visible 
   *               when this method was called.  In such a case, the caller
   *               may want to redisplay the panel in its own Frame.
   */
  public synchronized boolean registerGUIWindow(Window container) {
      boolean visible = false;
      if (editselsWin != null) {
	  visible = editselsWin.isVisible();
	  editselsWin.dispose();
      }
	  
      editselsWin = container;
      return visible;
  }

  /**
   * get the current selected display Line.
   */
  public Rectangle getLineSelection() { 
    return selection.getLineSelection(); 
  } //end GraphicsSelectionViewer.getLineSelection

  /**
   * get the current selected display pixel.
   */
  public Point getPixelSelection() {
    return selection.getPixelSelection();
  } //end GraphicsSelectionViewer.getPixelSelection

  /**
   * return the current selected Slice, or null if there is no current
   * Viewable;
   */
  public Slice getSliceSelection() {
    if (viewable == null)
       return null;
    
    updatePixelmap();

    // convert the last selected box to a data region
    return pixelMap.getDataSlice(getBoxSelection());
  } //end GraphicsSelectionViewer.getSliceSelection

  /**
   * Return a reference to the current Viewable object, or null if 
   * none are attached to this Viewer.
   * @return The current Viewable object; null if none present.
   */
  public Viewable getViewable() {
    return viewable;
  } // end GraphicsSelectionViewer.getViewable

  /**
   * return a Slice object describing the data currently being viewed, 
   * or null if there is no Viewable currently being viewed.
   */
  public Slice getViewSlice() {
    return (slice == null) ? null : (Slice) slice.clone();
  } // end GraphicsSelectionViewer.getViewSlice

  /**
   * return the current selected Voxel, or null if there is no current 
   * Viewable.
   */
  public Voxel getVoxelSelection() {
    Voxel out;

    updatePixelmap();

    // convert the last selected display pixel to a data pixel
    return pixelMap.getDataVoxel(getPixelSelection());
  }  // end GraphicsSelectionViewer.getVoxelSelection

  /**
   * Return a rectangle that describes where an image of a particular
   * size will be placed.
   */
  protected Rectangle imagePlacement(int width, int height) {
    Rectangle out = new Rectangle(0, 0, width, height);
    Dimension disp = size();

    return out;
  } //end GraphicsSelectionViewer.imagePlacement

  /**
   * Here is where the awt.compoents are layouted.
   * It was called only by constructors.
   */
  protected void layoutComponents() {

    // Set up the layout
    GridBagConstraints bc = new GridBagConstraints();
    GridBagLayout gbag = new GridBagLayout();
    setLayout(gbag);
//    bc.insets = new Insets(4,4,4,4);
    bc.weightx = bc.weighty = 0.0;
    bc.anchor = bc.NORTHWEST;
    bc.gridx = bc.gridy = 0;
    bc.gridwidth = bc.REMAINDER;
    bc.fill = bc.NONE;
    setFont(new Font("Helvetica", Font.PLAIN, 14));

    gbag.setConstraints(display, bc);
    add(display);

    layoutPosPanel();
    bc.gridy = bc.RELATIVE;
    gbag.setConstraints(posPanel, bc);
    add(posPanel);

    if (showButtons) {
	layoutButtonPanel();
	gbag.setConstraints(butPanel, bc);
	add(butPanel);
    }

  } //end GraphicsSelectionViewer.layoutComponents

  /**
   * Here is how posPanel is layouted.
   * It is called only by layoutComponents methods.
   * @see #layoutComponents
   */
  protected void layoutPosPanel() {
    // Set the position panel
    GridBagLayout bag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    posPanel.setLayout(bag);

    // Data value Label
    Label a1Label = new Label("Data Pixel Value: ", Label.LEFT);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 4;
    c.gridheight = 1;
    c.anchor = c.NORTHWEST;
    bag.setConstraints(a1Label, c);
    posPanel.add(a1Label);
        
    // Data Pixel value
    c.gridx = 4;
    c.gridwidth = 6;
    bag.setConstraints(valueLabel, c);
    posPanel.add(valueLabel);

    // Data Pixel Position Label
    Label aLabel = new Label("Data Pixel: ", Label.LEFT);
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 3;
    c.gridheight = 1;
    c.anchor = c.NORTHWEST;
    bag.setConstraints(aLabel, c);
    posPanel.add(aLabel);
        
    // Data Pixel Position
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 3;
    bag.setConstraints(xDataPos, c);
    posPanel.add(xDataPos);
        
    aLabel = new Label(", ", Label.LEFT);
    c.gridx = 5;
    c.gridwidth = 1;
    bag.setConstraints(aLabel, c);
    posPanel.add(aLabel);
        
    c.gridx = 6;
    c.gridwidth = 2;
    bag.setConstraints(yDataPos, c);
    posPanel.add(yDataPos);
        
    // Coordinate Position Labels
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 3;
    bag.setConstraints(xLabel, c);
    posPanel.add(xLabel);

    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 3;
    bag.setConstraints(yLabel, c);
    posPanel.add(yLabel);

    c.gridx = 3;
    c.gridy = 4;
    c.gridwidth = 6;
    bag.setConstraints(xCoordPos, c);
    posPanel.add(xCoordPos);

    c.gridx = 3;
    c.gridy = 5;
    c.gridwidth = 6;
    bag.setConstraints(yCoordPos, c);
    posPanel.add(yCoordPos);
  }    

  /**
   * layout the panel contain the buttons (actually one Button and 
   * 2 Checkboxes.  This is meant to only be called by layoutComponents()
   * and only if showingButtons() returns true.
   */
  protected void layoutButtonPanel() {
      butPanel = new Panel();
      Font f = getFont();
      butPanel.setFont(new Font(f.getName(), f.getStyle(), 12));
      butPanel.setLayout(new BorderLayout(0, 0));
      butPanel.add("West", editselsBut);
      butPanel.add("Center", freeposBox);
      butPanel.add("East", onselectBox);
  }

  /**
   * set the current selected display box.  The locations are measured in
   * real display (i.e. screen) pixels relative to the upper left hand
   * corner.
   * @param x1,y1  the location of one vertex of the selected box
   * @param x2,y2  the location of the vertex of the selected box opposite 
   *               to the one given by x1,y1
   */
  public void setBoxSelection(int x1, int y1, int x2, int y2) {
    display.setBoxSelection(x1, y1, x2, y2);
  } // end GraphicsSelectionViewer.setBoxSelection

  /**
   * set the current selected display line.  The locations are measured in
   * real display (i.e. screen) pixels relative to the upper left hand
   * corner.
   * @param x1,y1  the location of the start of the line
   * @param x2,y2  the location of the end of the line
   */
  public void setLineSelection(int x1, int y1, int x2, int y2) { 
    display.setLineSelection(x1, y1, x2, y2);
  } // end GraphicsSelectionViewer.setLineSelection

  /**
   * set the current selected display pixel.  The location is measured in
   * real display (i.e. screen) pixels relative to the upper left hand
   * corner.
   */
  public void setPixelSelection(int x, int y) {
     display.setPixelSelection(x, y);
  } // end GraphicsSelectionViewer.setPixelSelection

  /**
   * set the ImageDisplayMap object, pixelMap (used to convert display
   * pixels into data pixels), to reflect changes in the current Viewable
   * It's called at the change from state1 to (state2, state3).
   */
  protected synchronized void setPixelmap() {
    Boolean xaxisReversed=null, yaxisReversed=null;
    if (viewable != null) {
      // the attached viewable may give some hints on how the 
      // data is ordered in the form of "xaxisReversed" and
      // "yaxisReversed" metadata
      // 
      Metadata md = viewable.getMetadata();
      if (md != null) {
	try {
	  xaxisReversed = (Boolean) md.getMetadatum("xaxisReversed");
	} catch (ClassCastException ex) {
	  xaxisReversed = null;
	}
	try {
	  yaxisReversed = (Boolean) md.getMetadatum("yaxisReversed");
	} catch (ClassCastException ex) {
	  yaxisReversed = null;
	}
      }
      
      if (xaxisReversed != null) {
	pixelMap.xaxisReversed = xaxisReversed.booleanValue();
      }
      if (yaxisReversed != null) {
	pixelMap.yaxisReversed = yaxisReversed.booleanValue();
      }
    }
  } // end GraphicsSelectionViewer.setPixelmap

  /**
   * Set current slice.
   */
  protected void setSlice(Slice slice) {
    this.slice = slice;
  } //end GraphicsSelectionViewer.setSlice

  protected void setSliceData(Slice slice) {
    // try to catch a NullPointerException, see if viewable is null
    // if it is, then this is called in initial state 
    try {
      sliceData = viewable.getData(slice);
    } catch (NullPointerException e) {
      if (viewable == null) {
        System.err.println("GraphicsSelectionViewer.displaySlice(Slice sl): addViewable first before call this method.\n");
      } else {
        throw e;
      }
    }
  }

  /**
   * set the current selected Slice to the given Volume as projected onto 
   * the currently displayed Slice, or do nothing if there is no current 
   * Viewable.
   */
  public synchronized void setSliceSelection(Volume vol) {
    // initial state and state 1
    if (getViewable() == null || newViewable)
      return;

    // project the specified Volume onto the current slice
    Slice roiSlice = slice.projection(vol);
    display.setSliceSelection(roiSlice, pixelMap);
  } //end GraphicsSelectionViewer.setSliceSelection

  /** 
   * set the current selected Voxel to the one given as projected onto 
   * the currently displayed Slice, or do nothing if there is no current 
   * Viewable.
   */
  public synchronized void setVoxelSelection(Voxel vox) {
    // initial state or state 1
    if (viewable == null || newViewable)
      return;
    
    display.setVoxelSelection(vox, pixelMap);
  } //end GraphicsSelectionViewer.setVoxelSelection

  public void update(Observable o, Object arg) {
    if (o instanceof SelectionData) {
      int changeAspect = ((Integer) arg).intValue();
      if ((changeAspect & SelectionData.MOUSE_ASPECT) != 0) {
	  if (! onselect) {
	      Point mouse = selection.getMousePosition();
	      updatePosDisplay(mouse.x, mouse.y);
	  }
      }
      if ((changeAspect & SelectionData.PIXEL_ASPECT) != 0) {
	  Point pixel = selection.getPixelSelection();
	  display.setPixelSelection(pixel);
	  updatePosDisplay(pixel.x, pixel.y);
      }
      if ((changeAspect & SelectionData.BOX_ASPECT) != 0) {
	display.setBoxSelection(selection.getBoxSelection());
      }
      if ((changeAspect & SelectionData.LINE_ASPECT) != 0) {
	display.setLineSelection(selection.getLineSelection());
      }
    }
  }

  /**
   * update Data Lable.
   */
  protected void updateDataLabel(double x, double y) {
    xDataPos.setText(" " + x);
    yDataPos.setText(" " + y);
    updateDataValueLabel(x, y);
  } //end GraphicsSelectionViewer.updateDataLabel

  /**
   * update valueLable
   */
  protected void updateDataValueLabel(double x, double y) {
    double[] loc = slice.getLocation();
    int[] intLoc = ArrayTypeConverter.arrayDoubleToInt(loc);
    int xaxis = slice.getXaxis();
    int yaxis = slice.getYaxis();
    intLoc[xaxis] = (int) x;
    intLoc[yaxis] = (int) y;
    Object aValue = null;
    Number valueWapper = null;
    try {
      aValue = sliceData.getValue(intLoc);
      valueWapper = (Number) aValue;
      valueLabel.setText(" " + valueWapper.doubleValue());
    } catch (NullPointerException e) {
      if (sliceData == null) {
        // System.err.println("Viewable does not return a sound data set");
        valueLabel.setText(" unKnown");
      } else if (aValue == null) {
	valueLabel.setText(" out of range");
      } else {
        throw e;
      }
    } catch (ClassCastException e) {
      if (!(aValue instanceof Number)) {
        // System.err.println("Data can't be displayed");
        valueLabel.setText(" unprintable");
      } else {
        throw e;
      }
    }
  } //end GraphicsSelectionDataViewer.updateDataValueLabel

  /**
   * update the ImageDisplayMap object, pixelMap (used to convert display
   * pixels into data pixels), to reflect changes in the currently viewed
   * slice.
   * This method should be called only in state 2 or state 3.
   */
  protected synchronized void updatePixelmap() { 
      updatePixelmap(slice, null); 
  }

  /**
   * update the ImageDisplayMap object, pixelMap (used to convert display
   * pixels into data pixels), to reflect changes in the currently viewed
   * slice.
   * @param dataSlice      the slice to be displayed;
   * @param displayRegion  the region of the display being used; if null,
   *                       assume that as much of the display as possible
   *                       will be used.
   */
  protected synchronized void updatePixelmap(Slice dataSlice, 
                                             Rectangle displayRegion) {
    int dwd, dht;

    if (displayRegion == null) {
      dwd = dataSlice.getTrueLength(slice.getXaxis());
      dht = dataSlice.getTrueLength(slice.getYaxis());
      // this is how much space an image with an unscaled size of 
      // wd x ht will take up on the screen
      //
      Dimension dispdim = display.viewSize(dwd, dht);
      displayRegion = new Rectangle(0, 0, dispdim.width, dispdim.height);
    }

    pixelMap.setSlice(dataSlice);
    pixelMap.setDisplay(displayRegion);
  } //end GraphicsSelectionViewer.updatePixelmap

  /**
   * update the ImageDisplayMap object, pixelMap (used to convert display
   * pixels into data pixels), to reflect changes in the currently viewed
   * slice.
   */
  protected void updatePosDisplay(int x, int y) {
    // initial state and state 1
    if (getViewable() == null || newViewable)
      return;

    updatePosDisplay1(x, y);
  }

  /**
   * updatePosDisplay without check its current state.
   * This method is used by updatePosDisplay only.
   */
  void updatePosDisplay1(int x, int y) {
    // First translate the display pixel to a data pixel;
    Voxel dvox = pixelMap.getDataVoxel(new Point(x, y));
    
    // display the selected data pixel.  Here we will display the
    // the selection as an integer.
    double dxpos = Math.floor(pixelMap.getXDataPos(dvox));
    double dypos = Math.floor(pixelMap.getYDataPos(dvox));
    updateDataLabel(dxpos, dypos);
    
    int xax = slice.getXaxis();
    int yax = slice.getYaxis();

    // snap to whole pixel position if necessary
    if (! freepos) {
	dvox.setAxisPos(xax, Math.floor(dvox.axisPos(xax)));
	dvox.setAxisPos(yax, Math.floor(dvox.axisPos(yax)));
    }

    // Now translate the data pixels to its coordinates
    // construct an array representing the current data voxel.
    // (Note: we could have alternatively used a Voxel to hold
    // this information.)
    if (coord != null) { //state 2

      // Now convert to a coordinate position and display it.
      try {
        CoordPos cpos = coord.getCoordPos(dvox);
        updateWorldlabel(cpos.valueString(xax), cpos.valueString(yax));
      }
      catch(Exception e) {
        String undef = "Undefined";
        updateWorldlabel(undef, undef);
      }
    }
  } //end GraphicsSelectionViewer.updatePosDisplay

  /**
   * update worldlabel.
   * It should be called only at state4.
   */
  protected void updateWorldlabel(String x, String y) {
    xCoordPos.setText(x);
    yCoordPos.setText(y);
  } // end GraphicsSelectionViewer.updateWorldlabel

  /**
   * set the size of the display area
   */
  public synchronized void setDisplaySize(int width, int height) {
      int dwd, dht;
      Dimension old = size();
      if (width != old.width || height != old.height) {
	  display.setPreferredSize(width, height);
	  if (pixelMap != null) {
	      Rectangle dispreg = pixelMap.getDisplay();
	      Slice datsl = pixelMap.getSlice();
	      dwd = datsl.getTrueLength(datsl.getXaxis());
	      dht = datsl.getTrueLength(datsl.getYaxis());
	      Dimension dim = display.viewSize(dwd, dht);
	      dispreg.width = dim.width;
	      dispreg.height = dim.height;
	      pixelMap.setDisplay(dispreg);
	  }
	  display.resize(width, height);
      }
  }

  /**
   * set the size of the display area
   */
  public void setDisplaySize(Dimension sz) {
      setDisplaySize(sz.width, sz.height);
  }

  /**
   * resize the viewer so that the display area takes up as much of
   * the available space as possible.
   */
  public synchronized void reshape(int x, int y, int width, int height) {
      Dimension psz=null;
      Dimension bsz=null;
      Dimension old = size();

      if (old.width > 0 && old.height > 0 &&
	  (width != old.width || height != old.height)) 
      {
	  psz = posPanel.size();
	  if (psz.width > 0 && psz.height > 0) {

	      // figure out how much height is taken up the position panel
	      // and the row of buttons.  
	      if (showButtons) {
		  bsz = butPanel.size();
		  if (bsz.width > 0 && bsz.height > 0) psz.height += bsz.height;
	      }
	      if (width < 2) width = 2;
	      if (height < psz.height + 2) height = psz.height + 2;

	      // resize the image canvas using the requested height minus
	      // the height need by above mentioned components
	      setDisplaySize(width, height - psz.height);

	      // update the pixelMap for the viewer
	      if (slice != null && pixelMap != null) {
		  Dimension newdim = 
		      display.viewSize(slice.getTrueLength(slice.getXaxis()),
				       slice.getTrueLength(slice.getYaxis()));
		  updatePixelmap(slice, new Rectangle(0, 0, newdim.width,
						      newdim.height));
	      }
	  }
      }
      super.reshape(x, y, width, height);
  }

  /**
   * if this method returns true, the Viewer contains three buttons:
   * <dl>
   *    <dd> Edit Selections
   *    <dt> pop up a Frame that contains a GUI for editing selections
   *         (i.e. points, regions, and lines) and the graphics associated 
   *         with them.  See getGui() method for details.
   *    <dd> Free Position (on/off)
   *    <dt> When this is function is turned on, the coordinates displayed
   *         is associated with fractional pixels.  This toggle calls the
   *         setFreePosition() method.
   *    <dd> Position On Select (on/off)
   *    <dt> When this position is turned on, the coordinates display is only 
   *         updated when a point selection is made (by clicking on with 
   *         the mouse over the image).
   * </dl>
   */
  public boolean showingButtons() {
      return showButtons;
  }

  /**
   * if false, then the display coordinate position will correspond the 
   * center of the data voxel beneath the mouse cursor; if true, the position
   * will correspond to a the fractional voxel position beneath the mouse
   * cursor.
   */
  public boolean positionIsFree() { return freepos; }

  /**
   * set whether displayed coordinate positions correspond to whole or 
   * fractional positions.  If the input is false, then the display 
   * coordinate position will correspond the center of the data voxel 
   * beneath the mouse cursor; if true, the position will correspond to a 
   * the fractional voxel position beneath the mouse cursor.
   */
  public void setFreePosition(boolean doFree) {
      freepos = doFree;
      if (freeposBox != null) freeposBox.setState(doFree);
  }

  /**
   * if true, the coordinate display will only be updated if when a pixel
   * selection has been made; if false, it will be updated whenever the 
   * mouse cursor moves over the image display.
   */
  public boolean positionOnSelect() {
      return onselect;
  }

  /**
   * set whether the coordinate display is updated whenever the mouse cursor
   * moves.  If the input is true, it will be only be updated when a pixel
   * selection has been made (i.e. the user clicks on the image).
   */
  public void setPositionOnSelect(boolean doOnSelect) {
      onselect = doOnSelect;
      if (onselectBox != null) onselectBox.setState(doOnSelect);
  }

  public boolean action(Event evt, Object what) {
      if (showButtons) {
	  if (evt.target == freeposBox) {
	      freepos = freeposBox.getState();
	  }
	  else if (evt.target == onselectBox) {
	      onselect = onselectBox.getState();
	  }
	  else if (evt.target == editselsBut) {
	      synchronized(this) {
		  if (editselsWin == null) createEditSelsWin();
		  if (editselsWin.isVisible()) 
		      editselsWin.hide();
		  else
		      editselsWin.show();
	      }
	  }
      }
      return false;
  }

  private synchronized void createEditSelsWin() {
      SimpleFrame f;
      editselsWin = f = new SimpleFrame();
      f.setKillOnClose();
      f.add("Center", getGui()[0]);
      f.pack();
  }

} // end GraphicsSelectionViewer

