package com.school.student.service;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
public class CsvUploadService {
    private final JobLauncher jobLauncher;
    private final Job studentJob;

    public CsvUploadService(JobLauncher jobLauncher, Job studentJob) {
        this.jobLauncher = jobLauncher;
        this.studentJob = studentJob;
    }

    public void uploadCsvToDb(String csvName) throws Exception {
        String dir = System.getProperty("os.name").toLowerCase().contains("win") ?
                "C:\\var\\log\\applications\\API\\dataprocessing\\" :
                "/var/log/applications/API/dataprocessing/";

        JobParameters params = new JobParametersBuilder()
                .addString("input.file", dir + csvName)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(studentJob, params);
        if (execution.getStatus() != BatchStatus.COMPLETED) {
            throw new IllegalStateException("Batch job failed with status: " + execution.getStatus());
        }
    }
}
