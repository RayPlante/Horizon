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
 */

/**
 * Convert an array with primitive data type to an
 * array with different primitive data type
 */
package ncsa.horizon.util;
public class ArrayTypeConverter {
  /**
   * Narrowing Conversion.  Information might be lost.
   */
  public static int[] arrayDoubleToInt(double[] array) {
    int length = array.length;
    int[] iarray = new int[length];
    for (int i = 0; i < length; i++) {
      iarray[i] = (int) array[i];
    }
    return iarray;
  }

  public static double[] arrayIntToDouble(int[] array) {
    int length = array.length;
    double[] darray = new double[length];
    for (int i = 0; i < length; i++) {
      darray[i] = (double) array[i];
    }
    return darray;
  }
}
