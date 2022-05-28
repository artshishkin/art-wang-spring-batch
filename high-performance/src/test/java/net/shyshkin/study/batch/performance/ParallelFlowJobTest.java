package net.shyshkin.study.batch.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource(properties = {
        "app.tasklet.pause.download=100",
        "app.tasklet.pause.file-process=100",
        "app.tasklet.pause.business3=100",
        "app.tasklet.pause.business4=100",
        "app.tasklet.pause.clean-up=100",
})
@ActiveProfiles("parallel-flow")
class ParallelFlowJobTest extends AbstractJobTest {


    @Value("${app.tasklet.pause.download}")
    private Long pause;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        return paramsBuilder.toJobParameters();
    }

    @Test
    void parallelFlowJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("parallel-flow-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        Date startTime = jobExecution.getStartTime();
        Date endTime = jobExecution.getEndTime();
        log.debug("Start: {}, complete: {}, delta in ms: {}", startTime, endTime, endTime.getTime() - startTime.getTime());
        assertThat(endTime.getTime() - startTime.getTime()).isLessThan(pause * 5);

    }

}