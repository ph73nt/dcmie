/*
 * Copyright (C) 2002 Thomas Hacklaender, mailto:hacklaender@iftm.de
 *
 * IFTM Institut fuer Telematik in der Medizin GmbH, www.iftm.de
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU  General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * http://www.gnu.org/copyleft/copyleft.html
 */
package de.iftm.ij.plugins.dcmie;


import java.util.*;

import ij.*;
import org.dcm4che.data.*;
import de.iftm.dcm4che.dcmie.*;


/**
 * Utility class for ImageJ ImagePlus-Properties.<br>
 *
 * @author   Thomas Hacklaender
 * @version  2002.08.19
 */
public class IPPropertiesUtil {
  
  
  /**
   * Converts an array of Dataset to properties for a ImagePlus and appends them
   * to that ImagePlus.
   * @param image the ImagePlus to that the properties should be added.
   * @param datasetArray an array of Dataset which contains the properties.
   * @param asString true, if properties should be added in the string representation.
   * @param asBinary true, if properties should be added in the binary representation.
   */
  public static void setImagePlusProperties(ImagePlus image, Dataset[] datasetArray, boolean asString, boolean asBinary) {
    Properties    proper;
    Enumeration   keyEnumeration;
    String        key;
    
    // Properties generieren
    proper = DcmiePropertiesUtil.datasetToProperties(datasetArray, asString, asBinary);
    
    // Alle keys bearbeiten
    keyEnumeration = proper.propertyNames();
    while (keyEnumeration.hasMoreElements()) {
      // Der key ist immer ein String
      key = (String) keyEnumeration.nextElement();
      
      // Property eintragen
      image.setProperty(key, proper.get(key));
    }    
  }
  
}
