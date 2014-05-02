/**
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-97, Board of Trustees of the University of Illinois
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
 **/

package apps.SciDat;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.awt.*;
import java.awt.image.*;
import java.applet.Applet;

import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.viewable.ComputerGraphicsViewable;
import ncsa.horizon.viewer.SciDat1Viewer;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.modules.ZoomControl;

public class SciDat1Applet extends Applet {

    protected SciDat1Viewer viewer = new SciDat1Viewer();
    protected ZoomControl zoomer = new ZoomControl(viewer);
    protected boolean isApplet = true;
    protected SD1A_URLButton getURL;
    public SD1A_URLTextField URLentry;
    protected Button browseButton;
    static FileDialog fileDialog;

    public SciDat1Applet() { super(); }
    public SciDat1Applet(boolean isApplet) { 
	super(); this.isApplet = isApplet; 
    }

    public void init() {
	String URLbase=null, previewLocation=null, imgFile=null, datadir=null;

	if (isApplet) {
	    datadir = getParameter("datadir");
	    if (datadir == null) datadir = "";
	    URLbase = getDocumentBase().toString();
	    if (! URLbase.endsWith("/")) {
		int p = URLbase.lastIndexOf('/');
		if (p > 0 && URLbase.charAt(p-1) != '/') 
		    URLbase = URLbase.substring(0, p+1);
	    }
	    imgFile = getParameter("imgFile");
	    if (imgFile == null)
		System.out.println("Warning: No image file specified");
	    previewLocation = URLbase + datadir + imgFile;
	}	
	
	if (previewLocation != null)
	    System.out.println ("looking for image in: " + previewLocation);

	viewer.setDrawPoint(true);
	setLayout(new BorderLayout());
	add("Center", viewer);
	add("West", zoomer);

	if (isApplet && previewLocation != null) 
	    newViewable(previewLocation);
    }

    public static void main(String args[]) {
        String fSep = null;
        String pwd = null;

//	String defurl = 
//	    "http://imagelib.ncsa.uiuc.edu/document/95.FY.01.06/fullprv.gif";
//  	String defurl = 
//            "file:/appl/data/rplante/Horizon/data/GalacticCenter.gif";
//	String defurl = 
//          "file:/afs/ncsa/projects/horizon/Horizon/data/GalacticCenter.gif";
	fSep = System.getProperty("file.separator");
	pwd = System.getProperty("user.dir");
        String imgFile = "GalacticCenter.gif";
	String defurl = new String("file:" + pwd + fSep + ".." + fSep + 
				   "data" + fSep + imgFile); 

	SimpleFrame f = new SimpleFrame("Scientific Image Viewer");
	f.setKillOnClose();
	SciDat1Applet vu = new SciDat1Applet(false);
	vu.init();
	f.setLayout(new BorderLayout());
	f.add("Center", vu);

	Panel src = new Panel();
	src.setFont(new Font("Helvetica", Font.PLAIN, 14));
	src.setLayout(new BorderLayout());
	vu.getURL = new SD1A_URLButton(vu, "Get: ");
	src.add("West", vu.getURL);
	vu.URLentry = new SD1A_URLTextField(vu, defurl, 30);
	src.add("Center", vu.URLentry);
	vu.browseButton = new SD1A_Browser(vu, "Browse...");
	src.add("East", vu.browseButton);
	if (vu.isApplet) vu.browseButton.disable();
	f.add("North", src);

	fileDialog = new FileDialog(f, "Load Image", FileDialog.LOAD);
	String cwd = System.getProperty("user.dir");
	String fs = System.getProperty("file.separator");
	fileDialog.setDirectory(cwd + fs + ".." + fs + "data");

	f.pack();
	f.show();
	vu.newViewable(defurl);
    }

    public boolean action(Event event, Object obj) {
	boolean handled = super.action(event, obj);

	System.out.println("Action!");
	if (event.target instanceof Button) {  // handle buttons
	    Button b = (Button) event.target;

	    if (b == browseButton) {
		fileDialog.show();             // pops up modal dialog

		String file = fileDialog.getFile();
		if (file == null) return( false );

		newViewable("file:" + file);
// 		try {
// 		    viewable = new TradImage( fileDialog.getDirectory()+file );
// 		} catch (java.io.IOException ex) {
// 		    throw new InternalError(ex.getMessage());
// 		}
// 		if (viewable != null) {
// 		    viewer.addViewable( viewable );
// 		    newViewable();
// 		}
	    }
	}
	else if (event.target == getURL || event.target == URLentry) {
	    System.out.println("getting URL image");
	    String text = URLentry.getText();
	    if (text.length() > 0) newViewable(text);
	    return true;
	}
	return handled;
    }

    public void newViewable(String urlstr) {
	Viewable img=null;
	System.out.println("Looking for " + urlstr);

	try { img = new ComputerGraphicsViewable( new URL(urlstr) ); }
	catch (MalformedURLException ex) { 
	    System.out.println(ex.getMessage()); 
	}
// 	catch (IOException ex) {
// 	    System.out.println(ex.getMessage());
// 	}
	if (img == null) {
	    System.out.println("Unable to create Viewable from URL.");
	    return;
	}

	viewer.addViewable(img);
	viewer.displayViewable();
    }

}

class SD1A_URLTextField extends TextField {
    SciDat1Applet parent=null;

    public SD1A_URLTextField() { super(); }
    public SD1A_URLTextField(SciDat1Applet app) { super(); parent = app; }
    public SD1A_URLTextField(SciDat1Applet app, int cols) { 
	super(cols); parent = app; 
    }
    public SD1A_URLTextField(SciDat1Applet app, String text) { 
	super(text); parent = app; 
    }
    public SD1A_URLTextField(SciDat1Applet app, String text, int cols) { 
	super(text, cols); parent = app; 
    }

    public boolean action(Event evt, Object what) {
	boolean handled = super.action(evt, what);
	if (evt.id == Event.ACTION_EVENT && what instanceof String) {
	    parent.newViewable((String) what);
	    return true;
	}

	return handled;
    }
}

class SD1A_URLButton extends Button {
    SciDat1Applet parent=null;

    public SD1A_URLButton() { super(); }
    public SD1A_URLButton(SciDat1Applet app) { super(); parent = app; }
    public SD1A_URLButton(SciDat1Applet app, String text) { 
	super(text); parent = app; 
    }

    public boolean action(Event evt, Object what) {
	boolean handled = super.action(evt, what);
	if (evt.id == Event.ACTION_EVENT && what instanceof String) {
	    parent.newViewable(parent.URLentry.getText());
	    return true;
	}

	return handled;
    }
}

class SD1A_Browser extends Button {
    SciDat1Applet parent=null;

    public SD1A_Browser() { super(); }
    public SD1A_Browser(SciDat1Applet app) { super(); parent = app; }
    public SD1A_Browser(SciDat1Applet app, String text) { 
	super(text); parent = app; 
    }

    public boolean action(Event evt, Object what) {
	boolean handled = super.action(evt, what);
	if (parent == null) return handled;

	parent.fileDialog.show();             // pops up modal dialog

	String file = parent.fileDialog.getFile();
	if (file == null) return( false );

	parent.newViewable("file:" + parent.fileDialog.getDirectory() + file);
	return true;
    }
}


