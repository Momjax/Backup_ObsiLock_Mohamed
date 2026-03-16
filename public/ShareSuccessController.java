package com.coffrefort.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public class ShareSuccessController {

    @FXML private TextField urlField;
    @FXML private Button copyBtn;

    private String url;

    public void setUrl(String url) {
        this.url = url;
        if (urlField != null) {
            urlField.setText(url);
        }
    }

    @FXML
    private void handleCopy() {
        if (url == null || url.isEmpty()) return;

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(url);
        clipboard.setContent(content);

        // Feedback visuel sur le bouton
        String originalText = copyBtn.getText();
        copyBtn.setText("Copié !");
        copyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8;");
        
        // On remet le texte après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    copyBtn.setText(originalText);
                    copyBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: #0a0c10; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8;");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) urlField.getScene().getWindow();
        stage.close();
    }
}
