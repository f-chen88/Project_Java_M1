package com.bibliotheque.dao;

import com.bibliotheque.model.*;
import com.bibliotheque.manager.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation SQLite de l'interface DocumentDAO
 * Utilise le pattern "Single Table Inheritance" :
 * - Une seule table "documents" stocke Livre, Magazine et CD
 * - La colonne "type" différencie les types
 * - Les colonnes spécifiques (isbn, duree, etc.) sont NULL selon le type
 */
public class DocumentDAOImpl implements DocumentDAO {
    
    private DatabaseManager dbManager;
    
    public DocumentDAOImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Insère un document en base (Livre, Magazine ou CD)
     * Remplit uniquement les colonnes pertinentes selon le type
     */
    @Override
    public int create(Document document) {
        String sql = "INSERT INTO documents (titre, auteur, genre, disponible, type, " +
                     "isbn, nombre_pages, numero, periodicite, duree, artiste) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Attributs communs à tous les documents
            stmt.setString(1, document.getTitre());
            stmt.setString(2, document.getAuteur());
            stmt.setString(3, document.getGenre());
            stmt.setBoolean(4, document.isDisponible());
            
            // Attributs spécifiques selon le type (instanceof)
            if (document instanceof Livre) {
                Livre livre = (Livre) document;
                stmt.setString(5, "Livre");
                stmt.setString(6, livre.getIsbn());
                stmt.setInt(7, livre.getNbPages());
                stmt.setNull(8, Types.INTEGER);    // numero (Magazine)
                stmt.setNull(9, Types.VARCHAR);    // periodicite (Magazine)
                stmt.setNull(10, Types.INTEGER);   // duree (CD)
                stmt.setNull(11, Types.VARCHAR);   // artiste (CD)
            } else if (document instanceof Magazine) {
                Magazine magazine = (Magazine) document;
                stmt.setString(5, "Magazine");
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.INTEGER);
                stmt.setInt(8, magazine.getNumero());
                stmt.setString(9, magazine.getPeriodicite());
                stmt.setNull(10, Types.INTEGER);
                stmt.setNull(11, Types.VARCHAR);
            } else if (document instanceof CD) {
                CD cd = (CD) document;
                stmt.setString(5, "CD");
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.INTEGER);
                stmt.setNull(8, Types.INTEGER);
                stmt.setNull(9, Types.VARCHAR);
                stmt.setInt(10, cd.getDuree());
                stmt.setString(11, cd.getArtiste());
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création du document a échoué");
            }
            
            // Récupérer l'ID auto-généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    document.setId(id);
                    return id;
                } else {
                    throw new SQLException("Aucun ID obtenu");
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du document", e);
        }
    }
    
    /**
     * Recherche un document par ID et reconstruit le type approprié
     */
    @Override
    public Document findById(int id) {
        String sql = "SELECT * FROM documents WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du document", e);
        }
        
        return null;
    }
    
    /**
     * Récupère tous les documents triés par titre
     */
    @Override
    public List<Document> findAll() {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY titre";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des documents", e);
        }
        
        return documents;
    }
    
    /**
     * Met à jour un document (type non modifiable)
     */
    @Override
    public void update(Document document) {
        String sql = "UPDATE documents SET titre = ?, auteur = ?, genre = ?, disponible = ?, " +
                     "isbn = ?, nombre_pages = ?, numero = ?, periodicite = ?, duree = ?, artiste = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, document.getTitre());
            stmt.setString(2, document.getAuteur());
            stmt.setString(3, document.getGenre());
            stmt.setBoolean(4, document.isDisponible());
            
            // Remplir selon le type (même logique que create)
            if (document instanceof Livre) {
                Livre livre = (Livre) document;
                stmt.setString(5, livre.getIsbn());
                stmt.setInt(6, livre.getNbPages());
                stmt.setNull(7, Types.INTEGER);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
            } else if (document instanceof Magazine) {
                Magazine magazine = (Magazine) document;
                stmt.setNull(5, Types.VARCHAR);
                stmt.setNull(6, Types.INTEGER);
                stmt.setInt(7, magazine.getNumero());
                stmt.setString(8, magazine.getPeriodicite());
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
            } else if (document instanceof CD) {
                CD cd = (CD) document;
                stmt.setNull(5, Types.VARCHAR);
                stmt.setNull(6, Types.INTEGER);
                stmt.setNull(7, Types.INTEGER);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setInt(9, cd.getDuree());
                stmt.setString(10, cd.getArtiste());
            }
            
            stmt.setInt(11, document.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, document non trouvé");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du document", e);
        }
    }
    
    /**
     * Supprime un document de la base
     * Note: Les emprunts associés seront supprimés en cascade (FK)
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM documents WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La suppression a échoué, document non trouvé");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du document", e);
        }
    }
    
    /**
     * Recherche partielle insensible à la casse sur le titre
     */
    @Override
    public List<Document> findByTitre(String titre) {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE LOWER(titre) LIKE ? ORDER BY titre";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + titre.toLowerCase() + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapResultSetToDocument(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par titre", e);
        }
        
        return documents;
    }
    
    /**
     * Recherche partielle insensible à la casse sur l'auteur
     */
    @Override
    public List<Document> findByAuteur(String auteur) {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE LOWER(auteur) LIKE ? ORDER BY auteur, titre";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + auteur.toLowerCase() + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapResultSetToDocument(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par auteur", e);
        }
        
        return documents;
    }

    /**
     * Recherche partielle insensible à la casse sur le genre
     */
    @Override 
    public List<Document> findByGenre(String genre) {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE LOWER(genre) LIKE ? ORDER BY genre, titre";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + genre.toLowerCase() + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapResultSetToDocument(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par genre", e);
        }
        
        return documents;
    }
    
    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM documents WHERE id = ?";
        
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
     * Convertit une ligne SQL en objet Document du bon type
     * Lit la colonne "type" pour instancier Livre, Magazine ou CD
     * Centralise la logique de mapping pour éviter la duplication
     */
    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        Document document = null;
        
        if ("Livre".equals(type)) {
            document = new Livre(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getString("genre"),
                rs.getString("isbn"),
                rs.getInt("nombre_pages")
            );
        } else if ("Magazine".equals(type)) {
            document = new Magazine(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getString("genre"),
                rs.getString("periodicite"),
                rs.getInt("numero")
            );
        } else if ("CD".equals(type)) {
            document = new CD(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getString("genre"),
                rs.getInt("duree"),
                rs.getString("artiste")
            );
        }
        
        // Appliquer les attributs communs
        if (document != null) {
            document.setId(rs.getInt("id"));
            document.setDisponible(rs.getBoolean("disponible"));
        }
        
        return document;
    }
}