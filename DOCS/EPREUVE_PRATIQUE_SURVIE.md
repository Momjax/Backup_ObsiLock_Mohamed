# 🚨 SURVIE À L'ÉPREUVE PRATIQUE DE 60 MINUTES (BTS SIO E5)

D'accord, je comprends ta panique. Le jury va te demander de **modifier ou rajouter un truc d'urgence en 1 heure**.
La bonne nouvelle, c'est qu'ils ne te demanderont JAMAIS de recoder tout le logiciel. Ils te demanderont une "petite évolution fonctionnelle" ou la résolution d'un bug.

Le secret de cette épreuve, ce n'est pas de connaître le code par cœur, **c'est de savoir FAIRE DU COPIER-COLLER INTELLIGENT**.

Voici les **4 scénarios probables** qu'ils pourraient te demander sur ObsiLock. Si tu comprends la logique de parcours de ces 4 scénarios, tu pourras répondre à 90% de leurs demandes.

---

## 🔥 Scénario 1 (Le grand classique) : "Rajoutez un champ en base de données et affichez-le"
*Exemple du jury : "Je veux que chaque utilisateur ait un champ "Role" (Admin ou User), et je veux le voir sur l'interface." ou "Je veux qu'on puisse mettre une description aux dossiers".*

**LA MÉTHODOLOGIE (L'effet domino) :**
Tu dois toujours suivre le flux des données, de la base vers l'écran.
1. **La Base de données** : Modifie la table dans PHPMyAdmin (ou adminer) en rajoutant la colonne.
2. **Le Backend PHP (Model)** : Va dans `src/Model/` (par exemple `UserRepository.php` ou `FolderRepository.php`). Cherche le moment où on fait le `insert()` ou `update()` en base, et rajoute ton champ dans le tableau.
3. **Le Backend PHP (Controller)** : Va dans `src/Controller/` (par exemple `AuthController` si c'est la connexion). Si le champ est attendu, lis-le depuis la variable `$data = $request->getParsedBody();`. S'il est renvoyé au client, assure-toi qu'il est bien dans le tableau `json_encode([ ... ])`.
4. **Le Frontend JavaFX (Modèle)** : Va dans `coffreFortJava-main/src/main/java/com/coffrefort/client/model/` (ex: `User.java` ou `FolderItem.java`). Rajoute l'attribut `private String role;`, et n'oublie pas de générer le Getter et Setter.
5. **L'Interface FXML** : Ouvre un éditeur FXML ou le fichier XML. Rajoute une balise `<Label fx:id="roleLabel"` pour l'afficher !
6. **Le Controller JavaFX** : Va dans le controller Java (`MySharesController.java`, etc), rajoute `@FXML private Label roleLabel;` et fait un `roleLabel.setText(user.getRole());`.

---

## 🚫 Scénario 2 : "Ajoutez une règle de gestion (Une limitation)"
*Exemple du jury : "Un utilisateur ne peut pas avoir plus de 10 dossiers à la racine" ou "On ne peut générer un lien que pour 5 usages maximum".*

Ici c'est très simple, ça ne se passe que du coté du **Backend PHP**. Il faut rajouter un `.if()` avant une action.

**Exemple : Restreindre à 5 usages max un partage.**
1. Ouvre `src/Controller/ShareController.php`. C'est là que l'API reçoit l'ordre de créer.
2. Va dans la fonction de création (`public function create`).
3. Analyse ce qui est reçu : `$data = $request->getParsedBody();`.
4. Ajoute simplement un test :
```php
if (isset($data['max_uses']) && $data['max_uses'] > 5) {
    $response->getBody()->write(json_encode(['error' => 'La limite est de 5 téléchargements maximum.']));
    return $response->withHeader('Content-Type', 'application/json')->withStatus(400); // 400 = erreur client
}
```
*Le Copier-Coller magique : Cherche toujours s'il n'y a pas déjà un `if (...) { return error }` juste au dessus. Tu le copies-colles et tu changes juste le texte d'erreur.*

---

## 🕵️ Scénario 3 : "Gardez une trace de ce qu'il se passe (Historisation/Log)"
*Exemple du jury : "Je veux tracer (historiser) chaque fois qu'un utilisateur échoue à se connecter".*

C'est très demandé pour tester si tu sais créer une table d'historique.

1. **BDD** : Tu crées une nouvelle base (via PHPMyAdmin) `login_logs (id, email_attempt, attempt_date)`.
2. **PHP** : Va dans `src/Controller/AuthController.php`, à l'endroit où le mot de passe est faux (cherche le commentaire "Identifiants invalides").
3. **Le Log en base** : Comme tu as le composant `Medoo` (`$this->db`), tu ajoutes ça juste avant le `return` de l'erreur :
```php
$this->db->insert('login_logs', [
    'email_attempt' => $data['email'],
    'attempt_date' => date('Y-m-d H:i:s')
]);
```
*Pas besoin de plus.* En deux lignes de code Medoo, tu viens de valider tout le scénario pour le jury.

---

## 🛠️ Scénario 4 : "Corriger un problème que j'ai créé"
Parfois, le jury sabote volontairement ton code avant l'épreuve (ex: changer une requête SQL, dérégler une variable, enlever un fichier).

**La règle d'Or du Debug :**
1. N'essaie pas de regarder le code au pif.
2. Ouvre l'application, reproduis le crash.
3. Attrappe la phrase d'erreur que l'écran t'affiche ou va voir les logs du terminal de ton IDE (ou Postman).
4. **Fais une recherche globale (Ctrl+F ou Ctrl+Shift+F dans ton éditeur / VSCode)** de ce texte d'erreur. Ça t'amène pile au fichier cassé.
5. Regarde la dernière modification ou ce qui paraît bizarre (ex: Si le JWT ne marche plus, le jury a peut-être changé la durée de validité `+ 3600` en `- 3600` dans ton `generateJWT`).

---

## 🧘‍♂️ L'attitude face au problème en 30 min (Le plus important)

Quand ils te donnent le sujet du crash ou de l'évolution, tu as 30 minutes de préparation d'analyse.
N'OUVRE PAS TOUT DE SUITE LE CODE !

Pendant ces 30 minutes, écris ce plan sur ta feuille de brouillon :
1. **La solution** : *"Je dois ajouter X. Pour faire X, je vais faire d'abord la Modif A, puis la Modif B".*
2. **Faisabilité** : Demande-toi si c'est faisable en 1h. Si tu penses que l'interface graphique (JavaFX FXML) va te faire perdre 40 minutes, écris dans ta feuille : *"Je ferai le coté serveur PHP en priorité, et je testerai avec POSTMAN pour gagner du temps. Et si j'ai le temps, je ferai l'interface visuelle JavaFX".*

Puis, pendant les **20 minutes de présentation** des solutions :
- Explique ce choix au jury !
- Dis : *"Voici ma solution : je vais travailler sur la route PHP /auth/login. Je vais tracer l'erreur. Pour optimiser l'heure impartie, je vais le valider avec mon outil Postman. Ainsi je sécurise la structure Backend avant de faire le visuel".*
Le jury ADORE cette façon de penser d'ingénieur ! Ça veut dire que tu maîtrises tes priorités. C'est presque plus important de savoir réfléchir comme ça que de coder vite !
