package com.processmonster.bpm.service;

import com.processmonster.bpm.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BpmnValidationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BPMN Validation Service Tests")
class BpmnValidationServiceTest {

    @Mock
    private MessageSource messageSource;

    private BpmnValidationService bpmnValidationService;

    private static final String VALID_BPMN_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                         targetNamespace="http://processmonster.com/bpmn">
              <process id="test-process" name="Test Process" isExecutable="true">
                <startEvent id="startEvent1"/>
                <endEvent id="endEvent1"/>
              </process>
            </definitions>
            """;

    @BeforeEach
    void setUp() {
        bpmnValidationService = new BpmnValidationService(messageSource);

        // Setup default message source responses
        when(messageSource.getMessage(eq("process.validation.empty"), any(), any(), any()))
                .thenReturn("BPMN XML content cannot be empty");
        when(messageSource.getMessage(eq("process.validation.invalid"), any(), any(), any()))
                .thenReturn("Invalid BPMN XML format");
        when(messageSource.getMessage(eq("process.validation.no-definitions"), any(), any(), any()))
                .thenReturn("BPMN XML must have a <definitions> root element");
        when(messageSource.getMessage(eq("process.validation.no-process"), any(), any(), any()))
                .thenReturn("BPMN XML must contain at least one <process> element");
        when(messageSource.getMessage(eq("process.validation.no-id"), any(), any(), any()))
                .thenReturn("Process element must have an 'id' attribute");
    }

    @Test
    @DisplayName("Should validate valid BPMN XML successfully")
    void validateBpmnXml_ShouldSucceed_WhenXmlIsValid() {
        // When/Then - no exception should be thrown
        bpmnValidationService.validateBpmnXml(VALID_BPMN_XML);
    }

    @Test
    @DisplayName("Should throw exception when BPMN XML is null")
    void validateBpmnXml_ShouldThrowException_WhenXmlIsNull() {
        // When/Then
        assertThatThrownBy(() -> bpmnValidationService.validateBpmnXml(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("BPMN XML content cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when BPMN XML is empty")
    void validateBpmnXml_ShouldThrowException_WhenXmlIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> bpmnValidationService.validateBpmnXml(""))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("BPMN XML content cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when XML is malformed")
    void validateBpmnXml_ShouldThrowException_WhenXmlIsMalformed() {
        // Given
        String malformedXml = "<invalid xml without closing tag";

        // When/Then
        assertThatThrownBy(() -> bpmnValidationService.validateBpmnXml(malformedXml))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid BPMN XML format");
    }

    @Test
    @DisplayName("Should throw exception when missing definitions element")
    void validateBpmnXml_ShouldThrowException_WhenMissingDefinitions() {
        // Given
        String xmlWithoutDefinitions = """
                <?xml version="1.0" encoding="UTF-8"?>
                <process id="test-process">
                  <startEvent id="start"/>
                </process>
                """;

        // When/Then
        assertThatThrownBy(() -> bpmnValidationService.validateBpmnXml(xmlWithoutDefinitions))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("BPMN XML must have a <definitions> root element");
    }

    @Test
    @DisplayName("Should throw exception when missing process element")
    void validateBpmnXml_ShouldThrowException_WhenMissingProcess() {
        // Given
        String xmlWithoutProcess = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <!-- no process element -->
                </definitions>
                """;

        // When/Then
        assertThatThrownBy(() -> bpmnValidationService.validateBpmnXml(xmlWithoutProcess))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("BPMN XML must contain at least one <process> element");
    }

    @Test
    @DisplayName("Should throw exception when process has no ID")
    void validateBpmnXml_ShouldThrowException_WhenProcessHasNoId() {
        // Given
        String xmlWithoutProcessId = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process name="Test Process">
                    <startEvent id="start"/>
                  </process>
                </definitions>
                """;

        // When/Then
        assertThatThrownBy(() -> bpmnValidationService.validateBpmnXml(xmlWithoutProcessId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Process element must have an 'id' attribute");
    }

    @Test
    @DisplayName("Should extract process key from valid BPMN XML")
    void extractProcessKey_ShouldReturnKey_WhenXmlIsValid() {
        // When
        String processKey = bpmnValidationService.extractProcessKey(VALID_BPMN_XML);

        // Then
        assertThat(processKey).isEqualTo("test-process");
    }

    @Test
    @DisplayName("Should extract process name from valid BPMN XML")
    void extractProcessName_ShouldReturnName_WhenXmlIsValid() {
        // When
        String processName = bpmnValidationService.extractProcessName(VALID_BPMN_XML);

        // Then
        assertThat(processName).isEqualTo("Test Process");
    }

    @Test
    @DisplayName("Should return empty string when process has no name")
    void extractProcessName_ShouldReturnEmpty_WhenNoName() {
        // Given
        String xmlWithoutName = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process id="test-process">
                    <startEvent id="start"/>
                  </process>
                </definitions>
                """;

        // When
        String processName = bpmnValidationService.extractProcessName(xmlWithoutName);

        // Then
        assertThat(processName).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when BPMN XML is valid")
    void getValidationErrors_ShouldReturnEmptyList_WhenXmlIsValid() {
        // When
        var errors = bpmnValidationService.getValidationErrors(VALID_BPMN_XML);

        // Then
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should return error list when BPMN XML is invalid")
    void getValidationErrors_ShouldReturnErrors_WhenXmlIsInvalid() {
        // Given
        String invalidXml = "<invalid xml";

        // When
        var errors = bpmnValidationService.getValidationErrors(invalidXml);

        // Then
        assertThat(errors).isNotEmpty();
    }
}
