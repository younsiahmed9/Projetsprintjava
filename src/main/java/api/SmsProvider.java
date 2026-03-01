package api;

public interface SmsProvider {
    boolean sendSms(String to, String message);
    String getProviderName();
}