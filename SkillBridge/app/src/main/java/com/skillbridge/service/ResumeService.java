package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeScoreService resumeScoreService;
    private final UserService userService;

    public Resume findOrCreate(User user) {
        return resumeRepository.findByUser(user).orElseGet(() -> {
            Resume resume = new Resume();
            resume.setUser(user);
            return resume;
        });
    }

    public Resume save(User user, Resume incoming, List<ResumeEducation> education,
                        List<ResumeExperience> experience, List<ResumeProject> projects, String skillsCsv) {
        Resume resume = findOrCreate(user);
        resume.setHeadline(incoming.getHeadline());
        resume.setSummary(incoming.getSummary());
        resume.setGithub(incoming.getGithub());
        resume.setLinkedin(incoming.getLinkedin());
        resume.setPortfolio(incoming.getPortfolio());
        resume.setUpdatedAt(LocalDateTime.now());

        resume.getEducation().clear();
        for (ResumeEducation e : education) {
            e.setResume(resume);
            resume.getEducation().add(e);
        }

        resume.getExperience().clear();
        for (ResumeExperience e : experience) {
            e.setResume(resume);
            resume.getExperience().add(e);
        }

        resume.getProjects().clear();
        for (ResumeProject p : projects) {
            p.setResume(resume);
            resume.getProjects().add(p);
        }

        resume.setScore(resumeScoreService.calculate(resume, skillsCsv));
        Resume saved = resumeRepository.save(resume);

        if (skillsCsv != null && !skillsCsv.isBlank()) {
            user.setSkills(skillsCsv);
            userService.save(user);
        }

        return saved;
    }

    public double averageScore() {
        List<Resume> all = resumeRepository.findAll();
        if (all.isEmpty()) return 0.0;
        double total = all.stream().mapToInt(Resume::getScore).sum();
        return Math.round((total / all.size()) * 100.0) / 100.0;
    }
}
