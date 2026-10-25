-- ========================================
-- FICHIER DE TEST COMPLET
-- Système de Gestion des Emprunts
-- ========================================

-- Nettoyer les données existantes
DELETE FROM emprunts;
DELETE FROM documents;
DELETE FROM adherents;
DELETE FROM sqlite_sequence;

PRAGMA foreign_keys = ON;

-- ========================================
-- 1. AJOUTER DES DOCUMENTS (12 documents)
-- ========================================

-- Livres (6)
INSERT INTO documents (titre, auteur, genre, disponible, type, isbn, nombre_pages)
VALUES 
('Le Seigneur des Anneaux', 'J.R.R. Tolkien', 'Fantasy', 1, 'Livre', '978-2-07-061332-8', 1200),
('Harry Potter à l''école des sorciers', 'J.K. Rowling', 'Fantasy', 1, 'Livre', '978-2-07-054120-0', 320),
('1984', 'George Orwell', 'Science-fiction', 1, 'Livre', '978-2-07-036822-8', 400),
('Le Petit Prince', 'Antoine de Saint-Exupéry', 'Conte', 1, 'Livre', '978-2-07-061275-8', 96),
('Les Misérables', 'Victor Hugo', 'Roman', 1, 'Livre', '978-2-253-09633-1', 1900),
('L''Étranger', 'Albert Camus', 'Roman', 1, 'Livre', '978-2-07-036002-4', 186);

-- Magazines (3)
INSERT INTO documents (titre, auteur, genre, disponible, type, numero, periodicite)
VALUES 
('Science & Vie', 'Collectif', 'Science', 1, 'Magazine', 1234, 'Mensuel'),
('Le Monde Diplomatique', 'Collectif', 'Actualités', 1, 'Magazine', 850, 'Mensuel'),
('National Geographic', 'Collectif', 'Nature', 1, 'Magazine', 567, 'Mensuel');

-- CDs (3)
INSERT INTO documents (titre, auteur, genre, disponible, type, duree, artiste)
VALUES 
('The Dark Side of the Moon', 'Pink Floyd', 'Rock', 1, 'CD', 43, 'Pink Floyd'),
('Abbey Road', 'The Beatles', 'Rock', 1, 'CD', 47, 'The Beatles'),
('Thriller', 'Michael Jackson', 'Pop', 1, 'CD', 42, 'Michael Jackson');

-- ========================================
-- 2. AJOUTER DES ADHÉRENTS (6 adhérents)
-- ========================================

INSERT INTO adherents (nom, prenom, email, telephone, statut_penalite, montant_penalite)
VALUES 
-- Adhérent normal (ID 1)
('Dupont', 'Jean', 'jean.dupont@email.com', '0123456789', 0, 0.0),

-- Adhérent normal (ID 2)
('Martin', 'Marie', 'marie.martin@email.com', '0987654321', 0, 0.0),

-- Adhérent avec pénalité (ID 3) - Pour tester le blocage
('Dubois', 'Pierre', 'pierre.dubois@email.com', '0612345678', 1, 4.5),

-- Adhérent normal (ID 4)
('Leroy', 'Sophie', 'sophie.leroy@email.com', '0698765432', 0, 0.0),

-- Adhérent qui va atteindre la limite (ID 5)
('Bernard', 'Luc', 'luc.bernard@email.com', '0656789012', 0, 0.0),

-- Adhérent normal (ID 6)
('Petit', 'Emma', 'emma.petit@email.com', '0634567890', 0, 0.0);

-- ========================================
-- 3. CRÉER DES EMPRUNTS - SCÉNARIOS DE TEST
-- ========================================

-- SCÉNARIO 1 : Emprunt en cours NORMAL (pas de retard)
-- Jean Dupont emprunte "Le Petit Prince" il y a 5 jours
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES (4, 1, date('now', '-5 days'), date('now', '+16 days'), NULL, 0.0, 1);

-- Mettre le document en non disponible
UPDATE documents SET disponible = 0 WHERE id = 4;

-- SCÉNARIO 2 : Emprunt EN RETARD (9 jours de retard)
-- Pierre Dubois emprunte "Le Seigneur des Anneaux" il y a 30 jours (retour prévu il y a 9 jours)
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES (1, 3, date('now', '-30 days'), date('now', '-9 days'), NULL, 0.0, 1);

-- Mettre le document en non disponible
UPDATE documents SET disponible = 0 WHERE id = 1;

-- SCÉNARIO 3 : Emprunt TERMINÉ avec pénalité
-- Pierre Dubois a emprunté et retourné "Harry Potter" avec 9 jours de retard (pénalité : 4.5€)
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES (2, 3, date('now', '-45 days'), date('now', '-36 days'), date('now', '-27 days'), 4.5, 0);

-- Le document est redevenu disponible (retourné)
UPDATE documents SET disponible = 1 WHERE id = 2;

-- SCÉNARIO 4 : Emprunt TERMINÉ à temps (pas de pénalité)
-- Marie Martin a emprunté et retourné "1984" à temps
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES (3, 2, date('now', '-60 days'), date('now', '-39 days'), date('now', '-42 days'), 0.0, 0);

-- Le document est disponible
UPDATE documents SET disponible = 1 WHERE id = 3;

-- SCÉNARIO 5 : Adhérent avec 5 emprunts (LIMITE ATTEINTE)
-- Luc Bernard a 5 emprunts en cours (pour tester le blocage)
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES 
(5, 5, date('now', '-3 days'), date('now', '+18 days'), NULL, 0.0, 1),
(6, 5, date('now', '-3 days'), date('now', '+18 days'), NULL, 0.0, 1),
(7, 5, date('now', '-3 days'), date('now', '+18 days'), NULL, 0.0, 1),
(8, 5, date('now', '-3 days'), date('now', '+18 days'), NULL, 0.0, 1),
(9, 5, date('now', '-3 days'), date('now', '+18 days'), NULL, 0.0, 1);

-- Mettre ces documents en non disponible
UPDATE documents SET disponible = 0 WHERE id IN (5, 6, 7, 8, 9);

-- SCÉNARIO 6 : Emprunt très en retard (30 jours) - Grosse pénalité
-- Sophie Leroy a un emprunt très en retard
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES (10, 4, date('now', '-51 days'), date('now', '-30 days'), NULL, 0.0, 1);

-- Mettre le document en non disponible
UPDATE documents SET disponible = 0 WHERE id = 10;

-- SCÉNARIO 7 : Emprunt récent (1 jour) - Encore beaucoup de temps
-- Emma Petit vient d'emprunter
INSERT INTO emprunts (id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle, penalite, en_cours)
VALUES (11, 6, date('now', '-1 days'), date('now', '+20 days'), NULL, 0.0, 1);

-- Mettre le document en non disponible
UPDATE documents SET disponible = 0 WHERE id = 11;

-- ========================================
-- 4. AFFICHER LES RÉSULTATS
-- ========================================

.mode column
.headers on
.width 5 30 20 10 8

SELECT '' as '';
SELECT '========================================' as '';
SELECT '   RÉSULTATS DES TESTS' as '';
SELECT '========================================' as '';

-- Statistiques générales
SELECT '' as '';
SELECT '=== STATISTIQUES GÉNÉRALES ===' as '';
SELECT COUNT(*) as "Total Documents" FROM documents;
SELECT COUNT(*) as "Docs Disponibles" FROM documents WHERE disponible = 1;
SELECT COUNT(*) as "Docs Empruntés" FROM documents WHERE disponible = 0;
SELECT COUNT(*) as "Total Adhérents" FROM adherents;
SELECT COUNT(*) as "Adhérents avec pénalités" FROM adherents WHERE statut_penalite = 1;
SELECT COUNT(*) as "Total Emprunts" FROM emprunts;
SELECT COUNT(*) as "Emprunts en cours" FROM emprunts WHERE en_cours = 1;
SELECT COUNT(*) as "Emprunts terminés" FROM emprunts WHERE en_cours = 0;
SELECT COUNT(*) as "Emprunts EN RETARD" FROM emprunts WHERE en_cours = 1 AND date_retour_prevue < date('now');

-- Liste des documents
SELECT '' as '';
SELECT '=== DOCUMENTS ===' as '';
.width 5 40 20 15 12
SELECT 
    id as "ID",
    titre as "Titre",
    auteur as "Auteur",
    type as "Type",
    CASE WHEN disponible = 1 THEN '✓ Dispo' ELSE '✗ Emprunté' END as "Statut"
FROM documents
ORDER BY disponible DESC, type, titre;

-- Liste des adhérents
SELECT '' as '';
SELECT '=== ADHÉRENTS ===' as '';
.width 5 15 15 30 18 12
SELECT 
    id_adherent as "ID",
    nom as "Nom",
    prenom as "Prénom",
    email as "Email",
    CASE WHEN statut_penalite = 1 THEN '⚠️ Oui' ELSE '✓ Non' END as "Pénalité",
    montant_penalite || ' €' as "Montant"
FROM adherents
ORDER BY statut_penalite DESC, nom;

-- Emprunts en cours
SELECT '' as '';
SELECT '=== EMPRUNTS EN COURS ===' as '';
.width 5 30 20 12 12 15
SELECT 
    e.id_emprunt as "ID",
    d.titre as "Document",
    a.prenom || ' ' || a.nom as "Adhérent",
    e.date_emprunt as "Emprunt",
    e.date_retour_prevue as "Retour prévu",
    CASE 
        WHEN e.date_retour_prevue < date('now') THEN '⚠️ EN RETARD'
        ELSE '✓ En cours'
    END as "Statut"
FROM emprunts e
JOIN documents d ON e.id_document = d.id
JOIN adherents a ON e.id_adherent = a.id_adherent
WHERE e.en_cours = 1
ORDER BY e.date_retour_prevue;

-- Emprunts en retard avec calcul des jours
SELECT '' as '';
SELECT '=== EMPRUNTS EN RETARD (DÉTAILS) ===' as '';
.width 30 20 12 15 10
SELECT 
    d.titre as "Document",
    a.prenom || ' ' || a.nom as "Adhérent",
    e.date_retour_prevue as "Retour prévu",
    CAST(julianday('now') - julianday(e.date_retour_prevue) AS INTEGER) as "Jours retard",
    CAST(julianday('now') - julianday(e.date_retour_prevue) AS INTEGER) * 0.50 || ' €' as "Pénalité"
FROM emprunts e
JOIN documents d ON e.id_document = d.id
JOIN adherents a ON e.id_adherent = a.id_adherent
WHERE e.en_cours = 1 AND e.date_retour_prevue < date('now')
ORDER BY e.date_retour_prevue;

-- Emprunts terminés
SELECT '' as '';
SELECT '=== EMPRUNTS TERMINÉS ===' as '';
.width 5 30 20 12 12 12 10
SELECT 
    e.id_emprunt as "ID",
    d.titre as "Document",
    a.prenom || ' ' || a.nom as "Adhérent",
    e.date_emprunt as "Emprunt",
    e.date_retour_prevue as "Retour prévu",
    e.date_retour_reelle as "Retour réel",
    e.penalite || ' €' as "Pénalité"
FROM emprunts e
JOIN documents d ON e.id_document = d.id
JOIN adherents a ON e.id_adherent = a.id_adherent
WHERE e.en_cours = 0
ORDER BY e.date_retour_reelle DESC;

-- Historique d'un adhérent spécifique
SELECT '' as '';
SELECT '=== HISTORIQUE : Pierre Dubois (avec pénalité) ===' as '';
.width 30 12 12 12 10 15
SELECT 
    d.titre as "Document",
    e.date_emprunt as "Emprunt",
    e.date_retour_prevue as "Retour prévu",
    e.date_retour_reelle as "Retour réel",
    e.penalite || ' €' as "Pénalité",
    CASE WHEN e.en_cours = 1 THEN 'EN COURS' ELSE 'Terminé' END as "Statut"
FROM emprunts e
JOIN documents d ON e.id_document = d.id
WHERE e.id_adherent = 3
ORDER BY e.date_emprunt DESC;

-- Nombre d'emprunts par adhérent
SELECT '' as '';
SELECT '=== NOMBRE D''EMPRUNTS PAR ADHÉRENT ===' as '';
.width 20 15 18
SELECT 
    a.prenom || ' ' || a.nom as "Adhérent",
    COUNT(*) as "Emprunts actifs",
    CASE 
        WHEN COUNT(*) >= 5 THEN '⚠️ LIMITE'
        ELSE '✓ OK'
    END as "Statut"
FROM adherents a
LEFT JOIN emprunts e ON a.id_adherent = e.id_adherent AND e.en_cours = 1
GROUP BY a.id_adherent
ORDER BY COUNT(*) DESC;

SELECT '' as '';
SELECT '========================================' as '';
SELECT '   SCÉNARIOS DE TEST CRÉÉS :' as '';
SELECT '========================================' as '';
SELECT '1. Emprunt normal en cours' as '';
SELECT '2. Emprunt en retard (9 jours)' as '';
SELECT '3. Emprunt terminé avec pénalité' as '';
SELECT '4. Emprunt terminé à temps' as '';
SELECT '5. Adhérent à la limite (5 emprunts)' as '';
SELECT '6. Emprunt très en retard (30 jours)' as '';
SELECT '7. Emprunt récent (1 jour)' as '';
SELECT '' as '';
SELECT 'TESTS À FAIRE DANS L''APPLICATION :' as '';
SELECT '- Jean Dupont peut encore emprunter (1/5)' as '';
SELECT '- Pierre Dubois NE PEUT PAS emprunter (pénalité)' as '';
SELECT '- Luc Bernard NE PEUT PAS emprunter (5/5)' as '';
SELECT '- Le Petit Prince N''EST PAS disponible' as '';
SELECT '- Retourner "Le Seigneur des Anneaux" = 4.50€' as '';
SELECT '- Retourner "The Dark Side..." (Sophie) = 15.00€' as '';
SELECT '========================================' as '';