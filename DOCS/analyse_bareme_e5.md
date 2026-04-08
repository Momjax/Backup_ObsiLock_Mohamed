# ✅ Analyse E5 BTS SIO SLAM — ObsiLock vs Barème Officiel

---

## 📌 Rappel du cadre de l'épreuve E5

| Élément | Valeur |
| :--- | :--- |
| **Intitulé** | Conception et développement d'applications (SLAM) |
| **Coefficient** | 4 |
| **Durée** | 10 min exposé + ~30 min entretien jury |
| **Support** | Fiches descriptives de réalisations + portfolio numérique |
| **Évaluation** | Note sur 20, via grille d'aide à l'évaluation (Annexe VI-3) |

---

## 🎯 Les 3 Compétences Évaluées (Bloc 2 SLAM)

### COMPÉTENCE 1 — Concevoir et développer une solution applicative

| Indicateur de performance | ObsiLock | Appréciation |
| :--- | :---: | :--- |
| Analyse des besoins et cahier des charges | ✅ | CDC rédigé, OpenAPI v1 dès J1 |
| Modélisation (UML, MCD, MLD, MPD) | ✅ | MCD/MLD/MPD complets, diagrammes de classes Backend/Frontend, Use Case |
| Choix technologiques justifiés | ✅ | Java 17+JavaFX, PHP 8+Slim, LibSodium, JWT — tous documentés et justifiés |
| Qualité du code (structure, lisibilité) | ✅ | Pattern MVC, séparation Controller/Model/View, Singleton ApiClient |
| Tests et validation | ⚠️ | PHPUnit présent mais couverture partielle (à renforcer à l'oral) |
| Documentation technique complète | ✅ | Doc Technique + Doc BDD dans /DOCS/ |
| Documentation utilisateur | ✅ | Guide utilisateur complet avec FAQ |
| Déploiement fonctionnel | ✅ | Docker + Traefik, prod sur api.obsilock.iris.a3n.fr |
| Gestion de version (Git) | ✅ | Git, branches main/dev, CI minimale |

**→ Niveau estimé : Maîtrisé / Très maîtrisé ✅**

---

### COMPÉTENCE 2 — Assurer la maintenance corrective ou évolutive

| Indicateur de performance | ObsiLock | Appréciation |
| :--- | :---: | :--- |
| Diagnostic de bugs | ✅ | Corrections documentées (parser JWT, quota 0B, NullPointerException login) |
| Corrections apportées et documentées | ✅ | compte_rendu_obsilock.md liste les bugs corrigés et leurs solutions |
| Évolutions successives (versioning) | ✅ | Features ajoutées J1→J7 : Auth → Upload → Partage → Versioning → Thème Switch |
| Non-régression après corrections | ⚠️ | Tests E2E Postman/Newman présents, mais à mentionner activement à l'oral |
| Traçabilité des modifications | ✅ | Git, branches feature/*, TROUBLESHOOTING.md |
| Adaptation aux contraintes nouvelles | ✅ | Passage Button → ToggleButton CSS pour le thème, refacto ApiClient (JWT parsing) |

**→ Niveau estimé : Maîtrisé ✅**

---

### COMPÉTENCE 3 — Gérer les données

| Indicateur de performance | ObsiLock | Appréciation |
| :--- | :---: | :--- |
| Conception du schéma BDD | ✅ | 7 tables normalisées (users, folders, files, file_versions, shares, downloads_log, settings) |
| Requêtes SQL sécurisées (PDO / ORM) | ✅ | Medoo ORM → requêtes préparées, pas d'injection SQL possible |
| Cohérence et intégrité des données | ✅ | FOREIGN KEY + ON DELETE CASCADE, UNIQUE(file_id, version) |
| Index et optimisation | ✅ | INDEX sur email, user_id, token |
| Sécurité des données | ✅ | **Point fort majeur** : chiffrement LibSodium streaming, clé maître env, bcrypt |
| Sauvegarde et restauration | ✅ | backup.sh + restore.sh documentés et testés |
| Gestion des quotas | ✅ | Quota calculé dynamiquement, recalculateQuotaUsed(), bloquant à 100% |
| Journalisation | ✅ | downloads_log : chaque accès tracé (IP, user-agent, succès/échec) |

**→ Niveau estimé : Très maîtrisé ✅**

---

## 🏆 Points FORTS à mettre en avant à l'oral

### 1. 🔐 Chiffrement en streaming (argument béton)
> *"J'ai choisi un chiffrement par blocs de 8 Ko avec LibSodium (XSalsa20-Poly1305) plutôt que de charger le fichier en mémoire. Ça permet de traiter des fichiers de plusieurs gigaoctets sans saturer la RAM du serveur."*

### 2. 🏗️ Architecture découplée et stateless
> *"Le client JavaFX et l'API PHP sont totalement indépendants. Ils communiquent uniquement via des requêtes HTTP REST authentifiées par JWT, ce qui rend le système scalable et maintenable."*

### 3. 🐳 Déploiement professionnel Docker + Traefik
> *"L'ensemble de l'API est conteneurisé via Docker Compose (3 conteneurs : API, BDD, PhpMyAdmin) et exposé derrière Traefik comme reverse proxy. Ça garantit un déploiement reproductible sans problèmes de 'ça marche sur ma machine'."*

### 4. 📊 Versioning immutable des fichiers
> *"Chaque upload crée une version immuable dans file_versions avec checksum SHA-256. L'ancienne version est conservée, garantissant la traçabilité complète de l'historique."*

### 5. 🎨 UI Premium avec système de thème dynamique
> *"Le client JavaFX propose un système de thème (Obsidian/Emerald Green) qui s'applique à toutes les scènes via App.applyTheme(Scene). Le Toggle Switch est implémenté en CSS pur sans bibliothèque externe."*

---

## ⚠️ Points à RENFORCER ou PRÉPARER pour l'oral

### 1. Tests unitaires (Compétence 1 & 2)
**Risque :** Le jury peut demander la couverture de tests.  
**Ce qu'il faut préparer :**
- Mentionner les tests PHPUnit existants : `tests/Unit/EncryptionServiceTest.php`, `tests/Unit/UserRepositoryTest.php`, `tests/Integration/AuthIntegrationTest.php`
- Citer la collection Postman (tests E2E du scénario fil rouge)
- Dire : *"Les tests couvrent les modules critiques : chiffrement, authent, upload. La CI exécute Newman automatiquement."*

### 2. Justifier les choix technologiques (questions jury)
Prépare des réponses courtes et précises pour :
- **Pourquoi PHP + Slim et pas Node.js/Laravel ?** → Slim = micro-framework léger, adapté à une API REST pure sans overhead ; maîtrise du langage en formation.
- **Pourquoi JavaFX et pas une appli web ?** → Cahier des charges exigeait un client lourd Desktop (accès local, pas de dépendance navigateur).
- **Pourquoi LibSodium et pas OpenSSL ?** → LibSodium est plus moderne, plus résistant aux erreurs d'implémentation (nonce management automatique).
- **Pourquoi JWT et pas sessions PHP ?** → API stateless = pas de sessions serveur = scalable horizontalement.

### 3. Maintenance corrective (exemples concrets)
Prépare 2-3 bugs réels corrigés avec la démarche :
- **Bug JWT** : API renvoyait `"token"`, client Java cherchait `"jwt"` → correction du parser dans ApiClient.java
- **Bug quota affichait 0 B** : clés JSON `"used"/"total"` mal parsées → correction du mapping JSON
- **NullPointerException login** : Label manquant dans login2.fxml → restauration du composant FXML

### 4. Sécurité (le jury adore ça)
Être capable d'expliquer :
- Comment les FOREIGN KEY protègent l'intégrité (ex: si un user est supprimé, ses fichiers aussi)
- Pourquoi le `stored_name` est différent du `filename` (indirection = impossibilité de deviner l'URL physique)
- Le `UNIQUE(file_id, version)` garantit l'immutabilité des versions

---

## 📋 Checklist Conformité Dossier

### Documents obligatoires :
- ✅ Fiche descriptive de réalisation (Annexe 7-1-B) remplie
- ✅ Les 3 compétences du Bloc 2 SLAM cochées
- ✅ Période de réalisation renseignée (10/02/2026 – 22/03/2026)
- ✅ Modalités d'accès aux productions (URL prod, GitHub, /DOCS/)
- ✅ Descriptif technique avec schémas (MCD, UML, flux)
- ⚠️ **N° candidat** → à compléter avant dépôt
- ⚠️ **Nom de l'organisation support** → à compléter
- ⚠️ **Validation enseignant** → faire signer avant dépôt

### Portfolio numérique (accessible par le jury) :
- ✅ Code source accessible (Git)
- ✅ Application en production (api.obsilock.iris.a3n.fr)
- ✅ Documentation dans /DOCS/ (Markdown)
- ✅ openapi.yaml consultable sur Swagger Editor
- ⚠️ Prévoir un PDF des docs pour accès hors ligne lors de l'oral

---

## 🎤 Conseils pour l'oral (10 min exposé)

**Plan suggéré :**
1. **(1 min)** Contexte : Présenter le problème (besoin de sécurité des fichiers) et votre solution ObsiLock
2. **(2 min)** Architecture : Schéma Client/Serveur, stack technique, Docker
3. **(3 min)** Points techniques forts : Chiffrement LibSodium streaming + JWT + Versioning
4. **(2 min)** Demo ou captures : Upload, téléchargement, partage, interface thème
5. **(1 min)** Maintenance : citer 2 bugs corrigés avec démarche
6. **(1 min)** Bilan : difficultés rencontrées, ce que vous avez appris

**À NE PAS FAIRE :**
- Lire ses notes
- Dire "j'ai pas eu le temps de faire X"
- Rester vague sur les choix technologiques

---

## 🔢 Estimation de la note

| Compétence | Niveau atteint | Estimation |
| :--- | :--- | :--- |
| Concevoir et développer | Très maîtrisé | 16-18/20 |
| Maintenance corrective/évolutive | Maîtrisé | 14-16/20 |
| Gérer les données | Très maîtrisé | 17-19/20 |
| **GLOBALE (qualité oral incluse)** | **Selon prestation** | **15-18/20** |

> Le projet ObsiLock répond à **toutes les compétences** du référentiel E5 SLAM. Le niveau technique est clairement au-dessus du minimum attendu. La note finale dépendra principalement de la **qualité de l'oral** et de la capacité à argumenter les choix techniques face au jury.
