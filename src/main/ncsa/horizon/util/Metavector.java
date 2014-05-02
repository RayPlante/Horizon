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
 * This software uses code found in Sun's version of Vector.java; thus,
 * be aware of its copyright:
 * 
 * @(#)Vector.java	1.29 95/12/01  
 *
 * Copyright (c) 1994 Sun Microsystems, Inc. All Rights Reserved.
 * @version 	1.29, 01 Dec 1995
 * @author	Jonathan Payne
 * @author	Lee Boynton
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *-------------------------------------------------------------------------
 * History: 
 *  97mar17  rlp  Original version adapted from java.util.Vector
 *  97sep30  rlp  fixed nullpointer bug in setSize().
 */
package ncsa.horizon.util;

import java.util.*;

/**
 * a container for holding an array of metadata.  This looks and behaves very
 * much like the Vector object (although it is not a subclass of Vector).  
 * It is intended (but not enforced or required) that all the array members 
 * have the same type.  <p> 
 *
 * An important feature of this container is its support for a default 
 * values.  This support differentiates a Metavector from a regular Vector 
 * in the following ways: <p>
 * <ul>
 * <li> default values (stored as an internal Metavector object) are set 
 *      during construction and cannot be publicly overwritten. 
 * <li> if a default value is not provide, the default value is null
 * <li> Array values may be set that override the default; however, when 
 *      values are removed or erased, the default remains in place.   
 * <li> if the user requests the value of an index that is out of range, 
 *      null is returned. (No exception is thrown, and the size of the 
 *      array does not change.)
 * <li> the size of the array cannot be reduced to less than the size
 *      default array.
 * <li> values at indices less than the size of the default array cannot 
 *      be shifted via a request to insert or remove a value.
 * </ul>  <p>
 *
 * The internally stored default metavector provides a way for managers of
 * metadata to protect their data from updates by other objects.  If a 
 * metadata manager has a Metavector it wants other objects to read from 
 * but not write into, it can create a new Metavector object that has the 
 * original as the default.  The other users of the new Metavector can then
 * read the elements and even override them without affecting the original
 * Metavector object.  <p>
 *
 * One other feature of the support for default values is that it allows
 * the originator of Metavector (the object that has a reference to the 
 * default Metavector) to update the defaults after the Metavector has been 
 * created and passed on.  Such changes would be seen by those objects that
 * have not overriden the defaults.  Client objects that prefer that their 
 * copy of the Metavector not be connected the originator in this way can 
 * "detach" it via the <a href="#detach()">detach()</a> method.  Other 
 * methods are provided for obtaining "detached" copies of the data, 
 * including <a href="#copyInto(Object[])">copyInto(Object[])</a>, 
 * <a href="#copyIntoVector()">copyIntoVector()</a>, 
 * <a href="#detachedClone()">detachedClone()</a>, and 
 * <a href="#cloneDefaults()">cloneDefaults()</a>.  (Note that the standard
 * <a href="#clone()">clone()</a> produces a Metavector whose defaults are 
 * "attached" to the same object as the original.)  <p>
 *
 * One should note that the defaults protection only applies to the internal 
 * default Metavector container itself, not its elements.  That is, the value
 * returned by <a href="#elementAt(int)">elementAt(int)</a> is the original 
 * version of the datum and not a copy.  Thus, there may be any number of 
 * references to the datum held by other objects.  (The datum is protected only 
 * if it is of a type that is not internally editable.  For example, the value
 * of an Integer or String object cannot be updated; however, if the datum is
 * of type Stack, the data internal to that Stack is not protected from 
 * updates.)  An exception to this is when the value is obtained from the 
 * default list and is of type Metavector or Metadata; in this case, the 
 * meta-container is placed as the default list in a new meta-container before 
 * being passed on.  <p>
 * 
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @version $Id: Metavector.java,v 1.2 1997/12/05 00:41:23 rplante Exp $
 */
public class Metavector implements Cloneable {

    /**
     * amount to increase the capacity of the internal array when additional 
     * data slots are needed.  If value is 0, then the capacity is double
     * each time it is expanded.
     */
    protected int capacityIncrement=0;

    /**
     * The number of elements in the array
     */
    protected int elementCount;

    /**
     * The buffer where elements are stored.  The capacity of the container 
     * is given by the length of this array.
     */
    protected Object elementData[];

    /**
     * The default values
     */
    protected Metavector defaults=null;

    /**
     * true if Runnable objects should be executed to obtain values
     * for requested metadata when available.  Note that this class
     * does not support transparent execution of Metarunners saved
     * elements of its array; this tag is only used by the 
     * <a href="#getMetadatum(int, java.lang.String)">getMetadatum()</a>
     * method.  
     */
    public boolean executeRunners = true;

    /**
     * Constructs a metavector with specified default values, storage
     * capacity and capacity increment.
     * @param defaults Metavector containing default values 
     * @param initialCapacity the initial storage capacity of the vector
     * @param capacityIncrement how much to increase the element's 
     * size by.
     */
    public Metavector(Metavector defaults, int initialCapacity, 
		      int capacityIncrement) 
    {
	super();
	this.defaults = defaults;
	this.elementCount = (defaults == null) ? 0 : defaults.size();
	this.elementData = new Object[initialCapacity];
	this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs an empty vector with the specified storage
     * capacity and the specified capacityIncrement but with no default 
     * values.
     * @param initialCapacity the initial storage capacity of the vector
     * @param capacityIncrement how much to increase the element's 
     * size by.
     */
    public Metavector(int initialCapacity, int capacityIncrement) {
	this(null, initialCapacity, capacityIncrement);
    }


    /**
     * Constructs a metavector with the specified storage capacity but
     * with no default values
     * @param initialCapacity the initial storage capacity of the vector
     */
    public Metavector(int initialCapacity) {
	this(null, initialCapacity, 0);
    }

    /**
     * Constructs a metavector with the specified storage capacity.
     * @param defaults Metavector containing default values 
     * @param initialCapacity the initial storage capacity of the vector
     */
    public Metavector(Metavector defaults, int initialCapacity) {
	this(defaults, initialCapacity, 0);
    }

    /**
     * Constructs a metavector with the specified defaults
     * @param defaults Metavector containing default values 
     * @param initialCapacity the initial storage capacity of the vector
     */
    public Metavector(Metavector defaults) {
	this(defaults, ((defaults.size() > 0) ? defaults.size() : 10), 0);
    }

    /**
     * Constructs a metavector with no default values.
     */
    public Metavector() {
	this(10);
    }


    /**
     * Returns the number of elements in the vector.
     * Note that this is not the same as the vector's capacity.
     */
    public final int size() {
	return elementCount;
    }

    /**
     * Returns the number of elements in the vector.
     * Note that this is not the same as the vector's capacity.
     */
    public final int defaultSize() {
	return ((defaults == null) ? 0 : defaults.size());
    }

    /**
     * Returns the current capacity of the vector.
     */
    public final int capacity() {
	return elementData.length;
    }

    /**
     * Trims the vector's capacity down to size. Use this operation to
     * minimize the storage of a vector. Subsequent insertions will
     * cause reallocation.
     */
    public final synchronized void trimToSize() {
	int copyCount = elementCount;

	if (elementData.length <= copyCount) {

	    // This will happen if capacity() < size()
	    int i;
	    for(i=copyCount; i > 0 && elementData[i-1] != null; i--);
	    copyCount = i;
	}

	if (copyCount < elementData.length) {
	    Object oldData[] = elementData;
	    elementData = new Object[elementCount];
	    System.arraycopy(oldData, 0, elementData, 0, copyCount);
	}
    }

    /**
     * Ensures that the vector has at least the specified capacity.
     * @param minCapacity the desired minimum capacity
     */
    public final synchronized void ensureCapacity(int minCapacity) {
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    Object oldData[] = elementData;
	    int newCapacity = (capacityIncrement > 0) ?
		(oldCapacity + capacityIncrement) : (oldCapacity * 2);
    	    if (newCapacity < minCapacity) {
		newCapacity = minCapacity;
	    }
	    elementData = new Object[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, oldData.length);
	}
//	System.err.println("Capacity Requested: " + minCapacity + 
//			   " granted: " + elementData.length);
    }

    /**
     * Sets the size of the vector. If the size shrinks, the extra elements
     * (at the end of the vector) are lost; if the size increases, the
     * new elements are set to null.  The size cannot be shrunk to less than 
     * defaultSize().
     * @param newSize the new size of the vector.  If newSize is less than 
     *                that returned by defaultSize(), the new size will be 
     *                made equal to the default size.
     */
    public final synchronized void setSize(int newSize) {
	if (defaults != null && newSize < defaults.size()) 
	    newSize = defaults.size();
	if (newSize > elementCount) {
	    ensureCapacity(newSize);
	} else {
	    for (int i = newSize ; i < elementCount ; i++) {
		elementData[i] = null;
	    }
	}
	elementCount = newSize;
    }

    /**
     * Returns an enumeration of the elements. Use the Enumeration methods on
     * the returned object to fetch the elements sequentially.
     */
    public final synchronized Enumeration elements() {
	return new MetavectorEnumerator(this);
    }
    
    
    /**
     * Returns true if the specified object is a value of the 
     * collection.
     * @param elem the desired element
     */
    public final boolean contains(Object elem) {
	return indexOf(elem, 0) >= 0;
    }

    /**
     * Searches for the specified object, starting from the first position
     * and returns an index to it.
     * @param elem the desired element
     * @return the index of the element, or -1 if it was not found.
     */
    public final int indexOf(Object elem) {
	return indexOf(elem, 0);
    }

    /**
     * Searches for the specified object, starting at the specified 
     * position and returns an index to it.
     * @param elem the desired element
     * @param index the index where to start searching
     * @return the index of the element, or -1 if it was not found.
     */
    public final synchronized int indexOf(Object elem, int index) {
	if (index < 0) index = 0;
	for (int i = index ; i < elementCount ; i++) {
	    if (defaults != null && 
		(i <= elementData.length || elementData[i] == null)) {

		// compare object with default value
		if (elem.equals(defaults.elementAt(i))) return i;
	    }
	    else if (elem.equals(elementData[i])) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Searches backwards for the specified object, starting from the last
     * position and returns an index to it. 
     * @param elem the desired element
     * @return the index of the element, or -1 if it was not found.
     */
    public final int lastIndexOf(Object elem) {
	return lastIndexOf(elem, elementCount);
    }

    /**
     * Searches backwards for the specified object, starting from the specified
     * position and returns an index to it. 
     * @param elem the desired element
     * @param index the index where to start searching
     * @return the index of the element, or -1 if it was not found.
     */
    public final synchronized int lastIndexOf(Object elem, int index) {
	if (index >= elementCount) index = elementCount - 1;
	for (int i = index ; --i >= 0 ; ) {
	    if (defaults != null && 
		(i <= elementData.length || elementData[i] == null)) {

		// compare object with default value
		if (elem.equals(defaults.elementAt(i))) return i;
	    }
	    else if (elem.equals(elementData[i])) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Returns the element at the specified index.
     * @param index the index of the desired element
     */
    public final Object elementAt(int index) {
	ValueAndStatus datum = elementRefAt(index, null, false);
	Object val = datum.value;

	// protect the output value if necessary
	if (datum.status == 0) val = protectValue(val);
	return val;
    }

    /**
     * Returns the element at the specified index.
     * @param index the index of the desired element
     * @param defaultValue object to return if the index is out of range or
     *                     if the primary value is null.  This overrides the 
     *                     internally supported defaults.
     */
    public final Object elementAt(int index, Object defaultValue)
    {
	ValueAndStatus datum = elementRefAt(index, defaultValue, true);
	Object val = datum.value;

	// protect the output value if necessary
	if (datum.status == 0) val = protectValue(val);
	return val;
    }

    final static Object protectValue(Object val) {
	if (val instanceof Metavector) {
//	    System.err.println("protecting Metavector...");
	    Metavector v = (Metavector) val;
	    val = new Metavector(v, v.elementCount, v.capacityIncrement);
	}
	else if (val instanceof Metadata) {
//	    System.err.println("protecting Metadata...");
	    Metadata d = (Metadata) val;
	    int sz = d.size();
	    if (sz < 1) sz = 5;
	    val = new Metadata(sz, d);
	}

	return val;
    }

    /**
     * Returns the element at the specified index.  This differs from 
     * ElementAt(String) in that if the returned value is of 
     * meta-container type (Metadata or Metavector), the value is 
     * <em>not</em> wrapped by a protecting meta-container before
     * being returned to the user.
     * @return ValueAndStatus result where the value field is the value
     *                        requested and the status field indicates
     *                        whether the value needs a protective wrapping 
     *                        (yes=0, no=1);
     */
    final synchronized ValueAndStatus elementRefAt(int index, Object def,
						   boolean useDef) {
	int yes = 0, no = 1;
	ValueAndStatus out = new ValueAndStatus(null, no);

	if (index >= elementCount) return out;

	if (index >= elementData.length || elementData[index] == null) {

	    // element not found in primary array; return a default value
	    if (useDef) {
		out.value = def;
	    }
	    else if (defaults != null) {
		out = defaults.elementRefAt(index, null, false);
		out.status = yes;
	    }
	    return out;
	}

	try {
	    out.value = elementData[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    out.value = null;
	}

	// do we need to return the user's default?
	if (out.value == null && useDef) {
	    out.value = def;
	    out.status = no;
	}

	return out;
    }

    /**
     * parse a metadatum name for a Metavector element reference and 
     * (optionally) a sub-metadatum name, assuming a form for the input
     * string of "[n]" or "[n].subname".
     * @return ValueAndStatus  the result of the parsing.  The integer
     *                         value found will appear in the status field
     *                         and the sub-name String in the value field.
     *                         If a element specification is not found,
     *                         status will be < 0 and value will contain
     *                         input string.  If no subname was found,
     *                         value will be null.
     */
    public final static ValueAndStatus parseOutElement(String name) {

	ValueAndStatus out = new ValueAndStatus(name, -1);
	Integer elemidx=null;
	String subkey=null;

	if (name.charAt(0) == '[') {

	    int rbrak = name.indexOf(']', 0);
	    if (rbrak > 0 && rbrak != name.length()-1 && 
		name.charAt(rbrak+1) != '.' && name.charAt(rbrak+1) != '[')
		rbrak = -1; 

	    if (rbrak > 0) {
		try {
		    elemidx = Integer.valueOf(name.substring(1, rbrak));
		    int offs = (rbrak+2 < name.length() && 
				name.charAt(rbrak+1) == '.') ? 2 : 1;
		    if (rbrak < name.length()-1) 
			subkey = name.substring(rbrak+offs);
		} 
		catch (NumberFormatException e) {
		    System.err.println("trouble parsing " + 
				       name.substring(1, name.length()-1));
		    elemidx = null;
		}
	    }
	    if (elemidx != null) {
		out.status = elemidx.intValue();
		out.value = subkey;
	    }
	}

	return out;
    }

    /**
     * if the element at a specified index is a Metadata object, return
     * the value of the metadatum given by the specified name; otherwise
     * return null.  (This is equivalent to calling 
     * <code>mv.getMetadatum(index, name, mv.executeRunners)</code>.)
     */
    public final Object getMetadatum(int index, String name) {
	return getMetadatum(index, name, executeRunners);
    }

    /**
     * if the element at a specified index is a Metadata object, return
     * the value of the metadatum given by the specified name; otherwise
     * return null.
     * @param index  the index of the desired Metadata element
     * @param name   the name of the metadatum to retrieve from the 
     *               Metadata object.
     * @param doRun  true if it is okay to execute a Runnable object 
     *               to obtain the desired metadatum when appropriate.
     */
    public final Object getMetadatum(int index, String name, boolean doRun) {

	ValueAndStatus datum = getMetadatumRef(index, name, null, 
					       false, doRun);
	Object val = datum.value;

	// protect the output value if necessary
	if (datum.status == 0) val = protectValue(val);
	return val;
    }

    /**
     * if the element at a specified index is a Metadata object, return
     * the value of the metadatum given by the specified name; otherwise
     * return null.  This differs from 
     * ElementAt(String) in that if the returned value is of 
     * meta-container type (Metadata or Metavector), the value is 
     * <em>not</em> wrapped by a protecting meta-container before
     * being returned to the user.
     * @param index  the index of the desired Metadata element
     * @param name   the name of the metadatum to retrieve from the 
     *               Metadata object.
     * @param doRun  true if it is okay to execute a Runnable object 
     *               to obtain the desired metadatum when appropriate.
     * @return ValueAndStatus result where the value field is the value
     *                        requested and the status field indicates
     *                        whether the value needs a protective wrapping 
     *                        (yes=0, no=1);
     */
    final synchronized ValueAndStatus getMetadatumRef(int index, String name,
						      Object def, 
						      boolean useDef,
						      boolean doRun) 
    {
	Object use = null;
	int yes = 0, no = 1;
	String orig = name;
	ValueAndStatus out = new ValueAndStatus(null, no);
// 	ValueAndStatus defout = 
// 	    new ValueAndStatus(((useDef) ? def : null), no);
	String subkey = null;

	if (defaults != null && 
	    (index >= elementData.length || elementData[index] == null)) {

	    out = defaults.getMetadatumRef(index, name, def, useDef, doRun);
	    out.status = yes;
	    return out;
	}

	try {
	    use = elementData[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    use = null;
	}
//	if (use == null) return defout;

	// parse out and process any leading "[n]" strings indicating
	// that an element of a submetavector is wanted.
	ValueAndStatus eas = new ValueAndStatus(name, index);
	if (use != null && eas.status >= 0 && eas.value != null) {
	    String old = (String) eas.value;
	    eas = parseOutElement(old);
	    if (eas.status >= 0) {
		if (use instanceof Metavector) {
		    boolean ud = (eas.value == null) ? false : true;
		    ValueAndStatus tmp = 
			((Metavector)use).elementRefAt(eas.status, def, ud);
		    use = tmp.value;
		    if (tmp.status == yes) out.status = yes;
		}
		else {
		    eas.status = -1;
		    eas.value = old;
		}
	    }
	}
	name = (String) eas.value;
//	if (use == null) return defout;

	// Now get the submetadatum
	if (name != null && use != null && use instanceof Metadata) {
	    int doProtect = out.status;
	    out = ((Metadata)use).getMetadatumRef(name, def, useDef, doRun);
	    if (doProtect == yes) out.status = yes;
	}

// 	// We need to make one more attempt to obtain a default value
// 	if (use == null || out.value == null) {
// 	    if (useDef) {
// 		out.value = def;
// 		out.status = no;
// 	    }
// 	    else if (defaults != null) {
// 		out = defaults.getMetadatumRef(index,orig,def,useDef,doRun);
// 		out.status = yes;
// 	    }
// 	}

	// check one last time to see if we should return the user's default
	if ((use == null || out.value == null) && useDef) {
	    out.value = def;
	    out.status = no;
	}

	return out;
    }

    /**
     * Returns the first element of the sequence.
     */
    public final synchronized Object firstElement() {
	if (elementCount == 0) return null;
	return elementAt(0);
    }

    /**
     * Returns the last element of the sequence.
     */
    public final Object lastElement() {
	return elementAt(elementCount-1);
    }

    /**
     * Sets the element at the specified index to be the specified object,
     * growing the array as necessary.
     * @param obj what the element is to be set to
     * @param index the specified index
     * @exception ArrayIndexOutOfBoundsException If the index is < 0
     */
    public final synchronized void setElementAt(Object obj, int index) {
	if (index < 0) {
	    throw new ArrayIndexOutOfBoundsException(index + " < 0");
	}

	if (index >= elementData.length) ensureCapacity(index+1);
	if (index >= elementCount) elementCount = index+1;
	elementData[index] = obj;
    }

    /**
     * Erases and (when possible) deletes the element at the specified 
     * index.  If the index is greater than or equal to the number of 
     * default values, the elements with an index greater than the current 
     * index are moved down; otherwise, the element value is erased, 
     * returning it to its default value.
     * @param index the element to remove
     * @return boolean true, if removal was possible; false, if default
     *                 value exists or index is out of range.
     */
    public final synchronized boolean removeElementAt(int index) {
	if (index < 0 || index >= elementCount) return false;

	if (defaults != null && index < defaults.size()) {
	    eraseElementAt(index);
	    return false;
	}

	int j = elementCount - index - 1;
	if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
	}
	elementCount--;
	elementData[elementCount] = null; /* to let gc do its work */
	return true;
    }

    /** 
     * erases the current value of an element, returning it to its default
     * value.  If the default value was not set at construction, the default
     * is null
     */
    public final synchronized void eraseElementAt(int index) {
	if (index < 0 || index > elementData.length) return;
	elementData[index] = null;
    }

    /**
     * Conditionally inserts the specified object as an element at the 
     * specified index.  If the specified index greater than or equal to 
     * the number of default values, the elements with an index greater 
     * or equal to the current index are shifted up; otherwise, the 
     * insertion is not done and false is returned.
     * @param obj the element to insert
     * @param index where to insert the new element
     * @return boolean true if request insertion was allowed and successful
     */
    public final synchronized boolean insertElementAt(Object obj, int index) {
	if (index < defaults.size()-1) return false;
	if (index >= elementCount) {
	    setElementAt(obj, index);
	    return true;
	}

	ensureCapacity(elementCount + 1);
	System.arraycopy(elementData, index, 
			 elementData, index + 1, elementCount - index);
	elementData[index] = obj;
	elementCount++;
	return true;
    }

    /**
     * Adds the specified object as the last element of the vector.
     * @param obj the element to be added
     */
    public final synchronized void addElement(Object obj) {
	ensureCapacity(elementCount + 1);
	elementData[elementCount++] = obj;
    }

    /**
     * Erases and (when possible) deletes the element from the vector. 
     * If the object occurs more than once, only the first is removed. 
     * If the object is not an element or not deletable, returns false.
     * @param obj the element to be removed
     * @return true if the element was actually removed; false otherwise.
     */
    public final synchronized boolean removeElement(Object obj) {
	int i = indexOf(obj);
	if (i >= 0) return removeElementAt(i);
	return false;
    }

    /**
     * Revert the metavector to its fully default state.
     */
    public final synchronized void removeAllElements() {
	for (int i = 0; i < elementCount; i++) {
	    elementData[i] = null;
	}
	elementCount = (defaults == null) ? 0 : defaults.size();
    }

    /**
     * replace the internal default Metavector with a copy, detaching 
     * control of the defaults from the originator of this Metavector.
     * Users should call this method to prevent the originator from 
     * updating the defaults later.
     */
    public final synchronized void detach() {
	Metavector old = defaults;
	synchronized (old) {
	    int n = old.size();
	    defaults = new Metavector(n);
	    old.copyInto(defaults.elementData);
	    defaults.elementCount = n;
	}
    }

    /**
     * Set the default Metavector of this Metavector object.  The specified
     * defaults replace the one previously in use.  This effectively 
     * "detaches" this Metavector from its originator and "attaches" it to
     * the caller of this method.  If no other object holds a reference to 
     * the old defaults, it will get garbage-collected.
     */
    public synchronized void setDefaults(Metavector to) {
	Metavector old = defaults;
	defaults = to;
	if (defaults == null) {
	    int oldsz = old.size();
	    if (elementCount <= oldsz) {
		int end = (elementData.length < oldsz) ? 
		    elementData.length : oldsz;
		int i;
		for(i=end-1; i >= 0 && elementData[i] == null; i--);
		elementCount = i + 1;
	    }
	}
	else {
	    int defsz = defaults.size();
	    elementCount = (elementData.length > defsz) ? 
		elementData.length : defsz;
	}
    }

    /**
     * Copies the elements of this vector into the specified array.
     * @param anArray the array where elements get copied into
     */
    public final synchronized void copyInto(Object anArray[]) {
	int i = elementCount;
	while (i-- > 0) {
	    anArray[i] = elementAt(i);
	}
    }

    /**
     * Copies the elements of this Metavector into another Metavector
     * @param that   the Metavector to copy into
     */
    public final synchronized void copyInto(Metavector that) {
	int i = elementCount;
	that.ensureCapacity(i);
	while (i-- > 0) {
	    that.setElementAt(elementAt(i), i);
	}
    }

    /**
     * Create a java.util.Vector object containing a shallow copy of 
     * the data in this metavector object.
     */
    public final synchronized Vector copyIntoVector() {
	Vector out = new Vector(elementCount, capacityIncrement);
	for(int i=0; i < elementCount; i++) {
	    out.setElementAt(elementAt(i), i);
	}
	return out;
    }
	
    /**
     * Clones this metavector. The elements are <strong>not</strong> cloned,
     * and the defaults are still in control of this metavector's originator.
     */
    public synchronized Object clone() {
	try { 
	    Metavector v = (Metavector)super.clone();
	    int copyCount = (elementCount > elementData.length) ? 
		elementCount : elementData.length;
	    v.elementData = new Object[copyCount];
	    System.arraycopy(elementData, 0, v.elementData, 0, copyCount);
	    return v;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    /**
     * Like clone(), except that a copy of the defaults metavector is also
     * made.  This provides a fully metavector object that is fully detached
     * from the originator of this metavector; that is, the originator
     * can not change the defaults of the new metavector.  The elements 
     * within both the primary and default arrays are <strong>not</strong> 
     * cloned.
     */
    public synchronized Metavector detachedClone() {
	return (Metavector) deepClone();
    }

    /** 
     * clone the defaults metavector.  The resulting metavector will not 
     * have a defaults list of its own and, therefore, will be detached 
     * from the control of other objects.  The elements within both the
     * primary and default arrays are <strong>not</strong> cloned.
     */
    public synchronized Metavector cloneDefaults() {
	return defaults.deepClone();
    }
	
    /**
     * create a semi-deep clone.  This method is like clone(), except that 
     * the Metadata and Metavector objects contained within are replaced 
     * with their own clones (using their respecitive deepClone() methods).
     */
    public final synchronized Metavector deepClone() {
	Metavector out = (Metavector) clone();
	int sz = out.size();
	for(int i=0; i < sz; i++) {

	    Object item = out.elementAt(i);

	    if (item instanceof Metadata) {
		item = ((Metadata)item).deepClone(); 
		out.setElementAt(item, i);
	    }
	    else if (item instanceof Metavector) {
		item = ((Metavector)item).deepClone(); 
		out.setElementAt(item, i);
	    }
	}

	return out;
    }	

    /**
     * Converts the vector to a string. Useful for debugging.
     */
    public final synchronized String toString() {
	int max = size() - 1;
	StringBuffer buf = new StringBuffer();
	buf.append("[");

	for (int i = 0 ; i <= max ; i++) {
	    ValueAndStatus vas = elementRefAt(i, null, false);
	    Object o = vas.value;
	    String s = (o == null) ? "(null)" : o.toString();
	    buf.append(s);
	    if (i < max) {
		buf.append(", ");
		if (o instanceof Metadata || o instanceof Metavector)
		    buf.append("\n");
	    }
	}
	buf.append("]");
	return buf.toString();
    }
}

final 
class MetavectorEnumerator implements Enumeration {
    Metavector vector;
    int count;

    MetavectorEnumerator(Metavector v) {
	vector = v;
	count = 0;
    }

    public boolean hasMoreElements() {
	return count < vector.elementCount;
    }

    public Object nextElement() {
	synchronized (vector) {
	    if (count < vector.elementCount) {
		return vector.elementAt(count++);
	    }
	}
	throw new NoSuchElementException("MetavectorEnumerator");
    }

}

