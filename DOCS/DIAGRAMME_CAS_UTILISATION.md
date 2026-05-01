# Diagramme de Cas d'Utilisation - ObsiLock

Le diagramme de cas d'utilisation permet de visualiser les interactions entre les utilisateurs et le système.

## 1. Code PlantUML (À copier-coller)

```plantuml
@startuml
skinparam actorStyle awesome
left to right direction

actor "Utilisateur Authentifié" as U
actor "Visiteur (via lien)" as V

rectangle "Système ObsiLock" {
  (S'authentifier) as UC1
  (Gérer les dossiers) as UC2
  (Déposer un fichier) as UC3
  (Chiffrer les données) as UC3_1
  (Télécharger / Déchiffrer) as UC4
  (Gérer les versions) as UC5
  (Générer un lien de partage) as UC6
  (Consulter un partage) as UC7
}

U -- UC1
U -- UC2
U -- UC3
U -- UC4
U -- UC5
U -- UC6

UC3 ..> UC3_1 : <<include>>
UC4 <.. UC7 : <<extend>>

V -- UC7
@enduml
```

## 2. Explication des Cas d'Utilisation

### Acteurs :
- **Utilisateur Authentifié** : Possède un compte, gère ses propres données.
- **Visiteur** : Personne externe accédant à un fichier/dossier via un token de partage.

### Cas clés :
- **Déposer un fichier <<include>> Chiffrer** : Le chiffrement LibSodium est obligatoire et transparent lors de l'upload.
- **Consulter un partage <<extend>> Télécharger** : Le visiteur accède à une page de consultation qui lui permet, s'il le souhaite, de déclencher le téléchargement (déchiffrement à la volée).
- **Gérer les versions** : Permet à l'utilisateur de lister et de récupérer d'anciennes versions d'un même fichier (gestion de l'historique).

## 3. Justification pour le dossier
Ce diagramme prouve que le système sépare bien les droits d'administration des données (réservés au propriétaire) et les droits de consultation (ouverts aux tiers via partage sécurisé).
