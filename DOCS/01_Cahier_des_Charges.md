# 🛡️ 01. CAHIER DES CHARGES - ObsiLock

## 1. Contexte et Problématique
**ObsiLock** est un coffre-fort numérique sécurisé. 
*   **Problème :** Les solutions cloud (Drive, Dropbox) possèdent les clés de chiffrement et peuvent lire vos fichiers.
*   **Solution :** ObsiLock utilise le "chiffrement au repos". Aucun fichier n'est stocké en clair sur le serveur.

## 2. Architecture Globale
Le projet repose sur une architecture **Client/Serveur découplée et Stateless**.

```mermaid
flowchart LR
    subgraph Frontend [Client Bureau]
        Java("☕ JavaFX 17<br/>Interface Riche")
    end
    
    subgraph API [Serveur Backend]
        PHP("🐘 PHP 8 (Slim)<br/>Chiffrement Flux")
        DB[("🐬 MariaDB<br/>Métadonnées")]
        FS[("📂 Stockage<br/>Fichiers .enc")]
    end
    
    Java -- "HTTPS / JSON / JWT" --> PHP
    PHP <--> DB
    PHP <--> FS
```

## 3. Analyse des Besoins
### Besoins Fonctionnels :
- **Authentification :** JWT (Json Web Tokens).
- **Gestion Fichiers :** Upload, Téléchargement, Dossiers, Arborescence.
- **Sécurité :** Soft Delete (Corbeille) et Versioning (Historique).
- **Partage :** Liens publics avec limite d'utilisation.
- **Thème :** Switch Dark/Light en pur CSS.

### Besoins Non-Fonctionnels :
- **Sécurité :** Algorithme LibSodium (XSalsa20-Poly1305).
- **Performance :** Streaming par blocs de 8 Ko (pas de saturation RAM).
- **Portabilité :** Environnement Dockerisé.

## 4. Planning de Réalisation
```mermaid
gantt
    title Cycle de Développement ObsiLock
    dateFormat  YYYY-MM-DD
    section Analyse
    Conception & MCD        :done, 2026-02-10, 2d
    section Backend
    API & Auth JWT          :done, 2026-02-12, 3d
    LibSodium Streaming     :done, 2026-02-15, 2d
    section Frontend
    JavaFX UI & Thème       :done, 2026-02-17, 4d
    section Finalisation
    Docker & Tests          :done, 2026-02-21, 2d
```
