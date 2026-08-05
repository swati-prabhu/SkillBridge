package com.skillbridge.service;

import com.skillbridge.entity.Resume;
import com.skillbridge.util.SkillMatcher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares a resume against a target role's required skills, optionally
 * cross-checking a pasted job description for skill mentions the student
 * hasn't listed. Deliberately keyword/skill-overlap based (transparent and
 * explainable) rather than an NLP/ML model - the required-skills list is the
 * source of truth; free-text JD parsing is only used as a secondary signal,
 * not to invent new "required skills" out of arbitrary text (too noisy/
 * unreliable to be honest about without a real NLP pipeline).
 */
@Service
public class AtsMatchService {

    public static class AtsResult {
        public int matchPercent;
        public List<String> matchedSkills;
        public List<String> missingSkills;
        public List<String> mentionedInJdButMissing; // skills the JD text mentions that the resume doesn't have
        public List<String> suggestions;
    }

    public AtsResult match(Resume resume, String resumeSkillsCsv, String requiredSkillsCsv, String jobDescriptionText) {
        AtsResult result = new AtsResult();

        Set<String> resumeSkills = SkillMatcher.toSkillSet(resumeSkillsCsv);
        Set<String> requiredSkills = SkillMatcher.toSkillSet(requiredSkillsCsv);

        Set<String> matched = new TreeSet<>(resumeSkills);
        matched.retainAll(requiredSkills);

        Set<String> missing = new TreeSet<>(requiredSkills);
        missing.removeAll(resumeSkills);

        int percent = requiredSkills.isEmpty() ? 0 : (int) Math.round((matched.size() * 100.0) / requiredSkills.size());

        // Small adjustment based on resume completeness - a real ATS also weighs formatting/sections, not just keywords.
        if (resume.getSummary() == null || resume.getSummary().isBlank()) percent -= 5;
        if (resume.getExperience().isEmpty()) percent -= 10;
        percent = Math.max(0, Math.min(100, percent));

        String jdLower = jobDescriptionText == null ? "" : jobDescriptionText.toLowerCase();
        List<String> jdMentionsMissing = missing.stream()
                .filter(skill -> jdLower.contains(skill))
                .collect(Collectors.toList());

        List<String> suggestions = new ArrayList<>();
        if (!missing.isEmpty()) {
            suggestions.add("Add these required skills to your resume if you genuinely have them: " + String.join(", ", limit(missing, 6)) + ".");
        }
        if (!jdMentionsMissing.isEmpty()) {
            suggestions.add("The job description specifically calls out " + String.join(", ", limit(jdMentionsMissing, 4))
                    + " - worth highlighting prominently if you have any hands-on experience with them.");
        }
        if (resume.getExperience().isEmpty()) {
            suggestions.add("Add at least one experience entry - ATS systems and recruiters both weight relevant experience heavily.");
        }
        if (resume.getProjects().isEmpty()) {
            suggestions.add("Add a project demonstrating the missing skills above if you don't have paid work experience with them.");
        }
        if (resume.getSummary() == null || resume.getSummary().isBlank()) {
            suggestions.add("Add a 2-3 sentence summary near the top restating your fit for this kind of role.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Strong match - your resume already covers the key requirements for this role.");
        }

        result.matchPercent = percent;
        result.matchedSkills = new ArrayList<>(matched);
        result.missingSkills = new ArrayList<>(missing);
        result.mentionedInJdButMissing = jdMentionsMissing;
        result.suggestions = suggestions;
        return result;
    }

    private List<String> limit(Collection<String> items, int max) {
        return items.stream().limit(max).collect(Collectors.toList());
    }
}
