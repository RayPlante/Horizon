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
* LineSelectionSettingPanel.java - A GUI to control the LineSelection Settings.
*
* Modification history:
*    20-Mar-1997 Wei Xie     Initial version.
*    28-May-1997 Wei Xie     Second version.
*    06-Dec-1997 Ray Plante  moved from awt to modules package; removed
*                              references to protected fields of 
*                              LabeledField and IntScrollPanel
*/
package ncsa.horizon.modules;

import ncsa.horizon.awt.*;
import java.awt.*;

/**
 * This class provide a graphic interface to control the 
 * ncsa.horizon.awt.LineSelection. <BR>
 * It is implemented as a panel with Checkbox, ncsa.horizon.
 * awt.LabeledFields, Choices, and ncsa.horizon.awt.IntScrollPanels
 * to make LineSelection visible or invisible,
 * set LineSelection color, position, delta_x, delta_y, 
 * and thickness.  The panel will update its position, 
 * width, height text field, and visibility CheckBox as 
 * LineSelection changes position and becomes visible or invisible.
 *
 * @see CursorSettingPanel
 * @see ROISettingPanel
 * @version 0.1 alpha
 * @author Horizon Team, University of Illinois at Urbana-Champaign
 * @author <br>Wei Xie <weixie@lai.ncsa.uiuc.edu>
 */
public class LineSelectionSettingPanel extends Panel implements LayoutRearrangeable {

  /**
   *The LineSelection to be controlled by this 
   * LineSelectionSettingPanel.
   */
  protected LineSelection ls;

  /**
   *The component to which ls associated.
   */
  protected Component lsMaster; // the component ls associated to

  /**
   * Label with "LineSelection Graphics" string
   */
  Label titleLabel;

  /**
   * A Checkbox used to control and show ls's current visible state.
   */
  protected Checkbox showBox;

  /**
   * Label with "ROI Postition and Size" string
   */
  protected Label rpLabel;

  /**
   * Label with "Color" string
   */
  protected Label clLabel;

  /**
   * LabeledField to set and show ls's current position, width,
   * and height.
   */
  protected LabeledField xField, yField, delta_xField, delta_yField;

  /**
   * Choice to set ls's Color
   */
  protected Choice colorChoice;

  /**
   * An array used to save colors which are corresponding to the color
   * name strings of colorChoice.
   */
  protected Color colorArray[];

  /**
   * An IntScrollPanel to control ls thickness
   */
  protected IntScrollPanel tkScroll;

  /**
  * Create a LineSelectionSettingPanel.
  * @param cursor   The LineSelection to be controlled by 
  *                 this LineSelectionSettingPanel.
  * @param c        The Component to which the cursor is associated.
  */
  public LineSelectionSettingPanel(LineSelection ls, Component c)
  {
    this.ls = ls;
    lsMaster = c;

    init();
    if(getClass().getName().compareTo(
	      "ncsa.horizon.modules.LineSelectionSettingPanel") == 0)
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
        if(ls != null)
          ls.show( ((Boolean)obj).booleanValue() );
      }
    }

    else if (ev.target instanceof Choice) { // handle Choice menu
      Choice c = (Choice)ev.target;
      int index = c.getSelectedIndex();
      if(c == colorChoice) {
        if(ls != null)
          ls.setColor(colorArray[index]);
      }
    }

    else if(ev.target instanceof TextField) {
      TextField t = (TextField) ev.target;
      if(xField.matchEventTarget(ev.target) == xField.VALUE_FIELD) 
        ls.x = Integer.parseInt(xField.getValue());
      else if(yField.matchEventTarget(ev.target) == yField.VALUE_FIELD) 
        ls.y = Integer.parseInt(yField.getValue());
      else if(delta_xField.matchEventTarget(ev.target) == xField.VALUE_FIELD) 
        ls.width = Integer.parseInt(delta_xField.getValue());
      else if(delta_yField.matchEventTarget(ev.target) == yField.VALUE_FIELD) 
        ls.height = Integer.parseInt(delta_yField.getValue());
      else
        ls.setThickness(tkScroll.getValue());
    }
    repaintLsmaster();
    return false;
  }  // end action

  /**
   * Get the Color from colorChoice's current value
   * @return the current Color of colorChoice which is LineSelection's drawing color
   */
  public Color getColorchoice() {
    int index = colorChoice.getSelectedIndex();
    return colorArray[index];
  }

  /**
   * Get delta_xField value which is meant to be the LineSelection's x component.
   * @return current value of delta_xField
   */
  public int getDelta_xfield()
  {
    return Integer.parseInt(delta_xField.getValue());
  }

  /**
   * Get delta_yField value which is meant to be the LineSelection's y component.
   * @return current value of delta_yField
   */
  public int getDelta_yfield()
  {
    return Integer.parseInt(delta_yField.getValue());
  }

  /**
   * Get the showBox's current state, it represents the visibility of
   * the LineSelection.
   * @return true if the LineSelection is visible, false if the
   * LineSelection is hiden.
   */
  public boolean getShowbox()
  {
    return showBox.getState();
  }

  /**
   * Get xField value which is meant to be the x coordinate
   * of the LineSelection's start point.
   * @return current value of xField
   */
  public int getXfield()
  {
    return Integer.parseInt(xField.getValue());
  }

  /**
   * Get yField value which is meant to be the y coordinate
   * of the LineSelection's start point.
   * @return current value of yField
   */
  public int getYfield()
  {
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
          ls.setThickness(tkScroll.getValue());
	  repaintLsmaster();
      }
    }
    return super.handleEvent(evt);
  } // handleEvent

  protected void init() {
    Font titleFont = new Font("Helvatica", Font.BOLD, 16);
    /* initialize all the graphic member components */
    titleLabel = new Label("LineSelection Graphics");
    titleLabel.setFont(titleFont);
    rpLabel = new Label("ROI Postition and Size");
    showBox = new Checkbox( "Show LineSelection", null, false);
    clLabel = new Label("Color");
    xField =  new LabeledField("X: " );
    yField =  new LabeledField("Y: " );
    delta_xField =  new LabeledField("dx: " );
    delta_yField =  new LabeledField("dy: " );
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


    if(ls != null)
    {
      showBox.setState(ls.isVisible());
      setColorchoice(ls.getColor());
      setFields(ls.x, ls.y, ls.width, ls.height);
      setScroll(ls.getThickness());
    }
  }

  public void layoutComponents()
  {
    layoutComponents(this);
  }

  /**
   * Layout Components on Container parent.
   * This is the implementation of LayoutRearrangeable
   * interface. <BR>
   * Components have been instantiated and to be layouted
   * here.  They are: titleLabel, showBox, clLabel, 
   * colorChoice, rpLabel, xField, delta_xField, 
   * yField, delta_yField, and tkScroll. <BR>
   * @param parent the Container on which Components
   * are layouted <p>
   * Here is the source code of default layout.
   * <pre>
   * public void layoutComponents(Container parent)
   * {
   *   GridBagLayout gbl = new GridBagLayout();
   *   setLayout(gbl);
   *   GridBagConstraints gbc = new GridBagConstraints();
   *   //
   *   // Layout titleLabel
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(titleLabel, gbc);
   *   parent.add(titleLabel);
   *   //
   *   // Layout showBox
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbl.setConstraints(showBox, gbc);
   *   parent.add(showBox);
   *   //
   *   // Layout clLabel
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(clLabel, gbc);
   *   parent.add(clLabel);
   *   //
   *   // Layout colorChoice
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbc.ipady = 10;
   *   // emptyLabel is used to take some space, so that
   *   // colorChoice would not cover rpLabel
   *   Label emptyLabel = new Label(" ");
   *   gbl.setConstraints(emptyLabel, gbc);
   *   parent.add(emptyLabel);
   *   gbc.ipady = 0;
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbc.anchor = GridBagConstraints.NORTHEAST;
   *   gbc.ipadx = 12;
   *   gbl.setConstraints(colorChoice, gbc);
   *   parent.add(colorChoice);
   *   //
   *   // Layout rpLabel
   *   gbc.ipadx = 0;
   *   gbc.anchor = GridBagConstraints.CENTER;
   *   gbl.setConstraints(rpLabel, gbc);
   *   parent.add(rpLabel);
   *   //
   *   // Layout xField
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbl.setConstraints(xField, gbc);
   *   parent.add(xField);
   *   //
   *   // Layout delta_xField
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(delta_xField, gbc);
   *   parent.add(delta_xField);
   *   //
   *   // Layout yField
   *   gbc.gridwidth = GridBagConstraints.RELATIVE;
   *   gbl.setConstraints(yField, gbc);
   *   parent.add(yField);
   *   //
   *   // Layout delta_yField
   *   gbc.gridwidth = GridBagConstraints.REMAINDER;
   *   gbl.setConstraints(delta_yField, gbc);
   *   parent.add(delta_yField);
   *   //
   *   // Layout tkScroll
   *   gbc.ipady = 10;
   *   gbl.setConstraints(tkScroll, gbc);
   *   parent.add(tkScroll);
   * }
   * </pre>
   */
  public void layoutComponents(Container parent)
  {
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    //
    // Layout titleLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(titleLabel, gbc);
    parent.add(titleLabel);
    //
    // Layout showBox
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(showBox, gbc);
    parent.add(showBox);
    //
    // Layout clLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(clLabel, gbc);
    parent.add(clLabel);
    //
    // Layout colorChoice
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbc.ipady = 10;
    // emptyLabel is used to take some space, so that
    // colorChoice would not cover rpLabel
    Label emptyLabel = new Label(" ");
    gbl.setConstraints(emptyLabel, gbc);
    parent.add(emptyLabel);
    gbc.ipady = 0;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.ipadx = 12;
    gbl.setConstraints(colorChoice, gbc);
    parent.add(colorChoice);
    //
    // Layout rpLabel
    gbc.ipadx = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    gbl.setConstraints(rpLabel, gbc);
    parent.add(rpLabel);
    //
    // Layout xField
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(xField, gbc);
    parent.add(xField);
    //
    // Layout delta_xField
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(delta_xField, gbc);
    parent.add(delta_xField);
    //
    // Layout yField
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(yField, gbc);
    parent.add(yField);
    //
    // Layout delta_yField
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(delta_yField, gbc);
    parent.add(delta_yField);
    //
    // Layout tkScroll
    gbc.ipady = 10;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbl.setConstraints(tkScroll, gbc);
    parent.add(tkScroll);
  }

  /** 
   * The lsMaster is updated by calling its repaint() method
   */
  private void repaintLsmaster() {
    lsMaster.repaint();
  }

  /**
   * Set a Color to colorChoice's current value
   * @param color Following value acceptable:(default Color.red)
   * Color.red, Color.green, Color.blue, Color.cyan,
   * Color.magenta, Color.yellow, Color.black,
   * Color.white, Color.gray
   */
  public void setColorchoice(Color color)
  {
    colorChoice.select(0);
    if(ls != null)
      ls.setColor(colorArray[0]);
    for(int i = 0; i < colorArray.length; i++)
    {
      if(colorArray[i] == color)
      {
        colorChoice.select(i);
        if(ls != null)
          ls.setColor(colorArray[i]);
        return;
      }
    }
    return;
  } // end setColorchoice

  /**
   * Set xField, yField, delta_xField, and delta_xField which are meant to be the 
   * LineSelection's start point, x component, and y component. <p>
   *
   * LineSelectionSettingPanel also call the LineSelection to update its 
   * start point, x component, and y component
   *
   * @param x  x coordinate of the LineSelection's start point with respect to 
   *           the left top of lsMaster
   * @param y  y coordinate of the LineSelection's start point with respect to 
   *           the left top of lsMaster
   * @param dx the LineSelection's x component
   * @param dy the LineSelection's y component
   */
  public void setFields(int x, int y, int dx, int dy)
  {
    setXfield(x);
    setYfield(y);
    setDelta_xfield(dx);
    setDelta_yfield(dy);
  }

  /**
   * Set delta_xField which is meant to be the LineSelection's
   * x component. <p>
   *
   * LineSelectionSettingPanel also call the LineSelection to update its 
   * x component.
   *
   * @param dx the LineSelection's x component
   */
  public void setDelta_xfield(int dx)
  {
    delta_xField.setValue("" + dx);
    if(ls != null)
      ls.width = dx;
  }

  /**
   * Set delta_yField which is meant to be the LineSelection's
   * y component. <p>
   *
   * LineSelectionSettingPanel also call the LineSelection to update its 
   * y component.
   *
   * @param dy the LineSelection's y component
   */
  public void setDelta_yfield(int dy)
  {
    delta_yField.setValue("" + dy);
    if(ls != null)
      ls.height = dy;
  }

  /**
   * Set xField value which is meant to be the x coordinate
   * of the LineSelection's start point.
   * @param x  x coordinate of the LineSelection's start point with respect to 
   *           the left top of lsMaster
   */
  public void setXfield(int x)
  {
    xField.setValue("" + x);
    if(ls != null)
      ls.x = x;
  }

  /**
   * Set yField value which is meant to be the y coordinate
   * of the LineSelection's start point.
   * @param y  y coordinate of the LineSelection's start point with respect to 
   *           the left top of lsMaster
   */
  public void setYfield(int y)
  {
    yField.setValue("" + y);
    if(ls != null)
      ls.y = y;
  }

  /**
   * Set tkScroll to be LineSelection's thickness.
   *
   * @param t  thickness
   */
  public void setScroll(int t)
  {
    tkScroll.setValue(t);
    if(ls != null)
      ls.setThickness(t);
  }

  /**
   * Set showBox to be true or false
   */
  public void setShowbox(boolean b)
  {
    showBox.setState(b);
    if(ls != null)
      ls.show(b);
  }

  /**
   * toggle the showBow true, false state.
   */
  public void toggleShowbox()
  {
    setShowbox(!getShowbox());
  }

}  // end LineSelectionSettingPanel

