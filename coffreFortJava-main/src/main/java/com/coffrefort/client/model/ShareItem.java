package com.coffrefort.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShareItem {

    //propriétés
    private int id;
    private int targetId;
    private String kind;        //file ou folder
    private String resource;        //nom du fichier/dossier
    private String label;
    private String token;
    private String expiresAt;
    private String createdAt;
    private String url;
    private Integer remainingUses;
    private Integer maxUses;
    private String folderName;
    private String fileName;

    // //Jackson mappe "is_revoked"(backend) en "revoked"(Java)
    @JsonProperty("is_revoked")
    private boolean revoked;
    private boolean allowFixedVersion;



    //méthodes
    public ShareItem() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getTargetId() {return  targetId;}
    public void setTargetId(int targetId) {this.targetId = targetId;}

    public String getKind() {return kind;}
    public void setKind(String kind) {this.kind = kind;}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getResource() {
        if(folderName != null && !folderName.isBlank()){
            return folderName;
        }
        if(fileName != null && !fileName.isBlank()){
            return fileName;
        }
        return resource != null ? resource : "Ressource inconnue";
    }
    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getToken() {return token;}
    public void setToken(String token) {this.token = token;}

    public String getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCreatedAt() {return createdAt;}
    public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}

    public Integer getRemainingUses() {
        return remainingUses;
    }
    public void setRemainingUses(Integer remainingUses) {
        this.remainingUses = remainingUses;
    }

    public Integer getMaxUses() {return maxUses;}
    public void setMaxUses(Integer maxUses) {this.maxUses = maxUses;}

    public boolean isRevoked() {
        return revoked;
    }
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isAllowFixedVersion() {return allowFixedVersion;}
    public void setAllowFixedVersion(boolean allowFixedVersion) {this.allowFixedVersion = allowFixedVersion;}

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return le statut de partage
     */
    public String getStatus(){
        if(revoked){
            return "Révoqué";
        }

        //vérifier si expiré
        if(expiresAt != null && !expiresAt.isEmpty()){
            try{
                LocalDateTime expires = LocalDateTime.parse(expiresAt.replace(" ", "T"));
                if(expires.isBefore(LocalDateTime.now())){
                    return "Expiré";
                }
            }catch(Exception e){
                //ignorer erreur de parsing
            }
        }

        //vérifier si quota attent
        if(remainingUses != null && remainingUses <= 0){
            return "Quota atteint";
        }

        return "Actif";
    }

    /**
     *
     * @return la date d'expiration formaté
     */
    public String getFormattedExpiresAt (){
        if(expiresAt == null || expiresAt.isEmpty()){
            return "Jamais";
        }

        try{
            LocalDateTime dateTime = LocalDateTime.parse(expiresAt.replace(" ", "T"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return dateTime.format(formatter);

        } catch (Exception e) {
            return expiresAt;
        }
    }

    /**
     * vérifie si le partage est expiré
     * @return
     */
    public boolean isExpired(){
        if(expiresAt == null || expiresAt.isEmpty()){
            return false;
        }

        try{
            LocalDateTime expires = LocalDateTime.parse(expiresAt.replace(" ", "T"));
            return expires.isBefore(LocalDateTime.now());
        }catch (Exception e){
            return false;
        }
    }

    /**
     * nbre de jours avant expiration  => si negatif => expiré
     * @return
     */
    public long getDaysUntilExpiration(){
        if(expiresAt == null || expiresAt.isEmpty()){
            return Long.MAX_VALUE; //=> jamais
        }

        try{
            LocalDateTime expires = LocalDateTime.parse(expiresAt.replace(" ", "T"));
            LocalDateTime now = LocalDateTime.now();
            return  java.time.Duration.between(expires, now).toDays();
        }catch (Exception e){
            return 0;
        }
    }

    /**
     *
     * @return le texte formatté pour 'Restant"
     */
    public String getRemainingText(){
        if(remainingUses == null && maxUses == null ){
            return "∞";
        }

        if(maxUses != null && maxUses > 0){
            int remaining = remainingUses != null ? remainingUses : 0;
            return remaining + " / " + maxUses;
        }

        return remainingUses != null ? remainingUses.toString() : "0";
    }

    @Override
    public String toString(){
        return "ShareItem{" +
                "id = " + id +
                ", kind = " + kind + '\'' +
                ", resource = " + resource + '\'' +
                ", label = " + label + '\'' +
                ", status + " + getStatus() + '\'' +
                '}';
    }





}
