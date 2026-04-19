package com.releasemgmt.dto;

import lombok.Data;

@Data
public class ComponentRequestDto {
    private String name;
    private String pipelineUrl;
    private String owner;
}
