package com.taskforge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures schema changes apply cleanly on file-based H2 databases
 * that already contain data from before a column was added.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaUpdater implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureMaxMembersColumn();
    }

    private void ensureMaxMembersColumn() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE projects ADD COLUMN IF NOT EXISTS max_members INTEGER DEFAULT 4");
            int updated = jdbcTemplate.update(
                    "UPDATE projects SET max_members = 4 WHERE max_members IS NULL");
            if (updated > 0) {
                log.info("Backfilled max_members for {} existing project(s)", updated);
            }
        } catch (Exception ex) {
            log.warn("Schema update for max_members skipped: {}", ex.getMessage());
        }
    }
}
