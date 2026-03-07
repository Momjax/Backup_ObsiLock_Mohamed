package com.coffrefort.client.views;

import com.coffrefort.client.model.UserQuota;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class QuotaManagementView {

    // Root
    private final VBox root = new VBox(15);

    // Search
    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("🔍 Rechercher");
    private final Button refreshButton = new Button("🔄 Actualiser");

    // Table
    private final TableView<UserQuota> usersTable = new TableView<>();
    private final TableColumn<UserQuota, Integer> idCol = new TableColumn<>("ID");
    private final TableColumn<UserQuota, String> emailCol = new TableColumn<>("Email");
    private final TableColumn<UserQuota, Long> quotaUsedCol = new TableColumn<>("Utilisé");
    private final TableColumn<UserQuota, Long> quotaMaxCol = new TableColumn<>("Quota Max");
    private final TableColumn<UserQuota, String> percentCol = new TableColumn<>(" % ");
    private final TableColumn<UserQuota, String> roleCol = new TableColumn<>("Rôle");

    // Info message
    private final Label infoLabel = new Label("Information");

    // Actions
    private final Button modifyQuotaButton = new Button("✏️ Modifier le quota");
    private final Button closeButton = new Button("Fermer");

    // Callbacks
    private Consumer<String> onSearch; // search query (email)
    private Runnable onRefresh;
    private Runnable onModifyQuota;
    private Runnable onClose;

    public QuotaManagementView() {
        buildUi();
    }

    private void buildUi() {
        root.setPrefSize(800, 600);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 8;");
        root.setPadding(new Insets(20, 25, 20, 25));

        // =========================
        // Header
        // =========================
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefWidth(48);
        iconBox.setStyle("-fx-background-color: #980b0b; -fx-background-radius: 8;");
        iconBox.setPadding(new Insets(10));

        Text icon = new Text("👥");
        icon.setFill(Color.WHITE);
        icon.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(icon);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Text title = new Text("Gestion des quotas utilisateurs");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Modifier les limites de stockage pour chaque utilisateur");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 12px;");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, titleBox);

        // Separator (marges comme FXML)
        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(10, 0, 5, 0));

        // =========================
        // Search bar
        // =========================
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Rechercher un utilisateur par email...");
        searchField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton.setFont(Font.font(12));
        searchButton.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 16;"
        );
        searchButton.setOnAction(e -> triggerSearch());

        refreshButton.setFont(Font.font(12));
        refreshButton.setStyle(
                "-fx-background-color: #666666;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 16;"
        );
        refreshButton.setOnAction(e -> triggerRefresh());

        searchBar.getChildren().addAll(searchField, searchButton, refreshButton);

        // =========================
        // Table
        // =========================
        usersTable.setPrefHeight(400);
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        usersTable.setStyle(
                "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-border-color: #cccccc;"
        );

        idCol.setPrefWidth(50);
        emailCol.setPrefWidth(200);
        quotaUsedCol.setPrefWidth(100);
        quotaMaxCol.setPrefWidth(100);
        percentCol.setPrefWidth(80);
        roleCol.setPrefWidth(80);

        usersTable.getColumns().setAll(
                idCol,
                emailCol,
                quotaUsedCol,
                quotaMaxCol,
                percentCol,
                roleCol
        );

        usersTable.setPlaceholder(buildPlaceholder());

        DropShadow tableShadow = new DropShadow();
        tableShadow.setWidth(5);
        tableShadow.setHeight(5);
        tableShadow.setRadius(2);
        tableShadow.setOffsetX(1);
        tableShadow.setOffsetY(1);
        tableShadow.setColor(Color.color(0.8, 0.8, 0.8, 0.30));
        usersTable.setEffect(tableShadow);

        // enable modify button only when selection exists
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            setModifyEnabled(newV != null);
        });

        // =========================
        // Info label (hidden by default)
        // =========================
        infoLabel.setVisible(false);
        infoLabel.setManaged(false);
        infoLabel.setWrapText(true);
        infoLabel.setStyle(
                "-fx-background-color: #e3f2fd;" +
                        "-fx-text-fill: #0d47a1;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 12px;"
        );

        // =========================
        // Actions
        // =========================
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);

        modifyQuotaButton.setDisable(true);
        modifyQuotaButton.setFont(Font.font(12));
        modifyQuotaButton.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;"
        );
        DropShadow btnShadow = new DropShadow();
        btnShadow.setRadius(10.0);
        btnShadow.setColor(Color.color(0.60, 0.05, 0.05, 0.35));
        modifyQuotaButton.setEffect(btnShadow);
        modifyQuotaButton.setOnAction(e -> triggerModifyQuota());

        closeButton.setFont(Font.font(12));
        closeButton.setStyle(
                "-fx-background-color: #cccccc;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 20;"
        );
        closeButton.setOnAction(e -> triggerClose());

        actions.getChildren().addAll(modifyQuotaButton, closeButton);

        // =========================
        // Assemble
        // =========================
        root.getChildren().addAll(header, sep, searchBar, usersTable, infoLabel, actions);

        // Focus behavior
        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> root.requestFocus());
    }

    private Node buildPlaceholder() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        Text t1 = new Text("Aucun utilisateur trouvé");
        t1.setFill(Color.web("#999999"));
        t1.setStyle("-fx-font-size: 13px;");

        Text t2 = new Text("Cliquez sur 'Actualiser' pour charger la liste");
        t2.setFill(Color.web("#cccccc"));
        t2.setStyle("-fx-font-size: 12px;");

        box.getChildren().addAll(t1, t2);
        return box;
    }

    // =========================
    // Triggers
    // =========================
    private void triggerSearch() {
        hideInfo();
        if (onSearch != null) onSearch.accept(getSearchText());
    }

    private void triggerRefresh() {
        hideInfo();
        if (onRefresh != null) onRefresh.run();
    }

    private void triggerModifyQuota() {
        hideInfo();
        if (onModifyQuota != null) onModifyQuota.run();
    }

    private void triggerClose() {
        hideInfo();
        if (onClose != null) onClose.run();
    }

    // =========================
    // API publique
    // =========================

    public Node getRoot() {
        return root;
    }

    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    public void setOnModifyQuota(Runnable onModifyQuota) {
        this.onModifyQuota = onModifyQuota;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public String getSearchText() {
        String v = searchField.getText();
        return v == null ? "" : v.trim();
    }

    public void setSearchText(String text) {
        searchField.setText(text == null ? "" : text);
    }

    public void clearSearch() {
        searchField.setText("");
    }

    public void showInfo(String message) {
        if (message == null || message.isBlank()) {
            hideInfo();
            return;
        }
        infoLabel.setText(message);
        infoLabel.setManaged(true);
        infoLabel.setVisible(true);
    }

    public void hideInfo() {
        infoLabel.setText("");
        infoLabel.setManaged(false);
        infoLabel.setVisible(false);
    }

    public void setModifyEnabled(boolean enabled) {
        modifyQuotaButton.setDisable(!enabled);
    }

    public TableView<UserQuota> getUsersTable() {
        return usersTable;
    }

    public UserQuota getSelectedUser() {
        return usersTable.getSelectionModel().getSelectedItem();
    }

    public TableColumn<UserQuota, Integer> getIdCol() {
        return idCol;
    }

    public TableColumn<UserQuota, String> getEmailCol() {
        return emailCol;
    }

    public TableColumn<UserQuota, Long> getQuotaUsedCol() {
        return quotaUsedCol;
    }

    public TableColumn<UserQuota, Long> getQuotaMaxCol() {
        return quotaMaxCol;
    }

    public TableColumn<UserQuota, String> getPercentCol() {
        return percentCol;
    }

    public TableColumn<UserQuota, String> getRoleCol() {
        return roleCol;
    }

    public void setSearchDisabled(boolean disabled) {
        searchButton.setDisable(disabled);
        refreshButton.setDisable(disabled);
        searchField.setDisable(disabled);
    }
}
