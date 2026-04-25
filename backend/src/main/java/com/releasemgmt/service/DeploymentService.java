package com.releasemgmt.service;

import com.releasemgmt.dto.DeploymentDto;
import com.releasemgmt.model.Component;
import com.releasemgmt.model.Deployment;
import com.releasemgmt.model.Environment;
import com.releasemgmt.model.Release;
import com.releasemgmt.model.ReleaseArtifact;
import com.releasemgmt.repository.DeploymentRepository;
import com.releasemgmt.repository.EnvironmentRepository;
import com.releasemgmt.repository.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final ReleaseRepository releaseRepository;
    private final EnvironmentRepository environmentRepository;

    @Transactional
    public List<DeploymentDto> deployRelease(Long releaseId, Long environmentId) {
        Release release = releaseRepository.findByIdWithArtifactsAndEnvironments(releaseId)
                .orElseThrow(() -> new IllegalArgumentException("Release not found: " + releaseId));

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + environmentId));

        if (!environment.isActive()) {
            throw new IllegalStateException("Environment is disabled: " + environment.getName());
        }

        if (release.getReleaseArtifacts() == null || release.getReleaseArtifacts().isEmpty()) {
            throw new IllegalStateException("Release has no artifacts to deploy");
        }

        List<DeploymentDto> result = new ArrayList<>();

        for (ReleaseArtifact artifact : release.getReleaseArtifacts()) {
            Component component = artifact.getComponent();

            Deployment deployment = deploymentRepository
                    .findByEnvironmentIdAndComponentId(environmentId, component.getId())
                    .orElseGet(() -> {
                        Deployment d = new Deployment();
                        d.setEnvironment(environment);
                        d.setComponent(component);
                        return d;
                    });

            deployment.setVersion(artifact.getVersion());
            deployment.setRelease(release);

            Deployment saved;
            try {
                saved = deploymentRepository.save(deployment);
            } catch (DataIntegrityViolationException e) {
                Deployment existing = deploymentRepository
                        .findByEnvironmentIdAndComponentId(environmentId, component.getId())
                        .orElseThrow(() -> new IllegalStateException("Concurrent deployment failed"));
                existing.setVersion(artifact.getVersion());
                existing.setRelease(release);
                saved = deploymentRepository.save(existing);
            }
            result.add(toDto(saved));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<DeploymentDto> getDeploymentsForEnvironment(Long environmentId) {
        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + environmentId));

        return deploymentRepository.findByEnvironmentId(environmentId).stream()
                .map(this::toDto)
                .toList();
    }

    private DeploymentDto toDto(Deployment deployment) {
        Release release = deployment.getRelease();
        return DeploymentDto.builder()
                .componentId(deployment.getComponent().getId())
                .componentName(deployment.getComponent().getName())
                .version(deployment.getVersion())
                .releaseId(release != null ? release.getId() : null)
                .releaseName(release != null ? release.getName() : null)
                .deployedAt(deployment.getDeployedAt())
                .build();
    }
}
