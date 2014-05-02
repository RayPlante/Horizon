/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1998, Board of Trustees of the University of Illinois
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
 *-------------------------------------------------------------------------
 * History: 
 *  98jan14  rlp  Original version
 */

package ncsa.horizon.awt;

import java.awt.*;

/**
 * a Panel that maintains a nice border around a single component.  <p>
 *
 * This Panel is a nice container to maintain space between an enclosing 
 * Frame and the Component this Panel contains.  When the Frame is resized,
 * this Panel will resize its enclosed component to maintain the space.  <p>
 * 
 * This Panel is designed to hold only one Component.  The Component to be 
 * enclosed can either be passed to the constructor or added afterward with 
 * the add(Component) method.  If there are several components to be laid 
 * out, one should wrap them in a regular AWT Panel before enclosing them in 
 * this one.  If add(Component) is called after a Component has already been
 * added, the previous one is replaced.  This Panel automatically sets the 
 * up the necessary layout (via initLayout()) upon construction; therefore,
 * one should not call this Panel's setLayout() method.  Doing so voids the
 * features of this class and may produce undesired effects.  Similarly,
 * one should not call the add(String, Component) or add(Component, int) 
 * methods.  
 * 
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class SingleComponentPanel extends Panel {

    /**
     * the single component contained in this Panel
     */
    protected Component item = null;

    protected int tgap=4, bgap=4, rgap=4, lgap=4;

    GridBagLayout bag;
    GridBagConstraints constraints;

    /**
     * construct the Panel with the single Component it will enclose to 
     * be added later.  
     */
    public SingleComponentPanel() { 
	super(); 
	initLayout();
    }

    /**
     * construct the Panel with a given size border.  (The single Component 
     * it will enclose should be added later.)  
     */
    public SingleComponentPanel(int bottom, int left, int right, int top) { 
	super(); 
	bgap = bottom;
	lgap = left;
	rgap = right;
	tgap = top;
	initLayout();
    }

    /**
     * construct the Panel with a given size border and enclosing a single 
     * given Component.
     */
    public SingleComponentPanel(Component comp, int bottom, int left, 
				int right, int top) 
    { 
	super(); 
	bgap = bottom;
	lgap = left;
	rgap = right;
	tgap = top;
	initLayout();
	add(comp);
    }

    /**
     * construct the Panel to enclose the single given Component
     */
    public SingleComponentPanel(Component comp) {
	super();
	initLayout();
	add(comp);
    }

    /**
     * initialize the Layout.  This sets the LayoutManager and its parameters.
     */
    protected void initLayout() {
        bag = new GridBagLayout();
        constraints = new GridBagConstraints();
        setLayout(bag);
        constraints.insets = new Insets(bgap,lgap,rgap,tgap);
        constraints.gridwidth = constraints.REMAINDER;
        constraints.weightx = constraints.weighty = 1;
        constraints.fill = constraints.BOTH;
        constraints.anchor = constraints.NORTHWEST;
    }

    public Component add(Component comp) {
	removeAll();
	item = comp;
	bag.setConstraints(comp, constraints);
	super.add(comp);
	return comp;
    }

    public void reshape(int x, int y, int width, int height) {
        if (item != null) item.reshape(lgap, tgap, width-(lgap+rgap), 
				       height-(tgap+bgap));
        super.reshape(x, y, width, height);
    }
}
