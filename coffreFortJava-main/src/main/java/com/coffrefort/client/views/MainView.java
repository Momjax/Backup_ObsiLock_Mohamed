package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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

public class MainView {

    // Root
    private final BorderPane root = new BorderPane();

    // ===== TOP (header) =====
    private final Label userEmailLabel = new Label("user@example.com");
    private final Label quotaLabel = new Label("0 / 1 GB");
    private final ProgressBar quotaBar = new ProgressBar(0);

    private final Button gestionQuotaButton = new Button("Modifier le quota");
    private final Button logoutButton = new Button("Déconnexion");

    // ===== CENTER (split) =====
    private final TreeView<String> treeView = new TreeView<>();
    private final Button newFolderButton = new Button("+");

    private final TableView<Object> table = new TableView<>();
    private final TableColumn<Object, String> nameCol = new TableColumn<>("📄 Nom");
    private final TableColumn<Object, String> sizeCol = new TableColumn<>("📊 Taille");
    private final TableColumn<Object, String> dateCol = new TableColumn<>("📅 Modifié le");

    private final Button uploadButton = new Button("📤 Uploader");
    private final Button shareButton = new Button("🔗 Partager");
    private final Button deleteButton = new Button("🗑 Supprimer");

    private final Pagination pagination = new Pagination();

    // ===== BOTTOM (status bar) =====
    private final Label statusLabel = new Label("Prêt");
    private final Label fileCountLabel = new Label("0 fichier(s)");
    private final Hyperlink openSharesLink = new Hyperlink("Mes partages");

    // ===== Callbacks =====
    private Runnable onQuota;
    private Runnable onLogout;
    private Runnable onNewFolder;
    private Runnable onUpload;
    private Runnable onShare;
    private Runnable onDelete;
    private Runnable onOpenShares;

    public MainView() {
        buildUi();
    }

    private void buildUi() {
        root.setPrefSize(1024, 700);
        root.setStyle("-fx-background-color: #E5E5E5;");

        root.setTop(buildTop());
        root.setCenter(buildCenter());
        root.setBottom(buildBottom());
    }

    // =========================
    // TOP
    // =========================
    private Node buildTop() {
        VBox topBox = new VBox();
        topBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        topBox.setPadding(new Insets(15, 20, 15, 20));

        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView();
        logo.setFitHeight(60);
        logo.setFitWidth(80);
        logo.setPreserveRatio(true);
        logo.setPickOnBounds(true);
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/images/Logo_CryptoVault.png")));
        } catch (Exception ignored) {}

        VBox titleBox = new VBox(2);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Text title = new Text("Coffre-Fort Numérique");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");

        userEmailLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        titleBox.getChildren().addAll(title, userEmailLabel);

        VBox quotaBox = new VBox(5);
        quotaBox.setAlignment(Pos.CENTER_RIGHT);

        HBox quotaLine = new HBox(8);
        quotaLine.setAlignment(Pos.CENTER_RIGHT);

        Label quotaText = new Label("Espace utilisé:");
        quotaText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        quotaLabel.getStyleClass().add("quota-label"); // comme FXML (si tu as une CSS)
        // fallback si pas de CSS :
        if (quotaLabel.getStyle() == null || quotaLabel.getStyle().isBlank()) {
            quotaLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        }

        quotaLine.getChildren().addAll(quotaText, quotaLabel);

        quotaBar.setPrefWidth(200);
        quotaBar.setPrefHeight(8);

        quotaBox.getChildren().addAll(quotaLine, quotaBar);

        styleHeaderButton(gestionQuotaButton);
        gestionQuotaButton.setPrefSize(133, 33);
        gestionQuotaButton.setFont(Font.font(12));
        gestionQuotaButton.setOnAction(e -> triggerQuota());

        styleHeaderButton(logoutButton);
        logoutButton.setFont(Font.font(12));
        logoutButton.setOnAction(e -> triggerLogout());

        row.getChildren().addAll(logo, titleBox, quotaBox, gestionQuotaButton, logoutButton);
        topBox.getChildren().add(row);

        return topBox;
    }

    private void styleHeaderButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 16;"
        );
    }

    // =========================
    // CENTER
    // =========================
    private Node buildCenter() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.25);
        splitPane.setStyle("-fx-background-color: #E5E5E5;");
        splitPane.setPadding(new Insets(10));

        VBox left = buildLeftPane();
        VBox right = buildRightPane();

        splitPane.getItems().addAll(left, right);
        return splitPane;
    }

    private VBox buildLeftPane() {
        VBox left = new VBox(8);
        left.setStyle("-fx-background-color: white; -fx-background-radius: 4;");
        left.setPadding(new Insets(10));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Mes dossiers");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        newFolderButton.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 4 12;"
        );
        newFolderButton.setTooltip(new Tooltip("Nouveau dossier à la racine"));
        newFolderButton.setOnAction(e -> triggerNewFolder());

        header.getChildren().addAll(title, spacer, newFolderButton);

        Separator sep = new Separator();

        treeView.setShowRoot(false);
        treeView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(treeView, Priority.ALWAYS);

        treeView.setEffect(makeSoftShadow(0.8, 0.8, 0.8, 0.30));

        left.getChildren().addAll(header, sep, treeView);
        return left;
    }

    private VBox buildRightPane() {
        VBox right = new VBox(8);
        right.setStyle("-fx-background-color: white; -fx-background-radius: 4;");
        right.setPadding(new Insets(10));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Fichiers");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        stylePrimaryAction(uploadButton);
        uploadButton.setFont(Font.font(12));
        uploadButton.setOnAction(e -> triggerUpload());

        styleDisabledAction(shareButton);
        shareButton.setFont(Font.font(12));
        shareButton.setDisable(true);
        shareButton.setOnAction(e -> triggerShare());

        styleDisabledAction(deleteButton);
        deleteButton.setFont(Font.font(12));
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> triggerDelete());

        header.getChildren().addAll(title, spacer, uploadButton, shareButton, deleteButton);

        Separator sep = new Separator();

        buildTable();

        pagination.setMaxPageIndicatorCount(7);

        VBox.setVgrow(table, Priority.ALWAYS);
        right.getChildren().addAll(header, sep, table, pagination);
        return right;
    }

    private void buildTable() {
        table.setStyle("-fx-background-color: transparent;");
        table.setPlaceholder(buildEmptyFilesPlaceholder());
        table.setEffect(makeSoftShadow(0.8, 0.8, 0.8, 0.30));

        nameCol.setPrefWidth(300);
        sizeCol.setPrefWidth(100);
        dateCol.setPrefWidth(150);

        table.getColumns().setAll(nameCol, sizeCol, dateCol);

        // Gestion sélection : active/désactive boutons comme dans l'app
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean hasSelection = newV != null;
            setShareDeleteEnabled(hasSelection);
        });
    }

    private Node buildEmptyFilesPlaceholder() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Text t1 = new Text("Aucun fichier");
        t1.setFill(Color.web("#999999"));
        t1.setStyle("-fx-font-size: 14px;");

        Text t2 = new Text("Cliquez sur 'Uploader' pour ajouter des fichiers");
        t2.setFill(Color.web("#cccccc"));
        t2.setStyle("-fx-font-size: 12px;");

        box.getChildren().addAll(t1, t2);
        return box;
    }

    private void stylePrimaryAction(Button btn) {
        btn.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 14;"
        );
    }

    private void styleDisabledAction(Button btn) {
        btn.setStyle(
                "-fx-background-color: #666666;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 14;"
        );
    }

    private DropShadow makeSoftShadow(double r, double g, double b, double opacity) {
        DropShadow ds = new DropShadow();
        ds.setWidth(5);
        ds.setHeight(5);
        ds.setRadius(2);
        ds.setOffsetX(1);
        ds.setOffsetY(1);
        ds.setColor(Color.color(r, g, b, opacity));
        return ds;
    }

    // =========================
    // BOTTOM
    // =========================
    private Node buildBottom() {
        HBox bottom = new HBox(15);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        bottom.setPadding(new Insets(8, 15, 8, 15));

        statusLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        fileCountLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Separator vert = new Separator(Orientation.VERTICAL);

        openSharesLink.setStyle(
                "-fx-text-fill: #980b0b;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 11px;"
        );
        openSharesLink.setOnAction(e -> triggerOpenShares());

        bottom.getChildren().addAll(statusLabel, spacer, fileCountLabel, vert, openSharesLink);
        return bottom;
    }

    // =========================
    // Triggers
    // =========================
    private void triggerQuota() { if (onQuota != null) onQuota.run(); }
    private void triggerLogout() { if (onLogout != null) onLogout.run(); }
    private void triggerNewFolder() { if (onNewFolder != null) onNewFolder.run(); }
    private void triggerUpload() { if (onUpload != null) onUpload.run(); }
    private void triggerShare() { if (onShare != null) onShare.run(); }
    private void triggerDelete() { if (onDelete != null) onDelete.run(); }
    private void triggerOpenShares() { if (onOpenShares != null) onOpenShares.run(); }

    // =========================
    // API publique
    // =========================

    public Node getRoot() {
        return root;
    }

    // Callbacks
    public void setOnQuota(Runnable onQuota) { this.onQuota = onQuota; }
    public void setOnLogout(Runnable onLogout) { this.onLogout = onLogout; }
    public void setOnNewFolder(Runnable onNewFolder) { this.onNewFolder = onNewFolder; }
    public void setOnUpload(Runnable onUpload) { this.onUpload = onUpload; }
    public void setOnShare(Runnable onShare) { this.onShare = onShare; }
    public void setOnDelete(Runnable onDelete) { this.onDelete = onDelete; }
    public void setOnOpenShares(Runnable onOpenShares) { this.onOpenShares = onOpenShares; }

    // Data setters
    public void setUserEmail(String email) {
        userEmailLabel.setText(email == null ? "" : email);
    }

    public void setQuotaText(String text) {
        quotaLabel.setText(text == null ? "" : text);
    }

    public void setQuotaProgress(double progress0to1) {
        quotaBar.setProgress(progress0to1);
    }

    public void setStatus(String status) {
        statusLabel.setText(status == null ? "" : status);
    }

    public void setFileCountText(String text) {
        fileCountLabel.setText(text == null ? "" : text);
    }

    /**
     * Active/désactive Partager/Supprimer + adapter le style (comme ton FXML : gris quand disable).
     */
    public void setShareDeleteEnabled(boolean enabled) {
        shareButton.setDisable(!enabled);
        deleteButton.setDisable(!enabled);

        if (enabled) {
            stylePrimaryAction(shareButton);
            stylePrimaryAction(deleteButton);
        } else {
            styleDisabledAction(shareButton);
            styleDisabledAction(deleteButton);
        }
    }

    // Getters pour que le controller/service puisse brancher les données
    public TreeView<String> getTreeView() {
        return treeView;
    }

    public TableView<Object> getTable() {
        return table;
    }

    public TableColumn<Object, String> getNameCol() {
        return nameCol;
    }

    public TableColumn<Object, String> getSizeCol() {
        return sizeCol;
    }

    public TableColumn<Object, String> getDateCol() {
        return dateCol;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Button getUploadButton() {
        return uploadButton;
    }

    public Button getShareButton() {
        return shareButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getNewFolderButton() {
        return newFolderButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public Button getGestionQuotaButton() {
        return gestionQuotaButton;
    }
}


//package com.coffrefort.client.views;
//
//import com.coffrefort.client.ApiClient;
//import com.coffrefort.client.controllers.MainController;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.layout.BorderPane;
//
//import java.io.IOException;
//
//public class MainView {
//
//    private final BorderPane root;
//    private final MainController controller;
//
//    public MainView(ApiClient apiClient, String userEmail) {
//        try {
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/com/coffrefort/client/main.fxml")
//            );
//            this.root = loader.load();
//            this.controller = loader.getController();
//
//            // Injecter ApiClient dans le contrôleur
//            if (apiClient != null) {
//                controller.setApiClient(apiClient);
//            }
//
//            // Injecter l'email (affiché dans userEmailLabel)
//            if (userEmail != null && !userEmail.isBlank()) {
//                controller.setUserEmail(userEmail);
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException("Erreur lors du chargement de main.fxml", e);
//        }
//    }
//
//    // Surcharge pratique si tu n'as pas encore l'email
//    public MainView(ApiClient apiClient) {
//        this(apiClient, null);
//    }
//
//    // Comme LoginView.getRoot()
//    public Node getRoot() {
//        return root;
//    }
//
//    // Accès au contrôleur, si besoin
//    public MainController getController() {
//        return controller;
//    }
//}