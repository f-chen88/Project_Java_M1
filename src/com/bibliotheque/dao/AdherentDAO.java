package com.bibliotheque.dao;
import java.util.List;

import com.bibliotheque.model.Adherent;

/**
 * Interface DAO pour la gestion des adhérents en base de données
 * Définit les opérations CRUD (Create, Read, Update, Delete) standard
 */
public interface AdherentDAO {
    /**
     * Crée un nouvel adhérent en base de données
     * @return L'ID généré automatiquement
     */
    int create(Adherent adherent);
    
    /**
     * Recherche un adhérent par son ID
     * @return L'adhérent trouvé ou null
     */
    Adherent findById(int id);
    
    /**
     * Recherche des adhérents par nom ou prénom (recherche partielle)
     * @return Liste des adhérents correspondants
     */
    List<Adherent> findByNom(String nom);
    
    /**
     * Récupère tous les adhérents
     * @return Liste complète des adhérents
     */
    List<Adherent> findAll();
    
    /**
     * Met à jour un adhérent existant
     */
    void update(Adherent adherent);
    
    /**
     * Supprime un adhérent de la base
     */
    void delete(int id);
    
    /**
     * Vérifie si un adhérent existe avec cet ID
     * @return true si l'adhérent existe
     */
    boolean exists(int id);
}