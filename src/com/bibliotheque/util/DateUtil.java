package com.bibliotheque.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilitaire de gestion des dates et calculs liés aux emprunts
 */
public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Retourne la date actuelle
     */
    public static LocalDate aujourdhui() {
        return LocalDate.now();
    }
    
    /**
     * Calcule la date de retour prévue (date emprunt + durée standard)
     */
    public static LocalDate calculerDateRetourPrevue(LocalDate dateEmprunt) {
        return dateEmprunt.plusDays(Constants.DUREE_EMPRUNT_JOURS);
    }
    
    /**
     * Calcule le nombre de jours de retard (0 si pas de retard)
     * @param dateRetourReelle si null, utilise la date actuelle
     */
    public static long calculerJoursRetard(LocalDate dateRetourPrevue, LocalDate dateRetourReelle) {
        LocalDate dateComparaison = (dateRetourReelle != null) ? dateRetourReelle : aujourdhui();
        long jours = ChronoUnit.DAYS.between(dateRetourPrevue, dateComparaison);
        return Math.max(0, jours);
    }
    
    /**
     * Calcule la pénalité financière basée sur les jours de retard
     */
    public static double calculerPenalite(long joursRetard) {
        return joursRetard * Constants.PENALITE_PAR_JOUR;
    }
    
    /**
     * Vérifie si un emprunt est en retard
     */
    public static boolean estEnRetard(LocalDate dateRetourPrevue) {
        return aujourdhui().isAfter(dateRetourPrevue);
    }
    
    /**
     * Formate une date au format dd/MM/yyyy
     */
    public static String format(LocalDate date) {
        return date != null ? date.format(FORMATTER) : "";
    }
    
    /**
     * Parse une chaîne de caractères en LocalDate (format dd/MM/yyyy)
     */
    public static LocalDate parse(String dateStr) {
        return LocalDate.parse(dateStr, FORMATTER);
    }
}