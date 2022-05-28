package net.shyshkin.study.batch.performance.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CleanUpTasklet implements Tasklet {

    @Value("${app.tasklet.pause.clean-up}")
    private Long pause;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("Clean Up started...");
        Thread.sleep(pause);
        log.debug("Clean Up completed");
        return RepeatStatus.FINISHED;
    }
}
