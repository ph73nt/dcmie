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
package de.iftm.dcm4che.dcmie.exp;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.text.*;
import javax.swing.table.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.xml.parsers.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.util.*;
import org.dcm4che.imageio.plugins.*;

import de.iftm.dcm4che.dcmie.*;


/**
 * Liefert eine Info Tabelle zu einem DICOM File:
 * - Beispiel fuer einen DICOMDIR File in PS 3.10 - Annex A
 * - Basic Directory IOD (Information Object Definition) in PS 3.3 - Annex F
 * @author   Thomas Hacklaender
 * @version  2002.6.2
 */
public class MetadataInfoPanel extends javax.swing.JPanel implements java.io.Serializable {

  
  /**
   * The metadata describing not image-related information of a Secondary Capture
   * image.
   */
  private Dataset         infoMetadataDataset = null;

  
  /**
   * Vector containing the tags of the table rows.
   */
  private Vector          tagVector = null;
 
  
  /**
   * The Tag-Dictionary
   */
  private TagDictionary   dict = null;
  
  
  /** Creates new form DcmInfoPanel */
  public MetadataInfoPanel() {
    initComponents();
  }
  
  
  /**
   * Set up the Panel
   * @param dcmieParam the parameters of the dcmie collection of plugins.
   */
  public void setUp(DcmieParam dcmieParam, Dataset infoMetadataDataset) {
    DcmElement    element;
    
    // Dataset loakal speichern
    this.infoMetadataDataset = infoMetadataDataset;
    
    // Wenn Dataset nicht gesetzt nichts tun
    if (infoMetadataDataset == null) return;
        
    // Default Dictionary laden
    dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
    
    // Inhalt der Tabelle loeschen
    while (infoTable.getModel().getRowCount() > 0) {
      ((DefaultTableModel) infoTable.getModel()).removeRow(0);
    }
    
    // Neuen Tag-Vector
    tagVector = new Vector();
    
    // Alle DcmElemente des infoMetadataDataset in die Tabelle eintragen
    for (Iterator i = infoMetadataDataset.iterator(); i.hasNext(); ) {
      try {
        addInfoRow(((DcmElement) i.next()).tag());
      } catch (Exception e) {}
    }
  }
  
  
  /**
   * Adds the singel info row.
   */
  private void addInfoRow(int tag) {
    String[]    stringArray = null;
    String      value = "";
    String      note = "";
    int         vr;

    // Value Representation
    vr = VRs.valueOf(dict.lookup(tag).vr);

    // Binaere und Sequence VR's werden nicht dargestellt.
    if ((vr == VRs.OB) | (vr == VRs.OW) | 
        (vr == VRs.OF) | (vr == VRs.SQ) |
        (vr == VRs.UN) | (vr == VRs.NONE)) {
      return;
    }
    
    // Hinweise fuer den Wertebereich generieren
    switch (tag) {

      case Tags.SOPClassUID:
      case Tags.SOPInstanceUID:
      case Tags.StudyInstanceUID:
      case Tags.SeriesInstanceUID:
        note = "[0 to 9 or .]";
        break;

      case Tags.PatientSex:
        note = "[O, M or F]";
        break;

      case Tags.PatientOrientation:
        note = "[A, P, R, L, H or F: <row>\\<column>]";
        break;

      case Tags.Laterality:
        note = "[L or R]";
        break;

      default:
        note = "";
    }
    
    try {
      
      // Value String konstruieren
      stringArray = infoMetadataDataset.getStrings(tag);
      if (stringArray != null) {
        if (stringArray.length > 0) {
          for (int i = 0; i < stringArray.length; i++) {
            value += "\\\\" + stringArray[i];
          }
          value = value.substring(2);
        }
      }
      
      // Eintag in Tabelle vornehmen
      ((DefaultTableModel) infoTable.getModel()).addRow(new String [] {dict.lookup(tag).name + "   " + note, value});
      
      // Tag als Integer-Object in die Liste eintagen
      tagVector.add(new Integer(tag));
      
    } catch (Exception e) {
    }
  }
  
  
  /**
   * Updates the not image-related metadata in dcmieParam with the contents of
   * this panel.
   * @return the Dataset containing the info-metadata.
   * @throws DcmValueException if a value is not valid for corresponding Tag and VR.
   */
  public Dataset getInfoMetadata() throws DcmValueException {
    int             vr;
    int             tag;
    String          value;
    String[]        stringArray;
    Vector          stringVector;
    ByteBuffer      byteBuffer;
    StringTokenizer st;
    Integer[]       tagArray;
    
    // Wenn Dataset nicht gesetzt nichts tun
    if (infoMetadataDataset == null) {
      return null;
    }
    
    // Vector in Array umwandeln
    tagArray = (Integer[]) tagVector.toArray(new Integer[0]);
    
    // Alle DcmElemente des Tag-Array/der Tabelle bearbeiten:
    
    for (int row = 0; row < tagArray.length; row++) {

      // Value aus Spalte 1 der Tabelle holen
      value = (String) ((DefaultTableModel) infoTable.getModel()).getValueAt(row, 1);
      if (value.equals("")) {
        stringArray = null;
      } else {
        st = new StringTokenizer(value, "\\");
        stringVector = new Vector();
        while (st.hasMoreTokens()) {
          stringVector.add(st.nextToken());
        }
        stringArray = (String[]) stringVector.toArray(new String[1]);
      }

      // Tag aus Array holen
      tag = tagArray[row].intValue();

      // Value Representation
      vr = VRs.valueOf(dict.lookup(tag).vr);
      
      testValue(tag, vr, stringArray);
      
      // Wert in infoMetadataDataset ueberschreiben
      infoMetadataDataset.putXX(tag, vr, stringArray);
    }
    
    // Metadaten zurueckgeben
    return infoMetadataDataset;
  }
  
  
  /**
   * Tests, if the values are valid for the Tag and VR.
   * @param tag the Tag.
   * @param vr the VR.
   * @param valueArray the array of value of the tag.
   * @throws DcmValueException if the value is not valid for the Tag and VR.
   */
  private void testValue(int tag, int vr, String[] valueArray) throws DcmValueException {
    // Bei Bedarf Testroutinen implementieren
    // throw new DcmValueException("Syntax error in: " + dict.lookup(tag).name + " = " + value);
  }

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    scroll = new javax.swing.JScrollPane();
    infoTable = new javax.swing.JTable();

    setLayout(new java.awt.BorderLayout());

    scroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setMinimumSize(new java.awt.Dimension(256, 64));
    scroll.setPreferredSize(new java.awt.Dimension(0, 0));
    infoTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "DICOM Tag", "Value"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, true
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scroll.setViewportView(infoTable);

    add(scroll, java.awt.BorderLayout.CENTER);

  }//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane scroll;
  private javax.swing.JTable infoTable;
  // End of variables declaration//GEN-END:variables
  
}
