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
package de.iftm.dcm4che.dcmie;


import java.io.*;
import java.util.*;
import java.net.*;

import org.dcm4che.data.*;

import de.iftm.ij.plugins.dcmie.*;


/**
 * This class is used to communicate parameter between the the property file,  
 * the Dcm_XXX-plugin's and the GUI elements.<br>
 *
 * @author   Thomas Hacklaender
 * @version  2002.08.21
 */
public class DcmieParam {
  
  ////////////////////////////////////////////////////////////////////////
  //
  // Import Properties set in the Property-File
  //
  ////////////////////////////////////////////////////////////////////////

	/**
	 * Property: dcmie.import.file = String <br>
	 * The <file-uri> of the source file or directory for import operations ("./" 
   * corresponds to "user.dir")
   * Defaultvalue : "user.dir".
	 */
	public File                 importFile = uriToFile("./");


	/**
	 * Property: dcmie.import.filesystem <br>
   * If the filesystem should be the default source for import operations
   * the value is true. If source should be a DICOMDIR the value is false.
	 */
	public boolean              isImportFilesystem = true;

  
  /**
   * Property: dcmie.import.ij.mode <br>
   * If the DcmImportPanel should display "dcmie.import.ij.*" properties the
   * value is true.
   */
	public boolean              ijMode = true;

  
	/**
	 * Property:  dcmie.import.ij.image.show <br>
   * Only valid if "dcmie.import.ij.mode = true". <br>
	 * If the imported ImagePlus should be displayed the value is true.<br>
	 */
	public boolean              isImageShow = true;


	/**
	 * Property: dcmie.import.ij.metadata.binary<br>
   * Only valid if "dcmie.import.ij.mode = true". <br>
   * If metadata should be appended as Binary properties the ImagePlus the value 
   * is true.
	 */
	public boolean              isMetadataBinary = false;


	/**
	 * Property: dcmie.import.ij.metadata.onlyfirst<br>
   * Only valid if "dcmie.import.ij.mode = true". <br>
	 * True, if only the metadata of the first image of a stack should be included.
	 */
	public boolean              isMetadataOnlyFirst = false;


	/**
	 * Property: dcmie.import.ij.metadata.string<br>
   * Only valid if "dcmie.import.ij.mode = true". <br>
	 * If metadata should be appended as String properties the ImagePlus the value 
   * is true.
	 */
	public boolean              isMetadataString = false;

  
  ////////////////////////////////////////////////////////////////////////
  //
  // Export Properties set in the Property-File
  //
  ////////////////////////////////////////////////////////////////////////

	/**
	 * Property: dcmie.export.file = String <br>
	 * The <file-uri> of the destination file or directory for export operations 
   * ("./" corresponds to "user.dir")
   * Defaultvalue : "user.dir".
   */
  public File                 exportFile = uriToFile("./");

  
  /**
	 * Property: dcmie.export.filesystem <br>
   * If the filesystem should be the default destination for export operations
   * the value is true. If destination should be a DICOMDIR the value is false.
   */
  public boolean              isExportFilesystem = true;

  
  /**
   * Property: dcmie.export.metadata.general <br>
	 * The <file-uri> of a property file containing the string-representation of a
   * Dataset describing general metadata valid for all images to export.
   */
  public Dataset              generalMetadataDataset = null;


	/**
   * Property: dcmie.export.metadata.useimage<br>
	 * True, if the writer should include the imageMetadataArray. In this case the 
   * infoMetadataDataset overwrites the corresponding DcmElements in all Datasets 
   * of the imageMetadataArray.
	 */
	public boolean              isUseImageMetadata = false;
  
  
  /**
   * Property: dcmie.export.metadata.mask<br>
   * A Dataset including a mask: If != null, only attributes defined in this 
   * Dataset will be included to the metadata of the exported image. All metadata 
   * necessary to construct the Image Information Entity need not to be declared 
   * in this Dataset.
   */
  public Dataset              maskMetadataDataset = null;

  
  ////////////////////////////////////////////////////////////////////////
  //
  // Constructor, Methods
  //
  ////////////////////////////////////////////////////////////////////////

  /**
   * Constructor. If InputStream is null the default value were choosen.
   * @param ins the InputStream of property file to use
   */
  public DcmieParam(InputStream ins) {
    if (ins != null) {
      getProperties(ins);
    }
  }


	/**
	 * Read a given property file and set the fields accordingly.
   * @param the stream of the property file.
	 */
	public void getProperties(InputStream ins) {
		Properties	prop = new Properties();
		String			s;
		File				f;

		try {
			prop.load(ins);
		} catch (Exception e) {
      return;
    }

    s = prop.getProperty("dcmie.export.file");
    if (s != null) {
      f = uriToFile(s);
      exportFile = f;
    } 

    s = prop.getProperty("dcmie.export.filesystem");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isExportFilesystem = true;
      } else {
        isExportFilesystem = false;
      } 
    } 

    s = prop.getProperty("dcmie.export.metadata.general");
    if (s != null) {
      f = uriToFile(s);
      generalMetadataDataset = fileToDataset(f);
    } 

    s = prop.getProperty("dcmie.export.metadata.mask");
    if (s != null) {
      f = uriToFile(s);
      maskMetadataDataset = fileToDataset(f);
    } 

    s = prop.getProperty("dcmie.export.metadata.useimage");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isUseImageMetadata = true;
      } else {
        isUseImageMetadata = false;
      } 
    } 

    s = prop.getProperty("dcmie.import.file");
    if (s != null) {
      f = uriToFile(s);
      importFile = f;
    } 

    s = prop.getProperty("dcmie.import.filesystem");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isImportFilesystem = true;
      } else {
        isImportFilesystem = false;
      } 
    } 

    s = prop.getProperty("dcmie.import.image.show");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isImageShow = true;
      } else {
        isImageShow = false;
      }
    }

    s = prop.getProperty("dcmie.import.ij.metadata.binary");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isMetadataBinary = true;
      } else {
        isMetadataBinary = false;
      } 
    } 

    s = prop.getProperty("dcmie.import.ij.mode");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        ijMode = true;
      } else {
        ijMode = false;
      } 
    } 

    s = prop.getProperty("dcmie.import.ij.metadata.onlyfirst");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isMetadataOnlyFirst = true;
      } else {
        isMetadataOnlyFirst = false;
      } 
    } 

    s = prop.getProperty("dcmie.import.ij.metadata.string");
    if (s != null) {
      if (s.toLowerCase().charAt(0) == 't') {
        isMetadataString = true;
      } else {
        isMetadataString = false;
      } 
    } 
    
	} 

  
  /**
   * Create a File from an URI.
   * <file-uri>    Describes a file in a operating-system independend way. See the
   *               API-Doc of the URI class. For Windows-OS the absolute URI
   *               "file:/c:/user/tom/foo.txt" describes the file "C:\\user\\tom\\foo.txt". 
   *               Relative URI's, e.g. without the "file:" schema-prefix, are
   *               relativ to the user-directory, given by the system property
   *               user.dir. For example: If the user.dir is "C:\\user\\tom\\" and 
   *               the relative URI is "/abc/foo.txt" the referenced file is
   *               "C:\\user\\tom\\abc\\foo.txt". The abbreviations "." for the current
   *               and ".." for the upper directory are valid to form a relative URI.
   * @param uriString the string-description of an absolute or relative URI.
   * @return the file which is described by the uriString. Returns null, if uriString
   *         is null or "". Returns null also, if a conversion error occures.
   */
  public static File uriToFile(String uriString) {
    URI   baseURI;
    URI   uri;
    
    if (uriString == null) return null;
    if (uriString.equals("")) return null;
    
    try {
      uri = new URI(uriString);
      // Redundante Elemente entfernen:
      // Auakommentiert, weil eine URI der  Form "./a.b" (nicht "./a/b.c") zu
      // einer ArrayIndexOutOfBoundsException fuehrt. Grund unklar. Interner Fehler?
      // uri = uri.normalize();
      // Absolute URI's sind von der Form file://de.iftm/abc/def/g.txt,
      // relative besitzen kein "schma" dh. im Beispiel "file://" fehlt.
      if (!uri.isAbsolute()) {
        // Relative URI's werden auf das user.dir bezogen.
        baseURI = (new File(System.getProperty("user.dir"))).toURI();
        uri = baseURI.resolve(uri);
      }
      return new File(uri);
    } catch (Exception e) {
      return null;
    }
  }

  
  /**
   * Converts a string-properties file to a Dataset.
   * @param f the File containig the Dataset as string-properties.
   * @return the Dataset.
   */
  private Dataset fileToDataset(File f) {
    Properties  metadataProperties;
    Dataset     ds = null;
    
    // Wenn File nicht definiert ist das Dataset null
    if (f == null) return null;
    
    try {
      // Properties einlesen
      FileInputStream propertiesFIS = new FileInputStream(f);
      metadataProperties = new Properties();
      metadataProperties.load(propertiesFIS);
      propertiesFIS.close();
      
      // Properties in ein Dataset konvertieren
      ds = DcmiePropertiesUtil.propertiesToDataset(metadataProperties, 1, true)[0];
      
    } catch (Exception e) {
      //  Bei Fehler waehrend der Konvertierung abbrechen
      System.err.println("*** Error: Syntax error in metadata-properties file: " + f.getPath());
    }
    
    return ds;
  }
  
}

