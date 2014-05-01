/**
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996, 1997 Board of Trustees of the University of Illinois
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
 *  97oct21  rlp  eliminate support of arbitrary first axis indices
 *  97dec04  rlp  fixed clone() method
 *  97dec09  rlp  added toString()
 */

package ncsa.horizon.util;

/**
 * a Volume in which all but one of the sides have unit lengths.
 *
 * See also <a href="ncsa.horizon.util.Voxel.html">Voxel</a>,
 *          <a href="ncsa.horizon.util.Volume.html">Volume</a>, and 
 *          <a href="ncsa.horizon.util.Slice.html">Slice</a>.
 * 
 * @version Alpha 0.1
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */
public class Segment extends Volume {

    protected int longaxis;

    /**
     * create a Segment object 
     * @param nax     number of axes in the space in which it exists
     * @param longaxis   axis to be considered the long axis  (zero-relative).
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 or if
     *                longaxis is outside of range [0, nax)
     */
    public Segment(int nax, int longaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	super(nax);
	setLongAxis(longaxis);
    }

    /**
     * create a Segment object of unit size and nax dimensions; the first 
     * axis is assumed to be the long axis.
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 
     */
    public Segment(int nax) {
	this(nax, 0);
    }

    /**
     * create a Segment object located at a given Voxel and of given 
     * dimensions.  
     * @param vox   location of volume vertex closest to origin of space
     * @param len   length of Segment along its long axis
     * @exception ArrayIndexOutOfBoundsException thrown if 
     *              longaxis is outside of range [0, nax)
     */
    public Segment(Voxel vox, double len, int longaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	this(vox.getNaxes(), longaxis);
	setLength(len);
    }
	
    /**
     * create a Segment object from a Volume
     * @param vol   volume to segment
     * @param longaxis axis index of x-axis (zero-relative)
     * @exception ArrayIndexOutOfBoundsException thrown if 
     *              xaxis or yaxis is outside of range [0, nax)
     */
    public Segment(Volume vol, int longaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	this(vol.getNaxes(), longaxis);
	loc[this.longaxis]    = vol.loc[this.longaxis];
	size[this.longaxis]   = vol.size[this.longaxis];
	sample[this.longaxis] = vol.sample[this.longaxis];
    }

    /**
     * set the long axis to be the given integer, indicating that 
     * this axis can be have a length larger than one.
     * @param i index of axis using the numbering convention defined when
     *          this object was created.
     * @exception ArrayIndexOutOfBoundsException if i < 0 or >= the 
     *          number of axes
     */
    public void setLongAxis(int i) {
	if (i < 0 || i >= naxes) throw new 
           ArrayIndexOutOfBoundsException(
	       i + " is out of range: [0, " + (naxes - 1) + "]");

	if (longaxis != i) {
	    size[longaxis] = 1;
	    longaxis = i;
	}
    }

    /**
     * return the currently set long axis 
     */
    public int getLongAxis() { return longaxis; }

    /**
     * set the length of each side of the volume; the input lengths are 
     * ignored except for the one currently tagged as the long axis.
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
	    use[i] = (j == longaxis) ? newsz[i] : 1.0;
	}

	super.setSize(use, firstaxis);
    }

    /**
     * set the length of a side of the volume; the request is ignored unless 
     * the requested side is the currently set long axis.
     * @param i  the index of the axis to be set
     * @param sz the length to set the side to
     */
    public void setLength(int i, double sz) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (i == longaxis) size[i] = sz;
    }

    /**
     * set the length of the long axis
     */
    public void setLength(double l) { size[longaxis] = l; }

    /**
     * set the sampling along the long axis
     */
    public void setLongAxisSampling(double l) { sample[longaxis] = l; }

    /**
     * return true if no more than one side have unit length
     */
    public static boolean isSegment(Volume vol) {
	int nax = vol.getNaxes();
	double[] sz = vol.getSize(0);

	int nonunit = 0;
	for(int i=0; i < nax && nonunit <= 1; i++) 
	    if (sz[i] != 1) nonunit++;
	return (nonunit <= 1);
    }

    /**
     * expand this slice to include the projection of a Voxel onto the
     * slice plane
     */
    public void add(Voxel vox) {
	Voxel proj = new Voxel(naxes, loc);
	if (vox.getNaxes() >= longaxis) 
	    proj.setAxisPos(longaxis, vox.axisPos(longaxis));
	super.add(proj);
    }

    /**
     * expand this slice to include the projection of a Volume onto the
     * slice plane
     */
    public void add(Volume vol) {
	Voxel proj = new Voxel(naxes, loc);
	if (vol.naxes >= longaxis) 
	    proj.setAxisPos(longaxis, vol.loc[longaxis]);
	super.add(proj);
	if (vol.naxes >= longaxis) 
	    proj.setAxisPos(longaxis, vol.loc[longaxis] + vol.size[longaxis]);
	super.add(proj);	
    }

    /**
     * increase the area of this slice by the projected distance of a Voxel 
     * projected onto this slice plane from its origin.
     */
    public void grow(Voxel vox) {
	Voxel proj = new Voxel(naxes, loc);
	if (vox.getNaxes() >= longaxis) 
	    proj.setAxisPos(longaxis, vox.axisPos(longaxis));
	super.grow(proj);
    }

    /**
     * increase the lengths of the x and y axes by the given amounts
     */
    public void grow(double sz) {
	size[longaxis] += sz;
    }

    /**
     * return a Segment that is a projection of a Volume onto the plane 
     * of this segment.  The sampling will be that of this Segment.
     */
    public Segment projection(Volume vol) {
	Segment out = (Segment) clone();
	if (vol.naxes >= longaxis) {
	    out.loc[longaxis] = vol.loc[longaxis];
	    out.size[longaxis] = vol.size[longaxis];
	}
	return out;
    }


    public Object clone() {
	return super.clone();
    }

    public String toString() {
	int i;
	StringBuffer buf = new StringBuffer("Slice(");
	buf.append("long axis=" + longaxis + ", loc=[");
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(loc[i]);
	}
	buf.append("], length=" + size[longaxis] + "], samp=[");
	
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(sample[i]);
	}
	buf.append("])");
	return buf.toString();
    }

}
