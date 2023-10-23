package edu.miu.cs.acs.domain.controlflow;

import edu.miu.cs.acs.domain.models.*;
import edu.miu.cs.acs.models.*;
import edu.miu.cs.acs.service.apicallservice.ApiTestService;
import edu.miu.cs.acs.service.keyextraction.KeyExtraction;
import edu.miu.cs.acs.domain.urlmessageprovider.models.*;
import edu.miu.cs.acs.utils.UrlUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class ApiControlFlow {

    ApiTestService apiCallTest;
    KeyExtraction keyExtractor;

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
