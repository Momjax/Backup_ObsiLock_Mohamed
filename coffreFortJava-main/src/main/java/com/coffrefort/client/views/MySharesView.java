package com.coffrefort.client.views;

import com.coffrefort.client.model.ShareItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MySharesView {

    private final VBox root = new VBox(12);

    private final Label titleLabel = new Label("Mes partages");

    private final TableView<ShareItem> sharesTable = new TableView<>();

    private final TableColumn<ShareItem, String> resourceCol = new TableColumn<>("Ressource");
    private final TableColumn<ShareItem, String> labelCol = new TableColumn<>("Label");
    private final TableColumn<ShareItem, String> expiresCol = new TableColumn<>("Expire le");
    private final TableColumn<ShareItem, String> remainingCol = new TableColumn<>("Restant");
    private final TableColumn<ShareItem, String> statusCol = new TableColumn<>("État");
    private final TableColumn<ShareItem, Void> actionCol = new TableColumn<>("Actions");

    private final Pagination pagination = new Pagination();

    public MySharesView() {
        buildUi();
    }

    private void buildUi() {
        root.setSpacing(12);
        root.setStyle("-fx-background-color: #F4F4F4;");
        root.setPadding(new Insets(15, 15, 15, 15));

        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #980b0b;");

        // Table
        sharesTable.setPrefHeight(420);
        VBox.setVgrow(sharesTable, Priority.ALWAYS);

        sharesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        resourceCol.setPrefWidth(160);
        labelCol.setPrefWidth(200);
        expiresCol.setPrefWidth(180);
        remainingCol.setPrefWidth(80);
        statusCol.setPrefWidth(80);
        actionCol.setPrefWidth(140);

        sharesTable.getColumns().setAll(
                resourceCol,
                labelCol,
                expiresCol,
                remainingCol,
                statusCol,
                actionCol
        );

        // Pagination
        pagination.setMaxPageIndicatorCount(7);

        root.getChildren().addAll(titleLabel, sharesTable, pagination);
    }

    // ===== API publique =====

    public Node getRoot() {
        return root;
    }

    public TableView<ShareItem> getSharesTable() {
        return sharesTable;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public TableColumn<ShareItem, String> getResourceCol() {
        return resourceCol;
    }

    public TableColumn<ShareItem, String> getLabelCol() {
        return labelCol;
    }

    public TableColumn<ShareItem, String> getExpiresCol() {
        return expiresCol;
    }

    public TableColumn<ShareItem, String> getRemainingCol() {
        return remainingCol;
    }

    public TableColumn<ShareItem, String> getStatusCol() {
        return statusCol;
    }

    public TableColumn<ShareItem, Void> getActionCol() {
        return actionCol;
    }
}

