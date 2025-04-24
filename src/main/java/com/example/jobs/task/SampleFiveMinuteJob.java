package com.example.jobs.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleFiveMinuteJob implements JobTask {

    private static final Logger logger = LoggerFactory.getLogger(SampleFiveMinuteJob.class);

    @Override
    public void execute(String parameters) {
        logger.info("Sample 5-minute job executed with parameters: {}", parameters);
        // Add your actual job logic here
        // This will run every 5 minutes when scheduled
        try {
            logger.info("Starting 2-minute countdown...");
            for (int i = 1; i <= 120; i++) {
                // Kiểm tra trạng thái interrupted trước mỗi lần sleep
                if (Thread.currentThread().isInterrupted()) {
                    logger.warn("Job interrupted before sleep");
                    throw new InterruptedException();
                }

                // In số thứ tự và sleep 1 giây
                logger.info("Count: {}", i);
                Thread.sleep(1000); // 1000 milliseconds = 1 giây

                // Kiểm tra interrupted sau khi sleep
                if (Thread.currentThread().isInterrupted()) {
                    logger.warn("Job interrupted during sleep");
                    throw new InterruptedException();
                }
            }
            logger.info("Countdown completed successfully");
        } catch (InterruptedException e) {
            logger.warn("Job was interrupted", e);
            Thread.currentThread().interrupt(); // Khôi phục trạng thái interrupted
            throw new RuntimeException("Job interrupted during countdown", e);
        }

        logger.info("End Job Sample 5-minute job executed with parameters: {}", parameters);
    }
}
