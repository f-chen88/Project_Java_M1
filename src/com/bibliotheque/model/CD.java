package com.bibliotheque.model;

/**
 * Modèle représentant un CD audio
 * Hérite de Document et ajoute les attributs spécifiques : durée et artiste
 */
public class CD extends Document {
    int duree;        // Durée en minutes
    String artiste;   // Nom de l'artiste ou du groupe

    /**
     * Constructeur avec ID (récupération depuis la base de données)
     */
    public CD(int id, String titre, String auteur, String genre, int duree, String artiste) {
        super(id, titre, auteur, genre);
        this.duree = duree;
        this.artiste = artiste;
    }

    /**
     * Constructeur sans ID (création d'un nouveau CD)
     */
    public CD(String titre, String auteur, String genre, int duree, String artiste) {
        super(titre, auteur, genre);
        this.duree = duree;
        this.artiste = artiste;
    }

    public int getDuree() {
        return duree;
    }
    
    public String getArtiste() {
        return artiste;
    }
}