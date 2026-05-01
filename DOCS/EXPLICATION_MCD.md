# Aide-Mémoire pour l'Oral : Explication du MCD ObsiLock

Voici comment expliquer ton schéma au jury du BTS SIO. Utilise ces termes techniques pour montrer que tu maîtrises ton sujet.

## 1. La Structure de Base (Les CIF)
- **Utilisateur / Fichier / Dossier** : "Le système est basé sur une propriété stricte. Chaque fichier et chaque dossier appartient à un utilisateur unique (cardinalité 1,1)."
- **Versioning (Historique)** : "J'ai mis en place une relation 1,N entre Fichier et Version. Cela garantit que chaque fichier possède au moins sa version initiale et permet de conserver l'historique des modifications."
- **Logs** : "Chaque accès via un partage est tracé dans la table Logs pour assurer l'auditabilité du système."

## 2. Le Cycle et la Contrainte "S" (Le point crucial)
**Question du jury : "Pourquoi y a-t-il un triangle entre Utilisateur, Dossier et Fichier ?"**
- **Réponse** : "C'est un cycle fonctionnel. Un fichier peut être lié à un utilisateur de deux manières : soit directement, soit via son dossier parent."
- **Explication de la contrainte S** : "Pour éviter toute incohérence, j'ai ajouté une **Contrainte de Simultanéité (S)**. Elle impose que le propriétaire du fichier (via CIF) soit obligatoirement le même que le propriétaire du dossier (via CIF2). Cela empêche qu'un utilisateur puisse mettre un fichier dans le dossier d'un autre utilisateur."

## 3. L'Héritage "XT" (La Spécialisation)
**Question du jury : "Comment gérez-vous le partage de différents types d'éléments ?"**
- **Réponse** : "J'ai utilisé un héritage de type **XT (Exclusion et Totalité)**."
- **Exclusion (X)** : "Un partage concerne soit un fichier, soit un dossier, mais jamais les deux simultanément."
- **Totalité (T)** : "Un partage ne peut pas exister 'dans le vide', il est forcément rattaché à l'un des deux éléments."

## 4. Les Cardinalités spécifiques
- **Le (1,1) de Fichier vers Dossier** : "J'ai pris la décision architecturale d'interdire les fichiers 'volants' à la racine. Chaque fichier DOIT obligatoirement appartenir à un dossier créé par l'utilisateur, ce qui simplifie la gestion des droits et l'interface."
- **Le (0,n) de Dossier vers Fichier** : "Un dossier peut bien sûr être vide ou contenir plusieurs fichiers."

## 5. Passage au MLD (Modèle Logique)
- "Lors de la génération du MLD, toutes les relations avec un '1' (CIF) se transforment en **Clés Étrangères (FK)**. Par exemple, `user_id` migre dans la table `Fichier` et la table `Dossier`."
- "L'héritage XT permet de simplifier les requêtes de partage en utilisant une structure commune pour les tokens et les expirations."
