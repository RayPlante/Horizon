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
* SimpleViewer.java - Source for the SimpleViewer class implementation
*       of the ncsa.horizon.viewer.Viewer abstract class.
*
* Modification history:
*   06-Nov-1996 plutchak     Initial version.
*/

package ncsa.horizon.viewer;

import java.awt.Panel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import ncsa.horizon.viewer.Viewer;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.util.Slice;

/** 
* Panel class to display an ncsa.horizon.viewable.Viewable object.  This
* implementation is an fairly simple Java Canvas, with three basic options
* for Canvas/Image sizing.
*
* @version 0.1 alpha
* @author Horizon team, University of Illinois at Urbana-Champaign
* @author <br>Joel Plutchak &ltplutchak@uiuc.edu&gt
*/
public class SimpleViewer extends ncsa.horizon.viewer.Viewer
       implements Cloneable {
    private Viewable          viewable;
    private int               mode;
    private Image             image;

    // constants
    /** * Fit the image to the Viewer size (default) */
    public final static int SIZE_IMAGE_FIT    = 1;
    /** * Trim the image to the Viewer size (centered) */
    public final static int SIZE_IMAGE_CENTER   = 2;
    /** * Trim the image to the Viewer size (centered) */
    public final static int SIZE_IMAGE_TRUNCATE   = 3;

    /**
    * Create a new instance of a SimpleViewer.  The default display mode
    * is SIZE_IMAGE_CENTER, which centers the image in the panel without
    * resizing the image.
    */
    public SimpleViewer ( ) {
       viewable = null;
       mode = SIZE_IMAGE_CENTER;
    }

    /**
     * This method adds a reference to a viewable object.
     * @param image the Viewable object to be added
     */
    public void addViewable ( Viewable image ) {
       viewable = image;
    }

    /**
     * Return a reference to the current Viewable object, or null if 
     * none are attached to this Viewer.
     * @return The current Viewable object; null if none present.
     */
    public Viewable getViewable ( ) {
       return( viewable );
    }

    /**
     * Display a slice from the current Viewable data, or do nothing if
     * the current Viewable is not set.  A null slice means display the
     * default slice.
     */
    public void displaySlice ( Slice sl ) {
       if (viewable == null) return;

       if ((image = viewable.getView( )) == null) return;

       repaint();
    }

    /**
     * Display a default slice of the current Viewable.
     */
    public void displaySlice ( ) {
       displaySlice( null );
    }

    /**
     * Return a Slice object describing the data currently being viewed, 
     * or null if there is no Viewable currently being viewed.  This
     * implementation always returns null.
     * @return The currently displayed Slice object; null if none present.
     */
    public Slice getViewSlice ( ) {
       return( null );
    } 
     
    /**
     * This method returns the size of the region that displays a Viewable
     * @return Dimension of the compoonent
     * @see java.awt.Dimension
     * @see java.awt.Component.size()
     */
    public Dimension getDisplaySize ( ) {
       return( this.size() );
    }

    /**
     * Create a clone of this Viewer Panel.
     * @return A clone of this object.
     * @exception java.lang.CloneNotSupportedException  Occurs if the
     *    superclass has not implemented the clone() method.
     */
    public Object clone() throws CloneNotSupportedException {
	return super.clone();
    }

    /**
    * Redraw current slice/viewable.
    * @param g The graphics context to paint.
    */
    public void update ( Graphics g ) {
       if (image == null) return;                // there's no image to update

       if (!prepareImage( image, this )) return; // the image isn't yet loaded
       
       int width = size().width, height = size().height;
       int x = 0, y = 0;

       g.clearRect( 0, 0, width, height );    // clear the panel

       switch (mode) {
          case SIZE_IMAGE_FIT: {         // resize the image to the panel size
             g.drawImage( image, x, y, width, height, this );
             break;
          }
          case SIZE_IMAGE_CENTER: {      // center the image in the panel
             int  xloc = (width - image.getWidth( this ))/2;
             int  yloc = (height - image.getHeight( this ))/2;

             g.drawImage( image, xloc, yloc, this );
             break;
          }
          case SIZE_IMAGE_TRUNCATE:      // truncate right/bottom if necessary the image into the panel
          default: {
             g.drawImage( image, 0, 0, this );
             break;
          }
       }
    }

   /**
   * This implementation simple issues a repaint() when an image has become
   * available.
   */
   public boolean imageUpdate ( Image img, int flags, int x, int y,
         int w, int h) {

      if ((flags & ALLBITS) == 0) return( true );

      repaint();
      return( false );
   }

   /**
   * This implementationm simplyt calls the update() method.
   * @param g The graphics context to paint.
   */
   public void paint ( Graphics g )  {
      update( g );
   }

   /**
   * Set the display mode for sizing or trimming the viewable image.
   * @param _mode The mode to use.  Valid modes are SIZE_IMAGE_FIT,
   *    SIZE_IMAGE_CENTER, and SIZE_IMAGE_TRUNCATE.
   */
   public void setMode ( int _mode ) {
      switch (_mode) {
         case SIZE_IMAGE_FIT:
         case SIZE_IMAGE_CENTER:
         case SIZE_IMAGE_TRUNCATE: {
            mode = _mode;
            repaint();
         }
      }
   }
}
