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

package ncsa.horizon.awt.image;

/**
 * Lookup table which converts a int or int array ranges 
 * from 0 to table size - 1 a byte or int or byte or int 
 * array that can be feed into java image consumer to set
 * pixels of a java image.
 */
public class SimpleColorLut extends Lut{
  private int size;
  private int[] table;

  SimpleColorLut() {
    size = 1024;
    table = new int[size];
    int r, g, b;
    for(int i = 0; i < size; i++) {
      r = (i * 3) >> 3;
      g = i >> 2;
      // g = g > 120 ? g : 70;
      b = i >> 1;
      b = b > 255 ? 255 : b;
      table[i] = (255 << 24) | (r << 16) | (g << 8)
	         | b;
    }      
  }

  /**
   * return size of the table
   */
  public int getSize() {
    return size;
  }

  /**
   * This is a color lookup table, this mothod is not supported.
   */
  public byte lookup(byte index) {
    return 0;
  }

  /**
   * Look into the table and return the grayscale content
   * The index range from 0 to table size - 1
   */
  public int lookup(int index) {
    try {
      return table[index];
    }
    catch(IndexOutOfBoundsException e) {
      System.err.println(e);
      if(index >= size)
	return table[size - 1];
      else
	return table[0];
    }
  }

  /**
   * This is a color lookup table, this mothod is not supported.
   */
  public byte[] lookup(byte[] index) {
    return null;
  }

  /**
   * Look into the table and return the grayscale content
   * The index range from 0 to table size - 1
   */
  public int[] lookup(int[] index) {
    int[] results = new int[index.length];
    for(int i = 0; i < index.length; i++)
      results[i] = lookup(index[i]);
    return results;
  }

}
