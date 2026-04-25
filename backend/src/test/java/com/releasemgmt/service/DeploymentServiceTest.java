package com.releasemgmt.service;

import com.releasemgmt.dto.DeploymentDto;
import com.releasemgmt.model.*;
import com.releasemgmt.repository.DeploymentRepository;
import com.releasemgmt.repository.EnvironmentRepository;
import com.releasemgmt.repository.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private DeploymentService deploymentService;

    private Release release;
    private Environment environment;
    private Component component;
    private ReleaseArtifact artifact;

    @BeforeEach
    void setUp() {
        environment = new Environment(1L, "SIT1", "SIT", true);
        component = new Component(1L, "payment-service", "http://pipe", "team");

        release = new Release();
        release.setId(1L);
        release.setName("Release 1");
        release.setStatus(ReleaseStatus.IN_PROGRESS);
        release.setSitEnvironment(environment);

        artifact = new ReleaseArtifact();
        artifact.setId(1L);
        artifact.setRelease(release);
        artifact.setComponent(component);
        artifact.setVersion("1.2.0");
        artifact.setOwner("team");

        release.setReleaseArtifacts(new java.util.ArrayList<>(List.of(artifact)));
    }

    @Test
    void deployRelease_shouldCreateNewDeployment() {
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(1L)).thenReturn(Optional.of(environment));
        when(deploymentRepository.findByEnvironmentIdAndComponentId(1L, 1L)).thenReturn(Optional.empty());
        when(deploymentRepository.save(any())).thenAnswer(inv -> {
            Deployment d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        List<DeploymentDto> result = deploymentService.deployRelease(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("payment-service", result.get(0).getComponentName());
        assertEquals("1.2.0", result.get(0).getVersion());
        assertEquals(1L, result.get(0).getReleaseId());
    }

    @Test
    void deployRelease_shouldOverrideExistingDeployment() {
        Deployment existing = new Deployment();
        existing.setId(1L);
        existing.setEnvironment(environment);
        existing.setComponent(component);
        existing.setVersion("1.1.0");

        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(1L)).thenReturn(Optional.of(environment));
        when(deploymentRepository.findByEnvironmentIdAndComponentId(1L, 1L)).thenReturn(Optional.of(existing));
        when(deploymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<DeploymentDto> result = deploymentService.deployRelease(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("1.2.0", result.get(0).getVersion());
        assertEquals(1L, result.get(0).getReleaseId());
    }

    @Test
    void deployRelease_releaseNotFound_shouldFail() {
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deploymentService.deployRelease(1L, 1L));
    }

    @Test
    void deployRelease_environmentNotFound_shouldFail() {
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deploymentService.deployRelease(1L, 1L));
    }

    @Test
    void deployRelease_disabledEnvironment_shouldFail() {
        environment.setActive(false);
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(1L)).thenReturn(Optional.of(environment));

        assertThrows(IllegalStateException.class, () -> deploymentService.deployRelease(1L, 1L));
    }

    @Test
    void deployRelease_noArtifacts_shouldFail() {
        release.setReleaseArtifacts(new java.util.ArrayList<>());
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(1L)).thenReturn(Optional.of(environment));

        assertThrows(IllegalStateException.class, () -> deploymentService.deployRelease(1L, 1L));
    }

    @Test
    void getDeploymentsForEnvironment_shouldReturnDeployments() {
        Deployment d = new Deployment();
        d.setId(1L);
        d.setEnvironment(environment);
        d.setComponent(component);
        d.setVersion("1.2.0");
        d.setRelease(release);

        when(environmentRepository.findById(1L)).thenReturn(Optional.of(environment));
        when(deploymentRepository.findByEnvironmentId(1L)).thenReturn(List.of(d));

        List<DeploymentDto> result = deploymentService.getDeploymentsForEnvironment(1L);

        assertEquals(1, result.size());
        assertEquals("payment-service", result.get(0).getComponentName());
        assertEquals("1.2.0", result.get(0).getVersion());
    }
}
