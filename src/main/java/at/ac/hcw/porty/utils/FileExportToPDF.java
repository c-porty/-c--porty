package at.ac.hcw.porty.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileExportToPDF {
    private static final Logger logger =
            LoggerFactory.getLogger(FileExportToPDF.class);

    public static boolean exportToPdf(File file, boolean darkMode, BufferedImage image) {
        final float pageWidth = PDRectangle.A4.getWidth();
        final float pageHeight = PDRectangle.A4.getHeight();
        final float margin = 36; // points

        float scale = (pageWidth - 2*margin) / image.getWidth();
        float sliceHeightPx = (pageHeight - 2*margin) / scale;

        try (PDDocument document = new PDDocument()) {
            int y = 0;
            while (y < image.getHeight()) {
                int height = Math.min((int)sliceHeightPx, image.getHeight() - y);
                BufferedImage pageImage = image.getSubimage(0, y, image.getWidth(), height);
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDImageXObject pdfImage = LosslessFactory.createFromImage(document, pageImage);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.setNonStrokingColor(
                        darkMode ? 20/255f : 1.0f,
                        darkMode ? 18/255f : 1.0f,
                        darkMode ? 24/255f : 1.0f
                    );
                    content.addRect(0, 0, pageWidth, pageHeight);
                    content.fill();

                    content.drawImage(
                        pdfImage,
                        margin,
                        pageHeight - margin - (height * scale),
                        pageWidth - 2*margin,
                        height * scale
                    );
                }

                y += height;
            }

            document.save(file);
            return true;
        } catch (IOException e) {
            logger.error("PDF export failed", e);
            return false;
        }
    }
}
