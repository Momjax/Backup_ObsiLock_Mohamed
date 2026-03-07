package com.coffrefort.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Représente une version renvoyée par:
 * GET /files/{id}/versions  (items[])
 *
 * Payload backend:
 * {
 *   "id": 123,
 *   "version": 3,
 *   "size": 1048576,
 *   "created_at": "2025-12-28 13:20:10",
 *   "checksum": "..." (hex)
 * }
 */
public class VersionEntry {

    //propriétés
    private int id;
    private int version;
    private long size;

    @JsonProperty("created_at")
    private String createdAt;
    private String checksum;

    @JsonProperty("is_current")
    private Boolean isCurrent;

    //méthodes

    //constructeur par défaut pour Jackson
    public VersionEntry(){}

    public VersionEntry(int id, int version, long size, String createdAt, String checksum, Boolean isCurrent) {
        this.id = id;
        this.version = version;
        this.size = size;
        this.createdAt = createdAt;
        this.checksum = checksum;
        this.isCurrent = isCurrent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getVersion(){
        return version;
    }

    public void setVersion(int version){
        this.version = version;
    }

    public long getSize(){
        return size;
    }

    public void setSize(long size){
        this.size = size;
    }

    public String getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(String createdAt){
        this.createdAt = createdAt;
    }

    public String getChecksum(){
        return checksum;
    }

    public void setChecksum(String checksum){
        this.checksum = checksum;
    }

    public String getFormattedSize(){
        return getFormattedSize(size);
    }

    public boolean getIsCurrent(){
        return isCurrent;
    }


    /**
     * Convertit la date createdAt (format backend) en format lisible pour l’UI, ou renvoie la valeur brute si le parsing échoue
     * @return
     */
    public String getCreatedAtFormatted(){

        if(createdAt ==  null || createdAt.isBlank()) return "";

        // backend: "yyyy-MM-dd HH:mm:ss"
        try {
            DateTimeFormatter in = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime date =  LocalDateTime.parse(createdAt.trim(), in);
            DateTimeFormatter out = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return date.format(out);
        }catch(Exception e){

            //afficher  brut si parsing échoue
            return  createdAt;
        }
    }

    /**
     * @return une version courte du checksum (12 premiers caractères + "...") pour l’affichage dans l’interface
     */
    public String getChecksumShort(){
        if(checksum == null) return "";

        String c = checksum.trim();

        if(c.length() < 20) return c;

        return  c.substring(0, 20) + "..."; //=> renvoi les 12 premiers caractères
    }

    /**
     * Formate une taille en octets en unité lisible (B, KB, MB, GB, …) avec une décimale
     * @param size
     * @return ??? peut etre mettre la function de la FileUtils
     */
    private static String getFormattedSize(long size) {
        if (size < 1024) return size + " B";                //=> size "bytes"
        int exp = (int) (Math.log(size) / Math.log(1024));  // => exposant
        char unit = "KMGTPE".charAt(exp - 1);               //p.ex exp=1 -> "K"
        double val = size / Math.pow(1024, exp);            // Math.pow => val = 2048 / 1024 = 2.0 KB
        return String.format("%.1f %sB", val, unit);
    }

    @Override
    /**
     * Retourne une représentation texte de l’objet VersionEntry pour le debug/logging (avec checksum raccourci)
     */
    public String toString(){
        return "VersionEntry {" +
                "id = " + id + ", " +
                "version = " + version + ", " +
                "size = " + size + ", " +
                "createdAt = '" + createdAt + '\'' + ", " +
                "checksum = '" + (checksum != null ? getChecksumShort() : null) + '\'' +
                '}';

    }
}
