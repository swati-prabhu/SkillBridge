package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(nullable = false, length = 60)
    private String entity;

    @Column(name = "entity_id", length = 60)
    private String entityId;

    @Column(length = 255)
    private String details;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
