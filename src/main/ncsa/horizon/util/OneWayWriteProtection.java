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
/**
 * @version 1.0
 * @author Raymond L. Plante
 */

package ncsa.horizon.util;

/**
 * an interface that allows users to temporarily update non-public 
 * data.  Classes implementing this interface allow users to update
 * non-public data via public methods until the 
 * <a href="#writeProtect()">writeProtect</a>() method is called.
 * From that point on, these methods throw a 
 * <a href="ncsa.horizon.util.WriteProtectionException.html#_top_"> 
 * WriteProtectionException</a>, indicating that the data may no
 * longer be updated. <p>
 *
 * Classes implementing this interface should have an internal storage 
 * item (e.g. a boolean) that is set when 
 * <a href="#writeProtect()">writeProtect</a>() is called.
 * The methods that write data in need of protection should check the 
 * status of that item; if it is set, they should throw a 
 * <a href="ncsa.horizon.util.WriteProtectionException.html#_top_"> 
 * WriteProtectionException</a>.  <p>
 *
 * It is not intended that implementations allow write protection to be 
 * turned off (i.e. it is one way); instead, a clone method should be 
 * defined so that the write protection on the new copy is turned off. <p>
 *
 * Thread safety should be considered when implementing this class. <p>
 */
public interface OneWayWriteProtection {

    /** 
     * turn on write protection
     * @return whether the write protection was changed
     */
    public boolean writeProtect();

    /**
     * return whether write protection is on
     */
    public boolean isWriteProtected();
}
