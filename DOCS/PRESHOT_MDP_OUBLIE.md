# 💡 ANALYSE : Implémentation du "Mot de passe oublié"

> Ce document te sert de base si le jury te donne cette problématique pendant les 30 minutes de préparation.

## 1. Analyse de la problématique
La perte d'un mot de passe est critique sur ObsiLock car le mot de passe utilisateur est souvent lié au déchiffrement (même si ici on utilise une MasterKey serveur pour simplifier, dans un système plus poussé, la clé est dérivée du mot de passe).

**Contrainte :** Ne jamais stocker de mot de passe en clair.

## 2. Solutions techniques possibles

### Solution A : Le Token par Email (La plus courante)
1. L'utilisateur saisit son email.
2. Le serveur génère un **Token temporaire** (ex: `bin2hex(random_bytes(32))`) stocké en BDD avec une date d'expiration (+15 min).
3. Envoi d'un email contenant le lien : `https://obsilock.fr/reset-password?token=XYZ`.
4. Le client clique, saisit un nouveau MDP. Le serveur vérifie le token et met à jour le hash Bcrypt.

### Solution B : Questions de sécurité (Moins recommandé)
L'utilisateur répond à des questions prédéfinies (ex: "Nom de votre premier animal"). 
- *Inconvénient :* Facile à deviner par ingénierie sociale ou par des proches.

## 3. Benchmark Comparatif des Solutions

| Critères | Solution A (Token Email) | Solution B (Questions) |
| :--- | :--- | :--- |
| **Niveau de Sécurité** | ⭐⭐⭐⭐ (Élevé) | ⭐⭐ (Faible) |
| **Expérience Utilisateur** | Fluide et habituelle. | Fastidieuse (mémoire). |
| **Facilité Technique** | Moyenne (besoin de SMTP). | Simple (juste des champs BDD). |
| **Robustesse** | Très bonne (expiration auto). | Faible (permanent). |
| **Conformité RGPD** | Recommandé. | Déconseillé (données perso). |

## 4. Analyse comparative (Choix retenu : Solution A)
J'ai choisi la Solution A car elle est la norme dans l'industrie. Même si elle demande plus de configuration serveur (serveur de mails), elle garantit que seul le possesseur de l'email peut reprendre le contrôle du compte, ce qui est indispensable pour un coffre-fort numérique comme ObsiLock.

## 4. Plan de mise en œuvre (60 min de code)
1. **BDD :** Ajouter une table `password_resets` (email, token, expires_at).
2. **Backend (API) :** 
    - Route `POST /auth/forgot` : Génère le token et "envoie" (ou simule) l'email.
    - Route `POST /auth/reset` : Vérifie le token et fait le `UPDATE users SET password = ...`.
3. **Frontend (JavaFX) :** 
    - Ajouter un lien "Mot de passe oublié ?" sur la fenêtre de Login.
    - Créer une petite fenêtre pour saisir le nouveau mot de passe.

## 5. Vocabulaire "Masterclasse" pour le jury
"Pour cette problématique, je propose une approche **Stateless** basée sur des jetons à usage unique. Cela évite de stocker des états temporaires complexes et garantit que seul le propriétaire légitime du compte peut réinitialiser son accès grâce au protocole **SMTP**."
