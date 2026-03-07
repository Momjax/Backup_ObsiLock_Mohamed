package com.coffrefort.client.controllers;


import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.FileEntry;
import com.coffrefort.client.model.PagedVersionsResponse;
import com.coffrefort.client.model.VersionEntry;
import com.coffrefort.client.util.UIDialogs;
import com.coffrefort.client.util.FileUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.util.List;

public class FileDetailsController {

    @FXML private Label fileNameLabel;
    @FXML private Label fileMetaLabel;

    @FXML private Button replaceButton;

    @FXML private VBox progressBox;
    @FXML private Label uploadStatusLabel;
    @FXML private ProgressBar uploadProgressBar;
    @FXML private Label errorLabel;
    @FXML private Label versionsCountLabel;

    @FXML private TableView<VersionEntry> versionsTable;
    @FXML private Pagination pagination;
    @FXML private TableColumn<VersionEntry, Number> versionCol;
    @FXML private TableColumn<VersionEntry, String> sizeCol;
    @FXML private TableColumn<VersionEntry, String> dateCol;
    @FXML private TableColumn<VersionEntry, String> checksumCol;

    @FXML private Button copyChecksumButton;
    @FXML private Button openLocalFolderButton;
    @FXML private Button downloadVersionButton;

    private final ObservableList<VersionEntry> versions = FXCollections.observableArrayList();

    private ApiClient apiClient;
    private FileEntry file;
    private Stage stage;

    private static final int PAGE_SIZE = 4; //=> à modifier si je veux changer la limit
    private int totalVersions = 0;
    private int currentPage = 0; //=> pour garder la trace de la page actuelle

    private Runnable onVersionUploaded;

    private Service<Void> uploadService;

    // pour garder une “trace” locale des téléchargements pour activer "ouvrir dossier local"
    // ObservableMap pour que les bindings JavaFX se mettent à jour
    // Map key = "fileId:v{versionNumber}" -> downloaded file path
    private final ObservableMap<String, Path> downloadedPaths = FXCollections.observableHashMap();

    @FXML
    /**
     * Initialise la table des versions, configure les bindings des boutons et prépare l’UI
     */
    private void initialize(){
        System.out.println("FileDetailsController - initialize() appelée");

        //configuration la PageFactory (pagination) avant charger les données!
        pagination.setPageFactory(this::loadPage);
        pagination.setVisible(false);
        pagination.setManaged(false);

        //Table setup
        setupVersionTable();

        versionsTable.setItems(versions);

        rebinButtons();

        //pour assurer qu'il n'y a pas binding
        progressBox.visibleProperty().unbind();
        progressBox.managedProperty().unbind();

        // le progress UI caché au début
        setProgressVisible(false);

        //utilisateur change de sélection => effacer les messages d'erreur
        versionsTable.getSelectionModel().selectedItemProperty().addListener((obs, v, n) -> hideError());

    }

    /**
     * charge une page de version d'un fichier => appelé par la Pagination quand user clique sur une page
     * @param pageIndex
     * @return
     */
    private Node loadPage(int pageIndex){
        currentPage = pageIndex;
        loadVersions(pageIndex);
        return new VBox(); //=> retourne un node vide => la table est déjà affichée
    }


    /**
     * Injecte l’ApiClient utilisé pour charger les versions et effectuer les uploads/téléchargements
     * @param apiClient
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        maybeRefresh();
    }

    /**
     * Injecte le Stage courant afin d’ouvrir des FileChooser et gérer la fenêtre.
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void updateTitle(){
        if(stage != null && file != null){
            stage.setTitle("Detail - " + file.getName());
        }
    }

    /**
     * Définit le fichier courant dont les versions sont affichées
     * @param file
     */
    public void setFile(FileEntry file) {
        this.file = file;
        maybeRefresh();
    }

    /**
     * Met à jour le label affichant le nom du fichier courant
     * @param name
     */
    public void setCurrentName(String name){
        if(fileNameLabel != null){
            fileNameLabel.setText(name != null ? name : "");
        }
    }

    /**
     * Définit le callback exécuté après l’upload réussi d’une nouvelle version
     * @param onVersionUploaded
     */
    public void setOnVersionUploaded(Runnable onVersionUploaded) {
        this.onVersionUploaded = onVersionUploaded;
    }

    //refresh quand apiclient et file sont injectés!!
    private void maybeRefresh() {
        if(this.apiClient != null && this.file != null) {
            hydrateThenRefresh();
        }
    }

    private void hydrateThenRefresh(){
        if(apiClient == null || file == null) return;

        versionsCountLabel.setText("Chargement...");

        new Thread(() -> {
            try {
                // pour rafraîchir les métadonnées
                FileEntry fresh = apiClient.getFile(file.getId());

                Platform.runLater(() -> {
                    this.file = fresh; // => mise à jour les valeurs de header (en-tête)
                    refresh(); // => appelle refreshHeader() ET loadVersions()
                    updateTitle();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    versions.clear();
                    versionsCountLabel.setText("Erreur");
                    UIDialogs.showError("Erreur", null, "Impossible de charger: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Rafraîchit l’en-tête du fichier et recharge la liste des versions depuis l’API
     */
    public void refresh(){
        refreshHeader();  // affiche nom, taille, date
        loadVersions(0);   // charge TOUTES les versions?????
    }

    /**
     * Met à jour les informations affichées du fichier (nom, taille, date de modification)
     */
    private void refreshHeader(){

        if(file == null) return;

        fileNameLabel.setText(file.getName() != null ? file.getName() : "");

        String size = file.getFormattedSize() != null ? file.getFormattedSize() : "";
        String date = file.getUpdatedAtFormatted() != null ? file.getUpdatedAtFormatted() : "";
        String meta = size;

        if (!size.isBlank() && !date.isBlank()) {
            meta += " • ";
        }
        if (!date.isBlank()) {
            meta += "Modifié le " + date;
        }

        fileMetaLabel.setText(meta.trim());
    }

    /**
     * Charge les versions du fichier en tâche de fond et met à jour la table et le compteur=>ok
     */
    private void loadVersions(int page){
        if(apiClient == null || file == null) return;

        //feedback UI
        versionsCountLabel.setText("Chargement des versions...");

        new Thread(() -> {
            try{
                int offset = page * PAGE_SIZE;

                //page => 1 , limit => 10µ
                //Charger la liste complète des versions => rempli la table
                //List<VersionEntry> list = apiClient.listFileVersions(file.getId(), 1, 100);

                PagedVersionsResponse response = apiClient.listFileVersions(file.getId(), PAGE_SIZE, offset); //limit et offset

                Platform.runLater(() -> {
                    var versionsList = response.getVersions();
                    //versions.setAll(versionsList);
                    versionsTable.getItems().setAll(versionsList);
                    System.out.println("FileDetailsController - " + versionsList.size() + " versions chargés");

                    totalVersions = response.getTotal();

                    //mise à jour la pagination
                    int totalPages = (int) Math.ceil((double) totalVersions / PAGE_SIZE);
                    pagination.setPageCount(Math.max(1, totalPages));
                    pagination.setCurrentPageIndex(page);

                    //afficher/ masquer la pagination
                    boolean showPagination = totalPages > 1;
                    pagination.setVisible(showPagination);
                    pagination.setManaged(showPagination);

                    versionsCountLabel.setText(totalVersions + " version(s)");

                    System.out.println("FileDetailsController - Total: " + totalVersions +
                            ", Pages: " + totalPages +
                            ", Page actuelle: " + (page + 1));
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    versionsTable.getItems().clear();
                    pagination.setVisible(false);
                    pagination.setManaged(false);
//                    versions.clear();
                    versionsCountLabel.setText("Erreur");
                    UIDialogs.showError("Erreur", null,"Impossible de charger les versions " + e.getMessage());
                });
            }
        }).start();
    }



    //activation d'un bouton si (que) un bouton est séléctionné
    private void rebinButtons(){

        copyChecksumButton.disableProperty().bind(
                Bindings.isNull(versionsTable.getSelectionModel().selectedItemProperty())
        );

        downloadVersionButton.disableProperty().bind(
                Bindings.isNull(versionsTable.getSelectionModel().selectedItemProperty())
        );

        // ouvrir le dossier local que s'il y a un chemin enregistré
        openLocalFolderButton.disableProperty().bind(
                Bindings.createBooleanBinding(() -> {
                    VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();
                    if(sel == null || file == null) return true;

                    return !downloadedPaths.containsKey(key(file.getId(), sel.getVersion()));
                }, versionsTable.getSelectionModel().selectedItemProperty(), downloadedPaths)
        );
    }


    /**
     * Configure les colonnes de la table des versions et le double-clic pour copier le checksum
     */
    private void setupVersionTable(){

        //colonne version
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

        //taille formatée
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));

        // date formatée
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));

        //checksum short
        checksumCol.setCellValueFactory(new PropertyValueFactory<>("checksumShort"));

        //clique sur la ligne
        versionsTable.setRowFactory(tv -> {
            TableRow<VersionEntry> row = new TableRow<>();

            //menu contextuel par ligne
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteVersion = new MenuItem("Supprimer cette version...");
            deleteVersion.setOnAction(e -> {
                VersionEntry version = row.getItem();
                if (version != null) {
                    versionsTable.getSelectionModel().select(version);
                    handleDeleteVersion();
                }
            });

            contextMenu.getItems().addAll(deleteVersion);

            //affichage le menu => que si la ligne n'est pas vide
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            // avec double clique => copier le checksum
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty()) {
                    onCopyChecksum();
                }
            });
            return row;
        });
    }

    /**
     * gestion de la suppression d'une version =>ok
     */
    public void handleDeleteVersion(){
        VersionEntry selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || file == null){
            return;
        }

        //ne pas supprimer la version courante
        if(selected.getIsCurrent()){
            UIDialogs.showError("Suppression impossible",
                    null,
                    "Impossible de supprimer la version active du fichier.\n" +
                            "Veuillez d'abord uploader une nouvelle version."
            );
            return;
        }

        // ne pas supprimer si c'est la seul version
        if(versions.size() <= 1){
            UIDialogs.showError("Suppression impossible",
                    null,
                    "Impossible de supprimer la dernière version du fichier."
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/coffrefort/client/confirmDelete.fxml")
            );

            VBox root = loader.load();

            // Récupération du contrôleur
            ConfirmDeleteController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmer la suppresion de la version");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(versionsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Injection du stage et du nom de fichier
            controller.setDialogStage(dialogStage);

            // personnaliser pour fichier
            controller.setMessage("Voulez-vous vraiment supprimer cette version ?");
            controller.setFileName(
                    file.getName() + " - Version " + selected.getVersion() +
                            " (" + selected.getFormattedSize() + ")"
            );

            //callbacks
            controller.setOnConfirm(() -> deleteVersion(selected));
            controller.setOnCancel(() -> {
                if(uploadStatusLabel != null){
                    uploadStatusLabel.setText("Suppression annulée");
                }
            });
            dialogStage.showAndWait();

        } catch (Exception e){
            System.err.println("Erreur lors du chargement de confirmDelete.fxml");
            e.printStackTrace();
            UIDialogs.showError("Erreur", null,"Impossible d'ouvrir la fenêtre de suppression: "+e.getMessage());
        }
    }

    /**
     * supprimer une version =>OK
     * @param version
     */
    private void deleteVersion(VersionEntry version){
        if (apiClient == null || file == null || version == null) {
            return;
        }
        setProgressVisible(true);
        uploadStatusLabel.setText("Suppression de la version " + version.getVersion() + " ...");
        uploadProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        // il faut unbind avant de modifier les boutons
        copyChecksumButton.disableProperty().unbind();
        openLocalFolderButton.disableProperty().unbind();
        downloadVersionButton.disableProperty().unbind();

        //désactiver les boutons
        replaceButton.setDisable(true);
        copyChecksumButton.setDisable(true);
        openLocalFolderButton.setDisable(true);
        downloadVersionButton.setDisable(true);

        //en cas de supprime le dernière élément de la page, retourner à la page précédente
        //dernière élément de la page (et pas la page 1)=> aller à la page précédente
        int itemsOnPage = versionsTable.getItems().size();
        int nextPage = (itemsOnPage == 1 && currentPage > 0) ? currentPage -1 : currentPage;

        new Thread(() -> {
            try {
                apiClient.deleteVersion(file.getId(), version.getId());

                Platform.runLater(() -> {

                    //masquer la progression
                    setProgressVisible(false);

                    //supprimer localement => ce n'est pas bon => il faut recharger depuis le serveur
//                    versions.remove(version);
                    loadVersions(nextPage);

                    //mise à jour le compteur
                    versionsCountLabel.setText(versions.size() + " version(s)");

                    // rafraîchir les données depuis le serveur
                    hydrateThenRefresh();

                    //notifier le parent (MainController) pour mettre à jour le quota
                    if(onVersionUploaded != null){
                        onVersionUploaded.run();
                    }

                    // réactiver les boutons
                    replaceButton.setDisable(false);
                    copyChecksumButton.setDisable(false);
                    openLocalFolderButton.setDisable(false);
                    downloadVersionButton.setDisable(false);

                    //rebind après modif
                    rebinButtons();

                    UIDialogs.showInfo("Suppression réussie",
                            null,
                            "La version " + version.getVersion() + " de \"" +
                            file.getName() + "\" a été supprimée."
                    );
                });
                uploadStatusLabel.setText("");
            } catch (Exception e) {
                Platform.runLater(() -> {

                    //masquer la progression
                    setProgressVisible(false);

                    // réactiver les boutons
                    replaceButton.setDisable(false);
                    copyChecksumButton.setDisable(false);
                    openLocalFolderButton.setDisable(false);
                    downloadVersionButton.setDisable(false);

                    //rebind après modif
                    rebinButtons();

                    //affichage message d'erreur
                    String errorMessage = e.getMessage();
                    if(errorMessage != null && errorMessage.contains("version active")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Impossible de supprimer la version active du fichier"
                        );
                    }else if (errorMessage != null && errorMessage.contains("dernière version")){
                        UIDialogs.showError("Suppression impossible",
                                null,
                                "Impossible de supprimer la dernière version du fichier"
                        );
                    }else{
                        UIDialogs.showError("Erreur de suppression",
                                null,
                                "Erreur: " + (errorMessage != null ? errorMessage : "Erreur inconnu.")
                        );
                    }
                });
            }
        }).start();
    }




    //=============== Action UI ==============
    @FXML
    /**
     * Permet de sélectionner un fichier local et d’uploader une nouvelle version avec suivi de progression =>ok
     */
    private void onReplace(){

        if(uploadService != null && uploadService.isRunning()){
            UIDialogs.showError("Erreur", null,"Un upload est deja en cours");
            return;
        }

        if(apiClient == null || file == null) {
            UIDialogs.showError("Erreur", null,"API ou fichier non initilalisé");
            return;
        }

        String currentFileName = file.getName();
        String currentExtension = FileUtils.getFileExtension(currentFileName);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir un fichier pour remplacer");
        chooser.setInitialFileName(file.getName());

        // filtrer par extension du fichier actuel
        if(currentExtension != null && !currentExtension.isEmpty()){
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Fichiers " + currentExtension.toUpperCase(), "*." + currentExtension));
        }

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Tous les fichiers (*.*)", "*.*")
        );

        File selected = chooser.showOpenDialog(stage);
        if(selected == null) return;

        //vérif si l'extension du selected correspond
        String selectedExtension = FileUtils.getFileExtension(selected.getName());
        if(!currentExtension.equalsIgnoreCase(selectedExtension)){
            UIDialogs.showError("Type de fichier incorrect", null,
                    "Le fichier de remplacement doit avoir le même extension. \n" +
                            "Attendu : ." + currentExtension + "\n" +
                            "Reçu: ." + selectedExtension
                    );
            return;
        }

        hideError();
        setProgressVisible(true);
        uploadStatusLabel.setText("Preparation upload...");
        uploadProgressBar.setProgress(0);

        uploadService =  new UploadVersionService(apiClient, file.getId(), selected);

        uploadProgressBar.progressProperty().bind(uploadService.progressProperty());
        uploadStatusLabel.textProperty().bind(uploadService.messageProperty());

        // désactiver le bouton pendant l'upload
        replaceButton.disableProperty().bind(uploadService.runningProperty());

        uploadService.setOnSucceeded(event -> {

            //unbind
            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);

            setProgressVisible(false);

            //refresh version et header
            hydrateThenRefresh();

            //reload liste fichiers et quota ... => callback vers MainController ???????
            if(onVersionUploaded != null) {
                onVersionUploaded.run();
            }
        });

        uploadService.setOnFailed(event -> {
            Throwable ex = uploadService.getException();

            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);

            //Masquer la progression et nettoyer l'état
            setProgressVisible(false);

            UIDialogs.showError("Upload echoue: ", null, (ex != null ? ex.getMessage() : "Erreur inconnu"));
        });

        uploadService.start();
    }


    @FXML
    /**
     * Copie le checksum complet de la version sélectionnée dans le presse-papiers
     */
    private void onCopyChecksum(){

        VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();

        if(sel == null) return;

        String checksum = sel.getChecksum();
        if(checksum == null || checksum.isBlank()){
            UIDialogs.showError("Erreur", null, "Checksum indisponible");
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(checksum.trim());
        Clipboard.getSystemClipboard().setContent(content);

        //feedback
        UIDialogs.showInfo("Checksum copie", null, "Le checksum a été copie dans le presse-papiers.");
    }

    @FXML
    /**
     * Ouvre le dossier local contenant la version téléchargée sélectionnée.
     */
    private void onOpenLocalFolder(){
        VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();

        if(sel == null) return;

        if(file == null) {
            UIDialogs.showError("Erreur", null, "Fichier non initialise.");
            return;
        }

        //chercher le chemin du fichier téléchargé associé au couple (fileId, selId)
        Path path = downloadedPaths.get(key(file.getId(), sel.getVersion()));
        if(path == null){
            UIDialogs.showError("Erreur", null, "Cette version n'a pas encore ete telecharge sur ce PC");
            return;
        }

        try {
            //si "path" est fichier => ouvrir son dossier parent
            // si "path" est un dossier => ouvrir ce dossier
            Path dir = Files.isDirectory(path) ? path : path.getParent();

            //vérifier si le dossier existe => p.ex fichier supprimé, disque externe débranché ...
            if (dir == null || !Files.exists(dir)) {
                UIDialogs.showError("Erreur", null, "Dossier local introuvable");
                return;
            }

            if(Desktop.isDesktopSupported()){

                //ouvrir le dossier dans l’explorateur de fichiers du système p.exFinder/Explorer
                Desktop.getDesktop().open(dir.toFile());
            }else{

                //p.ex. VM, certaines plateformes
                UIDialogs.showError("Erreur", null, "Ouverture du dossier non supportée sur cette plateforme.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIDialogs.showError("Erreur", null, "Impossible d'ouvrir le dossier: " + e.getMessage());
        }
    }


    @FXML
    /**
     * Télécharge une version spécifique du fichier avec affichage de la progression =>ok
     */
    private void onDownloadVersion(){
        VersionEntry sel = versionsTable.getSelectionModel().getSelectedItem();

        if(sel == null || file == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer la version " + sel.getVersion());
        //chooser.setInitialFileName(file.getName());
        //chooser.setInitialFileName(file.getName() + "_v" + sel.getVersion());
        chooser.setInitialFileName("v" + sel.getVersion() + "_" + file.getName());

        //configuration automatique les filtres
        FileUtils.configureFileChooserFilter(chooser, file.getName());

        File target = chooser.showSaveDialog(stage);
        if(target == null) return;

        hideError();
        setProgressVisible(true);
        uploadStatusLabel.setText("Telechargement");
        uploadProgressBar.setProgress(0);

        //utilisation d'un Service pour télécharger en tâche de fond
        Service<Void> downloadService = new Service<>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<>(){

                    @Override
                    protected Void call() throws Exception {
                        updateMessage("Downloading version " + sel.getVersion() + "...");

                        apiClient.downloadFileVersionTo(file.getId(), sel.getVersion(), target, (done, total) -> {
                            if(total > 0) {
                                updateProgress(done, total);
                            }
                        });
                        updateMessage("Download complete");
                        updateProgress(1, 1);
                        return null;
                    }
                };
            }
        };

        uploadProgressBar.progressProperty().bind(downloadService.progressProperty());
        uploadStatusLabel.textProperty().bind(downloadService.messageProperty());

        downloadVersionButton.disableProperty().bind(downloadService.runningProperty());
        replaceButton.disableProperty().bind(downloadService.runningProperty());

        downloadService.setOnSucceeded(event -> {
            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            downloadVersionButton.disableProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);
            downloadVersionButton.setDisable(false);


            setProgressVisible(false);

            //enregistrer le chemin local pour activer "ouvrir dossier local"
            downloadedPaths.put(key(file.getId(), sel.getVersion()), target.toPath());

            // optionnel (UI) ?????????
            versionsTable.refresh();

            // afficher le chemin (sur FX thread)
            Platform.runLater(() ->
                    UIDialogs.showInfo("Téléchargement", null, "Version téléchargée :\n" + target.getAbsolutePath())
            );
        });

        downloadService.setOnFailed(event -> {
            Throwable ex = downloadService.getException();

            uploadProgressBar.progressProperty().unbind();
            uploadStatusLabel.textProperty().unbind();
            downloadVersionButton.disableProperty().unbind();
            replaceButton.disableProperty().unbind();

            replaceButton.setDisable(false);
            downloadVersionButton.setDisable(false);

            setProgressVisible(false);

            UIDialogs.showError("Téléchargement échoué: ", null,  (ex != null ? ex.getMessage() : "Erreur inconnu"));
        });

        downloadService.start();
    }




    //================ Helper UI ===============

    /**
     * Affiche ou masque la zone de progression et réinitialise l’état d’erreur si nécessaire
     * @param visible
     */
//    private void setProgressVisible(boolean visible){
//        if (progressBox == null) {
//            System.err.println("ERREUR: progressBox est NULL !");
//            return;
//        }
//
//        progressBox.setVisible(visible);
//        progressBox.setManaged(visible);
//        if(!visible){
//            hideError();
//        }
//    }

    private void setProgressVisible(boolean visible) {
        System.out.println("DEBUG: setProgressVisible(" + visible + ") appelé");
        System.out.println("DEBUG: progressBox = " + progressBox);

        if (progressBox == null) {
            System.err.println("ERREUR: progressBox est NULL !");
            return;
        }

        System.out.println("DEBUG: Avant - visible=" + progressBox.isVisible() + ", managed=" + progressBox.isManaged());

        progressBox.setVisible(visible);
        progressBox.setManaged(visible);

        System.out.println("DEBUG: Après - visible=" + progressBox.isVisible() + ", managed=" + progressBox.isManaged());

        if (!visible) {
            hideError();
        }
    }

    /**
     * Masque le message d’erreur affiché dans l’interface.
     */
    private void hideError(){
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * énère une clé unique pour associer un fichier et une version à un chemin local téléchargé
     * @param fileId
     * @param versionNumber
     * @return
     */
    private static String key(long fileId, int versionNumber){
        return fileId + ":v" + versionNumber;
    }

    //encapsulation Service/Task lié à cette écran

    /**
     * Service JavaFX encapsulant l’upload d’une nouvelle version en tâche de fond
     */
    private static class UploadVersionService extends Service<Void> {

        private final ApiClient api;
        private final int fileId;
        private final File selectedFile;

        UploadVersionService(ApiClient api, int fileId, File selectedFile) {
            this.api = api;
            this.fileId = fileId;
            this.selectedFile = selectedFile;
        }

        @Override
        /**
         * Crée la tâche d’upload avec suivi de progression et mise à jour des messages d’état
         */
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Upload en cours...");
                    updateProgress(-1, 1); // indeterminate au début

                    api.uploadNewVersion(fileId, selectedFile, (sent, total) -> {
                        if (isCancelled()) return;
                        if (total > 0) updateProgress(sent, total);
                        else updateProgress(-1, 1);
                    });

                    updateProgress(1, 1);
                    updateMessage("Upload terminé");
                    return null;
                }
            };
        }
    }


}
