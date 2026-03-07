package com.coffrefort.client.controllers;

import com.coffrefort.client.model.UserQuota;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.PagedShareResponse;
import com.coffrefort.client.model.ShareItem;
import javafx.scene.control.Pagination;
import com.coffrefort.client.util.UIDialogs;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MySharesController {

    @FXML private TableView<ShareItem> sharesTable;
    @FXML private Pagination pagination;
    @FXML private TableColumn<ShareItem, String> resourceCol;
    @FXML private TableColumn<ShareItem, String> labelCol;
    @FXML private TableColumn<ShareItem, String> expiresCol;
    @FXML private TableColumn<ShareItem, String> remainingCol;
    @FXML private TableColumn<ShareItem, String> statusCol;
    @FXML private TableColumn<ShareItem, Void> actionCol; // Changé de Object à Void

    private ApiClient apiClient;
    private Stage stage;

    private static final int PAGE_SIZE = 4; //=> à modifier si je veux changer la limit => remettre à 10 ou 20...!
    private int totalShares = 0;
    private int currentPage = 0; //=> pour garder la trace de la page actuelle
    //private boolean paginationInitialized = false; =>j'utilise quand la pagination fix (il reste visible dans la fenêtre)

    @FXML
    private void initialize() {
        System.out.println("MySharesController - initialize() appelée");

        //configuration la PageFactory (pagination) avant charger les données!
        pagination.setPageFactory(this::loadPage);
        pagination.setVisible(false);
        pagination.setManaged(false);

        //config des colonnes simples
        resourceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getResource()));

        labelCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLabel() != null ? data.getValue().getLabel() : "-"));

        //colonne d'expiration
        expiresCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getExpiresAt()));

        expiresCol.setCellFactory(col -> new TableCell<ShareItem, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText(null);
                    setStyle("");
                }else{
                    setText(item);

                    //coloriser selon expiration
                    ShareItem share = getTableView().getItems().get(getIndex());
                    long daysLeft = share.getDaysUntilExpiration();

                    if(share.isExpired()){
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold");
                    }else if(daysLeft > 0 && daysLeft <=3){
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold");
                    }else{
                        setStyle("");
                    }
                }
            }
        });
        centerColumn(expiresCol);

        //color Restant
        remainingCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRemainingText()));

        remainingCol.setCellFactory(col -> new TableCell<ShareItem, String>(){

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText(null);
                    setStyle("");
                }else{
                    setText(item);

                    //coloriser selon nbre restant
                    ShareItem share = getTableView().getItems().get(getIndex());
                    Integer remaining = share.getRemainingUses();

                    if(remaining != null){
                        if(remaining == 0){
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold");
                        }else if(remaining <= 3){
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold");
                        }else{
                            setStyle("-fx-text-fill: orange");
                        }
                    }else{
                        setStyle("");
                    }
                }
            }
        });
        centerColumn(remainingCol);


        //status
        statusCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        statusCol.setCellFactory(col -> new TableCell<ShareItem, String>() {

            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setStyle("");
                }else{
                    setText(item);

                    //coloriser
                    switch (item) {
                        case "Actif":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold");
                            break;
                        case "Expiré":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold");
                            break;
                        case "Révoqué":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold");
                            break;
                        case "Quota atteint":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold");
                            break;
                        default:
                            setStyle("");

                    }
                }
            }
        });
        centerColumn(statusCol);
    }

    private <T> void centerColumn(TableColumn<ShareItem, T> col) {
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

    public void setApiClient(ApiClient apiClient) {
        System.out.println("MySharesController - setApiClient() appelée");
        this.apiClient = apiClient;
        initActionColumn();
        //loadShares(); //sans pagination
        loadSharesPage(0); //=> charger la première page
    }

    /**
     * Charge les partages depuis l'API sans pagination => ne pas supprmier
     */
//    private void loadShares() {
//        System.out.println("MySharesController - loadShares() démarrage...");
//
//        try {
//            var shares = apiClient.listShares();
//
//            System.out.println("MySharesController - Nombre de partages reçus: " + shares.size());
//
//            // Debug: afficher chaque partage
//            for (int i = 0; i < shares.size(); i++) {
//                ShareItem share = shares.get(i);
//                System.out.println("Partage " + i + ": " +
//                        "id=" + share.getId() +
//                        ", resource=" + share.getResource() +
//                        ", label=" + share.getLabel() +
//                        ", status=" + share.getStatus() +
//                        ", expires=" + share.getExpiresAt() +
//                        ", remaining=" + share.getRemainingUses() +
//                        ", revoked=" + share.isRevoked());
//            }
//
//            sharesTable.getItems().setAll(shares);
//            System.out.println("MySharesController - Données ajoutées à la table");
//
//        } catch (Exception e) {
//            System.err.println("MySharesController - ERREUR lors du chargement: " + e.getMessage());
//
//            e.printStackTrace();
//            UIDialogs.showError("Erreur", null, "Impossible de charger les partages: " + e.getMessage());
//        }
//    }

    /**
     * charger une page de partage
     * @param page
     */
    private void loadSharesPage(int page){
        if(apiClient == null) return;
        System.out.println("MySharesController - loadSharesPage(" + page + ")");

        new Thread(() -> {
            try{
                int offset = page * PAGE_SIZE;

                //appel API avec pagination
                PagedShareResponse response = apiClient.listShares(PAGE_SIZE, offset);

                Platform.runLater(()->{
                    var shares = response.getShares();
                    sharesTable.getItems().setAll(shares);
                    System.out.println("MySharesController - " + shares.size() + " partages chargés");

                    totalShares = response.getTotal();

                    //mise à jour la pagination
                    int totalPages = (int) Math.ceil((double) totalShares / PAGE_SIZE);
                    pagination.setPageCount(Math.max(1, totalPages));
                    pagination.setCurrentPageIndex(page);

                    //afficher/ masquer la pagination
                    boolean showPagination = totalPages > 1;
                    pagination.setVisible(showPagination);
                    pagination.setManaged(showPagination);

                    System.out.println("MySharesController - Total: " + totalShares +
                            ", Pages: " + totalPages +
                            ", Page actuelle: " + (page + 1));
                });
            } catch (Exception e) {
                 e.printStackTrace();
                 Platform.runLater(()->{
                     sharesTable.getItems().clear();
                     pagination.setVisible(false);
                     pagination.setManaged(false);
                     UIDialogs.showError("Erreur", null, "Impossible de charger les partages: " + e.getMessage());
                     System.err.println("MySharesController - ERREUR lors du chargement: " + e.getMessage());
                 });
            }
        }).start();
    }

    /**
     * charge une page de version d'un fichier => appelé par la Pagination quand user clique sur une page
     * @param pageIndex
     * @return
     */
    private Node loadPage(int pageIndex){
        currentPage = pageIndex;
        loadSharesPage(pageIndex);
        return new VBox(); //=> retourne un node vide => la table est déjà affichée
    }

    /**
     * initialise la colonne Actions avec les boutons
     */
    private void initActionColumn() {

        actionCol.setCellFactory(col -> new TableCell<ShareItem, Void>() {

            private final Button copyBtn = new Button("📋");
            private final Button revokeBtn = new Button("🚫");
            private final Button deleteBtn = new Button("🗑"); //❌
            private final HBox box = new HBox(6,  copyBtn, revokeBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER); //????

                //style les bouton
                copyBtn.setStyle("-fx-background-color: #b00909; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 14px");
                revokeBtn.setStyle("-fx-background-color: #980b0b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 14px");
                deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 16px");

                //tooltip => quand je survole??
                copyBtn.setTooltip( new Tooltip("Copier le lien de partage"));
                revokeBtn.setTooltip( new Tooltip("Révoquer ce partage"));
                deleteBtn.setTooltip( new Tooltip("Supprimer ce partage"));

                // action => copier le lien
                copyBtn.setOnAction(event -> {
                    ShareItem item = getTableView().getItems().get(getIndex());
                    copyShareLink(item);
                });

                // action => révoquer
                revokeBtn.setOnAction(event -> {
                    ShareItem item = getTableView().getItems().get(getIndex());
                    revokeShare(item);
                });

                //action => supprimer
                deleteBtn.setOnAction(event -> {
                    ShareItem item = getTableView().getItems().get(getIndex());
                    deleteShare(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    ShareItem share = getTableView().getItems().get(getIndex());

                    //désactive le btn révoquer s'il est déjà récoqué
                    revokeBtn.setDisable(share.isRevoked());
                    setGraphic(box);
                }
            }
        });
    }

    /**
     * copie le lien de partage dans le presse-papier
     * @param item
     */
    private void copyShareLink(ShareItem item) {

       String url = item.getUrl();
        if(url == null || url.isBlank()) {
            UIDialogs.showError("Erreur", null, "Url de partage introuvable");
            return;
        }

        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(url);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

        UIDialogs.showInfo("Succès",null,  "Lien copié dans le presse-papier : \n" + url);
    }

    /**
     * révoquer une partage =>ok
     * @param item
     */
    private void revokeShare(ShareItem item) {
        boolean confirmed = UIDialogs.showConfirmation(
                "Confirmer la révocation",
                "Révoquer le partage de : " + item.getResource(),
                "Voulez-vous vraiment révoquer ce partage ? \nLe lien ne sera plus accessible."
        );

        if(!confirmed) return;
        new Thread(() -> {
            try {
                apiClient.revokeShare(item.getId());

                Platform.runLater(() -> {
                    //màj item localement => ca ne va pas!!
//                    item.setRevoked(true);
//                    sharesTable.refresh();

                    //recharger la page depuis le serveur!!
                    loadSharesPage(currentPage);
                    UIDialogs.showInfo("Succès", null, "Le partage a été révoqué.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->{
                    UIDialogs.showError("Erreur", null, "Impossible de révoquer le partage: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * supprimer le partage =>ok
     * @param item
     */
    private  void deleteShare(ShareItem item) {
        boolean confirmed = UIDialogs.showConfirmation(
                "Confirmer la suppression",
                "Supprimer le partage de : " + item.getResource(),
                "Voulez-vous vraiment supprimer ce partage ? \nCette action est irréversible."
        );

        if(!confirmed) return;

        //en cas de supprime le dernière élément de la page, retourner à la page précédente
        //dernière élément de la page (et pas la page 1)=> aller à la page précédente
        int itemsOnPage = sharesTable.getItems().size();
        int nextPage = (itemsOnPage == 1 && currentPage > 0) ? currentPage -1 : currentPage;

        new Thread(() -> {
            try{
                apiClient.deleteShare(item.getId());

                Platform.runLater(()->{
                    //recharger depuis le serveur la page calculée
                    loadSharesPage(nextPage);
                    UIDialogs.showInfo("Succès", null, "Le partage a été supprimé.");
                });
                //retirer de la table localement => ca ne va pas!!!
//                sharesTable.getItems().remove(item);

            }catch(Exception e){
                e.printStackTrace();
                Platform.runLater(()->{
                    UIDialogs.showError("Erreur", null, "Impossible de supprimer le partage: " + e.getMessage());
                });
            }
        }).start();
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

