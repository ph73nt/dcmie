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

import gnu.getopt.*;

import ij.plugin.*;

import de.iftm.dcm4che.dcmie.*;
import de.iftm.dcm4che.dcmie.imp.*;
import de.iftm.javax.swing.*;


/**
 * This class is a PlugIn for the ImageJ program. The interface defines one
 * method:<br>
 *     public void run(java.lang.String arg) <br>
 *     This method is called when the plugin is loaded. 'arg', which may be 
 *     blank, is the argument specified for this plugin in IJ_Props.txt. <br>
 * Its purpose is to analyse DICOM files.<br>
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
public class Dcm_Inspector extends javax.swing.JFrame implements PlugIn {
  
	/**
	 * True, if this class is invoken by its main methode
	 */
  private static boolean      runAsApplication = false;

  
	/**
	 * The parameters of the dcmie collection of plugins
	 */
  private DcmieParam          dcmieParam = null;
  
  
  /**
   * Creates a new form: Dcm_Import
   */
  public Dcm_Inspector() {
    // setOperatingSystemLF();
    initComponents();
    Utilities.centerOnScreen(this);
  }
  
  
  /**
   * This method will only be called, if this frame is run as an application.
   * ImageJ only calls the method "run".
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    String    arg = "";
    
    Dcm_Inspector myself = new Dcm_Inspector();
    runAsApplication = true;

    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        arg += " " + args[i];
      }
      arg = arg.substring(1);
    }
    myself.run(arg);
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


  /*
   * This method is called by ImageJ when the plugin is loaded. It my have the 
   * following options:<br>
   * -p <file-uri> The name of an optional property file. If this option is not
   *               present the default <filename> = "dcmie.properties" is choosen.
   *               If that file is not found in the user.directory the default
   *               values of the class DcmieParam were taken.<br>
   * <br>
   * <file-uri>    Describes a file in a operating-system independend way. See the
   *               API-Doc of the URI class. For Windows-OS the absolute URI
   *               "file:/c:/user/tom/foo.txt" describes the file "C:\\user\\tom\\foo.txt". 
   *               Relative URI's, e.g. without the "file:" schema-prefix, are
   *               relativ to the user-directory, given by the system property
   *               user.dir. For example: If the user.dir is "C:\\user\\tom\\" and 
   *               the relative URI is "/abc/foo.txt" the referenced file is
   *               "C:\\user\\tom\\abc\\foo.txt". The abbreviations "." for the current
   *               and ".." for the upper directory are valid to form a relative URI.
   * @param arg the argument specified for this plugin in IJ_Props.txt. It may be "".
   */
  public void run(String arg) {
    Getopt              g;
    int                 c;
    Vector              argv;
    StringTokenizer     tok;
    FileInputStream     propertyFIS = null;
    File                propertyFile = new File("dcmie.properties");
    
    // System.out.println("arg: " + arg);
    
    // Wandelt den arg-String in ein POSIX konformes Array um.
    // siehe auch http://java.sun.com/docs/books/tutorial/essential/attributes/cmdLineArgs.html
    tok = new StringTokenizer(arg);
    argv = new Vector();
    while (tok.hasMoreTokens()) {
      argv.addElement(tok.nextToken());
    }
    
    // Optionen auswerten
    g = new Getopt("Dcm_Import", (String[]) argv.toArray(new String[0]), "p:");
    while ((c = g.getopt()) != -1) {
      switch(c) {
        
        case 'p':
          propertyFile = DcmieParam.uriToFile(g.getOptarg());
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
    
    // Setup Methode des DcmInspectorPanel aufrufen. In diesem Panel ist das GUI
    // zusammengefasst.
    dcmInspectorPanel.setUp(dcmieParam);
    
    // Dialog anzeigen
    this.setVisible(true);
    
    // An dieser Stelle ist nichts weiter zu tun. Da das Plugin als Frame
    // realisiert ist, bleibt es solange sichtbar, bis es vom Anwender
    // geschlossen wird. Die Aktionen des Plugin werden in der Methode 
    // "actionPerformed" realisiert.

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
    dcmInspectorPanel = new de.iftm.dcm4che.dcmie.inspect.DcmInspectorPanel();

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        exitForm(evt);
      }
    });

    dcmInspectorPanel.setMinimumSize(new java.awt.Dimension(790, 486));
    dcmInspectorPanel.setPreferredSize(new java.awt.Dimension(790, 486));
    getContentPane().add(dcmInspectorPanel, java.awt.BorderLayout.CENTER);

    pack();
  }//GEN-END:initComponents

  
  /**
   * Called if a windowClosing event is received.
   */
  private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
    terminate();
  }//GEN-LAST:event_exitForm

  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.iftm.dcm4che.dcmie.inspect.DcmInspectorPanel dcmInspectorPanel;
  // End of variables declaration//GEN-END:variables
  
}
