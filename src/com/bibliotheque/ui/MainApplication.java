package com.bibliotheque.ui;
import com.bibliotheque.dao.*;
import com.bibliotheque.manager.BibliothequeManager;
import com.bibliotheque.manager.DatabaseManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;

/**
 * Application principale Swing avec interface à onglets
 * Point d'entrée du système de gestion de bibliothèque
 */
public class MainApplication extends JFrame {
    
    private BibliothequeManager manager;
    private DatabaseManager dbManager;
    
    private JTabbedPane tabbedPane;
    
    // Vues
    private DocumentView documentView;
    private AdherentView adherentView;
    private EmpruntView empruntView;
    
    public MainApplication() {
        initApplication();
        initUI();
    }
    
    /**
     * Initialise l'application : connexion BD, DAO, Manager
     */
    private void initApplication() {
        try {
            // Initialiser la base de données
            dbManager = DatabaseManager.getInstance("bibliotheque.db");
            dbManager.enableForeignKeys();
            
            // Initialiser les DAO
            DocumentDAO documentDAO = new DocumentDAOImpl(dbManager);
            AdherentDAO adherentDAO = new AdherentDAOImpl(dbManager);
            EmpruntDAO empruntDAO = new EmpruntDAOImpl(dbManager, documentDAO, adherentDAO);
            
            // Initialiser le manager
            manager = new BibliothequeManager(documentDAO, adherentDAO, empruntDAO);
            
            // Afficher les infos de la base
            dbManager.printDatabaseInfo();
            
        } catch (Exception e) {
            showError("Erreur d'initialisation", 
                     "Impossible d'initialiser l'application : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Initialise l'interface utilisateur Swing
     */
    private void initUI() {
        // Configuration de la fenêtre principale
        setTitle("Système de Gestion des Emprunts de Bibliothèque");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Créer la barre de menu
        setJMenuBar(createMenuBar());
        
        // Créer le panneau à onglets
        tabbedPane = new JTabbedPane();
        
        // Créer les vues
        documentView = new DocumentView(manager);
        adherentView = new AdherentView(manager);
        empruntView = new EmpruntView(manager);

        // Lier les vues pour synchronisation automatique
        empruntView.setDocumentView(documentView);
        empruntView.setAdherentView(adherentView);
        
        // Ajouter les onglets
        tabbedPane.addTab("Documents", documentView.getPanel());
        tabbedPane.addTab("Adhérents", adherentView.getPanel());
        tabbedPane.addTab("Emprunts", empruntView.getPanel());

        // Rafraîchir la vue quand on change d'onglet
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                switch (selectedIndex) {
                    case 0: // Onglet Documents
                        documentView.refresh();
                        break;
                    case 1: // Onglet Adhérents
                        adherentView.refresh();
                        break;
                    case 2: // Onglet Emprunts
                        empruntView.refresh();
                        break;
                }
            }
        });
        
        // Ajouter au centre
        add(tabbedPane, BorderLayout.CENTER);
        
        // Barre de statut
        JLabel statusBar = new JLabel(" Système de Gestion des Emprunts - Prêt");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
        
        // Hook de fermeture
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dbManager.closeConnection();
                System.out.println("Application fermée.");
            }
        });
    }
    
    /**
     * Crée la barre de menu (Fichier, Base de données, Aide)
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Fichier
        JMenu menuFichier = new JMenu("Fichier");
        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> {
            dbManager.closeConnection();
            System.exit(0);
        });
        menuFichier.add(itemQuitter);
        
        // Menu Base de données
        JMenu menuDB = new JMenu("Base de données");
        JMenuItem itemInfos = new JMenuItem("Informations");
        itemInfos.addActionListener(e -> dbManager.printDatabaseInfo(true, this));
        
        JMenuItem itemReset = new JMenuItem("Réinitialiser (DANGER)");
        itemReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "ATTENTION : Toutes les données seront supprimées. Continuer ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                dbManager.resetDatabase();
                refreshAllViews();
                showInfo("Base réinitialisée", "La base de données a été réinitialisée.");
            }
        });
        
        menuDB.add(itemInfos);
        menuDB.addSeparator();
        menuDB.add(itemReset);
        
        // Menu Aide
        JMenu menuAide = new JMenu("Aide");
        JMenuItem itemAPropos = new JMenuItem("À propos");
        itemAPropos.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Système de Gestion des Emprunts de Bibliothèque\n\n" +
                "Version 1.0\n" +
                "Projet Java M1 IDD 2025-2026",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        menuAide.add(itemAPropos);
        
        menuBar.add(menuFichier);
        menuBar.add(menuDB);
        menuBar.add(menuAide);
        
        return menuBar;
    }
    
    /**
     * Rafraîchit toutes les vues (après réinitialisation BD par exemple)
     */
    private void refreshAllViews() {
        documentView.refresh();
        adherentView.refresh();
        empruntView.refresh();
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    public static void showError(String title, String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Affiche une boîte de dialogue d'information
     */
    public static void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Affiche une boîte de dialogue de succès
     */
    public static void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            "✓ " + title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Point d'entrée de l'application
     */
    public static void main(String[] args) {
        // Encodage UTF-8 pour les caractères spéciaux
        System.setProperty("file.encoding", "UTF-8");
        
        // Utiliser le Look and Feel du système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Lancer l'application dans le thread Swing
        SwingUtilities.invokeLater(() -> {
            MainApplication app = new MainApplication();
            app.setVisible(true);
        });
    }
}