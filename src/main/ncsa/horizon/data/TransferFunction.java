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
 *  97dec10  rlp  Original version
 *  98jan14  rlp  added calcRange, implements ModedGUIControllable
 *  98jan19  rlp  moved from ncsa.horizon.util to ncsa.horizon.data
 */

package ncsa.horizon.data;

import ncsa.horizon.util.JavaType;
import ncsa.horizon.viewable.Viewable;
import ncsa.horizon.awt.ModedGUIControllable;
import ncsa.horizon.awt.ModedGUI;
import ncsa.horizon.awt.ModedGUIException;
import java.util.Observable;
import java.awt.Event;

/**
 * an abstract representation of the mapping of a range of values into 
 * discreet bins.  <p>
 *
 * This class is used when a range of values must be to binned up, such
 * as when turning a slice of data into an image with a certain number of 
 * colors.  A paricular value's membership in a certain bin is represented 
 * an integer.  Before sending any data through this function, one should 
 * first set the number of output bins.  In addition, one should set the 
 * range of that should mapped using setMinimum() and setMaximum().  Then 
 * one can call either getLevel(Object) (for one value at a time) or 
 * getLevels() (for converting a whole array at once); both of these 
 * return integers.  <p>
 *
 * Implementations of this abstract class should observe the following 
 * convention when mapping data values to integers: <p>
 * <center>
 * <table border cellpadding=5>
 * <tr><th align=left>Data Value <th align=left>Output Integer</tr>
 * <tr><td> Undefined    <td> 0 </tr>
 * <tr><td> &lt minimum  <td> 1 </tr>
 * <tr><td> &gt= minimum <td> &gt= 2 </tr>
 * <tr><td> &lt= maximum  <td> &lt= <code>getDynamicRange() - 2</code> </tr>
 * <tr><td> &gt maximum <td> <code>getDynamicRange()<code> - 1 </tr>
 * </table>
 * </center> <p>
 *
 * The purpose behind this convention is to allow special handling of 
 * out of range and undefined values; for example, special colors could 
 * be assigned to each of the out of range and undefined sets of values.  <p>
 *
 * It is recommended "undefined" be assigned to so-called "blanked"
 * values.  Values in a data array may be blanked where no measurement 
 * was made or where the data was removed.  In the case of images, it 
 * might be useful to set these to black (or even made transparent) so 
 * that it is visually obvious which pixels are blanked.  <p>
 *
 * TransferFunction objects can be driven by a TransferFunctionPanel,
 * a graphical user interface (GUI) that can interact with the TransferFunction
 * via the ModedGUIControllable Java interface.  See the
 * TransferFunctionPanel and ModedGUIControllable API's for details.  <p>
 *
 * A TrnasferFunction object can be observed for changes.  When an
 * update occurs, observers are passed an Event object via their
 * update() method in which the Event id can be one of
 * MINIMUM_CHANGED, MAXIMUM_CHANGED, FUNCTION_CHANGED, or possibly an 
 * implementation-specific id.  When the id is one of the first two,
 * the event argument is the the value the new minimum or maximum.  <p>
 * 
 * @author Raymond L. Plante
 * @author Horizon team, University of Illinois at Urbana-Champaign
 */
public abstract class TransferFunction extends Observable 
    implements Cloneable, ModedGUIControllable
{

    /**
     * an event id that indicates that the mimimum value has 
     * been updated; an event with this id will be passed to 
     * Observers of this class.
     */
    public final static int FUNCTION_CHANGED = 2000;

    /**
     * an event id that indicates that the mimimum value has 
     * been updated; an event with this id will be passed to 
     * Observers of this class.
     */
    public final static int MINIMUM_CHANGED = 2001;

    /**
     * an event id that indicates that the maximum value has 
     * been updated; an event with this id will be passed to 
     * Observers of this class.
     */
    public final static int MAXIMUM_CHANGED = 2002;

    /**
     * an event id that indicates that the number of levels has
     * been updated; an event with this id will be passed to 
     * Observers of this class.
     */
    public final static int DYNAMIC_RANGE_CHANGED = 2003;

    /**
     * the dynamic range.  In other words, the number of intensity bins 
     * the data will be divided into.  
     */
    protected int dynarange;

    /**
     * return the integer bin level for the given input value
     */
    public abstract int getLevel(Object value);

    /**
     * return a value that is representative of the input bin level
     */
    public abstract Object getValue(int level);

    /**
     * apply this transfer function to an array of input values by 
     * converting it to an array of integers.
     */
    public abstract int[] getLevels(Object[] values);
    
    /**
     * apply this transfer function to an array of input values by 
     * converting it to an array of integers.  NOTE: THIS METHOD
     * WILL BE REMOVED IN THE NEXT RELEASE.
     * @param values   an array
     */
    public abstract int[] getLevels(Object values, JavaType arrayType) 
	throws IllegalArgumentException;
    
    /**
     * set the dynamic range of the transfer function.  In other words,
     * set the number of different integer bins that a data value can be 
     * mapped to.  When the value-to-integer convention is followed (see 
     * general description above), the actual dynamic range is this number
     * minus 3.
     */
    public void setDynamicRange(int nlevels) { 
	dynarange = nlevels; 
	setChanged();
	notifyObservers(new Event(this, MAXIMUM_CHANGED, 
				  new Integer(dynarange)));
    }
     
    /**
     * get the current dynamic range
     */
    public int getDynamicRange() { return dynarange; }

    /**
     * set the Minimum value.
     */
    public abstract void setMinimum(Object val)
	throws IllegalArgumentException;

    /**
     * get the Minimum value.  
     */
    public abstract Object getMinimum();

    /**
     * set the Maximum value.
     */
    public abstract void setMaximum(Object val)
	throws IllegalArgumentException;

    /**
     * get the Maximum value.  
     */
    public abstract Object getMaximum();

    /**
     * return a string representation of the input value
     */
    public abstract String toString(Object val);

    /**
     * return a two element array that gives the minimum and maximum 
     * values for in a viewable dataset.
     */
    public abstract Object[] calcRange(Viewable v);

    /**
     * use the mode identified by the given name.  Unrecognized names are
     * ignored.  This default implentation does nothing, returning false
     * always.  This method is usually called by a TransferFunctionPanel
     * object that is providing a GUI interface to this TransferFunction.
     * @returns true if mode name was recognized
     */
    public boolean useMode(String name) throws ModedGUIException {
	return false;
    }

    /**
     * carry out any initialization necessary for connecting this object
     * to a TransferFunctionPanel object.  This method will be called by
     * the TransferFunctionPanel class when it attaches this object.  
     * Part of the initialization can be to call the TransferFunctionPanel's 
     * addMode() method for each mode supported by the TransferFunction.
     * The default implementaion does nothing.  Implementers of this class
     * should override this method to allow automatic loading of modes into
     * the TransferFunctionPanel;  <em> documentation of this method should 
     * indicate which modes if any have been registered with addMode(). </em>  
     * @exceptions ModedGUIException if an error occurs during initialization
     */
    public void initModedGUI(ModedGUI gui) throws ModedGUIException { } 

    /**
     * Return a list of recognized mode names that may be passed to 
     * the useMode() method, or null if there is effectively only one
     * mode of operation which is unamed.  (Single mode controllable
     * objects may provide a name for that single mode.)  The default
     * implementation is to return null.  
     */
    public String[] getModeNames() { return null; } 

    /**
     * return a clone of this object
     */
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException ex) { 
	    // should not happen
	    throw new InternalError("Clone not supported: " + ex.getMessage());
	}
    }
}
