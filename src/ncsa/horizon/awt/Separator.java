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
package ncsa.horizon.awt;

import java.awt.*;

/**
 * A separator that is drawn either vertically or horizontally 
 * depending upon how it is laid out.  Can be drawn either 
 * etched-in or etched-out.  Etching are settable at construction time 
 * only.<p>
 *
 * Default etching is ETCHIN.<p>
 *
 * @version 1.0, May 16 1997
 * @author  Wei Xie
 */
public class Separator extends Canvas
{
  static final public int ETCHIN   = 0;
  static final public int ETCHOUT   = 1;
  static private int thickness = 2;
  
  private int etch = ETCHIN;
  
  public Separator()
  {
    this(true);
  }

  /**
   * @param in true the seperator will be draw as etching in,
   *           false - the seperator will be draw as etching out
   */
  public Separator(boolean in)
  {
    etch = in ? ETCHIN : ETCHOUT;
    resize(thickness, thickness);
  }

  public Dimension minimumSize()
  {
    return preferredSize();
  }

  public Dimension preferredSize()
  {
    return new Dimension(thickness, thickness);
  }

  public void paint(Graphics g)
  {
    Dimension size     = size();
    Color brighter = getBackground().brighter().brighter();
    Color darker   = getBackground().darker().darker();
    
    if(etch == ETCHIN)
    {
      if(size.width > size.height)
	paintHorizontal(g, size, darker, brighter);
      else
	paintVertical(g, size, darker, brighter);
    }
    else
    {
      if(size.width > size.height)
	paintHorizontal(g, size, brighter, darker);
      else
	paintVertical(g, size, brighter, darker);
    }
  }

  private void paintHorizontal(Graphics g, Dimension size, 
			       Color top, Color bottom)
  {
    g.setColor(top);
    g.fillRect(0, (size.height/2) - (thickness/2), 
	       size.width, thickness/2);
    g.setColor(bottom);
    g.fillRect(0, size.height/2, size.width, thickness/2); 
  }
  private void paintVertical(Graphics g, Dimension size, 
			     Color left, Color right)
  {
    g.setColor(left);
    g.fillRect((size.width/2) - (thickness/2), 
	       0, thickness/2, size.height);
    g.setColor(right);
    g.fillRect(size.width/2, 0, thickness/2, size.height);
  }
}
