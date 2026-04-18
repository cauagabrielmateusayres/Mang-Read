package com.mangareader.app.pdf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Renders individual PDF pages as JavaFX Images using Apache PDFBox 3.x.
 * Keeps the document open while reading to avoid re-loading on every page turn.
 */
public class PdfRenderer implements AutoCloseable {

    private PDDocument document;
    private PDFRenderer renderer;
    private String currentPath;

    /** Opens a PDF file for rendering. Closes any previously open document. */
    public void open(String filePath) throws IOException {
        close();
        document = Loader.loadPDF(new File(filePath));
        renderer = new PDFRenderer(document);
        currentPath = filePath;
    }

    /** Returns the number of pages in the currently open PDF. */
    public int getPageCount() {
        if (document == null) throw new IllegalStateException("No PDF open");
        return document.getNumberOfPages();
    }

    /**
     * Renders a zero-indexed page at 150 DPI and returns a JavaFX Image.
     * 150 DPI is a good balance between quality and performance for manga.
     */
    public Image renderPage(int pageIndex) throws IOException {
        if (document == null) throw new IllegalStateException("No PDF open");
        BufferedImage bimg = renderer.renderImageWithDPI(pageIndex, 150, ImageType.RGB);
        return SwingFXUtils.toFXImage(bimg, null);
    }

    public String getCurrentPath() { return currentPath; }

    public boolean isOpen() { return document != null; }

    @Override
    public void close() {
        if (document != null) {
            try { document.close(); } catch (IOException ignored) {}
            document = null;
            renderer = null;
            currentPath = null;
        }
    }
}
