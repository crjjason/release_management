package com.releasemgmt.config;

import com.releasemgmt.model.Environment;
import com.releasemgmt.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EnvironmentRepository environmentRepository;

    @Override
    public void run(String... args) {
        // No pre-seeded data — environments are managed via the UI
    }
}
