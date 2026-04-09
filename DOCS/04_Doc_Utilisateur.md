# 📖 04. GUIDE UTILISATEUR

## 1. Installation du Serveur (Docker)
1.  Ouvrez un terminal dans le dossier du projet.
2.  Lancez la commande : `docker-compose up -d`.
3.  L'API est accessible sur `http://localhost:8080`.

## 2. Lancement du Client (Java)
1.  Vérifiez que **Java 17** est installé.
2.  Exécutez le fichier JAR ou lancez via votre IDE (Main App).

## 3. Utilisation Courante
### Connexion :
Saisissez vos identifiants. Si c'est votre première connexion, créez un compte (password complexe requis).

### Upload de fichier :
- Cliquez sur le bouton **Upload**.
- Le fichier est instantanément chiffré et stocké sur le serveur.

### Partage :
- Sélectionnez un fichier.
- Cliquez sur **Share**.
- Un lien unique est généré. Vous pouvez définir une date d'expiration.

### Thème :
- Cliquez sur le switch en haut à droite pour passer en **Mode Sombre** (Thème Obsidian).

## 4. Support et Sécurité
- **Attention :** En cas de perte de votre mot de passe, les données ne sont pas récupérables sans l'intervention de l'administrateur (voir Doc Technique).
- **Quota :** Chaque utilisateur dispose de 50 Mo par défaut.
