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
 */

package ncsa.horizon.data;

import java.io.InputStream;
import ncsa.horizon.util.*;

/**
 * Abstract n-dimension Reader class.  It generalize the methods
 * a n-dimension Reader should provide.  A NdArrayReader is able to
 * extract n-dimensional data from a file or network connection.
 * The implemention depends on the file format or network protocol.
 */
public abstract class NdArrayReader {
  /**
   * Return how many nd arrays is available
   */
  public int NumberOfNdarray() {
    return 1;
  }

  /**
   * Return the data size in byte for the 
   * first Nd array.  For example,
   * if data type is int, dataSize will return
   * 4.
   */
  public abstract int dataSize();

  /**
   * Return the data size in byte for the 
   * which_array Nd array, starting from 0 to
   * NumberOfNdarray - 1.  For example,
   * if data type is int, dataSize will return
   * 4.
   */
  public int dataSize(int which_array) {
    return dataSize();
  }

  /**
   * Return NdArrayData representing first Nd array.  Default
   * implementation here is return null.
   */
  public NdArrayData getNdArrayData() {
    return null;
  }

  /**
   * Return NdArrayData representing the which_array Nd array.
   */
  public NdArrayData getNdArrayData(int which_array) {
    return getNdArrayData();
  }

  /**
   * Return metadata about the first Nd array.
   */
  public Metadata getMetadata() {
    return null;
  }

  /**
   * Return metadata about the which_array Nd array.
   */
  public Metadata getMetadata(int which_array) {
    return null;
  }

  /**
   * Return the number of axes for the first Nd array.
   */
  public abstract int getNaxes();

  /**
   * Return the number of axes for the which_array Nd array.
   */
  public int getNaxes(int which_array) {
    return getNaxes();
  }

  /**
   * Return the axis sizes of the first Nd-array
   */
  public abstract int[] getSize();

  /**
   * Return the axis sizes of the which_array Nd-array
   */
  public int[] getSize(int which_array) {
    return getSize();
  }

  /**
   * Return stream of the reading content
   */
  public abstract InputStream getStream();

  /**
   * return data type of the first nd array
   */
  public abstract JavaType getType();

  /**
   * return data type of the which_array nd array
   */
  public JavaType getType(int which_array) {
    if(which_array == 0)
      return getType();
    else
      return null;
  }

  /**
   * return a VolumeUpdater class for the first
   * nd array if the more refined
   * nd array data is coming from networks.
   * If null returned, it means that the data
   * are final.
   */
  public NdArrayDataUpdater getUpdater() {
    return null;
  }

  /**
   * return a VolumeUpdater class if the more refined
   * nd array data is coming from networks
   * If null returned, it means that the data
   * are final.
   */
  public NdArrayDataUpdater getUpdater(int which_array) {
    return null;
  }

  /**
   * Return the double value at location coord 
   * of the first nd array.
   * IllegalArgumentException will be thrown if
   * coord is out of bound of int[] getSize.
   */
  public abstract double getValue(int[] coord) throws
             IllegalArgumentException;

  /**
   * Return the double value at location coord 
   * of the which_array nd array.
   * IllegalArgumentException will be thrown if
   * coord is out of bound of int[] getSize.
   */
  public double getValue(int[] coord, int which_array) throws
  IllegalArgumentException {
    return getValue(coord);
  }

  /**
   * Return the 1d double value array
   * of the first nd array
   */
  public abstract Object getValue();

  /**
   * Return the 1d double value array
   * of the which_array nd array
   */
  public Object getValue(int which_array) {
    return getValue();
  }

  /**
   * Tell if the current available data 
   * about Nd Array is final.  Default implementation
   * returns true;
   */
  public boolean isFinal() {
    return true;
  }

  /**
   * Tell if the current available data 
   * about which_array Nd Array is final.
   * Default implementation
   * returns true;
   */
  public boolean isFinal(int which_array) {
    return true;
  }

  public String readByKey(String key) {
    return null;
  }

  public String readByKey(String key, int which_array) {
    return null;
  }

}
