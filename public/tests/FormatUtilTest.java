package com.coffrefort.client;

import com.coffrefort.client.util.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatUtilTest {

    @ParameterizedTest(name = "Taille {0} octets formatée donne '{1}'")
    @CsvSource({
            "1536, 1,5 KB",
            "2411724, 2,3 MB",
            "1073741824, 1,00 GB",
            "0, 0 B",
            "999, 999 B",
            "1048576, 1,0 MB"
    })
    @DisplayName("Formatage de tailles dynamiques - @CsvSource")
    void testFormatFileSize_Parametrized(long bytes, String expectedOutput) {
        // AAA Strategy

        // Arrange (Given) est géré par les paramètres
        
        // Act (When)
        String formattedSize = FileUtils.formatSize(bytes);
        
        // Assert (Then) avec AssertJ
        assertThat(formattedSize).isEqualTo(expectedOutput);
    }
}