package com.bibliotheque.exception;

/**
 * Exception personnalisée pour les erreurs liées aux documents
 * Utilisée pour signaler des problèmes lors de :
 * - Ajout (titre manquant, ID déjà existant)
 * - Recherche (document introuvable, aucun résultat)
 * - Modification (document inexistant)
 * - Suppression (document emprunté)
 */
public class DocumentException extends Exception {
    
    /**
     * Constructeur avec message d'erreur
     * @param message Description de l'erreur
     */
    public DocumentException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause sous-jacente
     * @param message Description de l'erreur
     * @param cause Exception originale qui a causé cette erreur
     */
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}