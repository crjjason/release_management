package com.releasemgmt.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "pipeline_url")
    private String pipelineUrl;

    @Column(nullable = false)
    private String owner;
}
