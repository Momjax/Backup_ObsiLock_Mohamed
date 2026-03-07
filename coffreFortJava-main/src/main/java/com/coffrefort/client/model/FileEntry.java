package com.coffrefort.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileEntry {

    //propriétés
    private int id; //=> pour pouvoir supprimer, renommer, télécharger

    //Jackson mappe "original_name"(backend) en "name"(Java)
    @JsonProperty("original_name")
    private String name;

    private long size;

    @JsonProperty("created_at")
    private String  createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    //méthodes

    //constructeur par défaut pour Jackson
    public FileEntry(){}

    //constructeur avec paramètres
    public FileEntry(int id, String name, long size, String  createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //méthode factory
    public static FileEntry of(int id, String name, long size, String  createdAt, String updatedAt) {
        return new FileEntry(id, name, size, createdAt, updatedAt);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getSize() { return size; }
    public String getCreatedAt() { return createdAt; }

    // === compatibilité avec MainView (ancien code) ===
    // MainView utilise getUpdatedAt(), donc on lui donne ce qu'il veut :
    public String getUpdatedAt() {
        return createdAt;
    }

    //pour la TableView => colonne taille

    /**
     * Formate une taille en octets en unité lisible (B, KB, MB, GB, …) avec une décimale
     * @return
     */
    public String getFormattedSize() {
        long bytes = size;
        if (bytes < 1024) return bytes + " B";                //=> size "bytes"
        int exp = (int) (Math.log(bytes) / Math.log(1024));  // => exposant
        char unit = "KMGTPE".charAt(exp - 1);               //p.ex exp=1 -> "K"
        double val = bytes / Math.pow(1024, exp);            // Math.pow => val = 2048 / 1024 = 2.0 KB
        return String.format("%.1f %sB", val, unit);
    }

    //Pour la TableView => colonne date
    public String getUpdatedAtFormatted() {
        return updatedAt != null ? updatedAt : "";
    }

    @Override
    public String toString(){
        return "FileEntry{" +
                "id = " + id +
                ", name = " + name + '\'' +
                ", size = " + size + '\'' +
                ", createdAt = " + createdAt + '\'' +
                ", updatedAt + " + updatedAt + '\'' +
                '}';
    }
}
