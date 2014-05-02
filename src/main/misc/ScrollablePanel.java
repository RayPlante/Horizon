
/*
 * @(#)ScrollablePanel.java         0.1 96/10/29 Kul Bhatt
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
 *   ScrollablePanel - is a scrollable Panel!
 *   It allows you to scroll any arbitrary component including images
 *   and other containers.
 *
 *   It creates two scroll bars around the component to be scrolled.
 *
 *
 *             ---------
 *             |        ||
 *             |        ||
 *             | Comp   ||Scrollbar
 *             |        ||
 *             |        ||
 *             |        ||
 *             _________
 *             ---------
 *              Scrollbar
 *
 *   The ScrollablePanel is resizable.
 *
 *   How to use ScrollablePanel?
 *   Create a  Scrollable Panel with
 *             new ScrollablePanel(Component scrolledComponent)
 *   add this to your container as usual.
 */


public class ScrollablePanel extends Panel implements LayoutManager
{
  Panel viewPort;
  Scrollbar vbar;
  Scrollbar hbar;

  Component scrolledComponent;
  
  Debug d = new Debug("");
  /**
   *  ScrollablePanel(Component scrolledComponent)
   *      creates a Scrollable  Panel that can scroll scrolledComponent
   */
  public ScrollablePanel(Component scrolledComponent)
  {
    Debug.GlobalDebug("all");
    this.scrolledComponent = scrolledComponent;
    viewPort = new Panel();
    hbar = new Scrollbar(Scrollbar.HORIZONTAL);
    vbar = new Scrollbar(Scrollbar.VERTICAL);
    setLayout(this);
    add("Not used", vbar);
    add("Not", hbar);
    add("Not", viewPort);
    viewPort.setLayout(new LazyLayout());
    viewPort.add("scroll!", scrolledComponent);
  }

  public  void addLayoutComponent(String  name,  Component  comp)
  {
  }
  
      
  public  void layoutContainer(Container  parent)
  {
    d.Trace("Scroller layout() called");
    Insets insets = parent.insets();
    Dimension parentSize = parent.size();
    int top = insets.top;
    int left = insets.left;
    int bottom = parentSize.height - insets.bottom;
    int right = parentSize.width - insets.right;

    d.Detail("layoutContainer(parent) left=" + left + " top=" + top +
	     " right=" + right + " bottom=" + bottom);
    Dimension pv = vbar.preferredSize();
    Dimension ph = hbar.preferredSize();
    
    d.Internal("Parent size =" + parentSize);
    d.Internal("vbar Dimension=" + vbar.size() + "preferred=" + vbar.preferredSize());
    
    vbar.reshape(right - pv.width, top, pv.width, bottom - top - ph.height);
    hbar.reshape(left, bottom - ph.height,  right - left - pv.width,
		 ph.height);
    viewPort.reshape(left, top, right -  vbar.size().width, 
		     bottom- hbar.size().height);

    d.Detail( "Viewport Location=" + viewPort.location() + " size=" +
	      viewPort.size());
    d.Internal("Resized vbar Dimension=" + vbar.size());
    d.Internal("Resized hbar Dimension=" + hbar.size());

    manageScrollbars();
 
  }
  
  public  Dimension      
  minimumLayoutSize(Container  parent)
  {
    Dimension d = new Dimension(scrolledComponent.minimumSize());
    d.height +=  hbar.preferredSize().height;
    d.width +=  vbar.preferredSize().width;
    return d;    
  }
  
  public  Dimension  
  preferredLayoutSize(Container  parent)
  {
    d.Trace("scrolledComponent.preferredSize() =" + 
	    scrolledComponent.preferredSize());
    Dimension d = new Dimension(scrolledComponent.preferredSize());
    d.height +=  hbar.preferredSize().height;
    d.width +=  vbar.preferredSize().width;
    return d; 

  }
  
  public  void       
  removeLayoutComponent(Component  comp)
  {
  }

  private void 
  manageScrollbars()
  {
    Dimension v = vbar.size();
    Dimension h = hbar.size();
    Dimension sc = scrolledComponent.size();
    d.Trace("manageScrollbars() sc=" + sc);
    
    d.Trace("vbar lineIncr=" + vbar.getLineIncrement() + " page=" + 
	    vbar.getPageIncrement());
    
    vbar.setValues(0, v.height, 0, sc.height - v.height);
    hbar.setValues(0, h.width, 0, sc.width - h.width);
    hbar.setLineIncrement( sc.width/12);
    hbar.setPageIncrement( sc.width/4);
    vbar.setLineIncrement( sc.height/12);
    vbar.setPageIncrement( sc.height/4);
  }


  
  public Dimension preferredSize()
  {
    Dimension d = new Dimension(scrolledComponent.preferredSize());
    d.height +=  hbar.preferredSize().height;
    d.width +=  vbar.preferredSize().width;
    return d;
  }
  public Dimension minimumSize()
  {
    return preferredSize();
  }

  public boolean handleEvent(Event event)
  {
    switch (event.id){
    case Event.SCROLL_LINE_UP:
    case Event.SCROLL_LINE_DOWN:
    case Event.SCROLL_PAGE_UP:
    case Event.SCROLL_PAGE_DOWN:
    case Event.SCROLL_ABSOLUTE:
      scroll();
    }
    return super.handleEvent(event);
  }
  public void scroll()
  {
    scrolledComponent.move(-hbar.getValue(), -vbar.getValue());
  }
  static public void main(String[] args)
  {
    Frame f = new Frame("Kul Bhatt's Scroller");
    f.setLayout(new GridLayout(0,1)); 
    Panel p = new Panel();
    //    p.setBackground(Color.black);
    //    p.setForeground(Color.white);
    
    p.setLayout(new GridLayout(0,2));
   for ( int i=0; i < 60; i++){
      String a = new String( "This is a Button, It does nothing #" + i );

      //      Label l = new Label(a);
      Button l = new Button(a);
      l.setBackground(Color.red);
      l.setForeground(Color.white);
      p.add( l);
    }
   Dimension d = p.preferredSize();
    int h = 400;
    int w = 400;
    h = Math.max(h, d.height);
    w = Math.max(w, d.width);
    
    p.resize(w, h);

    ScrollablePanel sp = new ScrollablePanel(p);
    f.add(sp);
    f.pack();
    f.show();
  }
  
}

  
    





