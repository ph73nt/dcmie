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

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import de.iftm.dcm4che.dcmie.*;
import de.iftm.java.util.*;
import de.iftm.javax.swing.*;
import de.iftm.javax.swing.filetree.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.5.15
 */
public class FileSystemTab extends javax.swing.JPanel implements java.io.Serializable {
  
  // The last selected roor directory
  private File    lastRootDir = null;
  
  
  /** Creates new form DirPanel */
  public FileSystemTab() {
    initComponents();
  }
  
  
  /**
   * Set up the Panel
   */
  public void setUp(DcmieParam dcmieParam) {
    if ((dcmieParam.isImportFilesystem) & (dcmieParam.importFile != null)) {
      // dcmieParam.importFile enthaelt ein Directory
      setDirectory(dcmieParam.importFile);
    } else {
      // Default Directory ist user.dir
      setDirectory(new File(System.getProperty("user.dir")));
    }
  }
  
  
  /**
   * Set a new local directory file.
   * @param dir the new local directory file.
   */
  public void setDirectory(File dir) {
    
    if (dir == null) return;
    
    lastRootDir = dir;
    
    // Textfeld updaten
    rootDirText.setText(dir.toString());
    
    fileSystemTreePanel.createNewTree(new FileSystemModel(dir));

    validate();
  }

  
  public FileSystemTreePanel getTreePanel () {
    return fileSystemTreePanel;
  }
  
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    openBtn = new javax.swing.JButton();
    rootDirText = new javax.swing.JTextField();
    scrollTree = new javax.swing.JScrollPane();
    fileSystemTreePanel = new de.iftm.javax.swing.filetree.FileSystemTreePanel();

    setLayout(new java.awt.GridBagLayout());

    openBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/iftm/dcm4che/dcmie/imp/resources/open.gif")));
    openBtn.setMaximumSize(new java.awt.Dimension(40, 23));
    openBtn.setMinimumSize(new java.awt.Dimension(40, 23));
    openBtn.setPreferredSize(new java.awt.Dimension(40, 23));
    openBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(openBtn, gridBagConstraints);

    rootDirText.setEditable(false);
    rootDirText.setPreferredSize(new java.awt.Dimension(256, 23));
    rootDirText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rootDirTextActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
    add(rootDirText, gridBagConstraints);

    scrollTree.setViewportView(fileSystemTreePanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
    add(scrollTree, gridBagConstraints);

  }//GEN-END:initComponents

  private void rootDirTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rootDirTextActionPerformed
    // Add your handling code here:
  }//GEN-LAST:event_rootDirTextActionPerformed

  private void openBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBtnActionPerformed
    File  dir;
    
    if (lastRootDir == null) {
      dir = FileChooser.openDir("Select the Root-Directory", new File("/"));
    } else {
      dir = FileChooser.openDir("Select the Root-Directory", lastRootDir);
    }
    
    setDirectory(dir);
  }//GEN-LAST:event_openBtnActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.iftm.javax.swing.filetree.FileSystemTreePanel fileSystemTreePanel;
  private javax.swing.JButton openBtn;
  private javax.swing.JTextField rootDirText;
  private javax.swing.JScrollPane scrollTree;
  // End of variables declaration//GEN-END:variables
  
}
