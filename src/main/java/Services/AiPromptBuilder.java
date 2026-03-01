package Services;

import Models.AiContext;

import java.util.Map;
import java.util.StringJoiner;

public class AiPromptBuilder {
    public static String buildSystemPrompt() {
        return "Tu es un conseiller financier. Donne des conseils simples et pratiques pour aider l'utilisateur a gerer son budget.";
    }

    public static String buildUserPrompt(String userMessage, AiContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Question utilisateur: ").append(userMessage).append("\n");
        sb.append("Contexte budget:\n");
        sb.append("- Budget initial total: ")
                .append(formatAmount(context.getTotalBudgetInitial(), context.getCurrency()))
                .append("\n");
        sb.append("- Budget restant total: ")
                .append(formatAmount(context.getTotalBudgetRemaining(), context.getCurrency()))
                .append("\n");
        sb.append("- Depenses totales: ")
                .append(formatAmount(context.getTotalSpent(), context.getCurrency()))
                .append("\n");

        Map<String, Double> categories = context.getSpentByCategoryMonth();
        if (categories != null && !categories.isEmpty()) {
            sb.append("Depenses par categorie (mois en cours): ");
            StringJoiner joiner = new StringJoiner(", ");
            for (Map.Entry<String, Double> entry : categories.entrySet()) {
                joiner.add(entry.getKey() + "=" + formatAmount(entry.getValue(), context.getCurrency()));
            }
            sb.append(joiner).append("\n");
        } else {
            sb.append("Depenses par categorie (mois en cours): aucune donnee.\n");
        }

        sb.append("Reponds avec un resume des depenses/budget et propose 1 a 2 idees simples pour reduire les depenses.");
        return sb.toString();
    }

    private static String formatAmount(double value, String currency) {
        return String.format("%.2f %s", value, currency);
    }
}
