package com.coffrefort.client.controllers;

import com.coffrefort.client.config.AppProperties;
import com.coffrefort.client.ApiClient;
import com.coffrefort.client.App;
import com.coffrefort.client.util.JwtUtils;
import com.coffrefort.client.util.UIDialogs;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import com.coffrefort.client.ApiClient;
import javafx.fxml.FXML;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.coffrefort.client.util.JsonUtils;


public class RegisterController {

    //propriétés
    @FXML private GridPane rootPane;
    @FXML private TextField emailField1;
    @FXML private PasswordField passwordField1;
    @FXML private TextField passwordVisibleField1;
    @FXML private PasswordField confirmPasswordField1;
    @FXML private TextField confirmPasswordVisibleField1;

    @FXML private CheckBox loginSelectShowPassword;

    @FXML private Label emailError1;
    @FXML private Label passwordError1;
    @FXML private Label confirmPasswordError1;
    @FXML private Label errorLabel1;
    @FXML private Label successLabel1;

    @FXML private Button registerButton1;
    @FXML private Button themeToggleButton;
    @FXML private javafx.scene.image.ImageView logoView;
    @FXML private Label statusLabel1;
    @FXML private Hyperlink mentionsLegales;

    private ApiClient apiClient;

    private Runnable onRegisterSuccess;
    private Runnable onGoToLogin;


    //méthodes
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setOnRegisterSuccess(Runnable onRegisterSuccess) {
        this.onRegisterSuccess = onRegisterSuccess;
    }

    public void setOnGoToLogin(Runnable onGoToLogin) {
        this.onGoToLogin = onGoToLogin;
    }


    @FXML //=> il faut qu'il soit lier avec le .fxml!!
    private void initialize(){

        //on clique sur la scène => retirer le focus du champs
        rootPane.setOnMouseClicked(event -> {
            if(event.getTarget() != emailField1 && event.getTarget() != passwordField1 && event.getTarget() != confirmPasswordField1 ){
                rootPane.requestFocus();
            }
        });

        // Binder le texte des 2 champs mot de passe => avoir le même texte
        passwordVisibleField1.textProperty().bindBidirectional(passwordField1.textProperty());
        confirmPasswordVisibleField1.textProperty().bindBidirectional(confirmPasswordField1.textProperty());

        //lier le lien à "mentions légales"
        mentionsLegales.setOnAction(event -> handleGoToMentionsLegalesFromRegister());

        App.updateThemeButton(themeToggleButton);
        App.updateLogo(logoView);
    }

    @FXML
    private void handleToggleTheme() {
        if (rootPane.getScene() != null) {
            App.toggleTheme(rootPane.getScene());
            App.updateThemeButton(themeToggleButton);
            App.updateLogo(logoView);
        }
    }

    /**
     * Afficher ou masquer le mot de passe
     */
    @FXML
    private void handleToggleShowPassword(){
        if(loginSelectShowPassword.isSelected()){

            //afficher le mot de passe et le confirmation de mot de passe
            passwordVisibleField1.setVisible(true);
            passwordVisibleField1.setManaged(true);
            confirmPasswordVisibleField1.setVisible(true);
            confirmPasswordVisibleField1.setManaged(true);

            //cacher le password et le mot de passe
            passwordField1.setVisible(false);
            passwordField1.setManaged(false);
            confirmPasswordField1.setVisible(false);
            confirmPasswordField1.setManaged(false);

            //garder le curseur à la fin
            passwordVisibleField1.requestFocus();
            passwordVisibleField1.positionCaret(passwordVisibleField1.getText().length());


        }else{
            //cacher le mot de passe et confirmation de mot de passe
            passwordField1.setVisible(true);
            passwordField1.setManaged(true);
            confirmPasswordField1.setVisible(true);
            confirmPasswordField1.setManaged(true);

            //cacher les champs de texte
            passwordVisibleField1.setVisible(false);
            passwordVisibleField1.setManaged(false);
            confirmPasswordVisibleField1.setVisible(false);
            confirmPasswordVisibleField1.setManaged(false);

            //garder le curseur à la fin
            passwordField1.requestFocus();
            passwordField1.positionCaret(passwordField1.getText().length());
        }
    }

    /**
     * Gestion de l'Inscription et connexion: l'un après l'autre
     */
    @FXML
    public void handleRegister(){

        System.out.println("handleRegister() appele !");
        clearAllErrors();


        String email = emailField1.getText() != null ? emailField1.getText().trim() : "";
        String password = passwordField1.getText() != null ? passwordField1.getText().trim() : "";
        String confirmPassword = confirmPasswordField1.getText() != null ? confirmPasswordField1.getText().trim() : "";

        //validation côté Client
        if (email.isEmpty()) {
            showError("Veuillez saisir votre email.");
            return;
        }

        //Validation format email
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showError("Format d'email invalide.");
            return;
        }

        //Validation password (12 min, 1 maj, 1 spé)
        if(password.length() < 12 || !password.matches(".*[A-Z].*") || !password.matches(".*[^a-zA-Z0-9].*")){
            showError("Le mot de passe doit comporter au moins 12 caractères,\n" +
                    "avec au moins 1 majuscule et 1 caractère spécial.");
            return;
        }

        //Validation la confirmation de password
        if(confirmPassword.isEmpty()){
            showError("Veuillez confirmer le mot de passe.");
            return;
        }

        if(!confirmPassword.equals(password)){
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if(loginSelectShowPassword.isSelected()){
            showError("Veuillez masquer le mot de passe avant de vous inscrire.");
            return;
        }

        //appel API

        // Désactiver le bouton pendant l'inscription
        if(registerButton1 != null) {
            registerButton1.setDisable(true);
        }

        if(statusLabel1 != null) { //=> ???????????
            statusLabel1.setText("Inscription en cours...");
            statusLabel1.setVisible(true);
        }

        //int quotaTotal = 31457280; // 30 Mo par défaut pour tests
        int quotaTotal = 1073741824; //=> 1 giga
        //Boolean isAdmin = false;     //=> pas admin => backend qui décide!!!


        //Task pour appel HTTP en arrière-plan
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {

                try{
                    // Appel à ApiClient.register()
                    return apiClient.register(email, password, quotaTotal);
                }catch (Exception e){
                    updateMessage(e.getMessage());
                    throw e;
                }
            }
        };

        //après la requête terminée => succès ou erreur HTTP
        task.setOnSucceeded(event -> {
            if(registerButton1 != null) {
                registerButton1.setDisable(false);
            }

            String token = task.getValue(); //échec => null
            if(token != null && !token.isEmpty()){

                //Inscription et login => ok
                if(statusLabel1 != null) {
                    statusLabel1.setText("Inscription réussie ! Connexion automatique...");
                }

                showSuccess("Bienvenue !  Vous êtes maintenant connecté(e).");

                if(onRegisterSuccess != null) {
                    onRegisterSuccess.run(); //=> changer de scène/ fenêtre
                }
            }else{

                // échec côté API (inscription ou login)
                if(statusLabel1 != null) {
                    statusLabel1.setText("");
                    statusLabel1.setVisible(false);
                }

                String apiMessage = task.getMessage();  // message ou null
                String errorMessage = (apiMessage != null && !apiMessage.isEmpty())
                        ? apiMessage
                        : "Erreur lors de l'inscription/connexion.";

                showError(errorMessage);
            }
        });

        task.setOnFailed(event -> {
            if(registerButton1 != null) {
                registerButton1.setDisable(false);
            }
            if(statusLabel1 != null) {
                statusLabel1.setText("");
                statusLabel1.setVisible(false);
            }
            
            //récup exception
            Throwable ex = task.getException();
            String errorMessage = "Erreur pendant l'inscription.";
            
            if (ex != null) {
                ex.printStackTrace();
                
                //extraire le msg erreur de l"exception
                String exMessage = ex.getMessage();
                if (exMessage != null && !exMessage.isEmpty()) {
                    errorMessage = exMessage;
                }
                
                if(exMessage != null) {
                    if(errorMessage.toLowerCase().contains("email")){
                        
                        //erreur lié au email
                        if(emailError1 != null) {
                            emailError1.setText(errorMessage);
                            emailError1.setVisible(true);
                            emailError1.setManaged(true);
                        }else{
                            showError(errorMessage);
                        }
                    } else if (errorMessage.toLowerCase().contains("password") || errorMessage.toLowerCase().contains("mot de passe")) {

                        //erreur lié au password
                        if(passwordError1 != null) {
                            passwordError1.setText(errorMessage);
                            passwordError1.setVisible(true);
                            passwordError1.setManaged(true);
                        }else{
                            showError(errorMessage);
                        }
                    }else{
                        //erreur général
                        showError(errorMessage);
                    }
                }else{
                    showError(errorMessage);
                }
            }else{
                showError(errorMessage);
            }
        });

        //lancer le Task en arrière-plan
        new Thread(task).start();
    }


    /**
     * Gestion du lien de "se connecter"
     */
    @FXML
    public void handleGoToLogin(){
        openLogin();
    }

    //méthode intern => pour revenir à l'écran Login
    public void openLogin(){
        if(onGoToLogin != null){
            onGoToLogin.run();
        }
    }

    private void handleGoToMentionsLegalesFromRegister(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/mentionsLegales.fxml"));

            Scene scene = new Scene(loader.load());
            com.coffrefort.client.App.applyTheme(scene);

            // Récupération du contrôleur
            MentionsLegalesController controller = loader.getController();
            Stage stage = (Stage)mentionsLegales.getScene().getWindow();
            stage.setTitle("CryptoVault - Mentions Légales");

            stage.setResizable(false);
            stage.setScene(scene);

            controller.setDialogStage(stage);
            controller.setApiClient(apiClient);

            //Transmettre les callbacks
            controller.setOnSuccess(onRegisterSuccess);
            controller.setOnGoToRegister(onGoToLogin);

            stage.setHeight(700);
            stage.setWidth(750);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,
                    "Impossible d'ouvrir la page de mentions légales : " + e.getMessage());
        }
    }


    //Méthodes utilitaires

    // nettoyer tous les messages d'erreur et de statut
    public void clearAllErrors(){
        hideLabel(emailError1);
        hideLabel(passwordError1);
        hideLabel(confirmPasswordError1);
        hideLabel(errorLabel1);
        hideLabel(successLabel1);
    }

    public void hideLabel(Label label){
        if(label == null) return;
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    //affiche un message d'eereur dans le label principal
    public void showError(String message){
        if(errorLabel1 != null){
            errorLabel1.setText(message);
            errorLabel1.setVisible(true);
            errorLabel1.setManaged(true);
        }
    }

    //affiche un message succes
    public void showSuccess(String message){
        if(successLabel1 != null){
            successLabel1.setText(message);
            successLabel1.setVisible(true);
            successLabel1.setManaged(true);
        }
    }


//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//
//    }
}
