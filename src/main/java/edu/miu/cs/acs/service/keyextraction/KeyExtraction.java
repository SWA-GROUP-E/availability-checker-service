package edu.miu.cs.acs.service.keyextraction;

import edu.miu.cs.acs.utils.UrlUtils;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class KeyExtraction {
    public String getKey(String url) {
        try {
            String domain = UrlUtils.extractDomain(url);
            // Connect to the web page.
            Document doc = Jsoup.connect(domain).get();

            // Select all input elements on the page.
            Elements inputElements = doc.select("input");

            // For each input element, check if it contains the value "api_key".
            for (Element inputElement : inputElements) {
                if (inputElement.attr("value").equals("api_key")) {
                    // If the input element contains the value "api_key", extract the API key from the element.
                    String apiKey = inputElement.attr("value");

                    return apiKey;
                }
            }
        } catch (Exception e) {
            log.warn("error while extracting key for {} error message: {}", url, e.getMessage());
        }

        return null;
    }
}
