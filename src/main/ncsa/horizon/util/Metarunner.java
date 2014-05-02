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
 *  97mar20  rlp  Original version 
 */
package ncsa.horizon.util;

/**
 * An Runnable object used to fetch the value of a metadatum. <p>
 *
 * To make this object do anything useful, one must sub-class it and 
 * override the <a href="#getDatum()">getDatum()</a> method.  (One 
 * should not override the <a href="#run()">run()</a> method which, in 
 * addition to calling the getDatum() method, updates internal data 
 * regarding its execution history.)  The user then has several ways to 
 * fetch and return a value:
 * <ul>
 *    <li> call the <a href="#getDatum()">getDatum()</a> method directly.
 *    <li> call the <a href="#run()">run()</a> method, then call 
 *         <a href="#getLastValue()">getLastValue()</a>.
 *    <li> call the static 
 *         <a href="#fetchDatum(ncsa.horizon.util.Metarunner)">fetchDatum</a>
 *         method (equivalent to the previous 2-step procedure).
 *    <li> pass this object as a Runnable to a Thread, execute the Thread,
 *         wait for it to finish, and then call 
 *         <a href="#getLastValue()">getLastValue()</a>.
 * </ul>
 *
 * If <a href="#run()">run()</a> completes without interruption, then an 
 * internally stored run counter (accessible via <a href="#getRunCount()">
 * getRunCount()</a>) is incremented and the error state is updated.  
 * If <a href="#getDatum()">getDatum()</a> returns with a state of 
 * <a href="#OK">OK</a> or <a href="#RUN_PROBLEM">RUN_PROBLEM</a>, the
 * value is stored internally for access via <a href="#getLastValue()">
 * getLastValue()</a>.  <p>
 *
 * This class is provided as part of support for metadata within 
 * <a href="ncsa.horizon.util.Metadata.html">Metadata</a> objects.  For 
 * example, some metadata may be costly to load into memory if it needs 
 * to be downloaded from the network or it requires an expensive computation.
 * This effort may be wasted if the user never asks for the data.  The 
 * <a href="ncsa.horizon.util.Metadata.html">Metadata</a> class provides a 
 * mechanism for one to store <em>procedures</em>, in the form of 
 * Metarunner objects, for obtaining metadata.  The Metarunner is then
 * tranparently executed by the Metadata object when the user first requests
 * the data.  <p> 
 * 
 * To make use of this capability, programmers should sub-class the 
 * Metarunner class, overridding the getDatum() method to fetch and return 
 * the value of the metadatum works for.  The Metarunner object should then 
 * stored in the <a href="ncsa.horizon.util.Metadata.html">Metadata</a> 
 * object using the name of metadatum appended with the String 
 * <a href="ncsa.horizon.util.Metadata.html#METARUNNER_TAG">
 * Metadata.METARUNNER_TAG</a>.  When the user later requests the metadatum, 
 * the <a href="ncsa.horizon.util.Metadata.html">Metadata</a> object first 
 * looks for a static value in its hashtable.  If it does not exist, then the 
 * <a href="ncsa.horizon.util.Metadata.html">Metadata</a> object engages the 
 * surrogate Metarunner object to fetch the value.  If the getDatum() method
 * returns with a status of <a href="#OK">OK</a>, the 
 * <a href="ncsa.horizon.util.Metadata.html">Metadata</a> store the returned
 * value as a static value in its list before passing it on to its client.
 * If getDataum() returns <a href="#RUN_PROBLEM">RUN_PROBLEM</a>, the 
 * <a href="ncsa.horizon.util.Metadata.html">Metadata</a> object does not
 * store the returned value, though it does return the value to the client.
 * Any other status from getDatum() will cause <code>null</code> to be 
 * returned to the <a href="ncsa.horizon.util.Metadata.html">Metadata</a> 
 * object.  See the <a href="ncsa.horizon.util.Metadata.html">Metadata</a>
 * API for more details.  <p>
 *
 * @author Raymond L. Plante
 * @author Horizon Java Team
 * @version $Id: Metarunner.java,v 1.1 1997/08/07 06:52:06 rplante Exp $
 */
public class Metarunner implements Runnable {

    /**
     * error state: no error occurred
     */
    public static final int OK = 0;

    /**
     * error state: runnable encountered an application-specific error;
     * however, a usuable or default value has been returned.
     */
    public static final int RUN_PROBLEM = 1;

    /**
     * error state: failed to initiate or complete execution of Thread
     */
    public static final int THREAD_FAILED = 2;

    /**
     * error state: runnable encountered an application-specific error
     */
    public static final int RUN_FAILED = 3;

    /**
     * value of metadatum from the last time this Runnable was executed
     */
    protected Object lastValue = null;

    /**
     * true if this Runnable is currently being executed.
     */
    protected Boolean isrunning = Boolean.FALSE;

    /**
     * true if this Runnable has been executed at least once since being
     * instantiated.
     */
    protected int runCount = 0;

    /**
     * value of the error state resulting from the last execution of this
     * Runnable
     */
    protected int errState = OK;

    /**
     * construct a Metarunner object
     */
    public Metarunner() { }

    /**
     * return true if this runnable is currently being run by some thread
     */
    public boolean isRunning() { return isrunning.booleanValue(); }

    /**
     * return the error state resulting from the last time this runnable
     * was executed.
     */
    public int getErrorState() { return errState; }

    /**
     * return the value from the last time this runnable was executed
     */
    public Object getLastValue() { return lastValue; }

    /**
     * return true if this runnable has been fully executed via the run() 
     * method at least once.
     */
    public boolean hasRun() { return (runCount > 0); }

    /**
     * return the number of times this runnable has been fully executed
     * via the run() method.
     */
    public int getRunCount() { return runCount; }

    /**
     * execute this Runnable.  This method calls getDatum(); thus, subclasses 
     * should not override this method but rather should override getDatum().
     * If getDatum() returns without interruption, the current error state
     * is updated and the run counter is incremented.  In addition, if the
     * error state <= 1 (i.e. OK or RUN_PROBLEM), the last known value is 
     * also updated.
     */
    public synchronized void run() {
	ValueAndStatus result=null;
	boolean stat = true;
	isrunning = Boolean.TRUE;

	try {
	    result = getDatum();
	} 
	catch (RuntimeException e) {
	    throw e;
	} 
	catch (Exception e) {
	    errState = THREAD_FAILED;
	    stat = false;
	}

	if (stat) {
	    if (result == null) {
		errState = RUN_FAILED;
	    }
	    else {
		errState = result.status;
		if (result.status <= 1) lastValue = result.value;
	    }
	}
	    
	runCount++;
	isrunning = Boolean.FALSE;
	notifyAll();
    }

    /**
     * calculate, read, or make up a value for a piece of data.  This
     * method returns null as a value and OK as an error; thus, subclasses
     * should override this method.  
     * @return ValueAndStatus a container hold the resulting value and an 
     *                        error state.  
     */
    public ValueAndStatus getDatum() throws Exception {

	return new ValueAndStatus(null, OK);
    }

    /**
     * create a Thread, execute the specified Metarunnable, wait for its
     * completion, then return the result.
     */
    public static Object fetchDatum(Metarunner fetcher) {

	synchronized (fetcher) {
	    fetcher.run();
	    return fetcher.lastValue;
	}
    }

}

