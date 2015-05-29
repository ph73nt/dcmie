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

import javax.swing.tree.*;


/**
 * Diese Klass ist eine Hilfsklasse, die das Interface TreeModel in eine
 * abstarkte Klasse umwandelt, in der schon die Registrierungs-Methoden konkret
 * implementiert sind.
 *
 * Ein TreeModel beschreibt, wie mit den Tree-Daten umgegangen werden muss.
 * Neben den Methoden
 *
 *  - Object getChild(Object parent, int index) : Returns the child of parent at index index in the parent's child array.
 *  - int getChildCount(Object parent) : Returns the number of children of parent.
 *  - int getIndexOfChild(Object parent, Object child) : Returns the index of child in parent.
 *  - Object getRoot() : Returns the root of the tree.
 *  - boolean isLeaf(Object node) : Returns true if node is a leaf.
 *  - void valueForPathChanged(TreePath path, Object newValue) : Messaged when the user has altered the value for the item identified by path to newValue.
 * 
 * benutzt ein TreeModel auch einen Event, TreeModelEvent, der anzeigt, dass 
 * sich der Tree geaendert hat. Das Interface fordert deshalbMethoden zur
 * Registrierung von Listeneren
 *
 *  - addTreeModelListener(TreeModelListener l) : Adds a listener for the TreeModelEvent posted after the tree changes.
 *  - void removeTreeModelListener(TreeModelListener l) : Removes a listener previously added with addTreeModelListener.
 *
 * In der Klasse TreeModelSupport werden schon die Methoden
 *
 *  - adTreeModelListener(TreeModelListener l)
 *  - void removeTreeModelListener(TreeModelListener l)
 *
 * konkret implementiert. Eine Klasse, die AbstractTreeModel "extends" muss von
 * dem Interface TreeModel noch die folgenden Methoden konkret implementieren:
 *
 *  - Object getChild(Object parent, int index)
 *  - int getChildCount(Object parent)
 *  - int getIndexOfChild(Object parent, Object child)
 *  - Object getRoot()
 *  - boolean isLeaf(Object node)
 *  - void valueForPathChanged(TreePath path, Object newValue)
 *
 * @author   Thomas Hacklaender
 * @version  2002.5.4
 */
public abstract class AbstractTreeModel extends TreeModelSupport implements TreeModel {}
