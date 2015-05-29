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

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import gnu.getopt.*;

import ij.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.process.*;

import org.dcm4che.data.*;

import de.iftm.ij.plugins.dcmie.*;
import de.iftm.ij.plugins.dcmie.testip.*;
import de.iftm.dcm4che.dcmie.*;
import de.iftm.dcm4che.dcmie.imp.*;
import de.iftm.javax.swing.*;


/**
 * This class is a PlugInFilter for the ImageJ program. The interface defines
 * two methods:<br>
 *     int setup(java.lang.String arg, ImagePlus imp) <br>
 *     This method is called once when the filter is loaded. <br>
 *     void run(ImageProcessor ip) <br>
 *     Filters use this method to process the image. <br>
 * Its purpose is to list all the properties assigned to an ImagePlus.<br>
 * <br>
 * The ImageJ project:<br>
 * http://rsb.info.nih.gov/ij/default.html<br>
 * Author: Wayne Rasband, wayne@codon.nih.gov<br>
 * Research Services Branch, National Institute of Mental Health, Bethesda, Maryland, USA.<br>
 * Download: ftp://codon.nih.gov/pub/image-j/<br>
 *
 * @author   Thomas Hacklaender
 * @version  2002.8.22
 */
public class Dcm_PropertyLister extends javax.swing.JFrame implements PlugInFilter {
  
  
	/**
	 * Version
	 */
  private final String        VERSION = "1.0.0";
  
  
	/**
	 * True, if this class is invoken by its main methode
	 */
  private static boolean      runAsApplication = false;
  
  
	/**
	 * The parameters of the dcmie collection of plugins
	 */
  private DcmieParam          dcmieParam = null;
  
  
  /**
   * The ImagePlus to process
   */
  private ImagePlus            imgPlus = null;

  
  /**
   * Creates a new form: Dcm_PropertyLister
   */
  public Dcm_PropertyLister() {
    // setOperatingSystemLF();
    initComponents();
    Utilities.centerOnScreen(this);
    thisCopyright.setText("Version " + VERSION + " (c) 2002 by Thomas Hacklaender under the GNU General Public License.");
    dcmCopyright.setText("Based on the DICOM library http://sourceforge.net/projects/dcm4che/ by Gunter Zeilinger.");
  }
  
  
  /**
   * This method will only be called, if this frame is run as an application.
   * ImageJ only calls the method "run".
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    String    arg = "";
    
    Dcm_PropertyLister myself = new Dcm_PropertyLister();
    runAsApplication = true;

    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        arg += " " + args[i];
      }
      arg = arg.substring(1);
    }
    myself.setup(arg, null);
  }

  
  /**
   * Terminate application/frame.
   */
  private void terminate() {
    if (runAsApplication) {
      // Ueber "main" gestartet: Applikation beenden
      System.exit(0);
    } else {
      // Plugin Aufruf von ImageJ: Frame freigeben
      this.dispose();
    }
  }

  
  /**
   * Terminate application/frame during the setup method.
   * @param value the return value of the setup method.
   * @return the return value given as parameter.
   */
  private int terminateSetup(int value) {
    if (runAsApplication) {
      // Ueber "main" gestartet: Applikation beenden
      System.exit(0);
      // Wird nicht erreicht
      return value;
    } else {
      // Plugin Aufruf von ImageJ: Frame freigeben
      this.dispose();
      // Einen Rueckgabewert uebergeben
      return value;
    }
  }


  /*
   * This method is called by ImageJ when the plugin is loaded. It my have the 
   * following options:<br>
   * -t   Run in test-mode: Use a 8-Bit grayscale image with String and Binary
   *      properties as a test image.<br>
   *
   * @param arg the argument specified for this plugin in IJ_Props.txt. It may be "".
   * @param imp the ImagePlus to process.
   * @return the supported image types.
   */
	public int setup(String arg, ImagePlus imp) {
    Getopt              g;
    int                 c;
    Vector              argv;
    boolean             testMode = false;
    StringTokenizer     tok;
    FileInputStream     propertyFIS = null;
    File                propertyFile = new File("dcmie.properties");

    // Wandelt den arg-String in ein POSIX konformes Array um.
    // siehe auch http://java.sun.com/docs/books/tutorial/essential/attributes/cmdLineArgs.html
    tok = new StringTokenizer(arg);
    argv = new Vector();
    while (tok.hasMoreTokens()) {
      argv.addElement(tok.nextToken());
    }
    
    // Optionen auswerten
    g = new Getopt("Dcm_Import", (String[]) argv.toArray(new String[0]), "t");
    while ((c = g.getopt()) != -1) {
      switch(c) {
          
        case 't':
          testMode = true;
          break;
        
        default:
          System.err.println("*** Warning: getopt() returned " + c);
      }
    }

    // Voreistellungen festlegen
    try {
      propertyFIS = new FileInputStream(propertyFile);
      dcmieParam = new DcmieParam(propertyFIS);
      propertyFIS.close();
    } catch (Exception e) {
      dcmieParam = new DcmieParam(null);
      System.err.println("*** Warning: Can't access property-file " + propertyFile.toString());
    }
    
    // Wenn in Test-Mode: Testbild verwenden
    if (testMode) {
      // ImagePlus simulieren
      imp = new TestImagePlusG8();
      ((TestImagePlus) imp).addProperties(DcmiePropertiesUtil.createPatientID(), 1, 1, 1, true, true);
    }

    // Kein Image Plus in ImageJ aktiv oder in den Parametern uebergeben
    if (imp == null) {
      System.err.println("*** Error: No ImagePlus.");
      return terminateSetup(DONE);
    }
   
    // Referenz auf ImgePlus speichern
    imgPlus = imp;
        
    // Frame anzeigen
    this.setVisible(true);
    
    // Als Standard wird die Tabelle fuer das aktive ImagePlus aktualisiert
    refreshPropertyTable();
    
    // An dieser Stelle ist nichts weiter zu tun. Da das Plugin als Frame
    // realisiert ist, bleibt es solange sichtbar, bis es vom Anwender
    // geschlossen wird. Die Aktionen des Plugin werden in der Methode 
    // "actionPerformed" realisiert.
    
    // Alle ImagePlus Typen werden unterstuetzt.
    // Stacks werden bearbeitet.
    // Run-Methode braucht nicht aufgerufen zu werden.
    // Pixeldaten werden nicht veraendert.
		return DOES_ALL +
		       DOES_STACKS +
           DONE +
           NO_CHANGES;
  }  


	/**
	 * Called once for every image in a stack.
	 * @param ip  the ImageProcessor of the image.
	 */
	public void run(ImageProcessor ip) {
    // Nichts zu tun
	} 
  
  
  /**
   *
   */
  private void refreshPropertyTable() {
    Properties    prop;
    Enumeration   enum;
    String        key;
    Vector        keyVector;
    String[]      keyArray;
    Object        value;
        
    // Inhalt der Tabelle loeschen
    while (propertyTable.getModel().getRowCount() > 0) {
      ((DefaultTableModel) propertyTable.getModel()).removeRow(0);
    }
    
    // Bildtitel darstellen
    title.setText("Properties of image: " + imgPlus.getTitle());
    
    // Properties des aktiven ImagePlus holen
    prop = imgPlus.getProperties();   
    if (prop == null) {
      ((DefaultTableModel) propertyTable.getModel()).addRow(new String [] {"No Properties", ""});
      return;
    }
    
    // Alle Key's in einen Vector eintragen
    enum = prop.propertyNames();
    keyVector = new Vector();
    while (enum.hasMoreElements()) {
      keyVector.addElement(enum.nextElement());
    }
    
    // Vector in sortiertes Array umwandel
    keyArray = (String[]) keyVector.toArray(new String[0]);
    Arrays.sort(keyArray);
    
    for (int p = 0; p < keyArray.length; p++) {
      key = keyArray[p];
      value = (Object) prop.getProperty(key);
      addPropertyRow(keyArray[p], value);
    }
  }
  
  
  /**
   * Adds the singel row to table.
   */
  private void addPropertyRow(String key, Object value) {
    if (value instanceof String) {
      ((DefaultTableModel) propertyTable.getModel()).addRow(new String [] {key, (String) value});
      return;
    } else {
      ((DefaultTableModel) propertyTable.getModel()).addRow(new String [] {key, "<Object>"});
    }
  }
  
  
  /**
   * Set the user interface to the type of the current operating system.
   */
  private void setOperatingSystemLF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
  }

  
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    scroll = new javax.swing.JScrollPane();
    propertyTable = new javax.swing.JTable();
    title = new javax.swing.JLabel();
    thisCopyright = new javax.swing.JLabel();
    dcmCopyright = new javax.swing.JLabel();

    getContentPane().setLayout(new java.awt.GridBagLayout());

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        exitForm(evt);
      }
    });

    propertyTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Key", "Value"
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
    scroll.setViewportView(propertyTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(scroll, gridBagConstraints);

    title.setToolTipText("null");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    getContentPane().add(title, gridBagConstraints);

    thisCopyright.setFont(new java.awt.Font("Dialog", 0, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    getContentPane().add(thisCopyright, gridBagConstraints);

    dcmCopyright.setFont(new java.awt.Font("Dialog", 0, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    getContentPane().add(dcmCopyright, gridBagConstraints);

    pack();
  }//GEN-END:initComponents

  
  /**
   * Called if a windowClosing event is received.
   */
  private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
    terminate();
  }//GEN-LAST:event_exitForm

  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTable propertyTable;
  private javax.swing.JLabel dcmCopyright;
  private javax.swing.JLabel title;
  private javax.swing.JScrollPane scroll;
  private javax.swing.JLabel thisCopyright;
  // End of variables declaration//GEN-END:variables
  
}
