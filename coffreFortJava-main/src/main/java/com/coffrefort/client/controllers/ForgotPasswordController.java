package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.util.UIDialogs;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Hyperlink mentionsLegales;

    private ApiClient apiClient =  new ApiClient();
    private Stage dialogStage;

    private Runnable onSuccess;
    private Runnable onGoToRegister;

    @FXML
    private void initialize() {

        //masquer le message par défaut
        hideMessage();

        //lier le lien à "mentions légales"
        mentionsLegales.setOnAction(event -> handleGoToMentionsLegalesFromForgot());
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


    @FXML
    private void handleSendResetLink(){
        String email = emailField.getText().trim();

        //validation
        if(email.isEmpty()){
            showError("Veuillez entrer votre adresse email.");
            return;
        }

        if(!isValidEmail(email)){
            showError("Adresse email invalide.");
            return;
        }

        //désactiver le btn pendant l'envoi
        sendButton.setDisable(true);
        hideMessage();

        new Thread(()->{
            try{
                //appel API => demande réinitialisation
                apiClient.requestPasswordReset(email);

                javafx.application.Platform.runLater(()->{
                    showSuccess("Un email de réinitialisation a été envoyé à " + email + ".");
                    emailField.clear();
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(()->{
                    showError("Erreur : " + e.getMessage());
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleBackToLogin(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/login2.fxml"));

            Scene scene = new Scene(loader.load());

            // Récupération du contrôleur
            LoginController controller = loader.getController();

            Stage stage = (Stage)backButton.getScene().getWindow();

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

    private void handleGoToMentionsLegalesFromForgot(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/mentionsLegales.fxml"));

            Scene scene = new Scene(loader.load());

            // Récupération du contrôleur
            MentionsLegalesController controller = loader.getController();
            Stage stage = (Stage)mentionsLegales.getScene().getWindow();
            stage.setTitle("CryptoVault - Mentions Légales");

            stage.setResizable(false);
            stage.setScene(scene);

            controller.setDialogStage(stage);
            controller.setApiClient(apiClient);

            //Transmettre les callbacks
            controller.setOnSuccess(onSuccess);
            controller.setOnGoToRegister(onGoToRegister);

            stage.setHeight(700);
            stage.setWidth(750);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,
                    "Impossible d'ouvrir la page de mentions légales : " + e.getMessage());
        }
    }


    @FXML
    private void handleContactSupport(){
        //ouvrir une page de contact ou un email
        UIDialogs.showInfo("Support", null, "Contactez-nous: support@cryptovault.com");
    }

    private void showError(String message){
        messageLabel.setText(message);
        messageLabel.setStyle(
            "-fx-background-color: #ffe5e5; " +
            "-fx-text-fill: #b00000; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4; "
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String message){
        messageLabel.setText(message);
        messageLabel.setStyle(
            "-fx-background-color: #e5ffe5; " +
            "-fx-text-fill: #006b00; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4; "
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage(){
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }





}
