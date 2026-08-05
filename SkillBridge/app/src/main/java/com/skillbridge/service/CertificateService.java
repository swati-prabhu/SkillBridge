package com.skillbridge.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.skillbridge.entity.User;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates a simple, print-friendly completion certificate PDF for a
 * finished aptitude test, a completed roadmap, or a reviewed mock interview.
 * Reuses the same OpenPDF stack as ResumePdfService.
 */
@Service
public class CertificateService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 26, Font.BOLD, new Color(79, 70, 229));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
    private static final Font NAME_FONT = new Font(Font.HELVETICA, 20, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    public byte[] generate(User student, String achievementTitle, String achievementDetail) {
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 60, 60);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph brand = new Paragraph("SkillBridge", SUBTITLE_FONT);
            brand.setAlignment(Element.ALIGN_CENTER);
            document.add(brand);

            document.add(Chunk.NEWLINE);
            Paragraph title = new Paragraph("Certificate of Completion", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);
            Paragraph presented = new Paragraph("This certifies that", BODY_FONT);
            presented.setAlignment(Element.ALIGN_CENTER);
            document.add(presented);

            document.add(Chunk.NEWLINE);
            Paragraph name = new Paragraph(student.getFullName(), NAME_FONT);
            name.setAlignment(Element.ALIGN_CENTER);
            document.add(name);

            document.add(Chunk.NEWLINE);
            Paragraph achievement = new Paragraph("has successfully completed " + achievementTitle, BODY_FONT);
            achievement.setAlignment(Element.ALIGN_CENTER);
            document.add(achievement);

            if (achievementDetail != null && !achievementDetail.isBlank()) {
                Paragraph detail = new Paragraph(achievementDetail, BODY_FONT);
                detail.setAlignment(Element.ALIGN_CENTER);
                document.add(detail);
            }

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            Paragraph date = new Paragraph("Issued on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), SMALL_FONT);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate certificate PDF", ex);
        }

        return out.toByteArray();
    }
}
