package edu.miu.cs.acs.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.api.test")
public class ApiTestProperties {
    private Set<Integer> successfulHttpStatuses;
    private Set<Integer> unauthorizedHttpStatuses;
}
