package com.releasemgmt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReleaseArtifactResponseDto {
    private Long id;
    private Long componentId;
    private String componentName;
    private String version;
    private String pipelineUrl;
    private String owner;
}
