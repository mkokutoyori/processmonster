package com.processmonster.bpm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic audit logging.
 * Methods annotated with @Audited will have their execution automatically logged.
 *
 * Example usage:
 * <pre>
 * {@code
 * @Audited(action = "CREATE_USER", entityType = "User")
 * public User createUser(CreateUserDTO dto) {
 *     // Implementation
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * The action being performed (e.g., "CREATE_USER", "UPDATE_PROCESS", "DELETE_TASK")
     */
    String action();

    /**
     * The type of entity being acted upon (e.g., "User", "Process", "Task")
     */
    String entityType();

    /**
     * Whether to log the full request payload as context
     * Default: false (for performance)
     */
    boolean logPayload() default false;

    /**
     * Whether this is a sensitive operation that should be marked as such
     * Default: false
     */
    boolean sensitive() default false;

    /**
     * Custom description of the operation (optional)
     */
    String description() default "";
}
