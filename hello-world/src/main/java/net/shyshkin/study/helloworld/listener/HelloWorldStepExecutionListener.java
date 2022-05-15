package net.shyshkin.study.helloworld.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloWorldStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.debug("Before Step: {}, with step exec context: {}\nfull stepExecution: {}",
                stepExecution.getStepName(), stepExecution.getExecutionContext(), stepExecution);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("After Step: {}, with step exec context: {}\nfull stepExecution: {}",
                stepExecution.getStepName(), stepExecution.getExecutionContext(), stepExecution);
        return stepExecution.getExitStatus();
    }
}
