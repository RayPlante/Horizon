/**
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
 * Comments and questions are welcome and can be sent to
 * horizon@ncsa.uiuc.edu.
 */

/*
 * Modification history:
 *    22-Aug-1997 Wei Xie     Initial version.
 *    19-Jan-1998 Ray Plante  moved from ncsa.horizon.util to ncsa.horizon.data
 */

package ncsa.horizon.data;

/**
 * The abstract class define the interface to update
 * a NdArrayData object until it is complete.  The strategy
 * to update the NdArrayData depends on the implementer.
 */
public abstract class NdArrayDataUpdater {
  public abstract void update(NdArrayData dataVolume);
}
