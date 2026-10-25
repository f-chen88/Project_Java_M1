package com.bibliotheque.exception;

/**
 * Exception personnalisée pour les erreurs liées aux emprunts
 * Utilisée pour signaler des problèmes lors de :
 * - Emprunt (document indisponible, adhérent bloqué, limite atteinte)
 * - Retour (emprunt introuvable, déjà retourné)
 * - Propagation des erreurs de Document/Adherent vers la couche supérieure
 */
public class EmpruntException extends Exception {
    
    /**
     * Constructeur avec message d'erreur
     * @param message Description de l'erreur
     */
    public EmpruntException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause sous-jacente
     * @param message Description de l'erreur
     * @param cause Exception originale qui a causé cette erreur
     */
    public EmpruntException(String message, Throwable cause) {
        super(message, cause);
    }
}