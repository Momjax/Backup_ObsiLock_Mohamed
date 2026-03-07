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

public class ModifyQuotaView {

    // Root
    private final VBox root = new VBox(15);

    // Infos utilisateur
    private final Label usernameLabel = new Label("user@example.com");
    private final Label currentQuotaLabel = new Label("5.0 GB");
    private final Label usedSpaceLabel = new Label("2.5 GB");

    // Input quota
    private final TextField quotaField = new TextField();

    // Suggestions
    private final Button quota1GBButton = new Button("1 GB");
    private final Button quota5GBButton = new Button("5 GB");
    private final Button quota10GBButton = new Button("10 GB");
    private final Button quota50GBButton = new Button("50 GB");
    private final Button quota100GBButton = new Button("100 GB");

    // Error
    private final Label errorLabel = new Label("Erreur");

    // Actions
    private final Button cancelButton = new Button("Annuler");
    private final Button confirmButton = new Button("Confirmer");

    // Callbacks
    private Runnable onCancel;
    private Consumer<String> onConfirm; // reçoit le texte saisi (ex: "10")
    private Consumer<Integer> onQuickQuota; // optionnel : s'il faut gérer côté controller

    public ModifyQuotaView() {
        buildUi();
    }

    private void buildUi() {
        root.setPrefSize(500, 350);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 8;");
        root.setPadding(new Insets(20, 25, 20, 25));

        // =========================
        // En-tête
        // =========================
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefWidth(48);
        iconBox.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 8;");
        iconBox.setPadding(new Insets(10));

        Text icon = new Text("💾");
        icon.setFill(Color.WHITE);
        icon.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(icon);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Modifier le quota");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Définir une nouvelle limite de stockage");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 12px;");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, titleBox);

        // Separator (marges comme FXML)
        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(10, 0, 5, 0));

        // =========================
        // Infos utilisateur
        // =========================
        VBox infoBox = new VBox(8);
        infoBox.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 6;"
        );

        HBox userRow = infoRow("Utilisateur :", usernameLabel);
        HBox currentRow = infoRow("Quota actuel :", currentQuotaLabel);
        HBox usedRow = infoRow("Espace utilisé :", usedSpaceLabel);

        infoBox.getChildren().addAll(userRow, currentRow, usedRow);

        // =========================
        // Nouveau quota
        // =========================
        VBox quotaBox = new VBox(8);

        Label quotaLabel = new Label("Nouveau quota (en GB) :");
        quotaLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666; -fx-font-size: 12px;");

        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        quotaField.setPromptText("Ex: 10");
        quotaField.setPrefWidth(150);
        quotaField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");

        Label gbLabel = new Label("GB");
        gbLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        inputRow.getChildren().addAll(quotaField, gbLabel);

        // Suggestions
        HBox quickRow = new HBox(8);

        styleQuickButton(quota1GBButton, 1);
        styleQuickButton(quota5GBButton, 5);
        styleQuickButton(quota10GBButton, 10);
        styleQuickButton(quota50GBButton, 50);
        styleQuickButton(quota100GBButton, 100);

        quickRow.getChildren().addAll(
                quota1GBButton, quota5GBButton, quota10GBButton, quota50GBButton, quota100GBButton
        );

        quotaBox.getChildren().addAll(quotaLabel, inputRow, quickRow);

        // =========================
        // Message d'erreur
        // =========================
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setWrapText(true);
        errorLabel.setStyle(
                "-fx-background-color: #ffe5e5;" +
                        "-fx-text-fill: #980b0b;" +
                        "-fx-padding: 8;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;"
        );

        // Spacer pour pousser les boutons en bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // =========================
        // Boutons d'action
        // =========================
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);

        cancelButton.setFont(Font.font(12));
        cancelButton.setStyle(
                "-fx-background-color: #cccccc;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 20;"
        );
        cancelButton.setOnAction(e -> triggerCancel());

        confirmButton.setFont(Font.font(12));
        confirmButton.setDefaultButton(true);
        confirmButton.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;"
        );

        DropShadow ds = new DropShadow();
        ds.setRadius(10.0);
        ds.setColor(Color.color(0.60, 0.05, 0.05, 0.35));
        confirmButton.setEffect(ds);

        confirmButton.setOnAction(e -> triggerConfirm());

        actions.getChildren().addAll(cancelButton, confirmButton);

        // =========================
        // Assemble
        // =========================
        root.getChildren().addAll(header, sep, infoBox, quotaBox, errorLabel, spacer, actions);

        // Focus behavior
        root.setFocusTraversable(true);
        root.setOnMouseClicked(event -> {
            Object t = event.getTarget();
            if (!(t instanceof TextField)) {
                root.requestFocus();
            }
        });
    }

    private HBox infoRow(String leftText, Label valueLabel) {
        HBox row = new HBox(8);

        Label left = new Label(leftText);
        left.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

        valueLabel.setStyle("-fx-text-fill: #333333;");

        row.getChildren().addAll(left, valueLabel);
        return row;
    }

    private void styleQuickButton(Button btn, int gb) {
        btn.setStyle(
                "-fx-background-color: #e0e0e0;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 12;" +
                        "-fx-font-size: 11px;"
        );
        btn.setOnAction(e -> {
            // comportement FXML: setQuotaXGB => rempli le champ
            setQuotaGb(gb);

            // optionnel => pour notifier un controller/presenter
            if (onQuickQuota != null) {
                onQuickQuota.accept(gb);
            }
        });
    }

    private void triggerCancel() {
        clearError();
        if (onCancel != null) onCancel.run();
    }

    private void triggerConfirm() {
        clearError();
        if (onConfirm != null) onConfirm.accept(getQuotaText());
    }

    // =========================
    // API publique
    // =========================

    public Node getRoot() {
        return root;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    /** Reçoit le texte du champ (ex "10") */
    public void setOnConfirm(Consumer<String> onConfirm) {
        this.onConfirm = onConfirm;
    }

    /** Optionnel => pour réagir aux boutons de suggestion côté controller. */
    public void setOnQuickQuota(Consumer<Integer> onQuickQuota) {
        this.onQuickQuota = onQuickQuota;
    }

    public void setUsername(String email) {
        usernameLabel.setText(email == null ? "" : email);
    }

    public void setCurrentQuota(String text) {
        currentQuotaLabel.setText(text == null ? "" : text);
    }

    public void setUsedSpace(String text) {
        usedSpaceLabel.setText(text == null ? "" : text);
    }

    public String getQuotaText() {
        String v = quotaField.getText();
        return v == null ? "" : v.trim();
    }

    public void setQuotaText(String text) {
        quotaField.setText(text == null ? "" : text);
    }

    public void setQuotaGb(int gb) {
        quotaField.setText(String.valueOf(gb));
    }

    public void showError(String message) {
        if (message == null || message.isBlank()) {
            clearError();
            return;
        }
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    public void setConfirmDisabled(boolean disabled) {
        confirmButton.setDisable(disabled);
    }

    public void setCancelDisabled(boolean disabled) {
        cancelButton.setDisable(disabled);
    }
}

