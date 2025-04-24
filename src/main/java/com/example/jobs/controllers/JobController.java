package com.example.jobs.controllers;

import com.example.jobs.dto.ApiResponse;
import com.example.jobs.entity.JobConfiguration;
import com.example.jobs.entity.JobExecutionLog;
import com.example.jobs.exception.NotFoundException;
import com.example.jobs.repository.JobConfigurationRepository;
import com.example.jobs.repository.JobExecutionLogRepository;
import com.example.jobs.services.JobSchedulerService;
import com.example.jobs.services.JobStatusService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;

    @Autowired
    private JobSchedulerService jobSchedulerService;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Autowired
    private JobStatusService jobStatusService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobConfiguration>>> getAllJobs() {
        List<JobConfiguration> jobs = jobConfigurationRepository.findAll();
        jobs.forEach(job -> job.setStatus(jobStatusService.getStatus(job.getJobName())));
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobConfiguration>> getJobById(@PathVariable Long id) {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));
        job.setStatus(jobStatusService.getStatus(job.getJobName()));
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobConfiguration>> createJob(@RequestBody JobConfiguration jobConfig) throws SchedulerException {
        JobConfiguration savedJob = jobConfigurationRepository.save(jobConfig);
        jobSchedulerService.scheduleNewJob(savedJob);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", savedJob));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobConfiguration>> updateJob(
            @PathVariable Long id,
            @RequestBody JobConfiguration jobConfig) throws SchedulerException {

        JobConfiguration existingJob = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        jobConfig.setId(id);
        JobConfiguration updatedJob = jobConfigurationRepository.save(jobConfig);
        jobSchedulerService.updateScheduledJob(updatedJob);

        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updatedJob));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        jobSchedulerService.unscheduleJob(job.getJobName());
        jobConfigurationRepository.delete(job);

        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Void>> startJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        job.setActive(true);
        jobConfigurationRepository.save(job);
        jobSchedulerService.rescheduleJob(job);

        return ResponseEntity.ok(ApiResponse.success("Job started successfully", null));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<Void>> stopJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        job.setActive(false);
        jobConfigurationRepository.save(job);
        jobSchedulerService.unscheduleJob(job.getJobName());

        return ResponseEntity.ok(ApiResponse.success("Job stopped successfully", null));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<JobExecutionLog>>> getJobLogs(@PathVariable Long id) {
        if (!jobConfigurationRepository.existsById(id)) {
            throw new NotFoundException("Job not found with id: " + id);
        }
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByJobIdOrderByStartTimeDesc(id);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<ApiResponse<Void>> triggerJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        jobSchedulerService.triggerJob(job.getJobName());
        return ResponseEntity.ok(ApiResponse.success("Job triggered successfully", null));
    }

    // New pause endpoint
    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<Void>> pauseJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        jobSchedulerService.pauseJob(job.getJobName());
        return ResponseEntity.ok(ApiResponse.success("Job paused successfully", null));
    }

    // New resume endpoint
    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        jobSchedulerService.resumeJob(job.getJobName());
        return ResponseEntity.ok(ApiResponse.success("Job resumed successfully", null));
    }

    // New pause endpoint
    @PostMapping("/{id}/interrupt")
    public ResponseEntity<ApiResponse<Void>> interruptJob(@PathVariable Long id) throws SchedulerException {
        JobConfiguration job = jobConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + id));

        boolean stopped = jobSchedulerService.stopJob(job.getJobName());

        if (stopped) {
            return ResponseEntity.ok(ApiResponse.success("Job stopped successfully", null));
        }
        return ResponseEntity.ok(
                (ApiResponse.error(HttpStatus.CONFLICT.value(), "FALSE", "Job is not currently running")));
    }

}