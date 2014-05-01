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

import java.awt.Component;
/**
 * An interface that allows a GUI to drive a functional class.  <p>
 * 
 * This provides a way for a functional class to be controlled
 * a GUI interface.  It assumes a simple model in which the class has one 
 * or more modes that it can operate under and that each mode is identified
 * by a name.  Only one mode can be enabled at a time.  Each mode can 
 * optionally have addition mode-specific options that can be controlled as 
 * well.  <p>
 *
 * This interface is implemented by the class that provides the GUI, while
 * the class to be controlled implements the ModedGUIControllable class.  In
 * addition to the methods defined here, the GUI class's interface should
 * also provide a way to "attach" the class being controlled.  When the 
 * controlled class is attached, the GUI class should call the other's 
 * initModedGUI() method.  This allows the controlled class to carry out
 * any necessary initialization, including calling the GUI's addMode() 
 * method to register the different modes.  <p>
 *
 * The addMode() is used to register modes of operation.  The name given
 * should not only be used as an identifying display within the GUI (e.g.
 * as a string in a Choice menu of selectable modes), but it should also 
 * be the that will engage that mode when it is based to the 
 * ModedGUIControllable's useMode() method.  An AWT Component can optionally
 * be given to the addMode() method as well.  This Component should be enabled
 * whenever the mode it is associated with is engaged.  How the component 
 * is engaged is up to the GUI class.  For example, it can hide the Component
 * when the associated mode is not engaged.  <p>
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
public interface ModedGUI {

    /**
     * register a mode of operation that a ModedGUIControllable can 
     * be set to.
     * @param modeName  the name identifying the mode which might
     *                  be displayed in the GUI and passed to the 
     *                  ModedGUIControllable's useMode() method.  
     * @param optionsControl  an AWT component that can be enabled
     *                  by the GUI when the mode is enabled; this 
     *                  component can have special controls for 
     *                  manipulating mode-specific parameters.  A null
     *                  value means that no additional controls are 
     *                  provided for this mode.  
     */
    public abstract void addMode(String modeName, Component optionsControl);
}

