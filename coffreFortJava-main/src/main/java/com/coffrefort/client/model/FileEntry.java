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

    @JsonProperty("current_version")
    private int version;

    public FileEntry(){}

    public FileEntry(int id, String name, long size, String createdAt, String updatedAt, int version) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static FileEntry of(int id, String name, long size, String createdAt, String updatedAt, int version) {
        return new FileEntry(id, name, size, createdAt, updatedAt, version);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
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
        return updatedAt != null ? updatedAt : (createdAt != null ? createdAt : "");
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
