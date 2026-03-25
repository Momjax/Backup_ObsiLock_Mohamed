package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.App;
import com.coffrefort.client.model.FileEntry;
import com.coffrefort.client.model.NodeItem;
import com.coffrefort.client.model.PagedFilesResponse;
import com.coffrefort.client.model.Quota;
import com.coffrefort.client.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.TableRow;
import com.coffrefort.client.controllers.ShareSuccessController;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Optional;

import com.coffrefort.client.config.AppProperties;
import com.coffrefort.client.util.UIDialogs;
import com.coffrefort.client.util.FileUtils;
import javafx.scene.Node;

public class MainController {

    //propriétés
    @FXML private TreeView<NodeItem> treeView;
    @FXML private TableView<FileEntry> table;
    @FXML private TableColumn<FileEntry, String> nameCol;
    @FXML private TableColumn<FileEntry, String> sizeCol;
    @FXML private TableColumn<FileEntry, String> dateCol;
    @FXML private TableColumn<FileEntry, Integer> versionCol;

    @FXML private ProgressBar quotaBar;
    @FXML private Label quotaLabel;
    private String quotaColor = "#5cb85c"; //=> pour la couleur persistante
    private boolean quotaStyleInitialized = false;
    private int quotaStyleRetries = 0;
    private static final int MAX_QUOTA_STYLE_RETRIES = 20;
    private Quota currentQuota;

    @FXML private Label userEmailLabel;
    @FXML private Label statusLabel;
    @FXML private Label fileCountLabel;

    @FXML private Button uploadButton;
    @FXML private Button shareButton;
    @FXML private Button deleteButton;
    @FXML private Button newFolderButton;
    @FXML private Button logoutButton;
    @FXML private Button returnToRootButton;
    @FXML private Button refreshQuotaButton;
    @FXML private Button gestionQuota;
    @FXML private Pagination pagination;
    @FXML private javafx.scene.control.ToggleButton themeToggleButton;
    @FXML private javafx.scene.image.ImageView logoView;

    private boolean isDarkTheme = true;


    private ApiClient apiClient;
    private Runnable onLogout;
    private ObservableList<FileEntry> fileList = FXCollections.observableArrayList();
    private NodeItem currentFolder;
    private App app;
    private String currentNameFolder;
    private String currentNameFile;

    private Stage mainStage;

    private static final int FILES_PER_PAGE = 10; //remettre à 20 ou 10 => pour modifier le limit!
    private int currentPage = 0; //=> pour garder la trace de la page actuelle
    private int totalFiles = 0;

    //méthodes

    @FXML
    private void initialize() {

        //préparation l'interface
        setupTable();
        setupTreeView();
        setupTreeViewRootContextMenu();

        //configuration la PageFactory (pagination) avant charger les données!
        pagination.setPageFactory(this::loadPage);
        pagination.setVisible(false);
        pagination.setManaged(false);

        // Initialiser le bouton de thème et le logo
        App.updateThemeButton(themeToggleButton);
        App.updateLogo(logoView);

        //mettre en place le listener
        // quand je clique sur un dossier => currentFolder <=> currentFolder= null
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                NodeItem node = newItem.getValue();

                if (node.getType() == NodeItem.NodeType.FOLDER){
                    currentFolder = node;
                    loadFiles(currentFolder);
                }
            }
        });

        refreshUI();
        loadData();

        // Initialiser l'état du bouton theme
        App.updateThemeButton(themeToggleButton);
    }

    @FXML
    private void handleGoToRoot() {
        currentFolder = null;
        treeView.getSelectionModel().clearSelection();
        loadFiles(null);
    }

    private void refreshUI() {

        //mise à jour compteur
        updateFileCount();

        //mise à jour le quota
        updateQuota();

        //mise à jour email d'utilisateur
        String email = AppProperties.get("auth.email");
        if(email != null && !email.isEmpty()){
            userEmailLabel.setText(email);
        }

        System.out.println("userEmail: " + userEmailLabel.getText());

        // pour garantir le styles inline => éviter le  CSS externe
        // label bold inline
        // on laisse le CSS gérer le quotaLabel
        quotaBar.setStyle("-fx-pref-height: 8px;");

        // IMPORTANT : on laisse JavaFX créer la skin, puis on stylise (avec retry)
        //progressbar => création des noeuds intern .track(fond), .bar(partie remplie)

        Platform.runLater(this::refreshQuotaBarStyleWithRetry);

        // Détection du plein écran pour augmenter la police (Futuriste !)
        Platform.runLater(() -> {
            if (treeView.getScene() != null && treeView.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) treeView.getScene().getWindow();
                stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        treeView.getScene().getRoot().setStyle("-fx-font-size: 18px;");
                    } else {
                        treeView.getScene().getRoot().setStyle("-fx-font-size: 16px;");
                    }
                });
            }
        });

        // Si la scene arrive / change -> restyle
        quotaBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                quotaStyleRetries = 0;
                Platform.runLater(this::refreshQuotaBarStyleWithRetry);
            }
        });

        // Si la skin change -> restyle
        quotaBar.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            quotaStyleRetries = 0;
            Platform.runLater(this::refreshQuotaBarStyleWithRetry);
        });

        // À chaque changement de progress, JavaFX peut reconstruire la bar -> restyle
        quotaBar.progressProperty().addListener((obs, oldV, newV) -> {
            Platform.runLater(this::refreshQuotaBarStyle);
        });

        // premier passage
        Platform.runLater(() -> {
            initQuotaBarStyleOnce();
            refreshQuotaBarStyle();
        });

        //masquer le bouton quota si pas admin
        if(gestionQuota != null){
            gestionQuota.setVisible(false);
            gestionQuota.setManaged(false);
        }
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        System.out.println("MainController - setApiClient() appelé, apiClient = " + (apiClient != null ? "OK" : "NULL"));
        System.out.println("MainController - Instance hashCode = " + this.hashCode());
    }

    public void setApp(App app){
        this.app = app;
        System.out.println("MainController - setApp() appelé, app = " + (app != null ? "OK" : "NULL"));
        System.out.println("MainController - Instance hashCode = " + this.hashCode());
    }

    public void setOnLogout(Runnable callback) {
        this.onLogout = callback;
    }

    public void setUserEmail(String email) {
        if (userEmailLabel != null) {
            userEmailLabel.setText(email);
        }
    }

    /**
     * mettre à jour les colonnes dans TableView
     */
    private void setupTable() {
        // Configuration des colonnes
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("updatedAtFormatted"));
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        
        // Centrer la version
        versionCol.setStyle("-fx-alignment: CENTER;");

        table.setItems(fileList);

        // Activer/désactiver les boutons selon la sélection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            shareButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        //clique sur une ligne
        table.setRowFactory(tv -> {
            TableRow<FileEntry> row = new TableRow<>();

            //menu contextuel par ligne
            ContextMenu contextMenu = new ContextMenu();

            MenuItem openItem = new MenuItem("👁  Ouvrir");
            openItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    handleOpenFile(file);
                }
            });

            MenuItem renameItem = new MenuItem("✏️  Renommer...");
            renameItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    openRenameFileDialog(file);
                }
            });

            MenuItem downloadItem = new MenuItem("📥  Télécharger");
            downloadItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    handleDownload(file);
                }
            });

//            au cas ou pour plus tard, si je veux changer...
//            MenuItem deleteItem = new MenuItem("Supprimer...");
//            deleteItem.setOnAction(e -> {
//                FileEntry file = row.getItem();
//                if (file != null) {
//                    table.getSelectionModel().select(file);
//                    handleDelete(); // réutilise ton flow confirmDelete.fxml
//                }
//            });

            MenuItem detailsItem = new MenuItem("📜  Historique & Versions");
            detailsItem.setOnAction(e -> {
                FileEntry file = row.getItem();
                if (file != null) {
                    openFileDetailsDialog(file);
                }
            });

            contextMenu.getItems().addAll(openItem, new SeparatorMenuItem(), renameItem, downloadItem, detailsItem);

            //affichage le menu => que si la ligne n'est pas vide
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            //ouvrir les détails d'un fichier en double cliquant dessus
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty()){
                    FileEntry selected = row.getItem();
                    handleOpenFile(selected);
                }
            });
            return row;
        });
    }

    /**
     * mise à jour : Listener sur le TreeView
     */
    private void setupTreeView() {

        // Style de l'arborescence
        treeView.setCellFactory(tv -> {
            TreeCell<NodeItem> cell = new TreeCell<>() {

                @Override
                protected void updateItem(NodeItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    setText("📁 " + item.getName());

                    // ne pas proposer suppression sur la racine virtuelle => id=0
                    if (item.getId() == 0) {
                        setContextMenu(null);
                    } else {
                        // afficher le menu au clique droite => setContextMenu()
                        // rendre le clique droit active
                        setContextMenu(createFolderContextMenu(this));
                    }
                }
                }
            };
            return cell;
        });
    }


    /**
     * création du menu contextuel pour un dossier donné
     * @param cell
     * @return
     */
    private ContextMenu createFolderContextMenu(TreeCell<NodeItem>  cell){
        ContextMenu menu = new ContextMenu();

        MenuItem createInside = new MenuItem("Nouveau dossier ici...");
        createInside.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            if (folder != null){
                openCreateFolderDialog(folder); // => parent = dossier cliqué
            }
        });

        MenuItem renameItem = new MenuItem("Renommer ce dossier");
        renameItem.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            if (folder != null){
                openRenameFolderDialog(folder);
            }
        });

        MenuItem shareItem = new MenuItem("Partager ce dossier");
        shareItem.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            if (folder != null){
                handleShareFolder(folder);
            }
        });

        MenuItem deleteItem = new MenuItem("Supprimer ce dossier...");
        deleteItem.setOnAction(event -> {
            NodeItem folder = cell.getItem();
            TreeItem<NodeItem> treeItem = cell.getTreeItem();

            if (folder != null && treeItem != null) {
                handleDeleteFolder(folder, treeItem);
            }
        });

        menu.getItems().addAll(createInside, renameItem, shareItem, new SeparatorMenuItem(), deleteItem);
        return menu;
    }


    /**
     * Gestion de comportement de la souris sur le TreeView
     * clic droit ou clic gauche
     */
    private void setupTreeViewRootContextMenu(){
        ContextMenu rootMenu = new ContextMenu();

        MenuItem createRootFolder = new MenuItem("Nouveau dossier à la racine...");
        createRootFolder.setOnAction(event -> openCreateFolderDialog(null));

        //ajoute le bouton au menu racine
        rootMenu.getItems().addAll(createRootFolder);

        //déclenchement que sur clic droit
        treeView.setOnContextMenuRequested(event -> {

            //détecter si la souris est sur une TreeCell => récupération du noeud -> texte, icône, cellule...
            Node node = event.getPickResult().getIntersectedNode();

            while ( node != null && !(node instanceof TreeCell) ) {
                node = node.getParent();
            }

            //clic droit sur un dossier => laisser le menu du dossier
            if(node instanceof TreeCell<?> cell && cell.getItem() !=null){
                // on fait rien=> "créer sous-dossier" / "supprimer dossier"
                return;
            }

            //sinon -> zone vide => afficher le menu racine
            rootMenu.show(treeView, event.getScreenX(), event.getScreenY());
            event.consume();

        });

        //on clique gauche dans le vide => déselectionne
        treeView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {  // => détection un clic gauche

                Node node = event.getPickResult().getIntersectedNode();
                while ( node != null && !(node instanceof TreeCell) ) {
                    node = node.getParent();
                }

                if (!(node instanceof TreeCell)){
                    treeView.getSelectionModel().clearSelection();
                    currentFolder = null;
                    fileList.clear();
                    updateFileCount();
                    statusLabel.setText("Aucun dossier séléctioné");
                }
            }
        });
    }


    /**
     * Chargement les données, l'arborescence
     */
    private void loadData() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                // Charger l'arborescence depuis l'API
                NodeItem root = apiClient.listRoot();

                Platform.runLater(() -> {
                    TreeItem<NodeItem> rootItem = buildTree(root);
                    treeView.setRoot(rootItem);

//                    Sélectionner le premier dossier si disponible
//                    il ne faut plus séléctionner automatiquement le premier dossier!!
//                    if (!rootItem.getChildren().isEmpty()) {
//                        TreeItem<NodeItem> first = rootItem.getChildren().get(0);
//                        treeView.getSelectionModel().select(first);
//                        currentFolder = first.getValue();
//
//                        // charge les fichiers du 1er dossier
//                        loadFiles(currentFolder);
//                    }

                    treeView.getSelectionModel().clearSelection();
                    currentFolder = null;
                    loadFiles(null);

                    //vider la table tant qu'aucun dossier n'est choisi
                    //fileList.clear();
                    updateFileCount();
                    statusLabel.setText("Données chargées");
                });

                // Charger les quotas avec endpoint
                //updateQuota();
//                Platform.runLater(() -> {
//                    statusLabel.setText("Données chargées");
//                });

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    UIDialogs.showError("Erreur de chargement", null, "Impossible de charger les données: " + e.getMessage());
                    statusLabel.setText("Erreur de chargement");
                });
            }
        }).start();
    }

    /**
     * Construction visuelle de l'arbre
     * @param node
     * @return
     */
    private TreeItem<NodeItem> buildTree(NodeItem node) {

        TreeItem<NodeItem> item = new TreeItem<>(node);
        item.setExpanded(true);

        for (NodeItem child : node.getChildren()) {
            item.getChildren().add(buildTree(child));
        }
        return item;
    }

    /**
     * charge une page de fichier => appelé par la Pagination quand user clique sur une page
     * @param pageIndex
     * @return
     */
    private Node loadPage(int pageIndex){
        currentPage = pageIndex;
        loadFiles(currentFolder, pageIndex);
        return new VBox(); //=> retourne un node vide => la table est déjà affichée
    }

    /**
     * Chargement des fichiers d'un dossier =>ok
     * @param folder
     */
    private void loadFiles(NodeItem folder, int page) {
        if (apiClient == null) return;

        statusLabel.setText("Chargement des fichiers ...");

        new Thread(() -> {
            try{
                Integer folderId = (folder != null) ? folder.getId() : null;
                int offset = page * FILES_PER_PAGE;

                PagedFilesResponse response = apiClient.listFilesPaginated(folderId, FILES_PER_PAGE, offset );
                //var files = apiClient.listFiles(folder.getId()); => sans pagination

                Platform.runLater(() -> {
                    fileList.setAll(response.getFiles());
                    totalFiles = response.getTotal();

                    //mise à jour la pagination
                    int totalPages = (int) Math.ceil((double) totalFiles / FILES_PER_PAGE);
                    pagination.setPageCount(Math.max(1, totalPages));
                    pagination.setCurrentPageIndex(page);

                   //afficher/ masquer la pagination
                    boolean showPagination = totalPages > 1;
                    pagination.setVisible(showPagination);
                    pagination.setManaged(showPagination);

                    updateFileCount();
                    statusLabel.setText("Fichier chargés");
                });

            }catch(Exception e){
                e.printStackTrace();
                Platform.runLater(() -> {
                    fileList.clear(); //vider en cas d'erreur
                    pagination.setVisible(false);
                    pagination.setManaged(false);
                    UIDialogs.showError("Erreur", null, "Impossible de charger les fichiers: " + e.getMessage());
                    statusLabel.setText("Erreur de chargement des fichiers");
                });
            }
        }).start();
    }

    /**
     * surcharge pour charger la première page
     * @param folder
     */
    private void loadFiles(NodeItem folder){
        loadFiles(folder, 0);
    }

    // à écrire!!!!
    private void updateFileCount() {

        int count = (fileList == null) ? 0 : fileList.size();

        if (fileCountLabel != null) {
            fileCountLabel.setText(count + " fichier" + (count > 1 ? "s" : ""));
        }
    }

// *****************************************   functions pour le quota   ****************************************

    private void initQuotaBarStyleOnce() {
        if (quotaStyleInitialized) return;
        quotaStyleInitialized = true;

        // Track (fond) : on le fixe une fois (sera réappliqué si skin change via refresh)
        var track = quotaBar.lookup(".track");  //=> cherche dans la ProgressBar le nœud interne CSS nommé .track
        if (track != null) {
            track.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 4px; -fx-background-insets: 0;");
        }
    }

    private void setQuotaColor(String hexColor) {
        quotaColor = hexColor;
        refreshQuotaBarStyleWithRetry();
    }

    private void refreshQuotaBarStyleWithRetry() {
        // Essayer d'appliquer, et si bar/track pas prêts, retenter quelques pulses
        if (!refreshQuotaBarStyle()) {
            if (quotaStyleRetries++ < MAX_QUOTA_STYLE_RETRIES) {
                Platform.runLater(this::refreshQuotaBarStyleWithRetry);
            } else {
                System.out.println("QuotaBar style: impossible de trouver .bar/.track après retries");
            }
        }
    }

    /**
     * @return true si .bar existe (style appliqué), false sinon
     */
    private boolean refreshQuotaBarStyle() {
        var track = quotaBar.lookup(".track");
        var bar = quotaBar.lookup(".bar");

        // si pas encore prêt, on ne fait rien
        if (track == null || bar == null) {
            return false;
        }

        track.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 4px; -fx-background-insets: 0;");
        bar.setStyle(
                "-fx-background-color: " + quotaColor + ";" +
                        "-fx-background-radius: 4px;" +
                        "-fx-background-insets: 0;"
        );
        return true;
    }

    @FXML
    private void handleUpdateQuotaAction() {
        updateQuota();
        statusLabel.setText("Quota mis à jour.");
    }

    /**
     * mettre à jour le quota
     */
    private void updateQuota() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                currentQuota = apiClient.getQuota();

                long used = currentQuota.getUsed();
                long max = currentQuota.getMax();

                Platform.runLater(() -> {
                    if (currentQuota == null) {
                        quotaBar.setProgress(0.0);
                        quotaLabel.setText("0 B / 0 B");

                        setQuotaColor("#d9534f"); //rouge
                        refreshQuotaBarStyleWithRetry();
                        return;
                    }

                    double ratio = currentQuota.getUsageRatio();
//                    if (ratio < 0) ratio = 0;
//                    if (ratio > 1) ratio = 1;

                    // progress
                    quotaBar.setProgress(ratio); // valeur entre 0 et 1

                    // texte et style via CSS classes
                    quotaLabel.setText(FileUtils.formatSize(used) + " / " + FileUtils.formatSize(max));
                    quotaLabel.getStyleClass().remove("quota-label-alert");
                    if (!quotaLabel.getStyleClass().contains("quota-label")) {
                        quotaLabel.getStyleClass().add("quota-label");
                    }

                    // couleur
                    if (ratio >= 0.9) {
                        quotaColor = "#ff4757"; // rouge néon
                        quotaLabel.getStyleClass().add("quota-label-alert");
                        statusLabel.setText("Quota atteint — upload bloqué");
                        uploadButton.setDisable(true);
                    }
                    else if (ratio >= 0.8) {
                        quotaColor = "#f0ad4e"; // orange
                        uploadButton.setDisable(false);
                    }
                    else{
                        quotaColor = "#2ecc71"; // vert émeraude standard
                        uploadButton.setDisable(false);
                    }

                    // restyle
                    quotaStyleRetries = 0;
                    refreshQuotaBarStyleWithRetry();
                });

            } catch (Exception e) {
                statusLabel.setText("Erreur lors du chargement du quota");
                e.printStackTrace();
                Platform.runLater(() -> {
                    quotaBar.setProgress(0.0);
                    quotaLabel.setText("Erreur quota");
                    quotaLabel.getStyleClass().remove("quota-label");
                    quotaLabel.getStyleClass().add("quota-label-alert");
                    quotaStyleRetries = 0;
                    refreshQuotaBarStyleWithRetry();
                });
            }
        }).start();
    }

    /**
     * Afficher le bouton de gestion quota si user est admin
     */
    public void checkAdminRole(){
        try{
            if (gestionQuota == null) return;
            
            //vérif si le rôle depuis le token ou API
            boolean isAdmin = apiClient.isAdmin();

            gestionQuota.setVisible(isAdmin);
            gestionQuota.setManaged(isAdmin);
        }catch (Exception e){
            if (gestionQuota != null) {
                gestionQuota.setVisible(false);
                gestionQuota.setManaged(false);
            }
        }
    }

    @FXML
    private  void handleQuota(){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/quotaManagement.fxml")
            );

            Scene scene = new Scene(loader.load());

            //récupération du contrôleur
            QuotaManagementController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(("Gestion des quotas"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(gestionQuota.getScene().getWindow());
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            controller.setApiClient(apiClient);
            controller.refreshNow();

            // mise à jour le quota après la fermeture de quotaManagement.fxml
            dialogStage.setOnHidden(event -> {
                updateQuota();
            });

            dialogStage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de gestion des quotas "+e.getMessage());
        }
    }



    // *****************************************   functions pour share  *************************
    /**
     * Gestion de share des fichiers =>ok
     */
    @FXML
    private void handleShare() {
        FileEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        shareButton.setDisable(true);

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/share.fxml")
            );

            VBox root =  loader.load();

            //récupération du contrôleur
            ShareController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(("Créer un lien de partage"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(shareButton.getScene().getWindow());

            //interdire de redimensionner  la fenêtre => taille fixe
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);
            controller.setItemName(selected.getName());

            //callback => quand user clique sur partage
            controller.setOnShare(data -> {
                statusLabel.setText("Partage en cours... ");

                new Thread(() -> {
                    try{
                        String url  = apiClient.shareFile(selected.getId(), data);

                        Platform.runLater(() -> {
                            statusLabel.setText("Lien: " + url);
                            showShareDialog(url);
                        });
                        System.out.println(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Partage", null,"Erreur " + e.getMessage());
                            statusLabel.setText("Erreur pendant le partage");
                        });
                    }
                }).start(); //lancement du Thread
            });

            //réactivation du bouton de partage
            dialogStage.setOnHidden(event -> shareButton.setDisable(false));

            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de partage "+e.getMessage());
            shareButton.setDisable(false);
        }
    }

    /**
     * Gestion de share des dossiers
     * @param folderNode
     * @throws Exception
     */
    private void handleShareFolder(NodeItem folderNode){
        if(folderNode == null) return;

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/share.fxml")
            );

            VBox root =  loader.load();

            //récupération du contrôleur
            ShareController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(("Créer un lien de partage"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());

            //interdire de redimensionner  la fenêtre => taille fixe
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);
            controller.setItemName(folderNode.getName());

            //controller.setIsFolder(true); => indiquer que c'est un folder....
            //désactiver allowVersions pour les dossiers

            //callback => quand user clique sur partage
            //callback => quand user clique sur partage
            controller.setOnShare(data -> {
                statusLabel.setText("Partage du dossier en cours... ");

                new Thread(() -> {
                    try{
                        String url  = apiClient.shareFolder(folderNode.getId(), data);

                        Platform.runLater(() -> {
                            statusLabel.setText("Lien: " + url);
                            showShareDialog(url);
                        });
                        System.out.println(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Partage", null,"Erreur " + e.getMessage());
                            statusLabel.setText("Erreur pendant le partage du dossier");
                        });
                    }
                }).start(); //lancement du Thread
            });
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de partage "+e.getMessage());
            shareButton.setDisable(false);
        }
    }


    /**
     * Afficher URL
     * @param url
     */
    private void showShareDialog(String url){

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coffrefort/client/shareSuccess.fxml"));
            VBox root = loader.load();
            
            ShareSuccessController controller = loader.getController();
            controller.setUrl(url);
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            if (shareButton != null && shareButton.getScene() != null) {
                stage.initOwner(shareButton.getScene().getWindow());
            }
            stage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showInfoUrl("Partage réussi", "Lien de partage généré", url);
        }
    }

    /**
     * gestion de "Mes partages"
     */
    @FXML
    private void handleOpenShares() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/myshares.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            MySharesController controller = loader.getController();


            Stage dialogStage = new Stage();
            dialogStage.setTitle("Mes partages");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            Scene scene = new Scene(root, 900, 600);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            controller.setApiClient(apiClient);
            controller.setStage(dialogStage);

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de myshares.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de Mes partages: " + e.getMessage());
        }

    }
    @FXML
    private void handleOpenTrash() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/trash.fxml")
            );

            VBox root = loader.load();
            TrashController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ma Corbeille — ObsiLock");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            Scene scene = new Scene(root, 1000, 700);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            controller.setApiClient(apiClient);
            controller.setStage(dialogStage);

            dialogStage.showAndWait();
            
            // Recharger la vue principale au cas où des éléments auraient été restaurés
            loadData();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de trash.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir la corbeille: " + e.getMessage());
        }
    }

    // *****************************************   functions pour file *************************

    /**
     * Gestion d'upload des fichiers =>ok
     */
    @FXML
    private void handleUpload() {

        if(currentQuota != null && currentQuota.getUsed() >= currentQuota.getMax()){
            UIDialogs.showError("Quota atteint",
                    null,
                    "Votre espace de stockage est plein. Veuillez supprimer des fichiers.");
            return;
        }

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/uploadDialog.fxml")
            );

            Parent root = loader.load();

            //récupération du contrôleur
            UploadDialogController controller = loader.getController();
            controller.setApiClient(apiClient);

            if(currentFolder != null){
                controller.setTargetFolderId(currentFolder.getId());
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Uploader des fichiers");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(uploadButton.getScene().getWindow());
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);

            //callback pour rafraîchir après upload
            controller.setOnUploadSuccess(() ->{
                Platform.runLater(() -> {
                    if(currentFolder != null){
                        loadFiles(currentFolder);
                    }

                    updateQuota();
                    statusLabel.setText("Upload terminé");
                });
            });

            dialogStage.showAndWait();

        }catch(Exception e){
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre d'upload "+e.getMessage());
        }
    }

    /**
     * ouvrir le dialog renameFile.fxml pour renommer un fichier =>ok
     * @param file
     */
    private void openRenameFileDialog(FileEntry file){
        if(file == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/renameFile.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            RenameFileController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Renommer le fichier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(treeView.getScene().getWindow()); => il est dans la table et pas dans treeView!!
            dialogStage.initOwner(table.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);


            dialogStage.setWidth(420);
            dialogStage.setHeight(400);

            controller.setStage(dialogStage);

            currentNameFile =file.getName();
            controller.setCurrentName(currentNameFile);

            controller.setOnConfirm(newName -> {

                //vérif si les 2 noms sont identiques
                if(newName.trim().equals(currentNameFile)){
                    Platform.runLater(() -> {
                        UIDialogs.showError("Renommer", null, "Le nouveau nom est identique à l'ancien.");
                        //dialogStage.close();
                    });
                    return;
                }

                statusLabel.setText("Renommage en cours...");
                new Thread(() -> {
                    try {
                        apiClient.renameFile(file.getId(), newName); //=> il faut id et name
                        Platform.runLater(() -> {
                            //dialogStage.close(); => si je laisse içi le texte de showInfo ne voit pas

                            if(currentFolder != null){
                                loadFiles(currentFolder);
                            }else{
                                loadData(); // => refresh tout
                            }
                            statusLabel.setText("Fichier renommé en \"" + newName + "\"");
                            statusLabel.setVisible(true);

                            UIDialogs.showInfo(
                                    "Renommage réussi",
                                    null,
                                    "Le fichier a été renommé en \"" + newName + "\"."
                            );
                            dialogStage.close();
                        });
                    } catch (IllegalArgumentException e) {
                        // erreur de validation
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de validation", null, e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            statusLabel.setVisible(true);
                            // pas fermer le dialogue
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de renommage", null,"Erreur: " + e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            statusLabel.setVisible(true);
                            // pas fermer le dialogue
                        });
                    }
                }).start();
            });

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de renameFile.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir renameFile.fxml: " + e.getMessage());
        }
    }

    /**
     * ouvrir le dialog fileDetails.fxml pour voir les versions d'un fichier =>ok
     * @param file
     */
    private void openFileDetailsDialog (FileEntry file){

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/fileDetails.fxml")
            );

            Parent root = loader.load();

            // Récupération du contrôleur
            FileDetailsController controller = loader.getController();
            controller.setApiClient(apiClient);
            controller.setFile(file);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detail - " + file.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(table.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            controller.setCurrentName(file.getName());

            controller.setOnVersionUploaded(() -> Platform.runLater(() -> {
                if(currentFolder != null) {
                    loadFiles(currentFolder);
                }
                updateQuota();
                statusLabel.setText("Version remplacée: " + file.getName());
            }));

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de fileDetails.fxml");
            e.printStackTrace();
            UIDialogs.showError("Detail fichier", null,"Impossible d'ouvrir la fenetre: " + e.getMessage());
        }
    }
    /**
     * gestion de suppression d'un fichier => ok
     */
    @FXML
    private void handleDelete() {
        FileEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        deleteButton.setDisable(true);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmDelete.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmDeleteController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la suppresion du fichier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(deleteButton.getScene().getWindow());
            Scene scene = new Scene(root);
            com.coffrefort.client.App.applyTheme(scene);
            dialogStage.setScene(scene);

            // Injection du stage et du nom de fichier
            controller.setDialogStage(dialogStage);

            // personnaliser pour fichier
            controller.setMessage("Voulez-vous vraiment supprimer ce fichier ?");
            controller.setFileName(selected.getName());

            //callbacks
            controller.setOnConfirm(() -> deleteFile(selected));
            controller.setOnCancel(() -> statusLabel.setText("Suppression annulé"));
            dialogStage.showAndWait();

        } catch (Exception e){
            System.err.println("Erreur lors du chargement de confirmDelete.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de suppression: "+e.getMessage());
        } finally {
            deleteButton.setDisable(false);
        }
    }

    /**
     * supprimer un file =>ok
     * @param file
     */
    private void deleteFile(FileEntry file) {
        if (file == null) {
            return;
        }
        statusLabel.setText("Suppression en cours...");

        //désactiver avant la suppression
        shareButton.setDisable(true);
        deleteButton.setDisable(true);

        new Thread(() -> {
            try {
                apiClient.permanentDeleteFile(file.getId());

                Platform.runLater(() -> {

                    fileList.remove(file);  // => ça n'enleve  que localement

                    if(currentFolder != null){ //=> recharger complètement le dossier
                        loadFiles(currentFolder);
                    }

                    //mise à jour le compteur et le quota
                    updateFileCount();
                    updateQuota();

                    //garder les boutons désactivés => pas de sélection
                    // Les boutons sont gérés par le listener de sélection

                    statusLabel.setText("Fichier supprimé: " + file.getName());

                    UIDialogs.showInfo("Suppression réussie",
                            null,
                            "Le fichier \"" + file.getName() + "\" a été supprimé."
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    //réactiver les boutons
                    shareButton.setDisable(false);
                    deleteButton.setDisable(false);

                    // Les boutons sont gérés par le listener de sélection

                    UIDialogs.showError("Erreur de suppression", null, "Erreur: " + e.getMessage());
                    statusLabel.setText("Erreur de suppression");
                });
            }
        }).start();
    }

    // *****************************************   functions pour download  *************************
    /**
     * gestion de téléchargement d'un fichier =>ok
     * @param file
     */
    private void handleDownload(FileEntry file) {

        if(file == null) return;

        FileChooser chooser = new FileChooser();

        //à choisir où enregistrer
        chooser.setTitle("Enregistrer le fichier...");

        // définir le nom => par défaut
        chooser.setInitialFileName(file.getName());

        //configuration automatique les filtres
        FileUtils.configureFileChooserFilter(chooser, file.getName());

        File target = chooser.showSaveDialog(table.getScene().getWindow());
        if (target == null){
            statusLabel.setText("Le téléchargement est annulé");
            return;
        }

        statusLabel.setText("Téléchargement de " + file.getName() + "...");

        new Thread(() -> {
            try {
                apiClient.downloadFileTo(file.getId(), target);

                Platform.runLater(() -> {
                    statusLabel.setText("Téléchargé " + target.getAbsolutePath());
                    updateQuota();

                    UIDialogs.showInfo(
                            "Téléchargement réussi",
                            null, "Le fichier a été téléchargé: \n"
                                    + target.getAbsolutePath()
                    );
                    
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(target);
                        }
                    } catch (Exception ex) {
                        System.err.println("Impossible d'ouvrir le fichier: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    UIDialogs.showError("Téléchargement échoué", null, "Impossible de télécharger: " + e.getMessage());
                    statusLabel.setText("Erreur de téléchargement");
                });
            }
        }).start();
    }

    // *****************************************   functions pour folders  *************************

    /**
     * Gestion de cas de "création d'un folder"
     */
    @FXML
    private void handleNewFolder() {
//        openCreateFolderDialog(currentFolder); // currentFolder peut être null => racine
        openCreateFolderDialog(null); //=> à la racine!!!
    }

    /**
     * Création d'un Folder
     * @param name
     */
    private void createFolder(String name, NodeItem parentFolder) {
        statusLabel.setText("Création du dossier...");

        new Thread(() -> {
            try {
                boolean success = apiClient.createFolder(name, parentFolder); //=> parentFolder peut être null

                Platform.runLater(() -> {
                    if (success) {
                        loadData(); // Recharger l'arborescence
                        statusLabel.setText("Dossier créé: " + name);
                    } else {
                        UIDialogs.showError("Erreur", null, "Impossible de créer le dossier.");
                        statusLabel.setText("Erreur de création");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    UIDialogs.showError("Erreur", null,"Erreur: " + e.getMessage());
                    statusLabel.setText("Erreur de création");
                });
            }
        }).start();
    }

    /**
     * ouvrir le dialog CreatFolder.fxml pour créer un dossier avec à la racine
     * @param parentFolder
     */
    private void openCreateFolderDialog(NodeItem parentFolder){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/createFolder.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            CreateFolderController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un nouveau dossier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            controller.setOnCreateFolder(name -> createFolder(name, parentFolder));

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de createFolder.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de création: " + e.getMessage());
        }
    }

    /**
     * ouvrir le dialog renameFolder.fxml pour renommer un dossier =>ok
     * @param folder
     */
    private void openRenameFolderDialog(NodeItem folder){
        if(folder == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/renameFolder.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            RenameFolderController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Renommer le dossier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));

            controller.setStage(dialogStage);
            currentNameFolder = folder.getName();
            controller.setCurrentName(currentNameFolder);

            controller.setOnConfirm(newName -> {

                //vérif si les 2 noms sont identiques
                if(newName.trim().equals(currentNameFolder)){
                    Platform.runLater(() -> {
                        UIDialogs.showError("Renommer", null , "Le nouveau nom est identique à l'ancien");
                        //dialogStage.close();
                    });
                    return;
                }

                statusLabel.setText("Renommage en cours...");

                new Thread(() -> {
                    try {
                        apiClient.renameFolder(folder.getId(), newName, currentNameFolder); //=> il faut id et name, currenNameFolder au cas ou

                        Platform.runLater(() -> {

                            loadData(); // => refresh Tree (arborescence
                            statusLabel.setText("Dossier renommé en \"" + newName + "\"");

                            UIDialogs.showInfo(
                                    "Renommage réussi",
                                    null,
                                    "Le dossier a été renommé en \"" + newName + "\"."
                            );

                            // il faut laisser içi!!! => sinon showInfo de renommage ne s'affiche pas!!
                            dialogStage.close(); //=> fermer si succès
                        });

                    } catch (IllegalArgumentException e) {
                        //Erreur de validation => nom vide, caractères invalides..
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de validation", null, e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            // pas fermer le dialogue
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            UIDialogs.showError("Erreur de renommage", null, "Erreur: " + e.getMessage());
                            statusLabel.setText("Erreur pendant le renommage");
                            // pas fermer le dialogue
                        });
                    }
                }).start();
            });

            dialogStage.showAndWait();

        }catch (Exception e){
            System.err.println("Erreur lors du chargement de renameFolder.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir renameFolder.fxml" + e.getMessage());
        }
    }



    /**
     * pour gérer la suppression d'un dossier =>ok
     * @param folder
     * @param treeItem => élément visuel dans le TreeView
     */
    private void handleDeleteFolder(NodeItem folder, TreeItem<NodeItem> treeItem){
        if(folder == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmDeleteFolder.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmDeleteFolderController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la suppresion du dossier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(treeView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Injection du stage et du nom de fichier
            controller.setDialogStage(dialogStage);
            controller.setFolderName(folder.getName());

            //callbacks
            controller.setOnConfirm(() -> deleteFolderOnServer(folder, treeItem));
            controller.setOnCancel(() -> statusLabel.setText("Suppression du dossier annulé"));
            dialogStage.showAndWait();

        } catch (Exception e){
            System.err.println("Erreur lors du chargement de confirmDeleteFolder.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir la fenêtre de suppression: "+e.getMessage());
        }

//        Optional<ButtonType> result = confirm.showAndWait();
//        if(result.isPresent() && result.get() == ButtonType.OK){
//            deleteFolderOnServer(folder, treeItem);
//        }else{
//            statusLabel.setText("Suppression du dossier annulée");
//        }
    }

    /**
     * supprimer le dossier sur le serveur (via API) + mise à jour l'affichage =>ok
     * @param folder
     * @param treeItem
     */
    private void deleteFolderOnServer(NodeItem folder, TreeItem<NodeItem> treeItem){
        if(folder == null) return;
        statusLabel.setText("Suppression du dossier en cours ...");

        new Thread(() -> {
            try{
                apiClient.permanentDeleteFolder(folder.getId());

                Platform.runLater(() -> {

                    //Si on est dans ce dossier => vider la table
                    if(currentFolder != null && currentFolder.getId() ==  folder.getId()){
                        fileList.clear();
                        updateFileCount();
                        currentFolder = null;
                    }

                    //recharger arborescence
                    loadData();
                    updateQuota();

                    statusLabel.setText("Dossier supprimé: " + folder.getName());

                    UIDialogs.showInfo(
                            "Suppression réussie",
                            null,
                            "Le dossier \"" + folder.getName() + "\" a été supprimé."
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    String errorMessage = e.getMessage();
                    if(errorMessage != null && errorMessage.contains("fichiers")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Le dossier contient des fichiers.\n" +
                                        "Veuillez d'abord supprimer tous les fichiers."
                        );
                    }else if(errorMessage != null && errorMessage.contains("sous-dossiers")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Le dossier contient des sous-dossiers.\n" +
                                        "Veuillez d'abord supprimer tous les sous-dossiers."
                        );
                    }else if(errorMessage != null && errorMessage.contains("introuvable")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Dossier introuvable ou déjà supprimé."
                        );
                    }else{
                        UIDialogs.showError(
                                "Erreur de suppression",
                                null,
                                "Erreur lors de la suppression du dossier.\n" +
                                        (errorMessage != null ? errorMessage : "Erreur inconnue")
                        );
                    }
                    statusLabel.setText("Erreur de suppression du dossier");
                });
            }
        }).start();
    }

    /*************************************** Logout *****************************************************

    /**
     * Gestion de déconnexion =>ok
     */
    @FXML
    private void handleLogout() {
        logoutButton.setDisable(true);

        System.out.println("MainController - handleLogout() appelé");
        System.out.println("MainController - Instance hashCode = " + this.hashCode());
        System.out.println("MainController - app is null ? " + (app == null));
        System.out.println("MainController - apiClient is null ? " + (apiClient == null));

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmLogout.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmLogoutController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la déconnexion");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(logoutButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Injection du stage et de la logique de déconnexion
            controller.setDialogStage(dialogStage);
            controller.setOnLogoutConfirmed(() -> {

                // Déconnexion (suppression du token)
                apiClient.logout();
                System.out.println("Déconnexion effectuée. Retour à l'écran de connexion...");

                //arrêter la surveillance de session
                SessionManager.getInstance().stopSessionMonitoring();

                // Fermer la fenêtre de dialogue AVANT de changer de scène
                dialogStage.close();

                // Utiliser Platform.runLater pour changer de scène de manière sûre
                Platform.runLater(() -> {
                    try {

//                        FXMLLoader loginLoader = new FXMLLoader(
//                                getClass().getResource("/com/coffrefort/client/login2.fxml")
//                        );
//                        Parent loginRoot = loginLoader.load();
//                        // Récupérer le contrôleur du login
//                        LoginController loginController = loginLoader.getController();
//                        // Injecter l'ApiClient existant
//                        loginController.setApiClient(apiClient);

                        // Récupérer la fenêtre principale (Stage)
                        Stage stage = (Stage)logoutButton.getScene().getWindow();

                        //appel la méthode openlogin de App
                        if(app != null){
                            System.out.println("MainController - Appel de app.openLogin()");

                            app.openLogin(stage); //Appel DIRECT, pas de callback

                            System.out.println("Redirection vers la page de connexion réussie.");
                        }else{
                            System.err.println("Erreur: App n'est pas injecté dans MainController");
                            UIDialogs.showError("Erreur", "Erreur de déconnexion", "Impossible de retourner à l'écran de connexion");
                        }

                        // Remplacer la scène par celle du login
//                        Scene loginScene = new Scene(loginRoot, 420, 600);
//                        stage.setTitle("Connexion - CryptoVault");
//                        stage.setScene(loginScene);
//                        stage.centerOnScreen();
//                        stage.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Erreur lors du chargement de login2.fxml");

                        // Afficher un message d'erreur à l'utilisateur

                        UIDialogs.showError("Erreur", "Erreur de déconnexion", "Impossible de charger l'écran de connexion." );
//                        Alert alert = new Alert(Alert.AlertType.ERROR);
//                        alert.setTitle("Erreur");
//                        alert.setHeaderText("Erreur de déconnexion");
//                        alert.setContentText("Impossible de charger l'écran de connexion.");
//                        alert.showAndWait();
                    }
                });
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de confirmLogout.fxml");

            // Afficher un message d'erreur
            UIDialogs.showError("Erreur", "Erreur de déconnexion", "Impossible de charger la fenêtre de confirmation.");

//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Erreur");
//            alert.setHeaderText("Erreur de déconnexion");
//            alert.setContentText("Impossible de charger la fenêtre de confirmation.");
//            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la déconnexion");
            e.printStackTrace();

        } finally {

            // Réactiver le bouton après fermeture du dialogue
            logoutButton.setDisable(false);
        }
    }
    /**
     * Ouvrir un fichier (télécharge dans temp et ouvre)
     * @param fileEntry
     */
    private void handleOpenFile(FileEntry fileEntry) {
        if (fileEntry == null) return;

        statusLabel.setText("Ouverture de " + fileEntry.getName() + "...");

        new Thread(() -> {
            try {
                // Créer un fichier temporaire
                String tempDir = System.getProperty("java.io.tmpdir");
                File tempFile = new File(tempDir, "obsilock_" + System.currentTimeMillis() + "_" + fileEntry.getName());
                
                apiClient.downloadFileTo(fileEntry.getId(), tempFile);

                Platform.runLater(() -> {
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(tempFile);
                            statusLabel.setText("Aperçu ouvert: " + fileEntry.getName());
                        } else {
                            UIDialogs.showError("Erreur", null, "L'ouverture automatique n'est pas supportée sur ce système.");
                        }
                    } catch (Exception ex) {
                        UIDialogs.showError("Erreur", null, "Impossible d'ouvrir le fichier : " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    UIDialogs.showError("Erreur", null, "Échec du téléchargement temporaire : " + e.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void handleToggleTheme() {
        if (table.getScene() != null) {
            App.toggleTheme(table.getScene());
            App.updateThemeButton(themeToggleButton);
            App.updateLogo(logoView);
        }
    }
}