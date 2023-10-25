package edu.miu.cs.acs.service.keyextraction;

import edu.miu.cs.acs.utils.UrlUtils;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Log4j2
public class KeyExtraction {

    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9]{32}$");

    public static boolean isPotentialApiKey(String string) {
        Matcher matcher = API_KEY_PATTERN.matcher(string);
        return matcher.matches();
    }

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
        ArrayList<Element> potentialKeyElements = getPotentialKeyPlaces(document);

        Set<String> apiKeys = potentialKeyElements.stream()
                .map(Element::text)
                .filter(KeyExtraction::isPotentialApiKey)
                .collect(Collectors.toSet());

        return apiKeys;
    }

    public static ArrayList<Element> getPotentialKeyPlaces(Document document) {
        ArrayList<Element> apiKeys = new ArrayList<>();

        // Scenario 1: Add all input elements with a name that matches "api-key*" or any variations.
        apiKeys.addAll(document.select("[name^=api-key], [name^=api_key], [name^=api-key-id], [name^=api-key-secret], [name^=api-key-token], [name^=api-key-password], [name^=api-key-auth], [name^=api-key-bearer], [name^=api-key-consumer], [name^=api-key-client], [name^=api-key-developer], [name^=api-key-partner], [name^=api-key-vendor], [name^=api-key-public], [name^=api-key-private], [name^=api-key-test], [name^=api-key-prod], [name^=api-key-dev], [name^=api-key-staging], [name^=api-key-production], [name^=api-key-live], [name^=api-key-sandbox], [name^=api-key-sandbox-id], [name^=api-key-sandbox-secret], [name^=api-key-sandbox-token], [name^=api-key-sandbox-password], [name^=api-key-sandbox-auth], [name^=api-key-sandbox-bearer], [name^=api-key-sandbox-consumer], [name^=api-key-sandbox-client], [name^=api-key-sandbox-developer], [name^=api-key-sandbox-partner], [name^=api-key-sandbox-vendor], [name^=api-key-sandbox-public], [name^=api-key-sandbox-private], [name^=api-key-sandbox-test], [name^=api-key-sandbox-prod], [name^=api-key-sandbox-dev], [name^=api-key-sandbox-staging], [name^=api-key-sandbox-production], [name^=api-key-sandbox-live]"));

        // Scenario 2: Add all elements with a class that contains "api-key" or any variations.
        apiKeys.addAll(document.select("[class~=.*\\bapi-key\\b.*]"));
        apiKeys.addAll(document.select("[class~=.*\\bapi_key\\b.*]"));

        // Scenario 3: Add all elements with a data-* attribute that contains "api-key" or any variations.
        apiKeys.addAll(document.select("[data-*~=.*\\bapi-key\\b.*]"));
        apiKeys.addAll(document.select("[data-*~=.*\\bapi_key\\b.*]"));

        // Scenario 4: Add all elements that contain the text "api-key" or any variations.
        apiKeys.addAll(document.select(":containsOwn(api-key)"));
        apiKeys.addAll(document.select(":containsOwn(api_key)"));

        // Scenario 5: Add all elements with ID containing "api-key" or any variations.
        apiKeys.addAll(document.select("[id~=.*\\bapi-key\\b.*]"));
        apiKeys.addAll(document.select("[id~=.*\\bapi_key\\b.*]"));

        // Scenario 6: Add all elements with a specific attribute value that matches any variations of "api-key".
        apiKeys.addAll(document.select("[data-custom-attribute~=^api-key|^api_key|^api-key-id|^api-key-secret|^api-key-token|^api-key-password|^api-key-auth|^api-key-bearer|^api-key-consumer|^api-key-client|^api-key-developer|^api-key-partner|^api-key-vendor|^api-key-public|^api-key-private|^api-key-test|^api-key-prod|^api-key-dev|^api-key-staging|^api-key-production|^api-key-live|^api-key-sandbox|^api-key-sandbox-id|^api-key-sandbox-secret|^api-key-sandbox-token|^api-key-sandbox-password|^api-key-sandbox-auth|^api-key-sandbox-bearer|^api-key-sandbox-consumer|^api-key-sandbox-client|^api-key-sandbox-developer|^api-key-sandbox-partner|^api-key-sandbox-vendor|^api-key-sandbox-public|^api-key-sandbox-private|^api-key-sandbox-test|^api-key-sandbox-prod|^api-key-sandbox-dev|^api-key-sandbox-staging|^api-key-sandbox-production|^api-key-sandbox-live]"));

        // Scenario 7: Add all elements inside a specific parent element with class "api-key" or any variations.
        apiKeys.addAll(document.select("parent-selector > [class~=.*\\bapi-key\\b.*]"));
        apiKeys.addAll(document.select("parent-selector > [class~=.*\\bapi_key\\b.*]"));

        // Scenario 8: Add all elements with a specific attribute name that contains "api-key" or any variations.
        apiKeys.addAll(document.select("[data-*=.*\\bapi-key\\b.*]"));
        apiKeys.addAll(document.select("[data-*=.*\\bapi_key\\b.*]"));

        // Add all input elements with a name that starts with "api-key".
        apiKeys.addAll(document.select("[name^=api-key]"));
        apiKeys.addAll(document.select("[name^=api_key]"));

        // Add all elements with a class that contains the string "api-key".
        apiKeys.addAll(document.select(".api-key"));
        apiKeys.addAll(document.select(".api_key"));

        // Add all elements with a data-* attribute that contains the string "api-key".
        apiKeys.addAll(document.select("[data-*]~=api-key"));
        apiKeys.addAll(document.select("[data-*]~=api_key"));

        return apiKeys;
    }
}
