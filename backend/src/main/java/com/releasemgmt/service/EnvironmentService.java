package com.releasemgmt.service;

import com.releasemgmt.dto.EnvironmentDto;
import com.releasemgmt.dto.EnvironmentRequestDto;
import com.releasemgmt.model.Environment;
import com.releasemgmt.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;

    @Transactional(readOnly = true)
    public List<EnvironmentDto> getAllEnvironments() {
        return environmentRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnvironmentDto> getActiveEnvironments() {
        return environmentRepository.findAll().stream()
                .filter(Environment::isActive)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public EnvironmentDto createEnvironment(EnvironmentRequestDto dto) {
        String name = dto.getName() != null ? dto.getName().trim() : "";
        String type = dto.getType() != null ? dto.getType().trim() : "";

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Environment name is required");
        }
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Environment type is required");
        }

        environmentRepository.findByName(name)
                .ifPresent(e -> {
                    throw new IllegalArgumentException("Environment name already exists: " + name);
                });

        Environment env = Environment.builder()
                .name(name)
                .type(type)
                .active(true)
                .build();

        try {
            Environment saved = environmentRepository.save(env);
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Environment name already exists: " + name);
        }
    }

    @Transactional
    public EnvironmentDto toggleEnvironment(Long id) {
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + id));
        env.setActive(!env.isActive());
        Environment saved = environmentRepository.save(env);
        return toDto(saved);
    }

    private EnvironmentDto toDto(Environment env) {
        return EnvironmentDto.builder()
                .id(env.getId())
                .name(env.getName())
                .type(env.getType())
                .active(env.isActive())
                .build();
    }
}
