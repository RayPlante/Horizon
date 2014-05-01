/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@atmos.ncsa.uiuc.edu
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
 *    14-Jan-1998 Wei Xie     changed private methods to protected for 
 *                              access to subclasses
 *    19-Jan-1998 Ray Plante  updated to NdArray* move to ncsa.horizon.data
 */

// Classes in this file:
// FITSViewable

package ncsa.horizon.viewable;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import ncsa.horizon.awt.ROI;
import ncsa.horizon.awt.image.SliceImageSource;
import ncsa.horizon.awt.image.FlippingFilter;
import ncsa.horizon.util.*;
import ncsa.horizon.data.*;
import ncsa.horizon.coordinates.CoordinateSystem;
import ncsa.horizon.coordinates.FITSCoordMetadata;

public class FITSViewable implements Viewable {

  /**
   * Coordinate system of the nd array.
   * Access it by getCoordSys
   */
  protected CoordinateSystem coord;

  /**
   * meta data about the source data
   */
  protected Metadata metadata;
  // current image is 
  protected Image image;
  protected Slice slice;
  protected NdArrayData dataVolume;

  /**
   * The slice used to gerenerate view image if getView()
   * is called. Access it by getDefaultSlice(), and makeDefaultSlice(Slice).
   */
  protected Slice defaultSlice;

  /**
   * Can't instantiate a FITSViewable without argument.
   */
  protected FITSViewable() {
    ;
  }

  /**
   * Used only for instance instantiation, it is null
   * after Constructor done.
   */
  // Null in all the states.  In fact, it is a local variable
  private NdArrayReader reader;

  /** 
   * New a Viewable to read local FITS file.
   * If a reader can't be created for this file. Exception throws.
   */
  public FITSViewable(String filename) throws InstantiationException {
    reader = new FITSNdArrayReader(filename);
    constructVariables();
  } // end FITSViewable(String name)

  /** 
   * New a Viewable to read through a URL.
   * If a reader can't be created for this file. Exception throws.
   */
  public FITSViewable(URL url) throws InstantiationException {
    reader = new FITSNdArrayReader(url);
    constructVariables();
  } // end FITSViewable(String name)

  // used only for constructor.
  private boolean constructDatavolume() {
    dataVolume = reader.getNdArrayData();
    if (dataVolume == null) {
      return false;
    }
    return true;
    /*
    try {
      int naxes = reader.getNaxes();
      int[] size = reader.getSize();
      try {
        dataVolume = new InMemoryData(naxes, size, reader.getType(), reader.getValue(), false);
      } catch (OutOfMemoryError e) {
        System.err.println(getClass().getName() + e);
        return false;
      }
      if(reader.isFinal())
        dataVolume.setComplete();
      else {
        NdArrayDataUpdater dvu = reader.getUpdater();
        dvu.update(dataVolume);
      }
    } catch(ArrayIndexOutOfBoundsException e)
    {
      System.out.println(e);
      return false;
    }
    return true; */
  }

  // used only for constructor.
  private void constructMetadata() {
    metadata = reader.getMetadata();
  }

  // used only for constructor.
  private Slice createDefaultSlice() {
    return new Slice(dataVolume.getVolume());
  }

  protected Image createImage(Slice sl, ColorModel cm) {
    SliceImageSource sis = null;
    if(cm == null) {
      sis = new SliceImageSource(sl, dataVolume);
    } else {
      sis = new SliceImageSource(sl, dataVolume, cm);
    }
    if(!dataVolume.isComplete()) {
      dataVolume.addObserver(sis);
    }
    image = Toolkit.getDefaultToolkit().createImage(sis);
    return flip(image);
  }

  // works only for Constructors
  private void constructVariables() throws InstantiationException {
    if (constructDatavolume()) {
      constructMetadata();
      reader = null;
    } else {
      reader = null;
      throw new InstantiationException(getClass().getName() + 
				       ": Can't create a NdArrayData.");
    }
  }

  // image corresponding to this.slice.  We want to crop out
  // a part from image corresponding to slice.
  protected Image cropImage(Slice slice) {
    int xaxis = slice.getXaxis();
    int yaxis = slice.getYaxis();
    int w1 = (int) this.slice.getLength(xaxis);
    int h1 = (int) this.slice.getLength(yaxis);
    // find the slice coord reference to the top-left point of this
    // slice
    int x = (int) (slice.axisPos(xaxis) - this.slice.axisPos(xaxis));
    int y = - (int) (slice.axisPos(yaxis) - (this.slice.axisPos(yaxis) + h1));
    int w = (int) slice.getLength(xaxis);
    int h = - (int) slice.getLength(yaxis);
    // since java.awt.image.CropImageFilter doesn't allow negative w or h
    // we need to find the top of rectangle (x, y, w, h)
    Rectangle rec = ROI.getTrueRectangle(new Rectangle(x, y, w, h));
    if((rec.x == 0) && (rec.y== 0) &&
       (w1 == rec.width) && (h1 == rec.height)) {
      return image;
    } else {
      CropImageFilter cif = new CropImageFilter(rec.x, rec.y, rec.width, rec.height);
      FilteredImageSource fis = new FilteredImageSource(image.getSource(),  cif);
      return Toolkit.getDefaultToolkit().createImage(fis);
    }
  }

  public Slice getDefaultSlice() {
    if (defaultSlice == null) {
      defaultSlice = createDefaultSlice();
    }
    return  defaultSlice;
  }
    
  protected Image flip(Image image) {
    ImageFilter filter = new FlippingFilter();
    ImageProducer producer = new FilteredImageSource(
				 image.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(producer);
  }

  public CoordinateSystem getCoordSys() {
    if(coord == null)
      try {
	Metadata cmd = (Metadata) metadata.getMetadatum("CoordinateSystem");
	coord = FITSCoordMetadata.createCoordSys(cmd);
      } catch (ncsa.horizon.coordinates.IllegalTransformException e) {
	System.out.println(e);
	coord = new CoordinateSystem(metadata);
      }
    return coord;
  }

  public NdArrayData getData() {
    return dataVolume;
  }

  public NdArrayData getData(Volume vol) {
    NdArrayData out = dataVolume.getNdArrayData(vol);
    return out;
  }

  public Metadata getMetadata() {
    return new Metadata(metadata);
  } // end FITSViewable.getMetadata

  public int getNaxes() {
    return dataVolume.getNaxes();
  }

  public int[] getSize() {
    return dataVolume.getSize();
  } // end FITSViewable.getSize

  public Image getView() {
    return getView(getDefaultSlice());
  }

  public Image getView(Slice slice) {
    return getView(slice, null, false);
  }

  /**
   * Create a (2-D) view from a slice into the image data and a given color
   * model.  
   * @param slice       region of data to make into an image.  Null is allowed.
   *                    If slice is null, I will use default slice instead.
   * @param colorModel  a java.awt.image.ColorModel to apply to the returned 
   *                    Image object, if possible.  If this is null, use
   *                    the Viewable's default colormodel.
   * @param makeDefault if true make this slice be the viewable's default view,
   *                    if possible
   * <p>
   * @return   A java.awt.Image object (null on failure)
   */
  public Image getView(Slice slice, 
                       ColorModel colorModel, boolean makeDefault) {
    if (slice == null) {
      slice = getDefaultSlice();
    } else if (makeDefault) {
      makeDefaultSlice(slice);
    }
    Volume volume = dataVolume.getVolume();
    // if the given slice is in the different plane, bringing the
    // largest image contain all the data of the new slice plane
    if((this.slice == null) || !isStrictlyInSamePlane(this.slice, slice)) {
      this.slice = slice.projection(volume);
      image = createImage(this.slice, colorModel);
    }
    if(image == null) {
      image = createImage(this.slice, colorModel);
    }

    // this.slice and slice are in the same plane
    // should be changed to be slice.equals(this.slice), 
    // but I don't have equal method right now
    if (this.slice == slice) {
      return image;
    } else {
      return cropImage(slice);
    }
  }

  // what about the case xaxis = 1 yaxis = 0,
  // while other slice yaxis = 1, xaxis = 0
  // now, there are not in the same plane.
  // This is why stupid "strictly" is there.
  protected boolean isStrictlyInSamePlane(Slice s1, Slice s2) {
    int naxes1 = s1.getNaxes();
    if (naxes1 != s2.getNaxes()) {
      return false;
    }
    int xaxis1 = s1.getXaxis();
    int yaxis1 = s1.getYaxis();
    if ((xaxis1 != s2.getXaxis()) ||
	(yaxis1 != s2.getYaxis())) {
      return false;
    }
    double[] loc1 = s1.getLocation();
    double[] loc2 = s2.getLocation();
    for (int i = 0; i < naxes1; i++) {
      if ((i == xaxis1) || (i == yaxis1)) {
	continue;
      }
      if (loc1[i] != loc2[i]) {
	return false;
      }
    }
    return true;
  }

  protected void makeDefaultSlice(Slice slice) {
    defaultSlice = slice;
    metadata.put("defaultSlice", slice);
  }

}
