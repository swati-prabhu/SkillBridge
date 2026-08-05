package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roadmap_steps")
@Getter
@Setter
@NoArgsConstructor
public class RoadmapStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 400)
    private String description;

    @Column(name = "resource_link", length = 255)
    private String resourceLink;

    @Column(name = "order_index")
    private int orderIndex;
}
