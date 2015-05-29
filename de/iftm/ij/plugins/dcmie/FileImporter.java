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

import java.io.*;
import java.util.*;
import java.awt.image.*;

import ij.*;
import ij.process.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.image.*;
import org.dcm4che.imageio.plugins.*;

import de.iftm.dcm4che.*;
import de.iftm.dcm4che.dcmie.*;
import de.iftm.dcm4che.dcmie.imp.*;
import de.iftm.ij.plugins.dcmie.*;
import de.iftm.javax.swing.*;


/**
 * Thread to import images.
 *
 * @author   Thomas Hacklaender
 * @version  2002.05.24
 */
public class FileImporter extends Thread {

  
  /**
   * Array of fFiles to process. Set by the constructor.
   */
  private File[]        selectedFiles;

  
  /**
   * Set of Parameters.
   */
  private DcmieParam    dcmieParam;

  
  /** Creates a new instance of FileImporter */
  public FileImporter(File[] selectedFiles, DcmieParam param) {
    this.selectedFiles = selectedFiles;
    dcmieParam = param;
  }
  
  
  /**
   * The only method one can call in a Thread. Invoke by FileImporter.start().
   */
  public void run() {
    DcmDataImage              ddi = null;
    Vector                    dsVector = new Vector();
    Dataset[]                 dsArray = null;
    ImageProcessor            ip = null;
    ImageStack                ipStack = null;
    ImagePlus                 imgPlus;
    String                    title;
    ProgressWindow            pw = null;
    
    // Falls selectedFiles nicht sinnvoll ist, nichts tun
    if (selectedFiles == null) return;
    if (selectedFiles.length == 0) return;
    
    // Progress Monitor anzeigen
    pw = new ProgressWindow("Importing " + Integer.toString(selectedFiles.length) + " images", "", 0 , selectedFiles.length-1);
    pw.setMillisToDecideToPopup(10);
    pw.setMillisToPopup(200);
    
    // Alle ausgewaehlten Bilder einlesen
    for (int i = 0; i < selectedFiles.length; i++) {
      
      if (pw.isCanceled()) break;
      pw.setProgress(i);
      
      try {
        
        // Naechstes Bild einlesen
        ddi = DcmImportPanel.readFromFilesystem(selectedFiles[i]);

        // Falls Bild erfolgreich eingelesen wurde
        if (ddi != null) {

          // Ggf. alle Bilder eines Multiframe Image bearbeiten
          for (int k = 0; k < ddi.getImageArray().length; k++) {

            // ImageProcessor erzeugen
            ip = getImageProcessor(ddi.getImageArray()[k], ddi.getDataset());

            // Falls noch kein Stack fuer ImageProcessoren existiert neuen erzeugen
            if (ipStack == null) {
              ipStack = new ImageStack(ip.getWidth(), ip.getHeight());
            }

            // Bildtitel ist die Bildnummer
            try {
              title = ddi.getDataset().getString(Tags.InstanceNumber);
            } catch (Exception e) {
              title = "";
            }
            if (ddi.getImageArray().length > 1) {
              // Multiframe: Bildtitel ist die Bildnummer plus Frame Nummer
              title += "-" + Integer.toString(k + 1);
            }

            // Den ImageProcessor dem Stack hinzufuegen. Falls die Dimension des
            // ImageProcessors nicht mit der des ImageStack uebereinstimmt wird
            // von der Klasse ImageStack eine IllegalArgumentException geworfen.
            // Da wir hier in einem try-Statement sind, wird diese durch catch
            // abgefangen und der ImageProcessor wird nicht dem Stack hinzugefuegt.
            ipStack.addSlice(title, ip);
            // Das Dataset dem Vector der Datasets hinzufuegen
            dsVector.addElement(ddi.getDataset());

          } // for
        } // if

      } catch (Exception ignore) {}
      
    }
    
    // Alle Files bearbeitet
    pw.close();
    
    // Falls keine Bilder gefunden wurden nichts weiter tun
    if (ipStack == null) return;

    // Den Vector in ein Array umwandel
    dsArray = (Dataset[]) dsVector.toArray(new Dataset[0]);
    
    if (ipStack.getSize() == 1) {
      imgPlus = new ImagePlus(ipStack.getSliceLabel(1), ipStack.getProcessor(1));
    } else {
      imgPlus = new ImagePlus(ipStack.getSliceLabel(1), ipStack);
    }

    // Ggf. alle Metadaten aanhaengen
    IPPropertiesUtil.setImagePlusProperties(imgPlus, dsArray, dcmieParam.isMetadataString, dcmieParam.isMetadataBinary);

    // Ggf. Bild anzeigen
    if (dcmieParam.isImageShow) {
      imgPlus.show();
    }
    
  }

  
  /**
   * Creats a new ImageProcessor for a BafferedImage.
   * @param bi the BufferedImage
   * @param ds the Dataset of the image
   */
  private ImageProcessor getImageProcessor(BufferedImage bi, Dataset ds) {
    ImageProcessor    ip = null;
    DataBuffer        buf;
    ColorModel        cm;
    String            pmi;
    ColorModelParam   cmParam;
    double            min;
    double            max;
    byte[]            lut;
        
    // Photometric Interpretation
    try {
      pmi = ds.getString(Tags.PhotometricInterpretation, null);
    } catch (Exception e) {
      return null;
    }

    if (("MONOCHROME1".equals(pmi)) | ("MONOCHROME2".equals(pmi))) {
      
      // Monochrome Bilder
      
      // Bildparameter extrahieren
      try {
        // Color Model Parameter
        cmParam = ColorModelFactory.getInstance().makeParam(ds);
        // Window festlegen
        int bits = ds.getInt(Tags.BitsStored, 8);
        int size = 1 << bits;
        int signed = ds.getInt(Tags.PixelRepresentation, 0);
        min = ds.getInt(Tags.SmallestImagePixelValue, signed == 0 ? 0 : -(size >> 1));
        max = ds.getInt(Tags.LargestImagePixelValue, signed == 0 ? size - 1 : (size >> 1) - 1);
        if (cmParam.getNumberOfWindows() > 0) {
          float center = cmParam.getWindowCenter(0);
          float width = cmParam.getWindowWidth(0);
          min = (double) cmParam.toPixelValue(center - width/2);
          max = (double) cmParam.toPixelValue(center + width/2);
        }
      } catch (Exception e) {
        return null;
      }
      
      buf = bi.getData().getDataBuffer();
      if (buf instanceof DataBufferByte) {
        ip = new ByteProcessor(bi.getWidth(), bi.getHeight());
        ip.setPixels(((DataBufferByte) buf).getData());
      } else if (buf instanceof DataBufferShort) {
        ip = new ShortProcessor(bi.getWidth(), bi.getHeight());
        ip.setPixels(((DataBufferShort) buf).getData());
      } else if (buf instanceof DataBufferUShort) {
        ip = new ShortProcessor(bi.getWidth(), bi.getHeight());
        ip.setPixels(((DataBufferUShort) buf).getData());
      } else {
        // Andere Datenformate werden nicht unterstuetzt
        return null;
      }
      
      // Window setzen: Das Bild wird so dargestellt, dass min..max mit den LUT
      // Indizes 0..255 dargestellt werden
      ip.setMinAndMax(min, max);
      
      // ColorModel erzeugen:
      // ImageJ verwendet LUT's mit 256 Eintraegen
      lut = new byte[256];
      if ("MONOCHROME2".equals(pmi)) {
        for (int i = 0; i < 256; i++) lut[i] = (byte) i;
      } else {
        for (int i = 0; i < 256; i++) lut[255 - i] = (byte) i;
      }
      cm = new IndexColorModel(8, 256, lut, lut, lut);
      ip.setColorModel(cm);
      
    } else {
      
      // Alle anderen Photometric Interpretations werden als RGB Bilder dargestellt
      
      ImagePlus imgPlus = new ImagePlus("", bi);
      ip = imgPlus.getProcessor();
    }

    return ip;
  }
  
}
