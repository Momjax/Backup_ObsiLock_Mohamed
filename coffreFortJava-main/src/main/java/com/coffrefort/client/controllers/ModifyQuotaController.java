package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.controllers.MainController;
import com.coffrefort.client.model.UserQuota;
import com.coffrefort.client.util.FileUtils;
import com.coffrefort.client.util.UIDialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ModifyQuotaController {
    @FXML private Label usernameLabel;
    @FXML private Label currentQuotaLabel;
    @FXML private Label usedSpaceLabel;
    @FXML private TextField quotaField;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private ApiClient apiClient;
    private Stage dialogStage;
    private Runnable onSuccess;
    private UserQuota user;

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUser(UserQuota user){
        this.user = user;

        usernameLabel.setText(user.getEmail());
        currentQuotaLabel.setText(user.getQuotaMax());
        usedSpaceLabel.setText(user.getQuotaUsed());

        //préremplir avec le quota actuel
        long quotaBytes = user.getMax();
        double quotaGB = quotaBytes / (1024.0 * 1024.0 * 1024.0);
        quotaField.setText(String.format("%.1f", quotaGB));
    }

    public void setOnSuccess(Runnable callback){
        this.onSuccess = callback;
    }

    @FXML
    private void setQuota1GB(){
        quotaField.setText("1");
    }

    @FXML
    private void setQuota5GB(){
        quotaField.setText("5");
    }

    @FXML
    private void setQuota10GB(){
        quotaField.setText("10");
    }

    @FXML
    private void setQuota50GB(){
        quotaField.setText("50");
    }

    @FXML
    private void setQuota100GB(){
        quotaField.setText("100");
    }

    @FXML
    private void handleCancel(){
        if(dialogStage != null){
            dialogStage.close();
        }
    }

    @FXML
    private void handleConfirm(){
        String quotaText = quotaField.getText();
        if(quotaText == null || quotaText.trim().isEmpty()){
            showError("Veuillez saisir un quota");
            return;
        }

        double quotaGB;
        try{
            quotaGB = Double.parseDouble(quotaText.trim());
        }catch(NumberFormatException e){
            showError("Format invalide. Veuillez saisir un nombre (ex.10)");
            return;
        }

        if ((quotaGB <= 0)) {
            showError("Le quota doit être supérieur à 0");
            return;
        }

        //convertir GB en bytes
        long quotaBytes = (long)(quotaGB * 1024.0 * 1024.0 *  1024.0);

        //vérif si le nouveau quota >= espace utilisé
        if(quotaBytes < user.getUsed()){
            showError("Le nouveau quota (" + FileUtils.formatSize(quotaBytes) +")\n" +
                    "ne peut pas être inférieur à l'espace utilisé (" + user.getQuotaUsed() +")"
            );
            return;
        }


        hideError();

        confirmButton.setDisable(true);
        cancelButton.setDisable(true);

        new Thread(() -> {
            try{
                apiClient.updateUserQuota(user.getId(), quotaBytes);

                Platform.runLater(() -> {
                    UIDialogs.showInfo(
                            "Succès",
                            null,
                            "Le quota de " + user.getEmail() + " a été modifié.\n" +
                                    "Nouveau quota : " + FileUtils.formatSize(quotaBytes)
                    );

                    if(onSuccess != null){
                        onSuccess.run();
                    }


                    if(dialogStage != null){
                        dialogStage.close();
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
                Platform.runLater(() -> {
                    confirmButton.setDisable(false);
                    cancelButton.setDisable(false);

                    showError("Erreur: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

}
