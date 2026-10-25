package com.bibliotheque.manager;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

/**
 * Gestionnaire de base de données SQLite - Pattern Singleton
 * Gère la connexion unique à la base de données et l'initialisation du schéma
 * Crée automatiquement les tables (documents, adhérents, emprunts) et les index
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    private String dbUrl;
    
    /**
     * Constructeur privé pour Singleton
     * Établit la connexion et initialise le schéma de la base de données
     * @param dbPath Chemin vers le fichier SQLite (ex: "bibliotheque.db")
     */
    private DatabaseManager(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        try {
            // Charger le driver SQLite
            Class.forName("org.sqlite.JDBC");
            // Créer la connexion
            this.connection = DriverManager.getConnection(dbUrl);
            System.out.println("Connexion à la base de données établie.");
            // Initialiser les tables
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trouvé : " + e.getMessage());
            throw new RuntimeException("Erreur lors du chargement du driver SQLite", e);
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }
    
    /**
     * Obtient l'instance unique du DatabaseManager (première initialisation)
     * @param dbPath Chemin vers la base de données
     * @return L'instance unique du DatabaseManager
     */
    public static DatabaseManager getInstance(String dbPath) {
        if (instance == null) {
            instance = new DatabaseManager(dbPath);
        }
        return instance;
    }
    
    /**
     * Obtient l'instance existante (doit avoir été initialisée avant)
     * @return L'instance unique
     * @throws IllegalStateException si getInstance(String) n'a pas été appelé
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager non initialisé. Appelez getInstance(String) d'abord.");
        }
        return instance;
    }
    
    /**
     * Retourne la connexion active à la base de données
     * Reconnecte automatiquement si la connexion est fermée
     * @return La connexion JDBC
     */
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est toujours valide
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl);
                System.out.println("Reconnexion à la base de données.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la connexion", e);
        }
        return connection;
    }
    
    /**
     * Initialise le schéma de la base de données
     * Crée les tables documents, adherents, emprunts avec contraintes
     * Crée les index pour optimiser les recherches fréquentes
     */
    private void initDatabase() {
        try (Statement stmt = connection.createStatement()) {
            
            // Table des documents (stocke Livre, Magazine, CD)
            // Utilise le pattern "Single Table Inheritance" avec colonne 'type'
            String createDocumentsTable = """
                CREATE TABLE IF NOT EXISTS documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titre TEXT NOT NULL,
                    auteur TEXT NOT NULL,
                    genre TEXT,
                    disponible BOOLEAN DEFAULT 1,
                    type TEXT NOT NULL,
                    isbn TEXT,
                    nombre_pages INTEGER,
                    numero INTEGER,
                    periodicite TEXT,
                    duree INTEGER,
                    artiste TEXT
                )
            """;
            stmt.execute(createDocumentsTable);
            
            // Table des adhérents avec gestion des pénalités
            String createAdherentsTable = """
                CREATE TABLE IF NOT EXISTS adherents (
                    id_adherent INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL,
                    prenom TEXT NOT NULL,
                    email TEXT UNIQUE,
                    telephone TEXT,
                    statut_penalite BOOLEAN DEFAULT 0,
                    montant_penalite REAL DEFAULT 0.0
                )
            """;
            stmt.execute(createAdherentsTable);
            
            // Table des emprunts avec clés étrangères et cascade
            String createEmpruntsTable = """
                CREATE TABLE IF NOT EXISTS emprunts (
                    id_emprunt INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_document INTEGER NOT NULL,
                    id_adherent INTEGER NOT NULL,
                    date_emprunt TEXT NOT NULL,
                    date_retour_prevue TEXT NOT NULL,
                    date_retour_reelle TEXT,
                    penalite REAL DEFAULT 0.0,
                    en_cours BOOLEAN DEFAULT 1,
                    FOREIGN KEY (id_document) REFERENCES documents(id) ON DELETE CASCADE,
                    FOREIGN KEY (id_adherent) REFERENCES adherents(id_adherent) ON DELETE CASCADE
                )
            """;
            stmt.execute(createEmpruntsTable);
            
            // Index pour améliorer les performances des recherches
            String createIndexDocumentsTitre = 
                "CREATE INDEX IF NOT EXISTS idx_documents_titre ON documents(titre)";
            stmt.execute(createIndexDocumentsTitre);
            
            String createIndexDocumentsAuteur = 
                "CREATE INDEX IF NOT EXISTS idx_documents_auteur ON documents(auteur)";
            stmt.execute(createIndexDocumentsAuteur);
            
            String createIndexAdherentsNom = 
                "CREATE INDEX IF NOT EXISTS idx_adherents_nom ON adherents(nom)";
            stmt.execute(createIndexAdherentsNom);
            
            String createIndexEmpruntsEnCours = 
                "CREATE INDEX IF NOT EXISTS idx_emprunts_en_cours ON emprunts(en_cours)";
            stmt.execute(createIndexEmpruntsEnCours);
            
            String createIndexEmpruntsAdherent = 
                "CREATE INDEX IF NOT EXISTS idx_emprunts_adherent ON emprunts(id_adherent)";
            stmt.execute(createIndexEmpruntsAdherent);
            
            System.out.println("Tables de la base de données initialisées avec succès.");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'initialisation de la base de données", e);
        }
    }
    
    /**
     * Ferme proprement la connexion à la base de données
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
    
    /**
     * Réinitialise complètement la base de données
     * ATTENTION : Supprime TOUTES les données (documents, adhérents, emprunts)
     * Puis recrée les tables vides
     */
    public void resetDatabase() {
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            stmt.execute("DROP TABLE IF EXISTS emprunts");
            stmt.execute("DROP TABLE IF EXISTS documents");
            stmt.execute("DROP TABLE IF EXISTS adherents");
            System.out.println("Base de données réinitialisée.");
            initDatabase();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réinitialisation de la base de données : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la réinitialisation", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Vérifie si la connexion est actuellement active
     * @return true si connecté, false sinon
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Active les contraintes de clés étrangères
     * Important : SQLite désactive les FK par défaut pour la rétrocompatibilité
     * Doit être appelé après chaque connexion
     */
    public void enableForeignKeys() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            System.out.println("Contraintes de clés étrangères activées.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'activation des clés étrangères : " + e.getMessage());
        }
    }
    
    /**
     * Affiche les statistiques de la base de données
     * Compte le nombre de documents, adhérents, emprunts (total et en cours)
     * @param showDialog Si true, affiche dans une JOptionPane, sinon dans la console
     * @param parent Composant parent pour le dialogue (peut être null)
     */
    public void printDatabaseInfo(boolean showDialog, Component parent) {
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            
            // Compter les documents
            var rsDoc = stmt.executeQuery("SELECT COUNT(*) as count FROM documents");
            int nbDocuments = rsDoc.next() ? rsDoc.getInt("count") : 0;
            
            // Compter les adhérents
            var rsAdh = stmt.executeQuery("SELECT COUNT(*) as count FROM adherents");
            int nbAdherents = rsAdh.next() ? rsAdh.getInt("count") : 0;
            
            // Compter les emprunts
            var rsEmp = stmt.executeQuery("SELECT COUNT(*) as count FROM emprunts");
            int nbEmprunts = rsEmp.next() ? rsEmp.getInt("count") : 0;
            
            // Compter les emprunts en cours
            var rsEnCours = stmt.executeQuery("SELECT COUNT(*) as count FROM emprunts WHERE en_cours = 1");
            int nbEmpruntsEnCours = rsEnCours.next() ? rsEnCours.getInt("count") : 0;
            
            // Construire le message
            StringBuilder info = new StringBuilder();
            info.append("=== Informations Base de Données ===\n\n");
            info.append("Fichier : ").append(dbUrl).append("\n\n");
            info.append("Documents : ").append(nbDocuments).append("\n");
            info.append("Adhérents : ").append(nbAdherents).append("\n");
            info.append("Emprunts (total) : ").append(nbEmprunts).append("\n");
            info.append("Emprunts en cours : ").append(nbEmpruntsEnCours).append("\n");
            
            String message = info.toString();
            
            // Afficher selon le mode choisi
            if (showDialog) {
                JOptionPane.showMessageDialog(
                    parent,
                    message,
                    "Informations Base de Données",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                System.out.println(message);
            }
            
        } catch (SQLException e) {
            String errorMsg = "Erreur lors de la récupération des informations : " + e.getMessage();
            if (showDialog) {
                JOptionPane.showMessageDialog(
                    parent,
                    errorMsg,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                System.err.println(errorMsg);
            }
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Affiche les informations dans la console (version simplifiée)
     */
    public void printDatabaseInfo() {
        printDatabaseInfo(false, null);
    }
}