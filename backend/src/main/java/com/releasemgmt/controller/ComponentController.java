package com.releasemgmt.controller;

import com.releasemgmt.dto.ComponentRequestDto;
import com.releasemgmt.dto.ComponentResponseDto;
import com.releasemgmt.service.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    @GetMapping
    public List<ComponentResponseDto> getAllComponents() {
        return componentService.getAllComponents();
    }

    @GetMapping("/{id}")
    public ComponentResponseDto getComponent(@PathVariable Long id) {
        return componentService.getComponent(id);
    }

    @PostMapping
    public ComponentResponseDto createComponent(@RequestBody ComponentRequestDto dto) {
        return componentService.createComponent(dto);
    }
}
