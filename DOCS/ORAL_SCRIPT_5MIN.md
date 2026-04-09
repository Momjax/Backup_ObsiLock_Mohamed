# 🎤 SCRIPT ORAL SERRÉ - 5 MINUTES CHRONO (ObsiLock)

> Ce texte est calibré pour durer environ 5 minutes à un débit de parole normal. Lis-le à voix haute en chronométrant pour t'ajuster.

---

## 🕒 1. INTRODUCTION & CONTEXTE (45 secondes)
"Bonjour, je m'appelle [Ton Nom]. Aujourd'hui, je vous présente **ObsiLock**, un coffre-fort numérique sécurisé. 

Le constat est simple : la plupart des solutions cloud actuelles (comme Google Drive) possèdent les clés de chiffrement de vos fichiers. Si leur serveur est compromis, vos données sont lisibles. **ObsiLock** répond à cette faille par une approche de **'Chiffrement au Repos'** : aucun fichier n'est stocké en clair sur le serveur, garantissant une confidentialité totale pour l'utilisateur."

## 🏗️ 2. L'ARCHITECTURE TECHNIQUE (1 minute)
"Pour ce projet, j'ai opté pour une architecture **Client/Serveur découplée et Stateless**.
- **Côté Client :** J'ai développé une application desktop en **JavaFX 17**. Ce choix permet d'avoir une interface performante, capable de traiter les fichiers localement avant l'envoi.
- **Côté Serveur :** J'utilise une **API REST en PHP 8** avec le framework Slim. Elle est Stateless : elle ne gère pas de sessions classiques mais utilise des **Tokens JWT** (JSON Web Tokens) pour l'authentification.
- **Infrastructure :** Tout est containerisé sous **Docker**, ce qui m'a permis d'assurer une portabilité parfaite entre mon développement et mon environnement de production."

## 🔐 3. LA SÉCURITÉ : LE CŒUR DU PROJET (1 minute 15)
"Le point nodal d'ObsiLock est sa gestion de la cryptographie. 
J'utilise la bibliothèque **LibSodium** avec l'algorithme **XSalsa20-Poly1305**. 

Ce qui est innovant ici, c'est le **streaming de chiffrement** : 
1. Le fichier n'est jamais chargé entièrement en mémoire vive (RAM). 
2. Il est traité par **blocs de 8 Ko**. 
3. Chaque fichier possède sa propre 'Clé de contenu', elle-même chiffrée par une 'Clé Maître' serveur. C'est ce qu'on appelle le **chiffrement par enveloppe**.

Même en cas de vol de la base de données, les fichiers restent des boîtes noires mathématiquement inviolables."

## 🖱️ 4. DÉMONSTRATION ÉCLAIR (1 minute 30)
"Passons à la pratique. 
- Je me connecte via l'interface JavaFX : mon token JWT est généré et stocké le temps de la session.
- Vous pouvez voir ici mon **thème dynamique** (Switch Dark/Light) géré en CSS.
- J'upload maintenant un fichier : vous ne voyez qu'une barre de progression, mais en arrière-plan, l'API est en train de fragmenter et chiffrer la donnée.
- Enfin, je génère un **lien de partage**. Ce lien est à usage unique ou limité dans le temps, grâce à une gestion de jetons opaques en base de données."

## 🏁 5. CONCLUSION (30 secondes)
"Pour conclure, ObsiLock est une solution robuste validée par une suite de **tests unitaires et d'intégration** (PHPUnit) que je peux vous montrer. Le projet a atteint ses objectifs de sécurité et de performance. Les prochaines étapes seraient d'implémenter le chiffrement asymétrique pour permettre le partage direct entre comptes utilisateurs.

Je vous remercie de votre écoute, je suis prêt pour vos questions."

---

# 📚 LEXIQUE TECHNIQUE (Les définitions à connaître)

> Si le jury te demande "C'est quoi ce mot ?", voici ta réponse.

| Terme | Définition simple |
| :--- | :--- |
| **Chiffrement au Repos** | Les données sont chiffrées AVANT d'être écrites sur le disque dur. Même si on vole le disque, les données sont illisibles. |
| **Architecture Découplée** | Le client (Java) et le serveur (PHP) sont indépendants. On pourrait changer l'un sans toucher à l'autre. |
| **Stateless (Sans état)** | Le serveur ne garde aucune session en mémoire. Chaque requête doit contenir sa propre preuve d'identité (JWT). |
| **API REST** | Interface qui permet à deux logiciels (Java et PHP) de se parler via des requêtes Web standards. |
| **JWT (Json Web Token)** | Un "badge d'accès" numérique signé. Il contient l'identité de l'utilisateur et prouve qu'il est connecté. |
| **Docker / Container** | Une boîte virtuelle qui contient tout ce qu'il faut pour faire tourner l'appli (PHP, MariaDB). Ça marche sur n'importe quel ordi. |
| **LibSodium** | La bibliothèque de sécurité la plus moderne en PHP 8 pour crypter des données. |
| **XSalsa20-Poly1305** | L'algorithme utilisé : **XSalsa20** (Cadenas rapide) + **Poly1305** (Sceau de garantie que rien n'a été modifié). |
| **Streaming (8 Ko)** | Le fichier est traité par petits paquets de 8 Kilo-octets. Cela évite de saturer la mémoire vive (**RAM**) du serveur. |
| **Chiffrement par Enveloppe** | On chiffre le fichier avec une clé, puis on chiffre CETTE clé avec une autre (Clé Maître). C'est la double sécurité. |
| **Soft Delete** | Suppression "douce". On ne supprime pas la ligne de la BDD, on met juste un drapeau `is_deleted = 1` (système de corbeille). |
| **Versioning** | Système qui permet de garder les anciennes versions d'un fichier après une modification. |
| **Tests Unitaires (PHPUnit)**| Des petits scripts qui testent chaque fonction une par une pour être sûr qu'il n'y a pas de bug caché. |

---

# 🛠️ MANIPS TECHNIQUES (À montrer si on te demande)

### 1. Comment montrer les Backups ?
Si le jury demande : *"Comment gérez-vous la sauvegarde ?"*
- **Réponse :** *"J'ai automatisé cela via un script Bash qui effectue un dump de la base MariaDB et archive les fichiers chiffrés."*
- **Action :** Montre le fichier `backup.sh` à la racine du projet.
- **Commande à taper (si besoin) :** `./backup.sh` (Cela va créer un dossier `backups/` avec ton SQL et tes fichiers).

### 2. Comment prouver que ton code fonctionne ? (Tests)
Si le jury demande : *"Comment garantissez-vous que votre chiffrement est fiable ?"*
- **Réponse :** *"J'ai mis en place une suite de tests automatisés avec PHPUnit qui valident le processus de login, d'upload et de chiffrement à chaque modification du code."*
- **Action :** Ouvre un terminal et tape : **`./run_tests.sh`**
- **Résultat :** Les points verts et le message "OK" prouvent que ton code est certifié sans erreurs.


