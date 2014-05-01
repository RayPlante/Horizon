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
public abstract class Lut {

  /*
   * 0 SimpleColorLut
   * 1 RainbowLut
   * 2 bandedRainbowLut
   * 3 invertedRainbowLut
   * 4 StripedRainbowLut
   * 5 GrayScaleLut
   * 6 BandedGrayScaleLut
   */
  private static Lut[] concreteLuts = new Lut[7];

  public byte[] getBlue() {
    return null;
  }

  public byte[] getGreen() {
    return null;
  }

  public byte[] getRed() {
    return null;
  }

  public int[] getRGB() {
    return null;
  }

  /**
   * return size of the table
   */
  public abstract int getSize();

  /**
   * Look into the table and return the grayscale content
   * The index range from -128 to 127
   */
  public abstract byte lookup(byte index);

  /**
   * Look into the table and return the grayscale content
   * The index range from 0 to table size - 1
   */
  public abstract int lookup(int index);

  /**
   * Look into the table and return the grayscale content
   * The index range from -128 to 127
   */
  public abstract byte[] lookup(byte[] index);

  /**
   * Look into the table and return the grayscale content
   * The index range from 0 to table size - 1
   */
  public abstract int[] lookup(int[] index);

  /**
   * Return a SimpleColorLut, keep the solity.
   */
  public static Lut getSimpleColorLut() {
    if(concreteLuts[0] == null) {
      concreteLuts[0] = new SimpleColorLut();
    }
    return concreteLuts[0];
  }

  /**
   * Return a RainbowLut, keep the solity.
   */
  public static Lut getRainbowLut() {
    if(concreteLuts[1] == null) {
      concreteLuts[1] = new RainbowLut();
    }
    return concreteLuts[1];
  }

  /**
   * Return a BandedRainbowLut, keep the solity.
   */
  public static Lut getBandedRainbowLut() {
    if(concreteLuts[2] == null) {
      concreteLuts[2] = new BandedRainbowLut();
    }
    return concreteLuts[2];
  }

  /**
   * Return a InvertedRainbowLut, keep the solity.
   */
  public static Lut getInvertedRainbowLut() {
    if(concreteLuts[3] == null) {
      concreteLuts[3] = new InvertedRainbowLut();
    }
    return concreteLuts[3];
  }

  /**
   * Return a StripedRainbowLut, keep the solity.
   */
  public static Lut getStripedRainbowLut() {
    if(concreteLuts[4] == null) {
      concreteLuts[4] = new StripedRainbowLut();
    }
    return concreteLuts[4];
  }

  /**
   * Return a GrayScaleLut, keep the solity.
   */
  public static Lut getGrayScaleLut() {
    if(concreteLuts[5] == null) {
      concreteLuts[5] = new GrayScaleLut();
    }
    return concreteLuts[5];
  }

  /**
   * Return a BandedGrayScaleLut, keep the solity.
   */
  public static Lut getBandedGrayScaleLut() {
    if(concreteLuts[6] == null) {
      concreteLuts[6] = new BandedGrayScaleLut();
    }
    return concreteLuts[6];
  }

}
