package com.example.jobs.services;


import com.example.jobs.entity.JobConfiguration;
import com.example.jobs.enums.JobStatus;
import com.example.jobs.repository.JobConfigurationRepository;
import com.example.jobs.repository.JobExecutionLogRepository;
import com.example.jobs.task.DynamicJob;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class JobSchedulerService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JobStatusService jobStatusService;

    @PostConstruct
    public void initialize() throws SchedulerException {
        scheduler.clear();

        // Load and schedule all active jobs from database
        List<JobConfiguration> activeJobs = jobConfigurationRepository.findByIsActive(true);
        for (JobConfiguration jobConfig : activeJobs) {
            try {
                scheduleNewJob(jobConfig);
                log.info("Scheduled job: {}", jobConfig.getJobName());
            } catch (SchedulerException e) {
                log.error("Failed to schedule job: {}", jobConfig.getJobName(), e);
            }
        }
    }

    public void scheduleNewJob(JobConfiguration jobConfig) throws SchedulerException {
        if (!jobConfig.isActive()) {
            return;
        }

        JobDetail jobDetail = createJobDetail(jobConfig);
        Trigger trigger = createTrigger(jobConfig, jobDetail);
        scheduler.scheduleJob(jobDetail, trigger);
    }

    private JobDetail createJobDetail(JobConfiguration jobConfig) {
        JobDataMap jobDataMap = new JobDataMap();
        // Only store serializable data
        jobDataMap.put("jobId", jobConfig.getId());
        jobDataMap.put("jobClassName", jobConfig.getJobClass());
        jobDataMap.put("jobName", jobConfig.getJobName());

        return JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobConfig.getJobName())
                .withDescription(jobConfig.getJobDescription())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger createTrigger(JobConfiguration jobConfig, JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobConfig.getJobName() + "-trigger")
                .withDescription(jobConfig.getJobDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobConfig.getCronExpression()))
                .build();
    }


    public void updateScheduledJob(JobConfiguration jobConfig) throws SchedulerException {
        unscheduleJob(jobConfig.getJobName());
        if (jobConfig.isActive()) {
            scheduleNewJob(jobConfig);
        }
    }

    public void unscheduleJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            jobStatusService.updateStatus(jobName, JobStatus.STOPPED);
        }
    }

    public void rescheduleJob(JobConfiguration jobConfig) throws SchedulerException {
        JobKey jobKey = new JobKey(jobConfig.getJobName());
        if (scheduler.checkExists(jobKey)) {
            Trigger newTrigger = createTrigger(jobConfig, scheduler.getJobDetail(jobKey));
            scheduler.rescheduleJob(newTrigger.getKey(), newTrigger);
        } else {
            scheduleNewJob(jobConfig);
        }
    }

    public void triggerJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.triggerJob(jobKey);
        }
    }

    public void pauseJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            // Kiểm tra nếu job đang chạy và yêu cầu dừng
            if (isJobRunning(jobName)) {
                scheduler.interrupt(jobKey);
            }
            jobStatusService.updateStatus(jobName, JobStatus.PAUSED);
        }
    }

    private boolean isJobRunning(String jobName) throws SchedulerException {
        for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
            if (context.getJobDetail().getKey().getName().equals(jobName)) {
                return true;
            }
        }
        return false;
    }

    public void resumeJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
            jobStatusService.updateStatus(jobName, JobStatus.RUNNING);
        }
    }

    public boolean stopJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        boolean stopped = false;

        // Kiểm tra job đang chạy
        for (JobExecutionContext jobContext : scheduler.getCurrentlyExecutingJobs()) {
            if (jobContext.getJobDetail().getKey().equals(jobKey)) {
                scheduler.interrupt(jobKey);
                stopped = true;
                break;
            }
        }

        // Cập nhật trạng thái
        if (stopped) {
            jobStatusService.updateStatus(jobName, JobStatus.STOPPED);
        }

        return stopped;
    }


}
