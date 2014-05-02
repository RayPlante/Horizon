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
package ncsa.horizon.coordinates.formatters;

/**
 * support for printing out frequencies (double values) as floating 
 * point numbers appended by a metric frequency unit.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: FreqAxisPosFormatter.java,v 1.1 1997/08/07 07:30:50 rplante Exp $
 */
public class FreqAxisPosFormatter extends MetricAxisPosFormatter {

    protected static String freqUnit = "Hertz";
    protected static String freqAbbrev = "Hz";

    /**
     * format frequency values with "Hz" as the unit assuming that the 
     * native unit of the values is Hertz.
     */
    public FreqAxisPosFormatter() {
	this(true);
    }

    /**
     * format frequency values assuming that the 
     * native unit of the values is Hertz.
     * @param useAbbreviation  if true, an abbreviation should be used.
     */
    public FreqAxisPosFormatter(boolean useAbbreviation) {
	super((useAbbreviation) ? freqAbbrev : freqUnit, 0,  useAbbreviation);
    }

    /**
     * format frequency values assuming that the 
     * native unit of the values is Hertz.
     * @param inPower  assume that values input to toString(double) will
     *                 be in units of 10^inPower, relative to unit
     * @param useAbbreviation  if true, an abbreviation should be used.
     */
    public FreqAxisPosFormatter(int inPower, boolean useAbbreviation) {
	super((useAbbreviation) ? freqAbbrev : freqUnit, inPower,  
	      useAbbreviation);
    }

    /**
     * format frequencies using the prefix for the specified power
     * @param inPower  assume that values input to toString(double) will
     *                 be in units of 10^inPower, relative to unit
     * @param useAbbreviation  if true, the prefix abbreviation should be
     *                         used.
     * @param outPower the power for the prefix to be used; if a prefix
     *                 for this power is not known, the highest valued 
     *                 prefix that is less than request will be used.
     */
    public FreqAxisPosFormatter(int inPower, boolean useAbbreviation, 
				int outPower) 
    {
	super((useAbbreviation) ? freqAbbrev : freqUnit, inPower, outPower, 
	      useAbbreviation);
    }

    /**
     * loads the prefix and abbreviation lists
     */
    protected void initialize() {
	super.initialize();
	if (minpow <= 1) {
	    prefixes.setElementAt(null, 1-minpow);
	    prefixes.setElementAt(null, 2-minpow);
	    abbreviations.setElementAt(null, 1-minpow);
	    abbreviations.setElementAt(null, 2-minpow);
	    revmap();
	}
    }	

    /**
     * set whether abbreviations are used
     */
    public void setAbbreviated(boolean useAbbreviation) {
	unit = (useAbbreviation) ? freqAbbrev : freqUnit;
	abbreviate = useAbbreviation;
    }

    protected static final String myname = 
        "Frequency Coordinate Axis Position Formatter";
}
