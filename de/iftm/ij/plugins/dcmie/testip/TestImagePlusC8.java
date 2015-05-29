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
package de.iftm.ij.plugins.dcmie.testip;

import ij.*;
import ij.process.*;


/**
 * Creates a 8Bit palette-color ImagePlus with binary and string DICOM properties.
 *
 * @author   Thomas Hacklaender
 * @version  2002.06.10
 */
public class TestImagePlusC8 extends TestImagePlus {

  
  /** Creates a new instance */
  public TestImagePlusC8() {
    super("resources/farbe8.gif");
    
    // ip ist vom Typ IndexedColor
    this.setTitle("ImageTypeIndexedColor8");
    
  }

  
  public void convertToRGB() {
    ic.convertToRGB();
    this.setTitle("ImageTypeRGB");
  }
  
}
