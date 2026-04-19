package com.releasemgmt.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "environments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Environment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
