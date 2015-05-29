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
package de.iftm.java.util;

import java.io.*;
import java.util.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.5.6
 */
public class FileSelectionEvent extends java.util.EventObject {
  
  private File[]    selectedFiles = null;
  private File      lastSelectedFile = null;

  
  /**
   *
   */
  public FileSelectionEvent(Object source) {
    super(source);
  }

  
  /**
   *
   */
  public void setLastSelectedFile(File f) {
    lastSelectedFile = f;
  }
  
  
  /**
   *
   */
  public File getLastSelectedFile() {
    return lastSelectedFile;
  }

  
  /**
   *
   */
  public void setSelectedFiles(File[] fa) {
    selectedFiles = fa;
  }
  
  
  /**
   *
   */
  public File[] getSelectedFiles() {
    return selectedFiles;
  }
  
}
