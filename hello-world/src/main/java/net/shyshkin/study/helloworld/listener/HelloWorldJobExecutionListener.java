package net.shyshkin.study.helloworld.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloWorldJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.debug("Before Job: {}, with job exec context: {}\nfull jobExecution: {}",
                jobExecution.getJobInstance().getJobName(), jobExecution.getExecutionContext(), jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.debug("After Job: {}, with job exec context: {}\nfull jobExecution: {}",
                jobExecution.getJobInstance().getJobName(), jobExecution.getExecutionContext(), jobExecution);
    }
}
