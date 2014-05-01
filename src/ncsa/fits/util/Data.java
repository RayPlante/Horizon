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

 *-------------------------------------------------------------------------
 * The Java Fits code used here was first developed by T. McGlynn
 * (NASA/GSFC/USRA) as public domain software.  Minor changes have 
 * have been made for use with the NCSA Horizon Image Browser package.
 * In general, developers are free to use, update, and redistribute 
 * this source code.
 *-------------------------------------------------------------------------
 * History: 
 *  97            Original version by T. McGlynn
 *
 *  97 Oct.  Wei Xie   Adapted for use with the Horizon package.
 *                     A few bug fixes.
 *                     Package renamed to ncsa.fits.
 */

package ncsa.fits.util;

import java.io.*;
/** 
  * Data is a generic container class which may be used to store
  * Objects of any type.  It has the following differences from the
  * Object class.
  * <ul>
  * <li>
  *   Data implements the IO interface which provides for the input
  *     and output of Data object to Data streams. 
  *     All atomic data types, Strings and Objects which implement
  *     the IO interface, and arrays of these types will be read and written.
  *   Arrays of arbitrary dimensionality (currently < 9) may
  * <li>
  *     be dynamically allocated.  When a multiple dimension array is
  *     allocated, the sub-arrays will also be allocated automatically.
  * <li>
  *   Data implements the Cloneable interface and will recursively
  *     generate a deep copy of a multidimensional array or an array
  *     of Data objects.
  * <li>
  *   Data has constuctors for any atomic type (and Object)
  * <li>
  *   Data has efficient I/O classes for reading/writing of arrays.
  *    [I trust these will eventually be superceded by efficient
  *     routines provided in the standard distribution]
  * </ul>
  *
  * To use atomic data objects the use can use the various constructors
  * for the different atomic types, and retrieve the value using
  * the getXXXX methods. 
  * <par>
  * To create Object and array Data objects, the user can use either
  * the Data(Object) or Data (ObjectType, dimensions) constructors.
  * <par>
  * In the first case, the Data object will contain a reference to
  * the given object (but will not copy it).  
  * In the second, a object of the given type
  * will be constructed.  To retrieve these types use the getData
  * method with appropriate coercion of the results.  E.g.,
  * <tt>
  *     int[] dims = {5,10,11}; <br>
  *     double[][][] d3;<br>
  *     Data x = new Data(Data.DOUBLE, dims);<br>
  *     d3 = (double [][][]) x.getData();<br>
  * </tt>
  *     .... set the values of the d3 array.<p>
  * <tt>    x.writeClass(dos);</tt><br>   Will write all of the array to the stream dos.
  *     
  */
public class Data extends Object implements IO, Cloneable {


        /** This Data element has no data */
    public static final int 
        NODATA=0;
        /** Data is <tt>boolean</tt> scalar or array */
    public static final int 
        BOOLEAN=1;
        /** Data is <tt>byte</tt> scalar or array */
    public static final int 
	BYTE=2;
        /** Data is <tt> char</tt> scalar or array */
    public static final int 
	CHAR=3;
        /** Data is <tt>short</tt> scalar or array */
    public static final int 
	SHORT=4;
        /** Data is <tt>int</tt> scalar or array */
    public static final int 
	INT=5;
        /** Data is <tt>long</tt> scalar or array */
    public static final int 
	LONG=6;
        /** Data is <tt>float</tt> scalar or array */
    public static final int 
	FLOAT=7;
        /** Data is <tt>double</tt> scalar or array */
    public static final int 
	DOUBLE=8;
        /** Data is <tt>String</tt> scalar or array */
    public static final int 
	STRING=9;
        /** Data is <tt>Object</tt> scalar or array */
    public static final int 
        OBJECT=10;
	
    
    /** Sizes of atomic types in bytes (for I/O).
      */
    public static final int[] atomSize={0,1,1,2,2,4,8,4,8};
    

    /* Private objects */
    

    private Object datum;		// The data within the generic array.
    private int type;			// The type: see constants above.
    private int dimensionality;         // The dimensionality.
    private int[] dimensions;           // The dimensions.  Note that a scalar
                                        // Data has a dimensions variable
                                        // with 0 length.

    // Constructors that use existing data.
    
    // Scalar constructors for non-objects
    private void setScalar() {
	dimensions = new int[0];
    }
    
    /** Create a null Data object */
    public Data() {
	datum = null;
	type = NODATA;
	setScalar();
	dimensionality = 0;
    }
    
    /** @param b boolean value to be encapsulated as Data */
    public Data(boolean  b) {
	datum =  new Boolean(b);
	type  = BOOLEAN;
	setScalar();
    }
    /** @param b byte value to be encapsulated as Data */
    public Data(byte b) {
	datum = new Integer(b);
	type = BYTE;
	setScalar();
    }
    /** @param c char value to be encapsulated as Data */
    public Data(char c) {
	datum = new Character(c);
	type = CHAR;
	setScalar();
    }
    /** @param s short value to be encapsulated as Data */
    public Data(short s) {
	datum = new Integer(s);
	type = SHORT;
	setScalar();
    }
    /** @param i int value to be encapsulated as Data */
    public Data(int i) {
	datum =  new Integer(i);
	type = INT;
	setScalar();
    }
    /** @param i long value to be encapsulated as Data */
    public Data(long i) {
	datum =  new Long(i);
	type = LONG;
	setScalar();
    }
    /** @param f float value to be encapsulated as Data */
    public Data(float f) {
	datum = new Float(f);
	type = FLOAT;
	setScalar();
    }
    /** @param f double value to be encapsulated as Data */
    public Data(double f) {
	datum =  new Double(f);
	type = DOUBLE;
	setScalar();
    }

    /**  Generic constructor for all non-atomic types.
      *  This constuctor may be used to create array of atomic quantities
      *  or Objects or arrays of Objects.  
      *  Object arrays will not be initialized.
      * 
      *  @param	 datum    object to be encapsulated as Data. 
      */
    public Data(Object datum){
	
	String myClass = (datum.getClass()).getName();
	
	this.datum = datum;
	this.dimensionality = setDimensionality(myClass);

	if (dimensionality == 0) {
	    setScalar();
	    if (myClass.equals("java.lang.String")) {
		type = STRING;
	    } else {
		type = OBJECT;
	    }
	} else {
	    
	    /* Get rid of the leading ['s that indicate the dimensions.
	     * Note that we assume that this array is filled if it
	     * is more than one dimensional.
	     */
	    String arrayType = myClass.substring(dimensionality);
	    
	    switch(arrayType.charAt(0)) {
	     case 'X': 
		type = BOOLEAN;
		break;
	     case 'B':
		type = BYTE;
		break;
	     case 'C':
		type = CHAR;
		break;
	     case 'S':
		type = SHORT;
		break;
	     case 'I':
		type = INT;
	        break;
	     case 'J':
		type = LONG;
		break;
	     case 'F':
		type = FLOAT;
		break;
	     case 'D':
		type = DOUBLE;
		break;
	     case 'L':
		if (arrayType.substring(1).equals("java.lang.String")) {
		    type = STRING;
		} else {
		    type = OBJECT;
		}
	    }
	    setDimensions(dimensionality, datum, type, 0);
	}
    }

    /** Parse the class name to find the dimensionality
      */
    private int setDimensionality(String className) {
	
	int i;
	/* Look for the leading '['s which indicate an array.
	 */
	for (i=0; i<className.length(); i += 1) {
	    if (className.charAt(i) != '[') {
		return i;
	    }
	}
	/* We should never get here... */
	System.err.println("Data: Classname is all '['s");
	return -1;
    }

    /** Find the dimensions of an object.
      * This routine finds the dimensions of a given object.
      * It recursively strips off the leading dimension and finds
      * the length of that array.
      * @param dims  the initially unfilled array of dimensions
      * @param datum the object we are finding the dimensions of
      * @param type  the type of the object.
      * @param level the level of the recursion.
      */
    private void setDimensions(int dims, Object datum, int type, int level) {
	
	/* We need to find the dimensions of the object which
	 * is actually an array of some kind.  We'll do this recursively
	 * eliminating one dimension of data at each level of recursion.
	 */
	
	
	if (datum == null){    /* This shouldn't happen but if a user
			       * gives us an unfilled array we'll set
			       * all remaining dimensions to 0.
			       */
	    System.err.println("Data: Null array given");
	    return; 
	}
	
        if (level == 0) {
	    dimensions = new int[dims];
	}
	
	if (level+1 == dims) {
	    /* We need to cast to the actual type of the data. */
	    switch(type) {
	     case BOOLEAN:
		dimensions[level] = ((boolean[])datum).length;
		break;
	     case BYTE:
		dimensions[level] = ((byte[])datum).length;
		break;
	     case CHAR:
		dimensions[level] = ((char[])datum).length;
		break;
	     case SHORT:
		dimensions[level] = ((short[])datum).length;
		break;
	     case INT:
		dimensions[level] = ((int[])datum).length;
		break;
	     case LONG:
		dimensions[level] = ((long[])datum).length;
		break;
	     case FLOAT:
		dimensions[level] = ((float[])datum).length;
		break;
	     case DOUBLE:
		dimensions[level] = ((double[])datum).length;
	        break;
	     default:
		dimensions[level] = ((Object[])datum).length;
		break;
	    }
	    
	} else {
	    
	    dimensions[level] = ((Object[])datum).length;
	    setDimensions(dims, ((Object[])datum)[0], type, level+1);
	}
    }
	
    /** Construct an  array given a user specification.
      * @param  type type of data.
      * @param  dims dimensions of array data.
      */

    public Data(int type, int[] dims) throws Exception {
	
	this.dimensionality = dims.length;
	this.type = type;
	if (dims.length < 0) {
	    return;
	} else if (dims.length == 0) {
	    // Scalar
	    setScalar();
	    switch (type) {
	     case BOOLEAN:
		datum = new Boolean(false);
		break;
	     case BYTE:
		datum = new Integer(0);
		break;
	     case CHAR:
		datum = new Character(' ');
		break;
	     case SHORT:
		datum = new Integer(0);
		break;
	     case INT:
		datum = new Integer(0);
		break;
	     case LONG:
		datum = new Long(0);
		break;
	     case FLOAT:
		datum = new Float(0.);
		break;
	     case DOUBLE:
		datum = new Double(0.);
	        break;
	     case STRING:
		datum = new String();
		break;
	     case OBJECT:
		datum = new Object();
		break;
	     default:
		this.type = NODATA;
		datum = null;
		break;
	    }
	} else {
	    /* It's an array */
	    datum = makeArray(type, dims);
	    this.dimensions = new int[dims.length];
	    System.arraycopy(dims, 0, this.dimensions, 0, dims.length);
	    this.dimensionality = dims.length;
	    
	    this.type = type;
        }
	    
    }
    
    /** @return a deep copy of the Data object.
     */
    public Object clone() throws CloneNotSupportedException {
	
	if (dimensions.length == 0) {
	    switch(type) {
	     case NODATA:
		return new Data();
	     case BOOLEAN:
		return new Data(((Boolean)datum).booleanValue());
	     case BYTE:
		return new Data((byte)((Integer)datum).intValue());
	     case CHAR:
		return new Data(((Character)datum).charValue());
	     case SHORT:
		return new Data((short)((Integer)datum).intValue());
	     case INT:
		return new Data(((Integer)datum).intValue());
	     case LONG:
		return new Data(((Long)datum).longValue());
	     case FLOAT:
		return new Data(((Float)datum).floatValue());
	     case DOUBLE:
		return new Data (((Double)datum).doubleValue());
	     case STRING:
		return new Data((String) datum);
	     case OBJECT:
		if (datum instanceof Data) {
		    return new Data(((Data)datum).clone());
		} else {
		    throw new CloneNotSupportedException();
		}
	     default:
		throw new CloneNotSupportedException();
	    }
	    
	} else {
	    return new Data(cloneArray(datum, dimensions));
	}
    }
    
    /** Create a deep copy of an array.
      * @param datum The object to be copied. 
      * @param dims  The dimensions of the array. 
      * @return  A copy of the array.
      */
    protected Object cloneArray(Object datum, int[] dims) 
      throws CloneNotSupportedException {
	
	// Arrays are cloneable by default.  However when we clone
	// an array we copy only one dimension.  E.g.,
	// int [][] i = {{1,2,3},{4,5,6},{7,8,9}};
	// int [][] j = i.clone();  does not create a new copy of the
	// data, though j[1]= i[1].clone will.
	// So we just recurse through the dimensions of the array
	// until we get to the last dimension.
	  
	Object nextData = null;
	
	if (dims.length > 1) {
	    nextData = ((Object [])datum).clone();
	    int[] ndims = new int[dims.length-1];
	    System.arraycopy(dims, 1, ndims, 0, dims.length-1);
	    
	    int i;
	    for (i=0; i<dims[0]; i += 1) {
		((Object[])nextData)[i] = 
		  cloneArray(((Object[])datum)[i], ndims);
	    }
	} else {
	    switch(type) {
	     case BOOLEAN:
		nextData = ((boolean[])datum).clone();
		break;
	     case BYTE:
		nextData = ((byte[])datum).clone();
		break;
	     case CHAR:
		nextData = ((char[])datum).clone();
		break;
	     case SHORT:
		nextData = ((short[])datum).clone();
		break;
	     case INT:
		nextData = ((int[])datum).clone();
		break;
	     case LONG:
		nextData = ((long[])datum).clone();
		break;
	     case FLOAT:
		nextData = ((float[])datum).clone();
		break;
	     case DOUBLE:
		nextData = ((double[])datum).clone();
	        break;
	     default:
		nextData = ((Object[])datum).clone();
		break;
	    }

	    if (type == STRING) {
	        int i;
		for (i=0; i<dims[0]; i += 1) {
		    ((String[]) nextData)[i] = new String(((String[])datum)[i]);
		}
	    } else if (type == OBJECT) {
		if (((Object[])datum)[0] instanceof Data) {
	            int i;
		    for (i=0; i<dims[0]; i += 1) {
			((Object[])nextData)[i] = ((Data[])datum)[i].clone();
		    } 
		} else {
		    throw new CloneNotSupportedException();
		}
	    }
	}
	return nextData;
    }
		    
		
	    
    /** Create an array of specified type and dimensionality.
      * @param type The type of the array. 
      * @param dims The dimensions of the array. 
      * @return The newly constructed array. 
      */
    
    protected Object makeArray(int type, int[] dims) throws Exception {

	/* Create the  array.
	 * The mechanism used is a kludge but we can't do dynamic
	 * allocation of arrays using Class.newInstance so this
	 * seems the best we can do.  Another alternative is
	 * to use make an n-1 dimensional array of objects which
	 * are themselves one dimensional arrays but this
	 * puts a burden on the user to understand the mixed class
	 * structure of the array.
	 * 
	 * This routine is quite long but it's just a set of nested
	 * selects.
	 */
	int i;
	
	Object myData = null;
	
	if (dims.length > 8) {
	    throw new Exception ("Two many dimensions in array (>8)");
	}
	if (dims.length < 1) {
	    throw new Exception ("Invalid Array dimensionality");
	}
	
	// The outer switch is over the type of data.  For each type
	// we have an inner switch over the dimensionality of the
	// array.  If you need to support more dimensions, just add
	// the appropriate statements.
	switch(type) {
	 case BOOLEAN:
	        switch (dims.length) {
		 case 1:
		    myData = new boolean[dims[0]];
		    break;
		 case 2:
		    myData = new boolean[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new boolean[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new boolean[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new boolean[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new boolean[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new boolean[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new boolean[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case BYTE:
	        switch (dims.length) {
		 case 1:
		    myData = new byte[dims[0]];
		    break;
		 case 2:
		    myData = new byte[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new byte[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new byte[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new byte[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new byte[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new byte[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new byte[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case CHAR:
	        switch (dims.length) {
		 case 1:
		    myData = new char[dims[0]];
		    break;
		 case 2:
		    myData = new char[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new char[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new char[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new char[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new char[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new char[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new char[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case SHORT:
	        switch (dims.length) {
		 case 1:
		    myData = new short[dims[0]];
		    break;
		 case 2:
		    myData = new short[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new short[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new short[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new short[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new short[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new short[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new short[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case INT:
	        switch (dims.length) {
		 case 1:
		    myData = new int[dims[0]];
		    break;
		 case 2:
		    myData = new int[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new int[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new int[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new int[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new int[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new int[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new int[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case LONG:
	        switch (dims.length) {
		 case 1:
		    myData = new long[dims[0]];
		    break;
		 case 2:
		    myData = new long[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new long[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new long[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new long[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new long[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new long[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new long[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case FLOAT:
	        switch (dims.length) {
		 case 1:
		    myData = new float[dims[0]];
		    break;
		 case 2:
		    myData = new float[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new float[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new float[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new float[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new float[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new float[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new float[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case DOUBLE:
	        switch (dims.length) {
		 case 1:
		    myData = new double[dims[0]];
		    break;
		 case 2:
		    myData = new double[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new double[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new double[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new double[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new double[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new double[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new double[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	 case STRING:
	        switch (dims.length) {
		 case 1:
		    myData = new String[dims[0]];
		    break;
		 case 2:
		    myData = new String[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new String[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new String[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new String[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new String[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new String[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new String[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	    break;
	    
	 case OBJECT:
	        switch (dims.length) {
		 case 1:
		    myData = new Object[dims[0]];
		    break;
		 case 2:
		    myData = new Object[dims[0]][dims[1]];
		    break;
		 case 3:
		    myData = new Object[dims[0]][dims[1]][dims[2]];
		    break;
		 case 4:
		    myData = new Object[dims[0]][dims[1]][dims[2]][dims[3]];
		    break;
		 case 5:
		    myData = new Object[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]];
		    break;
		 case 6:
		    myData = new Object[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]];
		    break;
		 case 7:
		    myData = new Object[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]];
		    break;
		 case 8:
		    myData = new Object[dims[0]][dims[1]][dims[2]][dims[3]][dims[4]][dims[5]][dims[6]][dims[7]];
		    break;
		}
	}
	
	if (dims.length > 1) {
	    /* Fill up the array using a recursive call
	     * Note that the final reference will not be created for
	     * Strings or Objects but all of the array structure will
	     * be filled in.
	     */
	    int[] ndims = new int[dims.length-1];
	    System.arraycopy(dims, 1, ndims, 0, dims.length-1);
	    
	    for (i=0; i<dims[0]; i ++) {
		((Object[])myData)[i] = makeArray(type, ndims);
	    }
	}
    	return myData;
    }
    
    /** Get the dimensions of the Data object.
      * @return An integer array,  possibly of length 0 for scalars.
      */ 

    public int[] getDimensions(){
        return dimensions;
    }
    
    /** Get the type of the Data object.
     */
    public int  getType(){
        return type;
    }
    
    /** Get the string corresponding to the type field. 
      * @param type The type for which the string equivalent is desired.
      */
    public static String types(int type) {
	
        switch (type) {
	 case NODATA:
	    return "null";
	 case BOOLEAN:
	    return "boolean";
	 case BYTE:
	    return "byte";
	 case CHAR:
	    return "char";
	 case SHORT:
	    return "short";
	 case INT:
	    return "int";
	 case LONG:
	    return "long";
	 case FLOAT:
	    return "float";
	 case DOUBLE:
	    return "double";
	 case STRING:
	    return "String";
	 case OBJECT:
	    return "Object";
	 default:
	    return null;
	}
    }
    
    
    /** Return the Data Object as a boolean
      *
      * @return The boolean value stored in the Data object
      */
    public boolean getBoolean() {
	if (checkMatch(BOOLEAN, type))
	    return ((Boolean) datum).booleanValue();
	else
	    return false;
    }
    /** Return the Data Object as a byte
      * @return The byte value stored in the Data object
      */
    public byte getByte() {
	if (checkMatch(BYTE, type))
	  return (byte) ((Integer) datum).intValue();
	else
	  return 0;
	}

    /** Return the Data Object as a char
      * @return The char value stored in the Data object
      */
    public char getChar() {
	if (checkMatch(CHAR, type))
	    return ((Character) datum).charValue();
	else
	    return 0;
    }

    /** Return the Data Object as a short
      * @return The short value stored in the Data object
      */
    public short getShort() {
	if (checkMatch(SHORT, type)) 
	    return (short) ((Integer) datum).intValue();
	else
	    return 0;
    }
    
    /** Return the Data Object as an int
      * @return The int value stored in the Data object
      */
    public int getInt() {
	if (checkMatch(INT, type))
	    return ((Integer) datum).intValue();
	else
	    return 0;
    }

    /** Return the Data Object as a float
      * @return The float value stored in the Data object
      */
    public float getFloat() {
	if (checkMatch(FLOAT, type) )
	    return ((Float) datum).floatValue();
	else
	    return (float) 0.;
    }
    /** Return the Data Object as a double
      * @return The double value stored in the Data object
      */
    public double getDouble() {
	if (checkMatch(DOUBLE, type))
	    return ((Double) datum).doubleValue();
	else
	    return 0.;
    }
    
    /** Return the Data Object as a String
      * @return The String value stored in the Data object
      */
    public String getString() {
	if (checkMatch(STRING, type))
	    return (String) datum;
	else
	    return null;
    }
	
    /** Return the Data Object as a generic Object.  Generally users
      * will want to coerce this Object into the actual type of the
      * data.  E.g., if the Data object contains a three-dimensional
      * double array, then the invocation of this method might be like: <p>
      * <tt> double[][][] x = (double [][][])someData.getData(); </tt>
      * 
      * @return The raw Object in which the data is stored.
      */
    public Object getData() {
	return datum;
    }
    
    /** Check that the user has asked for valid data.
      */
    protected boolean checkMatch (int reqType, int arrType) {

	if (dimensions.length > 0 && dimensions[0] > 0) {
	    System.err.println("Data: Invalid scalar request for array data");
	    return false;
	}
	
        if (reqType != arrType) {
	    System.err.println("Data: Invalid scalar request for type "+
			       types(reqType)+" from type "+types(arrType));
	    return false;
	}
	return true;
    }
    
    
    /** Write the Data to an output data stream.
      *  @param os The open DataOutput object.
      */
    
    public void writeClass(DataOutput os) throws IOException { 
	
	if (dimensionality == 0) {
	    // Handle the scalars.
	    switch(type) {
	     case BOOLEAN:
		os.writeBoolean(((Boolean)datum).booleanValue());
		break;
	     case BYTE:
		os.writeByte((byte)((Integer) datum).intValue());
		break;
	     case CHAR:
		os.writeChar(((Character) datum).charValue());
		break;
	     case SHORT:
		os.writeShort((short) ((Integer)datum).intValue());
		break;
	     case INT:
		os.writeInt(((Integer)datum).intValue());
		break;
	     case LONG:
		os.writeLong(((Long)datum).longValue());
		break;
	     case FLOAT:
		os.writeFloat(((Float)datum).floatValue());
		break;
	     case DOUBLE:
		os.writeDouble(((Double)datum).doubleValue());
	        break;
	     case STRING:
		os.writeUTF((String) datum);
	        break;
	     default:
		if (datum instanceof IO) {
		    ((IO) datum).writeClass(os);
		}
		/* Don't do anything with other types */
		break;
	    }
	} else if (dimensionality > 0) {
	    /* Recursively descend the dimensions to write out the
	     * the entire array.
	     */
	    writeArray(os, datum, 0);
	}
    }

    /** Recursively write a (possibly multidimensional) 
      * array to an output stream.
      * @param os The open output stream.
      * @param array The array to be written.
      * @level The current recursion level.
      */
    protected void writeArray(DataOutput os, Object array, int level) 
      throws IOException {
	
	if (array == null)
	    return;
	
	if (level < dimensionality-1) {
	    
	    /* Multi-dimensional array */
	    
	    int i;
	    /* Note that we can't use recursion to the last
	     * step because the array may not contain objects.
	     */
	    for (i=0; i< ((Object[])array).length; i += 1) {
	        writeArray(os, ((Object[])array)[i], level+1);
	    }
	    
	} else {
	    
	    /* One dimensional array */
	    
	    /* Handle two special cases */
	    if (type == BYTE) {
	        // This is the only type that we can write as a block
		// using calls in the API.
	        os.write((byte[]) array, 0, ((byte[])array).length);
	        return;
		
	    } else if (type == OBJECT && ((Object[])datum)[0] instanceof IO) {
		
		// Note that we should be checking each element
		// of the array not just the first.  Also,
		// we could try to handle Objects that were arrays
		// here but we currently don't.
		
		int i;
		for (i=0; i<((Data[]) array).length; i+= 1) {
		    ((Data[]) array)[i].writeClass(os);
		}
	    }
	    if (type > 0 && type < OBJECT) {
		write1DArray(os, array, dimensions[level]);
	    }
	}
    }
    
    /** Write all one-dimensional arrays (except bytes).
      * This routine should probably be optimized in the
      * way that read1DArray has been, i.e., to
      * buffer the data internally and avoid multiple
      * calls to synchronized routines.
      * @param os The output stream.
      * @param array The one-dimensional array.
      * @param size The size of the array.
      */
    protected void write1DArray (DataOutput os, Object array, int size) 
      throws IOException {
        
        int i;
        for (i=0; i<size; i += 1) {
        
	   switch (type){
	     case BOOLEAN:
		os.writeBoolean( ((boolean[]) array)[i]);
		break;
	     case CHAR:
		os.writeChar( ((char[]) array)[i]);
		break;
	     case SHORT:
		os.writeShort( ((short[]) array)[i]);
		break;
	     case INT:
		os.writeInt( ((int []) array)[i]);
		break;
	     case LONG:
		os.writeLong( ((long[]) array)[i]);
		break;
	     case FLOAT:
		os.writeFloat( ((float[]) array)[i]);
		break;
	     case DOUBLE:
		os.writeDouble( ((double[]) array)[i]);
		break;
	     case STRING:
		os.writeUTF( ((String[]) array)[i]);
		break;
	    default:
	        throw new IOException("write1DArray called for invalid type "+type);
	    }
	}
    }

    /**  Read Data from a binary stream.
      *   Since Data implements IO, a hierarchy of Datas can be read.
      *  @param is  The input data stream.
      */
    public void readClass(DataInput is) throws IOException { 
	
	
	if (dimensionality == 0  && type != 0) {
	    // Handle the scalars.
	    switch(type) {
	     case BOOLEAN:
		datum = new Boolean(is.readBoolean());
		break;
	     case BYTE:
		datum = new Integer(is.readByte());
		break;
	     case CHAR:
		datum = new Character(is.readChar());
		break;
	     case SHORT:
		datum = new Integer(is.readShort());
		break;
	     case INT:
		datum = new Integer(is.readInt());
		break;
	     case LONG:
		datum = new Long(is.readLong());
		break;
	     case FLOAT:
		datum = new Float(is.readFloat());
		break;
	     case DOUBLE:
		datum = new Double(is.readDouble());
	        break;
	     case STRING:
		datum = new String(is.readUTF());
	        break;
	     case OBJECT:
		if (datum instanceof IO) {
		    ((IO) datum).readClass(is);
		} 
		/* Don't do anything with other types */
		break;
	     default:
		break;
	    }
	} else if (dimensionality > 0) {
	    /* Recursively descend the dimensions to write out the
	     * the entire array.
	     */
	    datum = readArray(is, datum, 0);
	}
    }
    
  /** Recursively read a (possibly multidimensional) array.
   * @param is  The input stream.
   * @param array The array into which data is to be read.
   * @param level The level of recursion.
   */
    
  protected Object readArray(DataInput is, Object array, int level) 
    throws IOException {
      /* Recursively read an arbitrary dimensionality array */
      if (array == null) {
	return null;
      }
      if (level < dimensionality-1) {
	int i;
	/* Note that we can't use recursion to the last
	 * step because the array may not contain objects.
	 */
	for (i=0; i< ( (Object[])array).length; i += 1) {
	  ((Object[])array)[i] = readArray(is, ((Object[])array)[i], level+1);
	}
	return array;    
      }
      else {
	/* Handle two special cases */
	if (type == BYTE) {
	  // This is the only type that we can read as a block.
	  is.readFully((byte[])array);
	}
	else if (type == OBJECT && ((Object[])array)[0] instanceof IO) {
	  int i;
	  Object[] temp;
	  temp = (Object []) array;
	  for (i=0; i<temp.length; i += 1) {
	    ((IO) temp[i]).readClass(is);
	  }	
	}
	else if (type > 0 && type < OBJECT) {
	  read1DArray(is, array, dimensions[level]);
	}
	return array;
      }
  }
    
  /** Read all one dimensional arrays (except bytes).
   * Amazingly enough these juryrigged conversions
   * seem to be faster than creating a ByteArrayInputStream and
   * doing the reads on that.  Apparently the standard I/O libraries
   * rely heavily on synchronized routines which are extremely slow.
   * 
   * These routines are final to maximize efficiency,
   * and static since they do not use the data object (and so
   * they can be used as general I/O routines by other classes.
   * These routines share the same structure:
   * @param is the input stream.
   * @param ioBuffer a pre-allocated I/O buffer.
   * @param array is the array into which data is to be read
   * @param size the number of items to be read.
   * @param offset the offset within the array into which the data is
   * to be placed.
   */
  public static final void readBArray
    (DataInput is, byte[] ioBuffer, boolean[] array, int size, int offset)
    throws IOException {
      
      // This assumes a representation of
      // booleans as true=non-zero byte
	  
      int i;
      is.readFully(ioBuffer, 0, size);
      
      for (i=0; i<size; i+= 1) {    
	array[i+offset] = (ioBuffer[i] != 0); 
      }
  }
    
  /** <i> see readBArray </i> */
  public static final void readCArray
    (DataInput is, byte[] ioBuffer, char[] array, int size, int offset)
    throws IOException {
      int i;
      int off = 0;
      is.readFully(ioBuffer, 0, 2*size);
      
      for (i=0; i<size; i+= 1) {
	    
	array[i+offset] = (char) ( 
				  ((ioBuffer[off]  &0xFF) <<8) |
				  ((ioBuffer[off+1]&0xFF)    ) );
	off += 2;
      }
  }
    
  /** <i> see readBArray </i> */
  public static final void readSArray
    (DataInput is, byte[] ioBuffer, short[] array, int size, int offset)
    throws IOException {
    int i;
    int off = 0;
    is.readFully(ioBuffer, 0, 2*size);
	
    for (i=0; i<size; i+= 1) {    
      array[i+offset] = (short) ( 
				 ((ioBuffer[off]  &0xff)<<8) |
				 ((ioBuffer[off+1]&0xff)    ));
      off += 2;
    }
  }
    
    /** <i> see readBArray </i> */
    public static final void readIArray
      (DataInput is, byte[] ioBuffer, int[] array, int size, int offset)
      throws IOException {
	int i;
	int off = 0;
	is.readFully(ioBuffer, 0, 4*size);
	
	for (i=0; i<size; i+= 1) {
	    
	    array[i+offset] = ((ioBuffer[off]  &0xFF) <<24) |
	                      ((ioBuffer[off+1]&0xFF) <<16) |
	                      ((ioBuffer[off+2]&0xFF) << 8) |
	                      ((ioBuffer[off+3]&0xFF)     ) ;
	    off += 4;
	}
    }

    /** <i> see readBArray </i> */
    public static final void readFArray
      (DataInput is, byte[] ioBuffer, float[] array, int size, int offset)
      throws IOException {
	int i;
	int off = 0;
	is.readFully(ioBuffer, 0, 4*size);
	
	for (i=0; i<size; i+= 1) {
	    
	    array[i+offset] = Float.intBitsToFloat(
	               ((ioBuffer[off]  &0xFF)<<24) |
	               ((ioBuffer[off+1]&0xFF)<<16) |
	               ((ioBuffer[off+2]&0xFF)<< 8) |
	               ((ioBuffer[off+3]&0xFF)    ) );
	    off += 4;
	}
    }

    /** <i> see readBArray </i> */
    public static final void readDArray
      (DataInput is, byte[] ioBuffer, double[] array, int size, int offset)
      throws IOException {
	int i;
	int off = 0;
	is.readFully(ioBuffer, 0, 8*size);
	
	for (i=0; i<size; i += 1) {
	    
	    long temp=
	               ((ioBuffer[off]  &0xFFL)<<56) |
	               ((ioBuffer[off+1]&0xFFL)<<48) |
	               ((ioBuffer[off+2]&0xFFL)<<40) |
	               ((ioBuffer[off+3]&0xFFL)<<32) |
	               ((ioBuffer[off+4]&0xFFL)<<24) |
	               ((ioBuffer[off+5]&0xFFL)<<16) |
	               ((ioBuffer[off+6]&0xFFL)<< 8) |
	               ((ioBuffer[off+7]&0xFFL)    ) ;
	    
	    array[i+offset] = Double.longBitsToDouble(temp);
	    off += 8;
	}
    }

    /** <i> see readBArray </i> */
    public static final void readLArray
      (DataInput is, byte[] ioBuffer, long[] array, int size, int offset)
      throws IOException {
	int i;
	int off = 0;
	is.readFully(ioBuffer, 0, 8*size);
	
	for (i=0; i<size; i+= 1) {
	    
	    array[i+offset] = ((ioBuffer[off]  &0xFFL)<<56) |
	                      ((ioBuffer[off+1]&0xFFL)<<48) |
	                      ((ioBuffer[off+2]&0xFFL)<<40) |
		              ((ioBuffer[off+3]&0xFFL)<<32) |
	                      ((ioBuffer[off+4]&0xFFL)<<24) |
	                      ((ioBuffer[off+5]&0xFFL)<<16) |
	                      ((ioBuffer[off+6]&0xFFL)<< 8) |
	                      ((ioBuffer[off+7]&0xFFL)    ) ;
	    off += 8;
	}
    }


  /** Read a 1-D array.  This calls the specialized <tt> readXArray </tt>
   * routines possibly breaking 
   * up a large array read into multiple segments.
   * 
   * @param is The input stream.
   * @param array The array to be read.
   * @param size The size of the array.
   */
  protected void read1DArray (DataInput is, Object array, int size)
                             throws IOException {
    int max = 32768/atomSize[type];
    int offset = 0;
    byte[] ioBuffer;
    
    if (size > max) {
      ioBuffer = new byte[32768];
    }
    else {
      ioBuffer = new byte[size*atomSize[type]];
    }

    while (offset < size) {
      int csize = size - offset;
      if (csize > max) {
	csize = max;
      }
        
      switch (type) {
      case BOOLEAN: 
	readBArray(is, ioBuffer, (boolean[]) array, csize, offset);
	break;
      case CHAR:
	readCArray(is, ioBuffer, (char[])array, csize, offset);
	break;
      case SHORT:
	readSArray(is, ioBuffer, (short[])array, csize, offset);
	break;
      case INT:
	readIArray(is, ioBuffer, (int[])array, csize, offset);
	break;
      case FLOAT:
	readFArray(is, ioBuffer, (float[])array, csize, offset);
	break;
      case LONG:
	readLArray(is, ioBuffer, (long[]) array, csize, offset);
	break;
      case DOUBLE:
	readDArray(is, ioBuffer, (double[]) array, csize, offset);
	break;
      default:
	throw new IOException("read1DArray called for invalid type "+type);
      }
      offset += csize;
    }    
  }

    /** Dump the contents of the Data for its string value.
      */
    public String toString () {
	
	
	if (dimensionality < 0 || 
	    (dimensionality == 0 && (type < BOOLEAN || type > OBJECT))) {
	    return "Invalid Data";
	} else if (dimensionality == 0) {
	    if (datum == null) {
		return "Unitialized Datum";
	    } else {
	        return datum.toString();
	    }
	} else {
	    int size=1;
	    int i;
	    for (i=0; i<dimensionality; i += 1) {
		size *= dimensions[i];
	    }
	    
	    if (datum == null) {
		return "Unitialized array of type: "+types(type)+
		  " with dimensionality "+dimensionality;
	    }
	    
	    ByteArrayOutputStream out = new ByteArrayOutputStream(size*8);
	    PrintStream os = new PrintStream(out);
	    printClass(os);
	    return out.toString();
	}
    }
   /** Print the Data in a readable form.
     * @param os an open PrintStream.
     */
    
    public void printClass (PrintStream os) {
        prbufData(datum, os, 0);
	os.print("\n");
	    
    }

   /** Recursively print the Data object.
     * @param datum the data to be listed.
     * @param os the PrintStream to write on.
     * @param the current level of recursion.
     */
    protected void prbufData(Object datum, PrintStream os, int level) {
	int i;
	String tstr;
	
	if (dimensionality == 0) {
	    if (type == NODATA) {
		os.print("null");
	    } else {
	        os.print(datum.toString());
	    }
	    return;
	} 
	
	os.print("\n {");
	if (level < dimensionality-1) {
	    for (i=0; i<dimensions[level]; i += 1) {
		prbufData(((Object [])datum)[i], os, level+1);
		if (i != dimensions[level]-1) {
		    os.print(",");
		} else {
		    os.print("}");
		}
	    }
	} else {
	    for (i=0; i<dimensions[level]; i += 1) {
	    if (i == dimensions[level]-1) {
		tstr = "";
	    } else {
		tstr = ", ";
	    }
		
	    switch(type) {
	     case BOOLEAN:
		os.print( ((boolean[])datum)[i]);
		os.print(tstr);
		break;
	     case BYTE:
		os.print( ((byte[])datum)[i]);
		os.print(tstr);
		break;
	     case CHAR:
		os.print( "'" + ((char[])datum)[i]+ "'");
		os.print(tstr);
		break;
	     case SHORT:
		os.print( ((short[])datum)[i]);
		os.print(tstr);
		break;
	     case INT:
		os.print( ((int[])datum)[i]);
		os.print(tstr);
		break;
	     case LONG:
		os.print( ((long[])datum)[i]);
		os.print(tstr);
		break;
	     case FLOAT:
		os.print( ((float[])datum)[i]);
		os.print(tstr);
		break;
	     case DOUBLE:
		os.print( ((double[])datum)[i]);
		os.print(tstr);
	        break;
	     case STRING:
		if (  ((String[])datum)[i] == null) {
		    os.print("<null>");
		} else {
		    os.print("\""+ ((String[])datum)[i]+ "\"");
		}
		os.print(tstr);
	        break;
	     default:
		if ( ((Object[])datum)[i] == null) {
		    os.print("<null>");
		} else {
		    String sout= ((Object[]) datum)[i].toString();
		    os.print(sout);
		}
		os.print(tstr);
		break;
	      }
	    }
	    os.print ("}");
	}
    }

   /** Test Data class functionality. */
    public static void main (String args[]) {
	
	/* Test the Data implementation
	 */
	
	/* Variables used */
	int[] dims1 = {5,5,5};
	int[][][] tx1;
	Data test1=null, test2=null, test3=null, test4=null;
	Data t2a=null, t2b=null, t2c=null, t2d=null, t2e=null;
	DataOutputStream dos=null;
	int i, j, k;
	int[] d2 = {5};
	int[] d2a = {7,7};
	int[] d2b = {7};
	int[] d2c = {7};
	double[][][] dtest=new double[10][3][7];
	Object[]  obj;
	
	
	
	System.out.println("*** Creating 2D Data ***");
	// Create  a Data
	try {
	    test1 = new Data(Data.INT, dims1);
	} catch (Exception e) {
	    System.out.println("Had exception:"+e);
	}
	
	// Populate the Data
	tx1= (int [][][]) test1.getData();
	for (i=0; i<5; i+= 1) {
	    for (j=0; j<5; j += 1) {
		for (k=0; k<5; k += 1) {
		    tx1[i][j][k] = i*j*k;
		}
	    }
	}
	
	System.out.println("*** Writing to output file ***");
	// Write the Data out to a file
	try {
	    dos = new DataOutputStream(new FileOutputStream("test.file"));
	} catch (IOException e) {
	    System.out.println("Unable to open test.file");
	}
	
	try {
	    test1.writeClass(dos);
	} catch (IOException e) {
	    System.out.println("Unable to write Data test1");
	}
	dos = null;
	
	// Read it back.
	
	System.out.println("*** Reading data back into second Data ***");
	
	DataInputStream dis=null;
	try {
	    dis = new DataInputStream(new FileInputStream("test.file"));
	} catch (IOException e) {
	    System.out.println("Unable to open test.file");
	}

	try {
	    test3 = new Data(Data.INT, dims1);
	} catch (Exception e) {
	    System.out.println("Had exception:"+e);
	}
	
	try {
	    test3.readClass(dis);
	} catch( IOException e) {
	    System.out.println("Had exception: "+e);
	}
	
	System.out.println("*** Displaying Read Back data ***");
	try {
	    test3.printClass(System.out);
	} catch (Exception e) {
	    System.out.println("Execption caught "+e);
	}
	
	
	System.out.println("*** Creating hierarchical Data ***");
	System.out.println("     7x7 Boolean");
	System.out.println("     7 element string");
	System.out.println("     7 element double");
	System.out.println("     Scalar string");
	System.out.println("     Scalar double");
	try {
	    test2 = new Data(Data.OBJECT, d2);
	    t2a = new Data(Data.BOOLEAN, d2a);
	    t2b = new Data(Data.STRING, d2b);
	    t2c = new Data(Data.DOUBLE, d2c);
	    t2d = new Data("This is a scalar String");
	    t2e = new Data(3.14159);
	} catch (Exception e) {
	    System.out.println("Caught exception "+e);
        }
	
	System.out.println("*** Populating hierarchical Data ***");
	for (i=0; i<7; i += 1) {
	    ((String [])t2b.getData())[i] = "String number "+i;
	    ((double [])t2c.getData())[i] = i*3.14;
	    for (j=0; j<7; j += 1) {
	        if (i == j) {
		    ((boolean[][]) t2a.getData())[i][j] = true;
	        }
	    }
	}
	
	obj = (Object []) test2.getData();
	obj[0] = t2a;
	obj[1] = t2b;
	obj[2] = t2c;
	obj[3] = t2d;
	obj[4] = t2e;
	
	System.out.println("*** Writing hierarchical Data ***");
	test2.printClass(System.out);
	
	System.out.println("*** Creating Data from 10x3x7 double ***");
	try {
	    test4 = new Data(dtest);
	} catch (Exception e) {
	    System.out.println("Exception creating from 3D double");
	}
	
	for (i=0; i<10; i += 1) {
	    for (j=0; j<3; j += 1) {
		for (k=0; k<7; k += 1) {
		    dtest[i][j][k] = 3.14*i*Math.sin((double)j)*Math.cos((double)k);
		}
	    }
	}
	
	System.out.println("*** Printing 10x3x7 double ***");
	test4.printClass(System.out);
	
	
    }
    
}
