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
 *                     A few bug fixes.
 *                     Package renamed to ncsa.fits.
 */

package ncsa.fits.fits;

import java.io.*;
import java.util.*;
import ncsa.fits.util.*;


public class Header extends Object implements IO {

  private String  cards[];

  public Header (){
  }
    
  public Header(DataInput is) {
    readHeader(is);
  }
    
  public Header(String cards[]) {
    this.cards = new String[cards.length];
    System.arraycopy(cards, 0, this.cards, 0, cards.length);
  }

  /**
   * return how many valid cards this header contains
   */
  public int getCardSize() {
    return cards.length;
  }
    
  public long getSize() {
    if (validHeader()) {
      return ( ((cards.length + 35)/36)*2880);
    }
    else {
      return 0;
    }
  }
    
  public long dataSize() {
    if (validHeader()) {
      long size;
      size = 1;
      int axis;
      for (axis = 0; axis<getLValue("NAXIS", 0L); axis += 1) {
	size *= getLValue("NAXIS"+(axis+1), 0L);
      }
      size += getLValue("PCOUNT", 0L);
      size *= getLValue("GCOUNT", 1L);
      size *= Math.abs(getLValue("BITPIX", 0L))/8;
	    
      return ((size + 2779)/2880)*2880;
    }
    else {
      return 0;
    }
  }
	    
  public boolean validHeader() {
    // Probably should do something more sophisticated than this...
    if (cards == null || cards.length < 5) {
      return false;
    }
    else {
      return true;
    }
  }
    
  public String getCard(int n) {
    if (n<0 || n >= cards.length) {
      return null;
    }
    else {
      return cards[n];
    }
  }
    
  public long getLValue(String key, long dft) {
    String val = checkKey(key);
    if (val == null) {
      return dft;
    }
      
    long res;
    try {
      res = Long.parseLong(val);
    }
    catch (NumberFormatException e) {
      System.err.println("Key "+key+" not integer value:"+val);
      return 0;
    }
    return res;
  }
    
  public double  getDValue(String key, double dft) {
    String val = checkKey(key);
    if (val == null) {
      return dft;
    }
    double res;
    try {
      res = new Double(val).doubleValue();
    } catch (NumberFormatException e) {
      System.err.println("Key "+key+" not double value:"+val);
      return 0.;
    }
    return res;
  }
      
  public boolean getBValue(String key, boolean dft) {
    String val = checkKey(key);
    if (val == null) {
      return dft;
    }
	
    if (val.equals("T")) {
      return true;
    }
    else if (val.equals("F")) {
      return false;
    }
    else {
      System.err.println("Key "+key+" not boolean: val="+val);
      return false;
    }
  }  
	
  public long getLValue(String key) {
    return getLValue(key, 0L);
  }

  public double getDValue(String key) {
    return getDValue(key, 0.);
  }
	    
  public boolean getBValue(String key) {
    return getBValue(key, false);
  }
    
  public String  getSValue(String key) {
    
    String val = checkKey(key);
    if (val.substring(0,1).equals("'")) {
      // Strip the quotes
      return val.substring(1,val.length()-1);
    }
    else {
      System.err.println("Key "+key+" not string:" + val);
      return null;
    }
  }
  
  public boolean readHeader(DataInput dis) {
    /* Get the Header in 80 byte chunks.
     */
	
    Vector vcards = new Vector(500,100);
    byte buffer[] = new byte[80];
    boolean firstCall = true;
    
    while (true) {
      /* Another kludge here.
       * readFully(byte[]) will hang here on a
       * RandomAccessFile if we're at the end.
       * So...
       */
      try {
	if (dis instanceof RandomAccessFile) {
	  if (((RandomAccessFile) dis).length() <
	      ((RandomAccessFile) dis).getFilePointer()+80) {
	    throw new EOFException();
	  }
	}
	dis.readFully(buffer);
      }
      catch (EOFException e) {
	return false;
      }
      catch (IOException e) {
	return false;
      }
      
      String str = new String(buffer, 0);
      if (firstCall && str.substring(0,4).equalsIgnoreCase("HTTP") ) {
	  
	// This is a bit of a kludge too.  Some Web servers
	// will return information at the beginning
	// of the stream.  We assume that the existence of a
	// "HTTP" at the beginning of a headers signals such
	// information.  So what we need to do is go until
	// we find a blank line and then start reading
	// after that.  Note that a blank line may have
	// both a \r and \n character.  We assume that
	// \n ends the line.
	  
	buffer = findStart(new String(buffer,0), dis);
	if (buffer == null) {
	  return false;
	}
	str = new String(buffer, 0);
      }
      firstCall = false;
	
      vcards.addElement(str);
      if (str.substring(0,8).equals("END     ") ){
	break;
      }
    }
      
    int blanks = 36 - vcards.size() % 36;
    if (blanks != 36) {
      while (blanks>0) {
	try {
	  dis.readFully(buffer);
	  blanks -= 1;
	}
	catch (EOFException e) {
	  return false;
	} catch (IOException e) {
	  return false;
	}
      }
    }
      
    cards = new String[vcards.size()];
    vcards.copyInto(cards);
      
    return true;
  }
	
  int findKey(String key) {
    String mykey = (key+"        ").substring(0,8);
    int i;
	
    for (i=0; i<cards.length; i += 1) {
      if (cards[i].substring(0,8).equalsIgnoreCase(mykey)) {
	return i;
      }
    }
    return -1;
  }
    
  String valStr(String card) {
    // We are going to assume that the value has no blanks in
    // it unless it is enclosed in quotes.  Also, we assume that
    // a / terminates the string (except inside quotes)
    
    // Make sure that this is a value keyword.
    if (!card.substring(8,9).equals("=")) {
      return null;
    }
	
    int vstart = -1;
    int vend = -1;
    boolean quote = false;
    
    int offset = 9;
    while (offset < 80 &&  card.substring(offset, offset+1).equals(" ")) {
      offset += 1;
    }
	
    if (offset >= 80) {
      return null;
    }
    vstart = offset;
	
    // If we have a ' then find the matching quote.
    if (card.substring(offset, offset+1).equals("'")) {
      
      offset += 1;
      while (offset < 80) {
	
	vend = card.indexOf("'", offset);;
	if (vend == 79 || !card.substring(vend+1,vend+2).equals("'")) {
	  break;
	}
	offset = vend+2;
	vend = -1;   // Found escaped '
      }
      if (vend < 0) {
	return null;
      }
      return card.substring(vstart, vend+1);
    }

    // Otherwise we can look for a space or a / to terminate
    // the field.  Note that it's probably legal FITS to
    // put the comment immediately after the value but it's
    // bad practice and we won't worry about it yet.
    
    int spaceLoc = card.indexOf(" ", vstart+1);
    int slashLoc = card.indexOf("/", vstart+1);
    
    if (spaceLoc<0) spaceLoc=80;
    if (slashLoc<0) slashLoc=80;
    
    vend = spaceLoc;
    if (slashLoc < spaceLoc) vend = slashLoc;
    
    if (vend < 79) {
      return card.substring(vstart, vend);
    } else {
      return card.substring(vstart);
    }
  }

  String checkKey(String key) {
    int card = findKey(key);
    if (card < 0) {
      return null;
    }
    return  valStr(cards[card]);
  }
    
  public void readClass (DataInput dis) throws IOException {
    readHeader(dis);
  }

  public void writeClass (DataOutput dos) throws IOException {
    int i;
    if (validHeader()) {
      for (i=0; i<cards.length; i += 1) {
	dos.writeBytes(cards[i]);
      }
      int pad = 36 - cards.length%36;
      if (pad != 36) {
	
	for (i=0; i<4*pad; i += 1) {
	  // For convenience I just use a 20 char blank.
	  dos.writeBytes("                    ");
	}
      }
    }
  }
   
  public void printClass(PrintStream ps) throws IOException {
    int i;
    if (validHeader()) {
      for (i=0; i<cards.length; i += 1) {
	ps.println(cards[i]);
      }
    }
  }
    
  /** This routine finds the beginning of the actual data assuming
   * it has been passed a data inputstream which contains HTTP
   * header information.
   */
  protected byte[] findStart(String buffer, DataInput dis) {
    /* We need to find a blank line, i.e., the sequence
     *   \n\n or \n\r\n.  There are three possibilities:
     * 
     *      The sequence line is in buffer.
     *      The sequence is partially within buffer
     *      The sequence has not yet been read.
     */
    System.out.println(getClass().getName() + "findStart:\n" + buffer);  
    int p = buffer.indexOf("\n\n");
    int q = buffer.indexOf("\n\r\n");
	
    /* First case */
    if (p > 0 && q > 0) {
      if (p < q) {
	return adjustBuffer(buffer, p+2, dis);
      }
      else {
	return adjustBuffer(buffer, q+3, dis);
      }
    }
    else if (p > 0) {
      return adjustBuffer(buffer, p+2, dis);
    }
    else if (q > 0) {
      return adjustBuffer(buffer, q+3, dis);
    }
	
    boolean haveNL = false;
	
    /* Check for second case */
    if (buffer.substring(78).equals("\n\r") ||
	buffer.substring(79).equals("\n")     ) {
      haveNL = true;
    }
    
    /* Now handle second and third cases */
    byte curr;
    while (true) { 
      try {
	curr = dis.readByte();
      } catch (IOException e) {
	return null;
      }
      
      if (haveNL && (curr == '\n')) {
	return(adjustBuffer(buffer, 80, dis));
      }
      if (curr == '\n') {
	haveNL = true;
      }
      else if (curr != '\r') {
	haveNL = false;
      }
    }
  }

  protected byte[] adjustBuffer(String obuf, int pos, DataInput dis) {
    StringBuffer buf;
    int need;
    buf = new StringBuffer();
	
    if (pos < 80) {
      buf.append(obuf.substring(pos));
      need = 80-pos;
    }
    else {
      need = 80;
    }
	
    try {
      byte[] temp = new byte[need];
      
      dis.readFully(temp);
      buf.append(new String(temp,0));
	    
      DataInputStream sbis = new DataInputStream(
			        new StringBufferInputStream(
					     new String(buf)));
      temp = new byte[80];
      sbis.readFully(temp);
      return temp;
	    
    }
    catch (IOException e) {
      return null;
    }
  }
        
}
