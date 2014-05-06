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
 * History: 
 * 97Sep25  Wei Xie  1st edition
 * 98Jan5   Wei Xie  Modify wrap methods to throw an exception instead of
 *                   return null when asked to wrap a data of different type.
 */

package ncsa.horizon.util;

/**
 * This class represents java primitive type information. It cannot be
 * instantiated but contains 8 primitive public final static type and
 * 1 object public final static type.  It 
 * also provide a rich set of methods to automatically wrap and unwrap
 * java primitive data, to create primitive data array, to access primitive
 * data array.<br>
 * The necessities of this class are justified in several situations. <br>
 * 1. Suppose a number wrapper nWrapper is returned, but the wrapper 
 * is not know of its type.
 * If we somehow get its corresponding JavaType jType, we can call
 * jType.value(nWrapper) to get the value.  It is especially useful
 * in wide conversion. <br>
 * 2. Allocate a 1-d array of corresponding primitive data type. <br>
 * JDK1.1 provides 9 final static types, which have most of the functionalities
 * of JavaType's 8 static types. However, if you want to allocate a 1-d 
 * array of corresponding primitive data type, JavaType is the way to go.
 */

public final class JavaType {
  // used for switch
  private int code;
  private String name;
  // Wrapper class name for primitive type
  // for instance: name int correspond to
  // classname Integer
  // and Class name for reference type
  // for instance: name object correspont to Object
  private String className;
  private int size;
  private boolean isSizeKnown;

  // size in bits
  private JavaType(int code, String name, String className, 
		   int size, boolean isSizeKnown) {
    this.code = code;
    this.name = name;
    this.className = className;
    this.size = size;
    this.isSizeKnown = isSizeKnown;
  }

  /**
   * Creates a new array with the specified length.  The new array
   * is with primitive type represented by this object.
   */
  public Object allocateArray(int length) throws OutOfMemoryError {
    Object array = null;
    try {
      switch(code) {
      case 1:
	array = new boolean[length];
	break;
      case 2:
	array = new char[length];
	break;
      case 3:
	array = new byte[length];
	break;
      case 4:
	array = new short[length];
	break;
      case 5:
	array = new int[length];
	break;
      case 6:
	array = new long[length];
	break;
      case 7:
	array = new float[length];
	break;
      case 8:
	array = new double[length];
	break;
      case 9:
	array = new Object[length];
	break;
      }
    }
    catch(OutOfMemoryError e) {
      System.out.println("The memory is not large enough to allocate array with type " + 
			 name + " and length " + length + ".");
      throw e;
    }
    return array;
  }

  /**
   * Provide a code to each array for the purpose of switch.
   * The code for the types are: <p>
   * 1 for JavaType.BOOLEAN <p>
   * 2 for JavaType.CHAR <p>
   * 3 for JavaType.BYTE <p>
   * 4 for JavaType.SHORT <p>
   * 5 for JavaType.INT <p>
   * 6 for JavaType.LONG <p>
   * 7 for JavaType.FLOAT <p>
   * 8 for JavaType.DOUBLE <p>
   * 9 for JavaType.OBJECT <p>
   */
  public int code() {
    return code;
  }

  public String name() {
    return name;
  }

  public String className() {
    return className;
  }

  /**
   * Set the element of array at index to be value of the wrapper
   */
  public void setArray(Object array, int index, Object wrapper) {
    switch(code) {
    case 1: {
      boolean[] trueArray = (boolean[]) array;
      trueArray[index] = value((Boolean) wrapper);
      break; }
    case 2: {
      char[] trueArray = (char[]) array;
      trueArray[index] = value((Character) wrapper);
      break; }
    case 3: {
      byte[] trueArray = (byte[]) array;
      trueArray[index] = (byte) value((Integer) wrapper);
      break; }
    case 4: {
      short[] trueArray = (short[]) array;
      trueArray[index] = (short) value((Integer) wrapper);
      break; }
    case 5: {
      int[] trueArray = (int[]) array;
      trueArray[index] = value((Integer) wrapper);
      break; }
    case 6: {
      long[] trueArray = (long[]) array;
      trueArray[index] = value((Long) wrapper);
      break; }
    case 7: {
      float[] trueArray = (float[]) array;
      trueArray[index] = value((Float) wrapper);
      break; }
    case 8: {
      double[] trueArray = (double[]) array;
      trueArray[index] = value((Double) wrapper);
      break; }
    }
  } // end setArray

  /**
   * Returns the type memory saving length.
   * If the length is unknow, java.util.NoSuchElementException will
   * be thrown.
   */
  public int size() throws java.util.NoSuchElementException {
    if(isSizeKnown)
      return size;
    else
      throw new java.util.NoSuchElementException("The size of this type is unknown.");
  }

  public boolean isSizeKnown() {
    return isSizeKnown;
  }

  public boolean value(Boolean wrapper) {
    if(code == 1)
      return wrapper.booleanValue();
    else
      throw new RuntimeException(name +
				 "can't get value from Boolean.");
  }
    
  public char value(Character wrapper) {
    if(code == 2)
      return wrapper.charValue();
    else
      throw new RuntimeException(name +
				 "can't get value from Character.");
  }    

  public int value(Integer wrapper) {
    int value = wrapper.intValue();
    if(code == 3) {
      if (value <= 127 && value >= -128)
	return value;
      else
	throw new RuntimeException(name +
			  "can't get value from Integer. Losing info.");
    }
    else if(code == 4) {
      if (value <= 32767 && value >= -32768)
	return value;
      else
	throw new RuntimeException(name +
		           "can't get value from Integer. Losing info.");
    }
    else if(code == 5)
      return wrapper.intValue();
    else
      throw new RuntimeException(name +
				 "can't get value from Integer.");
  }    

  public long value(Long wrapper) {
    if(code == 6)
      return wrapper.longValue();
    else
      throw new RuntimeException(name +
				 "can't get value from Long.");
  }

  public float value(Float wrapper) {
    if(code == 7)
      return wrapper.floatValue();
    else
      throw new RuntimeException(name +
				 "can't get value from Float.");
  }    

  public double value(Double wrapper) {
    if(code == 8)
      return wrapper.doubleValue();
    else
      throw new RuntimeException(name +
				 "can't get value from Double.");
  }    

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Boolean wrap(boolean value) {
    if (code == 1) {
      return new Boolean(value);
    } else {
      throw new NumberFormatException(name + " cannot wrap a boolean");
    }
  }
    
  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Character wrap(char value) {
    if (code == 2) {
      return new Character(value);
    } else {
      throw new NumberFormatException(name + " cannot wrap a char");
    }
  }    

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Integer wrap(byte value) {
    if (code == 3) {
      return new Integer((int) value);
    } else {
      throw new NumberFormatException(name + " cannot wrap a byte");
    }
  }    

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Integer wrap(short value) {
    if(code == 4)
      return new Integer((int) value);
    else {
      throw new NumberFormatException(name + " cannot wrap a short");
    }
  }    

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Integer wrap(int value) {
    if(code == 5)
      return new Integer(value);
    else {
      throw new NumberFormatException(name + " cannot wrap an int");
    }
  }    

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Long wrap(long value) {
    if(code == 6)
      return new Long(value);
    else {
      throw new NumberFormatException(name + " cannot wrap an long");
    }
  }

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Float wrap(float value) {
    if(code == 7)
      return new Float(value);
    else {
      throw new NumberFormatException(name + " cannot wrap an float");
    }
  }    

  /**
   * The overloading methods wrap corresponding
   * primitive data type, and returns the wrapper.
   * Since no Byte for byte and no Short for
   * short in JDK1.02, byte and short are wrapped 
   * in Integer.<br>
   * If this javaType is asked to wrap a data with
   * other data type, NumberFormatException is thrown.
   */
  public Double wrap(double value) {
    if(code == 8)
      return new Double(value);
    else {
      throw new NumberFormatException(name + " cannot wrap an double");
    }
  }    

  public Object wrappedValueFromArray(Object array, int index) {
      switch(code) {
      case 1: {
	boolean[] trueArray = (boolean[]) array;
	return wrap(trueArray[index]); }
      case 2: {
	char[] trueArray = (char[]) array;
	return wrap(trueArray[index]);}
      case 3: {
	byte[] trueArray = (byte[]) array;
	return wrap(trueArray[index]); }
      case 4: {
	short[] trueArray = (short[]) array;
	return wrap(trueArray[index]); }
      case 5: {
	int[] trueArray = (int[]) array;
	return wrap(trueArray[index]); }
      case 6: {
	long[] trueArray = (long[]) array;
	return wrap(trueArray[index]);}
      case 7: {
	float[] trueArray = (float[]) array;
	return wrap(trueArray[index]); }
      case 8: {
        double[] trueArray = (double[]) array;
	return wrap(trueArray[index]); }
      }
      return null;
  }
    
  /**
   * code: 1, name: "boolean", className: "Java.Lang.Boolean"
   * data size: unknown
   */
  public static final JavaType BOOLEAN = new JavaType(1, "boolean", 
				      "Java.Lang.Boolean", 0, false);
  /**
   * code: 2, name: "char", className: "Java.Lang.Character"
   * data size: 16
   */
  public static final JavaType CHAR = new JavaType(2, "char", 
				      "Java.Lang.Character", 16, true);
  /**
   * code: 3, name: "byte", className: "Java.Lang.Integer"
   * data size: 8
   */
  public static final JavaType BYTE = new JavaType(3, "byte", 
				      "Java.Lang.Integer", 8, true);
  /**
   * code: 4, name: "short", className: "Java.Lang.Integer"
   * data size: 16
   */
  public static final JavaType SHORT = new JavaType(4, "short",
				      "Java.Lang.Integer", 16, true);
  /**
   * code: 5, name: "int", className: "Java.Lang.Integer"
   * data size: 32
   */
  public static final JavaType INT = new JavaType(5, "int",
				     "Java.Lang.Integer", 32, true);
  /**
   * code: 6, name: "long", className: "Java.Lang.Long"
   * data size: 64
   */
  public static final JavaType LONG = new JavaType(6, "long",
				      "Java.Lang.Long", 64, true);
  /**
   * code: 7, name: "float", className: "Java.Lang.Float"
   * data size: 32
   */
  public static final JavaType FLOAT = new JavaType(7, "float",
				      "Java.Lang.Float", 32, true);
  /**
   * code: 8, name: "double", className: "Java.Lang.Double"
   * data size: 64
   */
  public static final JavaType DOUBLE = new JavaType(8, "double",
				      "Java.Lang.Double", 64, true);
  /**
   * code: 9, name: "Object", className: "Java.Lang.Object"
   * data size: unknown
   */
  public static final JavaType OBJECT = new JavaType(9, "Object",
				      "Java.Lang.Object", 0, false);

}
