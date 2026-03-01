package config;

import java.io.InputStream;
import java.util.Properties;

public class SmsConfig {

    private static final String CONFIG_FILE = "config/sms.properties";
    private static Properties props = new Properties();

    static {
        try (InputStream input = SmsConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
            } else {
                props.setProperty("sms.provider", "simulation");
                props.setProperty("smsmode.api_key", "");
                props.setProperty("textbelt.api_key", "");
            }
        } catch (Exception e) {
            props.setProperty("sms.provider", "simulation");
        }
    }

    public static String getSmsProvider() {
        return props.getProperty("sms.provider", "simulation");
    }

    public static String getSmsModeApiKey() {
        return props.getProperty("smsmode.api_key", "");
    }

    public static String getTextbeltApiKey() {
        return props.getProperty("textbelt.api_key", "");
    }
}