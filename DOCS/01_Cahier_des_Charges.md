# Cahier des Charges - ObsiLock

## 1. Présentation du Projet
**ObsiLock** est une solution de coffre-fort numérique sécurisé permettant de stocker, d'organiser et de partager des fichiers. La particularité du projet est de garantir la confidentialité des données grâce à un chiffrement systématique avant le stockage sur le serveur.

## 2. Objectifs
- Fournir une plateforme sécurisée pour le stockage de documents sensibles.
- Garantir que même en cas de compromission du serveur, les données restent illisibles (chiffrement au repos).
- Permettre un partage contrôlé et temporaire de fichiers via des liens sécurisés.

## 3. Besoins Fonctionnels
### 3.1 Gestion des Utilisateurs
- Création de compte (Inscription).
- Authentification sécurisée via JWT (JSON Web Token).
- Gestion du quota de stockage par utilisateur.

### 3.2 Gestion des Fichiers et Dossiers
- Organisation en arborescence (dossiers et sous-dossiers illimités).
- Chaque utilisateur possède un dossier "Racine" système créé à l'inscription.
- Un fichier appartient obligatoirement à un dossier parent.
- Déplacement et Duplication : les éléments peuvent être déplacés ou dupliqués entre dossiers.
- Gestion des versions : conservation de l'historique des modifications d'un fichier.

### 3.3 Partage Sécurisé
- Génération de liens de partage pour des fichiers ou des dossiers (Spécialisation XT).
- Protection par token unique et signature.
- Double condition d'expiration : par date (expires_at) ET/OU par nombre d'utilisations (max_uses). Le lien devient invalide dès que la première limite est atteinte.
- **Règle de sécurité** : Le partage d'un dossier vide est bloqué par le système (Alerte utilisateur).
- Possibilité de révoquer un partage manuellement à tout moment.

### 3.4 Journalisation et Sécurité
- Suivi détaillé des accès via les partages (IP, User-Agent, succès/échec).
- Journalisation des tentatives d'upload et des erreurs système.
- Protection contre les abus (Rate Limiting).

## 4. Contraintes Techniques
- **Backend** : PHP 8.1 avec le micro-framework Slim 4.
- **Frontend** : Application JavaFX (Java 17).
- **Base de données** : MySQL 8.0.
- **Sécurité** : Utilisation de la bibliothèque LibSodium pour le chiffrement XSalsa20-Poly1305.
- **Déploiement** : Architecture conteneurisée (Docker) derrière un reverse proxy (Traefik).
