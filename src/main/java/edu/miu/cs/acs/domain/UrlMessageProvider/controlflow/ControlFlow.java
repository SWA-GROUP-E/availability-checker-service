package edu.miu.cs.acs.domain.UrlMessageProvider.controlflow;

import edu.miu.cs.acs.domain.UrlMessageProvider.apicallservice.ApiTestService;
import edu.miu.cs.acs.domain.UrlMessageProvider.keyextraction.KeyExtraction;
import edu.miu.cs.acs.domain.UrlMessageProvider.models.*;
import edu.miu.cs.acs.utils.UrlUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class ControlFlow {

    ApiTestService apiCallTest;
    KeyExtraction keyExtractor;

    public CheckedAPIMessage handle(String url) throws Exception {
        if(!UrlUtils.isValidURL(url)) {
            log.warn("Invalid Api URL {}", url);
            throw new Exception("Invalid URL");
        }

        ApiTestStatus flowResult = apiCallTest.test(url);
        if(flowResult == ApiTestStatus.SUCCESSFUL) {
            return new UnauthorizedApiMessage(ApiTestStatus.UNAUTHORIZED, url);
        } else if (flowResult == ApiTestStatus.UNAUTHORIZED) {
            return tryToExtractKey(url);
        }

        return new FailedApiMessage(ApiTestStatus.FAILED, url);
    }

    private CheckedAPIMessage tryToExtractKey(String url) {
        try {
            String apikey = keyExtractor.getKey(url);
            return new SuccessfulApiMessage(ApiTestStatus.SUCCESSFUL, url, apikey);
        } catch (Exception ex) {
            log.warn("Couldn't extract key for {}", url);
            return new FailedApiMessage(ApiTestStatus.FAILED, url);
        }
    }
}
