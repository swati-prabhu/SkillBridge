package com.skillbridge.service;

import com.skillbridge.entity.Internship;
import com.skillbridge.entity.User;
import com.skillbridge.repository.InternshipRepository;
import com.skillbridge.util.SkillMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<Internship> findAll() {
        return internshipRepository.findAll();
    }

    public List<Internship> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return internshipRepository
                .findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrLocationContainingIgnoreCase(
                        keyword, keyword, keyword);
    }

    /** Advanced search: free-text keyword + category filter + pagination. */
    public Page<Internship> search(String keyword, String category, Pageable pageable) {
        return internshipRepository.search(keyword, category, pageable);
    }

    public List<String> findDistinctCategories() {
        return internshipRepository.findDistinctCategories();
    }

    public Internship findById(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found: " + id));
    }

    public List<Internship> findByPostedBy(Long postedById) {
        return internshipRepository.findByPostedById(postedById);
    }

    public Internship save(Internship internship) {
        return internshipRepository.save(internship);
    }

    public void delete(Long id) {
        internshipRepository.deleteById(id);
    }

    public long count() {
        return internshipRepository.count();
    }

    /**
     * Naive but transparent skill-overlap recommender: ranks internships by how many
     * of the student's skills match the internship's required skills.
     */
    public List<Map.Entry<Internship, Integer>> recommendForSkills(String studentSkillsCsv, int limit) {
        if (SkillMatcher.toSkillSet(studentSkillsCsv).isEmpty()) return List.of();

        return internshipRepository.findAll().stream()
                .map(i -> Map.entry(i, SkillMatcher.overlapCount(studentSkillsCsv, i.getRequiredSkills())))
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Weighted recommendation engine: blends how much of the internship's required
     * skills the student covers ("coverage") with how focused that match is relative
     * to the student's whole skillset ("specialization"), using an F1-like harmonic
     * mean so an internship needing 8 skills where the student has 2 doesn't
     * outrank a tightly-matched 3-of-3. Each result includes a plain-English
     * explanation - "why was this recommended" isn't a black box here.
     */
    public List<com.skillbridge.dto.InternshipRecommendation> recommendWeighted(String studentSkillsCsv, int limit) {
        var studentSkills = SkillMatcher.toSkillSet(studentSkillsCsv);
        if (studentSkills.isEmpty()) return List.of();

        return internshipRepository.findAll().stream()
                .map(i -> buildRecommendation(i, studentSkills))
                .filter(r -> r.getMatchPercent() > 0)
                .sorted((a, b) -> b.getMatchPercent() - a.getMatchPercent())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private com.skillbridge.dto.InternshipRecommendation buildRecommendation(Internship internship, java.util.Set<String> studentSkills) {
        var required = SkillMatcher.toSkillSet(internship.getRequiredSkills());
        var matched = new java.util.TreeSet<>(studentSkills);
        matched.retainAll(required);
        var missing = new java.util.TreeSet<>(required);
        missing.removeAll(studentSkills);

        double coverage = required.isEmpty() ? 0 : (double) matched.size() / required.size();
        double specialization = studentSkills.isEmpty() ? 0 : (double) matched.size() / studentSkills.size();
        int weightedPercent = (coverage + specialization) == 0 ? 0
                : (int) Math.round(100 * (2 * coverage * specialization) / (coverage + specialization));

        String explanation = matched.isEmpty()
                ? "No overlap with your listed skills."
                : "Matches " + matched.size() + " of " + required.size() + " required skills ("
                  + String.join(", ", matched) + ")"
                  + (missing.isEmpty() ? "." : "; missing: " + String.join(", ", missing) + ".");

        return new com.skillbridge.dto.InternshipRecommendation(
                internship, weightedPercent, new java.util.ArrayList<>(matched), new java.util.ArrayList<>(missing), explanation
        );
    }

    /** All internships that share at least one skill with the given student, used to fan out "new match" notifications. */
    public List<Internship> findMatchingForSkills(String studentSkillsCsv, Long excludeInternshipId) {
        if (SkillMatcher.toSkillSet(studentSkillsCsv).isEmpty()) return List.of();

        return internshipRepository.findAll().stream()
                .filter(i -> !i.getId().equals(excludeInternshipId))
                .filter(i -> SkillMatcher.overlapCount(studentSkillsCsv, i.getRequiredSkills()) > 0)
                .collect(Collectors.toList());
    }

    /**
     * When a new internship is posted, notify every student whose profile skills
     * overlap with its required skills - closes the loop between "student sets
     * their skills" and "student hears about relevant opportunities" without
     * them having to keep re-checking the listings page.
     */
    public void notifyMatchingStudents(Internship internship) {
        List<User> students = userService.findAllStudents();
        for (User student : students) {
            if (SkillMatcher.overlapCount(student.getSkills(), internship.getRequiredSkills()) > 0) {
                notificationService.notify(
                        student,
                        "New internship matching your skills: \"" + internship.getTitle() + "\" at " + internship.getCompany() + ".",
                        "/internships/" + internship.getId()
                );
            }
        }
    }
}
