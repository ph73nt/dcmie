/*
 * Copyright (C) 2002 Thomas Hacklaender, mailto:hacklaender@iftm.de
 *
 * IFTM Institut fuer Telematik in der Medizin GmbH, www.iftm.de
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * http://www.gnu.org/copyleft/copyleft.html
 */
package de.iftm.javax.swing;

import java.awt.*;
import javax.swing.*;


/**
 * This class displays a progress-monitor window.
 * @author   Thomas Hacklaender
 * @version  2002.05.25
 */
public class ProgressWindow {

  
	/**
   *
   */
	private           ProgressMonitor pm;

  
	/**
   *
   */
	private int				aktValue;

  
	/**
   *
   */
  private Frame     dummy = null;

  
	/**
	 * 
	 */
	public ProgressWindow(Component parent, String message, String note, int min, int max) {
		pm = new ProgressMonitor(parent, message, note, min, max);
	}

  
	/**
	 * 
	 */
	public ProgressWindow(String message, String note, int min, int max) {
    dummy = new Frame();
    Utilities.centerOnScreen(dummy);
		pm = new ProgressMonitor(dummy, message, note, min, max);
	}

  
  /**
   *
   */
  public void setMillisToDecideToPopup(int t) {
    pm.setMillisToDecideToPopup(t);
  }

  
  /**
   *
   */
  public int getMillisToDecideToPopup() {
    return pm.getMillisToDecideToPopup();
  }

  
  /**
   *
   */
  public void setMillisToPopup(int t) {
    pm.setMillisToPopup(t);
  }

  
  /**
   *
   */
  public int getMillisToPopup() {
    return pm.getMillisToPopup();
  }

  
	/**
   *
   */
	public void setProgress(int value) {
		aktValue = value;

		// Mit diesem Methodenaufruf wird die Klasse Update in die System Event Queue
		// gelegt. Die System Event Queue ruft dann die Methode run der uebergebenen
    // Klasse Update auf.
		SwingUtilities.invokeLater(new Update());
	} 


	/**
	 * Ueberprueft, ob der Benutzer den "Cancel" Button gedrueckt hat.
	 * @return  true, wenn der Benutzer den "Cancel" Button gedrueckt hat.
	 */
	public boolean isCanceled() {
		return pm.isCanceled();
	} 


	/**
	 * Schliesst das ProgressMonitor Fenster auf dem Bildschirm.
	 */
	public void close() {
		pm.close();
    if (dummy != null) dummy.dispose();
	} 


	/**
	 * Eine "inner class", die nur die Methode run implementiert.
	 * Mit dieser Methode wird der Progressbalken auf den neuen Wert gesetzt.
	 */
	class Update implements Runnable {

		/**
     *
		 */
		public void run() {
			pm.setProgress(aktValue);
		} 

	}

}
