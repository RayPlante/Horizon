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
 */

/*
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    19-Jan-1998 Ray Plante  moved from ncsa.horizon.util to ncsa.horizon.data
 *    02-Feb-1998 Ray Plante  updated minMax1d for floats and doubles to 
 *                              allow for NaN.
 */

package ncsa.horizon.data;

import ncsa.horizon.util.JavaType;

public final class NdArrayMath
{
  /**
   * Convert the n-d array's index from a length n
   * array, to a index number.  For example the index of 
   * 4-d array simplearray's element simplearray[i][j][k][l]
   * is index[4] with index[0] == i, index[1] == j,
   * index[2] == k, index[3] == l in array form.  Its
   * index number is l*size[2]*size[1]*size[0] + 
   * k*size[1]*size[0] + j*size[0] + i.  size[i] is 
   * the size of simplearray's ith dimension. <br>
   * Two kinds of check is done. if dimension of index
   * is different from that of size, then
   * IllegalArgumentException is thrown with message 
   * "index.length != size.length :: ..."
   * If index is out of bound of size, then IllegalArgumentException
   * is thrown with
   * message "index out of bound of size :: ..." 
   * @param index the index 
   * @param size the size of the nd array 
   */
  public static long indexArrayToNumber(int[] index, int[] size) {
    if (index.length != size.length) {
      throw new IllegalArgumentException("index.length != size.length :: " + 
					 index.length + " != " + size.length);
    }
    int naxes = index.length;
    for (int i = 0; i < naxes; i++) {
      if ((index[i] >= size[i]) || (index[i] < 0) ) {
	throw new IllegalArgumentException("index out of bound of size.");
      }
    }
    long[] multiple = new long[naxes];
    multiple[0] = 1;
    for (int i = 1; i < naxes; i++) {
      multiple[i] = multiple[i - 1] * size[i-1];
    }
    long number = 0;
    for (int i = 0; i < naxes; i++) {
      number += index[i] * multiple[i];
    }
    return number;
  }

  /**
   * Compute the highest 1-d index of the n-d array
   * with size[].
   */
  public static long indexHigh(int[] size) {
    long high = 1;
    for(int i = 0; i < size.length; i++)
      high *= size[i];
    high --;
    return high;
  }

  /**
   * Convert the n-d array's index from an index number
   * to a length n array.  For example the index of 
   * 4-d array simplearray's element simplearray[i][j][k][l]
   * is index[4] with index[0] == i, index[1] == j,
   * index[2] == k, index[3] == l in array form.  Its
   * index number is l*size[2]*size[1]*size[0] + 
   * k*size[1]*size[0] + j*size[0] + i.  size[i] is 
   * the size of simplearray's ith dimension.<BR>
   * The conversion is useful to locate the n-d index
   * from the 1-d storage.
   */
  public static int[] indexNumberToArray(long number, int[] size)
    throws IllegalArgumentException
  {
    if(number < 0)
    {
      throw new IllegalArgumentException("number < 0");
    }
    int naxes = size.length;
    long[] multiple = new long[naxes];
    multiple[0] = 1;
    for(int i = 1; i < naxes; i++)
    {
      multiple[i] = multiple[i - 1] * size[i-1];
    }

    //check if number out of bound
    long bound = multiple[naxes-1] * size[naxes-1] - 1;
    if(number > bound)
      throw new IllegalArgumentException("number > bound :: " +
					 number + " > " + bound);

    int[] index = new int[naxes];
    for(int i = naxes - 1; i >= 0; i--)
    {
      index[i] = (int) (number / multiple[i]);
      number -= index[i] * multiple[i];
    }
    return index;
  } // end indexNumberToArray

  public static byte[] minMax1d(byte[] array) {
    byte min = array[0];
    byte max = array[0];
    for(int i = 1; i < array.length; i++) {
      min = min < array[i] ? min: array[i];
      max = max > array[i] ? max: array[i];
    }
    byte[] limits = {min, max};
    return limits;
  }

  public static short[] minMax1d(short[] array) {
    short min = array[0];
    short max = array[0];
    for(int i = 1; i < array.length; i++) {
      min = min < array[i] ? min: array[i];
      max = max > array[i] ? max: array[i];
    }
    short[] limits = {min, max};
    return limits;
  }

  public static int[] minMax1d(int[] array) {
    int min = array[0];
    int max = array[0];
    for(int i = 1; i < array.length; i++) {
      min = min < array[i] ? min: array[i];
      max = max > array[i] ? max: array[i];
    }
    int[] limits = {min, max};
    return limits;
  }

  public static long[] minMax1d(long[] array) {
    long min = array[0];
    long max = array[0];
    for(int i = 1; i < array.length; i++) {
      min = min < array[i] ? min: array[i];
      max = max > array[i] ? max: array[i];
    }
    long[] limits = {min, max};
    return limits;
  }

  public static float[] minMax1d(float[] array) {
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;
    for(int i = 0; i < array.length; i++) {
      if (min > array[i] && min != Float.NaN) min = array[i];
      if (max < array[i] && max != Float.NaN) max = array[i];
    }
    if (min == Float.POSITIVE_INFINITY) min = max = 0.0f;
    float[] limits = {min, max};
    return limits;
  }

  public static double[] minMax1d(double[] array) {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.POSITIVE_INFINITY;
    for(int i = 0; i < array.length; i++) {
      if (min > array[i] && min != Double.NaN) min = array[i];
      if (max < array[i] && max != Double.NaN) max = array[i];
    }
    if (min == Double.POSITIVE_INFINITY) min = max = 0.0;
    double[] limits = {min, max};
    return limits;
  }

  public static int[] scaleTo(Object arrayObject,
			      int low, int high, JavaType jp) {
    // limits[0] is min(array)
    // limits[1] is max(array)
    double scaleto = (double) (high - low);
    switch(jp.code()) {
    case 3: {
      byte[] array = (byte[]) arrayObject;
      int size = array.length;
      byte[] limits = NdArrayMath.minMax1d(array);
      double scalefrom = (double) (limits[1] - limits[0]);
      int[] results = new int[size];
      double delta = 0.0;
      if (scalefrom != 0.0) {
	for(int i = 0; i < size; i++) {
	  delta = (double) (array[i] - limits[0]);
	  results[i] = low + (int) (scaleto / scalefrom * delta);
	}
      }
      return results; }
    case 4: {
      short[] array = (short[]) arrayObject;
      int size = array.length;
      short[] limits = NdArrayMath.minMax1d(array);
      double scalefrom = (double) (limits[1] - limits[0]);
      int[] results = new int[size];
      double delta = 0.0;
      if (scalefrom != 0.0) {
	for(int i = 0; i < size; i++) {
	  delta = (double) (array[i] - limits[0]);
	  results[i] = low + (int) (scaleto / scalefrom * delta);
	}
      }
      return results; }
    case 5: {
      int[] array = (int[]) arrayObject;
      int size = array.length;
      int[] limits = NdArrayMath.minMax1d(array);
      double scalefrom = (double) (limits[1] - limits[0]);
      int[] results = new int[size];
      double delta = 0.0;
      if (scalefrom != 0.0) {
	for(int i = 0; i < size; i++) {
	  delta = (double) (array[i] - limits[0]);
	  results[i] = low + (int) (scaleto / scalefrom * delta);
	}
      }
      return results; }
    case 6: {
      long[] array = (long[]) arrayObject;
      int size = array.length;
      long[] limits = NdArrayMath.minMax1d(array);
      double scalefrom = (double) (limits[1] - limits[0]);
      int[] results = new int[size];
      double delta = 0.0;
      if (scalefrom != 0.0) {
	for(int i = 0; i < size; i++) {
	  delta = (double) (array[i] - limits[0]);
	  results[i] = low + (int) (scaleto / scalefrom * delta);
	}
      }
      return results; }
    case 7: {
      float[] array = (float[]) arrayObject;
      int size = array.length;
      float[] limits = NdArrayMath.minMax1d(array);
      double scalefrom = (double) (limits[1] - limits[0]);
      int[] results = new int[size];
      double delta = 0.0;
      if (scalefrom != 0.0) {
	for(int i = 0; i < size; i++) {
	  delta = (double) (array[i] - limits[0]);
	  results[i] = low + (int) (scaleto / scalefrom * delta);
	}
      }
      return results; }
    case 8: {
      double[] array = (double[]) arrayObject;
      int size = array.length;
      double[] limits = NdArrayMath.minMax1d(array);
      double scalefrom = limits[1] - limits[0];
      int[] results = new int[size];
      double delta = 0.0;
      if (scalefrom != 0.0) {
	for(int i = 0; i < size; i++) {
	  delta = (double) (array[i] - limits[0]);
	  results[i] = low + (int) (scaleto / scalefrom * delta);
	}
      }
      return results; }
    }
    return null;
  }

  /**
   * Compute the size of a 1d array to hold all the
   * element in nd array with size[]
   */
  public static long size(int[] size) {
    return indexHigh(size) + 1;
  }
}
