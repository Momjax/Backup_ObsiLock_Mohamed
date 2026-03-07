package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class CreateFolderView {

    // Root
    private final VBox root = new VBox(15);

    // UI elements (équivalents des fx:id du FXML)
    private final TextField folderNameField = new TextField();
    private final Label errorLabel = new Label();
    private final Button cancelButton = new Button("Annuler");
    private final Button createButton = new Button("Créer");

    // Callbacks
    private Runnable onCancel;
    private Consumer<String> onCreateFolder;

    public CreateFolderView() {
        buildUi();
    }

    private void buildUi() {
        // Root styling (équivalent VBox racine du FXML)
        root.setPrefSize(450, 350);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 8;");
        root.setPadding(new Insets(20, 25, 20, 25));

        // =========================
        // En-tête
        // =========================
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Bande rouge avec icône 📁
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefWidth(48);
        iconBox.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 8;");
        iconBox.setPadding(new Insets(10));

        Text iconText = new Text("📁");
        iconText.setFill(Color.WHITE);
        iconText.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(iconText);

        // Titre + sous-titre
        VBox titles = new VBox(4);
        titles.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Créer un nouveau dossier");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Entrez le nom du dossier à créer");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 11px;");

        titles.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, titles);

        // Séparateur + marges
        Separator sepTop = new Separator();
        VBox.setMargin(sepTop, new Insets(10, 0, 5, 0));

        // =========================
        // Zone de saisie (blanche)
        // =========================
        VBox inputBox = new VBox(12);
        inputBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6;"
        );
        inputBox.setPadding(new Insets(15));

        Label nameLabel = new Label("Nom du dossier :");
        nameLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 12px;");

        folderNameField.setPromptText("Entrez le nom du dossier");
        folderNameField.setStyle(
                "-fx-background-radius: 4; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-font-size: 13px;"
        );
        folderNameField.setEffect(makeShadow(
                0.6, 0.04, 0.04,
                0.15,
                5.0, 2.0,
                1.0, 1.0
        ));

        inputBox.getChildren().addAll(nameLabel, folderNameField);

        // =========================
        // Bloc erreur (comme FXML : VBox align CENTER + label caché)
        // =========================
        VBox errorBox = new VBox();
        errorBox.setAlignment(Pos.CENTER);

        errorLabel.setText("");
        errorLabel.setWrapText(true);
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setStyle(
                "-fx-background-color: #ffe5e5; " +
                        "-fx-text-fill: #980b0b; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6;"
        );

        errorBox.getChildren().add(errorLabel);

        // =========================
        // Spacer pour pousser les boutons en bas
        // =========================
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // =========================
        // Boutons d'action
        // =========================
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        cancelButton.setCancelButton(true);
        cancelButton.setFont(Font.font(12));
        cancelButton.setStyle(
                "-fx-background-color: #cccccc; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 20;"
        );
        cancelButton.setOnAction(e -> triggerCancel());

        createButton.setDefaultButton(true);
        createButton.setFont(Font.font(12));
        createButton.setStyle(
                "-fx-background-color: #980b0b; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 24; " +
                        "-fx-font-weight: bold;"
        );
        createButton.setEffect(makeShadow(
                0.6, 0.04, 0.04,
                0.4,
                10.0, 10.0,
                0.0, 0.0
        ));
        createButton.setOnAction(e -> triggerCreate());

        actions.getChildren().addAll(cancelButton, createButton);

        // =========================
        // Ajout au root
        // =========================
        root.getChildren().addAll(
                header,
                sepTop,
                inputBox,
                errorBox,
                spacer,
                actions
        );

        // Comportement focus comme RegisterView
        root.setFocusTraversable(true);
        root.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            if (!(target instanceof TextField)) {
                root.requestFocus();
            }
        });
    }

    private DropShadow makeShadow(
            double r, double g, double b, double opacity,
            double width, double radius, double offsetX, double offsetY
    ) {
        DropShadow ds = new DropShadow();
        ds.setWidth(width);
        ds.setHeight(width);
        ds.setRadius(radius);
        ds.setOffsetX(offsetX);
        ds.setOffsetY(offsetY);
        ds.setColor(new Color(r, g, b, opacity));
        return ds;
    }

    // =========================
    // Triggers
    // =========================
    private void triggerCancel() {
        clearError();
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void triggerCreate() {
        clearError();
        String name = getFolderName();
        if (name.isEmpty()) {
            showError("Le nom du dossier ne peut pas être vide.");
            return;
        }

        if (onCreateFolder != null) {
            onCreateFolder.accept(name);
        }
    }

    // =========================
    // API publique
    // =========================

    public Node getRoot() {
        return root;
    }

    /** Récupère le nom du dossier (trim). */
    public String getFolderName() {
        String v = folderNameField.getText();
        return v == null ? "" : v.trim();
    }

    /** Définit le nom du dossier dans le champ. */
    public void setFolderName(String name) {
        folderNameField.setText(name == null ? "" : name);
    }

    /** Callback lorsque l'utilisateur valide (Créer). */
    public void setOnCreateFolder(Consumer<String> onCreateFolder) {
        this.onCreateFolder = onCreateFolder;
    }

    /** Callback lorsque l'utilisateur annule. */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setCreateDisabled(boolean disabled) {
        createButton.setDisable(disabled);
    }

    public void setCancelDisabled(boolean disabled) {
        cancelButton.setDisable(disabled);
    }

    // =========================
    // Gestion erreur
    // =========================
    public void showError(String message) {
        errorLabel.setText(message == null ? "" : message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
}
