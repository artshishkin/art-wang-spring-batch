package net.shyshkin.study.batch.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource(properties = {"app.processor.pause=10"})
@ActiveProfiles("range-partitioner")
@Sql(scripts = {"classpath:jdbc/products-data.sql"})
class RangePartitionerJobTest extends AbstractJobTest {


    @Value("${app.processor.pause}")
    private Long processorPause;

    @Test
    void rangePartitionerJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("range-partitioner-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        Map<String, StepExecution> stepExecutions = jobExecution.getStepExecutions()
                .stream()
                .collect(Collectors.toMap(StepExecution::getStepName, Function.identity()));

        assertThat(stepExecutions.get("partitionStep").getWriteCount()).isEqualTo(39);

        int totalWriteCount = jobExecution.getStepExecutions()
                .stream()
                .filter(stepExecution -> stepExecution.getStepName().startsWith("slaveStep"))
                .mapToInt(StepExecution::getWriteCount)
                .sum();
        jobExecution.getStepExecutions().forEach(se -> log.debug("{}", se));
        assertThat(totalWriteCount).isEqualTo(39);
    }

}