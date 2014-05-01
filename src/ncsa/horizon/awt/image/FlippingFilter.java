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

import java.awt.image.*;
import java.util.Hashtable;
import java.awt.Rectangle;

/**
 * An ImageFilter class for flipping images.  The image will be
 * flipped upside down.
 * It is meant to be used in conjunction with a FilteredImageSource
 * object to produce flipped versions of existing images.
 *
 */
public class FlippingFilter extends ImageFilter {

  public void setPixels(int x, int y, int w, int h,
			ColorModel model, byte pixels[], int off,
			int scansize) {
    byte[] newPixels = new byte[w * h];
    for (int i = 0; i < h; i ++) {
      System.arraycopy(pixels, off + i * scansize, newPixels, (h - 1 - i) * w, w);
    }

    consumer.setPixels(0, 0, w, h, model, newPixels, 0, w);
    }
    
  /**
   * Determine if the delivered int pixels intersect the region to
   * be extracted and pass through only that subset of pixels that
   * appear in the output region.
   */
  public void setPixels(int x, int y, int w, int h,
			ColorModel model, int pixels[], int off,
			int scansize) {
    int[] newPixels = new int[w * h];
    for (int i = 0; i < h; i ++) {
      System.arraycopy(pixels, off + i * scansize, newPixels, (h - 1 - i) * w, w);
    }
    consumer.setPixels(0, 0, w, h, model, newPixels, 0, w);
    // ??consumer.setPixels(x, y, w, h, model, newPixels, off, w);
  }
}
