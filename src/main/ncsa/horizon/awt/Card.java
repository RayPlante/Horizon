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

import java.awt.Component;

class Card {
  String name;
  Component component;
  boolean visible;

  Card(String name, Component component) {
    this.name = name;
    this.component = component;
  }

  void hide() {
    show(false);
  }

  boolean isVisible() {
    return visible;
  }

  void show() {
    show(true);
  }

  void show(boolean b) {
    visible = b;
  }
}
