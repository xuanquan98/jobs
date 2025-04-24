package com.example.jobs.entity;

import com.example.jobs.config.JsonbConverter;
import com.example.jobs.enums.JobStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_configurations")
@Data
public class JobConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jobName;

    private String jobDescription;

    @Column(nullable = false)
    private String jobClass;

    @Column(nullable = false)
    private String cronExpression;

    private boolean isActive = true;

    @Column(columnDefinition = "text")
    private String parameters;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime updatedAt;

    @Transient
    private JobStatus status;
}