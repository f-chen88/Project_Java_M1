package com.bibliotheque.dao;
import java.util.List;

import com.bibliotheque.model.Document;

/**
 * Interface DAO pour la gestion des documents en base de données
 * Gère les 3 types de documents : Livre, Magazine, CD
 * Utilise le pattern "Single Table Inheritance" (tous les types dans une table)
 */
public interface DocumentDAO {
    /**
     * Crée un nouveau document en base (Livre, Magazine ou CD)
     * @return L'ID généré automatiquement
     */
    int create(Document document);
    
    /**
     * Recherche un document par son ID
     * @return Le document trouvé (type spécifique) ou null
     */
    Document findById(int id);
    
    /**
     * Recherche des documents par titre (recherche partielle)
     * @return Liste des documents correspondants
     */
    List<Document> findByTitre(String titre);
    
    /**
     * Recherche des documents par auteur (recherche partielle)
     * @return Liste des documents correspondants
     */
    List<Document> findByAuteur(String auteur);
    
    /**
     * Recherche des documents par genre (recherche partielle)
     * @return Liste des documents correspondants
     */
    List<Document> findByGenre(String genre);
    
    /**
     * Récupère tous les documents (tous types confondus)
     * @return Liste complète des documents
     */
    List<Document> findAll();
    
    /**
     * Met à jour un document existant
     */
    void update(Document document);
    
    /**
     * Supprime un document de la base
     */
    void delete(int id);
    
    /**
     * Vérifie si un document existe avec cet ID
     * @return true si le document existe
     */
    boolean exists(int id);
}