package com.coffrefort.client;

import com.coffrefort.client.controllers.LoginController;
import com.coffrefort.client.controllers.MainController;
import com.coffrefort.client.controllers.RegisterController;
import com.coffrefort.client.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Mini client lourd JavaFX d'exemple pour le projet « Coffre‑fort numérique ».
 * Objectif pédagogique: fournir une base exécutable, simple à lire, sur laquelle
 * les étudiants peuvent s'appuyer pour intégrer de vrais appels REST.
 */
public class App extends Application {

    //private final ApiClient apiClient = new ApiClient(); avant implementation de SessionManager
    private ApiClient apiClient;


    //Connexion
    @Override
    public void start(Stage stage) throws Exception {
        this.apiClient = new ApiClient();

        //config de SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setApiClient(apiClient);
        sessionManager.setOnSessionExpired(() -> {

            //rediriger vers connexion
            openLogin(stage);
        });

        stage.setTitle("Coffre‑fort numérique — Mini client");
        openLogin(stage);
    }


    /**
     * ÉCRAN CONNEXION
     * @param stage
     */
    public void openLogin(Stage stage) {
        try {

            //arrêter la session quand on retourne au login
            SessionManager.getInstance().stopSessionMonitoring();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/login2.fxml"));

            // Controller factory pour injecter ApiClient et callbacks
            loader.setControllerFactory(type -> {
                if (type == LoginController.class) {
                    LoginController c = new LoginController();
                    c.setApiClient(apiClient);

                    // Après connexion réussie → tableau de bord
                    c.setOnSuccess(() -> openMainAndClose(stage));

                    // Clique sur "S'inscrire" → ouvrir l'écran d'inscription
                    c.setOnGoToRegister(() -> openRegister(stage));

                    return c;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 450, 650);

            stage.setScene(scene); //avec ça le stage reste 1024x640
            stage.setTitle("Coffre-fort numérique — Connexion");

            //il faut redimensionner!! sinon il prend la taille de main.fxml
            stage.setWidth(450);
            stage.setHeight(650);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger login2.fxml", e);
        }
    }


    /**
     * ÉCRAN INSCRIPTION
     * @param stage
     */
    public void openRegister(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/register.fxml"));

            loader.setControllerFactory(type -> {
                if (type == RegisterController.class) {
                    RegisterController c = new RegisterController();
                    c.setApiClient(apiClient);

                    // Après inscription réussie → retour à l'écran de login
                    c.setOnRegisterSuccess(() -> openMainAndClose(stage));

                    // Clique sur "Se connecter" → retour à l'écran de login
                    c.setOnGoToLogin(() -> openLogin(stage));

                    return c;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 420, 680);

            stage.setScene(scene);  //avec ça le stage reste 1024x640
            stage.setTitle("Coffre-fort numérique — Inscription");

            //il faut redimensionner!!
            stage.setWidth(420);
            stage.setHeight(680);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger register.fxml", e);
        }
    }


    /**
     * TABLEAU DE BORD
     *  * @param loginStage
     * Solution avec data URI pour inclure le CSS directement
     */
    private void openMainAndClose(Stage loginStage) {
        try {
            System.out.println("App - Chargement de main.fxml...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/main.fxml"));
            Parent root = loader.load();  //le controller est créé

            MainController controller = loader.getController();
            controller.setApiClient(apiClient);
            controller.setApp(this);  //passer App en référence au MainController
            controller.checkAdminRole();
            //loader.setController(controller);

            System.out.println("App - Controller configuré, chargement du root...");

            //Parent root = loader.load();  //le controller est créé
            System.out.println("App - Configuration de la scène...");
//            Stage mainStage = new Stage();
//            mainStage.setTitle("Coffre‑fort — Espace personnel");

            //réutiliser le stage existant à la place de créer un nouveau
            Scene scene = new Scene(root, 1024, 640);
            loginStage.setScene(scene);

            loginStage.setTitle("Coffre‑fort — Espace personnel");

            loginStage.setWidth(1024);
            loginStage.setHeight(640);
            loginStage.setResizable(true);
            loginStage.centerOnScreen();
            loginStage.show(); // important afficher la fenetre??????? elotte commentben volt es ment

            // Fermer la fenêtre de login => avec ça je n'arriva pas ouvrir la vue de connexion
            //loginStage.close();

            //démarrer la surveillance de session après connexion réussi
            SessionManager.getInstance().startSessionMonitoring();

            System.out.println("App - Interface principale affichée");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger main.fxml", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
