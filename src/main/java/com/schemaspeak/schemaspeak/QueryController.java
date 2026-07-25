package com.schemaspeak.schemaspeak;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class QueryController {

    private final LlmService llmService;
    private final DdlValidationService validationService;
    private final SchemaExecutionService executionService;
    private final SchemaIntrospectionService introspectionService;

    public QueryController(LlmService llmService, DdlValidationService validationService,
            SchemaExecutionService executionService, SchemaIntrospectionService introspectionService) {
        this.llmService = llmService;
        this.validationService = validationService;
        this.executionService = executionService;
        this.introspectionService = introspectionService;
    }

    @PostMapping("/interpret")
    public InterpretResponse interpret(@RequestBody InterpretRequest request) {
        String proposedDdl = llmService.getProposedDdl(request.getSentence());
        var validation = validationService.validate(proposedDdl);

        if (!validation.valid()) {
            return new InterpretResponse(request.getSentence(), "REJECTED: " + validation.reason());
        }

        try {
            executionService.execute(validation.ddl());
            return new InterpretResponse(request.getSentence(), "APPLIED: " + validation.ddl());
        } catch (Exception e) {
            return new InterpretResponse(request.getSentence(), "EXECUTION FAILED: " + e.getMessage());
        }
    }

    @GetMapping("/schema")
    public String getSchema() {
        return introspectionService.getSchemaAsMermaid();
    }
}