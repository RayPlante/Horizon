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

  Filename: CGImageBrowser

  Description: see Java DeSP file

  Version: 1 (unfinished)

  By: Wei Xie, R. Plante

  of: National Center for Supercomputing Applications (NCSA)

  Date: 

 */

package apps.CGImageBrowser;
   
import java.applet.Applet;   
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;   
import java.lang.*;
import java.util.*;

import ncsa.horizon.awt.*;
import ncsa.horizon.modules.*;
import ncsa.horizon.viewable.*;
import ncsa.horizon.viewer.*;

public class CGImageBrowser extends Applet
{
  private static final boolean isDebug = false;

  Panel urlPanel; // panel for url browser
  Label urlLabel;
  TextField urlField;
  Button browseButton;
  Button repeatButton; // Show the previous images
  Button zoomButton; // pop out the zoomControl frame
  static TextArea msgArea;
  RoundArray urlHistory;  // save all the urls
  
  static FileDialog fileDialog;
  boolean isApplet = true;
  
  SimpleFrame imageFrame;
  Viewer viewer;
  Viewable viewable;

  SimpleFrame zoomFrame;
  ZoomControl zoomControl;
  String urlString;
  String fSep = null;
  String pwd = null;

  public CGImageBrowser() { super(); }
  public CGImageBrowser(boolean isApplet) { 
      super(); this.isApplet = isApplet; 
  }
    
  public void init()
  {
    setLayout(new BorderLayout());
    urlPanel = new Panel();
    urlPanel.setLayout(new FlowLayout());
    
    add("North", urlPanel);
    urlLabel = new Label("URL");
    urlPanel.add(urlLabel);

    if (isApplet) {

	// If we are an applet, the data directory will is set by
	// the document base and the "datadir" applet parameter.  The 
	// default file is given by the value of "imgFile"
	//
	String datadir = getParameter("datadir");
	if (datadir == null) datadir = "";
	String URLbase = getDocumentBase().toString();
	if (! URLbase.endsWith("/")) {
	    int p = URLbase.lastIndexOf('/');
	    if (p > 0 && URLbase.charAt(p-1) != '/') 
		URLbase = URLbase.substring(0, p+1);
	}
	String imgFile = getParameter("imgFile");
	if (imgFile == null)
	    System.out.println("Warning: No image file specified");
	urlString = URLbase + datadir + imgFile;
    }	
    else {

	// If we are running as an application, we will expect the 
	// data to be in ../data, relative to the current working 
	// directory
	//
	fSep = System.getProperty("file.separator");
	pwd = System.getProperty("user.dir");
	String imgFile = "io.gif";
	urlString = new String(pwd + fSep + ".." + fSep + "data" + 
			       fSep + imgFile); 
    }

    urlField = new TextField(urlString, 30);
    urlPanel.add(urlField);
    browseButton = new Button("Browse...");
    urlPanel.add(browseButton);
    if (isApplet) browseButton.disable();
    repeatButton = new Button("Previous");
    urlPanel.add(repeatButton);
    zoomButton = new Button("Zoom");
    urlPanel.add(zoomButton);
    
    msgArea = new TextArea("Click Browse button to load a local file, or " +
			   "enter a URL above and hit return.\n", 10, 30);
    msgArea.setEditable(false);
    add("South", msgArea);
    
    imageFrame = new SimpleFrame("Computer Graphics Image Browser");
    viewer = new GraphicsSelectionViewer(320,320);
    imageFrame.add("Center", new CGIB_Panel(viewer) );
    imageFrame.pack();
        
    zoomFrame = new SimpleFrame("Zoom Control");
    zoomFrame.validate();
    
    zoomControl = new ZoomControl((SelectionViewer)viewer);
    zoomControl.validate();
    zoomControl.resize(350, 170);

    zoomFrame.setLayout(new BorderLayout());
    zoomFrame.add("Center", zoomControl);
    zoomFrame.pack();

    urlHistory = new RoundArray();
  }  // end init

  public void stop() {
      zoomFrame.hide();
      imageFrame.hide();
      super.stop();
  }
  
  public boolean action( Event e, Object arg)
  {
    if(isBrowsebuttonClicked(e.target))
    {
      Viewable vtemp;
      String url_name = getLocalFilename();
      
      if(url_name == null)
      {
        displayMsg("null Filename");
        return false;
      }
      vtemp = getViewable(url_name);
      if(vtemp == null)
      {
        displayMsg("null viewable");
        return false;
      }
      if(displayViewable(vtemp))
      {
        setCurrentViewable(vtemp);
        save2Urlhistory(url_name);
        showCurrentUrl();
      }
      if(isDebug)
      {
        System.out.print("action: ");
        // Io.press_enter_continue();
      }
    }
    
    if(isRepeatbuttonClicked(e.target))
    {
      String url_name = getPreviousUrl();
      displayViewable(getViewable(url_name));
      showCurrentUrl();
    }
    
    if(isZoombuttonClicked(e.target))
    {
      zoomFrame.show();
    }

    if(isUrlfieldEntered(e.target))
    {
      String url_name = get4Urlfield();
      if(url_name == null)
      {
        displayMsg("null url name");
        return false;
      }
      Viewable vtemp = getViewable(url_name);
      if(vtemp == null)
      {
        displayMsg("null viewable");
        return false;
      }
      if(displayViewable(vtemp))
      {
        setCurrentViewable(vtemp);
        save2Urlhistory(url_name);
      }
    }
    return false;
  }  // end action
  
  public Viewable currentViewable()
  {
    return viewable;
  }
  
  public void displayMsg(String message)
  {
    msgArea.appendText(message);
  }
  
  public void displayViewable()
  {
    displayViewable(currentViewable());
  }
  
  public boolean displayViewable(Viewable v)
  {
    showImageframe();
    displayMsg("\nTo roam image, click on Zoom button above, then\n");
    displayMsg("use the mouse to make the following selections:\n");
    displayMsg("  Pixel:   Left button\n");
    displayMsg("  Box:     Right button (META-Left button)\n");
    displayMsg("  Line:    Middle button (ALT- or SHIFT-Left button)\n");
    displayMsg("Click \"Edit Graphics\" button to change selection " + 
	       "properties\n");
    viewer.addViewable(v);
    viewer.displaySlice();
    repaint();
    if(isDebug)
    {
      System.out.print("displayViewable: ");
      // Io.press_enter_continue();
    }
    return true;
  }
  
  public String getCurrentUrl()
  {
    return (String) urlHistory.currentElement();
  }
  
  public String get4Urlfield()
  {
    return urlField.getText();
  }
  
  public String getLocalFilename()
  {
    fileDialog.show();
    return (fileDialog.getDirectory() + fileDialog.getFile());
  }
  
  public String getPreviousUrl()
  {
    urlHistory.cclockAdvanceIndex();
    return getCurrentUrl();
  }
    
  public Viewable getViewable(String url_name)
  {
    URL url;
    Viewable vtemp;
    
    if(TSV_HttpJudge.ishttp(url_name))
    {
      try
      {
        url = new URL(url_name);
      }
      catch (MalformedURLException e)
      {
        displayMsg( "Error loading image:\n   " + e.getMessage() + "\n");
        return null;
      }

      vtemp = new ComputerGraphicsViewable(url);
    }
    else
    {
      vtemp = new ComputerGraphicsViewable(url_name);
    }
    return vtemp;
  } // end getViewable
  
  private boolean isBrowsebuttonClicked(Object o)
  {
    if (o instanceof Button)
    {
      if((Button)o == browseButton)
	return true;
      else
        return false;
    }
    else
      return false;
  }
  
  private boolean isRepeatbuttonClicked(Object o)
  {
    if (o instanceof Button)
    {
      if((Button)o == repeatButton)
	return true;
      else
        return false;
    }
    else
      return false;
  }
  
  public boolean isUrlfieldEntered(Object o)
  {
    if(o instanceof TextField)
      return true;
    else
      return false;
  }
  
  private boolean isZoombuttonClicked(Object o)
  {
    if (o instanceof Button)
    {
      if((Button)o == zoomButton)
	return true;
      else
        return false;
    }
    else
      return false;
  }

  public void save2Urlhistory(String url_name)
  {
    urlHistory.addElement2RA(url_name);
  }
  
  public void setCurrentViewable(Viewable v)
  {
    viewable = v;
  }
  
  public void showCurrentUrl()
  {
    urlField.setText(getCurrentUrl());
  }
  
  public void showImageframe()
  {
    imageFrame.show();
  }
  
  public static void main(String argv[])
  {
    SimpleFrame mainFrame = new SimpleFrame("Computer Graphics Image Browser");
    CGImageBrowser app = new CGImageBrowser(false);

    mainFrame.add("Center", app);
    mainFrame.setKillOnClose();
    
    fileDialog = new FileDialog(mainFrame, "Load Image", FileDialog.LOAD);    
    String cwd = System.getProperty("user.dir");
    String fs = System.getProperty("file.separator");
    fileDialog.setDirectory(cwd + fs + ".." + fs + "data");
    
    app.init();
    app.start();
    
    mainFrame.pack();
    mainFrame.validate();
    mainFrame.show();
  }
}



/* TSV_HttpJudge used to check url format
  public boolean ishttp(String url_name)
  	 TSV_HttpJudge check if url_name is a legal http url
 */
  
class TSV_HttpJudge
{
  public static boolean ishttp(String url_name)
  {
    if((url_name.charAt(0) == 'h') && (url_name.charAt(0) == 'h'))
      return true;
    else
      return false;
  }
}

class RoundArray extends Vector
{
  int currentIndex;
  
  public RoundArray()
  {
    currentIndex = 0; // empty array
  }
  
  public void addElement2RA(Object obj)
  {
    clockAdvanceIndex();
    insertElementAt(obj, currentIndex);
  }
  
  public void cclockAdvanceIndex()
  {
    if(!isEmpty())  // if array is an empty do nothing
    {
      if(currentIndex == 0)
        currentIndex = size() - 1;
      else
        currentIndex--;
    }
  }
  
  public void clockAdvanceIndex()
  {
    if(!isEmpty())  // if array is an empty do nothing
    {
      if(currentIndex == size() - 1)
        currentIndex = 0;
      else
        currentIndex++;
     }
  }

  public Object currentElement()
  {
    return elementAt(currentIndex);
  }
  
  public int getCurrentIndex()
  {
    return currentIndex;
  }
}

class CGIB_Panel extends Panel {
    public Component item = null;

    public CGIB_Panel(Component viewer) { 
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


