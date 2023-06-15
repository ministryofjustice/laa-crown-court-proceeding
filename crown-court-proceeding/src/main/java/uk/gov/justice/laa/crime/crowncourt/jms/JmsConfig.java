package uk.gov.justice.laa.crime.crowncourt.jms;


import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import uk.gov.justice.laa.crime.crowncourt.config.SqsClientConfig;

import jakarta.jms.Session;


@Slf4j
@AllArgsConstructor
@Configuration
@EnableJms
public class JmsConfig {
    private final JmsErrorHandler jmsErrorHandler;
    private final SqsClientConfig sqsClientConfig;

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(sqsConnectionFactory());
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("1");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(jmsErrorHandler);
        return factory;
    }

    @Bean
    public SQSConnectionFactory sqsConnectionFactory() {
        return new SQSConnectionFactory(new ProviderConfiguration(), sqsClientConfig.awsSqsClient());
    }
}


