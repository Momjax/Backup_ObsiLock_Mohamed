package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.coffrefort.client.util.FileUtils;

import java.util.function.Consumer;

public class RenameFileController {

    @FXML private Label currentNameLabel;
    @FXML private TextField nameField;
    @FXML private Label extensionLabel;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Stage dialogStage;
    private Consumer<String> onConfirm;
    private String fileExtension = "";

    /**
     * Injecte le Stage de la fenêtre modale pour pouvoir la fermer depuis le contrôleur
     * @param stage
     */
    public void setStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Affiche le nom actuel, pré-remplit le champ de saisie et sélectionne le texte pour un renommage rapide =>ok
     * @param name
     */
    public void setCurrentName(String name) {
        if(name == null || name.isEmpty()){
            return;
        }
        currentNameLabel.setText(name);

        // extraire l'extension
        int lastDot = name.lastIndexOf('.');

        //vérif si le point existe et n'est pas à la fin
        if(lastDot != -1 && lastDot < name.length() - 1 ){

            // il y a une extension
            fileExtension = name.substring(lastDot); //=> .pdf

            //String nameWithoutExtension = name.substring(0, lastDot);
            String nameWithoutExtension = FileUtils.removeExtension(name);

            //préremplir sans l'extension
            nameField.setText(nameWithoutExtension);

            if(extensionLabel != null){
                extensionLabel.setText(fileExtension);
            }
        }else{
            fileExtension = "";
            nameField.setText(name);

            if(extensionLabel != null){
                extensionLabel.setText("");
            }
        }

        nameField.requestFocus();
        nameField.selectAll();
    }

    /**
     * Définit le callback appelé avec le nouveau nom quand l’utilisateur confirme le renommage
     * @param callback
     */
    public void setOnConfirm(Consumer<String> callback) {
        this.onConfirm = callback;
    }

    @FXML
    /**
     * Gère le clic sur “Annuler” en fermant la fenêtre de renommage
     */
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    /**
     * Valide le nouveau nom, affiche une erreur si vide, sinon appelle le callback puis ferme la fenêtre =>ok
     */
    private void handleConfirm() {
        String newName = nameField.getText() == null ? "" : nameField.getText().trim();

        if (newName.isBlank()) {
            showError("Le nom ne peut pas être vide.");
            return;
        }

        hideError();

        String fullName = newName + fileExtension;

        if (onConfirm != null) {
            onConfirm.accept(fullName);
        }
        //pas mettre !!!=> si je le mets : error 'nom identique' fenêtre renamefile se ferme!!!
       // if (dialogStage != null) dialogStage.close();
    }

    /**
     * Affiche un message d’erreur dans l’UI en rendant le label visible
     * @param msg
     */
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    /**
     * Masque le message d’erreur dans l’UI
     */
    private void hideError() {
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
}


