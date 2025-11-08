package com.processmonster.bpm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.processmonster.bpm.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for validating form data against JSON Schema
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormValidationService {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    /**
     * Validate JSON data against a JSON Schema
     *
     * @param schemaJson JSON Schema (Draft 7)
     * @param dataJson   Data to validate
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate(String schemaJson, String dataJson) {
        List<String> errors = new ArrayList<>();

        try {
            // Parse schema and data
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonNode dataNode = objectMapper.readTree(dataJson);

            // Create JSON Schema validator
            JsonSchema schema = schemaFactory.getSchema(schemaNode);

            // Validate
            Set<ValidationMessage> validationMessages = schema.validate(dataNode);

            // Convert validation messages to error strings
            for (ValidationMessage msg : validationMessages) {
                errors.add(msg.getMessage());
            }

            log.debug("Validation completed with {} errors", errors.size());

        } catch (Exception e) {
            log.error("Error during form validation", e);
            String errorMsg = getMessage("form.validation.error", e.getMessage());
            errors.add(errorMsg);
        }

        return errors;
    }

    /**
     * Validate and throw exception if invalid
     *
     * @param schemaJson JSON Schema
     * @param dataJson   Data to validate
     * @throws BusinessException if validation fails
     */
    public void validateAndThrow(String schemaJson, String dataJson) {
        List<String> errors = validate(schemaJson, dataJson);
        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            throw new BusinessException(getMessage("form.validation.failed", errorMessage));
        }
    }

    /**
     * Check if JSON is valid
     *
     * @param jsonString JSON string to check
     * @return true if valid JSON
     */
    public boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if JSON Schema is valid
     *
     * @param schemaJson JSON Schema to check
     * @return true if valid JSON Schema
     */
    public boolean isValidJsonSchema(String schemaJson) {
        try {
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            schemaFactory.getSchema(schemaNode);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JSON Schema: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Pretty print JSON for logging/debugging
     *
     * @param jsonString JSON string
     * @return Pretty formatted JSON
     */
    public String prettifyJson(String jsonString) {
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return jsonString;
        }
    }

    /**
     * Get internationalized message
     */
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
