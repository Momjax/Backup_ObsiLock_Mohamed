package com.coffrefort.client.util;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

public final class UIDialogs {

    private UIDialogs() {}


    /**
     * Afficher une information
     * @param title
     * @param header
     * @param content
     */
    public static void showInfo(String title,String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Style personnalisé
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(UIDialogs.class.getResource("/com/coffrefort/client/style-javafx.css").toExternalForm());
        pane.getStyleClass().add("root");
        pane.setMinWidth(500);
        pane.setStyle("-fx-background-color: #0d1117;");

        Label icon = new Label("i");
        icon.setStyle(
                "-fx-background-color: rgba(46, 204, 113, 0.1);" +
                        "-fx-text-fill: #2ecc71;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 32px;" +
                        "-fx-min-height: 32px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #2ecc71;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-font-size: 16px;"
        );
        alert.setGraphic(icon);

        alert.showAndWait();
    }

    /**
     * Afficher une info avec un contenu custom  p.ex TextArea pour URL
     * @param title
     * @param header
     * @param contentNode
     */
    public static void showInfoWithNode(String title,String header, Node contentNode) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(null);

        // Style personnalisé
        alert.getDialogPane().setMinWidth(500);

        Label icon = new Label("i");
        icon.setStyle(
                "-fx-background-color: rgba(46, 204, 113, 0.1);" +
                        "-fx-text-fill: #2ecc71;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 32px;" +
                        "-fx-min-height: 32px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #2ecc71;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-font-size: 16px;"
        );
        alert.setGraphic(icon);

        DialogPane pane = alert.getDialogPane();
        pane.setContent(contentNode);

        styleOkButtonInfo(alert);

        alert.showAndWait();
    }

    /**
     * Helper => boîte url =>prêt à l'emploi  => textarea non éditable
     * @param title
     * @param header
     * @param url
     */
    public static void showInfoUrl (String title, String header, String url) {
        TextArea textArea = new TextArea(url == null ? "" : url);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(3);
        textArea.setFocusTraversable(false);

        showInfoWithNode(title, header, textArea);
    }


    /**
     * Afficher une erreur
     * @param title
     * @param header
     * @param content
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Style personnalisé
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(UIDialogs.class.getResource("/com/coffrefort/client/style-javafx.css").toExternalForm());
        pane.getStyleClass().add("root");
        pane.setMinWidth(500);
        pane.setStyle("-fx-background-color: #0d1117;");

        Label icon = new Label("!");
        icon.setStyle(
                "-fx-background-color: rgba(255, 71, 87, 0.1);" +
                        "-fx-text-fill: #ff4757;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 32px;" +
                        "-fx-min-height: 32px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #ff4757;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-font-size: 16px;"
        );
        alert.setGraphic(icon);

        styleOkButtonError(alert);

        alert.showAndWait();
    }

    /**
     * Afficher une confirmation pour une révocation
     * @param title
     * @param header
     * @param content
     * @return
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Style personnalisé
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(UIDialogs.class.getResource("/com/coffrefort/client/style-javafx.css").toExternalForm());
        pane.getStyleClass().add("root");
        pane.setMinWidth(500);
        pane.setMinHeight(250);
        pane.setStyle("-fx-background-color: #0d1117;");

        // Icône bordeaux
        Label icon = new Label("!");
        icon.setStyle(
                "-fx-background-color: rgba(255, 71, 87, 0.1);" +
                        "-fx-text-fill: #ff4757;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 32px;" +
                        "-fx-min-height: 32px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #ff4757;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-font-size: 16px;"
        );
        alert.setGraphic(icon);

        // Boutons personnalisés
        ButtonType confirmType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmType, cancelType);

        // Styles boutons
        Button revokeBtn = (Button) pane.lookupButton(confirmType);
        if (revokeBtn != null) {
            revokeBtn.getStyleClass().add("button-primary");
            revokeBtn.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white;");
        }

        Button cancelBtn = (Button) pane.lookupButton(cancelType);
        if (cancelBtn != null) {
            cancelBtn.getStyleClass().add("button-secondary");
        }

        return alert.showAndWait().filter(btn -> btn == confirmType).isPresent();
    }


    /**
     * Applique un style CSS personnalisé au bouton OK d’une Alert JavaFX (couleur, texte, graisse et curseur)
     * @param alert
     */
    private static void styleOkButtonError(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("button-primary");
            okBtn.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white;");
        }
    }

    /**
     * Applique un style CSS personnalisé au bouton OK d’une Alert JavaFX (couleur, texte, graisse et curseur)
     * @param alert
     */
    private static void styleOkButtonInfo(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("button-primary");
        }
    }
}
