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

import de.iftm.dcm4che.image.*;
import de.iftm.dcm4che.dcmie.*;
import de.iftm.javax.swing.filetree.*;
import de.iftm.java.util.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.5.27
 */
public class ImagePanel extends javax.swing.JPanel implements FileSelectionListener, java.io.Serializable {

  
  /**
   * Creates new form DirPanel
   */
  public ImagePanel() {
    initComponents();
  }
  
  
  /**
   * Set up the Panel
   */
  public void setUp(DcmieParam dcmieParam) {
    
    imageBean.setSizePolicy(ImageBean.SIZE_POLICY_SCROLL);
    
    switch (imageBean.getSizePolicy()) {
      
      case ImageBean.SIZE_POLICY_ORIGINAL:
        originalSize.setSelected(true);
        break;
        
      case ImageBean.SIZE_POLICY_FIT:
        fitSize.setSelected(true);
        break;
        
      case ImageBean.SIZE_POLICY_SCROLL:
        scrollSize.setSelected(true);
        break;
    }
    
    imageBean.setMousePolicy(ImageBean.MOUSE_POLICY_WINDOW);
    
    switch (imageBean.getMousePolicy()) {
      
      case ImageBean.MOUSE_POLICY_WINDOW:
        windowMouse.setSelected(true);
        break;
        
      case ImageBean.MOUSE_POLICY_ZOOM:
        zoomMouse.setSelected(true);
        break;
    }

  }
  

  /**
   * Implementation of the FileSelectionListener interface.
   * @param e the FileSelection event.
   */
  public void fileSelected(FileSelectionEvent e) {
    try {
      imageBean.setInput(e.getLastSelectedFile());
    } catch (Exception ex) {
      // Default Bild darstellen
      try {
        imageBean.setInput((File) null);
      } catch (Exception ex2) {}
    }
  }

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    mouseGroup = new javax.swing.ButtonGroup();
    sizeGroup = new javax.swing.ButtonGroup();
    imageBean = new de.iftm.dcm4che.image.ImageBean();
    windowMouse = new javax.swing.JRadioButton();
    zoomMouse = new javax.swing.JRadioButton();
    originalSize = new javax.swing.JRadioButton();
    fitSize = new javax.swing.JRadioButton();
    scrollSize = new javax.swing.JRadioButton();
    mouseLabel = new javax.swing.JLabel();
    sizeLabel = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(256, 64));
    setPreferredSize(new java.awt.Dimension(0, 0));
    imageBean.setMaximumSize(new java.awt.Dimension(400, 400));
    imageBean.setMinimumSize(new java.awt.Dimension(400, 400));
    imageBean.setPreferredSize(new java.awt.Dimension(400, 400));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridheight = 9;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(imageBean, gridBagConstraints);

    windowMouse.setText("Window");
    mouseGroup.add(windowMouse);
    windowMouse.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        windowMouseActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
    add(windowMouse, gridBagConstraints);

    zoomMouse.setText("Zoom");
    mouseGroup.add(zoomMouse);
    zoomMouse.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        zoomMouseActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
    add(zoomMouse, gridBagConstraints);

    originalSize.setText("Original");
    sizeGroup.add(originalSize);
    originalSize.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        originalSizeActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
    add(originalSize, gridBagConstraints);

    fitSize.setText("Fit to panel");
    sizeGroup.add(fitSize);
    fitSize.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fitSizeActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
    add(fitSize, gridBagConstraints);

    scrollSize.setText("Scroll");
    sizeGroup.add(scrollSize);
    scrollSize.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        scrollSizeActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
    add(scrollSize, gridBagConstraints);

    mouseLabel.setText("Mouse policy");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
    add(mouseLabel, gridBagConstraints);

    sizeLabel.setText("Size policy");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
    add(sizeLabel, gridBagConstraints);

  }//GEN-END:initComponents

  
  private void scrollSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollSizeActionPerformed
    // Add your handling code here:
    imageBean.setSizePolicy(ImageBean.SIZE_POLICY_SCROLL);
  }//GEN-LAST:event_scrollSizeActionPerformed

  
  private void originalSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_originalSizeActionPerformed
    // Add your handling code here:
    imageBean.setSizePolicy(ImageBean.SIZE_POLICY_ORIGINAL);
  }//GEN-LAST:event_originalSizeActionPerformed

  
  private void fitSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitSizeActionPerformed
    // Add your handling code here:
    imageBean.setSizePolicy(ImageBean.SIZE_POLICY_FIT);
  }//GEN-LAST:event_fitSizeActionPerformed

  
  private void zoomMouseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomMouseActionPerformed
    // Add your handling code here:
    imageBean.setMousePolicy(ImageBean.MOUSE_POLICY_ZOOM);
  }//GEN-LAST:event_zoomMouseActionPerformed

  
  private void windowMouseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowMouseActionPerformed
    // Add your handling code here:
    imageBean.setMousePolicy(ImageBean.MOUSE_POLICY_WINDOW);
  }//GEN-LAST:event_windowMouseActionPerformed
  

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup sizeGroup;
  private javax.swing.JLabel sizeLabel;
  private javax.swing.JRadioButton fitSize;
  private javax.swing.JRadioButton originalSize;
  private javax.swing.JRadioButton scrollSize;
  private de.iftm.dcm4che.image.ImageBean imageBean;
  private javax.swing.ButtonGroup mouseGroup;
  private javax.swing.JRadioButton windowMouse;
  private javax.swing.JRadioButton zoomMouse;
  private javax.swing.JLabel mouseLabel;
  // End of variables declaration//GEN-END:variables
  
}
