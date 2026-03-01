package api;

import config.SmsConfig;

public class SmsProviderFactory {

    public static SmsProvider createProvider() {
        String provider = SmsConfig.getSmsProvider();

        switch (provider.toLowerCase()) {
            case "smsmode":
                return new SmsModeProvider(SmsConfig.getSmsModeApiKey());
            case "textbelt":
                return new TextBeltProvider();
            case "simulation":
            default:
                return new SimulationSmsProvider();
        }
    }
}