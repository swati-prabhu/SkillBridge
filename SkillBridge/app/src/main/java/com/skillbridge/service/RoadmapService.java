package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.RoadmapProgressRepository;
import com.skillbridge.repository.RoadmapRepository;
import com.skillbridge.repository.RoadmapStepRepository;
import com.skillbridge.util.SkillMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private static final int[] MILESTONES = {25, 50, 75, 100};

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository stepRepository;
    private final RoadmapProgressRepository progressRepository;
    private final NotificationService notificationService;

    public List<Roadmap> findAll() {
        return roadmapRepository.findAll();
    }

    public Roadmap findById(Long id) {
        return roadmapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found: " + id));
    }

    /** Map of stepId -> completed, for a given student across one roadmap. */
    public Map<Long, Boolean> completionMap(User student, Roadmap roadmap) {
        List<RoadmapProgress> progress = progressRepository.findByStudent(student);
        Map<Long, Boolean> completed = progress.stream()
                .filter(p -> p.getStep().getRoadmap().getId().equals(roadmap.getId()))
                .collect(Collectors.toMap(p -> p.getStep().getId(), RoadmapProgress::isCompleted));

        // ensure every step has an entry (defaults to false)
        roadmap.getSteps().forEach(s -> completed.putIfAbsent(s.getId(), false));
        return completed;
    }

    public int percentComplete(User student, Roadmap roadmap) {
        Map<Long, Boolean> map = completionMap(student, roadmap);
        long total = roadmap.getSteps().size();
        if (total == 0) return 0;
        long done = map.values().stream().filter(Boolean::booleanValue).count();
        return (int) Math.round((done * 100.0) / total);
    }

    /** Average completion percent across every roadmap that has at least one step - a single "career prep" number. */
    public int overallPercentComplete(User student) {
        List<Roadmap> roadmaps = findAll();
        List<Integer> withSteps = roadmaps.stream()
                .filter(r -> !r.getSteps().isEmpty())
                .map(r -> percentComplete(student, r))
                .collect(Collectors.toList());
        if (withSteps.isEmpty()) return 0;
        return (int) Math.round(withSteps.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    /**
     * Recommends the single roadmap whose track most closely matches the student's
     * skills, using the same transparent skill-overlap logic as internship matching.
     * Returns empty if the student hasn't listed any skills yet, or none overlap -
     * callers should fall back to showing the generic roadmap list in that case.
     */
    public Optional<Roadmap> recommendForSkills(String studentSkillsCsv) {
        List<Roadmap> roadmaps = findAll();
        if (roadmaps.isEmpty()) return Optional.empty();

        if (SkillMatcher.toSkillSet(studentSkillsCsv).isEmpty()) {
            return Optional.empty();
        }

        return roadmaps.stream()
                .map(r -> Map.entry(r, SkillMatcher.overlapCount(studentSkillsCsv, r.getRelatedSkills())))
                .filter(e -> e.getValue() > 0)
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    /**
     * Toggles a step's completion and, if the student just crossed a progress
     * milestone (25/50/75/100%) on that roadmap, sends an in-app notification.
     */
    public void toggleStep(User student, Long stepId) {
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));
        Roadmap roadmap = step.getRoadmap();

        int beforePercent = percentComplete(student, roadmap);

        RoadmapProgress progress = progressRepository.findByStudentAndStepId(student, stepId)
                .orElseGet(() -> {
                    RoadmapProgress p = new RoadmapProgress();
                    p.setStudent(student);
                    p.setStep(step);
                    return p;
                });

        progress.setCompleted(!progress.isCompleted());
        progress.setCompletedAt(progress.isCompleted() ? LocalDateTime.now() : null);
        progressRepository.save(progress);

        int afterPercent = percentComplete(student, roadmap);
        notifyIfMilestoneCrossed(student, roadmap, beforePercent, afterPercent);
    }

    private void notifyIfMilestoneCrossed(User student, Roadmap roadmap, int before, int after) {
        for (int milestone : MILESTONES) {
            if (before < milestone && after >= milestone) {
                String message = milestone == 100
                        ? "You completed the \"" + roadmap.getTitle() + "\" roadmap! 🎉"
                        : "You're " + milestone + "% through the \"" + roadmap.getTitle() + "\" roadmap.";
                notificationService.notify(student, message, "/roadmaps/" + roadmap.getId());
            }
        }
    }

    public Roadmap save(Roadmap roadmap) {
        return roadmapRepository.save(roadmap);
    }
}
