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
 *-------------------------------------------------------------------------
 * History: 
 *  97jun06  Wei Xie     1st edition
 *  97Nov26  Wei Xie     implement getNdArrayData(Volume vol);
 *  98Jan19  Ray Plante  moved from ncsa.horizon.util to ncsa.horizon.data
 */

package ncsa.horizon.data;

import ncsa.horizon.util.*;

/**
 * The class keeps a volume and its corresponding 
 * data on memory.  The data on memory are expecting
 * to be changed.  The possible situation is that
 * a InMemoryData is constructed quickly to put
 * into service.  It's 
 * initial on-memory data are defaults or primitive
 * approximation of real data.  Then
 * the data are updated from a approximation to
 * real data.
 */
public class InMemoryData extends NdArrayData {
  // this one is used to return volume
  private Volume volume;
  private int naxes;
  private int[] isize;
  // the size of the whole data
  private int size;
  private JavaType javaType;
  // 1d array
  private Object array;
  // if it's true, then on-memory data are 
  // complete.
  private boolean flag_complete;

  /**
   * Disabled.
   */
  protected InMemoryData() {
    ;
  }

  /**
   * Create a InMemoryData instance given volume and the data type
   * of data elements.  The actual data is not set yet.
   * @param volume volume contains information about number of axes, 
   *               NdArrayData dimension.
   * @param type data type, a 1-d array is allocated based on
   *             this information.
   */
  public InMemoryData(Volume volume, JavaType type)
    throws InstantiationException {
    this.volume = new Volume(volume);
    javaType = type;
    naxes = volume.getNaxes();
    isize = volume.getTrueSize();
    size = (int) NdArrayMath.size(isize);
    try {
      // may likely to get out of memeory
      array = type.allocateArray(size);
    } catch (OutOfMemoryError e) {
      System.err.println(getClass().getName() + e);
      throw new InstantiationException(getClass().getName() + e +
				   "Maybe not enough memory space to hold all the data");
    }
    flag_complete = false;
  }

  /**
   * create a InMemoryData instance given NdArrayData dimension,
   * data type, a 1-d array containing data, and a flag indicating
   * if the data is completed.
   * @param naxes number of axes.
   * @param isize dimensions of n-d array.
   * @param type data type
   * @param data real data conained in 1-d array.
   * @param isFinal a flag indicating if the data is complete.
   */
  public InMemoryData(int naxes, int[] isize,
		      JavaType type, Object data, boolean isFinal) {
    this.naxes = naxes;
    this.isize = isize;
    javaType = type;
    array = data;
    flag_complete = isFinal;
  } 

  /**
   * Return number of axes.
   */
  public int getNaxes() {
    return naxes;
  }

  /**
   * May return null if there is not enough memeory
   * to created a new NdArrayData of desired dimension.
   * The case of return null is not likely to
   * happen since this NdArrayData already instantiates.
   */
  public NdArrayData getNdArrayData(Volume vol) {
    NdArrayData out = null;
    try {
      Volume intersectVolume = volume.intersection(vol);
      out = new InMemoryData(intersectVolume, getType());
      int startCoord[] = ArrayTypeConverter.arrayDoubleToInt(intersectVolume.getLocation());
      int out_size[] = intersectVolume.getTrueSize();
      out.setValues(getValue(startCoord, out_size));
    } catch (InstantiationException e) {
      System.err.println(e);
      return null;
    }
    return out;
  }

  /**
   * Return a double array represent the 
   * dimensions of the volume.
   */
  public int[] getSize() {
    return isize;
  }

  /**
   * Return the data type
   */
  public JavaType getType() {
    return javaType;
  }

  /**
   * Return the data element at index.  The index is for 
   * the 1-d array.
   */
  public Object getValue(int index) {
    return javaType.wrappedValueFromArray(array, index);
  }

  /**
   * Return the data element at coord.  The coord is for
   * the n-d array.
   * Return null if coord out of range.
   */
  public Object getValue(int[] coord) {
    int[] realCoord = realStartCoord(coord);
    int number = 0;
    try {
      number = (int) (NdArrayMath.indexArrayToNumber(realCoord, isize));
    } catch(IllegalArgumentException e) {
      return null;
    }
    return getValue(number);
  } //end InMemoryData.getValue

  /**
   * Get 1d array elements of a sub nd array starting at startCoord 
   * with size to be size.
   * May return null if the required sub nd array out of range of this
   * volume.   */
  public Object getValue(int[] startCoord, int[] out_size) {
    int length = (int) NdArrayMath.size(out_size);
    Object outArray = javaType.allocateArray(length);
    int[] realStartCoord = realStartCoord(startCoord);
    int src_position = (int) NdArrayMath.indexArrayToNumber(realStartCoord, isize);
    int dst_position = 0;
    // find the largest continue unit of data
    int unitLength = 1;
    int jumpLength = 1;
    for(int i = 0; i < out_size.length; i++) {
      unitLength *= out_size[i];
      if(out_size[i] < isize[i]) {
        jumpLength *= isize[i];
        break;
      }
      else if(out_size[i] > isize[i]) {
        System.err.println(getClass().getName() +
                                   "size out of range.");
        return null;
      }
      else
        jumpLength = unitLength;
    }
    try {
      while(length > 0) {
        System.arraycopy(array, src_position, outArray, dst_position, unitLength);
        src_position += jumpLength;
        dst_position += unitLength;
        length -= unitLength;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.println(getClass().getName() +
                         "size out of range.");
      return null;
    }
    return outArray;
  }

  /**
   * Get all the element as one-d array.  The reference
   * to the memory array is returned.
   */
  public Object getValue() {
    return array;
  }

  /**
   * Return a copy of the volume.
   */
  public Volume getVolume() {
    double[] sampling = new double[naxes];
    for(int i = 0; i < naxes; i++)
      sampling[i] = 1.0;
    if(volume == null)
      volume = new Volume(naxes, new double[naxes], ArrayTypeConverter.arrayIntToDouble(isize),
			  sampling);
    return new Volume(volume);
  }

  /**
   * Test if the data is complete.
   */
  public boolean isComplete() {
    return flag_complete;
  }

  protected int[] realStartCoord(int[] startCoord) {
    double[] volumeStart = volume.getLocation();
    int[] realStartCoord = new int[naxes];
    for (int i = 0; i < naxes; i++) {
      realStartCoord[i] = startCoord[i] - (int) volumeStart[i];
    }
    return realStartCoord;
  }

  /**
   * Set the data to be complete.
   */
  public void setComplete() {
    flag_complete = true;
  }

  /**
   * Set the data element at index with wrappedValue.
   * Index is for the 1-d array, which is the actual form
   * of on-memory storage for the nd array.
   */
  public void setValue(int index, Object wrappedValue) {
    javaType.setArray(array, index, wrappedValue);
    setChanged();
  }

  /**
   * Set the data element at coord with wrappedValue.
   * Index is for the nd array.
   */
  public void setValue(int[] coord, Object wrappedValue) {
    int number = (int) (NdArrayMath.indexArrayToNumber(coord, isize));
    setValue(number, wrappedValue);
    setChanged();
  } //end InMemoryData.setValue

  /**
   * Set length elements to be value, starting
   * at startIndex.  The element
   * is located by treating the whole nd array
   * as 1d array. The receiver will treat value
   * as default saving type.
   */
  public void setValue(int startIndex, int length, Object value) {
    System.arraycopy(value, 0, array, startIndex, length);
    setChanged();
  }

  /**
   * Set elements of a sub nd array starting at startCoord 
   * with size to be value.  The element
   * is located by coord.
   */
  public void setValue(int[] startCoord, int[] size, Object value){
    setChanged();
  }

  /**
   * Set the on-memory 1-dimentional double array.
   * The 1-d array holds data of a n-dimensional array,
   * whose attributes are saved in volume.
   * Every element of n-dimensional array is saved in
   * the 1-d array in the order from low axis to high axis.
   * For example, the index of 
   * 4-d array simplearray's element simplearray[i][j][k][l]
   * is index[4] with index[0] == i, index[1] == j,
   * index[2] == k, index[3] == l in array form.  Its
   * index number of the 1-d array is l*size[2]*size[1]*size[0] + 
   * k*size[1]*size[0] + j*size[0] + i.  size[i] is 
   * the size of simplearray's ith dimension.
   * @param data double array to set the 1-d storage from
   *             0 to data.length
   */
  public void setValues(Object data) {
    System.arraycopy(data, 0, array, 0, size);
    setChanged();
  }

} //end InMemoryData
