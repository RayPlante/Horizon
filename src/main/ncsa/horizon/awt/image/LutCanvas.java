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

package ncsa.horizon.awt.image;

import java.awt.*;
import java.util.StringTokenizer;

public class LutCanvas extends Canvas {
  Lut lut;

  public LutCanvas(Lut lut) {
    this.lut = lut;
  }

  public void paint(Graphics g) {
    int pixelValue;
    Dimension dm = size();
    int stepNumber = lut.getSize();
    int width = dm.width/stepNumber;
    width = width > 1 ? width : 1;
    int fontSize = g.getFont().getSize();
    int bottomGap = 5;
    int markGap = 4 * fontSize / width / 10 * 10;
    int height = dm.height - fontSize - bottomGap;
    int markBaseY = dm.height - bottomGap;
    for (int i = 0; i < stepNumber; i++) {
      pixelValue = lut.lookup(i);
      g.setColor(new Color(pixelValue));
      // System.out.println("lookup " + i +" is " + Integer.toHexString(pixelValue));
      g.fillRect(i * width, 0, width, height);
      if(i%markGap == 0) {
	g.drawString( "" + i, i * width - fontSize, markBaseY);
      }
    }
    String label = lutLabel();
    g.setColor(Color.white);
    g.drawString(label, 20, 20);
    g.drawString(label, 21, 20);
    g.setColor(Color.black);
    g.drawString(label, 22, 21);
  }

  // It's used only in lutLabel() method to save
  // some computation.
  private String lutLabel;
  private String lutLabel() {
    if(lutLabel == null) {
      lutLabel = lut.getClass().getName();
      // strip out only the last part of the class name
      StringTokenizer stkn = new StringTokenizer(lutLabel, ".");
      while (stkn.hasMoreTokens()) {
	lutLabel = stkn.nextToken();
      }
    lutLabel = lutLabel.substring(0, lutLabel.length() - 3);
    }
    return lutLabel;
  }
}
