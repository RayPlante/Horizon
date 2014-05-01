/** Cool_Beans.awt.PositionLayout
*/
// See legal.txt for licencing info.  Using this code or includeing a .class
// file derived from this code without first reading the licence is a violation
// of the licence.  Please be kind.  :)

package Cool_Beans.awt;

import java.awt.*;
import java.util.Vector;

public class PositionLayout implements LayoutManager
 {
  private protected Vector dims, comps;
  private protected int xsize, ysize;
  private protected boolean elastic;
  
  public PositionLayout()
   {
    dims = new Vector(0, 1);
    comps = new Vector(0, 1);
    xsize = 0;
    ysize = 0;
    elastic = true;
   };

  public PositionLayout(int nx, int ny)
   {
    dims = new Vector(0, 1);
    comps = new Vector(0, 1);
    xsize = nx;
    ysize = ny;
    elastic = false;
   };
  
  public Dimension setSize(Dimension nSize)
   {
    xsize = nSize.width;
    ysize = nSize.height;
    return nSize;
   };

  public void setSize(int nx, int ny)
   {
    xsize = nx;
    ysize = ny;
   };

  public boolean setElastic()
   {
    return elastic;
   };

  public boolean setElastic(boolean nElastic)
   {
    return (elastic = nElastic);
   };

  public void addLayoutComponent(String name, Component comp)
   {
    int i = name.indexOf(" ");
    if (i == -1)
      return;
    int x = Integer.valueOf(name.substring(0, i), 10).intValue();
    if (x < 0)
      return;
    int y = Integer.valueOf(name.substring(i + 1, name.length()), 10).intValue();
    if (y < 0)
      return;
    dims.addElement(new Dimension(x, y));
    comps.addElement(comp);
   };

  public void removeLayoutComponent(Component comp)
   {
    int i = comps.indexOf(comp);
    try
     {
      comps.removeElementAt(i);
      dims.removeElementAt(i);
     }
    catch (ArrayIndexOutOfBoundsException e)
     {
      System.err.println("Internal Syncronyzation Error :" + e);
     };
    if (elastic)
     {
      xsize = 0;
      ysize = 0;
     };
   };

  public Dimension preferredLayoutSize(Container parent)
   {
    if (elastic)
     {
      xsize = 0;
      ysize = 0;
      int dsize = comps.size();
      Dimension d, l;
      for (int i = 0;  i < dsize; i++)
       {
        d = ((Component) comps.elementAt(i)).preferredSize();
        l = (Dimension) dims.elementAt(i);
        xsize = Math.max(xsize, d.width  + l.width);
        ysize = Math.max(ysize, d.height + l.height);
       }
     };
    Insets insets = parent.insets();
    return new Dimension(xsize + insets.left + insets.right,
                         ysize + insets.top + insets.bottom);
   };

  public Dimension minimumLayoutSize(Container parent)
   {
    if (elastic)
     {
      xsize = 0;
      ysize = 0;
      int dsize = comps.size();
      Dimension d, l;
      for (int i = 0;  i < dsize; i++)
       {
        d = ((Component) comps.elementAt(i)).minimumSize();
        l = (Dimension) dims.elementAt(i);
        xsize = Math.max(xsize, d.width  + l.width);
        ysize = Math.max(ysize, d.height + l.height);
       }
     };
    Insets insets = parent.insets();
    return new Dimension(xsize + insets.left + insets.right,
                         ysize + insets.top + insets.bottom);
   };

  public void layoutContainer(Container comp)
   {
    int count = comps.size();
    Dimension d;
    Component c;
    Insets insets = comp.insets();
    for (int i = 0; i < count; i ++)
     {
      d = (Dimension) dims.elementAt(i);
      c = (Component) comps.elementAt(i);
      d = c.preferredSize();
      c.resize(d.width, d.height);
      d = (Dimension) dims.elementAt(i);
      c.move(d.width + insets.left, d.height + insets.top);
     };
   };

 };



