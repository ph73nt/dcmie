/*
 * Copyright (C) 2002 Thomas Hacklaender, mailto:hacklaender@iftm.de
 *
 * IFTM Institut fuer Telematik in der Medizin GmbH, www.iftm.de
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * http://www.gnu.org/copyleft/copyleft.html
 */
package de.iftm.dcm4che.dirtable;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import de.iftm.java.util.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.media.*;


/**
 * This class implements a panel which displays the contents of a DICOMDIR file
 * as a set of 4 tables: One contains the list of PATIENT, one the list of STUDY, 
 * one the list of SERIES and one the list of IMAGE keys.<br>
 * Examples of a DICOMDIR file can be found in PS 3.10 - Annex A. The Basic 
 * Directory IOD (Information Object Definition) is defined in PS 3.3 - Annex F.<br>
 *
 * @author   Thomas Hacklaender
 * @version  2002.5.22
 */
public class DirTable extends javax.swing.JPanel implements java.io.Serializable {

  
  // Liste mit Listenern
	private Vector          listenerVector = new Vector();


  // Nur Record bearbeiten, die das In-use Flag gestzt haben Tag (0004,1410).
  private final boolean   onlyInUse = true;
  
  // Der File Descriptor des DICOMDIR
  private File            dicomdirFile = null;
  
  // Der DICOMDIR Reader
  private DirReader       dicomdirReader = null;
  
  // Byte-Buffer for DICOMDIR File
  private byte[]          dicomdirBuffer = null;
  
  private DirRecord[]     patientArray = null;
  private DirRecord[]     studyArray = null;
  private DirRecord[]     seriesArray = null;
  private DirRecord[]     imageArray = null;
  
  
  /**
   * Creates new form DcmDirTable
   */
  public DirTable() {
    initComponents();
    
    patientTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    patientTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        patientSelectionChanged(e);
      };
    });
    
    studyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    studyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        studySelectionChanged(e);
      };
    });
    
    seriesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        seriesSelectionChanged(e);
      };
    });
    
    imageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        imageSelectionChanged(e);
      };
    });
  }
  
  
  /**
   * Set up the Panel
   */
  public void setUp() {
  }

  
  /**
   *
   */
  public void setDICOMDIR(File dcmDirFile) {
    ImageInputStream  iis = null;
    
    // Den DICOMDIR File speichern
    dicomdirFile = dcmDirFile;
    
    // Patient-Tabelle loeschen
    clearTable(patientTable);

    try {
      // Nicht den File sondern ein Bytearray als Stream verwenden.
      dicomdirBuffer = fileToByteBuffer(dcmDirFile);
      // Einen Reader fuer das DICOMDIR oeffnen. Der Reader bleibt geoeffnet!
      iis = ImageIO.createImageInputStream(new ByteArrayInputStream(dicomdirBuffer));
      dicomdirReader = DirBuilderFactory.getInstance().newDirReader(iis);
      // Patienten Tabelle updaten
      updatePatientTable();
    } catch (IOException e) {
			((DefaultTableModel) patientTable.getModel()).addRow(new String [] {"No DICOMDIR file selected", ""});
    } 
  }

  
  /**
   * Reads the contents of a File into a Byte-Array.
   * @param f the File to read.
   * @return the byte array.
   * @throws IOEception if an error occured during File IO.
   */
  private byte[] fileToByteBuffer(File f) throws IOException {
    byte[]            buf = null;
    DataInputStream   dis = null;
    
    try {
      // Byte Array hat die Laenge des Fileinhaltes
      buf = new byte[(int) f.length()];
      // Fileinhalt einlesen
      dis = new DataInputStream(new FileInputStream(f));
      dis.readFully(buf);
    } finally {
      // DataInputStream auf jeden Fall schliessen
      try {
        if (dis != null) dis.close();
      } catch (Exception ignore) {}
    }
    // Den Buffer zurueckgeben
    return buf;
  }

  
  /**
   *
   */
  private void clearTable(JTable table) {
    while (table.getModel().getRowCount() > 0) {
      ((DefaultTableModel) table.getModel()).removeRow(0);
    }
  }
  
  
  /**
   *
   */
  private void updatePatientTable() {
    
    // Patienten DirRecords zunaechst in Vector sammeln
    Vector patientVector = new Vector();
    
    try {
      // Patient-Tabelle neu generieren
      DirRecord next = dicomdirReader.getFirstRecord(onlyInUse);
      while (next != null) {
        if ("PATIENT".equals(next.getType())) {
          try {
            Dataset  ds = next.getDataset();
            String[] patientInfo = new String [] {ds.getString(Tags.PatientName), ds.getString(Tags.PatientID)};
            ((DefaultTableModel) patientTable.getModel()).addRow(patientInfo);
            patientVector.add(next);
          } catch (Exception e) {}
        }
        next = next.getNextSibling(onlyInUse);
      }
    } catch (IOException e) {}
    
    // Vector in Array umwandeln
    patientArray = (DirRecord[]) patientVector.toArray(new DirRecord[0]);
  }
  
  
  /**
   *
   */
  private void updateStudyTable(DirRecord patientRecord) {
    
    // Study DirRecords zunaechst in Vector sammeln
    Vector studyVector = new Vector();
    
    try {
      // Study-Tabelle neu generieren
      DirRecord next = patientRecord.getFirstChild(onlyInUse);
      while (next != null) {
        if ("STUDY".equals(next.getType())) {
          try {
            Dataset  ds = next.getDataset();
            String[] studyInfo = new String [] {ds.getString(Tags.StudyID),
                                                ds.getString(Tags.StudyDescription),
                                                ds.getString(Tags.StudyDate),
                                                ds.getString(Tags.StudyTime)};
            ((DefaultTableModel) studyTable.getModel()).addRow(studyInfo);
            studyVector.add(next);
          } catch (Exception e) {}
        }
        next = next.getNextSibling(onlyInUse);
      }
    } catch (IOException e) {}
    
    // Vector in Array umwandeln
    studyArray = (DirRecord[]) studyVector.toArray(new DirRecord[0]);
  }
  
  
  /**
   *
   */
  private void updateSeriesTable(DirRecord studyRecord) {
    
    // Series DirRecords zunaechst in Vector sammeln
    Vector seriesVector = new Vector();
    
    try {
      // Series-Tabelle neu generieren
      DirRecord next = studyRecord.getFirstChild(onlyInUse);
      while (next != null) {
        if ("SERIES".equals(next.getType())) {
          try {
            Dataset  ds = next.getDataset();
            String[] seriesInfo = new String [] {ds.getString(Tags.SeriesNumber),
                                                 ds.getString(Tags.Modality)};
            ((DefaultTableModel) seriesTable.getModel()).addRow(seriesInfo);
            seriesVector.add(next);
          } catch (Exception e) {}
        }
        next = next.getNextSibling(onlyInUse);
      }
    } catch (IOException e) {}
    
    // Vector in Array umwandeln
    seriesArray = (DirRecord[]) seriesVector.toArray(new DirRecord[0]);
  }
  
  
  /**
   *
   */
  private void updateImageTable(DirRecord seriesRecord) {
    
    // Series DirRecords zunaechst in Vector sammeln
    Vector imageVector = new Vector();
    
    try {
      // Image-Tabelle neu generieren
      DirRecord next = seriesRecord.getFirstChild(onlyInUse);
      while (next != null) {
        if ("IMAGE".equals(next.getType())) {
          try {
            Dataset  ds = next.getDataset();
            String[] imageInfo = new String [] {ds.getString(Tags.InstanceNumber)};
            ((DefaultTableModel) imageTable.getModel()).addRow(imageInfo);
            imageVector.add(next);
          } catch (Exception e) {}
        }
        next = next.getNextSibling(onlyInUse);
      }
    } catch (IOException e) {}
    
    // Vector in Array umwandeln
    imageArray = (DirRecord[]) imageVector.toArray(new DirRecord[0]);
  }

  
  /**
   *
   */
  private void patientSelectionChanged(ListSelectionEvent e) {
    // Deselektieren ignorieren
    if (!e.getValueIsAdjusting()) {
      // Study, Series und Image-Tabellen loeschen
      clearTable(studyTable);
      clearTable(seriesTable);
      clearTable(imageTable);
      int patientIndex = patientTable.getSelectionModel().getMinSelectionIndex();
      // Nur dann die untergeordnete Tabelle updaten, wenn die Selektion nich leer
      if (patientIndex != -1) {
        updateStudyTable(patientArray[patientIndex]);
      }
    }
  }

  
  /**
   *
   */
  private void studySelectionChanged(ListSelectionEvent e) {
    // Deselektieren ignorieren
    if (!e.getValueIsAdjusting()) {
      // Series und Image-Tabellen loeschen
      clearTable(seriesTable);
      clearTable(imageTable);
      int studyIndex = studyTable.getSelectionModel().getMinSelectionIndex();
      // Nur dann die untergeordnete Tabelle updaten, wenn die Selektion nich leer
      if (studyIndex != -1) {
        updateSeriesTable(studyArray[studyIndex]);
      }
    }
  }

  
  /**
   *
   */
  private void seriesSelectionChanged(ListSelectionEvent e) {
    
    // Deselektieren ignorieren
    if (!e.getValueIsAdjusting()) {
      
      // Series und Image-Tabellen loeschen
      clearTable(imageTable);
      
      int seriesIndex = seriesTable.getSelectionModel().getMinSelectionIndex();
      // Nur dann die untergeordnete Tabelle updaten, wenn die Selektion nich leer
      if (seriesIndex != -1) {
        
        fireSeriesSelectionEvent();
        // Nur wenn eine Serie ausgewaehlt wurde koennen die Bildnummern angezeigt werden
        if (seriesTable.getSelectedRowCount() == 1)  {
          updateImageTable(seriesArray[seriesIndex]);
        }
          
      }
      
    }
  }

  
  /**
   *
   */
  private void fireSeriesSelectionEvent () {
    Vector              fv = new Vector();
    File[]              fa;
    FileSelectionEvent  fse;
    DirRecord           seriesRecord;
    DirRecord           next;
    
    // Alle selektierten Serien bearbeiten
    for (int s = 0; s < seriesTable.getSelectedRowCount(); s++) {
      seriesRecord = seriesArray[seriesTable.getSelectedRows()[s]];
      
      // Alle Bilder einer Serie dem Vector hinzufuegen
      try {
        next = seriesRecord.getFirstChild(onlyInUse);
        while (next != null) {
          if ("IMAGE".equals(next.getType())) {
            fv.add(buildFile(dicomdirFile, next.getRefFileIDs()));
          }
          next = next.getNextSibling(onlyInUse);
        }
      } catch (IOException e) {}
    }
    
    // Vector in Array umwandeln
    if (fv.isEmpty()) {
      fa = null;
    } else {
      fa = (File[]) fv.toArray(new File[0]);
    }
      
    // FileSelectionEvent generieren und feuern
    fse = new FileSelectionEvent(this);
    fse.setLastSelectedFile(null);
    fse.setSelectedFiles(fa);
    fireFileSelectionEvent(fse);
  }
  
  
  /**
   *
   */
  private void imageSelectionChanged(ListSelectionEvent e) {
    File                f;
    File[]              fa;
    FileSelectionEvent  fse;
    int                 index;
    
    // Deselektieren ignorieren
    if (!e.getValueIsAdjusting()) {
      
      // Das File-Objekt zu dem letzten ausgewaehlten Image generieren
      index = imageTable.getSelectionModel().getLeadSelectionIndex();
      
      // Falls die Selektion leer ist, nichts tun
      if (index == -1) return;
      
      // File Objekt aus den Angaben des DICOMDIR erzeugen
      f = buildFile(dicomdirFile, imageArray[index].getRefFileIDs());
      
      // Falls kein File-Objekt erzeugt werden konnte nichts tun
      if (f == null) return;
      
      // Ein Array mit den aktuell selektierten Files erzeugen
      if (imageTable.getSelectedRowCount() > 0) {
        // Ein Array mit allen ausgewaehlten Images erzeugen
        fa = new File[imageTable.getSelectedRowCount()];
        for (int i = 0; i < imageTable.getSelectedRowCount(); i++) {
          index = imageTable.getSelectedRows()[i];
          f = buildFile(dicomdirFile, imageArray[index].getRefFileIDs());
          fa[i] = f;
        }
      } else {
        fa = null;
      }
      
      // FileSelectionEvent generieren und feuern
      fse = new FileSelectionEvent(this);
      fse.setLastSelectedFile(f);
      fse.setSelectedFiles(fa);
      fireFileSelectionEvent(fse);
    }
  }
  
  
  /**
   * Builds a File from the root file and the path to the file.
   */
  private File buildFile(File parent, String[] path) {
    if (path == null) return null;
    
    String s = parent.getParent();
    for (int i = 0; i < path.length; i++) {
      s = s + File.separator + path[i];
    }
    return new File(s);
  }

  
  /**
   *
   */
  public JTable getPatientTable() {
    return patientTable;
  }

  
  /**
   *
   */
  public JTable getStudyTable() {
    return studyTable;
  }

  
  /**
   *
   */
  public JTable getSeriesTable() {
    return seriesTable;
  }

  
  /**
   *
   */
  public JTable getImageTable() {
    return imageTable;
  }

  
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    patientScroll = new javax.swing.JScrollPane();
    patientTable = new javax.swing.JTable();
    studyScroll = new javax.swing.JScrollPane();
    studyTable = new javax.swing.JTable();
    seriesScroll = new javax.swing.JScrollPane();
    seriesTable = new javax.swing.JTable();
    imageScroll = new javax.swing.JScrollPane();
    imageTable = new javax.swing.JTable();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(380, 320));
    setPreferredSize(new java.awt.Dimension(380, 320));
    patientScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    patientTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Patient's Name", "Patients ID"
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
    patientScroll.setViewportView(patientTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.25;
    add(patientScroll, gridBagConstraints);

    studyScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    studyTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Study ID", "Study Description", "Study Date", "Study Time"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    studyScroll.setViewportView(studyTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
    add(studyScroll, gridBagConstraints);

    seriesScroll.setToolTipText("null");
    seriesScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    seriesTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Series Number", "Modality"
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
    seriesScroll.setViewportView(seriesTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
    add(seriesScroll, gridBagConstraints);

    imageScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    imageTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Image Instance Number"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    imageScroll.setViewportView(imageTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
    add(imageScroll, gridBagConstraints);

  }//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane studyScroll;
  private javax.swing.JTable imageTable;
  private javax.swing.JScrollPane patientScroll;
  private javax.swing.JScrollPane seriesScroll;
  private javax.swing.JTable seriesTable;
  private javax.swing.JTable patientTable;
  private javax.swing.JScrollPane imageScroll;
  private javax.swing.JTable studyTable;
  // End of variables declaration//GEN-END:variables
  
  
  /**
   * FileSelectionEvent an alle Listener verschicken
   * @param fse the FileSelectionEvent.
   */
  private void fireFileSelectionEvent(FileSelectionEvent fse) {
    // Event an alle Listener verschicken
		Enumeration listeners = listenerVector.elements();
		while (listeners.hasMoreElements()) {
			FileSelectionListener listener = (FileSelectionListener) listeners.nextElement();
			listener.fileSelected(fse);
		} 
  }

  
	/**
	 * Add a FileSelectionListener.
	 * @param listener the new listener.
	 */
	public void addFileSelectionListener(FileSelectionListener listener) {
		if (listener != null && !listenerVector.contains(listener)) {
			listenerVector.addElement(listener);
		} 
	} 


	/**
	 * Remove a FileSelectionListener.
	 * @param listener the listener to remove.
	 */
	public void removeFileSelectionListener(FileSelectionListener listener) {
		if (listener != null) {
			listenerVector.removeElement(listener);
		} 
	} 
  
  
}
