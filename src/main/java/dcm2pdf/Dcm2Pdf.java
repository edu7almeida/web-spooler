
package dcm2pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.image.BufferedImage;

//import org.dcm4che.imageio.plugins.dcm;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

 

/**
 * Data type that makes a conversion from a DICOM file format to PDF.
 * 
 * @author eduardo
 */

public class Dcm2Pdf {
    
    /** Path to the resulting PDF file. */
    public static final String PDF = "result.pdf";
    public static final String DCM = "image.dcm";
 
    /**
     * Creates a PDF file: hello.pdf
     * @param    args    no arguments needed
     */
    
    public static void main(String[] args)
    	throws DocumentException, IOException {
        
        boolean success = false;
        
    	success = new Dcm2Pdf().createPdf(PDF, DCM);
        System.out.println("Success = "+ success);
    }
 
    /**
     * Creates a PDF document.
     * @param pdfName the name to the new PDF document
     * @param dcmName the name of the dcm File
     * @return true on success
     * @throws    DocumentException 
     * @throws    IOException 
     */
    
    public boolean createPdf(String pdfName, String dcmName)
	throws DocumentException, IOException {
        
        /* setup the DICOM reader */
        ImageReader reader = ImageIO.getImageReadersByFormatName("DICOM").next();
        
        /* check image reader */
	if (reader == null)
            return false;
        
        //DicomImageReadParam readParams = (DicomImageReadParam) reader.getDefaultReadParam();
        
        File dcmFile = new File(dcmName);
        
        
        /* */
        ImageInputStream inStream = ImageIO.createImageInputStream(dcmFile);
        reader.setInput(inStream);
        
        int frameIndex = 0;
        
        
        /* read the specified frame from the file */
        BufferedImage bufferImage = reader.read(frameIndex, readParams);
        inStream.close();
        
        /* create a document */
        Document document = new Document();
        
        /* Get the document instance */
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfName));
        
        /* open the document */
        document.open();
        
        PdfContentByte pdfcb = new PdfContentByte(writer);
        
        Image image = Image.getInstance(pdfcb, bufferImage, 1);
        
        document.add(image);
        
        /* add data to the document */
        //document.add(new Paragraph("Hello World!"));
        
        /* close the document */
        document.close();
        return true;
    }
}