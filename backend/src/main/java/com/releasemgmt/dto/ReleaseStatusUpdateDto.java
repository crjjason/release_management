package com.releasemgmt.dto;

import com.releasemgmt.model.ReleaseStatus;
import lombok.Data;

@Data
public class ReleaseStatusUpdateDto {
    private ReleaseStatus status;
}
