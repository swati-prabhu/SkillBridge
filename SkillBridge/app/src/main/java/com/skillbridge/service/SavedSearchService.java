package com.skillbridge.service;

import com.skillbridge.entity.Internship;
import com.skillbridge.entity.SavedSearch;
import com.skillbridge.entity.User;
import com.skillbridge.repository.SavedSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final NotificationService notificationService;

    public List<SavedSearch> findByStudent(User student) {
        return savedSearchRepository.findByStudent(student);
    }

    public SavedSearch save(User student, String keyword, String category) {
        SavedSearch search = new SavedSearch();
        search.setStudent(student);
        search.setKeyword(keyword);
        search.setCategory(category);
        return savedSearchRepository.save(search);
    }

    public void delete(Long id) {
        savedSearchRepository.deleteById(id);
    }

    /** Called whenever a new internship is posted; notifies students whose saved search matches it. */
    public void notifyMatchingSavedSearches(Internship internship) {
        List<SavedSearch> allSearches = savedSearchRepository.findAll();
        for (SavedSearch search : allSearches) {
            if (matches(search, internship)) {
                notificationService.notify(
                        search.getStudent(),
                        "New internship matches your saved search"
                                + (search.getKeyword() != null && !search.getKeyword().isBlank() ? " \"" + search.getKeyword() + "\"" : "")
                                + ": \"" + internship.getTitle() + "\" at " + internship.getCompany() + ".",
                        "/internships/" + internship.getId()
                );
                search.setLastNotifiedAt(LocalDateTime.now());
                savedSearchRepository.save(search);
            }
        }
    }

    private boolean matches(SavedSearch search, Internship internship) {
        boolean keywordMatches = search.getKeyword() == null || search.getKeyword().isBlank()
                || internship.getTitle().toLowerCase().contains(search.getKeyword().toLowerCase())
                || internship.getCompany().toLowerCase().contains(search.getKeyword().toLowerCase())
                || internship.getLocation().toLowerCase().contains(search.getKeyword().toLowerCase());

        boolean categoryMatches = search.getCategory() == null || search.getCategory().isBlank()
                || search.getCategory().equalsIgnoreCase(internship.getCategory());

        return keywordMatches && categoryMatches;
    }
}
