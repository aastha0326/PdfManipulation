package pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.kernel.pdf.filters.FlateDecodeFilter;

public class decode {
	
	public static void main(String[] args) throws IOException {
		
		
		//String SRC = "D:\\Experiments\\Encryption\\samplePDFoutput.pdf";
		//String SRC = "D:\\Books\\Sample PDFs\\sample.pdf";
		//String SRC = "D:\\Books\\Sample PDFs\\Encryption.pdf";
		//String SRC = "C:\\Users\\souvik\\Documents\\PDF signed 1\\Sample PDF 3[Signed][LTV].pdf";
		String SRC = "D:\\firstPDF1.pdf";
		String DEST = "D:\\output.txt";
		
		//String SRC = "D:\\Revision6PDFEncrypted_syncfusion_password.pdf";

		
		//String SRC ="D:\\firstPDF-formfield - Copy.pdf"; 
				
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC,new ReaderProperties().setPassword("World".getBytes()))); //for samplePDFoutput.pdf
		//PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC)); //for any pdf with no user/owner password
        PdfObject obj;
        FileOutputStream fos = new FileOutputStream(DEST);
        
        for (int i = 1; i <= pdfDoc.getNumberOfPdfObjects(); i++) {
            obj = pdfDoc.getPdfObject(i);
            
            //System.out.println(i + " "+ pdfDoc.getPdfObject(i)+"\n");
            
            if(obj != null && obj.isDictionary()) {
            	System.out.println(obj.getIndirectReference());
            	System.out.print(obj);
            	System.out.println();
            }
            	
            if (obj != null && obj.isStream()) {
                byte[] b;
                try {
                    b = ((PdfStream) obj).getBytes();
                } catch (PdfException exc) {
                    b = ((PdfStream) obj).getBytes(false);
                }
                
                //System.out.println(b.length);
                
                System.out.println(obj.getIndirectReference());
                System.out.print(new String(b,StandardCharsets.UTF_8));
                /*BigInteger no = new BigInteger(1, b);
                String decodedString = no.toString(16);
                System.out.println(decodedString);*/
                
                System.out.println();
                
                //fos.write(b);
                //fos.write(FlateDecodeFilter.flateDecode(b, false)); 
            }
        }
        pdfDoc.close();
	}
	
	
}
