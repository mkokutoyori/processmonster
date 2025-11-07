package com.processmonster.bpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ProcessMonster Banking BPM Application
 *
 * Main Spring Boot application class for the Banking Business Process Management system.
 *
 * Features:
 * - Full i18n support (FR/EN)
 * - JWT authentication with access and refresh tokens
 * - RBAC with roles and permissions
 * - BPMN 2.0 process modeling and execution with Camunda
 * - Dynamic forms with validation
 * - Audit logging for banking compliance
 * - Real-time task management
 * - Reporting and analytics
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class BankingBpmApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingBpmApplication.class, args);
    }
}
