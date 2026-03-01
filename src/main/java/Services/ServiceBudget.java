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
        String sql = "INSERT INTO budget (id_utilisateur, nom_budget, montant_total, periode, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, budget.getIdUtilisateur());
            ps.setString(2, budget.getNomBudget());
            ps.setDouble(3, budget.getMontantTotal());
            ps.setString(4, budget.getPeriode());
            ps.setString(5, budget.getStatut());
            ps.setDate(6, new java.sql.Date(budget.getDateCreation().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void supprimer(Budget budget) throws SQLDataException {
        // First delete associated expenses to avoid foreign key constraint error
        String deleteExpensesSql = "DELETE FROM depense WHERE id_budget = ?";
        try (PreparedStatement psExpenses = connection.prepareStatement(deleteExpensesSql)) {
            psExpenses.setInt(1, budget.getIdBudget());
            psExpenses.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting associated expenses: " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }

        // Then delete the budget itself
        String sql = "DELETE FROM budget WHERE id_budget = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, budget.getIdBudget());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void modifier(Budget budget) throws SQLDataException {
        String sql = "UPDATE budget SET nom_budget=?, montant_total=?, periode=?, statut=? WHERE id_budget=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, budget.getNomBudget());
            ps.setDouble(2, budget.getMontantTotal());
            ps.setString(3, budget.getPeriode());
            ps.setString(4, budget.getStatut());
            ps.setInt(5, budget.getIdBudget());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Budget> recuperer() throws SQLDataException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budget";
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
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

    public Budget recupererParId(int idBudget) throws SQLDataException {
        String sql = "SELECT * FROM budget WHERE id_budget = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idBudget);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Budget b = new Budget();
                    b.setIdBudget(rs.getInt("id_budget"));
                    b.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    b.setNomBudget(rs.getString("nom_budget"));
                    b.setMontantTotal(rs.getDouble("montant_total"));
                    b.setPeriode(rs.getString("periode"));
                    b.setStatut(rs.getString("statut"));
                    b.setDateCreation(rs.getDate("date_creation"));
                    return b;
                }
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return null;
    }
}
