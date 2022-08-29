package pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfEncryption;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.annot.PdfStampAnnotation;
import com.itextpdf.layout.Document;
import com.itextpdf.signatures.EncryptionAlgorithms;

public class encdec {
	
	public static void main(String [] args)throws IOException{
		
		//String SRC = "D:\\Experiments\\Encryption\\samplePDF1.pdf";
		//String DEST = "D:\\Experiments\\Encryption\\samplePDFoutput.pdf";
		//String SRC = "D:\\firstPDF.pdf";
		//String DEST = "D:\\firstPDF1.pdf";		
		
		String SRC = "D:\\firstPDF3.pdf";
		String DEST = "D:\\firstPDF3Encrypted.pdf";
		
		
		File file = new File(DEST);
		file.getParentFile().mkdirs();
		
		String USER_PASSWORD = "Dinosaur";
		String OWNER_PASSWORD = "Extinct";
		
		/**Encrypting PDF*/
		//PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC), new PdfWriter(DEST, new WriterProperties().setStandardEncryption(USER_PASSWORD.getBytes(),OWNER_PASSWORD.getBytes(), EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ENCRYPTION_AES_128 | EncryptionConstants.DO_NOT_ENCRYPT_METADATA)));
		
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC), new PdfWriter(DEST, new WriterProperties().setStandardEncryption(USER_PASSWORD.getBytes(),OWNER_PASSWORD.getBytes(), EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ENCRYPTION_AES_128 | EncryptionConstants.DO_NOT_ENCRYPT_METADATA)));

		
		/**Computing user password with owner password*/
		//PdfReader reader = new PdfReader(DEST,new ReaderProperties().setPassword(OWNER_PASSWORD.getBytes()));
		//PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(SRC));
		//byte[] userBytes = reader.computeUserPassword();
		/**ByteArray to HexString*/
        //BigInteger no = new BigInteger(1, userBytes);
        //String userPassword = no.toString(16);
        //System.out.println(new String(userBytes,StandardCharsets.UTF_8));
        
		pdfDoc.close();
		
	}
	
}
