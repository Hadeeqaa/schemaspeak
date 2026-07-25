package com.schemaspeak.schemaspeak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LlmService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    public String getProposedDdl(String sentence) {
        String url = "https://generativelanguage.googleapis.com/v1beta/interactions";
        System.out.println("DEBUG - API key being used: " + apiKey);

        String prompt = """
                You convert English sentences into a single MySQL DDL statement.
                Only respond with the raw SQL statement, nothing else, no explanation, no markdown.
                If the sentence doesn't make sense as a schema change, respond with exactly: NONE

                Sentence: %s
                """.formatted(sentence);

        Map<String, Object> requestBody = Map.of(
                "model", "gemini-3.5-flash",
                "input", prompt);

        Map response = restClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        try {
            var steps = (java.util.List) response.get("steps");
            for (Object stepObj : steps) {
                Map step = (Map) stepObj;
                if ("model_output".equals(step.get("type"))) {
                    var content = (java.util.List) step.get("content");
                    Map firstContent = (Map) content.get(0);
                    return ((String) firstContent.get("text")).trim();
                }
            }
            return "NONE";
        } catch (Exception e) {
            System.out.println("Parse error, raw response: " + response);
            return "NONE";
        }
    }
}