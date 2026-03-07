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
            String fileName = unescapeJsonString(extractJsonField(o, "file_name"));
            item.setResource(fileName != null ? fileName : "Fichier inconnu");
            System.out.println("  - file_name: " + fileName);

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

            // fallback => au cas ou un jour changer le champ côté backend
            name = unescapeJsonString(extractJsonField(json, "name"));
        }

        if (name == null) name = "";

        // size
        String sizeStr = extractJsonNumberField(json, "size");
        long size = (sizeStr != null && !sizeStr.isEmpty()) ? Long.parseLong(sizeStr) : 0L;

        // created_at
        String createdAt = unescapeJsonString(extractJsonField(json, "created_at"));
        if (createdAt == null) createdAt = "";

        // created_at
        String updatedAt = unescapeJsonString(extractJsonField(json, "updated_at"));
        if (updatedAt == null) updatedAt = "";

        if (id <= 0) {
            // Si l’API renvoie une erreur HTML/texte => éviter de créer un objet “fantôme”
            throw new IllegalArgumentException("parseFileEntry: champ 'id' invalide ou manquant. JSON=" + json);
        }

        return FileEntry.of(id, name, size, createdAt, updatedAt);
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
//            block = block.replaceAll("^\\[?\\{?", "").replaceAll("\\}?\\]?$", "");;
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

            int id = (idStr != null) ? Integer.parseInt(idStr) : 0;
            long size = (sizeStr != null )? Long.parseLong(sizeStr) : 0L;

            if (name == null) {
                // sécurité : si jamais ton API change de champ un jour
                name = JsonUtils.extractJsonField(objet, "name");
            }

            result.add(FileEntry.of(id, name, size, date, updatedDate));
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
