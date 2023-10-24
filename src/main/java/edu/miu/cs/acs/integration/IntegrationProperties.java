package edu.miu.cs.acs.integration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.kafka")
public class IntegrationProperties implements InitializingBean {
    private String inputDestination;
    private String successDestination;
    private String failedDestination;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (inputDestination == null || inputDestination.isBlank()) {
            throw new IllegalArgumentException("Property app.kafka.input-destination must be configured");
        }
        if (successDestination == null || successDestination.isBlank()) {
            throw new IllegalArgumentException("Property app.kafka.success-destination must be configured");
        }
        if (failedDestination == null || failedDestination.isBlank()) {
            throw new IllegalArgumentException("Property app.kafka.failed-destination must be configured");
        }
    }
}
