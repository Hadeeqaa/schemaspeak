package com.schemaspeak.schemaspeak;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DdlValidationService {

    // Only these DDL verbs are allowed to run for now
    private static final List<String> ALLOWED_PREFIXES = List.of(
        "ALTER TABLE",
        "CREATE TABLE"
    );

    // Anything containing these words is rejected outright, regardless of allowed prefixes
    private static final List<String> BLOCKED_KEYWORDS = List.of(
        "DROP", "TRUNCATE", "DELETE", "UPDATE", "INSERT", "GRANT", "REVOKE"
    );

    public ValidationResult validate(String ddl) {
        if (ddl == null || ddl.isBlank() || ddl.equalsIgnoreCase("NONE")) {
            return ValidationResult.rejected("Sentence did not produce a valid schema change.");
        }

        String cleaned = ddl.trim();
        String upper = cleaned.toUpperCase();

        for (String blocked : BLOCKED_KEYWORDS) {
            if (upper.contains(blocked)) {
                return ValidationResult.rejected("Statement contains a disallowed operation: " + blocked);
            }
        }

        boolean allowedPrefix = ALLOWED_PREFIXES.stream().anyMatch(upper::startsWith);
        if (!allowedPrefix) {
            return ValidationResult.rejected("Only ALTER TABLE and CREATE TABLE statements are permitted.");
        }

        // Basic sanity check: must end in a semicolon or be a clean single statement (no chained statements)
        if (cleaned.contains(";") && cleaned.indexOf(';') != cleaned.length() - 1) {
            return ValidationResult.rejected("Multiple statements are not allowed.");
        }

        return ValidationResult.accepted(cleaned);
    }

    public record ValidationResult(boolean valid, String ddl, String reason) {
        public static ValidationResult accepted(String ddl) {
            return new ValidationResult(true, ddl, null);
        }
        public static ValidationResult rejected(String reason) {
            return new ValidationResult(false, null, reason);
        }
    }
}