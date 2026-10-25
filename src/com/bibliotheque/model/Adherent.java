package com.bibliotheque.model;

/**
 * Modèle représentant un adhérent de la bibliothèque
 * Gère les informations personnelles et le système de pénalités
 */
public class Adherent {
    int idAdherent;
    String nom;
    String prenom;
    String email;
    String telephone;
    boolean statutPenalite;      // Indique si l'adhérent a des pénalités en cours
    double montantPenalite;      // Montant total dû en €

    /**
     * Constructeur avec ID (pour récupération depuis la base de données)
     */
    public Adherent(int idAdherent, String nom, String prenom, String email, String telephone) {
        this.idAdherent = idAdherent;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.statutPenalite = false;
        this.montantPenalite = 0.0;
    }

    /**
     * Constructeur sans ID (pour création d'un nouvel adhérent)
     * L'ID sera assigné automatiquement lors de l'insertion en base
     */
    public Adherent(String nom, String prenom, String email, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.statutPenalite = false;
        this.montantPenalite = 0.0;
    }

    public int getIdAdherent() {
        return idAdherent;
    }

    public void setIdAdherent(int idAdherent) {
        this.idAdherent = idAdherent;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    /**
     * Vérifie si l'adhérent a des pénalités actives
     */
    public boolean hasPenalite() {
        return statutPenalite;
    }

    public void setStatutPenalite(boolean statut) {
        this.statutPenalite = statut;
    }

    public double getMontantPenalite() {
        return montantPenalite;
    }

    public void setMontantPenalite(double montant) {
        this.montantPenalite = montant;
    }

    /**
     * Ajoute une pénalité au compte de l'adhérent (retard d'emprunt)
     * Active automatiquement le statut de pénalité
     */
    public void ajouterPenalite(double montant) {
        this.montantPenalite += montant;
        this.statutPenalite = true;
    }

    /**
     * Enregistre un paiement de pénalité
     * Désactive le statut si le solde atteint 0
     */
    public void payerPenalite(double montant) {
        this.montantPenalite -= montant;
        if (this.montantPenalite <= 0) {
            this.montantPenalite = 0.0;
            this.statutPenalite = false;
        }
    }
}