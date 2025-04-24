package com.example.jobs.repository;


import com.example.jobs.entity.JobExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {
    List<JobExecutionLog> findByJobIdOrderByStartTimeDesc(Long jobId);
}
