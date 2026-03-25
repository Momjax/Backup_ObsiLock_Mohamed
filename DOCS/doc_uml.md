# Diagrammes UML - ObsiLock

## 1. Diagramme de Cas d'Utilisation (Use Case)

Le diagramme montre les interactions entre les différents acteurs (Utilisateurs et Admin) et le système.

```mermaid
usecaseDiagram
    actor "Utilisateur Lambda" as user
    actor "Administrateur" as admin

    rectangle "ObsiLock System" {
        user -- (Inscription / Connexion)
        user -- (Uploader / Télécharger)
        user -- (Gérer ses dossiers / fichiers)
        user -- (Partager un fichier)
        user -- (Gérer la corbeille)
        user -- (Switch de Thème)

        admin -- (Inscription / Connexion)
        admin -- (Gérer tous les utilisateurs)
        admin -- (Modifier les quotas globaux)
        admin -- (Purger la corbeille système)
    }
```

## 2. Diagramme de Classes (Class Diagram - Frontend)

Représente l'architecture logicielle JavaFX et les couches de données.

```mermaid
classDiagram
    direction TB
    class App {
        +static boolean isDarkTheme
        +static void applyTheme(Scene scene)
        +static void toggleTheme(Scene scene)
        +static void updateLogo(ImageView view)
    }

    class ApiClient {
        -String baseUrl
        -String authToken
        +static getInstance()
        +login(String email, String pwd)
        +uploadFile(File file, String folderId)
        +listFiles(String folderId)
        +shareFile(String fileId, String email)
    }

    class MainController {
        -TreeView treeView
        -TableView table
        -ApiClient apiClient
        +handleUpload()
        +handleDelete()
        +handleToggleTheme()
        +refreshUI()
    }

    class LoginController {
        -TextField emailField
        -PasswordField passwordField
        +handleLogin()
        +handleToggleTheme()
    }

    class FileEntry {
        +String id
        +String name
        +long size
        +Date createdAt
    }

    class NodeItem {
        +String id
        +String name
        +NodeType type
    }

    App ..> ApiClient : "utilise"
    MainController --> ApiClient : "utilise"
    LoginController --> ApiClient : "utilise"
    MainController --> FileEntry : "affiche"
    MainController --> NodeItem : "affiche"
```
