package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmLogoutController {

    //propriétés
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Label infoLabel;
    @FXML private Label infoLabel1;

    private Stage dialogStage;
    private Runnable onLogoutConfirmed;

   //Méthodes
    @FXML
    /**
     * Initialise les textes par défaut des labels si aucun texte n’est déjà défini dans le FXML.
     */
    private void initialize() {
        if (infoLabel != null && (infoLabel.getText() == null || infoLabel.getText().isBlank())) {
            infoLabel.setText("Voulez-vous vraiment vous déconnecter ?");
        }

        if (infoLabel1 != null && (infoLabel1.getText() == null || infoLabel1.getText().isBlank())) {
            infoLabel1.setText("Toutes les opérations en cours seront interrompues.");
        }
    }


    /**
     * Injecte le Stage de la fenêtre modale pour permettre sa fermeture depuis le contrôleur
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


    /**
     * Définit le callback exécuté lorsque l’utilisateur confirme la déconnexion
     * @param onLogoutConfirmed
     */
    public void setOnLogoutConfirmed(Runnable onLogoutConfirmed) {
        this.onLogoutConfirmed = onLogoutConfirmed;
    }


    @FXML
    /**
     * Gère le clic sur “Annuler” en loggant l’action et en fermant la fenêtre de confirmation
     */
    private void handleCancel() {
        System.out.println("Déconnexion annulée par l'utilisateur.");
        if (dialogStage != null) {
            dialogStage.close();
        }
    }


    @FXML
    /**
     * Gère le clic sur “Confirmer” en exécutant le callback de déconnexion puis en fermant la fenêtre (même en cas d’erreur)
     */
    private void handleConfirm() {
        try {
            System.out.println("Confirmation de la déconnexion...");

            // Exécuter la logique de déconnexion
            if (onLogoutConfirmed != null) {
                onLogoutConfirmed.run();
            } else {
                System.err.println("ATTENTION: onLogoutConfirmed est null !");
                // Fermer quand même la fenêtre
                if (dialogStage != null) {
                    dialogStage.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la confirmation de déconnexion");
            e.printStackTrace();

            // Fermer la fenêtre même en cas d'erreur
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }
}
