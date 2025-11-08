package com.processmonster.bpm.config;

import com.processmonster.bpm.camunda.listener.ProcessInstanceListener;
import com.processmonster.bpm.camunda.listener.TaskAssignmentListener;
import com.processmonster.bpm.camunda.listener.TaskCompleteListener;
import com.processmonster.bpm.camunda.listener.TaskCreateListener;
import com.processmonster.bpm.camunda.plugin.ProcessMonsterProcessEnginePlugin;
import com.processmonster.bpm.service.NotificationService;
import com.processmonster.bpm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService as CamundaTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

/**
 * Camunda BPM Engine Configuration
 * Configures the Camunda process engine with custom plugins and listeners
 */
@Slf4j
@Configuration
@EnableProcessApplication
@RequiredArgsConstructor
public class CamundaConfiguration {

    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final TaskService taskService;
    private final NotificationService notificationService;

    /**
     * Configure custom process engine plugin
     */
    @Bean
    public ProcessMonsterProcessEnginePlugin processMonsterPlugin() {
        return new ProcessMonsterProcessEnginePlugin(taskService, notificationService);
    }

    /**
     * Task creation listener - creates tasks in our system when Camunda creates them
     */
    @Bean
    public TaskCreateListener taskCreateListener() {
        return new TaskCreateListener(taskService);
    }

    /**
     * Task completion listener - updates tasks in our system when completed
     */
    @Bean
    public TaskCompleteListener taskCompleteListener() {
        return new TaskCompleteListener(taskService);
    }

    /**
     * Task assignment listener - handles task assignments
     */
    @Bean
    public TaskAssignmentListener taskAssignmentListener() {
        return new TaskAssignmentListener(taskService, notificationService);
    }

    /**
     * Process instance listener - tracks process lifecycle
     */
    @Bean
    public ProcessInstanceListener processInstanceListener() {
        return new ProcessInstanceListener(notificationService);
    }

    /**
     * Event listener for post-deployment
     */
    @EventListener
    public void onPostDeploy(PostDeployEvent event) {
        ProcessEngine processEngine = event.getProcessEngine();
        log.info("Camunda BPM Process Engine deployed successfully");
        log.info("Process Engine Name: {}", processEngine.getName());

        // Log deployed process definitions
        RepositoryService repositoryService = processEngine.getRepositoryService();
        long processCount = repositoryService.createProcessDefinitionQuery().count();
        log.info("Total Process Definitions Deployed: {}", processCount);

        // Log available services
        log.info("RuntimeService available: {}", processEngine.getRuntimeService() != null);
        log.info("TaskService available: {}", processEngine.getTaskService() != null);
        log.info("HistoryService available: {}", processEngine.getHistoryService() != null);
    }
}
