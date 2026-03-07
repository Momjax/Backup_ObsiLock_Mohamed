package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class ConfirmDeleteView {

    private final VBox root = new VBox(15);

    private final Label questionLabel = new Label("Voulez-vous vraiment supprimer ce fichier ?");
    private final Label fileNameLabel = new Label("NomDuFichier.ext");
    private final Label warningLabel = new Label("Impossible d'annuler après validation.");

    private final Button cancelButton = new Button("Annuler");
    private final Button confirmButton = new Button("Supprimer");

    // callbacks
    private Runnable onCancel;
    private Runnable onConfirm;

    public ConfirmDeleteView() {
        buildUi();
    }

    private void buildUi() {
        root.setPadding(new Insets(20, 25, 20, 25));
        root.setPrefSize(420, 220);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 8;");

        // ===== En-tête =====
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefWidth(48);
        iconBox.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 8;");
        iconBox.setPadding(new Insets(10));

        Text icon = new Text("🗑️");
        icon.setFill(Color.WHITE);
        icon.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(icon);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Confirmer la suppression");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Cette action est définitive.");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 12px;");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, titleBox);

        // ===== Separator + marges =====
        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(10, 0, 5, 0));

        // ===== Zone de message =====
        VBox messageBox = new VBox(8);

        questionLabel.setAlignment(Pos.CENTER);
        questionLabel.setPrefWidth(370);
        questionLabel.setWrapText(true);
        questionLabel.setTextAlignment(TextAlignment.CENTER);
        questionLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 13px;");

        fileNameLabel.setAlignment(Pos.CENTER);
        fileNameLabel.setPrefWidth(370);
        fileNameLabel.setWrapText(true);
        fileNameLabel.setTextAlignment(TextAlignment.CENTER);
        fileNameLabel.setStyle("-fx-text-fill: #980b0b; -fx-font-weight: bold; -fx-font-size: 13px;");

        warningLabel.setAlignment(Pos.CENTER);
        warningLabel.setPrefWidth(370);
        warningLabel.setWrapText(true);
        warningLabel.setTextAlignment(TextAlignment.CENTER);
        warningLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 13px;");

        messageBox.getChildren().addAll(questionLabel, fileNameLabel, warningLabel);

        // Spacer pour pousser les boutons en bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // ===== Boutons (centrés) =====
        cancelButton.setCancelButton(true);
        cancelButton.setFont(Font.font(12));
        cancelButton.setStyle(
                "-fx-background-color: #cccccc; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 20;"
        );
        cancelButton.setOnAction(e -> triggerCancel());

        confirmButton.setDefaultButton(true);
        confirmButton.setFont(Font.font(12));
        confirmButton.setStyle(
                "-fx-background-color: #d9534f; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 24; " +
                        "-fx-font-weight: bold;"
        );

        DropShadow ds = new DropShadow();
        ds.setRadius(10.0);
        ds.setColor(Color.color(0.85, 0.2, 0.2, 0.45));
        confirmButton.setEffect(ds);

        confirmButton.setOnAction(e -> triggerConfirm());

        HBox actions = new HBox(12, cancelButton, confirmButton);
        actions.setAlignment(Pos.CENTER);

        // ===== Construction finale =====
        root.getChildren().addAll(header, separator, messageBox, spacer, actions);
    }

    private void triggerCancel() {
        if (onCancel != null) onCancel.run();
    }

    private void triggerConfirm() {
        if (onConfirm != null) onConfirm.run();
    }

    // ===== API publique =====

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    /** Permet d'injecter le nom du fichier sélectionné */
    public void setFileName(String fileName) {
        fileNameLabel.setText(fileName != null ? fileName : "");
    }

    public Node getRoot() {
        return root;
    }
}
