package com.releasemgmt.service;

import com.releasemgmt.dto.*;
import com.releasemgmt.model.Component;
import com.releasemgmt.model.Environment;
import com.releasemgmt.model.Release;
import com.releasemgmt.model.ReleaseArtifact;
import com.releasemgmt.model.ReleaseStatus;
import com.releasemgmt.repository.ComponentRepository;
import com.releasemgmt.repository.EnvironmentRepository;
import com.releasemgmt.repository.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final EnvironmentRepository environmentRepository;
    private final ComponentRepository componentRepository;

    @Transactional(readOnly = true)
    public List<ReleaseResponseDto> getAllReleases() {
        return releaseRepository.findAllWithArtifactsAndEnvironments().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReleaseResponseDto getRelease(Long id) {
        Release release = releaseRepository.findByIdWithArtifactsAndEnvironments(id)
                .orElseThrow(() -> new IllegalArgumentException("Release not found: " + id));
        return toDto(release);
    }

    @Transactional
    public ReleaseResponseDto createRelease(ReleaseRequestDto dto) {
        String name = dto.getName() != null ? dto.getName().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Release name is required");
        }

        Release release = Release.builder()
                .name(name)
                .status(ReleaseStatus.NEW)
                .build();
        Release saved = releaseRepository.save(release);
        return toDto(saved);
    }

    @Transactional
    public ReleaseResponseDto updateStatus(Long id, ReleaseStatusUpdateDto dto) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Release not found: " + id));

        ReleaseStatus current = release.getStatus();
        ReleaseStatus next = dto.getStatus();

        if (!isValidTransition(current, next)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + current + " to " + next);
        }

        release.setStatus(next);
        Release saved = releaseRepository.save(release);
        return toDto(saved);
    }

    @Transactional
    public ReleaseResponseDto assignEnvironments(Long id, ReleaseAssignmentDto dto) {
        Release release = releaseRepository.findByIdWithArtifactsAndEnvironments(id)
                .orElseThrow(() -> new IllegalArgumentException("Release not found: " + id));

        if (release.getStatus() != ReleaseStatus.PLANNED && release.getStatus() != ReleaseStatus.NEW) {
            throw new IllegalStateException("Can only assign environments when release is NEW or PLANNED");
        }

        Environment sitEnv = environmentRepository.findById(dto.getSitEnvironmentId())
                .orElseThrow(() -> new IllegalArgumentException("SIT environment not found: " + dto.getSitEnvironmentId()));
        if (!sitEnv.isActive()) {
            throw new IllegalStateException("SIT environment is disabled: " + sitEnv.getName());
        }

        Environment uatEnv = environmentRepository.findById(dto.getUatEnvironmentId())
                .orElseThrow(() -> new IllegalArgumentException("UAT environment not found: " + dto.getUatEnvironmentId()));
        if (!uatEnv.isActive()) {
            throw new IllegalStateException("UAT environment is disabled: " + uatEnv.getName());
        }

        release.setSitEnvironment(sitEnv);
        release.setUatEnvironment(uatEnv);

        if (dto.getReleaseArtifacts() != null && !dto.getReleaseArtifacts().isEmpty()) {
            updateArtifacts(release, dto.getReleaseArtifacts());
        }

        Release saved = releaseRepository.save(release);
        return toDto(saved);
    }

    @Transactional
    public ReleaseResponseDto updateReleaseArtifacts(Long id, ReleaseArtifactsUpdateDto dto) {
        Release release = releaseRepository.findByIdWithArtifactsAndEnvironments(id)
                .orElseThrow(() -> new IllegalArgumentException("Release not found: " + id));

        if (!canEditArtifacts(release.getStatus())) {
            throw new IllegalStateException("Can only update artifacts when release is NEW, PLANNED, or IN_PROGRESS");
        }

        updateArtifacts(release, dto.getArtifacts());

        Release saved = releaseRepository.save(release);
        return toDto(saved);
    }

    private void updateArtifacts(Release release, List<ReleaseArtifactRequestDto> artifactDtos) {
        Set<Long> componentIds = artifactDtos.stream()
                .map(ReleaseArtifactRequestDto::getComponentId)
                .collect(Collectors.toSet());

        Map<Long, Component> componentMap = componentRepository.findAllById(componentIds).stream()
                .collect(Collectors.toMap(Component::getId, c -> c));

        if (componentMap.size() != componentIds.size()) {
            throw new IllegalArgumentException("One or more components not found");
        }

        release.getReleaseArtifacts().clear();

        for (ReleaseArtifactRequestDto artDto : artifactDtos) {
            Component component = componentMap.get(artDto.getComponentId());
            ReleaseArtifact ra = ReleaseArtifact.builder()
                    .release(release)
                    .component(component)
                    .version(artDto.getVersion())
                    .pipelineUrl(artDto.getPipelineUrl() != null ? artDto.getPipelineUrl() : component.getPipelineUrl())
                    .owner(artDto.getOwner() != null ? artDto.getOwner() : component.getOwner())
                    .build();
            release.getReleaseArtifacts().add(ra);
        }
    }

    private boolean isValidTransition(ReleaseStatus current, ReleaseStatus next) {
        if (current == next) return true;

        return switch (current) {
            case NEW -> next == ReleaseStatus.PLANNED || next == ReleaseStatus.CANCELLED;
            case PLANNED -> next == ReleaseStatus.IN_PROGRESS || next == ReleaseStatus.CANCELLED;
            case IN_PROGRESS -> next == ReleaseStatus.FINISHED || next == ReleaseStatus.CANCELLED;
            case FINISHED, CANCELLED -> false;
        };
    }

    private boolean canEditArtifacts(ReleaseStatus status) {
        return EnumSet.of(ReleaseStatus.NEW, ReleaseStatus.PLANNED, ReleaseStatus.IN_PROGRESS).contains(status);
    }

    private ReleaseResponseDto toDto(Release release) {
        return ReleaseResponseDto.builder()
                .id(release.getId())
                .name(release.getName())
                .status(release.getStatus())
                .sitEnvironment(release.getSitEnvironment() != null ? toEnvDto(release.getSitEnvironment()) : null)
                .uatEnvironment(release.getUatEnvironment() != null ? toEnvDto(release.getUatEnvironment()) : null)
                .artifacts(release.getReleaseArtifacts().stream().map(this::toArtifactDto).toList())
                .createdAt(release.getCreatedAt())
                .updatedAt(release.getUpdatedAt())
                .build();
    }

    private EnvironmentDto toEnvDto(Environment env) {
        return EnvironmentDto.builder()
                .id(env.getId())
                .name(env.getName())
                .type(env.getType())
                .active(env.isActive())
                .build();
    }

    private ReleaseArtifactResponseDto toArtifactDto(ReleaseArtifact ra) {
        return ReleaseArtifactResponseDto.builder()
                .id(ra.getId())
                .componentId(ra.getComponent().getId())
                .componentName(ra.getComponent().getName())
                .version(ra.getVersion())
                .pipelineUrl(ra.getPipelineUrl())
                .owner(ra.getOwner())
                .build();
    }
}
