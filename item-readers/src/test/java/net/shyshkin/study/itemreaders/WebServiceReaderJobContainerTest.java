package net.shyshkin.study.itemreaders;

import net.shyshkin.study.itemreaders.config.AppConfiguration;
import net.shyshkin.study.itemreaders.config.WebServiceReadBatchConfiguration;
import net.shyshkin.study.itemreaders.containers.ProductServiceAbstractTest;
import net.shyshkin.study.itemreaders.model.Product;
import net.shyshkin.study.itemreaders.service.ProductClient;
import net.shyshkin.study.itemreaders.service.ProductClientAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {AppConfiguration.class, WebServiceReadBatchConfiguration.class, ProductClientAdapter.class, ProductClient.class})
class WebServiceReaderJobContainerTest extends ProductServiceAbstractTest {

    @Autowired
    ItemReaderAdapter<Product> itemReader;

    @Autowired
    ProductClientAdapter adapter;

    @AfterEach
    void reinitClientAdapter() {
        adapter.init();
    }

    @Test
    void listReaderJobText() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("webServiceReadJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void webServiceReaderStepTest() {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("readWebService");
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                                () -> assertThat(execution.getWriteCount()).isEqualTo(3),
                                () -> assertThat(execution.getReadCount()).isEqualTo(3)
                        )
                );
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void webServiceReaderStepScopeTest() throws Exception {
        //given
        StepExecution stepExecution = MetaDataInstanceFactory
                .createStepExecution();

        //when
        StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            Product productRead;
//            itemReader.open(stepExecution.getExecutionContext());
            while ((productRead = itemReader.read()) != null) {

                //then
                Product product = productRead;
                assertAll(
                        () -> assertThat(product).hasNoNullFieldsOrProperties(),
                        () -> assertThat(product.getProductID()).isGreaterThan(0),
                        () -> assertThat(product.getPrice()).isGreaterThan(new BigDecimal("0.0")),
                        () -> assertThat(product.getProductName()).isNotEmpty(),
                        () -> assertThat(product.getProductDesc()).isNotEmpty()
                );
            }
//            itemReader.close();
            return null;
        });
    }

    private List<Product> mockProducts() {
        return List.of(
                Product.builder()
                        .productID(1L)
                        .productName("Apple WEB")
                        .productDesc("apple cell phone")
                        .price(new BigDecimal("500.00"))
                        .unit(10L)
                        .build(),
                Product.builder()
                        .productID(2L)
                        .productName("Dell WEB")
                        .productDesc("dell computer")
                        .price(new BigDecimal("3000"))
                        .unit(5L)
                        .build(),
                Product.builder()
                        .productID(3L)
                        .productName("office WEB")
                        .productDesc("ms office software")
                        .price(new BigDecimal("196"))
                        .unit(23L)
                        .build()
        );
    }

}
