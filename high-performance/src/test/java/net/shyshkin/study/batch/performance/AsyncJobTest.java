package net.shyshkin.study.batch.performance;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.config.AppConfiguration;
import net.shyshkin.study.batch.performance.config.AsyncBatchConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource(properties = {"app.processor.pause=100"})
@ContextConfiguration(classes = {AppConfiguration.class, AsyncBatchConfiguration.class})
@ActiveProfiles("async")
class AsyncJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/high-performance/productOut-async.csv";

    @Value("${app.processor.pause}")
    private Long processorPause;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/high-performance/product-multi.csv");
        paramsBuilder.addString("csvOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    void asyncJobTest() throws Exception {

        //given
        int expectedCount = 39;
        long start = System.currentTimeMillis();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("async-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        long stop = System.currentTimeMillis();
        assertThat(stop - start).isLessThan(processorPause * expectedCount / 2);

        AssertFile.assertLineCount(41, new FileSystemResource(TEST_OUTPUT));
        //        checkWriteOrder(); //NO GUARANTY
    }

    private void checkWriteOrder() throws Exception {
        List<Integer> indexes = Files.readAllLines(Path.of(TEST_OUTPUT))
                .stream()
                .skip(1)
                .limit(39)
                .map(row -> row.split("|")[0])
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        Integer lastIndex = 0;
        boolean indexesIsInOrder = true;
        for (Integer index : indexes) {
            indexesIsInOrder = indexesIsInOrder && (index > lastIndex);
            lastIndex = index;
        }
        assertThat(indexesIsInOrder).isTrue();
    }

    @Test
    void asyncStepTest() throws Exception {

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

        AssertFile.assertLineCount(41, new FileSystemResource(TEST_OUTPUT));
//        checkWriteOrder(); //NO GUARANTY
    }

}