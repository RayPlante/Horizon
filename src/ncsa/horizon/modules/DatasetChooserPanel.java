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
 *  98jan15  rlp  Original version
 *  98feb02  rlp  tweaked layout around choice menu, added 
 *                  setWorkingDirectory() method.
 */

package ncsa.horizon.modules;

import java.awt.*;
import java.util.Properties;
import java.util.Observable;
import java.util.Observer;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.awt.SingleComponentLayout;

/*
 * a Panel for choosing and loading a Dataset.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class DatasetChooserPanel extends Panel {

    protected Button getIt = new Button("Load");
    protected TextField urlField = null;
    protected Choice defChoice = new DCP_Choice(this);
    protected Button store = new Button("Remember...");
    protected Button browse = new Button("Browse...");
    protected Button reset = new Button("Reload Current");
    protected FileDialog fileBrowser = null;
    protected Frame browserFrame = null;

    protected String current = null;
    protected String currentName = null;
    protected String wdir = null;

    protected Properties dsurls = new Properties();

    /**
     * the string that tells user to edit the test in the URL TextField
     */
    protected String editChoice = "Enter URL above";

    /**
     * the delegate Observerable
     */
    protected Observable loadAlerter = new DCP_Observable();

    boolean autoLoad = false;

    DCP_RememberWin win = null;

    /**
     * Create a blank DatasetChooserPanel 
     */
    public DatasetChooserPanel() { 
	this(null, 0, null);
    }

    /**
     * Create a blank DatasetChooserPanel 
     */
    public DatasetChooserPanel(int cols) { 
	this(null, cols, null);
    }

    /**
     * Create a DatasetChooserPanel with default string in URL TextField
     */
    public DatasetChooserPanel(String defURL) {
	this(defURL, 0, null);
    }

    /**
     * Create a DatasetChooserPanel with default string in URL TextField
     */
    public DatasetChooserPanel(String defURL, Frame parent) {
	this(defURL, 0, parent);
    }

    /**
     * Create a DatasetChooserPanel with default string in URL TextField
     * @param defURL  the string to appear in the URL TextField 
     * @param cols    the default number of columns in the URL TextField;
     *                the actual width may be larger depending on the layout
     *                of this Component in its Container.
     */
    public DatasetChooserPanel(String defURL, int cols) {
	this(defURL, cols, null);
    }

    /**
     * Create a DatasetChooserPanel with default string in URL TextField
     * @param defURL  the string to appear in the URL TextField (can be null).
     * @param cols    the default number of columns in the URL TextField;
     *                the actual width may be larger depending on the layout
     *                of this Component in its Container.  
     * @param parent  the Frame that will hold this Panel; this is needed if
     *                browsing of the local filesystem is desired.
     */
    public DatasetChooserPanel(String defURL, int cols, Frame parent) {
	super();
	browserFrame = parent;
	setFont(new Font("Helvetica", Font.PLAIN, 14));
	initData(defURL, cols);
	layoutComponents();
    }

    /**
     * initialize the internal data
     * @param defURL  the default URL String to put in the URL text field.
     */
    private void initData(String defURL, int cols) {
	defChoice.addItem(editChoice);

	int sl = cols;
	if (defURL != null && sl <= 0) sl = defURL.length();
	if (sl <= 0) sl = 50;
//	System.err.println("field width: " + sl);
	urlField = new TextField(sl);
	if (defURL != null) urlField.setText(defURL);

	if (browserFrame == null) browse.disable();

	reset.disable();
    }

    /**
     * layout the components.
     */
    protected void layoutComponents() {

	GridBagLayout b = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(b);
	c.gridwidth = c.gridheight = 1;
	c.anchor = c.CENTER;
	c.gridx = c.RELATIVE;
	c.gridy = 0;
	Insets zero = c.insets;

	c.weightx = c.weighty = 0;
	c.fill = c.BOTH;
	b.setConstraints(getIt, c);
	add(getIt);
	
	c.fill = c.HORIZONTAL;
	c.weightx = 1;
	c.gridwidth = c.REMAINDER;
	b.setConstraints(urlField, c);
	add(urlField);

 	c.fill = c.NONE;
 	c.gridy = c.RELATIVE;
 	c.weightx = c.weighty = 0;
 	c.insets = new Insets(2,0,6,10);
 	c.gridwidth = 3;
 	b.setConstraints(defChoice, c);
 	add(defChoice);
	
 	c.weightx = c.weighty = 1;
 	c.gridwidth = 1;
 	c.fill = c.BOTH;
 	c.insets = zero;
	b.setConstraints(store, c);
	add(store);

 	b.setConstraints(browse, c);
 	add(browse);
	
 	b.setConstraints(reset, c);
 	add(reset);
    }

    /**
     * add an Observer that wants to be alerted when there has been a 
     * request to load a dataset.
     */
    public void addObserver(Observer o) { loadAlerter.addObserver(o); }

    /**
     * return a reference to the actual Observable object used by this
     * Panel.
     */
    public Observable getObservable() { return loadAlerter; }

    /**
     * return a reference to the "Load" button
     */
    public Button getLoadButton() { return getIt; }

    /**
     * return a reference to the url TextField
     */
    public TextField getURLTextField() { return urlField; }

    /**
     * return a reference to the data Choice menu 
     */
    public Choice getDataChoice() { return defChoice; }

    /**
     * return a reference to the "Browse..." Button
     */
    public Button getBrowseButton() { return browse; }

    /**
     * return a reference to the "Reload Current" Button
     */
    public Button getReloadCurrentButton() { return reset; }

    /**
     * return the URL string currently in the URL TextField 
     */
    public String getURLString() { return urlField.getText().trim(); }

    /**
     * add predefined dataset URL to the choice menu
     * @param datasetName  the name that will appear on the Choice menu
     * @param datasetURL   the URL string that will return that dataset
     */
    public synchronized void addDatasetURL(String datasetName, 
					   String datasetURL) 
    {
	dsurls.put(datasetName, datasetURL);
	defChoice.addItem(datasetName);
	if (isValid()) invalidate();
    }

    public boolean action(Event e, Object what) {
	if (e.target == getIt || e.target == urlField) {
	    loadData();
	    return true;
	} 
	else if (e.target == defChoice) {
	    String dsname = defChoice.getSelectedItem();
	    doSelectDataset(dsname);
	    return true;
	}
	else if (e.target == store) {
	    rememberURL();
	    return true;
	}
	else if (e.target == browse) {
	    openFileBrowser();
	    return true;
	}
	else if (e.target == reset) {
	    reloadCurrentData();
	    return true;
	}

	return false;
    }

    /**
     * set whether a dataset will automatically be loaded when the File
     * browser window is closed.
     */
    public void setAutoFileLoad(boolean doIt) {  autoLoad = doIt; }

    /**
     * return whether a dataset will automatically be loaded when the File
     * browser window is closed.  This method will always return false if 
     * a call to canBrowse() would return false.
     */
    public boolean willAutoLoadFile() { return (autoLoad & canBrowse()); }

    /**
     * return whether a FileDialog window can be opened.
     */
    public boolean canBrowse() {  
	return (browse.isEnabled() && browserFrame != null); 
    }

    /**
     * set whether a FileDialog window can be opened.
     * @returns false if this state cannot be change; to enable browsing,
     *                a Frame must have been passed to this class's constructor.
     */
    public boolean setBrowsing(boolean allow) {
	if (browserFrame == null) return false;
	if (allow) 
	    browse.enable();
	else
	    browse.disable();
	return true;
    }

    /**
     * the default working directory for the file browser window
     * @returns false if this directory cannot be set (because a Frame was
     *                not passed to this object's constructor
     */
    public boolean setWorkingDirectory(String dirname) {
	if (browserFrame == null) return false;
	wdir = dirname;
	if (fileBrowser != null) fileBrowser.setDirectory( wdir );
	return true;
    }

    /**
     * open a FileDialog window for the selection of a file from the 
     * local filesystem.  This method does nothing if a call to canBrowse() 
     * would return false.
     */
    public void openFileBrowser() {
	if (canBrowse()) { 
	    if (fileBrowser == null) {
		fileBrowser = new FileDialog(browserFrame, "Load Dataset");
		System.err.println("cwd: " + wdir);
		if (wdir == null) wdir = System.getProperty("user.dir");
		fileBrowser.setDirectory( wdir );
	    }

	    fileBrowser.show();
	    String file = fileBrowser.getDirectory() + fileBrowser.getFile();

	    setURLText("file:" + file);

	    if (autoLoad) loadData();
	}
    }

    /**
     * return the URL string for the last dataset for which there was a 
     * load request, or null if a request has not yet been made.
     */
    public String getRequestedDataset() { return current; }

    /**
     * request that the dataset given by the string appearing in the 
     * URL TextField be loaded.  All observers will be alerted and given
     * the dataset's URL string as an argument.
     */
    public synchronized void loadData() {
	current = urlField.getText();
	currentName = null;

	String selitem = defChoice.getSelectedItem();
	if (! selitem.equals(editChoice)) currentName = selitem;

	reset.enable();
	System.err.println("Loading " + current);
	loadAlerter.notifyObservers(current);
    }

    /**
     * set the String appearing in the URL TextField.  This method will 
     * cause the TextField to become editable, and the dataset Choice menu
     * will be set to the "Enter URL above" choice.
     */
    public synchronized void setURLText(String url) {
	selectDataset(editChoice);
	urlField.setText(url);
    }

    /**
     * reload the last dataset that was loaded either via the "Load" button
     * or an explicit call to the loadData() method.  Nothing happens if 
     * no data has yet been loaded.  This method is called when the 
     * "Reload Current" button is pressed.  
     */
    public synchronized void reloadCurrentData() {
	if (current == null) return;

	if (currentName == null) 
	    setURLText(current);
	else
	    selectDataset(currentName);
	loadData();
    }

    /**
     * select the dataset with the given name.  This equivalent to making
     * a choice via the GUI.
     * @param name   one of the Strings that appears in the dataset Choice
     *               menu
     */
    public synchronized void selectDataset(String name) {
	if (dsurls.getProperty(name) != null || name.equals(editChoice)) {
	    defChoice.select(name);

	    // the DCP_Choice.select() will call our doSelectDataset(name)
	    // method.
	}
    }
	
    /**
     * respond to a change in the dataset Choice menu
     */
    protected synchronized void doSelectDataset(String name) {

//	boolean wantEdit = name.equals(editChoice);
	String url = dsurls.getProperty(name);
	boolean wantEdit = (url == null);
	if (urlField != null) {
	    urlField.setEditable(wantEdit);

	    if (! wantEdit) {
		if (url == null) url = "";
//	        System.err.println("New URL for " + name + ": " + url);
		urlField.setText(url);
	    }
	}
    }

    public synchronized void rememberURL() {
	if (win == null) win = new DCP_RememberWin(this);
	win.open(urlField.getText());
    }

    public static void main(String args[]) {
	String url = 
	    "http://imagelib.ncsa.uiuc.edu/Horizon/examples/data/io.gif";

	SimpleFrame f = new SimpleFrame();
	f.setKillOnClose();
	DatasetChooserPanel dcp = new DatasetChooserPanel(url, 48, f);
	dcp.addDatasetURL("US Map", 
	    "http://imagelib.ncsa.uiuc.edu/Horizon/examples/data/us.gif");
	dcp.setAutoFileLoad(true);
//	dcp.setFont(new Font("Courier", Font.PLAIN, 14));

	SingleComponentLayout scp = new SingleComponentLayout(2,2,2,2);
	f.setLayout(scp);
// 	f.add(new Button("Yes!"));
// 	f.add(new Button("no"));
	f.add(dcp);
//	f.add("Center", dcp);
	f.pack();
	f.show();
    }

}

class DCP_Choice extends Choice {

    DatasetChooserPanel boss = null;

    public DCP_Choice(DatasetChooserPanel parent) {
	super();
	boss = parent;
    }

    public void select(int pos) {
	super.select(pos);
	boss.doSelectDataset(getItem(pos));
    }
}

class DCP_RememberWin extends Dialog {

    protected DatasetChooserPanel boss = null;

    protected Button ok = new Button("OK");
    protected Button cancel = new Button("Cancel");
    protected TextField urltf = new TextField("", 50);
    protected TextField filetf = new TextField("", 15);

    public DCP_RememberWin(DatasetChooserPanel boss) {
	super(new Frame(), "Remember URL", false);
	this.boss = boss;
	layoutComponents();
    }

    void layoutComponents() {
	Frame dummy = new Frame();
	setFont(getFont());

	GridBagLayout b = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(b);
	c.gridwidth = c.gridheight = 1;
	c.anchor = c.WEST;
	c.fill = c.BOTH;
	c.gridx = c.gridy = c.RELATIVE;

	Label l = new Label("URL:");
	b.setConstraints(l, c);
	add(l);

	c.gridwidth = c.REMAINDER;
	b.setConstraints(urltf, c);
	add(urltf);

	c.gridwidth = 1;
	l = new Label("Name: ") ;
	b.setConstraints(l, c);
	add(l);

	c.gridwidth = 3;
	c.fill = c.VERTICAL;
	b.setConstraints(filetf, c);
	add(filetf);

	c.anchor = c.CENTER;
	c.gridheight = 1;
	c.gridy = 2;
	c.gridwidth = 4;
	c.weightx = c.weighty = 0;
	c.fill = c.NONE;

	c.gridx = 2;
	b.setConstraints(ok, c);
	add(ok);
	c.gridx = 5;
	b.setConstraints(cancel, c);
	add(cancel);

	pack();
    }

    public void open(String url) {
	urltf.setText(url);
	filetf.setText(defName(url));
	boss.store.disable();
	show();
    }
    
    private String defName(String from) {
	int pos = from.lastIndexOf(System.getProperty("file.separator"));
	if (pos < 0) return "";

	return from.substring(pos+1);
    }

    public boolean action(Event e, Object what) {
	if (e.target == ok) {
	    hide();
	    boss.store.enable();
	    boss.addDatasetURL(filetf.getText(), urltf.getText());
	    return true;
	}
	else if (e.target == cancel) {
	    hide();
	    boss.store.enable();
	    return true;
	}

	return false;
    }
}

class DCP_Observable extends Observable {
    public DCP_Observable() { super(); }

    public void notifyObservers() {
	setChanged();
	super.notifyObservers();
    }

    public void notifyObservers(Object arg) {
	setChanged();
	super.notifyObservers(arg);
    }
}
