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
package de.iftm.dcm4che;

import java.awt.image.*;
import org.dcm4che.data.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.08.20
 */
public class DcmDataImage {
  
  /**
   *
   */
  private Dataset           dataset = null;
  
  /**
   *
   */
  private BufferedImage[]   imageArray = null;

  
  /**
   * Creates a new, empty instance of DcmDataImage.
   */
  public DcmDataImage() {
  }

  
  /**
   * Creates a new instance of DcmDataImage.
   * @param ds the Dataset.
   * @param img the BufferedImage to construct a one dimensional array with the 
   *            given value..
   */
  public DcmDataImage(Dataset ds, BufferedImage img) {
    setDataset(ds);
    setImage(img);
  }

  
  /**
   * Creates a new instance of DcmDataImage.
   * @param ds the Dataset.
   * @param imgArray the array of BufferedImage.
   */
  public DcmDataImage(Dataset ds, BufferedImage[] imgArray) {
    setDataset(ds);
    setImageArray(imgArray);
  }
  
  
  /**
   * Set property dataset.
   * @param ds the Dataset.
   */
  public void setDataset (Dataset ds) {
    dataset = ds;
  }
  
  
  /**
   * Get property dataset
   * @return the Dataset.
   */
  public Dataset getDataset () {
    return dataset;
  }
  
  
  /**
   * Set the property imageArray to a one dimensional array with th given value.
   * @param img the BufferedImage.
   */
  public void setImage (BufferedImage img) {
    imageArray = new BufferedImage[1];
    imageArray[0] = img;
  }
  
  
  /**
   * Set the property imageArray[.
   * @param imgArray the array of BufferedImage.
   */
  public void setImageArray (BufferedImage[] imgArray) {
    imageArray = imgArray;
  }
  
  
  /**
   * Get the prperty imageArray.
   * @return the BufferedImage.
   */
  public BufferedImage[] getImageArray () {
    return imageArray;
  }
  
}
