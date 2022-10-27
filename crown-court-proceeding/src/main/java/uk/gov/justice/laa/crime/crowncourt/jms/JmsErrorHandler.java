package uk.gov.justice.laa.crime.crowncourt.jms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Slf4j
@Service
public class JmsErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable t) {
        log.warn("In default JMS error handler...");
        log.error("Error Message : {}", t.getMessage());
        throw new RuntimeException(t.getMessage());
    }

}
