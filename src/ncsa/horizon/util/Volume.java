/**
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
 *  96       rlp  Original version
 *  97may15  rlp  Fixed clone() method
 *  97aug08  rlp  Fixed intersection() bug
 *  97oct21  rlp  eliminate support of arbitrary first axis indices
 *  97dec09  rlp  fixed bug in makeLengthsPositive(); added toString()
 *  97dec23  rlp  put in work-around in clone() that will work with Netscape3
 **/
package ncsa.horizon.util;

/**
 * an object that represents a (hyper-) box enclosure of regularly gridded 
 * points in a space of an arbitrary number of dimensions.  The enclosure 
 * is described in terms of the location of one of its vertices (the origin 
 * vertex), a length along each side of the box, and the sampling of points 
 * along each side.  The axes of the space are referenced by an index that
 * begins with zero.  <p>
 *
 * This object is intended to provide a way of describing a subset of 
 * data that exists in a multidimensional space (e.g. a multidimensional
 * data array).  The concept of regularly gridded data is supported 
 * through an internally stored sampling rate which is equal to one by 
 * default.  The origin and voxel size need not correspond to how the 
 * data it is applied to is stored; more often, they would reflect the
 * units used to measure locations in the space represented by the data. 
 * Data retrieval routines would determine how this Volume should be 
 * applied to extract or operate on the data. <p>
 * 
 * It is important to note that the lengths of sides of the volume can be 
 * set to negative values.  This merely means that the voxel that indicates
 * the volumes position is not the voxel closest to the origin of the space.
 * Note that if "increasing" a negative-valued length by providing a positive
 * value via add() will actually decrease the length.  Similarly, the sampling
 * rate can be negative as well, which means that the sampling should start
 * from a vertex other than the one closest to the origin of the space.  This
 * confusion can all be straightened out with the makeLengthsPositive() method,
 * which rearranges the description of the volume so that lengths and sampling
 * are both positive, but the same data is sampled. <p>
 *
 * See also <a href="ncsa.horizon.util.Voxel.html">Voxel</a>,
 *          <a href="ncsa.horizon.util.Slice.html">Slice</a>, and 
 *          <a href="ncsa.horizon.util.Segment.html">Segment</a>.
 *
 * @version Alpha 0.1
 * @author Horizon team, University of Illinois at Urbana-Champaign
 * @author Raymond L. Plante
 */
public class Volume implements Cloneable {

    protected int naxes;
    protected double[] loc;
    protected double[] size;
    protected double[] sample;

    /** 
     * flags that indicate how this volume can be used; objects operating
     * with this volume, may choose to ignore this value.
     */
    public int flags = 0;

    /** null flags value */
    public final static int NONE = 0;

    /** this flag indicates that interpolation is desired, if possible,
      * when this volume is applied to a data set; otherwise, use nearest
      * values.  */
    public final static int CAN_INTERPOLATE = 1;

    /** this flag indicates that interpolation is desired, if possible,
      * when this volume is applied to a data set; otherwise, use only 
      * the intersection of this volume with the dataset domain.  
      */
    public final static int CAN_EXTRAPOLATE = 2;

    /**
     * create a Volume object in a space of nax dimensions and 
     * initialize it to have a unit size with a sampling of 1 along 
     * each axis.  The index of the first axis will be zero.
     * @param nax number of axes or dimensions in the space 
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 
     */
    public Volume(int nax) throws ArrayIndexOutOfBoundsException {
	super();
	this.naxes = nax;
	if (nax <= 0) throw new ArrayIndexOutOfBoundsException(nax);
	loc = new double[nax];
	size = new double[nax];
	for(int i=0; i < nax; i++) size[i] = 1.0;
	sample = new double[nax];
	for(int i=0; i < nax; i++) sample[i] = 1.0;
    }

    /**
     * create a Volume object in a space of nax dimensions and 
     * initialize it to a given location, size, and sampling for
     * each axis.  The index of the first axis will be zero.  Input
     * arrays can be null to get default.
     * @param nax number of axes or dimensions in the space 
     * @param loc location of volume vertex (default: origin)
     * @param sz  length of each side (default: one)
     * @param sam sampling along each side (default: one)
     * @exception ArrayIndexOutOfBoundsException thrown if nax < 0 
     */
    public Volume(int nax, double[] loc, double[] sz, double[] sam) 
	throws ArrayIndexOutOfBoundsException 
    {
	this(nax);
	if (loc != null) setLocation(loc, 0);
	if (sz != null) setSize(sz, 0);
	if (sam != null) setSampling(sam, 0);
    }

    /** 
     * create a Volume object that is a copy of another Volume.  The 
     * index of the new Volume will be that of the input Volume
     */
    public Volume(Volume in) {
	this(in.getNaxes());
	setLocation(in.getLocation(0), 0);
	setSize(in.getSize(0), 0);
	setSampling(in.getSampling(0), 0);
    }

    /**
     * create a Volume object of unit size (and unit sampling) whose origin
     * vertex is located at the given voxel.   The index of the new Volume 
     * will be that of the input Volume.
     */
    public Volume(Voxel in) {
	this(in.getNaxes());
	setLocation(in.getValues(0), 0);
    }

    /** return the number of dimensions in the space containing this 
        Volume **/
    public int getNaxes() { return naxes; }

    /**
     * return an array of doubles representing the location of the volume's 
     * reference vertex.
     * @param firstaxis the desired index of the first axis in the output array
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0;
     */
    public double[] getLocation(int firstaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	int i;
	double out[] = new double[loc.length + firstaxis];

	for(i=0; i < firstaxis; i++) out[i] = 0;
	for(i=0; i < loc.length; i++) out[firstaxis + i] = loc[i];
	return out;
    }

    /**
     * return an array of doubles representing the location of the 
     * volume's reference vertex.  The first element (index 0) will 
     * contain the value for the first axis.
     */
    public double[] getLocation() { 
	return getLocation(0);
    }

    /**
     * get the location of volume's reference vertex 
     * represented as a Voxel object.  
     */
    public Voxel getVoxel() {
	Voxel out = new Voxel(naxes);
	out.setValues(loc);
	return out;
    }

    /**
     * get the location of the Volume's origin vertex along one axis.
     * @param int axis index of desired axis (index is 0-relative).
     */
    public double axisPos(int i) throws ArrayIndexOutOfBoundsException { 
	return loc[i]; 
    }

    /**
     * get the location of the Volume's origin vertex along one axis.
     * @param i   axis index of desired axis (index is 0-relative).
     * @param pos position along the desired axis
     */
    public void setAxisPos(int i, double pos) 
	throws ArrayIndexOutOfBoundsException 
    { 
	loc[i] = pos; 
    }

    /**
     * return an array of doubles representing the size of the box along 
     * each side.  
     * @param firstaxis the desired index of the first axis in the output array
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0;
     */
    public double[] getSize(int firstaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	int i;
	double out[] = new double[size.length + firstaxis];

	for(i=0; i < firstaxis; i++) out[i] = 0;
	for(i=0; i < size.length; i++) out[firstaxis + i] = size[i];
	return out;
    }

    /**
     * return an array of doubles representing the size of the box along 
     * each side.  
     */
    public double[] getSize() { 
	return getSize(0);
    }

    /**
     * return the length of a requested side 
     */
    public double getLength(int i) 
	throws ArrayIndexOutOfBoundsException 
    {
	return size[i];
    }

    /**
     * return an array of ints representing the true size of the volume 
     * by accounting for the current sampling
     * @param firstaxis the desired index of the first axis in the output array
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0;
     */
    public int[] getTrueSize(int firstaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	double[] sz = getSize(firstaxis);
	int[] out = new int[sz.length];

	for(int i=0; i < naxes; i++) 
	    out[i + firstaxis] = (sample[i] == 0) ? 
		                   0 : (int) Math.abs(sz[i]/sample[i]);

	return out;
    }

    /**
     * return an array of ints representing the true size of the volume 
     * by accounting for the current sampling.
     */
    public int[] getTrueSize() { 
	return getTrueSize(0);
    }

    /**
     * return the true length of a requested side 
     */
    public int getTrueLength(int i) 
	throws ArrayIndexOutOfBoundsException 
    {
	return ( (sample[i] == 0) ? 0 : (int) Math.abs(size[i]/sample[i]) );
    }

    /** 
     * set the location of the volume's vertex closest to the origin
     * @param newpos array of doubles containing position values
     * @param firstaxis index at which first value appears in newpos
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setLocation(double[] newpos, int firstaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	int i;
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	for(i=firstaxis; i < newpos.length && i - firstaxis < loc.length; i++)
	    loc[i - firstaxis] = newpos[i];
    }

    /** 
     * set the location of the volume's vertex closest to the origin.  The
     * first value will be assumed to be at the first index axis
     * defined when this object was constructed, unless that index is < 0, 
     * in which case, the first value will be assumed to be at 0.
     * @param newpos array of doubles containing position values
     */
    public void setLocation(double[] newpos) {
	setLocation(newpos, 0);
    }

    /**
     * set the location of the volume's vertex closest to the origin.  
     */
    public void setLocation(Voxel vox) {
	double[] newloc = vox.getValues(0);
	setLocation(newloc);
    }

    /**
     * set the length of each side of the volume 
     * @param newsz array of doubles containing position values
     * @param firstaxis index at which first value appears in newpos
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setSize(double[] newsz, int firstaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	int i;
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	for(i=firstaxis; i < newsz.length && i - firstaxis < size.length; i++)
	    size[i - firstaxis] = newsz[i];
    }

    /**
     * set the length of each side of the volume.  
     * @param newsz array of doubles containing position values
     */
    public void setSize(double[] newsz) {
	setSize(newsz, 0);
    }

    /**
     * set the length of a side of the volume
     * @param i  the index of the axis to be set
     * @param sz the length to set the side to
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setLength(int i, double sz) 
	throws ArrayIndexOutOfBoundsException 
    {
	size[i] = sz;
    }

    /**
     * set the volume sampling: each value in the input array represents 
     * the sampling of the volume along each axis; thus, the volume includes
     * the first voxel and every value-th voxel along each axis.
     * @param newsam array of doubles containing the sampling values
     * @param firstaxis index at which first value appears in newpos
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setSampling(double[] newsam, int firstaxis) 
	throws ArrayIndexOutOfBoundsException 
    {
	int i;
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	for(i=firstaxis; i<newsam.length && i - firstaxis < sample.length; i++)
	    sample[i - firstaxis] = newsam[i];
    }

    /**
     * set the volume sampling: each value in the input array represents 
     * the sampling of the volume along each axis; thus, the volume includes
     * the first voxel and every value-th voxel along each axis.  
     * @param newsz array of doubles containing position values
     */
    public void setSampling(double[] newsz) {
	setSampling(newsz, 0);
    }

    /**
     * set the sampling along a side of the volume
     * @param i  the index of the axis to be set
     * @param sam the sampling to set the side to
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0
     */
    public void setSampling(int i, double sam) 
	throws ArrayIndexOutOfBoundsException 
    {
	sample[i] = sam;
    }

    /**
     * get the current sampling along each side of the volume
     * @param firstaxis the desired index of the first axis in the output array
     * @exception ArrayIndexOutOfBoundsException if firstaxis < 0;
     */
    public double[] getSampling(int firstaxis)
	throws ArrayIndexOutOfBoundsException 
    {
	if (firstaxis < 0) throw new ArrayIndexOutOfBoundsException(firstaxis);

	int i;
	double out[] = new double[sample.length + firstaxis];

	for(i=0; i < firstaxis; i++) out[i] = 0;
	for(i=0; i < size.length; i++) out[firstaxis + i] = sample[i];
	return out;
    }

    /**
     * return an array of doubles representing the size of the box along 
     * each side.  
     */
    public double[] getSampling() { 
	return getSampling(0);
    }

    /**
     * get the sampling for a given side
     */
    public double getStep(int i) 
	throws ArrayIndexOutOfBoundsException 
    {
	return sample[i];
    }

    /**
     * adjust the description of this volume so that the lengths
     * and sampling of each side are positive but without changing 
     * the actual enclosed volume.  The location voxel will be moved 
     * to the vertex that is closest to the origin of the space containing 
     * this volume.
     * @return boolean true if a transformation was applied
     */
    public boolean makeLengthsPositive() {
	int t, i;
	boolean adjusted = false;

	for(i=0; i < naxes; i++) {
	    if (size[i] >= 0 && sample[i] >= 0) continue;

	    if (sample[i]*size[i] < 0) {
		t = (int) (size[i]/sample[i]);
		loc[i] = loc[i] + t*sample[i];
		size[i] = Math.abs(size[i]);
		sample[i] = Math.abs(sample[i]);
	    }
	    else {
		loc[i] = loc[i] + size[i];
		sample[i] = -sample[i];
	    }
	    adjusted = true;
	}

	return adjusted;
    }

    /**
     * create a deep copy of this object
     */
    public Object clone() {
	try {
	    Volume out = (Volume) super.clone();

	    // clonearrays is a work-around of a Netscape 3 bug triggered 
	    // by the clone statements below
// 	    out.loc = (double[]) loc.clone();
// 	    out.size = (double[]) size.clone();
// 	    out.sample = (double[]) sample.clone();
	    out.clonearrays();

	    out.setLocation(loc, 0);
	    out.setSize(size, 0);
	    out.setSampling(sample, 0);
	    return out;
	}
	catch (CloneNotSupportedException ex) {
	    // Should not happen
	    throw new InternalError(ex.getMessage());
	}
    }

    /**
     * assist the clone method with the cloning of internal arrays.  
     * This method exists as a work-around of a bug in the Netscape 3.0
     * implementation of the Class Verifier and will be removed in the 
     * next release of Horizon.
     */
    void clonearrays() {
	if (naxes > 0) {
	    loc = new double[naxes];
	    size = new double[naxes];
	    sample = new double[naxes];
	}
    }

    public String toString() {
	int i;
	StringBuffer buf = new StringBuffer("Slice(loc=[");
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(loc[i]);
	}
	buf.append("], size=[");
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(size[i]);
	}
	buf.append("], samp=[");
	for(i=0; i < naxes; i++) {
	    if (i != 0) buf.append(",");
	    buf.append(sample[i]);
	}
	buf.append("])");
	return buf.toString();
    }

    /**
     * return true if this volume includes a voxel.  The voxel will not 
     * be considered included if the number of dimensions in its space is 
     * greater than that of this volume.
     */
    public boolean includes(Voxel vox) {
	int i;
	double min, max;
	double[] pos = vox.getValues(0);
	if (pos.length > naxes) return false;

	for(i=0; i < naxes && i < pos.length; i++) {
	    min = (size[i] > 0) ? loc[i] : loc[i] + size[i];
	    max = (size[i] < 0) ? loc[i] : loc[i] + size[i];
	    if (pos[i] < min || pos[i] > max) return false;
	}
	return true;
    }

    /**
     * return true if this volume encloses another volume.  The given voxel 
     * will not be considered included if the number of dimensions in its 
     * space is greater than that of this volume.
     */
    public boolean includes(Volume vol) {
	int i;
	if (vol.getNaxes() > naxes) return false;

	Voxel vertex = vol.getVoxel();
	if (! includes(vertex)) return false;
	vertex.translate(new Voxel(vol.naxes, vol.size));
	if (! includes(vertex)) return false;

	return true;
    }

    /** 
     * return true if this volume intersects another volume.  Missing 
     * dimensions of over either volume will be considered overlapping
     */
    public boolean intersects(Volume vol) {
	double min, max;

	for(int i=0; i < naxes && i < vol.naxes; i++) {
	    min = (size[i] > 0) ? loc[i] : loc[i] + size[i];
	    max = (size[i] < 0) ? loc[i] : loc[i] + size[i];
	    if ((vol.loc[i] <= min && vol.loc[i] + vol.size[i] <= min) ||
		(vol.loc[i] >= max && vol.loc[i] + vol.size[i] >= min))

		return false;

	}
	return true;
    }

    /**
     * move this Volume to a relative position
     * @param vox the relative position to add to this volume's position; only
     *            dimensions that overlap this space will be added.
     */
    public void translate(Voxel vox) {
	int vn = vox.getNaxes();
	double[] rel = vox.getValues(0);

	for(int i=0; i < naxes && i < vn; i++)
	    loc[i] += rel[i];
    }

    /**
     * increase the size of each side by the projected distance of a Voxel 
     * from its origin along each corresponding axis.
     */
    public void grow(Voxel vox) {
	int vn = vox.getNaxes();
	double[] rel = vox.getValues(0);

	for(int i=0; i < naxes && i < vn; i++)
	    size[i] += rel[i];
    }

    /**
     * increase the size of this volume just enough to include the 
     * projection of a Voxel into this space
     */
    public void add(Voxel vox) {
	int vn = vox.getNaxes();
	double[] pos = vox.getValues(0);

	for(int i=0; i < naxes && i < vn; i++) {
	    if (size[i] >= 0) {
		if (pos[i] < loc[i]) 
		    loc[i] = pos[i];
		else if (pos[i] > loc[i] + size[i]) 
		    size[i] = pos[i] - loc[i];
	    }
	    else {
		if (pos[i] > loc[i]) 
		    loc[i] = pos[i];
		else if (pos[i] < loc[i] + size[i]) 
		    size[i] = pos[i] - loc[i];
	    }
	}
    }

    /**
     * increase the size of this volume just enough to include the 
     * projection of another Volume into this space
     */
    public void add(Volume vol) {
	Voxel vertex = vol.getVoxel();
	add(vertex);
	vertex.translate(new Voxel(vol.naxes, vol.size));
	add(vertex);
    }

    /**
     * compute the union of this volume with another.  The dimensionality 
     * and sampling will be that of this volume.
     */
    public Volume union(Volume that) {
	Volume out = (Volume) clone();
	out.add(that);
	return out;
    }

    /**
     * compute the intersection of this volume with another.  The 
     * dimensionality, the sampling, and the sign of the size will 
     * be that of this volume.  If the two volumes do not actually 
     * overlap the result will be the smallest volume that connects
     * the two vertices that are closest.
     */
    public Volume intersection(Volume vol) {
	Volume out = (Volume) clone();
	Volume that = (Volume) vol.clone();
	double min1, min2, max1, max2;
	int i;

	out.makeLengthsPositive();
	that.makeLengthsPositive();
	double[] thatloc = that.getLocation(0);
	double[] thatsz  = that.getSize(0);

	for(i=0; i < naxes && i < thatloc.length; i++) {
	    min1 = Math.min(thatloc[i], thatloc[i] + thatsz[i]);
	    min2 = Math.min(out.loc[i], out.loc[i] + out.size[i]);
	    max1 = Math.max(thatloc[i], thatloc[i] + thatsz[i]);
	    max2 = Math.max(out.loc[i], out.loc[i] + out.size[i]);
	    out.loc[i] = Math.max(min1, min2);
	    out.size[i] = Math.min(max1, max2) - out.loc[i];
	    if (size[i]*out.size[i] < 0) {
		out.loc[i] += out.size[i];
		out.size[i] *= -1;
	    }
	}

	return out;
    }

    /**
     * return true if another Volume (in principle) samples the same 
     * data as this Volume.
     */
    public boolean equals(Volume vol) {
	int i;
	Volume thisvol = (Volume) this.clone();
	Volume thatvol = (Volume) vol.clone();

	thisvol.makeLengthsPositive();
	thatvol.makeLengthsPositive();

	for(i=0; i < thisvol.naxes && i < thatvol.naxes; i++) {
	    if (thisvol.loc[i]    != thatvol.loc[i]   ||
		thisvol.size[i]   != thatvol.size[i]  ||
		thisvol.sample[i] != thatvol.sample[i])   return false;
	}
	if (thisvol.naxes != thatvol.naxes) {
	    Volume larger = (thisvol.naxes > thatvol.naxes) ? 
		                                   thisvol : thatvol;
	    for(; i < thatvol.naxes; i++) {
		if (larger.loc[i] != 0 || 
		    larger.getTrueLength(i) != 1)    return false;
	    }
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
	double[] sz = { 5.0, 27.5, 1.0 };
	Voxel szvox;

	Volume my = new Volume(4, data, sz, null);
	szvox = new Voxel(4, my.getSize());
	Voxel mytmp = my.getVoxel();
	System.out.println("My volume lives in " + my.getNaxes() + 
			   "-space at: " + mytmp);
	System.out.println("It has a size of: " + szvox);
	if (args.length == 0) return;
	
	Volume your = new Volume(args.length);
	Voxel tmp = new Voxel(args.length);
	for(i=0; i < args.length; i++) {
	    tmp.setAxisPos(i, new Double(args[i]).doubleValue());
	}
	your.setLocation(tmp.getValues(3), 3);
	Voxel yourtmp = your.getVoxel();
	System.out.println("Your volume lives in " + your.getNaxes() + 
			   "-space: " + yourtmp);
	System.out.println("The length along the 1st axis is " + 
			    your.getLength(1));

	Volume mineinyours = new Volume(your.getNaxes());
	mineinyours.setLocation(my.getLocation(0));
	tmp = mineinyours.getVoxel();
	System.out.println("My voxel intersects your space at: " +
			    tmp);
    }

}

