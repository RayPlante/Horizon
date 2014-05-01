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

package ncsa.fits.util;

/** The IO interface defines routines which indicate
  * that the current class supports input and output of the
  * class object.  It is generally expected that one may use
  * readData on the output of writeData to restore the value
  * of a class.
  */

import java.io.*;

public interface IO {

public void readClass(DataInput is) throws IOException;
public void writeClass(DataOutput os) throws IOException;
public void printClass(PrintStream ps) throws IOException;

}
