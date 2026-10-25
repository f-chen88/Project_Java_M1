package com.bibliotheque.util;

/**
 * Constantes de configuration du système de bibliothèque
 */
public class Constants {
    // Règles d'emprunt
    public static final int MAX_EMPRUNTS_PAR_ADHERENT = 5;
    public static final int DUREE_EMPRUNT_JOURS = 21; // 3 semaines
    public static final double PENALITE_PAR_JOUR = 0.50; // 0,50€ par jour  
    
    // Configuration base de données
    public static final String DB_URL = "jdbc:sqlite:resources/database/bibliotheque.db";
}