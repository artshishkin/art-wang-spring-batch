package net.shyshkin.study.batch.resilience;

import net.shyshkin.study.batch.resilience.config.AppConfiguration;
import net.shyshkin.study.batch.resilience.config.RestartBatchConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.AssertFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {AppConfiguration.class, RestartBatchConfiguration.class})
@ActiveProfiles("restart-batch")
class RestartResilienceJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/resilience/product-restart-test-output.csv";
    public static final String ORIGINAL_TEST_INPUT = "../input/resilience/product-restart.csv";
    public static final String TEST_INPUT = "../output/resilience/product-restart-test-input.csv";

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Path.of(TEST_OUTPUT));
        Files.copy(Path.of(ORIGINAL_TEST_INPUT), Path.of(TEST_INPUT), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void cleanFiles() throws IOException {
//        Files.deleteIfExists(Path.of(TEST_INPUT));
//        Files.deleteIfExists(Path.of(TEST_OUTPUT));
    }

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("restartCsvInputFile", TEST_INPUT);
        paramsBuilder.addString("restartCsvOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    void restartJobTest() throws Exception {

        //First Launch
        //given
        System.out.println("--------------FIRST LAUNCH---------------");

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();
        Map<String, StepExecution> stepsMap = jobExecution.getStepExecutions()
                .stream()
                .collect(Collectors.toMap(StepExecution::getStepName, Function.identity()));

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("restartJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("FAILED");
        assertThat(stepsMap.get("step0"))
                .isNotNull()
                .satisfies(stepExecution -> assertThat(stepExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED));
        assertThat(stepsMap.get("step1"))
                .isNotNull()
                .satisfies(stepExecution -> assertAll(
                                () -> assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.FAILED.getExitCode()),
                                () -> assertThat(stepExecution.getReadCount()).isEqualTo(3),
                                () -> assertThat(stepExecution.getWriteCount()).isEqualTo(3)
                        )
                );
        AssertFile.assertLineCount(4, new FileSystemResource(TEST_OUTPUT));

        //Fixing Input File
        String content = Files.readString(Path.of(TEST_INPUT));
        content = content.replace(",error", "");
        Files.writeString(Path.of(TEST_INPUT), content, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);


        //Second Launch
        //given
        System.out.println("--------------SECOND LAUNCH---------------");
        //when
        jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        actualJobInstance = jobExecution.getJobInstance();
        actualJobExitStatus = jobExecution.getExitStatus();
        stepsMap = jobExecution.getStepExecutions()
                .stream()
                .collect(Collectors.toMap(StepExecution::getStepName, Function.identity()));

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("restartJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        assertThat(stepsMap.get("step0"))
                .isNull();
        assertThat(stepsMap.get("step1"))
                .isNotNull()
                .satisfies(stepExecution -> assertAll(
                                () -> assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode()),
                                () -> assertThat(stepExecution.getReadCount()).isEqualTo(9),
                                () -> assertThat(stepExecution.getWriteCount()).isEqualTo(9)
                        )
                );
        AssertFile.assertLineCount(13, new FileSystemResource(TEST_OUTPUT));

    }
}