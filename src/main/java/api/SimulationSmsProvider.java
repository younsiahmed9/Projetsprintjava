package api;

public class SimulationSmsProvider implements SmsProvider {

    @Override
    public boolean sendSms(String to, String message) {
        System.out.println("\n📱 [SIMULATION SMS]");
        System.out.println("   À: " + to);
        System.out.println("   Message: " + message);
        System.out.println("   ✅ Simulation réussie (aucun SMS réel envoyé)");
        return true;
    }

    @Override
    public String getProviderName() {
        return "simulation";
    }
}