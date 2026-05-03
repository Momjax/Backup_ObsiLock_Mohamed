# 🚀 Guide d'Installation et Déploiement - ObsiLock

Ce guide explique comment remettre en service l'application ObsiLock à partir du code source.

## 1. Pré-requis
- **PHP 8.2+** avec Composer.
- **Java 21** avec Maven.
- **Docker & Docker-Compose**.

## 2. Installation des dépendances
Si les dossiers `vendor` (PHP) et `target` (Java) sont manquants, exécutez les commandes suivantes :

### Backend (PHP)
Allez à la racine du projet et lancez :
```bash
composer install
```
*Cela va recréer le dossier `vendor` avec Slim, Medoo, LibSodium, etc.*

### Frontend (Java)
Allez dans le dossier `coffreFortJava-main/` et lancez :
```bash
mvn clean compile
```
*Cela va recréer le dossier `target` avec les dépendances JavaFX.*

## 3. Lancement de l'infrastructure
Pour démarrer la base de données et l'API :
```bash
docker-compose up -d --build
```
L'API sera accessible sur `https://api.obsilock.iris.a3n.fr:4433`.

## 4. Initialisation de la Base de Données
Si la base est vide, importez le fichier `init.sql` dans votre instance MySQL (via phpMyAdmin ou en ligne de commande).
