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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 */

/*
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    19-Jan-1998 Ray Plante  moved from ncsa.horizon.util to ncsa.horizon.data
 */

package ncsa.horizon.data;

import java.util.Observable;
import ncsa.horizon.util.*;

/**
 * A abstract interface representing n-dimensional(nd) data array.
 * The data could be on memory or cached with some
 * implementer-dependent policy.
 * It is a observable class.
 */

public abstract class NdArrayData extends Observable {

  /**
   * Return the intersected part of this NdArrayData
   */
  public abstract NdArrayData getNdArrayData(Volume vol);

  /**
   * Get the number of axes of this nd data array
   */
  public abstract int getNaxes();

  /**
   * Return a int array represent the 
   * dimensions of the volume.
   */
  public abstract int[] getSize();

  /**
   * Return the data type
   */
  public abstract JavaType getType();

  /**
   * Return the value at the index.  The element
   * is located by treating the whole nd array
   * as 1d array. Check data type to know how
   * it is wrapped by call getType().
   */
  public abstract Object getValue(int index);

  /**
   * Return the value at the coord.
   * Coord should be getNaxes dememsion.
   */
  public abstract Object getValue(int[] coord);

  /**
   * Get 1d array elements of a sub nd array starting at startCoord 
   * with size to be value.  The element
   * is located by coord.
   */
  public abstract Object getValue(int[] startCoord, int[] size);

  /**
   * Get all the element as one-d array.
   */
  public abstract Object getValue();

  /**
   * Return a copy of the volume.
   */
  public abstract Volume getVolume();

  /**
   * Check if the data are final.
   */
  public abstract boolean isComplete();

  /**
   * Set the data to be final.
   */
  public abstract void setComplete();

  /**
   * Set length elements to be value, starting
   * at startIndex.  The element
   * is located by treating the whole nd array
   * as 1d array. The receiver will treat value
   * as default saving type.
   */
  public abstract void setValue(int startIndex, int length, Object value);

  /**
   * Set an element to be value.  The element
   * is located by treating the whole nd array
   * as 1d array. The receiver will treat value
   * as default saving type.
   */
  public abstract void setValue(int index, Object value);

  /**
   * Set an element to be value.  The element
   * is located by coord.
   */
  public abstract void setValue(int[] coord, Object value);

  /**
   * Set elements of a sub nd array starting at startCoord 
   * with size to be value.  The element
   * is located by coord.
   */
  public abstract void setValue(int[] startCoord, int[] size, Object value);

  /**
   * Set the nd array.  data is an one dimensiional array.
   * Its data type must be the same as this NdArrayData 
   * instance's data type.
   * Otherwise, a runtime exception will be thrown. <p>
   * The 1-d array data holds data of a n-dimensional array.
   * Client can call getNaxes() and getSize() to retrieve
   * the attribute about the n-d array.
   * Every element of n-dimensional array is saved in
   * the 1-d array in the order from low axis to high axis. <p>
   * For example, the index of a 
   * 4-d array simplearray's element simplearray[i][j][k][l]
   * is index[] with index[0] == i, index[1] == j,
   * index[2] == k, index[3] == l in array form.  Its
   * index number of the 1-d array is l*size[2]*size[1]*size[0] + 
   * k*size[1]*size[0] + j*size[0] + i.  size[i] is 
   * the size of simplearray's ith dimension.
   * @param data an one dimensiional array to set the 1-d storage from
   *             0 to data.length
   */
  public abstract void setValues(Object data);
}
