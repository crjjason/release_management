package com.releasemgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "deployments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"environment_id", "component_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deployment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(nullable = false)
    private String version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id")
    private Release release;

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        deployedAt = LocalDateTime.now();
    }
}
