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
 * support for printing out velocities (double values) as floating 
 * point numbers appended by a metric velocity unit.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: MetricVelocityFormatter.java,v 1.1 1997/08/07 07:30:50 rplante Exp $
 */
public class MetricVelocityFormatter extends MetricAxisPosFormatter {

    protected static String velUnit = "meters/second";
    protected static String velAbbrev = "m/s";

    /**
     * format velocity values with "m/s" as the unit assuming that the 
     * native unit of the values is meters/second.
     */
    public MetricVelocityFormatter() {
	this(true);
    }

    /**
     * format velocity values assuming that the 
     * native unit of the values is meters/second.
     * @param useAbbreviation  if true, an abbreviation should be used.
     */
    public MetricVelocityFormatter(boolean useAbbreviation) {
	super((useAbbreviation) ? velAbbrev : velUnit, 0,  useAbbreviation);
    }

    /**
     * format velocity values assuming that the 
     * native unit of the values is meters/second.
     * @param inPower  assume that values input to toString(double) will
     *                 be in units of 10^inPower, relative to unit
     * @param useAbbreviation  if true, an abbreviation should be used.
     */
    public MetricVelocityFormatter(int inPower, boolean useAbbreviation) {
	super((useAbbreviation) ? velAbbrev : velUnit, inPower,  
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
    public MetricVelocityFormatter(int inPower, boolean useAbbreviation, 
				int outPower) 
    {
	super((useAbbreviation) ? velAbbrev : velUnit, inPower, outPower, 
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
	unit = (useAbbreviation) ? velAbbrev : velUnit;
	abbreviate = useAbbreviation;
    }

    protected static final String myname = 
        "Metric Velocity Coordinate Axis Position Formatter";
}
