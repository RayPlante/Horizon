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
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 */

package ncsa.horizon.awt;

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.NoSuchElementException;

// used only for CardPanel class
// every method keep some state: currentTab is
// always in the last row
class TabCanvas extends Canvas {
  static final int ROWHEIGHT = 25;
  private final int INITIALWIDTH = 200;

  Vector tabRows;
  Tab currentTab = null;
  // used to denote if the tabs need to be repositioned.
  // This happens when new tab is added or old tab is removed
  // The actual work will be delayed at paint time since
  // only when at that time the real size of this TabCanvas
  // can be decided.

  private boolean needReposition;
  // The number of row which currentTab is in should be.
  // The currentTab is always to be put in the last row.
  // But it should be in this number as added order.  This
  // number need to be remembered, so that whenever
  // other reorganization is to be done, this row
  // should be switch back with the last row.
  private int currentRow = 0; // use currentRow() instead
                              // when tabRows repositioned

  /** Construct
   */
  TabCanvas() {
    resize(INITIALWIDTH, ROWHEIGHT);
    tabRows = new Vector();
    Vector tabRow = new Vector();
    tabRows.addElement(tabRow);
  } // end TabCanvas.TabCanvas

  /* Add the card. A new tab is created for this
   * card.  The new tab is made the current tab.
   */
  void add(Card card) {
    // return the tabRows' position in order
    resetCurrentTab();
    // create a tab for this card
    Tab tab = new Tab(card);

    // add the tab to the end of the tabRows
    Vector lastTabRow = (Vector) tabRows.lastElement();
    int xend = computeWidth(lastTabRow);
    int tempWidth = size().width;
    //   if the last row have enough spare space
    //   put this tab in this row and set its 
    //   graphic position
    if(xend + tab.width() < tempWidth) {
      lastTabRow.addElement(tab);
      tab.move(xend, size().height);
      // make the new tab current Tab and make 
      // this card visible
      setCurrentTab(tab);
    }
    //   if the last row hasn't enough spare space
    //   create a new row, and add graphic space to this
    //   put this tab in the new row and set its 
    //   graphic position
    else {
      Vector newTabRow = new Vector();
      newTabRow.addElement(tab);
      resize(size().width, size().height + ROWHEIGHT);
      tabRows.addElement(newTabRow);
      // can't use setCurrentTab because a new row added
      // the currentTab is not in the last row now. State 
      // is wrong
      currentTab.setInvisible();
      currentTab = tab;
      currentTab.setVisible();
      tab.move(0, size().height);
      currentRow++;
    }
    // a new card added, the tabs need to be repositioned.
    needReposition();
  } // end TabCanvas.add

  /* Compute the total width of a tab row.
   */
  private int computeWidth(Vector row) {
    Tab tab;
    Object obj;
    try {
      obj = row.lastElement();
    }
    catch(NoSuchElementException e) {
      return 0;
    }
    tab = (Tab) obj;
    int row_width = tab.getBase().x + tab.width();
    return row_width;
  }

  /* the row currentTab is in
   */
  private int currentRow() {
    Point base = currentTab.getBase();
    return base.y/ROWHEIGHT - 1;
  } // end TabCanvas.currentRow

  /* extract a Tab from the tabrows upon give
   * a component reference or a String name.
   */
  private Tab extractTab(Vector v, Object query) {
    Enumeration enm = v.elements();
    while(enm.hasMoreElements()) {
      Vector tabRow = (Vector) enm.nextElement();
      Enumeration tabRowEnm = tabRow.elements();
      while(tabRowEnm.hasMoreElements()) {
	Tab tab = (Tab) tabRowEnm.nextElement();
	Card card = tab.getCard();
	if((card.component == query) || 
	   ((query instanceof String) &&
	   (card.name.compareTo((String)query) == 0))) {
	  return tab;
	}
      }
    }
    return null;
  }

  /* return the first row's first tab 
   */
  Tab firstTab() {
    Vector firstRow = (Vector) tabRows.elementAt(0);
    if(!firstRow.isEmpty())
      return (Tab) firstRow.elementAt(0);
    return null;
  }

  // find the tab that (x, y) is inside.  If (x, y) is
  // out of any tab, return null.
  Tab getTab(int x, int y) {
    int row_no = y/ROWHEIGHT;
    Vector row = (Vector) tabRows.elementAt(row_no);
    Enumeration enm = row.elements();
    while(enm.hasMoreElements()) {
      Tab tab = (Tab) enm.nextElement();
      Point start = tab.getBase();
      if((start.x < x) && (x < (start.x + tab.width())))
	return tab;
    }
    return null;
  } // end TabCanvas.select(int x, int y)

  /* return the last tab.
   */
  Tab lastTab() {
    for(int i = tabRows.size() - 1; i >= 0; i--) {
      Vector row = (Vector) tabRows.elementAt(i);
      if(!row.isEmpty())
	return (Tab) row.lastElement();
    }
    return null;
  }

  public boolean mouseDown(Event evt, int x, int y) {
    setCurrentTab(getTab(x, y));
    return super.mouseDown(evt, x, y);
  }

  // move every element at and after index 
  // of row left deltax.  This happens
  // when an element of the row is move up or removed.
  private void moveLeft(Vector row, int deltax, int index) {
    for(int i = index; i < row.size(); i++) {
      Tab tab = (Tab) row.elementAt(i);
      tab.move(tab.getBase().x - deltax, tab.getBase().y);
    }
  }

  // move every element of row up deltay
  private void moveUp(Vector row, int deltay) {
    Enumeration enm = row.elements();
    while(enm.hasMoreElements()) {
      Tab tab = (Tab) enm.nextElement();
      tab.move(tab.getBase().x, tab.getBase().y - deltay);
    }
  }

  //move all the rows at index and after up deltay
  private void moveUpAll(int deltay, int index) {
    for(int k = index; k < tabRows.size(); k++) {
      Vector row = (Vector) tabRows.elementAt(k);
      moveUp(row, deltay);
    }
  }

  private void needReposition() {
    needReposition = true;
  }

  public void paint(Graphics g) {
    if(needReposition) {
      repositionAll();
    }
    Color save = g.getColor();
    g.setColor(Color.white);
    Dimension size = size(); 
    g.drawRect(0, 0, size.width-1, size.height-1);
    g.setColor(save);
    Enumeration enm = tabRows.elements();
    while(enm.hasMoreElements()) {
      Vector tabRow = (Vector) enm.nextElement();
      Enumeration tabRowEnm = tabRow.elements();
      while(tabRowEnm.hasMoreElements()) {
	Tab tab = (Tab) tabRowEnm.nextElement();
	tab.draw(g, getBackground());
      }
    }
  } // end TabCanvas.paint

  // Remove the tag given a string name
  Component remove(String name) {
    return removeByObject(name);
  } // end TabCanvas.remove

  void remove(Component cmp) {
    removeByObject(cmp);
  } // end TabCanvas.remove
  
  // Remove the tag given a string name or a component reference
  private Component removeByObject(Object query) {
    Card card = null;
    for(int i = 0; i < tabRows.size(); i++) {
      Vector row = (Vector) tabRows.elementAt(i);
      for(int j = 0; j < row.size(); j++) {
	Tab tab = (Tab) row.elementAt(j);
	card = tab.getCard();
	if((query == card.component) ||
	   ((query instanceof String) && 
	    (card.name.compareTo((String) query) == 0))) {
	  row.removeElement(tab);
	  moveLeft(row, tab.width(), j);
	  resetCurrentTab();
	  needReposition();
	  repaint();
	  return card.component;
	}
      }
    } // end first while
    return null;
  } // end TabCanvas.removeByObject

  // before doing this there maybe some blank row
  private void repositionAll() {
    switchWithLastRow(currentRow);
    for(int i = 1; i < tabRows.size(); i++) {
      Vector row = (Vector) tabRows.elementAt(i);
      Vector previousRow = (Vector) tabRows.elementAt(i - 1);
      // use clone since row is change inside the loop
      Enumeration enm = ((Vector)row.clone()).elements();
      while(enm.hasMoreElements()) {
	Tab tab = (Tab) enm.nextElement();
	int endx = computeWidth(previousRow);

	// if the previous row have space for this tab
	// move tab from row to previous row
	if(tab.width() + endx <= size().width) {
	  row.removeElement(tab);
	  tab.move(endx, tab.getBase().y - ROWHEIGHT);
	  // move the remaining elements of row left tab.width()
	  moveLeft(row, tab.width(), 0);
	  previousRow.addElement(tab);
	}
	else {
	  break;
	}
      }
      if(row.isEmpty()) {
	tabRows.removeElementAt(i);
	// move all the following rows up ROWHEIGHT
	moveUpAll(ROWHEIGHT, i);
	resize(size().width, size().height - ROWHEIGHT);
	i--;
      }
    }
    currentRow = currentRow();
    switchWithLastRow(currentRow);
    Container parent = getParent();
    parent.invalidate();
    parent.validate();
    needReposition = false;
  }

  private void resetCurrentTab() {
    switchWithLastRow(currentRow);
    currentRow = tabRows.size() - 1;
    setCurrentTab(lastTab());
  }

  void select(Component component) {
    setCurrentTab(extractTab(tabRows, component));
  } // end TabCanvas.select

  void select(String name) {
    setCurrentTab(extractTab(tabRows, name));
  } // end TabCanvas.select

  Card selected() {
    return currentTab.getCard();
  } // end TabCanvas.selected

  private void setCurrentTab(Tab tab) {
    if(tab != null && currentTab != tab) {
      if(currentTab != null)
        currentTab.setInvisible();
      currentTab = tab;
      currentTab.setVisible();
      int basey = tab.getBase().y;
      // if the tab is in the last row
      // everything is already ok now
      // if the tab is not in last row
      // otherwise:
      if(basey  != size().height) {
	switchWithLastRow(currentRow);
	int index = basey/ROWHEIGHT - 1;
	// if the tab is in the original last row
	if(currentRow == index)
	  currentRow = tabRows.size() - 1;
	else
	  currentRow = index;
	switchWithLastRow(currentRow);
      }
      if(getPeer() != null)
	repaint();
    }
  } // end TabCanvas.setCurrentTab

  // set the base point of all the tabs
  // in row to be y
  private void setRowy(Vector row, int y) {
    Enumeration enm = row.elements();
    while(enm.hasMoreElements()) {
      Tab tab = (Tab) enm.nextElement();
      Point base = tab.getBase();
      tab.move(base.x, y);
    }
  } // end TabCanvas.setRowy

  // switch index row with last row of tabRows
  private void switchWithLastRow(int index) {
    // the index of last row
    int lastIndex = tabRows.size() - 1;
    if(index >= lastIndex)
      return;
    Vector lastRow = (Vector) tabRows.lastElement();
    Vector row = (Vector) tabRows.elementAt(index);
    tabRows.removeElementAt(lastIndex);
    tabRows.setElementAt(lastRow, index);
    tabRows.addElement(row);
    setRowy(row, (lastIndex + 1) * ROWHEIGHT);
    setRowy(lastRow, (index + 1) * ROWHEIGHT);
  } // end TabCanvas.update

}
