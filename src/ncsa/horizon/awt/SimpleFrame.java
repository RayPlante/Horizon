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
   SimpleFrame.java - A Frame that handles simple actions.

   Classes:
      SimpleFrame

   Edit history:
      plutchak    08-Nov-96 Initial version; from PopUp.java
*/
package ncsa.horizon.awt;

import java.awt.Frame;
import java.awt.Event;

/**
* A frame that provides minimal event handling, i.e., WINDOW_DESTROY.
* @version 0.1 alpha
* @author The Horizon team, University of Illinois at Urbana-Champaign,
* @author <br>Joel Plutchak &ltplutchak@uiuc.edu&gt
*/
public class SimpleFrame extends Frame {
   private boolean   killOnClose = false;

   public SimpleFrame ( ) {
     super();
   }

   public SimpleFrame ( String title ) {
     super( title );
   }

   /**
   * Provide minimal event handling: close window on WINDOW_DESTROY.
   * @param ev the Frame event.
   */
   public boolean handleEvent ( Event ev ) {
      switch (ev.id) {
         case ev.WINDOW_DESTROY: {   // either close or destroy
            if (killOnClose) {
               dispose();            // use this for killing an application
               System.exit( 0 );
            }
            else {
               this.hide();          // use this for hiding a subframe
            }
            break;
         }  
      }
      return( false );
   }

   /**
   * Set flag to either hide frame or exit application when a WINDOW_DESTROY
   * event is received.
   * @param flag True if exit application on window close; false to simply
   *    hide the frame.
   */
   public void setKillOnClose ( boolean flag ) {
      killOnClose = flag;
   }

   /**
   * Destroy this frame and exit application when a WINDOW_DESTROY event is
   * received.
   */
   public void setKillOnClose ( ) {
      killOnClose = true;
   }
}
