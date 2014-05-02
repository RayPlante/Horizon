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
 *  97mar26  rlp  Original version;
 *  97jul15  rlp  added {fwd|rev}names and {fwd|rev}fmtrs fields along 
 *                with supporting methods.  Updated documentation.
 */
package ncsa.horizon.coordinates;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.CorruptedMetadataException;
import java.util.Observable;
import java.util.Vector;

/**
 * an object for transforming positions from one coordinate system to 
 * another.  <p>
 *
 * This is an abstract class which can be sub-classed and fully 
 * implemented to create a <code>CoordTransform</code> object that can be 
 * attached to a <code>CoordinateSystem</code> object or used by itself.  <p>
 *
 * <b> Attaching a <code>CoordTransform</code> to a 
 * <code>CoordinateSystem</code>C </b><p>
 * 
 * A <code>CoordTransform</code> object is most commonly used by
 * attaching it to a <code>CoordinateSystem</code> via the latter's
 * <code>attachTransform</code>.  This causes the
 * <code>CoordinateSystem</code> to automatically apply the transform
 * everytime positions are requested from the system.  When the transform
 * is attached, it can (if necessary) adjust the parameters of the
 * transform based on the <code>CoordinateSystem</code>'s metadata; it
 * can also advise the <code>CoordinateSystem</code> on how the latter
 * should update its metadata to reflect the new transformation.  <p>
 * 
 * Two of the things an attached <code>CoordTransform</code> can suggest
 * to a <code>CoordinateSystem</code> are what to name the transformed
 * axes and how to print out the transformed positions.  This is done
 * by providing some suggested <code>String</codes>s to use as names and
 * <code>AxisPosFormatter</code> objects to use as formatters.  The default 
 * suggestions (if there are any) can be overriden via the 
 * <code>setName()</code> and <code>setFormatter()</code> method.  <p>
 *
 * <b>Using a <code>CoordTransform</code> Directly</b><p>
 *
 * The methods <code>forward()</code> and <code>reverse()</code> apply
 * the transform to an array of doubles that represents a position in one
 * of the systems (each element being the position along an axis of the
 * system).  What defines the forward transform is in general completely
 * arbitrary and up to the specific implementations.  Furthermore, the
 * number axes in the two systems do not need to be equal.  For
 * efficiency, these methods have the option of manipulating the elements
 * of the input position array directly and returning it, or return a new
 * array if number of axes in the input system is different from that of
 * the output system.  <p>
 *
 * Each element in the position array given to
 * <code>forward(double[])</code> or <code>reverse(double[])</code>
 * represents a position along some coordinate system axis.  When one of
 * these methods is called, the CoordTransform applies its operation on
 * the input array assuming that axes are in some specific order; for
 * instance, it may assume that the first element is a longitude and the
 * second is a latitude.  (See the transform's documentation for
 * <code>forward(double[])</code> and <code>reverse(double[])</code> for
 * the assumed order.)  However, one can take more control over how the
 * transform is applied by calling <code>forward(double[], int[])</code>
 * or <code>reverse(double[], int[])</code>.  The second argument in this
 * case is an axis index array which gives the location in the position
 * array of each required axis.  So, using our example, if the longitude
 * axis is actually the third element and the latitude is first element,
 * the axis index array should be equal to {2, 0}.  The position elements
 * not listed in the axis index array are left unchanged by the
 * transform.  <p>
 *
 * In the documentation below, the terms "old" and "new" refer the to 
 * two systems relative to the direction of this transform; that is,
 * the forward() method transforms a position in the "old" system to 
 * a position in the new system. <p>
 *
 * <b>Implementing a CoordTransform Object</b><p>
 *
 * A instantiatable CoordTransform must at a minimum implement six method:
 * <ul>
 *    <li> double[] forward(double[] position)
 *    <li> double[] forward(double[] position, int[] axisIndices)
 *    <li> double[] reverse(double[] position)
 *    <li> double[] reverse(double[] position, int[] axisIndices)
 *    <li> int getInNaxes()
 *    <li> int getOutNaxes()
 * </ul>
 * The last two return the minimum number of axes needed to apply the 
 * transform (see description below).  The other four actually perform the 
 * transformation.  The forward(double[]) and reverse(double[]) methods 
 * assume that the axes of the position are ordered in the default way.  
 * Most of the Horizon CoordTransform classes put the actual transforming
 * code in the version of forward() and reverse() that take an axis index
 * array (see previous section); the single argument versions of forward() 
 * and reverse() merely call the two-argument versions with a default 
 * axis index array.  <p>
 * 
 * Implementations may also wish to override one or a few of the other
 * methods in this class that are called when the transform is attached
 * to a <code>CoordinateSystem</code>.  When the transform is attached,
 * the <code>CoordinateSystem</code> calls the transform's
 * <code>init()</code>; this allows the transform to adjust its internal
 * parameters according to the system's metadata.  (By default
 * <code>init()</code> is a do-nothing method.)  Next, the
 * <code>CoordinateSystem</code> calls the transform's
 * <code>getMetadata()</code> to update the system's metadata to reflect
 * the new transformation.  The default version of <code>getMetadata()</code> 
 * calls <code>applyNamesAndFormatters()</code> to update the axis names
 * and formatters; thus overriding versions should call either 
 * <code>super.getMetadata()</code> or <code>applyNamesAndFormatters()</code>.
 * <p>
 *
 * Finally, implementations may wish to set some default axis names and 
 * formatters within the transform constructors.  For these to be passed on 
 * to a <code>CoordinateSystem</code> during attachment, it is not necessary
 * to override <code>getMetadata()</code>; the default version does this 
 * automatically. <p>
 *
 * Note that method that update the internal state of a 
 * <code>CoordTransform<code> should consider that the transform might be
 * attached to a <code>CoordinateSystem</code> at the time; therefore, the
 * methods should call hasChanged() and notifyObservers(). <p>
 * 
 * @author Raymond Plante
 * @author the Horizon Java Team 
 */
public abstract class CoordTransform extends Observable implements Cloneable 
{

    /**
     * suggested AxisPosFormatter objects to use with forward transformations 
     * when attached to a CoordinateSystem object; null value means use the 
     * formatter CoordinateSystem's default formatter.  The formatters are 
     * not used when independent of the CoordinateSystem object, and they
     * may not get used if this transform is not the last one in a chain.
     */
    protected Vector fwdfmtrs = null;

    /**
     * suggested AxisPosFormatter objects to use with reverse transformations 
     * when attached to a CoordinateSystem object; null value means use the 
     * formatter CoordinateSystem's default formatter.  The formatters are 
     * not used when independent of the CoordinateSystem object, and they
     * may not get used if this transform is not the last one in a chain.
     */
    protected Vector revfmtrs = null;

    /**
     * suggested strings to use as axis names to use with forward 
     * transformations when attached to a CoordinateSystem object; null 
     * value means use the formatter CoordinateSystem's default formatter.  
     * The names are ignored when tranform is used independent of a 
     * CoordinateSystem object, and they may not get used if this transform 
     * is not the last one in a chain.
     */
    protected Vector fwdnames = null;

    /**
     * suggested strings to use as axis names to use with reverse
     * transformations when attached to a CoordinateSystem object; null 
     * value means use the formatter CoordinateSystem's default formatter.  
     * The names are ignored when tranform is used independent of a 
     * CoordinateSystem object, and they may not get used if this transform 
     * is not the last one in a chain.
     */
    protected Vector revnames = null;

    /**
     * apply a forward tranform on an input position.  
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public abstract double[] forward(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException, TransformUndefinedException;


    /**
     * apply a forward tranform on an input position.  A list of axis indices
     * is assumed (usually { 0, ... getInNAxes()-1 }).
     * @param position    an array giving the input position to transform
     */
    public abstract double[] forward(double[] position) 
	throws PositionBeyondDomainException, TransformUndefinedException;

    /**
     * apply a reverse tranform on an input position
     * @param position    an array giving the input position to transform
     * @param axisIndices an array containing the indices of the position
     *                    array that should be used in the tranformation.
     *                    The order of the indices indicate how the position
     *                    should be interpreted by the transform.
     */
    public abstract double[] reverse(double[] position, int[] axisIndices) 
	throws PositionBeyondDomainException, TransformUndefinedException;

    /**
     * apply a reverse tranform on an input position.  A list of axis indices
     * is assumed (usually { 0, ... getOutNAxes()-1 }).
     * @param position    an array giving the input position to transform
     */
    public abstract double[] reverse(double[] position) 
	throws PositionBeyondDomainException, TransformUndefinedException;

    /**
     * return the minimum number of axes that the forward transform operates 
     * on.  This value is equal to the minimum number of axes that results 
     * from the reverse transform.  This value is often equal to the that 
     * returned by getOutNaxes(), but is not required to.
     */
    public int getInNaxes() { return 0; }

    /**
     * return the minimum number of axes that results from the forward 
     * transform.  This value is equal to the minimum number of axes that 
     * the reverse transform operates on.  This value is often equal to the 
     * that returned by getInNaxes(), but is not required to.
     */
    public int getOutNaxes() { return 0; }

    /**
     * initialize this transform according to the system it is to be
     * applied to.  This method is usually called by a CoordinateSystem 
     * object when the transform is attached to it.  By default, this method 
     * does nothing; however, sub-classers have the option of overriding 
     * this method.  
     * @exception IllegalTransformException if this transform cannot 
     *                initialize itself for the given system and constraints.
     */
    public void init(CoordinateSystem csys, boolean forward, 
		     int[] axisIndices) 
	throws IllegalTransformException 
    { }

    /**
     * update the input Metadata object to reflect the changes that this
     * tranform makes to a coordinate position.  In general, this method 
     * will actually edit the contents of the input Metadata when changes
     * are necessary.  By default, this method returns the input Metadata 
     * object updated with new values for the "Axis[n].formatter" metadata
     * when available.  Note therefore that subclasses that override this 
     * method should either call super.getMetadata() or applyFormatters()
     * to properly update the formatters in the list.
     * @param in          the Metadata list to update
     * @param forward     if true, assume the transform is being applied in
     *                    the forward direction; otherwise, assume reverse
     * @param axisIndices the index list that describes which axes this 
     *                    transform will operate on; should not be null.
     */
    public Metadata getMetadata(Metadata in, boolean forward, 
				int[] axisIndices) 
    { 
	applyNamesAndFormatters(in, forward, axisIndices);
	return in;
    }

    /**
     * make an educated guess as to the proper way to apply this transform
     * to a coordinate system with the specified Metadata.  By default, this
     * method returns <code>new CoordTransformConstraints(n)</code> where 
     * n is the value of the "naxes" metadatum; however, sub-classers are
     * encouraged to override this.  Note that there may exist a number
     * of ways that this transform might be logically applied to a 
     * coordinate system. 
     * @param in         the Metadata describing the system to be transformed
     * @return CoordTransformConstraints the resulting guess, null if the 
     *                                   transform cannot be logically applied
     */
    public CoordTransformConstraints determineConstraints(Metadata in) {
	return determineConstraints(in, true);
    }

    /**
     * make an educated guess as to the proper way to apply this transform
     * to a coordinate system with the specified Metadata.  By default, this
     * method returns <code>new CoordTransformConstraints(n)</code> where 
     * n is the value of the "naxes" metadatum; however, sub-classers are
     * encouraged to override this.  Note that there may exist a number
     * of ways that this transform might be logically applied to a 
     * coordinate system. 
     * @param in         the Metadata describing the system to be transformed
     * @param forwards   if false, the constraints determined should be 
     *                   for attaching the reverse of the transform 
     * @return CoordTransformConstraints the resulting guess, null if the 
     *                                   transform cannot be logically applied
     */
    public CoordTransformConstraints determineConstraints(Metadata in,
							  boolean forwards) 
    {
	try {
	    Integer naxes = (Integer) in.getMetadatum("naxes");
	    return new CoordTransformConstraints(naxes.intValue());
	}
	catch (ClassCastException e) {
	    return null;
	}
    }

    /**
     * return true if this transform can be logically applied to a 
     * a coordinate system with the specified Metadata.  By default,
     * this method returns true if 
     * <a href="#determineConstraints(ncsa.horizon.util.Metadata)">
     * determineConstraints()</a> returns a non-null value.
     * @param in         the Metadata describing the system to be transformed
     */
    public boolean canTransform(Metadata in) {
	return canTransform(in, false);
    }

    /**
     * return true if this transform can be logically applied to a 
     * a coordinate system with the specified Metadata.  By default,
     * this method returns true if 
     * <a href="#determineConstraints(ncsa.horizon.util.Metadata)">
     * determineConstraints()</a> returns a non-null value.
     * @param in         the Metadata describing the system to be transformed
     * @param forwards   if false, return whether the reverse transform can
     *                   be applied
     */
    public boolean canTransform(Metadata in, boolean forwards) {
	return (determineConstraints(in, forwards) != null);
    }

    /**
     * return true if this transform can be logically applied to a 
     * a coordinate system with the specified Metadata using the specified 
     * constraints.  By default, this method returns false only if the
     * CoordTransformConstraints refers to an axis index outside the range
     * specified within the Metadata object.  Sub-classers are encouraged
     * to override this method as is appropriate (for example, it may
     * check to make sure that there exists at least one longitude axis
     * and one latitude axis).  
     */
    public boolean canTransform(Metadata in, CoordTransformConstraints c) {
	Integer naxes;
	try {
	    naxes = (Integer) in.getMetadatum("naxes");
	} catch (ClassCastException ex) { naxes = null; }
	if (naxes == null) return false;

	int[] ail = c.getAxisIndexList();
	if (ail.length <= 0) return false;

	int nax = naxes.intValue();
	for(int i=0; i < ail.length; i++) {
	    if (ail[i] < 0 || ail[i] >= nax) return false;
	}

	return true;
    }

    /**
     * set the formatter object(s) that get used (when necessary) while this 
     * transform is attached to a CoordinateSystem.  Note that this method
     * must be called prior to attachment in order for the CoordinateSystem
     * to recieve the formatter.
     * @param axis       the axis number intended for the formatter; if <= 0, 
     *                   then use the formatter for all axes, taking the 
     *                   absolute value as the total number of axes we should 
     *                   expect (0 keep the same number of assumed axes).
     * @param formatter  the formatter to set
     * @param forForward if true, the formatter should be used when this  
     *                   transform is attached in the forward direction; 
     *                   otherwise, it should only be used when attached
     *                   in the reverse direction.
     */
    public synchronized void setFormatter(int axis, AxisPosFormatter formatter, 
					  boolean forForward)
    {
	Vector use = (forForward) ? fwdfmtrs : revfmtrs;

	if (axis < 0) {
	    int nax = -axis;
	    if (axis == 0) 
		axis = (use == null || use.size() == 0) ? 5 : use.size();
	    if (use == null) use = new Vector(axis);
	    for(int i=0; i < axis; i++) use.setElementAt(formatter, i);
	    if (forForward) 
		fwdfmtrs = use;
	    else 
		revfmtrs = use;
	}
	else {
	    if (axis >= use.size()) use.setSize(axis);
	    use.setElementAt(formatter, axis);
	}
    }

    /**
     * return the formatter objects that will get used (when 
     * necessary) after this transform is attached to a CoordinateSystem.  
     * @param  forForward          if true, return the ones applicable when
     *                             this transform is attached in the forward
     *                             direction; otherwise, return those for the 
     *                             reverse direction.
     * @return AxisPosFormatter[]  the array that will be used or null if
     *                             the CoordinateSystem should use whatever
     *                             other formatters available to it.  Null
     *                             values within the array also mean use the
     *                             default for that axis.
     */
    public AxisPosFormatter[] getFormatters(boolean forForward) {
	Vector use = (forForward) ? fwdfmtrs : revfmtrs;
	if (use == null) return null;

	int nax = use.size();
	AxisPosFormatter[] out = new AxisPosFormatter[nax];
	for(int i=0; i < nax; i++) 
	    out[i] = (AxisPosFormatter) use.elementAt(i);
	return out;
    }

    /**
     * set the name that get used (when necessary) to identify an axis while 
     * this transform is attached to a CoordinateSystem.  Note that this method
     * must be called prior to attachment in order for the CoordinateSystem
     * to receive the name.
     * @param axis       the axis number intended for the formatter; if < 1, 
     *                   then use the formatter for all axes
     * @param name       the name to be passed to the CoordinateSystem
     * @param forForward if true, the formatter should be used when this  
     *                   transform is attached in the forward direction; 
     *                   otherwise, it should only be used when attached
     *                   in the reverse direction.
     */
    public synchronized void setName(int axis, String name, boolean forForward)
    {
	Vector use = (forForward) ? fwdnames : revnames;

	if (axis < 0) {
	    int nax = -axis;
	    if (axis == 0) 
		axis = (use == null || use.size() == 0) ? 5 : use.size();
	    if (use == null) use = new Vector(axis);
	    for(int i=0; i < axis; i++) use.setElementAt(name, i);
	    if (forForward) 
		fwdnames = use;
	    else 
		revnames = use;
	}
	else {
	    if (axis >= use.size()) use.setSize(axis);
	    use.setElementAt(name, axis);
	}
    }

    /**
     * return the names that will (when necessary) identify the transformed
     * axes after this transform is attached to a CoordinateSystem.  
     * @param  forForward          if true, return the ones applicable when
     *                             this transform is attached in the forward
     *                             direction; otherwise, return those for the 
     *                             reverse direction.
     * @return String[]            the array that will be used or null if
     *                             the CoordinateSystem should use whatever
     *                             other names available to it.  Null
     *                             values within the array also mean use the
     *                             default for that axis.
     */
    public String[] getNames(boolean forForward) {
	Vector use = (forForward) ? fwdnames : revnames;
	if (use == null) return null;

	int nax = use.size();
	String[] out = new String[nax];
	for(int i=0; i < nax; i++) 
	    out[i] = (String) use.elementAt(i);
	return out;
    }

    /**
     * update the "Axes[n].formatter" and "Axes[n].name" metadata in the 
     * given list with the assumption that the transform is being applied 
     * to a system described by the metadata list.  Note that most 
     * applications need not call this method as it is usually invoked within 
     * the getMetadata() method of this class.
     * @param md           the metadata list to update
     * @param forward      if true, assume the transform is being applied 
     *                     in the forward direction; otherwise assume reverse.
     * @param axisIndices  the index list that describes which axes this 
     *                     transform will operate on; should not be null.
     */
    public synchronized void applyNamesAndFormatters(Metadata md, 
						     boolean forward, 
						     int[] axisIndices)
    {
	int nf = 0, nn = 0;
	Vector usefmtrs = (forward) ? fwdfmtrs : revfmtrs;
	Vector usenames = (forward) ? fwdnames : revnames;
	if (usefmtrs == null && usenames == null) return;

	nn = usenames.size();
	nf = usefmtrs.size();

	synchronized (md) {

	    // how many axes are we dealing with here?
	    int n=0;
	    try {
		Integer nax = (Integer) md.getMetadatum("naxes");
		if (nax != null) n = nax.intValue();
	    } catch (ClassCastException ex) { n = 0; }
	    if (n > 0) { 
		if (n < nn) nn = n;
		if (n < nf) nn = n;
	    }

	    // now insert data
	    for(int i=0; i < axisIndices.length; i++) {
		if (axisIndices[i] < nn) 
		    CoordMetadata.setAxisName(md, axisIndices[i], 
					      (String) usenames.elementAt(i));
		if (axisIndices[i] < nf) 
		    CoordMetadata.setAxisFormatter(md, axisIndices[i],
				    (AxisPosFormatter) usefmtrs.elementAt(i));
	    }
	}
    }

    /**
     * create a copy of this Transform
     */
    public synchronized Object clone() {
	CoordTransform out = null;
	try {
	    out = (CoordTransform) super.clone();
	}
	catch (CloneNotSupportedException ex) {
	    // Should not happen
	    throw new InternalError(ex.getMessage());
	}
	out.deleteObservers();
	return out;
    }

}
