package Services;

import Models.AiContext;
import Models.Budget;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetInsightsService {
    private final Connection connection;

    public BudgetInsightsService() {
        this.connection = MyDatabase.getInstance().getConn();
    }

    public AiContext buildContext(List<Budget> budgets) {
        AiContext context = new AiContext();
        double totalInitial = 0;
        double totalRemaining = 0;
        double totalSpent = 0;

        if (budgets != null) {
            for (Budget budget : budgets) {
                double initial = budget.getMontantInitial();
                double remaining = budget.getMontantTotal();
                double spent = Math.max(0, initial - remaining);

                totalInitial += initial;
                totalRemaining += remaining;
                totalSpent += spent;
            }
        }

        context.setTotalBudgetInitial(totalInitial);
        context.setTotalBudgetRemaining(totalRemaining);
        context.setTotalSpent(totalSpent);
        context.setSpentByCategoryMonth(loadCategoryTotalsForCurrentMonth());
        return context;
    }

    private Map<String, Double> loadCategoryTotalsForCurrentMonth() {
        Map<String, Double> totals = new HashMap<>();
        if (connection == null) {
            return totals;
        }

        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDate nextMonth = firstDay.plusMonths(1);
        String sql = "SELECT categorie, SUM(montant) AS total FROM depense "
                + "WHERE date_depense >= ? AND date_depense < ? "
                + "GROUP BY categorie";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(firstDay));
            ps.setDate(2, java.sql.Date.valueOf(nextMonth));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String categorie = rs.getString("categorie");
                    double total = rs.getDouble("total");
                    totals.put(categorie, total);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des dépenses par catégorie:");
            e.printStackTrace();
        }

        return totals;
    }
}
