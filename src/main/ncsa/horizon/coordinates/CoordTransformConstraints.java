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
 *  96mar26  rlp  Original version;
 */
package ncsa.horizon.coordinates;

import java.util.Vector;
import java.util.Enumeration;

/**
 * a description about how a CoordTransform object should be applied for
 * transforming positions from one coordinate system to another. <p>
 *
 * An important piece of information stored in this object is an Axis 
 * Index List which specifies which axes of a coordinate system the
 * transform should be applied to.  In general, the transform usually
 * assumes that each axis refered to in the list represents a different
 * but particular quantity in the domain space.  For instance, it may 
 * assume that the first axis in the list represents longitude.  The list
 * provides a way of telling the transform which axis is the longitude 
 * axis.  The <a href="#getAxisIndexList()">getAxisIndexList()</a> returns
 * this list as an array of integers where each element is an axis index.
 * If the transform wants the first axis to be longitude and the second
 * axis to be latitude, then this method might return an integer 
 * array = { 0, 4 }, indicating that axis number 0 is the longitude axis
 * and axis number 4 is the latitude axis.
 */
public class CoordTransformConstraints {

    protected Vector indices;

    /**
     * if false, then the reverse CoordTransform should be applied.  
     * That is, when an object might normally call the transform's 
     * forward() method, it should instead call the reverse() method
     * (and vice versa).
     */
    public boolean forward = true;

    /**
     * create a CoordTransformConstraints with an empty Axis Index List
     */
    public CoordTransformConstraints() { indices = new Vector(3); }

    /**
     * create a CoordTransformConstraints with an Axis Index List 
     * containing the numbers 0 through n-1 in order.
     */
    public CoordTransformConstraints(int n) {
	indices = new Vector(n);
	for(int i=0; i < n; i++) indices.addElement(new Integer(i));
    }

    /**
     * create a CoordTransformConstraints with an initial Axis Index List
     * specified by the given array.
     * @param axes       the axis index list
     * @param doForward  false, if the transform should be applied in reverse
     */
    public CoordTransformConstraints(int[] axes, boolean doForward) {
	forward = doForward;
	if (axes == null) {
	    indices = new Vector(3);
	} else {
	    indices = new Vector(axes.length);
	    for(int i=0; i < axes.length; i++) {
		if (axes[i] < 0) 
		    throw new ArrayIndexOutOfBoundsException(axes[i]);
		indices.addElement(new Integer(axes[i]));
	    }
	}
    }

    /**
     * create a CoordTransformConstraints with an initial Axis Index List
     * specified by the given array.
     * @param axes       the axis index list
     */
    public CoordTransformConstraints(int[] axes) {
	this(axes, false);
    }

    /**
     * return an array of integer indicating the indices of the axes
     * that the transform should be applied to.  This returns an array
     * reflecting the current state of the list but with blank values 
     * removed.  
     */
    public int[] getAxisIndexList() {
	int i, j;
	int sz = indices.size();
	int[] out = new int[sz];
	Enumeration e = indices.elements();
	for(i=j=0; i < sz && e.hasMoreElements(); i++) {
	    Integer idx = null;
	    while (idx == null && e.hasMoreElements()) {
		idx = (Integer) e.nextElement();
		j++;
	    }
	    out[i] = (idx == null) ? 0 : idx.intValue();
	}

	if (i < j) {
	    int[] tmp = out;
	    out = new int[i];
	    System.arraycopy(tmp, 0, out, 0, i);
	}

	return out;
    }

    /**
     * remove the all the axis indices in the Axis Index List.
     */
    public void removeAllAxes() {
	indices.removeAllElements();
    }

    /**
     * add an axis index to the end of the list
     */
    public void addAxis(int ax) {
	indices.addElement(new Integer(ax));
    }

    /**
     * set the axis index at the specified position of the axis index list
     */
    public void setAxisAt(int ax, int i) 
	throws ArrayIndexOutOfBoundsException
    {
	if (i < 0) throw new ArrayIndexOutOfBoundsException(i);
	if (i >= indices.size()) indices.setSize(i+1);
	indices.setElementAt(new Integer(ax), i);
    }

    /**
     * return the i-th index of the index list
     * @return Integer the index, or null if the index is not set.
     */
    public Integer getIndexAt(int i) 
	throws ArrayIndexOutOfBoundsException
    {
	if (i < 0) throw new ArrayIndexOutOfBoundsException(i);
	if (i >= indices.size()) return null;
	return (Integer) indices.elementAt(i);
    }

    /**
     * remove the i-th axis index from the list; all axes at positions
     * higher that i are shifted down.
     */
    public void removeIndexAt(int i) {
	indices.removeElementAt(i);
    }
	
    /**
     * set the size of the list.  This does not guarantee the size of the
     * array returned by getAxisIndexList().  This method is mainly provided
     * as a way of reducing the size of the list, removing axes from the 
     * end.
     */
    public void setAxisListSize(int size) {
	indices.setSize(size);
    }
}
