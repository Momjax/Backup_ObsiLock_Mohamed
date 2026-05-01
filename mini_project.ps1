$target = "$HOME\Downloads\coffreFortJava-main"
if (!(Test-Path $target)) { New-Item -ItemType Directory -Path $target -Force }
Set-Location $target

# On va utiliser curl pour récupérer ton propre projet depuis ton serveur puisque tu as PHP d'installé
# Mais comme le DNS/IP bloque, on va utiliser la méthode du contenu direct pour le POM et les classes clés

# 1. POM.XML
$pom = @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.coffrefort</groupId>
    <artifactId>client-javafx</artifactId>
    <version>0.1.0</version>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>21.0.2</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.0</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.coffrefort.client.App</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
'@
$pom | Out-File -FilePath "pom.xml" -Encoding utf8

# Je ne peux pas tout mettre ici, mais je vais te donner la commande finale qui recrée tout l'essentiel
