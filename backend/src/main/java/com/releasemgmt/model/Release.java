package com.releasemgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "releases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Release {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sit_environment_id")
    private Environment sitEnvironment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uat_environment_id")
    private Environment uatEnvironment;

    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReleaseArtifact> releaseArtifacts = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ReleaseStatus.NEW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
