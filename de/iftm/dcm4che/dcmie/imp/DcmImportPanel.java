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
package de.iftm.dcm4che.dcmie.imp;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.Implementation;
import org.dcm4che.image.*;
import org.dcm4che.imageio.plugins.*;

import de.iftm.javax.swing.filetree.*;
import de.iftm.java.util.*;
import de.iftm.dcm4che.*;
import de.iftm.dcm4che.image.*;
import de.iftm.dcm4che.dcmie.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.8.22
 */
public class DcmImportPanel extends javax.swing.JPanel implements FileSelectionListener, java.io.Serializable {

  
	/**
	 * Version number of this plugin
	 */
	public final static String    VERSION = "1.0.0";

  private File                        lastSelectedFile = null;
  private File[]                      selectedFiles = null;
  private IJPropertyPanel             ijPropertyPanel;
  private GridBagConstraints          ijPropGridBagConstraints;

  
  /**
   * Creates new form DirPanel
   */
  public DcmImportPanel() {
    initComponents();
    myInitComponents();
  }
  
  
  /**
   * Set up the Panel
   */
  public void setUp(DcmieParam dcmieParam) {
    selInputPanel.setUp(dcmieParam);
    fileInfoPanel.setUp(dcmieParam);
    
    // Als Listener registrieren
    selInputPanel.getFileSystemTab().getTreePanel().addFileSelectionListener(this);
    selInputPanel.getDICOMDIRTab().getDirTable().addFileSelectionListener(this);

    // Das Panel zur Modifiktion der ImageJ Properties ggf. hinzufuegen
    if (dcmieParam.ijMode) {
      add(ijPropertyPanel, ijPropGridBagConstraints);
      ijPropertyPanel.setUp(dcmieParam);
    }
  }
  

  /**
   * Implementation of the FileSelectionListener interface.
   * @param e the FileSelection event.
   */
  public void fileSelected(FileSelectionEvent e) {
    
    lastSelectedFile = e.getLastSelectedFile();
    selectedFiles = e.getSelectedFiles();
    
    // File Info anzeigen
    fileInfoPanel.setInput(lastSelectedFile);
    
    // System.out.println("DcmImportPanel: " + lastSelectedFile.toString());
    // dcmInfoPanel.setInput(lastSelectedFile);
    
    try {
      imageBean.setSizePolicy(ImageBean.SIZE_POLICY_FIT);
      imageBean.setInput(lastSelectedFile);
    } catch (Exception ex) {
      // Default Bild darstellen
      try {
        imageBean.setInput((File) null);
      } catch (Exception ex2) {}
    }
  }
  
  
  /**
   *
   */
  public File getLastSelectedFile() {
    return lastSelectedFile;
  }
  
  
  /**
   *
   */
  public File[] getSelectedFiles() {
    return selectedFiles;
  }
  
  
  /**
   *
   */
  public JButton getImportImageButton() {
    return importImageButton;
  }
  
  
  /**
   *
   */
  public SelInputPanel getSelInputPanel() {
    return selInputPanel;
  }
  
  
  /** This method is called from within the constructor to
   * initialize the form.
   */
  private void myInitComponents() {
    
    // Copyright Anmerkungen in die GUI eintragen
    thisCopyright.setText("(c) 2002 by Thomas Hacklaender under the GNU General Public License. Version: " + VERSION);
    dcmCopyright.setText("Based on the DICOM library http://sourceforge.net/projects/dcm4che/ by Gunter Zeilinger. Version: " + Implementation.getVersionName());

    // Das Panel zur Modifiktion der ImageJ Properties vorbereiten. Das Panel 
    // wird in der Methode setUp ggf. zum GUI hinzugefuegt.
    ijPropertyPanel = new IJPropertyPanel();
    ijPropGridBagConstraints = new java.awt.GridBagConstraints();
    ijPropGridBagConstraints.gridx = 0;
    ijPropGridBagConstraints.gridy = 2;
    ijPropGridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    ijPropGridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 15);
    ijPropGridBagConstraints.weightx = 0.5;
    ijPropGridBagConstraints.weighty = 0.5;
    // add(ijPropertyPanel, ijPropGridBagConstraints);
  }

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    thisCopyright = new javax.swing.JLabel();
    dcmCopyright = new javax.swing.JLabel();
    importImageButton = new javax.swing.JButton();
    selInputPanel = new de.iftm.dcm4che.dcmie.imp.SelInputPanel();
    imageBean = new de.iftm.dcm4che.image.ImageBean();
    fileInfoPanel = new de.iftm.dcm4che.dcmie.imp.FileInfoPanel();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(256, 128));
    setPreferredSize(new java.awt.Dimension(0, 0));
    thisCopyright.setFont(new java.awt.Font("Dialog", 0, 10));
    thisCopyright.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    thisCopyright.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
    gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 10);
    add(thisCopyright, gridBagConstraints);

    dcmCopyright.setFont(new java.awt.Font("Dialog", 0, 10));
    dcmCopyright.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    dcmCopyright.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 10);
    add(dcmCopyright, gridBagConstraints);

    importImageButton.setIcon(new javax.swing.ImageIcon(""));
    importImageButton.setText("Import images");
    importImageButton.setMaximumSize(new java.awt.Dimension(120, 27));
    importImageButton.setMinimumSize(new java.awt.Dimension(120, 27));
    importImageButton.setPreferredSize(new java.awt.Dimension(120, 27));
    importImageButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        importImageButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.insets = new java.awt.Insets(10, 15, 5, 15);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 0.5;
    add(importImageButton, gridBagConstraints);

    selInputPanel.setMinimumSize(new java.awt.Dimension(256, 256));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
    add(selInputPanel, gridBagConstraints);

    imageBean.setMinimumSize(new java.awt.Dimension(192, 192));
    imageBean.setPreferredSize(new java.awt.Dimension(192, 192));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(27, 10, 0, 15);
    add(imageBean, gridBagConstraints);

    fileInfoPanel.setPreferredSize(new java.awt.Dimension(256, 140));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 15);
    add(fileInfoPanel, gridBagConstraints);

  }//GEN-END:initComponents

  private void importImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importImageButtonActionPerformed
    // Add your handling code here:
  }//GEN-LAST:event_importImageButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.iftm.dcm4che.image.ImageBean imageBean;
  private de.iftm.dcm4che.dcmie.imp.FileInfoPanel fileInfoPanel;
  private javax.swing.JLabel dcmCopyright;
  private de.iftm.dcm4che.dcmie.imp.SelInputPanel selInputPanel;
  private javax.swing.JLabel thisCopyright;
  private javax.swing.JButton importImageButton;
  // End of variables declaration//GEN-END:variables

  
  /**
   * Read a DICOM file. The file may be a multiframe image file. Use the class
   * method getSelectedFiles to get a list of selected files to read (either from
   * the filesystem or DICOMDIR).
   * @param src the file to read.
   * @return the contents of the file as a DcmDataImage.
   * @throws IOException in the case of an input/output exception.
   * @throws UnsupportedOperationException if no DcmImageReader could be found.
   */
  public static synchronized DcmDataImage readFromFilesystem(File src) throws IOException, UnsupportedOperationException {
    javax.imageio.ImageReader   dicomReader = null;
    ImageInputStream            iis = null;
    Dataset                     ds;
    int                         numImages;
    BufferedImage[]             imageArray = null;
     
    // DICOM Reader erzeugen
    try {
      Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
      dicomReader = (javax.imageio.ImageReader) iter.next();
    } catch (Exception e) {
      throw new UnsupportedOperationException("No DcmImageReader found.");
    }

    try {
      iis = ImageIO.createImageInputStream(src);
      dicomReader.setInput(iis, false);
      
      // Liefert Dataset ohne Pixeldaten
      ds = ((DcmMetadata) dicomReader.getStreamMetadata()).getDataset();
      
      // Anzahl der Bilder (bei Multiframe Images groesser als 1)
      numImages = dicomReader.getNumImages(true);

      // Array fuer die Bilder
      imageArray = new BufferedImage[numImages];
      
      // Alle Bilder einlesen
      for (int i = 0; i < numImages; i++) {
        // Liefert das BufferedImage des k-ten Bildes
        imageArray[i] = dicomReader.read(i);        
      }
      
      // Erfolgreich eingelesen
      return new DcmDataImage(ds, imageArray);
      
    } finally {
      
      // Stream schliessen
      try {
      iis.close();
      } catch (Exception ignore) {}
      
    }
    
  }
  
}
