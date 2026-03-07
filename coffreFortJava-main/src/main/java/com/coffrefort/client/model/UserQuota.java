package com.coffrefort.client.model;

import com.coffrefort.client.util.FileUtils;
public class UserQuota {

    private int id;
    //private String username;
    private String email;
    private long used;
    private long max;
    private String role;

    public UserQuota(int id, String email, long used, long max, String role) {
        this.id = id;
        this.email = email;
        this.used = used;
        this.max = max;
        this.role = role;
    }

    //getters
    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public long getUsed() {
        return used;
    }

    public long getMax() {
        return max;
    }

    public String getRole() {
        return role;
    }

    //formatted value pour afficher dans TableView
    public String getQuotaUsed(){
        return FileUtils.formatSize(used);
    }

    public String getQuotaMax(){
        return FileUtils.formatSize(max);
    }

    public String getPercent(){
        System.out.println("getPercent() appelé pour " + email); //majd enlevé demain
        if(max == 0){
            return "0%";
        }
        double percent = (used * 100.0) / max;
        return String.format("%.1f", percent);
    }

}
