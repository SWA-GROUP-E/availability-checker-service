package edu.miu.cs.acs.service.apicallservice;

import edu.miu.cs.acs.models.ApiTestStatus;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.Optional;

/**
 * A Service that tests the status of the API
 */
@Log4j2
@AllArgsConstructor
@Service
public class ApiTestService {

    private RestOperations restOperations;

    private ApiTestProperties apiTestProperties;
    public ApiTestStatus test(String url) {
        return test(url, null);
    }

    /**
     * Service entry point takes an api and its key determines if its a valid api or not
     * @param url
     * @param apiKey
     * @return ApiTestStatus
     */
    public ApiTestStatus test(String url, String apiKey) {
        try {
            log.info("Testing url {}. apiKey = {}", url, apiKey);
            return headForStatus(url, Optional.ofNullable(apiKey));
        } catch (Exception ex1) {
            log.error("An error occurred while testing the url {}. Method = HEAD. Error message: {}", url, ex1.getMessage());
            try {
                return getForStatus(url, Optional.ofNullable(apiKey));
            } catch (Exception ex2) {
                log.error("An error occurred while testing the url {}. Method = GET. Error message: {}", url, ex2.getMessage());
            }
        }
        return ApiTestStatus.FAILED;
    }

    private ApiTestStatus headForStatus(String url, Optional<String> apiKeyOpt) throws Exception {
        return exchangeForStatus(url, HttpMethod.HEAD, apiKeyOpt);
    }

    private ApiTestStatus getForStatus(String url, Optional<String> apiKeyOpt) throws Exception {
        return exchangeForStatus(url, HttpMethod.GET, apiKeyOpt);
    }

    private ApiTestStatus exchangeForStatus(String url, HttpMethod method, Optional<String> apiKeyOpt) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        apiKeyOpt.ifPresent(apiKey -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "Basic " + apiKey));
        HttpEntity<String> request = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restOperations.exchange(url, method, request, String.class);
        HttpStatusCode statusCode = response.getStatusCode();
        log.info("Method = {}, responseStatus = {}, url = {}. ", method, statusCode, url);
        if (apiTestProperties.getSuccessfulHttpStatuses().contains(statusCode.value())) {
            return ApiTestStatus.SUCCESSFUL;
        } else if (apiTestProperties.getUnauthorizedHttpStatuses().contains(statusCode.value())) {
            return ApiTestStatus.UNAUTHORIZED;
        }

        return ApiTestStatus.FAILED;
    }
}
