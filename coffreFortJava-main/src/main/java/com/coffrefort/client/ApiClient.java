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
        this("http://api.obsilock.iris.a3n.fr:8080");
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
