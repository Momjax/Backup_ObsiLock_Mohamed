# Compte-Rendu : Projet ObsiLock (Coffre-Fort Numérique)

Ce document résume l'ensemble des travaux, des choix architecturaux et des corrections apportées au projet **ObsiLock**. Il vous servira de référence complète pour votre **oral de présentation**.

---

## 1. Architecture Globale du Projet

Le projet ObsiLock repose sur une architecture moderne de type **Client/Serveur** :
*   **Le Serveur (Backend API)** : Développé en **PHP** avec le micro-framework **Slim**. Il s'occupe de la logique métier (Authentification, gestion de la base de données, chiffrement/déchiffrement des fichiers). Il est conteneurisé grâce à **Docker** et exposé derrière un reverse proxy **Traefik**.
*   **Le Client Lourd (Frontend)** : Une application de bureau développée en **Java / JavaFX**. Ce client interagit avec l'API serveur via des requêtes HTTP REST. Il affiche l'interface utilisateur, gère la navigation dans l'arborescence des dossiers et déclenche les téléchargements.

*(Note : Au cours du développement, une interface web "Single Page Application" en HTML/JS a temporairement été créée dans `public/ui/` pour illustrer le design, avant de réaliser que le cahier des charges exigeait expressément un client lourd JavaFX. Ce design a ensuite été porté sur le JavaFX).*

---

## 2. Le Backend (API PHP Slim)

Le cœur de la sécurité d'ObsiLock se trouve du côté serveur.

### A. Sécurisation et Authentification (JWT)
L'authentification ne se base pas sur des sessions PHP classiques (qui poseraient problème avec une API stateless), mais sur des **JSON Web Tokens (JWT)**.
*   **Connexion (`AuthController`)** : L'utilisateur envoie son email et mot de passe. Le serveur vérifie le hash en base de données avec `password_verify()`. Si c'est valide, un token JWT signé secrètement par le serveur est généré et renvoyé au client Java.
*   **Sécurisation des routes** : Un *Middleware* Slim vérifie que le header `Authorization: Bearer <token>` est présent pour chaque requête critique (Upload, Suppression, Listage).

### B. Le Chiffrement des Fichiers (Module `FileCrypto`)
C'est le point fort technique d'ObsiLock. Les fichiers ne sont **jamais stockés en clair** sur le disque dur du serveur.
*   **Algorithme utilisé** : **LibSodium** (Chacha20-Poly1305), le standard moderne et robuste pour le chiffrement symétrique.
*   **Chiffrement en flux continu (Streaming)** : Plutôt que de charger un fichier entier en mémoire RAM avec `file_get_contents()` (ce qui ferait crasher le serveur pour un fichier de 5Go), le système lit le fichier envoyé par le client, le chiffre *morceau par morceau* (chunks) et l'écrit directement sur le disque.
*   **Déchiffrement In-the-fly** : Lors d'un téléchargement, le serveur lit le fichier chiffré, le déchiffre à la volée, et l'envoie dans la réponse HTTP directement vers le client Java.

### C. Gestion des Données (Base de données MySQL)
*   **Modélisation** : Création et optimisation des tables `users`, `folders`, `files`, `sys_logs` et `file_shares`.
*   **Sécurité SQL** : Utilisation stricte de requêtes préparées (PDO) pour annuler tout risque d'injection SQL.

---

## 3. L'Infrastructure (Docker & Serveur Distant)

Afin d'assurer que l'API puisse tourner de manière fiable n'importe où (notamment sur votre serveur `api.obsilock.iris.a3n.fr`), nous avons utilisé **Docker**.

*   **Dockerfile** : Construit une image personnalisée combinant `PHP 8`, l'extension `libsodium` pour la cryptographie, l'extension `pdo_mysql` et le serveur `Apache` configuré avec le module `mod_rewrite` pour faire fonctionner les routes de l'API Slim.
*   **Docker Compose** : Orchestre 3 conteneurs :
    1.  `db` : La base de données MariaDB.
    2.  `phpmyadmin` : Pour administrer la base facilement.
    3.  `api` : Le code PHP.
*   **Traefik / Nginx** : Sur le serveur distant, le conteneur API est relié au routeur/reverse-proxy afin que le trafic public atteigne correctement le conteneur isolé.

---

## 4. Le Client Lourd (JavaFX)

C'est l'interface avec laquelle l'utilisateur final interagit. Le code source est structuré dans le dossier `coffreFortJava-main`.

### A. Intégration du Design "Cyber Dark & Lime Green"
L'interface JavaFX originale était très classique (fond gris/blanc, boutons rouges). Vous avez proposé une maquette très moderne ("Cyber" sombre avec des accents vert fluo).
*   **Fichiers FXML** : Les vues principales `main.fxml` (le dashboard complet avec l'arborescence, la table des fichiers, la barre de quota) et `login2.fxml` (l'écran de connexion) ont été réécrites.
*   Nous avons remplacé les attributs CSS JavaFX (`-fx-background-color: #E5E5E5;`) par les teintes Cyber Dark (`#121417`, `#1c1f23`) et l'accent vert primaire (`#94E01E`), donnant au client lourd une finition premium, tout en supprimant les bordures ou ombrages inutiles (design "Flat").
*   **Composants Avancés (Feuille de style externe)** : Les composants complexes comme la `TreeView` (arborescence des dossiers) et la `TableView` (liste des fichiers) nécessitant une personnalisation plus profonde, ont été intégralement stylisés via un fichier CSS dédié (`style-javafx.css`) pour retirer les fonds blancs d'origine de Swing/JavaFX.

### B. Consommation de l'API (HTTP Client)
Le client Java requiert une classe `ApiClient.java` qui utilise la librairie interne `java.net.http.HttpClient` pour émettre les requêtes (GET, POST, DELETE) vers votre domaine distant. C'est cette classe qui gère l'injection du JWT dans les requêtes pour prouver l'identité de l'utilisateur.

---

## 5. Résolution des Derniers Bugs (Mise en production)
Lors du branchement du client JavaFX sur l'API de production en direct, plusieurs ajustements ont été effectués pour assurer une parfaite cohésion :
1. **Parser du Token JWT** : Le backend renvoyait le token dans une clé JSON `"token"`, tandis que le client Java cherchait `"jwt"`. L'`ApiClient.java` a été corrigé.
2. **Affichage du Quota** : La barre de progression (Espace utilisé) affichait "0 B", car le parseur cherchait `"used_bytes"` alors que l'API renvoie `"used"` et `"total"`. Ceci a été fixé et la barre fonctionne et change de couleur (verte, orange, rouge) selon le ratio d'utilisation.
3. **NullPointerException (Écran login)** : Disparition de certains `Labels` de statut entraînant un crash applicatif lors du clic sur le bouton de connexion. La vue `login2.fxml` a été restaurée en ajoutant le composant manquant.

---

## Points forts à mettre en avant à l'oral 🗣️

1. **"J'ai opté pour un chiffrement en flux (streaming) avec LibSodium pour optimiser les performances RAM de mon serveur, me permettant de traiter d'énormes fichiers sans ralentissements."**
2. **"L'application est totalement découplée. J'ai un client lourd autonome en JavaFX et une API Backend autonome en PHP, communicant de manière sécurisée et "stateless" grâce aux JWT."**
3. **"L'ensemble du back-end est isolé sous Docker, ce qui permet un déploiement instantané (Continuous Deployment) sans problème de "ça marche sur ma machine mais pas en prod"."**
4. **"J'ai soigné l'UX et l'UI du client lourd, en utilisant directement les contraintes et possibilités graphiques de JavaFX (FXML) et une feuille CSS personnalisée pour avoir un thème dark immersif sans recourir à des solutions web hybrides."**
