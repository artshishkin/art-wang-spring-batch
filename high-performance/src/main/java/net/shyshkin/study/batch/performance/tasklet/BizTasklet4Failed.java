package net.shyshkin.study.batch.performance.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class BizTasklet4Failed extends BizTasklet4 implements StepExecutionListener {

    private final Boolean willComplete;

    public BizTasklet4Failed(@Value("#{jobParameters['willComplete']}") Boolean willComplete) {
        this.willComplete = willComplete;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        if (willComplete) {
            return ExitStatus.COMPLETED;
        } else {
            log.debug("After Step BizTasklet4Failed");
            return ExitStatus.FAILED;
        }
    }
}
