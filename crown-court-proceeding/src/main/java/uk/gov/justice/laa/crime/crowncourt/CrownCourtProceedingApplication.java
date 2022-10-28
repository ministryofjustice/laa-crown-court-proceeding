package uk.gov.justice.laa.crime.crowncourt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass=true)
@Slf4j
public class CrownCourtProceedingApplication {

    public static void main(String[] args) {
        log.info("********** CrownCourtProceedingApplication start **************");
        SpringApplication.run(CrownCourtProceedingApplication.class);
    }

}
