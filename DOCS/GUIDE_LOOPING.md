# Guide Looping - Architecture en Arborescence (Simplifiée)

Utilise ce guide pour créer ton nouveau MCD sans cycle.

## 1. Création des Entités

### UTILISATEUR
- **user_id** : Compteur, Identifiant
- ... (email, password, etc.)

### DOSSIER
- **folder_id** : Compteur, Identifiant
- **nom** : Texte (Variable 255)
- **is_root** : Booléen (ou Entier 0/1)
- **created_at** : Date-Heure

### FICHIER
- **file_id** : Compteur, Identifiant
- **nom_original** : Texte (Variable 255)
- **taille** : Numérique (Entier)
- ... (mime_type, checksum, etc.)

---

## 2. Création des Associations (CIF)

1. **DOSSIER (1,1) --- [CIF] --- UTILISATEUR (0,n)**
   - *Signification :* Chaque dossier appartient à un utilisateur.
2. **DOSSIER (1,1) --- [PARENT] --- DOSSIER (0,n)**
   - *Action :* Relie l'entité DOSSIER sur elle-même (Relation réflexive).
   - *Signification :* Un dossier peut avoir un dossier parent (sauf la racine).
3. **FICHIER (1,1) --- [CONTENIR] --- DOSSIER (0,n)**
   - *Signification :* Un fichier est obligatoirement dans un dossier.
4. **FICHIER (0,1) --- [SOURCE] --- FICHIER (0,n)**
   - *Action :* Relie l'entité FICHIER sur elle-même.
   - *Signification :* Permet de gérer la duplication (lien vers le fichier source).
5. **VERSION_FICHIER (1,1) --- [HISTORIQUE] --- FICHIER (1,n)**
6. **PARTAGE (1,1) --- [AUTEUR] --- UTILISATEUR (0,n)**

---

## 3. Héritage XT (Triangle)
- Relie **PARTAGE** (Haut) à **FICHIER** et **DOSSIER** (Bas).
- Coche **X** et **T**.

## ✅ Pourquoi c'est mieux ?
- **Plus de cycle** : Le lien Utilisateur -> Fichier passe par le Dossier.
- **Plus de contrainte "S"** : Le schéma est "plat" et logique.
- **Vrai explorateur** : On peut faire des dossiers dans des dossiers comme sur PC.
