package com.coffrefort.client.util;

import com.coffrefort.client.ApiClient;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

public class SessionManager {

    private static SessionManager instance;
    private Timer sessionTimer;
    private Runnable onSessionExpired;
    private ApiClient apiClient;

    public SessionManager(){}

    public static SessionManager getInstance() {
        if(instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setOnSessionExpired(Runnable onSessionExpired) {
        this.onSessionExpired = onSessionExpired;
    }

    /**
     * démarrer la vérif de token => toutes les minutes
     */
    public void startSessionMonitoring(){

        // arrêter l'ancien timer s'il existe
        stopSessionMonitoring();

        sessionTimer = new Timer(true); //=>deamon thread
        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkTokenValidity();
            }
            // 1.check après 1 minute => puis tous les minutes
        }, 60000, 60000);

        System.out.println("SessionManager - Surveillance de session démarrée");
    }

    /**
     * arrête la vérif périodique
     */
    public void stopSessionMonitoring(){
        if(sessionTimer != null) {
            sessionTimer.cancel();
            sessionTimer = null;
            System.out.println("SessionManager - Surveillance de session arrêtée");
        }
    }

    /**
     * vérif la validité du token
     */
    public void checkTokenValidity(){
        if(apiClient == null){
            return;
        }

        String token = apiClient.getAuthToken();
        if(token == null || token.isEmpty()){
            return;
        }

        //vérif si le token est expiré via JwtUtils
        if(JwtUtils.isTokenExpired(token)){
            System.out.println("SessionManager - Token expiré détecté !");
            handleSessionExpiration();
        }
    }

    public void handleSessionExpiration(){
        stopSessionMonitoring();

        Platform.runLater(() ->{
            if(onSessionExpired != null){

                //déconnecion automatique
                if(apiClient != null){
                    apiClient.logout();
                }

                //afficher un message au user
                UIDialogs.showError(
                        "Session expirée",
                        "Votre session a expiré",
                        "Veuillez vous reconnecter."
                );

                //rediriger vers UI connexion
                onSessionExpired.run();
            }
        });
    }
}
