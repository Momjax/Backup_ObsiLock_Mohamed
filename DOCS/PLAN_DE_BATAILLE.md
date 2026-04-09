# 🎯 CONTRAT DE RÉUSSITE - ORAL E5

## 📋 1. Timing du jour J
| Phase | Durée | Objectif |
| :--- | :--- | :--- |
| **Présentation Projet** | 20 min | Expliquer ObsiLock et faire la démo live. |
| **Analyse Problème** | 30 min | (En loge) Trouver 2 solutions à un problème client. |
| **Défense Solution** | 20 min | Présenter tes choix techniques au jury. |
| **Mise en œuvre** | 60 min | Coder la solution sur ton PC. |
| **Audit Final** | 20 min | Démontrer ton code et répondre aux questions. |

---

## 🗣️ 2. Les 5 Mots-clés qui font gagner
1. **Stateless** : Mon serveur n'a pas de mémoire (sessions), il utilise des jetons JWT.
2. **Streaming** : Mon chiffrement traite les fichiers par petits morceaux de 8 Ko.
3. **Decouplage** : Le frontal (Java) et le dorsal (API) sont totalement indépendants.
4. **Bcrypt** : L'algorithme de hachage ultra-sécurisé pour les mots de passe.
5. **Rate-Limiting** : Protection contre les attaques par force brute sur l'API.

---

## 🖱️ 3. La Démo Parfaite
1. **Login** rapide.
2. **Switch de Thème** (Immersion visuelle).
3. **Upload** d'un fichier en expliquant le chiffrement LibSodium.
4. **Partage** d'un lien avec 1 seule utilisation et démo du lien dans le navigateur.

---

## 🛠️ 4. Aide-mémoire Code (Pour les 60 min)
- **Ajouter une table** : `CREATE TABLE ...` en SQL.
- **Requête Medoo** : `$this->db->insert('table', ['col' => 'val'])`.
- **Retour API** : `return $response->withJson(['data' => true])`.
