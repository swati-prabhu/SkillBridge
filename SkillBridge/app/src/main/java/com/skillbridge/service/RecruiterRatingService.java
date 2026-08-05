package com.skillbridge.service;

import com.skillbridge.entity.Company;
import com.skillbridge.entity.RecruiterRating;
import com.skillbridge.entity.User;
import com.skillbridge.repository.RecruiterRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterRatingService {

    private final RecruiterRatingRepository recruiterRatingRepository;

    public static class RatingSummary {
        public double avgDifficulty;
        public double avgCommunication;
        public double avgProcess;
        public long totalRatings;
        public List<String> comments;
    }

    public boolean hasRated(User student, Company company) {
        return recruiterRatingRepository.findByStudentAndCompany(student, company).isPresent();
    }

    public RecruiterRating submit(User student, Company company, int difficulty, int communication, int process, String comment) {
        if (hasRated(student, company)) {
            throw new IllegalStateException("You've already rated this company.");
        }
        RecruiterRating rating = new RecruiterRating();
        rating.setStudent(student);
        rating.setCompany(company);
        rating.setDifficulty(clamp(difficulty));
        rating.setCommunication(clamp(communication));
        rating.setProcess(clamp(process));
        rating.setComment(comment);
        return recruiterRatingRepository.save(rating);
    }

    public RatingSummary summarize(Company company) {
        List<RecruiterRating> ratings = recruiterRatingRepository.findByCompany(company);
        RatingSummary summary = new RatingSummary();
        summary.totalRatings = ratings.size();
        summary.avgDifficulty = avg(ratings.stream().mapToInt(RecruiterRating::getDifficulty));
        summary.avgCommunication = avg(ratings.stream().mapToInt(RecruiterRating::getCommunication));
        summary.avgProcess = avg(ratings.stream().mapToInt(RecruiterRating::getProcess));
        summary.comments = ratings.stream()
                .map(RecruiterRating::getComment)
                .filter(c -> c != null && !c.isBlank())
                .toList();
        return summary;
    }

    private double avg(java.util.stream.IntStream stream) {
        return Math.round(stream.average().orElse(0) * 10.0) / 10.0;
    }

    private int clamp(int value) {
        return Math.max(1, Math.min(5, value));
    }
}
