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

import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

import org.dcm4che.Implementation;
import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.media.*;
import org.dcm4che.util.*;
import org.dcm4che.imageio.plugins.*;

import de.iftm.javax.swing.*;
import de.iftm.java.util.*;
import de.iftm.dcm4che.*;
import de.iftm.dcm4che.image.*;
import de.iftm.dcm4che.dcmie.*;

import de.iftm.dcm4che.dcmie.imp.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.8.21
 */
public class DcmExportPanel extends javax.swing.JPanel implements java.io.Serializable {

  
	/**
	 * Version number of this plugin
	 */
	public final static String    VERSION = "1.0.0";

  
  /**
   * The DcmImageWriter
   */
  private ImageWriter           dcmImageWriter = null;
  
  /**
   * Creates new form DirPanel
   */
  public DcmExportPanel() {
    initComponents();
  }
  
  
  /**
   * Set up the Panel
   * @param dcmieParam the parameters of the dcmie collection of plugins.
   * @param ip the ImagePlus to write.
   */
  public void setUp(DcmieParam dcmieParam, Dataset infoMetadataDataset) {
    File defaultFilesystem = new File(System.getProperty("user.dir"), "SecondaryCapture.dcm");
    File defaultDICOMDIR = new File(System.getProperty("user.dir"), "DICOMDIR");

    // Componenten initialisieren:
    filesystemParentText.setText(defaultFilesystem.getParentFile().toString());
    filesystemNameText.setText(defaultFilesystem.getName());
    dicomdirNameText.setText(defaultDICOMDIR.getPath());
    thisCopyright.setText("(c) 2002 by Thomas Hacklaender under the GNU General Public License. Version: " + VERSION);
    dcmCopyright.setText("Based on the DICOM library http://sourceforge.net/projects/dcm4che/ by Gunter Zeilinger. Version: " + Implementation.getVersionName());
    
    // GUI Elemente enabeln/disabeln
    if (dcmieParam.isExportFilesystem) {
      // Filesystem
      if (dcmieParam.exportFile != null) {
        filesystemParentText.setText(dcmieParam.exportFile.getParentFile().toString());
        filesystemNameText.setText(dcmieParam.exportFile.getName());
      }
      filesystemBtn.setSelected(true);
      filesystemBtnActionPerformed(new ActionEvent(this, 0, ""));
    } else {
      // DICOMDIR
      if (dcmieParam.exportFile != null) {
        dicomdirNameText.setText(dcmieParam.exportFile.getPath());
      }
      dicomdirBtn.setSelected(true);
      dicomdirBtnActionPerformed(new ActionEvent(this, 0, ""));
    }
        
    // MetadataInfoPanel initialisieren
    metadataInfoPanel.setUp(dcmieParam, infoMetadataDataset);
    
    // Schreiben moeglich
    writeBtn.setEnabled(true);
  }
  
  
  /**
   * Update the DcmieParam data with the contents of the MetadataInfoPanel
   * @param dcmieParam the parameters of the dcmie collection of plugins.
   */
  public void updateDcmieParam(DcmieParam dcmieParam) throws DcmValueException {

    // Falls in Filesystem gespeichert werden soll
    if (filesystemBtn.isSelected()) {
      // Filesystem Informationen in Parameterblock eintragen
      dcmieParam.isExportFilesystem = true;
      dcmieParam.exportFile = new File(new File(filesystemParentText.getText()), filesystemNameText.getText());
    } else {
      // DICOMDIR Informationen in Parameterblock eintragen
      dcmieParam.isExportFilesystem = false;
      dcmieParam.exportFile = new File(dicomdirNameText.getText());
    }
  }
  
  
  /**
   * Get the not image-related metadata from the MetadataInfoPanel.
   * @return the Dataset containing the info-metadata.
   * @throws DcmValueException if a value is not valid for corresponding Tag and VR.
   */
  public Dataset getInfoMetadata() throws DcmValueException {
    return metadataInfoPanel.getInfoMetadata();
  }

  
  /**
   *
   */
  public JButton getWriteButton() {
    return writeBtn;
  }

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    saveGroup = new javax.swing.ButtonGroup();
    thisCopyright = new javax.swing.JLabel();
    dcmCopyright = new javax.swing.JLabel();
    metadataInfoPanel = new de.iftm.dcm4che.dcmie.exp.MetadataInfoPanel();
    writeBtn = new javax.swing.JButton();
    filesystemBtn = new javax.swing.JRadioButton();
    dicomdirBtn = new javax.swing.JRadioButton();
    writeLabel = new javax.swing.JLabel();
    filesystemOpenBtn = new javax.swing.JButton();
    filesystemParentText = new javax.swing.JTextField();
    filesystemNameText = new javax.swing.JTextField();
    fileNameLabel = new javax.swing.JLabel();
    dicomdirNameText = new javax.swing.JTextField();
    existingDirOpenBtn = new javax.swing.JButton();
    newDirOpenBtn = new javax.swing.JButton();
    dicomdirNameLabel = new javax.swing.JLabel();
    newDirLabel = new javax.swing.JLabel();
    existingDirLabel = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(790, 486));
    setPreferredSize(new java.awt.Dimension(790, 486));
    thisCopyright.setFont(new java.awt.Font("Dialog", 0, 10));
    thisCopyright.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    thisCopyright.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
    add(thisCopyright, gridBagConstraints);

    dcmCopyright.setFont(new java.awt.Font("Dialog", 0, 10));
    dcmCopyright.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    dcmCopyright.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    add(dcmCopyright, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridheight = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 30);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(metadataInfoPanel, gridBagConstraints);

    writeBtn.setText("Write");
    writeBtn.setEnabled(false);
    writeBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        writeBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
    add(writeBtn, gridBagConstraints);

    filesystemBtn.setText("Filesystem");
    saveGroup.add(filesystemBtn);
    filesystemBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        filesystemBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(25, 15, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(filesystemBtn, gridBagConstraints);

    dicomdirBtn.setText("DICOMDIR");
    saveGroup.add(dicomdirBtn);
    dicomdirBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dicomdirBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(25, 15, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(dicomdirBtn, gridBagConstraints);

    writeLabel.setText("Write to ...");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.insets = new java.awt.Insets(10, 15, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(writeLabel, gridBagConstraints);

    filesystemOpenBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/iftm/dcm4che/dcmie/exp/resources/open.gif")));
    filesystemOpenBtn.setMinimumSize(new java.awt.Dimension(36, 23));
    filesystemOpenBtn.setPreferredSize(new java.awt.Dimension(36, 23));
    filesystemOpenBtn.setEnabled(false);
    filesystemOpenBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        filesystemOpenBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(15, 35, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    add(filesystemOpenBtn, gridBagConstraints);

    filesystemParentText.setEditable(false);
    filesystemParentText.setMinimumSize(new java.awt.Dimension(128, 23));
    filesystemParentText.setPreferredSize(new java.awt.Dimension(256, 23));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(filesystemParentText, gridBagConstraints);

    filesystemNameText.setMinimumSize(new java.awt.Dimension(128, 23));
    filesystemNameText.setPreferredSize(new java.awt.Dimension(256, 23));
    filesystemNameText.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(filesystemNameText, gridBagConstraints);

    fileNameLabel.setText("Filename:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(15, 35, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    add(fileNameLabel, gridBagConstraints);

    dicomdirNameText.setEditable(false);
    dicomdirNameText.setMinimumSize(new java.awt.Dimension(128, 23));
    dicomdirNameText.setPreferredSize(new java.awt.Dimension(256, 23));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(dicomdirNameText, gridBagConstraints);

    existingDirOpenBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/iftm/dcm4che/dcmie/exp/resources/open.gif")));
    existingDirOpenBtn.setMinimumSize(new java.awt.Dimension(36, 23));
    existingDirOpenBtn.setPreferredSize(new java.awt.Dimension(36, 23));
    existingDirOpenBtn.setEnabled(false);
    existingDirOpenBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        existingDirOpenBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(existingDirOpenBtn, gridBagConstraints);

    newDirOpenBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/iftm/dcm4che/dcmie/exp/resources/open.gif")));
    newDirOpenBtn.setMinimumSize(new java.awt.Dimension(36, 23));
    newDirOpenBtn.setPreferredSize(new java.awt.Dimension(36, 23));
    newDirOpenBtn.setEnabled(false);
    newDirOpenBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newDirOpenBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(newDirOpenBtn, gridBagConstraints);

    dicomdirNameLabel.setText("Filename:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.insets = new java.awt.Insets(15, 35, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    add(dicomdirNameLabel, gridBagConstraints);

    newDirLabel.setText("Create new");
    newDirLabel.setToolTipText("null");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.insets = new java.awt.Insets(15, 15, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(newDirLabel, gridBagConstraints);

    existingDirLabel.setText("Open existing");
    existingDirLabel.setToolTipText("null");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.insets = new java.awt.Insets(15, 35, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    add(existingDirLabel, gridBagConstraints);

  }//GEN-END:initComponents

  private void dicomdirBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dicomdirBtnActionPerformed
    // Add your handling code here:
    
    // GUI Elemente enabeln/disabeln
    filesystemOpenBtn.setEnabled(false);
    filesystemNameText.setEnabled(false);
    existingDirOpenBtn.setEnabled(true);
    newDirOpenBtn.setEnabled(true);
    dicomdirNameText.setEnabled(true);

  }//GEN-LAST:event_dicomdirBtnActionPerformed

  private void filesystemBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filesystemBtnActionPerformed
    // Add your handling code here:
    
    // GUI Elemente enabeln/disabeln
    filesystemOpenBtn.setEnabled(true);
    filesystemNameText.setEnabled(true);
    existingDirOpenBtn.setEnabled(false);
    newDirOpenBtn.setEnabled(false);
    dicomdirNameText.setEnabled(false);

  }//GEN-LAST:event_filesystemBtnActionPerformed

  
  private void newDirOpenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDirOpenBtnActionPerformed
    // Add your handling code here:
    File f = FileChooser.saveFile("Create new DICOMDIR", null, "DICOMDIR");
    //  Wenn der Anwender abbricht nichts tun
    if (f == null) return;
    // Als Text darstellen
    dicomdirNameText.setText(f.getPath());
  }//GEN-LAST:event_newDirOpenBtnActionPerformed

  
  private void existingDirOpenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingDirOpenBtnActionPerformed
    // Add your handling code here:
    File f = FileChooser.openFile("Select a DICOMDIR", null);
    //  Wenn der Anwender abbricht nichts tun
    if (f == null) return;
    // Als Text darstellen
    dicomdirNameText.setText(f.getPath());
  }//GEN-LAST:event_existingDirOpenBtnActionPerformed

  
  private void filesystemOpenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filesystemOpenBtnActionPerformed
    // Add your handling code here:
    File  dir;
    File  lastSelectedDirectory;

    lastSelectedDirectory = new File(filesystemParentText.getText());
    if (lastSelectedDirectory == null) {
      dir = FileChooser.openDir("Select the Root-Directory", null);
    } else {
      dir = FileChooser.openDir("Select the Root-Directory", lastSelectedDirectory);
    }
    
    // Bei Abbruch nichts tun
    if (dir == null) return;
    
    lastSelectedDirectory = dir;
    filesystemParentText.setText(lastSelectedDirectory.toString());
  }//GEN-LAST:event_filesystemOpenBtnActionPerformed

  private void writeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeBtnActionPerformed
    // Add your handling code here:
    
    // Action wird in Dcm_Export ausgefuehrt
  }//GEN-LAST:event_writeBtnActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton writeBtn;
  private javax.swing.JLabel dicomdirNameLabel;
  private javax.swing.JRadioButton filesystemBtn;
  private javax.swing.JRadioButton dicomdirBtn;
  private javax.swing.JTextField dicomdirNameText;
  private javax.swing.JLabel newDirLabel;
  private javax.swing.JButton newDirOpenBtn;
  private javax.swing.JTextField filesystemNameText;
  private javax.swing.JLabel dcmCopyright;
  private javax.swing.JLabel writeLabel;
  private javax.swing.JLabel existingDirLabel;
  private javax.swing.JButton existingDirOpenBtn;
  private javax.swing.JLabel fileNameLabel;
  private javax.swing.JLabel thisCopyright;
  private javax.swing.JButton filesystemOpenBtn;
  private de.iftm.dcm4che.dcmie.exp.MetadataInfoPanel metadataInfoPanel;
  private javax.swing.JTextField filesystemParentText;
  private javax.swing.ButtonGroup saveGroup;
  // End of variables declaration//GEN-END:variables
  
  
  /**
   * Write a BufferedImage to the local filesystem.
   * @param ddi the DcmDataImage to write.
   * @param dest the file to which the DcmDataImage should be written.
   * @param appendImageNumber true, if image number should be appended to the 
   *                          filename. Preserve a file extension.
   * @throws IOException in the case of an input/output exception.
   * @throws UnsupportedOperationException if no DcmImageWriter could be found.
   */
  public static synchronized void writeToFilesystem(DcmDataImage ddi, File dest, boolean appendImageNumber) throws IOException, UnsupportedOperationException {
    File                f = null;
    String              fileName;
    int                 imageNumber;
    ImageOutputStream   imageOutputStream = null;
    ImageWriter         dcmImageWriter = null;
    IIOMetadata         dummyMetadata = null;

    // DcmImageWriter holen
    Iterator imageWriters = ImageIO.getImageWritersByFormatName("DICOM");
    while (true) {
      dcmImageWriter = (ImageWriter) imageWriters.next();
      if (dcmImageWriter == null) {
        throw new UnsupportedOperationException("No DcmImageWriter found.");
      }
      if (dcmImageWriter.getDefaultStreamMetadata(null) instanceof DcmMetadata) {
        break;
      }
    }

    // Filename festlegen
    fileName = dest.getName();
    if (appendImageNumber) {
      try {
        imageNumber = ddi.getDataset().getInt(Tags.InstanceNumber, 1);
      } catch (Exception e) {
        imageNumber = 1;
      }
      String s = "000" + Integer.toString(imageNumber);
      int i = fileName.lastIndexOf('.');
      if (i == -1) {
        fileName += s.substring(s.length() - 3);
      } else {
        fileName = fileName.substring(0, i) + s.substring(s.length() - 3) + fileName.substring(i);
      }
    }
    
    // Bestehenden File erst loeschen, dann neuen erstellen
    f = new File(dest.getParentFile(), fileName);
    if (f.exists()) {
      if (!f.delete()) throw new IOException("Can't delete output file.");
      f.createNewFile();
    }
    // Neuen ImageOutputStream fuer File oeffnen
    imageOutputStream = ImageIO.createImageOutputStream(f);

    // Den OutputStream des DcmImageWriters setzen
    dcmImageWriter.setOutput(imageOutputStream);


    // Dummy Metadaten holen
    dummyMetadata = dcmImageWriter.getDefaultStreamMetadata(null);
    // Mit DcmImageWriter konvertieren und speichern
    ((DcmMetadata) dummyMetadata).setDataset(ddi.getDataset());
    dcmImageWriter.write(dummyMetadata, new IIOImage(ddi.getImageArray()[0], null, null), null);

    // Stream schliessen
    imageOutputStream.close();
  }
  
  
  /**
   * Write a BufferedImage to a DICOMDIR.
   * @param ddi the DcmDataImage to write.
   * @param dicomdir the DICOMDIR file to which the DcmDataImage should be written.
   * @throws IOException in the case of an input/output exception.
   * @throws UnsupportedOperationException if no DcmImageWriter could be found.
   */
  public static synchronized void writeToDICOMDIR(DcmDataImage ddi, File dicomdir) throws IOException {
    File                imageFile;
    ImageOutputStream   imageOutputStream = null;
    ImageWriter         dcmImageWriter = null;
    IIOMetadata         dummyMetadata = null;
    DirBuilder          dirBuilder = null;
    DirWriter           dirWriter = null;
    String              uid;
    DirBuilderPref      pref;

    // DcmImageWriter holen
    Iterator imageWriters = ImageIO.getImageWritersByFormatName("DICOM");
    while (true) {
      dcmImageWriter = (ImageWriter) imageWriters.next();
      if (dcmImageWriter == null) {
        throw new UnsupportedOperationException("No DcmImageWriter found.");
      }
      if (dcmImageWriter.getDefaultStreamMetadata(null) instanceof DcmMetadata) {
        break;
      }
    }
    
    // Einen DirWriter und DirBuilder oeffnen
    if (dicomdir.exists()) {
      // Writer oeffnen
      dirWriter = DirBuilderFactory.getInstance().newDirWriter(dicomdir, null);
    } else {
      // Neues DICOMDIR mit eigener UID erzeugen:
      uid = UIDGenerator.getInstance().createUID();
      dirWriter = DirBuilderFactory.getInstance().newDirWriter(dicomdir, uid, null, null, null, null);
    }
    // DirBuilder erzeugen
    pref = getDirBuilderPref();
    dirBuilder = DirBuilderFactory.getInstance().newDirBuilder(dirWriter, pref);
    
    // Image-File als Ziel für den ImageWriter erzeugen
    imageFile = toFile(dicomdir, ddi.getDataset());

    // Ggf. Unterverzeichnisse generiereren
    imageFile.getParentFile().mkdirs();

    // Neuen ImageOutputStream fuer File oeffnen
    imageOutputStream = ImageIO.createImageOutputStream(imageFile);

    // Den OutputStream des DcmImageWriters setzen
    dcmImageWriter.setOutput(imageOutputStream);

    // Dummy Metadaten holen
    dummyMetadata = dcmImageWriter.getDefaultStreamMetadata(null);
    // Mit DcmImageWriter konvertieren und speichern
    ((DcmMetadata) dummyMetadata).setDataset(ddi.getDataset());
    dcmImageWriter.write(dummyMetadata, new IIOImage(ddi.getImageArray()[0], null, null), null);

    // Stream (Image-File) schliessen
    imageOutputStream.close();
    
    // Referenz auf den Image-File dem DICOMDIR hinzufuegen
    dirBuilder.addFileRef(imageFile);
    
    // DirBuilder und implizit DirWriter schliessen
    try {
      dirBuilder.close();
    } catch (Exception ignore) {}
    
  }

  
  /**
   * Create a new file with name PatientName/StudyID/SeriesNumber/InstanceNumber 
   * in the directory of a given DICOMDIR. If a file of calculated name already
   * exists create a random name (in the directory of DICOMDIR).
   * @param dicomdir the DICOMDIR.
   * @param ds the metadata to derive the filename.
   * @return the file.
   */
  private static File toFile(File dicomdir, Dataset ds) {
    File    file;
    File    parent;
    Random  RND = new Random();
    
    int[]   fileIDTags = {Tags.PatientName, Tags.StudyID, Tags.SeriesNumber, Tags.InstanceNumber};
    
    file = dicomdir.getParentFile();
    for (int i = 0; i < fileIDTags.length; ++i) {
     file = new File(file, toFileID(ds, fileIDTags[i]));
    }
    
    parent = file.getParentFile();
    while (file.exists()) {
     file = new File(parent, Integer.toHexString(RND.nextInt()).toUpperCase());
    }
    return file;
  }

  
  /**
   * Konvert given attribute to a string of max 8 uppercase letters and numbers. 
   * @param ds the Dataset containing the attribute.
   * @param tag the tag of the attribute.
   * @return the calculated string.
   */
  private static String toFileID(Dataset ds, int tag) {
    try {
      String s = ds.getString(tag);
      if (s == null || s.length() == 0)
        return "__NULL__";
      char[] in = s.toUpperCase().toCharArray();
      char[] out = new char[Math.min(8,in.length)];
      for (int i = 0; i < out.length; ++i) {
        out[i] = in[i] >= '0' && in[i] <= '9'
              || in[i] >= 'A' && in[i] <= 'Z'
               ? in[i] : '_';
      }
      return new String(out);
    } catch (DcmValueException e) {
      return "__ERR__";
    }
  }

  
  /**
   * Read the preferences file "resources/dcmdir.cfg" and add all lines with key
   * starting with "dir.".
   * @return the DirBuilderPref.
   */
  private static DirBuilderPref getDirBuilderPref() {
    Properties cfg;
     
    cfg = new Properties();
    InputStream in = DcmExportPanel.class.getResourceAsStream("resources/dcmdir.cfg");
    try {
     Properties retval = new Properties();
     cfg.load(in);
    } catch (Exception e) {
      throw new RuntimeException("Could not read dcmdir.cfg", e);
    } finally {
      if (in != null) {
        try { in.close(); } catch (IOException ignore) {}
      }
    }

    HashMap map = new HashMap();
    for (Enumeration en = cfg.keys(); en.hasMoreElements();) {
      addDirBuilderPrefElem(map, (String)en.nextElement());
    }
    DirBuilderPref pref = DirBuilderFactory.getInstance().newDirBuilderPref();
    for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry)it.next();
      pref.setFilterForRecordType((String)entry.getKey(), (Dataset)entry.getValue());
    }
    return pref;
   }

  
  /**
   * Add one line of preferences (key starting with "dir.") to map.
   * @param map the map to add to.
   * @param key the key of the preference entry.
   */
   private static void addDirBuilderPrefElem(HashMap map, String key) {
    if (!key.startsWith("dir.")) return;
      
    int pos2 = key.lastIndexOf('.');
    String type = key.substring(4,pos2).replace('_',' ');
    Dataset ds = (Dataset)map.get(type);
    if (ds == null) {
      map.put(type, ds = DcmObjectFactory.getInstance().newDataset());
    }
    int tag;
    tag = Tags.forName(key.substring(pos2+1));
    ds.putXX(tag, VRMap.DEFAULT.lookup(tag));
   }
  
}
