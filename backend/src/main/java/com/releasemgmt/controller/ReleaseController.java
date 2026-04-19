package com.releasemgmt.controller;

import com.releasemgmt.dto.*;
import com.releasemgmt.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseController {

    private final ReleaseService releaseService;

    @GetMapping
    public List<ReleaseResponseDto> getAllReleases() {
        return releaseService.getAllReleases();
    }

    @GetMapping("/{id}")
    public ReleaseResponseDto getRelease(@PathVariable Long id) {
        return releaseService.getRelease(id);
    }

    @PostMapping
    public ReleaseResponseDto createRelease(@RequestBody ReleaseRequestDto dto) {
        return releaseService.createRelease(dto);
    }

    @PutMapping("/{id}/status")
    public ReleaseResponseDto updateStatus(@PathVariable Long id, @RequestBody ReleaseStatusUpdateDto dto) {
        return releaseService.updateStatus(id, dto);
    }

    @PutMapping("/{id}/assign")
    public ReleaseResponseDto assignEnvironments(@PathVariable Long id, @RequestBody ReleaseAssignmentDto dto) {
        return releaseService.assignEnvironments(id, dto);
    }

    @PutMapping("/{id}/artifacts")
    public ReleaseResponseDto updateArtifacts(@PathVariable Long id, @RequestBody ReleaseArtifactsUpdateDto dto) {
        return releaseService.updateReleaseArtifacts(id, dto);
    }
}
