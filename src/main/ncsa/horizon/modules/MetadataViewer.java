/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-7, Board of Trustees of the University of Illinois
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
 *  97nov    fm   Original version
 *  97dec06  rlp  moved to modules package
 */
package ncsa.horizon.modules;

import java.awt.*;
import java.lang.*;
import java.util.*;
import java.applet.*;

import ncsa.horizon.util.*;
import ncsa.horizon.coordinates.*;

import Cool_Beans.awt.*;
import misc.*;

/**
 * A panel which displays Metadata in hierarchical (tree-like) fashion.
 * It uses some ready-made packages and some custom classes to do that.<p>
 *
 * Using it should be simple. The user of the class should simply create an
 * object of type MetadataViewer and allocate a (600, 400) pixel area in her
 * application or applet in order to display the Metadata object that needs to
 * be displayed. The Metadata object to be displayed can be displayed
 * immediately if it is passed with the constructor, for example,
 * <pre>
 *      MetadataViewer mdv = new MetadataViewer(mdata);
 *      mdv.display();
 *      ScrollablePanel span = new ScrollablePanel(mdv);
 *      setLayout(new BorderLayout());
 *      this.add("Center",span);
 * </pre>
 * where `mdata' is the Metadata object the user wants to be displayed.<p>
 *
 * @author Devang Mehta
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */

public class MetadataViewer extends Panel
{
  private Metadata mdata = null; // this is the object to be viewed.
  private Button treeRoot = null; // The root node for the tree that we're
                                   // going to make
  private String mdataName = null; // name of the root of the Metadata tree
  private Object dummy = new Object(); // dummy object used at places.
  private int vPosition = 30;
  private int hPosition = 30;
  private int vStep = 30;
  private int hStep = 30;

  /**
   * Constructs a MetadataViewer object set to display the specified Metadata
   * object.
   * @param mdata the object to be displayed
   */

  public MetadataViewer(Metadata mdata)
    {
      setMetadata(mdata);
    }

  /**
   * Sets the MetadataViewer to display the specified Metadata object.
   * @param mdata the object to be displayed
   */

  private void setMetadata(Metadata mdata)
    {
      this.mdata = mdata;
      if (mdata != null) this.mdataName = "Metadata root";
      else mdataName = "No Metadata to display as yet";
      
      // setting the layout manager
      // this.setLayout(new PositionLayout());
      this.setLayout(new PositionLayout());

      // building the tree to be displayed.
      build(mdata, mdataName);
    }

  /**
   * Resizes and sets up the Metadata object to be displayed within the viewer.
   */

  public void display()
    {
      // Setting the default size for the MetadataViewer ScrollPane
      // this.setSize(600, 400);
      this.resize(600, 400);

      // re-displaying the whole thing
      this.invalidate();
      this.validate();
    }

  // add the Component to the tree.

  private void addToTree(Component c)
    {
      String pos = Integer.toString(hPosition) + " " + Integer.toString(vPosition);
      //System.err.println(pos);
      //add(pos, comp);

      this.add(pos, c);
      //Dimension d = c.preferredSize();
      //c.resize(d.width, d.height);
      //c.move(hPosition, vPosition);
      //c.reshape(hPosition, vPosition, d.width, d.height);
      //c.invalidate();
      //c.validate();
    }


  // Builds the tree which represents the given Object
  // (Metadata or Metavector).

  private void build(Object mdata, String name)
    {
      // we want to build the tree in different ways according to the type of
      // the object being provided to us. hence we have the following "if"
      // statements.

      if (mdata instanceof Metadata)
	{
	  // we want to create a root node to be returned
	  addToTree(new Button(name + " :Metadata"));
	  vPosition += vStep;
	  hPosition += hStep;

	  // I'm trying to get an Enumeration object so that i can get
	  // keys (names) to all the objects in the Metadata object.
	  Enumeration e = ((Metadata)mdata).metadatumNames();

	  // Running a recursive loop to get the values of all the Metadata and
	  // build the panel.
	  for (; e.hasMoreElements();)
	    {
	      // Saving the Metadatum name.
	      String nextName = (String)e.nextElement();

	      // Saving the Metadatum value.
	      Object nextValue = ((Metadata)mdata).getMetadatum(nextName);

	      // This is the part i like most. recursion!!!. if nextValue is a
	      // Metadata Object, then we recursively build a new root. If not,
	      // then we just create a new MDVPanel with the right arguments.
	      if (nextValue instanceof Metadata || nextValue instanceof Metavector)
		{
		  // insert a new node
		  build(nextValue, nextName);
		}
	      else
		{
		  // insert the MDVPanel into the tree.
		  addToTree(new MDVPanel(this, nextName, nextValue));
		  vPosition += vStep;
		}
	    }
	  hPosition -= hStep;
	  return;
	}	

      if (mdata instanceof Metavector) 
	{
	  int noOfElements = ((Metavector)mdata).defaultSize();
	  int count = 0;

	  // we want to create a root node to be returned
	  addToTree(new Button(name + " :MetaVector"));
	  vPosition += vStep;
	  hPosition += hStep;

	  // starting the loop to read the Metavector elements.
	  for (; count < noOfElements; ++count)
	    {
	      // Saving the Metavector name.
	      String nextName = name + "[" + count + "]";

	      // Saving the Metavector value.
	      Object nextValue = ((Metavector)mdata).elementAt(count);

	      // This is the part i like most. recursion!!!. if nextValue is a
	      // Metadata or Metavector Object, then we recursively build a
	      // new root. If not, then we just create a new MDVPanel with the
	      // right arguments.
	      if (nextValue instanceof Metadata || nextValue instanceof Metavector)
		{
		  build(nextValue, nextName);
		}
	      else
		{
		  // insert the MDVPanel into the tree.
		  addToTree(new MDVPanel(this, nextName, nextValue));
		  vPosition += vStep;
		}
	    }
	  hPosition -= hStep;
	  return;
	}

      // we want to create a root node to be returned
      addToTree(new Button(name + " :Unknown"));
      vPosition += vStep;
      return;
    }


  
  /**
   * Returns the Metadata object associated with the MetadataViewer.
   * @return the Metadata object associated with the MetadataViewer, null if there is no associated object
   */

  public Metadata getMetadata()
    {
      return mdata;
    }
}

/* Code for the MDVPanel */

// This class has been made to extend the capabilities of Panel
// to suit those required by the MDV.

class MDVPanel extends Panel
{
  private Button t = null;// the button which will show the t option
  private Button v = null;// the button which will show the v option
  private Button nameButton = null; // this contains the metadataName
  private String type = null; // used to store the type of Metadata
  private String value = null; // used to store the value of Metadata
  private String mdatumName = null; // used to store the value of the
                                    // Metadata name (key)
  private Component parentComponent = null; // a reference to the 
                              // parentComponent containing the
                              // MDVPanel. needed for refreshing the display
                              // after changes.
  private Object mdatum; // saving a reference to the object being shown by
                         // this Panel
 
  // Constructor for MDVPanel with a Metadata name and the type and value
  // strings.
   

  MDVPanel(Component parentComponent, String mdatumName, Object mdatum)
    {
      super(); // calling the super constructor.
      this.parentComponent = parentComponent; // storing a reference to the
                                              // parentComponent which
                                  // controls the display.
      setLayout(new FlowLayout()); // setting the layout manager
      this.mdatum = mdatum; // remembering values
      this.mdatumName = mdatumName; // remembering values.      
      // Saving the Metadatum value in String form
      value = mdatum.toString();
      // Saving the Metadatum type in Class form
      Class mdatumClass = mdatum.getClass();
      // Saving the Metadatum type in String form
      type = mdatumClass.toString();
      // Printing the values; for debugging.
      //System.err.println(mdatumName + " " + value);

      nameButton = new Button(mdatumName); // creating Buttons.

      t = new Button(type);
      v = new Button(value);

      this.add(nameButton); // adding Buttons to the MDVPanel
      this.add(t);
      this.add(v);
    }

  public String getMetadatumName()
    {
      return(mdatumName);
    }
}








