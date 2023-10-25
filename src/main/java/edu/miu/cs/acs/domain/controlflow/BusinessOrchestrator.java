package edu.miu.cs.acs.domain.controlflow;

import edu.miu.cs.acs.models.*;
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
    public CheckedAPIMessage handle(String url){
        if(!UrlUtils.isValidURL(url)) {
            log.warn("Invalid Api URL {}", url);
            return new FailedApiMessage(ApiTestStatus.FAILED, url);
        }

        ApiTestStatus flowResult = apiCallTest.test(url);
        if(flowResult == ApiTestStatus.SUCCESSFUL) {
            return new UnauthorizedApiMessage(ApiTestStatus.UNAUTHORIZED, url);
        } else if (flowResult == ApiTestStatus.UNAUTHORIZED) {
            return tryToExtractKey(url);
        }

        return new FailedApiMessage(ApiTestStatus.FAILED, url);
    }


    /**
     * communicates with the apikey extractor service and tests against the returned key
     * @param url
     * @return CheckedAPIMessage
     */
    private CheckedAPIMessage tryToExtractKey(String url) {
        try {
            Set<String> apiKeys = keyExtractor.getKeys(url, keyExtractionProperties.getScrapingDepthLevel());
            String validApiKey = null;
            for (String apiKey : apiKeys) {
                if (apiCallTest.test(url, apiKey) == ApiTestStatus.SUCCESSFUL) {
                    validApiKey = apiKey;
                }
            }
            if (validApiKey != null) {
                return new SuccessfulApiMessage(ApiTestStatus.SUCCESSFUL, url, validApiKey);
            }
        } catch (Exception ex) {
            log.warn("Couldn't extract key for {}", url);
        }
        return new FailedApiMessage(ApiTestStatus.FAILED, url);
    }
}
