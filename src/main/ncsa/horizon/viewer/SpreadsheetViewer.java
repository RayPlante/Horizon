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
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    09-Dec-1997 Ray Plante  improved support for default slices
 *    19-Jan-1998 Ray Plante  updated to NdArray* move to ncsa.horizon.data
 */

package ncsa.horizon.viewer;

import java.awt.*;
import java.util.*;
import ncsa.horizon.coordinates.CoordinateSystem;
import ncsa.horizon.modules.Spreadsheet;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.Volume;
import ncsa.horizon.util.JavaType;
import ncsa.horizon.data.DataSlice;
import ncsa.horizon.viewable.Viewable;

public class SpreadsheetViewer extends Viewer {
  /** the current viewable */
  protected Viewable viewable;

  /** CoordinateSystem object for converting between data pixels and 
   *  world coordinates */
  protected CoordinateSystem coord=null;

  /** A canvas used to display the image and hanle the selection
   * events */
  protected SelectionSpreadsheet display;

  protected Slice slice;

  /**
   */
  Dimension pref_size = new Dimension(500, 500);

  /**
   * create a viewer with no argument
   */
  public SpreadsheetViewer() {
    // Set up the display area with the requested size
    display = new SelectionSpreadsheet();
    layoutComponents();
  } // end constructor(int, int)

  /**
   * replace the current Viewable object with a new one; the display 
   * will not be affected until displaySlice() is called.
   */
  public synchronized void addViewable(Viewable data) {
    viewable = data;
    coord = viewable.getCoordSys();
  } // end addViewable

  protected Slice createDefaultSlice(Viewable v) {
    Volume volume = v.getData().getVolume();
    return new Slice(volume);
  } // end createDefaultSlice

  /**
   * Display a default slice of the current Viewable.
   */
  public void displaySlice() {
    displaySlice(null);
  }

  /**
   * display a slice from the current Viewable data, or do nothing if
   * the current Viewable is not set.
   */
  public synchronized void displaySlice(Slice sl) {
    Slice requestedSlice = sl;

    //make sure there is a Viewable attached
    if(getViewable() == null) // initial state
      return;

    // if no slice was given, use the viewable's default slice
    if (requestedSlice == null) requestedSlice = viewable.getDefaultSlice();
    if (requestedSlice == null) requestedSlice = createDefaultSlice(viewable);

    if (requestedSlice == null) {
	System.err.println("Failed to come up with default slice; " + 
	                   "display request aborted");
	return;
    }

    // Now that we are sure we have a usable slice; now we will 
    // save it as the current slice being displayed.
    slice = requestedSlice;
    slice.makeLengthsPositive();
    
    display.setSpreadsheetData(new DataSlice(viewable.getData(), slice));
    display.getSpreadsheetData();
    display.updateSpreadsheet();
  } // end displaySlice(Slice sl)

  /**
   * This method returns the size in display pixel units of the region 
   * that displays a Viewable
   * @return Dimension of the compoonent
   * @see java.awt.Dimension
   * @see java.awt.Component.size()
   */
  public Dimension getDisplaySize() {
    return null;
  } //end getDisplaySize

  /**
   * Return a reference to the current Viewable object, or null if 
   * none are attached to this Viewer.
   * @return The current Viewable object; null if none present.
   */
  public Viewable getViewable() {
    return viewable;
  } // end getViewable

  /**
   * return a Slice object describing the data currently being viewed, 
   * or null if there is no Viewable currently being viewed.
   */
  public Slice getViewSlice() {
    return (slice == null) ? null : (Slice) slice.clone();
  } // end getViewSlice

  // works soly for constructor
  private void layoutComponents() {
    setLayout(new BorderLayout());
    // Set up the layout
    add("Center", display);
  } //end layoutComponents

  /**
   * return the preferred size of this Viewer Panel
   */
  public Dimension preferredSize() {
    return pref_size;
  } // end preferredSize

  public void update(Observable o, Object arg) {
    ;
  }
}

class SelectionSpreadsheet extends Spreadsheet {
  SelectionSpreadsheet() {
    this(null);
  }

  SelectionSpreadsheet(Object obj) {
    super(obj);
  }

  /** Prepare the spreadsheet data after the spreadsheet 
   * frame has been setup 
   * . This method should be overwriten. 
   */
  public  void getSpreadsheetData() {
    // spreadsheet row & colnum number (range of the spreadsheet )
    int rowNum = getRowNumber();
    int colNum = getColnumNumber();
    //  force to release the space 
    int rows = 0;
    int firstRow = getFirstLineNumber();
    // for each record 
    for (int kk= firstRow;
	 kk< (rowNum  + firstRow);
	 kk++, rows++) {
      // for each field of a vdata
      for (int i=0; i< colNum; i++) {
	// specify read data size
	int  dataType = getColnumType(i);
	int  order    = getColnumOrder(i); 
	// assume repeat number is less than 3
	if ((order>3) && (dataType != DFNT_CHAR8) &&
	    (dataType != DFNT_UINT8) ) 
	  order = 3;
	int datasize = getDataTypeSize(dataType);
	if (datasize == FAIL)
	  return;
	if (order>0) { // non dummy
	  datasize = order * datasize;   
	  //char[] dat = new char[datasize];
	  // organize data
	  switch(dataType) {
	  case DFNT_CHAR:  
	  case DFNT_UCHAR8:
	    //data[rows][i] = new String(dat,0,datasize);
	    break;
	    // signed integer (byte)	
	  case DFNT_UINT8: 
	  case DFNT_INT8: {
	    /*************************************************
	      String tmpStr="";
	      for (int j=0; j<order; j++) {
	      int tmpVal = (int)((byte)dat[j]);
	      tmpStr += Integer.toString(tmpVal) +"  ";
	      }
	      data[rows][i] = new String(tmpStr);
	      **************************************************/
	    break;
	  }
	  // short	
	  case DFNT_INT16:
	  case DFNT_UINT16: {
	    /************************************************* 
	      String tmpStr="";
	      int pos = 0;
	      for (int j=0; j<order; j++) {
	      int tmpVal = (short)HDF.readShort(dat, pos);
	      tmpStr += Integer.toString(tmpVal) +"  ";
	      pos += HDF.DFKNTsize(dataType);
	      }
	      data[rows][i] = new String(tmpStr);
	      *************************************************/
	    break;
	  }	    
	  // integer	
	  case DFNT_INT32:
	  case DFNT_UINT32: {
	    /****************************************
	      String tmpStr="";
	      int pos = 0;
	      for (int j=0; j<order; j++) {
	      int tmpVal = HDF.readInt(dat, pos);		    
	      tmpStr += Integer.toString(tmpVal) +"  ";
	      pos += HDF.DFKNTsize(dataType);
	      }
	      data[rows][i] = new String(tmpStr);
	      *****************************************/
	    break;
	  }
	  // Float	
	  //case DFNT_FLOAT:
	  case DFNT_FLOAT32: {
	    /*****************************************
	      String tmpStr="";
	      int pos = 0;
	      for (int j=0; j<order; j++) {
	      float tmpVal = HDF.readFloat(dat, pos);
	      tmpStr += Float.toString(tmpVal) +"  ";
	      pos += HDF.DFKNTsize(dataType);
	      }
	      
	      data[rows][i] = new String(tmpStr);
	      *********************************************/
	    break;
	  }
	  // Double	
	  //case DFNT_DOUBLE: 
	  case DFNT_FLOAT64: {
	    /*********************************************
	      String tmpStr="";
	      int pos = 0;
	      for (int j=0; j<order; j++) {
	      
	      float tmpVal =(float)HDF.readDouble(dat, pos);	       
	      tmpStr += Float.toString(tmpVal) +"  ";
	      pos += HDF.DFKNTsize(dataType);		    
	      }
	      data[rows][i] = new String(tmpStr);
	      *********************************************/
	    break;
	  }
	  default:
	    data[rows][i] = new String("");
	  } // switch()
	} // if (order > 0)
      } // for (int i=0; ...)
    } // for (int kk=....)
  }
  
  /**
   * Since I would like to use inherited Object obj
   * so I use Object dataSlice
   */
  public void setSpreadsheetData(DataSlice dataSlice) {
    Rectangle rect = new Rectangle(0, 0, dataSlice.getXaxisLength(), 
				   dataSlice.getYaxisLength());
    // set variables for spreadsheet by provided  bounding box
    // set first row of spreadsheet
    setFirstLineNumber(dataSlice.getYaxisLocation());
    // set spreadsheet row number
    setRowNumber(rect.height);
    // set spreadsheet colnum number
    setColnumNumber(rect.width);
    // specify spreadsheet variables
    String[] cellName = new String[rect.width];
    int[] cellType = new int[rect.width];
    int[] cellOrder= new int[rect.width];
    // set cell name, cell type , cell order...
    int firstColnum = dataSlice.getXaxisLocation();
    JavaType dataType = dataSlice.getType();
    for (int i=0; i<rect.width; i++) {
      // i colnum
      cellName[i] = new String(Integer.toString(i+firstColnum));
      switch(dataType.code()) {
      case 7:
	cellType[i] = Spreadsheet.DFNT_FLOAT32;
      default:
	cellType[i] = Spreadsheet.DFNT_UCHAR8;
      }
      cellOrder[i] = 12;    
    }
    // set spreadsheet frame
    setColnumName(cellName);
    setColnumType(cellType);
    setColnumOrder(cellOrder);
    data = new String[rect.height][rect.width];
    Object dataArray = dataSlice.getValue();
    for(int i=0; i < rect.height; i++) { // row
      int rowStart = i * rect.width;
      for(int j = 0; j < rect.width; j++) {
	data[i][j] = dataType.
	    wrappedValueFromArray(dataArray, rowStart + j).toString();
      }
    } 
  }
}
