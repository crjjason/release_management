package com.releasemgmt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentResponseDto {
    private Long id;
    private String name;
    private String pipelineUrl;
    private String owner;
}
