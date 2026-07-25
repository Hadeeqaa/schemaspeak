package com.schemaspeak.schemaspeak;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

@Service
public class SchemaIntrospectionService {

    private final DataSource dataSource;

    public SchemaIntrospectionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getSchemaAsMermaid() {
        StringBuilder mermaid = new StringBuilder("erDiagram\n");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            // H2 stores unquoted identifiers as uppercase by default
            ResultSet tables = meta.getTables(null, "PUBLIC", "%", new String[]{"TABLE"});
            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }

            for (String table : tableNames) {
                mermaid.append("    ").append(table).append(" {\n");
                ResultSet columns = meta.getColumns(null, "PUBLIC", table, "%");
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    String colType = columns.getString("TYPE_NAME");
                    mermaid.append("        ").append(colType).append(" ").append(colName).append("\n");
                }
                mermaid.append("    }\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to introspect schema: " + e.getMessage(), e);
        }

        return mermaid.toString();
    }
}