package com.releasemgmt.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "release_artifacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(nullable = false)
    private String version;

    @Column(name = "pipeline_url")
    private String pipelineUrl;

    @Column(nullable = false)
    private String owner;
}
