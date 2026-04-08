package com.coffrefort.client.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Unitaires de JwtUtils")
public class JwtUtilsTest {

    private String generateFakeToken(String payloadJson) {
        String base64Header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String base64Payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
        return base64Header + "." + base64Payload + ".fakeSignature";
    }

    @Test
    @DisplayName("Extraction d'un user_id valide depuis le payload")
    void testExtractUserID_Valid() {
        // Arrange
        String mockToken = generateFakeToken("{\"user_id\": 42, \"email\": \"admin@test.com\"}");

        // Act
        String userId = JwtUtils.extractUserID(mockToken);

        // Assert
        assertThat(userId).isEqualTo("42");
    }

    @Test
    @DisplayName("Extraction de la donnée is_admin booléenne du payload")
    void testExtractIsAdmin_ValidBoolean() {
        // Arrange
        String mockTokenAdmin = generateFakeToken("{\"user_id\": 1, \"is_admin\": true}");
        String mockTokenUser = generateFakeToken("{\"user_id\": 2, \"is_admin\": false}");

        // Act & Assert
        assertThat(JwtUtils.extractIsAdmin(mockTokenAdmin))
                .as("Le token contenant is_admin: true doit retourner vrai")
                .isTrue();
                
        assertThat(JwtUtils.extractIsAdmin(mockTokenUser))
                .as("Le token contenant is_admin: false doit retourner faux")
                .isFalse();
    }

    @Test
    @DisplayName("Gère le token invalide en retournant false/null selon le champ demandé")
    void testExtractData_InvalidToken() {
        // Arrange
        String invalidToken = "pasDuTout.UnJwt.Valide";

        // Act
        String userId = JwtUtils.extractUserID(invalidToken);
        Boolean isAdmin = JwtUtils.extractIsAdmin(invalidToken);

        // Assert
        assertThat(userId).isNull();
        assertThat(isAdmin).isFalse();
    }
}
