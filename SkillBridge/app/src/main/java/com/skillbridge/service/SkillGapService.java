package com.skillbridge.service;

import com.skillbridge.entity.Internship;
import com.skillbridge.entity.Roadmap;
import com.skillbridge.util.SkillMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillGapService {

    private final RoadmapService roadmapService;

    public static class SkillGapResult {
        public List<String> missingSkills;
        public int matchPercent;
        public Roadmap suggestedRoadmap; // may be null
    }

    /** Compares a student's skills against a target internship's requirements and suggests a roadmap covering the gap. */
    public SkillGapResult analyze(String studentSkillsCsv, Internship targetInternship) {
        SkillGapResult result = new SkillGapResult();

        Set<String> studentSkills = SkillMatcher.toSkillSet(studentSkillsCsv);
        Set<String> requiredSkills = SkillMatcher.toSkillSet(targetInternship.getRequiredSkills());

        Set<String> missing = new TreeSet<>(requiredSkills);
        missing.removeAll(studentSkills);

        result.missingSkills = new ArrayList<>(missing);
        result.matchPercent = requiredSkills.isEmpty() ? 100
                : (int) Math.round(((requiredSkills.size() - missing.size()) * 100.0) / requiredSkills.size());

        // Suggest whichever roadmap covers the most of the missing skills.
        result.suggestedRoadmap = roadmapService.findAll().stream()
                .max(Comparator.comparingInt(r -> SkillMatcher.overlapCount(String.join(",", missing), r.getRelatedSkills())))
                .filter(r -> SkillMatcher.overlapCount(String.join(",", missing), r.getRelatedSkills()) > 0)
                .orElse(null);

        return result;
    }
}
