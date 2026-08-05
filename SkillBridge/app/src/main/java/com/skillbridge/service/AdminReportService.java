package com.skillbridge.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.skillbridge.entity.Application;
import com.skillbridge.entity.ApplicationStatus;
import com.skillbridge.entity.Interview;
import com.skillbridge.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ApplicationService applicationService;
    private final InterviewRepository interviewRepository;
    private final PlacementStatisticsService placementStatisticsService;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(79, 70, 229));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font META_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    public byte[] placementsReport() {
        var stats = placementStatisticsService.build();
        Document doc = newDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addTitle(doc, "Placement Report");
            addMeta(doc);

            doc.add(new Paragraph("Total Placed Students: " + stats.getTotalPlacedStudents(), CELL_FONT));
            doc.add(new Paragraph("Average Package: " + stats.getAveragePackage() + " LPA", CELL_FONT));
            doc.add(new Paragraph("Highest Package: " + stats.getHighestPackage() + " LPA", CELL_FONT));
            doc.add(Chunk.NEWLINE);

            addSectionHeading(doc, "Offers by Company");
            addKeyValueTable(doc, stats.getOffersByCompany());
            doc.add(Chunk.NEWLINE);

            addSectionHeading(doc, "Placed Students by Department");
            addKeyValueTable(doc, stats.getPlacedByDepartment());

            doc.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate placements report", ex);
        }
        return out.toByteArray();
    }

    public byte[] interviewsReport() {
        List<Interview> interviews = interviewRepository.findAll();
        Document doc = newDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addTitle(doc, "Interviews Report");
            addMeta(doc);
            doc.add(new Paragraph("Total Interviews Scheduled: " + interviews.size(), CELL_FONT));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addHeaderRow(table, "Student", "Internship", "Scheduled", "Mode");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            for (Interview iv : interviews) {
                table.addCell(cell(iv.getStudent().getFullName()));
                table.addCell(cell(iv.getApplication().getInternship().getTitle()));
                table.addCell(cell(iv.getScheduledAt().format(fmt)));
                table.addCell(cell(iv.getMode()));
            }
            doc.add(table);
            doc.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate interviews report", ex);
        }
        return out.toByteArray();
    }

    public byte[] studentProgressReport() {
        List<Application> applications = applicationService.findAll();
        Document doc = newDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addTitle(doc, "Student Application Progress Report");
            addMeta(doc);

            long applied = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.APPLIED).count();
            long shortlisted = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
            long selected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count();
            long rejected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

            doc.add(new Paragraph("Total Applications: " + applications.size(), CELL_FONT));
            doc.add(new Paragraph("Applied: " + applied + " | Shortlisted: " + shortlisted
                    + " | Selected: " + selected + " | Rejected: " + rejected, CELL_FONT));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addHeaderRow(table, "Student", "Internship", "Company", "Status");
            for (Application a : applications) {
                table.addCell(cell(a.getStudent().getFullName()));
                table.addCell(cell(a.getInternship().getTitle()));
                table.addCell(cell(a.getInternship().getCompany()));
                table.addCell(cell(a.getStatus().name()));
            }
            doc.add(table);
            doc.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate student progress report", ex);
        }
        return out.toByteArray();
    }

    private Document newDocument() {
        return new Document(PageSize.A4, 40, 40, 50, 40);
    }

    private void addTitle(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, TITLE_FONT);
        doc.add(p);
    }

    private void addMeta(Document doc) throws DocumentException {
        doc.add(new Paragraph("Generated on " + java.time.LocalDate.now(), META_FONT));
        doc.add(Chunk.NEWLINE);
    }

    private void addSectionHeading(Document doc, String text) throws DocumentException {
        doc.add(new Paragraph(text, new Font(Font.HELVETICA, 12, Font.BOLD)));
    }

    private void addKeyValueTable(Document doc, java.util.Map<String, Long> data) throws DocumentException {
        if (data.isEmpty()) {
            doc.add(new Paragraph("No data yet.", CELL_FONT));
            return;
        }
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addHeaderRow(table, "Name", "Count");
        data.forEach((k, v) -> {
            table.addCell(cell(k));
            table.addCell(cell(String.valueOf(v)));
        });
        doc.add(table);
    }

    private void addHeaderRow(PdfPTable table, String... headers) {
        for (String h : headers) {
            var cell = new com.lowagie.text.pdf.PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(new Color(79, 70, 229));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private com.lowagie.text.pdf.PdfPCell cell(String text) {
        var cell = new com.lowagie.text.pdf.PdfPCell(new Phrase(text, CELL_FONT));
        cell.setPadding(4);
        return cell;
    }
}
