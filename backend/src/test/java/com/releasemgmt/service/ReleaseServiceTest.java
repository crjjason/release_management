package com.releasemgmt.service;

import com.releasemgmt.dto.*;
import com.releasemgmt.model.Component;
import com.releasemgmt.model.Environment;
import com.releasemgmt.model.Release;
import com.releasemgmt.model.ReleaseStatus;
import com.releasemgmt.repository.ComponentRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ReleaseService releaseService;

    private Release release;
    private Environment sitEnv;
    private Environment uatEnv;
    private Component component;

    @BeforeEach
    void setUp() {
        sitEnv = new Environment(1L, "SIT1", "SIT", true);
        uatEnv = new Environment(2L, "UAT1", "UAT", true);
        component = new Component(1L, "app", "http://pipe", "team");

        release = new Release();
        release.setId(1L);
        release.setName("Release 1");
        release.setStatus(ReleaseStatus.NEW);
    }

    @Test
    void createRelease_shouldSetStatusToNew() {
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseResponseDto result = releaseService.createRelease(new ReleaseRequestDto() {{ setName("Release 1"); }});

        assertEquals("Release 1", result.getName());
        assertEquals(ReleaseStatus.NEW, result.getStatus());
    }

    @Test
    void updateStatus_newToPlanned_shouldSucceed() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.PLANNED);

        ReleaseResponseDto result = releaseService.updateStatus(1L, dto);

        assertEquals(ReleaseStatus.PLANNED, result.getStatus());
    }

    @Test
    void updateStatus_plannedToInProgress_shouldSucceed() {
        release.setStatus(ReleaseStatus.PLANNED);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.IN_PROGRESS);

        ReleaseResponseDto result = releaseService.updateStatus(1L, dto);

        assertEquals(ReleaseStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void updateStatus_inProgressToFinished_shouldSucceed() {
        release.setStatus(ReleaseStatus.IN_PROGRESS);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.FINISHED);

        ReleaseResponseDto result = releaseService.updateStatus(1L, dto);

        assertEquals(ReleaseStatus.FINISHED, result.getStatus());
    }

    @Test
    void updateStatus_newToCancelled_shouldSucceed() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.CANCELLED);

        ReleaseResponseDto result = releaseService.updateStatus(1L, dto);

        assertEquals(ReleaseStatus.CANCELLED, result.getStatus());
    }

    @Test
    void updateStatus_finishedToAnything_shouldFail() {
        release.setStatus(ReleaseStatus.FINISHED);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.CANCELLED);

        assertThrows(IllegalStateException.class, () -> releaseService.updateStatus(1L, dto));
    }

    @Test
    void updateStatus_cancelledToAnything_shouldFail() {
        release.setStatus(ReleaseStatus.CANCELLED);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.NEW);

        assertThrows(IllegalStateException.class, () -> releaseService.updateStatus(1L, dto));
    }

    @Test
    void updateStatus_invalidTransition_shouldFail() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.IN_PROGRESS);

        assertThrows(IllegalStateException.class, () -> releaseService.updateStatus(1L, dto));
    }

    @Test
    void assignEnvironments_shouldSucceed() {
        release.setStatus(ReleaseStatus.PLANNED);
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(1L)).thenReturn(Optional.of(sitEnv));
        when(environmentRepository.findById(2L)).thenReturn(Optional.of(uatEnv));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseAssignmentDto dto = new ReleaseAssignmentDto();
        dto.setSitEnvironmentId(1L);
        dto.setUatEnvironmentId(2L);

        ReleaseResponseDto result = releaseService.assignEnvironments(1L, dto);

        assertNotNull(result.getSitEnvironment());
        assertNotNull(result.getUatEnvironment());
    }

    @Test
    void updateReleaseArtifacts_newStatus_shouldSucceed() {
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(componentRepository.findAllById(any())).thenReturn(List.of(component));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseArtifactsUpdateDto dto = new ReleaseArtifactsUpdateDto();
        ReleaseArtifactRequestDto artDto = new ReleaseArtifactRequestDto();
        artDto.setComponentId(1L);
        artDto.setVersion("2.0.0");
        artDto.setOwner("team");
        dto.setArtifacts(List.of(artDto));

        ReleaseResponseDto result = releaseService.updateReleaseArtifacts(1L, dto);

        assertEquals(1, result.getArtifacts().size());
        assertEquals("2.0.0", result.getArtifacts().get(0).getVersion());
    }

    @Test
    void updateReleaseArtifacts_inProgressStatus_shouldSucceed() {
        release.setStatus(ReleaseStatus.IN_PROGRESS);
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(componentRepository.findAllById(any())).thenReturn(List.of(component));
        when(releaseRepository.save(any())).thenReturn(release);

        ReleaseArtifactsUpdateDto dto = new ReleaseArtifactsUpdateDto();
        ReleaseArtifactRequestDto artDto = new ReleaseArtifactRequestDto();
        artDto.setComponentId(1L);
        artDto.setVersion("2.0.0");
        artDto.setOwner("team");
        dto.setArtifacts(List.of(artDto));

        ReleaseResponseDto result = releaseService.updateReleaseArtifacts(1L, dto);

        assertEquals(1, result.getArtifacts().size());
    }

    @Test
    void updateReleaseArtifacts_finishedStatus_shouldFail() {
        release.setStatus(ReleaseStatus.FINISHED);
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));

        ReleaseArtifactsUpdateDto dto = new ReleaseArtifactsUpdateDto();
        ReleaseArtifactRequestDto artDto = new ReleaseArtifactRequestDto();
        artDto.setComponentId(1L);
        artDto.setVersion("2.0.0");
        artDto.setOwner("team");
        dto.setArtifacts(List.of(artDto));

        assertThrows(IllegalStateException.class, () -> releaseService.updateReleaseArtifacts(1L, dto));
    }

    @Test
    void updateReleaseArtifacts_missingComponent_shouldFail() {
        when(releaseRepository.findByIdWithArtifactsAndEnvironments(1L)).thenReturn(Optional.of(release));
        when(componentRepository.findAllById(any())).thenReturn(List.of());

        ReleaseArtifactsUpdateDto dto = new ReleaseArtifactsUpdateDto();
        ReleaseArtifactRequestDto artDto = new ReleaseArtifactRequestDto();
        artDto.setComponentId(99L);
        artDto.setVersion("2.0.0");
        artDto.setOwner("team");
        dto.setArtifacts(List.of(artDto));

        assertThrows(IllegalArgumentException.class, () -> releaseService.updateReleaseArtifacts(1L, dto));
    }
}
