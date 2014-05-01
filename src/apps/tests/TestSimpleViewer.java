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
   TestSimpleViewer.java - testbed for Horizon SimpleViewer and ComputerGraphicsViewable
         packages.

   Classes:
      TestSimpleViewer

   Classes referenced from ncsa.horizon.*:
      SimpleViewer, ComputerGraphicsViewable, SimpleFrame

   Edit history:
      08-Nov-96 plutchak     Initial version
      05-Dec-97 plante       Switched from depricated SimpleViewable to
                               ComputerGraphicsViewable
*/
package apps.tests;

import java.applet.Applet;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;

import ncsa.horizon.viewable.ComputerGraphicsViewable;
import ncsa.horizon.viewer.SimpleViewer;
import ncsa.horizon.awt.SimpleFrame;
//import ncsa.horizon.util.*;

/**
* The main class for the test program for SimpleViewer and ComputerGraphicsViewable.
* @version 0.1 alpha
* @author Joel Plutchak <plutchak@uiuc.edu>
*/
public class TestSimpleViewer extends Applet
{
   // various user interface components and objects
   Panel             controlPanel;             // panel for controls
   Panel             userPanel;                // panel for user input/output
   SimpleViewer      viewer;                   // Display area
   SimpleFrame       imageFrame;               // frame for viewer display
   TextField         urlField;                 // entry field for URL
   Choice            urlChoice;                // URL choices 
   Button            browseButton;             // to bring up browse dialog
   Button            refreshButton;            // to bring up browse dialog
   static TextArea   msgArea;                  // for output to user
   ComputerGraphicsViewable      viewable;                 // main viewable
   static FileDialog fileDialog;
   final static String      titleString = "TestSimpleViewer, v0.1";
   static boolean    started = false;
   private static boolean   isApplication = false;

   /**
   * Main program for application entry point.
   * @param argv An array of Strings representing command line parameters
   */
   public static void main ( String argv[] ) {
      SimpleFrame       mainFrame = null;
      TestSimpleViewer  app = new TestSimpleViewer();

      // create top-level frame and add the applet to it
      mainFrame = new SimpleFrame( titleString );
      mainFrame.setKillOnClose();
      mainFrame.add( "Center", app );

      // initialize and start the applet
      isApplication = true; 
      app.init();
      app.start();

      fileDialog = new  FileDialog( mainFrame, "Load Image", FileDialog.LOAD );
      String cwd = System.getProperty("user.dir");
      String fs = System.getProperty("file.separator");
      fileDialog.setDirectory(cwd + fs + ".." + fs + "data");

      // resize, etc.
      mainFrame.pack();
      mainFrame.layout();
      mainFrame.validate();
      mainFrame.show();
   }

   /**
   * Creation and initialization of user interface components, etc.
   */
   public void init ( ) {
      GridBagLayout        gridbag;         // throw-away gridbag & constraints
      GridBagConstraints   constraints = new GridBagConstraints();
      Label                label;

      /*
         create and place all the GUI elements:
            BorderLayout: The Applet
               South: User Input/Output (userPanel)
                  GridBagLayout - URL input and text output components
      */

      // user input/output panel and components
      userPanel = new Panel();
      userPanel.setLayout( gridbag = new GridBagLayout() );

      // label for URL input field
      label = new Label( "URL: " );
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.weightx = 0.0;
      constraints.weighty = 0.5;
      gridbag.setConstraints( label, constraints );
      userPanel.add( label );

      // text field for user input of a URL
      if (isApplication) {
	  String fSep = System.getProperty("file.separator");
	  String pwd = System.getProperty("user.dir");
	  urlField = new TextField("file:" + pwd + fSep + ".." + 
				   fSep + "data" + fSep + "io.gif");
      }
      else {
	  urlField = new TextField(getCodeBase().toString() + 
				   "../examples/data/io.gif");
      }

//      urlField = new TextField( "hurricane.jpg" );
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.gridwidth = GridBagConstraints.RELATIVE;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.weightx = 0.5;
      constraints.weighty = 0.5;
      gridbag.setConstraints( urlField, constraints );
      userPanel.add( urlField );

      // file browse dialog button
      browseButton = new Button( "Browse..." );
      if (!isApplication) browseButton.disable();
      constraints.anchor = GridBagConstraints.EAST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.weightx = 0.0;
      constraints.weighty = 0.5;
      gridbag.setConstraints( browseButton, constraints );
      userPanel.add( browseButton );

      // image display mode controls
      Panel modePanel = new Panel();
      modePanel.setLayout( new FlowLayout() );
      CheckboxGroup modeGroup = new CheckboxGroup();
      modePanel.add( new Checkbox( "Center Image", modeGroup, true ));
      modePanel.add( new Checkbox( "Fit Image", modeGroup, false ));
      modePanel.add( new Checkbox( "Truncate Image", modeGroup, false ));
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.gridx = 0;
      constraints.gridy = GridBagConstraints.RELATIVE;
      gridbag.setConstraints( modePanel, constraints );
      userPanel.add( modePanel );

      msgArea = new TextArea();
      msgArea.setEditable( false );
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.gridheight = GridBagConstraints.REMAINDER;
      constraints.gridx = 0;
      constraints.gridy = GridBagConstraints.RELATIVE;
      constraints.weightx = 0.3;
      constraints.weighty = 0.3;
      gridbag.setConstraints( msgArea, constraints );
      userPanel.add( msgArea );

      add( userPanel );
      show();

      // create dynamic components
      imageFrame = new SimpleFrame( "Image" );
      imageFrame.setLayout( new GridLayout( 1, 1 ));
      viewer = new SimpleViewer();
      imageFrame.resize( 512, 512 );
      viewer.resize( 512, 512 );
      imageFrame.add( viewer );
   }

   /**
   * Things to be done after initialization.
   */
   public void start () {
      // things that should to be done after components are realized

      if (!started) {
         started = true;

         displayMsg( "Enter the URL of a GIF or JPEG image in the above field and\npress the ENTER key.\n" );
      }
   }

   /**
   * Provide information about this applet.
   * @return A string containing information about this applet.
   */
   public String getAppletInfo () {
      return( "TestSimpleViewer version 0.1 alpha; November 1, 1996\n" +
              "Author:\n" +
              "      Joel Plutchak <plutchak@uiuc.edu>\n" +
              "      Department of Atmospheric Sciences\n" +
              "      University of Illinois at Urbana-Champaign");
   }

   /**
   * Display a text message to the user.
   * @param msg A string containing the message to display.
   */
   public final static void displayMsg ( String msg ) {
      if (msg != null) msgArea.appendText( msg );
   }

   /**
   * Handle applet GUI events.
   * @param ev The event that caused the action.
   * @param obj The action.
   * @return true if the event has been handled; false to pass it to parent.
   */
   public boolean action ( Event ev, Object obj ) {
      int         n;
      boolean     value;

      if (ev.target instanceof Button) {  // handle buttons
         Button      b = (Button)ev.target;

         if (b == refreshButton) {
            if (viewer != null) viewer.displaySlice();
         }
         else if (b == browseButton) {
            fileDialog.show();             // pops up modal dialog

            String file = fileDialog.getFile();
            if (file == null) return( false );

            viewable = new ComputerGraphicsViewable(fileDialog.getDirectory() +
						    file);
            if (viewable != null) {
               viewer.addViewable( viewable );
               newViewable();
            }
         }
      }
      else if (ev.target instanceof TextField) {  // handle text fields
         TextField      t = (TextField)ev.target;

         if (t == urlField) {
            URL   url;
        
            try {
               url = new URL( t.getText() );
            }
            catch (java.net.MalformedURLException e) {
               displayMsg( "Error loading image:\n   "+e.getMessage()+"\n" );
               return( false );
            }
            viewable = new ComputerGraphicsViewable( url );
            if (viewable != null) {
               viewer.addViewable( viewable );
               newViewable();
            }
         }
      }
      else if (ev.target instanceof Checkbox) {  // handle Checkbox
         Checkbox      b = (Checkbox)ev.target;

         if (viewer == null) return( false );

         if (b.getLabel().startsWith( "Center" ))
            viewer.setMode( SimpleViewer.SIZE_IMAGE_CENTER );
         else if (b.getLabel().startsWith( "Fit" ))
            viewer.setMode( SimpleViewer.SIZE_IMAGE_FIT );
         else if (b.getLabel().startsWith( "Truncate" ))
            viewer.setMode( SimpleViewer.SIZE_IMAGE_TRUNCATE );
      }
      else {
         System.out.println( "(applet) unhandled event "+ev.toString() );
      }

      return false;
   }

   /**
   * Display a new viewable in the frame.
   */
   public void newViewable ( ) {
      viewer.displaySlice();
      imageFrame.show();
   }
}
