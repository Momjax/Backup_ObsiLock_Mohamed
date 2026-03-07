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

public class ConfirmLogoutView {

    private final VBox root = new VBox(15);

    // Dans le FXML il y a 2 labels séparés
    private final Label infoLabel = new Label("Voulez-vous vraiment vous déconnecter ?");
    private final Label infoLabel1 = new Label("Toutes les opérations en cours seront interrompues.");

    private final Button cancelButton = new Button("Annuler");
    private final Button confirmButton = new Button("Se déconnecter");

    // callback quand l’utilisateur clique sur "Annuler"
    private Runnable onCancel;

    // callback quand l’utilisateur confirme la déconnexion
    private Runnable onConfirm;

    public ConfirmLogoutView() {
        buildUi();
    }

    private void buildUi() {
        // comme FXML : padding 20/25
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

        Text icon = new Text("🚪");
        icon.setFill(Color.WHITE);
        icon.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(icon);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Confirmer la déconnexion");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Vous allez être déconnecté de CryptoVault.");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 11px;");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, titleBox);

        // ===== Separator + marges =====
        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(10, 0, 5, 0));

        // ===== Zone de message =====
        VBox messageBox = new VBox(8);

        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.setPrefWidth(369);
        infoLabel.setWrapText(true);
        infoLabel.setTextAlignment(TextAlignment.CENTER);
        infoLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        infoLabel1.setAlignment(Pos.CENTER);
        infoLabel1.setPrefWidth(370);
        infoLabel1.setWrapText(true);
        infoLabel1.setTextAlignment(TextAlignment.CENTER);
        infoLabel1.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        messageBox.getChildren().addAll(infoLabel, infoLabel1);

        // Spacer pour pousser les boutons en bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // ===== Boutons =====
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
                "-fx-background-color: #980b0b; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 24; -fx-font-weight: bold;"
        );

        DropShadow ds = new DropShadow();
        ds.setRadius(10.0);
        ds.setColor(Color.color(0.6, 0.04, 0.04, 0.4));
        confirmButton.setEffect(ds);

        confirmButton.setOnAction(e -> triggerConfirm());

        HBox actions = new HBox(12, cancelButton, confirmButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // ===== Construction finale =====
        root.getChildren().addAll(header, separator, messageBox, spacer, actions);
    }

    private void triggerCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void triggerConfirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    public Node getRoot() {
        return root;
    }
}

