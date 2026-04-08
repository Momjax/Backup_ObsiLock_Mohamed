# 4. GUIDE UTILISATEUR — ObsiLock

![Logo ObsiLock](https://img.shields.io/badge/ObsiLock-Client_Lourd-green?style=for-the-badge)

*ObsiLock — Manuel Client 2026*  

---

> Ce manuel d'utilisation présente le fonctionnement fonctionnel courant du Coffre Fort Numérique "ObsiLock". Il est dédié au mode Client, lancé depuis l'application JavaFX sur système d'exploitation Bureau (Windows/Linux/MacOS).

## 🚀 1. Découverte de l'Interface

Une fois votre connexion (Email/Mot de passe sécurisé complexe) accréditée par la plateforme en ligne, l'interface du "Dashboard ObsiLock" s'ouvre. Ce composant visuel fonctionne selon 3 axes majeurs :
1. **La Fenêtre Centrale "Zone Active" :** Là où se dessinent tous vos sous-dossiers et Fichiers consultables.
2. **Le Panneau Latéral Gauche (Navigation) :** Le module principal "Arborescence Système" reprenant le fil d'Ariane de vos accès. Juste en dessous réside une **Barre Critique Dynamique des Quotas** vous renseignant via la logique de l'indicateur thermique de la place qu'il vous reste (Vert → Orange → Rouge).
3. **Le Menu TopBar Clic Droit Contextuel :** Action sur les objets (Upload, Partager le lien, Supprimer dans la Corbeille). 

---

## 💻 2. Gérer vos Fichiers au quotidien

### Sécuriser un nouveau Fichier ("Uploader")
- Le bouton "Upload Fichier" déclenche la balise explorateur standard de votre plateforme (`Windows Explorer` par exemple).
- Vous téléversez et à ce mouvement, le fichier original de votre Ordinateur reste identique, mais une **copie jumelle ultra-chiffrée** transite en streaming asymétrique chez le fournisseur d'hébergement.

### Ranger l'Espace (Arborescence)
- N'hésitez pas à créer des *"Nouveaux Dossiers"*. Entrez dans un Dossier fraîchement créé avec le système et commencez un Upload de fichier directement à l'intérieur de ce chemin. 

---

## 📈 3. Un atout majeur : Le Versioning

*(Qu'est-ce que le Versioning ?)*
En Informatique le concept d'*"immutabilité"* empêche d'effacer une trace de ce qu'on a écrit. C'est idéal contre la fraude comptable, la détérioration de fichiers ou les suppressions involontaires de collègues. 

Quand vous modifiez le Rapport PDF "MiseAjour.pdf" et que vous Uploadez **un nouveau fichier par dessus**, l'application le conserve fièrement avec le **TAG : VERSION N°2**. 
Vous pouvez à tout instant interroger le logiciel via la *"Timeline Versions"* et **télécharger librement la Version N°1 archivée**.

---

## 🔗 4. Le Partage Public : Lier à vos collaborateurs

Un des pivots fondamentaux de la suite logicielle est l'incitation à la collaboration à un Tiers distant, **sans pour autant l'obliger ni à s'inscrire ni à s'authentifier localement au Coffre.**

1. Clic-droit sur le fichier du Coffre fort → `Créer un Partage`.
2. Une fenètre technique vous demande :
   * Quel est son intitulé / sa phrase d'accroche ("Description / Titre").
   * *(Optionnel)* Combien d'utilisations Max le lien permet ? Si vous mettez 3 c'est 3 clics par le consultant et le lien explose brutalement en renvoyant une Erreur 403.
   * *(Optionnel)* Le jeton a-t-il une durée de péremption standard calendaire ?
3. Copiez le lien auto-généré sous forme d'une chaîne hypertexte cryptique indevinable, et envoyez le (E-mail, Signal, Discord, etc).

Le Consultant ouvrira une Page Web vitrine standard au nom de l'Entreprise pour un simple bouton de Téléchargement "Download Securisé" en accréditation Zero-Trust totale. Si d'aventure le collaborateur est infecté, ou malicieux : vous avez l'outil de **Révocation Active Manuelle** depuis votre propre logiciel pour "Couper en urgence la route de ce Partage".

---

## 🎨 5. Ergonomie Premium

Vous aimez l'aspect Hacker terminal sombre ? Le bouton "Toggle Theme" (Interrupteur Switch) sur la page d'authentification ET le Dashboard vous permet à l'action physique "Clic" de basculer 100% de la Suite Logicielle Client via la colorimétrie dynamique vers le **Obsidian Dark** (Thème Sombre).
Vous préférez la fraîcheur des feuilles de couleur pour contrer le contraste d'une pièce très vitrée lumineuse avec les reflets de l'écran ? Le **Thème Emerald Green** (Couleur Verte Clarté) répond en une milliseconde.

---

## 🗑️ 6. Et Si je jette un fichier ? (Corbeille Locale)

L'Action "Supprimer" sur ObsiLock n'est jamais définitive *(Sauf si vous videz expressément la corbeille manuellement, là le serveur brûle la clé de sécurité pour toujours)*. 
Jeter un fichier le retire simplement de la fenêtre des Actifs. Il devient Invisible (concept du `Soft Deletion`), et il se rattache alors à la corbeille (Menu Dashboard / Trash Icon).

Si vous reprenez ce fichier, vous pouvez déclencher à 100% le fait de le **"Restorer"** avec l'ensemble du patrimoine rattaché comme ses Vies antérieures (Versioning) et ses Liens hypertexte Partagés déjà déployés !

---

## ❓ 7. Foire aux Questions des Employés

**Mon Upload a coupé en plein milieu du Streaming à 99% !! Mon fichier corrompu en base m'expose ?**
> Non. Le système ne génère la Clé Enveloppe sécurisée dans la table Méta *(L'enregistrement décisif pour l'App)* QUE si et seulement si l'octet n° de la fin du fichier à été processé et acquitté ! Tant que ce n'est pas fait, il est mort-né. A l'expiration ou via cron, le charabia coupé à mi-course sur le disque physique du serveur hébergeur sera supprimé automatiquement dans l'oubli.

**Je souhaite mettre une vidéo de mariage en 4K. Est-ce que le système va planter parce qu'elle fait 16 Gigas ?**
> Oui et Non. Le système ne plantera absolument jamais, l'architecture d'épluchage de patates (les fameux Blocs streamés de 8ko par LibSodium évoqués dans le manuel Technique) protègent complètement les ressources de l'hébergement serveur d'un effondrement matériel (Out Of Memory Exception). En revanche, La limite d'usine de l'Upload et des Partitions est gérée par le Quota alloué. Si votre Quota est de "50 Mega-Octets", au delà la base de donnée interdira la demande avec un Refus.

**Si un espion a accès au fichier du logiciel serveur, il récupère l'original ?**
> Aucuement. Le serveur est littéralement et purement "idiot". Il ne connait jamais votre document, il se contente de brasser à l'aveugle une enveloppe de mathématiques non interprétables. Le vol de base de données complet des Fichiers par un attaquant le mettra face à un océan de fichiers indéchiffrables en 10 000 ans machine sans l'appuie de la fameuse Master Key, elle conservée localement dans une zone séparée !
