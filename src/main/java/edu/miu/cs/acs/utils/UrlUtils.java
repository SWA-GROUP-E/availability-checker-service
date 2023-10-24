package edu.miu.cs.acs.utils;

import java.net.URI;
import java.net.URL;

public class UrlUtils {

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
