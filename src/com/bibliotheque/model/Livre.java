package com.bibliotheque.model;

/**
 * Modèle représentant un livre
 * Hérite de Document et ajoute les attributs spécifiques : ISBN et nombre de pages
 */
public class Livre extends Document {
    String isbn;        // ISBN-13 (13 chiffres)
    int nbPages;

    /**
     * Constructeur avec ID (récupération depuis la base de données)
     */
    public Livre(int id, String titre, String auteur, String genre, String isbn, int nbPages) {
        super(id, titre, auteur, genre);
        this.isbn = isbn;
        this.nbPages = nbPages;
    }

    /**
     * Constructeur sans ID (création d'un nouveau livre)
     */
    public Livre(String titre, String auteur, String genre, String isbn, int nbPages) {
        super(titre, auteur, genre);
        this.isbn = isbn;
        this.nbPages = nbPages;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getNbPages() {
        return nbPages;
    }
}