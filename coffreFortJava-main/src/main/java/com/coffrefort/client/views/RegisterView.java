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
import javafx.scene.text.TextAlignment;

public class RegisterView {

    private final GridPane root = new GridPane();

    // Champs
    private final TextField emailField = new TextField();

    // Password masqué / visible
    private final PasswordField passwordField = new PasswordField();
    private final TextField passwordVisibleField = new TextField();

    // Confirm masqué / visible
    private final PasswordField confirmPasswordField = new PasswordField();
    private final TextField confirmPasswordVisibleField = new TextField();

    private final CheckBox showPasswordsCheckBox = new CheckBox("Afficher les mots de passe");

    // Messages
    private final Label errorLabel = new Label();
    private final Label successLabel = new Label();
    private final Label statusLabel = new Label();

    // Actions
    private final Button registerButton = new Button("S'inscrire");
    private final Hyperlink loginLink = new Hyperlink("Se connecter");
    private final Hyperlink legalLink = new Hyperlink("Mentions Légales");

    /** Callback (email, password, confirmPassword) */
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    private TriConsumer<String, String, String> onRegister;
    private Runnable onGoToLogin;
    private Runnable onOpenLegal;

    public RegisterView() {
        buildUi();
    }

    private void buildUi() {
        // Root
        root.setMinSize(380, 750);
        root.setPrefSize(380, 750);
        root.setStyle("-fx-background-color: #E5E5E5;");
        root.setPadding(new Insets(30, 40, 20, 40));
        root.setHgap(5);
        root.setVgap(15);

        ColumnConstraints col = new ColumnConstraints();
        col.setFillWidth(true);
        col.setHgrow(Priority.ALWAYS);
        col.setMinWidth(200);
        col.setPrefWidth(380);
        col.setHalignment(javafx.geometry.HPos.CENTER);
        root.getColumnConstraints().add(col);

        // =========================
        // Logo (row 0)
        // =========================
        ImageView logo = new ImageView();
        logo.setFitHeight(120);
        logo.setFitWidth(170);
        logo.setPreserveRatio(true);
        logo.setPickOnBounds(true);
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/images/Logo_CryptoVault.png")));
        } catch (Exception ignored) {}
        GridPane.setRowIndex(logo, 0);
        GridPane.setHalignment(logo, javafx.geometry.HPos.CENTER);
        root.getChildren().add(logo);

        // =========================
        // Titre (row 1)
        // =========================
        Text title = new Text("Inscription");
        title.setFill(Color.web("#980b0b"));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFont(Font.font("System Bold", 24));
        GridPane.setRowIndex(title, 1);
        GridPane.setHalignment(title, javafx.geometry.HPos.CENTER);
        root.getChildren().add(title);

        // =========================
        // Sous-titre (row 2)
        // =========================
        Text subtitle = new Text("Créez votre compte sécurisé");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setFont(Font.font(12));
        GridPane.setRowIndex(subtitle, 2);
        GridPane.setHalignment(subtitle, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(subtitle, new Insets(0, 0, 10, 0));
        root.getChildren().add(subtitle);

        // =========================
        // Email (row 3)
        // =========================
        VBox emailBox = new VBox(4);

        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 11px; -fx-font-weight: bold;");
        emailLabel.setPadding(new Insets(0, 0, 0, 2));

        emailField.setPromptText("votre.email@exemple.com");
        emailField.setPrefHeight(35);
        emailField.setStyle(
                "-fx-background-radius: 4;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-padding: 8;"
        );
        emailField.setEffect(makeSoftShadow());

        emailBox.getChildren().addAll(emailLabel, emailField);
        GridPane.setRowIndex(emailBox, 3);
        root.getChildren().add(emailBox);

        // =========================
        // Mot de passe (row 4)
        // =========================
        VBox pwdBox = new VBox(4);

        Label pwdLabel = new Label("Mot de passe");
        pwdLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 11px; -fx-font-weight: bold;");
        pwdLabel.setPadding(new Insets(0, 0, 0, 2));

        StackPane pwdStack = new StackPane();
        passwordField.setPromptText("Minimum 8 caractères");
        passwordField.setPrefHeight(35);
        passwordField.setStyle(
                "-fx-background-radius: 4;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-padding: 8;"
        );
        passwordField.setEffect(makeSoftShadow());

        passwordVisibleField.setPromptText("Minimum 8 caractères");
        passwordVisibleField.setPrefHeight(35);
        passwordVisibleField.setStyle(
                "-fx-background-radius: 4;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-padding: 8;"
        );
        passwordVisibleField.setEffect(makeSoftShadow());
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);

        // synchro texte
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        pwdStack.getChildren().addAll(passwordField, passwordVisibleField);

        pwdBox.getChildren().addAll(pwdLabel, pwdStack);
        GridPane.setRowIndex(pwdBox, 4);
        root.getChildren().add(pwdBox);

        // =========================
        // Confirmation (row 5)
        // =========================
        VBox confirmBox = new VBox(4);

        Label confirmLabel = new Label("Confirmation du mot de passe");
        confirmLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 11px; -fx-font-weight: bold;");
        confirmLabel.setPadding(new Insets(0, 0, 0, 2));

        StackPane confirmStack = new StackPane();
        confirmPasswordField.setPromptText("Retapez votre mot de passe");
        confirmPasswordField.setPrefHeight(35);
        confirmPasswordField.setStyle(
                "-fx-background-radius: 4;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-padding: 8;"
        );
        confirmPasswordField.setEffect(makeSoftShadow());

        confirmPasswordVisibleField.setPromptText("Retapez votre mot de passe");
        confirmPasswordVisibleField.setPrefHeight(35);
        confirmPasswordVisibleField.setStyle(
                "-fx-background-radius: 4;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 4;" +
                        "-fx-padding: 8;"
        );
        confirmPasswordVisibleField.setEffect(makeSoftShadow());
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);

        // synchro texte
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        confirmStack.getChildren().addAll(confirmPasswordField, confirmPasswordVisibleField);

        confirmBox.getChildren().addAll(confirmLabel, confirmStack);
        GridPane.setRowIndex(confirmBox, 5);
        root.getChildren().add(confirmBox);

        // =========================
        // Checkbox afficher mdp (row 6)
        // =========================
        HBox showRow = new HBox(8);
        showRow.setAlignment(Pos.CENTER_LEFT);

        showPasswordsCheckBox.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        showPasswordsCheckBox.setOnAction(e -> toggleShowPasswords(showPasswordsCheckBox.isSelected()));

        showRow.getChildren().add(showPasswordsCheckBox);

        GridPane.setRowIndex(showRow, 6);
        GridPane.setMargin(showRow, new Insets(5, 0, 0, 2));
        root.getChildren().add(showRow);

        // =========================
        // Erreur + succès (row 7)
        // =========================
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setTextAlignment(TextAlignment.CENTER);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setStyle(
                "-fx-background-color: #ffe5e5;" +
                        "-fx-text-fill: #b00000;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 4;"
        );

        successLabel.setAlignment(Pos.CENTER);
        successLabel.setTextAlignment(TextAlignment.CENTER);
        successLabel.setWrapText(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        successLabel.setStyle(
                "-fx-background-color: #d4edda;" +
                        "-fx-text-fill: #155724;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 4;"
        );

        GridPane.setRowIndex(errorLabel, 7);
        GridPane.setMargin(errorLabel, new Insets(2, 0, 0, 0));
        root.getChildren().add(errorLabel);

        GridPane.setRowIndex(successLabel, 7);
        GridPane.setMargin(successLabel, new Insets(2, 0, 0, 0));
        root.getChildren().add(successLabel);

        // =========================
        // Status (row 8)
        // =========================
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        statusLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-font-style: italic;");

        GridPane.setRowIndex(statusLabel, 8);
        GridPane.setMargin(statusLabel, new Insets(2, 0, 0, 0));
        root.getChildren().add(statusLabel);

        // =========================
        // Bouton inscription (row 9)
        // =========================
        registerButton.setDefaultButton(true);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(40);
        registerButton.setTextFill(Color.WHITE);
        registerButton.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;"
        );
        registerButton.setFont(Font.font("System Bold", 14));
        registerButton.setEffect(makeRegisterShadow());
        registerButton.setOnAction(e -> triggerRegister());

        GridPane.setRowIndex(registerButton, 9);
        GridPane.setMargin(registerButton, new Insets(3, 0, 0, 0));
        root.getChildren().add(registerButton);

        // =========================
        // Lien login (row 10)
        // =========================
        HBox loginRow = new HBox(5);
        loginRow.setAlignment(Pos.CENTER);

        Text already = new Text("Déjà inscrit ?");
        already.setFill(Color.web("#666666"));
        already.setFont(Font.font(12));

        loginLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        loginLink.setFont(Font.font(12));
        loginLink.setOnAction(e -> triggerGoToLogin());

        loginRow.getChildren().addAll(already, loginLink);

        GridPane.setRowIndex(loginRow, 10);
        GridPane.setMargin(loginRow, new Insets(5, 0, 0, 0));
        root.getChildren().add(loginRow);

        // =========================
        // Mentions légales (row 10 aussi)
        // =========================
        legalLink.setStyle("-fx-text-fill: #980b0b; -fx-cursor: hand;");
        legalLink.setFont(Font.font(12));
        legalLink.setOnAction(e -> triggerOpenLegal());

        GridPane.setRowIndex(legalLink, 10);
        GridPane.setHalignment(legalLink, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(legalLink, new Insets(0, 0, 20, 0));
        root.getChildren().add(legalLink);

        clearMessages();

        // Focus behavior (comme les autres views)
        root.setFocusTraversable(true);
        root.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            if (!(target instanceof TextField) && !(target instanceof PasswordField)) {
                root.requestFocus();
            }
        });
    }

    private void toggleShowPasswords(boolean show) {
        if (show) {
            passwordVisibleField.setManaged(true);
            passwordVisibleField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);

            confirmPasswordVisibleField.setManaged(true);
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordField.setManaged(false);
            confirmPasswordField.setVisible(false);
        } else {
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            passwordVisibleField.setManaged(false);
            passwordVisibleField.setVisible(false);

            confirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(true);
            confirmPasswordVisibleField.setManaged(false);
            confirmPasswordVisibleField.setVisible(false);
        }
    }

    private DropShadow makeSoftShadow() {
        DropShadow ds = new DropShadow();
        ds.setWidth(8.0);
        ds.setHeight(8.0);
        ds.setRadius(3.5);
        ds.setOffsetX(1.0);
        ds.setOffsetY(1.0);
        ds.setSpread(0.05);
        ds.setColor(Color.color(0, 0, 0, 0.15));
        return ds;
    }

    private DropShadow makeRegisterShadow() {
        DropShadow ds = new DropShadow();
        ds.setWidth(12.0);
        ds.setHeight(12.0);
        ds.setRadius(5.5);
        ds.setOffsetX(2.0);
        ds.setOffsetY(2.0);
        ds.setSpread(0.1);
        ds.setColor(Color.color(0.596, 0.043, 0.043, 0.3));
        return ds;
    }

    private void triggerRegister() {
        clearMessages();

        String email = getEmail();
        String password = getPassword();
        String confirm = getConfirmPassword();

        boolean hasError = false;

        if (email.isEmpty()) {
            showError("L'email est obligatoire.");
            hasError = true;
        } else if (!email.contains("@") || !email.contains(".")) {
            showError("Email invalide.");
            hasError = true;
        }

        if (password.isEmpty()) {
            showError("Le mot de passe est obligatoire.");
            hasError = true;
        } else if (password.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères.");
            hasError = true;
        }

        if (confirm.isEmpty()) {
            showError("Veuillez confirmer le mot de passe.");
            hasError = true;
        } else if (!confirm.equals(password)) {
            showError("Les mots de passe ne correspondent pas.");
            hasError = true;
        }

        if (hasError) return;

        if (onRegister != null) {
            // status "en cours"
            showStatus("Inscription en cours...");
            onRegister.accept(email, password, confirm);
        }
    }

    private void triggerGoToLogin() {
        if (onGoToLogin != null) onGoToLogin.run();
    }

    private void triggerOpenLegal() {
        if (onOpenLegal != null) onOpenLegal.run();
    }

    // =========================
    // API publique
    // =========================

    public Node getRoot() {
        return root;
    }

    public void setOnRegister(TriConsumer<String, String, String> onRegister) {
        this.onRegister = onRegister;
    }

    public void setOnGoToLogin(Runnable onGoToLogin) {
        this.onGoToLogin = onGoToLogin;
    }

    public void setOnOpenLegal(Runnable onOpenLegal) {
        this.onOpenLegal = onOpenLegal;
    }

    public String getEmail() {
        String v = emailField.getText();
        return v == null ? "" : v.trim();
    }

    public String getPassword() {
        String v = passwordField.getText();
        return v == null ? "" : v;
    }

    public String getConfirmPassword() {
        String v = confirmPasswordField.getText();
        return v == null ? "" : v;
    }

    public void clearMessages() {
        hideError();
        hideSuccess();
        hideStatus();
    }

    public void showError(String message) {
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        errorLabel.setText(message == null ? "" : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public void showSuccess(String message) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        successLabel.setText(message == null ? "" : message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
    }

    public void hideSuccess() {
        successLabel.setText("");
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    public void showStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    public void hideStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    public void setRegisterDisabled(boolean disabled) {
        registerButton.setDisable(disabled);
    }
}
