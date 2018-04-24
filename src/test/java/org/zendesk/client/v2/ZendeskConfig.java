package org.zendesk.client.v2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author stephenc
 * @since 04/04/2013 13:53
 */
public final class ZendeskConfig {

    private static final String ZENDESK_VARIABLE_PREFIX = "ZENDESK_JAVA_CLIENT_TEST_";

    private ZendeskConfig() {
        throw new IllegalAccessError("utility class");
    }

    /**
     * Reads variables from a properties file, or if none found, reads from environment variables
     * Environment variables should have a prefix, "ZENDESK_JAVA_CLIENT_TEST_" to be properly used
     *
     * @return A Properties object
     */
    public static Properties load() {
        Properties result = new Properties();
        System.out.println("zendesk properies file length: " + new File(System.getProperty("user.dir") + "/ws/src/test/resources/zendesk.properties").length());
        System.out.println(System.getProperty("user.dir") + "");
        InputStream is = ZendeskConfig.class.getResourceAsStream("/zendesk.properties");
        if (is == null) {
            Map<String, String> systemVars = System.getenv();
            for (String key : systemVars.keySet()) {
                if (key.startsWith(ZENDESK_VARIABLE_PREFIX)) {
                    // Remove the prefix and normalize the key name
                    String newKey = key.substring(ZENDESK_VARIABLE_PREFIX.length()).toLowerCase().replaceAll("_", ".");
                    result.put(newKey, systemVars.get(key));
                }
            }

            if (result.isEmpty()) {
                System.out.println("Result is empty");
                return null;
            }
        } else {
            try {
                result.load(is);
                is.close();
            } catch (IOException e) {
                System.out.println("unable to Load");
                return null;
            }
        }
        return result;
    }
}
