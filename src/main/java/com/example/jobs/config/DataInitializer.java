package com.example.jobs.config;



import com.example.jobs.entity.JobConfiguration;
import com.example.jobs.repository.JobConfigurationRepository;
import com.example.jobs.services.JobSchedulerService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final JobConfigurationRepository jobConfigurationRepository;
    private final JobSchedulerService jobSchedulerService;

    public DataInitializer(JobConfigurationRepository jobConfigurationRepository,
                           JobSchedulerService jobSchedulerService) {
        this.jobConfigurationRepository = jobConfigurationRepository;
        this.jobSchedulerService = jobSchedulerService;
    }

    @PostConstruct
    public void init() {
        // Create sample 5-minute job if it doesn't exist
        if (jobConfigurationRepository.findByJobName("sampleFiveMinuteJob").isEmpty()) {
            JobConfiguration job = new JobConfiguration();
            job.setJobName("sampleFiveMinuteJob");
            job.setJobDescription("Sample job that runs every 5 minutes");
            job.setJobClass("com.example.jobs.task.SampleFiveMinuteJob");
            job.setCronExpression("0 */1 * ? * *"); // Every 5 minutes
            job.setActive(true);
            job.setParameters("{\"key\":\"value\"}");

            try {
                JobConfiguration savedJob = jobConfigurationRepository.save(job);
                jobSchedulerService.scheduleNewJob(savedJob);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
