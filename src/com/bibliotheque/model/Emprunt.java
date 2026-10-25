package com.bibliotheque.model;

import java.time.LocalDate;

/**
 * Modèle représentant un emprunt de document
 * Gère le cycle de vie d'un emprunt : création, retour, calcul de pénalités
 */
public class Emprunt {
    int idEmprunt; 
    Document document;              // Document emprunté
    Adherent adherent;              // Adhérent emprunteur
    LocalDate dateEmprunt;          // Date de l'emprunt
    LocalDate dateRetourPrevue;     // Date prévue de retour (emprunt + 21 jours)
    LocalDate dateRetourReelle;     // Date effective du retour (null si en cours)
    double penalite;                // Montant de la pénalité en € (si retard)
    boolean enCours;                // true si le document n'est pas encore retourné

    /**
     * Constructeur pour créer un nouvel emprunt
     * Par défaut : en cours, pas de pénalité
     */
    public Emprunt(Document document, Adherent adherent, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        this.document = document;
        this.adherent = adherent;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.enCours = true;
        this.penalite = 0.0;
    }

    public int getIdEmprunt() {
        return idEmprunt;
    }

    public void setIdEmprunt(int idEmprunt) {
        this.idEmprunt = idEmprunt;
    }

    public Document getDocument() {
        return document;
    } 

    public Adherent getAdherent() {
        return adherent;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public LocalDate getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public void setDateRetourPrevue(LocalDate dateRetourPrevue) {
        this.dateRetourPrevue = dateRetourPrevue;
    }

    public LocalDate getDateRetourReelle() {
        return dateRetourReelle;
    }

    public void setDateRetourReelle(LocalDate dateRetourReelle) {
        this.dateRetourReelle = dateRetourReelle;
    }

    public boolean isEnCours() {
        return enCours;
    }

    public void setEnCours(boolean enCours) {
        this.enCours = enCours;
    }

    /**
     * Vérifie si l'emprunt est en retard
     * Compare la date de retour prévue avec la date réelle (ou aujourd'hui si en cours)
     */
    public boolean isEnRetard() {
        if (dateRetourReelle != null) {
            return dateRetourReelle.isAfter(dateRetourPrevue);
        } else {
            return LocalDate.now().isAfter(dateRetourPrevue);
        }
    }

    public double getPenalite() {
        return penalite;
    }

    public void setPenalite(double penalite) {
        this.penalite = penalite;
    }

    /**
     * Calcule le montant de la pénalité en fonction du nombre de jours de retard
     * Tarif : 1.0€ par jour de retard (défini dans cette méthode)
     * Note : Le manager utilise Constants.PENALITE_PAR_JOUR (0.50€)
     * @return Le montant de la pénalité
     */
    public double calculerPenalite() {
        if (isEnRetard()) {
            LocalDate dateFin = (dateRetourReelle != null) ? dateRetourReelle : LocalDate.now();
            long joursRetard = java.time.temporal.ChronoUnit.DAYS.between(dateRetourPrevue, dateFin);
            penalite = joursRetard * 1.0; // 1.0 unité monétaire par jour de retard
        }
        return penalite;
    }

    /**
     * Effectue le retour du document
     * Met à jour : date de retour réelle, statut, disponibilité, calcul de pénalité
     */
    public void retourner() {
        this.dateRetourReelle = LocalDate.now();
        this.enCours = false;
        this.document.setDisponible(true);
        calculerPenalite();
    }
}