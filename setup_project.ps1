$target = "$HOME\Downloads\coffreFortJava-main"; if (!(Test-Path $target)) { New-Item -ItemType Directory -Path $target -Force }; cd $target;
$content = @"
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.coffrefort</groupId>
  <artifactId>client-javafx</artifactId>
  <version>0.1.0</version>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <javafx.version>21.0.2</javafx.version>
    <jackson.version>2.17.2</jackson.version>
    <okhttp.version>4.12.0</okhttp.version>
  </properties>

  <dependencies>
    <!-- JavaFX UI -->
    <dependency>
      <groupId>org.openjfx</groupId>
      <artifactId>javafx-controls</artifactId>
      <version>`${javafx.version}</version>
    </dependency>
    <dependency>
      <groupId>org.openjfx</groupId>
      <artifactId>javafx-fxml</artifactId>
      <version>`${javafx.version}</version>
    </dependency>

    <!-- JSON (pour illustrer les échanges REST) -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>`${jackson.version}</version>
    </dependency>

    <!-- HTTP client (upload, appels API) -->
    <dependency>
      <groupId>com.squareup.okhttp3</groupId>
      <artifactId>okhttp</artifactId>
      <version>`${okhttp.version}</version>
    </dependency>
    <!-- JUnit 5 (Jupiter) -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.1</version>
      <scope>test</scope>
    </dependency>
    <!-- AssertJ pour les assertions fluides (ex: assertThat(..).isEqualTo(..)) -->
    <dependency>
      <groupId>org.assertj</groupId>
      <artifactId>assertj-core</artifactId>
      <version>3.24.2</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!-- Lancer avec: mvn clean javafx:run -->
      <plugin>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-maven-plugin</artifactId>
        <version>0.0.8</version>
        <configuration>
          <!-- Use a separate launcher to avoid IDE run issues -->
        <mainClass>com.coffrefort.client.Launcher</mainClass>
        </configuration>
      </plugin>
      <!-- Maven Surefire pour l'exécution des tests JUnit 5 via 'mvn test' -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.3</version>
      </plugin>
    </plugins>
  </build>
</project>
"@; $dir = Split-Path "pom.xml"; if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }; [System.IO.File]::WriteAllText("pom.xml", $content, [System.Text.Encoding]::ASCII)
$content = @"
package com.coffrefort.client;

import com.coffrefort.client.controllers.LoginController;
import com.coffrefort.client.controllers.MainController;
import com.coffrefort.client.controllers.RegisterController;
import com.coffrefort.client.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Mini client lourd JavaFX d'exemple pour le projet « Coffre‑fort numérique ».
 * Objectif pédagogique: fournir une base exécutable, simple à lire, sur laquelle
 * les étudiants peuvent s'appuyer pour intégrer de vrais appels REST.
 */
public class App extends Application {

    //private final ApiClient apiClient = new ApiClient(); avant implementation de SessionManager
    private ApiClient apiClient;

    // --- GESTION DU THEME ---
    public static boolean isDarkTheme = true;
    public static final String DARK_THEME = "/com/coffrefort/client/style-javafx.css";
    public static final String LIGHT_THEME = "/com/coffrefort/client/style-light.css";

    public static void toggleTheme(Scene scene) {
        isDarkTheme = !isDarkTheme;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String themeUrl = App.class.getResource(isDarkTheme ? DARK_THEME : LIGHT_THEME).toExternalForm();
        scene.getStylesheets().add(themeUrl);
    }

    public static void updateThemeButton(javafx.scene.control.Control button) {
        if (button == null) return;
        
        if (button instanceof javafx.scene.control.ToggleButton) {
            javafx.scene.control.ToggleButton tb = (javafx.scene.control.ToggleButton) button;
            // Mode clair (Green) -> switch activé (vert)
            // Mode sombre (Dark) -> switch désactivé (gris)
            tb.setSelected(!isDarkTheme);
            tb.getStyleClass().add("theme-switch"); // S'assurer que la classe est présente
        } else if (button instanceof javafx.scene.control.Button) {
            javafx.scene.control.Button b = (javafx.scene.control.Button) button;
            if (isDarkTheme) {
                b.setText("☾");
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 22px;");
            } else {
                b.setText("☀");
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2328; -fx-cursor: hand; -fx-font-size: 22px;");
            }
        }
    }

    public static void updateLogo(javafx.scene.image.ImageView logoView) {
        if (logoView == null) return;
        String imgPath = isDarkTheme ? "/images/Logo_CryptoVault_transparent.png" : "/images/Logo_CryptoVault_light.png";
        java.net.URL resource = App.class.getResource(imgPath);
        if (resource != null) {
            logoView.setImage(new javafx.scene.image.Image(resource.toExternalForm()));
        }
    }
    // ------------------------

    //Connexion
    @Override
    public void start(Stage stage) throws Exception {
        this.apiClient = new ApiClient();

        //config de SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setApiClient(apiClient);
        sessionManager.setOnSessionExpired(() -> {

            //rediriger vers connexion
            openLogin(stage);
        });

        stage.setTitle("Coffre‑fort numérique — Mini client");
        openLogin(stage);
    }


    /**
     * ÉCRAN CONNEXION
     * @param stage
     */
    public void openLogin(Stage stage) {
        try {

            //arrêter la session quand on retourne au login
            SessionManager.getInstance().stopSessionMonitoring();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/login2.fxml"));

            // Controller factory pour injecter ApiClient et callbacks
            loader.setControllerFactory(type -> {
                if (type == LoginController.class) {
                    LoginController c = new LoginController();
                    c.setApiClient(apiClient);

                    // Après connexion réussie → tableau de bord
                    c.setOnSuccess(() -> openMainAndClose(stage));

                    // Clique sur "S'inscrire" → ouvrir l'écran d'inscription
                    c.setOnGoToRegister(() -> openRegister(stage));

                    return c;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 450, 700);
            applyTheme(scene);

            stage.setScene(scene); //avec ça le stage reste 1024x640
            stage.setTitle("Coffre-fort numérique — Connexion");

            //il faut redimensionner!! sinon il prend la taille de main.fxml
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger login2.fxml", e);
        }
    }


    /**
     * ÉCRAN INSCRIPTION
     * @param stage
     */
    public void openRegister(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/register.fxml"));

            loader.setControllerFactory(type -> {
                if (type == RegisterController.class) {
                    RegisterController c = new RegisterController();
                    c.setApiClient(apiClient);

                    // Après inscription réussie → retour à l'écran de login
                    c.setOnRegisterSuccess(() -> openMainAndClose(stage));

                    // Clique sur "Se connecter" → retour à l'écran de login
                    c.setOnGoToLogin(() -> openLogin(stage));

                    return c;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 420, 750);
            applyTheme(scene);

            stage.setScene(scene);  //avec ça le stage reste 1024x640
            stage.setTitle("Coffre-fort numérique — Inscription");

            //il faut redimensionner!!
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger register.fxml", e);
        }
    }


    /**
     * TABLEAU DE BORD
     *  * @param loginStage
     * Solution avec data URI pour inclure le CSS directement
     */
    private void openMainAndClose(Stage loginStage) {
        try {
            System.out.println("App - Chargement de main.fxml...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/main.fxml"));
            Parent root = loader.load();  //le controller est créé

            MainController controller = loader.getController();
            controller.setApiClient(apiClient);
            controller.setApp(this);  //passer App en référence au MainController
            controller.checkAdminRole();
            //loader.setController(controller);

            System.out.println("App - Controller configuré, chargement du root...");

            //Parent root = loader.load();  //le controller est créé
            System.out.println("App - Configuration de la scène...");
//            Stage mainStage = new Stage();
//            mainStage.setTitle("Coffre‑fort — Espace personnel");

            //réutiliser le stage existant à la place de créer un nouveau
            Scene scene = new Scene(root, 1024, 640);
            applyTheme(scene);
            loginStage.setScene(scene);

            loginStage.setTitle("Coffre‑fort — Espace personnel");

            loginStage.setWidth(1024);
            loginStage.setHeight(640);
            loginStage.setResizable(true);
            loginStage.centerOnScreen();
            loginStage.show(); // important afficher la fenetre??????? elotte commentben volt es ment

            // Fermer la fenêtre de login => avec ça je n'arriva pas ouvrir la vue de connexion
            //loginStage.close();

            //démarrer la surveillance de session après connexion réussi
            SessionManager.getInstance().startSessionMonitoring();

            System.out.println("App - Interface principale affichée");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger main.fxml", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

"@; $dir = Split-Path "src\main\java\com\coffrefort\client\App.java"; if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }; [System.IO.File]::WriteAllText("src\main\java\com\coffrefort\client\App.java", $content, [System.Text.Encoding]::ASCII)
$content = @"
package com.coffrefort.client;

import com.coffrefort.client.config.AppProperties;
import com.coffrefort.client.model.*;
import com.coffrefort.client.util.JsonUtils;
import com.coffrefort.client.util.JwtUtils;
import com.coffrefort.client.util.SessionManager;
import com.coffrefort.client.util.UIDialogs;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//import pour la classe statique ProgressBodyPublisher
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;



public class ApiClient {

    //propriétés
    private static ApiClient INSTANCE;
    private final HttpClient httpClient;
    private final String baseUrl;
    private String authToken;
    private Boolean isAdmin;
    private final HttpClient http = HttpClient.newHttpClient();


    //méthodes

    /**
     * Initialise l’ApiClient avec l’URL par défaut (localhost)
     */
    public ApiClient() {
        this("https://api.obsilock.iris.a3n.fr:4433");
    }

    /**
     * Initialise l’ApiClient avec une URL de backend personnalisée
     * @param baseUrl
     */
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.authToken = null;
    }

    /**
     * récuperer email de user actuellement connecté
     * (en même façon on peut récuperer userId...
     * @return
     */
    public String getCurrentUserEmail(){

        // extraire depuis AppProperties (persisté)
        String email = AppProperties.get("auth.email");

        //extraire du token actuel => fallback
        if(email == null && authToken != null){
            email = JwtUtils.extractEmail(authToken);
        }
        return email;
    }

    public Boolean isAdmin(){
        return this.isAdmin;
    }

    /**
     *
     * @return l’instance singleton d’ApiClient (créée au premier appel)
     */
    public static ApiClient getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ApiClient();
        }
        return INSTANCE;
    }

    /**
     * Indique si un token JWT valide est présent côté client
     */
    public boolean isAuthenticated() {
        return this.authToken != null && !this.authToken.isEmpty();
    }

    /**
     * Retourne le token JWT actuellement stocké en mémoire
     */
    public String getAuthToken() {
        return this.authToken;
    }


    //Appel API

    /**
     * POST /auth/login
     * Authentification utilisateur avec email et mot de passe
     * @param email Email de l'utilisateur
     * @param password Mot de passe
     * @return Le token JWT si succès, null sinon
     * @throws Exception En cas d'erreur réseau ou serveur
     */
    public String login(String email, String password) throws Exception {
        String url = baseUrl + "/auth/login";

        // Construction du body JSON
        String jsonBody = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password
        );

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Envoi de la requête
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //HttpResponse<String> response = executeRequest(request);

        int statusCode = response.statusCode();
        String responseBody = response.body();

        System.out.println("Login - Status: " + statusCode);
        System.out.println("Login - Response: " + responseBody);

        // Gestion des erreurs HTTP
        if (statusCode == 401) {
            throw new AuthenticationException("Identifiants invalides.");
        } else if (statusCode == 400) {
            String errorMsg = JsonUtils.extractJsonField(responseBody, "error");
            throw new AuthenticationException(
                    errorMsg != null ? errorMsg : "Requête invalide."
            );
        } else if (statusCode != 200) {
            String errorMsg = JsonUtils.extractJsonField(responseBody, "error");
            throw new Exception(
                    errorMsg != null ? errorMsg : "Erreur serveur (code " + statusCode + ")."
            );
        }

        // Extraction du token JWT
        String token = JsonUtils.extractJsonField(responseBody, "token");
        if (token == null || token.isEmpty()) {
            throw new Exception("Token JWT non reçu du serveur.");
        }

//        String isAdminStr = JsonUtils.extractJsonNumberField(responseBody, "is_admin");
//        System.out.println("DEBUG - is_admin extrait: '" + isAdminStr + "'");
//        if(isAdminStr != null){
//            this.isAdmin = "1".equals(isAdminStr.trim());
//        }else{
//            this.isAdmin = false;
//        }
//        System.out.println("DEBUG - this.isAdmin: " + this.isAdmin);
        setAuthToken(token);

        return token;
    }

    /**
     * POST/auth/register puis POST/auth/login
     * Inscrit un utilisateur  puis le connecte => stocke le JWT
     * @param email Email de l'utilisateur
     * @param password Mot de passe
     * @param quotaTotal
     * //@param isAdmin => backend qui décide
     * @return Le token JWT si succès
     * @throws Exception En cas d'erreur
     */
    public String register(String email, String password, int quotaTotal) throws Exception{
        // auth/register
        String registerUrl = baseUrl + "/auth/register";

        String registerJson = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"quota_total\":\"%d\"}",
                email, password, quotaTotal
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(registerUrl))
                .header("Accept", "application/json")
                .header ("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .build();

        HttpResponse<String> registerResponse = http.send(request, HttpResponse.BodyHandlers.ofString());

        int regStatus = registerResponse.statusCode();
        String regBody = registerResponse.body();
        System.out.println("Register Status: " + regStatus);
        System.out.println("Register Response: " + regBody);

        // Pour une inscription, l'API peut renvoyer 200 ou 201 (Created)
        if(regStatus < 200 || regStatus >= 300) {

            //Erreur d'inscritption
            String apiError = JsonUtils.extractJsonField(regBody, "error");

            if(apiError == null || apiError.isEmpty()) {
                apiError = "Inscription refusée par le serveur (code " + regStatus + ").";
            }
            throw new RegistrationException(apiError); //=> il ne faut pas return après!!
        }

        // /auth/login
        String loginUrl = baseUrl + "/auth/login";

        String LoginJson = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password
        );

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(loginUrl))
                .header("Accept", "application/json")
                .header ("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(LoginJson))
                .build();

        HttpResponse<String> loginResponse = http.send(loginRequest, HttpResponse.BodyHandlers.ofString());

        int logStatus = loginResponse.statusCode();
        String logBody = loginResponse.body();
        System.out.println("Login Status: " + logStatus);
        System.out.println("Login Response: " + logBody);

        if(logStatus != 200){

            //Erreur de connexion
            String apiError = JsonUtils.extractJsonField(logBody, "error");
            if(apiError == null || apiError.isEmpty()) {
                apiError = "Connexion automatique échouée (code " + logStatus + ").";
            }
            throw new RegistrationException(apiError);
        }

        //récupération de token
        String token =  JsonUtils.extractJsonField(logBody, "token");
        if(token == null || token.isEmpty()) {
            throw new RegistrationException("Connexion réussi mais aucun token renvoyé par le serveur.");
        }

        setAuthToken(token);

        return token;
    }


    /**
     * Enregistre le token JWT et persist email/userId extraits du token dans AppProperties
     * Définir manuellement le token (pour restauration depuis persistance)
     * stocker authToken en mémoire
     */
    public void setAuthToken(String token) {
        this.authToken = token;
        if (token != null) {
            AppProperties.set("auth.token", token);

            String email = JwtUtils.extractEmail(token);
            if (email != null) {
                AppProperties.set("auth.email", email);
            }

            String userId = JwtUtils.extractUserID(token);
            if (userId != null) {
                AppProperties.set("auth.userId", userId);
            }
            System.out.println("setAuthToken() userId = " + userId);

            //extraire is_admin
            Boolean isAdminBoolean = JwtUtils.extractIsAdmin(token);
            if(isAdminBoolean != null){
                this.isAdmin = isAdminBoolean;
                AppProperties.set("auth.isAdmin", isAdminBoolean.toString());
            }else{
                this.isAdmin = false;
            }
            System.out.println("setAuthToken() isAdmin = " + this.isAdmin);

        }
    }

    /**
     * POST /folders
     * Crée un dossier (racine ou enfant) pour l’utilisateur connecté
     * @param name
     * @param parentFolder
     * @return
     * @throws Exception
     */
    public boolean createFolder(String name, NodeItem parentFolder) throws Exception{
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (authToken null).");
        }

        String userIdStr = AppProperties.get("auth.userId");
        System.out.println("createFolder() userIdStr = " + userIdStr);

        if(userIdStr == null || userIdStr.isEmpty()) {
            throw new IllegalStateException("auth.userId non défini dans AppProperties.");
        }

        int userId = Integer.parseInt(userIdStr);

        Integer parentId = null; // => null => dossier à la racine
        if(parentFolder != null && parentFolder.getId() != 0) {
            parentId = parentFolder.getId();
        }

        //construction de json
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"user_id\": ").append(userId).append(",");

        if(parentId == null) {
            sb.append("\"parent_id\": null,");
        }else{
            sb.append("\"parent_id\": ").append(parentId).append(",");
        }

        sb.append("\"name\": \"").append(JsonUtils.escapeJson(name)).append("\"");
        sb.append("}");
//        String jsonBody = "{"
//                + "\"user_id\": " + userId + ","
//                + "\"parent_id\": " + parentId + ","
//                + "\"name\": \"" + escapeJson(name) + "\""
//                + "}";

        String jsonBody = sb.toString();
        System.out.println("POST /folders body = " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/folders"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        if (status == 201) {
            System.out.println("Dossier créé: " + response.body());
            return true;
        }

        System.err.println("Erreur création dossier. Status=" + status + " body=" + response.body());
        return false;
    }


    /**
     * POST /files
     * Upload un fichier dans un dossier (ou racine) en multipart/form-data
     * Upload un fichier dans la racine en réutilisant uploadFile(file, null)
     * @param file fichier local
     * @param folderId  id du dossier cible (peut être null pour racine si ton backend le gère)
     * @return
     * @throws Exception
     */
    public boolean uploadFile(File file, Integer folderId) throws Exception {
        if (file == null || !file.exists()) {
            throw new Exception("Fichier invalide");
        }

        // Vérifier token
        String token = this.authToken;
        if (token == null || token.isEmpty()) {
            token = AppProperties.get("auth.token"); // fallback
        }
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("Utilisateur non connecté (token manquant).");
        }

        String url = baseUrl + "/files";
        String boundary = "----CryptoVaultBoundary" + UUID.randomUUID();

        // Construire le body multipart
        byte[] body = buildMultipartBody(file, boundary, folderId);

        // Faire la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String responseBody = response.body();

        System.out.println("UPLOAD Status: " + status);
        System.out.println("UPLOAD Response: " + responseBody);

        //status == 401 => géré par executeRequest()
        if (status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if (status < 200 || status >= 300) {
            String apiError = JsonUtils.extractJsonField(responseBody, "error");
            if (apiError == null || apiError.isEmpty()) {
                apiError = "Upload refusé (code " + status + ")";
            }
            throw new Exception(apiError);
        }
        return true;
    }


    /**
     * GET /folders
     * Récupère tous les dossiers et reconstruit l’arborescence en NodeItem
     * @return
     * @throws Exception
     */
    public NodeItem listRoot() throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/folders"))
                //.header("Content-Type", "application/json") //=> lehet hogy le kell venni
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();

        if (status != 200) {
            System.err.println("Erreur listRoot. Status=" + status + " body=" + response.body());
            throw new IllegalStateException("Erreur HTTP " + status + " lors du chargement de l'arborescence");
        }

        String body = response.body();
        System.out.println("GET /folders => " + body);

        // Construire l'arbre de NodeItem
        return buildFolderTreeFromJson(body);
    }

    /**
     * GET /files ou /files?folder=...
     * Récupère la liste des fichiers ou la liste des fichiers d’un dossier =>les parse en FileEntry
     * List<FileEntry> => sans pagination
     * @param folderId ID du dossier (null pour tous les fichiers)
     * @param limit Nombre de fichiers par page
     * @param offset Décalage (0 pour la première page)
     * @return Réponse paginée
     */
    public PagedFilesResponse listFilesPaginated(Integer folderId, int limit, int offset) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        //construction de url
        int page = (offset / limit) + 1;
        String url =  baseUrl + "/files?page=" + page + "&per_page=" + limit;
        if(folderId != null && folderId > 0){
            url += "&folder_id=" + folderId;
        }

        HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create(baseUrl + "/files?folder=" + folderId)) => sans pagination
                .uri(URI.create(url))
                //.header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        System.out.println("GET " + url  + " => status " + status);
        System.out.println("Response body: " + body);

        if (status != 200) {
            String error = JsonUtils.extractJsonField(body, "error");
            throw new RuntimeException("Erreur HTTP " + status + " : " + (error != null ? error : body));
        }

        // Construire l'arbre de NodeItem
        return JsonUtils.parsePagedFilesResponse(body);
    }


    /**
     * Déconnecte l’utilisateur en supprimant le token en mémoire et dans AppProperties
     */
    public void logout() {
        this.authToken = null;
        this.isAdmin = false;
        AppProperties.remove("auth.token");
        AppProperties.remove("auth.email");
        System.out.println("Déconnexion effectuée.");
    }


    /**
     * GET /me/quota
     * Récupère le quota utilisateur et retourne un objet Quota (used/total)
     */
    public Quota getQuota() throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/me/quota"))
                //.header("Content-Type", "application/json") //=> lehet hogy le kell venni
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        System.out.println("GET /me/quota status=" + status);
        System.out.println("GET /me/quota body=" + body);

        if(status != 200){
            String apiError = JsonUtils.extractJsonField(body, "error");

            if(apiError == null || apiError.isEmpty()){
                apiError = "Erreur quota (code " + status + ")";
            }
            throw  new Exception(apiError);
        }

        String usedStr = JsonUtils.extractJsonNumberField(body, "used");
        String totalStr = JsonUtils.extractJsonNumberField(body, "total");

        long used = (usedStr != null && !usedStr.isEmpty()) ? Long.parseLong(usedStr) : 0;
        long total = (totalStr != null && !totalStr.isEmpty()) ? Long.parseLong(totalStr) : 0;

        return new Quota(used, total);
    }


    /**
     * DELETE /files/{id}
     * Supprime un fichier (toutes ses versions) =>ok
     * retourne true si succès
     * @param fileId
     * @return
     * @throws Exception
     */
    public void deleteFile(int fileId) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0){
            throw new IllegalArgumentException("FileId invalide: " + fileId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/files/" + fileId))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        //204 => requête réussi, pas besoin de quitter la page
        if(status == 200 || status == 204) {
            return;
        }

        //status == 401 => par executeRequest()
        if(status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes");
        }

        if(status == 404) {
            throw new RuntimeException("Fichier introuvable");
        }

        //autres erreurs
        String error = JsonUtils.extractJsonField(body, "error");
        error = JsonUtils.unescapeJsonString(error);

        if(error == null || error.isEmpty()){
            error = body;
        }

        throw new RuntimeException("Erreur de suppression (HTTP " + status + "): " + error);
    }

    /**
     * DELETE /files/{file_id}/versions/{id}
     * Supprime un fichier  =>ok
     * @param fileId
     * @param versionId
     * @throws Exception
     */
    public void deleteVersion(int fileId, int versionId) throws Exception{
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0){
            throw new IllegalArgumentException("FileId invalide: " + fileId);
        }

        if(versionId <= 0){
            throw new IllegalArgumentException("VersionId invalide: " + versionId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/files/" + fileId + "/versions/" + versionId))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        //204 => requête réussi, pas besoin de quitter la page
        if(status == 200 || status == 204) {
            return;
        }

        //status == 401 => par executeRequest
        if(status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if(status == 404) {
            String error = JsonUtils.extractJsonField(body, "error");
            error = JsonUtils.unescapeJsonString(error);

            if (error == null || error.isEmpty()) {
                error = "Fichier ou version introuvable";
            }

            throw new RuntimeException(error);
        }

        //autres erreurs
        String error = JsonUtils.extractJsonField(body, "error");
        error = JsonUtils.unescapeJsonString(error);

        if(error == null || error.isEmpty()){
            error = body;
        }

        throw new RuntimeException("Erreur de suppression (HTTP " + status + "): " + error);
    }


    /**
     * DELETE /folders/{id}
     * Supprime un dossier et retourne true si succès =>ok
     * @param folderId
     * @return
     * @throws Exception
     */
    public void deleteFolder(int folderId) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(folderId <= 0){
            throw new IllegalArgumentException("FolderId invalide: " + folderId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/folders/" + folderId))
                //.header("Content-Type", "application/json") //=> lehet hogy le kell venni
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        System.out.println("DELETE /folders/" + folderId + " => status " + status);
        System.out.println("DELETE /folders/" + folderId + " =>  body: " + body);

        //204 => requête réussi, pas besoin de quitter la page
        if(status == 200 || status == 204) {
            return;
        }

        //status == 401 => par executeRequest
        if(status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if(status == 404) {
            throw new RuntimeException("Dossier introuvable");
        }

        //dossier non vide
        if(status == 400){
            String error = JsonUtils.extractJsonField(body, "error");
            error = JsonUtils.unescapeJsonString(error);

            if(error == null || error.isEmpty()){
                error = "Dossier non vide";
            }
            throw new RuntimeException(error);
        }

        //autres erreurs
        String error = JsonUtils.extractJsonField(body, "error");
        error = JsonUtils.unescapeJsonString(error);

        if(error == null || error.isEmpty()){
            error = body;
        }

        throw new RuntimeException("Erreur de suppression (HTTP " + status + "): " + error);
    }

    /**
     * GET /files/{id}/download
     * Télécharge un fichier et l’écrit dans le fichier cible
     * @param fileId
     * @param target
     * @throws Exception
     */
    public void downloadFileTo(long fileId, File target) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/files/" + fileId + "/download"))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if(response.statusCode() != 200) {

            //lire le message d'erreur du backend
            String errorMessage = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("HTTP " +response.statusCode() + " lors du téléchargement: " + errorMessage);
        }

        //écriture le flux dans le fichier
        try (InputStream in = response.body()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

    }

    /**
     * GET /files/{id}/versions/{version}/download
     * télecharger une version sur ordi par le propriétaire
     * @param fileId
     * @param version
     * @param target
     * @param progress
     * @throws Exception
     */
    public void downloadFileVersionTo(long fileId, int version,  File target, DownloadProgressListener progress) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0) throw new IllegalArgumentException("fileId invalide");
        if(version <= 0) throw new IllegalArgumentException("version invalide");
        if(target == null) throw new IllegalArgumentException("target invalide");

        String url = baseUrl + "/files/" + fileId + "/versions/" + version + "/download";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if(response.statusCode() != 200) {

            //lire le message d'erreur du backend
            String errorMessage = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("HTTP " +response.statusCode() + " lors du téléchargement: " + errorMessage);
        }

        long total = -1;
        String cl = response.headers().firstValue("Content-Length").orElse(null);
        if(cl != null) {
            try{
                total = Long.parseLong(cl); // => convertion p.ex. "84011" → 84011L
            }catch (Exception ignored){
                //ingore => total reste -1
            }
        }

        //copier le flux HTTP vers le fichier cible + progression
        //try-with-resources : ferme automatiquement in et out à la fin, même s'il y a erreur.
        try (InputStream in = response.body();  //=> flux entrant depuis HTTP
            var out = Files.newOutputStream(target.toPath())) {  //=> flux d'écriture vers le fichier local target

            byte[] buffer = new byte[8192];  //=> tableau 8192 octets => 8kb
            long done = 0;  //=>nbre octets déjà téléchargé
            int read;  //=> nbre octets lus par chaque itération

            if(progress != null){
                progress.onProgress(0, total);
            }

            while((read = in.read(buffer)) != -1) {  //= retourne -1 si on atteint la fin du flux (EOF)
                out.write(buffer, 0, read);
                done += read;                       //=> màj le total téléchargé

                if(progress != null){
                    progress.onProgress(done, total);
                }
            }
        }
    }


    /**
     * POUR FILE
     * Crée un lien de partage via POST /shares et retourne l’URL générée par le backend
     * @param fileId
     * @param data Format: "recipient|maxUses|expiresDays|allowVersions"
     * @return
     * @throws Exception
     */
    public String shareFile(int fileId, String data) throws Exception {

        //découper => label|description|maxUses|expiresDays|allowVersions
        String[] parts = data.split("\\|");
        if(parts.length < 5){
            throw new IllegalArgumentException("Format de donées invalide \n(attendu: label|description|maxUses|expiresDays|allowVersions|[recipientNote])");
        }

        String label = parts[0];
        String description = parts[1];
        String maxUsesStr = parts[2];
        String expiresDaysStr = parts[3];
        boolean allowVersions = Boolean.parseBoolean(parts[4]);
        String recipientNote = (parts.length > 5) ? parts[5] : null;

        if("null".equals(recipientNote)) recipientNote = null;

       Integer maxUses = null;
       if(!"null".equals(maxUsesStr) && !maxUsesStr.isEmpty()){
           try{
               maxUses = Integer.parseInt(maxUsesStr);
           }catch(NumberFormatException e){
               //ignoré
           }
       }

       Integer expiresDays = null;
        if(!"null".equals(expiresDaysStr) && !expiresDaysStr.isEmpty()){
            try{
                expiresDays = Integer.parseInt(expiresDaysStr);
            }catch(NumberFormatException e){
                //ignoré
            }
        }

        // On peut éventuellement concaténer label et description si le label est petit au niveau DB
        // Mais ici on utilise le label fourni
        return createShare("file", fileId, label, description, recipientNote, maxUses, expiresDays, allowVersions);
    }

    /**
     * POUR Folder
     * @param folderId
     * @param data
     * @return
     * @throws Exception
     */
    public String shareFolder(int folderId, String data) throws Exception {

        //découper => label|description|maxUses|expiresDays|allowVersions
        String[] parts = data.split("\\|");
        if(parts.length < 5){
            throw new IllegalArgumentException("Format de donées invalide \n(attendu: label|description|maxUses|expiresDays|[recipientNote])");
        }

        String label = parts[0];
        String description = parts[1];
        String maxUsesStr = parts[2];
        String expiresDaysStr = parts[3];
        // parts[4] ignore pour dossiers
        String recipientNote = (parts.length > 5) ? parts[5] : null;

        if("null".equals(recipientNote)) recipientNote = null;

        Integer maxUses = null;
        if(!"null".equals(maxUsesStr) && !maxUsesStr.isEmpty()){
            try{
                maxUses = Integer.parseInt(maxUsesStr);
            }catch(NumberFormatException e){
                //ignoré
            }
        }

        Integer expiresDays = null;
        if(!"null".equals(expiresDaysStr) && !expiresDaysStr.isEmpty()){
            try{
                expiresDays = Integer.parseInt(expiresDaysStr);
            }catch(NumberFormatException e){
                //ignoré
            }
        }

        return createShare("folder", folderId, label, description, recipientNote, maxUses, expiresDays, false);
    }

    /**
     * POST /shares
     * créer un share file ou folder
     * @param kind
     * @param targetId
     * @param label
     * @param maxUses
     * @param expiresDays
     * @param allowVersions
     * @return
     * @throws Exception
     */
    public String createShare(String kind, int targetId, String label, String description, String recipientNote, Integer maxUses, Integer expiresDays, boolean allowVersions) throws Exception{
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(targetId <= 0) {
            throw new IllegalArgumentException("id invalide");
        }

        if(!"file".equals(kind) && !"folder".equals(kind)){
            throw new IllegalArgumentException("kind doit être 'file' ou 'folder'");
        }

        // Construire le JSON
        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{");
        jsonBody.append("\"kind\":\"").append(kind).append("\",");
        jsonBody.append("\"target_id\":").append(targetId).append(",");
        jsonBody.append("\"label\":\"").append(JsonUtils.escapeJson(label)).append("\",");
        jsonBody.append("\"allow_fixed_versions\":").append(allowVersions);

        // Ajouter max_uses si présent
        if(maxUses != null && maxUses > 0){
            jsonBody.append(",\"max_uses\":").append(maxUses);
        }

        // ajouter expires_at si présent  =>calculer la date ISO
        if(expiresDays != null && expiresDays > 0){
            // Calculer la date future en ISO 8601
            java.time.ZonedDateTime futureDate = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                    .plusDays(expiresDays);
            String isoDate = futureDate.format(java.time.format.DateTimeFormatter.ISO_INSTANT);

            jsonBody.append(",\"expires_at\":\"").append(isoDate).append("\"");
        }

        if(description != null && !description.isBlank()){
            jsonBody.append(",\"description\":\"").append(JsonUtils.escapeJson(description)).append("\"");
        }

        if(recipientNote != null && !recipientNote.isBlank()){
            jsonBody.append(",\"recipient_note\":\"").append(JsonUtils.escapeJson(recipientNote)).append("\"");
        }

        jsonBody.append("}");

        System.out.println("ApiClient - JSON envoyé: " + jsonBody);

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/shares"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString(), StandardCharsets.UTF_8))
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        System.out.println("ApiClient - Réponse HTTP: " + status);
        System.out.println("ApiClient - Corps: " + body);

        if(status == 201){

            //extraire url du backend
            String url = JsonUtils.extractJsonField(body, "url");
            url = JsonUtils.unescapeJsonString(url);

            if(url == null || url.isBlank()){
                //renvoyer body pour le debug
                return body;
            }
            return url;
        }

        if(status == 400){
            String error = JsonUtils.extractJsonField(body, "error");
            error = JsonUtils.unescapeJsonString(error);
            throw new IllegalArgumentException("Validation échouée : " + error);
        }

        if(status == 403){
            String error = JsonUtils.extractJsonField(body, "error");
            error = JsonUtils.unescapeJsonString(error);
            throw new Exception("Accès refusé : " + error);
        }

        if(status == 404){
            throw new Exception(kind.equals("file") ? "Fichier introuvable" : "Dossier introuvable");
        }

        //géré par executeRequest
//        if(status == 401){
//            throw new AuthenticationException("Non autorisé : token invalide ou expiré");
//        }

        String error = JsonUtils.extractJsonField(body, "error");
        if(error == null || error.isEmpty()){
            error = body;
        }else{
            error = JsonUtils.unescapeJsonString(error);
        }

        throw new RuntimeException("Erreur de partage: (HTTP " + status + "): " + error);

    }

    /**
     * GET /shares
     * Récupère tous les partages et les parse en List<ShareItem> => sans pagination
     * PagedShareResponse listShares(int limit, int offset) => pour la pagination
     * @return
     * @throws Exception
     */
    public PagedShareResponse listShares(int limit, int offset) throws Exception {

        System.out.println("ApiClient - listShares(limit, offset) démarrage...");
        System.out.println("ApiClient - URL: " + baseUrl + "/shares?limit=" +limit + "&offset=" + offset);
        System.out.println("ApiClient - Token: " + (authToken != null ? "présent" : "absent"));

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/shares?limit=" +limit + "&offset=" + offset))
                .header("Accept", "application/json")
                //.header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        System.out.println("ApiClient - Code de statut HTTP: " + status);
        System.out.println("ApiClient - Corps de la réponse: " + response.body());

        if(status != 200){
            throw new RuntimeException("Erreur de partage: (HTTP " + status + "): " + response.body());
        }

        // Parser JSON en List<ShareItem> => ancien sans pagination
//        List<ShareItem> shares = JsonUtils.parseShareItem(response.body());
//        System.out.println("ApiClient - Nombre de partages parsés: " + shares.size());
//        return shares;

        //parser JSON en PagedSharesREsponse
        var paged = JsonUtils.parsePagedSharesResponse(response.body());

        System.out.println("ApiClient - Total: " + paged.getTotal()
                + ", limit: " + paged.getLimit()
                + ", offset: " + paged.getOffset()
                + ", reçus: " + (paged.getShares() != null ? paged.getShares().size() : 0));

        return paged;
    }

    /**
     * PATCH /shares/{id}/revoke
     * Révoque un partage => ok
     * @param id
     * @throws Exception
     */
    public void revokeShare(int id) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/shares/" + id + "/revoke"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();


        if(status == 200 || status == 204){
            //UIDialogs.showInfo("Succès", null, "Partage #\" + id + \" révoqué avec succès");
            System.out.println("Partage #" + id + " révoqué avec succès");
            return;
        }

        // Erreur d'authentification
        //status == 401 => par executeRequest
        if (status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        // Erreur 404 : partage introuvable
        if (status == 404) {
            throw new Exception("Partage #" + id + " introuvable.");
        }

        String errorMessage = parseErrorMessage(response.body(), "Erreur lors de la révocation du partage");
        throw new Exception(errorMessage + " (HTTP " + status + ")");

    }

    /**
     * DELETE /shares/{id}
     * supprimer un partage  => OK
     * @param id
     * @return
     * @throws Exception
     */
    public void deleteShare(int id) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/shares/" + id))
                //.header("Content-Type", "application/json") //=> lehet hogy le kell venni
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();

        //204 => requête réussi, pas besoin de quitter la page
        if(status == 200 || status == 204) {
            System.out.println("Suppression du partage a réussi");
            return;
        }

        //status == 401 => par executeRequest()
        if(status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if (status == 404) {
            throw new Exception("Partage introuvable (déjà supprimé ou n'existe pas).");
        }

        //autres erreur
        String errorMessage = parseErrorMessage(response.body(), "Erreur lors de la suppression du partage");
        throw new Exception(errorMessage + " (HTTP " + status + ")");
    }


    /**
     * PUT /folders/{id}
     * Renomme un dossier avec le nouveau nom
     * @param folderId
     * @param newName
     * @throws Exception
     */
    public void renameFolder(int folderId, String newName, String currentName) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant)."); //=> état de l'objet invalide
        }

        if(folderId <= 0){
            throw new IllegalArgumentException("FolderId invalide " + folderId); //=> argument mauvais
        }

        if(newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }

//        if(newName.trim().equals(currentName)){
//            throw new IllegalArgumentException("Le nouveau nom est identique à l'ancien");
//        }

        String jsonBody = "{"
                + "\"name\":\"" + JsonUtils.escapeJson(newName.trim()) + "\""
                + "}";

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/folders/" + folderId))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        if(status == 200 || status == 204) return;

        String error = JsonUtils.extractJsonField(body, "error");
        if(error == null || error.isEmpty()){
            error = body;
        }

        throw new RuntimeException("Erreur de renameFolder: (HTTP " + status + "): " + error);
    }

    /**
     * PUT /files/{id}
     * Renomme un fichier avec le nouveau nom
     * @param fileId
     * @param newName
     * @throws Exception
     */
    public void renameFile(int fileId, String newName) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0){
            throw new IllegalArgumentException("FileId invalide " + fileId);
        }

        if(newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }

        String jsonBody = "{"
                + "\"name\":\"" + JsonUtils.escapeJson(newName.trim()) + "\""
                + "}";

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/files/" + fileId))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        if(status == 200 || status == 204) return;

        String error = JsonUtils.extractJsonField(body, "error");
        if(error == null || error.isEmpty()){
            error = body;
        }

        throw new RuntimeException("Erreur de renameFile: (HTTP " + status + "): " + error);
    }

    /**
     * Interface callback pour remonter l’avancement (octets envoyés / total) pendant un upload
     */
    public interface ProgressListener {
        void onProgress(long sentBytes, long totalBytes);
    }

    public interface DownloadProgressListener{
        void onProgress(long done, long total);
    }


    /**
     * GET /files/{id}/versions
     * Liste les versions d’un fichier et retourne une List<VersionEntry>
     * List<VersionEntry> => sans pagination
     * @param fileId
     * @param limit
     * @param offset
     * @return
     * @throws Exception
     */
    public PagedVersionsResponse listFileVersions(int fileId, int limit, int offset) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0){
            throw new IllegalArgumentException("FileId invalide " + fileId);
        }

        String url = baseUrl + "/files/" + fileId + "/versions?limit=" + limit + "&offset=" + offset;
        System.out.println("GET " + url);

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                //.header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        System.out.println("GET " + url + "status = " + status);
        System.out.println("GET versions body = " + body );

        //status == 401 => par executeRequest
        if (status == 403){
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if(status != 200){
            String apiError = JsonUtils.extractJsonField(body, "error");

            if(apiError == null || apiError.isEmpty()){
                apiError = body;
                throw new RuntimeException("Erreur listFileVersions (HTTP " + status + "): " + apiError);
            }
        }
        //return JsonUtils.parseVersionEntriesFromVersionsList(body); => sans pagination
        return JsonUtils.parsePagedVersionsResponse(body);
    }

    /**
     * POST /files/{id}/versions
     * Upload une nouvelle version d’un fichier  en multipart avec suivi de progression =>ok
     * @param fileId
     * @param newFile
     * @param progress
     * @throws Exception
     */
    public void uploadNewVersion(int fileId, File newFile, ProgressListener progress) throws Exception {

        if(newFile == null || !newFile.exists()){
            throw new Exception("Fichier invalide");
        }

        //vérifier le token
        String token = this.authToken;
        if(token == null || token.isEmpty()){
            token = AppProperties.get("auth.token");
        }
        if(token == null || token.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0){
            throw new IllegalArgumentException("FileId invalide " + fileId);
        }

        String url = baseUrl + "/files/" + fileId + "/versions";
        String boundary = "----CryptoVaultBoundary" + UUID.randomUUID();
        String contentType = "multipart/form-data; boundary=" + boundary;

        //construction le multipart en bytes
        byte[] bodyBytes = buildMultipartBody(newFile, boundary, null);
        long total = bodyBytes.length;

        HttpRequest.BodyPublisher publisher = new ProgressBodyPublisher(
                HttpRequest.BodyPublishers.ofByteArray(bodyBytes),
                total,
                progress
        );

        // Construction de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", contentType)
                .header("Authorization", "Bearer " + token)
                .POST(publisher)
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        System.out.println("POST " + url + "status = " + status);
        System.out.println("POST versions body = " + body );

        //status == 401 => par executeRequest
        if (status == 403){
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if(status != 201){
            String apiError = JsonUtils.extractJsonField(body, "error");

            if(apiError == null || apiError.isEmpty()){
                apiError = body;
            }
            throw new RuntimeException("Erreur uploadNewVersion (HTTP " + status + "): " + apiError);
        }
    }

    /**
     * GET /files/{id}
     * charge le smétadonnées des files dans FileDetailsController
     * @param fileId
     * @return
     * @throws Exception
     */
    public FileEntry getFile(int fileId) throws Exception {
        if(authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié (auth.token manquant).");
        }

        if(fileId <= 0){
            throw new IllegalArgumentException("FileId invalide " + fileId);
        }

        String url = baseUrl + "/files/" + fileId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();

        if(status != 200){
            String apiError =  JsonUtils.extractJsonField(response.body(), "error");

            if(apiError == null || apiError.isEmpty()){
                apiError = response.body();
            }

            throw new RuntimeException("Erreur getFile (HTTP " + status + "): " + apiError);
        }

        return JsonUtils.parseFileEntry(response.body());
    }

    /**
     * GET /admin/users/quotas
     * Récupère tous les utilisateurs avec leurs quotas (admin uniquement) =>ok
     */
    public List<UserQuota> getAllUsersWithQuota() throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/users/quotas"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();
        String body = response.body();

        if (status == 200) {
            return JsonUtils.parseUserQuotaList(body);
        }

        //status == 401 => par executeRequest
        if (status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        throw new RuntimeException("Erreur: " + body);
    }

    /**
     * PUT /admin/users/{id}/quota
     * Modifie le quota d'un utilisateur (admin uniquement) =>ok
     * @param userId
     * @param newQuotaBytes
     * @throws Exception
     */
    public void updateUserQuota(int userId, long newQuotaBytes) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }

        String json = "{\"quota\":" + newQuotaBytes + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/users/" + userId + "/quota"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();

        if (status == 200 || status == 204) {
            return;
        }

        //status == 401 => par executeRequest
        if (status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        String error = JsonUtils.extractJsonField(response.body(), "error");
        throw new RuntimeException(error != null ? error : "Erreur lors de la modification");
    }

    public void requestPasswordReset(String email) throws Exception {
        // TODO: Implémenter l'appel API backend pour la réinitialisation
        // Exemple:
        // POST /auth/forgot-password
        // Body: {"email": "user@example.com"}

        throw new UnsupportedOperationException(
                "La fonctionnalité de réinitialisation de mot de passe n'est pas encore implémentée côté serveur."
        );
    }

    /**
     * DELETE /admin/users/{id} =>supprimer un user (admin uniquement)
     * @param userId
     * @throws Exception
     */
    public String deleteUser(int userId) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/users/" + userId))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = executeRequest(request);

        int status = response.statusCode();

        //200 => suppression réussi avec résummé
        if(status == 200) {
            System.out.println("Suppression de l'utilisateur réussi");

            //extraire le résumé du backend
            String body = response.body();
            String message = JsonUtils.extractJsonField(body, "message");
            String deletedFiles = JsonUtils.extractJsonField(body, "deleted_files");

            if(message != null && deletedFiles != null){
                return message + " (" + deletedFiles + " fichier(s) supprimé(s))";
            }
            return "Utilisateur supprimé avec succès";
        }

        // 204 No Content : Suppression réussie sans résumé (ancien comportement)
        if (status == 204) {
            System.out.println("Suppression de l'utilisateur réussie (204)");
            return "Utilisateur supprimé avec succès";
        }

        //status == 401 => par executeRequest()
        if(status == 403) {
            throw new AuthenticationException("Accès refusé : permissions insuffisantes.");
        }

        if (status == 404) {
            throw new Exception("Utilisateur introuvable (déjà supprimé ou n'existe pas).");
        }

        // 400 Bad Request => tentative d'auto-suppression
        if (status == 400) {
            String error = parseErrorMessage(response.body(), "Requête invalide");
            throw new Exception(error);
        }

        //autres erreur
        String errorMessage = parseErrorMessage(response.body(), "Erreur lors de la suppression de l'utilisateur");
        throw new Exception(errorMessage + " (HTTP " + status + ")");
    }


    //**************************************  Methode PRIVATE   **************************************

    private String parseErrorMessage(String body, String defaultMessage) {
        try {
            String error = JsonUtils.extractJsonField(body, "error");
            if (error!= null && !error.isEmpty()) {
                return JsonUtils.unescapeJsonString(error);
            }
        } catch (Exception e) {
            // Ignore : le body n'est pas du JSON valide
        }
        return defaultMessage;
    }

    //************************************************************************************************
    //méthodes private  => HELPERS


    /**
     * construction d'un NodeItem "racine" virtuel avec tous les dossiers enfants
     * Reconstruit l’arborescence NodeItem (racine virtuelle + enfants) à partir du JSON des dossiers
     * @param json
     * @return
     */
    private NodeItem buildFolderTreeFromJson(String json) {
        List<JsonUtils.FolderDto> folders = JsonUtils.parseFolders(json);

        // Racine virtuelle (id 0) non affichée parce que TreeView.showRoot = false
        NodeItem root = NodeItem.folder(0, "Racine");
        // ou NodeItem root = NodeItem.folder(0, "Racine", NodeItem.NodeType.FOLDER);
        java.util.Map<Integer, NodeItem> map = new java.util.HashMap<>();

        // Créer tous les noeuds
        for (JsonUtils.FolderDto f : folders) {
            NodeItem node = NodeItem.folder(f.id, f.name);
            // ou NodeItem node = NodeItem.folder(f.id, f.name, NodeItem.NodeType.FOLDER);
            map.put(f.id, node);
        }

        // Assembler l'arborescence selon parent_id
        for (JsonUtils.FolderDto f : folders) {
            NodeItem node = map.get(f.id);

            if (f.parentId == null || f.parentId == 0) {

                // dossier racine
                root.addChild(node);
            } else {
                NodeItem parent = map.get(f.parentId);
                if (parent != null) {
                    parent.addChild(node);
                } else {

                    // parent non trouvé → par sécurité à accrocher à la racine
                    root.addChild(node);
                }
            }
        }
        return root;
    }

    /**
     * Construit le body multipart/form-data (folder_id optionnel + part 'file') pour les uploads
     * @param file
     * @param boundary
     * @return
     * @throws Exception
     */
    private byte[] buildMultipartBody(File file, String boundary, Integer folderId) throws Exception {
        String CRLF = "\r\n";

        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // fallback
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // ---- Partie "folder_id" (si fourni)
        if (folderId != null) {
            String folderPart =
                    "--" + boundary + CRLF +
                            "Content-Disposition: form-data; name=\"folder_id\"" + CRLF + CRLF +
                            folderId + CRLF;

            output.write(folderPart.getBytes(StandardCharsets.UTF_8));
        }

        // ---- Partie "file"
        String filePartHeader =
                "--" + boundary + CRLF +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + CRLF +
                        "Content-Type: " + mimeType + CRLF + CRLF;

        output.write(filePartHeader.getBytes(StandardCharsets.UTF_8));

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        output.write(fileBytes);

        // Fin de la part fichier
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));

        // ---- Fin multipart
        String ending = "--" + boundary + "--" + CRLF;
        output.write(ending.getBytes(StandardCharsets.UTF_8));

        return output.toByteArray();
    }


    /**
     * uploader un fichier dans le dossier racine par défaut => ce n'est pas fait
     * @param file
     * @return
     * @throws Exception
     */
    public boolean uploadFile(File file) throws Exception{
        //s'il n'y a pas dossier => passer en null
        return uploadFile(file, null);
    }


    /**
     * méthode générique pour faire des requêtes HTTP avec gestion du 401
     * @param request
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws AuthenticationException
     */
    private HttpResponse<String> executeRequest(HttpRequest request) throws IOException, InterruptedException, AuthenticationException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        //détection l'expiration token => pas autorisé
        int status = response.statusCode();
        if(status == 401){
            System.out.println("ApiClient - Token invalide ou expiré (401)");

            //déclencher l'expiration de session
            SessionManager.getInstance().handleSessionExpiration();

            throw new AuthenticationException("Votre session a expiré.\nVeuillez vous reconnecter");
        }
        return response;
    }


    /**
     * Exception levée en cas d’erreur d’authentification (token manquant/invalide, accès refusé)
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    /**
     * Exception levée en cas d’échec d’inscription ou de connexion automatique après inscription
     */
    public static class RegistrationException extends Exception {
        public RegistrationException(String message) {
            super(message);
        }
    }


    /**
     * BodyPublisher wrapper qui comptabilise les octets envoyés et notifie un ProgressListener pendant l’upload
     */
    private static class ProgressBodyPublisher implements HttpRequest.BodyPublisher {
        private final HttpRequest.BodyPublisher delegate;
        private final long totalBytes;
        private final ProgressListener listener;

        ProgressBodyPublisher(HttpRequest.BodyPublisher delegate, long totalBytes, ProgressListener listener) {
            this.delegate = delegate;
            this.totalBytes = totalBytes;
            this.listener = listener;
        }

        @Override
        public long contentLength() {
            return totalBytes >= 0 ? totalBytes : delegate.contentLength();
        }

        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
            delegate.subscribe(new Flow.Subscriber<>() {
                long sent = 0;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                    if (listener != null) listener.onProgress(0, totalBytes);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    sent += item.remaining();
                    if (listener != null) listener.onProgress(sent, totalBytes);
                    subscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    if (listener != null) listener.onProgress(totalBytes, totalBytes);
                    subscriber.onComplete();
                }
            });
        }
    }


    // === CORBEILLE (TRASH) ===

    private String doGenericGet(String endpoints) throws Exception {
        if(authToken == null || authToken.isEmpty()) throw new IllegalStateException("Non authentifié.");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoints))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpResponse<String> response = executeRequest(request);
        if(response.statusCode() != 200) throw new Exception("Erreur GET " + endpoints + " : " + response.body());
        return response.body();
    }

    private void doGenericPostOrDelete(String endpoints, boolean isDelete) throws Exception {
        if(authToken == null || authToken.isEmpty()) throw new IllegalStateException("Non authentifié.");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoints))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken);
        if (isDelete) {
            builder.DELETE();
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }
        HttpResponse<String> response = executeRequest(builder.build());
        int status = response.statusCode();
        if(status != 200 && status != 204) throw new Exception("Erreur request " + endpoints + " : " + response.body());
    }

    /**
     * GET /trash/files
     */
    public List<FileEntry> getTrashFiles() throws Exception {
        String json = doGenericGet("/trash/files");
        com.fasterxml.jackson.databind.ObjectMapper tempMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
        com.fasterxml.jackson.databind.JsonNode root = tempMapper.readTree(json);
        if (root.has("data")) {
            return tempMapper.readerForListOf(FileEntry.class).readValue(root.get("data"));
        }
        return tempMapper.readerForListOf(FileEntry.class).readValue(json);
    }

    /**
     * GET /trash/folders
     */
    public List<NodeItem> getTrashFolders() throws Exception {
        String json = doGenericGet("/trash/folders");
        com.fasterxml.jackson.databind.ObjectMapper tempMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
        return tempMapper.readerForListOf(NodeItem.class).readValue(json);
    }

    /**
     * POST /files/{id}/restore
     */
    public void restoreFile(int id) throws Exception {
        doGenericPostOrDelete("/files/" + id + "/restore", false);
    }

    /**
     * POST /folders/{id}/restore
     */
    public void restoreFolder(int id) throws Exception {
        doGenericPostOrDelete("/folders/" + id + "/restore", false);
    }

    /**
     * DELETE /files/{id}/permanent
     */
    public void permanentDeleteFile(int id) throws Exception {
        doGenericPostOrDelete("/files/" + id + "/permanent", true);
    }

    /**
     * DELETE /folders/{id}/permanent
     */
    public void permanentDeleteFolder(int id) throws Exception {
        doGenericPostOrDelete("/folders/" + id + "/permanent", true);
    }
}

"@; $dir = Split-Path "src\main\java\com\coffrefort\client\ApiClient.java"; if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }; [System.IO.File]::WriteAllText("src\main\java\com\coffrefort\client\ApiClient.java", $content, [System.Text.Encoding]::ASCII)
$content = @"
package com.coffrefort.client.util;


import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    /**
     * Extrait la valeur texte d’un champ JSON (entre guillemets) à partir d’une chaîne JSON simple
     * @param json
     * @param fieldName
     * @return
     */
    public static String extractJsonField(String json, String fieldName) {
        if (json == null || fieldName ==  null) return null;

        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;

        int colon = json.indexOf(":", idx + pattern.length());
        if (colon == -1) return null;

        int firstQuote = json.indexOf("\"", colon);
        if (firstQuote == -1) return null;

        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null;

        return json.substring(firstQuote + 1, secondQuote);
    }


    /**
     * Extrait la valeur numérique (int/long sous forme de String) d’un champ JSON à partir d’une chaîne JSON simple
     * (ex: user_id: 6)
     * @param json
     * @param fieldName
     * @return
     */
    public static String extractJsonNumberField(String json, String fieldName) {
        if (json == null) return null;

        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;

        int colon = json.indexOf(":", idx + pattern.length());
        if (colon == -1) return null;

        int i = colon + 1;

        // sauter les espaces
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }

        if (i >= json.length()) return null;

        int start = i;

        // lire les chiffres (+ signe -)
        while (i < json.length()) {
            char c = json.charAt(i);
            if (!Character.isDigit(c) && c != '-') {
                break;
            }
            i++;
        }

        if (i == start) return null;

        return json.substring(start, i);
    }

    /**
     * Extrait le tableau JSON (incluant les crochets []) associé à un champ donné en gérant les crochets imbriqués
     * @param json Le JSON source
     * @param fieldName Le nom du champ contenant le tableau
     * @return Le contenu du tableau (incluant les crochets [])
     */
    public static String extractJsonArrayField(String json, String fieldName) {
        if (json == null) return null;

        String pattern = "\"" + fieldName + "\"";
        int index = json.indexOf(pattern);
        if (index == -1) return null;

        int colon = json.indexOf(":", index + pattern.length());
        if (colon == -1) return null;

        //chercher le crochet ouvrant
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }

        if (i >= json.length() || json.charAt(i) != '['){
            return null;
        }

        int start = i;
        int bracketCount = 1;
        i++;

        //parcourir jusqu'à trouver le crochet fermant correspondant
        while ( i < json.length() && bracketCount > 0 ){
            char c = json.charAt(i);
            if (c == '['){
                bracketCount++;
            }else if (c == ']'){
                bracketCount--;
            }
            i++;
        }

        if(bracketCount == 0){
            return json.substring(start, i);
        }
        return null;
    }

    /**
     * Déséchappe une chaîne JSON (\/, \", \\) pour obtenir la valeur lisible côté client
     * @param s
     * @return
     */
    public static String unescapeJsonString(String s) {
        if (s == null) return null;
        return s
                // séquences JSON classiques
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")

                .replace("\\/", "/")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * il était dans ApiClient
     * Échappe les caractères spéciaux pour insérer une valeur proprement dans une chaîne JSON
     * @param value
     * @return
     */
    public static String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Extrait un champ JSON (chaîne OU nombre)
     * @param json
     * @param fieldName
     * @return
     */
    public static String extractJsonFieldAny(String json, String fieldName){
        //essayer d'abord en tant que chaine
        String value = extractJsonField(json, fieldName);

        if(value == null){
            value = extractJsonNumberField(json, fieldName);
        }
        return value;
    }


    /**
     * Parse la réponse JSON des partages (champ "shares" ou tableau direct) en une liste de ShareItem => sans pagination
     * @param json
     * @return
     */
    public static List<ShareItem> parseShareItem(String json) {
        System.out.println("JsonUtils - parseShareItem() - JSON reçu: " + json);

        List<ShareItem> result = new ArrayList<>();

        if (json == null || json.isBlank()) {
            System.out.println("JsonUtils - JSON vide ou null");
            return result;
        }

        String arrayContent = json.trim();

        // Vérifier si le JSON contient un objet avec un champ "shares"
        if (arrayContent.startsWith("{")) {
            System.out.println("JsonUtils - JSON est un objet, extraction du champ 'shares'");

            String sharesArray = extractJsonArrayField(json, "shares");


            if (sharesArray == null) {
                System.err.println("JsonUtils - ERREUR: Impossible d'extraire le champ 'shares'");
                return result;
            }

            arrayContent = sharesArray;
            System.out.println("JsonUtils - Tableau 'shares' extrait: " + arrayContent);
        }

        // Retirer les crochets [] si présents
        String trimmed = arrayContent.trim();
        if (trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        trimmed = trimmed.trim();

        if (trimmed.isEmpty()) {
            System.out.println("JsonUtils - Tableau vide");
            return result;
        }

        System.out.println("JsonUtils - Contenu après nettoyage (premiers 200 chars): " +
                (trimmed.length() > 200 ? trimmed.substring(0, 200) + "..." : trimmed));

        // Séparation de chaque objet JSON
        List<String> objects = splitJsonObjects(trimmed);
        System.out.println("JsonUtils - Nombre d'objets détectés: " + objects.size());

        for (int idx = 0; idx < objects.size(); idx++) {
            String o = objects.get(idx).trim();

            if (!o.startsWith("{")) {
                o = "{" + o;
            }
            if (!o.endsWith("}")) {
                o = o + "}";
            }

            System.out.println("JsonUtils - Parsing objet " + idx + ": " + o);

            ShareItem item = new ShareItem();

            // id
            String id = extractJsonNumberField(o, "id");
            if (id != null) {
                item.setId(Integer.parseInt(id));
                System.out.println("  - id: " + id);
            }

            //resource => nom du fichier
            //resource => nom du fichier ou dossier
            String fileName = unescapeJsonString(extractJsonField(o, "file_name"));
            String folderName = unescapeJsonString(extractJsonField(o, "folder_name"));
            
            if (fileName != null) {
                item.setResource(fileName);
                item.setFileName(fileName);
            } else if (folderName != null) {
                item.setResource(folderName);
                item.setFolderName(folderName);
            } else {
                item.setResource("Ressource inconnue");
            }

            // resource (utiliser label comme resource pour l'affichage)
            String label = unescapeJsonString(extractJsonField(o, "label"));
            item.setLabel(label);
            System.out.println("  - label: " + label);

            // expires_at
            String expiresAt = unescapeJsonString(extractJsonField(o, "expires_at"));
            item.setExpiresAt(expiresAt != null ? expiresAt : "-");
            System.out.println("  - expires_at: " + expiresAt);

            // remaining_uses
            String remaining = extractJsonNumberField(o, "remaining_uses");
            if (remaining != null && !remaining.isEmpty() && !"null".equalsIgnoreCase(remaining)) {
                item.setRemainingUses(Integer.parseInt(remaining));
                System.out.println("  - remaining_uses: " + remaining);
            } else {
                item.setRemainingUses(null);
                System.out.println("  - remaining_uses: null (illimité)");
            }

            // url
            String url = unescapeJsonString(extractJsonField(o, "url"));
            item.setUrl(url);
            System.out.println("  - url: " + url);

            // is_revoked
            String revoked = extractJsonNumberField(o, "is_revoked");
            boolean isRevoked = "1".equals(revoked);
            item.setRevoked(isRevoked);
            System.out.println("  - is_revoked: " + isRevoked);

            result.add(item);
        }

        System.out.println("JsonUtils - Total d'items parsés: " + result.size());
        return result;
    }

    /**
     * Parse la réponse paginée des shares
     * @param json
     * @return
     */
    public static PagedShareResponse parsePagedSharesResponse(String json) {
        try{
            return mapper.readValue(json, PagedShareResponse.class);
        }catch(Exception e){
            throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
        }
    }

    /**
     * Parse la réponse paginée des fichiers
     */
    public static PagedFilesResponse parsePagedFilesResponse(String json) {
        try {
            return mapper.readValue(json, PagedFilesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
        }
    }

    /**
     * Parse la réponse paginée des versions d'un fichier
     */
    public static PagedVersionsResponse parsePagedVersionsResponse(String json) {
        try {
            // FIX: Gère le cas où le serveur renvoie [] (array) au lieu d'un objet paginé {}
            if (json != null && json.trim().startsWith("[")) {
                return new PagedVersionsResponse(new ArrayList<>(), 0, 0, 10);
            }
            return mapper.readValue(json, PagedVersionsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
        }
    }

    /**
     * Découpe une chaîne contenant plusieurs objets JSON en objets individuels en comptant les accolades
     * Sépare une chaîne contenant plusieurs objets JSON
     * Plus robuste que split() car gère les virgules dans les valeurs
     */
    private static List<String> splitJsonObjects(String content) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '{') {
                if (braceCount == 0) {
                    start = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(content.substring(start, i + 1));
                }
            }
        }

        return objects;
    }


    /**
     * Parse la réponse JSON paginée des versions (champ "items") en une liste de VersionEntry
     * @param json
     * @return
     */
    public static List<VersionEntry> parseVersionEntriesFromVersionsList(String json) {

        List<VersionEntry> result = new ArrayList<>();

        if (json == null || json.isBlank()) {
            System.out.println("JsonUtils - JSON vide ou null");
            return result;
        }

        //réponse de backend => { file_id, page, limit, total, items: [ ... ] }
        String itemsArray = extractJsonArrayField(json, "versions");
        if (itemsArray == null || itemsArray.isBlank()) {
            System.out.println("'versions' recu du backend vide ou null");
            return result;
        }

        String trimmed = itemsArray.trim();

        // enlever []
        if(trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        trimmed = trimmed.trim();

        if(trimmed.isEmpty()) {
            return result;
        }

        List<String> objects = splitJsonObjects(trimmed);

        for(String object : objects) {
            String o = object.trim();
            if (!o.startsWith("{")) {
                o = "{" + o;
            }
            if (!o.endsWith("}")) {
                o = o + "}";
            }

            String idString = extractJsonNumberField(o, "id");
            String versionString =  extractJsonNumberField(o, "version");
            String sizeString =  extractJsonNumberField(o, "size");
            String createdAtString =  unescapeJsonString(extractJsonField(o, "created_at"));
            String checksumHex = unescapeJsonString(extractJsonField(o, "checksum"));

            int id = (idString != null && !idString.isEmpty()) ? Integer.parseInt(idString) : 0;
            int version = (versionString != null && !versionString.isEmpty()) ? Integer.parseInt(versionString) : 0;
            long size = (sizeString != null && !sizeString.isEmpty()) ? Long.parseLong(sizeString) : 0L;

            Boolean isCurrent = false;
            String isCurrentStr = extractJsonField(o, "is_current");
            if (isCurrentStr != null && isCurrentStr.equals("true")) {
                isCurrent = true;
            }

            result.add(new VersionEntry(id, version, size, createdAtString, checksumHex, isCurrent));
        }

        return result;
    }

    public static FileEntry parseFileEntry(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        // id
        String idStr = extractJsonNumberField(json, "id");
        int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;

        // name => "original_name" renvoyé par le backend
        String name = unescapeJsonString(extractJsonField(json, "original_name"));
        if (name == null || name.isBlank()) {
            name = unescapeJsonString(extractJsonField(json, "filename"));
        }

        if (name == null || name.isBlank()) {
            // fallback => au cas ou un jour changer le champ côté backend
            name = unescapeJsonString(extractJsonField(json, "name"));
        }

        if (name == null) name = "Fichier sans nom";

        // size
        String sizeStr = extractJsonNumberField(json, "size");
        long size = (sizeStr != null && !sizeStr.isEmpty()) ? Long.parseLong(sizeStr) : 0L;

        // created_at
        String createdAt = unescapeJsonString(extractJsonField(json, "created_at"));
        if (createdAt == null) createdAt = "";

        // created_at
        String updatedAt = unescapeJsonString(extractJsonField(json, "updated_at"));
        if (updatedAt == null) updatedAt = "";

        // current_version
        String versionStr = extractJsonNumberField(json, "current_version");
        int version = (versionStr != null && !versionStr.isEmpty()) ? Integer.parseInt(versionStr) : 1;

        if (id <= 0) {
            // Si l’API renvoie une erreur HTML/texte => éviter de créer un objet “fantôme”
            throw new IllegalArgumentException("parseFileEntry: champ 'id' invalide ou manquant. JSON=" + json);
        }

        return FileEntry.of(id, name, size, createdAt, updatedAt, version);
    }

    /**
     * Parse une liste d'utilisateurs avec quotas
     * @param json
     * @return
     * @throws Exception
     */
//    public static List<UserQuota> parseUserQuotaList(String json) throws Exception {
//
//        List<UserQuota> list = new ArrayList<>();
//
//        String usersJson = extractJsonArrayField(json, "users");
//        if(usersJson == null || usersJson.isEmpty()) {
//            return list;
//        }
//
//        //Parser chaque user
//        String [] userBlocks = usersJson.split("\\},\\s*\\{");
//
//        for(String block : userBlocks) {
//            block = block.replaceAll("^\\[?\\{?", "").replaceAll("\\}?\\]?`$", "");;
//
//            int id = Integer.parseInt(extractJsonField("{" + block + "}", "id"));
//            //String username = extractJsonField("{" + block + "}", "username");
//            String email = extractJsonField("{" + block + "}", "email");
//            long used = Long.parseLong(extractJsonField("{" + block + "}", "used"));
//            long max = Long.parseLong(extractJsonField("{" + block + "}", "max"));
//
//            Boolean isAdmin = false;
//            String isAdminStr = extractJsonField("{" + block + "}", "is_admin");
//            if (isAdminStr != null && isAdminStr.equals("true")) {
//                isAdmin = true;
//            }
//
//            String role = isAdmin ? "admin" : "user";
//
//            list.add(new UserQuota(id, email, used, max, role));
//        }
//        return list;
//    }

    public static List<UserQuota> parseUserQuotaList(String json) throws Exception {
        List<UserQuota> list = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode usersNode = root.get("users");
        if (usersNode == null || !usersNode.isArray()) {
            return list;
        }

        for (JsonNode u : usersNode) {
            int id = u.path("id").asInt();
            String email = u.path("email").asText(null);

            long used = u.path("used").asLong();
            long max = u.path("max").asLong();

            boolean isAdmin = false;
            JsonNode isAdminNode = u.get("is_admin");
            if (isAdminNode != null) {
                if (isAdminNode.isBoolean()) {
                    isAdmin = isAdminNode.asBoolean();
                } else {
                    // si l’API renvoie 0/1 ou "0"/"1"
                    String s = isAdminNode.asText("");
                    isAdmin = s.equals("1") || s.equalsIgnoreCase("true");
                }
            }

            String role = isAdmin ? "admin" : "user";
            list.add(new UserQuota(id, email, used, max, role));
        }

        return list;
    }

    // ILS ETAIENT DANS APICLIENT!
    /**
     * DTO interne pour parser les dossiers (id, name, parentId) avant de reconstruire l’arbre
     * DTO =Data Transfer Object => pour structurer les données pour les rendre faciles à échanger
     */
    public static class FolderDto{
        public int id;
        public String name;
        public Integer parentId;
    }

    /**
     * Parse un JSON de type:
     *   [ { "id":1, "name":"Docs", "parent_id":null }, ... ]
     * ou un seul objet:
     *   { "id":1, "name":"Docs", "parent_id":null, ... }
     */
    /**
     * Parse un JSON de dossiers (tableau ou objet) en liste de FolderDto (id/name/parentId)
     * Parse un JSON de type:
     *   [ { "id":1, "name":"Docs", "parent_id":null }, ... ]
     * ou un seul objet:
     *   { "id":1, "name":"Docs", "parent_id":null, ... }
     * @param json
     * @return
     */
    public static List<FolderDto> parseFolders(String json) {
        List<FolderDto> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        String trimmed = json.trim();

        String[] parts;

        if (trimmed.startsWith("[")) {    // => tableau: [ {...}, {...} ]

            // enlever les crochets
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isBlank()) return result;

            // découper à la grosse: "},{"
            parts = trimmed.split("\\},\\s*\\{"); //=> les objets séparés par } , {
        } else {

            //un seul objet: { ... }
            parts = new String[]{ trimmed };
        }

        for (String part : parts) {
            String objet = part.trim();
            if (!objet.startsWith("{")) objet = "{" + objet;
            if (!objet.endsWith("}")) objet = objet + "}";

            FolderDto dto = new FolderDto();

            // "id": 1
            String idStr = JsonUtils.extractJsonNumberField(objet, "id");

            if (idStr != null) {
                dto.id = Integer.parseInt(idStr);
            }

            // "name": "Documents"
            dto.name = JsonUtils.extractJsonField(objet, "name");

            // "parent_id": null ou un nombre
            String parentStr = JsonUtils.extractJsonNumberField(objet, "parent_id");
            if (parentStr != null) {
                dto.parentId = Integer.parseInt(parentStr);
            } else {
                dto.parentId = null; // parent_id null => dossier racine
            }
            result.add(dto);
        }
        return result;
    }

    /**
     * Parse un JSON de fichiers en liste de FileEntry (id, nom, taille, date)
     * @param json
     * @return
     */
    private List<FileEntry> parseFiles(String json) {
        List<FileEntry> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        String trimmed = json.trim();
        String[] parts;

        if (trimmed.startsWith("[")) {

            // Cas tableau: [ {...}, {...} ]
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isBlank()) return result;

            // découpe grossière sur "},{"
            parts = trimmed.split("\\},\\s*\\{");
        } else {

            //un seul objet: { ... }
            parts = new String[]{ trimmed };
        }

        // découper à la grosse: "},{"
        String[] filesParts = trimmed.split("\\},\\s*\\{");

        for (String part : filesParts) {
            String objet = part.trim();
            if (!objet.startsWith("{")) objet = "{" + objet;
            if (!objet.endsWith("}")) objet = objet + "}";


            String idStr = JsonUtils.extractJsonNumberField(objet, "id");
            String name = JsonUtils.extractJsonField(objet, "original_name");
            String sizeStr = JsonUtils.extractJsonNumberField(objet, "size");
            String date = JsonUtils.extractJsonField(objet, "created_at");
            String updatedDate =  JsonUtils.extractJsonField(objet, "updated_at");
            String versionStr = JsonUtils.extractJsonNumberField(objet, "current_version");

            int id = (idStr != null) ? Integer.parseInt(idStr) : 0;
            long size = (sizeStr != null )? Long.parseLong(sizeStr) : 0L;
            int version = (versionStr != null && !versionStr.isEmpty()) ? Integer.parseInt(versionStr) : 1;

            if (name == null) {
                name = JsonUtils.extractJsonField(objet, "filename");
            }
            if (name == null) {
                // sécurité : si jamais ton API change de champ un jour
                name = JsonUtils.extractJsonField(objet, "name");
            }

            result.add(FileEntry.of(id, name, size, date, updatedDate, version));
        }
        return result;
    }

















    /**
     * Empêche l’instanciation de la classe utilitaire JsonUtils.
     */
    private JsonUtils() {
        // constructeur privé pour empêcher l'instanciation
    }












}

"@; $dir = Split-Path "src\main\java\com\coffrefort\client\util\JsonUtils.java"; if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }; [System.IO.File]::WriteAllText("src\main\java\com\coffrefort\client\util\JsonUtils.java", $content, [System.Text.Encoding]::ASCII)
$content = @"
package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.App;
import com.coffrefort.client.model.FileEntry;
import com.coffrefort.client.model.NodeItem;
import com.coffrefort.client.model.PagedFilesResponse;
import com.coffrefort.client.model.Quota;
import com.coffrefort.client.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.TableRow;
import com.coffrefort.client.controllers.ShareSuccessController;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Optional;

import com.coffrefort.client.config.AppProperties;
import com.coffrefort.client.util.UIDialogs;
import com.coffrefort.client.util.FileUtils;
import javafx.scene.Node;

public class MainController {

    //propriétés
    @FXML private TreeView<NodeItem> treeView;
    @FXML private TableView<FileEntry> table;
    @FXML private TableColumn<FileEntry, String> nameCol;
    @FXML private TableColumn<FileEntry, String> sizeCol;
    @FXML private TableColumn<FileEntry, String> dateCol;
    @FXML private TableColumn<FileEntry, Integer> versionCol;

    @FXML private ProgressBar quotaBar;
    @FXML private Label quotaLabel;
    private String quotaColor = "#5cb85c"; //=> pour la couleur persistante
    private boolean quotaStyleInitialized = false;
    private int quotaStyleRetries = 0;
    private static final int MAX_QUOTA_STYLE_RETRIES = 20;
    private Quota currentQuota;

    @FXML private Label userEmailLabel;
    @FXML private Label statusLabel;
    @FXML private Label fileCountLabel;

    @FXML private Button uploadButton;
    @FXML private Button shareButton;
    @FXML private Button deleteButton;
    @FXML private Button newFolderButton;
    @FXML private Button logoutButton;
    @FXML private Button returnToRootButton;
    @FXML private Button refreshQuotaButton;
    @FXML private Button gestionQuota;
    @FXML private Pagination pagination;
    @FXML private ToggleButton themeToggleButton;
    @FXML private javafx.scene.image.ImageView logoView;

    private boolean isDarkTheme = true;


    private ApiClient apiClient;
    private Runnable onLogout;
    private ObservableList<FileEntry> fileList = FXCollections.observableArrayList();
    private NodeItem currentFolder;
    private App app;
    private String currentNameFolder;
    private String currentNameFile;

    private Stage mainStage;

    private static final int FILES_PER_PAGE = 10; //remettre à 20 ou 10 => pour modifier le limit!
    private int currentPage = 0; //=> pour garder la trace de la page actuelle
    private int totalFiles = 0;

    //méthodes

    @FXML
    private void initialize() {

        //préparation l'interface
        setupTable();
        setupTreeView();
        setupTreeViewRootContextMenu();

        //configuration la PageFactory (pagination) avant charger les données!
        pagination.setPageFactory(this::loadPage);
        pagination.setVisible(false);
        pagination.setManaged(false);

        // Initialiser le bouton de thème et le logo
        App.updateThemeButton(themeToggleButton);
        App.updateLogo(logoView);

        //mettre en place le listener
        // quand je clique sur un dossier => currentFolder <=> currentFolder= null
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                NodeItem node = newItem.getValue();

                if (node.getType() == NodeItem.NodeType.FOLDER){
                    currentFolder = node;
                    loadFiles(currentFolder);
                }
            }
        });

        refreshUI();
        loadData();

        // Initialiser l'état du bouton theme
        App.updateThemeButton(themeToggleButton);
    }

    @FXML
    private void handleGoToRoot() {
        currentFolder = null;
        treeView.getSelectionModel().clearSelection();
        loadFiles(null);
    }

    private void refreshUI() {

        //mise à jour compteur
        updateFileCount();

        //mise à jour le quota
        updateQuota();

        //mise à jour email d'utilisateur
        String email = AppProperties.get("auth.email");
        if(email != null && !email.isEmpty()){
            userEmailLabel.setText(email);
        }

        System.out.println("userEmail: " + userEmailLabel.getText());

        // pour garantir le styles inline => éviter le  CSS externe
        // label bold inline
        // on laisse le CSS gérer le quotaLabel
        quotaBar.setStyle("-fx-pref-height: 8px;");

        // IMPORTANT : on laisse JavaFX créer la skin, puis on stylise (avec retry)
        //progressbar => création des noeuds intern .track(fond), .bar(partie remplie)

        Platform.runLater(this::refreshQuotaBarStyleWithRetry);

        // Détection du plein écran pour augmenter la police (Futuriste !)
        Platform.runLater(() -> {
            if (treeView.getScene() != null && treeView.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) treeView.getScene().getWindow();
                stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        treeView.getScene().getRoot().setStyle("-fx-font-size: 18px;");
                    } else {
                        treeView.getScene().getRoot().setStyle("-fx-font-size: 16px;");
                    }
                });
            }
        });

        // Si la scene arrive / change -> restyle
        quotaBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                quotaStyleRetries = 0;
                Platform.runLater(this::refreshQuotaBarStyleWithRetry);
            }
        });

        // Si la skin change -> restyle
        quotaBar.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            quotaStyleRetries = 0;
            Platform.runLater(this::refreshQuotaBarStyleWithRetry);
        });

        // À chaque changement de progress, JavaFX peut reconstruire la bar -> restyle
        quotaBar.progressProperty().addListener((obs, oldV, newV) -> {
            Platform.runLater(this::refreshQuotaBarStyle);
        });

        // premier passage
        Platform.runLater(() -> {
            initQuotaBarStyleOnce();
            refreshQuotaBarStyle();
        });

        //masquer le bouton quota si pas admin
        if(gestionQuota != null){
            gestionQuota.setVisible(false);
            gestionQuota.setManaged(false);
        }
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        System.out.println("MainController - setApiClient() appelé, apiClient = " + (apiClient != null ? "OK" : "NULL"));
        System.out.println("MainController - Instance hashCode = " + this.hashCode());
    }

    public void setApp(App app){
        this.app = app;
        System.out.println("MainController - setApp() appelé, app = " + (app != null ? "OK" : "NULL"));
        System.out.println("MainController - Instance hashCode = " + this.hashCode());
    }

    public void setOnLogout(Runnable callback) {
        this.onLogout = callback;
    }

    public void setUserEmail(String email) {
        if (userEmailLabel != null) {
            userEmailLabel.setText(email);
        }
    }

    /**
     * mettre à jour les colonnes dans TableView
     */
    private void setupTable() {
        // Configuration des colonnes
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("updatedAtFormatted"));
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        
        // Centrer la version
        versionCol.setStyle("-fx-alignment: CENTER;");

        table.setItems(fileList);

        // Activer/désactiver les boutons selon la sélection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            shareButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        //clique sur une ligne
        table.setRowFactory(tv -> {
            TableRow<FileEntry> row = new TableRow<>();

            //menu contextuel par ligne
            ContextMenu contextMenu = new ContextMenu();

            MenuItem openItem = new MenuItem("👁  Ouvrir");
            openItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    handleOpenFile(file);
                }
            });

            MenuItem renameItem = new MenuItem("✏️  Renommer...");
            renameItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    openRenameFileDialog(file);
                }
            });

            MenuItem downloadItem = new MenuItem("📥  Télécharger");
            downloadItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    handleDownload(file);
                }
            });

//            au cas ou pour plus tard, si je veux changer...
//            MenuItem deleteItem = new MenuItem("Supprimer...");
//            deleteItem.setOnAction(e -> {
//                FileEntry file = row.getItem();
//                if (file != null) {
//                    table.getSelectionModel().select(file);
//                    handleDelete(); // réutilise ton flow confirmDelete.fxml
//                }
//            });

            MenuItem detailsItem = new MenuItem("📜  Historique & Versions");
            detailsItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    openFileDetailsDialog(file);
                }
            });

            contextMenu.getItems().addAll(openItem, new SeparatorMenuItem(), renameItem, downloadItem, detailsItem);

            //affichage le menu => que si la ligne n'est pas vide
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            //ouvrir les détails d'un fichier en double cliquant dessus
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty()){
                    FileEntry selected = row.getItem();
                    handleOpenFile(selected);
                }
            });
            return row;
        });
    }

    /**
     * mise à jour : Listener sur le TreeView
     */
    private void setupTreeView() {

        // Style de l'arborescence
        treeView.setCellFactory(tv -> {
            TreeCell<NodeItem> cell = new TreeCell<>() {

                @Override
                protected void updateItem(NodeItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    setText("📁 " + item.getName());

                    // ne pas proposer suppression sur la racine virtuelle => id=0
                    if (item.getId() == 0) {
                        setContextMenu(null);
                    } else {
                        // afficher le menu au clique droite => setContextMenu()
                        // rendre le clique droit active
                        setContextMenu(createFolderContextMenu(this));
                    }
                }
                }
            };
            return cell;
        });
    }


    /**
     * création du menu contextuel pour un dossier donné
     * @param cell
     * @return
     */
    private ContextMenu createFolderContextMenu(TreeCell<NodeItem>  cell){
        ContextMenu menu = new ContextMenu();

        MenuItem createInside = new MenuItem("Nouveau dossier ici...");
        createInside.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            if (folder != null){
                openCreateFolderDialog(folder); // => parent = dossier cliqué
            }
        });

        MenuItem renameItem = new MenuItem("Renommer ce dossier");
        renameItem.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            if (folder != null){
                openRenameFolderDialog(folder);
            }
        });

        MenuItem shareItem = new MenuItem("Partager ce dossier");
        shareItem.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            if (folder != null){
                handleShareFolder(folder);
            }
        });

        MenuItem deleteItem = new MenuItem("Supprimer ce dossier...");
        deleteItem.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            TreeItem<NodeItem> treeItem = cell.getTreeItem();

            if (folder != null && treeItem != null) {
                handleDeleteFolder(folder, treeItem);
            }
        });

        menu.getItems().addAll(createInside, renameItem, shareItem, new SeparatorMenuItem(), deleteItem);
        return menu;
    }


    /**
     * Gestion de comportement de la souris sur le TreeView
     * clic droit ou clic gauche
     */
    private void setupTreeViewRootContextMenu(){
        ContextMenu rootMenu = new ContextMenu();

        MenuItem createRootFolder = new MenuItem("Nouveau dossier à la racine...");
        createRootFolder.setOnAction(event -> openCreateFolderDialog(null));

        //ajoute le bouton au menu racine
        rootMenu.getItems().addAll(createRootFolder);

        //déclenchement que sur clic droit
        treeView.setOnContextMenuRequested(event -> {

            //détecter si la souris est sur une TreeCell => récupération du noeud -> texte, icône, cellule...
            Node node = event.getPickResult().getIntersectedNode();

            while ( node != null && !(node instanceof TreeCell) ) {
                node = node.getParent();
            }

            //clic droit sur un dossier => laisser le menu du dossier
            if(node instanceof TreeCell<?> cell && cell.getItem() !=null){
                // on fait rien=> "créer sous-dossier" / "supprimer dossier"
                return;
            }

            //sinon -> zone vide => afficher le menu racine
            rootMenu.show(treeView, event.getScreenX(), event.getScreenY());
            event.consume();

        });

        //on clique gauche dans le vide => déselectionne
        treeView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {  // => détection un clic gauche

                Node node = event.getPickResult().getIntersectedNode();
                while ( node != null && !(node instanceof TreeCell) ) {
                    node = node.getParent();
                }

                if (!(node instanceof TreeCell)){
                    treeView.getSelectionModel().clearSelection();
                    currentFolder = null;
                    fileList.clear();
                    updateFileCount();
                    statusLabel.setText("Aucun dossier séléctioné");
                }
            }
        });
    }


    /**
     * Chargement les données, l'arborescence
     */
    private void loadData() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                // Charger l'arborescence depuis l'API
                NodeItem root = apiClient.listRoot();

                Platform.runLater(() -> {
                    TreeItem<NodeItem> rootItem = buildTree(root);
                    treeView.setRoot(rootItem);

//                    Sélectionner le premier dossier si disponible
//                    il ne faut plus séléctionner automatiquement le premier dossier!!
//                    if (!rootItem.getChildren().isEmpty()) {
//                        TreeItem<NodeItem> first = rootItem.getChildren().get(0);
//                        treeView.getSelectionModel().select(first);
//                        currentFolder = first.getValue();
//
//                        // charge les fichiers du 1er dossier
//                        loadFiles(currentFolder);
//                    }

                    treeView.getSelectionModel().clearSelection();
                    currentFolder = null;
                    loadFiles(null);

                    // Vérification si aucun dossier n'existe (première connexion)
                    if (root.getChildren().isEmpty()) {
                        uploadButton.setDisable(true);
                        UIDialogs.showInfo("Bienvenue sur ObsiLock !", 
                                "C'est votre première connexion.", 
                                "Pour garantir la sécurité et l'organisation de vos fichiers, vous devez créer au moins un dossier avant de pouvoir uploader des documents.");
                        statusLabel.setText("Veuillez créer un dossier");
                    } else {
                        uploadButton.setDisable(false);
                        statusLabel.setText("Données chargées");
                    }

                    updateFileCount();
                });

                // Charger les quotas avec endpoint
                //updateQuota();
//                Platform.runLater(() -> {
//                    statusLabel.setText("Données chargées");
//                });

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    UIDialogs.showError("Erreur de chargement", null, "Impossible de charger les données: " + e.getMessage());
                    statusLabel.setText("Erreur de chargement");
                });
            }
        }).start();
    }

    /**
     * Construction visuelle de l'arbre
     * @param node
     * @return
     */
    private TreeItem<NodeItem> buildTree(NodeItem node) {

        TreeItem<NodeItem> item = new TreeItem<>(node);
        item.setExpanded(true);

        for (NodeItem child : node.getChildren()) {
            item.getChildren().add(buildTree(child));
        }
        return item;
    }

    /**
     * charge une page de fichier => appelé par la Pagination quand user clique sur une page
     * @param pageIndex
     * @return
     */
    private Node loadPage(int pageIndex){
        currentPage = pageIndex;
        loadFiles(currentFolder, pageIndex);
        return new VBox(); //=> retourne un node vide => la table est déjà affichée
    }

    /**
     * Chargement des fichiers d'un dossier =>ok
     * @param folder
     */
    private void loadFiles(NodeItem folder, int page) {
        if (apiClient == null) return;

        statusLabel.setText("Chargement des fichiers ...");

        new Thread(() -> {
            try{
                Integer folderId = (folder != null) ? folder.getId() : null;
                int offset = page * FILES_PER_PAGE;

                PagedFilesResponse response = apiClient.listFilesPaginated(folderId, FILES_PER_PAGE, offset );
                //var files = apiClient.listFiles(folder.getId()); => sans pagination

                Platform.runLater(() -> {
                    fileList.setAll(response.getFiles());
                    totalFiles = response.getTotal();

                    //mise à jour la pagination
                    int totalPages = (int) Math.ceil((double) totalFiles / FILES_PER_PAGE);
                    pagination.setPageCount(Math.max(1, totalPages));
                    pagination.setCurrentPageIndex(page);

                   //afficher/ masquer la pagination
                    boolean showPagination = totalPages > 1;
                    pagination.setVisible(showPagination);
                    pagination.setManaged(showPagination);

                    updateFileCount();
                    statusLabel.setText("Fichier chargés");
                });

            }catch(Exception e){
                e.printStackTrace();
                Platform.runLater(() -> {
                    fileList.clear(); //vider en cas d'erreur
                    pagination.setVisible(false);
                    pagination.setManaged(false);
                    UIDialogs.showError("Erreur", null, "Impossible de charger les fichiers: " + e.getMessage());
                    statusLabel.setText("Erreur de chargement des fichiers");
                });
            }
        }).start();
    }

    /**
     * surcharge pour charger la première page
     * @param folder
     */
    private void loadFiles(NodeItem folder){
        loadFiles(folder, 0);
    }

    // à écrire!!!!
    private void updateFileCount() {

        int count = (fileList == null) ? 0 : fileList.size();

        if (fileCountLabel != null) {
            fileCountLabel.setText(count + " fichier" + (count > 1 ? "s" : ""));
        }
    }

// *****************************************   functions pour le quota   ****************************************

    private void initQuotaBarStyleOnce() {
        if (quotaStyleInitialized) return;
        quotaStyleInitialized = true;

        // Track (fond) : on le fixe une fois (sera réappliqué si skin change via refresh)
        var track = quotaBar.lookup(".track");  //=> cherche dans la ProgressBar le nœud interne CSS nommé .track
        if (track != null) {
            track.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 4px; -fx-background-insets: 0;");
        }
    }

    private void setQuotaColor(String hexColor) {
        quotaColor = hexColor;
        refreshQuotaBarStyleWithRetry();
    }

    private void refreshQuotaBarStyleWithRetry() {
        // Essayer d'appliquer, et si bar/track pas prêts, retenter quelques pulses
        if (!refreshQuotaBarStyle()) {
            if (quotaStyleRetries++ < MAX_QUOTA_STYLE_RETRIES) {
                Platform.runLater(this::refreshQuotaBarStyleWithRetry);
            } else {
                System.out.println("QuotaBar style: impossible de trouver .bar/.track après retries");
            }
        }
    }

    /**
     * @return true si .bar existe (style appliqué), false sinon
     */
    private boolean refreshQuotaBarStyle() {
        var track = quotaBar.lookup(".track");
        var bar = quotaBar.lookup(".bar");

        // si pas encore prêt, on ne fait rien
        if (track == null || bar == null) {
            return false;
        }

        track.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 4px; -fx-background-insets: 0;");
        bar.setStyle(
                "-fx-background-color: " + quotaColor + ";" +
                        "-fx-background-radius: 4px;" +
                        "-fx-background-insets: 0;"
        );
        return true;
    }

    @FXML
    private void handleUpdateQuotaAction() {
        updateQuota();
        statusLabel.setText("Quota mis à jour.");
    }

    /**
     * mettre à jour le quota
     */
    private void updateQuota() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                currentQuota = apiClient.getQuota();

                long used = currentQuota.getUsed();
                long max = currentQuota.getMax();

                Platform.runLater(() -> {
                    if (currentQuota == null) {
                        quotaBar.setProgress(0.0);
                        quotaLabel.setText("0 B / 0 B");

                        setQuotaColor("#d9534f"); //rouge
                        refreshQuotaBarStyleWithRetry();
                        return;
                    }

                    double ratio = currentQuota.getUsageRatio();
//                    if (ratio < 0) ratio = 0;
//                    if (ratio > 1) ratio = 1;

                    // progress
                    quotaBar.setProgress(ratio); // valeur entre 0 et 1

                    // texte et style via CSS classes
                    quotaLabel.setText(FileUtils.formatSize(used) + " / " + FileUtils.formatSize(max));
                    quotaLabel.getStyleClass().remove("quota-label-alert");
                    if (!quotaLabel.getStyleClass().contains("quota-label")) {
                        quotaLabel.getStyleClass().add("quota-label");
                    }

                    // couleur et état du bouton upload
                    boolean hasFolders = false;
                    if (treeView != null && treeView.getRoot() != null) {
                        hasFolders = !treeView.getRoot().getChildren().isEmpty();
                    }

                    if (ratio >= 0.9) {
                        quotaColor = "#ff4757"; // rouge néon
                        quotaLabel.getStyleClass().add("quota-label-alert");
                        statusLabel.setText("Quota atteint — upload bloqué");
                        uploadButton.setDisable(true);
                    }
                    else if (ratio >= 0.8) {
                        quotaColor = "#f0ad4e"; // orange
                        uploadButton.setDisable(!hasFolders);
                        if (!hasFolders) statusLabel.setText("Veuillez créer un dossier");
                    }
                    else {
                        quotaColor = "#2ecc71"; // vert émeraude standard
                        uploadButton.setDisable(!hasFolders);
                        if (!hasFolders) statusLabel.setText("Veuillez créer un dossier");
                    }

                    // restyle
                    quotaStyleRetries = 0;
                    refreshQuotaBarStyleWithRetry();
                });

            } catch (Exception e) {
                statusLabel.setText("Erreur lors du chargement du quota");
                e.printStackTrace();
                Platform.runLater(() -> {
                    quotaBar.setProgress(0.0);
                    quotaLabel.setText("Erreur quota");
                    quotaLabel.getStyleClass().remove("quota-label");
                    quotaLabel.getStyleClass().add("quota-label-alert");
                    quotaStyleRetries = 0;
                    refreshQuotaBarStyleWithRetry();
                });
            }
        }).start();
    }

    /**
     * Afficher le bouton de gestion quota si user est admin
     */
    public void checkAdminRole(){
        try{
            if (gestionQuota == null) return;
            
            //vérif si le rôle depuis le token ou API
            boolean isAdmin = apiClient.isAdmin();

            gestionQuota.setVisible(isAdmin);
            gestionQuota.setManaged(isAdmin);
        }catch (Exception e){
            if (gestionQuota != null) {
                gestionQuota.setVisible(false);
                gestionQuota.setManaged(false);
            }
        }
    }

    @FXML
    private  void handleQuota(){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/quotaManagement.fxml")
            );

            Scene scene = new Scene(loader.load());

            //récupération du contrôleur
            QuotaManagementController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(("Gestion des quotas"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(gestionQuota.getScene().getWindow());
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            controller.setApiClient(apiClient);
            controller.refreshNow();

            // mise à jour le quota après la fermeture de quotaManagement.fxml
            dialogStage.setOnHidden(event -> {
                updateQuota();
            });

            dialogStage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de gestion des quotas "+e.getMessage());
        }
    }



    // *****************************************   functions pour share  *************************
    /**
     * Gestion de share des fichiers =>ok
     */
    @FXML
    private void handleShare() {
        FileEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        shareButton.setDisable(true);

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/share.fxml")
            );

            VBox root =  loader.load();

            //récupération du contrôleur
            ShareController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(("Créer un lien de partage"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(shareButton.getScene().getWindow());

            //interdire de redimensionner  la fenêtre => taille fixe
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);
            controller.setItemName(selected.getName());

            //callback => quand user clique sur partage
            controller.setOnShare(data -> {
                statusLabel.setText("Partage en cours... ");

                new Thread(() -> {
                    try{
                        String url  = apiClient.shareFile(selected.getId(), data);

                        Platform.runLater(() -> {
                            statusLabel.setText("Lien: " + url);
                            showShareDialog(url);
                        });
                        System.out.println(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Partage", null,"Erreur " + e.getMessage());
                            statusLabel.setText("Erreur pendant le partage");
                        });
                    }
                }).start(); //lancement du Thread
            });

            //réactivation du bouton de partage
            dialogStage.setOnHidden(event -> shareButton.setDisable(false));

            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de partage "+e.getMessage());
            shareButton.setDisable(false);
        }
    }

    /**
     * Gestion de share des dossiers
     * @param folderNode
     * @throws Exception
     */
    private void handleShareFolder(NodeItem folderNode){
        if(folderNode == null) return;

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/share.fxml")
            );

            VBox root =  loader.load();

            //récupération du contrôleur
            ShareController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(("Créer un lien de partage"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());

            //interdire de redimensionner  la fenêtre => taille fixe
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);
            controller.setItemName(folderNode.getName());

            //controller.setIsFolder(true); => indiquer que c'est un folder....
            //désactiver allowVersions pour les dossiers

            //callback => quand user clique sur partage
            //callback => quand user clique sur partage
            controller.setOnShare(data -> {
                statusLabel.setText("Partage du dossier en cours... ");

                new Thread(() -> {
                    try{
                        String url  = apiClient.shareFolder(folderNode.getId(), data);

                        Platform.runLater(() -> {
                            statusLabel.setText("Lien: " + url);
                            showShareDialog(url);
                        });
                        System.out.println(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Partage", null,"Erreur " + e.getMessage());
                            statusLabel.setText("Erreur pendant le partage du dossier");
                        });
                    }
                }).start(); //lancement du Thread
            });
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de partage "+e.getMessage());
            shareButton.setDisable(false);
        }
    }


    /**
     * Afficher URL
     * @param url
     */
    private void showShareDialog(String url){

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/shareSuccess.fxml"));
            VBox root = loader.load();
            
            ShareSuccessController controller = loader.getController();
            controller.setUrl(url);
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            if (shareButton != null && shareButton.getScene() != null) {
                stage.initOwner(shareButton.getScene().getWindow());
            }
            stage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showInfoUrl("Partage réussi", "Lien de partage généré", url);
        }
    }

    /**
     * gestion de "Mes partages"
     */
    @FXML
    private void handleOpenShares() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/myshares.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            MySharesController controller = loader.getController();


            Stage dialogStage = new Stage();
            dialogStage.setTitle("Mes partages");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            Scene scene = new Scene(root, 900, 600);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            controller.setApiClient(apiClient);
            controller.setStage(dialogStage);

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de myshares.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de Mes partages: " + e.getMessage());
        }

    }
    @FXML
    private void handleOpenTrash() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/trash.fxml")
            );

            VBox root = loader.load();
            TrashController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ma Corbeille — ObsiLock");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            Scene scene = new Scene(root, 1000, 700);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            controller.setApiClient(apiClient);
            controller.setStage(dialogStage);

            dialogStage.showAndWait();
            
            // Recharger la vue principale au cas où des éléments auraient été restaurés
            loadData();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de trash.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir la corbeille: " + e.getMessage());
        }
    }

    // *****************************************   functions pour file *************************

    /**
     * Gestion d'upload des fichiers =>ok
     */
    @FXML
    private void handleUpload() {

        // Vérifier si un dossier est sélectionné ET que ce n'est pas "Ma Racine" (ID 0)
        if (currentFolder == null || currentFolder.getId() == 0) {
            UIDialogs.showInfo("Action impossible", 
                    "Aucun dossier sélectionné", 
                    "Veuillez créer ou sélectionner un dossier d'abord.");
            return;
        }

        if(currentQuota != null && currentQuota.getUsed() >= currentQuota.getMax()){
            UIDialogs.showError("Quota atteint",
                    null,
                    "Votre espace de stockage est plein. Veuillez supprimer des fichiers.");
            return;
        }

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/uploadDialog.fxml")
            );

            Parent root = loader.load();

            //récupération du contrôleur
            UploadDialogController controller = loader.getController();
            controller.setApiClient(apiClient);

            if(currentFolder != null){
                controller.setTargetFolderId(currentFolder.getId());
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Uploader des fichiers");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(uploadButton.getScene().getWindow());
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);

            //callback pour rafraîchir après upload
            controller.setOnUploadSuccess(() ->{
                Platform.runLater(() -> {
                    if(currentFolder != null){
                        loadFiles(currentFolder);
                    }

                    updateQuota();
                    statusLabel.setText("Upload terminé");
                });
            });

            dialogStage.showAndWait();

        }catch(Exception e){
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre d'upload "+e.getMessage());
        }
    }

    /**
     * ouvrir le dialog renameFile.fxml pour renommer un fichier =>ok
     * @param file
     */
    private void openRenameFileDialog(FileEntry file){
        if(file == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/renameFile.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            RenameFileController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Renommer le fichier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(treeView.getScene().getWindow()); => il est dans la table et pas dans treeView!!
            dialogStage.initOwner(table.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);


            dialogStage.setWidth(420);
            dialogStage.setHeight(400);

            controller.setStage(dialogStage);

            currentNameFile =file.getName();
            controller.setCurrentName(currentNameFile);

            controller.setOnConfirm(newName -> {

                //vérif si les 2 noms sont identiques
                if(newName.trim().equals(currentNameFile)){
                    Platform.runLater(() -> {
                        UIDialogs.showError("Renommer", null, "Le nouveau nom est identique à l'ancien.");
                        //dialogStage.close();
                    });
                    return;
                }

                statusLabel.setText("Renommage en cours...");
                new Thread(() -> {
                    try {
                        apiClient.renameFile(file.getId(), newName); //=> il faut id et name
                        Platform.runLater(() -> {
                            //dialogStage.close(); => si je laisse içi le texte de showInfo ne voit pas

                            if(currentFolder != null){
                                loadFiles(currentFolder);
                            }else{
                                loadData(); // => refresh tout
                            }
                            statusLabel.setText("Fichier renommé en \"" + newName + "\"");
                            statusLabel.setVisible(true);

                            UIDialogs.showInfo(
                                    "Renommage réussi",
                                    null,
                                    "Le fichier a été renommé en \"" + newName + "\"."
                            );
                            dialogStage.close();
                        });
                    } catch (IllegalArgumentException e) {
                        // erreur de validation
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de validation", null, e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            statusLabel.setVisible(true);
                            // pas fermer le dialogue
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de renommage", null,"Erreur: " + e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            statusLabel.setVisible(true);
                            // pas fermer le dialogue
                        });
                    }
                }).start();
            });

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de renameFile.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir renameFile.fxml: " + e.getMessage());
        }
    }

    /**
     * ouvrir le dialog fileDetails.fxml pour voir les versions d'un fichier =>ok
     * @param file
     */
    private void openFileDetailsDialog (FileEntry file){

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/fileDetails.fxml")
            );

            Parent root = loader.load();

            // Récupération du contrôleur
            FileDetailsController controller = loader.getController();
            controller.setApiClient(apiClient);
            controller.setFile(file);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detail - " + file.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(table.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            controller.setCurrentName(file.getName());

            controller.setOnVersionUploaded(() -> Platform.runLater(() -> {
                if(currentFolder != null) {
                    loadFiles(currentFolder);
                }
                updateQuota();
                statusLabel.setText("Version remplacée: " + file.getName());
            }));

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de fileDetails.fxml");
            e.printStackTrace();
            UIDialogs.showError("Detail fichier", null,"Impossible d'ouvrir la fenetre: " + e.getMessage());
        }
    }
    /**
     * gestion de suppression d'un fichier => ok
     */
    @FXML
    private void handleDelete() {
        FileEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        deleteButton.setDisable(true);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmDelete.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmDeleteController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la suppresion du fichier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(deleteButton.getScene().getWindow());
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            // Injection du stage et du nom de fichier
            controller.setDialogStage(dialogStage);

            // personnaliser pour fichier
            controller.setMessage("Voulez-vous vraiment supprimer ce fichier ?");
            controller.setFileName(selected.getName());

            //callbacks
            controller.setOnConfirm(() -> deleteFile(selected));
            controller.setOnCancel(() -> statusLabel.setText("Suppression annulé"));
            dialogStage.showAndWait();

        } catch (Exception e){
            System.err.println("Erreur lors du chargement de confirmDelete.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de suppression: "+e.getMessage());
        } finally {
            deleteButton.setDisable(false);
        }
    }

    /**
     * supprimer un file =>ok
     * @param file
     */
    private void deleteFile(FileEntry file) {
        if (file == null) {
            return;
        }
        statusLabel.setText("Suppression en cours...");

        //désactiver avant la suppression
        shareButton.setDisable(true);
        deleteButton.setDisable(true);

        new Thread(() -> {
            try {
                apiClient.permanentDeleteFile(file.getId());

                Platform.runLater(() -> {

                    fileList.remove(file);  // => ça n'enleve  que localement

                    if(currentFolder != null){ //=> recharger complètement le dossier
                        loadFiles(currentFolder);
                    }

                    //mise à jour le compteur et le quota
                    updateFileCount();
                    updateQuota();

                    //garder les boutons désactivés => pas de sélection
                    // Les boutons sont gérés par le listener de sélection

                    statusLabel.setText("Fichier supprimé: " + file.getName());

                    UIDialogs.showInfo("Suppression réussie",
                            null,
                            "Le fichier \"" + file.getName() + "\" a été supprimé."
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    //réactiver les boutons
                    shareButton.setDisable(false);
                    deleteButton.setDisable(false);

                    // Les boutons sont gérés par le listener de sélection

                    UIDialogs.showError("Erreur de suppression", null, "Erreur: " + e.getMessage());
                    statusLabel.setText("Erreur de suppression");
                });
            }
        }).start();
    }

    // *****************************************   functions pour download  *************************
    /**
     * gestion de téléchargement d'un fichier =>ok
     * @param file
     */
    private void handleDownload(FileEntry file) {

        if(file == null) return;

        FileChooser chooser = new FileChooser();

        //à choisir où enregistrer
        chooser.setTitle("Enregistrer le fichier...");

        // définir le nom => par défaut
        chooser.setInitialFileName(file.getName());

        //configuration automatique les filtres
        FileUtils.configureFileChooserFilter(chooser, file.getName());

        File target = chooser.showSaveDialog(table.getScene().getWindow());
        if (target == null){
            statusLabel.setText("Le téléchargement est annulé");
            return;
        }

        statusLabel.setText("Téléchargement de " + file.getName() + "...");

        new Thread(() -> {
            try {
                apiClient.downloadFileTo(file.getId(), target);

                Platform.runLater(() -> {
                    statusLabel.setText("Téléchargé " + target.getAbsolutePath());
                    updateQuota();

                    UIDialogs.showInfo(
                            "Téléchargement réussi",
                            null, "Le fichier a été téléchargé: \n"
                                    + target.getAbsolutePath()
                    );
                    
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(target);
                        }
                    } catch (Exception ex) {
                        System.err.println("Impossible d'ouvrir le fichier: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    UIDialogs.showError("Téléchargement échoué", null, "Impossible de télécharger: " + e.getMessage());
                    statusLabel.setText("Erreur de téléchargement");
                });
            }
        }).start();
    }

    // *****************************************   functions pour folders  *************************

    /**
     * Gestion de cas de "création d'un folder"
     */
    @FXML
    private void handleNewFolder() {
//        openCreateFolderDialog(currentFolder); // currentFolder peut être null => racine
        openCreateFolderDialog(null); //=> à la racine!!!
    }

    /**
     * Création d'un Folder
     * @param name
     */
    private void createFolder(String name, NodeItem parentFolder) {
        statusLabel.setText("Création du dossier...");

        new Thread(() -> {
            try {
                boolean success = apiClient.createFolder(name, parentFolder); //=> parentFolder peut être null

                Platform.runLater(() -> {
                    if (success) {
                        loadData(); // Recharger l'arborescence
                        statusLabel.setText("Dossier créé: " + name);
                    } else {
                        UIDialogs.showError("Erreur", null, "Impossible de créer le dossier.");
                        statusLabel.setText("Erreur de création");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    UIDialogs.showError("Erreur", null,"Erreur: " + e.getMessage());
                    statusLabel.setText("Erreur de création");
                });
            }
        }).start();
    }

    /**
     * ouvrir le dialog CreatFolder.fxml pour créer un dossier avec à la racine
     * @param parentFolder
     */
    private void openCreateFolderDialog(NodeItem parentFolder){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/createFolder.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            CreateFolderController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un nouveau dossier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            controller.setOnCreateFolder(name -> createFolder(name, parentFolder));

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de createFolder.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de création: " + e.getMessage());
        }
    }

    /**
     * ouvrir le dialog renameFolder.fxml pour renommer un dossier =>ok
     * @param folder
     */
    private void openRenameFolderDialog(NodeItem folder){
        if(folder == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/renameFolder.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            RenameFolderController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Renommer le dossier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));

            controller.setStage(dialogStage);
            currentNameFolder = folder.getName();
            controller.setCurrentName(currentNameFolder);

            controller.setOnConfirm(newName -> {

                //vérif si les 2 noms sont identiques
                if(newName.trim().equals(currentNameFolder)){
                    Platform.runLater(() -> {
                        UIDialogs.showError("Renommer", null , "Le nouveau nom est identique à l'ancien");
                        //dialogStage.close();
                    });
                    return;
                }

                statusLabel.setText("Renommage en cours...");

                new Thread(() -> {
                    try {
                        apiClient.renameFolder(folder.getId(), newName, currentNameFolder); //=> il faut id et name, currenNameFolder au cas ou

                        Platform.runLater(() -> {

                            loadData(); // => refresh Tree (arborescence
                            statusLabel.setText("Dossier renommé en \"" + newName + "\"");

                            UIDialogs.showInfo(
                                    "Renommage réussi",
                                    null,
                                    "Le dossier a été renommé en \"" + newName + "\"."
                            );

                            // il faut laisser içi!!! => sinon showInfo de renommage ne s'affiche pas!!
                            dialogStage.close(); //=> fermer si succès
                        });

                    } catch (IllegalArgumentException e) {
                        //Erreur de validation => nom vide, caractères invalides..
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de validation", null, e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            // pas fermer le dialogue
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de renommage", null, "Erreur: " + e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            // pas fermer le dialogue
                        });
                    }
                }).start();
            });

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de renameFolder.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir renameFolder.fxml" + e.getMessage());
        }
    }



    /**
     * pour gérer la suppression d'un dossier =>ok
     * @param folder
     * @param treeItem => élément visuel dans le TreeView
     */
    private void handleDeleteFolder(NodeItem folder, TreeItem<NodeItem> treeItem){
        if(folder == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmDeleteFolder.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmDeleteFolderController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la suppresion du dossier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Injection du stage et du nom de fichier
            controller.setDialogStage(dialogStage);
            controller.setFolderName(folder.getName());

            //callbacks
            controller.setOnConfirm(() -> deleteFolderOnServer(folder, treeItem));
            controller.setOnCancel(() -> statusLabel.setText("Suppression du dossier annulé"));
            dialogStage.showAndWait();

        } catch (Exception e){
            System.err.println("Erreur lors du chargement de confirmDeleteFolder.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir la fenêtre de suppression: "+e.getMessage());
        }

//        Optional<ButtonType> result = confirm.showAndWait();
//        if(result.isPresent() && result.get() == ButtonType.OK){
//            deleteFolderOnServer(folder, treeItem);
//        }else{
//            statusLabel.setText("Suppression du dossier annulée");
//        }
    }

    /**
     * supprimer le dossier sur le serveur (via API) + mise à jour l'affichage =>ok
     * @param folder
     * @param treeItem
     */
    private void deleteFolderOnServer(NodeItem folder, TreeItem<NodeItem> treeItem){
        if(folder == null) return;
        statusLabel.setText("Suppression du dossier en cours ...");

        new Thread(() -> {
            try{
                apiClient.permanentDeleteFolder(folder.getId());

                Platform.runLater(() -> {

                    //Si on est dans ce dossier => vider la table
                    if(currentFolder != null && currentFolder.getId() ==  folder.getId()){
                        fileList.clear();
                        updateFileCount();
                        currentFolder = null;
                    }

                    //recharger arborescence
                    loadData();
                    updateQuota();

                    statusLabel.setText("Dossier supprimé: " + folder.getName());

                    UIDialogs.showInfo(
                            "Suppression réussie",
                            null,
                            "Le dossier \"" + folder.getName() + "\" a été supprimé."
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    String errorMessage = e.getMessage();
                    if(errorMessage != null && errorMessage.contains("fichiers")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Le dossier contient des fichiers.\n" +
                                        "Veuillez d'abord supprimer tous les fichiers."
                        );
                    }else if(errorMessage != null && errorMessage.contains("sous-dossiers")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Le dossier contient des sous-dossiers.\n" +
                                        "Veuillez d'abord supprimer tous les sous-dossiers."
                        );
                    }else if(errorMessage != null && errorMessage.contains("introuvable")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Dossier introuvable ou déjà supprimé."
                        );
                    }else{
                        UIDialogs.showError(
                                "Erreur de suppression",
                                null,
                                "Erreur lors de la suppression du dossier.\n" +
                                        (errorMessage != null ? errorMessage : "Erreur inconnue")
                        );
                    }
                    statusLabel.setText("Erreur de suppression du dossier");
                });
            }
        }).start();
    }

    /*************************************** Logout *****************************************************

    /**
     * Gestion de déconnexion =>ok
     */
    @FXML
    private void handleLogout() {
        logoutButton.setDisable(true);

        System.out.println("MainController - handleLogout() appelé");
        System.out.println("MainController - Instance hashCode = " + this.hashCode());
        System.out.println("MainController - app is null ? " + (app == null));
        System.out.println("MainController - apiClient is null ? " + (apiClient == null));

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmLogout.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmLogoutController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la déconnexion");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(logoutButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Injection du stage et de la logique de déconnexion
            controller.setDialogStage(dialogStage);
            controller.setOnLogoutConfirmed(() -> {

                // Déconnexion (suppression du token)
                apiClient.logout();
                System.out.println("Déconnexion effectuée. Retour à l'écran de connexion...");

                //arrêter la surveillance de session
                SessionManager.getInstance().stopSessionMonitoring();

                // Fermer la fenêtre de dialogue AVANT de changer de scène
                dialogStage.close();

                // Utiliser Platform.runLater pour changer de scène de manière sûre
                Platform.runLater(() -> {
                    try {

//                        FXMLLoader loginLoader = new FXMLLoader(
//                                getClass().getResource("/com/coffrefort/client/login2.fxml")
//                        );
//                        Parent loginRoot = loginLoader.load();
//                        // Récupérer le contrôleur du login
//                        LoginController loginController = loginLoader.getController();
//                        // Injecter l'ApiClient existant
//                        loginController.setApiClient(apiClient);

                        // Récupérer la fenêtre principale (Stage)
                        Stage stage = (Stage)logoutButton.getScene().getWindow();

                        //appel la méthode openlogin de App
                        if(app != null){
                            System.out.println("MainController - Appel de app.openLogin()");

                            app.openLogin(stage); //Appel DIRECT, pas de callback

                            System.out.println("Redirection vers la page de connexion réussie.");
                        }else{
                            System.err.println("Erreur: App n'est pas injecté dans MainController");
                            UIDialogs.showError("Erreur", "Erreur de déconnexion", "Impossible de retourner à l'écran de connexion");
                        }

                        // Remplacer la scène par celle du login
//                        Scene loginScene = new Scene(loginRoot, 420, 600);
//                        stage.setTitle("Connexion - CryptoVault");
//                        stage.setScene(loginScene);
//                        stage.centerOnScreen();
//                        stage.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Erreur lors du chargement de login2.fxml");

                        // Afficher un message d'erreur à l'utilisateur

                        UIDialogs.showError("Erreur", "Erreur de déconnexion", "Impossible de charger l'écran de connexion." );
//                        Alert alert = new Alert(Alert.AlertType.ERROR);
//                        alert.setTitle("Erreur");
//                        alert.setHeaderText("Erreur de déconnexion");
//                        alert.setContentText("Impossible de charger l'écran de connexion.");
//                        alert.showAndWait();
                    }
                });
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de confirmLogout.fxml");

            // Afficher un message d'erreur
            UIDialogs.showError("Erreur", "Erreur de déconnexion", "Impossible de charger la fenêtre de confirmation.");

//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Erreur");
//            alert.setHeaderText("Erreur de déconnexion");
//            alert.setContentText("Impossible de charger la fenêtre de confirmation.");
//            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la déconnexion");
            e.printStackTrace();

        } finally {

            // Réactiver le bouton après fermeture du dialogue
            logoutButton.setDisable(false);
        }
    }
    /**
     * Ouvrir un fichier (télécharge dans temp et ouvre)
     * @param fileEntry
     */
    private void handleOpenFile(FileEntry fileEntry) {
        if (fileEntry == null) return;

        statusLabel.setText("Ouverture de " + fileEntry.getName() + "...");

        new Thread(() -> {
            try {
                // Créer un fichier temporaire
                String tempDir = System.getProperty("java.io.tmpdir");
                File tempFile = new File(tempDir, "obsilock_" + System.currentTimeMillis() + "_" + fileEntry.getName());
                
                apiClient.downloadFileTo(fileEntry.getId(), tempFile);

                Platform.runLater(() -> {
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(tempFile);
                            statusLabel.setText("Aperçu ouvert: " + fileEntry.getName());
                        } else {
                            UIDialogs.showError("Erreur", null, "L'ouverture automatique n'est pas supportée sur ce système.");
                        }
                    } catch (Exception ex) {
                        UIDialogs.showError("Erreur", null, "Impossible d'ouvrir le fichier : " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    UIDialogs.showError("Erreur", null, "Échec du téléchargement temporaire : " + e.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void handleToggleTheme() {
        if (table.getScene() != null) {
            App.toggleTheme(table.getScene());
            App.updateThemeButton(themeToggleButton);
            App.updateLogo(logoView);
        }
    }
}
"@; $dir = Split-Path "src\main\java\com\coffrefort\client\controllers\MainController.java"; if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }; [System.IO.File]::WriteAllText("src\main\java\com\coffrefort\client\controllers\MainController.java", $content, [System.Text.Encoding]::ASCII)
$content = @"
package com.coffrefort.client.controllers;


import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.FileEntry;
import com.coffrefort.client.model.PagedVersionsResponse;
import com.coffrefort.client.model.VersionEntry;
import com.coffrefort.client.util.UIDialogs;
import com.coffrefort.client.util.FileUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.util.List;

public class FileDetailsController {

    @FXML private Label fileNameLabel;
    @FXML private Label fileMetaLabel;

    @FXML private Button replaceButton;

    @FXML private VBox progressBox;
    @FXML private Label uploadStatusLabel;
    @FXML private ProgressBar uploadProgressBar;
    @FXML private Label errorLabel;
    @FXML private Label versionsCountLabel;

    @FXML private TableView<VersionEntry> versionsTable;
    @FXML private Pagination pagination;
    @FXML private TableColumn<VersionEntry, Number> versionCol;
    @FXML private TableColumn<VersionEntry, String> sizeCol;
    @FXML private TableColumn<VersionEntry, String> dateCol;
    @FXML private TableColumn<VersionEntry, String> checksumCol;

    @FXML private Button copyChecksumButton;
    @FXML private Button openLocalFolderButton;
    @FXML private Button downloadVersionButton;

    private final ObservableList<VersionEntry> versions = FXCollections.observableArrayList();

    private ApiClient apiClient;
    private FileEntry file;
    private Stage stage;

    private static final int PAGE_SIZE = 4; //=> à modifier si je veux changer la limit
    private int totalVersions = 0;
    private int currentPage = 0; //=> pour garder la trace de la page actuelle

    private Runnable onVersionUploaded;

    private Service<Void> uploadService;

    // pour garder une “trace” locale des téléchargements pour activer "ouvrir dossier local"
    // ObservableMap pour que les bindings JavaFX se mettent à jour
    // Map key = "fileId:v{versionNumber}" -> downloaded file path
    private final ObservableMap<String, Path> downloadedPaths = FXCollections.observableHashMap();

    @FXML
    /**
     * Initialise la table des versions, configure les bindings des boutons et prépare l’UI
     */
    private void initialize(){
        System.out.println("FileDetailsController - initialize() appelée");

        //configuration la PageFactory (pagination) avant charger les données!
        pagination.setPageFactory(this::loadPage);
        pagination.setVisible(false);
        pagination.setManaged(false);

        //Table setup
        setupVersionTable();

        versionsTable.setItems(versions);

        rebinButtons();

        //pour assurer qu'il n'y a pas binding
        progressBox.visibleProperty().unbind();
        progressBox.managedProperty().unbind();

        // le progress UI caché au début
        setProgressVisible(false);

        //utilisateur change de sélection => effacer les messages d'erreur
        versionsTable.getSelectionModel().selectedItemProperty().addListener((obs, v, n) -> hideError());

    }

    /**
     * charge une page de version d'un fichier => appelé par la Pagination quand user clique sur une page
     * @param pageIndex
     * @return
     */
    private Node loadPage(int pageIndex){
        currentPage = pageIndex;
        loadVersions(pageIndex);
        return new VBox(); //=> retourne un node vide => la table est déjà affichée
    }


    /**
     * Injecte l’ApiClient utilisé pour charger les versions et effectuer les uploads/téléchargements
     * @param apiClient
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        maybeRefresh();
    }

    /**
     * Injecte le Stage courant afin d’ouvrir des FileChooser et gérer la fenêtre.
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void updateTitle(){
        if(stage != null && file != null){
            stage.setTitle("Detail - " + file.getName());
        }
    }

    /**
     * Définit le fichier courant dont les versions sont affichées
     * @param file
     */
    public void setFile(FileEntry file) {
        this.file = file;
        maybeRefresh();
    }

    /**
     * Met à jour le label affichant le nom du fichier courant
     * @param name
     */
    public void setCurrentName(String name){
        if(fileNameLabel != null){
            fileNameLabel.setText(name != null ? name : "");
        }
    }

    /**
     * Définit le callback exécuté après l’upload réussi d’une nouvelle version
     * @param onVersionUploaded
     */
    public void setOnVersionUploaded(Runnable onVersionUploaded) {
        this.onVersionUploaded = onVersionUploaded;
    }

    //refresh quand apiclient et file sont injectés!!
    private void maybeRefresh() {
        if(this.apiClient != null && this.file != null) {
            hydrateThenRefresh();
        }
    }

    private void hydrateThenRefresh(){
        if(apiClient == null || file == null) return;

        versionsCountLabel.setText("Chargement...");

        new Thread(() -> {
            try {
                // pour rafraîchir les métadonnées
                FileEntry fresh = apiClient.getFile(file.getId());

                Platform.runLater(() -> {
                    this.file = fresh; // => mise à jour les valeurs de header (en-tête)
                    refresh(); // => appelle refreshHeader() ET loadVersions()
                    updateTitle();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    versions.clear();
                    versionsCountLabel.setText("Erreur");
                    UIDialogs.showError("Erreur", null, "Impossible de charger: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Rafraîchit l’en-tête du fichier et recharge la liste des versions depuis l’API
     */
    public void refresh(){
        refreshHeader();  // affiche nom, taille, date
        loadVersions(0);   // charge TOUTES les versions?????
    }

    /**
     * Met à jour les informations affichées du fichier (nom, taille, date de modification)
     */
    private void refreshHeader(){

        if(file == null) return;

        fileNameLabel.setText(file.getName() != null ? file.getName() : "");

        String size = file.getFormattedSize() != null ? file.getFormattedSize() : "";
        String date = file.getUpdatedAtFormatted() != null ? file.getUpdatedAtFormatted() : "";
        String meta = size;

        if (!size.isBlank() && !date.isBlank()) {
            meta += " • ";
        }
        if (!date.isBlank()) {
            meta += "Modifié le " + date;
        }

        fileMetaLabel.setText(meta.trim());
    }

    /**
     * Charge les versions du fichier en tâche de fond et met à jour la table et le compteur=>ok
     */
    private void loadVersions(int page){
        if(apiClient == null || file == null) return;

        //feedback UI
        versionsCountLabel.setText("Chargement des versions...");

        new Thread(() -> {
            try{
                int offset = page * PAGE_SIZE;

                //page => 1 , limit => 10µ
                //Charger la liste complète des versions => rempli la table
                //List<VersionEntry> list = apiClient.listFileVersions(file.getId(), 1, 100);

                PagedVersionsResponse response = apiClient.listFileVersions(file.getId(), PAGE_SIZE, offset); //limit et offset

                Platform.runLater(() -> {
                    var versionsList = response.getVersions();
                    //versions.setAll(versionsList);
                    versionsTable.getItems().setAll(versionsList);
                    System.out.println("FileDetailsController - " + versionsList.size() + " versions chargés");

                    totalVersions = response.getTotal();

                    //mise à jour la pagination
                    int totalPages = (int) Math.ceil((double) totalVersions / PAGE_SIZE);
                    pagination.setPageCount(Math.max(1, totalPages));
                    pagination.setCurrentPageIndex(page);

                    //afficher/ masquer la pagination
                    boolean showPagination = totalPages > 1;
                    pagination.setVisible(showPagination);
                    pagination.setManaged(showPagination);

                    versionsCountLabel.setText(totalVersions + " version(s)");

                    System.out.println("FileDetailsController - Total: " + totalVersions +
                            ", Pages: " + totalPages +
                            ", Page actuelle: " + (page + 1));
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    versionsTable.getItems().clear();
                    pagination.setVisible(false);
                    pagination.setManaged(false);
//                    versions.clear();
                    versionsCountLabel.setText("Erreur");
                    UIDialogs.showError("Erreur", null,"Impossible de charger les versions " + e.getMessage());
                });
            }
        }).start();
    }



    //activation d'un bouton si (que) un bouton est séléctionné
    private void rebinButtons(){

        copyChecksumButton.disableProperty().bind(
                Bindings.isNull(versionsTable.getSelectionModel().selectedItemProperty())
        );

        downloadVersionButton.disableProperty().bind(
                Bindings.isNull(versionsTable.getSelectionModel().selectedItemProperty())
        );

        // ouvrir le dossier local que s'il y a un chemin enregistré
        openLocalFolderButton.disableProperty().bind(
                Bindings.createBooleanBinding(() -> {
                    VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();
                    if(sel == null || file == null) return true;

                    return !downloadedPaths.containsKey(key(file.getId(), sel.getVersion()));
                }, versionsTable.getSelectionModel().selectedItemProperty(), downloadedPaths)
        );
    }


    /**
     * Configure les colonnes de la table des versions et le double-clic pour copier le checksum
     */
    private void setupVersionTable(){

        //colonne version
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

        //taille formatée
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));

        // date formatée
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));

        //checksum short
        checksumCol.setCellValueFactory(new PropertyValueFactory<>("checksumShort"));

        //clique sur la ligne
        versionsTable.setRowFactory(tv -> {
            TableRow<VersionEntry> row = new TableRow<>();

            //menu contextuel par ligne
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteVersion = new MenuItem("Supprimer cette version...");
            deleteVersion.setOnAction(e -> {
                VersionEntry version = row.getItem();
                if (version != null) {
                    versionsTable.getSelectionModel().select(version);
                    handleDeleteVersion();
                }
            });

            contextMenu.getItems().addAll(deleteVersion);

            //affichage le menu => que si la ligne n'est pas vide
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            // avec double clique => copier le checksum
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty()) {
                    onCopyChecksum();
                }
            });
            return row;
        });
    }

    /**
     * gestion de la suppression d'une version =>ok
     */
    public void handleDeleteVersion(){
        VersionEntry selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || file == null){
            return;
        }

        //ne pas supprimer la version courante
        if(selected.getIsCurrent()){
            UIDialogs.showError("Suppression impossible",
                    null,
                    "Impossible de supprimer la version active du fichier.\n" +
                            "Veuillez d'abord uploader une nouvelle version."
            );
            return;
        }

        // ne pas supprimer si c'est la seul version
        if(versions.size() <= 1){
            UIDialogs.showError("Suppression impossible",
                    null,
                    "Impossible de supprimer la dernière version du fichier."
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmDelete.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmDeleteController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la suppresion de la version");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(versionsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Injection du stage et du nom de fichier
            controller.setDialogStage(dialogStage);

            // personnaliser pour fichier
            controller.setMessage("Voulez-vous vraiment supprimer cette version ?");
            controller.setFileName(
                    file.getName() + " - Version " + selected.getVersion() +
                            " (" + selected.getFormattedSize() + ")"
            );

            //callbacks
            controller.setOnConfirm(() -> deleteVersion(selected));
            controller.setOnCancel(() -> {
                if(uploadStatusLabel != null){
                    uploadStatusLabel.setText("Suppression annulée");
                }
            });
            dialogStage.showAndWait();

        } catch (Exception e){
            System.err.println("Erreur lors du chargement de confirmDelete.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de suppression: "+e.getMessage());
        }
    }

    /**
     * supprimer une version =>OK
     * @param version
     */
    private void deleteVersion(VersionEntry version){
        if (apiClient == null || file == null || version == null) {
            return;
        }
        setProgressVisible(true);
        uploadStatusLabel.setText("Suppression de la version " + version.getVersion() + " ...");
        uploadProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        // il faut unbind avant de modifier les boutons
        copyChecksumButton.disableProperty().unbind();
        openLocalFolderButton.disableProperty().unbind();
        downloadVersionButton.disableProperty().unbind();

        //désactiver les boutons
        replaceButton.setDisable(true);
        copyChecksumButton.setDisable(true);
        openLocalFolderButton.setDisable(true);
        downloadVersionButton.setDisable(true);

        //en cas de supprime le dernière élément de la page, retourner à la page précédente
        //dernière élément de la page (et pas la page 1)=> aller à la page précédente
        int itemsOnPage = versionsTable.getItems().size();
        int nextPage = (itemsOnPage == 1 && currentPage > 0) ? currentPage -1 : currentPage;

        new Thread(() -> {
            try {
                apiClient.deleteVersion(file.getId(), version.getId());

                Platform.runLater(() -> {

                    //masquer la progression
                    setProgressVisible(false);

                    //supprimer localement => ce n'est pas bon => il faut recharger depuis le serveur
//                    versions.remove(version);
                    loadVersions(nextPage);

                    //mise à jour le compteur
                    versionsCountLabel.setText(versions.size() + " version(s)");

                    // rafraîchir les données depuis le serveur
                    hydrateThenRefresh();

                    //notifier le parent (MainController) pour mettre à jour le quota
                    if(onVersionUploaded != null){
                        onVersionUploaded.run();
                    }

                    // réactiver les boutons
                    replaceButton.setDisable(false);
                    copyChecksumButton.setDisable(false);
                    openLocalFolderButton.setDisable(false);
                    downloadVersionButton.setDisable(false);

                    //rebind après modif
                    rebinButtons();

                    UIDialogs.showInfo("Suppression réussie",
                            null,
                            "La version " + version.getVersion() + " de \"" +
                            file.getName() + "\" a été supprimée."
                    );
                });
                uploadStatusLabel.setText("");
            } catch (Exception e) {
                Platform.runLater(() -> {

                    //masquer la progression
                    setProgressVisible(false);

                    // réactiver les boutons
                    replaceButton.setDisable(false);
                    copyChecksumButton.setDisable(false);
                    openLocalFolderButton.setDisable(false);
                    downloadVersionButton.setDisable(false);

                    //rebind après modif
                    rebinButtons();

                    //affichage message d'erreur
                    String errorMessage = e.getMessage();
                    if(errorMessage != null && errorMessage.contains("version active")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Impossible de supprimer la version active du fichier"
                        );
                    }else if (errorMessage != null && errorMessage.contains("dernière version")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Impossible de supprimer la dernière version du fichier"
                        );
                    }else{
                        UIDialogs.showError("Erreur de suppression",
                                null,
                                "Erreur: " + (errorMessage != null ? errorMessage : "Erreur inconnu.")
                        );
                    }
                });
            }
        }).start();
    }




    //=============== Action UI ==============
    @FXML
    /**
     * Permet de sélectionner un fichier local et d’uploader une nouvelle version avec suivi de progression =>ok
     */
    private void onReplace(){

        if(uploadService != null && uploadService.isRunning()){
            UIDialogs.showError("Erreur", null,"Un upload est deja en cours");
            return;
        }

        if(apiClient == null || file == null) {
            String details = (apiClient == null ? "apiClient " : "") + (file == null ? "file " : "") + "est null";
            System.err.println("FileDetailsController - ERREUR: " + details);
            UIDialogs.showError("Erreur", null,"API ou fichier non initialisé (" + details + ")");
            return;
        }

        String currentFileName = file.getName();
        String currentExtension = FileUtils.getFileExtension(currentFileName);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir un fichier pour remplacer");
        chooser.setInitialFileName(file.getName());

        // filtrer par extension du fichier actuel
        if(currentExtension != null && !currentExtension.isEmpty()){
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Fichiers " + currentExtension.toUpperCase(), "*." + currentExtension));
        }

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Tous les fichiers (*.*)", "*.*")
        );

        File selected = chooser.showOpenDialog(stage);
        if(selected == null) return;

        //vérif si l'extension du selected correspond
        String selectedExtension = FileUtils.getFileExtension(selected.getName());
        
        // Si currentExtension est vide, on laisse passer (on considère que c'est ok)
        // ou si elles correspondent.
        boolean isMatch = currentExtension.isEmpty() || currentExtension.equalsIgnoreCase(selectedExtension);
        
        if(!isMatch){
            UIDialogs.showError("Type de fichier incorrect", null,
                    "Le fichier de remplacement doit avoir le même extension. \n" +
                            "Attendu : " + (currentExtension.isEmpty() ? "n'importe quelle extension" : "." + currentExtension) + "\n" +
                            "Reçu : ." + selectedExtension
                    );
            return;
        }

        hideError();
        setProgressVisible(true);
        uploadStatusLabel.setText("Preparation upload...");
        uploadProgressBar.setProgress(0);

        uploadService =  new UploadVersionService(apiClient, file.getId(), selected);

        uploadProgressBar.progressProperty().bind(uploadService.progressProperty());
        uploadStatusLabel.textProperty().bind(uploadService.messageProperty());

        // désactiver le bouton pendant l'upload
        replaceButton.disableProperty().bind(uploadService.runningProperty());

        uploadService.setOnSucceeded(event -> {

            //unbind
            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);

            setProgressVisible(false);

            //refresh version et header
            hydrateThenRefresh();

            //reload liste fichiers et quota ... => callback vers MainController ???????
            if(onVersionUploaded != null) {
                onVersionUploaded.run();
            }
        });

        uploadService.setOnFailed(event -> {
            Throwable ex = uploadService.getException();

            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);

            //Masquer la progression et nettoyer l'état
            setProgressVisible(false);

            UIDialogs.showError("Upload echoue: ", null, (ex != null ? ex.getMessage() : "Erreur inconnu"));
        });

        uploadService.start();
    }


    @FXML
    /**
     * Copie le checksum complet de la version sélectionnée dans le presse-papiers
     */
    private void onCopyChecksum(){

        VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();

        if(sel == null) return;

        String checksum = sel.getChecksum();
        if(checksum == null || checksum.isBlank()){
            UIDialogs.showError("Erreur", null, "Checksum indisponible");
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(checksum.trim());
        Clipboard.getSystemClipboard().setContent(content);

        //feedback
        UIDialogs.showInfo("Checksum copie", null, "Le checksum a été copie dans le presse-papiers.");
    }

    @FXML
    /**
     * Ouvre le dossier local contenant la version téléchargée sélectionnée.
     */
    private void onOpenLocalFolder(){
        VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();

        if(sel == null) return;

        if(apiClient == null || file == null) {
            String details = (apiClient == null ? "apiClient " : "") + (file == null ? "file " : "") + "est null";
            System.err.println("FileDetailsController - ERREUR: " + details);
            UIDialogs.showError("Erreur", null,"API ou fichier non initialisé (" + details + ")");
            return;
        }

        //chercher le chemin du fichier téléchargé associé au couple (fileId, selId)
        Path path = downloadedPaths.get(key(file.getId(), sel.getVersion()));
        if(path == null){
            UIDialogs.showError("Erreur", null, "Cette version n'a pas encore ete telecharge sur ce PC");
            return;
        }

        try {
            //si "path" est fichier => ouvrir son dossier parent
            // si "path" est un dossier => ouvrir ce dossier
            Path dir = Files.isDirectory(path) ? path : path.getParent();

            //vérifier si le dossier existe => p.ex fichier supprimé, disque externe débranché ...
            if (dir == null || !Files.exists(dir)) {
                UIDialogs.showError("Erreur", null, "Dossier local introuvable");
                return;
            }

            if(Desktop.isDesktopSupported()){

                //ouvrir le dossier dans l’explorateur de fichiers du système p.exFinder/Explorer
                Desktop.getDesktop().open(dir.toFile());
            }else{

                //p.ex. VM, certaines plateformes
                UIDialogs.showError("Erreur", null, "Ouverture du dossier non supportée sur cette plateforme.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir le dossier: " + e.getMessage());
        }
    }


    @FXML
    /**
     * Télécharge une version spécifique du fichier avec affichage de la progression =>ok
     */
    private void onDownloadVersion(){
        VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();

        if(sel == null || file == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer la version " + sel.getVersion());
        //chooser.setInitialFileName(file.getName());
        //chooser.setInitialFileName(file.getName() + "_v" + sel.getVersion());
        chooser.setInitialFileName("v" + sel.getVersion() + "_" + file.getName());

        //configuration automatique les filtres
        FileUtils.configureFileChooserFilter(chooser, file.getName());

        File target = chooser.showSaveDialog(stage);
        if(target == null) return;

        hideError();
        setProgressVisible(true);
        uploadStatusLabel.setText("Telechargement");
        uploadProgressBar.setProgress(0);

        //utilisation d'un Service pour télécharger en tâche de fond
        Service<Void> downloadService = new Service<>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<>(){

                    @Override
                    protected Void call() throws Exception {
                        updateMessage("Downloading version " + sel.getVersion() + "...");

                        apiClient.downloadFileVersionTo(file.getId(), sel.getVersion(), target, (done, total) -> {
                            if(total > 0) {
                                updateProgress(done, total);
                            }
                        });
                        updateMessage("Download complete");
                        updateProgress(1, 1);
                        return null;
                    }
                };
            }
        };

        uploadProgressBar.progressProperty().bind(downloadService.progressProperty());
        uploadStatusLabel.textProperty().bind(downloadService.messageProperty());

        downloadVersionButton.disableProperty().bind(downloadService.runningProperty());
        replaceButton.disableProperty().bind(downloadService.runningProperty());

        downloadService.setOnSucceeded(event -> {
            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            downloadVersionButton.disableProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);
            downloadVersionButton.setDisable(false);


            setProgressVisible(false);

            //enregistrer le chemin local pour activer "ouvrir dossier local"
            downloadedPaths.put(key(file.getId(), sel.getVersion()), target.toPath());

            // optionnel (UI) ?????????
            versionsTable.refresh();

            // afficher le chemin (sur FX thread)
            Platform.runLater(() ->
                    UIDialogs.showInfo("Téléchargement", null, "Version téléchargée :\n" + target.getAbsolutePath())
            );
        });

        downloadService.setOnFailed(event -> {
            Throwable ex = downloadService.getException();

            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            downloadVersionButton.disableProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);
            downloadVersionButton.setDisable(false);

            setProgressVisible(false);

            UIDialogs.showError("Téléchargement échoué: ", null,  (ex != null ? ex.getMessage() : "Erreur inconnu"));
        });

        downloadService.start();
    }




    //================ Helper UI ===============

    /**
     * Affiche ou masque la zone de progression et réinitialise l’état d’erreur si nécessaire
     * @param visible
     */
//    private void setProgressVisible(boolean visible){
//        if (progressBox == null) {
//            System.err.println("ERREUR: progressBox est NULL !");
//            return;
//        }
//
//        progressBox.setVisible(visible);
//        progressBox.setManaged(visible);
//        if(!visible){
//            hideError();
//        }
//    }

    private void setProgressVisible(boolean visible) {
        System.out.println("DEBUG: setProgressVisible(" + visible + ") appelé");
        System.out.println("DEBUG: progressBox = " + progressBox);

        if (progressBox == null) {
            System.err.println("ERREUR: progressBox est NULL !");
            return;
        }

        System.out.println("DEBUG: Avant - visible=" + progressBox.isVisible() + ", managed=" + progressBox.isManaged());

        progressBox.setVisible(visible);
        progressBox.setManaged(visible);

        System.out.println("DEBUG: Après - visible=" + progressBox.isVisible() + ", managed=" + progressBox.isManaged());

        if (!visible) {
            hideError();
        }
    }

    /**
     * Masque le message d’erreur affiché dans l’interface.
     */
    private void hideError(){
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * énère une clé unique pour associer un fichier et une version à un chemin local téléchargé
     * @param fileId
     * @param versionNumber
     * @return
     */
    private static String key(long fileId, int versionNumber){
        return fileId + ":v" + versionNumber;
    }

    //encapsulation Service/Task lié à cette écran

    /**
     * Service JavaFX encapsulant l’upload d’une nouvelle version en tâche de fond
     */
    private static class UploadVersionService extends Service<Void> {

        private final ApiClient api;
        private final int fileId;
        private final File selectedFile;

        UploadVersionService(ApiClient api, int fileId, File selectedFile) {
            this.api = api;
            this.fileId = fileId;
            this.selectedFile = selectedFile;
        }

        @Override
        /**
         * Crée la tâche d’upload avec suivi de progression et mise à jour des messages d’état
         */
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Upload en cours...");
                    updateProgress(-1, 1); // indeterminate au début

                    api.uploadNewVersion(fileId, selectedFile, (sent, total) -> {
                        if (isCancelled()) return;
                        if (total > 0) updateProgress(sent, total);
                        else updateProgress(-1, 1);
                    });

                    updateProgress(1, 1);
                    updateMessage("Upload terminé");
                    return null;
                }
            };
        }
    }


}

"@; $dir = Split-Path "src\main\java\com\coffrefort\client\controllers\FileDetailsController.java"; if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }; [System.IO.File]::WriteAllText("src\main\java\com\coffrefort\client\controllers\FileDetailsController.java", $content, [System.Text.Encoding]::ASCII)
$env:JAVA_HOME = "C:\Users\M0mjax\.jdks\ms-17.0.18"; & "C:\Users\M0mjax\AppData\Local\Programs\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile javafx:run
