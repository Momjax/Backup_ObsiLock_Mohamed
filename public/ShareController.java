package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ShareController {

    @FXML private Label itemNameLabel;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;

    // @FXML private TextField recipientField; // Supprimé
    @FXML private TextField expiresField;
    @FXML private TextField maxUsesField;
    @FXML private TextField shareLabelField;
    @FXML private TextField shareDescriptionField;
    @FXML private TextField recipientNoteField;

    private Stage stage;

    // Callback appelé si l'utilisateur valide (destinataire|max|expire|versions)
    private Consumer<String> onShare;
    private Runnable onCancel;
    private boolean isFolder = false;

    @FXML
    private void initialize() {
        hideError();
        if (expiresField != null) {
            expiresField.setText("7");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setItemName(String name) {
        itemNameLabel.setText(name != null ? name : "");
    }

    public void setOnShare(Consumer<String> onShare) {
        this.onShare = onShare;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    @FXML
    private void handleShare() {
        hideError();
        
        String recipient = ""; // Plus de destinataire requis

        Integer maxUses = null;
        try {
            String maxUsesText = maxUsesField.getText();
            if(maxUsesText != null && !maxUsesText.isBlank()) {
                maxUses = Integer.parseInt(maxUsesText.trim());
                if(maxUses < 1) {
                    showError("Max uses doit être >= 1 ou vide (illimité)");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            showError("Max uses invalide");
            return;
        }

        Integer expiresDays = null;
        try {
            String expiresText = expiresField.getText();
            if(expiresText != null && !expiresText.isBlank()){
                expiresDays = Integer.parseInt(expiresText.trim());
                if(expiresDays < 1) {
                    showError("Expiration doit être >= 1 ou vide (jamais)");
                    return;
                }
            }
        }catch(NumberFormatException e){
            showError("Expiration invalide");
            return;
        }

        // Récupération label et description (OBLIGATOIRES)
        String labelValue = "";
        if(shareLabelField != null && shareLabelField.getText() != null && !shareLabelField.getText().isBlank()){
            labelValue = shareLabelField.getText().trim();
        } else {
            showError("Le nom du partage est obligatoire");
            return;
        }

        String descriptionValue = "";
        if(shareDescriptionField != null && shareDescriptionField.getText() != null && !shareDescriptionField.getText().isBlank()){
            descriptionValue = shareDescriptionField.getText().trim();
        } else {
            showError("La description du partage est obligatoire");
            return;
        }

        String recipientNote = "null";
        if(recipientNoteField != null && recipientNoteField.getText() != null && !recipientNoteField.getText().isBlank()){
            recipientNote = recipientNoteField.getText().trim();
        }

        // On passe les données au format: label|description|maxUses|expiresDays|allowVersions|recipientNote
        String data = labelValue
                + "|" + descriptionValue
                + "|" + (maxUses != null ? maxUses : "null")
                + "|" + (expiresDays != null ? expiresDays : "null")
                + "|false"
                + "|" + recipientNote;

        if (onShare != null) {
            onShare.accept(data);
        }
        if (stage != null) {
            stage.close();
        }
    }


    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
        close();
    }

    private void close() {
        if (stage != null) {
            stage.close();
        } else {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }
}
