package com.releasemgmt.dto;

import com.releasemgmt.model.ReleaseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReleaseResponseDto {
    private Long id;
    private String name;
    private ReleaseStatus status;
    private EnvironmentDto sitEnvironment;
    private EnvironmentDto uatEnvironment;
    private List<ReleaseArtifactResponseDto> artifacts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
