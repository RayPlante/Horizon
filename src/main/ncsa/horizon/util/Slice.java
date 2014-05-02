/**
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
 *  96       rlp  Original version
 *  96dec12  rlp  fixed bugs in constructors taking a Volume or Slice 
 *                  as input: location was not being properly set
 *  97feb02  rlp  bug fix: projection(), grow(), add() allow for input 
 *                  Voxel/Volume to have fewer axes than "this" one has.
 *  97may15  rlp  Fixed clone() method
 *  97jun06  rlp  Fixed bug in Slice(Voxel, Dimension, int, int)
 *  97oct21  rlp  eliminate support of arbitrary first axis indices
 *  97nov26  rlp  Fixed another bug in projection(): now sets location 
 *                  of output slice to that of input slice before adjusting
 *                  x and y locations.
 *  97dec09  rlp  added toString()
 */

package ncsa.horizon.util;

import java.awt.Dimension;

/**
 * a Volume in which all but two of the sides have unit lengths.
 *
 * See also <a href="ncsa.horizon.util.Voxel.html">Voxel</a>,
 *          <a href="ncsa.horizon.util.Volume.html">Volume</a>, and 
 *          <a href="ncsa.horizon.util.Segment.html">Segment</a>.
 * 
 * @version Alpha 0.1
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */
public class Slice extends Volume {

    protected int xaxis, yaxis;

    /**
     * create a Slice object in which all the lengths of all sides have
     * a length of one.
     * @param nax     number of axes in the space in which it exists
     * @param xaxis   axis to be considered the x axis (zero-relative).
     * @param yaxis   axis to be considered the y axis (zero-relative).
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 or if
     *                xaxis or yaxis is outside of range 
     *                [0, nax)
     */
    public Slice(int nax, int xaxis, int yaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	super(nax);
	setXaxis(xaxis);
	setYaxis(yaxis);
    }

    /**
     * create a Slice object of unit size and nax dimensions; the first 
     * two axes are assumed to be the ones defining the slice plane.
     * The index of the first axis will be zero.
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 
     */
    public Slice(int nax) {
	this(nax, 0, 0);
	if (nax > 1) yaxis = 1;
    }

    /**
     * create a Slice object located at a given Voxel and of given dimensions
     * @param vox   location of slice vertex closest to origin of space
     * @param dim   dimension of slice in x and y directions
     * @param xaxis axis index of x-axis (zero-relative).
     * @param yaxis axis index of y-axis (zero-relative).
     * @exception ArrayIndexOutOfBoundsException thrown if 
     *              xaxis or yaxis is outside of range [0, nax)
     */
    public Slice(Voxel vox, Dimension dim, int xaxis, int yaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	this(vox.getNaxes(), xaxis, yaxis);
	setLocation(vox.getValues(0), 0);
	setXaxisLength(dim.width);
	setYaxisLength(dim.height);
    }
	
    /**
     * create a Slice object from a Volume
     * @param vol   volume to slice
     * @param xaxis axis index of x-axis (zero-relative).
     * @param yaxis axis index of y-axis (zero-relative).
     * @exception ArrayIndexOutOfBoundsException thrown if 
     *              xaxis or yaxis is outside of range [0, nax)
     */
    public Slice(Volume vol, int xaxis, int yaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	this(vol.getNaxes(), xaxis, yaxis);
	setLocation(vol.getLocation(0), 0);
	size[this.xaxis]   = vol.size[this.xaxis];
	size[this.yaxis]   = vol.size[this.yaxis];
	sample[this.xaxis] = vol.sample[this.xaxis];
	sample[this.yaxis] = vol.sample[this.yaxis];
    }

    /**
     * create a Slice object from a Volume.  The x and y axes will be set to 
     * the first two non-unit length axes in the volume.  If only one axis is 
     * non-unit in length, the y axis will be the one after it; if no non-unit
     * axes are found, the first two axes will be made the x and y axes.
     * @param vol             the input volume
     */
    public Slice(Volume vol) {
	this(vol.getNaxes(), 0, 0);
	setLocation(vol.getLocation(0), 0);
	double[] sz = vol.getSize(0);
	xaxis = yaxis = -1;

	for(int i=0; i < naxes; i++) {
	    size[i] = sz[i];
	    if (sz[i] != 1) {
		if (xaxis < 0) {
		    xaxis = i;
		} else if (yaxis < 0) {
		    yaxis = i;
		} else {
		    size[i] = 1;
		}
	    }
	}
	if (xaxis < 0) xaxis = 0;
	if (yaxis < 0) yaxis = (xaxis >= naxes - 1) ? 0 : xaxis + 1;

	setSampling(vol.getSampling(0), 0);
    }

    /**
     * return true if no more than two sides of a Volume have greater than 
     * unit length
     */
    public static boolean isSlice(Volume vol) {
	int nax = vol.getNaxes();
	double[] sz = vol.getSize(0);

	int nonunit = 0;
	for(int i=0; i < nax && nonunit <= 2; i++) 
	    if (sz[i] != 1) nonunit++;
	return (nonunit <= 2);
    }

    /**
     * set the x axis to be the given integer, indicating that 
     * this axis can be have a length larger than one.
     * @param i index of axis (zero-relative)
     * @exception ArrayIndexOutOfBoundsException if i < 0 or >= the 
     *          number of axes
     */
    public void setXaxis(int i) {
	if (i >= naxes || i < 0) throw new 
	   ArrayIndexOutOfBoundsException(
               i + " is out of range: [0, " + (naxes - 1) + "]");

	if (xaxis  != i) {
	    size[xaxis] = 1;
	    xaxis = i;
	}
    }

    /**
     * set the ordinate (y-) axis to be the given integer, indicating that 
     * this axis can be have a length larger than one.
     * @param i index of axis (zero-relative)
     * @exception ArrayIndexOutOfBoundsException if i < 0 or >= the 
     *          number of axes
     */
    public void setYaxis(int i) {
	if (i > naxes || i < 0) throw new 
           ArrayIndexOutOfBoundsException(
	       i + " is out of range: [0, " + (naxes - 1) + "]");

	if (xaxis  != i) {
	    size[yaxis] = 1;
	    yaxis = i;
	}
    }

    /**
     * return the currently set x axis.
     */
    public int getXaxis() { return xaxis; }

    /**
     * return the currently set ordinate (y) axis.
     */
    public int getYaxis() { return yaxis; }

    /**
     * set the length of each side of the volume; the input lengths are 
     * ignored except for those currently tagged as the x and y axes.
     * @param newsz array of doubles containing position values
     * @param firstaxis index at which first value appears in newpos
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setSize(double[] newsz, int firstaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	int i, j;
	double[] use = new double[newsz.length];
	for(i=0; i < use.length; i++) {
	    j = i - firstaxis;
	    use[i] = (j == xaxis || j == yaxis) ? newsz[i] : 1.0;
	}

	super.setSize(use, firstaxis);
    }

    /**
     * set the length of a side of the volume; the request is ignored unless 
     * the requested side is either the currently set x or y axis.
     * @param i  the index of the axis to be set
     * @param sz the length to set the side to
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setLength(int i, double sz) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (i == xaxis || i == yaxis) size[i] = sz;
    }

    /**
     * set the position of the Slice along the x axis
     */
    public void setXaxisLocation(double l) { loc[xaxis] = l; }

    /**
     * set the position of the Slice along the y axis
     */
    public void setYaxisLocation(double l) { loc[yaxis] = l; }

    /**
     * set the length of the x axis
     */
    public void setXaxisLength(double l) { size[xaxis] = l; }

    /**
     * set the length of the ordinate (y) axis
     */
    public void setYaxisLength(double l) { size[yaxis] = l; }

    /**
     * set the length of the currently selected x and y axes
     */
    public void setArea(double xlen, double ylen) {
	size[xaxis] = xlen;
	size[yaxis] = ylen;
    }

    /**
     * set the sampling along the x axis
     */
    public void setXaxisSampling(double l) { sample[xaxis] = l; }

    /**
     * set the sampling along the ordinate (y) axis
     */
    public void setYaxisSampling(double l) { sample[yaxis] = l; }

    /**
     * expand this slice to include the projection of a Voxel onto the
     * slice plane
     */
    public void add(Voxel vox) {

	Voxel proj = new Voxel(naxes, loc);
	if (vox.getNaxes() >= xaxis) 
	    proj.setAxisPos(xaxis, vox.axisPos(xaxis));
	if (vox.getNaxes() >= yaxis) 
	    proj.setAxisPos(yaxis, vox.axisPos(yaxis));
	super.add(proj);
    }

    /**
     * expand this slice to include the projection of a Volume onto the
     * slice plane
     */
    public void add(Volume vol) {
	Voxel proj = new Voxel(naxes, loc);
	if (vol.naxes >= xaxis) proj.setAxisPos(xaxis, vol.loc[xaxis]);
	if (vol.naxes >= yaxis) proj.setAxisPos(yaxis, vol.loc[yaxis]);
	super.add(proj);
	if (vol.naxes >= xaxis) 
	    proj.setAxisPos(xaxis, vol.loc[xaxis] + vol.size[xaxis]);
	if (vol.naxes >= yaxis) 
	    proj.setAxisPos(yaxis, vol.loc[yaxis] + vol.size[yaxis]);
	super.add(proj);	
    }

    /**
     * increase the area of this slice by the projected distance of a Voxel 
     * projected onto this slice plane from its origin.
     */
    public void grow(Voxel vox) {
	Voxel proj = new Voxel(naxes, loc);
	if (vox.getNaxes() >= xaxis) 
	    proj.setAxisPos(xaxis, vox.axisPos(xaxis));
	if (vox.getNaxes() >= yaxis) 
	    proj.setAxisPos(yaxis, vox.axisPos(yaxis));
	super.grow(proj);
    }

    /**
     * increase the lengths of the x and y axes by the given amounts
     */
    public void grow(double xsize, double ysize) {
	size[xaxis] += xsize;
	size[yaxis] += ysize;
    }

    /**
     * return a Slice that is a projection of a Volume onto the plane 
     * of this slice.  The sampling and indexing convention will be 
     * that of the input Volume.
     */
    public Slice projection(Volume vol) {
	Slice out = new Slice(naxes, xaxis, yaxis);
	out.setSize(vol.getSize(0), 0);
	out.setLocation(getLocation());
	if (vol.naxes >= xaxis) {
	    out.loc[xaxis] = vol.loc[xaxis];
	    out.size[xaxis] = vol.size[xaxis];
	}
	if (vol.naxes >= yaxis) {
	    out.loc[yaxis] = vol.loc[yaxis];
	    out.size[yaxis] = vol.size[yaxis];
	}
	return out;
    }

    /**
     * return a Voxel that is a projection of a given Voxel onto the plane 
     * of this slice.  The indexing convention will be that of the input
     * Voxel
     */
    public Voxel projection(Voxel vox) {
	Voxel out = new Voxel(naxes);
	out.setValues(loc, 0);
	if (vox.getNaxes() >= xaxis) 
	    out.setAxisPos(xaxis, vox.axisPos(xaxis));
	if (vox.getNaxes() >= yaxis) 
	    out.setAxisPos(yaxis, vox.axisPos(yaxis));
	return out;
    }

    public Object clone() {
	Slice out = (Slice) super.clone();
	return out;
    }

    public String toString() {
	int i;
	StringBuffer buf = new StringBuffer("Slice(");
	buf.append("axes=[" + xaxis + "," + yaxis + "], loc=[");
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(loc[i]);
	}
	buf.append("], size=[" + size[xaxis] + "," + size[yaxis] + 
		   "], samp=[");
	
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(sample[i]);
	}
	buf.append("])");
	return buf.toString();
    }
}
