package edu.miu.cs.acs.utils;

import java.net.URI;
import java.net.URL;

public class UrlUtils {

    /**
     * checks the validity of the input url
     * @param url
     * @return
     */
    public static boolean isValidURL(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * get the domain of the url input
     * @param url
     * @return
     */
    public static String extractDomain(String url) {
        if (!isValidURL(url)) {
            return null;
        }

        URI uri = URI.create(url);
        String host = uri.getHost();

        if (host.contains(".api")) {
            host = host.replace(".api", "");
        }

        return uri.getScheme() + "://" + host;
    }
}
