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

import java.awt.image.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.metadata.*;

import ij.*;
import ij.process.*;

import org.dcm4che.data.*;

import de.iftm.dcm4che.dcmie.*;
import de.iftm.ij.plugins.dcmie.*;


/**
 * Creates an ImagePlus-Stack with 2 Studys:<br>
 * 1. Study with 1 series with 2 images.<br>
 * 2. Study with 2 series with 2 images each.<br>
 * All images contain binary and string properties.
 *
 * @author   Thomas Hacklaender
 * @version  2002.06.10
 */
public class TestImageStack extends ij.ImagePlus {
  
  ImageConverter  ic = null;
  
  /** Creates a new instance of TestImageStack */
  public TestImageStack() {
    super();

    Dataset[]       dsArray = new Dataset[6];
    BufferedImage   bi = null;
    ImagePlus       image;
    ImageStack      stack;
    
    try {

      // Einen leeren Stack generieren
      dsArray[0] = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 1, 1, 1);
      bi = ImageIO.read(getClass().getResourceAsStream("resources/1_1_1.gif"));
      image = new ImagePlus("", bi);
      stack = image.createEmptyStack();
      stack.addSlice("1_1_1", image.getProcessor());
      
      dsArray[1] = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 1, 1, 2);
      bi = ImageIO.read(getClass().getResourceAsStream("resources/1_1_2.gif"));
      image = new ImagePlus("", bi);
      stack.addSlice("1_1_2", image.getProcessor());
      
      dsArray[2] = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 2, 1, 1);
      bi = ImageIO.read(getClass().getResourceAsStream("resources/2_1_1.gif"));
      image = new ImagePlus("", bi);
      stack.addSlice("2_1_1", image.getProcessor());
      
      dsArray[3] = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 2, 1, 2);
      bi = ImageIO.read(getClass().getResourceAsStream("resources/2_1_2.gif"));
      image = new ImagePlus("", bi);
      stack.addSlice("2_1_2", image.getProcessor());
      
      dsArray[4] = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 2, 2, 1);
      bi = ImageIO.read(getClass().getResourceAsStream("resources/2_2_1.gif"));
      image = new ImagePlus("", bi);
      stack.addSlice("2_2_1", image.getProcessor());
      
      dsArray[5] = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 2, 2, 2);
      bi = ImageIO.read(getClass().getResourceAsStream("resources/2_2_2.gif"));
      image = new ImagePlus("", bi);
      stack.addSlice("2_2_2", image.getProcessor());
      
      // Stack anhaengen
      this.setStack("TestImageStack", stack);
      this.setSlice(1);
      
      // Properties anhaengen
      IPPropertiesUtil.setImagePlusProperties(this, dsArray, true, true);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    ic = new ImageConverter(this);
  }
  
}
