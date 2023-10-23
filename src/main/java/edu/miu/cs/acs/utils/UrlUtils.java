package edu.miu.cs.acs.utils;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final String URL_REGEX = "^(http|https)://\\w+\\.\\w+(\\/[\\w\\.]+)*$";

    public static boolean isValidURL(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(url);

        return matcher.matches();
    }

    public static String extractDomain(String url) {
        if (url == null || url.isEmpty() || !isValidURL(url)) {
            return null;
        }

        URI uri = URI.create(url);
        String host = uri.getHost();

        if (host.contains(".api")) {
            host = host.replace(".api", "");
        }

        return host;
    }
}
