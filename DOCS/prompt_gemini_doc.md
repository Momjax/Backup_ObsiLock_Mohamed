# PROMPT GEMINI PRO — Documentation Projet ObsiLock

---

Tu es un expert en documentation technique et en BTS SIO. 
Tu dois produire une documentation de projet **complète, professionnelle et visuellement soignée** pour le projet **ObsiLock** (Coffre-Fort Numérique).

---

## CONTEXTE DU PROJET

**Nom du projet :** ObsiLock — Coffre-Fort Numérique  
**Contexte :** Projet BTS SIO — Architecture Client/Serveur  
**Objectif :** Application de stockage sécurisé de fichiers, avec chiffrement au repos, partage par liens, versioning, et gestion des quotas.

### Stack technique réelle :
- **Frontend :** Java 17 + JavaFX 17 (FXML + CSS custom). Interface Desktop.
- **Backend :** PHP 8 + Slim Framework 4. API REST stateless.
- **ORM :** Medoo (micro-ORM PHP)
- **Base de données :** MariaDB 10 / MySQL 8
- **Chiffrement :** PHP LibSodium — XSalsa20-Poly1305 — streaming blocs 8 Ko
- **Authentification :** JWT (JSON Web Tokens) — HS256
- **Infrastructure :** Docker (3 conteneurs : api, db, phpmyadmin) + Traefik reverse proxy
- **URL prod :** api.obsilock.iris.a3n.fr

---

## CE QUE TU DOIS PRODUIRE

Génère les **4 documents** suivants, chacun dans un bloc séparé et bien structuré :

---

## DOCUMENT 1 — CAHIER DES CHARGES

Contenu obligatoire :
1. **Contexte & Problématique** : coffre-fort numérique sécurisé, jamais de fichier en clair
2. **Architecture globale** : schéma Client/Serveur (JavaFX ↔ API PHP ↔ MariaDB + Storage chiffré), déployé via Docker + Traefik
3. **Besoins fonctionnels** :
   - Authentification (inscription/connexion JWT, bcrypt)
   - Gestion fichiers & dossiers (upload chiffré streaming, download, renommage, soft delete)
   - Versioning immutable (chaque upload = nouvelle version dans file_versions)
   - Partages sécurisés (token opaque, expiration date/nb usages, révocation)
   - Gestion quotas (50 Mo défaut, barre progression verte/orange/rouge)
   - Thème UI (Toggle Switch : mode Obsidian sombre ↔ Emerald vert clair)
   - Corbeille (restauration ou suppression définitive)
4. **MCD** (Modèle Conceptuel de Données) — entités :
   - USERS, FOLDERS, FILES, FILE_VERSIONS, SHARES, DOWNLOADS_LOG, SETTINGS
   - Relations : USERS possède FOLDERS et FILES ; FOLDERS contient FOLDERS (récursif) et FILES ; FILES est versionné par FILE_VERSIONS ; FILES est partagé via SHARES ; SHARES génère DOWNLOADS_LOG
5. **MLD** (Modèle Logique) — liste des tables avec PK soulignées et FK notées #
6. **MPD** (Modèle Physique) — script SQL complet MariaDB/MySQL :

```sql
USE coffre_fort;
CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, quota_total BIGINT DEFAULT 52428800, quota_used BIGINT DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX idx_email (email)) ENGINE=InnoDB;
CREATE TABLE folders (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, parent_id INT NULL, name VARCHAR(255) NOT NULL, is_deleted TINYINT(1) DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE SET NULL) ENGINE=InnoDB;
CREATE TABLE files (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, folder_id INT NULL, filename VARCHAR(255) NOT NULL, stored_name VARCHAR(255) NOT NULL, size BIGINT NOT NULL, mime_type VARCHAR(100) NOT NULL, current_version INT DEFAULT 1, is_deleted TINYINT(1) DEFAULT 0, uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL) ENGINE=InnoDB;
CREATE TABLE file_versions (id INT AUTO_INCREMENT PRIMARY KEY, file_id INT NOT NULL, version INT NOT NULL DEFAULT 1, stored_name VARCHAR(255) NOT NULL, size BIGINT NOT NULL, checksum VARCHAR(64), mime_type VARCHAR(100), iv TEXT, auth_tag TEXT, key_envelope TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE, UNIQUE KEY unique_version (file_id, version)) ENGINE=InnoDB;
CREATE TABLE shares (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, file_id INT NOT NULL, token VARCHAR(255) UNIQUE NOT NULL, label VARCHAR(255), expires_at DATETIME NULL, max_uses INT NULL, remaining_uses INT NULL, is_revoked TINYINT(1) DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE) ENGINE=InnoDB;
CREATE TABLE downloads_log (id INT AUTO_INCREMENT PRIMARY KEY, share_id INT NOT NULL, ip VARCHAR(45), user_agent TEXT, success TINYINT(1) DEFAULT 1, message TEXT, downloaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (share_id) REFERENCES shares(id) ON DELETE CASCADE) ENGINE=InnoDB;
CREATE TABLE settings (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) UNIQUE NOT NULL, value VARCHAR(255) NOT NULL) ENGINE=InnoDB;
INSERT INTO settings (name, value) VALUES ('quota_bytes', '52428800');
```

7. **UML Cas d'Utilisation** : 2 acteurs (Utilisateur, Administrateur) avec leurs cas d'usage
8. **UML Diagramme de Classes** : Backend PHP (UserRepository, FolderRepository, FileRepository, FileVersion, Share, EncryptionService, AuthController, FileController, ShareController) + Frontend JavaFX (App, ApiClient, MainController, LoginController)
9. **Planning GANTT** sur 7 jours :
   - J1 (02-10) : OpenAPI v1, Squelette Slim+Medoo, Git & CI
   - J2 (02-11) : Auth JWT, Endpoints dossiers, Upload chiffré, Squelette JavaFX
   - J3 (02-12) : Liens partage, Journal téléchargements, UI Partages
   - J4 (02-13) : Versioning fichiers, Métadonnées, UI Versions
   - J5 (02-14) : Headers sécurité, Rate-Limiting, Pagination+quotas
   - J6 (02-15) : Tests unitaires, Doc installation, Backup/Restore
   - J7 (03-22) : UI Thème Switch, Déploiement prod, Documentation finale
10. **Contraintes non-fonctionnelles** : tableau (Sécurité, Performance, Disponibilité, Compat, Quota)

---

## DOCUMENT 2 — DOCUMENTATION BASE DE DONNÉES

Contenu obligatoire :
1. Introduction (schéma d'ensemble des tables)
2. Description détaillée de chaque table — pour chacune :
   - Nom, rôle
   - Tableau des colonnes : Nom | Type | Contrainte | Description
   - Règles métier associées
3. Tables à documenter :
   - **users** : email unique, password bcrypt, quota_total (50Mo défaut), quota_used
   - **folders** : user_id, parent_id (NULL = racine), name, is_deleted (soft delete)
   - **files** : user_id, folder_id, filename (original), stored_name (physique unique sur disque), size, mime_type, current_version, is_deleted
   - **file_versions** : file_id, version (auto-incrémenté), stored_name, size, checksum SHA-256, iv (nonce base64), auth_tag, key_envelope (clé chiffrée avec master key, base64) — UNIQUE(file_id, version) = versions immutables
   - **shares** : user_id, file_id, token (opaque unique), label, expires_at (NULL=jamais), max_uses (NULL=illimité), remaining_uses (décrémenté atomiquement), is_revoked
   - **downloads_log** : share_id, ip, user_agent, success (1/0), message (raison échec)
   - **settings** : clé-valeur app (quota_bytes = 52428800)
4. Index et performances (tableau)
5. Schéma ERD textuel ou visuel

---

## DOCUMENT 3 — DOCUMENTATION TECHNIQUE

Contenu obligatoire :
1. **Architecture** : schéma et description (Client JavaFX → API Slim → MariaDB + Storage chiffré, via Traefik)
2. **Structure des fichiers backend** :
   src/Controller/ (Auth, File, Folder, Share)
   src/Middleware/ (RateLimit, SecurityHeaders)
   src/Model/ (UserRepository, FolderRepository, FileRepository, FileVersion, Share, DownloadLog)
   src/Service/ (EncryptionService)
   public/index.php (point d'entrée unique)
3. **Middlewares** : SecurityHeadersMiddleware (CSP, X-Frame-Options, HSTS, nosniff), RateLimitMiddleware (IP-based, 429 + Retry-After), JSON Parser, CORS, JWT Validator
4. **Authentification JWT** :
   - Payload : {user_id, email, role, iat, exp}
   - Flux : POST /auth/login → vérif bcrypt → génère JWT HS256 → client stocke en mémoire → Bearer Token dans chaque requête
5. **Chiffrement LibSodium** (point fort technique) :
   - Algorithme : sodium_crypto_secretbox (XSalsa20 + Poly1305 MAC)
   - Clé 32 octets, nonce 24 octets
   - Streaming blocs 8192 octets (pas de saturation RAM)
   - Pipeline upload : génère content_key aléatoire → chiffre par blocs → incrémente nonce → chiffre content_key avec master_key → stocke key_envelope + iv en BDD
   - Pipeline download : déchiffre key_envelope → lit fichier chiffré par blocs → envoie déchiffré en streaming HTTP
6. **Endpoints API** — tableau complet :
   - POST /auth/register, POST /auth/login
   - GET /folders, POST /folders, DELETE /folders/{id}, POST /folders/{id}/restore, DELETE /folders/{id}/permanent
   - GET /files, POST /files (upload multipart), GET /files/{id}, GET /files/{id}/download, DELETE /files/{id}, POST /files/{id}/versions, GET /files/{id}/versions
   - POST /shares, GET /shares, POST /shares/{id}/revoke
   - GET /s/{token} (public), POST /s/{token}/download (public)
   - GET /me/quota, PUT /admin/users/{id}/quota
7. **Codes d'erreur** : 400, 401, 403, 404, 409, 413, 422, 429 — format JSON {error, code}
8. **Infrastructure Docker** : 3 conteneurs (api:PHP8+Apache+LibSodium, db:MariaDB10, phpmyadmin), Traefik labels, volume storage
9. **Client JavaFX** : pattern MVC, ApiClient singleton (java.net.http.HttpClient), SessionManager (expiration JWT), App.java (gestion thème CSS appliqué à toutes les scènes)
10. **Procédure Sauvegarde/Restauration** :
    - Backup : mysqldump coffre_fort + tar storage/uploads/
    - Restore : mysql coffre_fort < dump.sql + tar -xzf
    - IMPORTANT : sans ENCRYPTION_KEY d'origine, les fichiers sont illisibles

---

## DOCUMENT 4 — GUIDE UTILISATEUR

Contenu obligatoire :
1. **Introduction** : qu'est-ce qu'ObsiLock, à quoi ça sert
2. **Premiers pas** : Inscription, Connexion, mot de passe oublié
3. **Interface** : description des 3 zones (barre latérale arborescence/quota/nav, zone centrale liste fichiers, barre d'actions)
4. **Gestion des fichiers** : Uploader, Télécharger, Renommer, Supprimer → corbeille
5. **Gestion des dossiers** : Créer, Naviguer, fil d'Ariane
6. **Versioning** : voir l'historique, uploader nouvelle version, comprendre que les partages pointent toujours vers la dernière version
7. **Partage** : Créer un lien (label, expiration, nb usages), copier dans presse-papiers, gérer ses partages, révoquer. Expliquer l'accès pour le destinataire sans compte.
8. **Corbeille** : voir éléments, restaurer, supprimer définitivement (irréversible)
9. **Quota** : barre de stockage verte/orange(80%)/rouge(100%), libérer espace, contacter admin pour augmentation
10. **Thème** : Toggle Switch Obsidian (sombre) ↔ Emerald (vert clair), s'applique instantanément à toutes les fenêtres
11. **FAQ** : 8+ questions/réponses (sécurité fichiers, mot de passe oublié, quota plein, sécurité des liens, droits destinataire, taille max, coupure pendant upload, différence soft delete/corbeille)

---

## INSTRUCTIONS DE MISE EN FORME

- Langue : **Français**
- Format : **Markdown soigné** avec titres hiérarchiques, tableaux, listes et blocs de code
- Chaque document doit avoir : logo textuel ou entête identifiable, pagination mentionnée, pied de page "ObsiLock — BTS SIO — 2026"
- Les diagrammes doivent être en **Mermaid** (erDiagram, classDiagram, flowchart, gantt, sequenceDiagram) — syntaxe stricte et fonctionnelle
- Tableaux avec en-têtes formatés et alignement des colonnes
- Blocs de code avec langage spécifié (```sql, ```java, ```php, etc.)
- Utilise des emojis de manière professionnelle pour les titres de sections (🗄️ 🔐 📋 💻 📖 ✅ ⚠️)
- Niveau de détail : **expert**, comme un vrai dossier technique de stage BTS SIO
- **Ne résume pas** — donne le contenu complet et exhaustif de chaque section

---

## ORDRE DE PRODUCTION

Produis les documents dans cet ordre :
1. Cahier des Charges (le plus long, avec MCD/MLD/MPD/UML/GANTT)
2. Documentation BDD
3. Documentation Technique
4. Guide Utilisateur

Pour chaque document, commence par :
`# [NUMÉRO]. [NOM DU DOCUMENT] — ObsiLock`

Puis le contenu complet.
