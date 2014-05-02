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

import ncsa.horizon.util.*;

/**
 * A abstract interface representing n-dimensional(nd) data array.
 * The data could be on memory or cached with some
 * implementer-dependent policy.
 * It is a observable class.
 */

public class DataSlice {
  // this one is used to return volume
  private Slice slice;
  // x, y origin in the slice plane
  private int xCoord, yCoord;
  private int xLength, yLength;
  // the size of the whole data
  private JavaType javaType;
  // 1d array
  private Object array;

  public DataSlice(NdArrayData dataVolume, Slice slice) {
    this.slice = slice;
    javaType = dataVolume.getType();
    int[] sliceStart = ArrayTypeConverter.
      arrayDoubleToInt(slice.getLocation());
    int xaxis = slice.getXaxis();
    int yaxis = slice.getYaxis();
    xLength = slice.getTrueLength(xaxis);
    yLength = slice.getTrueLength(yaxis);
    xCoord = sliceStart[xaxis];
    yCoord = sliceStart[yaxis];
    array = dataVolume.getValue(sliceStart, slice.getTrueSize());
    if (array == null) {
      array = new int[xLength * yLength];
    }
  }

  public Slice getSlice() {
    return (Slice) slice.clone();
  }

  /**
   * Return the data type
   */
  public JavaType getType() {
    return javaType;
  }

  public int getXaxisLength() {
    return xLength;
  }

  public int getXaxisLocation() {
    return xCoord;
  }

  public int getYaxisLength() {
    return yLength;
  }

  public int getYaxisLocation() {
    return yCoord;
  }

  /**
   * (x, y) are coord regard to slice's origin
   */
  public Object getValue(int x, int y) {
    return javaType.wrappedValueFromArray(array, y*xLength + x);
  }

  /**
   * Get all the element as one-d array.  The reference
   * to the memory array is returned.
   */
  public Object getValue() {
    return array;
  }
}
