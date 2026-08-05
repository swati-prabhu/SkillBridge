package com.skillbridge.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared, transparent skill-overlap matching logic used by internship
 * recommendations, roadmap personalization, and the "notify matching
 * students" feature. Deliberately simple (case-insensitive set overlap)
 * rather than ML-based, so it's easy to explain and reason about.
 */
public final class SkillMatcher {

    private SkillMatcher() {}

    public static Set<String> toSkillSet(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        return Arrays.stream(csv.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toSet());
    }

    /** Number of skills shared between two comma-separated skill lists. */
    public static int overlapCount(String skillsCsvA, String skillsCsvB) {
        Set<String> a = toSkillSet(skillsCsvA);
        Set<String> b = toSkillSet(skillsCsvB);
        a.retainAll(b);
        return a.size();
    }
}
