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
package de.iftm.dcm4che.dcmie;


import java.io.*;
import java.nio.*;
import java.util.*;
import java.text.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.util.*;
import org.dcm4che.imageio.plugins.*;


/**
 * Utility class for ImageJ ImagePlus-Properties.<br>
 * <br>
 * String-Properties for each element of each Dataset:<br>
 * Key:   dcm4che.string.[dataset_number].[tag]<br>
 * Value: The value of the element as a String. If the value of the element 
 *        is empty the string "" is returned. Note: In the current version of
 *        dcm4che (2002.5.26) elements with VR = UN, OB, OW and SQ are
 *        ignored. The library does not convert their binary values to a string
 *        representation.<br>
 * <br>
 * [dataset_number]: For each Dataset in the array of Datasets a seperate set of 
 *                   properties is given. The slice number starts with 1. For an 
 *                   array with only one entry [dataset_numer] is 1.<br>
 * <br>
 * [tag]:          The tag-number of the element. it is a 8 character long
 *                 hexadecimal number with the first 4 digits corresponding to
 *                 the group-number and the last 4 digits corresponding to the
 *                 element number.<br>
 * <br>
 * Binary-properties:<br>
 * Key:   dcm4che.binary<br>
 * Value: The value of the element as a Datset[]. Each Dataset has one entry in 
 *        the array. For an Dataset[] with only one Dataset the property is a 
 *        Dataset[1].<br>
 * <br>
 * Linitation:<br>
 * In method addElement only elements with a value representation of OB, OW, SQ, 
 * OF, UN and NONE are only includet, if their value == null.<br>
 * <br>
 * Changes:<br>
 * 2002.07.19: datasetToProperties replace control character with escape sequence.<br>
 *
 * @author   Thomas Hacklaender
 * @version  2002.07.19
 */
public class DcmiePropertiesUtil {

    
  /**
   * Instance of the TagDictionary
   */
  private static TagDictionary dict = DictionaryFactory.getInstance().getDefaultTagDictionary();

  
  /**
   * Converts a set of properties to an array of Dataset. If the tag of an element
   * could not be found in the TagDictionary, the element is ignored. Elements
   * with a value representation of OB, OW, SQ, OF, UN and NONE are only includet
   * if their value == null.
   * @param proper the properties to convert.
   * @param datasetDimension the dimension of the returned Dataset[].
   * @param stringOverwrites true, if string-properties should overwrite
   *                         binary-properties.
   * @return an array of Dataset which collects the properties.
   *         Returns null, if proper == null.
   * @throws DcmValueException in the case of a conversion error.
   */
  public static Dataset[] propertiesToDataset(Properties proper, int datasetDimension, boolean stringOverwrites) throws DcmValueException{
    Dataset[]       propertiesDatasetArray = null;
    Dataset[]       stringDatasetArray = null;
    Enumeration     keyEnumeration;
    Vector          keyVector;
    String[]        keyArray;
    String          key;
    int             i;
    String          numberString;
    String          tagString;
    int             tag;
    int             number;
    String          value;
    int             vr;
    int             dimIdx;
    
    // Wenn keine Properties vorhanden sind nichts machen
    if (proper == null) return null;
    
    // Leere Dataset[] anlegen
    propertiesDatasetArray = new Dataset[datasetDimension];
    stringDatasetArray = new Dataset[datasetDimension];
    for (dimIdx = 0; dimIdx < datasetDimension; dimIdx++) {
      propertiesDatasetArray[dimIdx] = DcmObjectFactory.getInstance().newDataset();
      stringDatasetArray[dimIdx] = DcmObjectFactory.getInstance().newDataset();
    }
    
    // Wenn vorhanden Binary-Properties in propertiesDatasetArray einlesen
    if (proper.containsKey("dcm4che.binary")) {
      for (dimIdx = 0; dimIdx < propertiesDatasetArray.length; dimIdx++) {
        if (dimIdx < ((Dataset[]) proper.get("dcm4che.binary")).length) {
          propertiesDatasetArray[dimIdx] = ((Dataset[]) proper.get("dcm4che.binary"))[dimIdx];
        }
      }
    }
    
    // Wenn String-Properties nicht ueberschreiben sollen nichts weiter machen
    if (!stringOverwrites) return propertiesDatasetArray;
    
    // Die Liste der Attribute muss sortiert werden:
    // Einige Attribute, wie (0008,0005)Specific Character Set muessen  v o r
    // anderen Attributen in einer DICOM Datei definiert sein.
    
    // Alle Key's in einen Vector eintragen
    keyEnumeration = proper.propertyNames();
    keyVector = new Vector();
    while (keyEnumeration.hasMoreElements()) {
      keyVector.addElement(keyEnumeration.nextElement());
    }
    
    // Vector in sortiertes Array umwandeln
    keyArray = (String[]) keyVector.toArray(new String[0]);
    Arrays.sort(keyArray);
    
    for (int p = 0; p < keyArray.length; p++) {
      
      // Der key ist immer ein String
      key = keyArray[p];
      
      // Testen, of es sich um ein String-Property handelt
      if (key.indexOf("dcm4che.string") == 0) {
        i = key.indexOf('.', 15);
        numberString = key.substring(15, i);
        tagString = key.substring(i + 1);
        // Index in das Datset[].
        // Achtung: Die Stringdarstellung beginnt mit 1!
        number = Integer.parseInt(numberString) - 1;
        // Numerischer Wert des Tag (hexadezimal)
        tag = Integer.parseInt(tagString, 16);
        // Value Representation
        vr = VRs.valueOf(dict.lookup(tag).vr);
        // Der String-Value des Elementes
        value = (String) proper.get(key);
        // Zum Dataset[] hinzufuegen
        addElement(stringDatasetArray, number, tag, vr, value);
      }
    }
    
    // Datsets[] mergen
    for (dimIdx = 0; dimIdx < propertiesDatasetArray.length; dimIdx++) {
      propertiesDatasetArray[dimIdx].putAll(stringDatasetArray[dimIdx]);
    }
    
    return propertiesDatasetArray;
  }

  
  /**
   * Add one DcmElement to the Dataset[]. Elements with a value representation 
   * of OB, OW, SQ, OF, UN and NONE are only includet if their value == null.
   * 
   * @param stringDatasetArray an array of Dataset which collects the properties.
   * @param number the index into Dataset[] of the Dataset to which the DcmElement
   *               should be added.
   * @param tag the tag of the DcmElement.
   * @param vr the value representation of the DcmElement.
   * @param value the value if the DcmElement.
   * @throws DcmValueException in the case of a conversion error.
   */
  private static void addElement(Dataset[] stringDatasetArray, int number, int tag, int vr, String value) throws DcmValueException{
    ByteBuffer        bytes;
    StringTokenizer   st;
    String[]          stringArray;
    Vector            stringVector;

    // Ignorieren, wenn Index des Keys groesser als moeglich
    if (number >= stringDatasetArray.length) return;
    
    // Nur DcmElemente verwenden, die im TagDictionary verzeichnet sind
    if (dict.lookup(tag) == null) {
      throw new DcmValueException("Tag " + Integer.toHexString(tag) + " not defined in dictionary.");
    }

    // In den Properties entspricht "" <kein Wert definiert>
    if (value.equals("")) value = null;

    // Falls value == null ist, kann jedes Element geschrieben werden
    if (value == null) {
      // Leeres Element zum Dataset hinzufuegen
      stringDatasetArray[number].putXX(tag, vr);
    }

    // Falls value != null ist, werden binaere und Sequence VR's nicht unterstuetzt.
    if ((vr == VRs.OB) | (vr == VRs.OW) | 
        (vr == VRs.OF) | (vr == VRs.SQ) |
        (vr == VRs.UN) | (vr == VRs.NONE)) {
      throw new DcmValueException("Can't process " + VRs.toString(vr));
    }
    
    // Element zum Dataset hinzufuegen
    if (value.indexOf('\\') == -1) {
      // Kein Value-Delimiter vorhanden: VM == 1
      stringDatasetArray[number].putXX(tag, vr, value);
      return;
    }
    
    // VM > 1
    st = new StringTokenizer(value, "\\");
    stringVector = new Vector();
    while (st.hasMoreTokens()) {
      stringVector.add(st.nextToken());
    }
    stringArray = (String[]) stringVector.toArray(new String[1]);
    stringDatasetArray[number].putXX(tag, vr, stringArray);
  }
  
  
  /**
   * Converts an array of Dataset to properties for a ImagePlus. If the value of
   * an DcmElement == null the string-repersentation will be the Properties is "".
   * If a DcmElement could not be found in the TagDictionary it will be excluded 
   * in the string-representation. It is still present in the binary-representation.
   * @param datasetArray an array of Dataset which contains the properties.
   * @param asString true, if Dataset's should be added in the string representation.
   * @param asBinary true, if Dataset's should be added in the binary representation.
   */
  public static Properties datasetToProperties(Dataset[] datasetArray, boolean asString, boolean asBinary) {
    Dataset         ds;
    DcmElement      element;
    String          key;
    Properties      proper;
    String          tagString;
    String          value;
    String[]        valueArray;
    Dataset         dummy;
    
    // Neue Properties anlegen
    proper = new Properties();

    // String-Properties
    if (asString) {
      // Ein Dummy Dataset erzeugen
      dummy = DcmObjectFactory.getInstance().newDataset();
      // Alle Datasets der Reihe nach bearbeiten
      for (int iDataset = 0; iDataset < datasetArray.length; iDataset++) {
        // Alle Elemente eines Datasets der Reihe nach bearbeiten
        for (Iterator iter = datasetArray[iDataset].iterator(); iter.hasNext(); ) {
          try {
            element = (DcmElement) iter.next();
            // Nur DcmElemente aufnehmen, die im Dictionary vorhanden sind
            if (dict.lookup(element.tag()) != null) {
              valueArray = datasetArray[iDataset].getStrings(element.tag());
              // Control character durch escape sequence ersetzen: Z.B. darf die  Text
              // VR=Short darf control character enthalten. Aber auch Retiered Tag 
              // koennen so aufgebaut sein. Deshalb Ersatz bei allen Tags:
              for (int i = 0; i < valueArray.length; i++) {
                valueArray[i] = valueArray[i].replaceAll("\r",   "\\\\r");
                valueArray[i] = valueArray[i].replaceAll("\n",   "\\\\n");
                valueArray[i] = valueArray[i].replaceAll("\f",   "\\\\f");
                valueArray[i] = valueArray[i].replaceAll("\033", "\\\\033"); // ESC
              }
              // Nur DcmElemente, die man spaeter auch schreiben kann, aufnehmen.
              // Achtung: VR aus dem Dictionary und nicht aus dem Dataset verwenden
              dummy.putXX(element.tag(), VRs.valueOf(dict.lookup(element.tag()).vr), valueArray);
              // Key generieren
              key = "dcm4che.string." + Integer.toString(iDataset + 1) + ".";
              tagString = "0000" + Integer.toHexString(element.tag());
              key += tagString.substring(tagString.length() - 8);
              // Value generierern
              switch (element.vm()) {
                case 0:
                  proper.setProperty(key, null);
                  break;
                  
                case 1:
                  proper.setProperty(key, valueArray[0]);
                  break;
                  
                default:
                  value = "";
                  for (int i = 0; i < element.vm(); i++) {
                    value +=  "\\\\" + valueArray[i];
                  }
                  proper.setProperty(key, value.substring(2));
              }
            }
          } catch (Exception ignore) {}
        }
      }
    }
    
    // Binary-Properties
    if (asBinary) {
      proper.put("dcm4che.binary", datasetArray);
    }
    
    // Properties zurueckgeben
    return proper;
  }
  
  
  /**
   * Creates the default info-metadata Dataset with the given PatientID, StudyID, 
   * SeriesNumber and image InstanceNumber.
   * @param the StudyID,.
   * @param the SeriesNumber.
   * @param the InstanceNumber.
   */
  public static Dataset getDefaultInfoMetadata(String patientID, int studyID, int seriesNumber, int instanceNumber) {
    Dataset       returnDS;
    Dataset       idDS;
    ImageWriter   dcmImageWriter;
    
    idDS = DcmObjectFactory.getInstance().newDataset();    
    idDS.putLO(Tags.PatientID, patientID);
    idDS.putSH(Tags.StudyID, String.valueOf(studyID));
    idDS.putIS(Tags.SeriesNumber, String.valueOf(seriesNumber));
    idDS.putIS(Tags.InstanceNumber, String.valueOf(instanceNumber));
    
    // DcmImageWriter holen
    Iterator writers = ImageIO.getImageWritersByFormatName("DICOM");
    while (true) {
      dcmImageWriter = (ImageWriter) writers.next();
      if (dcmImageWriter == null) {
        throw new UnsupportedOperationException("No DcmImageWriter found");
      }
      if (dcmImageWriter.getDefaultStreamMetadata(null) instanceof DcmMetadata) {
        break;
      }
    }
    // Default Metadaten-Dataset holen
    returnDS = ((DcmMetadata) dcmImageWriter.getDefaultStreamMetadata(null)).getDataset();

    // datasets mergen
    returnDS.putAll(idDS);
    
    return returnDS;
  }
  
  
  /**
   * Creates a PatientID. It is the current time in milli-seconds.
   * @return the PatientID.
   */
  public static String createPatientID() {
    return Long.toString(System.currentTimeMillis());
  }

  
  /**
   * This methods creates a String-Property-file with the default metadata for a
   * Secondary Capture image. The file has the name "default_metadata.properties" 
   * and will be written in the user.dir directory.
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    TagDictionary dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
    Dataset ds = getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 1, 1, 1);
    File f = new File(System.getProperty("user.dir"), "default_metadata.properties");
    try {
      PrintStream ps = new PrintStream(new FileOutputStream(f));

      // Alle Elemente eines Datasets der Reihe nach bearbeiten
      for (Iterator iter = ds.iterator(); iter.hasNext(); ) {
        try {
          DcmElement element = (DcmElement) iter.next();
          String name = dict.lookup(element.tag()).name;
          String value = ds.getString(element.tag());
          if (value == null) value = "";
          // DcmElement in Datei schreiben
          ps.println("dcm4che.string.1." + Tags.toHexString(element.tag(), 8) + " = " + value);
        } catch (Exception ignore) {}
      }
      
      ps.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
