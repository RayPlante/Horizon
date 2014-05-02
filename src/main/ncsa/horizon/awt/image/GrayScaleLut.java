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
public class GrayScaleLut extends RainbowLut {

  char[] tableValues() {
    char[] charArray = new char[3*size];
    for (char i = 0; i < size; i++) {
      int j = i * 3;
      charArray[j]   = i;
      charArray[j+1] = i;
      charArray[j+2]  = i;
    };
    return charArray;
  }
}
