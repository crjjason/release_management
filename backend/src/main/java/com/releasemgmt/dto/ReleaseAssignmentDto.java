package com.releasemgmt.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReleaseAssignmentDto {
    private Long sitEnvironmentId;
    private Long uatEnvironmentId;
    private List<ReleaseArtifactRequestDto> releaseArtifacts;
}
