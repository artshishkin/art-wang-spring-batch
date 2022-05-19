package net.shyshkin.study.itemwriters;

import net.shyshkin.study.itemwriters.config.AppConfiguration;
import net.shyshkin.study.itemwriters.config.DatabaseConfig;
import net.shyshkin.study.itemwriters.config.JdbcWriteBatchConfiguration;
import net.shyshkin.study.itemwriters.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {AppConfiguration.class, JdbcWriteBatchConfiguration.class, DatabaseConfig.class})
class JdbcWriterJobTest extends AbstractJobTest {

    @Autowired
    JdbcBatchItemWriter<Product> itemWriter;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/product.csv");
        return paramsBuilder.toJobParameters();
    }

    @Test
    void jdbcWriterJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("jdbcWriteJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
    }

    @Test
    void jdbcWriterStepTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("writeJdbc", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

}