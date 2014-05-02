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
 *  98jan19  rlp  updated for NdArray* move to ncsa.horizon.data
 */
package apps.FITSDemo;

import java.awt.*;
import java.applet.Applet;
import java.util.Observer;
import java.util.Observable;
import java.net.URL;
import java.net.MalformedURLException;

import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.awt.SingleComponentLayout;
import ncsa.horizon.viewer.ExtendedGraphicsSelectionViewer;
import ncsa.horizon.viewable.FITSFuncViewable;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.data.NumericTransferFunction;
import ncsa.horizon.data.TransferFunction;
import ncsa.horizon.modules.*;
import ncsa.horizon.util.*;
import misc.ScrollablePanel;

public class FITSImageBrowser extends Applet implements Observer {

    static TextArea msgArea = null;
    DatasetChooserPanel dcp = null;
    TransferFunctionPanel tfPanel = null;
    Frame tfFrame = null;

    ExtendedGraphicsSelectionViewer vu = 
        new ExtendedGraphicsSelectionViewer(320,320,false);
    FIB_MenuFrame vuFrame = null;

    Viewable data = null;
    TransferFunction transf = null;
    boolean firstView = false;

    public FITSImageBrowser() {  this(null); }
	
    public FITSImageBrowser(DatasetChooserPanel p) {
	dcp = p;
	if (dcp == null) dcp = new DatasetChooserPanel(48);

	if (dcp.canBrowse()) {
	    String cwd = System.getProperty("user.dir");
	    String fs = System.getProperty("file.separator");
	    String base;
	    StringBuffer wd = new StringBuffer();

	    int fsp = cwd.lastIndexOf(fs);
	    if (fsp <= 0) 
		wd.append(cwd + fs + ".." + fs);
	    else 
		wd.append(cwd.substring(0,fsp+1));
	    wd.append( "data" );
	    dcp.setWorkingDirectory(wd.toString());
	    base = new String("file:" + wd + fs);

	    dcp.addDatasetURL("Evolved Star IRC+10216", 
			      base + "IRC+10216_CN.fits");

	    dcp.selectDataset("Evolved Star IRC+10216");
	}

	init();
    }

    private boolean canConnect(String host) {
	try {
	    SecurityManager smgr = System.getSecurityManager();
	    if (smgr != null) smgr.checkConnect(host, -1);
	}
	catch (SecurityException ex) {
	    return false;
	}

	return true;
    }

    public void init() {

	if (dcp == null) new DatasetChooserPanel(48);
	String ADILhost = "imagelib.ncsa.uiuc.edu";
	if (canConnect(ADILhost)) {
	    String URLbase = "http://" + ADILhost + "/project/download/";
	    dcp.addDatasetURL("The \"Sickle\"", URLbase + "95.FY.01.07");
	    dcp.addDatasetURL("NGC 4258 in H-alpha", URLbase + "95.RP.05.05");
	    if(! dcp.canBrowse()) dcp.selectDataset("The \"Sickle\"");
	}

	dcp.addObserver(this);

	setLayout(new BorderLayout());
	add("North", dcp);
	msgArea = new TextArea(10, 48);
	msgArea.setEditable(false);
	add("Center", msgArea);

	msgArea.appendText("To Load an Image...\n" + 
			   "  * selected a dataset from the menu, or\n" +
			   "  * select \"Enter URL above\" from the menu and " +
			   "type in a URL");
	if (dcp.canBrowse()) msgArea.appendText(", or\n" + 
			   "  * click the \"Browse...\" button to select a " +
			   "local file");
	msgArea.appendText("\nthen click \"Load\"\n");

	vuFrame = new FIB_MenuFrame("Image Viewer", this, vu);
    }

    public void stop() {
	if (tfFrame != null) tfFrame.dispose();
	if (vuFrame != null) vuFrame.dispose();
    }

    public void update(Observable o, Object arg) {
	if (o == dcp.getObservable()) {
	    try {
		loadFITS(new URL((String) arg));
	    } catch (MalformedURLException ex) {
		System.err.println("Bad Dataset URL: " + ex.getMessage() +
				   "\n  loading operation aborted.");
	    }
	}
    }

    void loadFITS(URL url) { 

	// display the Viewer
	vuFrame.show();

	// create the Viewable object which needs to be connected to
	// a TransferFunction
	if (transf == null) transf =  new NumericTransferFunction();

	try {
	    data = new FITSFuncViewable(url, transf);
	} catch (InstantiationException e) {
	    System.err.println("Failed to load data: " + e.getMessage() +
			       "\n  display request aborted");
	    return;
	}

	// attach the TransferFunction to its GUI
	if (tfPanel != null) tfPanel.setTransferFunction(transf, "Linear"); 

	// display the data in the viewer
	msgArea.appendText("Loading " + url);
	displayViewable(data);

	if (! firstView) {
	    msgArea.appendText("\nYou can use the mouse to make the " +
			       "following selections:\n");
	    msgArea.appendText("  Pixel:   Left button\n");
	    msgArea.appendText("  Box:     Right button (META-Left button)\n");
	    msgArea.appendText("  Line:    Middle button (ALT- or " +
			       "SHIFT-Left button)\n");
	    firstView = true;
	}

	if (vuFrame != null && vuFrame.mdframe != null && 
	    vuFrame.mdframe.isVisible()) 
	    vuFrame.showMDViewer(data.getMetadata());
    }

    public void showTransferFunctionPanel() {

	if (tfFrame == null) {
	    tfFrame = new SimpleFrame("Transfer Function Control");
	    tfFrame.setLayout(new SingleComponentLayout(2,2,2,2));
	}

	if (tfPanel == null) {
	    tfPanel = new TransferFunctionPanel();
	    if (transf != null) tfPanel.setTransferFunction(transf, "Linear");
	    tfPanel.registerViewer(vu);
	    tfFrame.add(tfPanel);
	    tfFrame.pack();
	}

	tfFrame.show();
    }

    public void displayViewable(Viewable v) {
	vu.addViewable(v);
	vu.displaySlice();
    }

    public static void main(String[] args) {

	SimpleFrame f = new SimpleFrame("FITS Image Browser");
	f.setKillOnClose();

	DatasetChooserPanel dcp = new DatasetChooserPanel(null, 48, f);
	FITSImageBrowser fib = new FITSImageBrowser(dcp);

	f.setLayout(new SingleComponentLayout(2,2,2,2));
	f.add(fib);
	f.pack();
	f.show();

    }
}

class FIB_MenuFrame extends Frame {

    protected ExtendedGraphicsSelectionViewer viewer = null;
    protected FITSImageBrowser boss = null;
    protected MenuItem slmi, rangemi, zmmi, grmi, cmmi, mdmi;
    protected CheckboxMenuItem freemi, posselmi;
    
    protected Frame slframe = null, selframe = null, lutframe = null,
	            zmframe = null, mdframe = null;

    public void dispose() {
	if (slframe  != null) slframe.dispose();
	if (selframe != null) selframe.dispose();
	if (lutframe != null) lutframe.dispose();
	if (zmframe  != null) zmframe.dispose();
	if (mdframe  != null) mdframe.dispose();
	super.dispose();
    }

    protected MetadataViewer mdvu = null;

    public FIB_MenuFrame(String title, FITSImageBrowser parent, 
			 ExtendedGraphicsSelectionViewer v) 
    {
	super(title);
	viewer = v;
	boss = parent;
	layoutFrame();
    }

    protected void layoutFrame() {
	MenuBar mb = new MenuBar();

	Menu dmenu = new Menu("Data");
	slmi = new MenuItem("Slice...");
	rangemi = new MenuItem("Range...");
	cmmi = new MenuItem("Palettes...");
	mdmi = new MenuItem("Metadata...");
	dmenu.add(slmi);
	dmenu.add(rangemi);
	dmenu.add(cmmi);
	dmenu.add(mdmi);
	mb.add(dmenu);

	Menu cmenu = new Menu("Control");
	zmmi = new MenuItem("Zoom...");
	grmi = new MenuItem("Selections...");
	freemi = new CheckboxMenuItem("Free Position");
	posselmi = new CheckboxMenuItem("Position On Select");
	
	cmenu.add(zmmi);
	cmenu.add(grmi);
	cmenu.add(freemi);
	cmenu.add(posselmi);
	mb.add(cmenu);

	setMenuBar(mb);

	setLayout(new SingleComponentLayout(2,2,2,2));
	add(viewer);

	pack();
    }

    public boolean handleEvent(Event e) {
	if (e.target == zmmi) {
//	    System.out.println("ZOOM!  " + e.id);
	    showZoomFrame();
	    return true;
	}
	else if (e.target == rangemi) {
	    boss.showTransferFunctionPanel();
	    return true;
	}
	else if (e.target == slmi) {
	    showSliceChooser();
	    return true;
	}
	else if (e.target == cmmi) {
	    showLUTChooser();
	    return true;
	}
	else if (e.target == mdmi) {
	    Metadata use = null;
	    if (boss.data != null) use = boss.data.getMetadata();
	    if (use == null) {
		System.err.println("Warning: no metadata available");
		use = new Metadata();
	    }
	    showMDViewer(use);
	    return true;
	}
	else if (e.target == grmi) {
	    showSelectionsEditor();
	    return true;
	}
	else if (e.target == freemi) {
	    viewer.setFreePosition(freemi.getState());
	    return true;
	}
	else if (e.target == posselmi) {
	    viewer.setPositionOnSelect(posselmi.getState());
	    return true;
	}
	return false;
    }

    public void showSliceChooser() {
// 	if (slframe == null) {
// 	    slframe = new SimpleFrame("Data Slice Selection");
// // 	    slframe.setLayout(new SingleComponentLayout(2,2,2,2));
// // 	    slframe.add(viewer.getGui()[1]);
// 	    slframe.setLayout(new BorderLayout());
// 	    Component c = viewer.getGui()[1];
// 	    if (c == null) System.err.println("Ack!");
// 	    System.err.println("slice! " + c);
// 	    slframe.add("Center", c);
// 	    slframe.pack();
// 	}

// 	slframe.show();

	Frame f = (Frame) viewer.getGui()[1];
	f.show();
    }

    public void showLUTChooser() {
	if (lutframe == null) {
	    lutframe = new SimpleFrame("Palette Chooser");
	    lutframe.setLayout(new SingleComponentLayout(2,2,2,2));
	    lutframe.add(viewer.getGui()[2]);
	    lutframe.pack();
	}

	lutframe.show();
    }

    public void showMDViewer(Metadata md) {
	mdvu = new MetadataViewer(md);
	mdvu.display();

	if (mdframe == null) {
	    ScrollablePanel span = new ScrollablePanel(mdvu);
	    mdframe = new SimpleFrame("Metadata Viewer");
	    mdframe.setLayout(new SingleComponentLayout(2,2,2,2));
//	    mdframe.setLayout(new BorderLayout(2,2));
	    mdframe.add(span);
	    mdframe.pack();
	    mdframe.resize(600, 400);
	}

	
	mdframe.show();
    }

    public void showZoomFrame() {
	if (zmframe == null) {
	    zmframe = new SimpleFrame("Zoom/Pan Control");
	    zmframe.setLayout(new SingleComponentLayout(2,2,2,2));
	    zmframe.add(new ZoomControl(viewer));
	    zmframe.pack();
	}

	zmframe.show();
    }

    public void showSelectionsEditor() {
	if (selframe == null) {
	    selframe = new SimpleFrame("Graphical Selections Editor");
	    selframe.setLayout(new SingleComponentLayout(2,2,2,2));
	    selframe.add(viewer.getGui()[0]);
	    selframe.pack();
	}

	selframe.show();
    }

}    
