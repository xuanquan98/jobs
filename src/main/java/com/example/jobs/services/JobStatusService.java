package com.example.jobs.services;

import com.example.jobs.enums.JobStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobStatusService {
    private final Map<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();


    public void updateStatus(String jobName, JobStatus status) {
        jobStatusMap.put(jobName, status);
    }

    public JobStatus getStatus(String jobName) {
        return jobStatusMap.getOrDefault(jobName, JobStatus.STOPPED);
    }

    public boolean isRunning(String jobName) {
        return getStatus(jobName) == JobStatus.RUNNING;
    }

    public boolean isPaused(String jobName) {
        return getStatus(jobName) == JobStatus.PAUSED;
    }
}
