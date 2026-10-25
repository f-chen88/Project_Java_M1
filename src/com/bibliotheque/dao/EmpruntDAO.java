package com.bibliotheque.dao;

import java.util.List;

import com.bibliotheque.model.Emprunt;

/**
 * Interface DAO pour la gestion des emprunts en base de données
 * Gère les opérations CRUD et les requêtes métier spécifiques
 * Note : Pas de delete() car les emprunts ne sont jamais supprimés (historique)
 */
public interface EmpruntDAO {
    /**
     * Crée un nouvel emprunt en base
     * @return L'ID généré automatiquement
     */
    int create(Emprunt emprunt);
    
    /**
     * Recherche un emprunt par son ID
     * @return L'emprunt trouvé avec Document et Adherent chargés
     */
    Emprunt findById(int id);
    
    /**
     * Récupère tous les emprunts (historique complet)
     * @return Liste complète des emprunts
     */
    List<Emprunt> findAll();
    
    /**
     * Met à jour un emprunt existant (pour le retour notamment)
     */
    void update(Emprunt emprunt);
    
    /**
     * Récupère l'historique des emprunts d'un adhérent
     * @param id ID de l'adhérent
     * @return Liste des emprunts de cet adhérent
     */
    List<Emprunt> findByAdherent(int id);
    
    /**
     * Récupère tous les emprunts actuellement en cours
     * @return Liste des emprunts non retournés
     */
    List<Emprunt> findEnCours();
    
    /**
     * Récupère tous les emprunts en retard (date prévue dépassée)
     * @return Liste des emprunts en retard
     */
    List<Emprunt> findEnRetard();
}