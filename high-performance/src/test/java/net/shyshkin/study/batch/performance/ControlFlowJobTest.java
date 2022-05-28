package net.shyshkin.study.batch.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource(properties = {
        "app.tasklet.pause.download=10",
        "app.tasklet.pause.file-process=10",
        "app.tasklet.pause.business3=10",
        "app.tasklet.pause.business4=10",
        "app.tasklet.pause.clean-up=10",
})
@ActiveProfiles("control-flow")
class ControlFlowJobTest extends AbstractJobTest {

    @Value("${app.tasklet.pause.download}")
    private Long pause;

    @Test
    void controlFlowJobTest_failed() throws Exception {

        //given
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("willComplete", "false");
        var params = paramsBuilder.toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("control-flow-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        assertStepPresent(jobExecution, "pager-duty");
        assertStepExecutionStatus(jobExecution, "biz4Step", "FAILED");
    }

    @Test
    void controlFlowJobTest_completed() throws Exception {

        //given
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("willComplete", "true");
        var params = paramsBuilder.toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("control-flow-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        assertStepAbsent(jobExecution, "pager-duty");
        assertStepExecutionStatus(jobExecution, "biz4Step", "COMPLETED");
    }

    private void assertStepAbsent(JobExecution jobExecution, String step) {
        assertStep(jobExecution, step, false);
    }

    private void assertStepPresent(JobExecution jobExecution, String step) {
        assertStep(jobExecution, step, true);
    }

    private void assertStep(JobExecution jobExecution, String step, boolean present) {
        List<String> stepsExecutionNames = jobExecution.getStepExecutions()
                .stream()
                .map(StepExecution::getStepName)
                .collect(Collectors.toList());
        if (present)
            assertThat(stepsExecutionNames).contains(step);
        else
            assertThat(stepsExecutionNames).doesNotContain(step);
    }


    private void assertStepExecutionStatus(JobExecution jobExecution, String stepName, String exitCode) {
        var biz4StepOptional = jobExecution.getStepExecutions()
                .stream()
                .filter(stepExecution -> stepName.equals(stepExecution.getStepName()))
                .findAny();
        assertThat(biz4StepOptional)
                .hasValueSatisfying(stepExecution -> assertThat(stepExecution.getExitStatus().getExitCode())
                        .isEqualTo(exitCode)
                );
    }

}