package net.shyshkin.study.batch.performance;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.config.AppConfiguration;
import net.shyshkin.study.batch.performance.config.DatabaseConfig;
import net.shyshkin.study.batch.performance.config.MultiThreadBatchConfiguration;
import net.shyshkin.study.batch.performance.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource(properties = {"app.processor.pause=100"})
@ContextConfiguration(classes = {AppConfiguration.class, MultiThreadBatchConfiguration.class, DatabaseConfig.class})
@ActiveProfiles("multi-thread")
class MultiThreadJobTest extends AbstractJobTest {

    @Autowired
    JdbcBatchItemWriter<Product> itemWriter;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${app.processor.pause}")
    private Long processorPause;

    @Override
    @AfterEach
    void tearDown() {
        super.tearDown();
        jdbcTemplate.execute("delete from products where 1=1");
    }

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/high-performance/product-multi.csv");
        paramsBuilder.addString("csvOutputFile", "../output/high-performance/productOut-multi.csv");
        return paramsBuilder.toJobParameters();
    }

    @Test
    void multiThreadJobTest() throws Exception {

        //given
        int expectedCount = 39;
        long start = System.currentTimeMillis();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("multi-thread-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        long stop = System.currentTimeMillis();
        assertThat(stop - start).isLessThan(processorPause * expectedCount / 2);

        Long count = jdbcTemplate.queryForObject("select count(*) from products", Long.class);
        assertThat(count).isEqualTo(expectedCount);

    }

    @Test
    void multiThreadStepTest() throws Exception {

        //given
        int expectedCount = 39;
        long start = System.currentTimeMillis();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(expectedCount));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        long stop = System.currentTimeMillis();
        assertThat(stop - start).isLessThan(processorPause * expectedCount / 2);

        Long count = jdbcTemplate.queryForObject("select count(*) from products", Long.class);
        assertThat(count).isEqualTo(expectedCount);

    }

}