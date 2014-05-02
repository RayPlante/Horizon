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
*  LabeledField.java  A labeled text field.
*
* Modification history:
*    20-Mar-1997 Wei Xie     Initial version. Based on plutchak's TestCursor class
*/

package ncsa.horizon.awt;

import java.awt.*;

/**
* A labeled text field.
*/
public class LabeledField extends Panel
{

  /**
   * returned by matchEventTarget() if a match is not made to the 
   * input Event target.
   */
  public final static int NOT_MATCHED = 0;

  /**
   * returned by matchEventTarget() if input Event target matches
   * the value text field
   */
  public final static int VALUE_FIELD = 1;

  private Label label;
  TextField value;
  private boolean editable = true;
  private boolean highlighted = false;

  /**
   * Create a new labeled text field.
   * @param l The label for the text field.
   */
  public LabeledField ( String l ) {
    label = new Label( l );
    add( label );

    value = new TextField(5);
    value.setEditable(false);
    this.add( value );
  }

  /**
   * Create a new labeled text field.
   * @param l The label for the text field.
   * @param w The width of the TextField.
   */
  public LabeledField ( String l, int w) {
    label = new Label( l );
    add( label );

    value = new TextField(w);
    value.setEditable(false);
    this.add( value );
  }

  /**
   * Set the textfield sensitive to user input
   */
  public void enable() {
    value.enable();
  }

  /**
   * Set the textfield sensitive or not sensitive
   * to user input.
   * @param b If true, set the textfield sensitive to user
   * input.  Otherwise, set the textfield insensitive to 
   * user input.
   */
  public void enable(boolean b) {
    value.enable(b);
  }

   /**
   * Get the label.
   * @return The label for the text field.
   */
   public String getLabel ( ) {
      return( label.getText() );
   }

   /**
   * Get the text field value.
   * @return The value of the text field.
   */
  public String getValue ( ) {
    return( value.getText() );
  }

  public void highlight() {
    setHighlight(true);
  }

  public boolean isEditable() {
    return value.isEditable();
  }

  public boolean mouseEnter(Event event, int x, int y)
  {
    if(editable)
      value.setEditable(true);
    return false;
  }

  public boolean mouseExit(Event event, int x, int y)
  {
    value.setEditable(false);
    return false;
  }

  /**
   * return an id indicating whether the input event target matches 
   * either the text field or the scrollbar that makes up this component
   * @param Object  the target object, usually the value of Event.target
   * @returns int   one of the following:
   * <blockquote>
   * <ul><li>   NOT_MATCHED:  neither component matches the target
   *     <li>   VALUE_FIELD:  the target matches the value field
   *     <li>   SCROLLBAR:    the target matches the scrollbar </ul>
   */ 
  public int matchEventTarget(Object target) {
    if (target == value) {
      return VALUE_FIELD;
    } else {
      return NOT_MATCHED;
    }
  }

  /**
   * return an id indicating whether the input event target matches 
   * either the text field or the scrollbar that makes up this component
   * @param Event  the event whose target will be checked
   * @returns int   one of the following:
   * <blockquote>
   * <ul><li>   NOT_MATCHED:  neither component matches the target
   *     <li>   VALUE_FIELD:  the target matches the value field
   */ 
  public int matchEventTarget(Event ev) {
    return matchEventTarget(ev.target);
  }

  public void setEditable(boolean b) {
    editable = b;
    value.setEditable(b);
  }

  public void setHighlight(boolean b) {
    highlighted = b;
    Color back = getBackground();
    if(back == null)
      back = Color.white;
    if(b)
      back = back.brighter();
    label.setBackground(back);
  }

  /**
  * Set the label.
  * @param l The label for the text field.
  */
  public void setLabel ( String l ) {
    label.setText( l );
  }

  /**
   * Set the text field value.
   * @param l The value of the text field.
   */
  public void setValue ( String l ) {
    value.setText( l );
  }

  /**
   * Set the text field value.
   * @param n The value of the text field.
   */
  public void setValue(double n) {
    value.setText("" + n);
  }

   /**
   * Set the text field value.
   * @param n The value of the text field.
   */
   public void setValue ( int n ) {
      value.setText( ""+n );
   }

   /**
   * Return a String version of the conmponent.
   * @return The value of the label and text fields.
   */
   public String toString ( ) {
      String s = "LabeledField[ "+label.getText()+","+value.getText()+"]";
      return( s );
   }
}
