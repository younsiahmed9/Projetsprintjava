package Services;

import Models.AiContext;
import Models.Budget;

import java.util.List;

public class AssistantIA {
    private final BudgetInsightsService insightsService;
    private final AiAssistantService aiService;

    public AssistantIA() {
        this(new AiAssistantService(), new BudgetInsightsService());
    }

    public AssistantIA(AiAssistantService aiService, BudgetInsightsService insightsService) {
        this.aiService = aiService;
        this.insightsService = insightsService;
    }

    public String analyserDepenses(List<Budget> budgets) {
        AiContext context = insightsService.buildContext(budgets);
        String userMessage = "Analyse mes depenses et mon budget, puis donne un conseil simple.";
        return aiService.getReply(userMessage, context);
    }
}
