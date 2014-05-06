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
 *-------------------------------------------------------------------------
 * History: 
 *  97aug    rlp  Original version 
 *  97sep30  rlp  fix bug getting power of a negative number
 *
 */
package ncsa.horizon.coordinates.formatters;

import java.util.*;
import ncsa.horizon.coordinates.*;

/**
 * support for printing out double values as floating point numbers
 * appended by a metric unit.  <p>
 *
 * The base unit is usually specified in the constructor.  
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: MetricAxisPosFormatter.java,v 1.2 1997/12/05 02:09:48 rplante Exp $
 */
public class MetricAxisPosFormatter extends GenericAxisPosFormatter {

    /**
     * the base measurement unit (e.g. "m", "g", "Gauss", etc.)
     */
    protected String unit;

    /**
     * the native base ten logarithmic scale; it is assumed that the 
     * input values to the toString(double) methods are in units of 
     * 10^ipower (relative to the base unit).
     */
    protected int ipower = 0;

    /**
     * the desired output base ten logarithmic scale; if fixed equals
     * true, toString(double) will attempt to print the value in units of 
     * 10^opower (relative to the base unit), assuming there is a known 
     * prefix for that power.
     */
    protected int opower = 0;

    /**
     * if true, the prefix to be used is fixed
     */
    protected boolean fixed = false;

    /**
     * if true, use the abbreviation for the scale prefix
     */
    protected boolean abbreviate = true;

    /**
     * the supported prefixes
     */
    protected final static String[] prefixList = {
	"atto", "femto", "pico", "nano", "micro", "milli", "centi", "deci", "",
	"deca", "hecto", "kilo", "Mega", "Giga",  "Tera",  "Peta",  "Exa" 
    };

    /**
     * prefix abbreviations 
     */
    protected final static String[] abbrevList = {
	"a",  "f", "p", "n", "\u03bc", "m", "c", "d", "",
	"da", "h", "k", "M", "G",      "T", "P", "E"
    };

    /**
     * the prefix powers
     */
    protected final static int[] powerList = {
	-18, -15, -12, -9, -6, -3, -2, -1, 0,
	  1,   2,   3,  6,  9, 12, 15, 18
    };

    /**
     * a container for the prefixes
     */
    protected Vector prefixes;

    /**
     * a container for the prefixes
     */
    protected Vector abbreviations;

    /**
     * a map that translates prefixes into powers
     */
    protected Hashtable prefmap = null;

    /**
     * a map that translates prefixes into powers
     */
    protected Hashtable abbmap = null;

    /**
     * the smallest (most negative) power supported.
     */
    protected int minpow = 0;

    /**
     * format values of specified units.  By default, it will be 
     * assumed that values input to toString will be in units of
     * this unit, and the most appropriate prefix abbreviation will be used.
     * @param unit  the unit name, can be an abbreviation
     */
    public MetricAxisPosFormatter(String unit) {
	this.unit = (unit == null) ? "" : unit;
	initialize();
    }
	
    /**
     * format values with no base unit (only prefix will appear).  By 
     * default, the most appropriate prefix abbreviation will be used.
     */
    public MetricAxisPosFormatter() {
	this(null);
    }
	
    /**
     * format values using the specified unit and the prefix abbreviation
     * for the specified power
     * @param unit   the unit name, can be an abbreviation
     * @param inPower  assume that values input to toString(double) will
     *                 be in units of 10^inPower, relative to unit
     */
    public MetricAxisPosFormatter(String unit, int inPower) {
	this(unit, inPower, true);
    }

    /**
     * format values using the specified unit and the prefix abbreviation
     * for the specified power
     * @param unit   the unit name, can be an abbreviation
     * @param inPower  assume that values input to toString(double) will
     *                 be in units of 10^inPower, relative to unit
     */
    public MetricAxisPosFormatter(String unit, int inPower,
				  boolean useAbbreviation) {
	this(unit);
	ipower = inPower;
	abbreviate = useAbbreviation;
    }

    /**
     * format values using the specified unit and the prefix 
     * for the specified power
     * @param unit     the unit name, can be an abbreviation
     * @param inPower  assume that values input to toString(double) will
     *                 be in units of 10^inPower, relative to unit
     * @param outPower the power for the prefix to be used; if a prefix
     *                 for this power is not known, the highest valued 
     *                 prefix that is less than request will be used.
     * @param useAbbreviation  if true, the prefix abbreviation should be
     *                         used.
     */
    public MetricAxisPosFormatter(String unit, int inPower, 
				  int outPower, boolean useAbbreviation) 
    {
	this(unit, inPower, useAbbreviation);

	Vector list = (abbreviate) ? abbreviations : prefixes;
	this.opower = maxPower(list, outPower, minpow);
	fixed = true;
    }

/***********************************************************************
 * 
 *  AxisPosFormatter methods
 *
 ***********************************************************************/

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     */
    public String toString(double val) { 
	StringBuffer theUnit = new StringBuffer();
	val = scaleValue(val, theUnit);
	return new String(super.toString(val) + " " + theUnit);
    }

    /**
     * return a value as a string with a specified precision
     * @param val  the input value
     * @param prec the number of places right of the decimal point
     */
    public String toString(double val, int prec) { 
	StringBuffer theUnit = new StringBuffer();
	val = scaleValue(val, theUnit);
	return new String(super.toString(val, prec) + " " + theUnit);
    }

    /**
     * parse the String representation of a floating point number
     */
    public synchronized double valueOf(String s) throws NumberFormatException {
	double val;
	int p = s.length();
	int up, uep, np, nep;

	if (! unit.equals("") && (p = s.lastIndexOf(unit)) < 0)
	    throw new NumberFormatException("Not of proper units = " + unit);
	while(p > 0 && ! Character.isSpace(s.charAt(p-1))) p--;
	if (p == 0) throw new NumberFormatException("No numeric value found");

	// find beginning of number part
	for(np=0; np < p && Character.isSpace(s.charAt(np)); np++);
	if (np == p) throw new NumberFormatException("Empty string");

	// find end of number part
	for(nep = np; nep < p && ! Character.isSpace(s.charAt(nep)); nep++);
	if (nep == p) throw new NumberFormatException("Empty string");

	// find beginning of prefix
	for(up = nep; up < p && Character.isSpace(s.charAt(up)); up++);
	String prefix = (up == p) ? "" : s.substring(up,p);

	// find power for prefix
	Hashtable map = (abbreviate) ? abbmap : prefmap;
	Integer pow = (Integer) map.get(prefix);
	if (pow == null) {
	    map = (abbreviate) ? prefmap : abbmap;
	    pow = (Integer) map.get(prefix);
	}

	if (pow == null) 
	    throw new NumberFormatException("Unrecognized unit prefix: " + 
					    prefix);

	val = super.valueOf(s.substring(np, nep));
	val *= Math.pow(10.0, 1.0*pow.intValue());

	return val;
    }

    public synchronized Object clone() { 
	MetricAxisPosFormatter out = (MetricAxisPosFormatter) super.clone();
	out.prefixes = new Vector(prefixes.size());
	out.abbreviations = new Vector(abbreviations.size());
	int sz = prefixes.size();
	for(int i=0; i < sz; i++) 
	    out.prefixes.setElementAt(prefixes.elementAt(i), i);
	sz = abbreviations.size();
	for(int i=0; i < sz; i++) 
	    out.abbreviations.setElementAt(abbreviations.elementAt(i), i);
	return out; 
    }

/***********************************************************************
 * 
 *  public MetricAxisPosFormatter methods
 *
 ***********************************************************************/

    /**
     * return the higest power that is less than or equal to the requested 
     * power for which there is a name in the given list.
     * @param list   the Vector to check
     * @param power  the requested power
     * @param minpow the minimum power in the list
     */
    public static int maxPower(Vector list, int power, int minpow) {

	Object name;
	int i, from;
	int sz = list.size(), 
	    request = power - minpow;

	name = (request >= 0 && request < sz) ? 
	    list.elementAt(request) : null;
	if (name != null) return power;

	if (request >= 0) {
	    from = (request >= sz) ? sz-1: request-1;
	    for(i=from; i >= 0 && name == null; i--)
		name = list.elementAt(i);
	    if (++i >= 0 && name != null) return i+minpow;
	}

	for(i=0; i < sz && name == null; i++)
	    name = list.elementAt(i);
	return ((i < sz) ? i+minpow-1 : 0);
    }

    /**
     * set the basic unit (word or abbreviation)
     */
    public void setUnit(String unitName) {
	unit = unitName;
    }

    /**
     * set whether abbreviations are used
     */
    public void setAbbreviated(boolean useAbbreviation) {
	abbreviate = useAbbreviation;
    }

    /**
     * assume that the values input to toString(double) will be in units
     * of 10^power (relative to the current unit name).
     */
    public void setUnitPower(int power) { ipower = power; }

    /**
     * when formatting values with toString(double), attempt to use the 
     * prefix associated with this power.  If a prefix is not known 
     * for this power, use the prefix of the maximum power less than 
     * this power for which a prefix is known.
     */
    public void fixPrefix(int power) { 
	opower = power; 
	fixed = true;
    }

    /**
     * when formatting values with toString(double), attempt to use the 
     * current default output prefix (set either at construction or during
     * the last call to fixPrefix(int).  
     */
    public void fixPrefix() { fixed = true; }

    /**
     * when formatting values with toString(double), attempt to use the 
     * current default output prefix (set either at construction or during
     * the last call to fixPrefix(int).  
     */
    public void freePrefix() { fixed = true; }

    /**
     * return the current default output prefix power that will be used by
     * toString(double) when isFixed() = true.
     */
    public int getDefPrefixPower() { return opower; }

    /**
     * return true if this formatter will use a fixed prefix 
     */
    public boolean isFixed() { return fixed; }

    /** 
     * return whether abbreviations are used
     */
    public boolean isAbbreviated() { return abbreviate; }

    /**
     * use a given prefix for the specified power
     */
    public synchronized void usePrefix(int power, String prefix) {
	if (power < minpow) setMinPower(power);
	prefixes.setElementAt(prefix, power-minpow);
	revmap();
    }

    /**
     * return the prefix that will be used for a given power, or null
     * if one is not known
     */
    public synchronized String prefixFor(int power) {
	return ((String) prefixes.elementAt(power-minpow));
    }
	    
    /**
     * use a given abbreviation for the specified power
     */
    public synchronized void useAbbrev(int power, String abbrev) {
	if (power < minpow) setMinPower(power);
	abbreviations.setElementAt(abbrev, power-minpow);
	revmap();
    }

    /**
     * return the prefix abbreviation that will be used for a given 
     * power, or null if one is not known.
     */
    public synchronized String AbbrevFor(int power) {
	return ((String) abbreviations.elementAt(power-minpow));
    }
	    
/***********************************************************************
 * 
 *  protected MetricAxisPosFormatter methods
 *
 ***********************************************************************/

    /**
     * loads the prefix and abbreviation lists
     */
    protected void initialize() {
	int i;
	int range = powerList[powerList.length-1] - powerList[0] + 1;
	minpow = powerList[0];

	prefixes = new Vector(range);
	prefixes.setSize(range);
	abbreviations = new Vector(range);
	abbreviations.setSize(range);
	
	for(i=0; i < powerList.length; i++) {
	    prefixes.setElementAt( prefixList[i], powerList[i]-minpow );
	    abbreviations.setElementAt( abbrevList[i], powerList[i]-minpow );
	}

	revmap();
    }

    /**
     * update the reverse maps
     */
    protected void revmap() {
	int i, asz = abbreviations.size(), 
	       psz = prefixes.size();
	Object itm;

	abbmap = new Hashtable(asz);
	prefmap = new Hashtable(psz);
	
	for(i=0; i < asz; i++) {
	    itm = abbreviations.elementAt(i);
	    if (itm != null) abbmap.put(itm, new Integer(i+minpow));
	}
	for(i=0; i < psz; i++) {
	    itm = prefixes.elementAt(i);
	    if (itm != null) prefmap.put(itm, new Integer(i+minpow));
	}
    }

    /**
     * shift the lists of prefixes and abbreviations to use a new 
     * minimum power.  This will only lower the value of minpow.
     */
    protected synchronized void setMinPower(int power) {
	if (power >= minpow) return;

	// we need to shift the lower end of the Vectors
	int oldsz = prefixes.size(),
	    newsz = oldsz+minpow-power;
	prefixes.ensureCapacity(newsz);
	abbreviations.ensureCapacity(newsz);
	for(int i=oldsz-1; i >= 0; i--) {
	    prefixes.setElementAt(prefixes.elementAt(i-(minpow-power)), i);
	    abbreviations.setElementAt(prefixes.elementAt(i-(minpow-power)), 
				       i);
	}
	minpow = power;

	return;
    }
	
    /**
     * scale the input value and return unit appropriate for that 
     * scale
     * @param  val       the input value
     * @param  unitBuf   a buffer to append unit to
     * @return double    the scaled value
     */
    protected synchronized double scaleValue(double val, StringBuffer unitBuf) 
    {
	if (ipower != 0) val *= Math.pow(10, ipower);
	int upow = opower,
	    vpow = log10(val);
	Vector list = (abbreviate) ? abbreviations : prefixes;

	// if not using a fixed power determine the best power to use
	if (! fixed) upow = maxPower(list, vpow, minpow);

	// scale the input value
	val /= Math.pow(10, upow);

	// set the unit
	unitBuf.append((String) list.elementAt(upow-minpow));
	if (unit != null) unitBuf.append(unit);

	return val;
    }

/***********************************************************************
 * 
 *  private MetricAxisPosFormatter methods
 *
 ***********************************************************************/

    /**
     * return the integer log of the input value.
     */
    private int log10(double val) {
	double tmp = 0;
	val = Math.abs(val);
	if ((val < 10.0 && val >= 1.0) || val == 0.0) return 0;

	int lopow=1, hipow = 20;
	if (tmp < 1) {
	    int temp = -lopow;
	    lopow = -hipow;
	    hipow = temp;
	}

	int inc = Math.abs(hipow - lopow) + 1;
	if (hipow < 0) {
	    while(val/Math.pow(10, hipow)  > 10.0) {
	        lopow = hipow;
	        hipow += inc;
	    }
	} else {
	    while(val/Math.pow(10, lopow)  < 1.0) {
	        hipow = lopow;
	        lopow -= inc;
	    }
	}
		
	int pow = (hipow + lopow) / 2;
	while (hipow != lopow) {
	    tmp = val / Math.pow(10.0, pow);
	    if (tmp < 1.0) {
		hipow = (Math.abs(hipow-lopow) == 1) ? lopow : pow;
	    } else if (tmp > 10.0) {
		lopow = (Math.abs(hipow-lopow) == 1) ? hipow : pow;
	    } else {
		return pow;
	    }
	    pow = (hipow + lopow) / 2;
	}

	return hipow;
    }
	    
    public static void main(String args[]) {
	int i;
	MetricAxisPosFormatter cap = new MetricAxisPosFormatter("m");

	double mine = 4.823e-17;
	System.out.println("My position: " + cap.toString(mine));
	cap.useAbbrev(-18, null);
	System.out.println(" is now at: "  + cap.toString(mine));

	cap.setUnit("Hz");

	double yours;
	for (i=0; i < args.length; i++) {
	    yours = new Double(args[i]).doubleValue();
	    cap.setAbbreviated(true);
	    System.out.println("Your position: " + cap.toString(yours));
	    cap.setAbbreviated(false);
	    System.out.println("Your position: " + cap.toString(yours));
	}
    }

    protected static final String myname = 
        "Metric Coordinate Axis Position Formatter";
};

