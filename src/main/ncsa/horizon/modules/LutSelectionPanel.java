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
 *    06-Dec-1997 Ray Plante  moved from awt.image to modules package
 */

package ncsa.horizon.modules;

import ncsa.horizon.awt.image.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Vector;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.util.Slice;
import ncsa.horizon.viewer.Viewer;

/**
 * Lookup table which converts a int or int array ranges 
 * from 0 to table size - 1 a byte or int or byte or int 
 * array that can be feed into java image consumer to set
 * pixels of a java image.
 */
public class LutSelectionPanel extends Panel {
  private int choices;
  private Lut[] luts;
  private LutCanvas[] lutCanvases;
  private Checkbox[] checkboxes;
  private int currentChoice;

  /**
   * Viewer vector contains registered viewers, which 
   * this panel controls.
   */
  protected Vector viewers;

  public LutSelectionPanel() {
    viewers = new Vector();
    choices = 5;
    luts = new Lut[choices];
    luts[0] = Lut.getRainbowLut();
    luts[1] = Lut.getInvertedRainbowLut();
    luts[2] = Lut.getBandedRainbowLut();
    luts[3] = Lut.getStripedRainbowLut();
    luts[4] = Lut.getGrayScaleLut();
    lutCanvases = new LutCanvas[choices];
    checkboxes = new Checkbox[choices];
    CheckboxGroup checkboxGroup = new CheckboxGroup();
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc= new GridBagConstraints();
    setLayout(gbl);
    for (int i = 0; i < choices; i++) {
      lutCanvases[i] = new LutCanvas(luts[i]);
      lutCanvases[i].resize(280, 50);
      checkboxes[i] = new Checkbox(" ", checkboxGroup, i == 0);
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = GridBagConstraints.RELATIVE;
      gbl.setConstraints(checkboxes[i], gbc);
      add(checkboxes[i]);
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbl.setConstraints(lutCanvases[i], gbc);
      add(lutCanvases[i]);
    }
  }

  public boolean action(Event evt, Object arg) {
    if (evt.target instanceof Checkbox) {
      for (int i = 0; i < choices; i++) {
	if(checkboxes[i] == evt.target) {
	  currentChoice = i;
	  redisplay();
	  break;
	}
      }
    }
    return super.action(evt, arg);
  }

  public ColorModel currentColorModel() {
    Lut aLut = luts[currentChoice];
    return new IndexColorModel(8, 256, aLut.getRed(),
			       aLut.getGreen(), aLut.getBlue());
  }

  public Lut getLut() {
    return luts[currentChoice];
  }

  /**
   * Inform registered viewers to redisplay
   */
  protected void redisplay() {
    Viewer viewer = null;
    Slice slice = null;
    int size = viewers.size();
    for (int i = 0; i < size; i++) {
      viewer = (Viewer) viewers.elementAt(i);
      slice = viewer.getViewSlice();
      if (slice != null) {
	viewer.displaySlice(slice);
      }
    }
  }

  public void register(Viewer viewer) {
    viewers.addElement(viewer);
  }

  public void removeViewer(Viewer viewer) {
    viewers.removeElement(viewer);
  }

  public static void main(String arg[]) {
    SimpleFrame frame = new SimpleFrame("Test LutSelectionPanel");
    frame.setKillOnClose();
    LutSelectionPanel lsp = new LutSelectionPanel();
    frame.add("Center", lsp);
    frame.pack();
    frame.show();
  }
    
}
