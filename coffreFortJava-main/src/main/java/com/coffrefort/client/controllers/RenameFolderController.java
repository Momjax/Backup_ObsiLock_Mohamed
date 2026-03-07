package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class RenameFolderController {

    @FXML private Label currentNameLabel;
    @FXML private TextField nameField;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Stage dialogStage;
    private Consumer<String> onConfirm;

    /**
     * Injecte le Stage de la fenêtre modale pour pouvoir la fermer depuis le contrôleur
     * @param stage
     */
    public void setStage(Stage stage){
        this.dialogStage = stage;
    }

    /**
     * Affiche le nom actuel, pré-remplit le champ de saisie et sélectionne le texte pour un renommage rapide
     * @param name
     */
    public void setCurrentName(String name){
        currentNameLabel.setText(name);
        nameField.setText(name);
        nameField.requestFocus();
        nameField.selectAll();
    }

    /**
     * Définit le callback appelé avec le nouveau nom quand l’utilisateur confirme le renommage
     * @param callback
     */
    public void setOnConfirm(Consumer<String> callback){
        this.onConfirm = callback;
    }

    @FXML
    /**
     * Gère le clic sur “Annuler” en fermant la fenêtre de renommage
     */
    private void handleCancel(){
        if(dialogStage != null){
            dialogStage.close();
        }
    }

    @FXML
    /**
     * Valide le nouveau nom, affiche une erreur si vide, sinon appelle le callback puis ferme la fenêtre
     */
    private void handleConfirm(){
        String newName = nameField.getText() == null ? "" : nameField.getText().trim();

        if(newName.isBlank()){
            showError("Le nom ne peut pas être vide");
            return;
        }

        hideError();

        if(onConfirm != null){
            onConfirm.accept(newName);
        }
    }

    public void close(){
        if(dialogStage != null){
            dialogStage.close();
        }
    }

    /**
     * Affiche un message d’erreur dans l’UI en rendant le label visible
     * @param message
     */
    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    /**
     * Masque le message d’erreur dans l’UI
     */
    private void hideError(){
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
}