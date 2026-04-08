# 🆘 ANTISÈCHE ULTIME POUR L'ORAL — "COMMENT FONCTIONNE MON CODE"

> Ce guide est fait pour toi. Il explique avec des mots **très simples** comment ton application fonctionne techniquement, comment les différentes parties du code communiquent entre elles, et quelles parties tu dois absolument maîtriser.

## 🧭 1. L'architecture globale (Le "Client / Serveur")

Ton application (ObsiLock) est découpée en deux gros morceaux qui ne se mélangent jamais :

1. **Le Frontend (Le Client JavaFX)** : C'est ce que l'utilisateur installe sur son ordinateur. C'est l'interface visuelle (les boutons, les fenêtres, le thème clair/sombre).  
   *Où est ce code ?* Dans le dossier `coffreFortJava-main/src/main/java/com/coffrefort/client/`.
2. **Le Backend (L'API PHP & BDD)** : C'est le cerveau qui tourne sur le serveur (chez *iris.a3n.fr*). Il gère la vraie logique, la base de données (MariaDB) et sauvegarde les fichiers.
   *Où est ce code ?* Dans le dossier `src/` (les Controlleurs, Modèles et Services) de la racine.

**Comment ils se parlent ?**
Le Client JavaFX et l'API PHP se parlent via des requêtes HTTP (comme ton navigateur web). Le client demande "Donne-moi mes fichiers" (`GET /files`), et l'API répond en lui envoyant du texte au format **JSON**.

---

## 🔑 2. Comment fonctionne l'Authentification (Le code JWT)

Le jury va à *100%* te demander comment les utilisateurs se connectent. Tu dois savoir que tu utilises **JWT (JSON Web Tokens)** et non pas les sessions PHP (`$_SESSION`).

**Ce que tu dois expliquer à l'oral :**
1. Quand l'utilisateur tape son email et mot de passe dans l'interface JavaFX, JavaFX envoie une requête `POST /auth/login` au serveur PHP.
2. Le code PHP (dans `AuthController.php`) vérifie si le mot de passe est bon grâce à la fonction `password_verify` (les mots de passe sont hachés en base de données avec `bcrypt`).
3. Si c'est bon, le serveur PHP crée un "Badge" électronique appelé **Token JWT**. Ce token contient l'ID du user et expire dans 1 heure. Il est chiffré grâce à une clé secrète.
4. L'API donne ce badge au client JavaFX. Le client JavaFX conserve ce badge en mémoire (`UserSession.java`).
5. Ensuite, à **CHAQUE FOIS** que JavaFX demande une action (comme voir les dossiers), il montre ce badge dans l'en-tête de sa requête HTTP (`Authorization: Bearer <token>`).
6. Le JWT Validator (dans le backend) vérifie si le badge est valide.

---

## 🔒 3. Le gros morceau de code : Le Chiffrement (LibSodium)

C'est LE point fort de ton projet. Si tu maîtrises ça, tu as une excellente note.
La règle absolue d'ObsiLock c'est : **Aucun fichier n'est conservé en clair sur le serveur.** Si un hacker vole le disque dur du serveur, les fichiers sont illisibles.

**Le code PHP qui fait ça est dans `EncryptionService.php` :**
1. L'utilisateur lance un Upload.
2. `EncryptionService` génère une clé au hasard (Une "Clé de Contenu" de 32 octets).
3. Il lit le fichier par petits morceaux de **8 Ko** et le chiffre (avec la méthode *ChaCha20-Poly1305* de la librairie **LibSodium**). *POURQUOI par morceaux ? Pour ne pas saturer la RAM du serveur si l'utilisateur envoie une vidéo de 2 Go.*
4. Il enregistre ce fichier charabia sur le disque du serveur (`/storage/uploads/xyz_fichier.enc`).
5. **Le problème** : L'utilisateur doit pouvoir redéchiffrer ce fichier plus tard. Donc l'API prend la petite "Clé de Contenu" générée à l'étape 2, et la chiffre à son tour avec la **"Clé Maître du Serveur"** (`ENCRYPTION_KEY` trouvée dans le fichier `.env`).
6. Cette clé de contenu chiffrée (appelée `key_envelope`) est sauvegardée dans la base de données.

**Pour le téléchargement, c'est l'inverse :**
Le code prend la `key_envelope` en base, la déchiffre avec la Clé Maître, retrouve la petite Clé de Contenu, déchiffre le fichier par morceaux de 8Ko et l'envoie à l'utilisateur.

---

## 🗄️ 4. La Base de données (Le code PHP avec Medoo)

Tu n'utilises pas `PDO` directement avec des `SELECT * FROM...`.
Tu utilises **Medoo**, un "micro-ORM" très léger. Il simplifie les requêtes SQL, et SURTOUT, il te protège automatiquement contre les failles d'Injection SQL (via les requêtes préparées).

Les dossiers dans `src/Model/` comme `UserRepository.php` ou `Share.php` font parler ton PHP a la base de données :
`$this->db->get('files', '*', ['id' => $id]);` (Au lieu de taper tout le SQL Select).

---

## 🔗 5. Le Partage public (Les liens token)

Quand on génère un lien (exemple: `share.html?token=Abcde...`)
1. Le Backend (dans `Share.php`) crée ce **token opaque** complètement au hasard et génère une **signature sécurisée** (pour être sûr que le lien n'est pas falsifié).
2. Ce token est lié à l'ID `file_id` du fichier en base. Il a un `max_uses` (nombre de clics max) et une date de péremption.
3. Quand la personne télécharge le fichier via le lien, le code (dans `ShareController.php`) fait un `decrementUses` (retire 1 utilisation) *atomiquement*. Et il écrit une ligne dans les logs.

---

## 🖥️ 6. L'interface (Le code JavaFX)

L'aspect de ton appli :
- Tu utilises des fichiers `FXML` (ex: `myshares.fxml`). C'est comme du HTML mais pour Java. Ça dessine l'écran.
- Des Fichiers `Controllers` (ex: `MySharesController.java`). C'est ce qui réagit quand on clique sur un bouton dans le FXML. Le controller appelle `ApiClient.java` pour envoyer l'ordre à l'API PHP.
- Le fameux **Toggle de Thème (Clair/Sombre)** : C'est géré en **pur CSS**. Quand on clique sur l'interrupteur, le code (`App.toggleTheme`) change juste les balises, et JavaFX applique le `emerald-theme.css` à la place du `obsidian-theme.css` sur la scène entière.

---

## 🐳 7. Pourquoi Docker ? (Question Jury très fréquente)

Le jury te demandera pourquoi tu as "Dockerisé" l'appli backend.
**Réponse à apprendre :**
*"Avant, pour installer l'API sur un serveur, il fallait installer manuellement Apache, PHP 8.2, l'extension LibSodium, MariaDB, créer la base, etc... Ça prenait du temps et s'il y avait une différence de version de PHP entre mon ordi et le serveur, le code explosait.*
*Avec mon fichier `docker-compose.yml`, je décris l'infrastructure dans du code. Sur le serveur, je tape `docker-compose up -d` et il télécharge ou installe TOUT (l'API, MySQL, PhpMyAdmin) de manière 100% identique et isolée."*

---

### 🔥 COMMENT RÉVISER ? 🔥
1. **Lis et relis** cette page 3 fois jusqu'à ce que les concepts te paraissent logiques.
2. Ouvre le fichier PHP **`src/Service/EncryptionService.php`** et regarde la fonction `encryptFile`. Re-vérifie comment l'API lit le fichier octet par octet avec la boucle `while (!feof($in))`. 
3. Ouvre ton fichier JavaFX **`coffreFortJava-main/src/main/java/com/coffrefort/client/ApiClient.java`** et regarde comment il construit des URL (`HttpClient`, `HttpRequest`) pour parler à ton API.
4. Les profils des jurys (souvent anciens dévs) adorent poser des questions sur "Et si je trouve ton fichier physique sur le disque du serveur, je peux l'ouvrir ?". Tu sais maintenant que la réponse ferme est **NON, car il est illisible à cause de LibSodium (XSalsa20), seule la clé est au chaud dans le fichier .env du serveur.**
