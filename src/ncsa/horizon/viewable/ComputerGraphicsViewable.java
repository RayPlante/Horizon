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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 *-------------------------------------------------------------------------
 * History: 
 *  97       rlp  Original version based on rlp's BasicViewable, which in
 *                turn was based on Plutchak's SimpleViewable (both of 
 *                which have been depricated).
 *  97dec04  rlp  Updated for changes to Viewable interface
 *  97dec07  rlp  made more robust to dataset read errors: on read error,
 *                  size is set 0,0; setdv() returns false.  (Should be 
 *                  handled by exceptions in future.)
 *  98jan19  rlp  updated for NdArray* move to ncsa.horizon.data
 */
package ncsa.horizon.viewable;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.*;
import java.net.URL;
import ncsa.horizon.coordinates.CoordinateSystem;
import ncsa.horizon.coordinates.CoordMetadata;
import ncsa.horizon.util.Metadata;
import ncsa.horizon.util.Voxel;
import ncsa.horizon.util.Slice;
import ncsa.horizon.util.Volume;
import ncsa.horizon.data.NdArrayData;

/**
 * a Viewable class for visualizing computer graphic images normally
 * support directly by Java, including GIF and JPEG. <p>
 *
 * This treats a computer graphic image as if it were a 2-d dataset of
 * colors.  Currently, the getData() methods always return null; 
 * eventually, support of extracting the data as NdArrayData objects 
 * filled with Color values will be added.  
 */
public class ComputerGraphicsViewable implements Viewable, ImageObserver {

    protected CoordinateSystem coord;
    protected Dimension dimension = new Dimension(-1, -1);
    protected Object source;           // URL or filename String 
    protected Image image=null;        // The image created from the Viewable
    protected Image defaultview=null;  // The default image for the Viewable
    protected Slice datavolume=null;
    protected Slice defslice=null;
    protected Metadata mdata=null;

    /**
     * Constructs a new instance of a ComputerGraphicsViewable given a URL.
     * @param _url The URL of an image from which to create the Viewable.
     */ 
    public ComputerGraphicsViewable(URL url) {
	source = url;
	image = defaultview = Toolkit.getDefaultToolkit().getImage(url);
    }

    /**
     * Constructs a new instance of a ComputerGraphicsViewable given a 
     * filename string.
     * @param filename   The complete filename of an image from which to 
     *                   create the Viewable.  
     */ 
    public ComputerGraphicsViewable(String filename) {
	source = filename;
	image = defaultview = Toolkit.getDefaultToolkit().getImage(filename);
    }
    
    /**
     * Constructs a new instance of a ComputerGraphicsViewable that wraps 
     * around an already constructed java Image object;
     * @param image   the input Image object to wrap around; should not be 
     *                null
     * @exceptions NullPointerException if image is null
     */ 
    public ComputerGraphicsViewable(Image image) throws NullPointerException {
	if (image == null) throw new NullPointerException();
	source = image;
	image = defaultview = image;
    }
    
    /**
     * Implementation of a Viewable interface method.
     * This generic getData() returns all the data in the image.  It
     * currently returns null.
     * <p>
     * @returns java.awt.Object which is a multidimensional array
     */
    public NdArrayData getData() { return null; }

    /**
     * Implementation of a ncsa.horizon.viewable.Viewable interface method.
     * This specific getData() returns a subregion of the data that makes up
     * the image.  This implementation always returns null.
     *<p>
     * @param volume Specifies the subset of data to return.
     * @returns java.awt.Object which is a multidimensional array
     */
    public NdArrayData getData(Volume volume) { return null; }

    /**
     * Returns the dimension of the data, i.e. the number of axes in the 
     * dataset.  This currently always returns the value 2.
     * @return The dimensionality of the data.
     */
    public int getNaxes ( ) { return 2; }

    /**
     * This specific getView() returns a subregion and/or resampled version
     * of the image, and optionally makes this new image the default view.
     *<p>
     * @param slice The 2-dimensional area of the image to return.
     * @param colorModel The cooro model to use for the image.
     * @param makeDefault True to make this slice the default view.
     * @returns java.awt.Image
     */
    public Image getView ( Slice slice ) {
	Image       img;
	int         newWidth, newHeight;
	int         cropx=0, cropy=0, cropWidth=0, cropHeight=0;
	double      zoomx=1.0, zoomy=1.0;
	double      arr[] = new double[2];
	boolean     crop, zoom;

	// check for subsetting
	if (slice != null) {              // subset and/or subsample image
	    slice.makeLengthsPositive();   // normalize data
	    arr = slice.getLocation();     // origin of area
	    cropx = (int)arr[0]; 
	    cropy = (int)arr[1];
	    arr = slice.getSize();         // extent of area
	    cropWidth = (int)arr[0];
	    cropHeight = (int)arr[1];
	    arr = slice.getSampling();     // zoom factor for each axis
	    zoomx = arr[0];
	    zoomy = arr[1];

	    if ((cropx == 0) && (cropy == 0) && (cropWidth == dimension.width)
		&& (cropHeight == dimension.height)) {
		crop = false;
	    }
	    else {
		crop = true;
	    }
	    if ((zoomx == 1) && (zoomy == 1))
		zoom = false;
	    else
		zoom = true;
	}
	else {
	    zoom = false;
	    crop = false;
	}

	newWidth = (int)(Math.round( cropWidth*zoomx ));
	newHeight = (int)(Math.round( cropHeight*zoomy ));
 
	// if no changes are requested, just return the whole thing
	if (zoom || crop) {
	    if (!crop) {
		img = image;
	    }
	    else {
		CropImageFilter cropFilter = new
		    CropImageFilter( cropx, cropy, cropWidth, cropHeight );

		if (source instanceof Image) {
		    img = Toolkit.getDefaultToolkit().createImage(
			new FilteredImageSource(image.getSource(), cropFilter));
		}
		else if (source instanceof URL) {
		    img = Toolkit.getDefaultToolkit().createImage(
			new FilteredImageSource(
			    Toolkit.getDefaultToolkit().getImage(
				(URL)source ).getSource(), cropFilter ));
		}
		else /* (source instanceof String) */ {
		    img = Toolkit.getDefaultToolkit().createImage(
			new FilteredImageSource(
			    Toolkit.getDefaultToolkit().getImage(
				(String)source ).getSource(), cropFilter ));
		}
	    }
	}
	else {                         // no crop or scale necessary
	    img = image;
	}

	return img;
    }

    /**
     * Implementation of a Viewable interface method.
     * This generic getView() returns either a previously defined default
     * view, or the whole image at nominal resolution if no default has
     * been defined.
     *<p>
     * @returns java.awt.Image
     */
    public Image getView() {
	if (defaultview != null)
	    return defaultview;
	
	return image;
    }

    /**
     * Implementation of a Viewable interface method.
     * This specific getView() returns a subregion and/or resampled version
     * of the URL image, and optionally makes this new image the default view.
     * This implementation ignores the color model parameter.
     *<p>
     * @param slice The 2-dimensional area of the image to return.
     * @param colorModel The cooro model to use for the image.
     * @param makeDefault True to make this slice the default view.
     * @returns java.awt.Image
     */
    public Image getView ( Slice slice, ColorModel colorModel,
			   boolean makeDefault ) {

	Image view = getView(slice);

	if (makeDefault) {
	    defslice = slice;
	    defaultview = view;
	}

	return( view );
    }

    /**
     * Implementation of a Viewable interface method.
     * This returns null.
     *<p>
     * @return	 null
     */
    public CoordinateSystem getCoordSys ( ) {
	if (coord == null) initCoord();
	return coord;
    }

    /**
     * Set the Coordinate system for this image.  Normally, computer 
     * graphics images do not contain metadata describing its coordinate 
     * system; however, one can attach a CoordinateSystem to this dataset
     * via this method.
     */
    public void setCoordSys(CoordinateSystem csys) {
	coord = csys;
	if (mdata != null) mdata.put("CoordinateSystem", coord.getMetadata());
    }

    private void initCoord() {
	coord = new CoordinateSystem(new CoordMetadata(2));
    }

    /**
     * Return the metadata associated with this viewable.
     */
    public Metadata getMetadata() {
	// TO DO: get "comment" property from Image
	//        determine format from content type

	if (mdata == null) setMetadata();
	return new Metadata(mdata);
    }

    public Slice getDefaultSlice() {
	if (datavolume == null && ! setdv()) return null;
	if (defslice == null) defslice = datavolume;
	return (Slice) defslice.clone();
    }

    protected void setMetadata() {
	mdata = new Metadata();
	mdata.put("dataFormat", 
		  "Java-supported Computer Graphics Image Format");

	// set the comment

	// set the size
	if (datavolume != null || setdv()) {
	    mdata.put("dataVolume", datavolume.clone());
	    mdata.put("defaultSlice", datavolume.clone());
	}

	// set the CoordinateSystem
	if (coord == null) initCoord();
	mdata.put("CoordinateSystem", coord.getMetadata());
    }

    private boolean setdv() {
	int[] isz = getSize();
	for(int i=0; i < isz.length; i++) {
	    if (isz[i] <= 0) {
		System.err.println("Warning: Dataset looks empty.");
		return false;
	    }
	}
	
	datavolume = new Slice(2);
	datavolume.setXaxisLength(isz[0]);
	datavolume.setYaxisLength(isz[1]);
	if (defslice == null) defslice = datavolume;
	return true;
    }

    /**
     * return the size of the dataset.  If the data is not loaded yet, 
     * this method will wait until it is (this will is not always good,
     * so this will get changed in the future).
     */
    public int[] getSize() {
	if (dimension == null)
            dimension = new Dimension(-1, -1);

	synchronized (dimension) {
	    if (dimension.width < 0) dimension.width  = image.getWidth(this);
	    if (dimension.height < 0) dimension.height = image.getHeight(this);
	    while (dimension.width < 0 || dimension.height < 0) {
		try { dimension.wait(30000); }
		catch (InterruptedException ex) { 
		    System.err.println("Interruption: " + ex.getMessage());
		    break;
		}
		if (dimension.width < 0 || dimension.height < 0) {
		    System.err.println("Still waiting for image size");
		}
		else if (dimension.width < -1 || dimension.height < -1) {
		    System.err.println("\nImage Loading Error preventing " +
				       "the determination of image size\n");
		    dimension.width = dimension.height = 0;
		}
	    }
	} 

	int      size[] = { dimension.width, dimension.height };
	return size;
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y,
			       int width, int height) 
    {
	if ((infoflags & (WIDTH|HEIGHT)) != 0) {
	    synchronized (dimension) {
		if (dimension == null)
		    dimension = new Dimension(-1, -1);

		if ((infoflags & WIDTH) != 0) dimension.width = width;
		if ((infoflags & HEIGHT) != 0) dimension.height = height;
		
		dimension.notifyAll();
	    }
	    infoflags &= ~(WIDTH|HEIGHT);
	}

// 	if (infoflags != 0) 
// 	    super.imageUpdate(img, infoflags, x, y, width, height);
 
	if ((infoflags & ERROR) != 0) {
	    synchronized (dimension) {
		if (dimension.width < 0) dimension.width = 0;
		if (dimension.height < 0) dimension.height = 0;
		System.err.println("Error loading image from " + source);
		dimension.notifyAll();
	    }
	}

	return ((dimension == null) || 
		(dimension.width < 0 && dimension.height < 0));

    }
}
