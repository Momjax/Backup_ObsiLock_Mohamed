# 📖 Guide Utilisateur — ObsiLock : Coffre-Fort Numérique

![Logo ObsiLock](/home/mohamed/.gemini/antigravity/brain/5aca6d1a-2316-4b38-9d90-a42f42cf1cd1/Logo_CryptoVault_transparent.png)

---

> [!NOTE]
> Ce guide est destiné aux utilisateurs finaux de l'application **ObsiLock**. Aucune connaissance technique n'est requise.

---

## 1. Premiers Pas

### 1.1 Lancer l'application

Lancez l'exécutable **ObsiLock** (fichier `.jar` ou raccourci). L'écran de connexion s'affiche automatiquement.

### 1.2 Créer un compte

1. Sur l'écran de connexion, cliquez sur **"Vous n'avez pas de compte ? S'inscrire"**
2. Renseignez votre adresse email et un mot de passe (min. 8 caractères)
3. Cliquez sur **"Créer mon compte"**
4. Vous serez automatiquement redirigé vers votre espace personnel

### 1.3 Se connecter

1. Entrez votre **email** et **mot de passe** dans les champs correspondants
2. Cochez **"Afficher le mot de passe"** si besoin
3. Cliquez sur **"Se Connecter"**

> [!TIP]
> Si vous avez oublié votre mot de passe, cliquez sur **"Mot de passe oublié ?"** pour lancer la procédure de récupération.

---

## 2. Interface Principale

Une fois connecté, l'interface se divise en trois zones :

```
┌─────────────────────────────────────────────────────────────────┐
│  🛡️ ObsiLock        [utilisateur@email.com]    [Switch Thème]  │
├───────────────┬─────────────────────────────────────────────────┤
│               │                                                 │
│  📁 Dossiers  │   Liste des fichiers                            │
│  ├── Perso    │   Nom | Taille | Date | Version                 │
│  ├── Travail  │                                                 │
│  └── Photos   │                                                 │
│               │                                                 │
│  💾 Stockage  ├─────────────────────────────────────────────────┤
│  [===---] 40% │  [📤 Uploader]  [🔗 Partager]  [🗑 Supprimer] │
│               │                                                 │
│  [🏠 Accueil] │                                                 │
│  [🗑 Corbeille]│                                                │
│  [📤 Partages]│                                                 │
│  [🚪 Déco]    │                                                 │
└───────────────┴─────────────────────────────────────────────────┘
```

### Zones de l'interface :

| Zone | Description |
| :--- | :--- |
| **Barre latérale gauche** | Arborescence des dossiers, barre de quota, boutons de navigation |
| **Zone centrale** | Tableau des fichiers du dossier sélectionné |
| **Barre d'actions** | Boutons Upload, Partager, Supprimer |

---

## 3. Gestion des Fichiers

### 3.1 Uploader un fichier

1. Naviguez dans le dossier de destination (cliquez dessus dans l'arborescence)
2. Cliquez sur **"📤 Uploader"**
3. Sélectionnez un ou plusieurs fichiers sur votre ordinateur
4. Attendez la progression — vos fichiers sont **automatiquement chiffrés** avant envoi

> [!IMPORTANT]
> Vos fichiers sont chiffrés dès qu'ils atteignent le serveur. Même l'administrateur ne peut pas lire leur contenu.

### 3.2 Télécharger un fichier

1. Cliquez sur un fichier dans la liste pour le sélectionner
2. Double-cliquez dessus ou faites **clic droit → Télécharger**
3. Choisissez l'emplacement de sauvegarde sur votre ordinateur

### 3.3 Renommer un fichier ou dossier

1. Sélectionnez le fichier ou dossier
2. Faites **clic droit → Renommer**
3. Tapez le nouveau nom et confirmez

### 3.4 Supprimer un fichier

1. Sélectionnez le fichier
2. Cliquez sur **"🗑 Supprimer"** ou faites **clic droit → Supprimer**
3. Le fichier est envoyé dans la **Corbeille** (pas encore effacé)

---

## 4. Gestion des Dossiers

### 4.1 Créer un nouveau dossier

1. Cliquez sur **"+ Nouveau dossier"** dans la barre latérale
2. Saisissez le nom du dossier
3. Cliquez sur **"Créer"**

### 4.2 Navigation dans les dossiers

- **Cliquez** sur un dossier dans l'arborescence gauche pour voir son contenu
- Cliquez sur **"🏠 Accueil"** pour revenir à la racine
- Un fil d'Ariane indique votre position actuelle

---

## 5. Gestionnaire de Versions

ObsiLock conserve un **historique complet** de chaque fichier. Chaque fois que vous uploader une nouvelle version d'un fichier existant, l'ancienne est conservée.

### Voir l'historique d'un fichier

1. Sélectionnez un fichier dans la liste
2. Faites **clic droit → Détails / Versions**
3. Une fenêtre affiche toutes les versions avec : Numéro, Date, Taille, Checksum

### Mettre à jour un fichier (nouvelle version)

1. Sélectionnez le fichier à mettre à jour
2. Cliquez sur **"Uploader une nouvelle version"**
3. Choisissez le fichier mis à jour sur votre ordinateur

> [!NOTE]
> Les liens de partage pointent toujours vers la **dernière version** du fichier, automatiquement.

---

## 6. Partage de Fichiers

### 6.1 Partager un fichier

1. Sélectionnez un fichier dans la liste
2. Cliquez sur **"🔗 Partager"**
3. Renseignez (au choix) :
   - **Label** : description du partage (ex: "Rapport annuel")
   - **Date d'expiration** : le lien sera invalide après cette date
   - **Nombre max d'utilisations** : le lien s'invalide après N téléchargements
4. Cliquez sur **"Créer le lien"**
5. L'URL est copiée dans votre presse-papiers — envoyez-la à votre destinataire

### 6.2 Gérer ses partages

1. Cliquez sur **"📤 Mes partages"** dans la barre latérale
2. Vous voyez tous vos liens avec leur statut (Actif / Expiré / Révoqué)
3. **Pour révoquer un lien** : cliquez sur **"Révoquer"** — le lien devient immédiatement invalide

### 6.3 Accès pour le destinataire

Le destinataire reçoit un lien du type `https://api.obsilock.../s/{token}`.  
En cliquant dessus depuis un navigateur, il verra les détails du fichier et pourra le télécharger directement, **sans compte ObsiLock**.

---

## 7. La Corbeille

### Voir les éléments supprimés

Cliquez sur **"🗑 Corbeille"** dans la barre latérale gauche.

### Restaurer un élément

1. Sélectionnez l'élément dans la corbeille
2. Cliquez sur **"♻️ Restaurer"** — il réapparaît dans son dossier d'origine

### Supprimer définitivement

> [!CAUTION]
> Cette action est **irréversible**. Le fichier sera effacé sans possibilité de récupération.

1. Sélectionnez l'élément dans la corbeille
2. Cliquez sur **"🗑 Supprimer définitivement"**
3. Confirmez dans la boîte de dialogue

---

## 8. Gestion du Quota

La **barre de stockage** dans la barre latérale vous indique votre espace utilisé.

| Couleur | Signification |
| :--- | :--- |
| 🟢 Vert | Espace suffisant |
| 🟠 Orange | Quota à 80% utilisé — pensez à libérer de l'espace |
| 🔴 Rouge | Quota plein — impossible d'uploader de nouveaux fichiers |

**Pour libérer de l'espace** : supprimez des fichiers ET **videz la corbeille** (les fichiers en corbeille comptent toujours dans votre quota).

**Quota par défaut** : 50 Mo par compte. Contactez votre administrateur pour une augmentation.

---

## 9. Changer le Thème

ObsiLock propose deux thèmes visuels :

| Thème | Description |
| :--- | :--- |
| 🌑 **Obsidian** (sombre) | Fond noir, accents vert fluo — mode recommandé en soirée |
| 🌿 **Emerald** (clair) | Fond blanc, tons verts — mode recommandé en journée |

**Pour changer de thème** :
- Sur l'écran de connexion : cliquez sur l'**interrupteur** en haut à droite
- Dans l'application : cliquez sur l'**interrupteur** dans la barre latérale (à côté du logo)

> [!TIP]
> Le thème choisi s'applique instantanément à toutes les fenêtres ouvertes (dialogues, pop-ups, etc.)

---

## 10. FAQ — Questions Fréquentes

**Q : Mes fichiers sont-ils protégés sur le serveur ?**  
R : Oui. Chaque fichier est chiffré avec l'algorithme **LibSodium (XSalsa20-Poly1305)** avant d'être écrit sur le disque du serveur. Même en cas d'accès physique au serveur, les fichiers sont illisibles sans la clé de chiffrement.

**Q : Que se passe-t-il si j'oublie mon mot de passe ?**  
R : Cliquez sur "Mot de passe oublié ?" sur l'écran de connexion. Une procédure de réinitialisation vous sera envoyée par email.

**Q : Mon quota est plein, que faire ?**  
R : Supprimez des fichiers dans l'application ET videz la corbeille. Si vous avez besoin de plus d'espace, contactez votre administrateur.

**Q : Les liens de partage sont-ils sécurisés ?**  
R : Les liens sont des tokens aléatoires opaques — ils ne contiennent aucune information sur votre compte. Vous pouvez les révoquer à tout moment et configurer une date d'expiration automatique.

**Q : Est-ce que la personne à qui je partage un fichier peut le modifier ?**  
R : Non. Les liens de partage donnent un accès en **lecture seule** (téléchargement uniquement).

**Q : Que deviennent mes fichiers si je ferme l'application pendant un upload ?**  
R : L'upload est interrompu. Le fichier partiel n'est pas enregistré. Vous devrez relancer l'upload depuis le début.

**Q : Quelle est la taille maximale d'un fichier ?**  
R : Il n'y a pas de limite de taille par fichier (en dehors de votre quota). Le chiffrement se fait en streaming (blocs de 8 Ko) pour ne pas surcharger la mémoire du serveur.
