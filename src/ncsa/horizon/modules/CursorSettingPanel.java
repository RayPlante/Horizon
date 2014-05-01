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
* CursorSettingPanel.java - A GUI to control the Cursor Setting.
*
* Modification history:
*    19-Mar-1997 Wei Xie     Initial version.
*    06-Dec-1997 Ray Plante  moved from awt to modules package; removed
*                              references to protected fields of 
*                              LabeledField and IntScrollPanel
*/

package ncsa.horizon.modules;

import ncsa.horizon.awt.*;
import java.awt.*;

/**
 * This class provides a graphic interface to control the 
 * ncsa.horizon.awt.Cursor.  It is implemented as a panel with
 * Checkbox, ncsa.horizon.awt.LabeledFields, Choices, and 
 * ncsa.horizon.awt.IntScrollPanels to make Cursor visible or invisible,
 * set Cursor appearence, color, position, width, height, and 
 * thickness.  The panel updates its position text field, 
 * and visibility CheckBox as Cursor 
 * changes position and becomes visible or invisible.
 * @see LineSelectionSettingPanel
 * @see ROISettingPanel
 *
 * @version 0.1 alpha
 * @author Horizon Team, University of Illinois at Urbana-Champaign
 * @author <br>Wei Xie <weixie@lai.ncsa.uiuc.edu>
 */

public class CursorSettingPanel extends Panel implements 
                 LayoutRearrangeable {
  /**
   *The cursor to be controlled by this CursorSettingPanel.
   */
  protected Cursor cursor;

  /**
   * The title label with "Cursor Graphics" string
   */
  protected Label titleLabel;

  /**
   * The component to which cursor associated.
   */
  protected Component cursorMaster; // the component cursor associated to

  /**
   * A Checkbox used to control and show the current cursor visible 
   * state.
   */
  protected Checkbox showBox;

  /**
   * The Label with "Appearance" string
   */
  protected Label apLabel;

  /**
   * The Label with "Cursor Position" string
   */
  protected Label cpLabel;

  /**
   * The Label with "Color" string
   */
  protected Label clLabel;

  /**
   * LabeledField to set and show current cursor position.
   */
  protected LabeledField xField, yField;

  /**
   * Choice to set Color and Style of the cursor
   */
  protected Choice colorChoice, styleChoice;

  /**
   * An array used to save colors which are corresponding to
   * the color name strings of colorChoice.
   * @see colorChoice
   */
  protected Color colorArray[];

  /**
   * The IntScrollPanel to control width, height, and thickness
   */
  protected IntScrollPanel wdScroll, htScroll, tkScroll;

  /**
  * Create a CursorSettingPanel.
  * @param cursor   The cursor to be controlled by this 
  *                 CursorSettingPanel.
  * @param c        The Component which the cursor associated to.
  */
  public CursorSettingPanel(Cursor cursor, Component c) {
    super();
    this.cursor = cursor;
    cursorMaster = c;
    init();
    if(getClass().getName().compareTo(
	"ncsa.horizon.modules.CursorSettingPanel") == 0)
      layoutComponents();
  } // end constructor

  /**
  * Handle GUI actions.  This responds to user input/selection using
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

      if(c == showBox)
        setShowbox(((Boolean)obj).booleanValue() );
    }

    else if (ev.target instanceof Choice) { // handle Choice menu
      Choice c = (Choice)ev.target;
      String choiceString = (String)obj;
      int index = c.getSelectedIndex();
      if(c == styleChoice) {
        if(cursor != null)
          cursor.setStyle( index+1 );
      }
      else if(c == colorChoice) {
        if(cursor != null)
          cursor.setColor( colorArray[index] );
      }
    }

    else if(ev.target instanceof TextField) {
      if(xField.matchEventTarget(ev.target) == xField.VALUE_FIELD) 
        cursor.x = Integer.parseInt(xField.getValue());
      else if(yField.matchEventTarget(ev.target) == yField.VALUE_FIELD) 
        cursor.y = Integer.parseInt(yField.getValue());
      else {
        cursor.resize(wdScroll.getValue(), htScroll.getValue());
        cursor.setThickness(tkScroll.getValue());
      }
    }
    repaintCursormaster();
    return false;
  }  // end action


 /**
   * Get the Color from colorChoice's current value
   */
  public Color getColorchoice() {
    int index = colorChoice.getSelectedIndex();
    return colorArray[index];
  }

  /**
   * Get the showBox's current state, it represents the visibility of
   * the Cursor
   * @return true if the Cursor is visible, false if the Cursor is 
   * hiden
   */
  public boolean getShowbox() {
    return showBox.getState();
  }

  /**
   * Get xField value which is meant to be the x coordinate
   * of the Cursor's current position.
   */
  public int getXfield() {
    return Integer.parseInt(xField.getValue());
  }

  /**
   * Get yField value which is meant to be the y coordinate
   * of the Cursor's current position.
   */
  public int getYfield() {
    return Integer.parseInt(yField.getValue());
  }

  public boolean handleEvent( Event evt ) {
    if((wdScroll.matchEventTarget(evt.target) == wdScroll.SCROLLBAR) ||
       (htScroll.matchEventTarget(evt.target) == htScroll.SCROLLBAR))  {
      switch (evt.id) {
      case Event.SCROLL_LINE_UP:
      case Event.SCROLL_LINE_DOWN:
      case Event.SCROLL_PAGE_UP:
      case Event.SCROLL_PAGE_DOWN:
      case Event.SCROLL_ABSOLUTE:
	cursor.resize(wdScroll.getValue(), htScroll.getValue());
      }
      repaintCursormaster();
    }
    else if(tkScroll.matchEventTarget(evt.target) == tkScroll.SCROLLBAR) {
      switch (evt.id) {
      case Event.SCROLL_LINE_UP:
      case Event.SCROLL_LINE_DOWN:
      case Event.SCROLL_PAGE_UP:
      case Event.SCROLL_PAGE_DOWN:
      case Event.SCROLL_ABSOLUTE:
	cursor.setThickness(tkScroll.getValue());
      }
      repaintCursormaster();
    }
    return super.handleEvent(evt);
  } // handleEvent

  // used only for constructor
  private void init() {
    Font titleFont = new Font("Helvetica", Font.BOLD, 16);
    /* initialize all the graphic member components */
    titleLabel = new Label("Cursor Graphics");
    titleLabel.setFont(titleFont);
    showBox = new Checkbox( "Show Cursor", null, false);
    cpLabel = new Label("Cursor Position");
    apLabel = new Label("Appearance");
    clLabel = new Label("Color");
    xField =  new LabeledField("X: " );
    yField =  new LabeledField("Y: " );
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
    styleChoice = new Choice();
    styleChoice.addItem( "Cross" );
    styleChoice.addItem( "Dot" );
    styleChoice.addItem( "Box" );
    styleChoice.addItem( "Bullseye" );
    styleChoice.addItem( "Open Cross" );
    styleChoice.addItem( "Spanning Cross" );
    wdScroll = new IntScrollPanel("Width:    ");
    htScroll = new IntScrollPanel("Height:   ");
    tkScroll = new IntScrollPanel("Thickness:");

    if(cursor != null) {
      showBox.setState(cursor.isVisible());
      styleChoice.select(cursor.getStyle() - 1);
      setColorchoice(cursor.getColor());
      Point p = cursor.location();
      xField.setValue("" + p.x);
      yField.setValue("" + p.y);
      Dimension d = cursor.size();
      wdScroll.setValue(d.width);
      htScroll.setValue(d.height);
      tkScroll.setValue(cursor.getThickness());
    }
  }

  public void layoutComponents() {
    layoutComponents(this);
  }

  /**
   * This method layout class member Components.
   * The components have been instantiated in the 
   * constructor.  This method is called inside constructor
   * if it is not overwritten. <BR>
   * The member Components are protected.  The Components to be
   * layouted are: <BR>
   * titleLabel, showBox, apLabel, cpLabel, styleChoice,
   * xField, clLabel, yField, colorChoice, wdScroll, htScroll,
   * and, tkScroll. <BR>
   * The default setting source codes are:
   * <pre>
   *   public void layoutComponents(Container parent)
   *   {
   *     // layout the components
   *     GridBagLayout gbl = new GridBagLayout();
   *     setLayout(gbl);
   *     GridBagConstraints gbc = new GridBagConstraints();
   *     //
   *     // layout titleLabel
   *     gbc.gridwidth = GridBagConstraints.REMAINDER;
   *     gbl.setConstraints(titleLabel, gbc);
   *     parent.add(titleLabel);
   *     //
   *     // layout showBox
   *     gbc.gridwidth = GridBagConstraints.RELATIVE;
   *     gbl.setConstraints(showBox, gbc);
   *     parent.add(showBox);
   *     //
   *     // layout apLabel
   *     gbc.gridwidth = GridBagConstraints.REMAINDER;
   *     gbc.anchor = GridBagConstraints.SOUTH;
   *     gbl.setConstraints(apLabel, gbc);
   *     parent.add(apLabel);
   *     gbc.anchor = GridBagConstraints.CENTER;
   *     //
   *     // layout cpLabel
   *     gbc.gridwidth = GridBagConstraints.RELATIVE;
   *     gbc.anchor = GridBagConstraints.SOUTH;
   *     gbl.setConstraints(cpLabel, gbc);
   *     parent.add(cpLabel);
   *     //
   *     // layout styleChoice
   *     gbc.anchor = GridBagConstraints.CENTER;
   *     gbc.gridwidth = GridBagConstraints.REMAINDER;
   *     gbc.ipadx = 12;
   *     gbl.setConstraints(styleChoice, gbc);
   *     parent.add(styleChoice);
   *     gbc.ipadx = 0;
   *     //
   *     // layout xField
   *     gbc.gridwidth = GridBagConstraints.RELATIVE;
   *     gbl.setConstraints(xField, gbc);
   *     parent.add(xField);
   *     //
   *     // layout clLabel
   *     gbc.gridwidth = GridBagConstraints.REMAINDER;
   *     gbc.anchor = GridBagConstraints.SOUTH;
   *     gbl.setConstraints(clLabel, gbc);
   *     parent.add(clLabel);
   *     //
   *     // layout yField
   *     gbc.anchor = GridBagConstraints.CENTER;
   *     gbc.gridwidth = GridBagConstraints.RELATIVE;
   *     gbl.setConstraints(yField, gbc);
   *     parent.add(yField);
   *     //
   *     // layout colorChoice
   *     gbc.gridwidth = GridBagConstraints.REMAINDER;
   *     gbc.anchor = GridBagConstraints.NORTH;
   *     gbl.setConstraints(colorChoice, gbc);
   *     parent.add(colorChoice);
   *     //
   *     // layout wdScroll
   *     gbc.anchor = GridBagConstraints.CENTER;
   *     gbc.ipady = 6;
   *     gbl.setConstraints(wdScroll, gbc);
   *     parent.add(wdScroll);
   *     //
   *     // layout htScroll
   *     gbl.setConstraints(htScroll, gbc);
   *     parent.add(htScroll);
   *     //
   *     // layout tkScroll
   *     gbl.setConstraints(tkScroll, gbc);
   *     parent.add(tkScroll);
   *   }
   * 
   * </pre>
   * @param parent the Container on which the Components are
   * to be layouted.
   * @see #titleLabel
   * @see #showBox
   * @see #apLabel
   * @see #cpLabel
   * @see #styleChoice
   * @see #xField
   * @see #clLabel
   * @see #yField
   * @see #colorChoice
   * @see #wdScroll
   * @see #htScroll
   * @see #tkScroll
   */
  public void layoutComponents(Container parent) {
    // layout the components
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    //
    // layout titleLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbl.setConstraints(titleLabel, gbc);
    parent.add(titleLabel);
    //
    // layout showBox
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(showBox, gbc);
    parent.add(showBox);
    //
    // layout apLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbl.setConstraints(apLabel, gbc);
    parent.add(apLabel);
    gbc.anchor = GridBagConstraints.CENTER;
    //
    // layout cpLabel
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbl.setConstraints(cpLabel, gbc);
    parent.add(cpLabel);
    //
    // layout styleChoice
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.ipadx = 12;
    gbl.setConstraints(styleChoice, gbc);
    parent.add(styleChoice);
    gbc.ipadx = 0;
    //
    // layout xField
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(xField, gbc);
    parent.add(xField);
    //
    // layout clLabel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbl.setConstraints(clLabel, gbc);
    parent.add(clLabel);
    //
    // layout yField
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    gbl.setConstraints(yField, gbc);
    parent.add(yField);
    //
    // layout colorChoice
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.NORTH;
    gbl.setConstraints(colorChoice, gbc);
    parent.add(colorChoice);
    //
    // layout wdScroll
    gbc.anchor = GridBagConstraints.WEST;
    gbc.ipady = 6;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbl.setConstraints(wdScroll, gbc);
    parent.add(wdScroll);
    //
    // layout htScroll
    gbl.setConstraints(htScroll, gbc);
    parent.add(htScroll);
    //
    // layout tkScroll
    gbl.setConstraints(tkScroll, gbc);
    parent.add(tkScroll);
  }

  /** 
   * The cursorMaster is updated by calling its repaint() method
   */
  private void repaintCursormaster() {
    cursorMaster.repaint();
  }

  /**
   * Set a Color to colorChoice's current value
   * @param color Following value acceptable:(default Color.red)
   * Color.red, Color.green, Color.blue, Color.cyan,
   * Color.magenta, Color.yellow, Color.black,
   * Color.white, Color.gray
   */
  public void setColorchoice(Color color) {
    colorChoice.select(0);
    if(cursor != null)
      cursor.setColor(colorArray[0]);
    for(int i = 0; i < colorArray.length; i++) {
      if(colorArray[i] == color) {
        colorChoice.select(i);
        if(cursor != null)
          cursor.setColor(colorArray[i]);
        return;
      }
    }
    return;
  } // end setColorchoice

  /**
   * Set xField and yField with cursor's current position.
   *
   * @param x  x coordinate of current cursor with respect to 
   *           the left top of cursorMaster
   * @param y  y coordinate of current cursor with respect to 
   *           the left top of cursorMaster
   */
  public void setFields(int x, int y) {
    setXfield(x);
    setYfield(y);
  }

  /**
   * Set a Color to colorChoice's current value
   * @param color Following value acceptable:(default Color.red)
   * Color.red, Color.green, Color.blue, Color.cyan,
   * Color.magenta, Color.yellow, Color.black,
   * Color.white, Color.gray
   */
  public void setStylechoice(int style) {
    styleChoice.select(style - 1);
    if(cursor != null)
      cursor.setStyle(style);
  }

  /**
   * Set xField value which is meant to be the x coordinate
   * of the Cursor's current position.
   * @param x  x coordinate of the cursor's position with respect to 
   *           the left top of lsMaster
   */
  public void setXfield(int x) {
    xField.setValue("" + x);
    if(cursor != null)
      cursor.move(x, cursor.y);
  }

  /**
   * Set yField value which is meant to be the y coordinate
   * of the Cursor's current position.
   * @param y  y coordinate of the cursor's position with respect to 
   *           the left top of lsMaster
   */
  public void setYfield(int y) {
    yField.setValue("" + y);
    if(cursor != null)
      cursor.move(cursor.x, y);
  }

  /**
   * Set wdScroll, htScroll and tkScroll to be cursor's width, height, 
   * and thickness.
   *
   * @param w  width
   * @param h  height
   * @param t  thickness
   */
  public void setScrolls(int w, int h, int t) {
    wdScroll.setValue(w);
    htScroll.setValue(h);
    tkScroll.setValue(t);
  }

  /**
   * Set showBox to be true or false
   */
  public void setShowbox(boolean b) {
    showBox.setState(b);
    if(cursor != null)
      cursor.show(b);
  }

  /**
   * toggle the showBow true, false state.
   */
  public void toggleShowbox() {
    setShowbox(!getShowbox());
  }

}  // end CursorSettingPanel
