package edu.miu.cs.acs.integration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.kafka")
public class IntegrationProperties {
    private String inputDestination;
    private String successDestination;
    private String failedDestination;
}
