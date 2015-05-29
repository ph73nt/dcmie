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
import java.net.*;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;

import de.iftm.dcm4che.image.*;
import de.iftm.dcm4che.dcmie.*;
import de.iftm.javax.swing.*;
import de.iftm.javax.swing.filetree.*;
import de.iftm.java.util.*;


/**
 * @author   Thomas Hacklaender
 * @version  2002.5.30
 */
public class XMLPanel extends javax.swing.JPanel implements FileSelectionListener, java.io.Serializable {

  /**
   * File in the last FileSelectionEvent
   */
  private File              lastSelectedFile = null;
  
  private TagDictionary     dict = null;
  
  private boolean           isXSLT = false;
  private Source            xsltSource = null;

  
  /**
   * Creates new form DirPanel
   */
  public XMLPanel() {
    initComponents();
  }
  
  
  /**
   * Set up the Panel
   */
  public void setUp(DcmieParam dcmieParam) {
    
    dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
  }
  

  /**
   * Implementation of the FileSelectionListener interface.
   * @param e the FileSelection event.
   */
  public void fileSelected(FileSelectionEvent e) {
    lastSelectedFile = e.getLastSelectedFile();
  }

  
  /**
   *
   */
  public void refresh() {
    DataInputStream           dicomStream = null;
    ByteArrayOutputStream     xmlStream = null;
    Source                    source = null;
    Result                    result = null;
    ByteArrayOutputStream     resultStream;
    DcmParser                 parser;
    Transformer               transformer;
    
    if (lastSelectedFile == null) return;

    try {
      FileInputStream fis = new FileInputStream(lastSelectedFile);
      BufferedInputStream bis = new BufferedInputStream(fis);
      dicomStream = new DataInputStream(bis);
      xmlStream = new ByteArrayOutputStream();

      try {
        
        // Eine konkrete Instance eines DcmParser generieren
        parser = DcmParserFactory.getInstance().newDcmParser(dicomStream);
        
        // Der DcmParser leitet aus dem SAX-ContentHandler einen DcmHandlerAdapter
        // ab. Dieser hat den Aufbau eines ContentHandlers. Die Methoden werden
        // aber nicht von einem SAX-Parser aufgerufen, sondern von der Methode
        // DcmParser.doParse(). Damit verhaelt sich der DcmParser im XML-Umfeld
        // wie ein Parser, der ein XML-Dokument parst.
        // getTransformerHandler(out) liefert den eigentlichen ContentHandler
        // dict gibt das zu verwendende Dictionary vor
        parser.setSAXHandler(getTransformerHandler(xmlStream), dict);

        // Parst den DICOM File: Da FileFormat = null , wird jeder File bearbeitet
        // stopTag ist Tags.PixelData. Pixel Daten werden nicht mehr eingeschlossen
        parser.parseDcmFile(null, Tags.PixelData);

        // Das Ergebnis des Parsens ist der ByteArrayOutputStream out

      } catch (TransformerException transEx) {
        textArea.setText("**** Parsing error" + transEx.getMessage());
      }

      try {
        
        if (isXSLT) {

          // Zunaechst Source, Result und Transformer festlegen
          source = new StreamSource(new ByteArrayInputStream(xmlStream.toByteArray()));
          resultStream = new ByteArrayOutputStream();
          result = new StreamResult(resultStream);
          transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
    
          // Set an output property that will be in effect for the transformation:

          // indent specifies whether the Transformer may add additional whitespace
          // when outputting the result tree; the value must be yes or no. 
          // transformer.setOutputProperty(OutputKeys.INDENT,"yes");

          // The method attribute identifies the overall method that should be used
          // for outputting the result tree; the value must be "xml" or "html" or
          // "text" or expanded name.
          // transformer.setOutputProperty(OutputKeys.METHOD,"xml");
          
          // Mit XSLT transformieren
          transformer.transform(source, result);
          
          // In TextArea ausgeben
          textArea.setText(resultStream.toString());
          
        } else {
          
          // Keine Umwandlung mit XSLT: In TextArea ausgeben
          textArea.setText(xmlStream.toString());
          
        }

      } catch (TransformerException transEx) {
        textArea.setText("**** XSLT transformation error: " + transEx.getMessage());
      }
                    
      // Stream schliessen
      dicomStream.close();
      fis.close();
      
    } catch (IOException ioEx) {
      textArea.setText("*** Warning: Can't open file " + lastSelectedFile.toString());
      return;
      
    } finally {
      try { dicomStream.close(); } catch (IOException ignore) {}
      try { xmlStream.close(); } catch (IOException ignore) {}
    }

  }        

  
  /**
   * This method returns a non-validating ContentHandler. It converts the
   * XML input into a well-formed XML document in the outStream. Each element
   * starts in a new line. 
   * A TransformerHandler listens for SAX ContentHandler parse events and
   * transforms them to a Result. [javax.xml.transformer.sax.TransformerHandler 
   * extends org.xml.sax.ContentHandler]
   */
  private TransformerHandler getTransformerHandler(OutputStream xmlStream) throws TransformerConfigurationException {
    SAXTransformerFactory     saxTF;
    TransformerHandler        transformerHandler;
    Transformer               transformer;
    Source                    xsltSource = null;
    Result                    result;

    // Ergebnis in den Stream outStream schreiben
    result = new StreamResult(xmlStream);
    
    // Eine konkrete Instance einer SAXTransformerFactory erzeugen
    // [javax.xml.transform.TransformerFactory]
    // [javax.xml.transform.sax.SAXTransformerFactory]
    saxTF = (SAXTransformerFactory) TransformerFactory.newInstance();
    
    if (xsltSource == null) {
      // Get a TransformerHandler object that can process SAX ContentHandler
      // events into a Result. The transformation is defined as an identity
      // (or copy) transformation, for example to copy a series of SAX parse
      // events into a DOM tree. [javax.xml.transform.sax.SAXTransformerFactory]
      transformerHandler = saxTF.newTransformerHandler();
    } else {
      // Get a TransformerHandler object that can process SAX ContentHandler
      // events into a Result, based on the transformation instructions
      // specified by the argument, e.g. a XSLT styleshet.
      // [javax.xml.transform.sax.SAXTransformerFactory]
      transformerHandler = saxTF.newTransformerHandler(xsltSource);
    }
    
    // Der transformerHandler ist eine Instanz der Default-Implementation
    // des org.xml.sax.ContentHandler

    // Get the Transformer associated with this handler, which is needed in
    // order to set parameters and output properties.
    // [javax.xml.transform.Transformer]
    transformer = transformerHandler.getTransformer();
    
    // Set an output property that will be in effect for the transformation:
    
    // indent specifies whether the Transformer may add additional whitespace
    // when outputting the result tree; the value must be yes or no. 
    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
    
    // The method attribute identifies the overall method that should be used
    // for outputting the result tree; the value must be "xml" or "html" or
    // "text" or expanded name.
    transformer.setOutputProperty(OutputKeys.METHOD,"xml");
    
    // Enables the user of the TransformerHandler to set the to set the result
    // Result result) for the transformation.
    transformerHandler.setResult(result);
    
    return transformerHandler;
  }


	/**
	 * Erlaubt die Speicherung des aktuell auf dem Panel ausgewaehlten
	 * Textes in einer Datei. Dazu wird dem Anwender ein Swing
	 * <code>JFileChooser</code>.
	 * praesentiert.
	 */
	public void save() {
		File							f;
		DataOutputStream	out;
		String						fName = "XML_View_Dump";;

		f = FileChooser.saveFile("", null, fName);
		if (f == null) return;

    try {
			out = new DataOutputStream(new FileOutputStream(f));
			out.writeBytes(textArea.getText());
			out.close();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    xsltGroup = new javax.swing.ButtonGroup();
    internalGroup = new javax.swing.ButtonGroup();
    refreshButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();
    scroll = new javax.swing.JScrollPane();
    textArea = new javax.swing.JTextArea();
    noXSLTBtn = new javax.swing.JRadioButton();
    internalXSLTBtn = new javax.swing.JRadioButton();
    fileXSLTBtn = new javax.swing.JRadioButton();
    openXSLTBtn = new javax.swing.JButton();
    fileTextField = new javax.swing.JTextField();
    internalHTMLBtn = new javax.swing.JRadioButton();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(256, 64));
    setPreferredSize(new java.awt.Dimension(0, 0));
    refreshButton.setText("Refresh");
    refreshButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        refreshButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(refreshButton, gridBagConstraints);

    saveButton.setText("Save...");
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
    add(saveButton, gridBagConstraints);

    textArea.setFont(new java.awt.Font("Monospaced", 0, 12));
    scroll.setViewportView(textArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
    add(scroll, gridBagConstraints);

    noXSLTBtn.setSelected(true);
    noXSLTBtn.setText("No XSLT");
    xsltGroup.add(noXSLTBtn);
    noXSLTBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        noXSLTBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 0);
    add(noXSLTBtn, gridBagConstraints);

    internalXSLTBtn.setText("Use build in XSLT");
    xsltGroup.add(internalXSLTBtn);
    internalXSLTBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        internalXSLTBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(internalXSLTBtn, gridBagConstraints);

    fileXSLTBtn.setText("Use XSLT File");
    xsltGroup.add(fileXSLTBtn);
    fileXSLTBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fileXSLTBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(fileXSLTBtn, gridBagConstraints);

    openXSLTBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/iftm/dcm4che/dcmie/inspect/resources/open.gif")));
    openXSLTBtn.setMaximumSize(new java.awt.Dimension(40, 23));
    openXSLTBtn.setMinimumSize(new java.awt.Dimension(40, 23));
    openXSLTBtn.setPreferredSize(new java.awt.Dimension(40, 23));
    openXSLTBtn.setEnabled(false);
    openXSLTBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openXSLTBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(openXSLTBtn, gridBagConstraints);

    fileTextField.setMinimumSize(new java.awt.Dimension(128, 23));
    fileTextField.setPreferredSize(new java.awt.Dimension(256, 23));
    fileTextField.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(fileTextField, gridBagConstraints);

    internalHTMLBtn.setSelected(true);
    internalHTMLBtn.setText("Metadata to HTML");
    internalHTMLBtn.setToolTipText("null");
    internalGroup.add(internalHTMLBtn);
    internalHTMLBtn.setEnabled(false);
    internalHTMLBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        internalHTMLBtnActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(internalHTMLBtn, gridBagConstraints);

  }//GEN-END:initComponents

  private void internalHTMLBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internalHTMLBtnActionPerformed
    // Add your handling code here:
    InputStream   inpStream;
    
    inpStream = this.getClass().getResourceAsStream("resources/SimpleDcmMetadata.xsl");
    
    // XSLT Resource als Transformation-Source speichern
    xsltSource = new StreamSource(inpStream);
  }//GEN-LAST:event_internalHTMLBtnActionPerformed

  
  private void openXSLTBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openXSLTBtnActionPerformed
    // Add your handling code here:
    File    f;
    
    f = FileChooser.openFile("Select a DICOM-Directory file", null);
    if (f == null) return;
    
    // Textfeld updaten
    fileTextField.setText(f.toString());
    
    // XSLT File als Transformation-Source speichern
    xsltSource = new StreamSource(f);
  }//GEN-LAST:event_openXSLTBtnActionPerformed

  
  private void fileXSLTBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileXSLTBtnActionPerformed
    // Add your handling code here:
    File    f;
    
    isXSLT = true;
    openXSLTBtn.setEnabled(true);
    internalHTMLBtn.setEnabled(false);
    
    // XSLT File aus TextField holen und als Transformation-Source speichern
    xsltSource = new StreamSource(new File(fileTextField.getText()));
  }//GEN-LAST:event_fileXSLTBtnActionPerformed

  
  private void internalXSLTBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internalXSLTBtnActionPerformed
    // Add your handling code here:
    InputStream   inpStream;
    
    isXSLT = true;
    openXSLTBtn.setEnabled(false);
    internalHTMLBtn.setEnabled(true);
    
    if (internalHTMLBtn.isSelected()) {
      inpStream = this.getClass().getResourceAsStream("resources/MetadataToHTML.xsl");
    
      // XSLT Resource als Transformation-Source speichern
      xsltSource = new StreamSource(inpStream);
    }
  }//GEN-LAST:event_internalXSLTBtnActionPerformed

  
  private void noXSLTBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noXSLTBtnActionPerformed
    // Add your handling code here:
    isXSLT = false;
    openXSLTBtn.setEnabled(false);
    internalHTMLBtn.setEnabled(false);
  }//GEN-LAST:event_noXSLTBtnActionPerformed

  
  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    // Add your handling code here:
    save();
  }//GEN-LAST:event_saveButtonActionPerformed

  
  private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
    // Add your handling code here:
    refresh();
  }//GEN-LAST:event_refreshButtonActionPerformed
  

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButton internalXSLTBtn;
  private javax.swing.ButtonGroup xsltGroup;
  private javax.swing.JRadioButton noXSLTBtn;
  private javax.swing.JTextField fileTextField;
  private javax.swing.JButton refreshButton;
  private javax.swing.JRadioButton fileXSLTBtn;
  private javax.swing.JTextArea textArea;
  private javax.swing.JRadioButton internalHTMLBtn;
  private javax.swing.JButton openXSLTBtn;
  private javax.swing.JButton saveButton;
  private javax.swing.JScrollPane scroll;
  private javax.swing.ButtonGroup internalGroup;
  // End of variables declaration//GEN-END:variables
  
}
