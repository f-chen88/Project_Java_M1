package com.bibliotheque.dao;

import com.bibliotheque.model.Adherent;
import com.bibliotheque.manager.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation SQLite de l'interface AdherentDAO
 * Gère la persistance des adhérents avec PreparedStatement (sécurité SQL injection)
 */
public class AdherentDAOImpl implements AdherentDAO {
    
    private DatabaseManager dbManager;
    
    public AdherentDAOImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Insère un nouvel adhérent en base
     * @return L'ID auto-généré par la base de données
     */
    @Override
    public int create(Adherent adherent) {
        String sql = "INSERT INTO adherents (nom, prenom, email, telephone, statut_penalite, montant_penalite) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, adherent.getNom());
            stmt.setString(2, adherent.getPrenom());
            stmt.setString(3, adherent.getEmail());
            stmt.setString(4, adherent.getTelephone());
            stmt.setBoolean(5, adherent.hasPenalite());
            stmt.setDouble(6, adherent.getMontantPenalite());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de l'adhérent a échoué");
            }
            
            // Récupérer l'ID généré automatiquement
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    adherent.setIdAdherent(id);
                    return id;
                } else {
                    throw new SQLException("Aucun ID obtenu");
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'adhérent", e);
        }
    }
    
    /**
     * Recherche un adhérent par son ID
     */
    @Override
    public Adherent findById(int id) {
        String sql = "SELECT * FROM adherents WHERE id_adherent = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdherent(rs);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'adhérent", e);
        }
        
        return null;
    }
    
    /**
     * Récupère tous les adhérents triés par nom et prénom
     */
    @Override
    public List<Adherent> findAll() {
        List<Adherent> adherents = new ArrayList<>();
        String sql = "SELECT * FROM adherents ORDER BY nom, prenom";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                adherents.add(mapResultSetToAdherent(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des adhérents", e);
        }
        
        return adherents;
    }
    
    /**
     * Met à jour toutes les informations d'un adhérent
     */
    @Override
    public void update(Adherent adherent) {
        String sql = "UPDATE adherents SET nom = ?, prenom = ?, email = ?, telephone = ?, " +
                     "statut_penalite = ?, montant_penalite = ? WHERE id_adherent = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, adherent.getNom());
            stmt.setString(2, adherent.getPrenom());
            stmt.setString(3, adherent.getEmail());
            stmt.setString(4, adherent.getTelephone());
            stmt.setBoolean(5, adherent.hasPenalite());
            stmt.setDouble(6, adherent.getMontantPenalite());
            stmt.setInt(7, adherent.getIdAdherent());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, adhérent non trouvé");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'adhérent", e);
        }
    }
    
    /**
     * Supprime un adhérent de la base
     * Note: Les emprunts associés seront supprimés en cascade (FK)
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM adherents WHERE id_adherent = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La suppression a échoué, adhérent non trouvé");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'adhérent", e);
        }
    }
    
    /**
     * Recherche partielle insensible à la casse sur nom ET prénom
     * Ex: "dupon" trouvera "Dupont" et "Jean Dupond"
     */
    @Override
    public List<Adherent> findByNom(String nom) {
        List<Adherent> adherents = new ArrayList<>();
        String sql = "SELECT * FROM adherents WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? " +
                     "ORDER BY nom, prenom";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + nom.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    adherents.add(mapResultSetToAdherent(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par nom", e);
        }
        
        return adherents;
    }
    
    /**
     * Vérifie l'existence d'un adhérent par comptage
     */
    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM adherents WHERE id_adherent = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'existence", e);
        }
        
        return false;
    }
    
    /**
     * Convertit une ligne de résultat SQL en objet Adherent
     * Centralise la logique de mapping pour éviter la duplication
     */
    private Adherent mapResultSetToAdherent(ResultSet rs) throws SQLException {
        Adherent adherent = new Adherent(
            rs.getInt("id_adherent"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getString("telephone")
        );
        
        adherent.setIdAdherent(rs.getInt("id_adherent"));
        adherent.setStatutPenalite(rs.getBoolean("statut_penalite"));
        adherent.setMontantPenalite(rs.getDouble("montant_penalite"));
        
        return adherent;
    }
}