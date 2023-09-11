package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded;

import com.google.gson.Gson;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;



public class TestConfig {

   /* @Bean
    @Primary
    public QueueMessageLogService queueMessageLogService(){
        return Mockito.mock(QueueMessageLogService.class);
    }

    @Bean
    @Primary
    public ProsecutionConcludedService prosecutionConcludedService(){
        return Mockito.mock(ProsecutionConcludedService.class);
    }

    @Bean
    @Primary
    public Gson gson(){
        return Mockito.mock(Gson.class);
    }*/
}
