package com.example.jobs.entity;



import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution_logs")
@Data
public class JobExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobConfiguration job;

    @Column(nullable = false)
    private String status; // RUNNING, SUCCESS, FAILED

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime startTime;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
