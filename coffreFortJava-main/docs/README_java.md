# Coffre‑fort numérique — Mini client JavaFX (MVP pédagogique)

Ce dépôt fournit un mini client lourd JavaFX servant d’exemple pour le projet « Coffre‑fort numérique » destiné aux étudiants. Il s’appuie sur le sujet du projet principal et donne un prototype MVP exécutable, minimal, pour comprendre ce qui est attendu côté client lourd, ainsi qu’un boilerplate JavaFX prêt à brancher sur une vraie API.

Référence projet global (back/API) : https://github.com/AstrowareConception/Coffre-fort-numerique

Objectifs du dépôt :
- Démarrer rapidement un client JavaFX fonctionnel sans backend (tout est simulé).
- Montrer la structure de code attendue (FXML + contrôleurs + modèles + client API).
- Guider l’intégration progressive d’un vrai backend REST et l’industrialisation du client.

---

## Démarrage rapide

Prérequis :
- JDK 17 à 21 installé (vérifier avec `java -version`).
- Maven installé (ou utilisable via votre IDE).

Lancer depuis le terminal :
1) À la racine du projet :
```
mvn clean javafx:run
```

Lancer depuis l’IDE (IntelliJ/ Eclipse) :
- Exécutez la classe: `com.coffrefort.client.Launcher` (et non `App`).
- Configurez un JDK 17–21 pour le projet.

Notes :
- Le plugin `javafx-maven-plugin` gère le module‑path JavaFX automatiquement.
- L’application démarre avec un écran de connexion, puis ouvre une fenêtre principale avec des données simulées.

---

## Structure du projet et rôle de chaque classe/FXML

Paquet `com.coffrefort.client` :
- `Launcher` — Point d’entrée recommandé depuis l’IDE. Lance `Application.launch(App.class, args)` pour éviter l’erreur « JavaFX runtime components are missing » avec certains JDK.
- `App` — Classe JavaFX `Application`. Charge `login.fxml`, injecte `ApiClient` et ouvre la fenêtre principale (`main.fxml`) après connexion.
- `ApiClient` — Client d’API simulé. Aujourd’hui, il retourne des données factices et simule l’authentification. À remplacer progressivement par de vrais appels HTTP (OkHttp inclus via Maven). Gère le `authToken`.

Paquet `com.coffrefort.client.controllers` :
- `LoginController` — Contrôleur de l’écran de connexion. Récupère email/mot de passe, appelle `apiClient.login(...)` et, en cas de succès, déclenche l’ouverture de la fenêtre principale.
- `MainController` — Contrôleur de la fenêtre principale. Configure le TreeView, le TableView, la barre de quota, charge l’arborescence simulée via `apiClient.listRoot()` et le quota via `apiClient.getQuota()`. Le bouton « Uploader (simulation) » ouvre un sélecteur de fichier.

Paquet `com.coffrefort.client.model` :
- `NodeItem` — Modèle d’un dossier (nœud) contenant des sous‑dossiers et une liste de fichiers.
- `FileEntry` — Modèle d’un fichier (nom, taille, date de mise à jour). Utilisé par la TableView.
- `Quota` — Modèle pour l’espace utilisé et le quota maximum, expose un ratio d’usage.

Ressources FXML `src/main/resources/com/coffrefort/client` :
- `login.fxml` — Décrit l’UI de connexion (champ email, mot de passe, bouton Se connecter) et référence `LoginController`.
- `main.fxml` — Décrit la fenêtre principale : `TreeView` pour les dossiers, `TableView` pour les fichiers, barre de progression du quota et bouton d’upload.

Autres fichiers utiles :
- `pom.xml` — Dépendances (JavaFX, OkHttp, Jackson), plugin JavaFX. Profil prêt pour `mvn javafx:run`.
- `implementation-java.md` — Guide détaillé pour passer du mock aux vrais appels HTTP (exemples OkHttp/Jackson, upload multipart, gestion de token, découpage en services, etc.).

---

## Ce que vous devez faire (à partir du MVP)

Intégrer un vrai backend et transformer ce MVP en logiciel opérationnel. Travaillez par incréments, branche par branche :

1) Authentification réelle
- Implémenter `ApiClient.login(email, password)` avec un appel HTTP vers l’API (JWT attendu en réponse).
- Stocker de façon sûre le token (mémoire + persistance optionnelle chiffrée si « se souvenir de moi »).
- Gérer les erreurs (401, messages utilisateur, états de bouton durant la requête).

2) Parcours des dossiers/fichiers
- Remplacer `listRoot()` pour appeler l’API et mapper la réponse JSON vers `NodeItem`/`FileEntry`.
- Ajouter la navigation (ouvrir un dossier recharge le TableView depuis l’API).

3) Upload et gestion de fichiers
- Implémenter l’upload multipart (OkHttp) avec barre de progression et annulation.
- Ajouter renommer/déplacer/supprimer (actions contextuelles sur la TableView/TreeView).
- Gérer le versionnage côté API (nouvelle version par upload de remplacement).

4) Quotas et retours UX
- Remplacer `getQuota()` par l’appel réel.
- Afficher avertissements à 80%/100%, bloquer l’upload si quota atteint.

5) Partage et liens publics
- Écran (ou dialog) pour créer un lien de partage (expiration ou nombre d’usages).
- Lister, révoquer, copier le lien. Côté Web, une page publique simple pour télécharger.

6) Sécurité/robustesse côté client
- Gestion du token (refresh/expiration si applicable, logout).
- Validation côté client (taille max de fichier, extensions interdites si besoin).
- Journalisation côté client (actions, erreurs) et reporting minimal.

7) Qualité et industrialisation
- Tests unitaires des mappers JSON et services.
- Gestion de configuration (URL API par environnement, variables système/properties).
- Packaging: jpackage/jlink pour livrer un exécutable.

Astuce : détaillez et validez l’API avec un contrat OpenAPI, puis générez des stubs si souhaité. Travaillez en feature branches et PR revues.

---

## Feuille de route d’amélioration vers un « vrai » logiciel

- Sécurité
  - Chiffrement côté serveur (au repos) et transport HTTPS strict.
  - Politique mots de passe, 2FA optionnel, verrouillage après X échecs.
- UX
  - Drag & drop pour upload, indicateurs de progression par fichier, recherches et filtres.
  - Notifications (système) en fin d’upload long.
- Fonctionnel
  - Tags, favoris, tri multi‑critères, corbeille/restauration.
  - Historique/versionning consultable et restauration d’une version.
- Partage
  - Droits fins (lecture/écriture), mot de passe sur lien, analytics de téléchargement.
- Opérations
  - Paramétrage via fichiers de conf, logs structurés, télémétrie minimale.
  - CI/CD (lint, build, tests, packaging), publication d’un installeur.

---

## Critères d’évaluation (exemple)

- Fonctionnalités réalisées vs. backlog et démonstration fluide.
- Qualité du code (clarté, séparation des responsabilités, gestion des erreurs, tests).
- Robustesse (gestion des états, erreurs réseau, quotas, UX cohérente).
- Documentation (README, commentaires, `implementation-java.md` suivi, schémas API).
- Collaboration (git flow, PR, issues, Kanban/Projects, revues code).

---

## Dépannage (FAQ)

- « JavaFX runtime components are missing » : lancez `com.coffrefort.client.Launcher` depuis l’IDE, ou utilisez `mvn javafx:run`.
- Rien ne s’affiche / plantage au lancement : vérifiez votre JDK (17–21) et que Maven télécharge bien JavaFX.
- Erreur réseau lors des appels API : pendant le MVP, les données sont simulées. Une fois le backend branché, vérifiez `ApiClient.baseUrl` et les CORS.

---

## Périmètre et architecture (rappel synthétique)

Pour une vision complète du sujet (comptes/JWT, chiffrement au repos, partage, quotas, clients JavaFX/Web, exigences non fonctionnelles, déploiement), référez‑vous au dépôt principal et à votre cahier des charges. En très bref :
- Comptes & sécurité : création, auth JWT, rôles (Utilisateur/Admin), politique MDp (Argon2id).
- Fichiers : chiffrement au repos (AES‑256‑GCM recommandé), enveloppe de clés, dossiers hiérarchiques, versionnage.
- Partage : liens signés (expiration ou nombre d’usages), révocation, journalisation.
- Quotas : espace par utilisateur avec alertes 80%/100%.
- Tech : Back Slim/Medoo + MariaDB/Postgres, JavaFX pour le client lourd, Web pour accès public/privé, Docker recommandé.

---

## 5) Modèle de données (proposition)

* **users**(id, email, pass_hash, quota_total, quota_used, is_admin, created_at)
* **folders**(id, user_id, parent_id, name, created_at)
* **files**(id, user_id, folder_id, original_name, mime, size, created_at)
* **file_versions**(id, file_id, version, stored_name, iv, auth_tag, key_envelope, checksum, created_at)
* **shares**(id, user_id, kind: 'file'|'folder', target_id, label, expires_at, max_uses, remaining_uses, is_revoked, created_at)
* **downloads_log**(id, share_id, version_id, downloaded_at, ip, user_agent, success)

> **Remarque** : `file_versions` permet de conserver l’historique et d’orienter un lien vers la dernière version.

---

## 6) Contrat d’API (brouillon à finaliser **Jour 1**)

> Doc Swagger interactive (en ligne) : https://editor.swagger.io/?url=https://raw.githubusercontent.com/AstrowareConception/Coffre-fort-numerique/refs/heads/main/openapi.yaml

**Auth**

* `POST /auth/register` {email,password} → 201
* `POST /auth/login` {email,password} → 200 {jwt}

**Dossiers & fichiers**

* `GET /folders` / `POST /folders` / `DELETE /folders/{id}`
* `GET /files?folder={id}`
* `POST /files` (multipart) → crée **version 1** (chiffrée)
* `POST /files/{id}/versions` (multipart) → **nouvelle version**
* `GET /files/{id}` (métadonnées + version courante)
* `DELETE /files/{id}` (supprime logique ou totale)
* `GET /files/{id}/download` (auth)

**Partages**

* `POST /shares` {kind,target_id,expires_at|max_uses,label}
* `GET /shares` (listes + stats)
* `POST /shares/{id}/revoke`
* **Public** : `GET /s/{token}` (infos) · `POST /s/{token}/download`

**Quotas & stats**

* `GET /me/quota` — utilisé / total / %
* `GET /me/activity` — derniers événements

> **Convention** : réponses **JSON**, erreurs normalisées `{error, code}` ; statuts : 200/201/204/400/401/403/404/409/413/422/429/500.

---

## 7) Sécurité détaillée

* **Chiffrement** : AES‑256‑GCM pour le contenu ; **clé par version** ; `key_envelope` = clé de version chiffrée par la clé publique serveur (RSA‑OAEP ou X25519 + sealed box). IV aléatoire, tag d’authentification stocké.
* **JWT** : durée courte (ex. 15 min) + **refresh token** (option) ; stockage côté JavaFX WebView : sécurisé.
* **Liens** : token signé (HMAC SHA‑256) + champs `exp` / `remaining_uses`.
* **Rate‑limit** basique et **headers de sécurité**.

---

## 8) Tests & qualité

* **Unitaires** : services (crypto wrapper, quotas, DAO Medoo).
* **Intégration** : routes (auth, upload, share/download, versions).
* **E2E** : collection Postman/Newman ; script de **jeu d’essai**.
* **Definition of Done** : tests verts, linter, doc mise à jour, revue de code OK.

---

## 9) Organisation de projet

* **Rôles** :

  * *Back‑end* (API, sécurité, BDD, packaging),
  * *JavaFX* (UX dépôt/gestion),
  * *Web* (pages partage & tableau de bord),
  * *Ops/Qualité* (Docker, CI, sauvegardes, Postman, documentation).
* **GitHub** : mono‑repo conseillé (api/, clients/javafx/, clients/web/). Branching : `main`, `dev`, feature branches ; **PR + review** obligatoires.
* **OpenAPI** source‑de‑vérité** (yaml) : générée **Jour 1** ; *mocks* via JSON Server/Prism (option).
* **Daily** 10 min ; **board** Kanban (ToDo / In Prog / Review / Done).

---

## 10) Planning (7 jours — Lundi→Vendredi + 2 jours)

> **Principe** : définir le contrat d’API en **Jour 1** pour débloquer le travail en parallèle.

### Jour 1 — Cadrage & contrat

* Kick‑off, risques, répartition des rôles.
* Schéma BDD + **OpenAPI v1** (endpoints ci‑dessus) + conventions d’erreurs.
* Squelette Slim + Medoo, middlewares (CORS, JSON, erreurs), migrations initiales.
* Setup repo GitHub (issues/labels), actions CI (lint + tests), .env.example.

### Jour 2 — Fichiers & dossiers (Back) / Shells clients

* Back : CRUD dossiers, upload **chiffré** v1, quotas, téléchargement.
* JavaFX : scaffolding, login écran, liste dossiers/fichiers (mock si besoin).
* Web : page publique `/s/{token}` (maquette) + intégration Bootstrap.

### Jour 3 — Partages & journalisation

* Back : création `shares`, tokens, expiration/uses, logs téléchargements.
* JavaFX : création de liens depuis la vue fichier/dossier ; affichage « mes partages ».
* Web : flux public `download` opérationnel.

### Jour 4 — Versions de fichiers

* Back : endpoint **nouvelle version** + règle « liens → dernière version ».
* JavaFX : *remplacer fichier* (progress bar) + métadonnées version.
* Web : afficher si une ressource a plusieurs versions (simple).

### Jour 5 — Finitions MVP & sécurité

* Back : passes sécurité (headers, rate‑limit simple), pagination, 404/413…
* Clients : UX minimale, messages d’erreur, indicateurs de quota.
* Tests Postman/Newman + README usage.

### Jour 6 — Stabilisation & doc

* Corrections, couverture de tests, script de jeu d’essai.
* Doc : OpenAPI finalisée, guide d’installation, sauvegarde/restauration, schémas.
* Démo interne : scénario de bout en bout.

### Jour 7 — Démo & soutenance

* Démo fil rouge (création compte → upload → lien → téléchargement → mise à jour version → suivi usages).
* Revue de code croisée, bilan d’équipe, dettes techniques & pistes (2FA, monitoring…).

---

## 11) Critères d’acceptation (extraits)

* **Upload chiffré** : hash du déchiffrement == hash original (test de preuve).
* **Lien expiré** : renvoie 410/403 à l’instant prévu ; **révocation** immédiate.
* **Versionnage** : un lien existant télécharge la **dernière version** => compteur usages OK.
* **Quota** : dépassement refusé avec message clair ; tableau de bord met à jour l’utilisation.
* **JavaFX** : upload avec barre de progression ; création de lien ; remplacement (nouvelle version).
* **Web** : téléchargement public via token ; affichage simple des métadonnées.

---

## 12) Livrables

* **Dépôt GitHub** (mono‑repo conseillé) avec README, **OpenAPI.yaml**, scripts SQL/migrations, collection Postman, docker‑compose (optionnel), captures démo.
* **Documentation** :

  * technique (archi, sécurité, modèle, choix crypto justifiés),
  * utilisateur (chemin critique),
  * exploitation (sauvegarde/restauration).
* **Jeu d’essai** reproductible.

---

## 13) Backlog améliorations (post‑MVP)

* 2FA TOTP, limites de débit, liste blanche IP.
* Prévisualisation, notifications email, statistiques détaillées.
* Rôles avancés/partage collaboratif, dossiers partagés, webhooks.
* Monitoring, PRA, rotation des clés, KMS.

---

## 14) Points à trancher en équipe (décisions documentées)

* RSA‑2048 vs ECC/X25519 pour l’enveloppe de clés.
* Suppression logique vs physique des fichiers.
* Rétention des logs (durée, anonymisation).
* Pagination et tailles limites par défaut.

---

