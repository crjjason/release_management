package com.releasemgmt.controller;

import com.releasemgmt.dto.ComponentRequestDto;
import com.releasemgmt.dto.ComponentResponseDto;
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
class ComponentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createComponent_shouldReturnCreatedComponent() {
        ComponentRequestDto dto = new ComponentRequestDto();
        dto.setName("auth-service");
        dto.setOwner("Platform Team");
        dto.setPipelineUrl("https://ci.example.com/auth");

        ResponseEntity<ComponentResponseDto> response =
                restTemplate.postForEntity("/api/components", dto, ComponentResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("auth-service", response.getBody().getName());
        assertEquals("Platform Team", response.getBody().getOwner());
        assertEquals("https://ci.example.com/auth", response.getBody().getPipelineUrl());
    }

    @Test
    void getAllComponents_shouldReturnComponents() {
        ComponentRequestDto dto = new ComponentRequestDto();
        dto.setName("inventory-api");
        dto.setOwner("Inventory Team");
        restTemplate.postForEntity("/api/components", dto, ComponentResponseDto.class);

        ResponseEntity<List<ComponentResponseDto>> response = restTemplate.exchange(
                "/api/components", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().stream().anyMatch(c -> c.getName().equals("inventory-api")));
    }

    @Test
    void getComponent_shouldReturnSingleComponent() {
        ComponentRequestDto dto = new ComponentRequestDto();
        dto.setName("order-service");
        dto.setOwner("Orders Team");
        ResponseEntity<ComponentResponseDto> created =
                restTemplate.postForEntity("/api/components", dto, ComponentResponseDto.class);
        assertNotNull(created.getBody());
        Long id = created.getBody().getId();

        ResponseEntity<ComponentResponseDto> response =
                restTemplate.getForEntity("/api/components/" + id, ComponentResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("order-service", response.getBody().getName());
    }
}
