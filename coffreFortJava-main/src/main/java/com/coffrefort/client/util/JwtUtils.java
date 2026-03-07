package com.coffrefort.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Décode la partie payload d’un JWT ( encodé en Base64URL) et la retourne en JSON sous forme de String
     * @param jwt
     * @return
     */
    public static String decodePayload(String jwt){
        if(jwt == null || jwt.isEmpty()) return null;

        String[] parts = jwt.split("\\.");

        //JWT a 3 parties: header.payload.signature !!!
        if(parts.length != 3) return null;

        String payload = parts[1]; // 2ième partie =>i= 1

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] decoded = decoder.decode(payload);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Extrait le champ "email" du payload du JWT décodé
     * @param jwt
     * @return
     */
    public static String extractEmail(String jwt){
        String json = decodePayload(jwt);
        if(json == null) return null;

        System.out.println("JWT Payload décodé: " + json); // Debug
        return JsonUtils.extractJsonField(json, "email");
    }


    /**
     * Extrait le champ numérique "user_id" du payload du JWT décodé
     * @param jwt
     * @return
     */
    public static String extractUserID(String jwt){
        String json = decodePayload(jwt);
        if(json == null) return null;

        System.out.println("JWT Payload décodé: " + json); // Debug
        return JsonUtils.extractJsonNumberField(json, "user_id");
    }

    /**
     * Extrait le champ is_admin du payload du JWT décodé (format: 0, 1, false, true)
     * @param jwt
     * @return
     */
    public static Boolean extractIsAdmin(String jwt){
        String json = decodePayload(jwt);
        if(json == null) return false;

        System.out.println("JWT Payload décodé: " + json); // Debug
        String isAdminStr = JsonUtils.extractJsonFieldAny(json, "is_admin");

        System.out.println("DEBUG - is_admin extrait du JWT: '" + isAdminStr + "'");

        if(isAdminStr != null){
            isAdminStr = isAdminStr.trim();
            return "true".equalsIgnoreCase(isAdminStr) || "1".equals(isAdminStr);
        }

        return false;
    }

    /**
     * vérif si le token JWT est expiré
     * @param token
     * @return
     */
    public static boolean isTokenExpired(String token){
        if(token == null || token.isEmpty()){
            return true;
        }

        try{
            String[] parts = token.split("\\.");
            if(parts.length != 3){
                return true;
            }

            //décoder le payload => partie 2
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payload);

            //récup expiration en secondes
            if(node.has("exp")){
                long exp = node.get("exp").asLong();
                long now = System.currentTimeMillis() / 1000; //=>convertir en secondes

                return now >= exp;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification du token : " + e.getMessage());

            //erreur => considérer comme true!!!
            return true;
        }
    }

    public static long getTimeUntilExpiration(String token){
        if(token == null || token.isEmpty()){
            return 0;
        }

        try {
            String[] parts = token.split("\\.");
            if(parts.length != 3){
                return 0;
            }

            //à voir
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            //String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payload);

            if(node.has("exp")){
                long exp = node.get("exp").asLong();
                long now = System.currentTimeMillis() / 1000; //=>convertir en secondes

                return Math.max(0, exp - now);
            }
            return 0;
        }catch (Exception e){
            System.out.println("Erreur lors du calcul du temps restant : " + e.getMessage());
            return 0;
        }
    }


    /**
     * Empêche l’instanciation de la classe utilitaire JwtUtils
     */
    private JwtUtils(){};
}














