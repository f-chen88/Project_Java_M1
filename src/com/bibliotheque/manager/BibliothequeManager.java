package com.bibliotheque.manager;

import com.bibliotheque.dao.DocumentDAO;
import com.bibliotheque.dao.AdherentDAO;    
import com.bibliotheque.dao.EmpruntDAO;
import com.bibliotheque.exception.AdherentException;
import com.bibliotheque.exception.DocumentException;
import com.bibliotheque.exception.EmpruntException;
import com.bibliotheque.model.Document;
import com.bibliotheque.model.Adherent;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.util.Constants;
import com.bibliotheque.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager central de la bibliothèque - Couche métier
 * Gère la logique métier et coordonne les opérations entre les DAO
 * Valide les règles métier (limites d'emprunts, pénalités, disponibilité)
 */
public class BibliothequeManager {
    DocumentDAO documentDAO;
    AdherentDAO adherentDAO;
    EmpruntDAO empruntDAO;
    int maxEmpruntsParAdherent = 5;
    int dureeEmpruntJours = 21;
    double penaliteParJour = 0.5;

    public BibliothequeManager(DocumentDAO documentDAO, AdherentDAO adherentDAO, EmpruntDAO empruntDAO) {
        this.documentDAO = documentDAO;
        this.adherentDAO = adherentDAO;
        this.empruntDAO = empruntDAO;
    }

    // ========================================
    // GESTION DES DOCUMENTS
    // ========================================

    /**
     * Ajoute un document à la bibliothèque
     * @throws DocumentException si validation échoue ou ID déjà existant
     */
    public int ajouterDocument(Document document) throws DocumentException {
        if (document == null) {
            throw new DocumentException("Le document ne peut pas être null");
        }
        
        if (document.getTitre() == null || document.getTitre().trim().isEmpty()) {
            throw new DocumentException("Le titre du document est obligatoire");
        }

        if (document.getId() != 0 && documentDAO.exists(document.getId())) {
            throw new DocumentException("Un document avec l'ID " + document.getId() + " existe déjà");
        }
        
        try {
            return documentDAO.create(document);
        } catch (Exception e) {
            throw new DocumentException("Erreur lors de l'ajout du document: " + e.getMessage());
        }
    }

    /**
     * Recherche un document par son ID
     * @throws DocumentException si document introuvable
     */
    public Document rechercherDocumentParId(int id) throws DocumentException {
        Document document = documentDAO.findById(id);
        if (document == null) {
            throw new DocumentException("Document avec l'ID " + id + " introuvable");
        }
        return document;
    }

    /**
     * Recherche des documents par titre (recherche partielle)
     * @throws DocumentException si aucun résultat
     */
    public List<Document> rechercherDocumentParTitre(String titre) throws DocumentException {
        List<Document> documents = documentDAO.findByTitre(titre);
        if (documents.isEmpty()) {
            throw new DocumentException("Aucun document trouvé avec le titre: " + titre);
        }
        return documents;
    }

    /**
     * Recherche des documents par auteur (recherche partielle)
     * @throws DocumentException si aucun résultat
     */
    public List<Document> rechercherDocumentParAuteur(String auteur) throws DocumentException {
        List<Document> documents = documentDAO.findByAuteur(auteur);
        if (documents.isEmpty()) {
            throw new DocumentException("Aucun document trouvé avec l'auteur: " + auteur);
        }
        return documents;
    }

    /**
     * Recherche des documents par genre
     * @throws DocumentException si aucun résultat
     */
    public List<Document> rechercherDocumentParGenre(String genre) throws DocumentException {
        List<Document> documents = documentDAO.findByGenre(genre);
        if (documents.isEmpty()) {
            throw new DocumentException("Aucun document trouvé avec le genre: " + genre);
        }
        return documents;
    }

    /**
     * Liste tous les documents de la bibliothèque
     */
    public List<Document> listerTousLesDocuments() {
        return documentDAO.findAll();
    }

    /**
     * Liste uniquement les documents disponibles (non empruntés)
     */
    public List<Document> listerDocumentsDisponibles() {
        return documentDAO.findAll().stream()
                .filter(Document::isDisponible)
                .collect(Collectors.toList());
    }

    /**
     * Modifie un document existant
     * @throws DocumentException si document n'existe pas
     */
    public void modifierDocument(Document document) throws DocumentException {
        if (document == null) {
            throw new DocumentException("Le document ne peut pas être null");
        }
        
        if (!documentDAO.exists(document.getId())) {
            throw new DocumentException("Le document avec l'ID " + document.getId() + " n'existe pas");
        }
        
        try {
            documentDAO.update(document);
        } catch (Exception e) {
            throw new DocumentException("Erreur lors de la modification du document: " + e.getMessage());
        }
    }

    /**
     * Supprime un document de la bibliothèque
     * @throws DocumentException si document emprunté ou introuvable
     */
    public void supprimerDocument(int id) throws DocumentException {
        Document document = documentDAO.findById(id);
        
        if (document == null) {
            throw new DocumentException("Document avec l'ID " + id + " introuvable");
        }
        
        if (!document.isDisponible()) {
            throw new DocumentException("Impossible de supprimer un document emprunté");
        }
        
        try {
            documentDAO.delete(id);
        } catch (Exception e) {
            throw new DocumentException("Erreur lors de la suppression du document: " + e.getMessage());
        }
    }

    // ========================================
    // GESTION DES ADHÉRENTS
    // ========================================

    /**
     * Inscrit un nouvel adhérent
     * @throws AdherentException si validation échoue (nom/prénom obligatoires)
     */
    public int inscrireAdherent(Adherent adherent) throws AdherentException {
        if (adherent == null) {
            throw new AdherentException("L'adhérent ne peut pas être null");
        }
        
        if (adherent.getNom() == null || adherent.getNom().trim().isEmpty()) {
            throw new AdherentException("Le nom de l'adhérent est obligatoire");
        }
        
        if (adherent.getPrenom() == null || adherent.getPrenom().trim().isEmpty()) {
            throw new AdherentException("Le prénom de l'adhérent est obligatoire");
        }
        
        try {
            return adherentDAO.create(adherent);
        } catch (Exception e) {
            // Vérifier si c'est une erreur de contrainte unique sur l'email
            String message = e.getMessage();
            String cause = e.getCause() != null ? e.getCause().getMessage() : "";
            
            if ((message != null && (message.toLowerCase().contains("unique") || 
                message.toLowerCase().contains("duplicate"))) ||
                (cause != null && (cause.toLowerCase().contains("unique") || 
                cause.toLowerCase().contains("duplicate")))) {
                throw new AdherentException("EMAIL_UNIQUE_CONSTRAINT");
            }
            
            throw new AdherentException("Erreur lors de l'inscription de l'adhérent: " + e.getMessage());
        }
    }

    /**
     * Recherche un adhérent par son ID
     * @throws AdherentException si adhérent introuvable
     */
    public Adherent rechercherAdherentParId(int id) throws AdherentException {
        Adherent adherent = adherentDAO.findById(id);
        if (adherent == null) {
            throw new AdherentException("Adhérent avec l'ID " + id + " introuvable");
        }
        return adherent;
    }

    /**
     * Recherche des adhérents par nom ou prénom (recherche partielle)
     */
    public List<Adherent> rechercherAdherentParNom(String nom) {
        return adherentDAO.findByNom(nom);
    }

    /**
     * Liste tous les adhérents
     */
    public List<Adherent> listerTousLesAdherents() {
        return adherentDAO.findAll();
    }

    /**
     * Modifie un adhérent existant
     * @throws AdherentException si adhérent n'existe pas
     */
    public void modifierAdherent(Adherent adherent) throws AdherentException {
        if (adherent == null) {
            throw new AdherentException("L'adhérent ne peut pas être null");
        }
        
        if (!adherentDAO.exists(adherent.getIdAdherent())) {
            throw new AdherentException("L'adhérent avec l'ID " + adherent.getIdAdherent() + " n'existe pas");
        }
        
        try {
            adherentDAO.update(adherent);
        } catch (Exception e) {
            // Vérifier si c'est une erreur de contrainte unique sur l'email
            String message = e.getMessage();
            String cause = e.getCause() != null ? e.getCause().getMessage() : "";
            
            if ((message != null && (message.toLowerCase().contains("unique") || 
                message.toLowerCase().contains("duplicate"))) ||
                (cause != null && (cause.toLowerCase().contains("unique") || 
                cause.toLowerCase().contains("duplicate")))) {
                throw new AdherentException("EMAIL_UNIQUE_CONSTRAINT");
            }
            
            throw new AdherentException("Erreur lors de la modification de l'adhérent: " + e.getMessage());
        }
    }

    /**
     * Récupère l'historique complet des emprunts d'un adhérent
     */
    public List<Emprunt> getHistoriqueAdherent(int idAdherent) {
        return empruntDAO.findByAdherent(idAdherent);
    }
    
    /**
     * Vérifie si un adhérent peut emprunter un document
     * Règles : pas de pénalités impayées, max 5 emprunts simultanés
     * @throws AdherentException si conditions non remplies
     */
    public boolean peutEmprunter(int idAdherent) throws AdherentException {
        Adherent adherent = rechercherAdherentParId(idAdherent);
        
        // Vérifier si l'adhérent a des pénalités non réglées
        if (adherent.hasPenalite() && adherent.getMontantPenalite() > 0) {
            throw new AdherentException("L'adhérent a des pénalités non réglées de " + adherent.getMontantPenalite() + "€");
        }
        
        // Vérifier le nombre d'emprunts en cours
        List<Emprunt> empruntsEnCours = empruntDAO.findByAdherent(idAdherent).stream()
            .filter(Emprunt::isEnCours)
            .toList();
        if (empruntsEnCours.size() >= Constants.MAX_EMPRUNTS_PAR_ADHERENT) {
            throw new AdherentException("L'adhérent a atteint le nombre maximal d'emprunts (" + 
                Constants.MAX_EMPRUNTS_PAR_ADHERENT + ")");
        }
        
        return true;
    }

    /**
     * Supprime un adhérent de la bibliothèque
     * @throws AdherentException si adhérent a des emprunts en cours
     */
    public void supprimerAdherent(int id) throws AdherentException {
        Adherent adherent = adherentDAO.findById(id);
        
        if (adherent == null) {
            throw new AdherentException("Adhérent avec l'ID " + id + " introuvable");
        }
        
        // Vérifier qu'il n'a pas d'emprunts en cours
        List<Emprunt> empruntsEnCours = empruntDAO.findByAdherent(id).stream()
            .filter(Emprunt::isEnCours)
            .toList();
        if (!empruntsEnCours.isEmpty()) {
            throw new AdherentException("Impossible de supprimer un adhérent avec des emprunts en cours");
        }
        
        try {
            adherentDAO.delete(id);
        } catch (Exception e) {
            throw new AdherentException("Erreur lors de la suppression de l'adhérent: " + e.getMessage());
        }
    }

    // ========================================
    // GESTION DES EMPRUNTS
    // ========================================

    /**
     * Crée un nouvel emprunt
     * Vérifie : disponibilité du document, droits de l'adhérent
     * Met à jour automatiquement la disponibilité du document
     * @return L'emprunt créé avec date de retour prévue (aujourd'hui + 21 jours)
     * @throws EmpruntException si document indisponible ou adhérent bloqué
     */
    public Emprunt emprunterDocument(int idDocument, int idAdherent) throws EmpruntException {
        try {
            // Récupérer le document et l'adhérent
            Document document = rechercherDocumentParId(idDocument);
            Adherent adherent = rechercherAdherentParId(idAdherent);
            
            // Vérifier la disponibilité du document
            if (!document.isDisponible()) {
                throw new EmpruntException("Le document '" + document.getTitre() + "' n'est pas disponible");
            }
            
            // Vérifier si l'adhérent peut emprunter
            peutEmprunter(idAdherent);
            
            // Créer l'emprunt
            LocalDate dateEmprunt = LocalDate.now();
            LocalDate dateRetourPrevue = DateUtil.calculerDateRetourPrevue(dateEmprunt);
            
            Emprunt emprunt = new Emprunt(document, adherent, dateEmprunt, dateRetourPrevue);
            
            // Enregistrer l'emprunt
            int idEmprunt = empruntDAO.create(emprunt);
            emprunt.setIdEmprunt(idEmprunt);
            
            // Mettre à jour la disponibilité du document
            document.setDisponible(false);
            documentDAO.update(document);
            
            return emprunt;
            
        } catch (DocumentException | AdherentException e) {
            throw new EmpruntException("Impossible d'emprunter: " + e.getMessage());
        } catch (Exception e) {
            throw new EmpruntException("Erreur lors de l'emprunt: " + e.getMessage());
        }
    }

    /**
     * Traite le retour d'un document emprunté
     * Calcule automatiquement les pénalités si retard (0.50€/jour)
     * Met à jour le compte de l'adhérent et remet le document disponible
     * @throws EmpruntException si emprunt introuvable ou déjà retourné
     */
    public void retournerDocument(int idEmprunt) throws EmpruntException {
        try {
            // Récupérer l'emprunt
            Emprunt emprunt = empruntDAO.findById(idEmprunt);
            
            if (emprunt == null) {
                throw new EmpruntException("Emprunt avec l'ID " + idEmprunt + " introuvable");
            }
            
            if (!emprunt.isEnCours()) {
                throw new EmpruntException("Cet emprunt a déjà été retourné");
            }
            
            // Enregistrer la date de retour
            LocalDate dateRetourReelle = LocalDate.now();
            emprunt.setDateRetourReelle(dateRetourReelle);
            emprunt.setEnCours(false);
            
            // Calculer la pénalité si retard
            if (DateUtil.estEnRetard(emprunt.getDateRetourPrevue())) {
                int joursRetard = (int) DateUtil.calculerJoursRetard(emprunt.getDateRetourPrevue(), emprunt.getDateRetourReelle());
                double penalite = joursRetard * Constants.PENALITE_PAR_JOUR;
                emprunt.setPenalite(penalite);
                
                // Ajouter la pénalité à l'adhérent
                Adherent adherent = emprunt.getAdherent();
                adherent.ajouterPenalite(penalite);
                adherent.setStatutPenalite(true);
                adherentDAO.update(adherent);
            }
            
            // Mettre à jour l'emprunt
            empruntDAO.update(emprunt);
            
            // Remettre le document disponible
            Document document = emprunt.getDocument();
            document.setDisponible(true);
            documentDAO.update(document);
            
        } catch (Exception e) {
            throw new EmpruntException("Erreur lors du retour du document: " + e.getMessage());
        }
    }

    /**
     * Liste tous les emprunts actuellement en cours
     */
    public List<Emprunt> getEmpruntsEnCours() {
        return empruntDAO.findEnCours();
    }

    /**
     * Liste tous les emprunts en retard (date de retour prévue dépassée)
     */
    public List<Emprunt> getEmpruntsEnRetard() {
        return empruntDAO.findEnRetard();
    }

    /**
     * Liste tous les emprunts (historique complet)
     */
    public List<Emprunt> getTousLesEmprunts() {
        return empruntDAO.findAll();
    }
}