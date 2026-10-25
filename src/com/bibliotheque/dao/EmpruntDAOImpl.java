package com.bibliotheque.dao;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Document;
import com.bibliotheque.model.Adherent;
import com.bibliotheque.manager.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation SQLite de l'interface EmpruntDAO
 * Particularité : utilise DocumentDAO et AdherentDAO pour charger les objets liés
 * Pattern : Collecte des données d'abord, puis chargement des objets (évite problèmes ResultSet)
 */
public class EmpruntDAOImpl implements EmpruntDAO {
    
    private DatabaseManager dbManager;
    private DocumentDAO documentDAO;
    private AdherentDAO adherentDAO;
    
    /**
     * Constructeur avec injection des DAO nécessaires
     * EmpruntDAO dépend de DocumentDAO et AdherentDAO pour reconstituer les emprunts
     */
    public EmpruntDAOImpl(DatabaseManager dbManager, DocumentDAO documentDAO, AdherentDAO adherentDAO) {
        this.dbManager = dbManager;
        this.documentDAO = documentDAO;
        this.adherentDAO = adherentDAO;
    }
    
    /**
     * Insère un nouvel emprunt en base
     * Stocke les dates au format ISO (yyyy-MM-dd)
     */
    @Override
    public int create(Emprunt emprunt) {
        String sql = "INSERT INTO emprunts (id_document, id_adherent, date_emprunt, " +
                     "date_retour_prevue, date_retour_reelle, penalite, en_cours) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, emprunt.getDocument().getId());
            stmt.setInt(2, emprunt.getAdherent().getIdAdherent());
            stmt.setString(3, emprunt.getDateEmprunt().toString());
            stmt.setString(4, emprunt.getDateRetourPrevue().toString());
            
            // Date de retour réelle peut être NULL si emprunt en cours
            if (emprunt.getDateRetourReelle() != null) {
                stmt.setString(5, emprunt.getDateRetourReelle().toString());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            
            stmt.setDouble(6, emprunt.getPenalite());
            stmt.setBoolean(7, emprunt.isEnCours());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de l'emprunt a échoué");
            }
            
            // Récupérer l'ID auto-généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next() ) {
                    int id = generatedKeys.getInt(1);
                    emprunt.setIdEmprunt(id);
                    return id;
                } else {
                    throw new SQLException("Aucun ID obtenu");
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'emprunt", e);
        }
    }
    
    /**
     * Recherche un emprunt par ID et charge le Document et l'Adherent associés
     */
    @Override
    public Emprunt findById(int id) {
        String sql = "SELECT * FROM emprunts WHERE id_emprunt = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Extraire toutes les données d'abord (évite problème ResultSet fermé)
                    int idDocument = rs.getInt("id_document");
                    int idAdherent = rs.getInt("id_adherent");
                    String dateEmprunt = rs.getString("date_emprunt");
                    String dateRetourPrevue = rs.getString("date_retour_prevue");
                    String dateRetourReelle = rs.getString("date_retour_reelle");
                    double penalite = rs.getDouble("penalite");
                    boolean enCours = rs.getBoolean("en_cours");
                    int idEmprunt = rs.getInt("id_emprunt");
                    
                    // Puis charger les objets liés via leurs DAO
                    Document document = documentDAO.findById(idDocument);
                    Adherent adherent = adherentDAO.findById(idAdherent);
                    
                    if (document == null || adherent == null) {
                        throw new SQLException("Document ou adhérent introuvable");
                    }
                    
                    // Construire l'emprunt avec le constructeur 4 paramètres
                    Emprunt emprunt = new Emprunt(
                        document, 
                        adherent, 
                        LocalDate.parse(dateEmprunt),
                        LocalDate.parse(dateRetourPrevue)
                    );
                    
                    emprunt.setIdEmprunt(idEmprunt);
                    
                    if (dateRetourReelle != null) {
                        emprunt.setDateRetourReelle(LocalDate.parse(dateRetourReelle));
                    }
                    
                    emprunt.setPenalite(penalite);
                    emprunt.setEnCours(enCours);
                    
                    return emprunt;
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'emprunt", e);
        }
        
        return null;
    }
    
    /**
     * Récupère tous les emprunts (historique complet)
     * Pattern : Collecte des données, puis chargement des objets
     * Évite les problèmes de ResultSet fermé lors du chargement des Documents/Adherents
     */
    @Override
    public List<Emprunt> findAll() {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT * FROM emprunts ORDER BY date_emprunt DESC";
        
        try (Connection conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            // ÉTAPE 1 : Collecter toutes les données brutes
            List<EmpruntData> dataList = new ArrayList<>();
            while (rs.next()) {
                EmpruntData data = new EmpruntData();
                data.idEmprunt = rs.getInt("id_emprunt");
                data.idDocument = rs.getInt("id_document");
                data.idAdherent = rs.getInt("id_adherent");
                data.dateEmprunt = rs.getString("date_emprunt");
                data.dateRetourPrevue = rs.getString("date_retour_prevue");
                data.dateRetourReelle = rs.getString("date_retour_reelle");
                data.penalite = rs.getDouble("penalite");
                data.enCours = rs.getBoolean("en_cours");
                dataList.add(data);
            }
            
            // ÉTAPE 2 : Charger les objets Document et Adherent pour chaque emprunt
            for (EmpruntData data : dataList) {
                Document document = documentDAO.findById(data.idDocument);
                Adherent adherent = adherentDAO.findById(data.idAdherent);
                
                if (document == null || adherent == null) {
                    System.err.println("Warning: Document ou adhérent introuvable pour l'emprunt " + data.idEmprunt);
                    continue;
                }
                
                Emprunt emprunt = new Emprunt(
                    document, 
                    adherent, 
                    LocalDate.parse(data.dateEmprunt),
                    LocalDate.parse(data.dateRetourPrevue)
                );
                
                emprunt.setIdEmprunt(data.idEmprunt);
                
                if (data.dateRetourReelle != null) {
                    emprunt.setDateRetourReelle(LocalDate.parse(data.dateRetourReelle));
                }
                
                emprunt.setPenalite(data.penalite);
                emprunt.setEnCours(data.enCours);
                
                emprunts.add(emprunt);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts", e);
        }
        
        return emprunts;
    }

    /**
     * Classe interne pour stocker temporairement les données d'un emprunt
     * Permet de fermer le ResultSet avant de charger les objets liés
     */
    private static class EmpruntData {
        int idEmprunt;
        int idDocument;
        int idAdherent;
        String dateEmprunt;
        String dateRetourPrevue;
        String dateRetourReelle;
        double penalite;
        boolean enCours;
    }
    
    /**
     * Met à jour un emprunt (utilisé principalement pour le retour)
     */
    @Override
    public void update(Emprunt emprunt) {
        String sql = "UPDATE emprunts SET id_document = ?, id_adherent = ?, date_emprunt = ?, " +
                     "date_retour_prevue = ?, date_retour_reelle = ?, penalite = ?, en_cours = ? " +
                     "WHERE id_emprunt = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, emprunt.getDocument().getId());
            stmt.setInt(2, emprunt.getAdherent().getIdAdherent());
            stmt.setString(3, emprunt.getDateEmprunt().toString());
            stmt.setString(4, emprunt.getDateRetourPrevue().toString());
            
            if (emprunt.getDateRetourReelle() != null) {
                stmt.setString(5, emprunt.getDateRetourReelle().toString());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            
            stmt.setDouble(6, emprunt.getPenalite());
            stmt.setBoolean(7, emprunt.isEnCours());
            stmt.setInt(8, emprunt.getIdEmprunt());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, emprunt non trouvé");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'emprunt", e);
        }
    }
    
    /**
     * Récupère l'historique des emprunts d'un adhérent spécifique
     */
    @Override
    public List<Emprunt> findByAdherent(int idAdherent) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT * FROM emprunts WHERE id_adherent = ? ORDER BY date_emprunt DESC";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idAdherent);
            
            try (ResultSet rs = stmt.executeQuery()) {
                // Pattern : collecte puis chargement
                List<EmpruntData> dataList = new ArrayList<>();
                while (rs.next()) {
                    EmpruntData data = new EmpruntData();
                    data.idEmprunt = rs.getInt("id_emprunt");
                    data.idDocument = rs.getInt("id_document");
                    data.idAdherent = rs.getInt("id_adherent");
                    data.dateEmprunt = rs.getString("date_emprunt");
                    data.dateRetourPrevue = rs.getString("date_retour_prevue");
                    data.dateRetourReelle = rs.getString("date_retour_reelle");
                    data.penalite = rs.getDouble("penalite");
                    data.enCours = rs.getBoolean("en_cours");
                    dataList.add(data);
                }
                
                for (EmpruntData data : dataList) {
                    Document document = documentDAO.findById(data.idDocument);
                    Adherent adherent = adherentDAO.findById(data.idAdherent);
                    
                    if (document != null && adherent != null) {
                        Emprunt emprunt = new Emprunt(
                            document, 
                            adherent, 
                            LocalDate.parse(data.dateEmprunt),
                            LocalDate.parse(data.dateRetourPrevue)
                        );
                        
                        emprunt.setIdEmprunt(data.idEmprunt);
                        
                        if (data.dateRetourReelle != null) {
                            emprunt.setDateRetourReelle(LocalDate.parse(data.dateRetourReelle));
                        }
                        
                        emprunt.setPenalite(data.penalite);
                        emprunt.setEnCours(data.enCours);
                        
                        emprunts.add(emprunt);
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des emprunts par adhérent", e);
        }
        
        return emprunts;
    }
    
    /**
     * Récupère tous les emprunts en cours (non retournés)
     * Triés par date de retour prévue
     */
    @Override
    public List<Emprunt> findEnCours() {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT * FROM emprunts WHERE en_cours = 1 ORDER BY date_retour_prevue";
        
        try (Connection conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            List<EmpruntData> dataList = new ArrayList<>();
            while (rs.next()) {
                EmpruntData data = new EmpruntData();
                data.idEmprunt = rs.getInt("id_emprunt");
                data.idDocument = rs.getInt("id_document");
                data.idAdherent = rs.getInt("id_adherent");
                data.dateEmprunt = rs.getString("date_emprunt");
                data.dateRetourPrevue = rs.getString("date_retour_prevue");
                data.dateRetourReelle = rs.getString("date_retour_reelle");
                data.penalite = rs.getDouble("penalite");
                data.enCours = rs.getBoolean("en_cours");
                dataList.add(data);
            }
            
            for (EmpruntData data : dataList) {
                Document document = documentDAO.findById(data.idDocument);
                Adherent adherent = adherentDAO.findById(data.idAdherent);
                
                if (document != null && adherent != null) {
                    Emprunt emprunt = new Emprunt(
                        document, 
                        adherent, 
                        LocalDate.parse(data.dateEmprunt),
                        LocalDate.parse(data.dateRetourPrevue)
                    );
                    
                    emprunt.setIdEmprunt(data.idEmprunt);
                    
                    if (data.dateRetourReelle != null) {
                        emprunt.setDateRetourReelle(LocalDate.parse(data.dateRetourReelle));
                    }
                    
                    emprunt.setPenalite(data.penalite);
                    emprunt.setEnCours(data.enCours);
                    
                    emprunts.add(emprunt);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts en cours", e);
        }
        
        return emprunts;
    }
    
    /**
     * Récupère tous les emprunts en retard
     * Critère : en_cours = true ET date_retour_prevue < aujourd'hui
     */
    @Override
    public List<Emprunt> findEnRetard() {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT * FROM emprunts WHERE en_cours = 1 AND date_retour_prevue < ? " +
                    "ORDER BY date_retour_prevue";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Comparer avec la date actuelle
            stmt.setString(1, LocalDate.now().toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<EmpruntData> dataList = new ArrayList<>();
                while (rs.next()) {
                    EmpruntData data = new EmpruntData();
                    data.idEmprunt = rs.getInt("id_emprunt");
                    data.idDocument = rs.getInt("id_document");
                    data.idAdherent = rs.getInt("id_adherent");
                    data.dateEmprunt = rs.getString("date_emprunt");
                    data.dateRetourPrevue = rs.getString("date_retour_prevue");
                    data.dateRetourReelle = rs.getString("date_retour_reelle");
                    data.penalite = rs.getDouble("penalite");
                    data.enCours = rs.getBoolean("en_cours");
                    dataList.add(data);
                }
                
                for (EmpruntData data : dataList) {
                    Document document = documentDAO.findById(data.idDocument);
                    Adherent adherent = adherentDAO.findById(data.idAdherent);
                    
                    if (document != null && adherent != null) {
                        Emprunt emprunt = new Emprunt(
                            document, 
                            adherent, 
                            LocalDate.parse(data.dateEmprunt),
                            LocalDate.parse(data.dateRetourPrevue)
                        );
                        
                        emprunt.setIdEmprunt(data.idEmprunt);
                        
                        if (data.dateRetourReelle != null) {
                            emprunt.setDateRetourReelle(LocalDate.parse(data.dateRetourReelle));
                        }
                        
                        emprunt.setPenalite(data.penalite);
                        emprunt.setEnCours(data.enCours);
                        
                        emprunts.add(emprunt);
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts en retard", e);
        }
        
        return emprunts;
    }
}