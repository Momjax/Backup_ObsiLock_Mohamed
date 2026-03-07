package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Vue JavaFX construite en code (équivalent du FXML UploadDialog.fxml).
 * Objectif: même structure/hiérarchie que le FXML (sans customNameField car commenté dans le FXML).
 */
public class UploadDialogView {

    // Root
    private final VBox root = new VBox(15);

    // --- fx:id nodes (mêmes noms que dans le FXML) ---
    private final Button selectFileButton = new Button("📁 Choisir un fichier");
    private final VBox fileListContainer = new VBox(8);
    private final VBox selectedFilesList = new VBox(6);
    private final Label noFileLabel = new Label("Aucun fichier sélectionné");

    private final VBox progressContainer = new VBox(5);
    private final Label progressLabel = new Label("Upload en cours...");
    private final Label progressPercentLabel = new Label("0%");
    private final ProgressBar uploadProgressBar = new ProgressBar(0);

    private final Label messageLabel = new Label();

    private final Button cancelButton = new Button("Annuler");
    private final Button uploadButton = new Button("📤 Uploader");

    // Data
    private final List<File> selectedFiles = new ArrayList<>();

    // Callbacks
    private Runnable onSelectFile;
    private Consumer<List<File>> onUpload;
    private Runnable onCancel;

    public UploadDialogView() {
        buildUi();
    }

    private void buildUi() {
        // Root styling
        root.setPrefSize(500, 600);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 8;");
        root.setPadding(new Insets(20, 25, 20, 25));

        // =========================
        // En-tête avec logo et titre
        // =========================
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/images/Logo_CryptoVault.png")));
        } catch (Exception ignored) {
            // pas bloquant si image manquante
        }
        logo.setFitHeight(50);
        logo.setFitWidth(65);
        logo.setPickOnBounds(true);
        logo.setPreserveRatio(true);

        VBox headerTitles = new VBox(2);

        Text title = new Text("📤 Uploader des fichiers");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setWrappingWidth(288.48866271972656);

        Text subtitle = new Text("Sélectionnez les fichiers à téléverser");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 11px;");
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        subtitle.setWrappingWidth(289.14794921875);

        headerTitles.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(logo, headerTitles);

        Separator sepTop = new Separator();
        VBox.setMargin(sepTop, new Insets(5, 0, 5, 0));

        // =========================
        // Zone de sélection de fichiers (grand bloc blanc)
        // =========================
        VBox selectionBox = new VBox(12);
        selectionBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6;"
        );
        selectionBox.setPadding(new Insets(15));
        VBox.setVgrow(selectionBox, Priority.ALWAYS);

        // Bouton choisir fichier
        HBox selectRow = new HBox(15);
        selectRow.setAlignment(Pos.CENTER);

        selectFileButton.setPrefHeight(39.0);
        selectFileButton.setPrefWidth(177.0);
        selectFileButton.setStyle(
                "-fx-background-color: #980b0b; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 10 20;"
        );
        selectFileButton.setFont(Font.font(13));
        selectFileButton.setEffect(makeShadow(0.6, 0.04, 0.04, 0.3, 8.0, 3.5, 1.0, 1.0));
        selectFileButton.setOnAction(e -> triggerSelectFile());

        // <Text .../> vide dans le FXML
        Text emptyHint = new Text();
        emptyHint.setFill(Color.web("#999999"));
        emptyHint.setStyle("-fx-font-size: 12px;");

        selectRow.getChildren().addAll(selectFileButton, emptyHint);

        Separator sepMid = new Separator();
        VBox.setMargin(sepMid, new Insets(5, 0, 5, 0));

        // Liste des fichiers sélectionnés
        Label filesLabel = new Label("Fichiers sélectionnés :");
        filesLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 12px;");

        selectedFilesList.setMinHeight(120);
        selectedFilesList.setSpacing(6);
        selectedFilesList.setStyle(
                "-fx-background-color: #f8f8f8; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 10; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4;"
        );
        VBox.setVgrow(selectedFilesList, Priority.ALWAYS);

        noFileLabel.setAlignment(Pos.CENTER);
        noFileLabel.setMaxWidth(Double.MAX_VALUE);
        noFileLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic; -fx-font-size: 11px;");

        fileListContainer.setSpacing(8);
        VBox.setVgrow(fileListContainer, Priority.ALWAYS);

        selectedFilesList.getChildren().add(noFileLabel);
        fileListContainer.getChildren().addAll(filesLabel, selectedFilesList);

        selectionBox.getChildren().addAll(selectRow, sepMid, fileListContainer);

        // =========================
        // Barre de progression (cachée par défaut)
        // =========================
        progressContainer.setManaged(false);
        progressContainer.setVisible(false);
        progressContainer.setSpacing(5);

        HBox progressRow = new HBox(10);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        progressLabel.setStyle("-fx-text-fill: #980b0b; -fx-font-weight: bold; -fx-font-size: 12px;");
        progressPercentLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

        Region progressSpacer = new Region();
        HBox.setHgrow(progressSpacer, Priority.ALWAYS);

        progressRow.getChildren().addAll(progressLabel, progressSpacer, progressPercentLabel);

        uploadProgressBar.setPrefHeight(10);
        uploadProgressBar.setMaxWidth(Double.MAX_VALUE);
        uploadProgressBar.setStyle("-fx-accent: #980b0b;");
        uploadProgressBar.setEffect(makeShadow(0.6, 0.04, 0.04, 0.2, 5.0, 2.0, 1.0, 1.0));

        progressContainer.getChildren().addAll(progressRow, uploadProgressBar);

        // =========================
        // Messages d'erreur ou succès (caché par défaut)
        // =========================
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-padding: 10; -fx-background-radius: 4; -fx-font-size: 12px;");

        // =========================
        // Boutons d'action
        // =========================
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(actions, new Insets(10, 0, 0, 0));

        cancelButton.setStyle(
                "-fx-background-color: #cccccc; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 20;"
        );
        cancelButton.setFont(Font.font(12));
        cancelButton.setOnAction(e -> triggerCancel());

        uploadButton.setDefaultButton(true);
        uploadButton.setDisable(true);
        uploadButton.setStyle(
                "-fx-background-color: #980b0b; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 24; " +
                        "-fx-font-weight: bold;"
        );
        uploadButton.setFont(Font.font(12));
        uploadButton.setEffect(makeShadow(0.6, 0.04, 0.04, 0.4, 10.0, 4.5, 2.0, 2.0));
        uploadButton.setOnAction(e -> triggerUpload());

        actions.getChildren().addAll(cancelButton, uploadButton);

        // =========================
        // Ajout au root (même ordre que FXML)
        // =========================
        root.getChildren().addAll(
                header,
                sepTop,
                selectionBox,
                progressContainer,
                messageLabel,
                actions
        );

        // Focus behavior
        root.setFocusTraversable(true);
        root.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            if (!(target instanceof TextField) && !(target instanceof PasswordField)) {
                root.requestFocus();
            }
        });
    }

    private DropShadow makeShadow(
            double r, double g, double b, double opacity,
            double size, double radius, double ox, double oy
    ) {
        DropShadow ds = new DropShadow();
        ds.setWidth(size);
        ds.setHeight(size);
        ds.setRadius(radius);
        ds.setOffsetX(ox);
        ds.setOffsetY(oy);
        ds.setColor(new Color(r, g, b, opacity));
        return ds;
    }

    // =========================
    // Triggers / callbacks
    // =========================
    private void triggerSelectFile() {
        clearMessage();
        if (onSelectFile != null) onSelectFile.run();
    }

    private void triggerUpload() {
        clearMessage();
        if (onUpload != null) onUpload.accept(new ArrayList<>(selectedFiles));
    }

    private void triggerCancel() {
        clearMessage();
        if (onCancel != null) onCancel.run();
    }

    // =========================
    // Public API
    // =========================

    public Node getRoot() {
        return root;
    }

    public void setOnSelectFile(Runnable onSelectFile) {
        this.onSelectFile = onSelectFile;
    }

    public void setOnUpload(Consumer<List<File>> onUpload) {
        this.onUpload = onUpload;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public List<File> getSelectedFiles() {
        return new ArrayList<>(selectedFiles);
    }

    // =========================
    // File list management
    // =========================

    public void setSelectedFiles(List<File> files) {
        selectedFiles.clear();
        if (files != null) selectedFiles.addAll(files);
        refreshFileList();
    }

    public void addSelectedFile(File file) {
        if (file == null) return;
        selectedFiles.add(file);
        refreshFileList();
    }

    public void removeSelectedFile(File file) {
        selectedFiles.remove(file);
        refreshFileList();
    }

    private void refreshFileList() {
        selectedFilesList.getChildren().clear();

        if (selectedFiles.isEmpty()) {
            selectedFilesList.getChildren().add(noFileLabel);
            uploadButton.setDisable(true);
            return;
        }

        for (File f : selectedFiles) {
            selectedFilesList.getChildren().add(createFileRow(f));
        }

        uploadButton.setDisable(false);
    }

    private Node createFileRow(File file) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(file.getName());
        name.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeBtn = new Button("✖");
        removeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #980b0b; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        removeBtn.setOnAction(e -> removeSelectedFile(file));

        row.getChildren().addAll(name, spacer, removeBtn);
        return row;
    }

    // =========================
    // Progress management
    // =========================

    public void showProgress(boolean show) {
        progressContainer.setVisible(show);
        progressContainer.setManaged(show);

        if (!show) {
            uploadProgressBar.setProgress(0);
            progressPercentLabel.setText("0%");
            progressLabel.setText("Upload en cours...");
        }
    }

    /** progress entre 0.0 et 1.0 */
    public void updateProgress(double progress) {
        double p = Math.max(0, Math.min(1, progress));
        uploadProgressBar.setProgress(p);
        int percent = (int) Math.round(p * 100);
        progressPercentLabel.setText(percent + "%");
    }

    public void setProgressText(String text) {
        progressLabel.setText(text == null ? "" : text);
    }

    // =========================
    // Message management
    // =========================

    public void showError(String message) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.setStyle(
                "-fx-padding: 10; -fx-background-radius: 4; -fx-font-size: 12px; " +
                        "-fx-background-color: #ffe6e6; -fx-text-fill: #980b0b;"
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    public void showSuccess(String message) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.setStyle(
                "-fx-padding: 10; -fx-background-radius: 4; -fx-font-size: 12px; " +
                        "-fx-background-color: #e7f8ee; -fx-text-fill: #1e8449;"
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    public void clearMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    // =========================
    // Buttons state
    // =========================

    public void setUploadDisabled(boolean disabled) {
        uploadButton.setDisable(disabled);
    }

    public void setSelectDisabled(boolean disabled) {
        selectFileButton.setDisable(disabled);
    }

    public void setCancelDisabled(boolean disabled) {
        cancelButton.setDisable(disabled);
    }
}
