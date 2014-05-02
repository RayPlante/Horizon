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
* ROISettingPanel.java - A GUI to control the ROI Settings.
*
* Modification history:
*    20-Mar-1997 Wei Xie     Initial version.
*    29-May-1997 Wei Xie     Second version.
*    06-Dec-1997 Ray Plante  moved from awt to modules package; removed
*                              references to protected fields of 
*                              LabeledField and IntScrollPanel
*/
package ncsa.horizon.modules;

import ncsa.horizon.awt.*;
import java.awt.*;

/**
 * This class provide a graphic interface to control the 
 * ncsa.horizon.awt.ROI. <BR>
 * It is implemented as a panel with Checkbox, ncsa.horizon.
 * awt.LabeledFields, Choices, and ncsa.horizon.awt.IntScrollPanels
 * to make ROI visible or invisible,
 * set ROI color, position, width, height, and thickness.  The 
 * panel will update its position, width, height text field, 
 * and visibility 
 * CheckBox as ROI changes position and becomes visible or invisible.
 *
 * @see CursorSettingPanel
 * @see LineSelectionSettingPanel
 * @version 0.1 alpha
 * @author Horizon Team, University of Illinois at Urbana-Champaign
 * @author <br>Wei Xie <weixie@lai.ncsa.uiuc.edu>
 */
public class ROISettingPanel extends Panel {
  /**
   *The ROI to be controlled by this ROISettingPanel.
   */
  protected ROI roi;

  /**
   * Label with "Region of Interests Graphics" string
   */
  protected Label titleLabel;

  /**
   * Label with "Color"
   */
  protected Label clLabel;

  /**
   * Label with "ROI Postition and Size"
   */
  protected Label rpLabel;

  /**
   *The component to which roi associated.
   */
  protected Component roiMaster; // the component roi associated to

  /**
   * A Checkbox used to control and show roi's 
   * current visible state. 
   */
  protected Checkbox showBox;

  /**
   * LabeledField to set and show roi's current position, width, 
   * and height.
   */
  protected LabeledField xField, yField, wField, hField;
  /**
   * Choice to set roi's Color
   */
  protected Choice colorChoice;

  /**
   * An array used to save colors which are corresponding 
   * to the color name strings of colorChoice.
   */
  protected Color colorArray[];

  /**
   * An IntScrollPanel to control roi thickness
   */
  protected IntScrollPanel tkScroll;

  /**
  * Create a ROISettingPanel.
  * @param cursor   The ROI to be controlled by this ROISettingPanel.
  * @param c        The Component to which the cursor is associated.
  */
  public ROISettingPanel(ROI roi, Component c) {

    super();
    this.roi = roi;
    roiMaster = c;

    init();
    if(getClass().getName().compareTo(
	 "ncsa.horizon.modules.ROISettingPanel") == 0)
      layoutComponents();
  } // end constructor

  /**
  * Handle applet GUI actions.  This responds to user input/selection using
  * the GUI controls.
  * @param ev The Event to handle.
  * @param obj AN Event-specific object.
  * @return false.
  */
  public boolean action(Event ev, Object obj) {
    int n;
    boolean value;

    if(ev.target instanceof Checkbox) { // handle checkboxes
      Checkbox c = (Checkbox)ev.target;
      
      if(c == showBox) {
        if(roi != null)
          roi.show( ((Boolean)obj).booleanValue() );
      }
    }

    else if (ev.target instanceof Choice) { // handle Choice menu
      Choice c = (Choice)ev.target;
      if(c == colorChoice) {
        if(roi != null)
          roi.setColor(getColorchoice());
      }
    }

    else if(ev.target instanceof TextField) {
      if(xField.matchEventTarget(ev.target) == xField.VALUE_FIELD) 
        roi.x = getXfield();
      else if(yField.matchEventTarget(ev.target) == yField.VALUE_FIELD) 
        roi.y = getYfield();
      else if(wField.matchEventTarget(ev.target) == wField.VALUE_FIELD) 
        roi.width = getWfield();
      else if(hField.matchEventTarget(ev.target) == hField.VALUE_FIELD) 
        roi.height = getHfield();
      else 
        roi.setThickness(tkScroll.getValue());
    }
    repaintRoimaster();
    return false;
  }  // end action

  /**
   * Get the Color from colorChoice's current value.
   * @return The colorChoice's current value which is meant to be the ROI's color
   */
  public Color getColorchoice() {
    int index = colorChoice.getSelectedIndex();
    return colorArray[index];
  }

  /**
   * Get hField value which is meant to be the ROI's height.
   */
  public int getHfield() {
    return Integer.parseInt(hField.getValue());
  }

  /**
   * Get the showBox's current state, it represents the visibility of
   * the ROI
   * @return true if the ROI is visible, false if the ROI is 
   * hiden
   */
  public boolean getShowbox() {
    return showBox.getState();
  }

  /**
   * Get wField value which is meant to be the ROI's width.
   */
  public int getWfield() {
    return Integer.parseInt(wField.getValue());
  }

  /**
   * Get xField value which is meant to be the x coordinate
   * of the ROI's base point.
   */
  public int getXfield() {
    return Integer.parseInt(xField.getValue());
  }

  /**
   * Get yField value which is meant to be the y coordinate
   * of the ROI's base point.
   */
  public int getYfield() {
    return Integer.parseInt(yField.getValue());
  }

  public boolean handleEvent( Event evt ) {
    if(tkScroll.matchEventTarget(evt.target) == tkScroll.SCROLLBAR) {
      switch (evt.id) {
        case Event.SCROLL_LINE_UP:
        case Event.SCROLL_LINE_DOWN:
        case Event.SCROLL_PAGE_UP:
        case Event.SCROLL_PAGE_DOWN:
        case Event.SCROLL_ABSOLUTE:
          roi.setThickness(tkScroll.getValue());
	  repaintRoimaster();
        break;
        default:
          return( super.handleEvent( evt ));
      }
    }
    return super.handleEvent(evt);
  } // handleEvent

  private void init() {
    Font titleFont = new Font("Helvetica", Font.BOLD, 16);
    /* initialize all the graphic member components */
    titleLabel = new Label("Region of Interests Graphics");
    titleLabel.setFont(titleFont);
    showBox = new Checkbox( "Show Region of Interests", null, false);
    rpLabel = new Label("ROI Postition and Size");
    clLabel = new Label("Color");
    xField =  new LabeledField("X: " );
    yField =  new LabeledField("Y: " );
    wField =  new LabeledField("w: " );
    hField =  new LabeledField("h: " );
    colorChoice = new Choice();
    colorArray = new Color[9];
    colorChoice.addItem( "Red" );
    colorArray[0] = Color.red;
    colorChoice.addItem( "Green" );
    colorArray[1] = Color.green;
    colorChoice.addItem( "Blue" );
    colorArray[2] = Color.blue;
    colorChoice.addItem( "Cyan" );
    colorArray[3] = Color.cyan;
    colorChoice.addItem( "Magenta" );
    colorArray[4] = Color.magenta;
    colorChoice.addItem( "Yellow" );
    colorArray[5] = Color.yellow;
    colorChoice.addItem( "Black" );
    colorArray[6] = Color.black;
    colorChoice.addItem( "White" );
    colorArray[7] = Color.white;
    colorChoice.addItem( "Gray" );
    colorArray[8] = Color.gray;
    tkScroll = new IntScrollPanel("Thickness:");

    if(roi != null) {
      showBox.setState(roi.isVisible());
      setColorchoice(roi.getColor());
      setFields(roi.x, roi.y, roi.width, roi.height);
      setScroll(roi.getThickness());
    }
  } // end init

  /**
   * Layout components.
   */
  public void layoutComponents() {
    layoutComponents(this);
  }

  /**
   * Layout components on Container parent.
   * Components have been instantiated and to
   * be layouted here. They are: titleLabel, showBox,
   * clLabel, colorChoice, rpLabel, xField, 
   * wField, yField, hField, and tkScroll <BR>
   * @param parent the Container on which Components
   * are layouted <P>
   * Here is the source code of default layout
   * <PRE>
   * public void layoutComponents(Container parent)
   * {
   *   GridBagLayout gbl = new GridBagLayout();
   *   setLayout(gbl);
   *   GridBagConstraints gbc = new GridBagConstraints();
   *   //
   *   // layout titleLabel
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(titleLabel, gbc);
   *   add(titleLabel);
   *   //
   *   // layout showBox
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbl.setConstraints(showBox, gbc);
   *   add(showBox);
   *   //
   *   // layout clLabel
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(clLabel, gbc);
   *   add(clLabel);
   *   //
   *   // layout colorChoice
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbc.ipady = 10;
   *   // emptyLabel is used to take some space, so that
   *   // colorChoice would not cover rpLabel
   *   Label emptyLabel = new Label(" ");
   *   gbl.setConstraints(emptyLabel, gbc);
   *   add(emptyLabel);
   *   gbc.ipady = 0;
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbc.anchor = GridBagConstraints.NORTHEAST;
   *   gbc.ipadx = 12;
   *   gbl.setConstraints(colorChoice, gbc);
   *   add(colorChoice);
   *   //
   *   // layout rpLabel
   *   gbc.ipadx = 0;
   *   gbc.anchor = GridBagConstraints.CENTER;
   *   gbl.setConstraints(rpLabel, gbc);
   *   add(rpLabel);
   *   //
   *   // layout xField
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbl.setConstraints(xField, gbc);
   *   add(xField);
   *   //
   *   // layout wField
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(wField, gbc);
   *   add(wField);
   *   //
   *   // layout yField
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbl.setConstraints(yField, gbc);
   *   add(yField);
   *   //
   *   // layout hField
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(hField, gbc);
   *   add(hField);
   *   //
   *   // layout tkScroll
   *   gbc.ipady = 10;
   *   gbl.setConstraints(tkScroll, gbc);
   *   add(tkScroll);
   * }
   * </PRE>
   */
  public void layoutComponents(Container parent) {
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    //
    // layout titleLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(titleLabel, gbc);
    add(titleLabel);
    //
    // layout showBox
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(showBox, gbc);
    add(showBox);
    //
    // layout clLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(clLabel, gbc);
    add(clLabel);
    //
    // layout colorChoice
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbc.ipady = 10;
    // emptyLabel is used to take some space, so that
    // colorChoice would not cover rpLabel
    Label emptyLabel = new Label(" ");
    gbl.setConstraints(emptyLabel, gbc);
    add(emptyLabel);
    gbc.ipady = 0;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.ipadx = 12;
    gbl.setConstraints(colorChoice, gbc);
    add(colorChoice);
    //
    // layout rpLabel
    gbc.ipadx = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    gbl.setConstraints(rpLabel, gbc);
    add(rpLabel);
    //
    // layout xField
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(xField, gbc);
    add(xField);
    //
    // layout wField
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(wField, gbc);
    add(wField);
    //
    // layout yField
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(yField, gbc);
    add(yField);
    //
    // layout hField
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(hField, gbc);
    add(hField);
    //
    // layout tkScroll
    gbc.ipady = 10;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbl.setConstraints(tkScroll, gbc);
    add(tkScroll);
  }

  /** 
   * The roiMaster is updated by calling its repaint() method
   */
  private void repaintRoimaster() {
    roiMaster.repaint();
  } // end repaintRoimaster

  /**
   * Set a Color to colorChoice's current value
   * @param color Following value acceptable:(default Color.red)
   * Color.red, Color.green, Color.blue, Color.cyan,
   * Color.magenta, Color.yellow, Color.black,
   * Color.white, Color.gray
   */
  public void setColorchoice(Color color) {
    colorChoice.select(0);
    if(roi != null)
      roi.setColor(colorArray[0]);
    for(int i = 0; i < colorArray.length; i++) {
      if(colorArray[i] == color) {
        colorChoice.select(i);
        if(roi != null)
          roi.setColor(colorArray[i]);
        return;
      }
    }
    return;
  } // end setColorchoice

  /**
   * Set xField, yField, wField, and hField with roi's current position, 
   * width, and height.
   *
   * @param x  x coordinate of base point of current ROI with respect to 
   *           the left top of roiMaster
   * @param y  y coordinate of base point of current ROI with respect to 
   *           the left top of roiMaster
   * @param w  width of roi
   * @param h  height of roi
   */
  public void setFields(int x, int y, int w, int h) {
    setXfield(x);
    setYfield(y);
    setWfield(w);
    setHfield(h);
  }

  /**
   * Set hField value which is meant to be the ROI's height.
   * @param h  height of roi
   */
  public void setHfield(int h) {
    hField.setValue("" + h);
    if(roi != null)
      roi.height = h;
  }

  /**
   * Set wField value which is meant to be the ROI's width.
   * @param w  width of roi
   */
  public void setWfield(int w) {
    wField.setValue("" + w);
    if(roi != null)
      roi.width = w;
  }

  /**
   * Set xField value which is meant to be the x coordinate
   * of the ROI's base point.
   * @param x  x coordinate of base point of current ROI with respect to 
   *           the left top of roiMaster
   */
  public void setXfield(int x) {
    xField.setValue("" + x);
    if(roi != null)
      roi.x = x;
  }

  /**
   * Set yField value which is meant to be the y coordinate
   * of the ROI's base point.
   * @param y  y coordinate of base point of current ROI with respect to 
   *           the left top of roiMaster
   */
  public void setYfield(int y) {
    yField.setValue("" + y);
    if(roi != null)
      roi.y = y;
  }

  /**
   * Set tkScroll to be cursor's thickness.
   *
   * @param t  thickness
   */
  public void setScroll(int t) {
    tkScroll.setValue(t);
    if(roi != null)
      roi.setThickness(t);
  }

  /**
   * Set showBox to be true or false
   */
  public void setShowbox(boolean b) {
    showBox.setState(b);
    if(roi != null)
      roi.show(b);
  }

  /**
   * toggle the showBow true, false state.
   */
  public void toggleShowbox() {
    setShowbox(!getShowbox());
  }

}  // end RoiSettingPanel
