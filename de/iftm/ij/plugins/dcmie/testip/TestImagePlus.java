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
 * Creates an ImagePlus.
 *
 * @author   Thomas Hacklaender
 * @version  2002.06.11
 */
public class TestImagePlus extends ij.ImagePlus {
  
  ImageConverter  ic = null;

  
  /** Creates a new instance of TestImagePlus */
  public TestImagePlus(String path) {
    super();

    BufferedImage bi = null;
    
    try {
      bi = ImageIO.read(getClass().getResourceAsStream(path));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    this.setImage(bi);    
    ic = new ImageConverter(this);
  }
  
  
  /**
   * Adds the default Dataset with the given StudyID, SeriesNumber and image 
   * InstanceNumber to this ImagePlus.
   * @param the StudyID,.
   * @param the SeriesNumber.
   * @param the InstanceNumber.
   * @param asString true, if properties should be added in the string representation.
   * @param asBinary true, if properties should be added in the binary representation.
  */
  public void addProperties(String patientID, int studyID, int seriesNumber, int instanceNumber, boolean asString, boolean asBinary) {
    Dataset[] dsArray = {DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), studyID, seriesNumber, instanceNumber)};
    IPPropertiesUtil.setImagePlusProperties(this, dsArray, asString, asBinary);
  }
  
}
