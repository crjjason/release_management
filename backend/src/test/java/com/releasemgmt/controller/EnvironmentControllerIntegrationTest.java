package com.releasemgmt.controller;

import com.releasemgmt.dto.EnvironmentDto;
import com.releasemgmt.dto.EnvironmentRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EnvironmentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllEnvironments_shouldReturnEmptyInitially() {
        ResponseEntity<List<EnvironmentDto>> response = restTemplate.exchange(
                "/api/environments", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void createEnvironment_shouldSucceed() {
        EnvironmentRequestDto request = new EnvironmentRequestDto();
        request.setName("SIT1");
        request.setType("SIT");

        ResponseEntity<EnvironmentDto> response = restTemplate.postForEntity(
                "/api/environments", request, EnvironmentDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("SIT1", response.getBody().getName());
        assertEquals("SIT", response.getBody().getType());
        assertTrue(response.getBody().isActive());
    }

    @Test
    void toggleEnvironment_shouldToggleActiveState() {
        EnvironmentRequestDto request = new EnvironmentRequestDto();
        request.setName("UAT1");
        request.setType("UAT");

        ResponseEntity<EnvironmentDto> createResponse = restTemplate.postForEntity(
                "/api/environments", request, EnvironmentDto.class);
        assertNotNull(createResponse.getBody());
        Long id = createResponse.getBody().getId();

        ResponseEntity<EnvironmentDto> toggleResponse = restTemplate.exchange(
                "/api/environments/" + id + "/toggle", HttpMethod.PUT, null, EnvironmentDto.class);

        assertEquals(200, toggleResponse.getStatusCode().value());
        assertNotNull(toggleResponse.getBody());
        assertFalse(toggleResponse.getBody().isActive());
    }
}
