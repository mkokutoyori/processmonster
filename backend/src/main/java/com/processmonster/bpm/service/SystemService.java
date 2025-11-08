package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.SystemParameter;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.repository.SystemParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for system parameter management.
 * Handles runtime configuration with encryption support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemService {

    private final SystemParameterRepository systemParameterRepository;
    private final AuditService auditService;

    // Encryption settings (AES-256-GCM)
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    // In production, this should come from a secure key management system (e.g., AWS KMS, HashiCorp Vault)
    // For now, this is a placeholder - MUST be replaced with proper key management
    private static final String ENCRYPTION_KEY_BASE64 = "CHANGE_ME_IN_PRODUCTION_32BYTES_KEY_HERE_BASE64_ENCODED==";

    /**
     * Get parameter value by key
     */
    @Transactional(readOnly = true)
    public String getParameterValue(String key) {
        SystemParameter param = systemParameterRepository.findByKeyAndDeletedFalse(key)
                .orElseThrow(() -> new BusinessException("System parameter not found: " + key));

        String value = param.getValueOrDefault();

        // Decrypt if encrypted
        if (param.getEncrypted() && value != null) {
            try {
                value = decrypt(value);
            } catch (Exception e) {
                log.error("Failed to decrypt parameter: {}", key, e);
                throw new BusinessException("Failed to decrypt parameter value");
            }
        }

        return value;
    }

    /**
     * Get parameter value with default fallback
     */
    @Transactional(readOnly = true)
    public String getParameterValue(String key, String defaultValue) {
        try {
            return getParameterValue(key);
        } catch (BusinessException e) {
            return defaultValue;
        }
    }

    /**
     * Get boolean parameter
     */
    @Transactional(readOnly = true)
    public Boolean getBooleanParameter(String key) {
        SystemParameter param = systemParameterRepository.findByKeyAndDeletedFalse(key)
                .orElseThrow(() -> new BusinessException("System parameter not found: " + key));
        return param.getBooleanValue();
    }

    /**
     * Get integer parameter
     */
    @Transactional(readOnly = true)
    public Integer getIntegerParameter(String key) {
        SystemParameter param = systemParameterRepository.findByKeyAndDeletedFalse(key)
                .orElseThrow(() -> new BusinessException("System parameter not found: " + key));
        return param.getIntegerValue();
    }

    /**
     * Get long parameter
     */
    @Transactional(readOnly = true)
    public Long getLongParameter(String key) {
        SystemParameter param = systemParameterRepository.findByKeyAndDeletedFalse(key)
                .orElseThrow(() -> new BusinessException("System parameter not found: " + key));
        return param.getLongValue();
    }

    /**
     * Get parameter by ID
     */
    @Transactional(readOnly = true)
    public SystemParameter getParameterById(Long id) {
        return systemParameterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("System parameter not found with ID: " + id));
    }

    /**
     * Get all parameters (paginated)
     */
    @Transactional(readOnly = true)
    public Page<SystemParameter> getAllParameters(Pageable pageable) {
        return systemParameterRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get all parameters (list)
     */
    @Transactional(readOnly = true)
    public List<SystemParameter> getAllParameters() {
        return systemParameterRepository.findByDeletedFalse();
    }

    /**
     * Get parameters by category
     */
    @Transactional(readOnly = true)
    public List<SystemParameter> getParametersByCategory(String category) {
        return systemParameterRepository.findByCategory(category);
    }

    /**
     * Get editable parameters
     */
    @Transactional(readOnly = true)
    public List<SystemParameter> getEditableParameters() {
        return systemParameterRepository.findEditableParameters();
    }

    /**
     * Get all categories
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return systemParameterRepository.findAllCategories();
    }

    /**
     * Search parameters
     */
    @Transactional(readOnly = true)
    public Page<SystemParameter> searchParameters(String keyword, Pageable pageable) {
        return systemParameterRepository.searchParameters(keyword, pageable);
    }

    /**
     * Create a new parameter
     */
    public SystemParameter createParameter(String key, String value, String description,
                                           String category, String dataType, String defaultValue,
                                           Boolean encrypted, Boolean editable,
                                           String validationPattern, String allowedValues,
                                           Integer displayOrder) {
        // Check if key already exists
        if (systemParameterRepository.existsByKeyAndDeletedFalse(key)) {
            throw new BusinessException("System parameter with key '" + key + "' already exists");
        }

        // Encrypt value if needed
        String finalValue = value;
        if (encrypted != null && encrypted && value != null) {
            try {
                finalValue = encrypt(value);
            } catch (Exception e) {
                log.error("Failed to encrypt parameter value", e);
                throw new BusinessException("Failed to encrypt parameter value");
            }
        }

        // Validate value
        validateParameterValue(finalValue, dataType, validationPattern, allowedValues, encrypted);

        SystemParameter param = SystemParameter.builder()
                .key(key)
                .value(finalValue)
                .description(description)
                .category(category != null ? category : "General")
                .dataType(dataType != null ? dataType : "STRING")
                .defaultValue(defaultValue)
                .encrypted(encrypted != null ? encrypted : false)
                .editable(editable != null ? editable : true)
                .validationPattern(validationPattern)
                .allowedValues(allowedValues)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        SystemParameter saved = systemParameterRepository.save(param);

        auditService.logAction(
                "CREATE_SYSTEM_PARAMETER",
                "SystemParameter",
                saved.getId(),
                saved.getKey(),
                null,
                buildParameterAuditData(saved),
                "SUCCESS"
        );

        log.info("System parameter created: {} (ID: {})", key, saved.getId());
        return saved;
    }

    /**
     * Update parameter value
     */
    public SystemParameter updateParameterValue(Long id, String newValue) {
        SystemParameter param = getParameterById(id);

        if (!param.getEditable()) {
            throw new BusinessException("System parameter '" + param.getKey() + "' is not editable");
        }

        String oldValue = param.getValue();
        String finalValue = newValue;

        // Encrypt if needed
        if (param.getEncrypted() && newValue != null) {
            try {
                finalValue = encrypt(newValue);
            } catch (Exception e) {
                log.error("Failed to encrypt parameter value", e);
                throw new BusinessException("Failed to encrypt parameter value");
            }
        }

        // Validate value
        validateParameterValue(finalValue, param.getDataType(), param.getValidationPattern(),
                param.getAllowedValues(), param.getEncrypted());

        param.setValue(finalValue);
        param.setUpdatedAt(LocalDateTime.now());

        SystemParameter updated = systemParameterRepository.save(param);

        auditService.logAction(
                "UPDATE_SYSTEM_PARAMETER_VALUE",
                "SystemParameter",
                updated.getId(),
                updated.getKey(),
                param.getEncrypted() ? "***ENCRYPTED***" : oldValue,
                param.getEncrypted() ? "***ENCRYPTED***" : finalValue,
                "SUCCESS"
        );

        log.info("System parameter value updated: {} (ID: {})", param.getKey(), id);
        return updated;
    }

    /**
     * Update parameter configuration
     */
    public SystemParameter updateParameter(Long id, String description, String category,
                                           String validationPattern, String allowedValues,
                                           Integer displayOrder, Boolean editable) {
        SystemParameter param = getParameterById(id);
        Map<String, Object> oldData = buildParameterAuditData(param);

        if (description != null) {
            param.setDescription(description);
        }
        if (category != null) {
            param.setCategory(category);
        }
        if (validationPattern != null) {
            param.setValidationPattern(validationPattern);
            // Re-validate current value against new pattern
            validateParameterValue(param.getValue(), param.getDataType(), validationPattern,
                    param.getAllowedValues(), param.getEncrypted());
        }
        if (allowedValues != null) {
            param.setAllowedValues(allowedValues);
            // Re-validate current value against new allowed values
            validateParameterValue(param.getValue(), param.getDataType(), param.getValidationPattern(),
                    allowedValues, param.getEncrypted());
        }
        if (displayOrder != null) {
            param.setDisplayOrder(displayOrder);
        }
        if (editable != null) {
            param.setEditable(editable);
        }

        param.setUpdatedAt(LocalDateTime.now());
        SystemParameter updated = systemParameterRepository.save(param);

        auditService.logAction(
                "UPDATE_SYSTEM_PARAMETER",
                "SystemParameter",
                updated.getId(),
                updated.getKey(),
                oldData,
                buildParameterAuditData(updated),
                "SUCCESS"
        );

        log.info("System parameter updated: {} (ID: {})", param.getKey(), id);
        return updated;
    }

    /**
     * Delete parameter (soft delete)
     */
    public void deleteParameter(Long id) {
        SystemParameter param = getParameterById(id);

        if (!param.getEditable()) {
            throw new BusinessException("System parameter '" + param.getKey() + "' cannot be deleted");
        }

        param.setDeleted(true);
        param.setUpdatedAt(LocalDateTime.now());
        systemParameterRepository.save(param);

        auditService.logAction(
                "DELETE_SYSTEM_PARAMETER",
                "SystemParameter",
                param.getId(),
                param.getKey(),
                buildParameterAuditData(param),
                null,
                "SUCCESS"
        );

        log.info("System parameter deleted: {} (ID: {})", param.getKey(), id);
    }

    /**
     * Reset parameter to default value
     */
    public SystemParameter resetToDefault(Long id) {
        SystemParameter param = getParameterById(id);

        if (!param.getEditable()) {
            throw new BusinessException("System parameter '" + param.getKey() + "' cannot be reset");
        }

        String oldValue = param.getValue();
        param.setValue(param.getDefaultValue());
        param.setUpdatedAt(LocalDateTime.now());

        SystemParameter updated = systemParameterRepository.save(param);

        auditService.logAction(
                "RESET_SYSTEM_PARAMETER",
                "SystemParameter",
                updated.getId(),
                updated.getKey(),
                oldValue,
                param.getDefaultValue(),
                "SUCCESS"
        );

        log.info("System parameter reset to default: {} (ID: {})", param.getKey(), id);
        return updated;
    }

    /**
     * Get system configuration as a map (for application use)
     */
    @Transactional(readOnly = true)
    public Map<String, String> getSystemConfiguration() {
        List<SystemParameter> params = systemParameterRepository.findByDeletedFalse();
        Map<String, String> config = new HashMap<>();

        for (SystemParameter param : params) {
            String value = param.getValueOrDefault();

            // Decrypt if encrypted
            if (param.getEncrypted() && value != null) {
                try {
                    value = decrypt(value);
                } catch (Exception e) {
                    log.error("Failed to decrypt parameter: {}", param.getKey(), e);
                    value = null;
                }
            }

            if (value != null) {
                config.put(param.getKey(), value);
            }
        }

        return config;
    }

    /**
     * Get system configuration by category
     */
    @Transactional(readOnly = true)
    public Map<String, String> getSystemConfigurationByCategory(String category) {
        List<SystemParameter> params = systemParameterRepository.findByCategory(category);
        Map<String, String> config = new HashMap<>();

        for (SystemParameter param : params) {
            String value = param.getValueOrDefault();

            // Decrypt if encrypted
            if (param.getEncrypted() && value != null) {
                try {
                    value = decrypt(value);
                } catch (Exception e) {
                    log.error("Failed to decrypt parameter: {}", param.getKey(), e);
                    value = null;
                }
            }

            if (value != null) {
                config.put(param.getKey(), value);
            }
        }

        return config;
    }

    // Helper methods

    private void validateParameterValue(String value, String dataType, String validationPattern,
                                       String allowedValues, Boolean encrypted) {
        // Skip validation for null values or encrypted values (already encrypted)
        if (value == null || (encrypted != null && encrypted)) {
            return;
        }

        // Validate data type
        try {
            switch (dataType.toUpperCase()) {
                case "INTEGER":
                    Integer.parseInt(value);
                    break;
                case "LONG":
                    Long.parseLong(value);
                    break;
                case "DOUBLE":
                    Double.parseDouble(value);
                    break;
                case "BOOLEAN":
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new BusinessException("Invalid boolean value: " + value);
                    }
                    break;
                case "STRING":
                default:
                    // No type validation needed for strings
                    break;
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid " + dataType + " value: " + value);
        }

        // Validate against pattern
        if (validationPattern != null && !validationPattern.isEmpty()) {
            if (!Pattern.matches(validationPattern, value)) {
                throw new BusinessException("Value does not match validation pattern");
            }
        }

        // Validate against allowed values
        if (allowedValues != null && !allowedValues.isEmpty()) {
            String[] allowed = allowedValues.split(",");
            boolean found = false;
            for (String allowedValue : allowed) {
                if (allowedValue.trim().equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new BusinessException("Value not in allowed values list");
            }
        }
    }

    private Map<String, Object> buildParameterAuditData(SystemParameter param) {
        Map<String, Object> data = new HashMap<>();
        data.put("key", param.getKey());
        data.put("description", param.getDescription());
        data.put("category", param.getCategory());
        data.put("dataType", param.getDataType());
        data.put("encrypted", param.getEncrypted());
        data.put("editable", param.getEditable());
        // Don't include actual value in audit (could be sensitive)
        return data;
    }

    /**
     * Encrypt a value using AES-256-GCM
     */
    private String encrypt(String plainText) throws Exception {
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Get encryption key
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(ENCRYPTION_KEY_BASE64), "AES");

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // Encrypt
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine IV and ciphertext
        byte[] encryptedData = new byte[GCM_IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
        System.arraycopy(cipherText, 0, encryptedData, GCM_IV_LENGTH, cipherText.length);

        // Return as Base64
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypt a value using AES-256-GCM
     */
    private String decrypt(String encryptedText) throws Exception {
        // Decode from Base64
        byte[] encryptedData = Base64.getDecoder().decode(encryptedText);

        // Extract IV and ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

        byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

        // Get encryption key
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(ENCRYPTION_KEY_BASE64), "AES");

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // Decrypt
        byte[] plainText = cipher.doFinal(cipherText);

        return new String(plainText, StandardCharsets.UTF_8);
    }
}
