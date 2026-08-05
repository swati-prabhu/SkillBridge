package com.skillbridge.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.skillbridge.entity.Resume;
import com.skillbridge.entity.ResumeEducation;
import com.skillbridge.entity.ResumeExperience;
import com.skillbridge.entity.ResumeProject;
import com.skillbridge.entity.User;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class ResumePdfService {

    private static final Font NAME_FONT = new Font(Font.HELVETICA, 20, Font.BOLD);
    private static final Font HEADLINE_FONT = new Font(Font.HELVETICA, 12, Font.ITALIC, Color.DARK_GRAY);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(79, 70, 229));
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font BOLD_BODY_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font SMALL_MUTED_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    public byte[] generate(User user, Resume resume) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(user.getFullName(), NAME_FONT));
            if (resume.getHeadline() != null && !resume.getHeadline().isBlank()) {
                document.add(new Paragraph(resume.getHeadline(), HEADLINE_FONT));
            }
            document.add(new Paragraph(user.getEmail(), SMALL_MUTED_FONT));

            StringBuilder links = new StringBuilder();
            if (notBlank(resume.getGithub())) links.append("GitHub: ").append(resume.getGithub()).append("   ");
            if (notBlank(resume.getLinkedin())) links.append("LinkedIn: ").append(resume.getLinkedin()).append("   ");
            if (notBlank(resume.getPortfolio())) links.append("Portfolio: ").append(resume.getPortfolio());
            if (links.length() > 0) document.add(new Paragraph(links.toString(), SMALL_MUTED_FONT));

            document.add(Chunk.NEWLINE);

            if (resume.getSummary() != null && !resume.getSummary().isBlank()) {
                addSectionHeading(document, "Summary");
                document.add(new Paragraph(resume.getSummary(), BODY_FONT));
                document.add(Chunk.NEWLINE);
            }

            if (user.getSkills() != null && !user.getSkills().isBlank()) {
                addSectionHeading(document, "Skills");
                document.add(new Paragraph(user.getSkills(), BODY_FONT));
                document.add(Chunk.NEWLINE);
            }

            if (!resume.getExperience().isEmpty()) {
                addSectionHeading(document, "Experience");
                for (ResumeExperience e : resume.getExperience()) {
                    document.add(new Paragraph(e.getRole() + " — " + e.getCompany(), BOLD_BODY_FONT));
                    String range = safe(e.getStartDate()) + " – " + (notBlank(e.getEndDate()) ? e.getEndDate() : "Present");
                    document.add(new Paragraph(range, SMALL_MUTED_FONT));
                    if (notBlank(e.getDescription())) document.add(new Paragraph(e.getDescription(), BODY_FONT));
                    document.add(Chunk.NEWLINE);
                }
            }

            if (!resume.getProjects().isEmpty()) {
                addSectionHeading(document, "Projects");
                for (ResumeProject p : resume.getProjects()) {
                    document.add(new Paragraph(p.getName(), BOLD_BODY_FONT));
                    if (notBlank(p.getTechStack())) document.add(new Paragraph(p.getTechStack(), SMALL_MUTED_FONT));
                    if (notBlank(p.getDescription())) document.add(new Paragraph(p.getDescription(), BODY_FONT));
                    if (notBlank(p.getLink())) document.add(new Paragraph(p.getLink(), SMALL_MUTED_FONT));
                    document.add(Chunk.NEWLINE);
                }
            }

            if (!resume.getEducation().isEmpty()) {
                addSectionHeading(document, "Education");
                for (ResumeEducation e : resume.getEducation()) {
                    document.add(new Paragraph(e.getDegree() + " — " + e.getInstitution(), BOLD_BODY_FONT));
                    String range = safe(e.getStartYear()) + " – " + (notBlank(e.getEndYear()) ? e.getEndYear() : "Present");
                    document.add(new Paragraph(range, SMALL_MUTED_FONT));
                    document.add(Chunk.NEWLINE);
                }
            }

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate resume PDF", ex);
        }

        return out.toByteArray();
    }

    private void addSectionHeading(Document document, String text) throws DocumentException {
        Paragraph heading = new Paragraph(text, SECTION_FONT);
        heading.setSpacingAfter(6);
        document.add(heading);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
