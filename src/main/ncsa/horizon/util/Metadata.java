/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996-7, Board of Trustees of the University of Illinois
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
 * This software uses code found in Sun's version of Properties.java; thus,
 * be aware of its copyright:
 * 
 * @(#)Properties.java	1.21 95/12/15 Arthur van Hoff
 *
 * Copyright (c) 1994 Sun Microsystems, Inc. All Rights Reserved.
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
 *  96       rlp  Original version adapted from java.util.Properties
 *  97jan18  rlp  Added static copyMetadata(Hashtable), copyMetadata(Metadata),
 *                giveDefaults(Metadata), methods; expanded documentation
 *  97apr    rlp  major overhaul to support Metavector and Metarunner classes
 *  97jun18  rlp  added support of "schema" and "schemaVersion" metadata
 *  98feb02  rlp  fixed bug getting sub-metadata
 */
package ncsa.horizon.util;

import java.util.*;

/**
 * a hashtable where keys are String objects and values are Object objects.
 * Like java.util.Properties, it contains an internal default value list. 
 * Data contained within Metadata within Metadata are directly accessible.
 * Also supported is the transparent loading of data into the hashtable only 
 * at the point when the user first requests them.  <p>
 *
 * Within the Horizon package, Metadata objects hold data that describe other
 * data.  For example, image data sets can have metadata associated with them
 * that can describe things like the number of dimensions it has, the origin 
 * of the data, etc; this can information can usually be accessed through
 * <a href="ncsa.horizon.viewable.Viewable.html">Viewable</a> method, 
 * <a href="ncsa.horizon.viewable.Viewable.html#getMetadata()">
 * getMetadata()</a>.  
 * <a href="ncsa.horizon.coordinates.CoordinateSystem.html">
 * CoordinateSystem</a> objects also have metadata associated with them that
 * provide the parameters that describe the system. <p>
 *
 * Users of this class should remember that metadata have an assumed type that
 * is implicitly agreed upon by all classes that access it.  For example, 
 * classes in <a href="Package-ncsa.horizon.coordinates.html">
 * ncsa.horizon.coordinates</a> package assume that the metadatum named 
 * "naxes" to be of type Integer.  If a class finds that a metadatum has
 * an unexpected type, the class may throw a 
 * <a href="ncsa.horizon.util.MetadataTypeException.html">
 * MetadataTypeException</a>.  (See 
 * <a href="ncsa.horizon.coordinates.CoordMetadata.html">
 * CoordMetadata</a> for more details about coordinate-related metadata.) <p>
 *
 * The only pre-defined metadata names are "schema" and "schemaVersion".
 * The "schema" metadatum is a String that identifies the set of metadata
 * names in use by this Metadata object, along with the types and logical
 * meaning of those names.  The "schemaVersion" is also a String to identify
 * the version of the schema.  <p>
 *
 * <b> Accessing Hierarchical Metadata </b><p>
 *
 * Each piece of Metadata can be accessed through its name (often referred
 * to as its keyword), represented as a String, using the 
 * <a href="#getMetadatum(java.lang.String)">getMetadatum(String)</a> 
 * method.  A metadatum value is set using the 
 * <a href="java.util.Hashtable.html#put(java.lang.Object, java.lang.Object)">
 * put</a> method (from the super-class, <a href="java.util.Hashtable.html">
 * Hashtable</a>). <p>
 *
 * Special support is provided for hierarchical metadata.  For example, a 
 * Metadata object can contain another Metadata object.  It can also contain 
 * one or more <a href="ncsa.horizon.Metavector.html">Metavector</a> objects,
 * each one containing an array of metadata of the same type.  The Metadata
 * class supports this notion by allowing one to request "sub-metadata" 
 * directly with the <a href="#getMetadatum(java.lang.String)">
 * getMetadatum(String key)</a> method.  If the key string contains a period
 * ("."), the substring to the left of the period is taken to be the name 
 * of a metadatum of type Metadata; the substring to the left of the period
 * is taken to be the name of a metadatum contained within that metadata 
 * object.  For example, the following code, 
 * <pre>
 *     Metadata md;
 *     ...
 *     Metadata cmd = (Metadata) md.getMetadatum("coordinates");
 *     String name = (String) cmd.getMetadatum("name");
 * </pre>
 * is equivalent to:
 * <pre>
 *     String name = (String) md.getMetadatum("coordinates.name")
 * </pre> 
 * In fact, the latter is actually safer and more efficient.  
 * 
 * Similarly, if a Metadata object contains a Metavector object as one of 
 * its items, one can access an element of the Metavector by appending 
 * "<code>[<em>n</em>]</code>" to the name of the Metavector, where 
 * <em>n</em> is an integer specifying the desired array index.  For example, 
 * <pre>
 *     Object element = md.getMetadatum("Axes[1]");
 * </pre> 
 * returns the object at index 1 (the second element) of the Metavector 
 * called "Axes".  <p>
 * 
 * Use the "sub-metadata" syntax, one can directly access hierarhical 
 * metadata of arbitrary depth.  For example, the following is the most 
 * efficient way (in terms of memory and time) of access the specified 
 * sub-metadatum:
 * <pre>
 *     String name = (String) md.getMetadatum("coordinates.Axes[1].name");
 * </pre> <p>
 *
 * Note that one <em>cannot</em> use the "sub-metadata" syntax to update
 * hierarchical metadata.  <p>
 *
 * <b> Default Metadata </b><p>
 *
 * Metadata objects can store two sets of data within themselves: primary
 * data and default data.  Calls to <a href="#getMetadatum(java.lang.String)">
 * getMetadatum(String)</a> will return requested data from the primary set;
 * if it does not exist there, the value in the default searched for a value.
 * (To prevent search of the default set, one should call 
 * <a href="#getMetadatum(java.lang.String, java.lang.Object)">
 * getMetadatum(String, null)</a>.)  The default data can only be set during 
 * construction.  Primary data may be updated via the 
 * <a href="java.util.Hashtable.html#put(java.lang.Object, java.lang.Object)">
 * put</a> and <a href="#copyMetadata(java.util.Hashtable)">
 * copyMetadata(Metadata)</a> methods.
 *
 * The internally stored default metadata provides a way for managers of
 * metadata to protect their data from updates by other objects.  If a 
 * metadata manager has a Metadata it wants other objects to read from 
 * but not write into, it can create a new Metadata object that has the 
 * original as the default.  The other users of the new Metadata can then
 * read the elements and even override them without affecting the original
 * Metadata object.  <p>
 *
 * One other feature of the support for default values is that it allows
 * the originator of Metadata (the object that has a reference to the 
 * default Metadata) to update the defaults after the Metadata has been 
 * created and passed on.  Such changes would be seen by those objects that
 * have not overriden the defaults.  Client objects that prefer that their 
 * copy of the Metadata not be connected the originator in this way can 
 * "detach" it via the <a href="#detach()">detach()</a> method.  Other 
 * methods are provided for obtaining "detached" copies of the data, 
 * <a href="#detachedClone()">detachedClone()</a>, and 
 * <a href="#cloneDefaults()">cloneDefaults()</a>.  (Note that the standard
 * <a href="#clone()">clone()</a> produces a Metavector whose defaults are 
 * "attached" to the same object as the original.)  <p>
 *
 * One should note that the defaults protection only applies to the internal 
 * default Metadata container itself, not its elements.  That is, the value
 * returned by <a href="#getMetadatum(String)">getMetadatum(String)</a> is the 
 * original version of the datum and not a copy.  Thus, there may be any number
 * of references to the datum held by other objects.  (The datum is protected 
 * only if it is of a type that is not internally editable.  For example, the 
 * value of an Integer or String object cannot be updated; however, if the 
 * datum is of type Stack, the data internal to that Stack is not protected 
 * from updates.)  An exception to this is when the value is obtained from the 
 * default list and is of type Metavector or Metadata; in this case, the 
 * meta-container is placed as the default list in a new meta-container before 
 * being passed on.  <p>
 *
 * <b> Dynamic Metadata Loading </b><p>
 * 
 * Some metadata may be costly to load into memory, for example, if it needs 
 * to be downloaded from the network or it requires an expensive computation.
 * The effort to load the data into a Metadata object may be wasted if the 
 * user never requests the data.  The Metadata class provides a mechanism 
 * for one to store <em>procedures</em>, in the form of 
 * <a href="ncsa.horizon.util.Metarunner.html">Metarunner</a> objects, for 
 * obtaining metadata.  When a user requests a metadatum object for which 
 * there exists a <a href="ncsa.horizon.util.Metarunner.html">Metarunner</a> 
 * object, the <a href="ncsa.horizon.util.Metarunner.html">Metarunner</a> is
 * executed and the resulting value is returned to the user as well as loaded 
 * into the hashtable for future requests.  <p>
 *
 * To make use of this capability, programmers should sub-class the 
 * <a href="ncsa.horizon.util.Metarunner.html">Metarunner</a> class, 
 * overridding the getDatum() method.  The Metarunner object is then stored
 * in the Metadata object using the name of metadatum appended with the 
 * String <a href="#METARUNNER_TAG">Metadata.METARUNNER_TAG</a>.  When the
 * user later requests a named metadatum, the Metadata object first looks 
 * for a static value in its hashtable.  If it does not exist, then the 
 * Metadata object looks for a Metarunner object that can obtain the value.
 * Failing that, the default list is consulted (which might also engage
 * a Metarunner object).  If the Metarunner does exist and successfully 
 * returns a value, the value is stored in the primary hastable as a static
 * value.  <p>
 *
 * Support for Metarunner objects is meant by default to be transparent; the
 * user is given some explicit control and access to the objects.  To prevent
 * the execution, one can set the public boolean field, 
 * <a href="#executeRunners">executeRunners</a> to false.  The 
 * <a href="#getRunnerNames()">getRunnerNames()</a> method returns the names
 * of metadata (without the METARUNNER_TAG appended) that have Metarunners
 * associated with them.  In contrast, the 
 * <a href="#getMetadatumNames()">MetadatumNames()</a> returns the only 
 * the names for which there currently exists static values (including the 
 * modified names pointing to Metarunner objects).  Since the latter method 
 * is expected to be used to step through each metadata value (say to copy 
 * it to another container), this behavior prevents the unintended execution 
 * of costly Metarunners.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: Metadata.java,v 0.7 1998/02/03 03:57:59 rplante Exp $
 */
public class Metadata extends Hashtable {

    public final static String schema        = "schema";
    public final static String schemaVersion = "schemaVersion";

    /**
     * the suffix to be appended to a metadatum name to refer to 
     * a Metarunnable object that can fetch the value of that metadatum.
     */
    public final static String METARUNNER_TAG = ":Metarunner";

    /**
     * the default Metadata values
     */
    protected Metadata defaults;

    /**
     * true if Runnable objects should be executed to obtain values
     * for requested metadata when available.  
     */
    public boolean executeRunners = true;

    /**
     * Creates an empty metadatum list.
     */
    public Metadata() {
        this(20, null);
    }

    /**
     * Creates an empty metadatum list with specified defaults.
     * @param defaults the defaults
     */
    public Metadata(Metadata defaults) {
	this(20, defaults);
    }

    /**
     * Creates an empty metadatum list with an initial capacity
     */
    public Metadata(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Creates an empty metadatum list with an initial capacity and 
     * specified defaults.
     */
    public Metadata(int initialCapacity, Metadata defaults) {
	super(initialCapacity);
        this.defaults = defaults;
    }

/*------------------------------------------------------------------------
 * Methods for setting certain metadata
 *------------------------------------------------------------------------*/

    /**
     * set the name of the schema used by this Metadata object
     */
    public void setSchema(String in) {  super.put(schema, in); }

    /**
     * set the version of the schema used by this Metadata object
     */
    public void setSchemaVersion(String in) {  super.put(schemaVersion, in); }

/*------------------------------------------------------------------------
 * Methods for obtaining values
 *------------------------------------------------------------------------*/

    /**
     * Gets a metadatum with the specified key. If the key is not 
     * found in this metadatum list, it tries the defaults. This method 
     * returns null if the metadatum is not found.
     * @param key the metadatum name
     */
    public Object getMetadatum(String key) {
	return getMetadatum(key, executeRunners);
    }

    /**
     * Gets a metadatum with the specified key. If the key is not 
     * found in this metadatum list, it tries the defaults. This method 
     * returns null if the metadatum is not found.
     * @param key the metadatum name
     * @param doRun if true, a Runnable object should be executed to 
     *              obtain the requested value when available; overrides
     *              the value of executeRunners
     */
    public Object getMetadatum(String key, boolean doRun) {
	ValueAndStatus datum = getMetadatumRef(key, null, false, doRun);
	Object val = datum.value;

	// protect the output value if necessary
	if (datum.status == 0) val = protectValue(val);
	return val;
    }


    /**
     * Gets a metadatum with the specified key and default. 
     * This method returns defaultValue if the metadatum is not found.
     */
    public Object getMetadatum(String key, Object defaultValue) {
	return getMetadatum(key, defaultValue, executeRunners);
    }

    /**
     * Gets a metadatum with the specified key and default. 
     * This method returns defaultValue if the metadatum is not found.
     */
    public Object getMetadatum(String key, Object defaultValue, boolean doRun)
    {
	ValueAndStatus datum = getMetadatumRef(key, defaultValue, true, doRun);
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
     * Gets the metadatum with the specified key.  This differs from 
     * getMetadatum(String) in that if the returned value is of 
     * meta-container type (Metadata or Metavector), the value is 
     * <em>not</em> wrapped by a protecting meta-container before
     * being returned to the user.
     */
    ValueAndStatus getMetadatumRef(String key, Object def, boolean useDef,
				   boolean doRun) 
    {
	String orig = key;
	int yes = 0, no = 1;
	ValueAndStatus out = new ValueAndStatus(null, no);

	// determine if a metavector element has been requested
	Integer elemidx = null;
	String subkey = null;
	int lbrak = key.indexOf('[');
	if (lbrak >= 0) {

	    ValueAndStatus eas = 
		Metavector.parseOutElement(key.substring(lbrak));

	    if (eas.status >= 0) {
		subkey = (String) eas.value;
		key = key.substring(0, lbrak);
		elemidx = new Integer(eas.status);
	    }
	}

	// determine if a sub-metadatum has been requested
	if (subkey == null) {
	    int dot = key.indexOf('.');
	    if (dot >= 0) {
		subkey = (dot >= key.length()) ? 
		    new String() : key.substring(dot+1);
		key = key.substring(0, dot);
	    }
	}

	// look for a value in the primary hashtable
        out.value = super.get(key);

	// look for a Runnable object that can fetch the value
	if (out.value == null && doRun) {
	    String use = key + METARUNNER_TAG;
	    Metarunner fetcher;
	    try {
		fetcher = (Metarunner) super.get(use);
		if (fetcher != null) out.value = fetchDatum(key, fetcher);
	    } catch (ClassCastException e) { }
	}

 	// look for a default value if necessary
 	if (out.value == null) {
 	    if (elemidx == null && useDef) {
 		return new ValueAndStatus(def, no);  
 	    }
 	    else if (defaults != null) {
 		out = defaults.getMetadatumRef(orig, def, useDef, doRun);
 		out.status = yes;
		return out;
 	    }
 	}

	// obtain metavector element
	if (elemidx != null && out.value != null) {
	    int idx = elemidx.intValue();
	    if (out.value instanceof Metavector) {

		// ought to be a Metavector
		Metavector use = (Metavector) out.value;
 		if (subkey != null) {

 		    // if we are looking for a sub-metadatum, use this more
 		    // efficient method
 		    out = use.getMetadatumRef(idx, subkey, def, useDef, doRun);
 		    subkey = null;
 		}
 		else {
		    int doProtect = out.status;
		    out = use.elementRefAt(idx, null, false);
		    if (doProtect == yes) out.status = yes;
		}
	    }
	    else if (idx != 0) {

		// if it's not a Metavector, we'll let it slide as long as
		// the requested index was 0; otherwise, it does not logically 
		// exist.
		out.value = null;
	    }
	}

	// obtain sub-metadatum value
	if (subkey != null && out.value != null) {
	    if (out.value instanceof Metadata) {

		// ought to be the case
		Metadata use = (Metadata) out.value;
		int doProtect = out.status;
		out = use.getMetadatumRef(subkey, def, useDef, doRun);
		if (doProtect == yes) out.status = yes;
	    }
	    else {
		out.value = null;
	    }
	}

	// check one last time to see if we should return the user's default
	if (out.value == null && useDef) {
	    out.value = def;
	    out.status = no;
	}

	return out;
    }

/*------------------------------------------------------------------------
 * Support for Runnable objects that fetch data
 *------------------------------------------------------------------------*/

    /**
     * Execute a Metarunner that fetches the value of a specifed key
     * and stores the result into this Metadata list
     * @param key      name of metadatum that the Metarunner will fetch.
     * @param fetcher  the Metarunner object to execute
     */
    protected Object fetchDatum(String key, Metarunner fetcher) {
	Object out = Metarunner.fetchDatum(fetcher);
	if (fetcher.getErrorState() == Metarunner.OK) 
	    put(key, out);
	return out;
    }


    /**
     * Enumerate the keys having Metarunner objects associated 
     * with them
     */
    public synchronized Enumeration getRunnerNames() {
	Hashtable h = new Hashtable();

	// get names from defaults list that have runners
	if (defaults != null) {
	    defaults.enumerateRunners(h);
	}

	// exclude default runners for which a real value exists in the 
	// primary list
	for(Enumeration e = h.keys() ; e.hasMoreElements() ;) {
	    String key = (String)e.nextElement();
	    if (super.get(key) != null) h.remove(key);
	}

	// get names from primary list that have runners
	enumerateRunners(h);
	return h.keys();
    }

    private synchronized void enumerateRunners(Hashtable h) {
        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
	    if (key.endsWith(METARUNNER_TAG)) {
		int i = key.lastIndexOf(METARUNNER_TAG);
		key = key.substring(0, i);
		h.put(key, null);
	    }
	}
    }	

    /**
     * execute the runner (if it exists and is accessible) for the 
     * specified key, and return its value.  The static value will be 
     * updated only if it does not exist or is equal to null.  Runners
     * from the default list are inaccessible if a static value or a 
     * runner exists in the primary array.  If a runner does not exist
     * or is inaccessible, the result is equivalent to getMetadatum();
     */
    public Object fetchMetadatum(String key) {
	Object old;
	String use = new String(key + METARUNNER_TAG);
	Metarunner fetcher;
	try {  fetcher = (Metarunner) super.get(use); }
	catch (ClassCastException e) { fetcher = null; }

	// if no runner exists, return primary static value, if available.
	// If that produces nothing, try a runner from the defaults list
	if (fetcher == null) {
	    old = getMetadatum(key);
	    if (old != null) return old;
	    return defaults.fetchMetadatum(key);
	}

	// execute the runner and obtain the value produced; update the 
	// primary static value if necessary
	Object out = Metarunner.fetchDatum(fetcher);
	int stat = fetcher.getErrorState();
	if (stat == Metarunner.OK && super.get(key) == null) put(key, out);
	if (stat > Metarunner.RUN_PROBLEM) out = null;

	// if we still don't have a value, get what ever the defaults 
	// list can provide
	if (out == null) out = defaults.getMetadatum(key);
	return out;
    }
	    

/*------------------------------------------------------------------------
 * methods for copying contents to and from other objects
 *------------------------------------------------------------------------*/

    /**
     * Copy all the data from a Hashtable whose keys are of type 
     * String into the primary data list.  Data with non-String keys 
     * are ignored.
     */
    public void copyMetadata(Hashtable from) {
        for (Enumeration e = from.keys() ; e.hasMoreElements() ;) {
            Object key = e.nextElement();
	    if (key instanceof String) put(key, from.get(key));
        }
    }

    /**
     * Copy all the data from another Metadata object into the primary data
     * list.  (Data with non-String keys are ignored.)  Metarunner objects
     * are copied without being executed.
     */
    public void copyMetadata(Metadata from) {
        for (Enumeration e = from.metadatumNames() ; e.hasMoreElements() ;) {
            Object key = e.nextElement();
	    if (key instanceof String) put(key, from.get(key));
        }
    }

    /**
     * Set the default Metadata of this Metadata object.  The specified
     * defaults replace the one previously in use.  This effectively 
     * "detaches" this Metadata from its originator and "attaches" it to
     * the caller of this method.  If no other object holds a reference to 
     * the old defaults, it will get garbage-collected.
     */
    public synchronized void setDefaults(Metadata to) {
	defaults = to;
    }

    /**
     * return a new Metadata object with the same default data but with
     * no primary data
     */
    public Metadata getDefaults() {
	return new Metadata(defaults);
    }

    /**
     * replace the internal default Metadata with a copy (via deepClone()), 
     * detaching control of the defaults from the originator of this 
     * Metadata.  Users should call this method to prevent the originator 
     * from updating the defaults later.
     */
    public final synchronized void detach() {
	Metadata old = defaults;
	defaults = old.deepClone();
    }

    // remove this method
    /**
     * Copy the default metadata of this Metadata object to the 
     * defaults of another Metadata object.  Most applications should not 
     * need to call this method; in fact, they should avoid it.  Because
     * the copy is shallow, it opens the possibility for the data internal
     * to the items held within the default list to become corrupted
     * @param to the other Metadata object to copy defaults to
     */
    public void giveDefaults(Metadata to) {
	if (defaults != null) {
	    if (to.defaults == null) to.defaults = new Metadata();
	    to.defaults.copyMetadata(defaults);
	}
    }

    /**
     * remove the primary key/value pairs from this Metadata object, 
     * leaving the defaults.
     */
    public void removeAllMetadata() {
        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            Object key = e.nextElement();
            remove(key);
        }
    }

    /**
     * Enumerates all the keys.
     */
    public Enumeration metadatumNames() {
        Hashtable h = new Hashtable();
        enumerate(h);
        return h.keys();
    }

    /**
     * Enumerates all keys into the specified hastable.
     * @param h the hashtable
     */
    private synchronized void enumerate(Hashtable h) {

        if (defaults != null) {
            defaults.enumerate(h);
        }
        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
// 	    if (key.endsWith(METARUNNER_TAG)) {
// 		int i = key.lastIndexOf(METARUNNER_TAG);
// 		key = key.substring(0, i);
// 	    }
            h.put(key, super.get(key));
        }
    }

    /**
     * attempt to convert contents to a multi-line String (useful for 
     * debugging
     */
    public String toString() {
	String key;
	Object val;
	StringBuffer out = new StringBuffer();

	out.append("==============================\n");
	out.append("Primary data:\n");
	for(Enumeration e = keys() ; e.hasMoreElements() ;) {
	    key = (String) e.nextElement();
	    val = super.get(key);
	    out.append("  " + key + ": ");
	    if (val instanceof Metadata || val instanceof Metavector) {
		StringTokenizer more = new StringTokenizer(val.toString(), 
							   "\n");
		int pl = key.length() + 4;
		StringBuffer pre = new StringBuffer(pl);
		for(int j=0; j < pl; j++) pre.append(" ");
		String line = (String) more.nextElement();
		out.append(line + "\n");
		while (more.hasMoreElements()) {
		    line = (String) more.nextElement();
		    out.append(pre + line + "\n");
		}
	    }
	    else {
		out.append(val + "\n");
	    }
	}
	if (defaults != null) {
	    out.append("Default data:\n");
	    StringTokenizer more = new StringTokenizer(defaults.toString(), 
						       "\n");
	    while (more.hasMoreElements()) {
		key = (String) more.nextElement();
		out.append("  " + key + "\n");
	    }
	}
	else {
	    out.append("No default data\n");
	}
//	out.append("==============================");
	out.append("==============================");

	return out.toString();
    }


/*------------------------------------------------------------------------
 * methods for copying contents to and from other objects
 *------------------------------------------------------------------------*/

    /**
     * Clones this metadata object. The data themselves are 
     * <strong>not</strong> cloned, and the defaults are still in control 
     * of this metadata's originator.
     */
    public Object clone() {
	return super.clone();
    }

    /**
     * Like clone(), except that a copy of the defaults metadata is also
     * made.  This provides a fully metavector object that is fully detached
     * from the originator of this metadata object; that is, the originator
     * can not change the defaults of the new metadata.  The elements 
     * within both the primary and default arrays are <strong>not</strong> 
     * cloned.
     */
    public Metadata detachedClone() {
	Metadata out = (Metadata) clone();
	out.detach();
	return out;
    }

    /**
     * clone the defaults metadata.  The resulting metadata object will not 
     * have a defaults list of its own and, therefore, will be detached 
     * from the control of other objects.  The data held within both the
     * primary and default hashtables are <strong>not</strong> cloned.
     */
    public Metadata cloneDefaults() {
	return defaults.deepClone();
    }

    /**
     * create a semi-deep clone.  This method is like clone(), except that 
     * the Metadata and Metavector objects contained within are replaced 
     * with their own clones (using their respecitive deepClone() methods).
     */
    public synchronized Metadata deepClone() {
	Metadata out = (Metadata) clone();
	Enumeration e = out.metadatumNames();
	while (e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    Object item = out.getMetadatum(key);

	    if (item instanceof Metadata) {
		item = ((Metadata)item).deepClone(); 
		out.put(key, item);
	    }
	    else if (item instanceof Metavector) {
		item = ((Metavector)item).deepClone(); 
		out.put(key, item);
	    }
	}

	return out;
    }
}	
    
