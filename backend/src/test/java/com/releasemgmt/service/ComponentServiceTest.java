package com.releasemgmt.service;

import com.releasemgmt.dto.ComponentRequestDto;
import com.releasemgmt.dto.ComponentResponseDto;
import com.releasemgmt.model.Component;
import com.releasemgmt.repository.ComponentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentServiceTest {

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ComponentService componentService;

    @Test
    void getAllComponents_shouldReturnAllComponents() {
        Component component = new Component(1L, "app", "http://pipe", "team");
        when(componentRepository.findAll()).thenReturn(List.of(component));

        List<ComponentResponseDto> result = componentService.getAllComponents();

        assertEquals(1, result.size());
        assertEquals("app", result.get(0).getName());
    }

    @Test
    void createComponent_shouldReturnCreatedComponent() {
        ComponentRequestDto dto = new ComponentRequestDto();
        dto.setName("app");
        dto.setPipelineUrl("http://pipe");
        dto.setOwner("team");

        Component saved = new Component(1L, "app", "http://pipe", "team");
        when(componentRepository.save(any())).thenReturn(saved);

        ComponentResponseDto result = componentService.createComponent(dto);

        assertEquals("app", result.getName());
        assertEquals("http://pipe", result.getPipelineUrl());
        assertEquals("team", result.getOwner());
    }
}
