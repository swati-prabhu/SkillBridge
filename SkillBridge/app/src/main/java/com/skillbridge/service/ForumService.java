package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.ForumCommentRepository;
import com.skillbridge.repository.ForumPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;

    public List<ForumPost> findVisible() {
        return forumPostRepository.findByHiddenFalseOrderByCreatedAtDesc();
    }

    public List<ForumPost> findVisibleByCategory(ForumCategory category) {
        return forumPostRepository.findByHiddenFalseAndCategoryOrderByCreatedAtDesc(category);
    }

    public List<ForumPost> findAllForModeration() {
        return forumPostRepository.findAllByOrderByCreatedAtDesc();
    }

    public ForumPost findById(Long id) {
        return forumPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
    }

    public ForumPost createPost(User author, String title, String body, ForumCategory category, String companyTag) {
        ForumPost post = new ForumPost();
        post.setAuthor(author);
        post.setTitle(title);
        post.setBody(body);
        post.setCategory(category);
        post.setCompanyTag(companyTag);
        return forumPostRepository.save(post);
    }

    public ForumComment addComment(User author, Long postId, String body) {
        ForumPost post = findById(postId);
        ForumComment comment = new ForumComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setBody(body);
        return forumCommentRepository.save(comment);
    }

    public void setHidden(Long postId, boolean hidden) {
        ForumPost post = findById(postId);
        post.setHidden(hidden);
        forumPostRepository.save(post);
    }

    public void flag(Long postId) {
        ForumPost post = findById(postId);
        post.setFlagged(true);
        forumPostRepository.save(post);
    }
}
