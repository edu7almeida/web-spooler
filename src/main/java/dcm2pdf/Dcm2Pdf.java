
package dcm2pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;


import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;

 



/**
 * Data type that makes a conversion from a DICOM file format to PDF.
 * 
 * @author eduardo
 */

public class Dcm2Pdf {
    
    /** Path to the resulting PDF file. */
    
    public static final String PDF = "result.pdf";
    
    /** Path to the origin DCM file. */
    
    public static final String DCM = "image.dcm";
    
    /** Factor of resize. */
    
    public static final float FACTOR =  0.5f;
 
    /**
     * Creates a PDF file
     * @param    args    no arguments
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
        
        
        int width, height, frameIndex = 0;
        
        /* setup the DICOM reader */
        ImageReader reader = ImageIO.getImageReadersByFormatName("DICOM").next();
        
        /* check image reader */
	if(reader == null)
            return false;
        
        /* read dicom image parameters */
        
        DicomImageReadParam readParams = (DicomImageReadParam) reader.getDefaultReadParam();
        File dcmFile = new File(dcmName);
        
        /* creation of a stream */
        
        ImageInputStream inStream = ImageIO.createImageInputStream(dcmFile);
        reader.setInput(inStream);
        
        
        /* read the dcmfile to a buffer */
        
        BufferedImage bufferImage = reader.read(frameIndex, readParams);
        
        width = (int) ((bufferImage.getWidth()) * FACTOR );
        height = (int) ((bufferImage.getHeight()) * FACTOR );
        System.out.println(width);
        System.out.println(height);
        
        /* resize */
        
        BufferedImage bi = resize(bufferImage, width, height);
        inStream.close();
        
        /* create a document */
        Document document = new Document();
        
        /* get the document instance */
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfName));
        
        /* open the document */
        document.open();
        
        /* process the image to the document */
        PdfContentByte pdfcb = new PdfContentByte(writer);
        Image image = Image.getInstance(pdfcb, bi, 1);
        document.add(image);
        
        /* close the document */
        document.close();
        return true;
    }
    
    /**
     * Resize the Image to the specific size.
     * 
     * @param img img to be resizable
     * @param width new width
     * @param height new height
     * @return image with the new size
     */
    
    
    public BufferedImage resize(BufferedImage img, int width, int height)
    {
        BufferedImage bi = new BufferedImage(width, height, img.getType());
        
        /* draw a  */
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, width, height, 0, 0, img.getWidth(), img.getHeight(), null);
        g.dispose();
        
        return bi;
    }
}