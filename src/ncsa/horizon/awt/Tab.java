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
// used only for TabCanvas class
class Tab {
  private int CHARSIZE = 6;
  private int FONTSIZE = 12;
  private int HEIGHT = TabCanvas.ROWHEIGHT;
  private int SMOOTHRADIUS = 15;
  private int GAPX = 10;
  private int GAPY = 5;
  Font normalFont;
  Font selectFont;
  Card card;
  Point basePoint;
  Dimension dimension;

  Tab(Card cd) {
    normalFont = new Font("Helvetica", Font.PLAIN, FONTSIZE);
    selectFont = new Font("Helvetica", Font.BOLD, FONTSIZE);
    card = cd;
    basePoint = new Point(0, HEIGHT);
    dimension = new Dimension(card.name.length() * CHARSIZE + 2 * GAPX,
			      HEIGHT);
  }

  void draw(Graphics g, Color background) {
    Color save = g.getColor();
    Font saveFont = g.getFont();
    g.setColor(Color.white);
    Point upleftCorner = new Point(basePoint.x + 1, 
				   basePoint.y - dimension.height + 1);
    Point bottomleftCorner = new Point(basePoint.x + 1, basePoint.y);
    Point uprightCorner = new Point(basePoint.x + dimension.width - 1,
				    basePoint.y - dimension.height + 1);
    Point bottomrightCorner = new Point(basePoint.x + dimension.width - 1,
					basePoint.y);
    Point c1 = new Point(upleftCorner.x, upleftCorner.y);
    c1.translate(SMOOTHRADIUS, SMOOTHRADIUS);
    Point p1 = new Point(upleftCorner.x, upleftCorner.y);
    p1.translate(0, SMOOTHRADIUS);
    Point p2 = new Point(upleftCorner.x, upleftCorner.y);
    p2.translate(SMOOTHRADIUS, 0);
    Point c2 = new Point(uprightCorner.x, uprightCorner.y);
    c2.translate(-SMOOTHRADIUS, SMOOTHRADIUS);
    Point p3 = new Point(uprightCorner.x, uprightCorner.y);
    p3.translate(0, SMOOTHRADIUS);
    Point p4 = new Point(uprightCorner.x, uprightCorner.y);
    p4.translate(-SMOOTHRADIUS, 0);
    // draw the left light line
    g.drawLine(p1.x, p1.y, 
	       bottomleftCorner.x, bottomleftCorner.y);
    // draw the upleft arc
    g.drawArc(upleftCorner.x, upleftCorner.y, 2 * SMOOTHRADIUS,
	      2 * SMOOTHRADIUS, 90, 90);
    // draw the upper light line
    g.drawLine(p2.x, p2.y, p4.x, p4.y);
    // draw the upright arc
    g.drawArc(p4.x - SMOOTHRADIUS, p4.y, 2 * SMOOTHRADIUS,
	      2 * SMOOTHRADIUS, 0, 90);
    // draw the right dark line
    g.setColor(Color.black);
    g.drawLine(p3.x, p3.y,
	       bottomrightCorner.x, bottomrightCorner.y - 2);

    if(card.isVisible()) {
      // open bottom line
      g.setColor(background);
      // g.setColor(Color.red);
      g.drawLine(bottomleftCorner.x, bottomleftCorner.y -1,
		 bottomrightCorner.x, bottomrightCorner.y -1);
      g.setFont(selectFont);
    }
    else {
      g.setColor(Color.gray);
      g.drawLine(bottomleftCorner.x, bottomleftCorner.y + 1,
		 bottomrightCorner.x, bottomrightCorner.y + 1);
      g.setFont(normalFont);
    }
    g.setColor(Color.black);
    g.drawString(card.name, basePoint.x + GAPX, basePoint.y - GAPY);
    g.setFont(saveFont);
    g.setColor(save);
  } // Tab.draw

  Card getCard() {
    return card;
  }

  Point getBase() {
    return new Point(basePoint.x, basePoint.y);
  }

  int height() {
    return dimension.height;
  } // end Tab.height

  void setInvisible() {
    card.hide();
  }

  void move(int x, int y) {
    basePoint.x = x;
    basePoint.y = y;
  } // end Tab.move

  void setVisible() {
    card.show();
  }

  int width() {
    return dimension.width;
  }

}
