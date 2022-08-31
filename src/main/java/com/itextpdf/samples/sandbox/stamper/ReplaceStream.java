package com.itextpdf.samples.sandbox.stamper;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ReplaceStream {
    public static final String SRC = "C:/pdf/hello02.pdf";
    public static final String DEST = "C:/pdf/hello_new02.pdf";

    public static void main(String[] args) throws Exception {
        File file = new File(DEST);
        file.getParentFile().mkdirs();

        new ReplaceStream().manipulatePdf(DEST);
    }

    protected void manipulatePdf(String dest) throws Exception {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC), new PdfWriter(dest));
        PdfPage page = pdfDoc.getFirstPage();
        PdfDictionary dict = page.getPdfObject();

        PdfObject object = dict.get(PdfName.Contents);
        if (object instanceof PdfStream) {
            PdfStream stream = (PdfStream) object;
            byte[] data = stream.getBytes();
            String replacedData = new String(data).replace("Hello", "Adios1235");
            //stream.setData(replacedData.getBytes(StandardCharsets.UTF_8));
        }

        pdfDoc.close();
    }
}