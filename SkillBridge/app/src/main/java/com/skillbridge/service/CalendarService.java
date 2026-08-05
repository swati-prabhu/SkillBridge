package com.skillbridge.service;

import com.skillbridge.entity.Interview;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Generates minimal but valid RFC 5545 .ics files so an interview can be
 * added to Google Calendar / Outlook / Apple Calendar with one click.
 * No external calendar API/OAuth integration - this is a self-contained
 * file export, which is what "Calendar Integration" means for most
 * portfolio-scale apps without a Google Cloud project to register.
 */
@Service
public class CalendarService {

    private static final DateTimeFormatter ICS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String generateIcs(Interview interview) {
        String start = interview.getScheduledAt().atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC).format(ICS_FORMAT);
        String end = interview.getScheduledAt().plusMinutes(45).atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC).format(ICS_FORMAT);
        String now = java.time.LocalDateTime.now().atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC).format(ICS_FORMAT);

        String summary = "Interview: " + interview.getApplication().getInternship().getTitle()
                + " @ " + interview.getApplication().getInternship().getCompany();
        String description = "Mode: " + interview.getMode()
                + (interview.getNotes() != null ? "\\nNotes: " + escape(interview.getNotes()) : "");

        return "BEGIN:VCALENDAR\r\n"
                + "VERSION:2.0\r\n"
                + "PRODID:-//SkillBridge//Interview Scheduling//EN\r\n"
                + "CALSCALE:GREGORIAN\r\n"
                + "BEGIN:VEVENT\r\n"
                + "UID:interview-" + interview.getId() + "@skillbridge.dev\r\n"
                + "DTSTAMP:" + now + "\r\n"
                + "DTSTART:" + start + "\r\n"
                + "DTEND:" + end + "\r\n"
                + "SUMMARY:" + escape(summary) + "\r\n"
                + "DESCRIPTION:" + escape(description) + "\r\n"
                + "LOCATION:" + escape(interview.getMode()) + "\r\n"
                + "BEGIN:VALARM\r\n"
                + "TRIGGER:-PT30M\r\n"
                + "ACTION:DISPLAY\r\n"
                + "DESCRIPTION:Interview reminder\r\n"
                + "END:VALARM\r\n"
                + "END:VEVENT\r\n"
                + "END:VCALENDAR\r\n";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
    }
}
