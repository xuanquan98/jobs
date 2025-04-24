package com.example.jobs.repository;



import com.example.jobs.entity.JobConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobConfigurationRepository extends JpaRepository<JobConfiguration, Long> {
    Optional<JobConfiguration> findByJobName(String jobName);
    List<JobConfiguration> findByIsActive(boolean isActive);
}
