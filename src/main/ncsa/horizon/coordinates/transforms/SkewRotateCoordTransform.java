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
 *-------------------------------------------------------------------------
 * History: 
 *  96mar26  rlp  Original version;
 *  97jul15  rlp  added hasChanged() calls prior to all notifyObservers().
 */
package ncsa.horizon.coordinates.transforms;

import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.CorruptedMetadataException;
import ncsa.horizon.coordinates.*;

/**
 * an object for transforming positions from one coordinate system to 
 * a skewed or rotated system via a (linear) matrix operation.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: SkewRotateCoordTransform.java,v 1.1 1997/08/07 07:43:42 rplante Exp $
 */
public class SkewRotateCoordTransform extends CoordTransform {

    protected int naxes=0;
    protected double[] fmatrix = null;
    protected double[] rmatrix = null;
    protected double[] voxsz   = null;

    public SkewRotateCoordTransform(int naxes, double[] matrix) 
	throws FITSWCS.exceptions.SingularMatrixException
    {
	setForwardMatrix(matrix, naxes);
    }

    public SkewRotateCoordTransform(int naxes, double[] matrix, 
				    double[] voxsz) 
	throws FITSWCS.exceptions.SingularMatrixException
    {
	setForwardMatrix(matrix, voxsz, naxes);
    }

    public synchronized double[] forward(double[] position, int[] axisIndices) {

	double[] out = new double[position.length];

	int i, ij, j;
	double tmp;
	for (i = 0, ij = 0; i < naxes; i++) {
	    if (axisIndices[i] < out.length) {
		out[axisIndices[i]] = 0.0;
		for (j = 0; j < naxes; j++, ij++) {
		    if (axisIndices[j] < position.length) 
			out[axisIndices[i]] += fmatrix[ij] * 
			                          position[axisIndices[j]];
		}
	    }
	}

	return out;
    }

    public synchronized double[] forward(double[] position) {

	double[] out = new double[position.length];

	int i, ij, j;
	double tmp;
	for (i = 0, ij = 0; i < naxes && i < position.length; i++) {
	    out[i] = 0.0;
	    for (j = 0; j < naxes && j < position.length; j++, ij++) {
		out[i] += fmatrix[ij] * position[j];
	    }
	}

	return out;
    }

    public synchronized double[] reverse(double[] position, int[] axisIndices) {

	double[] out = new double[position.length];

	int i, ij, j;
	double tmp;
	for (i = 0, ij = 0; i < naxes; i++) {
	    if (axisIndices[i] < out.length) {
		out[axisIndices[i]] = 0.0;
		for (j = 0; j < naxes; j++, ij++) {
		    if (axisIndices[j] < position.length) 
			out[axisIndices[i]] += rmatrix[ij] * 
			                          position[axisIndices[j]];
		}
	    }
	}

	return out;
    }

    public synchronized double[] reverse(double[] position) {

	double[] out = new double[position.length];

	int i, ij, j;
	double tmp;
	for (i = 0, ij = 0; i < naxes && i < position.length; i++) {
	    out[i] = 0.0;
	    for (j = 0; j < naxes && j < position.length; j++, ij++) {
		out[i] += rmatrix[ij] * position[j];
	    }
	}

	return out;
    }

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
     * return the maximum number of axes this transform operates on
     */
    public int getMaxNaxes() { return naxes; }

    /**
     * set the forward matrix and voxel size
     * @param matrix   the matrix assuming voxels of unit size; missing 
     *                 diagonal elements will be set to 1, missing non-diagonal
     *                 elements will be set to 0.
     * @param voxsz    the voxel size; missing elements will be assumed to be
     *                 one.  matrix will be vector-multiplied by this size.
     * @param naxes    the number of axes being operated on 
     * @exception FITSWCS.SingularMatrixException if the input matrix 
     *                 cannot be inverted.
     */
    public synchronized void setForwardMatrix(double[] matrix, double[] voxsz,
					      int naxes) 
	throws FITSWCS.exceptions.SingularMatrixException
    {
	int i, ij, j;
	double[] tmp = new double[naxes*naxes];
	System.err.println("Preparing " + naxes + " by " + naxes + " matrix");
	for(i=0, ij=0; i < naxes; i++) {
	    for(j=0; j < naxes; j++, ij++) {
		tmp[ij] = (ij < matrix.length) ? matrix[ij] :
		                                 ((i == j) ? 1.0 : 0.0);
		if (i < voxsz.length) tmp[ij] *= voxsz[i];
	    }
	}
	rmatrix = FITSWCS.LinearTransform.matinv(naxes, tmp);
	fmatrix = tmp;
	this.naxes = naxes;

	this.voxsz = new double[naxes];
	System.arraycopy(voxsz, 0, this.voxsz, 0, 
			 (voxsz.length < naxes) ? voxsz.length : naxes);
	for(i=voxsz.length; i < naxes; i++) voxsz[i] = 1.0;
	hasChanged();
	notifyObservers();
    }

    /**
     * set the forward matrix
     * @param matrix   the matrix assuming voxels of unit size; missing 
     *                 diagonal elements will be set to 1, missing non-diagonal
     *                 elements will be set to 0.
     * @param naxes    the number of axes being operated on 
     * @exception FITSWCS.SingularMatrixException if the input matrix 
     *                 cannot be inverted.
     */
    public synchronized void setForwardMatrix(double[] matrix, int naxes) 
	throws FITSWCS.exceptions.SingularMatrixException
    {
	int i, ij, j;
	double[] tmp;
	if (matrix.length < naxes*naxes) {
	    tmp = new double[naxes*naxes];
	    for(i=0, ij=0; i < naxes; i++) {
		for(j=0; j < naxes; j++, ij++) {
		    tmp[ij] = (ij < matrix.length) ? matrix[ij] :
		                                     ((i == j) ? 1.0 : 0.0);
		}
	    }
	} 
	else {
	    tmp = matrix;
	}

	rmatrix = FITSWCS.LinearTransform.matinv(naxes, matrix);
	fmatrix = matrix;
	this.naxes = naxes;
	this.voxsz = null;
	hasChanged();
	notifyObservers();
    }

    /**
     * create a copy of this Transform
     */
    public synchronized Object clone() {
	SkewRotateCoordTransform out = 
	    (SkewRotateCoordTransform) super.clone();
	out.fmatrix = new double[fmatrix.length];
	System.arraycopy(fmatrix, 0, out.fmatrix, 0, fmatrix.length);
	out.rmatrix = new double[rmatrix.length];
	System.arraycopy(rmatrix, 0, out.rmatrix, 0, rmatrix.length);

	if (voxsz != null) {
	    out.voxsz = new double[naxes];
	    System.arraycopy(voxsz, 0, out.voxsz, 0, voxsz.length);
	}

	return out;
    }

    /**
     * update the input Metadata object to reflect the changes that this
     * tranform makes to a coordinate position.  In general, this method 
     * will actually edit the contents of the input Metadata when changes
     * are necessary.  
     */
    public synchronized Metadata getMetadata(Metadata in, boolean forward, 
					     int[] axisIndices) 
    { 
	super.getMetadata(in, forward, axisIndices);

	if (voxsz != null) {
	    int i, n;
	    Double R;

	    try {
		Integer N = (Integer) in.getMetadatum("naxes");
		n = (N == null) ? 0 : N.intValue();
	    } catch (ClassCastException ex) {
		throw new CorruptedMetadataException("naxes: not of " + 
						     "Integer type");
	    }
	    if (n <= 0) return in;

	    double[] ss = new double[n];

	    // get the values for the stepsize
	    for(i=0; i < naxes; i++) {
		try {
		    R = (Double) in.getMetadatum("Axes[" + i + "].stepsize");
		    ss[i] = (R == null) ? 1.0 : R.doubleValue();
		} catch (ClassCastException ex) {
		    throw new CorruptedMetadataException("stepsize: " +
							 "not of Double " +
							 "type");
		}
	    }

	    // Now update with the new values
	    for(i=0; i < axisIndices.length; i++) {
		if (axisIndices[i] < n) 
		    CoordMetadata.setAxisStepsize(in, axisIndices[i], 
						  ss[axisIndices[i]] );
	    }
	}

	return in;
    }

}
