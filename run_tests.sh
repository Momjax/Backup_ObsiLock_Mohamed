#!/bin/bash
# ==========================================
# Script d'exécution des tests — ObsiLock
# ==========================================

echo "🚀 Lancement de la suite de tests ObsiLock..."
echo "------------------------------------------------"

# Vérifier si docker est lancé et si le conteneur cible existe
if ! docker ps | grep -q "obsilock-web"; then
    echo "❌ Erreur : Le conteneur 'obsilock-web' (API PHP) ne semble pas être en cours d'exécution."
    echo "Assurez-vous d'avoir lancé : docker compose up -d"
    exit 1
fi

# Copier le dossier de tests le plus récent dans le conteneur juste au cas où
echo "📂 Synchronisation des fichiers de test avec le conteneur..."
docker cp ./tests obsilock-web:/var/www/html/ 2>/dev/null
docker cp ./phpunit.xml obsilock-web:/var/www/html/ 2>/dev/null

echo "🧪 Exécution des tests via PHPUnit dans l'environnement du serveur..."
echo ""

# Lancer la commande phpunit depuis PHP
docker exec obsilock-web vendor/bin/phpunit --colors=always

test_status=$?

echo ""
echo "------------------------------------------------"
if [ $test_status -eq 0 ]; then
    echo "✅ Les tests ont réussi avec succès !"
else
    echo "❌ Certains tests ont échoué. Veuillez vérifier les erreurs ci-dessus."
fi

exit $test_status
