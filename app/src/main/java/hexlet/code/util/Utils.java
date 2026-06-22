package hexlet.code.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class Utils {

    public static String normalizeUrl(String rawUrl) {
        try {
            URL url = URI.create(rawUrl.trim()).toURL();
            return String.format("%s://%s", url.getProtocol(), url.getAuthority())
                    .toLowerCase();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + rawUrl, e);
        }
    }
}
