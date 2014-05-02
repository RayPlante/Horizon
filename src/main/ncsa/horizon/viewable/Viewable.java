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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  96       jp   Original version
 *  97nov27  wx   change return type of getData() to NdArrayData
 *  97dec04  rlp  added getDefaultSlice() method; updated documentation
 *  98jan19  rlp  updated to NdArray* move to ncsa.horizon.data
 */
package ncsa.horizon.viewable;        

import ncsa.horizon.coordinates.*;
import ncsa.horizon.util.*;
import ncsa.horizon.data.NdArrayData;
import java.lang.*;
import java.awt.Image;
import java.awt.image.ColorModel;

/**
 * Defines the interface for accessing 2 dimensional views into a dataset
 * 
 * @version	Alpha $Id: Viewable.java,v 0.6 1998/01/19 19:47:09 rplante Exp $
 * @author	Horizon team, University of Illinois at Urbana-Champaign
 * @author	Joel Plutchak
 */
public interface Viewable {

   /**
    * Creates a (2-D) view into the image data.  This no-argument version
    * should return a reasonable default view of the image data, e.g., a
    * subsampled version of a very large image, or representative plane from an
    * animation sequence.  Versions with other parameters specific to the
    * type of image data used should be defined, e.g., specifying arbitrary
    * slices from volume data.<p>
    *
    * @return  java.awt.Image   the requested view (null on failure)
    */
   public Image getView();

   /**
    * Create a (2-D) visualization from a slice into the image data.  
    * @param Slice       region of data to make into an image; if null,
    *                    return a default view (as with the no-arg getView()).
    * @return java.awt.Image   the requested view (null on failure)
    */
   public Image getView(Slice slice);

   /**
    * Create a (2-D) view from a slice into the image data and a given color
    * model.  
    * @param Slice       region of data to make into an image; if null,
    *                    return a default view (as with the no-arg getView()).
    * @param colorModel	 a java.awt.image.ColorModel to apply to the returned 
    *                    Image object, if possible.  If this is null, use
    *                    the Viewable's default colormodel
    * @param makeDefault if true make this slice be the viewable's default 
    *                    view, if possible.
    * <p>
    * @return  java.awt.Image   the requested view (null on failure)
    */
   public Image getView(Slice slice, ColorModel colorModel, 
			boolean makeDefault);

   /** 
    * Returns the default Slice that will be used by getView() when
    * a slice is not provided (e.g. in when the no-arg getView() is called).
    */
   public Slice getDefaultSlice();

   /**
    * Returns an N-dimensional chunk of the image data in its native format.
    * This no-argument version should return all the data in the image.
    * Versions with other parameters specific to the type of image data used
    * may be defined, e.g., specifying alternate return value types for the
    * data.<p>
    *
    * @return NdArrayData  the N-dimensional array (null on failure)
    */
   public NdArrayData getData();

   /**
    * Returns an N-dimensional chunk of the image data in its native format.
    *
    * @param vol region of data desired
    * <p>
    * @return NdArrayData  the N-dimensional array (null on failure)
    * <p>
    */
   public NdArrayData getData(Volume vol);

   /**
    * Returns the dimension of the data.
    * <p>
    * @return	An integer representing the dimensionality of (i.e. the 
    *           number of axes) the data.
    */
   public int getNaxes();

   /**
    * Returns an array containing the extent of the data.  The length of
    * the array will be equal to the dimension of the data.
    * <p>
    * @return	An integer array specifying the size of the data in each 
    *           dimension.
    */
   public int[] getSize();

   /**
    * Return a copy of the metadata associated with this data set
    */
   public Metadata getMetadata();

   /**
    * Returns a CoordinateSystem object associated with the Viewable.  In 
    * general, this system should not be a copy, but rather, editable such
    * that other objects that call this method will see any changes that
    * have been made to it.
    * <p>
    * @return	A CoordinateSystem object (null if none present)
    */
   CoordinateSystem getCoordSys();
}
