package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.function.Consumer;

public class ShareView {

    private final VBox root = new VBox(15);

    private final Label itemNameLabel = new Label("NomDeLElement");

    private final TextField recipientField = new TextField();
    private final TextField expiresField = new TextField();
    private final TextField maxUsesField = new TextField();

    private final CheckBox allowVersionsCheckBox =
            new CheckBox("Autoriser le téléchargement de versions spécifiques");

    private final Label errorLabel = new Label("");

    private final Button cancelButton = new Button("Annuler");
    private final Button shareButton = new Button("Partager");

    private Consumer<ShareData> onShare;
    private Runnable onCancel;

    public ShareView() {
        buildUi();
    }

    private void buildUi() {
        root.setPrefSize(460, 500);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 8;");
        root.setPadding(new Insets(20, 25, 20, 25));

        // ===== En-tête =====
        root.getChildren().add(buildHeader());

        // Séparateur
        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(10, 0, 5, 0));
        root.getChildren().add(sep);

        // ===== Zone message =====
        root.getChildren().add(buildMessageBox());

        // ===== Options de partage (bloc dédié comme dans le FXML) =====
        root.getChildren().add(buildAllowVersionsBox());

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);

        // ===== Actions =====
        HBox actions = buildActionsBox();
        VBox.setMargin(actions, new Insets(0, 0, 15, 0));
        root.getChildren().add(actions);

        // Focus behavior
        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> {
            Object t = e.getTarget();
            if (!(t instanceof TextField)) {
                root.requestFocus();
            }
        });
    }

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Bande rouge avec icône
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(49, 41);
        iconBox.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 8;");
        iconBox.setPadding(new Insets(10));

        Text icon = new Text("🔗");
        icon.setFill(Color.WHITE);
        icon.setStyle("-fx-font-size: 24px;");
        icon.setWrappingWidth(24.0);
        iconBox.getChildren().add(icon);

        // Titre + sous-titre
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Partager");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Choisissez un destinataire");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 12px;");

        titleBox.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(iconBox, titleBox);
        return header;
    }

    private VBox buildMessageBox() {
        VBox box = new VBox(10);

        Label introLabel = new Label("Vous êtes sur le point de partager l'élément suivant :");
        introLabel.setAlignment(Pos.CENTER);
        introLabel.setPrefWidth(410);
        introLabel.setWrapText(true);
        introLabel.setTextAlignment(TextAlignment.CENTER);
        introLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 13px;");

        itemNameLabel.setAlignment(Pos.CENTER);
        itemNameLabel.setPrefWidth(410);
        itemNameLabel.setWrapText(true);
        itemNameLabel.setTextAlignment(TextAlignment.CENTER);
        itemNameLabel.setStyle("-fx-text-fill: #980b0b; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Destinataire
        VBox recipientBox = new VBox(6);

        Label recipientLabel = new Label("Destinataire (email ou nom d'utilisateur)");
        VBox.setMargin(recipientLabel, new Insets(4, 0, 0, 0));
        recipientLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px; -fx-font-weight: bold;");

        recipientField.setPromptText("ex: alice@domaine.com");
        recipientField.setStyle(
                "-fx-background-radius: 6; -fx-padding: 8 10; " +
                        "-fx-border-radius: 6; -fx-border-color: #C8C8C8;"
        );

        recipientBox.getChildren().addAll(recipientLabel, recipientField);

        // Options expiration / max uses
        HBox optionsRow = new HBox(12);
        optionsRow.getChildren().addAll(buildExpiresBox(), buildMaxUsesBox());

        // Erreur
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: #980b0b; -fx-font-size: 12px; -fx-font-weight: bold;");

        box.getChildren().addAll(introLabel, itemNameLabel, recipientBox, optionsRow, errorLabel);
        return box;
    }

    private VBox buildExpiresBox() {
        VBox expiresBox = new VBox(6);
        expiresBox.setPrefWidth(195);
        HBox.setHgrow(expiresBox, Priority.ALWAYS);

        Label expiresLabel = new Label("Expiration (jours)");
        VBox.setMargin(expiresLabel, new Insets(4, 0, 0, 0));
        expiresLabel.setAlignment(Pos.CENTER);
        expiresLabel.setPrefWidth(186);
        expiresLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px; -fx-font-weight: bold;");

        expiresField.setAlignment(Pos.CENTER);
        expiresField.setPromptText("ex: 7");
        expiresField.setPrefSize(85, 36);
        expiresField.setStyle(
                "-fx-background-radius: 6; -fx-padding: 8 10; " +
                        "-fx-border-radius: 6; -fx-border-color: #C8C8C8;"
        );
        VBox.setMargin(expiresField, new Insets(0, 0, 0, 50));

        expiresBox.getChildren().addAll(expiresLabel, expiresField);
        return expiresBox;
    }

    private VBox buildMaxUsesBox() {
        VBox maxUsesBox = new VBox(6);
        maxUsesBox.setPrefWidth(110);
        HBox.setHgrow(maxUsesBox, Priority.ALWAYS);

        Label maxUsesLabel = new Label("Nombre maximum d'utilisations");
        VBox.setMargin(maxUsesLabel, new Insets(4, 0, 0, 0));
        maxUsesLabel.setAlignment(Pos.CENTER);
        maxUsesLabel.setPrefWidth(215);
        maxUsesLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px; -fx-font-weight: bold;");

        maxUsesField.setAlignment(Pos.CENTER);
        maxUsesField.setPromptText("ex: 3");
        maxUsesField.setPrefSize(85, 36);
        maxUsesField.setStyle(
                "-fx-background-radius: 6; -fx-padding: 8 10; " +
                        "-fx-border-radius: 6; -fx-border-color: #C8C8C8;"
        );
        VBox.setMargin(maxUsesField, new Insets(0, 0, 0, 50));

        maxUsesBox.getChildren().addAll(maxUsesLabel, maxUsesField);
        return maxUsesBox;
    }

    private VBox buildAllowVersionsBox() {
        VBox allowVersionsBox = new VBox(6);
        VBox.setMargin(allowVersionsBox, new Insets(6, 0, 0, 0));

        Label optionsLabel = new Label("Options de partage");
        optionsLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px; -fx-font-weight: bold;");

        allowVersionsCheckBox.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");
        allowVersionsCheckBox.setPrefWidth(394);

        Label helpLabel = new Label("Si activé, le destinataire pourra choisir une version précise du fichier.");
        helpLabel.setWrapText(true);
        helpLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px; -fx-padding: 0 0 0 25;");

        allowVersionsBox.getChildren().addAll(optionsLabel, allowVersionsCheckBox, helpLabel);
        return allowVersionsBox;
    }

    private HBox buildActionsBox() {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);

        cancelButton.setCancelButton(true);
        cancelButton.setFont(Font.font(12));
        cancelButton.setStyle(
                "-fx-background-color: #cccccc; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 20;"
        );
        cancelButton.setOnAction(e -> triggerCancel());

        shareButton.setDefaultButton(true);
        shareButton.setFont(Font.font(12));
        shareButton.setStyle(
                "-fx-background-color: #980b0b; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 24; " +
                        "-fx-font-weight: bold;"
        );
        shareButton.setOnAction(e -> triggerShare());

        DropShadow ds = new DropShadow();
        ds.setRadius(10.0);
        ds.setColor(Color.color(0.05, 0.25, 0.55, 0.35));
        shareButton.setEffect(ds);

        actions.getChildren().addAll(cancelButton, shareButton);
        return actions;
    }

    // ===== Triggers =====

    private void triggerShare() {
        hideError();

        String recipient = getRecipient();
        if (recipient.isEmpty()) {
            showError("Veuillez renseigner un destinataire.");
            return;
        }

        Integer expiresDays = parsePositiveIntOrNull(expiresField.getText());
        if (expiresField.getText() != null && !expiresField.getText().trim().isEmpty() && expiresDays == null) {
            showError("Expiration invalide (nombre attendu).");
            return;
        }

        Integer maxUses = parsePositiveIntOrNull(maxUsesField.getText());
        if (maxUsesField.getText() != null && !maxUsesField.getText().trim().isEmpty() && maxUses == null) {
            showError("Nombre d'utilisations invalide (nombre attendu).");
            return;
        }

        ShareData data = new ShareData(
                recipient,
                expiresDays,
                maxUses,
                allowVersionsCheckBox.isSelected()
        );

        if (onShare != null) {
            onShare.accept(data);
        }
    }

    private void triggerCancel() {
        hideError();
        if (onCancel != null) {
            onCancel.run();
        }
    }

    // ===== API publique =====

    public void setOnShare(Consumer<ShareData> onShare) {
        this.onShare = onShare;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public Node getRoot() {
        return root;
    }

    public void setItemName(String name) {
        itemNameLabel.setText(name != null ? name : "");
    }

    public String getRecipient() {
        String v = recipientField.getText();
        return v == null ? "" : v.trim();
    }

    public void showError(String message) {
        errorLabel.setText(message == null ? "" : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private Integer parsePositiveIntOrNull(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        try {
            int v = Integer.parseInt(t);
            return v >= 1 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ===== DTO interne (comme avant) =====
    public static class ShareData {
        private final String recipient;
        private final Integer expiresDays;
        private final Integer maxUses;
        private final boolean allowVersions;

        public ShareData(String recipient, Integer expiresDays, Integer maxUses, boolean allowVersions) {
            this.recipient = recipient;
            this.expiresDays = expiresDays;
            this.maxUses = maxUses;
            this.allowVersions = allowVersions;
        }

        public String getRecipient() {
            return recipient;
        }

        public Integer getExpiresDays() {
            return expiresDays;
        }

        public Integer getMaxUses() {
            return maxUses;
        }

        public boolean isAllowVersions() {
            return allowVersions;
        }
    }
}
