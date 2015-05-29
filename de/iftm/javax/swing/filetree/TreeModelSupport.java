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

import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;


/**
 * Diese Methode funktioniert als event source:
 *
 * Sie stellt zunaechst die beiden Methoden
 *
 *  - void addTreeModelListener(TreeModelListener listener)
 *  - void removeTreeModelListener(TreeModelListener listener)
 *
 * zur Verfügung, mit denen sich interessierte Klassen bei TreeModelSupport
 * als listener registrieren koennen. Diese Klassen muessen dazu das Interface
 * javax.swing.event.TreeModelListener implementieren. Dieses verlangt die
 * Methoden:
 *
 *  - void treeNodesChanged(TreeModelEvent e) : Invoked after a node (or a set of siblings) has changed in some way.
 *  - void treeNodesInserted(TreeModelEvent e) : Invoked after nodes have been inserted into the tree.
 *  - void treeNodesRemoved(TreeModelEvent e) : Invoked after nodes have been removed from the tree.
 *  - void treeStructureChanged(TreeModelEvent e) : Invoked after the tree has drastically changed structure from a given node down.
 *
 * Korrespondierend zu diesen Listener-Methoden implementiert TreeModelSupport
 * vier fireTreeXxx(TreeModelEvent e) Methoden.
 *
 * @author   Thomas Hacklaender
 * @version  2002.5.4
 */
public class TreeModelSupport {
	private Vector	vector = new Vector();


	/**
	 * Add a TreeModelListener.
	 * @param listener the new listener.
	 */
	public void addTreeModelListener(TreeModelListener listener) {
		if (listener != null &&!vector.contains(listener)) {
			vector.addElement(listener);
		} 
	} 


	/**
	 * Remove a TreeModelListener.
	 * @param listener the listener to remove.
	 */
	public void removeTreeModelListener(TreeModelListener listener) {
		if (listener != null) {
			vector.removeElement(listener);
		} 
	} 


	/**
	 * Fire a TreeNodesChanged Event.
	 * @param e the Event.
	 */
	public void fireTreeNodesChanged(TreeModelEvent e) {
		Enumeration listeners = vector.elements();

		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners.nextElement();

			listener.treeNodesChanged(e);
		} 
	} 


	/**
	 * Fire a TreeNodesInserted Event.
	 * @param e the Event.
	 */
	public void fireTreeNodesInserted(TreeModelEvent e) {
		Enumeration listeners = vector.elements();

		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners.nextElement();

			listener.treeNodesInserted(e);
		} 
	} 


	/**
	 * Fire a TreeNodesRemoved Event.
	 * @param e the Event.
	 */
	public void fireTreeNodesRemoved(TreeModelEvent e) {
		Enumeration listeners = vector.elements();

		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners.nextElement();

			listener.treeNodesRemoved(e);
		} 
	} 


	/**
	 * Fire a TreeStructureChanged Event.
	 * @param e the Event.
	 */
	public void fireTreeStructureChanged(TreeModelEvent e) {
		Enumeration listeners = vector.elements();

		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners.nextElement();

			listener.treeStructureChanged(e);
		} 
	} 

}
