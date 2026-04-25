package com.releasemgmt.dto;

import lombok.Data;

@Data
public class DeployReleaseRequestDto {
    private Long releaseId;
    private Long environmentId;
}
