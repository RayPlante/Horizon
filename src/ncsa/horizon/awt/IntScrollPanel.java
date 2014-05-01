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
* IntScrollPanel.java  A labeled and value-displaying horizontal 
* scrollbar. 
*
* Modification history:
*    19-Mar-1997 Wei Xie     Initial version. Based on plutchak's 
*                            TestCursor class
*    06-Dec-1997 Ray Plante  Added matchEventTarget() methods along with
*                              final int values it returns; provides a 
*                              way of responding to events sent to this
*                              component without subclassing it.
*/
package ncsa.horizon.awt;

import java.awt.*;

/**
* This class is a labeled and value-displaying horizontal scrollbar. The value
* is displayed in a TextField which becomes editable or non-editalbe dynamically.
*/

public class IntScrollPanel extends Panel
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

  /**
   * returned by matchEventTarget() if input Event target matches
   * the scrollbar
   */
  public final static int SCROLLBAR = 2;

  protected Scrollbar scroll;
  protected Label label;
  protected TextField value;

  /**
   * Create a scrollbar with label and value fields.
   * The maximum value will be 100.
   * @param l The label for the scrollbar.
   */
  public IntScrollPanel(String l)
  {
    this(100, l);
  }

  /**
   * Create a scrollbar with label and value fields.
   * @param maximum The maximum int value.
   * @param l The label for the scrollbar.
   */
  public IntScrollPanel(int maximum, String l)
  {
    if(maximum < 1)
      maximum = 100;
    GridBagLayout gb = new GridBagLayout();
    setLayout( gb );
    GridBagConstraints gbc = new GridBagConstraints();
    
    // the label
    gbc.fill = GridBagConstraints.NONE; 
    label = new Label(l, Label.RIGHT);
    // Courier letter have same width
    label.setFont(new Font("Courier", Font.PLAIN, 14));
    gb.setConstraints( label, gbc );
    add( label );

    // the scrollbar
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 100;
    gbc.gridx = GridBagConstraints.RELATIVE; 
    gbc.ipadx = 12; 
    scroll = new Scrollbar( Scrollbar.HORIZONTAL, 0, 10, 1, maximum);
    gb.setConstraints( scroll, gbc );
    add( scroll );
    
    // the value
    gbc.fill = GridBagConstraints.NONE;    
    gbc.weightx = 0;
    value = new TextField(3);
    value.setEditable(false);
    gbc.anchor = GridBagConstraints.WEST; 
    gbc.fill = GridBagConstraints.NONE; 
    gb.setConstraints( value, gbc );
    add( value );
  }

  public boolean action(Event ev, Object obj)
  {
    if(ev.target instanceof TextField)
      scroll.setValue(Integer.parseInt(value.getText()));
    return false;
  }


  /**
   * Handle all scrollbar events.  The associated text field is updated
   * on any scrollbar manipulation.  This implementation lets the 
   * superclass
   * handle all non-scroll events.
   * @param evt The Scrollbar event to handle.
   * @return true if event has been handled; false otherwise.
   */
  public boolean handleEvent ( Event evt )
  {
    super.handleEvent(evt);
    switch (evt.id)
    {
      case Event.SCROLL_LINE_UP:
      case Event.SCROLL_LINE_DOWN:
      case Event.SCROLL_PAGE_UP:
      case Event.SCROLL_PAGE_DOWN:
      case Event.SCROLL_ABSOLUTE:
	value.setText( ""+scroll.getValue() );
	break;
      default:
	return( super.handleEvent( evt ));
    }
    
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
    } else if (target == scroll) {
      return SCROLLBAR;
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
   *     <li>   SCROLLBAR:    the target matches the scrollbar </ul>
   */ 
  public int matchEventTarget(Event ev) {
    return matchEventTarget(ev.target);
  }

   /**
   * Get the scrollbar's line increment value.
   * @return The increment value.
   */
   public int getLineIncrement ( ) {
      return( scroll.getLineIncrement() );
   }

   /**
   * Get the scrollbar's maximum value.
   * @return The maximum value.
   */
   public int getMaximum ( ) {
      return( scroll.getMaximum() );
   }

   /**
   * Get the scrollbar's minimum value.
   * @return The minimum value.
   */
   public int getMinimum ( ) {
      return( scroll.getMinimum() );
   }

   /**
   * Get the scrollbar's orientation value.
   * @return The orientation value.
   */
   public int getOrientation ( ) {
      return( scroll.getOrientation() );
   }

   /**
   * Get the scrollbar's page increment value.
   * @return The increment value.
   */
   public int getPageIncrement ( ) {
      return( scroll.getPageIncrement() );
   }

   /**
   * Get the scrollbar's current value.
   * @return The current value.
   */
   public int getValue ( ) {
      return( scroll.getValue() );
   }

   /**
   * Get the scrollbar's visible value.
   * @return The visible value.
   */
   public int getVisible ( ) {
      return( scroll.getVisible() );
   }

  public boolean mouseEnter(Event event, int x, int y)
  {
    value.setEditable(true);
    return false;
  }

  public boolean mouseExit(Event event, int x, int y)
  {
    value.setEditable(false);
    return false;
  }

   /**
   * Set the scrollbar's line increment value.
   * @param l The line increment value.
   */
   public void setLineIncrement ( int  l ) {
      scroll.setLineIncrement( l );
   }

   /**
   * Set the scrollbar's page increment value.
   * @param l The page increment value.
   */
   public void setPageIncrement ( int  l ) {
      scroll.setPageIncrement( l );
   }

   /**
   * Set the scrollbar's value.
   * @param l The value.
   */
   public void setValue ( int  _value ) {
      scroll.setValue( _value );
      value.setText( ""+_value );
   }

   /**
   * Set several of the the scrollbar's values.
   * @param _value The value.
   * @param visible The visibility value.
   * @param minimum The minimum value.
   * @param maximum The maximum value.
   */
   public void setValues ( int  _value, int  visible,
                           int  minimum, int  maximum) {
      scroll.setValues( _value, visible, minimum, maximum );
      value.setText( ""+_value );
   }

   /**
   * Set the label.
   * @param l The label value.
   */
   public void setLabel ( String l ) {
      label.setText( l );
   }

   /**
   * Get the label value.
   * @return The label.
   */
   public String getLabel ( ) {
      return( label.getText() );
   }

   /**
   * Get a string representation of the extended scrollbar values.
   * @return The values of the label and scrollbar.
   */
   public String toString ( ) {
      String s = "IntScrollPanel[ "+label.getText()+"," + 
	value.getText()+"]";
      return( s );
   }

  /**
   * Make sure all components get resized.
   */
  public void reshape ( int  x, int  y, int  width, int height )
  {
    scroll.resize( width - label.size().width - value.size().width,
		   scroll.size().height );
    super.reshape( x, y, width, height );
   }
}
