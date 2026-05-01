# Documentation Technique - ObsiLock

## 1. Architecture Système
ObsiLock utilise une architecture **Client/Serveur** découplée :
- **Serveur (API)** : Traite les requêtes, gère la base de données et stocke les fichiers chiffrés.
- **Client (JavaFX)** : Fournit l'interface utilisateur et gère le chiffrement/déchiffrement côté client pour certains flux.

## 2. Pile Technologique
- **Environnement** : Docker & Docker Compose.
- **Serveur Web** : Apache (via l'image PHP-Apache).
- **Langage Backend** : PHP 8.1.
- **Framework API** : Slim 4.
- **Base de données** : MySQL 8.0.
- **Reverse Proxy** : Traefik (Gestion SSL/HTTPS).

## 3. Sécurité et Chiffrement
### 3.1 Chiffrement au repos
Les fichiers sont chiffrés avant d'être stockés sur le disque du serveur via la bibliothèque **LibSodium**.
- **Algorithme** : XSalsa20-Poly1305.
- **Mécanisme** : Utilisation d'un `nonce` unique pour chaque version de fichier et d'une clé d'enveloppe.

### 3.2 Authentification
- Utilisation de **JWT (JSON Web Tokens)**.
- Le token est généré lors de la connexion et doit être fourni dans le header `Authorization: Bearer <token>` pour chaque requête protégée.

### 3.3 Protection Réseau
- **HTTPS** : Obligatoire sur le port 4433.
- **Rate Limiting** : Limite du nombre de requêtes par IP pour prévenir les abus.
- **Headers de sécurité** : CSP, X-Frame-Options, HSTS, X-Content-Type-Options.

## 4. Schéma et Modélisation (Merise 2)
La base de données `coffre_fort` repose sur un modèle hiérarchique :
- **Arborescence** : Les dossiers sont récursifs (un dossier peut contenir d'autres dossiers).
- **Propriété héritée** : Un fichier appartient à un dossier, qui appartient à un utilisateur. Plus de lien direct Utilisateur-Fichier, ce qui simplifie le modèle.
- **Duplication** : Un fichier peut être lié à un fichier source en cas de copie.
- **Versioning** : Chaque fichier possède au moins une version initiale.
- **Héritage XT** : Les partages concernent soit un fichier, soit un dossier.

### 4.1 Tables de la base de données
1. `users` : Comptes et quotas.
2. `folders` : Dossiers personnels (sans récursivité).
3. `files` : Métadonnées et indexation.
4. `file_versions` : Historique, nonces et clés d'enveloppe.
5. `shares` : Liens de partage publics.
6. `downloads_log` : Logs d'accès aux partages.
7. `upload_logs` : Logs de dépôt.
8. `settings` : Configuration globale.

## 5. Déploiement
Le déploiement s'effectue via Docker Compose :
```bash
docker compose -f docker-compose.prod.yml up -d
```
Le script `init.sql` initialise automatiquement la structure de la base lors du premier lancement.
