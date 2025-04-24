package com.example.jobs.task;



import com.example.jobs.entity.JobConfiguration;
import com.example.jobs.entity.JobExecutionLog;
import com.example.jobs.enums.JobStatus;
import com.example.jobs.repository.JobExecutionLogRepository;
import com.example.jobs.services.JobStatusService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;


import com.example.jobs.repository.JobConfigurationRepository;

@Slf4j
public class DynamicJob implements Job, InterruptableJob {
    private JobStatusService jobStatusService;
    private volatile boolean interrupted = false;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Lấy các bean từ context

        try {
            while (!interrupted) {
                // Logic chính của job
                if (Thread.interrupted() || interrupted) {
                    throw new InterruptedException();
                }
                // Thực hiện công việc
                mainAction(context);
            }
        } catch (InterruptedException e) {
            log.info("Job was interrupted");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
    }

    private void mainAction(JobExecutionContext context) throws JobExecutionException {
        SchedulerContext schedulerContext = null;
        try {
            schedulerContext = context.getScheduler().getContext();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        JobStatusService jobStatusService = (JobStatusService)
                schedulerContext.get("jobStatusService");
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Long jobId = jobDataMap.getLong("jobId");
        String jobClassName = jobDataMap.getString("jobClassName");
        String jobName = jobDataMap.getString("jobName");
        jobStatusService.updateStatus(jobName, JobStatus.RUNNING);
        try {

            // Get required beans from scheduler context
            JobConfigurationRepository jobConfigRepo = (JobConfigurationRepository)
                    schedulerContext.get("jobConfigurationRepository");
            JobExecutionLogRepository logRepo = (JobExecutionLogRepository)
                    schedulerContext.get("jobExecutionLogRepository");
            ApplicationContext appContext = (ApplicationContext)
                    schedulerContext.get("applicationContext");

            JobConfiguration jobConfig = jobConfigRepo.findById(jobId)
                    .orElseThrow(() -> new JobExecutionException("Job configuration not found"));

            JobExecutionLog log = new JobExecutionLog();
            log.setJob(jobConfig);
            log.setStatus("RUNNING");
            log.setStartTime(LocalDateTime.now());
            logRepo.save(log);

            // Get and execute the job
            Class<?> jobClass = Class.forName(jobClassName);
            JobTask jobTask = (JobTask) appContext.getBean(jobClass);
            jobTask.execute(jobConfig.getParameters());

            log.setStatus("SUCCESS");
            log.setEndTime(LocalDateTime.now());
            logRepo.save(log);
            jobStatusService.updateStatus(jobClassName, JobStatus.COMPLETED);
        } catch (Exception e) {
            jobStatusService.updateStatus(jobClassName, JobStatus.FAILED);
            throw new JobExecutionException("Job execution failed", e);
        }
    }
}