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

 *-------------------------------------------------------------------------
 * The Java Fits code used here was first developed by T. McGlynn
 * (NASA/GSFC/USRA) as public domain software.  Minor changes have 
 * have been made for use with the NCSA Horizon Image Browser package.
 * In general, developers are free to use, update, and redistribute 
 * this source code.
 *-------------------------------------------------------------------------
 * History: 
 *  97            Original version by T. McGlynn
 *
 *  97 Oct.  Wei Xie   Adapted for use with the Horizon package.
 *                     Package renamed to ncsa.fits.
 */

package ncsa.fits.fits;


import java.io.*;
import java.net.*;
import java.util.*;
import ncsa.fits.util.*;

/** This class provides the functionality to manipulate
  * FITS files at the URL, FILE and HDU level.
  */
public class Fits extends Object implements IO {

    private int		HDUOffset = 0;			// Pointer to current HDU
    private Vector    	HDUlist = new Vector();
    private DataInput 	dataStr;
    
   /** Use an open DataStream.
     * This constructor is the base constuctor for a number of others.
     * @param str The open DataInput object
     */    
    public Fits(DataInput str) {
	
	if (str != null) {
	    this.dataStr = str;
	    HDUOffset = 0;
	}
    }

    /** Create the FITS object using an existing File Object */
    public Fits(File fitsFile) throws IOException {
	this(new RandomAccessFile(fitsFile, "r"));
    }
    
    /** Create the FITS object from a given file name */
    public Fits(String filename) throws NullPointerException, IOException {
  	this(new File(filename));
    }

    /** Create the FITS object from a given URL */
    public Fits (URL myURL) throws IOException {
	this (new DataInputStream(myURL.openStream()));
    }
    
   
    /** Read an entire FITS file.
      * This function reads all HDU's in a FITS object.  It
      * will try to go back and read any HDU's which were skipped earlier.
      * @return an array HDUs.  The first element is the primary image
      * and any further elements are extensions.
      */
    public HDU[] readHDUs() {
	
	HDU[] allHDUs;
	
	if (!isConnected()) {
	    return null;
	}
	finishReading();
	allHDUs = new HDU[HDUlist.size()];
	
	HDUlist.copyInto(allHDUs);
	return allHDUs;
    }
	
    /** Read the next HDU.
      * If the HDU is cached simply return the already read HDU.
      */
    public HDU   readHDU() {
	
	HDU myHDU;
	
	// First Handle the case where we've already read
	// this HDU.
	if (HDUOffset  < HDUlist.size()) {
	    
	    long eof = getLastByte(HDUlist);
	    
	    try {
		myHDU = (HDU) HDUlist.elementAt(HDUOffset);
	    } catch (ArrayIndexOutOfBoundsException e) {
		System.err.println("System Exception: "+e);
		return null;
	    }
	    
	    if (!myHDU.haveData()) {
		// Try to fill the data.  This may not succeed if
		// the dataStr is not seekable.  We just return what we can.
		myHDU.fillData(eof);
            }
	    HDUOffset += 1;
	    return myHDU;
	    
	} else {
	    // Now do the case where we have to read in a new HDU.
	    
	    if (nextHDU(true)) {
		
		try {
		    return (HDU) HDUlist.lastElement();
		} catch (NoSuchElementException e) {
		    System.out.println("Caught no such element\n");
		    return null;
		}
		
	    } else {
		return null;
	    }
	}
    }
    private boolean nextHDU(boolean needData) {
	    
	HDU myHDU;
     
	myHDU = new HDU(dataStr, HDUlist,  needData);
	
	
	if (myHDU.isValid()) {
	    
	    HDUlist.addElement(myHDU);
	    HDUOffset += 1;
	    
	    return true;
		
        } else {
	    
	    if (HDUlist.size() > 0) {
		try {
	     	    ((HDU) HDUlist.lastElement()).setLast();
		} catch (NoSuchElementException e) {}
	    }
	    return false;
	
        }
    }
    
   /** Read the n'th HDU.
     * If the HDU is already cached simply return a pointer to the
     * cached data.
     */
    public HDU	 readHDU(int n) {
	
	if (!isConnected()) {
	    return null;
	}
	int delta = n-HDUOffset;
	if (!skipHDU(delta)) {
	    return null;
        }
	return readHDU();
    }

    /** Skip an HDU.
      * This creates a skeletal HDU containing only some fundamental
      * data about the size of the object but this information
      * is not available to the user.
      */
    public boolean  skipHDU(){
	
	if (HDUlist.size() > HDUOffset ){
	    try {
	        if ( ( (HDU)HDUlist.elementAt(HDUOffset)).isLast() ) {
		    return false;
		}
	    } catch (ArrayIndexOutOfBoundsException e) {
		System.err.println("Exception in skipHDU: "+e);
		return false;
	    }
	    HDUOffset += 1;
	    return true;
	    
	} else {
	    return nextHDU(false);
	}
    }
    
    /** Step back an HDU. This is equivalent to skipHDU(-1).
      */
    public boolean  prevHDU(){
	if (HDUOffset <= 0) 
	    return false;
	else {
	    HDUOffset -= 1;
	    return true;
	}
    }
    
    /** Skip a number of HDUs.  This is implemented as
      * a series of calls to skipHDU() or prevHDU().
      */
    
    public boolean skipHDU(int n) {
	
	if (!isConnected()) {
	    return false;
	}
	
	if (n == 0) {
	    return true;
	    
	} else if (n < 0) {
	    
	    while (n<0 && prevHDU()) {
		n += 1;
	    }
	    return (n == 0);
	    
	} else {
	    
	    while (n>0 && skipHDU()) {
		n -= 1;
	    }
	    return (n==0);
	}
    }

    /** Check if the Fits object is connected to data. */ 
    public boolean isConnected() {
	return dataStr != null;
    }
    
    /** Disconnect the Fits object from the File/URL/...
      *  This  flushes all cached HDU information.
      */
    public void disconnect() {
	dataStr = null;
	HDUlist.removeAllElements();
	HDUOffset = 0;
    }
    
    /** Write a FITS file to a DataOutput object.
      */
    public void writeClass(DataOutput dos) throws IOException {
	int i;
	
	if (!isConnected())
	  return;
	finishReading();
	
	HDU  hh;
	for (i=0; i<HDUlist.size(); i += 1) {
	    try {
		hh = (HDU) HDUlist.elementAt(i);
	        hh.writeClass(dos);
	    } catch (ArrayIndexOutOfBoundsException e) {
		System.err.println("Fits.writeClass exception: "+e);
	    }
	}
    }

    /** Read a FITS file from a DataInput object.
      */
    public void readClass(DataInput dis) throws IOException {
	
	if (isConnected() && (dis != dataStr) && (dis != null)) {
	    disconnect();
	    dataStr = dis;
	}
	readHDUs();
    }

    /** List a FITS file on a PrintStream object */
    public void printClass(PrintStream ps) throws IOException {
	int i;
	
	if (!isConnected()) {
	    ps.println("*** No FITS connection ***");
	    return;
	}
	
	finishReading();
	for (i=0; i<HDUlist.size(); i += 1) {
	    ps.println("************************");
	    
	    if (i == 0) {
		ps.println("*****  Primary HDU *****");
	    } else if (i < 10) {
		ps.println("*****  Extension "+i+" *****");
	    } else {
		ps.println("*****  Extension "+i+ "*****");
	    }
		
	    ps.println("************************");
	    try {
		HDU hh = (HDU) HDUlist.elementAt(i);
	        hh.printClass(ps);
	    } catch (ArrayIndexOutOfBoundsException e) {
		System.err.println("Fits.printClass exception: "+e);
	    }
	    ps.print("\n\n");
	}
    }

    /** Complete reading any skipped HDU's and read any unread.
      */
    protected void finishReading() {
	int i;
	boolean last = false;
	
	HDU h=null;
	long eof = getLastByte(HDUlist);
	
        /* First check for any HDU's that were skipped previously */
	for (i=0; i<HDUlist.size(); i += 1) {
	    h = (HDU) HDUlist.elementAt(i);
    	    if ( !h.haveData()) {
		h.fillData(eof);
	    }
	}
	
	if (h != null && !h.isLast()) {
	    return;
	}
	
	/* Read any unread HDUs */
	while (readHDU() != null) {
	}
	return;
    }
    
    /** Find the current maximum penetration of the Fits file. */
    protected long getLastByte(Vector HDUlist) {
	
	if (HDUlist.size() <= 0) {
	    return 0;
	} else {
	    try {
		HDU h = (HDU) HDUlist.lastElement();
		return h.getFileOffset() + h.getHeaderSize() + h.getDataSize();
    	    } catch (NoSuchElementException e) {
		return 0;
	    }
	}
    }
    
    
}
