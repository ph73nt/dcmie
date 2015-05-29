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
package de.iftm.javax.swing.filetree;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;


/**
 * Diese Klasse bildet das lokale File-System in der "Sprache" des TreeModel
 * Interfaces ab.
 *
 * Die Parameter vom Typ "Object" sind hier generell auf den Type "File"
 * eingeschraenkt.
 *
 * Ein TreeModel arbeitet unabhaengig von der konkreten Implementation mit
 * einigen Annahmen:
 * 1. Es existiert genau ein root-Objekt (Knoten), das kein Eltern-Objekt besitzt.
 * 2. Jeder Knoten verwaltet seine Kinder in einem Array
 * 3. Kinder koennen ueber einen Index identifiziert werden.
 *
 * Diese Klasse implementiert diejenigen Methoden konkret , die in der Klasse
 * AbstarctTreeModel noch abstrakt deklariert sind.<br>
 * <br>
 * To do:<br>
 * In setDirectory(File dirFile) ueberpruefen, ob dirFile ein Directory ist!<br>
 * <br>
 * @author   Thomas Hacklaender
 * @version  2002.5.4
 */
public class FileSystemModel extends AbstractTreeModel implements Serializable {

	private File    root = null;


	/**
	 * Der root-Knoten wird auf das Verzeichnis "user.dir" gesetzt.
	 */
	public FileSystemModel() {
		this((File) null);
	}


	/**
	 * Der root-Knoten wird auf das Verzeichnis startPath gesetzt.
	 * @param startPathFile der Pfad zum root-Verzeichnis des Trees.
	 */
	public FileSystemModel(File startPathFile) {
    setDirectory(startPathFile);
	}

  
  /**
   * Sets a new base directory.
   * @param dirFile the new base directory.
   */
  public void setDirectory(File dirFile) {
    
    // Achtung: Noch ueberpruefen, ob es ein Directory ist!
    
    root = dirFile;
  }


	/**
	 * Returns the root of the tree. Returns null only if the tree has no nodes.
	 * @return the root of the tree.
	 */
	public Object getRoot() {
    return root;
	} 


	/**
	 * Returns true if node is a leaf. It is possible for this method to return
   * false  even if node has no children. A directory in a filesystem, for
   * example, may contain no files; the node representing the directory is not
   * a leaf, but it also has no children.
	 * @param node a node in the tree, obtained from this data source.
	 * @return true if node is a leaf
	 */
	public boolean isLeaf(Object node) {
		return ((File) node).isFile();
	} 


	/**
	 * Returns the number of children of parent. Returns 0 if the node is a leaf
   * or if it has no children. parent must be a node previously obtained from
   * this data source.
	 * @param parent a node in the tree, obtained from this data source.
	 * @return the number of children of the node parent
	 */
	public int getChildCount(Object parent) {
		File	fileSysEntity = (File) parent;

		if (fileSysEntity.isDirectory()) {
			String[]	children = fileSysEntity.list();

			return children.length;
		} else {
			return 0;
		} 
	} 


	/**
	 * Returns the child of parent at index index  in the parent's child array.
   * parent must be a node previously obtained from this data source. This
   * should not return null if index is a valid index for parent (that is
   * index >= 0 &&  index < getChildCount(parent)).
   * @param parent a node in the tree, obtained from this data source.
   * @param index the index of the child.
	 * @return the child of parent at index index
	 */
	public Object getChild(Object parent, int index) {
		File			directory = (File) parent;
		String[]	children = directory.list();

    Arrays.sort(children);
    
		return new File(directory, children[index]);
	} 


	/**
	 * Returns the index of child in parent. If parent is null or child is null,
   * returns -1.
	 * @param parent a note in the tree, obtained from this data source
	 * @param child the node we are interested in
	 * @return the index of the child in the parent, or -1 if either child or parent are null
	 */
	public int getIndexOfChild(Object parent, Object child) {
		File			directory = (File) parent;
		File			fileSysEntity = (File) child;
		String[]	children = directory.list();
		int				result = -1;

    Arrays.sort(children);
    
		for (int i = 0; i < children.length; ++i) {
			if (fileSysEntity.getName().equals(children[i])) {
				result = i;
				break;
			} 
		} 

		return result;
	} 


	/**
   * Diese Methode benoetigt nur dann eine von null verschiedene Implementation
   * wenn der Anwender einen Wert innerhalb des Trees aendern kann. Das ist
   * hier nicht der Fall.
	 * Messaged when the user has altered the value for the item identified by
   * path to newValue. If newValue signifies a truly new value the model should
   * post a treeNodesChanged event.
	 * @param path path to the node that the user has altered
	 * @param newValue the new value from the TreeCellEditor
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
    // Anwender kann Tree nicht aendern
  }
  
}

