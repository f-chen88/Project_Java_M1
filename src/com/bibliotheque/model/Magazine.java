package com.bibliotheque.model;

/**
 * Modèle représentant un magazine
 * Hérite de Document et ajoute les attributs spécifiques : périodicité et numéro
 */
public class Magazine extends Document {
    String periodicite;    // Ex: "Mensuel", "Hebdomadaire", "Trimestriel"
    int numero;            // Numéro de l'édition

    /**
     * Constructeur avec ID (récupération depuis la base de données)
     */
    public Magazine(int id, String titre, String auteur, String genre, String periodicite, int numero) {
        super(id, titre, auteur, genre);
        this.periodicite = periodicite;
        this.numero = numero;
    }

    /**
     * Constructeur sans ID (création d'un nouveau magazine)
     */
    public Magazine(String titre, String auteur, String genre, String periodicite, int numero) {
        super(titre, auteur, genre);
        this.periodicite = periodicite;
        this.numero = numero;
    }

    public String getPeriodicite() {
        return periodicite;
    }
    
    public int getNumero() {
        return numero;
    }
}