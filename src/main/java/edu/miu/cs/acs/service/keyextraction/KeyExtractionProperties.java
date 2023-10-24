package edu.miu.cs.acs.service.keyextraction;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.key-extraction")
public class KeyExtractionProperties implements InitializingBean {

    private Integer scrapingDepthLevel;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (scrapingDepthLevel == null) {
            throw new IllegalArgumentException("Property app.key-extraction.scraping-depth-level must be configured");
        }
    }
}
