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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import ij.*;
import ij.process.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.media.*;
import org.dcm4che.util.*;
import org.dcm4che.image.*;
import org.dcm4che.imageio.plugins.*;

import de.iftm.dcm4che.*;
import de.iftm.dcm4che.dcmie.*;
import de.iftm.dcm4che.dcmie.exp.*;
import de.iftm.javax.swing.*;


/**
 * This class writes an ImagePlus as a DICOM image.
 *
 * @author   Thomas Hacklaender
 * @version  2002.08.21
 */
public class FileExporter {

  
  /**
   * The ImagePlus to process. Set by the constructor.
   */
  private ImagePlus       imagePlus;

  
  /**
   * Set of Parameters.
   */
  private DcmieParam      dcmieParam;

  
  /**
   * The metadata describing not image-related information of a Secondary Capture
   * image.
   */
  private Dataset         infoMetadataDataset = null;

  
  /**
   * The metadata of the ImagePlus (only used by Dcm_Exporter).
   */
  private Dataset[]       imageMetadataArray = null;

  
  /**
   * Stops the thread if true.
   */
  private boolean         stopThread = false;

  
  /** Creates a new instance of FileImporter */
  public FileExporter(ImagePlus ip, DcmieParam dcmieParam, Dataset infoMetadataDataset, Dataset[] imageMetadataArray) {
    imagePlus = ip;
    this.dcmieParam = dcmieParam;
    this.infoMetadataDataset = infoMetadataDataset;
    this.imageMetadataArray = imageMetadataArray;
  }
  
  
  /**
   * The only method one can call.
   */
  public void run() {
    DcmDataImage      ddi;
    Dataset           dummy;
    int               imageNumber;
    Dataset[]         metadataArray = null;
    Dataset           metadata;
    int               numSlices;
    ProgressWindow    pw = null;
    int               slice;
    
    // Falls kein Input gesetzt nichts tun
    if (dcmieParam == null) return;
    if (imagePlus == null) return;
    
    // Anzahle der Slices
    numSlices = imagePlus.getStackSize();

    // Imagenummer (Instance number) des ersten Bildes extrahieren
    try {
      imageNumber = infoMetadataDataset.getInt(Tags.InstanceNumber, 1);
    } catch (Exception e) {
      imageNumber = 1;
    }

    // Metadaten setzen
    metadataArray = new Dataset[numSlices];
    for (slice = 1; slice <= numSlices; slice++) {
      
      // Leeres Dataset erzeugen
      metadata = DcmObjectFactory.getInstance().newDataset();
      
      // Falls Image-Metadaten verwendet werden sollen
      if (dcmieParam.isUseImageMetadata) {
        // Image-Metadaten als Basis verwenden
        if (imageMetadataArray != null) {
          if (slice <= imageMetadataArray.length) {
            metadata.putAll(imageMetadataArray[slice -1]);
          }
        }
      }
      
      // Mit Info-Metadaten ueberschreiben
      metadata.putAll(infoMetadataDataset);
      
      // Imagenummer updaten
      metadata.putIS(Tags.InstanceNumber, imageNumber - 1 + slice);
    
      // Metadaten gegebenenfalls auf die Mask-Metadaten eingrenzen
      if (dcmieParam.maskMetadataDataset == null) {
        metadataArray[slice - 1] = metadata; 
      } else {
        dummy = metadata.subSet(dcmieParam.maskMetadataDataset);
        // Alle Elemente des eingegrenzten Dataset in ein neues Dataset kopieren
        metadataArray[slice - 1] = DcmObjectFactory.getInstance().newDataset();
        metadataArray[slice - 1].putAll(dummy);

      }
      
    } // for (): Metadaten setzen

    // Progress Monitor anzeigen
    pw = new ProgressWindow("Exporting " + Integer.toString(numSlices) + " images", "", 1 , numSlices);
    pw.setMillisToDecideToPopup(10);
    pw.setMillisToPopup(200);

    // Alle Slices durchlaufen
    for (slice = 1; slice <= numSlices; slice++) {

      // Wenn Flag gesetzt, Thread abbrechen
      if (stopThread) break;
      
      // Progressbar updaten
      if (pw.isCanceled()) break;
      pw.setProgress(slice);

      // Slice auswaehlen
      imagePlus.setSlice(slice);

      // Bei Bildserien erhaelt jedes Bild eine neue SOPInstanceUID
      metadata = metadataArray[slice - 1];
      if (slice > 1) {
        metadata.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance().createUID());
      }

      // Slice schreiben
      ddi = new DcmDataImage(metadata, imagePlustoBufferedImage(imagePlus));
      try {
        if (dcmieParam.isExportFilesystem) {
          DcmExportPanel.writeToFilesystem(ddi, dcmieParam.exportFile, numSlices > 1);
        } else {
          DcmExportPanel.writeToDICOMDIR(ddi, dcmieParam.exportFile);
        }
      } catch (Exception e) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, e.getMessage() + ": " + "Can't create output file.", "Error in FileExporter", JOptionPane.INFORMATION_MESSAGE);
        stopThread = true;
      }
    }

    
  }

  
  /**
   * Convert the actual selected slice of an ImagePlus to a BufferedImage.
   * @param ip the ImagePlus.
   */
  private BufferedImage imagePlustoBufferedImage(ImagePlus ip) {
    BufferedImage       bi = null;
    DataBufferByte      byteBuffer;
    byte[]              bytePixel;
    Raster              raster;
    DataBufferUShort    shortBuffer;
    short[]             shortPixel;
    
    switch (ip.getType()) {

      case ImagePlus.GRAY8:
        // Ein neues BufferedImage mit definierten Eigenschaften erzeugen:
        // - ComponentColorModel
        // - PixelInterleavedSampleModel (superclass: ComponentSampleModel)
        //   -- numBanks = 1
        //   -- numBands = 1
        //   -- bandOffset[0] = 0
        //   -- pixelStride = 1
        //   -- scanlineStride = <image width>
        // - ByteInterleavedRaster
        // - DataBufferByte
        bi = new BufferedImage(ip.getWidth(), ip.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        // Pixel-Daten aus dem ImageProcessor holen
        bytePixel = (byte[]) ip.getProcessor().getPixels();
        // Aus Pixel-Daten einen DataBuffer erzeugen
        byteBuffer = new DataBufferByte(bytePixel, bytePixel.length);
        // Ein zum BufferedImage kompatibles WritabelRaster erzeugen
        raster = Raster.createWritableRaster(bi.getSampleModel(), byteBuffer, new Point(0, 0));
        // Das neue Raster als Daten eintragen
        bi.setData(raster);
        break;

      case ImagePlus.GRAY16:
        // Ein neues BufferedImage mit definierten Eigenschaften erzeugen:
        // - ComponentColorModel
        // - PixelInterleavedSampleModel (superclass: ComponentSampleModel)
        //   -- numBanks = 1
        //   -- numBands = 1
        //   -- bandOffset[0] = 0
        //   -- pixelStride = 1
        //   -- scanlineStride = <image width>
        // - ShortInterleavedRaster
        // - DataBufferUShort
        bi = new BufferedImage(ip.getWidth(), ip.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
        // Pixel-Daten aus dem ImageProcessor holen
        shortPixel = (short[]) ip.getProcessor().getPixels();
        // Aus Pixel-Daten einen DataBuffer erzeugen
        shortBuffer = new DataBufferUShort(shortPixel, shortPixel.length);
        // Ein zum BufferedImage kompatibles WritabelRaster erzeugen
        raster = Raster.createWritableRaster(bi.getSampleModel(), shortBuffer, new Point(0, 0));
        // Das neue Raster als Daten eintragen
        bi.setData(raster);
        break;

      case ImagePlus.COLOR_256:
        bi = imagetoBufferedImage(ip.getImage(), BufferedImage.TYPE_BYTE_INDEXED);
        break;

      case ImagePlus.COLOR_RGB:
        bi = imagetoBufferedImage(ip.getImage(), BufferedImage.TYPE_INT_RGB);
        break;

      default:
        // ImagePlus.Gray_32 wird nicht unterstuetzt
        break;
    }
    
    return bi;
  }
  
  
  /**
   * Convert an AWT Image to a BufferedImage (from The Java Developers Almanac 1.4).
   * @param image the AWT image.
   * @param imageType the type of the resulting BufferedImage.
   * @return the BufferedImage.
   */
  private BufferedImage imagetoBufferedImage(Image image, int imageType) {
    // Create the buffered image.
    BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), imageType);

    // Copy image to buffered image.
    Graphics g = bufferedImage.createGraphics();

    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bufferedImage;
  }

   
}
