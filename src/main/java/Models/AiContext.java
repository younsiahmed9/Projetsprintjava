package Models;

import java.util.Collections;
import java.util.Map;

public class AiContext {
    private double totalBudgetInitial;
    private double totalBudgetRemaining;
    private double totalSpent;
    private Map<String, Double> spentByCategoryMonth = Collections.emptyMap();
    private String currency = "DT";

    public double getTotalBudgetInitial() {
        return totalBudgetInitial;
    }

    public void setTotalBudgetInitial(double totalBudgetInitial) {
        this.totalBudgetInitial = totalBudgetInitial;
    }

    public double getTotalBudgetRemaining() {
        return totalBudgetRemaining;
    }

    public void setTotalBudgetRemaining(double totalBudgetRemaining) {
        this.totalBudgetRemaining = totalBudgetRemaining;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Map<String, Double> getSpentByCategoryMonth() {
        return spentByCategoryMonth;
    }

    public void setSpentByCategoryMonth(Map<String, Double> spentByCategoryMonth) {
        this.spentByCategoryMonth = spentByCategoryMonth;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
