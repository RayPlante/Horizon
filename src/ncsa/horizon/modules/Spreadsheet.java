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
 *-------------------------------------------------------------------------
 * History: 
 *  97nov    wx   Original version ported from the JDK
 *                   1.0.2-compatible version of the HDF viewer by
 *                   Xinjian Lu
 *  97dec06  rlp  Moved to modules package
 *  97dec09  rlp  fixed NegativeArraySizeException throwing bug in 
 *                   computeCellPosition().
 */

package ncsa.horizon.modules;

import java.awt.*;
import java.io.*;

/** This class will display the simple dataset or complex metadata 
 *  within the spreadsheet 
 */
class SpreadsheetData extends Canvas {
  /**  the spreadsheet  frame */
  Spreadsheet	 spreadsheetPanel;

  /** a spreadsheet data value   */
  String[][]       data = null;

  //  the current canvas size  
  int           canvasWidth ;
  int           canvasHeight;

  /** the cell size of the spreadsheet (default) */
  public  static  Rectangle CellRect = null ;

  /** the cell size of each field for metadata */
  public  Rectangle[]  dataCellRect = null;
    
  /** spreadsheet row number   */
  public  static  int cellNumberByRow;
    
  /** spreadsheet colnum number  */ 
  public  static  int cellNumberByColnum;
  
  // spreadsheet width size;  
  public int spreadsheetWidth = 0;
    
  // 10 digits number to be displayed for each cell 
  public  static  int NumberLength = 10;
    
  // the default font size
  public static   int defaultFontSize = 14;
    
  // Create the new font for displaying the  data correctly 
  // in the specified canvas within the scrollbar
  Font dataFont = new Font("Fixed", Font.PLAIN, defaultFontSize);
    
  // default font width & height
  public static   int fontWidth = 14;
  public static   int fontHeight= 15;
    
  // Flicker-free update with translation by scrollbars
  Image offScreenImage = null;
  Dimension offscreensize;		
  Graphics offGraphics;
     
  //  the indicator of the spreadsheet frame
  boolean frameDisplayed = false;
    
  // translated value
  // All subsequent operations on this graphics context will be relative
  // to this origin.
  int tx = 0;
  int ty = 0;
    
  /** offset value of the scrollbar  */
  int hOffset = 0;

  /** offset value of the scrollbar  */
  int vOffset = 0;
       
  // the position and size of each cell in the spreadsheet
  Rectangle [][] cells = new Rectangle[120][60];
     
  // the indicator that the spreadsheet has been selected 
  boolean drawFlag = false;
    
  // draw area
  Rectangle drawRectangle = null;

  /** new constructor
   * @param panel the spreadsheet object
   */
  public SpreadsheetData(Spreadsheet panel) {
    // set spreadsheet object
    spreadsheetPanel = panel;
    // do initalization
    initialize();
  }

  /** Calculate spreadsheet  cell size  based on provided metadata by row.
   *  The metedata may be associated with different order.
   */
  public void calculateCellSize() {	
    // set cell size based on the current selected font.
    CellRect = new Rectangle(NumberLength*(fontWidth+10), fontHeight + 4);
    // spreadsheet colnum number
    int colNum = spreadsheetPanel.getColnumNumber();
    // set spreadsheetWidth
    spreadsheetWidth = 0;
    // big enough ?
    dataCellRect = new Rectangle[colNum + 1];
    // for each field(colnum)
    for (int i=0; i<colNum; i++) {
      // get selected colnum index
      int index = i;
      int datatype   = spreadsheetPanel.getColnumType(i);
      int order      = spreadsheetPanel.getColnumOrder(i);
      String fldName = spreadsheetPanel.getColnumName(i);
      // assume repeat number is less than 3
      if ((order>3) && (datatype != spreadsheetPanel.DFNT_CHAR8)) // &&(dataType != HDFConstants.DFNT_UINT8) )	                      
	order = 3;
      if ((datatype != spreadsheetPanel.DFNT_CHAR8) && 
	  (datatype != spreadsheetPanel.DFNT_UCHAR8)) {
	// set cell size based on the current selected font.
	int numLength = NumberLength; // default
	if ((datatype == spreadsheetPanel.DFNT_INT8)  ||
	   (datatype == spreadsheetPanel.DFNT_UINT8)  ||
	   (datatype == spreadsheetPanel.DFNT_INT16)  ||
	   (datatype == spreadsheetPanel.DFNT_UINT16) )
   	   numLength /= 2;
	else if ((datatype == spreadsheetPanel.DFNT_FLOAT32)  ||
		 (datatype == spreadsheetPanel.DFNT_FLOAT64) )		   
		numLength += 2;
	int len = Math.max(order * numLength, fldName.length());
	//int len = Math.max(order * NumberLength, fldName.length());
	//if (order > 2) 
	//   dataCellRect[i] = new Rectangle(len*(fontWidth+1)/2, fontHeight + 4);
	// else
	dataCellRect[i] = new Rectangle(len*(fontWidth+10), fontHeight + 4);
      }
      else {
	// char 
	int len = Math.max(order, fldName.length());    	
	dataCellRect[i] = new Rectangle( len * (fontWidth+10), fontHeight + 4);
      }
      // compute spreadsheet width
      spreadsheetWidth  += dataCellRect[i].width;
    }
  } //end calculateCellSize

  /**
   * If the size of the spreadsheet is appropriate 
   * the spreadsheet will be displayed
   */
  public  void checkSize() {
    // set canvas size
    resize(canvasWidth, canvasHeight);
    if (!frameDisplayed) {
      // popup frame
      spreadsheetPanel.popup();
      frameDisplayed = true;
    }
  } //end checkSize

  /** initialize some variables and get displayed data string */
  public void initialize() {   
    // set identifier of spreadsheet frame
    frameDisplayed = false;
    // set font
    setFont(dataFont);
    // calculate cell size
    calculateCellSize();
    // prepare the spreadsheet data
    setSpreadsheetData();
    // set draw flag
    drawFlag = false;
  } // end initialize

  /** Set minimum size at SxS for current canvas  */
  public Dimension minimumSize(){
    return new Dimension(canvasWidth, canvasHeight);
  }

  /** Set preferred  size at SxS for current canvas */ 
  public Dimension preferredSize() {
    return minimumSize();
  }

  /** get cell number by provided canvas size (w*h)
   * @param w the width  of canvas
   * @param h the height of canvas
   */
  public void getCellNumber(int w, int h) {
    // get cell number by row for current canvas size
    cellNumberByRow = h / CellRect.height;
    int colNum = 0;
    int width = 0;
    // get cell number by colnum for current canvas size
    for (int i=hOffset; i<spreadsheetPanel.getColnumNumber(); 
	 i++, colNum++)  {
      width += dataCellRect[i].width;
      if (width > w)
	break;
    }
    cellNumberByColnum  = colNum;   
    //System.out.println("row: " + cellNumberByRow);
    //System.out.println("col: "  +cellNumberByColnum );
  } //end getCellNumber

  /**
   * Called if the mouse is down.
   * @param evt the event
   * @param x the x coordinate
   * @param y the y coordinate
   * @see java.awt.Component#mouseDown
   */
  public boolean mouseDown(Event evt, int x, int y) {
    // set draw flag
    drawFlag = false;
    // when mouse down , paint the cell to deep gray(darkGray)
    // first using the mouse position to get the cell position
    // int col = x / CellRect.width  ;
    int row = y / CellRect.height ;
    int col = 0;
    int len = 0;
    for (int i=hOffset; i<cellNumberByColnum + hOffset;  i++, col++) {
	len += dataCellRect[i].width;
	if (len>x)
	    break;
    }
    Rectangle rect = null;
    if (cells[row][col] != null)
      rect = cells[row][col];
    //System.out.println("rect: " + rect);
    if (rect != null) {
      // confirm this cell will be redraw
      if (rect.inside(x,y)) {  
	// the specified point lies inside a rectangle
	drawFlag = true;
	drawRectangle = rect;
      }
    }
    // repaint
    repaint();
    //return super.mouseDown(evt,x,y);
    return true;
  } // end mouseDown

  /** draw the spreadsheet */
  public  void paint(Graphics g) {
    // get background color
    Color bColor = getBackground();
    // get current graphics color 
    Color gColor = g.getColor();
    // get current canvas size
    int w = size().width;
    int h = size().height;
    // compute the current lines & colnums based on the canvas size
    //getCellNumber(w,h);
    computeCellPosition();
    //System.out.println("rows: " + cellNumberByRow);
    //g.translate(-tx,-ty);
    // repaint the spreadsheet label
    spreadsheetPanel.rowInfoCanvas.init(h-2);
    spreadsheetPanel.colnumInfoCanvas.init(w-2);
    int startx = 1;
    int starty = 1;
    // draw the rectangle
    g.drawRect(startx,starty,w-2,h-2);
    // click on cell
    if (drawFlag) {
      // set background color
      g.setColor(Color.cyan);
      // draw cell
      if (drawRectangle != null)
	g.fillRect(drawRectangle.x, drawRectangle.y, 
		   drawRectangle.width-1, drawRectangle.height-1);
    }
    // set color
    g.setColor(Color.lightGray);
    // draw grid
    startx = 1;
    starty = 1;
    // draw the grid (Horizontal)
    for (int i=1; i<= cellNumberByRow; i++) {
      starty += CellRect.height;
      //g.drawLine(startx+1, starty+1, w-3, starty+1);
      g.draw3DRect(startx+1, starty+1, w-3, 1, true);
    }
    startx = 1;
    starty = 1;
    // draw the grid (Vertical)
    for (int i=1; i<= cellNumberByColnum; i++) {
      //startx += CellRect.width;
      if (dataCellRect[hOffset+i-1] != null)
	startx += dataCellRect[hOffset+i-1].width;
	
      //g.drawLine(startx+1, starty+1, startx+1, h-3);
      g.draw3DRect(startx+1, starty+1, 1, h-3, true);
    }  
    //System.out.println("Row: " + cellNumberByRow);
    //System.out.println("Col: " + cellNumberByColnum);
    // reset the color
    g.setColor(gColor);
    for (int i =0; i<=cellNumberByRow; i++) {
      startx = 1;
      for (int j=0; j<=cellNumberByColnum; j++) {
	// display position
	//startx = CellRect.width * j + 4;
	if (j>0) {
	  if (dataCellRect[hOffset+j-1] != null)
	    startx += dataCellRect[hOffset+j-1].width ;
	  else 
	    break;
	}
	starty = CellRect.height * (i+1) ;
	// display float number
	String dispStr = "";
	if (((vOffset+i) < (spreadsheetPanel.getRowNumber())) &&
	    ((hOffset+j) < (spreadsheetPanel.getColnumNumber()))) {
	  // assign value
	  dispStr = data[vOffset+i][hOffset+j];
   	  g.drawString(dispStr, startx + 4, starty);
	}
	else
	  continue; 
      }
    } // for (int i=0; i<cellNumberByRow; i++) {
    //g.translate( tx,ty);
  } // end paint

  /** 
   * Sets the font of the component.
   * @param f the font
   */
  public  void setFont(Font f) {
    // set Font for component
    super.setFont(f);
    // get new FontMetrics
    FontMetrics fontMetrics = getFontMetrics(getFont());
    // set font width & height
    fontHeight = fontMetrics.getHeight();
    fontWidth  = fontMetrics.charWidth('A');
  }

  /** Reshapes the Component to the specified bounding box. 
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width of the component
   * @param height the height of the component
   */
  public synchronized  void reshape(int x, int y, int w, int h) {
    super.reshape(x, y, w, h);
    // resize horizontal scrollbar
    setHScrollbarValue();
    // resize the vertical scrollbar
    setVScrollbarValue();
    // recompute the cell position
    computeCellPosition();
  }

  /** Compute the each cell position after the canvas changed */
  public void computeCellPosition() {
    // current canvas size
    int w = size().width;
    int h = size().height;
    if (h < 0) return;

    // compute cell number
    getCellNumber(w,h);
    // specify the dimension for the cells
    cells = null;
    cells = new Rectangle[cellNumberByRow][cellNumberByColnum+1];
    int starty = 2;
    for (int i=0; i<cellNumberByRow; i++ ) {
      starty = CellRect.height*i + 3;
      int startx = 2;
      for (int j=0; j<cellNumberByColnum; j++) {
	// x position
	//startx = CellRect.width*j + 3;
        if (j>0)
	    startx += dataCellRect[hOffset+j-1].width;
	// set rectangle    
	if (cells[i][j] == null)
	  cells[i][j] = new Rectangle(startx+3, starty, 
				      dataCellRect[hOffset+j].width, CellRect.height);
	else
	  cells[i][j].reshape(startx+3, starty, 
	                      dataCellRect[hOffset+j].width, CellRect.height);
      }
    }  // for (int i=0; i<cellNumberByRow; i++ ) {
  }
  
  /** Adjust the Scrollbar value by the specifyed dataset range	 */
  void setHScrollbarValue() {
    // get current canvas size
    int canvasWidth = size().width;
    int canvasHeight= size().height;
    // canvas is valid?
    if ((canvasWidth <= 0)||(canvasHeight<=0)) { 
      //System.out.println("Canvas has no width; can't resize scrollbar");
      return;
    }
    //Shift everything to the right if we're displaying empty space
    //on the right side.
    //int width = spreadsheetPanel.getColnumNumber()*CellRect.width;
    int 	width = spreadsheetWidth;
    if ((tx + canvasWidth) > width) {
      int newtx = width - canvasWidth;
      if (newtx < 0) {
	newtx = 0;
      }
      tx = newtx;
    }
   spreadsheetPanel.hScrollbar.setValues(
	     //draw the part of the dataset that starts at this x:
	     tx, 
	     //amount to scroll for a "page":
	     (int)(canvasWidth * 0.9), 
	     //minimum image x to specify:
	     0,
	     //maximum image x to specify:
	     width - canvasWidth);
    //"visible" arg to setValues() has no effect after scrollbar is visible.
    spreadsheetPanel.hScrollbar.setPageIncrement((int)(canvasWidth * 0.9));
    return;
  }

  /** Adjust the Scrollbar value by the specifyed dataset range	 */
  void setVScrollbarValue() {
    // get current canvas size
    int canvasWidth = size().width;
    int canvasHeight= size().height;
    // canvas is valid?
    if ((canvasWidth <= 0)||(canvasHeight<=0)) { 
      //System.out.println("Canvas has no width; can't resize scrollbar");
      return;
    }
    //Shift everything to the right if we're displaying empty space
    //on the right side.
    int height = spreadsheetPanel.getRowNumber()*CellRect.height+5;
    if ((ty + canvasHeight) > height) {
      int newty = height - canvasHeight;
      if (newty < 0) {
	newty = 0;
      }
      ty  = newty;
    }
    spreadsheetPanel.vScrollbar.setValues(
             //draw the part of the dataset that starts at this x:
             ty, 
	     //amount to scroll for a "page":
	     (int)(canvasHeight*0.9),
	     //minimum image y to specify:
	     0,
	     //maximum image y to specify:
	     height - canvasHeight);

    //"visible" arg to setValues() has no effect after scrollbar is visible.
    spreadsheetPanel.hScrollbar.setPageIncrement((int)(canvasHeight*0.9));
    return;
  }
  
  /** Set spreadsheet dataset 
   * @param dat an dataset of a spreadsheet 
   */
  public void setSpreadsheetData(String[][] dat) {
    // set spreadsheet data
    data = dat;
  }

  /** Set spreadsheet dataset 
   */
  public void setSpreadsheetData() {
    // set spreadsheet data
    setSpreadsheetData(spreadsheetPanel.data);
  }

  /** set size for current canvas 
   * @param w the width
   * @param h the height
   */
  public void setCanvasSize(int w,int h) {
    // set canvas size
    canvasWidth = w;
    canvasHeight= h;
  }

  /** set scrollbar offset(vertical) */
  public void setVoffset() {
    vOffset = ty/CellRect.height ;
    // adjust vOffset so we can see the last row clearly
    if (vOffset>1)
	++vOffset; 
  }

  /** set horizontal scrollbar offset value */
  public void setHoffset() {
    //hOffset = tx/CellRect.width;
    int offset = 0;
    int width  = 0;
    for (int i=0; i<spreadsheetPanel.getColnumNumber(); i++) {
      width += dataCellRect[i].width;
      if (width > tx) 
	break;
      else
	++offset;
    }
    // get offset value
    hOffset = offset;
    // more than one cell
    if (tx>dataCellRect[0].width)
        ++hOffset;
  } // end setHoffset

  /**
   * Updates the component. This method is called in
   * response to a call to repaint. You can assume that
   * the background is not cleared.
   * @param g the specified Graphics window
   * @see java.awt.Component#update
   */
  public   void update(Graphics g) {
    Dimension d = size();
    setCanvasSize(d.width, d.height);
    if (offScreenImage == null) {
      // offScreenImage not created; create it.
      offScreenImage = createImage(d.width*2, d.height*2);	
      // get the off-screen graphics context    
      offGraphics    = offScreenImage.getGraphics();
      // set the font for offGraphics
      offGraphics.setFont(getFont()); 
    }
    // paint the background on the off-screen graphics context
    offGraphics.setColor(getBackground());
    offGraphics.fillRect(1,1,d.width-2,d.height-2);    
    offGraphics.setColor(getForeground());
    // draw the current frame to the off-screen 
    paint(offGraphics);
    //then draw the image to the on-screen 
    g.drawImage(offScreenImage, 0, 0, null);
  }

  /** Update spreadsheet & repaint it */
  public void updateSpreadsheet() {
    // initialize spreadsheet variables
    initialize();
    // reset
    hOffset = 0;
    vOffset = 0;
    // set scrollbar value
    setVScrollbarValue();
    setHScrollbarValue();
    // re-calculate the cell position
    computeCellPosition();
    // reset scrollbar values
    spreadsheetPanel.vScrollbar.setValue(0);
    spreadsheetPanel.hScrollbar.setValue(0);
    //repaint
    repaint();
  } // end updateSpreadsheet

} // end SpreadsheetData

/** This class draw the spreadsheet label by spreadsheet canvas size */
class SpreadsheetLabel extends Canvas {
 
  /** the spreadsheet panel */
  Spreadsheet	spreadsheetPanel;
  
  /** the indicator of the spreadsheet label */
  int             orientation;
   
  /** The horizontal spreadsheet label variable. */
  public static final int	HORIZONTAL = 0;
   
  /** The vertical spreadsheet label   variable.  */    
  public static final int	VERTICAL   = 1;

  // default value of a spreadsheet row and colnum number
  int rectLen 	= 30;	
  int xLen	= 256*2;
  int yLen	= 256*1;

  Font		font;
  FontMetrics	fontMetrics;
  int		fontWidth, fontHeight;
    
  // double buffering
  // variables for duble-buffer
  Image 	offScreenImage = null;
  Graphics 	offGraphics;

  /** new class constructor 
   * @param panel  the spreadsheet object
   * @param direction the variable to specify the spreadsheet label
   */
  public SpreadsheetLabel(Spreadsheet panel, int direction) {
    // set spreadsheet panel
    spreadsheetPanel    = panel;
    // set canvas type
    orientation = direction;
    // set the current used font
    font 	    = panel.dataCanvas.dataFont;
    // Sets the font of the component.
    setFont(font);
    // initialize label canvas
    init();
  }

  /** Initialize the label canvas 
   * @param len the label canvas width or length
   */
  public void init(int len) {
    // set the canvas size
    if (orientation == HORIZONTAL) {
      // Assume scrollbar width to 10;
      xLen = len + rectLen + 20 ;
      resize(xLen, rectLen);
    }
    else {
      yLen = len;
      resize(rectLen, yLen);
    }
    // repaint the canvas
    repaint();
  } //end init

  /** Initialize the canvas by default */
  public void init() {
    // set the canvas size
    if (orientation == HORIZONTAL) 
      resize(xLen, rectLen);	
    else 
      resize(rectLen, yLen);  
  } //end init

  /** 
   * Sets the font of the component.
   * @param f the font
   */
  public synchronized void setFont(Font f) {
    // set Font for component
    super.setFont(f);
    // get new FontMetrics
    fontMetrics = getFontMetrics(getFont());
    // set font width & height
    fontHeight = fontMetrics.getHeight();
    fontWidth  = fontMetrics.charWidth('A');
  } //end setFont

  /** Draw spreadsheet label  */
  public synchronized void paint(Graphics g) {
    //setBackground(Color.pink);
    // cell size of the spreadsheet
    Rectangle rect = spreadsheetPanel.dataCanvas.CellRect;
    // the row and colnum of the current spreadsheet
    int rowNumber  = spreadsheetPanel.dataCanvas.cellNumberByRow;
    int colNumber  = spreadsheetPanel.dataCanvas.cellNumberByColnum;
    if (orientation == HORIZONTAL)  {
      g.setColor(Color.pink);
      g.fill3DRect(rectLen,1, xLen , rectLen, false);
      g.setColor(Color.white);
      // drawable position
      int x = rectLen;
      int y = 1;
      int pos = 0;
      int hVal = spreadsheetPanel.dataCanvas.hOffset	;
      for (int i=0; i<=colNumber; i++)
	if((hVal + i) <
	   spreadsheetPanel.numberOfColnum) {
	  //x = rectLen + rect.width*i + 4;
	  int offset = 0;
	  if(i>0)
	    pos +=   spreadsheetPanel.dataCanvas.dataCellRect[hVal+i-1].width;
	  // width for one character
	  int w = spreadsheetPanel.dataCanvas.fontWidth + 1;
	  if (i<colNumber)
	    // width of cell
	    offset = spreadsheetPanel.dataCanvas.dataCellRect[hVal+i].width/w;
	  x = rectLen + 4 + pos;
	  // find index of selected field  
	  int idx  =  spreadsheetPanel.dataCanvas.hOffset + i ;  
	  String kk = spreadsheetPanel.cellName[idx];
          // offset to display title within the center
	  if (offset > kk.length())
	    offset = (offset - kk.length()) * w / 2;
	  else
	    offset = 6;
	  // adjust display postion
	  x += offset;
	  // display title in the cnter of cell  
	  g.drawString(kk,x, rectLen-10 );
	}
    } //if (orientation == HORIZONTAL)  {
    else {
      // set color 
      g.setColor(Color.magenta);
      // draw the graphics
      g.fill3DRect(1,1, rectLen, yLen, true);
      g.setColor(Color.white);
      int y = 1;  
      // first value
      int kk = spreadsheetPanel.dataCanvas.vOffset + 
	       spreadsheetPanel.getFirstLineNumber();
      // selected  records number
      int selectedDataNumber = spreadsheetPanel.numberOfRow;
      for (int i=1; i<=rowNumber; i++)  
	if ((spreadsheetPanel.dataCanvas.vOffset + i) <=
	    selectedDataNumber)  {
	  y = rect.height*i;
	  g.drawString(Integer.toString(kk+i-1), 1, y);
	}
    } //   else {
  } // end paint
  
  /**
   * Updates the component. This method is called in
   * response to a call to repaint. You can assume that
   * the background is not cleared.
   * @param g the specified Graphics window
   * @see java.awt.Component#update
   */
  public void update(Graphics g) {
    Dimension d = size();
    if (offScreenImage == null) {
      // offScreenImage not created; create it.
      offScreenImage = createImage(d.width*2, d.height*2);	
      // get the off-screen graphics context    
      offGraphics    = offScreenImage.getGraphics();
      // set the font for offGraphics
      offGraphics.setFont(getFont());	 
    }
    // paint the background on the off-screen graphics context
    offGraphics.setColor(getBackground());
    offGraphics.fillRect(1,1,d.width-2,d.height-2);    
    offGraphics.setColor(getForeground());
    // draw the current frame to the off-screen 
    paint(offGraphics);
    //then draw the image to the on-screen 
    g.drawImage(offScreenImage, 0, 0, null);
  }

} // end SpreadsheetLabel

/**
 * This is a spreadsheet component. 
 * To use this class, you may write a derived class for 
 * your purpose, especifilly
 * overwrite the "getSpreadsheetData" method to organize 
 * the displayed spreadsheet
 * data.  The data may be a simple dataset or complex 
 * metadata which is an object
 * of another class(component).  See followed example:
 *  <pre>
 *  Suppose YourSpreadsheet is a subclass of Spreadsheet.
 *  // define a new YourSpreadsheet object with a new object
 *  YourSpreadsheet sp = new  YourSpreadsheet(new ImageTest("test.gif"));
 *  
 *  // define a bounding box for this image(subset of a image)
 *  Rectangle rect = new Rectangle(1,1,20,30);
 *
 *  // set spreadsheet variables based on this bounding box
 *  // set first line number of spreadsheet
 *  sp.setFirstLineNumber(rect.y);
 *
 *  // set spreadsheet row number
 *  sp.setRowNumber(rect.height);
 *   
 *  // set spreadsheet colnum number
 *  sp.setColnumNumber(rect.width);
 *   
 *  // specify spreadsheet colnum features
 *  String[] cellName = new String[rect.width];
 *  int[]    cellType = new int[rect.width];
 *  int[]    cellOrder= new int[rect.width];
 *
 *  // set cell name, cell type , cell order...
 *  int firstColnum = rect.x;
 *
 *  // simple spreadsheet setting
 *  for (int i=0; i<rect.width; i++) {
 *	    
 *	    // i colnum
 *          // name os colnum
 *	    cellName[i] = new String(Integer.toString(i+firstColnum));
 *          // namber type of colnum
 *	    cellType[i] = Spreadsheet.DFNT_UCHAR8;
 *          // repeat number of colnum
 *	    cellOrder[i]= 12;
 *  }
 *	
 *  // set spreadsheet frame
 *  sp.setColnumName(cellName);
 *  sp.setColnumType(cellType);
 *  sp.setColnumOrder(cellOrder);
 *  
 *  // origanize your data by overwitting this class
 *  sp.getSpreadsheetData() 
 *
 *  // repaint the spreadsheet
 *  sp.updateSpreadsheet();
 *  // your spreadsheet has been created. <p>
 * </pre>
 * @version 1.00 12 Sept 1996
 * @auther  Xinjian Lu, HDF Group
 */
public class Spreadsheet extends Panel {

  /** an object that spreadsheet will communicated with */
  protected Object obj;
    
  /** the spreadsheet canvas */
  protected SpreadsheetData dataCanvas;
  
  /** the spreadsheet label canvas */
  protected SpreadsheetLabel rowInfoCanvas,  colnumInfoCanvas;

  /** the scrollbar associated with the spreadsheet */
  protected Scrollbar hScrollbar, vScrollbar;
    
  /** the first row number of spreadsheet  */
  protected int firstLineNumber = 1;
    
  /** spreadsheet row number  */
  protected int numberOfRow = 0;
    
  /** spreadsheet colnum number */
  protected int numberOfColnum = 0;
    
  /** colnum name */
  protected String[] cellName ;
    
  /** colnum data number type */
  protected int[] cellType ;
    
  /** spreadsheet title */
  protected String title;
    
  /** colnum order */
  protected int[]    cellOrder;
  
  /** spreadsheet data set */
  protected String[][] data;
    
  // spreadsheet data number type
  /** unsigned char */
  public static final int DFNT_UCHAR8 = 3;
  public static final int DFNT_UCHAR  = 3;
    
  /** char */
  public static final int DFNT_CHAR8  = 4;
  public static final int DFNT_CHAR   = 4;
    
  /** No supported  */
  public static final int DFNT_CHAR16 = 42;
  public static final int DFNT_UCHAR16= 43;  
    
  /** float */
  public static final int  DFNT_FLOAT32   =  5;
  public static final int  DFNT_FLOAT     =  5 ;
    
  /** double */
  public static final int  DFNT_FLOAT64   =  6;
  public static final int  DFNT_DOUBLE    =  6  ;
    
  /** 8-bit integer */
  public static final int  DFNT_INT8      =  20;
    
  /** unsigned 8-bit interger */
  public static final int  DFNT_UINT8    =  21;
  
  /** short */
  public static final int  DFNT_INT16    =  22;
  
  /** unsigned interger */
  public static final int  DFNT_UINT16   =  23;
  
  /** interger */
  public static final int  DFNT_INT32    =  24;
  
  /** unsigned interger */
  public static final int  DFNT_UINT32   =  25;
    
  /** FAIL */
  public static int FAIL = -1;

  /** new constructor of the class.
   * @param obj  an  object
   */ 
  public Spreadsheet(Object obj) { 
    // set object
    this.obj = obj;
    // create new SpreadsheetData object
    dataCanvas = new SpreadsheetData(this);
    // set canvas size(default, which size is best?)
    dataCanvas.setCanvasSize(2*256,256);
    // create the canvas to display the title
    rowInfoCanvas   = new SpreadsheetLabel(this, SpreadsheetLabel.VERTICAL);
    // another canvas to hold the colnum title
    colnumInfoCanvas= new SpreadsheetLabel(this, SpreadsheetLabel.HORIZONTAL);
    // create spreadsheet graphics user interface
    createSpreadsheetGUI();
    // check the spreadsheet size
    dataCanvas.checkSize();
  }

  /** create Spreadsheet */
  public void createSpreadsheetGUI() {
    // set Layout Manager
    setLayout(new BorderLayout());
    // spreadsheet panel
    Panel sPanel = new Panel();
    sPanel.setLayout(new BorderLayout());	
    // Horizontal & vertical Scrollbar 
    hScrollbar = new Scrollbar(Scrollbar.HORIZONTAL);
    vScrollbar = new Scrollbar();
    sPanel.add("North", colnumInfoCanvas);	
    sPanel.add("West",  rowInfoCanvas);
    sPanel.add("Center", dataCanvas);
    sPanel.add("East",  vScrollbar);
    sPanel.add("South", hScrollbar);
    // set Spreadsheet
    add("Center", sPanel);
  }

  /** get spreadsheet title
   * @return a spreadsheet title
   */
  public String getSpreadsheetTitle() {
    return (String)title;
  }

 /** return spreadsheet first line number
   * @return a first row number of spreadsheet
   */
  public int getFirstLineNumber(){
    return(firstLineNumber);
  }

  /** get spreadsheet row number
   * @return the spreadsheet row number
   */
  public int getRowNumber(){
    return numberOfRow;
  }

  /** set colnum number
   * @param num   a colnum number of spreadsheet
   */
  public void setColnumNumber(int num){
    this.numberOfColnum = num;
  }

  /** get spreadsheet colnum number
   * @return a colnum number of spreadsheet
   */
  public int getColnumNumber(){
    return numberOfColnum ;
  }

  /** set spreadsheet colnum name
   * @param name a colnum name array of a spreadsheet
   */
  public void setColnumName(String[] name){
    cellName = name;
  }

  /** get spreadsheet colnum name
   * @return a colnum name array of a spreadsheet
   */
  public String[] getColnumName(){
    return(cellName);
  }

  /** get spreadsheet colnum name
   * @param index the colnum name
   * @return a specified colnum name
   */
  public String  getColnumName(int index) {
    if (index <= cellName.length) 
      return(cellName[index]);
    else
      return "";
  }

  /** set spreadsheet colnum type
   * @param type a colnum data type of a spreadsheet
   */
  public void setColnumType(int[] type){
    cellType = type;
  }

  /** get spreadsheet colnum type
   * @return a colnum data type of a spreadsheet
   */
  public int[] getColnumType(){
    return(cellType);
  }

  /** get spreadsheet colnum type
   * @param index the colnum type
   * @return a specified colnum data type of a spreadsheet
   */
  public int  getColnumType(int index) {
    if (index <= cellType.length) 
      return(cellType[index]);
    else
      return this.FAIL;
  }

  /** set  spreadsheet colnum order
   * @return a colnum order of a spreadsheet
   */
  public void setColnumOrder(int[] order){
    cellOrder = order;
  }

  /** get  spreadsheet colnum order
   * @param index the colnum  
   * @return a specified colnum order  
   */
  public int getColnumOrder(int index){   
    if (index <= cellOrder.length) 
      return(cellOrder[index]);
    else
      return this.FAIL;  
  }

  /** get  spreadsheet colnum order
   * @return a  colnum order  
   */
  public int[] getColnumOrder(){
    return(cellOrder);  
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
    data = null;
    // specify data size
    data = new String[rowNum][colNum];
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
	    data[rows][i] = new String(Integer.toString(kk+
					dataCanvas.hOffset)+ "," + 
				       Integer.toString(i+dataCanvas.vOffset));
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
  
  /**  Return the size of the data number type
   * @param nt the data number type
   * @return the size otherwise FAIL
   */
  public int getDataTypeSize(int nt) {
    int retVal = FAIL;
    switch(nt) {
    case DFNT_CHAR:  
    case DFNT_UCHAR8:
    case DFNT_UINT8: 
    case DFNT_INT8: 
      retVal = 1;
      break;
      // short	
    case DFNT_INT16:
    case DFNT_UINT16:
      retVal = 2;
      break;
      // integer & float	
    case DFNT_INT32:
    case DFNT_UINT32:
    case DFNT_FLOAT32:
      retVal = 4;
      break;
      // double	
    case DFNT_FLOAT64:
      retVal = 8;
      break;
    }
    return retVal;
  }
  	   

  /**
   * Handles the event. Returns true if the event is handled and
   * should not be passed to the parent of this component. The default
   * event handler calls some helper methods to make life easier
   * on the programmer.
   * @param evt the event
   * @see java.awt.Component#handleEvent
   */
  public boolean handleEvent(Event evt) {
    // check whether the event is the scrollbar
    switch (evt.id) {
    case Event.PGUP :
    case Event.SCROLL_PAGE_UP :
    case Event.UP :
    case Event.SCROLL_LINE_UP :
    case Event.SCROLL_ABSOLUTE :
    case Event.DOWN :
    case Event.SCROLL_LINE_DOWN :
    case Event.PGDN :
    case Event.SCROLL_PAGE_DOWN :
      // detect the vertical scrollbar
      if (evt.target ==  vScrollbar) {
	// set draw flag
	dataCanvas.drawFlag = false;
	// get offset
	dataCanvas.ty = vScrollbar.getValue();
	// set scrollbar offset to display the correct data
	dataCanvas.setVoffset();		
	// repaint the graphics
	dataCanvas.repaint();
	return  true;
      }   
      // detect the horizontal scrollbar
      if (evt.target ==  hScrollbar) {    
	// set draw flag
	dataCanvas.drawFlag = false;
	// get offset
	dataCanvas.tx = hScrollbar.getValue();
	// set horizontal scrollbar offset
	dataCanvas.setHoffset();
	// repaint the graphics
	dataCanvas.repaint();
	return true;
      }
    } // switch(evt.id)
    return(super.handleEvent(evt));
  }

  /** popup the new component actually */
  public void popup() {
    //pack();
    // default frame size
    resize(700, 500);
    // paint dataspread sheet
    dataCanvas.repaint();
    // show component of the frame
    show();
    // compute the cell position
    dataCanvas.computeCellPosition();
  }
  
 /** set spreadsheet first line number
   * @param rownum   a first row number
   */
  public void setFirstLineNumber(int rownum){
    firstLineNumber = rownum;
  }

  /** set row number
   * @param rownum   a row number of spreadsheet
   */
  public void setRowNumber(int rownum){
    this.numberOfRow = rownum;
  }

  /** set title for this spreadsheet 
   * @param title  a spreadsheet title 
   */
  public void setSpreadsheetTitle(String title) {
    this.title = title;
  }

 /** Update spreadsheet and repaint it */
  public void updateSpreadsheet() {
    dataCanvas.updateSpreadsheet();
  }

}
