package com.bibliotheque.ui;

import com.bibliotheque.manager.BibliothequeManager;
import com.bibliotheque.model.*;
import com.bibliotheque.exception.EmpruntException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Vue Swing pour la gestion des emprunts
 * Permet de créer des emprunts, retourner des documents et consulter l'historique
 */
public class EmpruntView {
    
    private BibliothequeManager manager;
    private JPanel mainPanel;
    private JTable tableEmprunts;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> comboFiltre;

    // Références vers les autres vues pour rafraîchissement automatique
    private DocumentView documentView;
    private AdherentView adherentView;
    
    public EmpruntView(BibliothequeManager manager) {
        this.manager = manager;
        initView();
        loadEmprunts();
    }
    
    /**
     * Définit la vue Document pour synchronisation
     */
    public void setDocumentView(DocumentView documentView) {
        this.documentView = documentView;
    }
    
    /**
     * Définit la vue Adhérent pour synchronisation
     */
    public void setAdherentView(AdherentView adherentView) {
        this.adherentView = adherentView;
    }

    /**
     * Initialise la structure de la vue
     */
    private void initView() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createFilterBar(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createActionButtons(), BorderLayout.SOUTH);
    }
    
    /**
     * Crée la barre de filtres (tous, en cours, en retard, terminés)
     */
    private JPanel createFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Afficher :"));
        
        comboFiltre = new JComboBox<>(new String[]{"Tous les emprunts", "En cours", "En retard", "Terminés"});
        comboFiltre.setSelectedItem("Tous les emprunts");
        comboFiltre.addActionListener(e -> filtrerEmprunts());
        panel.add(comboFiltre);
        
        JButton btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> filtrerEmprunts());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    /**
     * Crée la table avec renderers pour le statut et les pénalités
     */
    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "Document", "Adhérent", "Date emprunt", "Retour prévu", 
                                "Retour effectué", "Statut", "Pénalité"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableEmprunts = new JTable(tableModel);
        tableEmprunts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEmprunts.setRowHeight(25);
        
        // Renderer pour le Statut (EN RETARD rouge, En cours bleu, Retourné vert)
        tableEmprunts.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String statut = (String) value;
                if (statut != null) {
                    if (statut.contains("RETARD")) {
                        setForeground(Color.RED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (statut.contains("En cours")) {
                        setForeground(Color.BLUE);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    } else {
                        setForeground(new Color(0, 150, 0));
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });
        
        // Renderer pour la Pénalité (rouge si > 0, sinon tiret)
        tableEmprunts.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    double montant = (Double) value;
                    if (montant == 0) {
                        setText("-");
                        setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    } else {
                        setText(String.format("%.2f €", montant));
                        setForeground(Color.RED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
                return c;
            }
        });
        
        return new JScrollPane(tableEmprunts);
    }
    
    /**
     * Crée les boutons d'action
     */
    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnEmprunter = new JButton("Nouveau emprunt");
        btnEmprunter.addActionListener(e -> nouvelEmprunt());
        panel.add(btnEmprunter);
        
        JButton btnRetourner = new JButton("Retourner document");
        btnRetourner.addActionListener(e -> retournerDocument());
        panel.add(btnRetourner);
        
        JButton btnDetails = new JButton("Details");
        btnDetails.addActionListener(e -> afficherDetails());
        panel.add(btnDetails);
        
        return panel;
    }
    
    /**
     * Ajuste automatiquement la largeur des colonnes
     */
    private void autoResizeColumns() {
        for (int column = 0; column < tableEmprunts.getColumnCount(); column++) {
            int width = 50;
            for (int row = 0; row < tableEmprunts.getRowCount(); row++) {
                javax.swing.table.TableCellRenderer renderer = 
                    tableEmprunts.getCellRenderer(row, column);
                Component comp = tableEmprunts.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            tableEmprunts.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }

    /**
     * Charge tous les emprunts
     */
    private void loadEmprunts() {
        tableModel.setRowCount(0);
        List<Emprunt> emprunts = manager.getTousLesEmprunts();
        addEmpruntsToTable(emprunts);
    }
    
    /**
     * Filtre les emprunts selon le critère sélectionné
     */
    private void filtrerEmprunts() {
        tableModel.setRowCount(0);
        String filtre = (String) comboFiltre.getSelectedItem();
        List<Emprunt> emprunts;
        
        if ("En cours".equals(filtre)) {
            emprunts = manager.getEmpruntsEnCours();
        } else if ("En retard".equals(filtre)) {
            emprunts = manager.getEmpruntsEnRetard();
        } else if ("Terminés".equals(filtre)) {
            emprunts = manager.getTousLesEmprunts().stream()
                .filter(e -> !e.isEnCours())
                .toList();
        } else {
            emprunts = manager.getTousLesEmprunts();
        }
        
        addEmpruntsToTable(emprunts);
    }
    
    /**
     * Ajoute des emprunts à la table avec calcul du statut
     */
    private void addEmpruntsToTable(List<Emprunt> emprunts) {
        for (Emprunt e : emprunts) {
            String statut;
            if (e.isEnCours()) {
                if (e.getDateRetourPrevue().isBefore(LocalDate.now())) {
                    statut = "EN RETARD";
                } else {
                    statut = "En cours";
                }
            } else {
                statut = "Retourné";
            }
            
            String adherentNom = e.getAdherent().getPrenom() + " " + e.getAdherent().getNom();
            String retourReel = e.getDateRetourReelle() != null ? 
                               e.getDateRetourReelle().toString() : "-";
            
            tableModel.addRow(new Object[]{
                e.getIdEmprunt(),
                e.getDocument().getTitre(),
                adherentNom,
                e.getDateEmprunt(),
                e.getDateRetourPrevue(),
                retourReel,
                statut,
                e.getPenalite()
            });
        }

        autoResizeColumns();
    }
    
    /**
     * Ouvre un dialogue pour créer un nouvel emprunt
     * Vérifie que l'adhérent n'a pas d'emprunts en retard avant d'autoriser
     */
    private void nouvelEmprunt() {
        JDialog dialog = new JDialog((Frame) null, "Nouvel emprunt", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Liste des documents disponibles
        List<Document> documentsDisponibles = manager.listerDocumentsDisponibles();
        JComboBox<Document> comboDocuments = new JComboBox<>();
        for (Document doc : documentsDisponibles) {
            comboDocuments.addItem(doc);
        }
        comboDocuments.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Document) {
                    Document doc = (Document) value;
                    setText(doc.getTitre() + " (" + doc.getAuteur() + ")");
                }
                return this;
            }
        });
        
        // Liste des adhérents
        List<Adherent> adherents = manager.listerTousLesAdherents();
        JComboBox<Adherent> comboAdherents = new JComboBox<>();
        for (Adherent adh : adherents) {
            comboAdherents.addItem(adh);
        }
        comboAdherents.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Adherent) {
                    Adherent adh = (Adherent) value;
                    setText(adh.getPrenom() + " " + adh.getNom() + " (ID: " + adh.getIdAdherent() + ")");
                }
                return this;
            }
        });
        
        formPanel.add(new JLabel("Document :"));
        formPanel.add(comboDocuments);
        formPanel.add(new JLabel("Adhérent :"));
        formPanel.add(comboAdherents);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnValider = new JButton("Emprunter");
        JButton btnAnnuler = new JButton("Annuler");
        dialog.getRootPane().setDefaultButton(btnValider);
        
        btnValider.addActionListener(e -> {
            Document doc = (Document) comboDocuments.getSelectedItem();
            Adherent adh = (Adherent) comboAdherents.getSelectedItem();
            
            if (doc != null && adh != null) {
                try {
                    // VÉRIFIER SI L'ADHÉRENT A DES RETARDS
                    List<Emprunt> empruntsEnRetard = manager.getHistoriqueAdherent(adh.getIdAdherent())
                        .stream()
                        .filter(emp -> emp.isEnCours() && emp.getDateRetourPrevue().isBefore(LocalDate.now()))
                        .toList();
                    
                    if (!empruntsEnRetard.isEmpty()) {
                        // Construire le message avec la liste des documents en retard
                        StringBuilder message = new StringBuilder();
                        message.append("Impossible d'emprunter !\n\n");
                        message.append(adh.getPrenom()).append(" ").append(adh.getNom());
                        message.append(" a ").append(empruntsEnRetard.size());
                        message.append(" document(s) en retard :\n\n");
                        
                        for (Emprunt empRetard : empruntsEnRetard) {
                            long joursRetard = java.time.temporal.ChronoUnit.DAYS.between(
                                empRetard.getDateRetourPrevue(), LocalDate.now());
                            message.append("• ").append(empRetard.getDocument().getTitre())
                                .append(" (").append(joursRetard).append(" jour(s) de retard)\n");
                        }
                        
                        message.append("\nVeuillez d'abord retourner ces documents.");
                        
                        MainApplication.showError("Emprunt refusé", message.toString());
                        return;
                    }
                    
                    // Si pas de retard, procéder à l'emprunt
                    Emprunt emprunt = manager.emprunterDocument(doc.getId(), adh.getIdAdherent());
                    MainApplication.showSuccess("Succès", 
                        "Emprunt enregistré !\nRetour prévu le : " + emprunt.getDateRetourPrevue());
                    
                    filtrerEmprunts();
                    
                    // Rafraîchir les autres vues
                    if (documentView != null) {
                        documentView.refresh();
                    }
                    if (adherentView != null) {
                        adherentView.refresh();
                    }
                    
                    dialog.dispose();
                } catch (EmpruntException ex) {
                    MainApplication.showError("Erreur", ex.getMessage());
                }
            } else {
                MainApplication.showError("Erreur", "Veuillez sélectionner un document et un adhérent.");
            }
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnValider);
        buttonPanel.add(btnAnnuler);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    /**
     * Retourne un document emprunté
     * Calcule et affiche automatiquement les pénalités si retard
     */
    private void retournerDocument() {
        int selectedRow = tableEmprunts.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un emprunt.");
            return;
        }
        
        int idEmprunt = (int) tableModel.getValueAt(selectedRow, 0);
        String statut = (String) tableModel.getValueAt(selectedRow, 6);
        
        if (statut.contains("Retourné")) {
            MainApplication.showError("Erreur", "Cet emprunt est déjà retourné.");
            return;
        }
        
        String document = (String) tableModel.getValueAt(selectedRow, 1);
        String adherent = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(
            mainPanel,
            "Document : " + document + "\nAdhérent : " + adherent + "\n\nConfirmer le retour ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.retournerDocument(idEmprunt);
                
                // Recharger l'emprunt pour voir la pénalité
                Emprunt empruntMaj = manager.getTousLesEmprunts().stream()
                    .filter(e -> e.getIdEmprunt() == idEmprunt)
                    .findFirst().orElse(null);
                
                String message = "Document retourné avec succès !";
                if (empruntMaj != null && empruntMaj.getPenalite() > 0) {
                    message += "\nPénalité appliquée : " + 
                              String.format("%.2f €", empruntMaj.getPenalite());
                }
                
                MainApplication.showSuccess("Succès", message);
                filtrerEmprunts();

                // Rafraîchir les autres vues
                if (documentView != null) {
                    documentView.refresh();
                }
                if (adherentView != null) {
                    adherentView.refresh();
                }
                
            } catch (EmpruntException ex) {
                MainApplication.showError("Erreur", ex.getMessage());
            }
        }
    }
    
    /**
     * Affiche les détails complets d'un emprunt sélectionné
     */
    private void afficherDetails() {
        int selectedRow = tableEmprunts.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un emprunt.");
            return;
        }
        
        int idEmprunt = (int) tableModel.getValueAt(selectedRow, 0);
        
        // Récupérer l'emprunt complet
        Emprunt emprunt = manager.getTousLesEmprunts().stream()
            .filter(e -> e.getIdEmprunt() == idEmprunt)
            .findFirst().orElse(null);
        
        if (emprunt == null) {
            MainApplication.showError("Erreur", "Emprunt introuvable.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("=== EMPRUNT #").append(emprunt.getIdEmprunt()).append(" ===\n\n");
        
        details.append("DOCUMENT:\n");
        details.append("  Titre: ").append(emprunt.getDocument().getTitre()).append("\n");
        details.append("  Auteur: ").append(emprunt.getDocument().getAuteur()).append("\n");
        details.append("  Type: ").append(emprunt.getDocument().getClass().getSimpleName()).append("\n\n");
        
        details.append("ADHÉRENT:\n");
        Adherent adh = emprunt.getAdherent();
        details.append("  Nom: ").append(adh.getPrenom()).append(" ").append(adh.getNom()).append("\n");
        details.append("  ID: ").append(adh.getIdAdherent()).append("\n\n");
        
        details.append("DATES:\n");
        details.append("  Emprunt: ").append(emprunt.getDateEmprunt()).append("\n");
        details.append("  Retour prévu: ").append(emprunt.getDateRetourPrevue()).append("\n");
        if (emprunt.getDateRetourReelle() != null) {
            details.append("  Retour effectué: ").append(emprunt.getDateRetourReelle()).append("\n");
        }
        
        details.append("\nSTATUT: ");
        if (emprunt.isEnCours()) {
            if (emprunt.getDateRetourPrevue().isBefore(LocalDate.now())) {
                long joursRetard = java.time.temporal.ChronoUnit.DAYS.between(
                    emprunt.getDateRetourPrevue(), LocalDate.now());
                details.append("EN RETARD (").append(joursRetard).append(" jour(s))");
            } else {
                details.append("En cours");
            }
        } else {
            details.append("✓ Retourné");
        }
        
        if (emprunt.getPenalite() > 0) {
            details.append("\n\nPÉNALITÉ: ").append(String.format("%.2f €", emprunt.getPenalite()));
        }
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(40);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(mainPanel, scrollPane, "Détails de l'emprunt", 
                                      JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Rafraîchit la vue avec le filtre actuel
     */
    public void refresh() {
        filtrerEmprunts();
    }
    
    /**
     * Retourne le panneau principal
     */
    public JPanel getPanel() {
        return mainPanel;
    }
}