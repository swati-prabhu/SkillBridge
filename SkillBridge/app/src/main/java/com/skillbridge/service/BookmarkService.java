package com.skillbridge.service;

import com.skillbridge.entity.Bookmark;
import com.skillbridge.entity.Internship;
import com.skillbridge.entity.User;
import com.skillbridge.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final InternshipService internshipService;

    public List<Bookmark> findByStudent(User student) {
        return bookmarkRepository.findByStudent(student);
    }

    public boolean isBookmarked(User student, Long internshipId) {
        return bookmarkRepository.existsByStudentAndInternshipId(student, internshipId);
    }

    /** Returns true if now bookmarked, false if it was removed. */
    public boolean toggle(User student, Long internshipId) {
        var existing = bookmarkRepository.findByStudentAndInternshipId(student, internshipId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        }
        Internship internship = internshipService.findById(internshipId);
        Bookmark bookmark = new Bookmark();
        bookmark.setStudent(student);
        bookmark.setInternship(internship);
        bookmarkRepository.save(bookmark);
        return true;
    }
}
