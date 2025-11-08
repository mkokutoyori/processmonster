package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service for sending notifications
 * Supports in-app notifications and email (future enhancement)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource messageSource;

    /**
     * Send task assigned notification
     */
    @Async
    public void sendTaskAssigned(Task task, String assignee) {
        log.info("Notification: Task '{}' assigned to '{}'", task.getName(), assignee);

        String message = getMessage("notification.task.assigned",
                task.getName(), assignee);

        // TODO: Send email notification
        // TODO: Send in-app notification via WebSocket
        // For now, just log

        publishEvent(new TaskNotificationEvent(
                task.getId(),
                assignee,
                "TASK_ASSIGNED",
                message
        ));
    }

    /**
     * Send task due soon notification
     */
    @Async
    public void sendTaskDueSoon(Task task) {
        log.info("Notification: Task '{}' is due soon ({})", task.getName(), task.getDueDate());

        String message = getMessage("notification.task.due-soon",
                task.getName(),
                formatDateTime(task.getDueDate()));

        // TODO: Send email notification
        publishEvent(new TaskNotificationEvent(
                task.getId(),
                task.getAssignee(),
                "TASK_DUE_SOON",
                message
        ));
    }

    /**
     * Send task overdue notification
     */
    @Async
    public void sendTaskOverdue(Task task) {
        log.warn("Notification: Task '{}' is overdue ({})", task.getName(), task.getDueDate());

        String message = getMessage("notification.task.overdue",
                task.getName(),
                formatDateTime(task.getDueDate()));

        // TODO: Send email notification with high priority
        publishEvent(new TaskNotificationEvent(
                task.getId(),
                task.getAssignee(),
                "TASK_OVERDUE",
                message
        ));
    }

    /**
     * Send task completed notification
     */
    @Async
    public void sendTaskCompleted(Task task) {
        log.info("Notification: Task '{}' completed by '{}'", task.getName(), task.getCompletedBy());

        String message = getMessage("notification.task.completed",
                task.getName(),
                task.getCompletedBy());

        // Notify process owner or interested parties
        publishEvent(new TaskNotificationEvent(
                task.getId(),
                task.getCreatedBy(), // Notify creator
                "TASK_COMPLETED",
                message
        ));
    }

    /**
     * Send task reassigned notification
     */
    @Async
    public void sendTaskReassigned(Task task, String previousAssignee, String newAssignee) {
        log.info("Notification: Task '{}' reassigned from '{}' to '{}'",
                task.getName(), previousAssignee, newAssignee);

        // Notify both old and new assignee
        String messageNew = getMessage("notification.task.assigned",
                task.getName(), newAssignee);

        String messageOld = getMessage("notification.task.reassigned",
                task.getName(), newAssignee);

        publishEvent(new TaskNotificationEvent(
                task.getId(),
                newAssignee,
                "TASK_ASSIGNED",
                messageNew
        ));

        publishEvent(new TaskNotificationEvent(
                task.getId(),
                previousAssignee,
                "TASK_REASSIGNED",
                messageOld
        ));
    }

    /**
     * Send comment added notification
     */
    @Async
    public void sendCommentAdded(Task task, String commenter, String commentText) {
        log.info("Notification: New comment on task '{}' by '{}'", task.getName(), commenter);

        String message = getMessage("notification.task.comment-added",
                commenter,
                task.getName(),
                truncate(commentText, 100));

        // Notify assignee if different from commenter
        if (task.getAssignee() != null && !task.getAssignee().equals(commenter)) {
            publishEvent(new TaskNotificationEvent(
                    task.getId(),
                    task.getAssignee(),
                    "TASK_COMMENT_ADDED",
                    message
            ));
        }
    }

    /**
     * Publish notification event
     */
    private void publishEvent(TaskNotificationEvent event) {
        eventPublisher.publishEvent(event);
        log.debug("Published notification event: {}", event);
    }

    /**
     * Format date time for display
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Truncate text to max length
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Get localized message
     */
    private String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }

    /**
     * Task notification event
     */
    public record TaskNotificationEvent(
            Long taskId,
            String recipient,
            String type,
            String message
    ) {
    }
}
