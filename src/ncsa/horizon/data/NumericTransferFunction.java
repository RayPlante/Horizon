/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1997, Board of Trustees of the University of Illinois
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
 *  97dec10  rlp  Original version
 *  97dec24  wx   Fixed for loop bug in several places: changed 
 *                "for(int i=0; i < 0; i++)" to "for(i=0; i < out.length; i++)"
 *  98jan14  rlp  updated for changes in TransferFunction, 
 *                ModedGUIControllable
 *  98jan16  wx   added support for byte, short as numeric types 
 *  98jan19  rlp  moved from ncsa.horizon.util to ncsa.horizon.data
 */

package ncsa.horizon.data;

import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.util.*;
import java.awt.Event;

/**
 * a class that applies a linear transfer function to an array of 
 * numeric values.  This TransferFunction can take individual values 
 * and arrays of Number objects or any of the corresponding primitive 
 * types (integer, long, float, and double).  <p>
 *
 * See also: <br>
 * @see #TransformFunction
 *
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public class NumericTransferFunction extends TransferFunction {

    /**
     * The minimum value in the range.  Values equal to this minimum will 
     * be mapped to an integer bin equal to 2; values less than this 
     * minimum will be mapped to 1.
     */
    protected double min;

    /**
     * The maximum value in the range.  Values equal to this minimum will 
     * be mapped to an integer bin equal to dynarange-2; values greater than 
     * this minimum will be mapped to dyanrange-1.  
     */
    protected double max;

    /**
     * create a NumericTransferFunction with a dynamic range of 256
     * and a range of 0 to 252
     */
    public NumericTransferFunction() { this(256); }

    /**
     * create a NumericTransferFunction with a given dynamic range 
     * and a range of 0 to the dynamic range minus 4.
     * @param  nlevels   the dynamic range; that is, the number of
     *                   levels the input values will be mapped into
     */
    public NumericTransferFunction(int nlevels) {
	if (nlevels < 4) 
	    throw new IllegalArgumentException("need at least 4 transfer " +
					       "function levels");
	dynarange = nlevels;
	min = 0.0;  max = dynarange-4;
    }

    /**
     * create a NumericTransferFunction with a given dynamic range 
     * and range
     * @param  nlevels   the dynamic range; that is, the number of
     *                   levels the input values will be mapped into
     * @param  min       minimum of the range
     * @param  max       maximum of the range
     */
    public NumericTransferFunction(int nlevels, double min, double max) {
	dynarange = nlevels;
	if (min < max) {
	    this.min = min;  
	    this.max = max;
	}
	else {
	    this.max = min;  
	    this.min = max;
	}	    
    }

    /**
     * return the integer bin level for the given input value using 
     * a simple linear increasing function.  An input value less than the 
     * minimum value of our set range will be converted to zero; greater 
     * than or equal to that will be set to one or larger.  An input value 
     * larger than the maximum will be set to the value of the dynamic 
     * range; less than or equal to that will be set to a lesser value;
     * A null input or any value that is not an instance of Number will
     * return -1.
     * @param Object   the input value
     */
    public int getLevel(Object value) {
	if (value == null || ! (value instanceof Number)) 
	    return 0;
	else 
	    return getLevel( ((Number) value).doubleValue() );
    }

    /**
     * return the integer bin level for the given input value using 
     * a simple linear increasing function.  See getLevel(Object) 
     * for mapping rules.
     */
    public int getLevel(double value) {
	if (value == Double.NaN) 
	    return 0;
	else if (value < min) 
	    return 1;
	else if (value > max) 
	    return dynarange-1;
	else if (min == max) 
	    return (dynarange-4)/2 + 2;
	else 
	    return (int) 
	       Math.round(1.0*(dynarange-4) * (value - min) / (max - min)) + 2;
    }

    /**
     * return the integer bin level for the given input value using 
     * a simple linear increasing function.  See getLevel(Object) 
     * for mapping rules.
     */
    public int getLevel(int value) {
	return getLevel((double) value);
    }

    /**
     * return the integer bin level for the given input value using 
     * a simple linear increasing function.  See getLevel(Object) 
     * for mapping rules.
     */
    public int getLevel(long value) {
	return getLevel((double) value);
    }

    /**
     * return the integer bin level for the given input value using 
     * a simple linear increasing function.  See getLevel(Object) 
     * for mapping rules.
     */
    public int getLevel(float value) {
	return getLevel((double) value);
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     * @param values   an input of array of type Number[]; if input is any
     *                   other array type all output values will be equal to
     *                   -1.
     */
    public synchronized int[] getLevels(Object[] values) {
	int[] out = new int[values.length];
	int i;

	if (! (values instanceof Number[])) {
	    for(i=0; i < out.length; i++) out[i] = 0;
	    return out;
	}

	Number[] v = (Number[]) values;
	for(i=0; i < out.length; i++) 
	    out[i] = getLevel( v[i].doubleValue() );

	return out;
    }

    /**
     * apply this transfer function to an array of input values by 
     * converting it to an array of integers.  NOTE: THIS METHOD
     * WILL BE REMOVED IN THE NEXT RELEASE.
     * @param values   an array
     */
    public int[] getLevels(Object values, JavaType arrayType) 
	throws IllegalArgumentException
    {
	try {
	    if (arrayType == JavaType.DOUBLE) {
		return getLevels((double[]) values);
	    }
	    else if (arrayType == JavaType.FLOAT) {
		return getLevels((float[]) values);
	    }
	    else if (arrayType == JavaType.LONG) {
		return getLevels((long[]) values);
	    }
	    else if (arrayType == JavaType.SHORT) {
		return getLevels((short[]) values);
	    }
            else if (arrayType == JavaType.BYTE) {
		return getLevels((byte[]) values);
	    }
	    else {
		return getLevels((int[]) values);
	    }
	}
	catch (ClassCastException ex) {
	    throw new IllegalArgumentException("Bad or mismatched input type");
	}
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     */
    public synchronized int[] getLevels(double[] values) {
	int[] out = new int[values.length];
	for (int i=0; i < values.length; i++) {
	    out[i] = getLevel( values[i] );
	}
	return out;
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     */
    public synchronized int[] getLevels(float[] values) {
	int[] out = new int[values.length];
	for (int i=0; i < values.length; i++) {
	    out[i] = getLevel( (double) values[i] );
	}
	return out;
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     */
    public synchronized int[] getLevels(int[] values) {
	int[] out = new int[values.length];
	for (int i=0; i < values.length; i++) {
	    out[i] = getLevel( (double) values[i] );
	}
	return out;
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     */
    public synchronized int[] getLevels(long[] values) {
	int[] out = new int[values.length];
	for(int i=0; i < values.length; i++) {
	    out[i] = getLevel( (double) values[i] );
	}
	return out;
    }

    /**
     * return a value that is representative of the input bin level
     */
    public Object getValue(int level) { 
	return new Double(getDoubleValue(level));
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     */
    public synchronized int[] getLevels(byte[] values) {
        int[] out = new int[values.length];
        for (int i=0; i < values.length; i++) {
            out[i] = getLevel( (double) values[i] );
        }
        return out;
    }

    /**
     * apply a linear transfer function to an array of input values.  See
     * getLevel(Object) for mapping rules.
     */
    public synchronized int[] getLevels(short[] values) {
        int[] out = new int[values.length];
        for(int i=0; i < values.length; i++) {
            out[i] = getLevel( (double) values[i] );
        }
        return out;
    }

    /**
     * return a double value that is representative of the input bin level
     */
    public double getDoubleValue(int level) {
	if (level <= 0) 
	    return Double.NaN;
	else if (level == 1) 
	    return min;
	else if (level >= dynarange-1)
	    return max;
	else
	    return ( (level - 2) * (max - min) / (dynarange-4) + min );
    }

    /**
     * set the Minimum value.
     * @exception IllegalArgumentException if input value is not of 
     *     type Number
     */
    public void setMinimum(Object val) throws IllegalArgumentException {
	try {
	    min = ((Number) val).doubleValue();
	} catch (ClassCastException ex) {
	    throw new IllegalArgumentException("Not of type Number");
	}
	setChanged();
	notifyObservers(createEvent(MINIMUM_CHANGED, new Double(min)));
    }

    private Event createEvent(int id, Object what) {
	if (id < 2000 || id > 2003) id = 2000;
	return new Event(this, id, what);
    }

    public void setMinimum(double val) { 
	min = val; 
	setChanged();
	notifyObservers(createEvent(MINIMUM_CHANGED, new Double(min)));
    }

    /**
     * get the Minimum value.  
     * @returns Double as an Object
     */
    public Object getMinimum() { return new Double(min); }
    public double getMinAsDouble() { return min; }

    /**
     * set the Minimum value.
     * @exception IllegalArgumentException if input value is not of 
     *     type Number
     */
    public void setMaximum(Object val) 
	throws IllegalArgumentException
    {
	try {
	    max = ((Number) val).doubleValue();
	} catch (ClassCastException ex) {
	    throw new IllegalArgumentException("Not of type Number");
	}
	setChanged();
	notifyObservers(createEvent(MAXIMUM_CHANGED, new Double(max)));
    }

    public void setMaximum(double val) { 
	max = val; 
	setChanged();
	notifyObservers(createEvent(MAXIMUM_CHANGED, new Double(max)));
    }

    /**
     * get the Maximum value.  
     * @returns Double as an Object
     */
    public Object getMaximum() { return new Double(max); }
    public double getMaxAsDouble() { return max; }

    /**
     * return a string representation of the input value
     */
    public String toString(Object val) { 
	return (val instanceof Number) ? 
	    val.toString() : Double.toString(Double.NaN);
    }

    /**
     * return a two element array that gives the minimum and maximum 
     * values for in a viewable dataset or null if the viewable cannot 
     * return its data.  This instantiation assumes that the viewable 
     * contains strictly numeric data.
     * @exceptions IllegalArgumentException if input viewable v does not 
     *       contain numeric data
     */
    public Object[] calcRange(Viewable v) {
	NdArrayData data = v.getData();
	if (data == null) return null;
	Volume vol = data.getVolume();
	int[] sz = vol.getTrueSize();

	JavaType t = data.getType();
	if (t != JavaType.DOUBLE && t != JavaType.FLOAT &&
	    t != JavaType.LONG   && t != JavaType.INT   &&
	    t != JavaType.OBJECT)
	    throw new IllegalArgumentException("Viewable does not contain " +
					       "numeric data");

	double mnval = Double.POSITIVE_INFINITY, 
	       mxval = Double.NEGATIVE_INFINITY,
	       val;
	Object wrap;
	
	int[] vx;
	int i,j;
	long npts = sz[0];
	for(i=1; i < sz.length; i++) npts *= sz[i];
	for(i=0; i < npts; i++) {
//	    vx = NdArrayMath.indexNumberToArray(i, sz);
	    try {
		val = ((Number) data.getValue(i)).doubleValue();
	    }
	    catch (ClassCastException ex) {
//		System.err.println("data type: " + data.getClass().getName());
		throw new IllegalArgumentException("Viewable does not " +
						   "contain numeric data");
	    }

	    if (val != Double.NaN && val != Float.NaN && 
		val != Double.NEGATIVE_INFINITY && 
		val != Double.POSITIVE_INFINITY &&
		val != Float.NEGATIVE_INFINITY  && 
		val != Float.POSITIVE_INFINITY) 
	    {
		if (val < mnval) mnval = val;
		if (val > mxval) mxval = val;
	    }
	}
	if (mnval == Double.POSITIVE_INFINITY &&
	    mxval == Double.POSITIVE_INFINITY)   mnval = mxval = 0.0;

	Object[] out = new Object[2];
	System.err.println("range = " + mnval + ", " + mxval);
	out[0] = JavaType.DOUBLE.wrap(mnval);
	out[1] = JavaType.DOUBLE.wrap(mxval);

	return out;
    }

}


    
