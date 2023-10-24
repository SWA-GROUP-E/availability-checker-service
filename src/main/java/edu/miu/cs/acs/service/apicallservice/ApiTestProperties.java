package edu.miu.cs.acs.service.apicallservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.api.test")
public class ApiTestProperties implements InitializingBean {
    private Set<Integer> successfulHttpStatuses;
    private Set<Integer> unauthorizedHttpStatuses;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (successfulHttpStatuses == null || successfulHttpStatuses.isEmpty()) {
            throw new IllegalArgumentException("Property app.api.test.successful-http-statuses must be configured");
        }
        if (unauthorizedHttpStatuses == null || unauthorizedHttpStatuses.isEmpty()) {
            throw new IllegalArgumentException("Property app.api.test.unauthorized-http-statuses must be configured");
        }
    }
}
