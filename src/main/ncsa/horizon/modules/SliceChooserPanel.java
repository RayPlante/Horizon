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
 * SliceChooserPanel.java - Provide a interactive GUI for user to
 *                          specify a slice and redisplay the slice
 *                          with registered viewers.
 *
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    06-Dec-1997 Ray Plante  Moved from awt to modules package
 *    
 */

package ncsa.horizon.modules;

import java.awt.*;
import java.util.StringTokenizer;
import ncsa.horizon.coordinates.CoordinateSystem;
import ncsa.horizon.util.*;
import ncsa.horizon.awt.*;
import ncsa.horizon.viewer.Viewer;

public class SliceChooserPanel extends Panel implements LayoutRearrangeable {
  private int naxes;
  private Viewer viewer;
  // size of every dimension
  private double[] size;
  private double[] loc;

  private Slice slice;

  /**
   * Axis names.
   */
  String[] axesLabel;

  /**
   * Slice Choosing Panel: title label
   */
  protected Label titleLabel;

  /**
   * Volume: label
   */
  protected Label volumeLabel;

  /**
   * Number of Axes labeledlField
   */
  protected LabeledField noaLabeledField;

  /**
   * The List of axis labels.
   */
  protected List axesList;

  /**
   * Location: Label
   */
  protected Label locationLabel;

  /**
   * Array of TextField to display location of
   * the volume
   */
  protected TextField[] volLocTextFields;

  /**
   * Dimension: Label
   */
  protected Label dimensionLabel;

  /**
   * Array of TextField to display dimension of
   * the n-d array
   */
  protected TextField[] dimensionTextFields;

  /**
   * Slice: label
   */
  protected Label sliceLabel;

  /**
   * Set button.  When it is hit, the current
   * slice will be reset with the displayed 
   * attributes.
   */
  protected Button setButton;

  /**
   * Xaxis label
   */
  protected Choice xaxisChoice;

  /**
   * Yaxis lable
   */
  protected Choice yaxisChoice;

  /**
   * Location label
   */
  protected Label sliceLocationLabel;

  /**
   * Array of TextField to display location of
   * current slice or for user to input the n-d 
   * coordinates to set the location of current
   * slice
   */
  protected TextField[] sliceLocTextFields;

  /**
   * X Size: LabeledField
   */
  protected LabeledField xSizeLabeledField;

  /**
   * Y Size: Label
   */
  protected LabeledField ySizeLabeledField;


  public SliceChooserPanel(Volume volume) {
    extractFrom(volume);
    slice = createDefaultSlice();
    constructComponents(null);
    layoutComponents(this);
  }

  public SliceChooserPanel(Volume volume, Slice slice) {
    extractFrom(volume);
    this.slice = slice;
    constructComponents(null);
    layoutComponents(this);
  }

  public SliceChooserPanel(Volume volume, CoordinateSystem coord) {
    extractFrom(volume);
    slice = createDefaultSlice();
    constructComponents(coord);
    layoutComponents(this);
  }

  public SliceChooserPanel(Volume volume, CoordinateSystem coord, Slice slice) {
    extractFrom(volume);
    this.slice = slice;
    constructComponents(coord);
    layoutComponents(this);
  }

  public SliceChooserPanel(int naxes, double[] size) {
    this(naxes, new double[naxes], size);
  }

  public SliceChooserPanel(int naxes, double[] location, double[] size) {
    this.naxes = naxes;
    this.size = new double[naxes];
    System.arraycopy(size, 0, this.size, 0, naxes);
    loc = new double[naxes];
    System.arraycopy(location, 0, loc, 0, naxes);
    slice =  createDefaultSlice();
    constructComponents(null);
    layoutComponents(this);
  }

  public SliceChooserPanel(int naxes, double[] size, CoordinateSystem coord) {
    this(naxes, new double[naxes], size, coord);
  }

  public SliceChooserPanel(int naxes, double[] location,
			   double[] size, CoordinateSystem coord) {
    this.naxes = naxes;
    this.size = new double[naxes];
    System.arraycopy(size, 0, this.size, 0, naxes);
    loc = new double[naxes];
    System.arraycopy(location, 0, loc, 0, naxes);
    slice =  createDefaultSlice();
    constructComponents(coord);
    layoutComponents(this);
  }

  protected void constructAxesLabel(CoordinateSystem coord) {
    axesLabel = new String[naxes];
    if (coord == null) {
      for(int i = 1; i <= naxes; i++) {
	axesLabel[i] = "The number " + i + "axis";
      }
    } else {
      for (int i = 0; i < naxes; i++) {
	axesLabel[i] = coord.getAxisLabel(i);
      }
    }
  }
    
  /**
   * Initialize axesList.
   * This method is used only for contructors.
   */
  protected void constructAxesList() {
    axesList = new List(naxes, false);
    for (int i = 0; i < naxes; i++) {
      axesList.addItem(axesLabel[i]);
    }
  }

  /**
   * Initialize xaxisChoice.
   * This method is used only for contructors.
   */
  protected void constructXaxisChoice() {
    xaxisChoice = new Choice();
    for (int i = 0; i < naxes; i++) {
      xaxisChoice.addItem(axesLabel[i]);
    }
    xaxisChoice.select(0);
  }

  /**
   * Initialize yaxisChoice.
   * This method is used only for contructors.
   */
  protected void constructYaxisChoice() {
    yaxisChoice = new Choice();
    for (int i = 0; i < naxes; i++) {
      yaxisChoice.addItem(axesLabel[i]);
    }
    yaxisChoice.select(1);
  }

  /**
   * Initialize the member graphic interface Components.
   * This method is used only for contructors.
   */
  protected void constructComponents(CoordinateSystem coord) {
    constructAxesLabel(coord);

    Font titleFont = new Font("Helvetica", Font.BOLD, 16);
    Font subtitleFont = new Font("Helvetica", Font.BOLD, 14);
    titleLabel = new Label("Slice Choosing Panel");
    titleLabel.setFont(titleFont);
    volumeLabel = new Label("Data Domain: ");
    volumeLabel.setFont(subtitleFont);
    noaLabeledField = new LabeledField("Number of Axes:");
    noaLabeledField.setEditable(false);
    noaLabeledField.setValue(naxes);

    constructAxesList();

    locationLabel = new Label("Location:    ");

    volLocTextFields = new TextField[naxes];
    for (int i = 0; i < naxes; i++) {
      volLocTextFields[i] = new TextField(5);
      volLocTextFields[i].setText("" + loc[i]);
      volLocTextFields[i].setEditable(false);
    }

    dimensionLabel = new Label("Dimension:");

    dimensionTextFields = new TextField[naxes];
    for (int i = 0; i < naxes; i++) {
      dimensionTextFields[i] = new TextField(5);
      dimensionTextFields[i].setText("" + size[i]);
      dimensionTextFields[i].setEditable(false);
    }

    sliceLabel = new Label("Slice Selection:");
    sliceLabel.setFont(subtitleFont);
    setButton = new Button(" Display Slice ");

    int xaxis = slice.getXaxis();
    int yaxis = slice.getYaxis();
    constructXaxisChoice();
    constructYaxisChoice();

    sliceLocationLabel = new Label("Location");
    sliceLocTextFields = new TextField[naxes];
    for (int i = 0; i < naxes; i++) {
      sliceLocTextFields[i] = new TextField(5);
      sliceLocTextFields[i].setText("" + loc[i]);
    }
      
    xSizeLabeledField = new LabeledField("X Size");
    xSizeLabeledField.setValue(size[xaxis]);
    ySizeLabeledField = new LabeledField("Y Size");
    ySizeLabeledField.setValue(size[yaxis]);

  } // end SliceChooserPanel.init

  public boolean action(Event evt, Object arg) {
    Object tar = evt.target;
    if (tar instanceof List) {
      int select = ((List) tar).getSelectedIndex();
    } else if (tar instanceof Choice) {
      // cho is xaxisChoice or yaxisChoice
      Choice cho = (Choice) tar;
      Choice choCounterpart = getCouterpartChoice(cho);
      int selectionIndex = choCounterpart.getSelectedIndex();
      if (selectionIndex == cho.getSelectedIndex()) {
	int totalChoce = choCounterpart.countItems();
	if (selectionIndex == 0) {
	  choCounterpart.select(totalChoce - 1);
	} else {
	  choCounterpart.select(selectionIndex - 1);
	}
      }
    } else if (tar instanceof Button) {
      if(((Button) tar) == setButton) {
	setSlice();
	if(viewer != null)
	  viewer.displaySlice(slice);
      }
    }
    return super.action(evt, arg);
  }

  private Slice createDefaultSlice() {
    Voxel vox = new Voxel(naxes, loc);
    Dimension dim = new Dimension((int) size[0], (int) size[1]);
    Slice localSlice = new Slice(vox, dim, 0, 1);
    return localSlice;
  } // end SliceChooserPanel.createDefaultSlice()

  private void extractFrom(Volume volume) {
    naxes = volume.getNaxes();
    size = volume.getSize();
    loc = volume.getLocation();
  } // end SliceChooserPanel.extractFrom

  private double[] extractLocation() {
    double[] d = new double[naxes];
    for(int i = 0; i < naxes; i++) {
      String loc = sliceLocTextFields[i].getText();
      d[i] = Double.valueOf(loc).doubleValue();
    }
    return d;
  }

  protected int extractXaxis() {
    return xaxisChoice.getSelectedIndex();
  }

  protected int extractXSize() {
    return Integer.parseInt(xSizeLabeledField.getValue());
  }

  protected int extractYaxis() {
    return yaxisChoice.getSelectedIndex();
  }

  protected int extractYSize() {
    return Integer.parseInt(ySizeLabeledField.getValue());
  }

  protected Choice getCouterpartChoice(Choice choice) {
    if (choice == xaxisChoice) {
      return yaxisChoice;
    } else if (choice == yaxisChoice) {
      return xaxisChoice;
    } else { // this should never happen
      throw new InternalError(getClass().getName() + ".getCouterpartChoice");
    }
  }

  public Slice getSlice() {
    return (Slice) slice.clone();
  }

  public void layoutComponents() {
    layoutComponents(this);
  }

  public void layoutComponents(Container container) {
    GridBagLayout gbl = new GridBagLayout();
    container.setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.weightx = 0;
    gbc.weighty = 0;

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(titleLabel, gbc);
    container.add(titleLabel);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    Separator sep1 = new Separator();
    gbl.setConstraints(sep1, gbc);
    container.add(sep1);

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    gbl.setConstraints(volumeLabel, gbc);
    container.add(volumeLabel);

    gbl.setConstraints(noaLabeledField, gbc);
    container.add(noaLabeledField);

    gbl.setConstraints(axesList, gbc);
    container.add(axesList);

    Panel locationPanel = new Panel();
    locationPanel.add(locationLabel);
    for(int i = 0; i < naxes; i++) {
      locationPanel.add(volLocTextFields[i]);
    }
    gbl.setConstraints(locationPanel, gbc);
    container.add(locationPanel);

    Panel dimensionPanel = new Panel();
    dimensionPanel.add(dimensionLabel);
    for(int i = 0; i < naxes; i++) {
      dimensionPanel.add(dimensionTextFields[i]);
    }
    gbl.setConstraints(dimensionPanel, gbc);
    container.add(dimensionPanel);

    Separator sep2 = new Separator();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbl.setConstraints(sep2, gbc);
    container.add(sep2);

    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(sliceLabel, gbc);
    container.add(sliceLabel);

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(setButton, gbc);
    container.add(setButton);

    Label xaxisLabel = new Label("               Xaxis: ");
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(xaxisLabel, gbc);
    container.add(xaxisLabel);
    
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(xaxisChoice, gbc);
    container.add(xaxisChoice);

    Label yaxisLabel = new Label("               Yaxis: ");
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(yaxisLabel, gbc);
    container.add(yaxisLabel);
    
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(yaxisChoice, gbc);
    container.add(yaxisChoice);

    Panel sliceLocPanel = new Panel();
    sliceLocPanel.add(sliceLocationLabel);
    for(int i = 0; i < naxes; i++) {
      gbl.setConstraints(sliceLocTextFields[i], gbc);
      sliceLocPanel.add(sliceLocTextFields[i]);
    }
    gbl.setConstraints(sliceLocPanel, gbc);
    container.add(sliceLocPanel);
      
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(xSizeLabeledField, gbc);
    container.add(xSizeLabeledField);

    gbl.setConstraints(ySizeLabeledField, gbc);
    container.add(ySizeLabeledField);
  } // end SliceChooserPanel.layoutComponents(Container)

  public void regist(Viewer viewer) {
    this.viewer = viewer;
  }

  private void setSlice() {
    int xaxis = extractXaxis();
    int yaxis = extractYaxis();
    int xSize = extractXSize();
    int ySize = extractYSize();
    Voxel vox = new Voxel(naxes, extractLocation());
    Dimension dim = new Dimension(xSize, ySize);
    slice = new Slice(vox, dim, xaxis, yaxis);
  }

}
