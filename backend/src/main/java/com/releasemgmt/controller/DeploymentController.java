package com.releasemgmt.controller;

import com.releasemgmt.dto.DeployReleaseRequestDto;
import com.releasemgmt.dto.DeploymentDto;
import com.releasemgmt.service.DeploymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentService deploymentService;

    @PostMapping
    public List<DeploymentDto> deployRelease(@RequestBody DeployReleaseRequestDto dto) {
        return deploymentService.deployRelease(dto.getReleaseId(), dto.getEnvironmentId());
    }
}
