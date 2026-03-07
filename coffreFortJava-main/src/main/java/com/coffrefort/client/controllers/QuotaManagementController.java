package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.UserQuota;
import com.coffrefort.client.model.VersionEntry;
import com.coffrefort.client.util.FileUtils;
import com.coffrefort.client.util.UIDialogs;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.naming.AuthenticationException;

public class QuotaManagementController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button modifyQuotaButton;
    @FXML private Button closeButton;
    @FXML private Label infoLabel;

    @FXML private TableView<UserQuota> usersTable;
    @FXML private TableColumn<UserQuota, Integer> idCol;
    //@FXML private TableColumn<UserQuota, String> usernameCol;
    @FXML private TableColumn<UserQuota, String> emailCol;
    @FXML private TableColumn<UserQuota, String> quotaUsedCol;
    @FXML private TableColumn<UserQuota, String> quotaMaxCol;
    @FXML private TableColumn<UserQuota, String> percentCol;
    @FXML private TableColumn<UserQuota, String> roleCol;

    private final ObservableList<UserQuota> userList = FXCollections.observableArrayList();

    private ApiClient apiClient;
    private Stage dialogStage;

    @FXML
    private void initialize() {

        //Table setup
        setupUsersTable();

        usersTable.setItems(userList);

        modifyQuotaButton.setDisable(true);
        // activer le bouton "modifier" => si une ligne est séléctionné
        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            modifyQuotaButton.setDisable(newValue == null);
        });

        //double clic pour modifier le quota => si ca marche avec l'autre il faut supprimer ça
//        usersTable.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2 && usersTable.getSelectionModel().getSelectedItem() != null) {
//                handleModifyQuota();
//            }
//        });
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void refreshNow(){
        usersTable.setItems(userList);
        handleRefresh();
    }

    private <T> void centerColumn(TableColumn<UserQuota, T> col) {
        col.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                }else{
                    setText(item.toString());
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    /**
     * Configure les colonnes de la table des users et le double-clic pour modifier le quota
     */
    private void setupUsersTable(){
        //config des colonnes
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        quotaUsedCol.setCellValueFactory(new PropertyValueFactory<>("quotaUsed"));
        quotaMaxCol.setCellValueFactory(new PropertyValueFactory<>("quotaMax"));
        percentCol.setCellValueFactory(new PropertyValueFactory<>("percent"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        centerColumn(idCol);
        centerColumn(quotaUsedCol);
        centerColumn(quotaMaxCol);
        centerColumn(percentCol);
        centerColumn(roleCol);

        //clique sur la ligne
        usersTable.setRowFactory(tv -> {
            TableRow<UserQuota> row = new TableRow<>();

            //menu contextuel par ligne
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteUser = new MenuItem("Supprimer cet utilisateur...");
            deleteUser.setOnAction(e -> {
                UserQuota user = row.getItem();
                if (user != null) {
                    usersTable.getSelectionModel().select(user);
                    handleDeleteUser();
                }
            });

            contextMenu.getItems().addAll(deleteUser);

            //affichage le menu => que si la ligne n'est pas vide
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            //double clic pour modifier le quota
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty()) {
                    handleModifyQuota();
                }
            });

            return row;
        });
    }


    /**
     * charge la liste de user depuis l'API
     */
    @FXML
    private void handleRefresh(){
        if(apiClient == null){
            showError("ApiClient non initialisé");
            return;
        }

        refreshButton.setDisable(true);
        showInfo("Chargement en cours...");

        new Thread(()->{

            try{
                //appel api pour récuperer tous les users avec leurs quotas
                java.util.List<UserQuota> users = apiClient.getAllUsersWithQuota();

                Platform.runLater(()->{
                    userList.clear();
                    userList.addAll(users);

                    refreshButton.setDisable(false);
                    hideInfo();
                    usersTable.setItems(userList);
                    if(users.isEmpty()){
                        showInfo("Aucun utilisateur trouvé");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    refreshButton.setDisable(false);
                    hideInfo();
                    UIDialogs.showError("Erreur", null, "Impossible de charger les utilisateurs " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Recherche d'utilisateurs par email
     */
    @FXML
    private void handleSearch(){
        String query = searchField.getText();
        if(query == null || query.trim().isEmpty()){
            usersTable.setItems(userList);
            hideInfo();
            return;
        }

        String lowerQuery = query.toLowerCase();
        ObservableList<UserQuota> filtered = userList.filtered(user ->
                //user.getName().toLowerCase().contains(lowerQuery) ||
                user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)
        );

        usersTable.setItems(filtered);

        if(filtered.isEmpty()){
            showInfo("Aucun résultat pour : " + query);
        } else {
            showInfo(filtered.size() + "résultat(s) trouvé(s)");
        }
    }

    /**
     * ouvre le dialogue pour modfier le quota d'un user
     */
    @FXML
    private void handleModifyQuota(){
        UserQuota selected = usersTable.getSelectionModel().getSelectedItem();
        if(selected == null){
            return;
        }

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/modifyQuota.fxml")
            );

            Scene scene = new Scene(loader.load());

            // Récupération du contrôleur
            ModifyQuotaController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Modifier le quota");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(dialogStage);
            stage.setResizable(false);
            stage.setScene(scene);

            controller.setDialogStage(stage);
            controller.setApiClient(apiClient);
            controller.setUser(selected);

            controller.setOnSuccess(() -> {
                //rafraichir après modif
                refreshNow();
            });

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir le dialogue" + e.getMessage());
        }
    }

    private void handleDeleteUser(){
        UserQuota selected = usersTable.getSelectionModel().getSelectedItem();
        if(selected == null){
            return;
        }

        String currentEmail = apiClient.getCurrentUserEmail();
        if(currentEmail != null && currentEmail.equals(selected.getEmail())){
            UIDialogs.showError("Action interdite", null, "Vous ne pouvez pas supprimer votre propre compte");
            return;
        }

        // confirmer avant de le supprimer
        boolean confirmed = UIDialogs.showConfirmation(
            "Supprimer l'utilisateur",
            "Êtes-vous sûr de vouloir supprimer cet utilisateur ?",
            "Cette action est irréversible.\n" +
                    "TOUTES les données de " + selected.getEmail() + " seront définitivement supprimées.\n" +
                    "• Fichiers et versions\n" +
                    "• Dossiers\n" +
                    "• Partages\n" +
                    "• Logs de téléchargement\n\n" +
                    "Conformité RGPD : suppression totale des données."
        );

        if(!confirmed){
            return;
        }

        modifyQuotaButton.setDisable(true);
        refreshButton.setDisable(true);
        searchButton.setDisable(true);
        showInfo("Suppression en cours...");

        new Thread(()->{
            try{
                //appel API
                String summary = apiClient.deleteUser(selected.getId());

                Platform.runLater(()->{

                    //afficher le résummé
                    UIDialogs.showInfo("Suppression réussi", null, summary);

                    refreshNow();
                    modifyQuotaButton.setDisable(false);
                    refreshButton.setDisable(false);
                    searchButton.setDisable(false);
                    hideInfo();
                });
            }catch (AuthenticationException e){
                Platform.runLater(() ->{
                    UIDialogs.showError("Accès refusé", null, e.getMessage());
                    modifyQuotaButton.setDisable(false);
                    refreshButton.setDisable(false);
                    searchButton.setDisable(false);
                    hideInfo();
                });

            }catch (Exception e){
                e.printStackTrace();
                UIDialogs.showError("Erreur", null, "Impossible de supprimer l'utilisateur " + e.getMessage());
                modifyQuotaButton.setDisable(false);
                refreshButton.setDisable(false);
                searchButton.setDisable(false);
                hideInfo();
            }
        }).start();
    }

    @FXML
    private void handleClose(){
        if(dialogStage != null){
            dialogStage.close();
        }
    }

    private void showInfo(String message){
        infoLabel.setText(message);
        infoLabel.setVisible(true);
        infoLabel.setManaged(true);
    }

    private void hideInfo(){
        infoLabel.setVisible(false);
        infoLabel.setManaged(false);
    }

    private void showError(String message){
        UIDialogs.showError("Erreur", null, message);
    }





}
