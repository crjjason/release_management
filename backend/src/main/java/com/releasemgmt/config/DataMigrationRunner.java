package com.releasemgmt.config;

import com.releasemgmt.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ComponentRepository componentRepository;

    @Override
    public void run(String... args) {
        boolean oldArtifactsTableExists = tableExists("artifacts");
        boolean newComponentsTableExists = tableExists("components");

        if (oldArtifactsTableExists && (!newComponentsTableExists || componentRepository.count() == 0)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT name, pipeline_url, owner FROM artifacts");

            Set<String> seenNames = new HashSet<>();
            for (Map<String, Object> row : rows) {
                String name = (String) row.get("name");
                if (name == null || !seenNames.add(name)) {
                    continue;
                }
                com.releasemgmt.model.Component component = com.releasemgmt.model.Component.builder()
                        .name(name)
                        .pipelineUrl((String) row.get("pipeline_url"))
                        .owner((String) row.get("owner"))
                        .build();
                componentRepository.save(component);
            }

            jdbcTemplate.execute("DROP TABLE IF EXISTS release_artifacts");
            jdbcTemplate.execute("DROP TABLE IF EXISTS artifacts");
        }
    }

    private boolean tableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",
                    Integer.class,
                    tableName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
