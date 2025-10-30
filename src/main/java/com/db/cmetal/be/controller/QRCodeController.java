package com.db.cmetal.be.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.entity.Tavolo;
import com.db.cmetal.be.repository.TavoloRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

@RestController
@RequestMapping("/api/qr")
public class QRCodeController {

    @Value("${frontend.url}")
    private String frontendUrl;
    
    @Autowired
    TavoloRepository tavoloRepository;
    
    

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfQrLowagie() throws IOException, WriterException, DocumentException {
    	List<Tavolo> tavoli = tavoloRepository.findAll()
    	        .stream()
    	        .filter(Tavolo::getAttivo)                       // solo tavoli attivi
    	        .sorted(Comparator.comparingInt(Tavolo::getNumero)) // ordina per numero
    	        .toList();


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36); // margini 36pt
            PdfWriter.getInstance(document, baos);
            document.open();

            int cols = 4;
            int rows = 4;
            float pageWidth = PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin();
            float pageHeight = PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin();
            float cellWidth = pageWidth / cols;
            float cellHeight = pageHeight / rows;

            int count = 0;

            for (Tavolo t : tavoli) {
                int numTavolo = t.getNumero();
                BufferedImage qrImage = createQrImage(numTavolo);

                ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                ImageIO.write(qrImage, "PNG", imgBaos);
                Image pdfImg = Image.getInstance(imgBaos.toByteArray());

                pdfImg.scaleToFit(cellWidth - 10, cellHeight - 10); // margine interno cella

                int col = count % cols;
                int row = (count / cols) % rows;

                float x = document.leftMargin() + col * cellWidth + 5;
                float y = PageSize.A4.getHeight() - document.topMargin() - (row + 1) * cellHeight + 5;

                pdfImg.setAbsolutePosition(x, y);
                document.add(pdfImg);

                count++;

                if (count % (cols * rows) == 0 && count < tavoli.size()) {
                    document.newPage();
                }
            }

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-tavoli.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        }
    }


    private BufferedImage createQrImage(int numTavolo) throws WriterException {
        String url = frontendUrl + "/tavoli/" + numTavolo;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int qrSize = 300;
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, qrSize, qrSize);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        String text = "Tavolo " + numTavolo;
        Font font = new Font("Arial", Font.BOLD, 60);

        BufferedImage finalImage = new BufferedImage(qrSize, qrSize + 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = finalImage.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

        g.setColor(Color.BLACK);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int xText = (finalImage.getWidth() - textWidth) / 2;
        int yText = fm.getAscent() + 20;
        g.drawString(text, xText, yText);

        int qrY = yText + 20;
        g.drawImage(qrImage, 0, qrY, null);

        int borderThickness = 5;
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(borderThickness));
        g.drawRect(borderThickness / 2, borderThickness / 2, finalImage.getWidth() - borderThickness, finalImage.getHeight() - borderThickness);

        g.dispose();
        return finalImage;
    }

    @GetMapping("/{numTavolo}")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable int numTavolo) throws WriterException, IOException {
        String url = frontendUrl + "/tavoli/" + numTavolo;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int qrSize = 300;
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, qrSize, qrSize);

        // Convert BitMatrix to BufferedImage
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Parametri testo
        String text = "Tavolo " + numTavolo;
        Font font = new Font("Arial", Font.BOLD, 60);

        // Calcola altezza totale immagine finale
        BufferedImage finalImage = new BufferedImage(
                qrSize,
                qrSize + 100, // spazio extra per la scritta
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = finalImage.createGraphics();

        // Sfondo bianco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

        // Disegna testo
        g.setColor(Color.BLACK);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int xText = (finalImage.getWidth() - textWidth) / 2;
        int yText = fm.getAscent() + 20; // margine superiore
        g.drawString(text, xText, yText);

        // Disegna QR sotto testo con margine
        int qrY = yText + 20; // distanza tra testo e QR
        g.drawImage(qrImage, 0, qrY, null);

        // Disegna bordo nero a rientrare
        int borderThickness = 5;
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(borderThickness));
        g.drawRect(borderThickness / 2, borderThickness / 2, finalImage.getWidth() - borderThickness, finalImage.getHeight() - borderThickness);

        g.dispose();

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(finalImage, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tavolo-" + numTavolo + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(pngData);
    }


}
