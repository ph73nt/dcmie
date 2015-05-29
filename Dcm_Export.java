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

import gnu.getopt.Getopt;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;

import de.iftm.dcm4che.dcmie.DcmieParam;
import de.iftm.dcm4che.dcmie.DcmiePropertiesUtil;
import de.iftm.ij.plugins.dcmie.FileExporter;
import de.iftm.ij.plugins.dcmie.testip.TestImagePlus;
import de.iftm.ij.plugins.dcmie.testip.TestImagePlusC8;
import de.iftm.ij.plugins.dcmie.testip.TestImagePlusG8;
import de.iftm.ij.plugins.dcmie.testip.TestImageStack;
import de.iftm.javax.swing.Utilities;


/**
 * This class is a PlugInFilter for the ImageJ program. The interface defines
 * two methods:<br>
 *     int setup(java.lang.String arg, ImagePlus imp) <br>
 *     This method is called once when the filter is loaded. <br>
 *     void run(ImageProcessor ip) <br>
 *     Filters use this method to process the image. <br>
 * The purpose of the PPlugInFilter is to export an ImageJ image as a DICOM 
 * Secondary Capture image. If the ImagePlus contains more than one slice a 
 * list of DICOM images is generated.<br>
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
public class Dcm_Export extends javax.swing.JFrame implements PlugInFilter, ActionListener {
  
	/**
	 * True, if this class is invoken by its main methode
	 */
  private static boolean      runAsApplication = false;

  
	/**
	 * True, if the GUI should not be displayed and the DcmExportPanel#doWriteImagePlus
   * should be called automatically.
	 */
  private boolean             hideGUI = false;

  
	/**
	 * The parameters of the dcmie collection of plugins.
	 */
  private DcmieParam          dcmieParam = null;

  
  /**
   * The metadata describing not image-related information of a Secondary Capture
   * image.
   */
  private Dataset             infoMetadataDataset = null;

  
  /**
   * The metadata of the ImagePlus (only used by Dcm_Exporter).
   */
  private Dataset[]           imageMetadataArray = null;

  
	/**
	 * The ImagePlus to write.
	 */
  private ImagePlus           imagePlus = null;
  
  
  /**
   * Creates a new form: Dcm_Import
   */
  public Dcm_Export() {
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
    
    Dcm_Export myself = new Dcm_Export();
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
   * -p <file-uri> The name of an optional property file. If this option is not
   *               present the default <filename> = "dcmie.properties" is choosen.
   *               If that file is not found in the user.directory the default
   *               values of the class DcmieParam were taken.<br>
   * -h            Do not show the GUI.<br>
   * -t <type>     Run in test-mode: Use a test image of type:<br>
   *               b = 8-Bit grayscale<br>
   *               s = 16-Bit grayscale<br>
   *               r = RGB<br>
   *               i = 8-Bit indexed color<br>
   *               m = 16-Bit grayscale multiimage<br>
   *               provided by this method.<br>
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
   * @param imp the ImagePlus to process.
   * @return the supported image types.
   */
	public int setup(String arg, ImagePlus imp) {
    
	if(arg != null && arg.equals("")){
		arg = Macro.getOptions();
	}
		
	Getopt              g;
    int                 c;
    Vector              argv;
    boolean             testMode = false;
    StringTokenizer     tok;
    Dataset             defaultMetadataDataset;
    Dataset             imageMetadataDataset;
    Dataset             dummy;
    char                testType = ' ';
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
    g = new Getopt("Dcm_Export", (String[]) argv.toArray(new String[0]), "hp:t:");
    while ((c = g.getopt()) != -1) {
      switch(c) {
          
        case 'h':
          hideGUI = true;
          break;
        
        case 'p':
          propertyFile = DcmieParam.uriToFile(g.getOptarg());
          System.out.println(g.getOptarg());
          break;
          
        case 't':
          testMode = true;
          testType = g.getOptarg().charAt(0);
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
      switch (testType) {
        case 'b':
          imp = new TestImagePlusG8();
          ((TestImagePlus) imp).addProperties(DcmiePropertiesUtil.createPatientID(), 1, 1, 1, true, true);
          break;
          
        case 's':
          imp = new TestImagePlusG8();
          ((TestImagePlusG8) imp).convertToG16();
          ((TestImagePlus) imp).addProperties(DcmiePropertiesUtil.createPatientID(), 1, 1, 1, true, true);
          break;
          
        case 'r':
          imp = new TestImagePlusC8();
          ((TestImagePlusC8) imp).convertToRGB();
          ((TestImagePlus) imp).addProperties(DcmiePropertiesUtil.createPatientID(), 1, 1, 1, true, true);
          break;
          
        case 'i':
          imp = new TestImagePlusC8();
          ((TestImagePlus) imp).addProperties(DcmiePropertiesUtil.createPatientID(), 1, 1, 1, true, true);
          break;
          
        case 'm':
          imp = new TestImageStack();
          break;

        default:
          break;
      }

    }

    // Kein Image Plus in ImageJ aktiv oder in den Parametern uebergeben
    if (imp == null) {
      System.err.println("*** Error: No ImagePlus.");
      return terminateSetup(DONE);
    }
    
    // Feststellen, ob der Bildtyp unterstuetzt wird
    if (imp.getType() == ImagePlus.GRAY32) {
      // GRAY32 wird nicht unterstuetzt
      return terminateSetup(DONE);
    }
    
    // ImagePlus in globaler Variablen speichern
    imagePlus = imp;
    
    // Ein Metadata Dataset mit Defaultwerten generieren
    defaultMetadataDataset = DcmiePropertiesUtil.getDefaultInfoMetadata(DcmiePropertiesUtil.createPatientID(), 1, 1, 1);

    // Das  Metadaten Array aus dem ImagePlus extrahieren
    try {
      // Im Parameterblock speichern
      imageMetadataArray = DcmiePropertiesUtil.propertiesToDataset(imp.getProperties(), 1, true);
    } catch (Exception e) {
      //  Bei Fehler waehrend der Konvertierung abbrechen
      System.err.println("*** Error: Syntax error in image properties.");
      return terminateSetup(DONE);
    }
    
    // Image Metadaten entsprechen den Metadaten des ersten Slice
    if (imageMetadataArray == null) {
      // Keine Metadaten im ImagePlus vorhanden
      imageMetadataDataset = defaultMetadataDataset;
    } else {
      // Es sind Metadaten im ImagePlus vorhanden
      imageMetadataDataset = imageMetadataArray[0];
    }
    
    // Falls Image-Metadaten verwendet werden sollen ueberschreiben diese
    // die Defaultwerte
    if (dcmieParam.isUseImageMetadata) {
      defaultMetadataDataset.putAll(imageMetadataDataset);
    }
    
    // Falls eine Property Datei mit General-Metadaten angegeben wurde, 
    // ueberschreiben diese die bisherigen Werte
    if (dcmieParam.generalMetadataDataset != null) {
      defaultMetadataDataset.putAll(dcmieParam.generalMetadataDataset);
    }
    
    // Metadaten in den Parameterblock eintragen. Dabei gegebenenfalls auf die
    // Mask-Metadaten eingrenzen
    if (dcmieParam.maskMetadataDataset == null) {
      infoMetadataDataset = defaultMetadataDataset;
    } else {
      dummy = defaultMetadataDataset.subSet(dcmieParam.maskMetadataDataset);
      // Alle Elemente des eingegrenzten Dataset in ein neues Dataset kopieren
      infoMetadataDataset = DcmObjectFactory.getInstance().newDataset();
      infoMetadataDataset.putAll(dummy);
    }
    
    // Setup Methode des DcmExportPanel aufrufen:
    dcmExportPanel.setUp(dcmieParam, infoMetadataDataset);
    
    // Dieses Plugin als Listener fuer den "Write" Button registrieren
    dcmExportPanel.getWriteButton().addActionListener(this);

    if (hideGUI) {
      // GUI nicht anzeigen: Ohne Dialogbox exportieren.
      doWriteImagePlus();
      // Automatisch terminieren
      return terminateSetup(DONE);
    } else {
      // GUI anzeigen
      this.setVisible(true);
    }
        
    // An dieser Stelle ist nichts weiter zu tun. Da das Plugin als Frame
    // realisiert ist, bleibt es solange sichtbar, bis es vom Anwender
    // geschlossen wird. Die Aktionen des Plugin werden in der Methode 
    // "actionPerformed" realisiert.
    
    // Alle ImagePlus Typen, bis auf GRAY32 (Float), werden unterstuetzt.
    // Stacks werden bearbeitet.
    // Run-Methode braucht nicht aufgerufen zu werden.
    // Pixeldaten werden nicht veraendert.
		return DOES_16 +
		       DOES_8C + 
		       DOES_8G + 
		       DOES_RGB + 
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
   * Receives Events from the "importImageButton" of DcmImportPanel.
   * This method is invoken by the event-dispatch thread.
   * @param actionEvent the received Event.
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    
    // Events von anderen Buttons ignorieren
    if (actionEvent.getSource() != dcmExportPanel.getWriteButton()) return;
    
    // Bilder exportieren
    doWriteImagePlus();
  }

  
  /**
   * Writes the ImagePlus.
   */
  private void doWriteImagePlus() {
    FileExporter    exporter;
    
    try {
      // Parameterblock aktualisieren
      dcmExportPanel.updateDcmieParam(dcmieParam);
      // Info-Metadaten holen
      infoMetadataDataset = dcmExportPanel.getInfoMetadata();
      // Export starten
      exporter = new FileExporter(imagePlus, dcmieParam, infoMetadataDataset, imageMetadataArray);
      exporter.run();
    } catch (DcmValueException e) {
      // Fehler bei der Konvertierung eines Eintrags in die Metadaten Tabelle
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(null, e.getMessage(), "Error during export.", JOptionPane.INFORMATION_MESSAGE);
    }

    // Automatisch beenden.
    // User braucht nicht aktiv den "schliessen"  Knopf des Frames zu klicken.
    terminate();
    
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
    dcmExportPanel = new de.iftm.dcm4che.dcmie.exp.DcmExportPanel();

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        exitForm(evt);
      }
    });

    getContentPane().add(dcmExportPanel, java.awt.BorderLayout.CENTER);

    pack();
  }//GEN-END:initComponents

  
  /**
   * Called if a windowClosing event is received.
   */
  private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
    terminate();
  }//GEN-LAST:event_exitForm

  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.iftm.dcm4che.dcmie.exp.DcmExportPanel dcmExportPanel;
  // End of variables declaration//GEN-END:variables

  
}
