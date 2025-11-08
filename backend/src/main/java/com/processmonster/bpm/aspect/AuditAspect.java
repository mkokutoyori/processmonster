package com.processmonster.bpm.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.annotation.Audited;
import com.processmonster.bpm.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic audit logging.
 * Intercepts methods annotated with @Audited and logs their execution.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    /**
     * Around advice for @Audited methods.
     * Logs the method execution before and after, capturing results and errors.
     */
    @Around("@annotation(com.processmonster.bpm.annotation.Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audited audited = method.getAnnotation(Audited.class);

        String action = audited.action();
        String entityType = audited.entityType();
        boolean logPayload = audited.logPayload();
        boolean sensitive = audited.sensitive();
        String description = audited.description();

        // Prepare context
        Map<String, Object> context = new HashMap<>();
        context.put("method", method.getName());
        context.put("class", joinPoint.getTarget().getClass().getSimpleName());

        if (!description.isEmpty()) {
            context.put("description", description);
        }

        if (sensitive) {
            context.put("sensitive", true);
        }

        // Log request payload if configured
        if (logPayload && joinPoint.getArgs().length > 0) {
            try {
                context.put("requestPayload", sanitizePayload(joinPoint.getArgs()));
            } catch (Exception e) {
                log.debug("Failed to serialize request payload for audit", e);
            }
        }

        Long entityId = null;
        String entityName = null;
        Object result = null;
        String resultStatus = "SUCCESS";
        String errorMessage = null;

        try {
            // Execute the method
            result = joinPoint.proceed();

            // Try to extract entity ID and name from result
            if (result != null) {
                entityId = extractEntityId(result);
                entityName = extractEntityName(result);
            }

            return result;

        } catch (Exception e) {
            resultStatus = "ERROR";
            errorMessage = e.getMessage();
            log.error("Error executing audited method: {}.{}",
                    joinPoint.getTarget().getClass().getSimpleName(), method.getName(), e);
            throw e;

        } finally {
            // Log the action
            try {
                if ("ERROR".equals(resultStatus)) {
                    auditService.logError(action, entityType, entityId, entityName, errorMessage);
                } else {
                    auditService.logAction(action, entityType, entityId, entityName,
                            null, result, resultStatus, context);
                }
            } catch (Exception e) {
                // Never fail the actual operation due to audit logging issues
                log.error("Failed to log audit event", e);
            }
        }
    }

    /**
     * Extract entity ID from result object.
     * Tries common patterns: getId(), id field
     */
    private Long extractEntityId(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // Try getId() method
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);

            if (id instanceof Long) {
                return (Long) id;
            } else if (id instanceof Integer) {
                return ((Integer) id).longValue();
            } else if (id instanceof String) {
                return Long.parseLong((String) id);
            }
        } catch (Exception e) {
            log.debug("Could not extract entity ID from result", e);
        }

        return null;
    }

    /**
     * Extract entity name from result object.
     * Tries common patterns: getName(), getTitle(), getKey(), toString()
     */
    private String extractEntityName(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // Try getName() method
            try {
                Method getNameMethod = result.getClass().getMethod("getName");
                Object name = getNameMethod.invoke(result);
                if (name != null) {
                    return name.toString();
                }
            } catch (NoSuchMethodException e) {
                // Try next method
            }

            // Try getTitle() method
            try {
                Method getTitleMethod = result.getClass().getMethod("getTitle");
                Object title = getTitleMethod.invoke(result);
                if (title != null) {
                    return title.toString();
                }
            } catch (NoSuchMethodException e) {
                // Try next method
            }

            // Try getKey() method
            try {
                Method getKeyMethod = result.getClass().getMethod("getKey");
                Object key = getKeyMethod.invoke(result);
                if (key != null) {
                    return key.toString();
                }
            } catch (NoSuchMethodException e) {
                // Use ID instead if available
            }

            // Fallback to ID if available
            Long id = extractEntityId(result);
            if (id != null) {
                return "ID: " + id;
            }

        } catch (Exception e) {
            log.debug("Could not extract entity name from result", e);
        }

        return null;
    }

    /**
     * Sanitize payload to remove sensitive data before logging.
     * Removes fields like password, token, secret, etc.
     */
    private Object sanitizePayload(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        try {
            // Convert to map/list structure
            Object payload = args.length == 1 ? args[0] : args;

            // Convert to JSON and back to sanitize
            String json = objectMapper.writeValueAsString(payload);

            // Replace sensitive fields with ***
            json = json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
            json = json.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
            json = json.replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"");
            json = json.replaceAll("\"apiKey\"\\s*:\\s*\"[^\"]*\"", "\"apiKey\":\"***\"");
            json = json.replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"***\"");
            json = json.replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"***\"");

            // Parse back to object
            return objectMapper.readValue(json, Object.class);

        } catch (Exception e) {
            log.debug("Could not sanitize payload", e);
            return "[Could not serialize payload]";
        }
    }
}
