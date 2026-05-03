# ✅ Rapport de Validation et Tests - ObsiLock

Conformément au bloc de compétences **B2.2**, voici les tests de validation effectués pour garantir l'opérationnalité de la solution.

## 1. Tests d'Intégration (API <=> Client)
| Cas de test | Procédure | Résultat | Statut |
| :--- | :--- | :--- | :--- |
| **Authentification** | Saisie login/password correct | Retourne un Token JWT valide | PASS |
| **Chiffrement** | Upload d'un fichier `.pdf` | Le fichier stocké sur le serveur est illisible | PASS |
| **Déchiffrement** | Téléchargement du même fichier | Le fichier est ouvert par le client et lisible | PASS |

## 2. Tests Fonctionnels (Nouveautés)
- **Partage Récursif** : Un lien généré sur un dossier permet de lister l'ensemble des sous-dossiers. (Validé via script de test API).
- **Téléchargement ZIP** : L'appel à `/folders/{id}/download` génère une archive ZIP déchiffrée sans erreur de corruption. (Validé via extraction manuelle).
- **Switch de Thème** : Le basculement en mode "Clair" impacte l'ensemble des pop-ups (Share, Rename, Delete). (Validé visuellement).

## 3. Tests de Robustesse
- **Limite de Quota** : Tentative d'upload d'un fichier de 100 Mo sur un compte limité à 50 Mo. L'API retourne une erreur 400 et bloque l'écriture disque.
- **Sécurité des Partages** : Accès à un token de partage après sa date d'expiration. L'API retourne une erreur 404.
