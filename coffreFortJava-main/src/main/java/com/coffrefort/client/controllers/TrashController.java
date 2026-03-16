package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.FileEntry;
import com.coffrefort.client.model.NodeItem;
import com.coffrefort.client.model.TrashItem;
import com.coffrefort.client.util.UIDialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TrashController {

    @FXML private TableView<TrashItem> trashTable;
    @FXML private TableColumn<TrashItem, String> typeCol;
    @FXML private TableColumn<TrashItem, String> nameCol;
    @FXML private TableColumn<TrashItem, String> sizeCol;
    @FXML private TableColumn<TrashItem, String> dateCol;
    @FXML private TableColumn<TrashItem, Void> actionCol;

    private ApiClient apiClient;
    private Stage stage;

    @FXML
    private void initialize() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        initActionColumn();
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        refresh();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void refresh() {
        if (apiClient == null) return;

        new Thread(() -> {
            try {
                List<FileEntry> deletedFiles = apiClient.getTrashFiles();
                List<NodeItem> deletedFolders = apiClient.getTrashFolders();

                List<TrashItem> allItems = new ArrayList<>();
                for (NodeItem f : deletedFolders) {
                    allItems.add(new TrashItem(f.getId(), f.getName(), 0, "", true));
                }
                for (FileEntry f : deletedFiles) {
                    allItems.add(new TrashItem(f.getId(), f.getName(), f.getSize(), f.getUpdatedAtFormatted(), false));
                }

                Platform.runLater(() -> trashTable.getItems().setAll(allItems));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> UIDialogs.showError("Erreur", null, "Impossible de charger la corbeille: " + e.getMessage()));
            }
        }).start();
    }

    private void initActionColumn() {
        actionCol.setCellFactory(col -> new TableCell<TrashItem, Void>() {
            private final Button restoreBtn = new Button("🔄");
            private final Button deleteBtn = new Button("🗑");
            private final HBox box = new HBox(10, restoreBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                restoreBtn.getStyleClass().add("button-secondary");
                deleteBtn.getStyleClass().add("button-trash");
                
                restoreBtn.setTooltip(new Tooltip("Restaurer"));
                deleteBtn.setTooltip(new Tooltip("Supprimer définitivement"));

                restoreBtn.setOnAction(e -> handleRestore(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handlePermanentDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void handleRestore(TrashItem item) {
        new Thread(() -> {
            try {
                if (item.isFolder()) {
                    apiClient.restoreFolder(item.getId());
                } else {
                    apiClient.restoreFile(item.getId());
                }
                Platform.runLater(() -> {
                    refresh();
                    UIDialogs.showInfo("Restauration", null, item.getName() + " a été restauré avec succès.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> UIDialogs.showError("Erreur", null, "Échec de la restauration: " + e.getMessage()));
            }
        }).start();
    }

    private void handlePermanentDelete(TrashItem item) {
        boolean confirmed = UIDialogs.showConfirmation(
                "Suppression définitive",
                "Supprimer " + item.getName() + " ?",
                "Cette action est irréversible. Toutes les données seront perdues."
        );

        if (!confirmed) return;

        new Thread(() -> {
            try {
                if (item.isFolder()) {
                    apiClient.permanentDeleteFolder(item.getId());
                } else {
                    apiClient.permanentDeleteFile(item.getId());
                }
                Platform.runLater(() -> {
                    refresh();
                    UIDialogs.showInfo("Supprimé", null, item.getName() + " a été supprimé définitivement.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> UIDialogs.showError("Erreur", null, "Échec de la suppression: " + e.getMessage()));
            }
        }).start();
    }
}
