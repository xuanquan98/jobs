package com.example.jobs.config;

import com.example.jobs.services.JobStatusService;
import org.quartz.SchedulerContext;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import com.example.jobs.repository.JobConfigurationRepository;
import com.example.jobs.repository.JobExecutionLogRepository;

@Configuration
public class QuartzConfig {

    @Autowired
    private JobStatusService jobStatusService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // Configure scheduler context
        SchedulerContext schedulerContext = new SchedulerContext();
        schedulerContext.put("applicationContext", applicationContext);
        schedulerContext.put("jobConfigurationRepository", jobConfigurationRepository);
        schedulerContext.put("jobExecutionLogRepository", jobExecutionLogRepository);
        schedulerContext.put("jobStatusService", jobStatusService);

        factory.setSchedulerContextAsMap(schedulerContext);
        return factory;
    }
}