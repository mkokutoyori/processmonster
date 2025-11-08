package com.processmonster.bpm.camunda.plugin;

import com.processmonster.bpm.service.NotificationService;
import com.processmonster.bpm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.stereotype.Component;

/**
 * Custom Camunda Process Engine Plugin
 * Integrates Camunda with ProcessMonster's task management system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessMonsterProcessEnginePlugin extends AbstractProcessEnginePlugin {

    private final TaskService taskService;
    private final NotificationService notificationService;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        log.info("ProcessMonster Plugin - Pre-initialization");

        // Set custom configurations
        processEngineConfiguration.setJobExecutorActivate(true);
        processEngineConfiguration.setAuthorizationEnabled(true);
        processEngineConfiguration.setTenantCheckEnabled(false);

        // Enable async operations
        processEngineConfiguration.setJobExecutorDeploymentAware(true);

        log.info("ProcessMonster Plugin - Pre-initialization completed");
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        log.info("ProcessMonster Plugin - Post-initialization");

        // Add custom interceptors or listeners here if needed

        log.info("ProcessMonster Plugin - Post-initialization completed");
    }

    @Override
    public void postProcessEngineBuild(org.camunda.bpm.engine.ProcessEngine processEngine) {
        log.info("ProcessMonster Plugin - Process Engine built successfully");
        log.info("Process Engine Name: {}", processEngine.getName());

        // Perform any post-build initialization
        // For example, deploy default processes, set up default users, etc.
    }
}
