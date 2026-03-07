package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDeleteController {

    //propriétés
    @FXML private Label messageLabel;
    @FXML private Label fileNameLabel;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Stage dialogStage;

    // callbacks
    private Runnable onCancel;
    private Runnable onConfirm;

    //méthodes
    @FXML
    /**
     * Initialise le contrôleur au chargement FXML (point d’extension pour logs ou configuration)
     */
    private void initialize() {
        // opossibleajouter du log si besoin
    }

    /**
     * Injecte le Stage de la fenêtre de confirmation afin de pouvoir la fermer depuis le contrôleur
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * définir le message
     * @param message
     */
    public void setMessage(String message){
        if(messageLabel != null){
            messageLabel.setText(message != null ? message : "");
        }
    }

    /**
     * Affiche dans le dialogue le nom du fichier sélectionné pour confirmer la suppression
     * @param fileName
     */
    public void setFileName(String fileName) {
        if (fileNameLabel != null) {
            fileNameLabel.setText(fileName != null ? fileName : "");
        }
    }

    /**
     * Définit le callback à exécuter lorsque l’utilisateur annule la suppression
     * @param onCancel
     */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    /**
     * Définit le callback à exécuter lorsque l’utilisateur confirme la suppression
     * @param onConfirm
     */
    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    @FXML
    /**
     * Gère le clic sur “Annuler” en exécutant le callback puis en fermant la fenêtre
     */
    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
        close();
    }

    @FXML
    /**
     * Gère le clic sur “Confirmer” en exécutant le callback puis en fermant la fenêtre
     */
    private void handleConfirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
        close();
    }

    /**
     * Ferme la fenêtre de dialogue (Stage injecté, sinon récupération via le bouton en fallback)
     */
    private void close() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            // fallback si jamais le stage n’est pas injecté
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }
}
