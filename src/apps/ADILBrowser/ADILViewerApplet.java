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
package apps.ADILBrowser;

import java.applet.Applet;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

import ncsa.horizon.viewable.ComputerGraphicsViewable;
import ncsa.horizon.viewer.ADILViewer;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.coordinates.systems.*;
import ncsa.horizon.coordinates.FITSCoordMetadata;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.util.*;

/**
 * An applet for browsing GIF preview images from the NCSA Astronomy Digital 
 * Image Library (ADIL).  <p>
 *
 * The ADIL is an on-line library of astronomical images in FITS format.  For
 * every FITS image in the Library there is a preview GIF version available
 * for browsing over the Web.  In general, these preview images download
 * more quickly than their FITS counterparts.  The ADIL also provides access
 * to the FITS headers separately.  This applet allows users to browse a
 * preview image and its related header.  Through use of the ADILViewer,
 * users may:
 * <ul>
 *    <li> view zoomed regions of the preview image
 *    <li> track world coordinate positions in realtime
 *    <li> display the FITS header
 * </ul> <p>
 *
 * <b> the Applet Interface </b><p>
 *
 * This applet contains one visual component: ncsa.horizon.viewer.ADILViewer. 
 * This viewer provides two display areas side-by-side.  The left side 
 * displays a scaled version of the requested slice.  The right side 
 * displays zoomed version of the image.  Clicking positions in the left 
 * display will cause a new zoomed image to appear in the right display 
 * centered over the selected pixel.  The user may control how much the
 * right image is zoomed via buttons along the bottom of the viewer panel.  
 * The user may optionally choose "Region" zooming which allows a box 
 * to be drawn in the left display, representing the region to be shown in 
 * the right display.  The coordinate positions corresponding
 * to the pixel below the mouse are displayed as the mouse is moved about
 * within either display.  Users may display the image's text header by 
 * clicking on the header button.  <p>
 *
 * <b> Using the Applet within an HTML Document </b><p>
 * 
 * Although this applet was developed for use with the ADIL, the data it
 * displays need not originate from the ADIL's Web server.  The information
 * needed to download the data are passed to the applet from the HTML 
 * document via the following parameters:
 * <dl>
 *    <dd> URLbase
 *    <dt> the base URL to be used for obtaining the data specified by 
 *         imgFile, header1, and header2.  This should <em>not</em> end 
 *         in a forward slash.  <p>
 * 
 *    <dd> imgFile 
 *    <dt> the filename of the GIF or JPEG preview image.  This file 
 *         will be downloaded to the applet via a URL formed by appending 
 *         a slash to the URLbase parameter followed by this filename. <p>
 * 
 *    <dd> header1
 *    <dt> the filename containing the header text.  If this parameter is 
 *         set, the file is downloaded (prior to downloading the preview 
 *         image) via a URL formed by appending a slash to the URLbase 
 *         parameter followed by this filename.  <p>
 *
 *         If downloaded successfully, the header is parsed for coordinate
 *         information assuming a modified FITS header format.  This format
 *         differs from standard FITS header format in that linefeeds appear
 *         at the end of every 80-character card-record.  (Eventually, this
 *         applet will be trained to take the standard format as well.)  <p>
 *
 *    <dd> header2
 *    <dt> a second (optional) file containing additional header information.
 *         If this parameter is set, the file is downloaded only if and when
 *         the user clicks the "Header" button for the first time.  The file
 *         is downloaded via a URL formed by appending a slash to the URLbase 
 *         parameter followed by this filename.  <p>
 *
 *         This parameter is provided because the ADIL keeps the keyword 
 *         and the "HISTORY" portions of the header in seperate files; thus,
 *         this parameter is used to specify the HISTORY portion.  When the 
 *         header is displayed, this text is appended to the text specified 
 *         by the header1 parameter. <p>
 *
 *    <dd> headerButtonText
 *    <dt> the text to display on the header display request button.  If not
 *         specified, the default "FITS Header Text" is printed on the button.
 *         <p>
 * </dl>
 *
 * Of the above parameters, only URLbase and imgFile are required.   
 *
 * @version 0.1 alpha
 * @author Ray Plante <rplante@ncsa.uiuc.edu>
 * @author Daniel Goscha
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class ADILViewerApplet extends Applet
{
    protected ADILViewer viewer;
    protected AVAp_Viewable data=null;
    protected boolean isApplet = true;
    protected Thread imageLoader=null;
    protected AVAp_HeaderLoader hdrldr=null;

    protected AVAp_Button getURL;
    protected TextField URLentry;
    protected AVAp_Button browseButton;
    protected FileDialog fileDialog;

    protected URL hdrURL1=null, hdrURL2=null;
    protected String hdr1=null;

    public ADILViewerApplet() { super(); }
    public ADILViewerApplet(boolean isApplet) { 
	super(); 
	this.isApplet = isApplet;
    }

    public void init() {
        String URLbase=null, previewLocation=null, imgFile=null,
	       header1=null, header2=null, headerButtonText=null;

	if (isApplet) {
	    headerButtonText = getParameter("headerButtonText");
	}

	viewer = new ADILViewer(headerButtonText);

	if (isApplet) {

	    // get input parameters
	    URLbase = getParameter("URLbase");            
	    if (URLbase == null) 
                URLbase = "http://imagelib.ncsa.uiuc.edu/project/fullprvimg";
            imgFile = getParameter("imgFile");
            if (imgFile == null)
                System.out.println ("Warning: No image file specified");
            previewLocation = URLbase + "/" + imgFile;
	    header1 = getParameter("header1");
	    header2 = getParameter("header2");

	    try {
		if (header1 != null) hdrURL1 = new URL(URLbase + "/" + 
						       header1);
		if (header2 != null) hdrURL2 = new URL(URLbase + "/" + 
						       header2);
	    } catch (MalformedURLException ex) {
		System.out.println("Bad header URL: " + ex.getMessage());
	    }

	    hdrldr = new AVAp_HeaderLoader(this, hdr1, hdrURL2);
	    viewer.setHeaderFetcher(hdrldr);
		
	    // start downloading image
	    Thread imageLoader = new Thread(
		new AVAp_ImageLoader(this, previewLocation, true));
	    int prio = imageLoader.getPriority() - 3;
	    if (prio < Thread.MIN_PRIORITY) prio = Thread.MIN_PRIORITY;
	    imageLoader.setPriority(prio);
	    imageLoader.start();
        }
        if (previewLocation != null)
            System.out.println ("looking for image in: " + previewLocation);

	add(viewer);
    }

    public CoordinateSystem getCoordSys(URL header) {
	FITSCoordMetadata md = new FITSCoordMetadata(2);
	DataInputStream hdr = null;
	StringBuffer buf = new StringBuffer();
	String line;
	CoordinateSystem csys=null;
	boolean haveRead = false;

	System.err.println("Downloading header info: " + header);

	if (header != null) {
	  try {
	    hdr = new DataInputStream(new BufferedInputStream(
		                                header.openStream()));
	    while ((line = hdr.readLine()) != null) {
		haveRead = true;
		if (line.startsWith("END") &&
		    (line.length() == 3 || 
		     line.charAt(3) == '/' || line.charAt(3) == ' ')) break;

		buf.append(line + "\n");

		md.scanHeaderCard(line);
	    }
	  }
	  catch (IOException e) {
	    buf.append("IO Exception reading FITS header");
	  }
	}
	else {
	  buf.append("No (primary) header text available");
	}
	if (! haveRead) System.err.println("Failed to get header from server");

	// updates various deprecated FITS conventions
	md.modernize();

	if (hdrldr != null) hdrldr.setPart1(buf.toString());

	try {
	    csys = new SphLinCoordinateSystem(md);
	} catch (IllegalTransformException ex) {
	    System.err.println("Warning: " + ex.getMessage() + "\n" +
			       "Using a default coordinate system.");
	    csys = new CoordinateSystem(new CoordMetadata(2));
	}
	
	return csys;
    }

    public static void main(String args[]) {
	String defurl = (args != null && args.length > 0) ?
	    args[0] :
	    "http://imagelib.ncsa.uiuc.edu/document/95.FY.01.06/fullprv.gif";

	SimpleFrame f = new SimpleFrame("ADIL Browser");
	ADILViewerApplet vu = new ADILViewerApplet(false);
	vu.init();
        f.setLayout(new BorderLayout());
        f.add("Center", vu);
	f.setKillOnClose();

        Panel src = new Panel();
        src.setFont(new Font("Helvetica", Font.PLAIN, 14));
        src.setLayout(new BorderLayout());
        vu.getURL = new AVAp_Button("Get: ", vu);
        src.add("West", vu.getURL);
        vu.URLentry = new TextField(defurl, 30);
        src.add("Center", vu.URLentry);
        vu.browseButton = new AVAp_Button("Browse...", vu);
        src.add("East", vu.browseButton);
        if (vu.isApplet) vu.browseButton.disable();
        f.add("North", src);

        vu.fileDialog = new FileDialog(f, "Load Image", FileDialog.LOAD);

        f.pack();
        f.show();
    }

    public boolean action(Event event, Object obj) {

        if (event.target == browseButton) {  // handle buttons
	    fileDialog.show();             // pops up modal dialog

	    String file = fileDialog.getFile();
	    if (file == null) return( false );

	    newViewable("file:" + fileDialog.getDirectory() + file, false);
	    if (isApplet) 
		showStatus("Downloading requested images...");
	    else 
		System.out.println("Downloading requested images...");
        }
        else if (event.target == getURL || event.target == URLentry) {
            System.out.println("getting URL image");
            String text = URLentry.getText();
            if (text.length() > 0) {
		newViewable(text, false);
		if (isApplet) 
		    showStatus("Downloading requested images...");
		else 
		    System.out.println("Downloading requested images...");
	    }
        }
        return false;
    }

    public void newViewable(String urlstr, boolean gethdr) {
	if (imageLoader != null && imageLoader.isAlive()) imageLoader.stop();
	Thread imageLoader = 
	    new Thread(new AVAp_ImageLoader(this, urlstr, gethdr));
	imageLoader.start();
    }
}

class AVAp_ImageLoader implements Runnable {

    ADILViewerApplet parent=null;
    String urlstr=null;
    boolean gethdr=false;

    AVAp_ImageLoader() { }
    AVAp_ImageLoader(String urlstr) { this.urlstr = urlstr; }
    AVAp_ImageLoader(ADILViewerApplet parent, String urlstr) { 
	this.parent = parent;
	this.urlstr = urlstr;
    }
    AVAp_ImageLoader(ADILViewerApplet parent, String urlstr, boolean gethdr) { 
	this(parent, urlstr);
	this.gethdr = gethdr;
    }

    public void run() {
        AVAp_Viewable img=null;
	CoordinateSystem csys=null;
	if (urlstr == null) return;
        System.out.println("Looking for " + urlstr);

	if (gethdr && parent != null) 
	    csys = parent.getCoordSys(parent.hdrURL1);

        try { img = new AVAp_Viewable( new URL(urlstr) ); }
        catch (MalformedURLException ex) { 
            System.out.println(ex.getMessage()); 
        }
        if (img == null) {
            System.out.println("Unable to create Viewable from URL.");
            return;
        }

	if (csys != null) img.setCoordSys(csys);

	if (parent != null) {
	    parent.data = img;
	    parent.viewer.addViewable(img);
	    parent.viewer.displayViewable();
	}

	return;
    }
}

class AVAp_Viewable extends ComputerGraphicsViewable {

    public AVAp_Viewable(URL in) { super(in); }

    public void setCoordSys(CoordinateSystem use) { 
	if (use != null) coord = use; 
    }
}	

class AVAp_HeaderLoader implements Runnable {

    ADILViewerApplet parent=null;
    String hdr1=null;
    URL hdrURL2=null;

    public AVAp_HeaderLoader(ADILViewerApplet parent, 
			    String hdr1, URL hdrURL2) { 
	this.hdr1 = hdr1;
	this.hdrURL2 = hdrURL2;
	this.parent = parent; 
    }
    public AVAp_HeaderLoader(String hdr1, URL hdrURL2) { 
	this.hdr1 = hdr1; 
	this.hdrURL2 = hdrURL2;
    }

    public void setPart1(String hdr1) { this.hdr1 = hdr1; }

    public void run() {
	StringBuffer buf = new StringBuffer();

	if (hdrURL2 == null) {
	    buf.append(hdr1);
	    buf.append("END\n");
	}
	else { 
	    
	    DataInputStream hdr=null;

	    String line;
	    if (hdr1 != null) buf.append(hdr1);

	    System.out.println("Downloading second half of header...");
	    try {
		hdr = new DataInputStream(new BufferedInputStream(
		    hdrURL2.openStream()));
		while ((line = hdr.readLine()) != null) {
		    buf.append(line + "\n");
		}
	    } 	
	    catch (IOException e) {
		buf.append("IO Exception reading FITS header");
	    }
	}

	if (parent != null) parent.viewer.setHeader(buf.toString());
    }
}

class AVAp_Button extends Button {

    ADILViewerApplet parent=null;

    AVAp_Button(String label, ADILViewerApplet parent) {
	super(label);
	this.parent = parent;
    }
    AVAp_Button(ADILViewerApplet parent) {
	super();
	this.parent = parent;
    }

    public boolean action(Event ev, Object what) {
	return ((parent == null) ? false : parent.action(ev, what));
    }
}


