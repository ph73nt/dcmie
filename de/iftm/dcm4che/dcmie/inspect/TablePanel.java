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
package de.iftm.dcm4che.dcmie.inspect;

import java.awt.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import javax.swing.table.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;

import de.iftm.dcm4che.image.*;
import de.iftm.dcm4che.dcmie.*;
import de.iftm.javax.swing.filetree.*;
import de.iftm.java.util.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.5.27
 */
public class TablePanel extends javax.swing.JPanel implements FileSelectionListener, java.io.Serializable {

  private String          VERSION = "0.2";
  
  
  // Das File Formmat
  private FileFormat      ff;

  
  // Das Dataset, das als Info dargestellt werden soll
  private Dataset         fileDataset = null;
  
  // Das Dictionary der Tags
  private TagDictionary   dict = null;

  
  // Der Indent String fuer SQ Elemente
  private String          sqIndent;
  
  
  /**
   * Creates new form DirPanel
   */
  public TablePanel() {
    initComponents();
  }
  
  
  /**
   * Set up the Panel
   */
  public void setUp(DcmieParam dcmieParam) {
    dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
    fileDataset = DcmObjectFactory.getInstance().newDataset();
  }
  

  /**
   * Implementation of the FileSelectionListener interface.
   * @param e the FileSelection event.
   */
  public void fileSelected(FileSelectionEvent e) {
    InputStream   in = null;
    
    // Altes Dataset ignorierern
    fileDataset = null;
    
    // Aktuellen Inhalt loeschen
    while (fileTable.getModel().getRowCount() > 0) {
      ((DefaultTableModel) fileTable.getModel()).removeRow(0);
    }
    while (dsTable.getModel().getRowCount() > 0) {
      ((DefaultTableModel) dsTable.getModel()).removeRow(0);
    }
    
    // Wenn File nicht gesetzt nichts tun
    if (e.getLastSelectedFile() == null) return;
    
    try {
      in = new BufferedInputStream(new FileInputStream(e.getLastSelectedFile()));
      DcmParser p = DcmParserFactory.getInstance().newDcmParser(in);
      ff = p.detectFileFormat();
      fileDataset = DcmObjectFactory.getInstance().newDataset();
      fileDataset.readFile(in, ff, -1);
      addFileInfo();
      addFileDatasetInfo();
    } catch (Exception e2) {
      ((DefaultTableModel) fileTable.getModel()).addRow(new String [] {"No DICOM file selected", ""});
    } finally {
      try {
        in.close();
      } catch (Exception ignore) {}
    }
    
  }
  
  
  /**
   * Adds file meta information.
   */
  private void addFileInfo() {
    String    s;
    String    preamble = "";
    
    FileMetaInfo fmi = fileDataset.getFileMetaInfo();
    
    // Das File Format ausgeben
    ((DefaultTableModel) fileTable.getModel()).addRow(new String [] {"FileFormat", ff.toString()});

    // Falls kein File Meta Information Block vorhanden
    if (fmi == null)  return;
    
    for (int i = 0; i < 128; i++) {
      s = Integer.toHexString(fmi.getPreamble()[i]);
      if (s.length() == 1) {
        s = "0" + s;
      }
      preamble += "\\" + s;
    }
    
    ((DefaultTableModel) fileTable.getModel()).addRow(new String [] {"Preamble", preamble});
    ((DefaultTableModel) fileTable.getModel()).addRow(new String [] {"MediaStorageSOPClassUID", fmi.getMediaStorageSOPClassUID()});
    ((DefaultTableModel) fileTable.getModel()).addRow(new String [] {"MediaStorageSOPInstanceUID", fmi.getMediaStorageSOPInstanceUID()});
    ((DefaultTableModel) fileTable.getModel()).addRow(new String [] {"TransferSyntaxUID", fmi.getTransferSyntaxUID()});
  }
  
  
  /**
   * Das Dataset, das im File gefunden wurde, hinzufuegen.
   */
  private void addFileDatasetInfo() {
    
    // Kein indent-Text vor den Namen
    sqIndent = "";
    
    // Dataset hinzufuegen
    addDSInfo(fileDataset);
  }
  
  
  /**
   * Ein dataset hinzufuegen. Achtung: Rekursion moeglich!
   */
  private void addDSInfo(Dataset ds) {
    
    for (Iterator i = ds.iterator(); i.hasNext(); ) {
      try {
        addDSRow((DcmElement) i.next(), ds.getCharset());
      } catch (Exception e) {}
    }
    
    // Eine Ebene des Indent zurueckgehen
    if (sqIndent.length() > 0) sqIndent = sqIndent.substring(2);
  }
  
  
  /**
   * Adds the singel info row.
   */
  private void addDSRow(DcmElement element, Charset cs) {
    String[]    col = new String[6];
    String      tagString;
    String[]    dataStringArray;
    String      dataString;
    Dataset     newDataset;
    
    try {
      // Defaultwerte
      col[0] = "[Private Tag]";
      col[1] = "";
      col[2] = "??";
      col[3] = "?";
      col[5] = "";
      col[5] = "";
      
      // Column 1 und 4 koennen auch fuer Private Tags erhoben werden
      tagString = "0000" + Integer.toHexString(element.tag());
      tagString = tagString.substring(tagString.length() - 8);
      col[1] = "(" + tagString.substring(0, 4) + ", " + tagString.substring(4) + ")";
      col[4] = Integer.toString(element.length());
      
      // Fuer Private Tags treten hier Exceptions auf
      col[0] = dict.lookup(element.tag()).name;
      if (sqIndent.length() > 0) col[0] = sqIndent + col[0];
      
      col[2] = VRs.toString(element.vr());
      col[3] = Integer.toString(element.vm());
      
    } catch (Exception ignore) {}

    // Wenn keine Daten vorhanden sind
    if (element.length() == 0) {
     ((DefaultTableModel) dsTable.getModel()).addRow(col);
     return;
    }
    
    // Wenn das Element nicht vom Typ "SQ" ist
    if (element.vr() != VRs.SQ) {
      try {
        dataStringArray = element.getStrings(cs);
        dataString = "";
        for (int i = 0, n = element.vm(); i < n; i++) {
          dataString += "\\" + dataStringArray[i];
        }
        col[5] = dataString.substring(1);
      } catch (Exception ignore) {}
      // Reihe anhaengen
      ((DefaultTableModel) dsTable.getModel()).addRow(col);
      return;
    }
    
    // Rekursiv anhaengen
    try {
      for (int i = 0, n = element.vm(); i < n; i++) {
        newDataset = element.getItem(i);
        // Das SQ-Element selbst anhaengen
        col[5] = "> Item " + Integer.toString(i + 1) + " >";
        ((DefaultTableModel) dsTable.getModel()).addRow(col);
        
        // Indent eine Ebene erhoehen
        sqIndent += "> ";
        
        // Inhalt des Dataset anhaengen
        addDSInfo(newDataset);
      }
    } catch (Exception ignore) {}
    
  }

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    scrollFile = new javax.swing.JScrollPane();
    fileTable = new javax.swing.JTable();
    scrollDS = new javax.swing.JScrollPane();
    dsTable = new javax.swing.JTable();
    fileLabel = new javax.swing.JLabel();
    dsLabel = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(256, 64));
    setPreferredSize(new java.awt.Dimension(0, 0));
    fileTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Name", "Data"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scrollFile.setViewportView(fileTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.35;
    add(scrollFile, gridBagConstraints);

    dsTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Name", "Tag", "VR", "VM", "Length", "Data"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false, true, true
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scrollDS.setViewportView(dsTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(scrollDS, gridBagConstraints);

    fileLabel.setText("File Meta Info");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(fileLabel, gridBagConstraints);

    dsLabel.setText("Dataset");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(dsLabel, gridBagConstraints);

  }//GEN-END:initComponents
  

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTable dsTable;
  private javax.swing.JTable fileTable;
  private javax.swing.JScrollPane scrollFile;
  private javax.swing.JLabel dsLabel;
  private javax.swing.JScrollPane scrollDS;
  private javax.swing.JLabel fileLabel;
  // End of variables declaration//GEN-END:variables
  
}
