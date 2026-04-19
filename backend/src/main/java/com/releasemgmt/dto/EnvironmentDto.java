package com.releasemgmt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnvironmentDto {
    private Long id;
    private String name;
    private String type;
    private boolean active;
}
