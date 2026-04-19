package com.releasemgmt.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReleaseArtifactsUpdateDto {
    private List<ReleaseArtifactRequestDto> artifacts;
}
