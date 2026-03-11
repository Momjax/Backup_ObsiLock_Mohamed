package com.coffrefort.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileEntry {

    private int id; 

    @JsonProperty("filename")
    private String name;

    private long size;

    @JsonProperty("uploaded_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public FileEntry(){}

    public FileEntry(int id, String name, long size, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FileEntry of(int id, String name, long size, String createdAt, String updatedAt) {
        return new FileEntry(id, name, size, createdAt, updatedAt);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getFormattedSize() {
        long bytes = size;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        double val = bytes / Math.pow(1024, exp);
        return String.format("%.1f %sB", val, unit);
    }

    public String getUpdatedAtFormatted() {
        return createdAt != null ? createdAt : "";
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
