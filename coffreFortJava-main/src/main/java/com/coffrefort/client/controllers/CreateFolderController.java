package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class CreateFolderController {

    //propriétés
    @FXML private TextField folderNameField;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button createButton;

    // Stage de la fenêtre modale (optionnel mais pratique)
    private Stage dialogStage;

    // Callback appelé quand l'utilisateur valide avec un nom correct
    private Consumer<String> onCreateFolder;

    // Callback quand l'utilisateur annule
    private Runnable onCancel;


    //méthodes
    @FXML
    /**
     * Initialise l’état de l’UI en masquant le label d’erreur au chargement de la vue
     */
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Injecte le Stage de la fenêtre modale pour permettre sa fermeture depuis le contrôleur
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


    /**
     * Définit le callback appelé avec le nom du dossier lorsque l’utilisateur valide la création.
     * @param onCreateFolder
     */
    public void setOnCreateFolder(Consumer<String> onCreateFolder) {
        this.onCreateFolder = onCreateFolder;
    }


    /**
     * Définit le callback exécuté lorsque l’utilisateur annule la création du dossier
     * @param onCancel
     */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }


    /**
     * Gère l’annulation en nettoyant l’erreur, exécutant le callback éventuel, puis en fermant la fenêtre
     */
    @FXML
    private void handleCancel() {
        clearError();

        if (onCancel != null) {
            onCancel.run();
        }

        if (dialogStage != null) {
            dialogStage.close();
        }

    }


    /**
     * Valide le nom saisi, affiche une erreur si invalide, sinon déclenche le callback de création et ferme la fenêtre.
     */
    @FXML
    private void handleCreate() {
        clearError();

        String name = folderNameField != null ? folderNameField.getText() : "";
        if (name == null) {
            name = "";
        }
        name = name.trim();

        if (name.isEmpty()) {
            showError("Le nom du dossier ne peut pas être vide.");
            return;
        }

        if (name.length() > 50) {
            showError("Le nom est trop long (maximum 50 caractères).");
            return;
        }

        // Validation du nom
        if (!isValidFolderName(name)) {
            showError("Le nom du dossier contient des caractères invalides (\\/:*?\"<>|).");
            return;
        }

        if (onCreateFolder != null) {
            onCreateFolder.accept(name);
        }

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Valide le nom du dossier
     */
    private boolean isValidFolderName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Caractères interdits dans les noms de fichiers/dossiers
        String invalidChars = "[\\\\/:*?\"<>|]";

        if (name.matches(".*" + invalidChars + ".*")) {
            return false;
        }

        return true;
    }


    // --- Gestion des erreurs ---

    /**
     * Affiche un message d’erreur dans l’UI en rendant le label visible
     * @param message
     */
    private void showError(String message) {
        if (errorLabel == null) return;

        errorLabel.setText(message == null ? "" : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Efface et masque le message d’erreur dans l’UI
     */
    private void clearError() {
        if (errorLabel == null) return;

        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
