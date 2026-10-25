package com.bibliotheque.ui;

import com.bibliotheque.manager.BibliothequeManager;
import com.bibliotheque.model.*;
import com.bibliotheque.util.ValidationUtil;
import com.bibliotheque.exception.DocumentException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue Swing pour la gestion des documents (Livre, Magazine, CD)
 * Permet l'ajout, la modification, la suppression et la recherche de documents
 */
public class DocumentView {
    
    private BibliothequeManager manager;
    private JPanel mainPanel;
    private JTable tableDocuments;
    private DefaultTableModel tableModel;
    
    private JTextField txtRecherche;
    private JComboBox<String> comboTypeRecherche;
    
    public DocumentView(BibliothequeManager manager) {
        this.manager = manager;
        initView();
        loadDocuments();
    }
    
    /**
     * Initialise la structure de la vue
     */
    private void initView() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createSearchBar(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createActionButtons(), BorderLayout.SOUTH);
    }
    
    /**
     * Crée la barre de recherche (titre, auteur, genre, ID, ou tous)
     */
    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Rechercher :"));
        
        comboTypeRecherche = new JComboBox<>(new String[]{"Titre", "Auteur", "Genre", "ID", "Tous"});
        panel.add(comboTypeRecherche);
        
        txtRecherche = new JTextField(25);
        panel.add(txtRecherche);
        
        JButton btnRechercher = new JButton("Rechercher");
        btnRechercher.addActionListener(e -> rechercher());
        panel.add(btnRechercher);
        
        JButton btnReset = new JButton("Tout afficher");
        btnReset.addActionListener(e -> loadDocuments());
        panel.add(btnReset);

        txtRecherche.addActionListener(e -> rechercher());
        
        return panel;
    }
    
    /**
     * Crée la table d'affichage avec renderer pour la disponibilité
     */
    private JScrollPane createTablePanel() {
        String[] columnNames = {"ID", "Type", "Titre", "Auteur", "Genre", "Info spécifique", "Disponible"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableDocuments = new JTable(tableModel);
        tableDocuments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDocuments.setRowHeight(25);
        
        // Renderer pour la colonne Disponible (✓ Oui / ✗ Non avec couleur)
        tableDocuments.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Boolean) {
                    setText((Boolean) value ? "✓ Oui" : "✗ Non");
                    setForeground((Boolean) value ? new Color(0, 150, 0) : Color.RED);
                }
                return c;
            }
        });
        
        return new JScrollPane(tableDocuments);
    }
    
    /**
     * Crée les boutons d'action
     */
    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAjouter = new JButton("Ajouter un document");
        btnAjouter.addActionListener(e -> ajouterDocument());
        panel.add(btnAjouter);
        
        JButton btnModifier = new JButton("Modifier");
        btnModifier.addActionListener(e -> modifierDocument());
        panel.add(btnModifier);
        
        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerDocument());
        panel.add(btnSupprimer);
        
        JButton btnDetails = new JButton("Détails");
        btnDetails.addActionListener(e -> afficherDetails());
        panel.add(btnDetails);
        
        return panel;
    }

    /**
     * Ajuste automatiquement la largeur des colonnes
     */
    private void autoResizeColumns() {
        for (int column = 0; column < tableDocuments.getColumnCount(); column++) {
            int width = 50;
            for (int row = 0; row < tableDocuments.getRowCount(); row++) {
                javax.swing.table.TableCellRenderer renderer = 
                    tableDocuments.getCellRenderer(row, column);
                Component comp = tableDocuments.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            tableDocuments.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }
    
    /**
     * Charge tous les documents dans la table
     */
    private void loadDocuments() {
        tableModel.setRowCount(0);
        List<Document> documents = manager.listerTousLesDocuments();
        
        for (Document doc : documents) {
            String type = doc.getClass().getSimpleName();
            String infoSpecifique = getInfoSpecifique(doc);
            
            tableModel.addRow(new Object[]{
                doc.getId(),
                type,
                doc.getTitre(),
                doc.getAuteur(),
                doc.getGenre(),
                infoSpecifique,
                doc.isDisponible()
            });
        }
        
        txtRecherche.setText("");
        autoResizeColumns();
    }
    
    /**
     * Récupère l'info spécifique selon le type de document
     * Livre: ISBN, Magazine: N°, CD: durée
     */
    private String getInfoSpecifique(Document doc) {
        if (doc instanceof Livre) {
            return "ISBN: " + ((Livre) doc).getIsbn();
        } else if (doc instanceof Magazine) {
            return "N°" + ((Magazine) doc).getNumero();
        } else if (doc instanceof CD) {
            return ((CD) doc).getDuree() + " min";
        }
        return "";
    }
    
    /**
     * Recherche des documents selon le critère sélectionné
     */
    private void rechercher() {
        String recherche = txtRecherche.getText().trim();
        if (recherche.isEmpty()) {
            loadDocuments();
            return;
        }
        
        tableModel.setRowCount(0);
        String type = (String) comboTypeRecherche.getSelectedItem();
        
        List<Document> resultats = new ArrayList<>();

        try {
            if ("Titre".equals(type)) {
                resultats = manager.rechercherDocumentParTitre(recherche);
            } else if ("Auteur".equals(type)) {
                resultats = manager.rechercherDocumentParAuteur(recherche);
            } else if ("Genre".equals(type)) {
                resultats = manager.rechercherDocumentParGenre(recherche);
            } else if ("ID".equals(type)){
                try {
                    int id = Integer.parseInt(recherche);
                    Document doc = manager.rechercherDocumentParId(id);
                    if (doc != null) {
                        resultats.add(doc);
                    } else {
                        MainApplication.showInfo("Aucun résultat", "Aucun document trouvé avec l'ID " + id);
                    }
                } catch (NumberFormatException ex) {
                    MainApplication.showError("Erreur", "Veuillez entrer un ID valide (nombre entier).");
                    return;
                }
            } else {
                // Recherche combinée (Tous)
                resultats = manager.rechercherDocumentParTitre(recherche);
                List<Document> parAuteur = manager.rechercherDocumentParAuteur(recherche);
                for (Document doc : parAuteur) {
                    if (!resultats.contains(doc)) {
                        resultats.add(doc);
                    }
                }
            }
        } catch (DocumentException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la recherche : " + e.getMessage());
        }
        
        for (Document doc : resultats) {
            tableModel.addRow(new Object[]{
                doc.getId(),
                doc.getClass().getSimpleName(),
                doc.getTitre(),
                doc.getAuteur(),
                doc.getGenre(),
                getInfoSpecifique(doc),
                doc.isDisponible()
            });
        }

        autoResizeColumns();
    }
    
    /**
     * Ouvre un dialogue pour ajouter un nouveau document
     * Formulaire dynamique selon le type (Livre, Magazine, CD)
     */
    private void ajouterDocument() {
        JDialog dialog = new JDialog((Frame) null, "Ajouter un document", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Livre", "Magazine", "CD"});
        JTextField txtTitre = new JTextField();
        JTextField txtAuteur = new JTextField();
        JTextField txtGenre = new JTextField();
        
        // Champs spécifiques à chaque type
        JTextField txtISBN = new JTextField();
        JTextField txtPages = new JTextField();
        JTextField txtNumero = new JTextField();
        JTextField txtPeriodicite = new JTextField();
        JTextField txtDuree = new JTextField();
        JTextField txtArtiste = new JTextField();
        
        JLabel lblSpecifique1 = new JLabel("ISBN:");
        JLabel lblSpecifique2 = new JLabel("Nombre de pages:");
        
        // Changer les champs spécifiques selon le type sélectionné
        comboType.addActionListener(e -> {
            // Retirer les anciennes lignes spécifiques
            int componentCount = formPanel.getComponentCount();
            if (componentCount > 8) {
                formPanel.remove(componentCount - 1);
                formPanel.remove(componentCount - 2);
                formPanel.remove(componentCount - 3);
                formPanel.remove(componentCount - 4);
            }
            
            String type = (String) comboType.getSelectedItem();
            if ("Livre".equals(type)) {
                lblSpecifique1.setText("ISBN:");
                lblSpecifique2.setText("Nombre de pages:");
                formPanel.add(lblSpecifique1);
                formPanel.add(txtISBN);
                formPanel.add(lblSpecifique2);
                formPanel.add(txtPages);
            } else if ("Magazine".equals(type)) {
                lblSpecifique1.setText("Numéro:");
                lblSpecifique2.setText("Périodicité:");
                formPanel.add(lblSpecifique1);
                formPanel.add(txtNumero);
                formPanel.add(lblSpecifique2);
                formPanel.add(txtPeriodicite);
            } else if ("CD".equals(type)) {
                lblSpecifique1.setText("Durée (min):");
                lblSpecifique2.setText("Artiste:");
                formPanel.add(lblSpecifique1);
                formPanel.add(txtDuree);
                formPanel.add(lblSpecifique2);
                formPanel.add(txtArtiste);
            }
            formPanel.revalidate();
            formPanel.repaint();
        });
        
        formPanel.add(new JLabel("Type:"));
        formPanel.add(comboType);
        formPanel.add(new JLabel("Titre:"));
        formPanel.add(txtTitre);
        formPanel.add(new JLabel("Auteur:"));
        formPanel.add(txtAuteur);
        formPanel.add(new JLabel("Genre:"));
        formPanel.add(txtGenre);
        
        // Initialiser avec Livre par défaut
        comboType.setSelectedIndex(0);
        comboType.getActionListeners()[0].actionPerformed(null);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnValider = new JButton("Ajouter");
        JButton btnAnnuler = new JButton("Annuler");
        dialog.getRootPane().setDefaultButton(btnValider);
        
        btnValider.addActionListener(e -> {
            try {
                String titre = txtTitre.getText().trim();
                String auteur = txtAuteur.getText().trim();
                String genre = txtGenre.getText().trim();
                
                // Validations communes
                if (!ValidationUtil.estNonVide(titre)) {
                    MainApplication.showError("Erreur", "Le titre est obligatoire.");
                    return;
                }
                if (!ValidationUtil.estNonVide(auteur)) {
                    MainApplication.showError("Erreur", "L'auteur est obligatoire.");
                    return;
                }
                
                Document doc = null;
                String type = (String) comboType.getSelectedItem();
                
                if ("Livre".equals(type)) {
                    String isbn = txtISBN.getText().trim();
                    String pagesStr = txtPages.getText().trim();
                    
                    if (!ValidationUtil.estIsbnValide(isbn)) {
                        MainApplication.showError("Erreur", 
                            "L'ISBN n'est pas valide.\n" +
                            "Format attendu : 13 chiffres\n" +
                            "Exemple : 9782123456789");
                        return;
                    }
                    
                    if (!ValidationUtil.estNonVide(pagesStr)) {
                        MainApplication.showError("Erreur", "Le nombre de pages est obligatoire.");
                        return;
                    }
                    
                    int pages = Integer.parseInt(pagesStr);
                    if (pages <= 0) {
                        MainApplication.showError("Erreur", "Le nombre de pages doit être positif.");
                        return;
                    }
                    
                    doc = new Livre(titre, auteur, genre, isbn, pages);
                    
                } else if ("Magazine".equals(type)) {
                    String periodicite = txtPeriodicite.getText().trim();
                    String numeroStr = txtNumero.getText().trim();
                    
                    if (!ValidationUtil.estNonVide(periodicite)) {
                        MainApplication.showError("Erreur", "La périodicité est obligatoire.");
                        return;
                    }
                    if (!ValidationUtil.estNonVide(numeroStr)) {
                        MainApplication.showError("Erreur", "Le numéro est obligatoire.");
                        return;
                    }
                    
                    int numero = Integer.parseInt(numeroStr);
                    if (numero <= 0) {
                        MainApplication.showError("Erreur", "Le numéro doit être positif.");
                        return;
                    }
                    
                    doc = new Magazine(titre, auteur, genre, periodicite, numero);
                    
                } else if ("CD".equals(type)) {
                    String dureeStr = txtDuree.getText().trim();
                    String artiste = txtArtiste.getText().trim();
                    
                    if (!ValidationUtil.estNonVide(dureeStr)) {
                        MainApplication.showError("Erreur", "La durée est obligatoire.");
                        return;
                    }
                    if (!ValidationUtil.estNonVide(artiste)) {
                        MainApplication.showError("Erreur", "L'artiste est obligatoire.");
                        return;
                    }
                    
                    int duree = Integer.parseInt(dureeStr);
                    if (duree <= 0) {
                        MainApplication.showError("Erreur", "La durée doit être positive.");
                        return;
                    }
                    
                    doc = new CD(titre, auteur, genre, duree, artiste);
                }
                
                manager.ajouterDocument(doc);
                MainApplication.showSuccess("Succès", "Document ajouté avec succès !");
                loadDocuments();
                dialog.dispose();
                
            } catch (NumberFormatException ex) {
                MainApplication.showError("Erreur", "Veuillez entrer des nombres valides.");
            } catch (DocumentException ex) {
                MainApplication.showError("Erreur", ex.getMessage());
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
     * Ouvre un dialogue pour modifier un document sélectionné
     * Pré-remplit les champs avec les valeurs actuelles
     */
    private void modifierDocument() {
        int selectedRow = tableDocuments.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un document.");
            return;
        }
        
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Document docActuel = manager.rechercherDocumentParId(id);
            
            JDialog dialog = new JDialog((Frame) null, "Modifier un document", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(400, 400);
            dialog.setLocationRelativeTo(mainPanel);
            
            JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Type non modifiable
            String typeDoc = docActuel.getClass().getSimpleName();
            JLabel lblType = new JLabel(typeDoc);
            
            // Champs communs pré-remplis
            JTextField txtTitre = new JTextField(docActuel.getTitre());
            JTextField txtAuteur = new JTextField(docActuel.getAuteur());
            JTextField txtGenre = new JTextField(docActuel.getGenre());
            
            // Champs spécifiques
            JTextField txtISBN = new JTextField();
            JTextField txtPages = new JTextField();
            JTextField txtNumero = new JTextField();
            JTextField txtPeriodicite = new JTextField();
            JTextField txtDuree = new JTextField();
            JTextField txtArtiste = new JTextField();
            
            JLabel lblSpecifique1 = new JLabel();
            JLabel lblSpecifique2 = new JLabel();
            
            formPanel.add(new JLabel("Type:"));
            formPanel.add(lblType);
            formPanel.add(new JLabel("Titre:"));
            formPanel.add(txtTitre);
            formPanel.add(new JLabel("Auteur:"));
            formPanel.add(txtAuteur);
            formPanel.add(new JLabel("Genre:"));
            formPanel.add(txtGenre);
            
            // Pré-remplir selon le type
            if (docActuel instanceof Livre) {
                Livre livre = (Livre) docActuel;
                txtISBN.setText(livre.getIsbn());
                txtPages.setText(String.valueOf(livre.getNbPages()));
                
                lblSpecifique1.setText("ISBN:");
                lblSpecifique2.setText("Nombre de pages:");
                formPanel.add(lblSpecifique1);
                formPanel.add(txtISBN);
                formPanel.add(lblSpecifique2);
                formPanel.add(txtPages);
                
            } else if (docActuel instanceof Magazine) {
                Magazine magazine = (Magazine) docActuel;
                txtNumero.setText(String.valueOf(magazine.getNumero()));
                txtPeriodicite.setText(magazine.getPeriodicite());
                
                lblSpecifique1.setText("Numéro:");
                lblSpecifique2.setText("Périodicité:");
                formPanel.add(lblSpecifique1);
                formPanel.add(txtNumero);
                formPanel.add(lblSpecifique2);
                formPanel.add(txtPeriodicite);
                
            } else if (docActuel instanceof CD) {
                CD cd = (CD) docActuel;
                txtDuree.setText(String.valueOf(cd.getDuree()));
                txtArtiste.setText(cd.getArtiste());
                
                lblSpecifique1.setText("Durée (min):");
                lblSpecifique2.setText("Artiste:");
                formPanel.add(lblSpecifique1);
                formPanel.add(txtDuree);
                formPanel.add(lblSpecifique2);
                formPanel.add(txtArtiste);
            }
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnValider = new JButton("Modifier");
            JButton btnAnnuler = new JButton("Annuler");
            dialog.getRootPane().setDefaultButton(btnValider);
            
            btnValider.addActionListener(e -> {
                try {
                    String titre = txtTitre.getText().trim();
                    String auteur = txtAuteur.getText().trim();
                    String genre = txtGenre.getText().trim();
                    
                    // Validations communes
                    if (!ValidationUtil.estNonVide(titre)) {
                        MainApplication.showError("Erreur", "Le titre est obligatoire.");
                        return;
                    }
                    if (!ValidationUtil.estNonVide(auteur)) {
                        MainApplication.showError("Erreur", "L'auteur est obligatoire.");
                        return;
                    }
                    
                    Document docModifie = null;
                    
                    if (docActuel instanceof Livre) {
                        String isbn = txtISBN.getText().trim();
                        String pagesStr = txtPages.getText().trim();
                        
                        if (!ValidationUtil.estIsbnValide(isbn)) {
                            MainApplication.showError("Erreur", 
                                "L'ISBN n'est pas valide.\n" +
                                "Format attendu : 13 chiffres\n" +
                                "Exemple : 9782123456789");
                            return;
                        }
                        
                        if (!ValidationUtil.estNonVide(pagesStr)) {
                            MainApplication.showError("Erreur", "Le nombre de pages est obligatoire.");
                            return;
                        }
                        
                        int pages = Integer.parseInt(pagesStr);
                        if (pages <= 0) {
                            MainApplication.showError("Erreur", "Le nombre de pages doit être positif.");
                            return;
                        }
                        
                        docModifie = new Livre(id, titre, auteur, genre, isbn, pages);
                        
                    } else if (docActuel instanceof Magazine) {
                        String periodicite = txtPeriodicite.getText().trim();
                        String numeroStr = txtNumero.getText().trim();
                        
                        if (!ValidationUtil.estNonVide(periodicite)) {
                            MainApplication.showError("Erreur", "La périodicité est obligatoire.");
                            return;
                        }
                        if (!ValidationUtil.estNonVide(numeroStr)) {
                            MainApplication.showError("Erreur", "Le numéro est obligatoire.");
                            return;
                        }
                        
                        int numero = Integer.parseInt(numeroStr);
                        if (numero <= 0) {
                            MainApplication.showError("Erreur", "Le numéro doit être positif.");
                            return;
                        }
                        
                        docModifie = new Magazine(id, titre, auteur, genre, periodicite, numero);
                        
                    } else if (docActuel instanceof CD) {
                        String dureeStr = txtDuree.getText().trim();
                        String artiste = txtArtiste.getText().trim();
                        
                        if (!ValidationUtil.estNonVide(dureeStr)) {
                            MainApplication.showError("Erreur", "La durée est obligatoire.");
                            return;
                        }
                        if (!ValidationUtil.estNonVide(artiste)) {
                            MainApplication.showError("Erreur", "L'artiste est obligatoire.");
                            return;
                        }
                        
                        int duree = Integer.parseInt(dureeStr);
                        if (duree <= 0) {
                            MainApplication.showError("Erreur", "La durée doit être positive.");
                            return;
                        }
                        
                        docModifie = new CD(id, titre, auteur, genre, duree, artiste);
                    }
                    
                    // Conserver la disponibilité
                    docModifie.setDisponible(docActuel.isDisponible());
                    
                    manager.modifierDocument(docModifie);
                    MainApplication.showSuccess("Succès", "Document modifié avec succès !");
                    loadDocuments();
                    dialog.dispose();
                    
                } catch (NumberFormatException ex) {
                    MainApplication.showError("Erreur", "Veuillez entrer des nombres valides.");
                } catch (DocumentException ex) {
                    MainApplication.showError("Erreur", ex.getMessage());
                }
            });
            
            btnAnnuler.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(btnValider);
            buttonPanel.add(btnAnnuler);
            
            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            
        } catch (DocumentException ex) {
            MainApplication.showError("Erreur", ex.getMessage());
        }
    }
    
    /**
     * Supprime un document après confirmation
     */
    private void supprimerDocument() {
        int selectedRow = tableDocuments.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un document.");
            return;
        }
        
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String titre = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(
            mainPanel,
            "Voulez-vous vraiment supprimer : " + titre + " ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.supprimerDocument(id);
                MainApplication.showSuccess("Succès", "Document supprimé !");
                loadDocuments();
            } catch (DocumentException ex) {
                MainApplication.showError("Erreur", ex.getMessage());
            }
        }
    }
    
    /**
     * Affiche les détails complets d'un document sélectionné
     */
    private void afficherDetails() {
        int selectedRow = tableDocuments.getSelectedRow();
        if (selectedRow == -1) {
            MainApplication.showError("Erreur", "Veuillez sélectionner un document.");
            return;
        }
        
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Document doc = manager.rechercherDocumentParId(id);
            
            StringBuilder details = new StringBuilder();
            details.append("Type: ").append(doc.getClass().getSimpleName()).append("\n");
            details.append("Titre: ").append(doc.getTitre()).append("\n");
            details.append("Auteur: ").append(doc.getAuteur()).append("\n");
            details.append("Genre: ").append(doc.getGenre()).append("\n");
            details.append("Disponible: ").append(doc.isDisponible() ? "Oui" : "Non").append("\n\n");
            
            if (doc instanceof Livre) {
                Livre livre = (Livre) doc;
                details.append("ISBN: ").append(livre.getIsbn()).append("\n");
                details.append("Pages: ").append(livre.getNbPages());
            } else if (doc instanceof Magazine) {
                Magazine mag = (Magazine) doc;
                details.append("Numéro: ").append(mag.getNumero()).append("\n");
                details.append("Périodicité: ").append(mag.getPeriodicite());
            } else if (doc instanceof CD) {
                CD cd = (CD) doc;
                details.append("Durée: ").append(cd.getDuree()).append(" min\n");
                details.append("Artiste: ").append(cd.getArtiste());
            }
            
            MainApplication.showInfo("Détails du document", details.toString());
            
        } catch (DocumentException ex) {
            MainApplication.showError("Erreur", ex.getMessage());
        }
    }
    
    /**
     * Rafraîchit la vue
     */
    public void refresh() {
        loadDocuments();
    }
    
    /**
     * Retourne le panneau principal
     */
    public JPanel getPanel() {
        return mainPanel;
    }
}