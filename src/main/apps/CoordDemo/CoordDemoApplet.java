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

package apps.CoordDemo;

import java.applet.Applet;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.*;
import java.util.*;

import ncsa.horizon.viewable.ComputerGraphicsViewable;
import ncsa.horizon.viewer.PosTrackerViewer;
import ncsa.horizon.coordinates.*;
import ncsa.horizon.coordinates.systems.*;
import ncsa.horizon.coordinates.formatters.*;
import ncsa.horizon.awt.SimpleFrame;
import ncsa.horizon.util.*;

/**
 * This applet demonstrates the use of the coordinate related-classes
 * through the use of the PosTrackerViewer.  
 */
public class CoordDemoApplet extends Applet {

    // User interface components:
    // The initial user panel
    TextField  urlField;              // entry field for URL
    Choice     dataChoice;            // choice of datasets
    Button     browseButton;          // to bring up browse dialog
    Button     displayButton;         // to bring up viewer frame
    TextArea   msgArea;               // for output to user
    static FileDialog fileDialog;

    // Viewer panel
    PosTrackerViewer  viewer;         // panel for visualization &
                                      //   and coordinate postions
    SimpleFrame       imageFrame;     // frame to hold viewer panel

    // the data of interest
    ComputerGraphicsViewable viewable;      // main viewable

    // a list of filenames for special datasets that we know about
    String[] datafiles = { "GalacticCenter.gif", "hurricane.jpg", "usmap.gif",
                           "io.gif" };

    // some other miscellaneous data
    final static String titleString = "Using Coordinates: an Example";
    static boolean started = false;
    boolean isApplet = true;
    String dataBase = null;

    public CoordDemoApplet() { super(); }
    public CoordDemoApplet(boolean isApplet) { 
	super(); this.isApplet = isApplet; 
    }
    
    /**
     * If we run this class as an application, execution begins with this 
     * method.
     */
    public static void main(String argv[]) {

	SimpleFrame mainFrame = null;
	CoordDemoApplet app = new CoordDemoApplet(false);

	// create a frame to hold our applet
	mainFrame = new SimpleFrame( titleString );
	mainFrame.setKillOnClose();
	mainFrame.add("Center", app);

	// initialize and start the applet
	String cwd = System.getProperty("user.dir");
	String fs = System.getProperty("file.separator");
	String datadir = new String(cwd + fs + ".." + fs + "data");

	app.dataBase = new String("file:" + datadir);
	app.init();
	app.start();

	fileDialog = new FileDialog(mainFrame, "Load Image", FileDialog.LOAD);
	fileDialog.setDirectory(datadir);

	// resize, etc.
	mainFrame.pack();
	mainFrame.layout();
	mainFrame.validate();
	mainFrame.show();
    }

    /**
     * Creates the user interface and initializes the applet
     */
    public void init() {

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	Insets gbi = gbc.insets;
	setLayout(gbl);
	gbc.ipadx = gbc.ipady = 4;

	// Create a menu of data sets.  Normally, the datasets would contain
	// their own coordinate information which could be extracted; however,
	// for the sake of example, we will hard-code the coordinate 
	// information into these datasets.  The last item will allow one to 
	// enter a URL.
	//
	dataChoice = new Choice();
	dataChoice.addItem("Galactic Center");
	dataChoice.addItem("Hurricane");
	dataChoice.addItem("Map of the US");
	dataChoice.addItem("Io, Jupiter's moon");
	dataChoice.addItem("URL:");
	dataChoice.select(0);
	gbc.anchor = gbc.WEST;
	gbl.setConstraints(dataChoice, gbc);
	add(dataChoice);

	// Determine the URL-directory containing our default data
	//
	String defImage = null;
	String fSep = null;
	if (isApplet) {

	    // If we are an applet, the data directory will is set by
	    // the document base and the "datadir" applet parameter.
	    //
	    String datadir = getParameter("datadir");
	    if (datadir == null) datadir = "";
	    String URLbase = getDocumentBase().toString();
	    if (! URLbase.endsWith("/")) {
		int p = URLbase.lastIndexOf('/');
		if (p > 0 && URLbase.charAt(p-1) != '/') 
		    URLbase = URLbase.substring(0, p+1);
	    }
	    defImage = getParameter("imgFile");
	    dataBase = URLbase + datadir;
	    fSep = "/";
	}	
	else if (dataBase == null) {

	    // If we are running as an application, we will expect the 
	    // data to be in ../data, relative to the current working 
	    // directory
	    //
	    fSep = System.getProperty("file.separator");
	    String pwd = System.getProperty("user.dir");
	    dataBase = new String("file:" + pwd + fSep + ".." + fSep + 
				  "data"); 
	    defImage = "io.gif";
	}
    
	// Create a text field for the case when the user wants to provide
	// an arbitrary URL to an image.  
        urlField = new TextField(dataBase + fSep + defImage);
	if (! dataChoice.getSelectedItem().equals("URL:")) urlField.disable();
	gbc.gridwidth = gbc.REMAINDER;
	gbc.fill = gbc.HORIZONTAL;
	gbc.insets = new Insets(0, 10, 0, 0);
	gbl.setConstraints(urlField, gbc);
	add(urlField);

	// button for requesting display of requested data
	displayButton = new Button("Display Data...");
	gbc.gridwidth = 1;
	gbc.fill = gbc.NONE;
	gbc.insets = gbi;
	gbl.setConstraints(displayButton, gbc);
	add(displayButton);
	
	// button for bringin up a file browser dialog window
	browseButton = new Button("Browse...");
	if (isApplet) browseButton.disable();
	gbc.gridwidth = gbc.REMAINDER;
	gbc.anchor = gbc.EAST;
	gbl.setConstraints(browseButton, gbc);
	add(browseButton);

	// Create a place to print messages
	msgArea = new TextArea(8, 85);
	msgArea.setEditable(false);
	gbc.fill = gbc.BOTH;
	gbl.setConstraints(msgArea, gbc);
	add(msgArea);

	show();
	repaint();

	// create dynamic components
//	viewer = new PosTrackerViewer(960, 640);
	viewer = new PosTrackerViewer(320, 320);
	imageFrame = new SimpleFrame( "Image" );
	imageFrame.add( "Center", new CDA_Panel(viewer) );
	imageFrame.pack();
    }

    /**
     * Things to be done after initialization.
     */
    public void start () {
    
	if (!started) {
	    started = true;

	    displayMsg(
	     "Choose a dataset from the menu, or select \"URL\" and enter\n" +
	     "the URL of a GIF or JPEG image in the text field; then click\n" +
	     "the \"Display Data...\" button.\n" 
	     );
	}
    }

    public void stop() { if (imageFrame != null) imageFrame.dispose(); }

    /**
     * Display a text message to the user.
     * @param msg A string containing the message to display.
     */
    public void displayMsg (String msg) {
	if (msg != null) msgArea.appendText(msg);
    }

    /**
     * Handle events within the applets user panel
     */
    public boolean action(Event ev, Object obj) {
	int n;

	// a button was pressed
	if (ev.target instanceof Button) { 
	    Button b = (Button) ev.target;

	    if (b == displayButton) {
		if (dataChoice.getSelectedItem().equals("URL:")) {
		    if (! setURLViewable()) return false;
		}
		else {
		    if (! setCoordDemoViewable()) return false;
		}
	    }
	    else if (b == browseButton) {
		if (! setFileViewable()) return false;
	    }
	}

	// A choice was made from the menu
        if (ev.target == dataChoice) {
            String choice = (String) obj;
            if (choice.equals("URL:"))
                urlField.enable();
            else
                urlField.disable();
//	    urlField.repaint();
	    repaint();
        }

	// Enter key was hit within the textfild window
	else if (ev.target instanceof TextField) {  
	    TextField t = (TextField) ev.target;

	    if (t == urlField && dataChoice.getSelectedItem().equals("URL:")) {
		if (! setURLViewable()) return false;
	    }
	}

	return false;
    }

    /**
     * load the dataset currently chosen from the data menu as a Viewable; 
     * hard-coded parameters for the dataset are used to set up the dataset's
     * coordinate system.
     */
    private boolean setCoordDemoViewable() { 

	// the name of the file containing selected dataset 
	int idx = dataChoice.getSelectedIndex();
	String file = (idx >= datafiles.length) ? urlField.getText()
	                                        : datafiles[idx];
	System.err.println("chose " + dataBase + ", " + file);

	// create a special viewable that knows how to set up the 
	// proper coordinate system
	try {
	    viewable = new CoordDemoViewable(dataBase, file);
	} catch (MalformedURLException ex) {
	    displayMsg("IO Error while trying to load " + file + 
		       "; aborting...");
	    viewable = null;
	}

	if (viewable != null) {
	    viewer.addViewable( viewable );
	    newViewable();
	    displayMsg("\nPre-set data selected; using known coordinate " +
		       "system.");
	}

	return true;
    }

    /**
     * get the URL currently printed in the text field and load it as 
     * a Viewable object
     */
    private boolean setURLViewable() {
	URL url;

	// get URL, turn it into a viewable, and display it
	try {
	    url = new URL(urlField.getText());
	}
	catch (MalformedURLException e) {
	    displayMsg( "Error loading image:\n   " + 
			e.getMessage() + "\n");
	    return( false );
	}

	viewable = new ComputerGraphicsViewable(url);
	if (viewable != null) {
	    viewer.addViewable(viewable);
	    newViewable();
	    displayMsg("\nURL selected; using simple pixel-based " +
		       "coordinate system.");
	    return true;
	}

	return false;
    }

    /**
     * pop up a file chooser window, get the selected file, and load it as
     * a Viewable object
     * @return boolean  true if file choice returns a valid viewable
     */
    private boolean setFileViewable() {
	fileDialog.show();         // pops up the file chooser window

	String file = fileDialog.getFile();
	if (file == null) return false;

	viewable = 
	    new ComputerGraphicsViewable( fileDialog.getDirectory()+file );

	if (viewable != null) {
	    viewer.addViewable( viewable );
	    newViewable();
	    displayMsg("\nFile selected; using simple pixel-based " +
		       "coordinate system.");
	    return true;
	}

	return true;
    }

   /**
   * Display a new viewable in the frame.
   */
   public void newViewable ( ) {
//       if (! imageFrame.isVisible()) {
// 	  displayMsg("\nUse the mouse to make the following selections:\n");
// 	  displayMsg("  Pixel:   Left button\n");
// 	  displayMsg("  Box:     Right button (META-Left button)\n");
// 	  displayMsg("  Line:    Middle button (ALT- or SHIFT-Left button)\n");
// 	  displayMsg("Click a mouse button while holding control key to" +
// 		     " toggle\na selection on or off");
//       }
       imageFrame.show();
       viewer.displaySlice();
   }
	
}

/*
 * 
 */
class CoordDemoViewable extends ComputerGraphicsViewable {

    String id=null;

    // We assume that baseName is a URL base of some kind
    public CoordDemoViewable(String baseName, String dataName) 
	throws MalformedURLException
    {
	super(new URL(baseName + "/" + dataName));
	coord = null;

	id = dataName;
    }

    public CoordinateSystem getCoordSys() {
	if (coord == null) initCoord();
	return coord;
    }

    /**
     * This method sets up the proper CoordinateSystem object for this 
     * Viewable by first creating a Metadata object with the necessary 
     * parameters.  The parameters are hard-coded into this method for
     * each of the datasets we know about to make it easier to see what's 
     * going on.
     */
    protected void initCoord() {

	// GalacticCenter.gif
	if (id.equals("GalacticCenter.gif")) {

	    // here's the basic data that applies to this image; this 
	    // information would usually be obtained from the input
	    // data itself using a reader that understands the data's 
	    // format
	    //
	    // Number of axes
	    int naxes = 2;

	    // Reference position in degrees
	    double[] refval = { 265.604165196, -28.9583335072 };

	    // Reference voxel: the voxel whose position is the reference 
	    // position
	    double[] refpos = { 265.0, 182.0 };

	    // Voxel size in absolute degrees
	    double[] voxelsize = { -2.777777845E-04, -2.777777845E-04 };

	    // Names for the axes:
	    String[] axisnames = { "R.A.", "Dec." };

	    // Projection code: type of projection used
	    String projcode = "SIN";

	    // Before we create the CoordinateSystem object, we need to 
	    // load the parameters into a Metadata object.  This is best
	    // done using a CoordMetadata object.
	    CoordMetadata cmdata = new CoordMetadata(2);

	    // The CoordMetadata class is aware of the metadata that 
	    // CoordinateSystems look for and helps ensure that the data
	    // is loaded with the correct metadata name and type.  It 
	    // does this by providing special set methods for the 
	    // Coordinate related metadata.  See the API documentation for
	    // CoordMetadata for more information.
	    //
	    cmdata.setAxisRefposition(0, refpos[0]);
	    cmdata.setAxisRefposition(1, refpos[1]);
	    cmdata.setAxisRefvalue(0, refval[0]);
	    cmdata.setAxisRefvalue(1, refval[1]);
	    cmdata.setAxisStepsize(0, voxelsize[0]);
	    cmdata.setAxisStepsize(1, voxelsize[1]);

	    cmdata.setAxisType(0, "longitude");
	    cmdata.setAxisType(1, "latitude");

	    // If we know what the correct data Metadata key name and type
	    // for the data we are setting, we could alternatively use a 
	    // method of the super-class Metadata (see API documentation
	    // for CoordMetadata for details):
	    // 
	    cmdata.setAxisName(axisnames);

	    // We should choose formatter objects of type CoordAxisPos.
	    // This object converts a double into a formatted String.
	    // If we do not set this, a default formatter will be set
	    // that prints the position as normal double values.
	    // 
	    // For this dataset, we want the positions printed with a 
	    // special format.  RA should be printed in 
	    // hours:minutes:seconds format and Dec should be printed 
	    // in degrees:minutes:seconds format.
	    cmdata.setAxisFormatter(0, new HHMMSSAxisPosFormatter());
	    cmdata.setAxisFormatter(1, new DDMMSSAxisPosFormatter());

	    // Now we create the desired CoordinateSystem.  Our reader tells
	    // us (or assumes) that the coordinate system is a sphere 
	    // projected on a plane; thus we will use the 
	    // ProjectedSphericalCoordinateSystem.  
	    //
	    // This CoordinateSystem requires a metadatum called "projection",
	    // the code that identifies the projection type. (See 
	    // ProjectedSphericalCoordinateSystem API documentation.)
	    //
	    cmdata.put("projection", projcode);

	    // We tell the constructor that axis 0 is the longitude axis and 
	    // axis 1 is the latitude axis.
	    //
	    try {
		coord = new SphLinCoordinateSystem(cmdata);
	    } catch (IllegalTransformException ex) {
		System.err.println("Warning: " + ex.getMessage() + "\n" +
				   "Using a default coordinate system.");
		coord = new CoordinateSystem(new CoordMetadata(2));
	    }
	}
	else if (id.equals("hurricane.jpg")) {

	    // here's the basic data that applies to this image; this 
	    // information would usually be obtained from the input
	    // data itself using a reader that understands the data's 
	    // format
	    //
	    // Number of axes
	    int naxes = 2;

	    // Reference position in degrees
	    double[] refval = { 0.0, 90.0 };

	    // Reference voxel: the voxel whose position is the reference 
	    // position.  This is the tangent point of the projection
	    double[] refpos = { 64.5, -358.520517196 };

	    // Voxel size in absolute degrees
//	    double[] voxelsize = { 0.111393996, 0.111393996 };
//	    double[] voxelsize = { 0.111468307, 0.111468307 };
	    double[] voxelsize = { 0.110771962, 0.110771962 };

	    // Names for the axes:
	    String[] axisnames = { "Longitude", "Latitude" };

	    // Projection code: type of projection used
	    String projcode = "STG";

	    // Before we create the CoordinateSystem object, we need to 
	    // load the parameters into a Metadata object.  This is best
	    // done using a CoordMetadata object.
	    CoordMetadata cmdata = new CoordMetadata(2);

	    // The CoordMetadata class is aware of the metadata that 
	    // CoordinateSystems look for and helps ensure that the data
	    // is loaded with the correct metadata name and type.  It 
	    // does this by providing special set methods for the 
	    // Coordinate related metadata.  See the API documentation for
	    // CoordMetadata for more information.
	    //
	    cmdata.setAxisRefposition(0, refpos[0]);
	    cmdata.setAxisRefposition(1, refpos[1]);
	    cmdata.setAxisRefvalue(0, refval[0]);
	    cmdata.setAxisRefvalue(1, refval[1]);
	    cmdata.setAxisStepsize(0, voxelsize[0]);
	    cmdata.setAxisStepsize(1, voxelsize[1]);

	    cmdata.setAxisType(0, "longitude");
	    cmdata.setAxisType(1, "latitude");

	    // If we know what the correct data Metadata key name and type
	    // for the data we are setting, we could alternatively use a 
	    // method of the super-class Metadata (see API documentation
	    // for CoordMetadata for details):
	    // 
//	    cmdata.put("axnames", axisnames);
	    cmdata.setAxisName(axisnames);

	    // We should choose formatter objects of type CoordAxisPos.
	    // This object converts a double into a formatted String.
	    // If we do not set this, a default formatter will be set
	    // that prints the position as normal double values.
	    // 
	    // For this dataset, we want the positions printed with a 
	    // special format.  RA should be printed in 
	    // hours:minutes:seconds format and Dec should be printed 
	    // in degrees:minutes:seconds format.
	    cmdata.setAxisFormatter(0, new CDDMMSSAxisPosFormatter());
	    cmdata.setAxisFormatter(1, new DDMMSSAxisPosFormatter());

	    // Now we create the desired CoordinateSystem.  Our reader tells
	    // us (or assumes) that the coordinate system is a sphere 
	    // projected on a plane; thus we will use the 
	    // ProjectedSphericalCoordinateSystem.  
	    //
	    // This CoordinateSystem requires a metadatum called "projection",
	    // the code that identifies the projection type. (See 
	    // ProjectedSphericalCoordinateSystem API documentation.)
	    //
	    cmdata.put("projection", projcode);
	    cmdata.put("longpole", new Double(-97.0));

	    // We tell the constructor that axis 0 is the longitude axis and 
	    // axis 1 is the latitude axis.
	    //
//	    coord = new ProjectedSphericalCoordinateSystem(0, 1, cmdata);
	    try {
		coord = new SphLinCoordinateSystem(cmdata);
	    } catch (IllegalTransformException ex) {
		System.err.println("Warning: " + ex.getMessage() + "\n" +
				   "Using a default coordinate system.");
		coord = new CoordinateSystem(new CoordMetadata(2));
	    }
	}
	else if (id.equals("usmap.gif")) {

	    // here's the basic data that applies to this image; this 
	    // information would usually be obtained from the input
	    // data itself using a reader that understands the data's 
	    // format
	    //
	    // Number of axes
	    int naxes = 2;

	    // Reference position in degrees
	    double[] refval = { 0.0, 90.0 };

	    // Reference voxel: the voxel whose position is the reference 
	    // position.  This is the tangent point of the projection
//	    double[] refpos = { 266.0, 303.0 };
//	    double[] refpos = { 240.0, -330.34 };   // calculated
	    double[] refpos = { 240.0, -331.34 };

	    // Voxel size in absolute degrees
//	    double[] voxelsize = { 0.111393996, 0.111393996 };  // calculated
	    double[] voxelsize = { 0.111468307, 0.111468307 };

	    // Names for the axes:
	    String[] axisnames = { "Longitude", "Latitude" };

	    // Projection code: type of projection used
	    String projcode = "STG";

	    // Before we create the CoordinateSystem object, we need to 
	    // load the parameters into a Metadata object.  This is best
	    // done using a CoordMetadata object.
	    CoordMetadata cmdata = new CoordMetadata(2);

	    // The CoordMetadata class is aware of the metadata that 
	    // CoordinateSystems look for and helps ensure that the data
	    // is loaded with the correct metadata name and type.  It 
	    // does this by providing special set methods for the 
	    // Coordinate related metadata.  See the API documentation for
	    // CoordMetadata for more information.
	    //
	    cmdata.setAxisRefposition(0, refpos[0]);
	    cmdata.setAxisRefposition(1, refpos[1]);
	    cmdata.setAxisRefvalue(0, refval[0]);
	    cmdata.setAxisRefvalue(1, refval[1]);
	    cmdata.setAxisStepsize(0, voxelsize[0]);
	    cmdata.setAxisStepsize(1, voxelsize[1]);

	    cmdata.setAxisType(0, "longitude");
	    cmdata.setAxisType(1, "latitude");

	    // If we know what the correct data Metadata key name and type
	    // for the data we are setting, we could alternatively use a 
	    // method of the super-class Metadata (see API documentation
	    // for CoordMetadata for details):
	    // 
//	    cmdata.put("axnames", axisnames);
	    cmdata.setAxisName(axisnames);

	    // We should choose formatter objects of type CoordAxisPos.
	    // This object converts a double into a formatted String.
	    // If we do not set this, a default formatter will be set
	    // that prints the position as normal double values.
	    // 
	    // For this dataset, we want the positions printed with a 
	    // special format.  RA should be printed in 
	    // hours:minutes:seconds format and Dec should be printed 
	    // in degrees:minutes:seconds format.
	    cmdata.setAxisFormatter(0, new CDDMMSSAxisPosFormatter());
	    cmdata.setAxisFormatter(1, new DDMMSSAxisPosFormatter());

	    // Now we create the desired CoordinateSystem.  Our reader tells
	    // us (or assumes) that the coordinate system is a sphere 
	    // projected on a plane; thus we will use the 
	    // ProjectedSphericalCoordinateSystem.  
	    //
	    // This CoordinateSystem requires a metadatum called "projection",
	    // the code that identifies the projection type. (See 
	    // ProjectedSphericalCoordinateSystem API documentation.)
	    //
	    cmdata.put("projection", projcode);
	    cmdata.put("longpole", new Double(-97.0));

	    // We tell the constructor that axis 0 is the longitude axis and 
	    // axis 1 is the latitude axis.
	    //
//	    coord = new ProjectedSphericalCoordinateSystem(0, 1, cmdata);
	    try {
		coord = new SphLinCoordinateSystem(cmdata);
	    } catch (IllegalTransformException ex) {
		System.err.println("Warning: " + ex.getMessage() + "\n" +
				   "Using a default coordinate system.");
		coord = new CoordinateSystem(new CoordMetadata(2));
	    }
	}
	else if (id.equals("io.gif")) {

	    // here's the basic data that applies to this image; this 
	    // information would usually be obtained from the input
	    // data itself using a reader that understands the data's 
	    // format
	    //
	    // Number of axes
	    int naxes = 2;

	    // Reference position in degrees
	    double[] refval = { 0.0, 0.0 };

	    // Reference voxel: the voxel whose position is the reference 
	    // position.  This is the tangent point of the projection
//	    double[] refpos = { 266.0, 303.0 };
	    double[] refpos = { 298.0, 289.0 };

	    // Voxel size in absolute degrees
	    double[] voxelsize = { -0.198254501, -0.207217783 };

	    // Names for the axes:
	    String[] axisnames = { "Longitude", "Latitude" };

	    // Projection code: type of projection used
	    String projcode = "SIN";

	    // Before we create the CoordinateSystem object, we need to 
	    // load the parameters into a Metadata object.  This is best
	    // done using a CoordMetadata object.
	    CoordMetadata cmdata = new CoordMetadata(2);

	    // The CoordMetadata class is aware of the metadata that 
	    // CoordinateSystems look for and helps ensure that the data
	    // is loaded with the correct metadata name and type.  It 
	    // does this by providing special set methods for the 
	    // Coordinate related metadata.  See the API documentation for
	    // CoordMetadata for more information.
	    //
	    cmdata.setAxisRefposition(0, refpos[0]);
	    cmdata.setAxisRefposition(1, refpos[1]);
	    cmdata.setAxisRefvalue(0, refval[0]);
	    cmdata.setAxisRefvalue(1, refval[1]);
	    cmdata.setAxisStepsize(0, voxelsize[0]);
	    cmdata.setAxisStepsize(1, voxelsize[1]);

	    cmdata.setAxisType(0, "longitude");
	    cmdata.setAxisType(1, "latitude");

	    // If we know what the correct data Metadata key name and type
	    // for the data we are setting, we could alternatively use a 
	    // method of the super-class Metadata (see API documentation
	    // for CoordMetadata for details):
	    // 
//	    cmdata.put("axnames", axisnames);
	    cmdata.setAxisName(axisnames);

	    // We should choose formatter objects of type CoordAxisPos.
	    // This object converts a double into a formatted String.
	    // If we do not set this, a default formatter will be set
	    // that prints the position as normal double values.
	    // 
	    // For this dataset, we want the positions printed with a 
	    // special format.  Longitude should be printed in 
	    // degrees:minutes:seconds format (range 0-360) and Latitude 
	    // should be printed in degrees:minutes:seconds format 
	    // (range -90 - +90).
	    cmdata.setAxisFormatter(0, new CDDMMSSAxisPosFormatter());
	    cmdata.setAxisFormatter(1, new DDMMSSAxisPosFormatter());

	    // Now we create the desired CoordinateSystem.  Our reader tells
	    // us (or assumes) that the coordinate system is a sphere 
	    // projected on a plane; thus we will use the 
	    // ProjectedSphericalCoordinateSystem.  
	    //
	    // This CoordinateSystem requires a metadatum called "projection",
	    // the code that identifies the projection type. (See 
	    // ProjectedSphericalCoordinateSystem API documentation.)
	    //
	    cmdata.put("projection", projcode);

	    // 
	    // 
	    //
	    try {
		coord = new SphLinCoordinateSystem(cmdata);
	    } catch (IllegalTransformException ex) {
		System.err.println("Warning: " + ex.getMessage() + "\n" +
				   "Using a default coordinate system.");
		coord = new CoordinateSystem(new CoordMetadata(2));
	    }
	}
	else {

	    // We do not have any special information on this dataset.
	    // Use a simple pixel-based coordinate system
	    coord = new CoordinateSystem(new CoordMetadata(2));
	}

    }
}

class CDA_Panel extends Panel {
    public Component item = null;

    public CDA_Panel(Component viewer) { 
	super(); 
	item = viewer;

 	GridBagLayout bag = new GridBagLayout();
 	GridBagConstraints c = new GridBagConstraints();
 	setLayout(bag);
 	c.insets = new Insets(4,4,4,4);
 	c.gridwidth = c.REMAINDER;
 	c.fill = c.BOTH;
 	c.anchor = c.NORTHWEST;
 	bag.setConstraints(viewer, c);
 	add(viewer);
    }

    public void reshape(int x, int y, int width, int height) {
	item.reshape(4, 4, width-8, height-8);
	super.reshape(x, y, width, height);
    }
}


