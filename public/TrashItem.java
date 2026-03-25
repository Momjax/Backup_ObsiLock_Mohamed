package com.coffrefort.client.model;

public class TrashItem {
    private int id;
    private String name;
    private long size;
    private String updatedAt;
    private boolean isFolder;

    public TrashItem(int id, String name, long size, String updatedAt, boolean isFolder) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.updatedAt = updatedAt;
        this.isFolder = isFolder;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getSize() { return size; }
    public String getUpdatedAt() { return updatedAt; }
    public boolean isFolder() { return isFolder; }
    
    public String getFormattedSize() {
        if (isFolder) return "- (Dossier)";
        long bytes = size;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        double val = bytes / Math.pow(1024, exp);
        return String.format("%.1f %sB", val, unit);
    }

    public String getType() {
        return isFolder ? "📁 Dossier" : "📄 Fichier";
    }
}
