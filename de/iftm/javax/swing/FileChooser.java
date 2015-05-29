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
import java.io.*;
import javax.swing.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.6.23
 */
public class FileChooser {


	/**
	 * Displays a JFileChosser for selecting an input file.
   * @return the selected File or null, if the user has canceled the operation.
	 */
  public static File openFile(String title, File rootDir) {
    JFileChooser chooser = new JFileChooser();
    if (title == null) title ="";
    if (title == "") {
      chooser.setDialogTitle("Select a file...");
    } else {
      chooser.setDialogTitle(title);
    }
    if (rootDir == null) {
      rootDir = new File(System.getProperty("user.dir"));
    }
    chooser.setCurrentDirectory(rootDir);
    JFrame dummy = new JFrame();
		chooser.showOpenDialog(dummy);
    File f = chooser.getSelectedFile();
    dummy.dispose();
		return f;
  }


	/**
	 * Displays a JFileChosser for selecting an input directory.
   * @return the selected File or null, if the user has canceled the operation.
	 */
  public static File openDir(String title, File rootDir) {
    JFileChooser chooser = new JFileChooser();
    if (title == null) title ="";
    if (title == "") {
      chooser.setDialogTitle("Select a file...");
    } else {
      chooser.setDialogTitle(title);
    }
    if (rootDir == null) {
      rootDir = new File(System.getProperty("user.dir"));
    }
    chooser.setCurrentDirectory(rootDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    JFrame dummy = new JFrame();
		chooser.showOpenDialog(dummy);
    File f = chooser.getSelectedFile();
    dummy.dispose();
		return f;
  }


	/**
	 * Displays a JFileChosser for saving an output file.
   * @return the selected File or null, if the user has canceled the operation.
	 */
  public static File saveFile(String title, File rootDir, String defaultName) {
    JFileChooser chooser = new JFileChooser();
    if (title == null) title = "";
    if (title == "") {
      chooser.setDialogTitle("Save to...");
    } else {
      chooser.setDialogTitle(title);
    }
    if (rootDir == null) {
      rootDir = new File(System.getProperty("user.dir"));
    }
    chooser.setCurrentDirectory(rootDir);
    if (defaultName == null) {
      chooser.setSelectedFile(new File(""));
    } else {
      chooser.setSelectedFile(new File(rootDir, defaultName));
    }
    JFrame dummy = new JFrame();
		chooser.showSaveDialog(dummy);
    File f = chooser.getSelectedFile();
    dummy.dispose();
		return f;
  }
  
}
