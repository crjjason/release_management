package com.releasemgmt.service;

import com.releasemgmt.dto.ComponentRequestDto;
import com.releasemgmt.dto.ComponentResponseDto;
import com.releasemgmt.model.Component;
import com.releasemgmt.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComponentService {

    private final ComponentRepository componentRepository;

    public List<ComponentResponseDto> getAllComponents() {
        return componentRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public ComponentResponseDto getComponent(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Component not found: " + id));
        return toDto(component);
    }

    public ComponentResponseDto createComponent(ComponentRequestDto dto) {
        Component component = Component.builder()
                .name(dto.getName())
                .pipelineUrl(dto.getPipelineUrl())
                .owner(dto.getOwner())
                .build();
        Component saved = componentRepository.save(component);
        return toDto(saved);
    }

    private ComponentResponseDto toDto(Component component) {
        return ComponentResponseDto.builder()
                .id(component.getId())
                .name(component.getName())
                .pipelineUrl(component.getPipelineUrl())
                .owner(component.getOwner())
                .build();
    }
}
