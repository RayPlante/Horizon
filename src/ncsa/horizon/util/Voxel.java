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
 *  97may15  rlp  fixed clone() method
 *  97oct21  rlp  eliminate support of arbitrary first axis indices
 *  98jan16  rlp  added untranslate methods
 */
package ncsa.horizon.util;

import java.lang.*;

/** 
 * a generalized description of a point in n-space whose dimensions n 
 * are specified in the constructor call.  <p>
 *
 * The axes of the space are referred to through an index which 
 * begins with zero.
 *
 * See also <a href="ncsa.horizon.util.Volume.html">Volume</a>,
 *          <a href="ncsa.horizon.util.Slice.html">Slice</a>, and 
 *          <a href="ncsa.horizon.util.Segment.html">Segment</a>. <p>
 *
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 * @version 1.0
 */
public class Voxel implements Cloneable {

    protected double[] pos;

    /**
     * create a Voxel in a space of nax dimensions and initialize it 
     * to the origin (i.e. position = [0, 0, 0, ...]).
     * @param nax number of axes or dimensions in the space 
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 
     */
    public Voxel(int nax) throws ArrayIndexOutOfBoundsException {
	super();
	if (nax <= 0) throw new ArrayIndexOutOfBoundsException(nax);
	pos = new double[nax];
    }

    /** 
     * create a Voxel in a space of nax dimensions and initialize it 
     * with a position.  The index of the first axis will be zero.
     * @param nax number of axes or dimensions in the space
     * @param in an array containing the position.  The first nax 
     *	         elements will be used.  If the array size is smaller 
     *		 than nax, the remaining components will be 0.	
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 
     */
    public Voxel(int nax, double[] in) throws ArrayIndexOutOfBoundsException {
	this(nax);
	setValues(in, 0);
    }

    /** return the number of dimensions in the space containing this 
        Voxel **/
    public int getNaxes() { return pos.length; }

    /** get the projection of the Voxel's position along the i-th axis.  
     *  (Axes are numbered such that the first axis is zero.)
     */
    public double axisPos(int i) throws ArrayIndexOutOfBoundsException { 
	return pos[i]; 
    }

    /** set the projection of the Voxel's position along the i-th axis   
        Axes are numbered beginning with 0. **/
    public void setAxisPos(int i, double value) 
	throws ArrayIndexOutOfBoundsException 
    { 
	pos[i] = value;
    }

    /**
     * return an array of doubles representing the position along each
     * axis
     * @param firstaxis index at which first value should appear
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public double[] getValues(int firstaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	int i;
	double out[] = new double[pos.length + firstaxis];

	for(i=0; i < firstaxis; i++) out[i] = 0;
	for(i=0; i < pos.length; i++) out[firstaxis + i] = pos[i];
	return out;
    }

    /**
     * return an array of doubles representing the position along each
     * axis.  
     */
    public double[] getValues() { 
	return getValues(0);
    }

    /**
     * set the position along all axes with the values given in the input
     * array.
     * @param newpos array of doubles containing position values
     * @param firstaxis index at which first value appears in newpos
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setValues(double[] newpos, int firstaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	int i;
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	for(i=firstaxis; i < newpos.length && i - firstaxis < pos.length; i++)
	    pos[i - firstaxis] = newpos[i];
    }

    /**
     * set the position along all axes with the values given in the input
     * array.  The first value will be assumed to be at the first index axis.
     * @param newpos array of doubles containing position values
     */
    public void setValues(double[] newpos) {
	setValues(newpos, 0);
    }


    /** converts position to (x, y, ...) formatted string **/
    public String toString() {
	int i;
	StringBuffer out = new StringBuffer().append("(");
	for(i=0; i < pos.length; i++) {
	    if (i>0) out.append(", ");
	    out.append(String.valueOf(pos[i]));
	}
	out.append(")");

	return new String(out);
    }

    /**
     * create a deep copy of this Voxel
     */
    public Object clone() {
	try {
	    Voxel out = (Voxel) super.clone();
	    out.pos = (double[]) pos.clone();
	    out.setValues(pos, 0);
	    return out;
	} 
	catch (CloneNotSupportedException ex) {
	    // Should not happen
	    throw new InternalError(ex.getMessage());
	}
    }

    /**
     * move this Voxel to a relative position
     * @param vox the relative position to add to this voxel's position; only
     *            dimensions that overlap this space will be added.
     */
    public void translate(Voxel vox) {
	for(int i=0; i < pos.length && i < vox.pos.length; i++)
	    pos[i] += vox.pos[i];
    }

    /**
     * move this Voxel to a relative position
     * @param newpos the relative position to add to this voxel's position; 
     *               only dimensions that overlap this space will be added.
     * @param firstaxis index at which first value appears in newpos
     */
    public void translate(double[] newpos, int firstaxis) 
	throws ArrayIndexOutOfBoundsException
    {
	for(int i=0; i < pos.length && i + firstaxis < newpos.length; i++)
	    pos[i] += newpos[i + firstaxis];
    }

    /**
     * transform this Voxel to a position that is relative to the given 
     * Voxel.  Just as translate(Voxel) adds another Voxel to its own 
     * location, this method subtracts another Voxel from its own location.
     * @param vox the position to subtract from this voxel's position; only
     *            dimensions that overlap this space will be subtracted.
     */
    public void untranslate(Voxel vox) {
	for(int i=0; i < pos.length && i < vox.pos.length; i++)
	    pos[i] -= vox.pos[i];
    }

    /**
     * transform this Voxel to a position that is relative to a given 
     * location.  Just as translate(Voxel) adds another Voxel to its own 
     * location, this method subtracts another position from its own location.
     * @param thatpos the position to subtract to this voxel's position; 
     *                only dimensions that overlap this space will be added.
     * @param firstaxis index at which first value appears in newpos
     */
    public void untranslate(double[] thatpos, int firstaxis) {
	for(int i=0; i < pos.length && i + firstaxis < thatpos.length; i++)
	    pos[i] -= thatpos[i + firstaxis];
    }


    /**
     * return true if another Voxel is (in principle) at the same location 
     * as this Voxel.  For equality to be true, locations along extra axes
     * (for either Voxel) must be zero.
     */
    public boolean equals(Voxel vox) {
	int i;
	for(i=0; i < pos.length && i < vox.pos.length; i++) 
	    if (pos[i] != vox.pos[i]) return false;

	if (pos.length != vox.pos.length) {
	    Voxel higher = (pos.length > vox.pos.length) ? this : vox;
	    for(; i < higher.pos.length; i++) 
		if (higher.pos[i] != 0) return false;
	}

	return true;
    }

    /** 
     * run this class as an application to test it 
     * @param args components of a vector 
     */
    public static void main(String[] args) {
	int i;
	double[] data = { 45.7, 23.0, 18.0 };

	Voxel my = new Voxel(4, data);
	System.out.println("My voxel lives in " + my.getNaxes() + 
			   "-space: " + my);
	if (args.length == 0) return;
	
	Voxel your = new Voxel(args.length);
	for(i=0; i < args.length; i++) {
	    your.setAxisPos(i, new Double(args[i]).doubleValue());
	}
	System.out.println("Your voxel lives in " + your.getNaxes() + 
			   "-space: " + your);
	System.out.println("The position along the 1st axis is " + 
			    your.axisPos(1));

	Voxel mineinyours = new Voxel(your.getNaxes(), data);
	System.out.println("My voxel intersects your space at: " +
			    mineinyours);
    }

};


	
