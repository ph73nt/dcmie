# dcmie
This package contains a set of plugins for the ImageJ framework (http://rsb.info.nih.gov/ij/) to import and export DICOM images.

* Dcm_Import: This plugin imports DICOM compatible images to ImageJ. 
*	Dcm_Export: This plugin exports all ImagesJ images (besides 32-bit float) as DICOM Secondary Capture images.
*	Dcm_PropertyLister: This plugin lists the properties assigned to an ImageJ image.
*	Dcm_Inspector: This is a plugin and a stand-alone program. Its purpose is to look inside a DICOM image. It also allows to convert a DICOM image to a XML representation and to process that by a XSL processor. 
In contrast to the ImageJ  build-in DICOM functionality it is possible to read and write to the local filesystem and to DICOM file-sets, i.e. a DICOMDIR. That means, that for example DICOM CD’s may be read and written. As an option the imported images may contain all the DICOM metadata either as Java property-strings and/or binary data. These metadata can be used by other plugins for image calculation.  

The package was developed under the GNU General Public License. Library parts are  under the  GNU Lesser General Public License. See the source code for details.

The package is based on the dcm4che DICOM library which includes network and media functions also. It was developed by Gunter Zeilinger. The homepage of this project is [http://sourceforge.net/projects/dcm4che/](http://sourceforge.net/projects/dcm4che/) .  
