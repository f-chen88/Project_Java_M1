package com.bibliotheque.util;

import java.util.regex.Pattern;

/**
 * Utilitaire de validation des données d'entrée
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern ISBN_PATTERN = Pattern.compile("^\\d{13}$");
    
    /**
     * Vérifie qu'une chaîne n'est ni null ni vide (après trim)
     */
    public static boolean estNonVide(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Valide le format d'une adresse email
     */
    public static boolean estEmailValide(String email) {
        return estNonVide(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Valide un ISBN-13 (13 chiffres, espaces et tirets ignorés)
     */
    public static boolean estIsbnValide(String isbn) {
        return estNonVide(isbn) && ISBN_PATTERN.matcher(isbn.replaceAll("[\\s-]", "")).matches();
    }
    
    /**
     * Vérifie qu'un champ n'est pas vide, lance une exception sinon
     * @throws IllegalArgumentException si le champ est vide
     */
    public static void verifierNonVide(String str, String nomChamp) {
        if (!estNonVide(str)) {
            throw new IllegalArgumentException(nomChamp + " ne peut pas être vide");
        }
    }
    
    /**
     * Vérifie qu'un nombre est positif ou zéro, lance une exception sinon
     * @throws IllegalArgumentException si le nombre est négatif
     */
    public static void verifierPositif(int nombre, String nomChamp) {
        if (nombre < 0) {
            throw new IllegalArgumentException(nomChamp + " ne peut pas être négatif");
        }
    }
}