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
class DeploymentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void deployRelease_shouldCreateDeployments() {
        // Create environments
        EnvironmentRequestDto sitRequest = new EnvironmentRequestDto();
        sitRequest.setName("SIT-DEP");
        sitRequest.setType("SIT");
        ResponseEntity<EnvironmentDto> sitResponse = restTemplate.postForEntity("/api/environments", sitRequest, EnvironmentDto.class);
        assertNotNull(sitResponse.getBody());
        Long sitEnvId = sitResponse.getBody().getId();

        // Create component
        ComponentRequestDto compDto = new ComponentRequestDto();
        compDto.setName("app");
        compDto.setOwner("team");
        ResponseEntity<ComponentResponseDto> compResponse = restTemplate.postForEntity("/api/components", compDto, ComponentResponseDto.class);
        assertNotNull(compResponse.getBody());
        Long componentId = compResponse.getBody().getId();

        // Create release and assign environment + artifacts
        ReleaseResponseDto release = createRelease("Deploy Test");

        ReleaseStatusUpdateDto statusDto = new ReleaseStatusUpdateDto();
        statusDto.setStatus(ReleaseStatus.PLANNED);
        restTemplate.exchange("/api/releases/" + release.getId() + "/status",
                HttpMethod.PUT, new HttpEntity<>(statusDto), ReleaseResponseDto.class);

        ReleaseAssignmentDto assignDto = new ReleaseAssignmentDto();
        assignDto.setSitEnvironmentId(sitEnvId);
        assignDto.setUatEnvironmentId(sitEnvId);
        ReleaseArtifactRequestDto artDto = new ReleaseArtifactRequestDto();
        artDto.setComponentId(componentId);
        artDto.setVersion("2.0.0");
        artDto.setOwner("team");
        assignDto.setReleaseArtifacts(List.of(artDto));

        restTemplate.exchange("/api/releases/" + release.getId() + "/assign",
                HttpMethod.PUT, new HttpEntity<>(assignDto), ReleaseResponseDto.class);

        // Deploy release to SIT
        DeployReleaseRequestDto deployDto = new DeployReleaseRequestDto();
        deployDto.setReleaseId(release.getId());
        deployDto.setEnvironmentId(sitEnvId);

        ResponseEntity<List<DeploymentDto>> response = restTemplate.exchange(
                "/api/deployments", HttpMethod.POST, new HttpEntity<>(deployDto),
                new ParameterizedTypeReference<>() {});

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("2.0.0", response.getBody().get(0).getVersion());
        assertEquals("app", response.getBody().get(0).getComponentName());
    }

    @Test
    void getEnvironmentDeployments_shouldReturnDeployments() {
        // Create environment
        EnvironmentRequestDto envRequest = new EnvironmentRequestDto();
        envRequest.setName("UAT-DEP");
        envRequest.setType("UAT");
        ResponseEntity<EnvironmentDto> envResponse = restTemplate.postForEntity("/api/environments", envRequest, EnvironmentDto.class);
        assertNotNull(envResponse.getBody());
        Long envId = envResponse.getBody().getId();

        // Get deployments (should be empty)
        ResponseEntity<List<DeploymentDto>> response = restTemplate.exchange(
                "/api/environments/" + envId + "/deployments", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
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
