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
public class FileProcessTasklet implements Tasklet {

    @Value("${app.tasklet.pause.file-process}")
    private Long pause;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("File Process started...");
        Thread.sleep(pause);
        log.debug("File Process completed");
        return RepeatStatus.FINISHED;
    }
}
