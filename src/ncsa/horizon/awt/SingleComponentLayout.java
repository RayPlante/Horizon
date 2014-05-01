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
 * a LayoutManager that maintains a border around a single component.  <p>
 *
 * This LayoutManager is a is sometimes useful for laying out a single 
 * component within a Frame maintaining a specifiable amount of space 
 * between the single component and the enclosing Frame.  When the Frame 
 * is resized, the enclosed component will resize such that the space is
 * maintained.  <p>
 * 
 * This LayoutManager is designed to layout only one Component.  By default,
 * the component that is laid out will be the last one that was added to 
 * the container.  If there is a chance that several the container's add()
 * methods might get called more than once, one can call the setComponent()
 * method to indicate which one is the desired component.  The constructors
 * that take a component as an argument calls the setComponent() method 
 * automatically.  One should note that all components that are not to be
 * laid out will be automatically removed from the container at layout time. 
 * <p>
 *
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class SingleComponentLayout implements LayoutManager {

    /**
     * the single component contained in this Panel
     */
    protected Component item = null;

    protected int tgap=4, bgap=4, rgap=4, lgap=4;

    GridBagLayout delegate = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();

    /**
     * construct the LayoutManager to manage a single Component.
     * If this constructor is used, the setComponent() method should also 
     * be called.  
     */
    public SingleComponentLayout() { 
	super(); 
	initConstraints();
    }

    /**
     * construct the LayoutManager to manage a single Component using
     * a given size border.  If this constructor is used, the setComponent()
     * method should also be called.  
     */
    public SingleComponentLayout(int bottom, int left, int right, int top) { 
	super(); 
	bgap = bottom;
	lgap = left;
	rgap = right;
	tgap = top;
	initConstraints();
    }

    /**
     * construct the LayoutManager to manage the single specified Component 
     * using a given size border.  If this constructor is used, it is <em>
     * not </em> necessary to call the setComponent() method.
     */
    public SingleComponentLayout(Component comp, int bottom, int left, 
				int right, int top) 
    { 
	super(); 
	bgap = bottom;
	lgap = left;
	rgap = right;
	tgap = top;
	initConstraints();
	setComponent(comp);
    }

    /**
     * construct the LayoutManager to manage the single specified Component.
     * If this constructor is used, it is <em> not </em> necessary to call 
     * the setComponent() method.
     */
    public SingleComponentLayout(Component comp) {
	super();
	initConstraints();
	setComponent(comp);
    }

    /**
     * initialize the Layout.  This sets the LayoutManager and its parameters.
     */
    protected void initConstraints() {
        constraints.insets = new Insets(bgap,lgap,rgap,tgap);
        constraints.gridwidth = constraints.REMAINDER;
        constraints.fill = constraints.BOTH;
        constraints.weightx = constraints.weighty = 1;
        constraints.anchor = constraints.NORTHWEST;
    }

    /**
     * tell the LayoutManager which component should be layed out.  This 
     * need only be called if there is a chance that more than one component
     * was added to the container before it is laid out.  This method would 
     * specify which component is the desired one; otherwise, the last 
     * component to be added is assumed to be the desired one.  Note that 
     * all other components will be removed from the container when it is 
     * laid out.  
     */
    public synchronized void setComponent(Component comp) {
	if (item != null) delegate.removeLayoutComponent(item);
	item = comp;
	delegate.setConstraints(comp, constraints);
    }

    public synchronized void removeLayoutComponent(Component comp) {
	if (comp == item) item = null;
	delegate.removeLayoutComponent(item);
    }

    public void addLayoutComponent(String name, Component comp) {
	delegate.addLayoutComponent(name, comp);
    }

    public Dimension minimumLayoutSize(Container parent) {
	return delegate.minimumLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
	return delegate.preferredLayoutSize(parent);
    }

    private synchronized void checkItem(Container parent) { 

	// get rid of all components in container that are not the one 
	// we are configured for.  If we do not know which component 
	// we are configured for, assume that the last one in the list 
	// is the one we want.
	Component[] comps = parent.getComponents();

	if (comps.length > 0) {

	    int i = comps.length;
	    if (item == null) {
		setComponent(comps[comps.length-1]);
	    }
	    else {
		if (item != null) {
		    for(i=0; i < comps.length; i++) 
			if (item == comps[i]) continue;
		}
		if (item == null || i >= comps.length) 
		    setComponent(comps[comps.length-1]);
	    }

	    if (comps.length > 1) {
		for(i=0; i < comps.length; i++) 
		    if (item != comps[i]) parent.remove(comps[i]);
	    }
	}
    }

    public synchronized void layoutContainer(Container parent) {
	
	synchronized (parent) {
	    checkItem(parent);
	    delegate.layoutContainer(parent);
	}
    }
}
