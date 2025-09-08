package com.school.student.batch;

import com.school.student.model.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    @StepScope
    public FlatFileItemReader<Student> csvReader(@Value("#{jobParameters['input.file']}") String path) {
        FlatFileItemReader<Student> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(path));
        reader.setLinesToSkip(1);
        reader.setLineMapper((line, lineNumber) -> {
            String[] f = line.split(",");
            return Student.builder()
                    .studentId((long) Double.parseDouble(f[0]))
                    .firstName(f[1])
                    .lastName(f[2])
                    .dob(LocalDate.parse(f[3]))
                    .className(f[4])
                    .score(Integer.parseInt(f[5]) + 5) // add +5
                    .build();
        });
        return reader;
    }


    @Bean
    public ItemProcessor<Student, Student> processor() {
        return student -> student; // already processed (+5)
    }

    @Bean
    public JdbcBatchItemWriter<Student> writer() {
        return new JdbcBatchItemWriterBuilder<Student>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("""
                    INSERT INTO student (student_id, first_name, last_name, dob, class_name, score)
                    VALUES (:studentId, :firstName, :lastName, :dob, :className, :score)
                    ON CONFLICT (student_id) DO UPDATE 
                    SET first_name = EXCLUDED.first_name,
                        last_name  = EXCLUDED.last_name,
                        dob        = EXCLUDED.dob,
                        class_name = EXCLUDED.class_name,
                        score      = EXCLUDED.score
                    """)
                .build();
    }

    @Bean
    public Step studentStep(FlatFileItemReader<Student> reader,
                            ItemProcessor<Student, Student> processor,
                            ItemWriter<Student> writer) {
        return new StepBuilder("studentStep", jobRepository)
                .<Student, Student>chunk(5000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(NumberFormatException.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job studentJob(Step studentStep) {
        return new JobBuilder("studentJob", jobRepository)
                .start(studentStep)
                .build();
    }
}
