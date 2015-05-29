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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *
 * http://www.gnu.org/copyleft/copyleft.html
 */
package de.iftm.javax.swing.filetree;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import de.iftm.java.util.*;


/**
 * Stellt das Filesystem als Tree auf einem Panel dar.<br>
 * <br>
 * Versendet FileSelection Events (Methode: fileSelected(FileSelectionEvent e))
 * an registrierte FileSelectionListener. Potentielle Listener muessen das
 * Interface FileSelectionListener implementieren und sich mit der Method
 * addFileSelectionListener(FileSelectionListener listener) registrieren.<br>
 * <br>
 * Note:<br>
 * Unterstuetzt z.Zt. nur die Selektion eines Files. Dies wird an zwei Stellen
 * festgelegt:<br>
 * In der Methode createNewTree durch setSelectionMode(SINGLE_TREE_SELECTION)<br>
 * In der Methode processValueChanged durch auffuellen des Fields selectedFiles.<br>
 * <br>
 * @author   Thomas Hacklaender
 * @version  2002.5.19
 */
public class FileSystemTreePanel extends JPanel implements java.io.Serializable {
  
	private JTree tree = null;

  
  // Liste mit Listenern
	private Vector	listenerVector = new Vector();


	/**
	 * Constructor declaration
	 */
	public FileSystemTreePanel() {
		this((FileSystemModel) null);
	}


	/**
	 * Constructor declaration
	 * @param startPathFile the root path for the tree.
	 */
	public FileSystemTreePanel(File startPathFile) {
		this(new FileSystemModel(startPathFile));
	}


	/**
	 * Constructor declaration
	 * @param model the TreeModel of the tree. The model should be created with the
   *              root path of the tree.
	 */
	public FileSystemTreePanel(FileSystemModel model) {
		initComponents();
    if (model != null) createNewTree(model);
	}


	/**
	 * Creates a new tree using the TreeModel model.
	 * @param model the TreeModel of the tree
	 */
	public void createNewTree(FileSystemModel model) {
    
    // Falls tree schon auf diesem Container dargestellt wird: entfernen
    if (tree != null) {
      remove(tree);
    }
    
    // Neuen Tree erzeugen
		tree = new JTree(model) {

      /**
       * Called by the renderers to convert the specified value to text. The
       * default implementation returns value.toString, ignoring all other
       * arguments.To control the conversion, subclass this method and use any
       * of the arguments you need.
       * @param value  the Object to convert to text
       * @param selected true if the node is selected
       * @param expanded true if the node is expanded
       * @param leaf true if the node is a leaf node
       * @param row an integer specifying the node's display row, where 0 is the first row in the display
       * @param hasFocus true if the node has the focus
       * @return the String representation of the node's value
       */
      public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        return ((File) value).getName();
      }
  
    };
    
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
    // JComponent: Adds an arbitrary key/value "client property" to this component.
		// tree.putClientProperty("JTree.lineStyle", "None");
		// tree.putClientProperty("JTree.lineStyle", "Horizontal");
		// tree.putClientProperty("JTree.lineStyle", "Angled");
    
    // Zunaechst nur einfache Selektion zulassen
    // tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // TreeSelectionListener registrieren
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        processValueChanged(e);
      }
    });

    // Neuen Tree dem Container hinzufuegen
		add(tree, BorderLayout.CENTER);
    invalidate();
	} 


	/**
   * Get the currently displayed tree.
	 * @return the displayed tree.
	 */
	public JTree getTree() {
		return tree;
	} 

  
  /**
   * This method is called from within the constructor to initialize the form.
   */
  private void initComponents() {
		// Bei dem Constructor tree = new JTree() wird ein Beispieltree, entspricht
    // DefaultTreeModel, dargestellt
		tree = new JTree((TreeNode) null);

    tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
    // JComponent: Adds an arbitrary key/value "client property" to this component.
		tree.putClientProperty("JTree.lineStyle", "Angled");

    setLayout(new BorderLayout());
		add(tree, BorderLayout.CENTER);
  }
  
  
  /**
   * Process the TreeSelectionEvent from the fileSystemTree
   * @param e the TreeSelectionEvent.
   */
  public void processValueChanged (TreeSelectionEvent tse) {
    File                lastSelectedFile;
    File[]              fa;
    Vector              selectedFiles = new Vector();
    TreePath[]          paths;
    FileSelectionEvent  fse;
    
    // Das fileSystemTreePanel verwendet das FileSystemModel als Datenmodell.
    // Darin sind die Knoten des Trees immer als File abgebildet.
    paths = tree.getSelectionPaths();
    
    // Falls der gesamte Pfad deselektiert wurde
    if (paths == null) return;
    
    // Neuen Event generieren
    fse = new FileSelectionEvent(this);
    
    // Die selektierten Files in den Event eintragen
    if (paths.length >= 0) {
      for (int i = 0; i < paths.length; i++) {
        // Directories ignorieren
        if (((File) paths[i].getLastPathComponent()).isFile()) {
          selectedFiles.addElement(paths[i].getLastPathComponent());
        }
      }
    }
    fa = (File[]) selectedFiles.toArray(new File[0]);
    fse.setSelectedFiles(fa);

    // Den zuletzt selektierten File in den Event eintragen
    if (fa.length == 0) {
      fse.setLastSelectedFile(null);
    } else {
      fse.setLastSelectedFile(fa[fa.length - 1]);
    }
    
    // Event an alle Listener verschicken
		fireFileSelectionEvent(fse);
  }
  
  
  /**
   * FileSelectionEvent an alle Listener verschicken
   * @param fse the FileSelectionEvent.
   */
  private void fireFileSelectionEvent(FileSelectionEvent fse) {
    // Event an alle Listener verschicken
		Enumeration listeners = listenerVector.elements();
		while (listeners.hasMoreElements()) {
			FileSelectionListener listener = (FileSelectionListener) listeners.nextElement();
			listener.fileSelected(fse);
		} 
  }

  
	/**
	 * Add a FileSelectionListener.
	 * @param listener the new listener.
	 */
	public void addFileSelectionListener(FileSelectionListener listener) {
		if (listener != null && !listenerVector.contains(listener)) {
			listenerVector.addElement(listener);
		} 
	} 


	/**
	 * Remove a FileSelectionListener.
	 * @param listener the listener to remove.
	 */
	public void removeFileSelectionListener(FileSelectionListener listener) {
		if (listener != null) {
			listenerVector.removeElement(listener);
		} 
	} 
  
}
