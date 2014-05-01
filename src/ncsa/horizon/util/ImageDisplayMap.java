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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  97jul22  rlp  Original version; meant as replacement to ImageDataOrigin
 *  97aug19  rlp  fixed bug in update.
 *  97nov20  rlp  fixed bugs in getDataSlice() and getDisplayRegion() which
 *                  caused errors when yaxisReversed = true.
 */

package ncsa.horizon.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;

/**
 * an object that provides a way to express the convention used for how the
 * display pixels in an image map to corresponding data pixels. <p>
 *
 * This object can be used by a Viewer to keep track of how data pixels are
 * begin mapped to display pixels.  This can be non-trivial when users have
 * requested a subregion of the data to display.  Another tricky effect is 
 * data order.  For instance, computer graphics images (like GIF and JPEG)
 * order the data such that the first pixel should appear in the top left 
 * corner; however, some scientific data (such as FITS) orders the data 
 * using the mathematical convention where the first pixel should appear in
 * the lower left corner.  Both these effects can be kept track of with this 
 * object.  <p>
 *
 * @version $Id: ImageDisplayMap.java,v 1.3 1997/12/05 00:34:58 rplante Exp $
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */
public class ImageDisplayMap implements Cloneable {

    /**
     * the 2-d portion of data being displayed
     */
    protected Slice data = null;

    /**
     * the 2-d region that the data is displayed to
     */
    protected Rectangle display = null;

    /** 
     * true if data pixel positions increase to the left and xpos should 
     * be taken to be relative to the right side of the display region 
     * (i.e. the right side of the right-most display pixel in the xaxis).
     */
    public boolean xaxisReversed=false;
     
    /** 
     * true if data pixel positions increase upwards and ypos should be 
     * taken to be relative to the right side of the display region (i.e. 
     * the bottom side of the bottom-most display pixel in the yaxis).
     */
    public boolean yaxisReversed=false;

    private int xaxis, yaxis;
    private double xsize, ysize, xpos, ypos;

    private void update() {
	xaxis = data.getXaxis();
	yaxis = data.getYaxis();
	xsize = Math.floor(data.getLength(xaxis))/display.width;
	ysize = Math.floor(data.getLength(yaxis))/display.height;
	xpos = data.axisPos(xaxis);
	ypos = data.axisPos(yaxis);
    }

    /**
     * create an ImageDisplayMap object assuming that the given slice will
     * be displayed in a given Rectangle
     */
    public ImageDisplayMap(Slice dataSlice, Rectangle displayRegion) {
	data = (Slice) dataSlice.clone();
	display = new Rectangle(displayRegion.x, displayRegion.y,
				displayRegion.width, displayRegion.height);
	update();
    }

    /**
     * create an ImageDisplayMap object assuming that the given slice will
     * be displayed in a given Rectangle
     */
    public ImageDisplayMap(Slice dataSlice, Rectangle displayRegion,
			   boolean xIsReversed, boolean yIsReversed) {
	this(dataSlice, displayRegion);
	xaxisReversed = xIsReversed;
	yaxisReversed = yIsReversed;
    }

    /**
     * create an ImageDisplayMap object assuming that the given slice will
     * be displayed in a 1 x 1 pixel region.
     */
    public ImageDisplayMap(Slice dataSlice) {
	data = (Slice) dataSlice.clone();
	display = new Rectangle(0, 0, 1, 1);
	update();
    }

    /**
     * create an ImageDisplayMap object assuming that a slice made up of the 
     * will first data voxel in a volume of data will be displayed
     * in a 1 x 1 pixel region.  
     */
    public ImageDisplayMap() {
	data = new Slice(2);
	display = new Rectangle(0, 0, 1, 1);
	update();
    }

    /**
     * return a data point location associated with a given display pixel
     * given display pixel.
     * @param displayPixel  a position in the display region
     * @returns Voxel       the position in the data set corresponding to 
     *                      the input display pixel
     */
    public synchronized Voxel getDataVoxel(Point displayPixel) {
	double xout = displayPixel.x - display.x, 
	       yout = displayPixel.y - display.y;
	Voxel out = new Voxel( data.getNaxes(), data.getLocation() );

	if (xaxisReversed) xout = display.width - xout;
	if (yaxisReversed) yout = display.height - yout;

	xout = xout*xsize + xpos;
	yout = yout*ysize + ypos;
	out.setAxisPos(xaxis, xout);
	out.setAxisPos(yaxis, yout);

	return out;
    }

    /**
     * return the position of a Voxel along the axis currently being 
     * displayed in horizontal direction.
     * @exception ArrayIndexOutOfBoundsException if current x-axis is 
     *                        beyond the domain of the Voxel
     */
    public double getXDataPos(Voxel dataVoxel) {
	return dataVoxel.axisPos(data.getXaxis());
    }

    /**
     * return the position of a Voxel along the axis currently being 
     * displayed in vertical direction.
     * @exception ArrayIndexOutOfBoundsException if current y-axis is 
     *                        beyond the domain of the Voxel
     */
    public double getYDataPos(Voxel dataVoxel) {
	return dataVoxel.axisPos(data.getYaxis());
    }

    /**
     * return a Point that represents the display pixel that coves a 
     * given data location.
     */
    public Point getDisplayPixel(Voxel dataVoxel) {
	double dx, dy;
	Point out = new Point(0,0);

	try { dx = dataVoxel.axisPos(xaxis); }
	catch (ArrayIndexOutOfBoundsException ex) { dx = 0.0; } 
	try { dy = dataVoxel.axisPos(yaxis); }
	catch (ArrayIndexOutOfBoundsException ex) { dy = 0.0; } 

	out.x = (int) Math.round((dx - xpos)/xsize);
	out.y = (int) Math.round((dy - ypos)/ysize);

	if (xaxisReversed) out.x = display.width - out.x;
	if (yaxisReversed) out.y = display.height - out.y;

	return out;
    }

    /**
     * return the Slice enclosed by a rectanglar region of the display
     */
    public synchronized Slice getDataSlice(Rectangle displayRegion) {
	Voxel vert = getDataVoxel(new Point(displayRegion.x, displayRegion.y));
	Dimension dim = new 
	    Dimension( (int) Math.round(displayRegion.width*xsize),
		       (int) Math.round(displayRegion.height*ysize));
	if (xaxisReversed) dim.width  *= -1;
	if (yaxisReversed) dim.height *= -1;
	Slice out = new Slice(vert, dim, xaxis, yaxis);
	if (xaxisReversed || yaxisReversed) out.makeLengthsPositive();
	return out;
    }


    /**
     * return the Rectangle that encloses the intersection of a given 
     * slice with the data currently being displayed.
     */
    public Rectangle getDisplayRegion(Volume dataVol) {
	Volume intersec = data.intersection(dataVol);
	Voxel loc = intersec.getVoxel();
	Point vert = getDisplayPixel(loc);
	Dimension dim = new 
	    Dimension( (int) Math.round(intersec.getLength(xaxis)/xsize),
		       (int) Math.round(intersec.getLength(yaxis)/ysize));
	if (xaxisReversed) vert.x -= dim.width;
	if (yaxisReversed) vert.y -= dim.height;
	return new Rectangle(vert, dim);
    }

    /**
     * change the Slice being mapped to the current display region
     */
    public synchronized void setSlice(Slice newSlice) {
	data = (Slice) newSlice.clone();
	update();
    }

    /**
     * return a copy of the Slice being mapped to the display region
     */
    public Slice getSlice() {
	return ((Slice) data.clone());
    }

    /**
     * change the Slice being mapped to the current display region
     */
    public synchronized void setDisplay(Rectangle newDisplay) {
	display = new Rectangle(newDisplay.x, newDisplay.y,
				newDisplay.width, newDisplay.height);
	update();
    }

    /**
     * return a copy of the Rectangle being used as the current the 
     * display region
     */
    public Rectangle getDisplay() {
	return new Rectangle(display.x, display.y,
			     display.width, display.height);
    }

    public Object clone() {
	ImageDisplayMap out;
	try {
	    out = (ImageDisplayMap) super.clone();
	} catch (CloneNotSupportedException ex) {
	    // should not happen
	    throw new InternalError("ImageDisplayMap: " + ex.getMessage());
	}
	out.display = getDisplay();
	out.data = getSlice();
	return out;
    }

}


