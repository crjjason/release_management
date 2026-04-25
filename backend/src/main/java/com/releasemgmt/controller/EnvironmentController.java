package com.releasemgmt.controller;

import com.releasemgmt.dto.DeploymentDto;
import com.releasemgmt.dto.EnvironmentDto;
import com.releasemgmt.dto.EnvironmentRequestDto;
import com.releasemgmt.service.DeploymentService;
import com.releasemgmt.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final DeploymentService deploymentService;

    @GetMapping
    public List<EnvironmentDto> getAllEnvironments() {
        return environmentService.getAllEnvironments();
    }

    @PostMapping
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentRequestDto dto) {
        return environmentService.createEnvironment(dto);
    }

    @PutMapping("/{id}/toggle")
    public EnvironmentDto toggleEnvironment(@PathVariable Long id) {
        return environmentService.toggleEnvironment(id);
    }

    @GetMapping("/{id}/deployments")
    public List<DeploymentDto> getEnvironmentDeployments(@PathVariable Long id) {
        return deploymentService.getDeploymentsForEnvironment(id);
    }
}
