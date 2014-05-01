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
 * History: 
 *  97jul15  rlp  Original version;
 */

package ncsa.horizon.coordinates.transforms;

import java.util.*;
import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.Metavector;
import ncsa.horizon.util.MetadataTypeException;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.*;

/**
 * a container for combining a chain of CoordTransform objects into a 
 * single CoordTransform.
 *
 * See the documentation for the super class, 
 * <a href="ncsa.horizon.coordinates.CoordTransform.html">CoordTransform</a>,
 * for a general description of how a transform is used.  <p>
 *
 * Several CoordTransforms may be strung together within this container
 * via successive calls to <code>addTransform()</code>.  Then one can 
 * attach this composite transform to a <code>CoordinateSystem</code> via
 * the system's <code>attachTransform()</code> method.  It is possible
 * to add additional transforms even after it is attached to a system,
 * causing the system to use the new transforms; however, this should be 
 * done with care.  <p>
 *
 * @author Raymond Plante
 * @author the Horizon Java Team 
 * @version $Id: MultiCoordTransform.java,v 1.1 1997/08/07 07:43:42 rplante Exp $
 */
public class MultiCoordTransform extends CoordTransform implements Observer 
{

    protected Stack transforms;
    protected Stack transmaps;
    protected Stack transdirs;

    /**
     * apply a forward transform on an input position.  
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public double[] forward(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	int sz = transforms.size();
	if (sz <= 0) return position;

	if (getInNaxes() > axisIndices.length) throw new 
	    TransformUndefinedException("insufficient number of axes " + 
					"provided to this transform");

	CoordTransform t;
	int[] ail;
	Boolean dir;
	int i;
	double[] out, upos = new double[axisIndices.length];

	// extract out the position axes we will operate on
	for(i=0; i < upos.length; i++) {
	    upos[i] = (axisIndices[i] < position.length) ? 
		position[axisIndices[i]] : 0.0;
	}

	// apply each transform to our extracted position
	for(i=0; i < sz; i++) {
	    try {
		t = (CoordTransform) transforms.elementAt(i);
		ail = (int[]) transmaps.elementAt(i);
		dir = (Boolean) transdirs.elementAt(i);
	    } catch (ClassCastException ex) {
		// should not happen
		throw new InternalError(
		    "MultiCoordTransform: corrupted stacks: bad types");
	    }
	    if (t == null || ail == null || dir == null) {
		// should not happen
		throw new InternalError(
		    "MultiCoordTransform: corrupted stacks: missing data");
	    }

	    upos = (dir.booleanValue()) ? t.forward(upos, ail) :
		                          t.reverse(upos, ail);
	}

	// now insert data into output array
	if (upos.length != axisIndices.length) {

	    // the number of axes has changed
	    //
	    int d = upos.length - axisIndices.length;
	    out = new double[position.length + d];
	    if (d > 0) {

		// position array has grown; first, copy in old values 
		System.arraycopy(position, 0, out, 0, position.length);

		// now update input axes with new values
		for(i=0; i < axisIndices.length; i++) {
		    if (axisIndices[i] < position.length)
			out[axisIndices[i]] = upos[i];
		}

		// add on extra values at end of array
		for(i=0; i < d; i++) 
		    out[position.length+i] = upos[axisIndices.length+i];
	    }
	    else {

		// array has shrunk; first, determine which elements in
		// input array are to be deleted.
		BitSet deleted = new BitSet(position.length);
		for(i=upos.length; i < axisIndices.length; i++) 
		    deleted.set(axisIndices[i]);

		// update input position array
		for(i=0; i < upos.length; i++) {
		    if (axisIndices[i] < position.length)
			position[axisIndices[i]] = upos[i];
		}

		// now copy position data, skipping data to be deleted
		int j=0;
		for(i=0; i < position.length; i++) {
		    if (! deleted.get(i)) out[j++] = position[i];
		}
	    }
	}
	else {

	    // the number of axes has not changed; just insert new values
	    for(i=0; i < upos.length; i++) {
		if (axisIndices[i] < position.length) 
		    position[axisIndices[i]] = upos[i];
	    }
	    out = position;
	}

	return out;
    }
	    
    /**
     * apply a forward tranform on an input position.  A list of axis indices
     * is assumed (usually { 0, ... getInNAxes()-1 }).
     * @param position    an array giving the input position to transform
     */
    public double[] forward(double[] position) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	int[] uail = new int[position.length];
	for(int i=0; i < uail.length; i++) uail[i] = i;
	return forward(position, uail);
    }

    /**
     * apply a reverse tranform on an input position
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public double[] reverse(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	int sz = transforms.size();
	if (sz <= 0) return position;

	if (getInNaxes() > axisIndices.length) throw new 
	    TransformUndefinedException("insufficient number of axes " + 
					"provided to this transform");

	CoordTransform t;
	int[] ail;
	Boolean dir;
	int i;
	double[] out, upos = new double[axisIndices.length];

	// extract out the position axes we will operate on
	for(i=0; i < upos.length; i++) {
	    upos[i] = (axisIndices[i] < position.length) ? 
		position[axisIndices[i]] : 0.0;
	}

	for(i=sz-1; i >= 0; i--) {
	    try {
		t = (CoordTransform) transforms.elementAt(i);
		ail = (int[]) transmaps.elementAt(i);
		dir = (Boolean) transdirs.elementAt(i);
	    } catch (ClassCastException ex) {
		// should not happen
		throw new InternalError(
		    "MultiCoordTransform: corrupted stacks: bad types");
	    }
	    if (t == null || ail == null || dir == null) {
		// should not happen
		throw new InternalError(
		    "MultiCoordTransform: corrupted stacks: missing data");
	    }

	    upos = (dir.booleanValue()) ? t.reverse(position, ail) :
		                          t.forward(position, ail);
	}

	// now insert data into output array
	if (upos.length != axisIndices.length) {

	    // the number of axes has changed
	    //
	    int d = upos.length - axisIndices.length;
	    out = new double[position.length + d];
	    if (d > 0) {

		// arrays has grown; first, copy in old values 
		System.arraycopy(position, 0, out, 0, position.length);

		// now update with new values
		for(i=0; i < axisIndices.length; i++) {
		    if (axisIndices[i] < position.length)
			out[axisIndices[i]] = upos[i];
		}

		// add on extra values at end of array
		for(i=0; i < d; i++) 
		    out[position.length+i] = upos[axisIndices.length+1];
	    }
	    else {

		// array has shrunk; first, determine which elements in
		// input array are to be deleted.
		BitSet deleted = new BitSet(position.length);
		for(i=upos.length; i < axisIndices.length; i++) 
		    deleted.set(axisIndices[i]);

		// update input position array
		for(i=0; i < upos.length; i++) {
		    if (axisIndices[i] < position.length)
			position[axisIndices[i]] = upos[i];
		}

		// now copy position data, skipping data to be deleted
		int j=0;
		for(i=0; i < position.length; i++) {
		    if (! deleted.get(i)) out[j++] = position[i];
		}
	    }
	}
	else {

	    // the number of axes has not changed; just insert new values
	    for(i=0; i < upos.length; i++) {
		if (axisIndices[i] < position.length)
		    position[axisIndices[i]] = upos[i];
	    }
	    out = position;
	}

	return out;
    }
	    
    /**
     * apply a reverse tranform on an input position.  A list of axis indices
     * is assumed (usually { 0, ... getOutNAxes()-1 }).
     * @param position    an array giving the input position to transform
     */
    public double[] reverse(double[] position) 
	throws PositionBeyondDomainException, TransformUndefinedException
    {
	int[] uail = new int[position.length];
	for(int i=0; i < uail.length; i++) uail[i] = i;
	return reverse(position, uail);
    }

    int ninax=0;
    int noutax=0;

    /**
     * return the minimum number of axes that the forward transform operates 
     * on.  This value is equal to the minimum number of axes that results 
     * from the reverse transform.  This value is often equal to the that 
     * returned by getOutNaxes(), but is not required to.
     */
    public synchronized int getInNaxes() {
	return ninax;
    }

    /**
     * return the minimum number of axes that results from the forward 
     * transform.  This value is equal to the minimum number of axes that 
     * the reverse transform operates on.  This value is often equal to the 
     * that returned by getInNaxes(), but is not required to.
     */
    public synchronized int getOutNaxes() {
	return noutax;
    }

    /**
     * initialize this transform according to the system it is to be
     * applied to.  This method is usually called by a CoordinateSystem 
     * object when the transform is attached to it.  By default, this method 
     * does nothing; however, sub-classers have the option of overriding 
     * this method.  
     */
    public synchronized void init(CoordinateSystem csys, boolean forward, 
				  int[] axisIndices) 
    { 
	int sz = transforms.size();
	if (sz <= 0) return;

	int i, j;

	// simulate a new system from the metadata and the axis index
	// array
	Metadata amd, md = csys.getMetadata();
	Metavector mv=null, umv=null;
	int nmva;
	try {
	    mv = (Metavector) md.getMetadatum("Axes");
	} catch (ClassCastException ex) {
	    throw new MetadataTypeException("Axes", "Metavector");
	}
	if (mv == null) return;  // IS THIS CORRECT?
	
	nmva = mv.size();
	umv = new Metavector(axisIndices.length);
	for(i=0; i < axisIndices.length; i++) 
	    umv.setElementAt(mv.elementAt(axisIndices[i]), i);
	md.put("naxes", new Integer(axisIndices.length));
	md.put("Axes", umv);
	CoordinateSystem usys = new CoordinateSystem(md);

	MTCStack t;
	int e;
	boolean dir;
	int[] ail;
	for(i=0; i < sz; i++) {
	    e = (forward) ? i : sz - i;

	    try {
		t = new MTCStack(transforms, transmaps, transdirs, e);
	    }
	    catch (IllegalArgumentException ex) {
		// should not happen
		throw new InternalError("MultiCoordTransform: corrupted " +
					"stacks: " + ex.getMessage());
	    }
	    dir = (forward) ? t.forward : !t.forward;

	    // this will call the transform's init() with the proper data
	    usys.attachTransform(t.transform, 
				 new CoordTransformConstraints(t.ail, dir));
	}
	usys.removeAllTransforms();
    }

    /**
     * update the input Metadata object to reflect the changes that this
     * tranform makes to a coordinate position.  This method 
     * will actually edit the contents of the input Metadata.
     * @param in          the Metadata list to update
     * @param forward     if true, assume the transform is being applied in
     *                    the forward direction; otherwise, assume reverse
     * @param axisIndices the index list that describes which axes this 
     *                    transform will operate on; should not be null.
     */
    public Metadata getMetadata(Metadata in, boolean forward, 
				int[] axisIndices) 
    { 
	int sz = transforms.size();
	if (sz <= 0) return super.getMetadata(in, forward, axisIndices);

      synchronized (in) {
	int i, j;
	int nax;
	try {
	    Integer N = (Integer) in.getMetadatum("naxes");
	    nax = N.intValue();
	}
	catch (Exception ex) { nax = -1; }

	// simulate a new system from the metadata and the axis index
	// array
	Metavector mv=null, umv=null, origmv=null;
	int nmva;
	try {
	    origmv = (Metavector) in.getMetadatum("Axes");
	} catch (ClassCastException ex) {
	    throw new MetadataTypeException("Axes", "Metavector");
	}
	if (mv == null) return super.getMetadata(in, forward, axisIndices);
	
	nmva = origmv.size();
	if (nax < 0) nax = nmva;
	umv = new Metavector(axisIndices.length);
	for(i=0; i < axisIndices.length; i++) 
	    umv.setElementAt(origmv.elementAt(axisIndices[i]), i);
//	Metadata md = new Metadata(in);
	in.put("naxes", new Integer(axisIndices.length));
	in.put("Axes", umv);

	MTCStack t;
	int e;
	boolean dir;
	for(i=0; i < sz; i++) {
	    e = (forward) ? i : sz - i;

	    try {
		t = new MTCStack(transforms, transmaps, transdirs, e);
	    }
	    catch (IllegalArgumentException ex) {
		// should not happen
		throw new InternalError("MultiCoordTransform: corrupted " +
					"stacks: " + ex.getMessage());
	    }
	    dir = (forward) ? t.forward : !t.forward;

	    // filter the Metadata
	    in = getMetadata(in, dir, t.ail);
	}

	// now update changes to axis metadata
	int got;
	try {
	    Integer N = (Integer) in.getMetadatum("naxes");
	    got = N.intValue();
	}
	catch (Exception ex) { got = -1; }

	umv = new Metavector();
	origmv.copyInto(umv);
	try {
	    mv = (Metavector) in.getMetadatum("Axes");
	} catch (ClassCastException ex) {
	    throw new MetadataTypeException("Axes", "Metavector");
	}
	if (mv == null) 
	    throw new CorruptedMetadataException("Lost Axes Metadata");

	nmva = mv.size();
	if (got < 0) got = nmva;
	int d = got - axisIndices.length;

	// the number of axes has changed
	if (d < 0) {

	    // number of axes has shrunk; first, determine which 
	    // elements in input array are to be deleted.
	    BitSet deleted = new BitSet(nax);
	    for(i=got; i < axisIndices.length; i++) 
		deleted.set(axisIndices[i]);

	    // update remaining axes
	    for (i=0; i < got; i++) {
		if (axisIndices[i] < nax)
		    umv.setElementAt(mv.elementAt(i), axisIndices[i]);
	    }

	    // delete axes that were lost in transformation
	    for(i=0; i < nax; i++) 
		if (deleted.get(i)) umv.removeElementAt(i);
	}
	else {

	    // position array has grown; first update input axes
	    for(i=0; i < axisIndices.length; i++) {
		if (axisIndices[i] < nax) 
		    umv.setElementAt(mv.elementAt(i), axisIndices[i]);
	    }

	    if (d > 0) {
		    
		// number of axes has grown, add on new axes
		for(i=0; i < d; i++)
		    umv.setElementAt(mv.elementAt(axisIndices.length+i),
				     got+i);
	    }
	    
	}

	// update the Metadata list
	if (d != 0) in.put("naxes", new Integer(nax+d));
	in.put("Axes", umv);
      }

	applyNamesAndFormatters(in, forward, axisIndices);
	return in;
    }

    /**
     * make an educated guess as to the proper way to apply this transform
     * to a coordinate system with the specified Metadata.  
     * @param in         the Metadata describing the system to be transformed
     * @param forwards   if false, the constraints determined should be 
     *                   for attaching the reverse of the transform 
     * @return CoordTransformConstraints the resulting guess, null if the 
     *                                   transform cannot be logically applied
     */
    public synchronized 
    CoordTransformConstraints determineConstraints(Metadata in,
						   boolean forwards) 
    {
	int sz = transforms.size();
	if (sz <= 0) return super.determineConstraints(in, forwards);

	int nax;
	try {
	    Integer N = (Integer) in.getMetadatum("naxes");
	    nax = N.intValue();
	}
	catch (Exception ex) { return null; }

	MTCStack t;
	int i, j, e;
	boolean dir;
	CoordTransformConstraints ctc=null;
	Metadata md = new Metadata(in);
	int[] out = new int[nax];
	for(i=0; i < out.length; i++) out[i] = -1;
	int[] iail;

	for(i=0; i < sz; i++) {
	    e = (forwards) ? i : sz - i;

	    try {
		t = new MTCStack(transforms, transmaps, transdirs, e);
	    }
	    catch (IllegalArgumentException ex) {
		// should not happen
		throw new InternalError("MultiCoordTransform: corrupted " +
					"stacks: " + ex.getMessage());
	    }

	    // determine the constraints for this component transform
	    dir = (forwards) ? t.forward : !t.forward;
	    ctc = t.transform.determineConstraints(md, dir);
	    if (ctc == null) return null;
	    iail = ctc.getAxisIndexList();

	    // map the constraints to a composite constaints
	    for(j=0; j < out.length; j++) {
		if (t.ail[j] < out.length && out[t.ail[j]] < 0)
		    out[t.ail[j]] = iail[j];
	    }

	    // update the metadata
	    md = t.transform.getMetadata(md, dir, iail);
	}    

	// chop unused elements of index array (index < 0) off end of 
	// the array
	for(i=out.length-1; i >= 0 && out[i] < 0; i--);
	if (i >= 0) {
	    int[] tmp = out;
	    out = new int[i+1];
	    System.arraycopy(tmp, 0, out, 0, i+1);
	}

	// any remaining unused elements means that the position value
	// at that index will pass through all transforms unchanged; replace
	// them with indices larger than the number of axes (just to be 
	// safe)
	j = nax;
	for(i=0; i < out.length; i++) {
	    if (out[i] < 0) out[i] = j++;
	}

	return new CoordTransformConstraints(out, forwards);
    }

    /**
     * add a coordinate transform to this system with 
     * @param t  the transform to be atached
     * @param c  the constraints by which to add the transform
     */
    public synchronized void addTransform(CoordTransform t, 
					  CoordTransformConstraints c)
	throws IllegalArgumentException, NullPointerException
    { 
	if (t == null) throw new NullPointerException("CoordTransform");

	// check over the input constraints
	int[] ail = c.getAxisIndexList();
	int nin, nout;
	if (c.forward) {
	    nin  = t.getInNaxes();
	    nout = t.getOutNaxes();
	} else {
	    nout = t.getInNaxes();
	    nin  = t.getOutNaxes();
	} 

	if (ail.length < nin) throw new 
	    IllegalArgumentException("Insufficient number of axes " +
				     "given by transform constraints");

	int max = ail.length - 1;   // find the maximum axis index listed
	for(int i=0; i < ail.length; i++) {
	    if (ail[i] < 0) throw new
		IllegalArgumentException("Element " + i + " of Axis Index " +
					 "List is less than zero");
	    if (ail[i] > max) max = ail[i];
	}
	max++;

	// push the transform and constaints onto our stacks
	transforms.push(t);
	transmaps.push(ail);
	transdirs.push(new Boolean(c.forward));
	t.addObserver(this);

	// adjust the values returned getInNaxes() and getOutNaxes()
	int d = max - ninax;
	if (d > 0) {
	    ninax += d;
	    noutax += d;
	}
	d = nout - nin;
	if (d != 0) {
	    if (!c.forward) d *= -1;
	    noutax += d;
	}

	hasChanged();
	notifyObservers();
    }

    /**
     * add a coordinate transform to this composite assuming an axis index 
     * list of { 0, ..., t.getInAxes()-1 }.
     * @param t               the transform to be atached
     * @param addForwards  if true, the reverse transform should be 
     *                        added
     */
    public synchronized void addTransform(CoordTransform t, 
					  boolean addForwards) 
	throws NullPointerException
    { 
	if (t == null) throw new NullPointerException("CoordTransform");

	int nin, nout;
	if (addForwards) {
	    nin  = t.getInNaxes();
	    nout = t.getOutNaxes();
	} else {
	    nout = t.getInNaxes();
	    nin  = t.getOutNaxes();
	} 

	int n = (addForwards) ? nin : nout;
	int[] ail = new int[n];
	for(int i=0; i < n; i++) ail[i] = i;

	// push the transform and constaints onto our stacks
	transforms.push(t);
	transmaps.push(ail);
	transdirs.push((addForwards) ? Boolean.TRUE : Boolean.FALSE);
	t.addObserver(this);

	// adjust the values returned getInNaxes() and getOutNaxes()
	int d = n - ninax;
	if (d > 0) {
	    ninax += d;
	    noutax += d;
	}
	d = nout - nin;
	if (d != 0) {
	    if (!addForwards) d *= -1;
	    noutax += d;
	}

	hasChanged();
	notifyObservers();
    }

    /**
     * add a forward coordinate transform to this composite assuming an 
     * axis index list of { 0, ..., t.getInAxes()-1 }.
     * @param t  the transform to be atached
     * @exception IllegalTransformException if transform cannot be 
     *           implicitly added in the forward direction 
     */
    public void addTransform(CoordTransform t) 
	throws NullPointerException
    { 
	addTransform(t, true);
    }

    /**
     * return the number of transforms that make up this composite
     * transform.  
     */
    public int getNTransforms() { 
	return transforms.size();
    }

    /**
     * remove and return the last CoordTransform object added to this
     * CoordinateSystem, or null if no transforms are currently in this
     * container.
     */
    public synchronized CoordTransform popTransform() { 
	CoordTransform out = null;

	if (getNTransforms() > 0) {
	    transmaps.pop();
	    transdirs.pop();
	    out = (CoordTransform) transforms.pop();
	    out.deleteObserver(this);

	    hasChanged();
	    notifyObservers();
	}

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
	out.ensureCapacity(sz);

	CoordTransform t;
	for(int i=0; i < sz; i++) {
	    t = null;
	    try { t = (CoordTransform) transforms.elementAt(i); }
	    catch (ClassCastException ex) { /* should not happen */ }
	    if (t != null) t.deleteObserver(this);
	    out.push(t);
	}

	transforms.setSize(0);
	transmaps.setSize(0);
	transdirs.setSize(0);
	hasChanged();
	notifyObservers();

	return out;
    }

    /**
     * remove all removable CoordTransform object currently
     * attached to this CoordinateSystem.  (If no other objects have 
     * references to the transform objects, the Garbage collector will
     * recover their memory.)
     */
    public synchronized void removeAllTransforms() { 
	if (transforms.size() > 0) {

	    CoordTransform t;
	    for(int i=0; i < transforms.size(); i++) {
		t = null;
		try { t = (CoordTransform) transforms.elementAt(i);}
		catch (ClassCastException ex) { /* should not happen */ }
		if (t != null) t.deleteObserver(this);
	    }

	    transforms.setSize(0);
	    transmaps.setSize(0);
	    transdirs.setSize(0);

	    hasChanged();
	    notifyObservers();
	}
    }

    /**
     * notify any observers of this object that one or more the 
     * component transforms has had an internal change of state.
     */
    public void update(Observable o, Object arg) {
	hasChanged();
	notifyObservers();
    }
}

class MTCStack {
    public CoordTransform transform=null;
    public int[] ail=null;
    public boolean forward=true;

    public MTCStack(CoordTransform t, int[] ail, Boolean fwd) {
	transform = t;
	this.ail = ail;
	forward = fwd.booleanValue();
    }

    public MTCStack(Stack ts, Stack as, Stack fs, int i) {
	Boolean dir;

	try {
	    transform = (CoordTransform) ts.elementAt(i);
	    ail = (int[]) as.elementAt(i);
	    dir = (Boolean) fs.elementAt(i);
	} catch (ClassCastException ex) {
	    // should not happen
	    throw new IllegalArgumentException("bad types");
	}
	if (transform == null || ail == null || dir == null) {
	    // should not happen
	    throw new IllegalArgumentException("missing data");
	}
	forward = dir.booleanValue();
    }
}
	
