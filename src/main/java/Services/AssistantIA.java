package Services;

import Models.AiContext;
import Models.Budget;

import java.util.List;

public class AssistantIA {
    private final BudgetInsightsService insightsService = new BudgetInsightsService();
    private final AiAssistantService aiService = new AiAssistantService();

    public String analyserDepenses(List<Budget> budgets) {
        AiContext context = insightsService.buildContext(budgets);
        String userMessage = "Analyse mes depenses et mon budget, puis donne un conseil simple.";
        return aiService.getReply(userMessage, context);
    }
}
