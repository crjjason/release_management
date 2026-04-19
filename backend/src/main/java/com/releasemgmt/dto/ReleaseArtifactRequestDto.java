package com.releasemgmt.dto;

import lombok.Data;

@Data
public class ReleaseArtifactRequestDto {
    private Long componentId;
    private String version;
    private String pipelineUrl;
    private String owner;
}
