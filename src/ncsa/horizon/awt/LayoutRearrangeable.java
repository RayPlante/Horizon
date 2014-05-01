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

package ncsa.horizon.awt;
import java.awt.*;

/**
 * This interface provides two common methods which 
 * user can overwrite to customize a container's
 * layout.  This interface should be implemented
 * by a Container.
 * If the two methods are overwritten, they should
 * be called explicitly.  A sample to call these
 * methods is:
 * <pre>
 * public ZoomControl()
 * { 
 *   // instantiate all the components
 *   init();
 *   // make sure every component is instantiated, because
 *   // this method(ZoomControl constructor) is always called as default
 *   // by a subclass. We don't want layoutComponents() be called
 *   // before some new components are instantiated.
 *   if(getClass().getName().compareTo("ncsa.horizon.control.ZoomControl") == 0)
 *     layoutComponents();
 * }
 * </pre>
 */
public interface LayoutRearrangeable
{
  /**
   * This method should always be :
   * { layoutComponents(this); }
   * as default.
   * User can overwrite it giving a wanted container
   * instead of "this".
   */
  public abstract void layoutComponents();

  /**
   * This method layouts some components that already
   * created.  The components' reference should be protected.
   * A figure should be provided to the user explaining
   * the name of each component.
   *
   * @param parent the container in which components are to
   * be layouted.
   */
  public abstract void layoutComponents(Container parent);
}
