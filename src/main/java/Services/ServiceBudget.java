package Services;

import Models.Budget;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceBudget implements Iservice<Budget> {
    private Connection connection;

    public ServiceBudget() {
        connection = MyDatabase.getInstance().getConn();
    }

    @Override
    public void ajouter(Budget budget) throws SQLDataException {
        String sql = "INSERT INTO budget (id_utilisateur, nom_budget, montant_total, periode, statut, date_creation) VALUES ('"
                + budget.getIdUtilisateur() + "', '"
                + budget.getNomBudget() + "', '"
                + budget.getMontantTotal() + "', '"
                + budget.getPeriode() + "', '"
                + budget.getStatut() + "', '"
                + new java.sql.Date(budget.getDateCreation().getTime()) + "')";

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void supprimer(Budget budget) throws SQLDataException {
        String sql = "DELETE FROM budget WHERE id_budget = '" + budget.getIdBudget() + "'";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void modifier(Budget budget) throws SQLDataException {
        String sql = "UPDATE budget SET nom_budget='" + budget.getNomBudget()
                + "', montant_total='" + budget.getMontantTotal()
                + "', periode='" + budget.getPeriode()
                + "', statut='" + budget.getStatut()
                + "' WHERE id_budget='" + budget.getIdBudget() + "'";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Budget> recuperer() throws SQLDataException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Budget b = new Budget();
                b.setIdBudget(rs.getInt("id_budget"));
                b.setIdUtilisateur(rs.getInt("id_utilisateur"));
                b.setNomBudget(rs.getString("nom_budget"));
                b.setMontantTotal(rs.getDouble("montant_total"));
                b.setPeriode(rs.getString("periode"));
                b.setStatut(rs.getString("statut"));
                b.setDateCreation(rs.getDate("date_creation"));
                budgets.add(b);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return budgets;
    }
}
