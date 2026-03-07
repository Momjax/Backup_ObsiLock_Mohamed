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

import java.util.function.BiConsumer;

public class LoginView {

    private final GridPane root = new GridPane();

    private final TextField emailField = new TextField();

    // Champ mot de passe masqué + champ visible (comme FXML)
    private final PasswordField passwordField = new PasswordField();
    private final TextField passwordVisibleField = new TextField();

    // Checkbox afficher mdp
    private final CheckBox loginSelectShowPassword = new CheckBox("Afficher le mot de passe");

    private final Label errorLabel = new Label();

    private final Hyperlink forgotPasswordLink = new Hyperlink("Mot de passe oublié?");
    private final Hyperlink registerLink = new Hyperlink("S'inscrire");
    private final Hyperlink legalLink = new Hyperlink("Mentions Légales");

    // bouton + status label (dans le FXML, statusLabel est “dans” le bouton)
    private final Button loginBtn = new Button("Se connecter");
    private final Label statusLabel = new Label("Prêt");

    // callback (email, password) → logique métier
    private BiConsumer<String, String> onLogin;

    // callback quand l'utilisateur clique sur "S'inscrire"
    private Runnable onGoToRegister;

    // callback quand l'utilisateur clique sur "Mentions Légales"
    private Runnable onOpenLegal;

    // optionnel : lien mdp oublié
    private Runnable onForgotPassword;

    public LoginView() {
        buildUi();
    }

    private void buildUi() {
        // ===== Root GridPane (comme FXML) =====
        root.setMinSize(350, 500);
        root.setPrefSize(350, 500);
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

        // rows “libres” (on n’a pas besoin de toutes les RowConstraints comme le FXML)
        // On place juste aux bons rowIndex.

        // ===== Logo (row 0) =====
        ImageView logo = new ImageView();
        logo.setFitHeight(130);
        logo.setFitWidth(180);
        logo.setPreserveRatio(true);
        logo.setPickOnBounds(true);
        try {
            // équivalent de @/images/Logo_CryptoVault.png
            Image img = new Image(getClass().getResourceAsStream("/images/Logo_CryptoVault.png"));
            logo.setImage(img);
        } catch (Exception ignored) {
            // si image absente, on laisse vide (évite crash)
        }
        GridPane.setRowIndex(logo, 0);
        root.getChildren().add(logo);

        // ===== Titre (row 2) =====
        Text title = new Text("Connexion");
        title.setFill(Color.web("#980b0b"));
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setWrappingWidth(150);
        title.setFont(Font.font("System Bold", 24));
        GridPane.setRowIndex(title, 2);
        root.getChildren().add(title);

        // ===== Champ email (row 4) =====
        emailField.setPromptText("votre.email@exemple.com");
        emailField.setPrefHeight(30);
        emailField.setPrefWidth(70);
        emailField.setEffect(makeShadow(
                0.5960784554, 0.0431372560, 0.0431372560,
                1.0, // opacity non précisée dans FXML => par défaut 1, mais le rendu est ok
                15.0, 7.0,
                2.0, 2.0
        ));
        GridPane.setRowIndex(emailField, 4);
        root.getChildren().add(emailField);

        // ===== Champ mdp (row 5) via StackPane =====
        StackPane pwdStack = new StackPane();
        GridPane.setRowIndex(pwdStack, 5);

        passwordField.setPromptText("Mot de passe");
        passwordField.setPrefHeight(30);
        passwordField.setPrefWidth(70);
        passwordField.setEffect(makeShadow(
                0.5960784554, 0.0431372560, 0.0431372560,
                1.0,
                15.0, 7.0,
                2.0, 2.0
        ));

        passwordVisibleField.setPromptText("Mot de passe");
        passwordVisibleField.setPrefHeight(30);
        passwordVisibleField.setPrefWidth(70);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setEffect(makeShadow(
                0.5960784554, 0.0431372560, 0.0431372560,
                1.0,
                15.0, 7.0,
                2.0, 2.0
        ));

        // sync des contenus
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        pwdStack.getChildren().addAll(passwordField, passwordVisibleField);
        root.getChildren().add(pwdStack);

        // ===== Checkbox + forgot link (row 6) =====
        HBox optionsRow = new HBox(10);
        optionsRow.setAlignment(Pos.CENTER_LEFT);
        GridPane.setRowIndex(optionsRow, 6);

        loginSelectShowPassword.setMnemonicParsing(false);
        loginSelectShowPassword.setOnAction(e -> toggleShowPassword());

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        forgotPasswordLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        forgotPasswordLink.setOnAction(e -> {
            if (onForgotPassword != null) onForgotPassword.run();
        });

        optionsRow.getChildren().addAll(loginSelectShowPassword, push, forgotPasswordLink);
        root.getChildren().add(optionsRow);

        // ===== Zone erreur (row 7) =====
        errorLabel.setText("");
        errorLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errorLabel.setStyle(
                "-fx-background-color: #ffe5e5; " +
                        "-fx-text-fill: #b00000; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 4;"
        );
        GridPane.setRowIndex(errorLabel, 7);
        root.getChildren().add(errorLabel);

        // ===== Bouton connexion (row 8) =====
        loginBtn.setDefaultButton(true);
        loginBtn.setAlignment(Pos.CENTER);
        loginBtn.setContentDisplay(ContentDisplay.CENTER);
        loginBtn.setPrefHeight(29);
        loginBtn.setPrefWidth(115);
        loginBtn.setTextFill(Color.WHITE);
        loginBtn.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 4; -fx-cursor: hand;");
        loginBtn.setFont(Font.font(14));
        loginBtn.setOnAction(e -> triggerLogin());

        // Dans le FXML, statusLabel est un enfant du Button.
        // Pour garder ce rendu => mettre un graphic.
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        loginBtn.setGraphic(statusLabel);

        GridPane.setRowIndex(loginBtn, 8);
        root.getChildren().add(loginBtn);

        // ===== Register row (row 12) =====
        Text question = new Text("Vous n'êtes pas inscrit(e) ?");
        question.setFill(Color.web("#666666"));
        question.setFont(Font.font(12));

        registerLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        registerLink.setFont(Font.font(12));
        registerLink.setOnAction(e -> triggerGoToRegister());

        HBox registerBox = new HBox(5, question, registerLink);
        registerBox.setAlignment(Pos.CENTER);
        GridPane.setRowIndex(registerBox, 12);
        root.getChildren().add(registerBox);

        // ===== Mentions légales (row 12 aussi) =====
        legalLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        legalLink.setFont(Font.font(12));
        legalLink.setOnAction(e -> triggerOpenLegal());
        GridPane.setRowIndex(legalLink, 12);
        GridPane.setMargin(legalLink, new Insets(0, 0, 20, 0));
        root.getChildren().add(legalLink);

        // Focus behavior
        root.setOnMouseClicked(event -> root.requestFocus());
    }

    private void toggleShowPassword() {
        boolean show = loginSelectShowPassword.isSelected();

        if (show) {
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            passwordVisibleField.requestFocus();
            passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);

            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    private DropShadow makeShadow(
            double r, double g, double b,
            double opacity,
            double width, double radius,
            double offsetX, double offsetY
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

    private void triggerLogin() {
        if (onLogin != null) {
            showError("");
            onLogin.accept(emailField.getText(), getPassword());
        }
    }

    private void triggerGoToRegister() {
        if (onGoToRegister != null) {
            onGoToRegister.run();
        }
    }

    private void triggerOpenLegal() {
        if (onOpenLegal != null) {
            onOpenLegal.run();
        }
    }

    private String getPassword() {
        // les deux sont bindBidirectional, donc n’importe lequel convient
        return passwordField.getText();
    }

    // ===== API publique =====

    public void setOnLogin(BiConsumer<String, String> onLogin) {
        this.onLogin = onLogin;
    }

    public void setOnGoToRegister(Runnable onGoToRegister) {
        this.onGoToRegister = onGoToRegister;
    }

    public void setOnOpenLegal(Runnable onOpenLegal) {
        this.onOpenLegal = onOpenLegal;
    }

    public void setOnForgotPassword(Runnable onForgotPassword) {
        this.onForgotPassword = onForgotPassword;
    }

    public void showError(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    // optionnel pour afficher un état (ex: “Connexion…”)
    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            statusLabel.setText("");
        } else {
            statusLabel.setText(status);
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        }
    }

    public Node getRoot() {
        return root;
    }
}
