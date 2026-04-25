package com.releasemgmt.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeploymentDto {
    private Long componentId;
    private String componentName;
    private String version;
    private Long releaseId;
    private String releaseName;
    private LocalDateTime deployedAt;
}
