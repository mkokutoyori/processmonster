package com.processmonster.bpm.service;

import com.processmonster.bpm.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for validating BPMN 2.0 XML documents
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BpmnValidationService {

    private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String BPMNDI_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/DI";

    private final MessageSource messageSource;

    /**
     * Validate BPMN XML content
     * @param bpmnXml BPMN XML content
     * @throws BusinessException if validation fails
     */
    public void validateBpmnXml(String bpmnXml) {
        log.debug("Validating BPMN XML, length: {}", bpmnXml != null ? bpmnXml.length() : 0);

        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            throw new BusinessException(getMessage("process.validation.empty"));
        }

        try {
            Document document = parseXml(bpmnXml);
            validateBpmnStructure(document);
            log.debug("BPMN XML validation successful");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("BPMN XML validation failed", e);
            throw new BusinessException(getMessage("process.validation.invalid") + ": " + e.getMessage());
        }
    }

    /**
     * Validate BPMN XML and return validation errors (if any)
     * @param bpmnXml BPMN XML content
     * @return list of validation error messages (empty if valid)
     */
    public List<String> getValidationErrors(String bpmnXml) {
        List<String> errors = new ArrayList<>();

        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            errors.add(getMessage("process.validation.empty"));
            return errors;
        }

        try {
            Document document = parseXml(bpmnXml);
            validateBpmnStructure(document);
        } catch (Exception e) {
            errors.add(e.getMessage());
        }

        return errors;
    }

    /**
     * Extract process key from BPMN XML
     * @param bpmnXml BPMN XML content
     * @return process key (id attribute of the first process element)
     * @throws BusinessException if extraction fails
     */
    public String extractProcessKey(String bpmnXml) {
        try {
            Document document = parseXml(bpmnXml);
            NodeList processList = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");

            if (processList.getLength() == 0) {
                throw new BusinessException(getMessage("process.validation.no-process"));
            }

            Element processElement = (Element) processList.item(0);
            String processId = processElement.getAttribute("id");

            if (processId == null || processId.trim().isEmpty()) {
                throw new BusinessException(getMessage("process.validation.no-id"));
            }

            return processId;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract process key from BPMN XML", e);
            throw new BusinessException(getMessage("process.validation.invalid"));
        }
    }

    /**
     * Extract process name from BPMN XML
     * @param bpmnXml BPMN XML content
     * @return process name (name attribute of the first process element, or empty string)
     */
    public String extractProcessName(String bpmnXml) {
        try {
            Document document = parseXml(bpmnXml);
            NodeList processList = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");

            if (processList.getLength() == 0) {
                return "";
            }

            Element processElement = (Element) processList.item(0);
            String name = processElement.getAttribute("name");

            return name != null ? name : "";
        } catch (Exception e) {
            log.warn("Failed to extract process name from BPMN XML", e);
            return "";
        }
    }

    /**
     * Parse XML string into DOM Document
     */
    private Document parseXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Security settings to prevent XXE attacks
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xml));

        return builder.parse(inputSource);
    }

    /**
     * Validate BPMN document structure
     */
    private void validateBpmnStructure(Document document) {
        Element root = document.getDocumentElement();

        // Check if root element is definitions
        if (!"definitions".equals(root.getLocalName())) {
            throw new BusinessException(getMessage("process.validation.no-definitions"));
        }

        // Check BPMN namespace
        String namespace = root.getNamespaceURI();
        if (!BPMN_NAMESPACE.equals(namespace)) {
            log.warn("BPMN namespace mismatch. Expected: {}, Found: {}", BPMN_NAMESPACE, namespace);
        }

        // Check for at least one process element
        NodeList processList = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");
        if (processList.getLength() == 0) {
            throw new BusinessException(getMessage("process.validation.no-process"));
        }

        // Validate process has an ID
        Element processElement = (Element) processList.item(0);
        String processId = processElement.getAttribute("id");
        if (processId == null || processId.trim().isEmpty()) {
            throw new BusinessException(getMessage("process.validation.no-id"));
        }

        log.debug("BPMN structure validation passed. Process ID: {}, Process count: {}",
                  processId, processList.getLength());
    }

    /**
     * Get localized message
     */
    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
