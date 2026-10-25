package com.bibliotheque.exception;

/**
 * Exception personnalisée pour les erreurs liées aux adhérents
 * Utilisée pour signaler des problèmes lors de :
 * - Inscription (données manquantes, email déjà existant)
 * - Modification (adhérent introuvable)
 * - Suppression (emprunts en cours)
 * - Validation des emprunts (pénalités impayées, limite atteinte)
 */
public class AdherentException extends Exception {
    
    /**
     * Constructeur avec message d'erreur
     * @param message Description de l'erreur
     */
    public AdherentException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause sous-jacente
     * @param message Description de l'erreur
     * @param cause Exception originale qui a causé cette erreur
     */
    public AdherentException(String message, Throwable cause) {
        super(message, cause);
    }
}