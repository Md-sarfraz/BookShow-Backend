package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class TicketPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private static final Font BRAND_FONT = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(245, 245, 255));
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(27, 28, 48));
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(108, 113, 142));
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(26, 27, 46));
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(90, 94, 120));

    public byte[] generateTicketPdf(Booking booking, byte[] qrPngBytes) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(new Rectangle(595, 842), 28, 28, 28, 28);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();

            drawHeader(document, writer);
            drawTicketCard(document, booking, qrPngBytes);
            drawFooter(document);

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    private void drawHeader(Document document, PdfWriter writer) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();

        canvas.saveState();
        canvas.setColorFill(new Color(20, 22, 48));
        canvas.rectangle(0, page.getHeight() - 120, page.getWidth(), 120);
        canvas.fill();
        canvas.restoreState();

        Paragraph brand = new Paragraph("TicketFlix", BRAND_FONT);
        brand.setSpacingBefore(16);
        brand.setAlignment(Element.ALIGN_LEFT);
        document.add(brand);

        Paragraph subtitle = new Paragraph("Digital Movie Ticket", new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(210, 211, 230)));
        subtitle.setSpacingBefore(4);
        subtitle.setSpacingAfter(30);
        document.add(subtitle);
    }

    private void drawTicketCard(Document document, Booking booking, byte[] qrPngBytes) throws Exception {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);

        PdfPCell outer = new PdfPCell();
        outer.setBorderColor(new Color(228, 230, 244));
        outer.setBorderWidth(1.2f);
        outer.setPadding(18);
        outer.setBackgroundColor(new Color(252, 252, 255));

        PdfPTable ticket = new PdfPTable(new float[]{3.4f, 0.15f, 1.45f});
        ticket.setWidthPercentage(100);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPaddingRight(10);

        String movieName = booking.getShow() != null && booking.getShow().getMovie() != null
                ? safe(booking.getShow().getMovie().getTitle()) : "Movie";
        String theaterName = booking.getShow() != null && booking.getShow().getTheater() != null
                ? safe(booking.getShow().getTheater().getName()) : "Theater";
        String screen = booking.getShow() != null && booking.getShow().getScreenNumber() != null
                ? safe(booking.getShow().getScreenNumber()) : "Screen 1";
        String date = booking.getShow() != null && booking.getShow().getShowDate() != null
                ? booking.getShow().getShowDate().format(DATE_FORMATTER) : "N/A";
        String time = booking.getShow() != null && booking.getShow().getShowTime() != null
                ? booking.getShow().getShowTime().format(TIME_FORMATTER) : "N/A";

        left.addElement(new Paragraph(movieName, TITLE_FONT));
        left.addElement(spacer(8));
        left.addElement(detailLine("THEATER", theaterName + " | " + screen));
        left.addElement(detailLine("DATE & TIME", date + " | " + time));
        left.addElement(detailLine("SEATS", safe(booking.getSeatLabels()).replace(",", ", ")));
        left.addElement(detailLine("BOOKING ID", safe(booking.getBookingReference())));
        left.addElement(detailLine("TOTAL", "Rs. " + String.format("%.2f", booking.getTotalAmount() == null ? 0.0 : booking.getTotalAmount())));
        left.addElement(spacer(6));

        Paragraph status = new Paragraph("CONFIRMED", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(12, 127, 58)));
        status.setAlignment(Element.ALIGN_LEFT);
        left.addElement(status);

        ticket.addCell(left);

        PdfPCell divider = new PdfPCell();
        divider.setBorder(Rectangle.NO_BORDER);
        divider.setHorizontalAlignment(Element.ALIGN_CENTER);
        divider.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph perforation = new Paragraph(":\n:\n:\n:\n:\n:\n:\n:\n:", new Font(Font.HELVETICA, 14, Font.NORMAL, new Color(210, 214, 232)));
        perforation.setAlignment(Element.ALIGN_CENTER);
        divider.addElement(perforation);
        ticket.addCell(divider);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph scanText = new Paragraph("Scan at entry", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(78, 84, 112)));
        scanText.setAlignment(Element.ALIGN_CENTER);
        right.addElement(scanText);
        right.addElement(spacer(8));

        if (qrPngBytes != null && qrPngBytes.length > 0) {
            Image qr = Image.getInstance(qrPngBytes);
            qr.scaleToFit(145, 145);
            qr.setAlignment(Image.ALIGN_CENTER);
            right.addElement(qr);
        }

        right.addElement(spacer(8));
        Paragraph ticketHint = new Paragraph("Show this ticket at the theater", new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(112, 116, 142)));
        ticketHint.setAlignment(Element.ALIGN_CENTER);
        right.addElement(ticketHint);

        ticket.addCell(right);

        outer.addElement(ticket);
        wrapper.addCell(outer);

        document.add(wrapper);
    }

    private Paragraph detailLine(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", LABEL_FONT));
        p.add(new Chunk(value + "\n", VALUE_FONT));
        p.setSpacingAfter(7);
        return p;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(height);
        return p;
    }

    private void drawFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("Thank you for choosing TicketFlix. Have a blockbuster experience!", FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
