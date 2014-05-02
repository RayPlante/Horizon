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
 *  96       rlp  Original version;
 *  97jan20  rlp  updated init() to adjust refpixel by the value of the 
 *             	  refoffset metadatum;  updated cloneMetadata() to use
 *             	  CoordMetadata.cloneMetadata(); changed getConvertedMetadata()
 *             	  to getMetadata()
 *  97mar26  rlp  updated for new coordinate system class design; no longer
 *                an abstract class.
 *  97jul15  rlp  added hasChanged() call to setUsingTransforms().
 */
package ncsa.horizon.coordinates;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.util.Voxel;
import ncsa.horizon.coordinates.formatters.GenericAxisPosFormatter;
import ncsa.horizon.coordinates.formatters.HHMMSSAxisPosFormatter;
import ncsa.horizon.coordinates.formatters.DDMMSSAxisPosFormatter;
import ncsa.horizon.coordinates.CoordPos;
import java.util.*;

/**
 * a description of a world coordinate system and the mapping between 
 * it and a multidimensional gridded dataset.  <p>
 *
 * <b>General Description</b><p>
 *
 * A CoordinateSystem object can be given a data voxel location and return
 * a position in its world coordinate space in the form of a CoordPos
 * object.  The code might look something like this:
 * <pre>
 *    CoordinateSystem wcs;
 *    CoordPos wcspos;
 *    ...
 *    double datapos[] = { 0, 0, 0 };     // a position in the dataset
 *    wcspos = wcs.getCoordPos(datapos);  // the position in coordinate space
 *
 *    System.out.println( wcspos.getAxisLabel(0) +     // print position
 *                        ": " +                       //  along 1st axis
 *                        wcspos.getValueString(0) );     
 *    System.out.println( wcspos.getAxisLabel(1) +     // print position
 *                        ": " +                       //  along 2nd axis
 *                        wcspos.getValueString(1) );     
 * </pre>
 * which might produce something the following:
 * <pre>
 *    RA: 12:45:39.11
 *    Dec: -30:17:20.2
 * </pre>
 *
 * The getDataLocation() and getVoxel() do the opposite transform of 
 * getCoordPos(), translating a coordinate positions into voxel locations 
 * in the dataset. <p>
 *
 * A CoordinateSystem object can automatically convert positions to other
 * coordinate systems by attaching one or more CoordTransform objects to 
 * it with attachTransform(); all subsequent calls to getCoordPos will produce 
 * positions in the new coordinate system. <p>
 *
 * <b>Constructing a CoordinateSystem</b>
 *
 * Usually, an application programmer would not need to explicitly construct 
 * CoordinateSystem objects; instead, such an object can be obtained by the
 * <a href="ncsa.horizon.viewable.Viewable.html">Viewable</a> method, 
 * getCoordinateSystem() which is all set up appropriately for the Viewable
 * dataset.  
 * 
 */

public class CoordinateSystem extends Observable implements Cloneable, 
    Observer 
{

    protected int naxes=0;
    protected int nnataxes=0;
    protected boolean usingTransforms = true;
    protected Metadata cmdata;
    protected Metadata modmdata;
    protected String[] labels, natlabs, modlabs;
    protected static GenericAxisPosFormatter defPosPrinter = 
                                                new GenericAxisPosFormatter();

    protected Stack transforms;
//    protected Stack constraints;
    protected Stack transmaps;
    protected Stack transdirs;
    protected int gluedTrans = 0;

    /**
     * Construct a CoordinateSystem object
     * @param naxes the number of axes in this system
     * @param md    the metadata that describes this system; note that no
     *              transforms are defined based on values stored in this list
     */
    public CoordinateSystem(int naxes, Metadata md) {
	init(naxes, md, null, null);
    }
	
    /**
     * Construct a CoordinateSystem object
     * @param naxes the number of axes in this system
     */
    public CoordinateSystem(int naxes) {
	init(naxes, null, null, null);
    }
	
    /**
     * Construct a CoordinateSystem object
     * @param md  the metadata that describes this system; note that no
     *            transforms are defined based on values stored in this list
     */
    public CoordinateSystem(Metadata md) {
	Integer nax;
	int ival;
        try {
	    nax = (Integer) md.getMetadatum("naxes");
	} catch (ClassCastException e) {
	    nax = null;
	}
	if (nax == null) 
	    ival = 0;
	else
	    ival = nax.intValue();

	init(ival, md, null, null);
    }
	
    /**
     * Construct a CoordinateSystem object with 2 default axes
     */
    public CoordinateSystem() {
	init(2, null, null, null);
    }

    /**
     * Construct a CoordinateSystem using the specified transforms
     * @param naxes        the number of axes
     * @param md           the metadata describing this system (after
     *                     any default transforms are applied)
     * @param trans        the CoordTransform objects to apply by default,
     *                     or null if none should be applied.  The transforms
     *                     should appear in this Vector in the order that
     *                     they should be applied.  The Vector should not 
     *                     contain any empty elements.
     * @param constraints  the CoordTransformConstraints with which the 
     *                     trans list should be applied, or null if 
     *                     trans=null.  The size of this Vector should be 
     *                     equal to the size of trans and also should not 
     *                     contain any empty elements.
     * @exception IllegalArgumentException thrown if either the trans or 
     *                     constraints Stack has an empty member or member 
     *                     of the wrong type.
     */
    public CoordinateSystem(int naxes, Metadata md, CoordTransform trans, 
			    CoordTransformConstraints constraints) 
	throws IllegalArgumentException
    {
	Vector transv = null, consv = null;
	if (trans != null) {
	    transv = new Vector(1); 
	    consv = new Vector(1); 
	    transv.addElement(trans);
	    consv.addElement(constraints);
	}

	init(naxes, md, transv, consv);
    }
	
    /**
     * Construct a CoordinateSystem using the specified transforms
     * @param naxes        the number of axes
     * @param md           the metadata describing this system (after
     *                     any default transforms are applied)
     * @param trans        a CoordTransform object to apply by default,
     *                     or null if none should be applied.  
     * @param constraints  the CoordTransformConstraints with which the 
     *                     transform should be applied, or null if 
     *                     trans=null.  
     * @exception IllegalArgumentException thrown if either the trans or 
     *                     constraints Stack has an empty member or member 
     *                     of the wrong type.
     */
    public CoordinateSystem(int naxes, Metadata md, Vector trans, 
			    Vector constraints) 
	throws IllegalArgumentException
    {
	init(naxes, md, trans, constraints);
    }
	
    /**
     * initialize the internal data of this object
     * @param naxes        the number of axes
     * @param md           the metadata describing this system (after
     *                     any default transforms are applied)
     * @param trans        the CoordTransform objects to apply by default,
     *                     or null if none should be applied.  The transforms
     *                     should appear in this Vector in the order that
     *                     they should be applied.  The Vector should not 
     *                     contain any empty elements.
     * @param constraints  the CoordTransformConstraints with which the 
     *                     trans list should be applied, or null if 
     *                     trans=null.  The size of this Vector should be 
     *                     equal to the size of trans and also should not 
     *                     contain any empty elements.
     * @exception IllegalArgumentException thrown if either the trans or 
     *                     constraints Stack has an empty member or member 
     *                     of the wrong type.
     */
    private void init(int naxes, Metadata md, Vector trans, 
		      Vector constraints) 
	throws IllegalArgumentException
    {
	if (naxes <= 0) 
	    throw new IllegalArgumentException("naxes = " + naxes + " <= 0");

	this.naxes = naxes;
	nnataxes = naxes;
	if (md == null) 
	    cmdata = new CoordMetadata(naxes);
	else 
	    cmdata = md.deepClone();
	cmdata.put("naxes", new Integer(naxes));
	modmdata = cmdata;

	// set the default labels
	natlabs = new String[naxes];
	for(int i=0; i < naxes; i++) {
	    try {
		natlabs[i] = (String) 
		    cmdata.getMetadatum("Axes[" + i + "].label");
	    } catch (ClassCastException ex) { natlabs[i] = null; }
	    try {
		if (natlabs[i] == null) natlabs[i] = (String) 
		    cmdata.getMetadatum("Axes[" + i + "].name");
	    } catch (ClassCastException ex) { natlabs[i] = null; }
	    if (natlabs[i] == null) natlabs[i] = "Unknown";
	}
	modlabs = natlabs;
	labels = natlabs;

	// attach the default transforms
	transforms = new Stack();
	transmaps = new Stack();
	transdirs = new Stack();
	if (trans != null) {
	    if (constraints == null) throw new 
		NullPointerException("CoordTransformConstraints");
	    int sz = trans.size();

	    transforms.ensureCapacity(sz);
	    transmaps.ensureCapacity(sz);
	    transdirs.ensureCapacity(sz);

	    CoordTransform t;
	    CoordTransformConstraints c;
	    for(int i=0; i < sz; i++) {

		// make sure we have a transform for each position
		// in the input transform stack
		try {
		    t = (CoordTransform) trans.elementAt(i);
		} catch (ClassCastException ex) {
		    t = null;
		}
		if (t == null) throw new 
		    IllegalArgumentException("Missing transform in input " +
					     "Stack, position = " + i);
		transforms.push(t);

		// come up with constaints for each transform
		try {
		    c = (CoordTransformConstraints) constraints.elementAt(i);
		} catch (ClassCastException ex) {
		    c = null;
		} catch (ArrayIndexOutOfBoundsException ex) {
		    c = null;
		}
		if (c == null) throw new 
		    IllegalArgumentException("Missing transform constraints " +
				    "in input Stack, position = " + i);

		transmaps.push(c.getAxisIndexList());
		transdirs.push((c.forward) ? Boolean.TRUE : Boolean.FALSE);
	    }
	}
	gluedTrans = transforms.size();
    }

    /**
     * return the number of axes in the space containing this position
     */
    public int getNaxes() { return naxes; }

    /**
     * attach a coordinate transform to this system with 
     * @param t  the transform to be atached
     * @param c  the constraints by which to attach the transform
     */
    public synchronized void attachTransform(CoordTransform t, 
					     CoordTransformConstraints c)
	throws IllegalArgumentException, NullPointerException
    { 
	if (t == null) throw new NullPointerException("CoordTransform");

	if (c == null) {
	    c = determineConstraints(t, true);
	    if (c == null) throw new 
		IllegalArgumentException("Null constraints found; no defaults"
					 + " available");
	}

	// initialize the transform and to fit with this coordinate system
	try {
	    t.init(this, c.forward, c.getAxisIndexList());
	} 
	catch(IllegalTransformException ex) {
	    throw new IllegalArgumentException(
		"Unable to initialize transform: " + ex.getMessage() );
	}

	// push the transform and constaints onto our stacks
	transforms.push(t);
	transmaps.push(c.getAxisIndexList());
	transdirs.push(new Boolean(c.forward));
	t.addObserver(this);

	updateModifiedMetadata();
	setUsingTransforms(usingTransforms);
    }

    /**
     * attach a coordinate transform to this system
     * @param t               the transform to be atached
     * @param attachForwards  if true, the reverse transform should be 
     *                        attached
     * @exception IllegalTransformException if transform cannot be 
     *           implicitly attached in the direction requested
     */
    public synchronized void attachTransform(CoordTransform t, 
					     boolean attachForwards) 
	throws IllegalTransformException
    { 

	CoordTransformConstraints c = determineConstraints(t, attachForwards);
	if (c == null) throw new 
            IllegalTransformException("Transform cannot be implicitly " +
				      "attached to this CoordinateSystem");

	// initialize the transform to fit with this coordinate system
	try {
	    t.init(this, attachForwards, c.getAxisIndexList());
	} 
	catch(IllegalTransformException ex) {
	    throw new IllegalArgumentException(
		"Unable to initialize transform: " + ex.getMessage() );
	}

	// push the transform and constaints onto our stacks
	transforms.push(t);
	transmaps.push(c.getAxisIndexList());
	transdirs.push((c.forward) ? Boolean.TRUE : Boolean.FALSE);
	t.addObserver(this);

	updateModifiedMetadata();
	setUsingTransforms(usingTransforms);
    }

    /**
     * attach a coordinate transform to this system 
     * @param t  the transform to be atached
     * @exception IllegalTransformException if transform cannot be 
     *           implicitly attached in the forward direction 
     */
    public synchronized void attachTransform(CoordTransform t) 
	throws IllegalTransformException
    { 

	CoordTransformConstraints c = determineConstraints(t, true);
	if (c == null) throw new 
            IllegalTransformException("Transform cannot be implicitly " +
				      "attached to this CoordinateSystem");

	// initialize the transform to fit with this coordinate system
	try {
	    t.init(this, true, c.getAxisIndexList());
	} 
	catch(IllegalTransformException ex) {
	    throw new IllegalArgumentException(
		"Unable to initialize transform: " + ex.getMessage() );
	}

	// push the transform and constaints onto our stacks
	transforms.push(t);
	transmaps.push(c.getAxisIndexList());
	transdirs.push((c.forward) ? Boolean.TRUE : Boolean.FALSE);
	t.addObserver(this);
	
	updateModifiedMetadata();
	setUsingTransforms(usingTransforms);
    }

    /**
     * permanently attach any removable transforms currently attached,
     * making them non-removable.  
     * @param updateMetadata  true if the default metadata should be updated 
     *                        accordingly, or false if the currently set
     *                        default metadata should remain unchanged.
     */
    protected void glueTransforms(boolean updateMetadata) {
	gluedTrans = transforms.size();
	if (updateMetadata) cmdata = modmdata;
    }

    /**
     * make an educated guess as to the proper way to apply a transform
     * to this coordinate system.  Note that there may exist a number
     * of ways that this transform might be logically applied to this
     * system. 
     * @param in         the Metadata describing the system to be transformed
     * @return CoordTransformConstraints the resulting guess, null if the 
     *                                   transform cannot be logically applied
     */
    public CoordTransformConstraints determineConstraints(CoordTransform in) 
    {
	return in.determineConstraints(getMetadata());
    }
     
    /**
     * make an educated guess as to the proper way to apply a transform
     * to this coordinate system.  Note that there may exist a number
     * of ways that this transform might be logically applied to this
     * system. 
     * @param in         the Metadata describing the system to be transformed
     * @param forwards   if false, the constraints determined should be 
     *                   for attaching the reverse of the transform 
     * @return CoordTransformConstraints the resulting guess, null if the 
     *                                   transform cannot be logically applied
     */
    public CoordTransformConstraints determineConstraints(CoordTransform in,
							  boolean forwards) 
    {
	return in.determineConstraints(getMetadata(), forwards);
    }
     
    /**
     * return the number of removeable transforms attached to this 
     * CoordinateSystem.  A non-zero result does not indicate whether 
     * they are being used.
     */
    public int getNTransforms() { 
	return transforms.size()-gluedTrans;
    }

    /**
     * remove and return the last CoordTransform object attached to this
     * CoordinateSystem, or null if no transforms are currently attached
     */
    public synchronized CoordTransform popTransform() { 
	CoordTransform out = null;

	if (transmaps.size() >= gluedTrans) transmaps.pop();
	if (transdirs.size() >= gluedTrans) transdirs.pop();
	if (transforms.size() >= gluedTrans) {
	    out = (CoordTransform) transforms.pop();
	    out.deleteObserver(this);
	}

	updateModifiedMetadata();
	setUsingTransforms(usingTransforms);
	return out;
    }

    /**
     * remove and return all removable CoordTransform objects currently
     * attached to this CoordinateSystem.
     * @return Stack  a list of the CoordTransform object, beginning
     *                with the first one that was attached and ending 
     *                with the last one.
     */
    public synchronized Stack popAllTransforms() { 
	int sz = transforms.size();
	Stack out = new Stack();
	out.ensureCapacity(sz-gluedTrans);
	if (sz <= gluedTrans) return out;

	CoordTransform t;
	for(int i=gluedTrans; i < sz; i++) {
	    t = null;
	    try { t = (CoordTransform) transforms.elementAt(i); }
	    catch (ClassCastException ex) { /* should not happen */ }
	    if (t != null) t.deleteObserver(this);
	    out.push(t);
	}

	transforms.setSize(gluedTrans);
	transmaps.setSize(gluedTrans);
	transdirs.setSize(gluedTrans);

	updateModifiedMetadata();
	setUsingTransforms(usingTransforms);

	return out;
    }

    /**
     * remove all removable CoordTransform object currently
     * attached to this CoordinateSystem.  (If no other objects have 
     * references to the transform objects, the Garbage collector will
     * recover their memory.)
     */
    public synchronized void removeAllTransforms() { 
	if (transforms.size() > gluedTrans) {

	    CoordTransform t;
	    for(int i=gluedTrans; i < transforms.size(); i++) {
		t = null;
		try { t = (CoordTransform) transforms.elementAt(i);}
		catch (ClassCastException ex) { /* should not happen */ }
		if (t != null) t.deleteObserver(this);
	    }

	    transforms.setSize(gluedTrans);
	    transmaps.setSize(gluedTrans);
	    transdirs.setSize(gluedTrans);

	    updateModifiedMetadata();
	    setUsingTransforms(usingTransforms);
	}
    }

    /**
     * return true attached CoordTransform objects are being used
     */
    public boolean isUsingTransforms() { 
	return usingTransforms;
    }

    /**
     * updates the modified metadata list.  This should be called anytime
     * a transform is added or removed
     */
    protected synchronized void updateModifiedMetadata() {

	CoordTransform t;
	int[] m;
	boolean d;

	modmdata = new Metadata(cmdata);
	for (int i=gluedTrans; i < transforms.size(); i++) {
	    t = (CoordTransform) transforms.elementAt(i);
	    m = (int[]) transmaps.elementAt(i);
	    d = ((Boolean)transdirs.elementAt(i)).booleanValue();
	    modmdata = t.getMetadata(modmdata, d, m);
	}

    }

    /**
     * turn on or off the use of attached CoordTransform objects.  By
     * default, the value is of this property is true.
     * @param yes true if the transforms should be used 
     */
    public synchronized void setUsingTransforms(boolean yes) { 

	boolean hasChanged = false;

	if (yes != usingTransforms && transforms.size() > gluedTrans) {

	    // reset the number of axes based on modified Metadata
	    Metadata use = (yes) ? modmdata : cmdata;
	    try {
		naxes = ((Integer)use.getMetadatum("naxes")).intValue();
	    } 
	    catch (ClassCastException ex) {
		throw new CorruptedMetadataException("naxes should be of " +
						     "type Integer");
	    }
	    catch (NullPointerException ex) {
		throw new CorruptedMetadataException("naxes metadatum not " +
						     "found");
	    }

	    // reset the modified axis labels
	    if (yes) {
		modlabs = new String[naxes];
		for(int i=0; i < naxes; i++) {
		    String lab = null;
		    try {
			lab = (String) 
			    modmdata.getMetadatum("Axes[" + i + "].label");
		    } catch (ClassCastException ex) { lab = null; }
		    try {
			if (lab == null) lab = (String) 
			    modmdata.getMetadatum("Axes[" + i + "].name");
		    } catch (ClassCastException ex) { lab = null; }
		    if (lab != null) modlabs[i] = lab;
		}
	    }
	    labels = (yes) ? modlabs : natlabs;

	    usingTransforms = yes;
	    hasChanged();
	    notifyObservers(new Boolean(usingTransforms));
	}

    }

    /**
     * return a CoordinateSystem that is a clone of this one except with
     * the CoordTransform objects permanently attached
     */
    public CoordinateSystem newCoordinateSystem() { 
	CoordinateSystem out = (CoordinateSystem) clone();
	out.gluedTrans = out.transforms.size();
	out.natlabs = out.modlabs;
	return out;
    }

    /**
     * return a copy of the Metadata object that will describes this 
     * this system, accounting for any CoordTransform objects that may
     * be attached and in use.
     */
    public Metadata getMetadata() { 
	return getMetadata(true);
    }

    /**
     * return a copy of the Metadata object that will describes this 
     * this system
     * @param withTransforms  true the Metadata returned should account
     *                        for any CoordTransform objects that may
     *                        be attached and in use; false, if they
     *                        should be ignored
     */
    public Metadata getMetadata(boolean withTransforms) { 
	return new Metadata((withTransforms) ? modmdata : cmdata);
    }

    /**
     * return the currently set label for an axis
     * @param axis the axis of interest; axes are numbered beginning with 0.
     */
    public String getAxisLabel(int axis) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (axis >= naxes) 
	    throw new ArrayIndexOutOfBoundsException("Requested axis=" + axis +
						     " >= naxes=" + naxes);

	return labels[axis];
    }

    /**
     * return the axis labels as an array of strings.  The index of the 
     * label for the first axis is zero.
     */
    public String[] getAxisLabel() { 
	String[] out = new String[naxes];
	System.arraycopy(labels, 0, out, 0, naxes);
	return out;
    }

    /**
     * set the axis labels.  Extra labels in the input array are ignored.
     */
    public void setAxisLabel(String[] labs) {
	int n = (naxes < labs.length) ? naxes : labs.length;
	System.arraycopy(labs, 0, labels, 0, n);
    }

    /**
     * set an axis label.  This method will not affect the contents of
     * the Metadata object returned by the getMetadata() method. 
     */
    public void setAxisLabel(int i, String lab) 
	throws ArrayIndexOutOfBoundsException 
    {
	if (i >= naxes) 
	    throw new ArrayIndexOutOfBoundsException("Requested axis=" + i +
						     " >= naxes=" + naxes);
	labels[i] = lab;
    }

    /**
     * return the coordinate position corresponding the specified
     * data voxel
     * @param vox  the location in the dataset expressed as a Voxel.
     *             The position along missing axes are taken to be
     *             zero; extra axes are ignored.
     * @return CoordPos the position in world coordinates
     */
    public CoordPos getCoordPos(Voxel vox) 
	throws PositionBeyondDomainException, TransformUndefinedException
    { 
	return getCoordPos(vox.getValues(0));
    }

    /**
     * return the coordinate position corresponding the specified
     * data voxel
     * @param vox  the location in the dataset expressed as an array
     *             of doubles, with each element being the location
     *             along each axis.  Missing elements are taken to be
     *             zero; extra elements are ignored.
     * @return CoordPos the position in world coordinates
     */
    public CoordPos getCoordPos(double[] vox) 
	throws PositionBeyondDomainException, TransformUndefinedException
    { 
	return new CoordPos(naxes, getCoordValue(vox), 
			    (usingTransforms) ? modmdata : cmdata);
    }

    /**
     * return the coordinate position corresponding to the specified
     * data voxel.  
     * @param vox  the location in the dataset expressed as a Voxel.
     *             The position along missing axes are taken to be
     *             zero; extra axes are ignored.
     * @param double[] the position in world coordinates, where each
     *             element in the array is a representation of the
     *             location along an axis as a double
     */
    public double[] getCoordValue(double[] vox) 
	throws PositionBeyondDomainException, TransformUndefinedException
    { 
	int i, n;

	// create output array
	double[] out = 
	    new double[(nnataxes > vox.length) ? nnataxes : vox.length];
	System.arraycopy(vox, 0, out, 0, vox.length);
	for(i=vox.length; i < nnataxes; i++) out[i] = 0;

	// apply transforms as necessary
	n = (usingTransforms) ? transforms.size() : gluedTrans;
	for(i=0; i < n; i++) {
	    Boolean d = (Boolean) transdirs.elementAt(i);
	    CoordTransform t = (CoordTransform) transforms.elementAt(i);
	    int[] m = (int[]) transmaps.elementAt(i);
	    if (d.booleanValue()) 
		out = t.forward(out, m);
	    else 
		out = t.reverse(out, m);
	}
	    
	return out;
    }

    /**
     * return the location within the dataset corresponding to the 
     * specified coordinate position
     * @param   pos   the position in world coordinates.  No checking
     *                is done to ensure that this position can exist 
     *                within this coordinate system (say, via a check 
     *                of its metadata).  Values for missing axes are 
     *                taken to be zero; extra axes are ignored.
     * @return  Voxel the location in the dataset.
     */
    public Voxel getVoxel(CoordPos pos) 
	throws PositionBeyondDomainException, TransformUndefinedException
    { 
	return getVoxel(pos.getValue());
    }

    /**
     * return the location within the dataset corresponding to the 
     * specified coordinate position
     * @param   pos      the position in world coordinates, expressed as 
     *                   an array of doubles where each element is a 
     *                   representation of the location along an axis
     *                	 Missing axes are taken to be zero; extra axes
     *                	 are ignored.
     * @return  Voxel the location in the dataset.
     */
    public Voxel getVoxel(double[] pos) 
	throws PositionBeyondDomainException, TransformUndefinedException
    { 
	return new Voxel(naxes, getDataLocation(pos));
    }

    /**
     * return the location within the dataset corresponding to the 
     * specified coordinate position
     * @param   pos      the position in world coordinates, expressed as 
     *                   an array of doubles where each element is a 
     *                   representation of the location along an axis
     *                	 Missing axes are taken to be zero; extra axes
     *                	 are ignored.
     * @return  double[] the location in the dataset, where each element
     *                   is a location in the dataset along each axis.
     */
    public double[] getDataLocation(double[] pos) 
	throws PositionBeyondDomainException, TransformUndefinedException
    { 
	int i, n;

	// create output array
	double[] out = new double[(naxes > pos.length) ? naxes : pos.length];
	System.arraycopy(pos, 0, out, 0, pos.length);
	for(i=pos.length; i < naxes; i++) out[i] = 0;

	// apply transforms as necessary
	n = (usingTransforms) ? transforms.size() : gluedTrans;
	for(i=0; i < n; i++) {
	    Boolean d = (Boolean) transdirs.elementAt(i);
	    CoordTransform t = (CoordTransform) transforms.elementAt(i);
	    int[] m = (int[]) transmaps.elementAt(i);
	    if (d.booleanValue()) 
		out = t.reverse(out, m);
	    else 
		out = t.forward(out, m);
	}
	    
	return out;
    }

//     /**
//      * add an observer interested in finding out about a change in the
//      * use of attached CoordTransform objects.
//      */
//     public void addObserver(Observer obs) { }

    /**
     * clone this object
     */
    public synchronized Object clone() {
	CoordinateSystem out;
	try {
	    out = (CoordinateSystem) super.clone();
	} catch (CloneNotSupportedException e) {

	    // should not happen
	    throw new InternalError(e.getMessage());
	}

	return out;
    }

    public void update(Observable o, Object arg) {
	if (o instanceof CoordTransform) {
	    updateModifiedMetadata();
	    setUsingTransforms(usingTransforms);
	}
    }

}
