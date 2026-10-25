package com.bibliotheque.util;

import java.time.LocalDate;

/**
 * Utilitaire de gestion des alertes et messages pour la bibliothèque
 */
public class AlerteUtil {
    
    /**
     * Génère un message d'emprunt avec la date de retour prévue
     */
    public static String messageEmprunt(String titre, LocalDate dateRetour) {
        return String.format("Emprunt: '%s' - À rendre le %s", titre, DateUtil.format(dateRetour));
    }
    
    /**
     * Génère un message de retour avec calcul de pénalité si retard
     */
    public static String messageRetour(String titre, long joursRetard, double penalite) {
        if (joursRetard > 0) {
            return String.format("Retour: '%s' - Retard: %d jours - Pénalité: %.2f€", 
                                titre, joursRetard, penalite);
        }
        return String.format("Retour: '%s' - Aucune pénalité", titre);
    }
    
    /**
     * Génère une alerte de retard pour un adhérent
     */
    public static String messageRetard(String nomAdherent, String titre, long jours) {
        return String.format("RETARD - %s - '%s' - %d jour(s)", nomAdherent, titre, jours);
    }
    
    /**
     * Vérifie si un adhérent peut emprunter (limite d'emprunts et pénalités)
     */
    public static boolean peutEmprunter(int nbEmprunts, double penalites) {
        return nbEmprunts < Constants.MAX_EMPRUNTS_PAR_ADHERENT && penalites < 50.0;
    }
}