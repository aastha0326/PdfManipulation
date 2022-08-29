package pdf;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi.MD5;

import com.itextpdf.io.IOException;
import com.itextpdf.kernel.crypto.AESCipherCBCnoPad;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.text.pdf.crypto.ARCFOUREncryption;

public class encKey {
	
	static byte[] p = {(byte)0x28, (byte) 0xBF, (byte)0x4E, (byte)0x5E, (byte)0x4E, (byte)0x75,
						(byte) 0x8A, (byte)0x41, (byte)0x64, (byte)0x00, (byte)0x4E, (byte)0x56, (byte) 0xFF, 
						(byte) 0xFA, (byte)0x01, (byte)0x08, (byte)0x2E, (byte)0x2E, (byte)0x00, (byte) 0xB6, 
						(byte) 0xD0, (byte)0x68, (byte)0x3E, (byte) 0x80, (byte)0x2F, (byte)0x0C, (byte) 0xA9, 
						(byte) 0xFE, (byte)0x64, (byte)0x53, (byte)0x69, (byte)0x7A};
	
	/**Algorithm 3*/
	public byte[] oValue(String ownerString, String userString) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		
		byte[] paddedValueO = padPassword(ownerString);
		byte[] paddedValueU = padPassword(userString);
		
		ARCFOUREncryption arcfour = new ARCFOUREncryption();
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		 
        byte[] key = md.digest(paddedValueO);
        
        for(int i=0;i<50;i++) {
        	key = md.digest(key);
        	//System.arraycopy(md.digest(), 0, key, 0, 128/8); //assumming to be 128 bits
        }
              
        byte[] u = new byte[32]; 
        u = paddedValueU;
        
        for(int i=0;i<20;i++) {
        	byte[] newKey = new byte[key.length];
        	for(int j=0;j<key.length;j++) {
        		newKey[j] = (byte) (i^key[j]);
        	}
     
        	arcfour.prepareARCFOURKey(newKey);
            arcfour.encryptARCFOUR(u);     
        }
        //System.out.println(new String(u,StandardCharsets.UTF_8));
        
		return u;
	}
	
	/**Algorithm 2*/
	public byte[] encryptionKey(String owner, String user, int perm, byte[] id) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		
		byte[] metadataNotEnc = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
		byte[] encKey = new byte[128/8];
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		md.update(padPassword(user));
		md.update(oValue(owner, user));
		
		byte[] permByte = new byte[4];
		for(int i=0;i<4;i++) {
			if(i!=0)
				perm = perm >> 8;
			permByte[i] = (byte) perm;
		}
		
		md.update(permByte);
		md.update(id);
		//if metadata is not encrypted
		md.update(metadataNotEnc);
		System.arraycopy(md.digest(), 0, encKey, 0, 128/8);
		
		for(int i=0;i<50;i++) {
			System.arraycopy(md.digest(encKey), 0, encKey, 0, 128/8);
		}
		
		return encKey;
	}
	
	/**Algorithm 5*/
	public byte[] uValue(String ownerString, String userString, int perm, byte[] id) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		
		ARCFOUREncryption arcfour = new ARCFOUREncryption();
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		//md.update(padPassword(userString));
		md.update(p);
		md.update(id);
		
		byte[] hashResult = md.digest();
		byte[] key = encryptionKey(ownerString, userString, perm, id);
		for(int i=0;i<20;i++) {
        	byte[] newKey = new byte[key.length];
        	for(int j=0;j<key.length;j++) {
        		newKey[j] = (byte) (i^key[j]);
        	}
     
        	arcfour.prepareARCFOURKey(newKey);
            arcfour.encryptARCFOUR(hashResult);
        }
        //System.out.println(new String(hashResult,StandardCharsets.UTF_8));
        
		return hashResult;
	}
	
	/**Algorithm 7.6.4.3.2 step a & b*/
	public byte[] padPassword(String password) {
		//byte[] s1 = PdfEncodings.convertToBytes(password, PdfEncodings.PDF_DOC_ENCODING);
		byte[] a = new byte[32];
		if(password.isEmpty() || password == null) {
			System.arraycopy(p, 0, a, 0, 32);
		}else {
			byte[] s1 = password.getBytes();
			System.arraycopy(s1, 0, a, 0, Math.min(s1.length, 32));
			if(s1.length<32)
				System.arraycopy(p, 0, a, s1.length, 32-s1.length);
		}
		return a;
	}
	
	public byte[] retreiveStreamsandStrings(String SRC) throws java.io.IOException {
		
		byte[] result = new byte[10];
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC));
		PdfObject pdfobj;
		int n = pdfDoc.getNumberOfPdfObjects();
		for(int i=0;i<n;i++) {
			pdfobj = pdfDoc.getPdfObject(i);
			if(pdfobj != null) {
				if(pdfobj.isDictionary()) {
					PdfDictionary pdfDict = (PdfDictionary)pdfobj;
					
					//Iterator<Map.Entry<PdfName, PdfObject>> iterator =  pdfDict.iterator();
					for(Map.Entry<PdfName, PdfObject> element : pdfDict.entrySet()) {
						//System.out.println(element.getKey()+ " " + element.getValue());
						
						System.out.println(element.getValue().isString());
						
					
					}
				}else continue;
				
			}
		}
		
		return result;
	}
		
	public void replace(PdfName key, PdfObject value, PdfDictionary pdfDict) {
		
		pdfDict.put(key, value);
		
	    }
	
	public void putAll(PdfDictionary d, PdfDictionary pdfDict) {
		
        pdfDict.putAll(d);
    }
	
	
	public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, java.io.IOException, InvalidAlgorithmParameterException {
		String userString = "Hello";
		String ownerString = "World";
		String SRC = "D:\\firstPDF.pdf";
		
		encKey enckey = new encKey();
		enckey.retreiveStreamsandStrings(SRC);
		//enckey.oValue(ownerString, userString);
		
		/**Case1_World_Hello*/
		byte[] id = {(byte)0xBD, (byte)0xE1, (byte)0x44, (byte)0x45, (byte)0x6F, (byte)0x60, (byte)0xBE, (byte)0xFE,
					(byte)0x46, (byte)0xC1, (byte)0x5B, (byte)0x66, (byte)0x08, (byte)0x03, (byte)0xA1, (byte)0xD1};
		enckey.uValue(ownerString, userString, -1852, id);
		
		/**Case2_Extinct_Dinosaur*/
		/*byte[] id = {(byte)0xb2, (byte)0x1a, (byte)0x72, (byte)0x5f, (byte)0x62, (byte)0x54, (byte)0x62, (byte)0x4b,
					(byte)0xd0, (byte)0x76, (byte)0xdc, (byte)0xe7, (byte)0xcf, (byte)0x6a, (byte)0x18, (byte)0xa4};
		enckey.uValue(ownerString, userString, -1340, id);*/
		
		Path path = Paths.get(SRC);
		byte[] bytes = Files.readAllBytes(path);
		//System.out.println(new String(bytes, StandardCharsets.UTF_8));
		
		
		/**Algorithm 1*/
		byte[] pdfStringBytes = new byte[64/*67*//*203*/];
		List <Byte> pdfString = new ArrayList<Byte>();
		System.arraycopy(bytes, 636/*710*//*788*/, pdfStringBytes, 0, 64/*67*//*203*/);
		//System.out.println(new String(pdfStringBytes, StandardCharsets.UTF_8));
		
		for(int i=0;i<pdfStringBytes.length;i++) {
			if(pdfStringBytes[i]==92) {
				
				/**converting '\xyz' as 4 different bytes to a single byte with 'xyz' as octal number*/
				if(pdfStringBytes[i+1]>=48 && pdfStringBytes[i+1]<=57)
				{
					byte[] a = new byte[3];
					a[0] = pdfStringBytes[i+1];
					a[1] = pdfStringBytes[i+2];
					a[2] = pdfStringBytes[i+3];
					//System.out.println(new String(a,StandardCharsets.UTF_8));
					String s = new String(a,StandardCharsets.UTF_8);
					int si = Integer.parseInt(s,8);
					byte x = (byte)si;
					//System.out.println(si);
					pdfString.add(x);
					i+=3;
				} else if(pdfStringBytes[i+1]==110){
					/** converting '\n' to byte 0x0A*/
					pdfString.add((byte)0x0A);
					i+=1;
				} else { 
					pdfString.add(pdfStringBytes[i+1]);
					i+=1;
				}
			} else {
				pdfString.add(pdfStringBytes[i]);
			}
		}
		
		/**Seperating first 16 bytes of the ciphertext as IV*/
		byte[] pdfStringFinal = new byte[pdfString.size()-16];
		byte[] iv = new byte[16];
		for (int i = 0; i < pdfString.size(); i++) {
			if(i<16)
				iv[i] = pdfString.get(i);
			else
				pdfStringFinal[i-16] = pdfString.get(i);
		}
		
		//System.out.println(new String(pdfStringFinal,StandardCharsets.UTF_8)+" "+ pdfStringFinal.length);
		//System.out.println(new String(iv,StandardCharsets.UTF_8)+" "+ iv.length);
		
		/**Appending object number and generation number to the key*/
		byte[] keyHashInput = new byte[25];
		System.arraycopy(enckey.encryptionKey(ownerString, userString, -1852, id), 0, keyHashInput, 0, 16);
		
		/**Appending lower order 3 bytes of object number, low order byte first*/
		keyHashInput[16] = (byte)0x03;
		keyHashInput[17] = (byte)0x00;
		keyHashInput[18] = (byte)0x00;
		/**Appending lower order 2 bytes of generation number, low order byte first*/
		keyHashInput[19] = (byte)0x00;
		keyHashInput[20] = (byte)0x00;
		/**Appending sAlT*/
		keyHashInput[21] = (byte)0x73;
		keyHashInput[22] = (byte)0x41;
		keyHashInput[23] = (byte)0x6C;
		keyHashInput[24] = (byte)0x54;
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		/**final key with which encryption is done*/
		byte[] key = md.digest(keyHashInput);
		//System.out.println(key.length);
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		SecretKey originalkey = new SecretKeySpec(key, 0, key.length, "AES");
		IvParameterSpec ivFinal = new IvParameterSpec(iv);
	    cipher.init(Cipher.DECRYPT_MODE, originalkey,ivFinal);
	     
	    byte[] original = cipher.doFinal(pdfStringFinal);
	    System.out.println(new String(original,StandardCharsets.UTF_8));
	    byte x = (byte) (0xAF-0x7F);
	    char c = (char)x;
	    //System.out.printf("%c",c);
	    /*BigInteger no = new BigInteger(original);
	    String noString = no.toString(16);
	    System.out.println(noString);*/

        //D:20220626040651+05'30'
        //D:20220626040827+05'30'
	    //iText� 7.0.2 �2000-2017 iText Group NV (AGPL-version); modified using iText� 7.0.2 �2000-2017 iText Group NV (AGPL-version)
	   	
	    
	    
	    
	}
		
}
