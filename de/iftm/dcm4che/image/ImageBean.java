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
package de.iftm.dcm4che.image;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import org.dcm4che.image.*;
import org.dcm4che.imageio.plugins.*;
import org.dcm4che.data.*;
import org.dcm4che.dict.*;

/**
 * This class implemnets a JPanel which displays a DICOM image.<br>
 * <br>
 * To do:<br>
 * In setInput(ImageInputStream iiStream, int frame) the ImageReader should be 
 * tested against instanceof DcmImageReader.<br>
 *
 * @author   Thomas Hacklaender
 * @version  2002.6.23
 */
public class ImageBean extends javax.swing.JPanel implements java.io.Serializable {

  
  private final String        VERSION = "0.9";

  
  public final static Color   BACKGROUND_DARK_BLUE = new Color(0, 0, 102);

  
  public final static Color   BACKGROUND_GRAY = new Color(204, 204, 204);
 
  
  private final Color         BORDER_ACTIVE_COLOR = new Color(255, 153, 0);

  
  /**
	 * Display image with original size.
	 */
	public final static int SIZE_POLICY_ORIGINAL = 0;

  
  /**
	 * Resize image that it fits into actual size of the panel.
	 */
	public final static int SIZE_POLICY_FIT = 1;

  
  /**
	 * Display image with original size and use scrollbars if necessary.
	 */
	public final static int SIZE_POLICY_SCROLL= 2;


	/**
	 * No mouse usage.
	 */
	public final static int MOUSE_POLICY_DISABLED = 0;


	/**
	 * Use mouse to change window levels.
	 */
	public final static int MOUSE_POLICY_WINDOW = 1;


	/**
	 * Use mouse to change image zoom.
	 */
	public final static int MOUSE_POLICY_ZOOM = 2;

  
  private Color                   imageBackground = BACKGROUND_GRAY;
	private Dataset                 theDataset  = null;
	private BufferedImage           origImage   = null;
	private BufferedImage           theImage    = null;
	private ImagePanelMemberClass   imagePanel  = null;
  private ColorModelFactory       cmFactory   = null;
	private ColorModelParam         cmParam     = null;
  private int                     numFrames   = 0;
  private int                     curFrame    = -1;


	/**
	 * Indicates, how the image should be displayed.
	 */
	private int             sizePolicy = SIZE_POLICY_ORIGINAL;


	/**
	 * Derived from photometricInterpretation: true, if it is possible (makes
	 * sense) to change the winow values of the image.
	 */
	private boolean					windowingPossible = false;


	/**
	 * Current window center of the image.
	 */
	private int							windowCenter;


	/**
	 * Current window width of the image.
	 */
	private int							windowWidth;


	/**
	 * Smallest possible window center value of the image.
	 */
	private int							minWindowCenter;


	/**
	 * Highest possible window center value of the image.
	 */
	private int							maxWindowCenter;


	/**
	 * Highest possible window width value of the image.
	 */
	private int							maxWindowWidth;


	/**
	 * Time of last Mouse Pressed Events.
	 */
	private long						lastWhen;


	/**
	 * Mouse x-coordinate at the time of last Mouse Pressed Event.
	 */
	private int							lastX;


	/**
	 * Mouse y-coordinate at the time of last Mouse Pressed Event.
	 */
	private int							lastY;


	/**
	 * Zoom factor. 1.0 = original image size; 0.0 = zoom to fit to size
	 * of bean.
	 */
	private double					zoom = 1.0;


	/**
	 * Sets the kind of mouse usage. Possible values are the final fields MOUSE_XXX.
	 */
	private int							mousePolicy = MOUSE_POLICY_DISABLED;


	/**
	 * Time in ms to ignore Mouse Dragged events after the last event.
	 */
	private int							blindTime = 50;


	/**
	 * Range in pixels in that mouse movement of one unit causes a change of one
	 * unit of the mouseDraggedAction (e.g. window, center, zoom).
	 */
	private int							sensitiveRange = 25;


	/**
	 * Multiplier for the mouse movement: mouse movement of one unit causes a change
	 * of mouseAcceleration units of the mouseDraggedAction (e.g. window, center, zoom).
	 */
	private int							mouseAcceleration = 5;

  
  /** Creates new form ImageBean */
  public ImageBean() {
    initComponents();
    postInitComponents();
    setDefaultBehavior();
  }


	/**
	 * Creates new ImageBean.
	 * @param f the input File
	 * @exception  IOException  in a case of I/O error
	 */
	public ImageBean(File f) throws IOException {
		this();
    setInput(f);
	}


	/**
	 * Creates new ImageBean.
	 * @param ds the datset
	 * @exception  IOException  in a case of I/O error
	 */
	public ImageBean(Dataset ds) throws IOException {
		this();
    setInput(ds);
	}


	/**
	 * Creates new ImageBean
	 * @param iis the ImageInputStream
	 * @exception  IOException  in a case of I/O error
	 */
	public ImageBean(ImageInputStream iis) throws IOException {
		this();
    setInput(iis);
	}
  
  
  private void setDefaultBehavior() {
    setSizePolicy(SIZE_POLICY_FIT);
    setMousePolicy(MOUSE_POLICY_WINDOW);
    setImageBackground(BACKGROUND_DARK_BLUE);
    setDefaultImage();
  }

  
  /**
   *
   */
  private void setDefaultImage() {
		Iterator          iter;
    ImageInputStream  iis;
    ImageReader       gr;

    iter = ImageIO.getImageReadersByFormatName("gif");
    gr = (javax.imageio.ImageReader) iter.next();
    try {
      iis = ImageIO.createImageInputStream(getClass().getResourceAsStream("resources/default.gif"));
      gr.setInput(iis, false);
      theImage = origImage = gr.read(0);
      iis.close();
      imageChanged();
    } catch (Exception e) {}
    
    windowingPossible = false;
  }  
  
  
  private void setDefaultWindow() {
    setWindow((minWindowCenter + maxWindowCenter) >> 1, maxWindowWidth);
  }


	/**
	 * Sets the input stream.
	 * @param iis the ImageInputStream.
	 */
	public void setInput(File f) throws IOException {
    setInput(f, 0);
  }


	/**
	 * Sets the input stream.
	 * @param ds the Dataset.
	 */
	public void setInput(Dataset ds) throws IOException {    
    setInput(ds, 0);
 	} 


	/**
	 * Sets the input stream.
	 * @param iiStream the ImageInputStream.
	 */
	public void setInput(ImageInputStream iiStream) throws IOException {    
    setInput(iiStream, 0);
 	} 


	/**
	 * Sets the input stream.
	 * @param iis the ImageInputStream.
	 */
	public void setInput(File f, int frame) throws IOException {
    ImageInputStream    iis;
    
    // Kein File angegeben
    if (f == null) {
      setInput((ImageInputStream) null);
      return;
    }
    
    // ImageInputStream oeffnen
    iis = ImageIO.createImageInputStream(f);
    
    // Bild einlesen
    setInput(iis);
    
    // Stream schliessen
    iis.close();

  }


	/**
	 * Sets the input stream.
	 * @param ds the Dataset.
   * @param frame the frame number to read.
	 */
	public void setInput(Dataset ds, int frame) throws IOException {
    ImageOutputStream   stream;
    
    // Datset in einen ImageOutoutStream schreiben
    stream = ImageIO.createImageOutputStream(new ByteArrayOutputStream());
    ds.writeDataset(stream, DcmEncodeParam.valueOf(UIDs.ImplicitVRLittleEndian));
    
    // ImageInputStream ist Superclasse von ImageOutputStream 
    // iOutpuStream wieder auf den Anfang setzen
    stream.seek(0);
    
    // Stream bearbeiten
    setInput(stream, frame);
    
    // Stream schliessen
    stream.close();
 	} 


	/**
	 * Sets the input stream.
	 * @param iiStream the ImageInputStream.
   * @param frame the frame number to read
	 */
	public void setInput(ImageInputStream iiStream, int frame) throws IOException {    
    String        pmi;
    int           bits;
    int           size;
    int           signed;
    int           min;
    int           max;
		Iterator      readers;
    ImageReader   dcmImageReader;

    if (frame < 0) return;
    
    // Wenn kein Input gesetzt, dann Default-Image darstellen
    if (iiStream == null) {
      setDefaultImage();
      return;
    }
        
    // Bild einlesen.
    
    // DcmImageReader holen
    readers = ImageIO.getImageReadersByFormatName("DICOM");
    while (true) {
      dcmImageReader = (ImageReader) readers.next();
      if (dcmImageReader == null) {
        throw new UnsupportedOperationException("No DcmImageReader found" + this);
      }
      // if (dcmImageReader instanceof DcmImageReader) {
      //   break;
      // }
      break;
    }

    cmFactory = ColorModelFactory.getInstance();
    
    dcmImageReader.setInput(iiStream, false);
		theDataset = ((DcmMetadata) dcmImageReader.getStreamMetadata()).getDataset();
		numFrames  = dcmImageReader.getNumImages(true);

    theImage = origImage = dcmImageReader.read(frame);
    
    // ImageInputStream wieder auf den Anfang setzen
    iiStream.seek(0);

    curFrame = frame;

    pmi = theDataset.getString(Tags.PhotometricInterpretation, null);
		if ("MONOCHROME1".equals(pmi) || "MONOCHROME2".equals(pmi)) {
      
      // Bild ist hat die PI MONOCHROME1 oder MONOCHROME2
      windowingPossible = true;
			cmParam = cmFactory.makeParam(theDataset);

      try {
        bits = theDataset.getInt(Tags.BitsStored, 8);
        size = 1 << bits;
        signed = theDataset.getInt(Tags.PixelRepresentation, 0);
        min = theDataset.getInt(Tags.SmallestImagePixelValue, signed == 0 ? 0 : -(size >> 1));
        max = theDataset.getInt(Tags.LargestImagePixelValue, signed == 0 ? size - 1 : (size >> 1) - 1);

        minWindowCenter = (int) cmParam.toMeasureValue(min);
        maxWindowCenter = (int) cmParam.toMeasureValue(max - 1);
        maxWindowWidth = maxWindowCenter - minWindowCenter;
      } catch (Exception e) {};
      
			if (cmParam.getNumberOfWindows() > 0) {
        setWindow((int) cmParam.getWindowCenter(0), (int) cmParam.getWindowWidth(0));
			} else {
        setDefaultWindow();
      }
      
      // Bild neu darstellen
      windowChanged();
      
    } else {
      
      // Bild ist hat eine andere PI als MONOCHROMEx
      windowingPossible = false;
      
      // Bild neu darstellen
      imageChanged();
    }

  }


	/**
   *
	 */
	private void imageChanged() {
    
    // Die Groesse des Panels in der ScrollPane auf die Groesse des Image festlegen
    // Achtung: Nicht die Groesse des ImageBean!!
    switch (sizePolicy) {
      
      case SIZE_POLICY_ORIGINAL:
        imagePanel.setPreferredSize(new Dimension(origImage.getWidth(), origImage.getHeight()));
        break;
      
      case SIZE_POLICY_FIT:
        imagePanel.setPreferredSize(new Dimension(0, 0));
        break;
      
      case SIZE_POLICY_SCROLL:
        imagePanel.setPreferredSize(new Dimension(theImage.getWidth(), theImage.getHeight()));
        break;
        
    }
    
    // Da sich die PreferredSize geaendert hat, muss die ScrollPane neu
    // gezeichnet werden.
    imagePanel.invalidate();
    scrollPane.validate();
    
    // Das Image neu zeichnen
    imagePanel.repaint();
  }
  

	/**
	 * Gets the currently displayed images.
	 * @return  the BufferedImage.
	 */
	public BufferedImage getImage() {
		return theImage;
	} 


	/**
	 * Indicates, how the image should be displayed.
	 * @param policy one of the constant values:.
	 */
	public void setSizePolicy(int policy) {
    sizePolicy = policy;
    
    if (sizePolicy == SIZE_POLICY_SCROLL) {
			// Scrollbars, wenn notwendig
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		} else {
			// Keine Scrollbars
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		} 
    
    // Bild unter neuer Policy darstellen
    imageChanged();

  }


	/**
	 * Indicates, how the image should be displayed.
	 * @param policy one of the constant values:.
	 */
	public int getSizePolicy() {
    return sizePolicy;
  }
  

	/**
	 * Gets the state of the switch windowingPossible.
	 * @return true, if windowing is supported.
	 */
	public boolean isWindowingPossible() {
    return windowingPossible;
  }


	/**
	 * Set a new center and width for the window.
	 * @param center the new center value.
	 * @param width the new width value.
	 */
	public void setWindow(int center, int width) {
    
    // Falls Windowing nicht unterstuetzt wird ist nichts zu tun.
    if (!windowingPossible) return;
    
		windowCenter = center;
		windowWidth = width;
    
    if (windowCenter < minWindowCenter) windowCenter = minWindowCenter;
    if (windowCenter > maxWindowCenter) windowCenter = maxWindowCenter;
    if (windowWidth < 1) windowWidth = 1;
    if (windowWidth > maxWindowWidth) windowWidth = maxWindowWidth;
    
    windowChanged();
  } 


	/**
   *
	 */
	private void windowChanged() {
    ColorModel  cm;
    
    // cmParam wird in setInput gesetzt
    if (cmParam == null) return;
    
		cmParam = cmParam.update(windowCenter, windowWidth, cmParam.isInverse());
		cm  = cmFactory.getColorModel(cmParam);
		theImage = new BufferedImage(cm, theImage.getRaster(), false, null);
    
    imageChanged();
	}


	/**
	 * Get the current center value of the window.
	 * @return The current center value.
	 */
	public int getWindowCenter() {
		return windowCenter;
	} 


	/**
	 * Get the current width value of the window.
	 * @return The current width value.
	 */
	public int getWindowWidth() {
		return windowWidth;
	} 


	/**
	 * Set a new zoom factor for the window.
	 * @param newZoom the zoom factor. Possible values are between 0.0625 and 4.0.
	 */
	public void setZoom(double newZoom) {

		if ((newZoom <= 0.125) | (newZoom > 4.0)) return;

    zoom = newZoom;
    zoomChanged();
	} 


	/**
	 * Gets the current zoom factor of the displayed image.
	 * @return The current zoom factor.
	 */
	public double getZoom() {
		return zoom;
	} 


	/**
	 * Description of the Method
	 *
	 * @since
	 */
	public void zoomChanged() {
    AffineTransformOp  op;

    // origImage wird in setInput gesetzt
    if (origImage == null) return;
    
		op = new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom),
                               AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		theImage = op.filter(origImage, op.createCompatibleDestImage(origImage, theImage.getColorModel()));
    
    imageChanged();
	}


	/**
	 * Sets the property imageBackground.
	 * @param enabled true, if scrollbars are enabled.
	 */
	public void setImageBackground(Color theColor) {
		imageBackground = theColor;
	}  


	/**
	 * Gets the property imageBackground.
	 * @return the current color of the image background.
	 */
	public Color getImageBackground() {
    return imageBackground;
  }


	/**
	 * Sets the kind of mouse usage. Possible values are the final fields MOUSE_XXX.
	 * The default value is MOUSE_DISABLED.
	 * @param usage The kind of mouse usage.
	 */
	public void setMousePolicy(int usage) {
		mousePolicy = usage;
	} 


	/**
	 * Sets the kind of mouse usage. Possible values are the final fields MOUSE_XXX.
	 * @param usage The kind of mouse usage.
	 */
	public int getMousePolicy() {
		return mousePolicy;
	} 


	/**
	 * Sets the time in ms to ignore Mouse Dragged events after the last event.
	 * Default value is 50 ms.
	 * @param usage The time in ms.
	 */
	public void setBlindTime(int blindTime) {
		this.blindTime = blindTime;
	} 


	/**
	 * Gets the time in ms to ignore Mouse Dragged events after the last event.
	 * @param usage The time in ms.
	 */
	public int getBlindTime() {
		return blindTime;
	} 


	/**
	 * Sets the range in pixels in that mouse movement of one unit causes a change
	 * of one unit of the mouseDraggedAction (e.g. window, center, zoom).
	 * Default value is 25.
	 * @param usage The range in pixel.
	 */
	public void setSensitiveRage(int sensitiveRage) {
		this.sensitiveRange = sensitiveRage;
	} 


	/**
	 * Sets the range in pixels in that mouse movement of one unit causes a change
	 * of one unit of the mouseDraggedAction (e.g. window, center, zoom).
	 * @param usage The range in pixel.
	 */
	public int getSensitiveRage() {
		return sensitiveRange;
	} 


	/**
	 * Sets the multiplier for the mouse movement: mouse movement of one unit
	 * causes a change of mouseAcceleration units of the mouseDraggedAction.
	 * Default value is 5.
	 * @param usage The multiplier.
	 */
	public void setMouseAcceleration(int mouseAcceleration) {
		this.mouseAcceleration = mouseAcceleration;
	} 


	/**
	 * Gets the multiplier for the mouse movement: mouse movement of one unit
	 * causes a change of mouseAcceleration units of the mouseDraggedAction.
	 * @param usage The multiplier.
	 */
	public int getMouseAcceleration() {
		return mouseAcceleration;
	} 


	/**
	 * Performe the mouse action.
	 */
	private void mouseDraggedAction(int deltaX, int deltaY) {

		switch (mousePolicy) {

		case MOUSE_POLICY_DISABLED:
			break;

		case MOUSE_POLICY_WINDOW:
			// Neue Window-Werte berechnen und Bild mit aktuellen Werten darstellen.
			setWindow(windowCenter + scaleMove4Window(deltaY), windowWidth + scaleMove4Window(deltaX));
			break;

		case MOUSE_POLICY_ZOOM:
			// Neue Zoom-Wert berechnen und Bild mit aktuellem Wert darstellen.
			setZoom(zoom + (double) deltaY / 100.0);
			break;
		}
	} 
  
	/**
   * Innerhald des sensitiveRange bewirkte die Bewegung der Mouse um 1 Pixel
	 * eine Aenderung um eine Einheit. Ausserhalb des Bereiches ist
	 * die Aenderung das 'mouseAcceleration'-fache der Mausbewegung.
   */
  private int scaleMove4Window(int distance) {
		if (Math.abs(distance) > sensitiveRange) {
			return distance * mouseAcceleration;
		} else {
      return distance;
    }
  }
  
  
	/**
	 * This method is called after the call of initComponents.
	 *
	 * @since
	 */
	private void postInitComponents() {
    
    // Rahmen zunaechst unsichtbar, da seine Farbe gleich der Background-Farge
    this.setBorder(new javax.swing.border.LineBorder(imageBackground, 2));
    
    // Das Panel mit dem Image der ScrollPane hinzufuegen. Falls das Panel
    // keiner ist, als die ScrollPane vom Layout-Manager gezeichnet wurde,
    // wird es in dr linken oberen Ecke dargestellt. Dieses Verhalten kann 
    // n i c h t  durch Einstellungen der ScrollPane veraendert werden!
		imagePanel = new ImagePanelMemberClass();
		scrollPane.setViewportView(imagePanel);

    // IamgePanel als Listener fuer Mouse-Events registrieren.
    imagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        imagePanelMousePressed(evt);
      }
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        imagePanelMouseReleased(evt);
      }
    });
    imagePanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseDragged(java.awt.event.MouseEvent evt) {
        imagePanelMouseDragged(evt);
      }
    });
    
	}


	/**
	 * Eventhandler fuer Mouse Dragged Events (Name von JBuielder generiert).
	 * Falls die Einstellungen fuer das Bild veraenderbar sind:
	 * Solange die Mousetaste gedrueckt bleibt, werden Center und Window des
	 * Bildes ueber die Mousebewegung geaendert. Window = links/rechts,
	 * Center = oben/unten.
	 * @param	evt		Das Event.
	 */
  private void imagePanelMouseDragged(java.awt.event.MouseEvent evt) {
		int		actX, actY;
		long	actWhen;

		// Ignoriert Events waehrend der Zeitspann blindTime (in ms) nach der
		// Bearbeitung des letzten Events
		actWhen = evt.getWhen();
		actX = evt.getX();
		actY = evt.getY();
    
		if ((int) (actWhen - lastWhen) < blindTime) return;

		mouseDraggedAction(actX - lastX, lastY - actY);

		// Aktuelle Werte fuer den naechsten Aufruf als 'alte Werte' speicherm.
		lastX = actX;
		lastY = actY;
		lastWhen = actWhen;
  }

  
  /**
	 * Eventhandler fuer Mouse Pressed Events.
	 * Falls die Einstellungen fuer das Bild veraenderbar sind:
	 * Statt des grauen Rahmens wird ein gruener um das Bild gezeichnet.
	 * @param	evt		Das Event.
	 */
  private void imagePanelMousePressed(java.awt.event.MouseEvent evt) {
    
    // Mouse daktiviert
    if (mousePolicy == MOUSE_POLICY_DISABLED) return;
    
    lastWhen = evt.getWhen();
		lastX = evt.getX();
		lastY = evt.getY();

    setBorder(new javax.swing.border.LineBorder(BORDER_ACTIVE_COLOR, 2));
		// this.repaint();
  }


	/**
	 * Eventhandler fuer Mouse Released Events.
	 * Falls die Einstellungen fuer das Bild veraenderbar sind:
	 * Es wird ein grauer Rahmen um das Bild gezeichnet.
	 * @param	evt		Das Event.
	 */
  private void imagePanelMouseReleased(java.awt.event.MouseEvent evt) {
    
    // Mouse daktiviert
    if (mousePolicy == MOUSE_POLICY_DISABLED) return;
    
    setBorder(new javax.swing.border.LineBorder(imageBackground, 2));
		// this.setBorder(grayBorder);
		// this.repaint();
  }

  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
    scrollPane = new javax.swing.JScrollPane();

    setLayout(new java.awt.BorderLayout());

    scrollPane.setBorder(null);
    add(scrollPane, java.awt.BorderLayout.CENTER);

  }//GEN-END:initComponents
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane scrollPane;
  // End of variables declaration//GEN-END:variables


  /**
   * Description of the Class
   *
   * @author   Thomas Hacklaender
   * @version  2002.5.24
	 */
	class ImagePanelMemberClass extends JPanel {

		/**
		 * Paints the image theImage onto onto the panel. The actual size of the
     * panel is set by the ImagePanel class and depends (a) on the size-policy 
     * of ImagePanel and (b) on the size of the ScrollPane (set by the layout-
     * manager) to which this panel was added. This method paints the image
     * centered on the panel.
		 * @param  g  Description of Parameter
		 * @since
		 */
		public void paint(Graphics g) {
      double  scale, wScale, hScale;
      int     w, h;
      
      // Noch kein Bild generiert
      if (theImage == null) return;
      
      // Hintergrund setzen
      g.setColor(imageBackground);
      g.fillRect(0, 0, getWidth(), getHeight());
      
      if (sizePolicy == SIZE_POLICY_FIT) {
        
        // Bild auf aktuelle Groesse des Panels skalieren
        wScale = (double) getWidth() / theImage.getWidth();
        hScale = (double) getHeight() / theImage.getHeight();
        if (wScale < hScale) {
          scale = wScale;
        } else {
          scale = hScale;
        }
        w = (int) (scale * theImage.getWidth());
        h = (int) (scale * theImage.getHeight());
        g.drawImage(theImage, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        
      } else {
        
        // Bild in Originalgroesse darstellen
        g.drawImage(theImage, 0, 0, null);
        
      }
		}

	}

}
