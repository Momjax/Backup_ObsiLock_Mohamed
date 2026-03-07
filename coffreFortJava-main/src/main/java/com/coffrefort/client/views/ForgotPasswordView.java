package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.function.Consumer;

public class ForgotPasswordView {

    private final GridPane root = new GridPane();

    private final TextField emailField = new TextField();
    private final Label messageLabel = new Label();

    private final Button sendButton = new Button("Envoyer le lien");
    private final Button backButton = new Button("Retour à la connexion");

    private final Hyperlink supportLink = new Hyperlink("Contactez le support");
    private final Hyperlink legalLink = new Hyperlink("Mentions Légales");

    // callbacks
    private Consumer<String> onSendResetLink; // email
    private Runnable onBackToLogin;
    private Runnable onContactSupport;
    private Runnable onOpenLegal;

    public ForgotPasswordView() {
        buildUi();
    }

    private void buildUi() {
        // ===== Root GridPane (comme FXML) =====
        root.setMinSize(400, 550);
        root.setPrefSize(400, 550);
        root.setStyle("-fx-background-color: #E5E5E5;");
        root.setPadding(new Insets(30, 40, 0, 40));
        root.setHgap(5);
        root.setVgap(20);

        ColumnConstraints col = new ColumnConstraints();
        col.setFillWidth(true);
        col.setHgrow(Priority.ALWAYS);
        col.setMinWidth(200);
        col.setPrefWidth(350);
        col.setHalignment(javafx.geometry.HPos.CENTER);
        root.getColumnConstraints().add(col);

        // ===== Logo (row 0) =====
        ImageView logo = new ImageView();
        logo.setFitHeight(130);
        logo.setFitWidth(180);
        logo.setPreserveRatio(true);
        logo.setPickOnBounds(true);
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/Logo_CryptoVault.png"));
            logo.setImage(img);
        } catch (Exception ignored) {
        }
        GridPane.setRowIndex(logo, 0);
        root.getChildren().add(logo);

        // ===== Titre (row 1) =====
        Text title = new Text("Mot de passe oublié");
        title.setFill(Color.web("#980b0b"));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrappingWidth(250);
        title.setFont(Font.font("System Bold", 24));
        GridPane.setRowIndex(title, 1);
        root.getChildren().add(title);

        // ===== Instruction (row 2) =====
        Text info = new Text("Entrez votre adresse email pour recevoir un lien de réinitialisation.");
        info.setFill(Color.web("#666666"));
        info.setTextAlignment(TextAlignment.CENTER);
        info.setWrappingWidth(270);
        info.setFont(Font.font(12));
        GridPane.setRowIndex(info, 2);
        GridPane.setMargin(info, new Insets(5, 0, 10, 0));
        root.getChildren().add(info);

        // ===== Champ email (row 3) =====
        emailField.setPromptText("votre.email@exemple.com");
        emailField.setPrefHeight(30);
        emailField.setPrefWidth(70);
        emailField.setEffect(makeShadow(
                0.5960784554481506, 0.04313725605607033, 0.04313725605607033,
                1.0,
                15.0, 7.0,
                2.0, 2.0,
                0.1
        ));
        GridPane.setRowIndex(emailField, 3);
        GridPane.setMargin(emailField, new Insets(10, 0, 0, 0));
        root.getChildren().add(emailField);

        // ===== Message (succès/erreur) (row 4) =====
        messageLabel.setText("");
        messageLabel.setWrapText(true);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setStyle("-fx-background-radius: 4; -fx-font-weight: bold;");
        GridPane.setRowIndex(messageLabel, 4);
        GridPane.setMargin(messageLabel, new Insets(10, 0, 0, 0));
        root.getChildren().add(messageLabel);

        // ===== Bouton Envoyer (row 5) =====
        sendButton.setDefaultButton(true);
        sendButton.setAlignment(Pos.CENTER);
        sendButton.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        sendButton.setPrefHeight(35);
        sendButton.setPrefWidth(200);
        sendButton.setTextFill(Color.WHITE);
        sendButton.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 4; -fx-cursor: hand;");
        sendButton.setFont(Font.font(14));
        sendButton.setOnAction(e -> triggerSendResetLink());
        GridPane.setRowIndex(sendButton, 5);
        GridPane.setHalignment(sendButton, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(sendButton, new Insets(10, 0, 0, 0));
        root.getChildren().add(sendButton);

        // ===== Bouton Retour (row 6) =====
        backButton.setAlignment(Pos.CENTER);
        backButton.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        backButton.setPrefHeight(35);
        backButton.setPrefWidth(200);
        backButton.setTextFill(Color.web("#980b0b"));
        backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: #980b0b; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 4; " +
                        "-fx-cursor: hand;"
        );
        backButton.setFont(Font.font(14));
        backButton.setOnAction(e -> triggerBackToLogin());
        GridPane.setRowIndex(backButton, 6);
        GridPane.setHalignment(backButton, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(backButton, new Insets(5, 0, 0, 0));
        root.getChildren().add(backButton);

        // ===== Texte aide (row 7) =====
        Text help = new Text("Si vous ne recevez pas d'email, vérifiez vos spams ou contactez le support.");
        help.setFill(Color.web("#666666"));
        help.setTextAlignment(TextAlignment.CENTER);
        help.setWrappingWidth(270);
        help.setFont(Font.font(10));
        GridPane.setRowIndex(help, 7);
        GridPane.setMargin(help, new Insets(30, 0, 0, 0));
        root.getChildren().add(help);

        // ===== Lien support (row 8) =====
        Text needHelp = new Text("Besoin d'aide ?");
        needHelp.setFill(Color.web("#666666"));
        needHelp.setFont(Font.font(12));

        supportLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        supportLink.setFont(Font.font(12));
        supportLink.setOnAction(e -> triggerContactSupport());

        HBox supportBox = new HBox(5, needHelp, supportLink);
        supportBox.setAlignment(Pos.CENTER);
        GridPane.setRowIndex(supportBox, 8);
        GridPane.setMargin(supportBox, new Insets(10, 0, 0, 0));
        root.getChildren().add(supportBox);

        // ===== Mentions légales (row 8 aussi) =====
        legalLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        legalLink.setFont(Font.font(12));
        legalLink.setOnAction(e -> triggerOpenLegal());
        GridPane.setRowIndex(legalLink, 8);
        GridPane.setHalignment(legalLink, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(legalLink, new Insets(0, 0, 20, 0));
        root.getChildren().add(legalLink);

        // Focus behavior (comme tes autres views)
        root.setOnMouseClicked(event -> root.requestFocus());
    }

    private DropShadow makeShadow(
            double r, double g, double b,
            double opacity,
            double width, double radius,
            double offsetX, double offsetY,
            double spread
    ) {
        DropShadow ds = new DropShadow();
        ds.setWidth(width);
        ds.setHeight(width);
        ds.setRadius(radius);
        ds.setOffsetX(offsetX);
        ds.setOffsetY(offsetY);
        ds.setSpread(spread);
        ds.setColor(new Color(r, g, b, opacity));
        return ds;
    }

    // =========================
    // Triggers
    // =========================
    private void triggerSendResetLink() {
        hideMessage();
        if (onSendResetLink != null) {
            onSendResetLink.accept(getEmail());
        }
    }

    private void triggerBackToLogin() {
        hideMessage();
        if (onBackToLogin != null) {
            onBackToLogin.run();
        }
    }

    private void triggerContactSupport() {
        if (onContactSupport != null) {
            onContactSupport.run();
        }
    }

    private void triggerOpenLegal() {
        if (onOpenLegal != null) {
            onOpenLegal.run();
        }
    }

    // =========================
    // API publique
    // =========================

    public Node getRoot() {
        return root;
    }

    public String getEmail() {
        String v = emailField.getText();
        return v == null ? "" : v.trim();
    }

    public void setEmail(String email) {
        emailField.setText(email == null ? "" : email);
    }

    public void setOnSendResetLink(Consumer<String> onSendResetLink) {
        this.onSendResetLink = onSendResetLink;
    }

    public void setOnBackToLogin(Runnable onBackToLogin) {
        this.onBackToLogin = onBackToLogin;
    }

    public void setOnContactSupport(Runnable onContactSupport) {
        this.onContactSupport = onContactSupport;
    }

    public void setOnOpenLegal(Runnable onOpenLegal) {
        this.onOpenLegal = onOpenLegal;
    }

    /**
     * Affiche un message de succès (style libre).
     */
    public void showSuccess(String message) {
        showMessage(message, true);
    }

    /**
     * Affiche un message d'erreur (style libre).
     */
    public void showError(String message) {
        showMessage(message, false);
    }

    public void setSendDisabled(boolean disabled) {
        sendButton.setDisable(disabled);
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message == null ? "" : message);

        // styles cohérents avec les autres vues
        if (success) {
            messageLabel.setStyle(
                    "-fx-background-color: #e6ffed; " +
                            "-fx-text-fill: #1b5e20; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8; " +
                            "-fx-background-radius: 4;"
            );
        } else {
            messageLabel.setStyle(
                    "-fx-background-color: #ffe5e5; " +
                            "-fx-text-fill: #b00000; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8; " +
                            "-fx-background-radius: 4;"
            );
        }

        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}
