package com.bibliotheque.ui;
import com.bibliotheque.manager.BibliothequeManager;
import com.bibliotheque.model.Adherent;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.util.ValidationUtil;
import com.bibliotheque.exception.AdherentException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Vue Swing pour la gestion des adhérents
 * Permet l'inscription, la modification, la suppression et la consultation des adhérents
 */
public class AdherentView {
    
    private BibliothequeManager manager;
    private JPanel mainPanel;
    private JTable tableAdherents;
    private DefaultTableModel tableModel;
    
    private JTextField txtRecherche;
    
    public AdherentView(BibliothequeManager manager) {
        this.manager = manager;
        initView();
        loadAdherents();
    }
    
    /**
     * Initialise la structure de la vue (barre recherche, table, boutons)
     */
    private void initView() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createSearchBar(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createActionButtons(), BorderLayout.SOUTH);
    }
    
    /**
     * Crée la barre de recherche (par nom/prénom ou ID)
     */
    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    
        panel.add(new JLabel("Rechercher par :"));
        
        // ComboBox pour choisir le type de recherche
        JComboBox<String> comboTypeRecherche = new JComboBox<>(new String[]{"Nom/Prénom", "ID"});
        panel.add(comboTypeRecherche);
        
        txtRecherche = new JTextField(20);
        txtRecherche.setToolTipText("Entrez le nom, prénom ou ID");
        panel.add(txtRecherche);
        
        JButton btnRechercher = new JButton("Rechercher");
        btnRechercher.addActionListener(e -> rechercher(comboTypeRecherche.getSelectedItem().toString()));
        panel.add(btnRechercher);
        
        JButton btnReset = new JButton("Tout afficher");
        btnReset.addActionListener(e -> loadAdherents());
        panel.add(btnReset);
        
        // Permettre la recherche avec Enter
        txtRecherche.addActionListener(e -> rechercher(comboTypeRecherche.getSelectedItem().toString()));
        
        return panel;
    }
    
    /**
     * Crée la table d'affichage des adhérents avec renderers personnalisés
     */
    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "Nom", "Prénom", "Email", "Téléphone", "Pénalité", "Montant dû"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableAdherents = new JTable(tableModel);
        tableAdherents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAdherents.setRowHeight(25);
    
        // Renderer pour la colonne Pénalité (Oui/Non avec couleur)
        tableAdherents.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Boolean) {
                    setText((Boolean) value ? "Oui" : "Non");
                    setForeground((Boolean) value ? Color.RED : new Color(0, 150, 0));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });
        
        // Renderer pour le montant (formatage €, couleur rouge si > 0)
        tableAdherents.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText(String.format("%.2f €", value));
                    if ((Double) value > 0) {
                        setForeground(Color.RED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });
        
        return new JScrollPane(tableAdherents);
    }
    
    /**
     * Crée les boutons d'action (inscrire, modifier, supprimer, historique, payer)
     */
    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAjouter = new JButton("Inscrire un adhérent");
        btnAjouter.addActionListener(e -> inscrireAdherent());
        panel.add(btnAjouter);
        
        JButton btnModifier = new JButton("Modifier");
        btnModifier.addActionListener(e -> modifierAdherent());
        panel.add(btnModifier);

        JButton btnSupprimer = new JButton("Supprimer"); 
        btnSupprimer.addActionListener(e -> supprimerAdherent());
        panel.add(btnSupprimer);
        
        JButton btnHistorique = new JButton("Historique");
        btnHistorique.addActionListener(e -> afficherHistorique());
        panel.add(btnHistorique);
        
        JButton btnPayerPenalite = new JButton("Payer pénalité");
        btnPayerPenalite.addActionListener(e -> payerPenalite());
        panel.add(btnPayerPenalite);
        
        return panel;
    }

    /**
     * Ajuste automatiquement la largeur des colonnes selon le contenu
     */
    private void autoResizeColumns() {
        for (int column = 0; column < tableAdherents.getColumnCount(); column++) {
            int width = 50; // Largeur minimum
            for (int row = 0; row < tableAdherents.getRowCount(); row++) {
                javax.swing.table.TableCellRenderer renderer = 
                    tableAdherents.getCellRenderer(row, column);
                Component comp = tableAdherents.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            tableAdherents.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }
    
    /**
     * Charge tous les adhérents dans la table
     */
    private void loadAdherents() {
        tableModel.setRowCount(0);
        List<Adherent> adherents = manager.listerTousLesAdherents();
        
        for (Adherent adh : adherents) {
            tableModel.addRow(new Object[]{
                adh.getIdAdherent(),
                adh.getNom(),
                adh.getPrenom(),
                adh.getEmail(),
                adh.getTelephone(),
                adh.hasPenalite(),
                adh.getMontantPenalite()
            });
        }
        
        txtRecherche.setText("");
        autoResizeColumns();
    }
    
    /**
     * Recherche des adhérents par nom/prénom ou ID
     */
    private void rechercher(String typeRecherche) {
        String recherche = txtRecherche.getText().trim();
        if (recherche.isEmpty()) {
            loadAdherents();
            return;
        }
        
        tableModel.setRowCount(0);
        
        if ("ID".equals(typeRecherche)) {
            // Recherche par ID
            try {
                int id = Integer.parseInt(recherche);
                Adherent adherent = manager.rechercherAdherentParId(id);
                
                if (adherent != null) {
                    tableModel.addRow(new Object[]{
                        adherent.getIdAdherent(),
                        adherent.getNom(),
                        adherent.getPrenom(),
                        adherent.getEmail(),
                        adherent.getTelephone(),
                        adherent.hasPenalite(),
                        adherent.getMontantPenalite()
                    });
                } else {
                    MainApplication.showInfo("Aucun résultat", "Aucun adhérent trouvé avec l'ID " + id);
                }
            } catch (NumberFormatException ex) {
                MainApplication.showError("Erreur", "Veuillez entrer un ID valide (nombre entier).");
            } catch (Exception ex) {
                MainApplication.showInfo("Aucun résultat", "Aucun adhérent trouvé avec l'ID " + recherche);
            }
        } else {
            // Recherche par Nom/Prénom
            List<Adherent> resultats = manager.rechercherAdherentParNom(recherche);
            
            if (resultats.isEmpty()) {
                MainApplication.showInfo("Aucun résultat", "Aucun adhérent trouvé avec : " + recherche);
            } else {
                for (Adherent adh : resultats) {
                    tableModel.addRow(new Object[]{
                        adh.getIdAdherent(),
                        adh.getNom(),
                        adh.getPrenom(),
                        adh.getEmail(),
                        adh.getTelephone(),
                        adh.hasPenalite(),
                        adh.getMontantPenalite()
                    });
                }
            }
        }
        
        autoResizeColumns();
    }
    
    /**
     * Ouvre un dialogue pour inscrire un nouvel adhérent
     * Validation : nom, prénom obligatoires + au moins un contact (email ou téléphone)
     */
    private void inscrireAdherent() {
        JDialog dialog = new JDialog((Frame) null, "Inscrire un adhérent", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(350, 220);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField txtNom = new JTextField();
        JTextField txtPrenom = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtTelephone = new JTextField();

        formPanel.add(new JLabel("Nom:"));
        formPanel.add(txtNom);
        formPanel.add(new JLabel("Prénom:"));
        formPanel.add(txtPrenom);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Téléphone:"));
        formPanel.add(txtTelephone);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnValider = new JButton("Inscrire");
        JButton btnAnnuler = new JButton("Annuler");
        dialog.getRootPane().setDefaultButton(btnValider);
        
        btnValider.addActionListener(e -> {
            try {
                String nom = txtNom.getText().trim();
                String prenom = txtPrenom.getText().trim();
                String email = txtEmail.getText().trim();
                String telephone = txtTelephone.getText().trim();
                
                // Validations
                if (!ValidationUtil.estNonVide(nom)) {
                    MainApplication.showError("Erreur", "Le nom est obligatoire.");
                    return;
                }
                if (!ValidationUtil.estNonVide(prenom)) {
                    MainApplication.showError("Erreur", "Le prénom est obligatoire.");
                    return;
                }
                
                // Vérifier qu'au moins un contact est fourni
                if (!ValidationUtil.estNonVide(email) && !ValidationUtil.estNonVide(telephone)) {
                    MainApplication.showError("Erreur", 
                        "Vous devez renseigner au moins un moyen de contact :\n" +
                        "- Email\n" +
                        "- Téléphone");
                    return;
                }
                
                // Valider l'email s'il est renseigné
                if (ValidationUtil.estNonVide(email) && !ValidationUtil.estEmailValide(email)) {
                    MainApplication.showError("Erreur", 
                        "L'email n'est pas valide.\nFormat attendu : exemple@domaine.com");
                    return;
                }
                
                // Créer l'adhérent
                Adherent adherent = new Adherent(nom, prenom, email, telephone);
                
                manager.inscrireAdherent(adherent);
                MainApplication.showSuccess("Succès", "Adhérent inscrit avec succès !");
                loadAdherents();
                dialog.dispose();
                
            } catch (AdherentException ex) {
                // Vérifier si c'est une erreur de contrainte unique sur l'email
                if (ex.getMessage().contains("UNIQUE") || 
                    ex.getMessage().contains("email") ||
                    ex.getMessage().toLowerCase().contains("unique constraint")) {
                    MainApplication.showError("Erreur", "Cet email est déjà utilisé.");
                } else {
                    MainApplication.showError("Erreur", ex.getMessage());
                }
            } catch (Exception ex) {
                // Attraper toutes les autres exceptions (notamment SQLException)
                String message = ex.getMessage();
                if (message != null && (message.contains("UNIQUE") || 
                    message.contains("email") ||
                    message.toLowerCase().contains("unique constraint") ||
                    message.toLowerCase().contains("duplicate"))) {
                    MainApplication.showError("Erreur", "Cet email est déjà utilisé.");
                } else {
                    MainApplication.showError("Erreur", "Erreur lors de la création de l'adhérent : " + 
                        (message != null ? message : ex.getClass().getSimpleName()));
                }
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
     * Ouvre un dialogue pour modifier un adhérent sélectionné
     * Pré-remplit les champs avec les valeurs actuelles
     */
    private void modifierAdherent() {
        int selectedRow = tableAdherents.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un adhérent.");
            return;
        }
        
        try {
            // Récupérer l'adhérent sélectionné
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Adherent adherentActuel = manager.rechercherAdherentParId(id);
            
            // Créer le dialogue de modification
            JDialog dialog = new JDialog((Frame) null, "Modifier un adhérent", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(350, 250);
            dialog.setLocationRelativeTo(mainPanel);
            
            JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Pré-remplir les champs avec les valeurs actuelles
            JTextField txtNom = new JTextField(adherentActuel.getNom());
            JTextField txtPrenom = new JTextField(adherentActuel.getPrenom());
            JTextField txtEmail = new JTextField(adherentActuel.getEmail());
            JTextField txtTelephone = new JTextField(adherentActuel.getTelephone());
            
            formPanel.add(new JLabel("Nom:"));
            formPanel.add(txtNom);
            formPanel.add(new JLabel("Prénom:"));
            formPanel.add(txtPrenom);
            formPanel.add(new JLabel("Email:"));
            formPanel.add(txtEmail);
            formPanel.add(new JLabel("Téléphone:"));
            formPanel.add(txtTelephone);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnValider = new JButton("Modifier");
            JButton btnAnnuler = new JButton("Annuler");

            dialog.getRootPane().setDefaultButton(btnValider);
            
            btnValider.addActionListener(e -> {
                try {
                    String nom = txtNom.getText().trim();
                    String prenom = txtPrenom.getText().trim();
                    String email = txtEmail.getText().trim();
                    String telephone = txtTelephone.getText().trim();
                    
                    // Validations (identiques à l'inscription)
                    if (!ValidationUtil.estNonVide(nom)) {
                        MainApplication.showError("Erreur", "Le nom est obligatoire.");
                        return;
                    }
                    if (!ValidationUtil.estNonVide(prenom)) {
                        MainApplication.showError("Erreur", "Le prénom est obligatoire.");
                        return;
                    }
                    
                    if (!ValidationUtil.estNonVide(email) && !ValidationUtil.estNonVide(telephone)) {
                        MainApplication.showError("Erreur", 
                            "Vous devez renseigner au moins un moyen de contact :\n" +
                            "- Email\n" +
                            "- Téléphone");
                        return;
                    }
                    
                    if (ValidationUtil.estNonVide(email) && !ValidationUtil.estEmailValide(email)) {
                        MainApplication.showError("Erreur", 
                            "L'email n'est pas valide.\nFormat attendu : exemple@domaine.com");
                        return;
                    }
                    
                    // Créer l'adhérent modifié
                    Adherent adherentModifie = new Adherent(nom, prenom, email, telephone);
                    
                    // Conserver l'ID et les pénalités
                    adherentModifie.setIdAdherent(id);
                    adherentModifie.setStatutPenalite(adherentActuel.hasPenalite());
                    adherentModifie.setMontantPenalite(adherentActuel.getMontantPenalite());
                    
                    manager.modifierAdherent(adherentModifie);
                    MainApplication.showSuccess("Succès", "Adhérent modifié avec succès !");
                    loadAdherents();
                    dialog.dispose();
                    
                } catch (AdherentException ex) {
                    if (ex.getMessage().contains("UNIQUE") || 
                        ex.getMessage().contains("email") ||
                        ex.getMessage().toLowerCase().contains("unique constraint")) {
                        MainApplication.showError("Erreur", "Cet email est déjà utilisé par un autre adhérent.");
                    } else {
                        MainApplication.showError("Erreur", ex.getMessage());
                    }
                } catch (Exception ex) {
                    // Attraper toutes les autres exceptions (notamment SQLException)
                    String message = ex.getMessage();
                    if (message != null && (message.contains("UNIQUE") || 
                        message.contains("email") ||
                        message.toLowerCase().contains("unique constraint") ||
                        message.toLowerCase().contains("duplicate"))) {
                        MainApplication.showError("Erreur", "Cet email est déjà utilisé par un autre adhérent.");
                    } else {
                        MainApplication.showError("Erreur", "Erreur lors de la modification de l'adhérent : " + 
                            (message != null ? message : ex.getClass().getSimpleName()));
                    }
                }
            });
            
            btnAnnuler.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(btnValider);
            buttonPanel.add(btnAnnuler);
            
            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            
        } catch (AdherentException ex) {
            MainApplication.showError("Erreur", ex.getMessage());
        }
    }
    
    /**
     * Supprime un adhérent après vérification qu'il n'a pas d'emprunts en cours
     */
    private void supprimerAdherent() {
        int selectedRow = tableAdherents.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un adhérent.");
            return;
        }
        
        try {
            // Récupérer l'adhérent sélectionné
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String nom = (String) tableModel.getValueAt(selectedRow, 1);
            String prenom = (String) tableModel.getValueAt(selectedRow, 2);
            
            // Vérifier si l'adhérent a des emprunts en cours
            List<Emprunt> empruntsEnCours = manager.getHistoriqueAdherent(id).stream()
                .filter(Emprunt::isEnCours)
                .toList();
            
            if (!empruntsEnCours.isEmpty()) {
                MainApplication.showError("Erreur", 
                    "Impossible de supprimer cet adhérent.\n" +
                    "Il a encore " + empruntsEnCours.size() + " emprunt(s) en cours.\n" +
                    "Veuillez d'abord retourner tous les documents.");
                return;
            }
            
            // Demander confirmation
            int confirm = JOptionPane.showConfirmDialog(
                mainPanel,
                "Voulez-vous vraiment supprimer l'adhérent :\n" + 
                prenom + " " + nom + " ?\n\n" +
                "Cette action est irréversible.",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                manager.supprimerAdherent(id);
                MainApplication.showSuccess("Succès", "Adhérent supprimé avec succès !");
                loadAdherents();
            }
            
        } catch (Exception ex) {
            MainApplication.showError("Erreur", "Erreur lors de la suppression : " + ex.getMessage());
        }
    }

    /**
     * Affiche l'historique des emprunts d'un adhérent
     */
    private void afficherHistorique() {
        int selectedRow = tableAdherents.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un adhérent.");
            return;
        }
        
        int idAdherent = (int) tableModel.getValueAt(selectedRow, 0);
        String nom = tableModel.getValueAt(selectedRow, 2) + " " + tableModel.getValueAt(selectedRow, 1);
        
        List<Emprunt> historique = manager.getHistoriqueAdherent(idAdherent);
        
        if (historique.isEmpty()) {
            MainApplication.showInfo("Historique", nom + " n'a aucun emprunt enregistré.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(nom).append(" - ").append(historique.size()).append(" emprunt(s)\n\n");
        
        for (Emprunt emprunt : historique) {
            sb.append("• ").append(emprunt.getDocument().getTitre())
              .append(" (").append(emprunt.getDateEmprunt()).append(")");
            if (!emprunt.isEnCours()) {
                sb.append(" - Retourné");
                if (emprunt.getPenalite() > 0) {
                    sb.append(" - Pénalité: ").append(String.format("%.2f€", emprunt.getPenalite()));
                }
            } else {
                sb.append(" - EN COURS");
            }
            sb.append("\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(50);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(mainPanel, scrollPane, "Historique des emprunts", 
                                      JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Permet de payer partiellement ou totalement les pénalités d'un adhérent
     */
    private void payerPenalite() {
        int selectedRow = tableAdherents.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un adhérent.");
            return;
        }
        
        try {
            int idAdherent = (int) tableModel.getValueAt(selectedRow, 0);
            Adherent adherent = manager.rechercherAdherentParId(idAdherent);
            
            if (adherent.getMontantPenalite() <= 0) {
                MainApplication.showInfo("Info", "Cet adhérent n'a pas de pénalité.");
                return;
            }
            
            String montantStr = JOptionPane.showInputDialog(
                mainPanel,
                "Montant dû : " + String.format("%.2f €", adherent.getMontantPenalite()) + 
                "\n\nMontant à payer :",
                String.format("%.2f", adherent.getMontantPenalite())
            );
            
            if (montantStr != null && !montantStr.trim().isEmpty()) {
                montantStr = montantStr.replace(',', '.');
                double montant = Double.parseDouble(montantStr);
                if (montant <= 0) {
                    MainApplication.showError("Erreur", "Le montant doit être positif.");
                    return;
                }
                
                adherent.payerPenalite(montant);
                manager.modifierAdherent(adherent);
                
                MainApplication.showSuccess("Succès", 
                    String.format("Paiement de %.2f € enregistré.\nNouveau solde : %.2f €", 
                                 montant, adherent.getMontantPenalite()));
                loadAdherents();
            }
            
        } catch (NumberFormatException ex) {
            MainApplication.showError("Erreur", "Veuillez entrer un montant valide.");
        } catch (AdherentException ex) {
            MainApplication.showError("Erreur", ex.getMessage());
        }
    }
    
    /**
     * Rafraîchit la vue (recharge les données)
     */
    public void refresh() {
        loadAdherents();
    }
    
    /**
     * Retourne le panneau principal pour intégration dans l'application
     */
    public JPanel getPanel() {
        return mainPanel;
    }
}