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
 *  98jan19  rlp  moved from ncsa.horizon.util to ncsa.horizon.data
 */
package ncsa.horizon.data;

import java.util.Hashtable;

public class MultiNumericTransferFunction extends NumericTransferFunction {

    protected final static String[] modeNames = { 
	"Linear", "Logarithmic", "Exponential"
    };

    /** linear mode */
    public final static int LINEAR = 0;

    /** logarithmic mode */
    public final static int LOG = 1;

    /** exponential mode */
    public final static int EXP = 2;

    /**
     * the current mode
     */
    protected int mode = LINEAR;

    /**
     * a hashtable mapping mode names to integer mode ids
     */
    protected Hashtable modeids = new Hashtable(3);

    /**
     * create a MultiNumericTransferFunction with a dynamic range of 256
     * and a range of 0 to 252
     */
    public MultiNumericTransferFunction() { 
	this(256); 
	init();
    }

    /**
     * create a MultiNumericTransferFunction with a given dynamic range 
     * and a range of 0 to the dynamic range minus 4.  The default mode 
     * will be linear.
     * @param  nlevels   the dynamic range; that is, the number of
     *                   levels the input values will be mapped into
     */
    public MultiNumericTransferFunction(int nlevels) { 
	super(nlevels); 
	init();
    }

    /**
     * create a MultiNumericTransferFunction with a given dynamic range 
     * and range.  The default mode will be linear.
     * @param  nlevels   the dynamic range; that is, the number of
     *                   levels the input values will be mapped into
     * @param  min       minimum of the range
     * @param  max       maximum of the range
     */
    public MultiNumericTransferFunction(int nlevels, double min, double max) {
	super(nlevels, min, max);
	init();
    }

    private void init() {
	for(int i=0; i < modeNames.length; i++) 
	    modeids.put(modeNames[i], new Integer(i));
    }

    /**
     * Return a list of recognized mode names that may be passed to 
     * the useMode() method.  This method returns four names: "Linear",
     * "Logarithmic", and "Exponential".
     */
    public String[] getModeNames() { 
	String[] out = new String[modeNames.length];
	System.arraycopy(modeNames, 0, out, 0, modeNames.length);
	return out;
    }

    /**
     * use the mode identified by the given name.  Unrecognized names are
     * ignored.  
     * @returns false if mode name was not recognized or this function is 
     *          already in this mode. 
     */
    public boolean useMode(String name) {
	Integer mode_ = (Integer) modeids.get(name); 
	if (mode_ == null || mode_.intValue() == mode) return false;
	mode = mode_.intValue();
	return true;
    }

    /**
     * use the mode identified by the given id.  Unrecognized ids are
     * ignored.  Supported ids include LINEAR, LOG, and EXP.
     * @returns false if mode name was not recognized or this function is 
     *          already in this mode. 
     */
    public boolean useMode(int mode_) {
	if (mode_ == mode || mode_ < 0 || mode_ >= modeids.size()) 
	    return false;
	mode = mode_;
	return true;
    }

    /**
     * return the integer bin level for the given input value using 
     * a simple linear increasing function.  See getLevel(Object) 
     * for mapping rules.
     */
    public int getLevel(double value) {
	if (value == Double.NaN) 
	    return 0;
	else if (value < min) 
	    return 1;
	else if (value > max) 
	    return dynarange-1;
	else if (min == max) 
	    return (dynarange-4)/2 + 2;
	else {
	    double linval = (value - min) / (max - min);
	    if (mode == LINEAR) 
		return (int) Math.round(1.0*(dynarange-4) * linval) + 2;
	    else if (mode == LOG) 
		return (int) Math.round(1.0*(dynarange-4)*
					Math.log(linval+1)) + 2;
	    else if (mode == EXP) 
		return (int) Math.round(1.0*(dynarange-4)*
					Math.exp(linval)) + 2;
	    else 
		return 0;
	}
    }
}
