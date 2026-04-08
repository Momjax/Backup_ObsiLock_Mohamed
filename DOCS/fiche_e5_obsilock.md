# BTS Services informatiques aux organisations — SESSION 2026
## Épreuve E5 - Conception et développement d'applications (option SLAM)
## ANNEXE 7-1-B : Fiche descriptive de réalisation professionnelle (recto)

---

## DESCRIPTION D'UNE RÉALISATION PROFESSIONNELLE — N° réalisation : 4

**Nom, prénom :** _(à compléter)_
**N° candidat :** _(à compléter)_

**Épreuve ponctuelle :** ☐ &nbsp;&nbsp; **Contrôle en cours de formation :** ☑ &nbsp;&nbsp; **Date :** 22 / 03 / 2026

---

## Organisation support de la réalisation professionnelle

_(à compléter : nom de l'établissement / organisme de formation)_

---

## Intitulé de la réalisation professionnelle

**ObsiLock – Coffre-Fort Numérique**

**Période de réalisation :** 10/02/2026 – 22/03/2026 &nbsp;&nbsp; **Lieu :** Nice

**Modalité :** ☐ Seul(e) &nbsp;&nbsp; ☑ En équipe

---

## Compétences travaillées

- ☑ Concevoir et développer une solution applicative
- ☑ Assurer la maintenance corrective ou évolutive d'une solution applicative
- ☑ Gérer les données

---

## Conditions de réalisation (ressources fournies, résultats attendus)

### Ressources fournies :
- Cahier des charges du projet ObsiLock,
- Contrat d'API au format OpenAPI v1 (openapi.yaml) défini en équipe en Jour 1,
- Accès au serveur mutualisé distant pour le déploiement (iris.a3n.fr),
- Documentation du micro-framework Slim 4 (PHP) et de la librairie Medoo,
- Configuration Docker préétablie (Dockerfile, docker-compose.yml, init.sql).

### Résultats attendus :
- Architecture Client/Serveur fonctionnelle et déployée (JavaFX + API PHP REST),
- Stockage et accès aux fichiers avec chiffrement au repos (LibSodium streaming),
- Authentification sécurisée via JSON Web Tokens (JWT),
- Gestion complète des fichiers : upload, download, versioning, corbeille,
- Système de partage sécurisé par liens (token, expiration, révocation),
- Gestion des quotas de stockage par utilisateur,
- Interface JavaFX avec thème dynamique (Obsidian / Emerald Green),
- Documentation technique et utilisateur complète (DOCS/).

---

## Description des ressources documentaires, matérielles et logicielles utilisées

### Ressources documentaires :
- Cahier des charges et contrat d'API OpenAPI v1 (openapi.yaml),
- Documentation officielle PHP 8, LibSodium, Slim Framework 4, Medoo ORM,
- Documentation JavaFX 17 (openjfx.io) et librairie Jackson (parsing JSON),
- RFC 7519 (JWT), RFC 2104 (HMAC), OWASP Secure Headers Project,
- Tutoriels JavaFX (code.makery.ch), exemples Postman/Newman,
- Fichiers de planification journalière fournis (docs/jour-01 à jour-07).

### Ressources matérielles :
- Ordinateur personnel sous Linux,
- Serveur distant mutualisé (VPS) pour le déploiement de l'API,
- Réseau local et réseau internet pour les tests et le déploiement.

### Ressources logicielles :

**Outils de développement :**
- Git – gestion de version
- GitHub – hébergement du code source et collaboration
- VS Code – environnement de développement intégré
- IntelliJ IDEA / Scene Builder – développement JavaFX et FXML
- Postman / Newman – tests et validation de l'API REST
- Swagger Editor – validation du contrat OpenAPI

**Technologies Backend :**
- PHP 8 – langage côté serveur (orienté objet)
- Slim Framework 4 – micro-framework PHP REST
- Medoo – micro-ORM PHP (requêtes préparées PDO)
- MariaDB 10 / MySQL 8 – système de gestion de base de données
- PHP LibSodium (ext-sodium) – chiffrement des fichiers (XSalsa20-Poly1305)
- Firebase PHP-JWT – génération et validation des tokens JWT
- Docker 28 – conteneurisation (API PHP/Apache, MariaDB, PhpMyAdmin)
- Traefik – reverse proxy et routage HTTPS

**Technologies Frontend (Client Lourd) :**
- Java 17 – langage du client lourd
- JavaFX 17 – framework d'interface graphique Desktop
- FXML – structuration des vues (équivalent XML/HTML pour JavaFX)
- CSS Vanilla – stylisation des composants JavaFX (thèmes Obsidian/Emerald)
- Jackson – parsing JSON (communication avec l'API REST)

---

## Modalités d'accès aux productions et à leur documentation :

- Code source Backend disponible sur le dépôt Git de l'établissement.
- Code source Client JavaFX dans le dossier `coffreFortJava-main/`.
- Application API accessible en production : `http://api.obsilock.iris.a3n.fr:8080`
- Base de données initialisée automatiquement via `init.sql` au démarrage Docker.
- Documentation technique et utilisateur fournie en Markdown dans `/DOCS/`.
- OpenAPI v1 consultable sur Swagger Editor via `openapi.yaml`.

---
---

# BTS Services informatiques aux organisations — SESSION 2026
## Épreuve E5 - Conception et développement d'applications (option SLAM)
## ANNEXE 7-1-B : Fiche descriptive de réalisation professionnelle (verso)

---

## Descriptif de la réalisation professionnelle, y compris les productions réalisées et schémas explicatifs

### Objectif du projet

Concevoir et déployer un **coffre-fort numérique** client/serveur permettant :
- de stocker des fichiers **chiffrés au repos** (jamais en clair sur le serveur),
- de les organiser dans une arborescence de dossiers personnels,
- de versionner chaque fichier (historique immuable des versions),
- de partager des fichiers via des **liens sécurisés** (token opaque, expiration, quota d'usages),
- de gérer les **quotas de stockage** par utilisateur (50 Mo par défaut),
- d'offrir une interface Desktop JavaFX premium avec thème dynamique.

---

### Description technique

#### Architecture :

Application **Client/Serveur découplée et stateless** :
- **Backend :** API REST PHP 8 + Slim Framework, déployée via Docker sur serveur distant, accessible derrière Traefik (reverse proxy).
- **Frontend :** Application Desktop Java 17 + JavaFX 17, communiquant avec l'API via des requêtes HTTP REST avec token JWT Bearer.
- **Base de données :** MariaDB 10, 7 tables : `users`, `folders`, `files`, `file_versions`, `shares`, `downloads_log`, `settings`.
- **Stockage :** Fichiers physiques chiffrés dans `/storage/uploads/` (nom physique unique, jamais le nom original).

#### Chiffrement (point fort technique) :

Utilisation de **PHP LibSodium** (`sodium_crypto_secretbox` — XSalsa20 + Poly1305 MAC) :
1. Génération d'une clé de contenu aléatoire (32 octets) + nonce de départ (24 octets).
2. Chiffrement du fichier **en streaming par blocs de 8 Ko** (pas de chargement RAM complet).
3. Chiffrement de la clé de contenu avec la clé maître du serveur (`ENCRYPTION_KEY` dans `.env`) → `key_envelope` stockée en base.
4. Lors du téléchargement : déchiffrement de la clé, puis déchiffrement du fichier à la volée et envoi en streaming HTTP.

#### Authentification JWT :

- `POST /auth/login` : vérification bcrypt → génération d'un JWT signé (HS256, expiry 1h).
- Toutes les routes protégées valident le header `Authorization: Bearer <token>` via un middleware Slim.

#### Versioning :

Chaque upload (initial ou remplacement) crée une entrée **immuable** dans `file_versions` avec numéro de version auto-incrémenté, `checksum` SHA-256, et les informations de chiffrement propres à la version. Les liens de partage pointent toujours vers la version courante.

#### Sécurité HTTP :

- `SecurityHeadersMiddleware` : `X-Frame-Options`, `X-Content-Type-Options`, `Content-Security-Policy`, `Strict-Transport-Security`.
- `RateLimitMiddleware` : limitation par IP sur les routes sensibles (429 + `Retry-After`).

---

### Fonctionnalités principales :

- Inscription / Connexion sécurisée (bcrypt + JWT)
- Gestion des dossiers (créer, renommer, suppression logique → corbeille, restauration)
- Upload de fichiers chiffré en streaming, téléchargement déchiffré à la volée
- Versioning immutable (historique, checksum, remplacement de version)
- Partage par lien sécurisé (token, expiration date/usages, révocation, journal `downloads_log`)
- Quotas de stockage avec barre de progression couleur (vert/orange/rouge)
- Interface thème dark (Obsidian) ↔ clair (Emerald Green) via Toggle Switch CSS
- Corbeille (restauration ou suppression définitive)
- Administration des quotas utilisateurs

---

### Productions livrées :

- Application API PHP REST complète, testée et déployée en production.
- Client Desktop Java 17 + JavaFX 17 complet et fonctionnel.
- Scripts SQL (`init.sql`) et Docker entièrement configurés.
- Code source versionné sur Git (branches main/dev).
- Collection Postman de tests API (scénarios fil rouge + cas d'erreurs).
- Documentation : Cahier des charges (MCD/MLD/MPD/UML/GANTT), Doc BDD, Doc Technique, Guide Utilisateur (`/DOCS/`).
- Fiche descriptive de réalisation (présente).

---

### Schémas explicatifs accompagnant cette réalisation :

- Un **modèle conceptuel de données (MCD)** illustrant les 7 entités et leurs relations.
- Un **diagramme de classes UML** pour le backend PHP et le client JavaFX.
- Un **schéma d'architecture technique** présentant les interactions Client ↔ API ↔ BDD ↔ Storage.
- Un **diagramme de séquence** du flux d'authentification JWT.
- Un **planning GANTT** sur 7 jours de développement.

---

### Représentation des flux typiques de l'application :

**Connexion / Authentification :**
```
Client JavaFX → POST /auth/login {email, password}
→ API vérifie bcrypt → génère JWT HS256
→ Client stocke JWT en mémoire → toutes requêtes suivantes avec Bearer Token
```

**Upload d'un fichier chiffré :**
```
Client → sélectionne fichier → POST /files (multipart)
→ API génère content_key + nonce → chiffre fichier par blocs 8Ko (LibSodium)
→ Écrit fichier chiffré sur disque (stored_name unique)
→ Chiffre content_key avec master_key → stocke key_envelope + iv en BDD
→ Retourne 201 {file_id, filename, size}
→ Client raffraîchit la liste des fichiers
```

**Téléchargement déchiffré :**
```
Client → GET /files/{id}/download (Bearer JWT)
→ API récupère key_envelope + iv dans file_versions
→ Déchiffre key_envelope avec master_key
→ Lit fichier chiffré par blocs → déchiffre → envoie en streaming HTTP
→ Client reçoit fichier en clair, propose téléchargement local
```

**Partage / Téléchargement public :**
```
Utilisateur → POST /shares {file_id, expires_at, max_uses}
→ API génère token opaque → retourne URL publique
→ Destinataire (sans compte) → GET /s/{token} → voit métadonnées
→ POST /s/{token}/download → API vérifie validité → déchiffre → décrémente remaining_uses → loggue dans downloads_log
```
