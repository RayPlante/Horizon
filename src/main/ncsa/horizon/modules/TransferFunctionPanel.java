/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-8, Board of Trustees of the University of Illinois
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

package ncsa.horizon.modules;

import java.awt.*;
import java.util.*;
import ncsa.horizon.awt.LayoutRearrangeable;
import ncsa.horizon.awt.NumberScrollPanel;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.awt.SingleComponentLayout;
import ncsa.horizon.awt.ModedGUI;
import ncsa.horizon.awt.ModedGUIException;
import ncsa.horizon.util.Slice;
import ncsa.horizon.data.TransferFunction;
import ncsa.horizon.data.NumericTransferFunction;
import ncsa.horizon.viewer.Viewer;

/**
 * a graphical user interface (GUI) for manipulating a TransferFunction 
 * object.  <p>
 *
 * A TransferFunction is used to map a range of data values into a 
 * given number of integer bins so that they may be converted to
 * colors for display.  In general, the TransferFunction allows the 
 * user to specify the minimum and maximum of the range of data to 
 * map as well as the number of bins.  Furthermore, different 
 * TransferFunctions can provide different functions to do the mapping 
 * or support different datatypes.  TransferFunctions thus allow the
 * user to create a visualization that focuses on specific signal 
 * strengths in an image that may have a large dynamic range.  <p>
 *
 * The TransferFunctionPanel provides a GUI for adjusting the parameters 
 * of the TransferFunction.  Controls include two scrollbars-textfield pairs 
 * for setting the minimum and maximum, a reset button, and an apply 
 * button.  There is also menu for switching between different 
 * (TransferFunction-specific) modes, and it can also optionally provide 
 * additional controls associated with each mode (see below).  <p>
 *
 * The TransferFunctionPanel can only drive one TransferFunction; however,
 * the TransferFunction may have several modes associated with it.  For
 * example, it may provide a choice of several mathematical functions to 
 * apply to the data.  The Panel also allows a panel of mode-specific 
 * controls to be displayed when a particular mode is selected.  <p>
 *
 * To deploy this GUI, the programmer should provide the TransferFunction
 * to be controlled either to the TransferFunctionPanel's constructor or
 * to its setTransferFunction() method.  One makes additional modes
 * available with the addMode() method; along with a name of the mode 
 * that will appear in the mode menu, one can also provide a AWT Component
 * that contains GUI controls for additional options associated with the 
 * mode.  One should consult the API documentation for the specific 
 * TransferFunction for details of available modes and their names.  The
 * configuring of modes may be done automatically when the TransferFunction
 * is attached to the Panel via the TransferFunction's init() method; 
 * consult the TransferFunction's API for details.  <p>
 *
 * Also as part of deploying this GUI, one would usually attach a Viewer 
 * to it via the registerViewer() method.  This will allow the user to 
 * apply the parameters to the data being displayed by the viewer by 
 * clicking the "Apply" button (which calls the apply() method).  One 
 * can have the visualization updated automatically whenever certain 
 * parameters are adjusted (like the min or max is changed) by calling
 * setAutoApplying(true).  Since there may be a large overhead with updating
 * the visualization, this feature is turned off by default.  <p>
 * 
 * @author Raymond L. Plante
 * @author NCSA Horizon team, University of Illinois at Urbana-Champaign
 */
public class TransferFunctionPanel extends Panel implements ModedGUI {

    /**
     * the TransferFunction driven by this GUI
     */
    protected TransferFunction tf = null;

    // the original min and max; used by the reset method
    Object origMin, origMax;

    // the current range accessible by the scrollbars
    Object rangeMin, rangeMax;

    /**
     * a choice menu for choosing a mode for the TransferFunction
     */
    protected Choice modeChoice = new Choice();

    /**
     * a panel containing the Choice menu for choosing a mode
     */
    protected Panel modePanel = new Panel();

    /**
     * a panel containing extra controls for updating options associated
     * with the current mode
     */
    protected Panel curoptPanel = new Panel();

    /**
     * a hastable of option panels indexed by mode label
     */
    protected Hashtable optPanels = new Hashtable();

    /**
     * the panel containing the scrollbar and textfield for setting the 
     * minimum value
     */
    protected Panel minScrollPanel;

    /**
     * the panel containing the scrollbar and textfield for setting the 
     * maximum value
     */
    protected Panel maxScrollPanel;

    /**
     * the scrollbar for setting the minimum value
     */
    protected Scrollbar minScroll;

    /**
     * the scrollbar for setting the maximum value
     */
    protected Scrollbar maxScroll;

    /**
     * the textfield for setting the minimum value
     */
    protected TextField minText;

    /**
     * the textfield for setting the maximum value
     */
    protected TextField maxText;

    /**
     * the reset button for setting scollbar panels to their initial 
     * state
     */
    protected Button resetBut = new Button("Reset");

    /**
     * the button for setting scollbar ranges using the currently
     * min/max values.
     */
    protected Button setBut = new Button("Range to Current");

    /**
     * the apply button for applying the TransferFunction
     */
    protected Button applyBut = new Button("Apply");

    /**
     * an attached viewer displaying a viewable that uses this transfer
     * function
     */
    protected Viewer viewer = null;

    /**
     * if true, changes to individual parameters will cause the attached
     * viewer to be notified to redisplay
     */
    protected boolean autoApply = false;

    /**
     * if true, the text field is editable. This is because the values handled 
     * by the transfer function are Numbers.
     */
    protected boolean textfieldEditable;

    boolean noRealChoice = true;
    GridBagLayout bag = new GridBagLayout();

    private TransferFunction fulltf = null;

    /**
     * instantiate and layout the panel with no attached function
     */
    public TransferFunctionPanel() { 
	this(null, null);
    } 

    /**
     * instantiate and layout the panel using a given function, using
     * a default mode label and no options panel.
     */
    public TransferFunctionPanel(TransferFunction func) { 
	this(func, "default");
    } 

    /**
     * instantiate and layout the panel using a given function, using a
     * a given mode label but no options panel.
     */
    public TransferFunctionPanel(TransferFunction func, String label) {
	this(func, label, null);
    }

    /**
     * instantiate and layout the panel using a given function, using a
     * a given mode label and options panel.
     */
    public TransferFunctionPanel(TransferFunction func, String label,
				 Component optionsPanel) 
    {
	if (func != null) {
	    tf = func;
	    rangeMin = origMin = tf.getMinimum();
	    rangeMax = origMax = tf.getMaximum();
	    
	    textfieldEditable = 
		(origMax instanceof Number && origMin instanceof Number);

	    // set the mode Choice menu
	    if (label == null) 
		label = "default";
	    else
		noRealChoice = false;
	    modeChoice.addItem(label);

	} else {
	    rangeMin = origMin = new Double(0);
	    rangeMax = origMax = new Double(256);
	    modeChoice.addItem("[none]");
	}

	// set the options panel for this mode
	curoptPanel = new Panel();
	if (optionsPanel != null) {
	    optPanels.put(label, optionsPanel);
	    curoptPanel.add("Center", optionsPanel);
	}

	// set up the scrollbars
	int levs = (func == null) ? 256 : func.getDynamicRange()-3;
	int inc = levs/10;
	minScroll = new Scrollbar(Scrollbar.HORIZONTAL, 0, inc, 0, levs-1);
	maxScroll = new Scrollbar(Scrollbar.HORIZONTAL, levs, inc, 0, levs-1);

	String mintxt, maxtxt;
	if (tf != null) {
	    mintxt = tf.toString( func.getValue( minScroll.getValue()+2 ) );
	    maxtxt = tf.toString( func.getValue( maxScroll.getValue()+2 ) );
	}
	else {
	    mintxt = Integer.toString(0);
	    maxtxt = Integer.toString(levs);
	}
	minText = new TextField(mintxt, 7);
	maxText = new TextField(maxtxt, 7);
	minText.setEditable(textfieldEditable);
	maxText.setEditable(textfieldEditable);

	minScrollPanel = new TFP_ScrollPanel(new Label("Min:"), 
					     minScroll, minText);
	maxScrollPanel = new TFP_ScrollPanel(new Label("Max:"), 
					     maxScroll, maxText);
	((TFP_ScrollPanel) minScrollPanel).setPreferredSize(300, 29);
	((TFP_ScrollPanel) maxScrollPanel).setPreferredSize(300, 29);

	layoutComponents();    
    }

    /**
     * set the TransferFunction to be driven by this GUI.  Calling this 
     * method will clear any previously set option Panels and labels
     */
    public synchronized void setTransferFunction(TransferFunction func,
						 String label) 
    {
	tf = func;

	Object min = func.getMinimum();
	Object max = func.getMaximum();
	origMin = rangeMin = min;
	origMax = rangeMax = max;
	setMinimum(min);
	setMaximum(max);

	textfieldEditable = 
	    (origMax instanceof Number && origMin instanceof Number);
	minText.setEditable(textfieldEditable);
	maxText.setEditable(textfieldEditable);
	    
	if (label == null) label = "default";
	resetForNewTF(label);
    }

    private synchronized void resetForNewTF(String deflabel) {
	optPanels = new Hashtable();

	modePanel.removeAll();
	modeChoice = new Choice();
	modeChoice.addItem(deflabel);
//	modePanel.add("Center", modeChoice);
	modePanel.add(modeChoice);
	invalidate();

	curoptPanel.removeAll();
	noRealChoice = true;
    }
	
    /**
     * add a mode entry and related option panel
     * @param label  the label to add to the menu (should not be null)
     * @param opts   a component/container hold GUI controls especially
     *               for this mode.
     */
    public synchronized void addMode(String label, Component opts) {
	if (label == null) throw new NullPointerException("Missing Label");

	if (noRealChoice) {
	    optPanels = new Hashtable();

	    modePanel.remove(modeChoice);
	    modeChoice = new Choice();
	    modePanel.add(modeChoice);
	    noRealChoice = false;
	}
	modeChoice.addItem(label);

	if (opts != null) {
	    optPanels.put(label, opts);
	    if (modeChoice.countItems() == 1) {
		curoptPanel.removeAll();
		curoptPanel.add("Center", opts);
	    }
	}

//	resetMinMax();
    }

    /**
     * set the lowest value one may set the minimum to 
     */
    public synchronized void setFullRangeMin(Object val) 
	throws IllegalArgumentException 
    {
	if (val == null) throw new NullPointerException("Range minimum");

	Object old = rangeMin;
	rangeMin = val;
	try {
	    updateRangeGuis();
	} catch (IllegalArgumentException ex) {
	    rangeMin = old;
	    throw ex;
	}
    }

    /**
     * set the highest value one may set the maximum to 
     */
    public void setFullRangeMax(Object val) 
	throws IllegalArgumentException 
    {
	if (val == null) throw new NullPointerException("Range minimum");

	Object old = rangeMax;
	rangeMax = val;
	try {
	    updateRangeGuis();
	} catch (IllegalArgumentException ex) {
	    rangeMax = old;
	    throw ex;
	}
    } 

    /**
     * return the lowest value one may set the minimum to 
     */
    public Object getFullRangeMin() {
	return rangeMin;
    }

    /**
     * return the highest value one may set the maximum to 
     */
    public Object getFullRangeMax() {
	return rangeMax;
    }

    synchronized void updateRangeGuis() {
	if (tf == null) return;

	int lev;
	Object mn = getMinimum(), mx = getMaximum();

	if (fulltf != null) { synchronized (fulltf) { fulltf = null; } }
	fulltf = (TransferFunction) tf.clone();
	synchronized (fulltf) {
	    try {
		fulltf.setMinimum(rangeMin);
		fulltf.setMaximum(rangeMax);
	    } catch (IllegalArgumentException ex) {
		fulltf = null;
		throw new InternalError("TransferFunction: range corrupted");
	    }

	    int dynarange = tf.getDynamicRange();

	    lev = fulltf.getLevel(mn);
	    if (lev < 2) lev = 2;
	    if (lev >= dynarange-1) lev = dynarange-2;
	    minScroll.setValue(lev-2);

	    lev = tf.getLevel(mx);
	    if (lev < 2) lev = 2;
	    if (lev >= dynarange-1) lev = dynarange-2;
	    maxScroll.setValue(lev-2);
	}
    }

    /**
     * set the TransferFunction minimum value and adjust the GUI to 
     * reflect this change.
     */
    public void setMinimum(Object min) throws IllegalArgumentException {
	if (tf == null) return;

	int lev;
	int dynarange = tf.getDynamicRange();
	tf.setMinimum(min);

	// To figure out what the position the scroll bar should have,
	// we use a clone of the TransferFunction set to the full range
	if (fulltf != null) { synchronized (fulltf) { fulltf = null; } }
	fulltf = (TransferFunction) tf.clone();
	synchronized (fulltf) {
	    try {
		fulltf.setMinimum(rangeMin);
		fulltf.setMaximum(rangeMax);
	    } catch (IllegalArgumentException ex) {
		throw new InternalError("TransferFunction: corrupted range");
	    }

	    // Get the corresponding bin level
	    lev = fulltf.getLevel(min);

	    if (lev == 0) {
		throw new IllegalArgumentException("Invalid value for minimum: "
						   + min);
	    }
	    else {
		if (lev == 1) {

		    // input minimum is less than the low end of the range; 
		    // extend the scrollbars to include this value.
		    setFullRangeMin(min);
		    minScroll.setValue(0);
		}
		else if (lev >= dynarange-1) {

		    // input minimum is greater than the high end of the range; 
		    // extend the scrollbars to include this value.
		    setFullRangeMax(min);
		    minScroll.setValue(minScroll.getMaximum());
		    setMaximum(min);
		}
		else {

		    // input minimum is within the current range
		    minScroll.setValue(lev-2);
		}

		// sync the text to the scrollbar
		minText.setText(fulltf.toString(min));
	    }
	}

	if (autoApply) apply();
    }

    /**
     * get the minimum value for the transfer function
     */
    public Object getMinimum() { 
	if (tf == null) 
	    return new Integer(minScroll.getValue());
	else
	    return tf.getMinimum();
    }

    /**
     * set the TransferFunction maximum value and adjust the GUI to 
     * reflect this change.
     */
    public void setMaximum(Object max) throws IllegalArgumentException {
	if (tf == null) return;

	int lev;
	int dynarange = tf.getDynamicRange();
	tf.setMaximum(max);

	// To figure out what the position the scroll bar should have,
	// we use a clone of the TransferFunction set to the full range
	if (fulltf != null) { synchronized (fulltf) { fulltf = null; } }
	fulltf = (TransferFunction) tf.clone();
	synchronized (fulltf) {
	    try {
		fulltf.setMinimum(rangeMin);
		fulltf.setMaximum(rangeMax);
	    } catch (IllegalArgumentException ex) {
		throw new InternalError("TransferFunction: corrupted range");
	    }

	    // Get the corresponding bin level
	    lev = fulltf.getLevel(max);

	    if (lev == 0) {
		throw new IllegalArgumentException("Invalid value for minimum: "
						    + max);
	    }
	    else {
		if (lev == 1) {

		    // input minimum is less than the low end of the range; 
		    // extend the scrollbars to include this value.
		    setFullRangeMin(max);
		    setMinimum(max);
		    maxScroll.setValue(0);
		}
		else if (lev >= dynarange-1) {

		    // input minimum is greater than the high end of the range; 
		    // extend the scrollbars to include this value.
		    setFullRangeMax(max);
		    maxScroll.setValue(maxScroll.getMaximum());
		}
		else {
		    
		    // input minimum is within the current range
		    maxScroll.setValue(lev-2);
		}

		// sync the text to the scrollbar
		maxText.setText(fulltf.toString(max));
	    }
	}

	if (autoApply) apply();
    }

    /**
     * get the maximum value for the transfer function
     */
    public Object getMaximum() { 
	if (tf == null) 
	    return new Integer(maxScroll.getValue());
	else
	    return tf.getMaximum();
    }

    /**
     * return a data value corresponding to a binned transfer function level.
     * By default, this will use the given TransferFunction to do the 
     * conversion by calling its getValue() method.  Subclasses have the 
     * option of affecting this conversion by overriding this method.  The
     * input value should follow the same convention as supported by the
     * TransferFunction getValue() method.
     */
    protected Object getValueFromLevel(int lev, TransferFunction func) {
	return func.getValue(lev);
    }

    /**
     * return the name of the currently selected mode
     */
    public String getMode() {
	return modeChoice.getSelectedItem();
    }

    /**
     * return an array of the names of the available modes 
     */
    public synchronized String[] getAllModes() {
	String[] names = new String[modeChoice.countItems()];
	for(int i=0; i < names.length; i++) {
	    names[i] = modeChoice.getItem(i);
	}
	return names;
    }

    /**
     * switch the mode identified by the given name and update the GUI to 
     * reflect the change.  
     * @returns true if the name was recognized and the mode was switched
     */
    public synchronized boolean setMode(String name) {

	// hash the available names
	int nnames = modeChoice.countItems();
	if (nnames <= 1) return false;
	Hashtable namlist = new Hashtable(nnames);
	for(int i=nnames-1; i >= 0; i--) {
	    namlist.put(modeChoice.getItem(i), new Integer(i));
	}

	Integer idx = (Integer) namlist.get(name);
	if (idx == null) return false;
	if (idx.intValue() == modeChoice.getSelectedIndex()) return false;

	modeChoice.select(idx.intValue());

	// update the options panel as necessary
	Component optspan = (Component) optPanels.get(name);
	if (optspan != null) {
	    curoptPanel.removeAll();
	    curoptPanel.add("Center", optspan);
	}

	repaint();
	if (autoApply) apply();
	return true;
    }

    /**
     * reset the scrollbars to their original positions
     */
    public synchronized void reset() {
	if (tf == null) return;

	setFullRangeMin(origMin);
	setFullRangeMax(origMax);
	setMinimum(origMin);
	setMaximum(origMax);
	apply();
    }

    /**
     * set the full range to the values of transfer function's min
     * and max
     */
    public synchronized void adjustToCurrent() {
	if (tf == null) return;

	setFullRangeMin(tf.getMinimum());
	setFullRangeMax(tf.getMaximum());
    }

    /**
     * attach a viewer that should be told to update its image when
     * the transfer function has been updated.  Normally (when isAutoAplied()
     * returns false), this only happens when the Apply button is pressed.
     */
    public void registerViewer(Viewer vu) {
	viewer = vu;
    }

    /**
     * tell the attached viewer to redisplay, so as to use the most
     * current parameters of the transfer function
     */
    public void apply() {
	if (viewer != null) {
	    synchronized (viewer) {
		Slice cursl = viewer.getViewSlice();
		viewer.displaySlice(cursl);
	    }
	}
    }

    /**
     * set whether changes to individual parameters will cause the attached
     * viewer to be notified to redisplay.
     * @param in  if true, the viewer will redisplay automatically.
     */
    public void setAutoApplying(boolean in) {  autoApply = in; }

    /** 
     * return true if changes to individual parameters will cause the attached
     * viewer to be notified to redisplay.
     */
    public boolean isAutoApplying() { return autoApply; }

    /**
     * handle action events
     */
    public boolean action(Event ev, Object what) {
	if (ev.target == applyBut) {
	    apply();
	    return true;
	}
	else if (ev.target == setBut) {
	    adjustToCurrent();
	    return true;
	}
	else if (ev.target == modeChoice) {
	    try {
		tf.useMode((String) what);
	    } catch (ModedGUIException ex) {
		System.err.println("Warning: " + ex.getMessage());
	    }
	    return true;
	}
	else if (ev.target == resetBut) {
	    reset();
	    return true;
	}
	else if (ev.target == minText) {
	    if (! textfieldEditable) return false;
	    try {
		Double value = Double.valueOf(minText.getText().trim());
		setMinimum(value);
	    } catch (NumberFormatException ex) {
		minText.setText(tf.toString(tf.getMinimum()));
		System.err.println("Bad input: " + what + ": " + 
				   "Not a Number");
		return true;
	    } catch (IllegalArgumentException ex) {
		minText.setText(tf.toString(tf.getMinimum()));
		System.err.println("Bad input: " + what + ": " + 
				   ex.getMessage());
		return true;
	    }
	}
	else if (ev.target == maxText) {
	    if (! textfieldEditable) return false;
	    try {
		Double value = Double.valueOf(maxText.getText().trim());
		setMaximum(value);
	    } catch (NumberFormatException ex) {
		maxText.setText(tf.toString(tf.getMaximum()));
		System.err.println("Bad input: " + what + ": " + 
				   "Not a Number");
		return true;
	    } catch (IllegalArgumentException ex) {
		maxText.setText(tf.toString(tf.getMaximum()));
		System.err.println("Bad input: " + what + ": " + 
				   ex.getMessage());
		return true;
	    }
	}

	return false;
    }

    /**
     * pass scroll events to the scroll handler
     */
    public boolean handleEvent(Event e) {

	boolean handled = false;
	if (e.target instanceof Scrollbar) 
	    handled = scrollEvent(e);

	if (! handled) handled = super.handleEvent(e);

	return handled;
    }
	
    /**
     * handle scroll events
     */
    public synchronized boolean scrollEvent(Event e) {
	Scrollbar sb; 
	TextField fld;
	if (e.target == minScroll) {
	    sb = minScroll; 
	    fld = minText;
	}
	else if (e.target == maxScroll) {
	    sb = maxScroll;
	    fld = maxText;
	}
	else {
	    return false;
	}

	if (tf == null) {
	    fld.setText(Integer.toString(sb.getValue()));
	    return true;
	}

	if (fulltf == null) fulltf = (TransferFunction) tf.clone();
	synchronized (fulltf) {
	    int sbv = sb.getValue();
	    Object val = getValueFromLevel(sbv+2, fulltf);
	    fld.setText(fulltf.toString(val));
	    if (sb == minScroll) {
		tf.setMinimum(val);
		if (sbv > maxScroll.getValue()) setMaximum(val);
	    }
	    else {
		tf.setMaximum(val);
		if (sbv < minScroll.getValue()) setMinimum(val);
	    }
	}

	if (autoApply) apply();
	return true;
    }	

    protected void layoutComponents() {
	layoutComponents(this);
    }

    protected void layoutComponents(Container con) {
	GridBagConstraints c = new GridBagConstraints();
	con.setLayout(bag);

	c.gridwidth = c.REMAINDER;
	c.gridheight = 1;
	c.gridx = 0;
	c.gridy = c.RELATIVE;
	c.anchor = c.CENTER;
	c.fill = c.NONE;

	modePanel.setLayout(new SingleComponentLayout(6,6,6,6));
	modePanel.add(modeChoice);
	bag.setConstraints(modePanel, c);
	con.add(modePanel);

	bag.setConstraints(curoptPanel, c);
	con.add(curoptPanel);

	c.fill = c.HORIZONTAL;
	bag.setConstraints(minScrollPanel, c);
	con.add(minScrollPanel);
	bag.setConstraints(maxScrollPanel, c);
	con.add(maxScrollPanel);

	c.fill = c.BOTH;
	Panel butpan = new Panel();
	butpan.setLayout(new FlowLayout());
	butpan.add(setBut);
	butpan.add(resetBut);
	butpan.add(applyBut);
	bag.setConstraints(butpan, c);
	con.add(butpan);
    }

    public void reshape(int x, int y, int width, int height) {
	if (height > 0 && width > 0) {
	    Dimension sz = minScrollPanel.preferredSize();
	    ((TFP_ScrollPanel)minScrollPanel).setPreferredSize(width, 
							       sz.height);
	    sz = maxScrollPanel.preferredSize();
	    ((TFP_ScrollPanel)maxScrollPanel).setPreferredSize(width, 
							       sz.height);
	}
	super.reshape(x, y, width, height);
    }

    public static void main(String[] args) {
	TransferFunction tf = new NumericTransferFunction(256, -1.5, 33.0);
//	TransferFunction tf = new NumericTransferFunction(103, 1.0, 100.0);

	SimpleFrame f = new SimpleFrame();
	f.setKillOnClose();
	TransferFunctionPanel tfp = new TransferFunctionPanel(tf, "Linear");
	f.add("Center", tfp);
	f.pack();
	f.show();
    }
}

class TFP_ScrollPanel extends Panel {

    Dimension prefsz = null;

    Label lab = null;
    TextField txt = null;
    Scrollbar sb = null;

    int hgap = 2;

    public TFP_ScrollPanel() { super(); }
    public TFP_ScrollPanel(Label l, Scrollbar s, TextField t) { 
	super(); 
	lab = l;
	txt = t;
	sb = s;

	setLayout(new BorderLayout(hgap, 0));

	if (lab != null) add("West", lab);
	if (sb  != null) add("Center", sb);
	if (txt != null) add("East", txt);
    }

    public void reshape(int x, int y, int width, int height) {
	if (sb != null && width > 0 && height > 0) {
	    int subwidth = width;
	    Dimension psz;
	    if (lab != null) {
		psz = lab.preferredSize();
		subwidth -= psz.width - hgap;
	    }
	    if (txt != null) {
		psz = txt.preferredSize();
		subwidth -= psz.width - hgap;
	    }
	    sb.resize(subwidth, height);
	}

//	setPreferredSize(height, width);
	super.reshape(x, y, width, height);
    }

    public void setPreferredSize(int width, int height) {
	if (width <= 0 || height <= 0) return;
	prefsz = new Dimension(width, height);
    }

    public void setPreferredSize(Dimension sz) {
	if (sz == null) {
	    prefsz = null;
	    return;
	}

	setPreferredSize(sz.width, sz.height);
    }

    public Dimension preferredSize() {
	return ( (prefsz == null) ? 
		    super.preferredSize() : 
		    new Dimension(prefsz.width, prefsz.height) );
    }
}


	
