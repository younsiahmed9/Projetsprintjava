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

    // CREATE
    public void ajouter(Produit produit) throws SQLException {
        String query = "INSERT INTO produit (id_produit, nom_produit, type_produit, montant, code_unique, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, produit.getIdProduit());
            pstmt.setString(2, produit.getNomProduit());
            pstmt.setString(3, produit.getTypeProduit().name());
            pstmt.setBigDecimal(4, produit.getMontant());
            pstmt.setString(5, produit.getCodeUnique());
            pstmt.setString(6, produit.getStatut().name());
            pstmt.setDate(7, Date.valueOf(produit.getDateCreation()));

            pstmt.executeUpdate();
            System.out.println("Produit ajouté: " + produit.getNomProduit());
        }
    }

    // READ - Tous
    public List<Produit> recuperer() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit ORDER BY id_produit DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setIdProduit(rs.getInt("id_produit"));
                produit.setNomProduit(rs.getString("nom_produit"));
                produit.setTypeProduit(Produit.TypeProduit.valueOf(rs.getString("type_produit")));
                produit.setMontant(rs.getBigDecimal("montant"));
                produit.setCodeUnique(rs.getString("code_unique"));
                produit.setStatut(Produit.StatutProduit.valueOf(rs.getString("statut")));

                Date dateCreation = rs.getDate("date_creation");
                if (dateCreation != null) {
                    produit.setDateCreation(dateCreation.toLocalDate());
                }

                produits.add(produit);
            }
        }

        return produits;
    }

    // READ - Par ID
    public Produit getById(int id) throws SQLException {
        String query = "SELECT * FROM produit WHERE id_produit = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Produit produit = new Produit();
                produit.setIdProduit(rs.getInt("id_produit"));
                produit.setNomProduit(rs.getString("nom_produit"));
                produit.setTypeProduit(Produit.TypeProduit.valueOf(rs.getString("type_produit")));
                produit.setMontant(rs.getBigDecimal("montant"));
                produit.setCodeUnique(rs.getString("code_unique"));
                produit.setStatut(Produit.StatutProduit.valueOf(rs.getString("statut")));

                Date dateCreation = rs.getDate("date_creation");
                if (dateCreation != null) {
                    produit.setDateCreation(dateCreation.toLocalDate());
                }

                return produit;
            }
        }
        return null;
    }

    // READ - Produits disponibles
    public List<Produit> getProduitsDisponibles() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE statut = 'disponible' ORDER BY nom_produit";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setIdProduit(rs.getInt("id_produit"));
                produit.setNomProduit(rs.getString("nom_produit"));
                produit.setTypeProduit(Produit.TypeProduit.valueOf(rs.getString("type_produit")));
                produit.setMontant(rs.getBigDecimal("montant"));
                produit.setCodeUnique(rs.getString("code_unique"));
                produit.setStatut(Produit.StatutProduit.valueOf(rs.getString("statut")));

                Date dateCreation = rs.getDate("date_creation");
                if (dateCreation != null) {
                    produit.setDateCreation(dateCreation.toLocalDate());
                }

                produits.add(produit);
            }
        }

        return produits;
    }

    // UPDATE
    public void modifier(Produit produit) throws SQLException {
        String query = "UPDATE produit SET nom_produit=?, type_produit=?, montant=?, code_unique=?, statut=? WHERE id_produit=?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeProduit().name());
            pstmt.setBigDecimal(3, produit.getMontant());
            pstmt.setString(4, produit.getCodeUnique());
            pstmt.setString(5, produit.getStatut().name());
            pstmt.setInt(6, produit.getIdProduit());

            pstmt.executeUpdate();
        }
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM produit WHERE id_produit = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}