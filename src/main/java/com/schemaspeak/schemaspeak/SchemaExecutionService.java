package com.schemaspeak.schemaspeak;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class SchemaExecutionService {

    private final DataSource dataSource;

    public SchemaExecutionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public void execute(String ddl) {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DDL: " + e.getMessage(), e);
        }
    }
}