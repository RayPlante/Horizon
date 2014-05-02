
/*
 * @(#)LazyLayout.java         0.1 96/10/29 Kul Bhatt
 *
 * Copyright (c) 1996 Kul Bhatt
 *
 *   This program is distributed freely. You can copy it, use it, and
 *   distribute it at your free will.
 *   Author provides no WARRANTY; Not even the implied warranty of USABILITY
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   Author is NOT responsible for any damages caused by direct or
 *   indirect use of this program. 
 *   
 *   k.bhatt@ieee.org 
 */


package misc;
import java.awt.*;

/**
 *  LazyLayout is a very simple Layout Manager. 
 *  It can layout one component at (0, 0) with its preferred size.
 *  It is not much useful except for scrolling when you do not want
 *  to resize the scrolled component
 */
public class LazyLayout implements LayoutManager
{
  Debug d = new Debug("all");
  Component c;
  
  public  LazyLayout()
  {
  }

  public  void addLayoutComponent(String  name,  Component  comp)
  {
    c = comp;
  }
  
      
  public  void layoutContainer(Container  parent)
  {
    c = parent.getComponent(0);
    d.Trace("Lazy layout() called");
    c.reshape(0, 0, c.preferredSize().width, c.preferredSize().height);
  }
  
  public  Dimension      
  minimumLayoutSize(Container  parent)
  {
    return c.minimumSize();
  }
  
  public  Dimension  
  preferredLayoutSize(Container  parent)
  {
    return c.preferredSize();
  }
  
  public  void       
  removeLayoutComponent(Component  comp)
  {
  }
  
}

