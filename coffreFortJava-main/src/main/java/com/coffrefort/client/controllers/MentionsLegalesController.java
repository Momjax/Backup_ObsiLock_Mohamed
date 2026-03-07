package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.UserQuota;
import com.coffrefort.client.util.FileUtils;
import com.coffrefort.client.util.UIDialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MentionsLegalesController {

    @FXML private Label versionLabel;
    @FXML private Button returnButton;
    @FXML private Button closeButton;

    private ApiClient apiClient;
    private Stage dialogStage;

    private Runnable onSuccess;
    private Runnable onGoToRegister;

    @FXML //=> il faut qu'il soit lier avec le .fxml!!
    private void initialize(){

    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void setOnGoToRegister(Runnable onGoToRegister) {
        this.onGoToRegister = onGoToRegister;
    }

    public void setVersionLabel(Label versionLabel) {
        this.versionLabel = versionLabel;
    }

    public Label getVersionLabel() {
        return versionLabel;
    }

    @FXML
    private void handleReturn() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/login2.fxml"));

            Scene scene = new Scene(loader.load());

            // Récupération du contrôleur
            LoginController controller = loader.getController();

            Stage stage = (Stage)returnButton.getScene().getWindow();

            stage.setTitle("CryptoVault - Connexion");
            stage.setResizable(false);
            stage.setScene(scene);

            controller.setDialogStage(stage);
            controller.setApiClient(apiClient);

            controller.setOnSuccess(onSuccess);
            controller.setOnGoToRegister(onGoToRegister);

            stage.setWidth(450);
            stage.setHeight(650);
            stage.centerOnScreen();

        }catch(Exception e){
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,
                    "Impossible de retourner à la page de connexion : " + e.getMessage());
        }
    }


    @FXML
    private void handleClose(){
        System.out.println("handleClose est appelé");
        if(dialogStage != null){
            System.out.println("dialogStage closed");
            dialogStage.close();
        }

    }
}
