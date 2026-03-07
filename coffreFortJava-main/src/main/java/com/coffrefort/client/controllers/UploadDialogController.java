package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.Quota;
import com.coffrefort.client.util.FileUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadDialogController {

    //propriétés
    @FXML private Button selectFileButton;
    @FXML private Button cancelButton;
    @FXML private Button uploadButton;
    @FXML private VBox fileListContainer;
    @FXML private VBox selectedFilesList;
    @FXML private Label noFileLabel;
    @FXML private TextField customNameField;
    @FXML private VBox progressContainer;
    @FXML private Label progressLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar uploadProgressBar;
    @FXML private Label messageLabel;

    private final List<File> selectedFiles = new ArrayList<>();

    private ApiClient apiClient;
    private Stage dialogStage;

    // Callback optionnel après succès (pour rafraîchir la liste des fichiers, etc.)
    private Runnable onUploadSuccess;

    private Integer targetFolderId;




    //méthodes

    /**
     * Définit l’identifiant du dossier cible dans lequel les fichiers seront uploadés.
     * @param targetFolderId
     */
    public void setTargetFolderId(Integer targetFolderId){
        this.targetFolderId = targetFolderId;
    }

    /**
     * Injecte l’ApiClient utilisé pour vérifier le quota et effectuer l’upload
     * @param apiClient
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Injecte le Stage de la fenêtre modale pour pouvoir la fermer depuis le contrôleur
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Définit le callback exécuté après un upload réussi (ex: rafraîchir la liste des fichiers)
     * @param onUploadSuccess
     */
    public void setOnUploadSuccess(Runnable onUploadSuccess) {
        this.onUploadSuccess = onUploadSuccess;
    }


    @FXML
    /**
     * Initialise l’UI (cache la progression/messages) et désactive l’upload tant qu’aucun fichier n’est sélectionné
     */
    private void initialize() {
        // Au début, pas de progression
        progressContainer.setVisible(false);
        progressContainer.setManaged(false);

        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        uploadButton.setDisable(true); // pas de fichier sélectionné

        refreshSelectedFilesUI();
    }


    /**
     * Gestion de la sélection d'un file ou plusieurs file =>ok
     * Ouvre un FileChooser pour sélectionner un ou plusieurs fichiers et met à jour la liste affichée
     */
    @FXML
    private void handleSelectFile() {
        Window owner = getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier à uploader");

        // une autre possibilité pour choisir des fichiers
        // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

        //filtrer les types de fichiers autorisés ???? à érifier
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp"),
                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("Tous les fichiers autorisés",
                        "*.jpg", "*.jpeg", "*.png", "*.webp", "*.pdf", "*.doc", "*.docx", "*.xlsx")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(owner);
        if (files != null && !files.isEmpty()) {
            //selectedFiles.clear(); => ce supprime tout!!!

            for(File file : files){

                //vérif enxtension avant d'ajouter
                String extension = FileUtils.getFileExtension(file.getName());

                if(!FileUtils.isExtensionAllowed(extension)) {
                    showErrorMessage(
                            "Fichier non autorisé : " + file.getName() + "\n" +
                                    "Extensions autorisées : " + FileUtils.getAllowedExtensionString()
                    );
                    continue; //=> passer au fichier suivant
                }

                if(!selectedFiles.contains(file)){
                    selectedFiles.add(file);
                }
            }

            refreshSelectedFilesUI();
            if(!selectedFiles.isEmpty()){
                uploadButton.setDisable(false);
            }

            hideMessage();
        }
    }

    /**
     * Vérifie les prérequis (ApiClient, fichiers, dossier, quota) puis upload les fichiers en tâche de fond avec progression =>ok
     */
    @FXML
    private void handleUpload() {

        if (apiClient == null) {
            showErrorMessage("Erreur interne : ApiClient non initialisé.");
            return;
        }

        if (selectedFiles.isEmpty()) {
            showErrorMessage("Veuillez sélectionner au moins un fichier.");
            return;
        }

        if(targetFolderId == null){
            showErrorMessage("Aucun dossier sélectionné pour l'upload.");
            return;
        }

        //vérif si tous les fichiers ont des extensions valides
        List<String> invalidFiles = new ArrayList<>();
        for(File file : selectedFiles){
            String extension = FileUtils.getFileExtension(file.getName());
            if(!FileUtils.isExtensionAllowed(extension)){
                invalidFiles.add(file.getName());
            }
        }

        if(!invalidFiles.isEmpty()){
            // \n =>retour à la ligne
            //String.join(", ", invalidFiles) => document.txt, video.mp4
            showErrorMessage(
                    "Certains fichiers ont des extensions non autorisées :\n" +
                            String.join(", ", invalidFiles) + "\n\n" +
                            "Extensions autorisées : " + FileUtils.getAllowedExtensionString()
            );
            return;
        }

        //vérif quota
        try{
            Quota quota = apiClient.getQuota();

            long used = quota.getUsed();
            long max = quota.getMax();

            long totalUploadSize = selectedFiles.stream().mapToLong(File::length).sum();
            long remaining = max - used;

            if(remaining <= 0){
                showErrorMessage("Upload impossible : espace de stockage plein.");
                return;
            }

            if(totalUploadSize > remaining){
                showErrorMessage(
                        "Espace de stockage insuffisant.\n\n" +
                            "Espace disponible : " + FileUtils.formatSize(remaining) + "\n" +
                            "Taille totale des fichiers sélectionnés : " + FileUtils.formatSize(totalUploadSize)
                );
                return;
            }
        } catch (ApiClient.AuthenticationException e) {
            // Gestion spécifique des erreurs d'authentification
            showErrorMessage(
                    "Session expirée.\n\n" +
                            e.getMessage() + "\n\n" +
                            "Veuillez vous reconnecter."
            );
            e.printStackTrace();
            return;

        } catch (Exception e) {
            String errorMessage = "Impossible de vérifier le quota.";

            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMessage = e.getMessage();
            }
            showErrorMessage(errorMessage);
            e.printStackTrace();
            return;
        }

        // Préparation UI
        selectFileButton.setDisable(true);
        uploadButton.setDisable(true);
        cancelButton.setDisable(true);

        progressContainer.setVisible(true);
        progressContainer.setManaged(true);
        uploadProgressBar.setProgress(0);
        progressLabel.setText("Upload en cours...");
        progressPercentLabel.setText("0%");
        hideMessage();

        // Task pour ne pas bloquer le thread JavaFX
        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                //apiClient.uploadFile(fileToUpload);
                int total = selectedFiles.size();
                int done = 0;

                updateProgress(0, total);
                updateMessage("Upload 0/" + total);

                // boucle upload fichiers
                for (File file : selectedFiles) {
                    if (isCancelled()) break;

                    updateMessage("Upload de " + file.getName() + " (" + (done + 1) + "/" + total + ")");

                    // upload réel
                    apiClient.uploadFile(file, targetFolderId);

                    done++;
                    updateProgress(done, total);
                }
                return null;
            }
        };

        // Binding de la progression
        uploadProgressBar.progressProperty().unbind();
        uploadProgressBar.progressProperty().bind(uploadTask.progressProperty());

        //Bind progress message => text
        uploadTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                progressLabel.setText(newVal);
            }
        });

        // % label stable
        uploadTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            double p = (newVal == null ? 0 : newVal.doubleValue());

            if (p < 0) { // indeterminate
                progressPercentLabel.setText("...");
                return;
            }

            int percent = (int) Math.round(p * 100);
            progressPercentLabel.setText(percent + "%");
        });

        uploadTask.setOnSucceeded(event -> {
            uploadProgressBar.progressProperty().unbind();
            uploadProgressBar.setProgress(1.0);

            selectFileButton.setDisable(false);
            uploadButton.setDisable(false);
            cancelButton.setDisable(false);

            progressLabel.setText("Upload terminé ✔");
            progressPercentLabel.setText("100%");
            showSuccessMessage("Fichier(s) uploadé(s) avec succès.");

            if (onUploadSuccess != null) {
                onUploadSuccess.run();
            }

            // fermer la fenêtre après succès
            closeDialog();
        });

        uploadTask.setOnFailed(event -> {
            uploadProgressBar.progressProperty().unbind();
            uploadProgressBar.setProgress(0);

            selectFileButton.setDisable(false);
            uploadButton.setDisable(false);
            cancelButton.setDisable(false);

            progressLabel.setText("Erreur lors de l'upload");

            Throwable ex = uploadTask.getException();
            String errorMessage = "Une erreur est survenue pendant l'upload.";
            if (ex != null) {
                ex.printStackTrace();

                // Afficher le message d'erreur réel
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    errorMessage = ex.getMessage();
                }
            }

            showErrorMessage(errorMessage);
        });

        new Thread(uploadTask, "upload-task").start();
    }

    /**
     * Gestion de "annulation"
     * Annule et ferme la fenêtre d’upload sans effectuer d’action
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }


    // ====== Méthodes utilitaires ======

    /**
     * Met à jour la liste visuelle des fichiers sélectionnés avec leur taille et un bouton de suppression =>ok
     */
    private void refreshSelectedFilesUI() {
        selectedFilesList.getChildren().clear();

        if (selectedFiles.isEmpty()) {
            noFileLabel.setVisible(true);
            noFileLabel.setManaged(true);
            selectedFilesList.getChildren().add(noFileLabel);
            uploadButton.setDisable(true);
            return;
        }

        noFileLabel.setVisible(false);
        noFileLabel.setManaged(false);

        for (File file : selectedFiles) {
            HBox row = new HBox(8);
            Label nameLabel = new Label(file.getName());
            nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

            Label sizeLabel = new Label(FileUtils.formatSize(file.length()));
            sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #777777;");

            Button removeBtn = new Button("✖");
            removeBtn.setStyle("-fx-font-size: 10px; -fx-background-color: transparent; -fx-text-fill: #980b0b;");
            removeBtn.setOnAction(e -> {
                selectedFiles.remove(file);
                refreshSelectedFilesUI();
            });

            row.getChildren().addAll(nameLabel, sizeLabel, removeBtn);
            selectedFilesList.getChildren().add(row);
        }
    }



    /**
     * Affiche un message d’erreur stylé dans le label de message.
     * @param text
     */
    private void showErrorMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: #b00020; -fx-background-color: #fdecea;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    /**
     * Affiche un message de succès stylé dans le label de message
     * @param text
     */
    private void showSuccessMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: #1b5e20; -fx-background-color: #e8f5e9;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    /**
     * Masque le label de message (erreur/succès) dans l’interface
     */
    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    /**
     * Ferme la fenêtre modale (Stage injecté ou récupération via la scène en fallback)
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            // fallback si pas de stage injecté
            Window w = getWindow();
            if (w instanceof Stage) {
                ((Stage) w).close();
            }
        }
    }

    /**
     * sélection de la fenêtre "concernéé"
     * Retourne la fenêtre courante (Stage injecté sinon fenêtre trouvée depuis le bouton)
     * @return
     */
    private Window getWindow() {
        if (dialogStage != null) {
            return dialogStage;
        }
        if (selectFileButton != null && selectFileButton.getScene() != null) {
            return selectFileButton.getScene().getWindow();
        }
        return null;
    }
}
