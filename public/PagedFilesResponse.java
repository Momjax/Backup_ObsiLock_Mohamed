package com.coffrefort.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedFilesResponse {

    @JsonProperty("data")
    private List<FileEntry> files;
    
    // Si l'API renvoie null, on initialise à vide pour éviter le NullPointerException
    public List<FileEntry> getFiles() { 
        return files != null ? files : new ArrayList<>(); 
    }
    public void setFiles(List<FileEntry> files) { this.files = files; }

    // Gérer l'objet pagination au lieu des champs plats
    @JsonProperty("pagination")
    private void setPaginationFields(Map<String,Object> pagination) {
        if (pagination != null) {
            if (pagination.containsKey("total")) {
                this.total = (Integer) pagination.get("total");
            }
            if (pagination.containsKey("per_page")) {
                this.limit = (Integer) pagination.get("per_page");
            }
            if (pagination.containsKey("page")) {
                int page = (Integer) pagination.get("page");
                this.offset = (page - 1) * this.limit;
            }
        }
    }

    private int total;
    private int offset;
    private int limit;

    public int getTotal() { return total; }
    public int getOffset() { return offset; }
    public int getLimit() { return limit; }

    public void setTotal(int total) { this.total = total; }
    public void setOffset(int offset) { this.offset = offset; }
    public void setLimit(int limit) { this.limit = limit; }
}
