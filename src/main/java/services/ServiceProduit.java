package services;

import models.Produit;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProduit {
    private Connection conn;

    public ServiceProduit() {
        conn = MyDataBase.getInstance().getConnection();
    }

    public void ajouter(Produit produit) throws SQLException {
        String query = "INSERT INTO produit (nom_produit, type_produit, montant, code_unique, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeProduit());
            pstmt.setBigDecimal(3, produit.getMontant());
            pstmt.setString(4, produit.getCodeUnique());
            pstmt.setString(5, produit.getStatut());
            pstmt.setDate(6, Date.valueOf(produit.getDateCreation()));
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                produit.setIdProduit(rs.getInt(1));
            }
        }
    }

    public List<Produit> recupererTous() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit ORDER BY id_produit DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    public List<Produit> getProduitsDisponibles() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE statut = 'disponible' ORDER BY nom_produit";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }

    public Produit getById(int id) throws SQLException {
        String query = "SELECT * FROM produit WHERE id_produit = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }
        }
        return null;
    }

    public void modifier(Produit produit) throws SQLException {
        String query = "UPDATE produit SET nom_produit=?, type_produit=?, montant=?, code_unique=?, statut=? WHERE id_produit=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeProduit());
            pstmt.setBigDecimal(3, produit.getMontant());
            pstmt.setString(4, produit.getCodeUnique());
            pstmt.setString(5, produit.getStatut());
            pstmt.setInt(6, produit.getIdProduit());
            pstmt.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM produit WHERE id_produit = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setIdProduit(rs.getInt("id_produit"));
        produit.setNomProduit(rs.getString("nom_produit"));
        produit.setTypeProduit(rs.getString("type_produit"));
        produit.setMontant(rs.getBigDecimal("montant"));
        produit.setCodeUnique(rs.getString("code_unique"));
        produit.setStatut(rs.getString("statut"));

        Date dateCreation = rs.getDate("date_creation");
        if (dateCreation != null) {
            produit.setDateCreation(dateCreation.toLocalDate());
        }
        return produit;
    }
}