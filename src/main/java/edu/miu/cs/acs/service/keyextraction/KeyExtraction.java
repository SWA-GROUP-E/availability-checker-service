package edu.miu.cs.acs.service.keyextraction;

import edu.miu.cs.acs.utils.UrlUtils;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Log4j2
public class KeyExtraction {

    public Set<String> getKeys(String url, int depth) {
        Set<String> apiKeys = new HashSet<>();
        if (depth <= 0) {
            return apiKeys;
        }
        String domain = UrlUtils.extractDomain(url);
        try {
            // Connect to the web page.
            Document doc = Jsoup.connect(url).get();
            apiKeys.addAll(getKeys(doc));
            Set<String> testUrls = extractUrls(domain, doc);
            depth--;
            for (String testUrl : testUrls) {
                apiKeys.addAll(getKeys(testUrl, depth));
            }
        } catch (Exception e) {
            log.debug("error while extracting key for {} error message: {}, depth: {}", url, e.getMessage(), depth);
        }

        return apiKeys;
    }

    private Set<String> extractUrls(String baseDomain, Document document) {
        Set<String> urls = new HashSet<>();
        Elements linkElements = document.select("a[href!=''][href]");
        for (Element linkElement : linkElements) {
            String pathOrUrl = linkElement.attr("href");
            String testUrl;
            if (pathOrUrl.trim().startsWith("/")) {
                testUrl = baseDomain + pathOrUrl;
            } else {
                testUrl = pathOrUrl;
            }
            urls.add(testUrl);
        }
        return urls;
    }

    private Set<String> getKeys(Document document) {
        Set<String> apiKeys = new HashSet<>();
        // Select all input elements on the page.
        Elements inputElements = document.select("input");

        // For each input element, check if it contains the value "api_key".
        for (Element inputElement : inputElements) {
            if (inputElement.attr("value").equals("api_key")) {
                // If the input element contains the value "api_key", extract the API key from the element.
                String apiKey = inputElement.attr("value");
                apiKeys.add(apiKey);
            }
        }

        return apiKeys;
    }
}
