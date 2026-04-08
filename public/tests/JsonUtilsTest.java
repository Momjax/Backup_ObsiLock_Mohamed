package com.coffrefort.client.util;

import com.coffrefort.client.model.ShareItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Tests Unitaires de JsonUtils")
public class JsonUtilsTest {

    @Nested
    @DisplayName("Extraction des champs simples JSON")
    class ExtractFields {

        @Test
        @DisplayName("Extraction d'un champ texte standard")
        void testExtractJsonField_ValidString() {
            // Arrange
            String json = "{\"id\": 1, \"name\": \"ObsiLock\", \"role\": \"admin\"}";

            // Act
            String name = JsonUtils.extractJsonField(json, "name");
            String role = JsonUtils.extractJsonField(json, "role");

            // Assert
            assertAll(
                    () -> assertThat(name).isEqualTo("ObsiLock"),
                    () -> assertThat(role).isEqualTo("admin")
            );
        }

        @Test
        @DisplayName("Extraction d'un champ numérique (number/Integer)")
        void testExtractJsonNumberField_ValidNumber() {
            // Arrange
            String json = "{\"id\": 42, \"quota_used\": -100}";

            // Act
            String id = JsonUtils.extractJsonNumberField(json, "id");
            String quota = JsonUtils.extractJsonNumberField(json, "quota_used");

            // Assert
            assertAll(
                    () -> assertThat(id).isEqualTo("42"),
                    () -> assertThat(quota).isEqualTo("-100")
            );
        }

        @Test
        @DisplayName("Gère le Json null ou champ manquant sans Exception")
        void testExtractJsonField_NullSafe() {
            // Act & Assert
            assertNull(JsonUtils.extractJsonField(null, "name"));
            assertNull(JsonUtils.extractJsonField("{\"id\":1}", "name"), "Devrait retourner null si le champ est manquant");
        }
    }

    @Nested
    @DisplayName("Extraction complexes et tableaux")
    class ExtractComplex {

        @Test
        @DisplayName("Extraction d'un tableau complet (JsonArray)")
        void testExtractJsonArrayField() {
            String json = "{\"data\": [{\"item\":1}, {\"item\":2}], \"success\": true}";

            String arrayContent = JsonUtils.extractJsonArrayField(json, "data");

            assertThat(arrayContent)
                    .isNotNull()
                    .startsWith("[")
                    .endsWith("]")
                    .contains("{\"item\":1}");
        }
    }

    @Nested
    @DisplayName("Échappement et Parsing d'objets métiers (ex: ShareItem)")
    class DomainModels {

        @ParameterizedTest(name = "Chaîne échappée: {0} => {1}")
        @CsvSource(value = {
                "Bonjour\\\\nObsiLock | Bonjour\\nObsiLock",
                "Mon\\\\/Fichier.txt  | Mon/Fichier.txt"
        }, delimiter = '|')
        @DisplayName("unescapeJsonString déséchappe correctement le code")
        void testUnescapeJsonString(String input, String expected) {
            String result = JsonUtils.unescapeJsonString(input);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Le parsing d'un partage de fichier (ShareItem) mappe bien les infos")
        void testParseShareItem_Success() {
            String jsonResponse = "{\"shares\": [" +
                    "{\"id\": 12, \"file_name\": \"secret.pdf\", \"label\": \"Mon Lien\", " +
                    "\"remaining_uses\": 5, \"url\": \"https://api.test/share/8f74a\", \"is_revoked\": 0}" +
                    "]}";

            List<ShareItem> shares = JsonUtils.parseShareItem(jsonResponse);

            assertThat(shares).hasSize(1);
            ShareItem item = shares.get(0);
            
            assertAll("Vérification détaillée de ShareItem",
                    () -> assertThat(item.getId()).isEqualTo(12),
                    () -> assertThat(item.getFileName()).isEqualTo("secret.pdf"),
                    () -> assertThat(item.getLabel()).isEqualTo("Mon Lien"),
                    () -> assertThat(item.getRemainingUses()).isEqualTo(5),
                    () -> assertThat(item.getUrl()).isEqualTo("https://api.test/share/8f74a"),
                    () -> assertThat(item.isRevoked()).isFalse()
            );
        }
    }
}
