/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1997, Board of Trustees of the University of Illinois
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
 *-------------------------------------------------------------------------
 * History: 
 *  98jan14  rlp  Original version
 */
package ncsa.horizon.awt;

/**
 * An interface that allows simple driving of a class via a GUI interface. <p>
 *
 * This interface provides a way for a functional class to be controlled
 * a GUI interface.  It assumes a simple model in which the class has one 
 * or more modes that it can operate under and that each mode is identified
 * by a name.  Only one mode can be enabled at a time.  Each mode can 
 * optionally have addition mode-specific options that can be controlled as 
 * well.  <p>
 *
 * This interface is implemented by the class that will be controlled, while
 * the GUI class implements the ModedGUI interface.  <p>
 *
 * For an example use of the ModedGUI and ModeGUIControllable interfaces,
 * see the TransferFunction and TransferFunctionPanel classes.  In this 
 * example, the TransferFunctionPanel provides GUI components that are needed
 * by all TransferFunctions (controlled via TransferFunction methods); it
 * also allows access to features specific to TransferFunction subclasses 
 * via the ModedGUI/ModedGUIControllable interfaces.   <p>
 *
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public interface ModedGUIControllable {

    /**
     * Tell this object to switch to a given operational mode identified
     * by a given name.  If mode swithcing is not supported, this 
     * method can be implemented to always return false.
     * @returns true if the name was recognized and the switch was 
     *          successly made; false should be returned if the name 
     *          was not recognized or if the object is already in this 
     *          mode. 
     * @exceptions ModedGUIException if the object fails to switch to 
     *          the specified mode.
     */
    public abstract boolean useMode(String modeName)
	throws ModedGUIException;

    /**
     * Return a list of recognized mode names that may be passed to 
     * the useMode() method, or null if there is effectively only one
     * mode of operation which is unamed.  (Single mode controllable
     * objects may provide a name for that single mode.)  The simplest
     * implementation of this method would be to return false if mode
     * switching is not supported.
     */
    public abstract String[] getModeNames();

    /**
     * carry out any initialization necessary for connecting this object
     * to a ModedGUI object.  This method is usually called by the ModedGUI 
     * object when it attaches to this ModedGUIControllable object, passing
     * the this reference as the argument.  Part of the initialization can
     * be to call the ModedGUI's addMode() method for each mode supported
     * by the implementing class; <em> documentation of this method should 
     * indicate which modes if any have been registered with addMode(). </em>  
     * This method can be implemented as an empty method if no initialization 
     * is needed or supported.  
     * @exceptions ModedGUIException if an error occurs during initialization
     */
    public abstract void initModedGUI(ModedGUI gui) 
	throws ModedGUIException;
}

