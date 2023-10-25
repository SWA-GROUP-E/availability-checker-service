package edu.miu.cs.acs.domain.controlflow;

import edu.miu.cs.acs.models.ApiTestStatus;
import edu.miu.cs.acs.models.CheckedApiMessage;
import edu.miu.cs.acs.service.apicallservice.ApiTestService;
import edu.miu.cs.acs.service.keyextraction.KeyExtraction;
import edu.miu.cs.acs.service.keyextraction.KeyExtractionProperties;
import edu.miu.cs.acs.utils.UrlUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Set;


/**
 * A provider pattern service that encapsulates all possible outcomes of the api status
 */
@Log4j2
@Service
@AllArgsConstructor
public class BusinessOrchestrator {

    ApiTestService apiCallTest;
    KeyExtraction keyExtractor;

    private KeyExtractionProperties keyExtractionProperties;

    /**
     * service entry point that starts all the logic of the service (it tests the api for all the possible scenarios and determine the type of the message to publish)
     * @param url
     * @return CheckedAPIMessage
     */
    public CheckedApiMessage testApi(String url){
        if(!UrlUtils.isValidURL(url)) {
            log.warn("Invalid Api URL {}", url);
            return CheckedApiMessage.builder()
                    .testStatus(ApiTestStatus.FAILED)
                    .apiUrl(url).build();
        }

        ApiTestStatus flowResult = apiCallTest.test(url);
        return CheckedApiMessage.builder()
                .testStatus(flowResult)
                .apiUrl(url)
                .build();
    }

    /**
     * communicates with the apikey extractor service and tests against the returned key
     * @param url
     * @return CheckedAPIMessage
     */
    public CheckedApiMessage tryToExtractKey(String url) {
        try {
            Set<String> apiKeys = keyExtractor.getKeys(url, keyExtractionProperties.getScrapingDepthLevel());
            String validApiKey = null;
            for (String apiKey : apiKeys) {
                if (apiCallTest.test(url, apiKey) == ApiTestStatus.SUCCESSFUL) {
                    validApiKey = apiKey;
                }
            }
            if (validApiKey != null) {
                return CheckedApiMessage.builder()
                        .testStatus(ApiTestStatus.SUCCESSFUL_AUTHORIZED)
                        .apiUrl(url)
                        .apiKey(validApiKey)
                        .build();
            }
        } catch (Exception ex) {
            log.warn("Couldn't extract key for {}", url);
        }
        return CheckedApiMessage.builder()
                .testStatus(ApiTestStatus.FAILED_UNAUTHORIZED)
                .apiUrl(url)
                .build();
    }
}
