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
   TestCursor.java - test and demonstrate ncsa.horizon.awt.Cursor class

   Classes:
      TestCursor, CursorCanvas, LabeledField, ExtendedScroll

   Edit history:
      plutchak    19-Sep-96 Created
*/
package apps.tests;

import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.net.MalformedURLException;

import ncsa.horizon.awt.Cursor;
import ncsa.horizon.awt.ROI;

public class TestCursor extends Applet
{
   public static int   Debug = 0;

   public final static int DEBUG_GENERAL = 0x0001;
   public final static int DEBUG_VERBOSE = 0x0002;
   public final static int DEBUG_DATE = 0x0004;

   CursorCanvas      canvas;

   ExtendedScroll    widthScroll, heightScroll, thicknessScroll;
   Choice            colorChoice, styleChoice;
   Checkbox          showBox;
   LabeledField      xField, yField;
   Checkbox          showROIBox;
   LabeledField      xROIField, yROIField, wROIField, hROIField;

   Font              defaultFont, defaultBoldFont, creditFont;
   static FontMetrics       defMetrics, boldMetrics;
   static boolean    started = false;

   Color             colorArray[];
   URL               imageURL;

   /**
   * main class for use as application 
   */
   public static void main ( String argv[] ) {
      Frame f = new Frame( "TestCursor v0.1a" );
      TestCursor app = new TestCursor();

      f.add( "Center", app );
      f.pack();
      f.layout();
      f.show();

      app.init();
      app.start();
   }

   /**
   * initialize GUI components, etc.
   */
   public void init ( ) {
      Panel                canvasPanel, sizePanel, attribPanel;
      GridBagLayout        gridbag, gb2;
      GridBagConstraints   constraints, constraints2;
      String               string;

      string = getParameter( "DEBUG" );
      if (string != null) {
         Debug = Integer.parseInt( string );
      }

      if (TestCursor.Debug != 0) {
         System.out.println( "Applet initializing..." );
      }

      // general initialization
      //Debug = TestCursor.DEBUG_GENERAL | TestCursor.DEBUG_DATE;

      // first check for essential station database URL
      string = getParameter( "IMAGE" );

      try {
         imageURL =  new URL( getDocumentBase(), "../data/" + string );
      }
      catch (MalformedURLException e) {
         imageURL = null;
         System.out.println( "**** Warning: ignoring image URL;" );
         System.out.println( e.getMessage() );
      }

      /*
         create and place all the GUI elements:
            GridBag: the applet
               Element: Image panel & canvas (canvasPanel, canvas)
               Grid: Cursor attribute panel (attribPanel)
                  Element: Style chooser (styleChoice)
                  Element: Color chooser (colorChoice)
                  Element: Show cursor (showBox)
                  Elements: X label and field
                  Elements: Y label and field
               Grid: Cursor size panel (sizePanel)
                  Element: Width scrollbar panel
                  Element: Height scrollbar panel
                  Element: Thickness scrollbar panel
      */

      // Create GUI components
      gridbag = new GridBagLayout();
      constraints = new GridBagConstraints();
      setLayout( gridbag );

      // canvas and canvas panel
      canvasPanel = new Panel();
      canvasPanel.add( canvas = new CursorCanvas( this ));

      constraints.anchor = GridBagConstraints.NORTHWEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      gridbag.setConstraints( canvasPanel, constraints );

      this.add( canvasPanel );

      // create attribute panel
      attribPanel = new Panel();
      attribPanel.setLayout( new GridLayout( 11, 1 ));

      styleChoice = new Choice();
      styleChoice.addItem( "Cross" );
      styleChoice.addItem( "Dot" );
      styleChoice.addItem( "Box" );
      styleChoice.addItem( "Bullseye" );
      styleChoice.addItem( "Open Cross" );
      styleChoice.addItem( "Spanning Cross" );
      attribPanel.add( styleChoice );

      colorChoice = new Choice();
      colorArray = new Color[9];
      colorChoice.addItem( "Red" );
      colorArray[0] = Color.red;
      colorChoice.addItem( "Green" );
      colorArray[1] = Color.green;
      colorChoice.addItem( "Blue" );
      colorArray[2] = Color.blue;
      colorChoice.addItem( "Cyan" );
      colorArray[3] = Color.cyan;
      colorChoice.addItem( "Magenta" );
      colorArray[4] = Color.magenta;
      colorChoice.addItem( "Yellow" );
      colorArray[5] = Color.yellow;
      colorChoice.addItem( "Black" );
      colorArray[6] = Color.black;
      colorChoice.addItem( "White" );
      colorArray[7] = Color.white;
      colorChoice.addItem( "Gray" );
      colorArray[8] = Color.gray;
      attribPanel.add( colorChoice );

      // cursor location/visibility
      showBox = new Checkbox( "Show Cursor", null, true );
      attribPanel.add( showBox );

      xField = new LabeledField( "X: " );
      attribPanel.add( xField );

      yField = new LabeledField( "Y: " );
      attribPanel.add( yField );

      // ROI location/size/visibility
      showROIBox = new Checkbox( "Show Region", null, true );
      attribPanel.add( showROIBox );

      xROIField = new LabeledField( "X: " );
      attribPanel.add( xROIField );

      yROIField = new LabeledField( "Y: " );
      attribPanel.add( yROIField );

      wROIField = new LabeledField( "W: " );
      attribPanel.add( wROIField );

      hROIField = new LabeledField( "H: " );
      attribPanel.add( hROIField );

      //constraints.gridy = 0;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      gridbag.setConstraints( attribPanel, constraints );
      this.add( attribPanel );

      // size panel
      sizePanel = new Panel();
      sizePanel.setLayout( gb2 = new GridBagLayout() );
      constraints2 = new GridBagConstraints();

      widthScroll = new ExtendedScroll( Scrollbar.HORIZONTAL, "Width: " );
      widthScroll.setValues( 16, 1, 1, 64 );
      widthScroll.setPageIncrement( 16 );
      constraints2.anchor = GridBagConstraints.WEST;
      constraints2.fill = GridBagConstraints.HORIZONTAL;
      constraints2.gridwidth = GridBagConstraints.REMAINDER;
      constraints2.weightx = 1.0;
      constraints2.weighty = 1.0;
      gb2.setConstraints( widthScroll, constraints2 );
      sizePanel.add( widthScroll );

      heightScroll = new ExtendedScroll( Scrollbar.HORIZONTAL, "Height: " );
      heightScroll.setValues( 16, 1, 1, 64 );
      heightScroll.setPageIncrement( 16 );
      constraints2.gridy = GridBagConstraints.RELATIVE;
      constraints2.gridwidth = GridBagConstraints.REMAINDER;
      gb2.setConstraints( heightScroll, constraints2 );
      sizePanel.add( heightScroll );

      thicknessScroll = new ExtendedScroll(Scrollbar.HORIZONTAL,"Thickness: ");
      thicknessScroll.setValues( 2, 1, 1, 32 );
      thicknessScroll.setPageIncrement( 4 );
      constraints2.gridy = GridBagConstraints.RELATIVE;
      constraints2.gridwidth = GridBagConstraints.REMAINDER;
      constraints2.gridheight = GridBagConstraints.REMAINDER;
      gb2.setConstraints( thicknessScroll, constraints2 );
      sizePanel.add( thicknessScroll );

      constraints.gridx = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.gridy = GridBagConstraints.RELATIVE;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.gridheight = GridBagConstraints.REMAINDER;
      gridbag.setConstraints( sizePanel, constraints );
      this.add( sizePanel );

      show();
   }

   public void stop () {
      if (TestCursor.Debug != 0) {
         System.out.println( "Applet stopping..." );
      }
      super.stop();
   }

   public void start () {
      int            n;
      String         txt;

      if (TestCursor.Debug != 0) {
         System.out.println( "Applet starting..." );
      }

      // things that have to be done after components are realized
      // In Netscape, at least, the applet init/start/stop cycle doesn't
      // seem to work as advertised, so this is kludgy.
      if (!started) {
         started = true;
      }
      canvas.setImage( imageURL );
   }

   public String getAppletInfo () {
      return( "Copyright (C) 1996  Dept. of Atmospheric Sciences\n" +
              "      University of Illinois at Urbana-Champaign\n" +
              "Author:\n" +
              "      Joel Plutchak <plutchak@uiuc.edu>\n" +
              "      Department of Atmospheric Sciences" );
   }

   public String[][] getParameterInfo ( ) {
      String info[][] = {{"IMAGE","URL", "URL for image file"}};
      return( info );
   }

   /**
   * Display cursor location int the appropriate fields.
   * @param x The x location of the cursor in its component.
   * @param y The y location of the cursor in its component.
   */
   public void dispCursorLoc ( int x, int y ) {
      xField.setValue( x );
      yField.setValue( y );
   }

   /**
   * Display Region of Interest parameters in the appropriate fields.
   * @param x The x location of the upper left corner of the region.
   * @param y The y location of the upper left corner of the region.
   * @param w The width of the region.
   * @param h The height of the region.
   */
   public void dispRegion ( int x, int y, int w, int h ) {
      xROIField.setValue( x );
      yROIField.setValue( y );
      wROIField.setValue( w );
      hROIField.setValue( h );
   }

   /**
   * Display Region of Interest location in the appropriate fields.
   * @param x The x location of the upper left corner of the region.
   * @param y The y location of the upper left corner of the region.
   */
   public void dispRegionLoc ( int x, int y ) {
      xROIField.setValue( x );
      yROIField.setValue( y );
   }

   /**
   * Display Region of Interest size in the appropriate fields.
   * @param w The width of the region.
   * @param h The height of the region.
   */
   public void dispRegionSize ( int w, int h ) {
      wROIField.setValue( w );
      hROIField.setValue( h );
   }

   public void dispMsg ( String s ) {
      showStatus( s );
   }

   /**
   * Handle all scrollbar events.  The associated text field is updated.
   */
   public boolean handleEvent ( Event evt ) {

      switch (evt.id) {
         case Event.SCROLL_LINE_UP:
         case Event.SCROLL_LINE_DOWN:
         case Event.SCROLL_PAGE_UP:
         case Event.SCROLL_PAGE_DOWN:
         case Event.SCROLL_ABSOLUTE:
            Cursor      curs = canvas.getCursor();

            if (((Scrollbar)evt.target).getParent() == widthScroll) {
               curs.resize( ((Scrollbar)evt.target).getValue(),
                     curs.size().height );
            }
            else if (((Scrollbar)evt.target).getParent() == heightScroll) {
               canvas.getCursor().resize( curs.size().width,
                     ((Scrollbar)evt.target).getValue() );
            }
            else if (((Scrollbar)evt.target).getParent() == thicknessScroll) {
               canvas.getCursor().setThickness(
                     ((Scrollbar)evt.target).getValue() );
            }
            break;
         default:
            return( super.handleEvent( evt ));
      }

      return( false );
   }

   /**
   * Handle applet GUI actions.  This responds to user input/selection using
   * the GUI controls.
   * @param ev The Event to handle.
   * @param obj AN Event-specific object.
   * @return true to indicate the Event has been handled; false otherwise.
   */
   public boolean action ( Event ev, Object obj ) {
      int         n;
      boolean     value;

      if (ev.target instanceof Checkbox) {  // handle checkboxes
         Checkbox    c = (Checkbox)ev.target;

         if (c == showBox) {
            Cursor curs = canvas.getCursor();
            if (curs != null)
               curs.show( ((Boolean)obj).booleanValue() );
         }
         else if (c == showROIBox) {
            ROI   roi = canvas.getRegion();
            if (roi != null)
               roi.show( ((Boolean)obj).booleanValue() );
         }
      }
      else if (ev.target instanceof ExtendedScroll) {  // handle Scrollbars
         System.out.println( "(action) scrollbar event: "+ev.toString() );
      }
      else if (ev.target instanceof Choice) {  // handle Choice menu
         Choice      c = (Choice)ev.target;
         String choiceString = (String)obj;
         int index = c.getSelectedIndex();
         Cursor curs = canvas.getCursor();

         if (c == styleChoice) {
            if (curs != null)
               curs.setStyle( index+1 );
         }
         else if (c == colorChoice) {
            if (curs != null)
               curs.setColor( colorArray[index] );
         }
      }

      return false;
   }

   /**
   * Set scrollbar values.
   * @param width The scrollbar value for the region width control.
   * @param height The scrollbar value for the region height control.
   */
   public void setCursorValues ( int width, int height ) {
      Cursor   curs = canvas.getCursor();

      widthScroll.setValues( widthScroll.getValue(), 1,
            thicknessScroll.getValue(), width );

      heightScroll.setValues( heightScroll.getValue(), 1,
            thicknessScroll.getValue(), height );

      if (curs != null) {
         xField.setValue( curs.location().x );
         yField.setValue( curs.location().y );
      }
   }
}

/**
* Handle image canvas painting and events.  Displays and image, and
* handles placement and drawing of a cursor and a region of interest.
*/
class CursorCanvas extends Canvas implements ImageObserver
{
   TestCursor          applet;

   Image           offScreenImage;         // buffer for double buffering
   Graphics        offScreenGraphics;      // graphics context for buffer
   Dimension       maxSize;                // dimension of the buffer
   Cursor          curs;                   // the component cursor
   ROI             roi;                    // the region of interest
   Image           image;                  // the image to display
   boolean         isInside = false;       // true if still "in" canvas
   int             roiDirection = 0;       // direction for ROI grab

   /**
   * Create a new instance of a CursorCanvas.
   * @param app The applet the canvas is attached to.
   */
   public CursorCanvas ( TestCursor app ) {
      applet = app;

      // set up a cursor
      curs = new Cursor( this );

      maxSize = preferredSize();
   }

   /**
   * Get the cursor associated with this canvas.
   * @return The cursor; null if no cursor available.
   */
   public Cursor getCursor ( ) {
      return( curs );
   }

   /**
   * Get the Region of Interest associated with this canvas.
   * @return The ROI; null if no region available.
   */
   public ROI getRegion ( ) {
      return( roi );
   }

   /**
   * Set a new image for the canvas background.
   * @param url The URL of an image to load.
   */
   public void setImage ( URL url ) {
      if (applet.Debug != 0)
         System.out.println( "(canvas) Loading "+url.getFile() );
      image = Toolkit.getDefaultToolkit().getImage( url );
   }

   /**
   * Report pointer coordinates.
   * @param x The x coordinate of the cursor.
   * @param y The y coordinate of the cursor.
   */
   private void reportCursor ( int x, int y ) {
      applet.dispCursorLoc( x, y );
   }

   /**
   * Report region location and size.
   */
   private void reportRegion ( ) {
      applet.dispRegion( roi.x, roi.y, roi.width, roi.height );
   }

   /**
   * Report region location.
   */
   private void reportRegionLoc ( ) {
      applet.dispRegionLoc( roi.x, roi.y );
   }

   /**
   * Report region size.
   */
   private void reportRegionSize ( ) {
      applet.dispRegionSize( roi.width, roi.height );
   }

   /**
   * Keep track of when mouse leaves component.  Useful if user drops
   * a cursor or region of the canvas.
   * @param evt The AWT event.
   * @param x The x location of the event.
   * @param y The y location of the event.
   * @return true if the event was handled; false to pass to parent.
   */
   public boolean mouseExit ( Event evt, int x, int y ) {
      isInside = false;

      return( false );
   }

   /**
   * Keep track of when mouse enters component.  Useful if user drags
   * a cursor or region from somewhere else.
   * @param evt The AWT event.
   * @param x The x location of the event.
   * @param y The y location of the event.
   * @return true if the event was handled; false to pass to parent.
   */
   public boolean mouseEnter ( Event evt, int x, int y ) {
      isInside = true;

      return( false );
   }

   /**
   * Perform action when mouse button pressed.
   * @param event The AWT event.
   * @param x The x location of the event.
   * @param y The y location of the event.
   * @return true if the event was handled; false to pass to parent.
   */
   public boolean mouseDown ( java.awt.Event event, int x, int y ) {
      int         rgb;

      // On right mouse button, begin defining a region of interest
      if ((event.modifiers & Event.META_MASK) != 0) {
         roi = new ROI( x, y, this );
         //reportRegion();            // display data to user
         applet.dispRegion( x, y, 0, 0 );            // display data to user
      }
      else {
         // if on the cursor, grab it
         if ((curs != null) && curs.inside( x, y, 4 )) {
            curs.grab();
            //reportCursor( x, y );         // display data to user
            applet.dispCursorLoc( x, y );
         }
         else if (roi != null) {
            if ((roiDirection = roi.on( x, y, 4 )) != 0) {
               roi.grab( x, y );
            }
         }
      }

      return false;
   }

   /**
   * Actions to perform when mouse (button down) moved in the component.
   * @param event The AWT event.
   * @param x The x location of the event.
   * @param y The y location of the event.
   * @return true if the event was handled; false to pass to parent.
   */
   public boolean mouseDrag ( Event event, int x, int y ) {
      int      rgb;

      if (!isInside) return( false );

      // On right mouse button drag, size the new region of interest
      if ((event.modifiers & Event.META_MASK) != 0) {
         if (roi != null) {            // manipulate region-of-interest
            if (roi.isInitialized()) {      // resize region
               roi.grow( x, y );
               //applet.dispRegionSize( roi.width, roi.height );
            }
         }
      }
      else {
         if (curs.isGrabbed()) {            // drag the cursor around
            applet.dispCursorLoc( x, y );
            curs.drag( x, y );
         }
         else if (roi != null) {            // manipulate region-of-interest
            if (roi.isGrabbed()) {          // move the region
               roi.drag( x, y );
               //applet.dispRegionLoc( roi.x, roi.y );   // display data to user
            }
         }
      }

      return false;
   }

   /**
   * Actions to perform when mouse button released.  This covers mouse
   * dropping, region dropping, and region draw/resize completion.
   * @param event The AWT event.
   * @param x The x location of the event.
   * @param y The y location of the event.
   * @return true if the event was handled; false to pass to parent.
   */
   public boolean mouseUp ( Event event, int x, int y ) {
      int         rgb;

      if (curs.isGrabbed()) {
         curs.drop( x, y );
         applet.dispCursorLoc( curs.x, curs.y );
      }
      else if ((roi != null)) {
         if (roi.isInitialized() || roi.isGrabbed()) {
            roi.drop( x, y );           // finish manipulating the region
            applet.dispRegion( roi.x, roi.y, roi.width, roi.height );
         }
      }

      return false;
   }

   /**
   * Handle screen updates, drawing background, if present, from an
   * offscreen buffer, then redraw the cursor and ROI on top of it.
   * @param g The Graphics to update.
   */
   public final synchronized void update ( Graphics g ) {
      Dimension   d = size();
      Rectangle clip = g.getClipRect();

      if (applet.Debug != 0)
         System.out.println( "(update) "+d.width+","+d.width+","+
               maxSize.width+","+maxSize.height+" )" );

      if ((maxSize == null) || (d.width <= 0) || (d.height <= 0))
         return;

      // make sure offscreen buffer is proper size
      if ((offScreenImage == null) || (d.width != maxSize.width) ||
           (d.height != maxSize.height)) {
         offScreenImage = createImage( d.width, d.height );
         maxSize = d;
         offScreenGraphics = offScreenImage.getGraphics();

         if (applet.Debug != 0)
            System.out.println( "(canvas) creating offScreen: "+d.width+"x"+
                  d.height );

         // set cursor to midpoint on canvas
         if (curs != null)
            curs.move( d.width/2, d.height/2 );

         applet.setCursorValues( d.width, d.height );
         paintOffscreen();
      }

      g.drawImage( offScreenImage, 0, 0, null );

      // The cursor is not double-buffered so updates are faster
      // when the cursor/ROI moves.
      if (curs != null) curs.draw( g );
      if (roi != null) roi.draw( g );
   }

   /**
   * Paint the image.  This always uses the offscreen image, so it just
   * calls the update() method.
   * @param g The graphics context to use.
   */
   public void paint ( Graphics g ) {
      if (maxSize != null)
         update( g );
   }

   /**
   * Repaint the offscreen image that is used to refresh the screen.
   */
   public void paintOffscreen ( ) {
      int         width = size().width, height = size().height;
      Graphics    g = offScreenGraphics;

      if (applet.Debug != 0)
         System.out.print( "(canvas) painting offScreen... " );

      if (g == null) {
         if (applet.Debug != 0)
            System.out.println( "no graphics context!" );
         return;
      }

      g.setColor( Color.lightGray );
      g.fillRect( 0, 0, width, height );

      // draw the image into the offscreen buffer
      if (image == null) {
         g.setColor( Color.black );
         g.draw3DRect( 0, 0, size().width-2, size().height-2, true );
         if (applet.Debug != 0)
            System.out.println( "no image." );
      }
      else {
         if (applet.Debug != 0)
            System.out.println( "image." );
         g.drawImage( image, 0, 0, this );
      }

      repaint();
   }

   /**
   * Catch image load events so the offscreen buffer gets repainted.
   * @param img The image being observed
   * @param infoflags See §2.11.9 for more information 
   * @param x An x coordinate 
   * @param y A y coordinate 
   * @param width The image width 
   * @param height The image height 
   * @return true if the image needs to be tracked further; false otherwise. 
   */
   public boolean imageUpdate ( Image img, int infoflags, int x, int y,
         int width, int height ) {

      if ((infoflags & (ALLBITS | ERROR)) != 0) {

      if (applet.Debug != 0)
         System.out.println( "(image) "+x+","+y+","+width+","+height+",0x"+
            Integer.toHexString( infoflags ));

         if ((infoflags & ALLBITS) != 0) {
            paintOffscreen();
            applet.dispMsg( "Loaded image file\n" );
         }
         else if ((infoflags & ERROR) != 0) {
            applet.dispMsg( "*** Error loading image file\n" );
         }
      }
      else if ((applet.Debug != 0) && ((y % 10) == 0))
         System.out.println( "(image) "+x+","+y+","+width+","+height+",0x"+
            Integer.toHexString( infoflags ));

      return (infoflags & (ALLBITS | ERROR)) == 0;
   }

   public Dimension minimumSize() {
      return new Dimension( 320, 320 );
   }

   public Dimension preferredSize() {
      return new Dimension( 320, 320 );
   }
}

/**
* A labeled text field.
*/
class LabeledField extends Panel {
   private Label        label;
   private TextField    value;

   /**
   * Create a new labeled text field.
   * @param l The label for the text field.
   */
   public LabeledField ( String l ) {
      super();

      label = new Label( l );
      this.add( label );

      value = new TextField( 8 );
      value.setEditable( false );
      this.add( value );
   }

   /**
   * Set the label.
   * @param l The label for the text field.
   */
   public void setLabel ( String l ) {
      label.setText( l );
   }

   /**
   * Get the label.
   * @return The label for the text field.
   */
   public String getLabel ( ) {
      return( label.getText() );
   }

   /**
   * Set the text field value.
   * @param l The value of the text field.
   */
   public void setValue ( String l ) {
      value.setText( l );
   }

   /**
   * Set the text field value.
   * @param n The value of the text field.
   */
   public void setValue ( int n ) {
      value.setText( ""+n );
   }

   /**
   * Get the text field value.
   * @return The value of the text field.
   */
   public String getValue ( ) {
      return( value.getText() );
   }

   /**
   * Return a String version of the conmponent.
   * @return The value of the label and text fields.
   */
   public String toString ( ) {
      String s = "LabeledField[ "+label.getText()+","+value.getText()+"]";
      return( s );
   }
}

/**
* A labeled and value-displaying horizontal scrollbar.
*/
class ExtendedScroll extends Panel {
   private Scrollbar    scroll;
   private Label        label;
   private TextField    value;

   /**
   * Create a scrollbar with label and value fields.
   * @param orientation The orientation of the scrollbar.  Either
   *       Scrollbar.HORIZONTAL or Scrollbar.VERTICAL .
   * @param l The label for the scrollbar.
   */
   public ExtendedScroll ( int orientation,  String l ) {
      super();

      GridBagLayout        gb = new GridBagLayout();
      GridBagConstraints   gbc = new GridBagConstraints();

      gbc.anchor = GridBagConstraints.NORTHEAST; 
      gbc.fill = GridBagConstraints.NONE; 
      gbc.gridheight = GridBagConstraints.REMAINDER; 
      gbc.weightx = 0.2; 
      gbc.weighty = 1.0; 

      // the label
      label = new Label( l );
      gb.setConstraints( label, gbc );
      this.add( label );

      // the scrollbar
      gbc.anchor = GridBagConstraints.NORTH; 
      gbc.fill = GridBagConstraints.HORIZONTAL; 
      gbc.gridx = GridBagConstraints.RELATIVE; 
      gbc.weightx = 0.6; 
      gbc.weighty = 1.0; 
      gbc.ipadx = 12; 
      scroll = new Scrollbar( Scrollbar.HORIZONTAL );
      gb.setConstraints( scroll, gbc );
      this.add( scroll );

      // the value
      value = new TextField( 6 );
      value.setEditable( false );
      gbc.anchor = GridBagConstraints.NORTHWEST; 
      gbc.fill = GridBagConstraints.NONE; 
      gbc.gridx = GridBagConstraints.RELATIVE; 
      gbc.weightx = 0.2; 
      gbc.weighty = 1.0; 
      gb.setConstraints( value, gbc );
      this.add( value );

      this.setLayout( gb );
   }

   /**
   * Handle all scrollbar events.  The associated text field is updated
   * on any scrollbar manipulation.  This implementation lets the superclass
   * handle all non-scroll events.
   * @param evt The Scrollbar event to handle.
   * @return true if event has been handled; false otherwise.
   */
   public boolean handleEvent ( Event evt ) {

      switch (evt.id) {
         case Event.SCROLL_LINE_UP:
         case Event.SCROLL_LINE_DOWN:
         case Event.SCROLL_PAGE_UP:
         case Event.SCROLL_PAGE_DOWN:
         case Event.SCROLL_ABSOLUTE:
            value.setText( ""+scroll.getValue() );
            break;
         default:
            return( super.handleEvent( evt ));
      }

      return( false );
   }

   /**
   * Get the scrollbar's line increment value.
   * @return The increment value.
   */
   public int getLineIncrement ( ) {
      return( scroll.getLineIncrement() );
   }

   /**
   * Get the scrollbar's maximum value.
   * @return The maximum value.
   */
   public int getMaximum ( ) {
      return( scroll.getMaximum() );
   }

   /**
   * Get the scrollbar's minimum value.
   * @return The minimum value.
   */
   public int getMinimum ( ) {
      return( scroll.getMinimum() );
   }

   /**
   * Get the scrollbar's orientation value.
   * @return The orientation value.
   */
   public int getOrientation ( ) {
      return( scroll.getOrientation() );
   }

   /**
   * Get the scrollbar's page increment value.
   * @return The increment value.
   */
   public int getPageIncrement ( ) {
      return( scroll.getPageIncrement() );
   }

   /**
   * Get the scrollbar's current value.
   * @return The current value.
   */
   public int getValue ( ) {
      return( scroll.getValue() );
   }

   /**
   * Get the scrollbar's visible value.
   * @return The visible value.
   */
   public int getVisible ( ) {
      return( scroll.getVisible() );
   }

   /**
   * Set the scrollbar's line increment value.
   * @param l The line increment value.
   */
   public void setLineIncrement ( int  l ) {
      scroll.setLineIncrement( l );
   }

   /**
   * Set the scrollbar's page increment value.
   * @param l The page increment value.
   */
   public void setPageIncrement ( int  l ) {
      scroll.setPageIncrement( l );
   }

   /**
   * Set the scrollbar's value.
   * @param l The value.
   */
   public void setValue ( int  _value ) {
      scroll.setValue( _value );
      value.setText( ""+_value );
   }

   /**
   * Set several of the the scrollbar's values.
   * @param _value The value.
   * @param visible The visibility value.
   * @param minimum The minimum value.
   * @param maximum The maximum value.
   */
   public void setValues ( int  _value, int  visible,
                           int  minimum, int  maximum) {
      scroll.setValues( _value, visible, minimum, maximum );
      value.setText( ""+_value );
   }

   /**
   * Set the label.
   * @param l The label value.
   */
   public void setLabel ( String l ) {
      label.setText( l );
   }

   /**
   * Get the label value.
   * @return The label.
   */
   public String getLabel ( ) {
      return( label.getText() );
   }

   /**
   * Get a string representation of the extended scrollbar values.
   * @return The values of the label and scrollbar.
   */
   public String toString ( ) {
      String s = "ExtendedScroll[ "+label.getText()+","+value.getText()+"]";
      return( s );
   }

   /**
   * Make sure all components get resized.
   */
   public void reshape ( int  x, int  y, int  width, int height ) {
      scroll.resize( width - label.size().width - value.size().width,
            scroll.size().height );

      super.reshape( x, y, width, height );
   }
}
