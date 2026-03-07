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
        alert.getDialogPane().setMinWidth(500);

        Label icon = new Label("i");
        icon.setStyle(
                "-fx-background-color: #0d5c05;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 28px;" +
                        "-fx-min-height: 28px;" +
                        "-fx-background-radius: 11px;" +
                        "-fx-font-size: 14px;"
        );
        alert.setGraphic(icon);

        styleOkButtonInfo(alert);

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
                "-fx-background-color: #0d5c05;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 28px;" +
                        "-fx-min-height: 28px;" +
                        "-fx-background-radius: 11px;" +
                        "-fx-font-size: 14px;"
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
        alert.getDialogPane().setMinWidth(500);

        Label icon = new Label("!");
        icon.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 28px;" +
                        "-fx-min-height: 28px;" +
                        "-fx-background-radius: 11px;" +
                        "-fx-font-size: 14px;"
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
        alert.getDialogPane().setMinWidth(500);
        alert.getDialogPane().setMinHeight(250);

        // Icône bordeaux
        Label icon = new Label("!");
        icon.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;" +
                        "-fx-min-width: 28px;" +
                        "-fx-min-height: 28px;" +
                        "-fx-background-radius: 11px;" +
                        "-fx-font-size: 14px;"
        );
        alert.setGraphic(icon);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: #E5E5E5;");

        // Boutons personnalisés
        ButtonType confirmType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmType, cancelType);

        // Styles boutons
        Button revokeBtn = (Button) pane.lookupButton(confirmType);
        if (revokeBtn != null) {
            revokeBtn.setStyle(
                    "-fx-background-color: #980b0b;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
        }

        Button cancelBtn = (Button) pane.lookupButton(cancelType);
        if (cancelBtn != null) {
            cancelBtn.setStyle(
                    "-fx-background-color: #cccccc;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-cursor: hand;"
            );
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
            okBtn.setStyle(
                    "-fx-background-color: #980b0b;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
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
            okBtn.setStyle(
                    "-fx-background-color: #0d5c05;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
        }
    }
}
