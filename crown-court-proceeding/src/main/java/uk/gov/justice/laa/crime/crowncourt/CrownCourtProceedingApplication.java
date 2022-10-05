package uk.gov.justice.laa.crime.crowncourt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class CrownCourtProceedingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrownCourtProceedingApplication.class);
    }

}
