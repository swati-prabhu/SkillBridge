package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aptitude_tests")
@Getter
@Setter
@NoArgsConstructor
public class AptitudeTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 255)
    private String description;

    // ── Coding Contest fields (a "contest" is just a time-boxed test with optional external coding links) ──
    @Column(nullable = false)
    private boolean contest = false;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "coding_links", length = 500)
    private String codingLinks; // comma-separated URLs to external coding problems (LeetCode/HackerRank/etc.)

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @Transient
    public boolean isLive() {
        if (!contest) return true; // regular tests are always "live"
        LocalDateTime now = LocalDateTime.now();
        return (startsAt == null || !now.isBefore(startsAt)) && (endsAt == null || !now.isAfter(endsAt));
    }

    @Transient
    public boolean isUpcoming() {
        return contest && startsAt != null && LocalDateTime.now().isBefore(startsAt);
    }

    @Transient
    public boolean isEnded() {
        return contest && endsAt != null && LocalDateTime.now().isAfter(endsAt);
    }
}
