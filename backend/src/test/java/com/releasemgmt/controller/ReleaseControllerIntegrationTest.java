package com.releasemgmt.controller;

import com.releasemgmt.dto.*;
import com.releasemgmt.model.ReleaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReleaseControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createRelease_shouldReturnNewRelease() {
        ReleaseRequestDto dto = new ReleaseRequestDto();
        dto.setName("Integration Test Release");

        ResponseEntity<ReleaseResponseDto> response =
                restTemplate.postForEntity("/api/releases", dto, ReleaseResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Release", response.getBody().getName());
        assertEquals(ReleaseStatus.NEW, response.getBody().getStatus());
    }

    @Test
    void getAllReleases_shouldReturnList() {
        createRelease("Release A");
        createRelease("Release B");

        ResponseEntity<List<ReleaseResponseDto>> response = restTemplate.exchange(
                "/api/releases", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    @Test
    void updateStatus_newToPlanned_shouldSucceed() {
        ReleaseResponseDto release = createRelease("Status Test");

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.PLANNED);

        ResponseEntity<ReleaseResponseDto> response = restTemplate.exchange(
                "/api/releases/" + release.getId() + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(dto),
                ReleaseResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ReleaseStatus.PLANNED, response.getBody().getStatus());
    }

    @Test
    void updateStatus_invalidTransition_shouldFail() {
        ReleaseResponseDto release = createRelease("Invalid Transition");

        ReleaseStatusUpdateDto dto = new ReleaseStatusUpdateDto();
        dto.setStatus(ReleaseStatus.FINISHED);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/releases/" + release.getId() + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(dto),
                String.class);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void assignEnvironments_shouldSucceed() {
        // Create environments
        EnvironmentRequestDto sitRequest = new EnvironmentRequestDto();
        sitRequest.setName("SIT-REL");
        sitRequest.setType("SIT");
        ResponseEntity<EnvironmentDto> sitResponse = restTemplate.postForEntity("/api/environments", sitRequest, EnvironmentDto.class);
        assertNotNull(sitResponse.getBody());
        Long sitEnvId = sitResponse.getBody().getId();

        EnvironmentRequestDto uatRequest = new EnvironmentRequestDto();
        uatRequest.setName("UAT-REL");
        uatRequest.setType("UAT");
        ResponseEntity<EnvironmentDto> uatResponse = restTemplate.postForEntity("/api/environments", uatRequest, EnvironmentDto.class);
        assertNotNull(uatResponse.getBody());
        Long uatEnvId = uatResponse.getBody().getId();

        // Create and plan release
        ReleaseResponseDto release = createRelease("Assignment Test");
        ReleaseStatusUpdateDto statusDto = new ReleaseStatusUpdateDto();
        statusDto.setStatus(ReleaseStatus.PLANNED);
        restTemplate.exchange("/api/releases/" + release.getId() + "/status",
                HttpMethod.PUT, new HttpEntity<>(statusDto), ReleaseResponseDto.class);

        // Assign environments only
        ReleaseAssignmentDto assignDto = new ReleaseAssignmentDto();
        assignDto.setSitEnvironmentId(sitEnvId);
        assignDto.setUatEnvironmentId(uatEnvId);

        ResponseEntity<ReleaseResponseDto> response = restTemplate.exchange(
                "/api/releases/" + release.getId() + "/assign",
                HttpMethod.PUT,
                new HttpEntity<>(assignDto),
                ReleaseResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("SIT-REL", response.getBody().getSitEnvironment().getName());
        assertEquals("UAT-REL", response.getBody().getUatEnvironment().getName());
    }

    @Test
    void updateReleaseArtifacts_shouldSucceed() {
        // Create component first
        ComponentRequestDto compDto = new ComponentRequestDto();
        compDto.setName("app");
        compDto.setOwner("team");
        ResponseEntity<ComponentResponseDto> compResponse =
                restTemplate.postForEntity("/api/components", compDto, ComponentResponseDto.class);
        assertNotNull(compResponse.getBody());
        Long componentId = compResponse.getBody().getId();

        // Create release
        ReleaseResponseDto release = createRelease("Artifact Update Test");

        // Update artifacts
        ReleaseArtifactsUpdateDto dto = new ReleaseArtifactsUpdateDto();
        ReleaseArtifactRequestDto artDto = new ReleaseArtifactRequestDto();
        artDto.setComponentId(componentId);
        artDto.setVersion("2.0.0");
        artDto.setOwner("team");
        dto.setArtifacts(List.of(artDto));

        ResponseEntity<ReleaseResponseDto> response = restTemplate.exchange(
                "/api/releases/" + release.getId() + "/artifacts",
                HttpMethod.PUT,
                new HttpEntity<>(dto),
                ReleaseResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getArtifacts().size());
        assertEquals("2.0.0", response.getBody().getArtifacts().get(0).getVersion());
    }

    private ReleaseResponseDto createRelease(String name) {
        ReleaseRequestDto dto = new ReleaseRequestDto();
        dto.setName(name);
        ResponseEntity<ReleaseResponseDto> response =
                restTemplate.postForEntity("/api/releases", dto, ReleaseResponseDto.class);
        assertEquals(200, response.getStatusCode().value());
        return response.getBody();
    }
}
