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
/**
 * This is a special panel to CardLayout Components
 * with tab-like CardButtons.  Card means the Component
 * and its label.  There are tabs
 * to select which Panel is visible.<p>
 * A Component is added with a name by call addCard 
 * method.  The name
 * then is used as the component's tab label.  A 
 * component can be removed with the name or component's
 * reference.  CardPanel will reorgornized the tab
 * layouts dynamically.
 */

public class CardPanel extends Panel {
  private CardContentPanel cardContent;
  private TabCanvas tabCanvas;
  private boolean isNeedLayout;

  /** Construct the CardPanel.
   */
  public CardPanel(String name, Component comp) {
    setLayout(new BorderLayout());
    cardContent = new CardContentPanel();
    tabCanvas = new TabCanvas();
    add("North", tabCanvas);
    add("Center", cardContent);
    add("West", new Separator(false));
    add("South", new Separator(false));
    add("East", new Separator(false));

    Card card = new Card(name, comp);
    addCard(card);
  }

  /** Add in a component.  It becomes the current visible
   * card.
   * @param name name of the component, it will be used as tab label
   * @param comp the component to be added.
   */
  public void addCard(String name, Component comp) {
    Card cd = new Card(name, comp);
    addCard(cd);
  }

  private void addCard(Card card) {
    cardContent.add(card);
    tabCanvas.add(card);
    needLayout();
  }

  public boolean mouseDown(Event evt, int x, int y) {
    if(evt.target instanceof Canvas)
      reSelect();
    return super.mouseDown(evt, x, y);
  } // end CardPanel.mouseDown

  private void needLayout() {
    if(getPeer() != null) {
      reLayout();
      repaint();
      isNeedLayout = false;
    }
    else {
      isNeedLayout = true;
    }
  } // end CardPanel.needLayout

  public void paint(Graphics g) {
    if(isNeedLayout) {
      reLayout();
    }
  }

  private void reLayout() {
    Container parent = getParent();
    parent.invalidate();
    parent.validate();
  }

  /** 
   * Remove the component by input its label.
   * @param name label of the component to be removed
   */
  public void removeCard(String name) {
    Component cmp = tabCanvas.remove(name);
    if(cmp == null)
      return;
    cardContent.remove(cmp);
    reSelect();
    needLayout();
  } // end CardPanel.removeCard

  /** 
   * Remove the component by input its reference.
   * @param cmp reference of the component to be removed
   */
  public void removeCard(Component cmp) {
    if(cmp == null)
      return;
    tabCanvas.remove(cmp);
    cardContent.remove(cmp);
    reSelect();
    needLayout();
  } // end CardPanel.removeCard 

  // Synchronize TabCanvas and CardContentPanel
  private void reSelect() {
    cardContent.show(tabCanvas.selected());
  } // end CardPanel.reSlect
  
  /**
   * Make this component visible among all the components
   * @param reference of the component to be showed.
   */
  public void show(Component comp) {
    tabCanvas.select(comp);
    reSelect();
  }

  /**
   * Make the component with this name to be visible among all the
   * Components.
   * @param name of the component to be showed.
   */
  public void show(String name) {
    tabCanvas.select(name);
    reSelect();
  }

}
