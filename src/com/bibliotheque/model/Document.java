package com.bibliotheque.model;

/**
 * Classe abstraite représentant un document de bibliothèque
 * Classe parent pour Livre, Magazine et CD
 * Gère les attributs communs : titre, auteur, genre, disponibilité
 */
public abstract class Document {
    int id;
    String titre;
    String auteur;
    String genre;
    boolean disponible;    // true si le document peut être emprunté

    /**
     * Constructeur avec ID (pour récupération depuis la base de données)
     */
    public Document(int id, String titre, String auteur, String genre) {
        this.id = id;
        this.titre = titre;
        this.auteur = auteur;
        this.genre = genre;
        this.disponible = true;
    }

    /**
     * Constructeur sans ID (pour création d'un nouveau document)
     * L'ID sera assigné automatiquement lors de l'insertion en base
     */
    public Document(String titre, String auteur, String genre) {
        this.titre = titre;
        this.auteur = auteur;
        this.genre = genre;
        this.disponible = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public String getAuteur() {
        return auteur;
    }

    public String getGenre() {
        return genre;
    }

    /**
     * Vérifie si le document peut être emprunté
     */
    public boolean isDisponible() {
        return disponible;
    }

    /**
     * Modifie la disponibilité (false lors d'un emprunt, true lors du retour)
     */
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Document [id=" + id + ", titre=" + titre + ", auteur=" + auteur + 
               ", genre=" + genre + ", disponible=" + disponible + "]";
    }
}