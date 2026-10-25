=====================================
SYSTÈME DE GESTION DE BIBLIOTHÈQUE
=====================================

INFORMATIONS PROJET
-------------------
Auteur        : Franck CHEN
Formation     : M1 IDD
Année         : 2025-2026

Description   : Application de gestion d'emprunts pour bibliothèque
                avec interface graphique Swing et base SQLite embarquée

Documentation : Consultez user.pdf pour le manuel utilisateur complet

TABLE DES MATIÈRES
------------------
1. Installation et exécution ........... ligne 26
2. Fonctionnalités ..................... ligne 77
3. Données de test (optionnel) ......... ligne 114
4. Dépendances ......................... ligne 186
5. Compilation manuelle (optionnel) .... ligne 211
6. Dépannage ........................... ligne 279

=====================================
1. INSTALLATION ET EXÉCUTION
=====================================

PRÉREQUIS
---------
Java 17 ou supérieur

Vérifier votre version de Java :
    java -version

Si Java n'est pas installé :
    https://www.oracle.com/java/technologies/downloads/

STRUCTURE DU PROJET
-------------------
projet-bibliotheque/
├── README.txt                  ← Ce fichier
├── CHEN_Franck.jar             ← Application exécutable (JAR autonome)
├── donnees_test.sql            ← Données de test (optionnel)
├── user.pdf                    ← Manuel utilisateur
└── src/                        ← Code source
    └── com/
        └── bibliotheque/
            ├── dao/            ← Couche d'accès aux données
            ├── manager/        ← Logique métier et gestion BD
            ├── model/          ← Modèles (Document, Adhérent, Emprunt)
            ├── ui/             ← Interface graphique Swing
            ├── util/           ← Classes utilitaires
            └── exception/      ← Exceptions personnalisées

LANCER L'APPLICATION
--------------------

Méthode 1 : Ligne de commande (RECOMMANDÉ)
-------------------------------------------

Windows (CMD ou PowerShell) :
    cd chemin\vers\projet-bibliotheque
    java -jar CHEN_Franck.jar

macOS / Linux (Terminal) :
    cd /chemin/vers/projet-bibliotheque
    java -jar CHEN_Franck.jar

Méthode 2 : Double-clic
-----------------------
Double-cliquez sur CHEN_Franck.jar

Note : Si le double-clic ne fonctionne pas, utilisez la Méthode 1

=====================================
2. FONCTIONNALITÉS
=====================================

GESTION DES DOCUMENTS
---------------------
- Ajouter des documents (livres, magazines, CD)
- Modifier et supprimer des documents
- Rechercher des documents par titre, auteur, ou type
- Afficher le statut de disponibilité en temps réel

GESTION DES ADHÉRENTS
---------------------
- Ajouter et modifier des adhérents
- Consulter les amendes et pénalités
- Gérer les cotisations annuelles
- Visualiser l'historique des emprunts par adhérent

GESTION DES EMPRUNTS
--------------------
- Enregistrer un nouvel emprunt
- Enregistrer les retours de documents
- Calcul automatique des amendes pour les retards
- Blocage automatique des emprunts pour les adhérents en retard
- Historique complet des emprunts

FONCTIONNALITÉS TECHNIQUES
--------------------------
- Base de données SQLite embarquée (créée automatiquement au premier lancement)
- Interface graphique intuitive avec Java Swing
- Gestion automatique des dates d'échéance (21 jours par défaut)
- Calcul des pénalités pour les retards (0.50€ par jour)
- Vérification automatique de la disponibilité des documents
- Architecture en couches (DAO Pattern)
- Gestion des exceptions personnalisées
- Classes utilitaires pour les opérations communes

=====================================
3. DONNÉES DE TEST (OPTIONNEL)
=====================================

Le projet inclut un fichier "donnees_test.sql" avec des données d'exemple :
- Documents variés (livres, magazines, CD)
- Adhérents fictifs
- Emprunts en cours et historiques

CHARGER LES DONNÉES DE TEST
----------------------------

IMPORTANT : Fermez l'application avant de charger les données

Étape 1 : Ouvrez un terminal dans le dossier du projet

Étape 2 : Exécutez la commande appropriée

    Windows (Command Prompt ou PowerShell) :
        sqlite3 bibliotheque.db < donnees_test.sql
        
        OU si vous avez placé sqlite3.exe dans le dossier du projet :
        .\sqlite3.exe bibliotheque.db < donnees_test.sql
        OU : 
        .\sqlite3 bibliotheque.db ".read donnees_test.sql"

    macOS / Linux :
        sqlite3 bibliotheque.db < donnees_test.sql
        ou 
        sqlite3 bibliotheque.db ".read donnees_test.sql"

Étape 3 : Lancez l'application
    java -jar CHEN_Franck.jar

UTILISER L'APPLICATION SANS DONNÉES DE TEST
--------------------------------------------
Vous pouvez lancer l'application directement et ajouter vos propres données
via l'interface graphique. Une base de données vide sera créée automatiquement.

RÉINITIALISER LA BASE DE DONNÉES
---------------------------------
Pour repartir d'une base vierge :

1. Fermez complètement l'application
2. Supprimez le fichier "bibliotheque.db"
3. Relancez l'application (une nouvelle base vide sera créée)
4. (Optionnel) Rechargez les données de test avec la commande ci-dessus

Ou bien : 

1. Cliquez sur le bouton "Base de données" en haut de l'interface
2. Puis cliquez sur "Réinitialiser (DANGER)"
3. Confirmez en cliquant sur le bouton "Oui"

INSTALLATION DE SQLITE3 (si nécessaire)
----------------------------------------
Si vous obtenez l'erreur "sqlite3 n'est pas reconnu" :

    Windows :
        1. Téléchargez sqlite-tools depuis https://www.sqlite.org/download.html
        2. Extrayez le fichier ZIP
        3. Placez sqlite3.exe dans le dossier du projet
           OU ajoutez-le au PATH Windows
    
    macOS :
        sqlite3 est préinstallé (aucune action nécessaire)
    
    Linux :
        sudo apt install sqlite3      (Debian/Ubuntu)
        sudo dnf install sqlite       (Fedora/RHEL)
        sudo pacman -S sqlite         (Arch Linux)

=====================================
4. DÉPENDANCES
=====================================

PRÉREQUIS SYSTÈME
-----------------
- Java 17 ou supérieur (JRE ou JDK)

BIBLIOTHÈQUES EXTERNES (incluses dans le JAR)
----------------------------------------------
- SQLite JDBC Driver 3.50.3.0    (Accès base de données SQLite)
- SLF4J API 2.0.9                (Framework de logging)
- SLF4J Simple 2.0.9             (Implémentation simple de SLF4J)

BIBLIOTHÈQUES JAVA STANDARD UTILISÉES
--------------------------------------
- javax.swing          (Interface graphique)
- java.sql             (Accès base de données JDBC)
- java.time            (Gestion des dates et durées)
- java.util            (Collections, listes, maps)
- java.io              (Entrées/sorties fichiers)

Note : Toutes les dépendances externes sont intégrées dans le JAR exécutable.
       Aucune installation supplémentaire n'est nécessaire.

=====================================
5. COMPILATION MANUELLE (OPTIONNEL)
=====================================

Si vous souhaitez recompiler le projet à partir du code source :

ÉTAPE 1 : Télécharger les dépendances
--------------------------------------
Créez un dossier "lib/" à la racine du projet et téléchargez :

1. sqlite-jdbc-3.50.3.0.jar
   https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.50.3.0/sqlite-jdbc-3.50.3.0.jar

2. slf4j-api-2.0.9.jar
   https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

3. slf4j-simple-2.0.9.jar
   https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar

ÉTAPE 2 : Compiler le code source
----------------------------------

Windows (Command Prompt ou PowerShell) :
    mkdir bin
    javac --release 17 -cp "lib\sqlite-jdbc-3.50.3.0.jar;lib\slf4j-api-2.0.9.jar;lib\slf4j-simple-2.0.9.jar" -d bin -encoding UTF-8 src\com\bibliotheque\dao\*.java src\com\bibliotheque\model\*.java src\com\bibliotheque\manager\*.java src\com\bibliotheque\ui\*.java src\com\bibliotheque\util\*.java src\com\bibliotheque\exception\*.java

macOS / Linux (Terminal) :
    mkdir -p bin
    javac --release 17 -cp "lib/sqlite-jdbc-3.50.3.0.jar:lib/slf4j-api-2.0.9.jar:lib/slf4j-simple-2.0.9.jar" -d bin -encoding UTF-8 $(find src -name "*.java")

ÉTAPE 3 : Créer le JAR exécutable standalone
---------------------------------------------

Windows :
    cd bin
    jar xf ..\lib\sqlite-jdbc-3.50.3.0.jar
    jar xf ..\lib\slf4j-api-2.0.9.jar
    jar xf ..\lib\slf4j-simple-2.0.9.jar
    cd ..

    echo Manifest-Version: 1.0 > MANIFEST.MF
    echo Main-Class: com.bibliotheque.ui.MainApplication >> MANIFEST.MF
    echo. >> MANIFEST.MF

    jar cfm CHEN_Franck.jar MANIFEST.MF -C bin .

macOS / Linux :
    cd bin
    jar xf ../lib/sqlite-jdbc-3.50.3.0.jar
    jar xf ../lib/slf4j-api-2.0.9.jar
    jar xf ../lib/slf4j-simple-2.0.9.jar
    cd ..

    cat > MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: com.bibliotheque.ui.MainApplication

EOF

    jar cfm CHEN_Franck.jar MANIFEST.MF -C bin .

ÉTAPE 4 : Tester le JAR créé
-----------------------------
    java -jar CHEN_Franck.jar

Si tout fonctionne correctement, l'application devrait se lancer avec
l'interface graphique.

=====================================
6. DÉPANNAGE
=====================================

PROBLÈME : "Error: Unable to access jarfile CHEN_Franck.jar"
-------------------------------------------------------------
Solution :
    - Vérifiez que vous êtes dans le bon dossier (cd chemin/vers/projet)
    - Vérifiez que le fichier CHEN_Franck.jar existe bien
    - Vérifiez les permissions du fichier (chmod +x sur Linux/macOS)

PROBLÈME : "java: command not found" ou "java n'est pas reconnu"
-----------------------------------------------------------------
Solution :
    - Java n'est pas installé ou pas dans le PATH
    - Installez Java 17+ depuis https://www.oracle.com/java/technologies/downloads/
    - Vérifiez l'installation avec : java -version

PROBLÈME : "Unsupported class file major version" ou erreur de version
-----------------------------------------------------------------------
Solution :
    - Votre version de Java est trop ancienne (< Java 17)
    - Installez Java 17 ou supérieur
    - Vérifiez avec : java -version

PROBLÈME : Base de données corrompue ou erreurs SQL
----------------------------------------------------
Solution :
    1. Fermez complètement l'application
    2. Supprimez le fichier bibliotheque.db
    3. Relancez l'application (une nouvelle base sera créée)
    4. Rechargez les données de test si nécessaire

PROBLÈME : Erreurs d'encodage (caractères accentués mal affichés)
------------------------------------------------------------------
Solution :
    - L'application utilise UTF-8 par défaut
    - Sur Windows, vérifiez que votre terminal supporte UTF-8
    - Lancez avec : java -Dfile.encoding=UTF-8 -jar CHEN_Franck.jar

PROBLÈME : Interface graphique ne s'affiche pas correctement
-------------------------------------------------------------
Solution :
    - Vérifiez que Java Swing est supporté sur votre système
    - Sur Linux sans environnement graphique, Swing ne fonctionnera pas
    - Essayez de lancer avec : java -Dsun.java2d.uiScale=1.0 -jar CHEN_Franck.jar

CONTACT ET SUPPORT
------------------
Pour toute question ou problème non résolu :
    - Consultez le manuel utilisateur (user.pdf)
    - Vérifiez la documentation du code source (dossier src/)
    - Contactez l'auteur : Franck CHEN

=====================================
NOTES TECHNIQUES
=====================================

Architecture    : Pattern DAO, séparation en couches (DAO, Manager, Model, UI)
Base de données : SQLite embarquée (fichier bibliotheque.db créé automatiquement)
Interface       : Java Swing (composants graphiques standards)
Version Java    : Compilé avec Java 17, compatible Java 17+
Encodage        : UTF-8 pour tous les fichiers sources et données

Règles métier implémentées :
    - Durée d'emprunt : 21 jours par défaut
    - Amende de retard : 0.50€ par jour de retard
    - Blocage automatique : Un adhérent avec un retour en retard ne peut pas emprunter
    - Limite d'emprunts : Configurable par adhérent
    - Vérification de disponibilité : Automatique avant chaque emprunt

Sécurité :
    - Utilisation de PreparedStatement pour éviter les injections SQL
    - Gestion des exceptions avec messages explicites
    - Validation des données avant insertion en base

Performance :
    - Connexion pooling pour optimiser les accès base de données
    - Index sur les colonnes fréquemment recherchées
    - Chargement lazy des données volumineuses

=====================================
FIN DU FICHIER README.txt
=====================================