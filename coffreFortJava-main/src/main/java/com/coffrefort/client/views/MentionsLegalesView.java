package com.coffrefort.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * View JavaFX (en code) équivalente à mentionLegales.fxml
 * Couleurs CryptoVault conservées.
 */
public class MentionsLegalesView {

    private final VBox root = new VBox(15);

    // --- fx:id ---
    private final Label versionLabel = new Label("Version de l’application : [0.1.0] • Dernière mise à jour : [YYYY-MM-DD]");
    private final Button returnButton = new Button("Retour à la connexion");
    private final Button closeButton = new Button("Fermer");

    // Callbacks
    private Runnable onReturn;
    private Runnable onClose;

    public MentionsLegalesView() {
        buildUi();
    }

    private void buildUi() {
        root.setPrefSize(720, 650);
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

        Text icon = new Text("⚖️");
        icon.setFill(Color.WHITE);
        icon.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(icon);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Text title = new Text("Mentions légales");
        title.setFill(Color.web("#980b0b"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text subtitle = new Text("Application CryptoVault");
        subtitle.setFill(Color.web("#666666"));
        subtitle.setStyle("-fx-font-size: 12px;");

        titleBox.getChildren().addAll(title, subtitle);

        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/images/Logo_CryptoVault.png")));
        } catch (Exception ignored) {}
        logo.setFitHeight(120);
        logo.setFitWidth(170);
        logo.setPreserveRatio(true);
        logo.setPickOnBounds(true);

        header.getChildren().addAll(iconBox, titleBox, logo);

        Separator sepTop = new Separator();
        VBox.setMargin(sepTop, new Insets(10, 0, 5, 0));

        // =========================
        // Scroll content
        // =========================
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox content = new VBox(14);
        content.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;"
        );
        content.setPadding(new Insets(18));

        // Intro
        Label intro = new Label(
                "Les présentes mentions légales décrivent les informations relatives à l’éditeur, " +
                        "l’hébergement, la propriété intellectuelle et la protection des données personnelles " +
                        "liées à l’application CryptoVault."
        );
        intro.setWrapText(true);
        intro.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        content.getChildren().addAll(intro, new Separator());

        // ========== INFORMATIONS LÉGALES ==========
        content.getChildren().add(sectionTitle("Informations légales"));

        content.getChildren().add(infoCardEditeur());
        content.getChildren().add(infoCardSiege());
        content.getChildren().add(infoCardContact());

        // ========== HÉBERGEMENT ==========
        content.getChildren().add(sectionTitle("Hébergement"));
        content.getChildren().add(infoCardHebergeur());

        // ========== PROPRIÉTÉ INTELLECTUELLE ==========
        content.getChildren().add(sectionTitle("Propriété intellectuelle"));

        Label ipText = new Label(
                "L’ensemble du contenu de CryptoVault (interfaces, textes, images, logos, icônes, sons, code et fonctionnalités) " +
                        "est la propriété exclusive de CryptoVault Inc et est protégé par les lois françaises et internationales " +
                        "relatives à la propriété intellectuelle."
        );
        ipText.setWrapText(true);
        ipText.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");
        content.getChildren().add(ipText);

        content.getChildren().add(warningBox());

        // ========== DONNÉES PERSONNELLES ==========
        content.getChildren().add(sectionTitle("Protection des données personnelles (RGPD)"));
        content.getChildren().add(rgpdCard());

        content.getChildren().add(subTitle("Finalités du traitement"));
        content.getChildren().add(basicLabel("Les données personnelles collectées sont utilisées pour :"));
        content.getChildren().add(basicLabel(
                "• La gestion des comptes et l’authentification\n" +
                        "• Le stockage, l’organisation et le partage de fichiers\n" +
                        "• La sécurité de la plateforme (prévention fraude/bruteforce, traçabilité)\n" +
                        "• Le support et la maintenance\n" +
                        "• Le respect des obligations légales et réglementaires"
        ));

        content.getChildren().add(subTitle("Base légale"));
        content.getChildren().add(basicLabel("Le traitement des données personnelles est fondé sur :"));
        content.getChildren().add(basicLabel(
                "• L’exécution d’un contrat (fourniture du service)\n" +
                        "• L’intérêt légitime de CryptoVault Inc (sécurité, amélioration du service)\n" +
                        "• Le respect d’obligations légales (conservation de certains logs, le cas échéant)"
        ));

        content.getChildren().add(subTitle("Durée de conservation"));
        content.getChildren().add(basicLabel(
                "Les données sont conservées pendant la durée nécessaire aux finalités pour lesquelles elles ont été collectées :"
        ));
        content.getChildren().add(basicLabel(
                "• Données de compte : durée du compte + 2 ans\n" +
                        "• Données de connexion : jusqu’à 1 an (sécurité)\n" +
                        "• Historique d’actions (partages/téléchargements) : 6 mois\n"
        ));

        // ========== RESPONSABILITÉ ==========
        content.getChildren().add(sectionTitle("Responsabilité et garanties"));

        content.getChildren().add(subTitle("Disponibilité du service"));
        content.getChildren().add(basicLabel(
                "CryptoVault Inc s’efforce d’assurer au mieux la disponibilité du service. " +
                        "Cependant, aucune disponibilité de 100% ne peut être garantie, et CryptoVault Inc " +
                        "ne saurait être tenue responsable des interruptions, programmées ou non."
        ));

        content.getChildren().add(subTitle("Exactitude des informations"));
        content.getChildren().add(basicLabel(
                "CryptoVault Inc s’efforce de fournir des informations exactes et à jour. Toutefois, " +
                        "l’exactitude, la précision et l’exhaustivité des informations mises à disposition ne peuvent être garanties."
        ));

        content.getChildren().add(subTitle("Limitation de responsabilité"));
        content.getChildren().add(basicLabel(
                "CryptoVault Inc ne pourra être tenue responsable des dommages directs ou indirects causés " +
                        "au matériel de l’utilisateur lors de l’accès au service, et résultant soit de l’utilisation " +
                        "d’un matériel ne répondant pas aux spécifications requises, soit de l’apparition d’un bug ou d’une incompatibilité."
        ));

        // ========== SÉCURITÉ ==========
        content.getChildren().add(sectionTitle("Sécurité"));
        content.getChildren().add(basicLabel(
                "CryptoVault Inc met en œuvre des mesures techniques et organisationnelles appropriées pour protéger " +
                        "les données contre la destruction, la perte, l’altération, la divulgation ou l’accès non autorisé."
        ));
        content.getChildren().add(basicLabel("Les mesures de sécurité incluent notamment :"));
        content.getChildren().add(basicLabel(
                "• Chiffrement des données sensibles (selon configuration)\n" +
                        "• Authentification sécurisée\n" +
                        "• Journalisation et surveillance de sécurité\n" +
                        "• Sauvegardes régulières"
        ));

        // ========== DROIT APPLICABLE ==========
        content.getChildren().add(sectionTitle("Droit applicable et juridiction"));
        content.getChildren().add(basicLabel(
                "Les présentes mentions légales sont régies par le droit français. En cas de litige, et après tentative " +
                        "de résolution amiable, les tribunaux français seront seuls compétents."
        ));

        // ========== CONTACT / RÉCLAMATIONS ==========
        content.getChildren().add(sectionTitle("Contact et réclamations"));
        content.getChildren().add(basicLabel(
                "Pour toute question concernant ces mentions légales ou pour toute réclamation, vous pouvez nous contacter :"
        ));
        content.getChildren().add(basicLabel(
                "• Par email : contact@cryptovault.com\n" +
                        "• Par courrier : [Adresse complète]\n" +
                        "• Par téléphone : +33 (0)1 88 32 19 55"
        ));
        content.getChildren().add(basicLabel(
                "Vous avez également la possibilité de saisir la Commission Nationale de l'Informatique et des Libertés (CNIL) " +
                        "pour tout litige relatif à la protection des données personnelles :"
        ));
        content.getChildren().add(basicLabel(
                "CNIL\n" +
                        "3 Place de Fontenoy - TSA 80715\n" +
                        "75334 Paris Cedex 07\n" +
                        "Téléphone : +33 (0)1 53 73 22 22\n" +
                        "Site web : www.cnil.fr"
        ));

        // ========== MODIFICATIONS ==========
        content.getChildren().add(sectionTitle("Modifications"));
        content.getChildren().add(basicLabel(
                "CryptoVault Inc se réserve le droit de modifier les présentes mentions légales à tout moment. " +
                        "Les modifications entreront en vigueur dès leur publication. Il est conseillé de consulter régulièrement cette page."
        ));

        Label lastUpdate = new Label("Dernière mise à jour : 9 Février 2026");
        lastUpdate.setWrapText(true);
        lastUpdate.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        content.getChildren().add(lastUpdate);

        content.getChildren().add(new Separator());

        versionLabel.setWrapText(true);
        versionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        content.getChildren().add(versionLabel);

        scrollPane.setContent(content);

        // =========================
        // Actions
        // =========================
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        returnButton.setPrefSize(200, 35);
        returnButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #980b0b;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;"
        );
        returnButton.setTextFill(Color.web("#980b0b"));
        returnButton.setFont(Font.font(14));
        returnButton.setOnAction(e -> triggerReturn());

        closeButton.setStyle(
                "-fx-background-color: #980b0b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 20;"
        );
        closeButton.setFont(Font.font(12));
        closeButton.setEffect(new DropShadow(10.0, Color.rgb(153, 11, 11, 0.35)));
        closeButton.setOnAction(e -> triggerClose());

        actions.getChildren().addAll(returnButton, closeButton);

        // Add all
        root.getChildren().addAll(header, sepTop, scrollPane, actions);
    }

    // =========================
    // Blocks helpers
    // =========================

    private Text sectionTitle(String t) {
        Text text = new Text(t);
        text.setFill(Color.web("#980b0b"));
        text.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return text;
    }

    private Text subTitle(String t) {
        Text text = new Text(t);
        text.setFill(Color.web("#980b0b"));
        text.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        return text;
    }

    private Label basicLabel(String txt) {
        Label l = new Label(txt);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");
        return l;
    }

    private VBox cardBase() {
        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: #f7f7f7;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;"
        );
        return box;
    }

    private VBox infoCardEditeur() {
        VBox card = cardBase();
        card.getChildren().add(subTitle("Éditeur de l’application"));

        card.getChildren().add(row("Raison sociale :", "CryptoVault Inc"));
        card.getChildren().add(row("Directeur :", "[Nom Prénom]"));
        card.getChildren().add(row("Qualité :", "Directeur de l’établissement"));
        return card;
    }

    private VBox infoCardSiege() {
        VBox card = cardBase();
        card.getChildren().add(subTitle("Siège social"));

        card.getChildren().add(row("Société :", "CryptoVault Inc"));

        Label addr = new Label("Adresse : [Adresse complète]\n[Code postal] [Ville]\n[Pays]");
        addr.setWrapText(true);
        addr.setStyle("-fx-text-fill: #333333;");
        card.getChildren().add(addr);
        return card;
    }

    private VBox infoCardContact() {
        VBox card = cardBase();
        card.getChildren().add(subTitle("Contact"));

        card.getChildren().add(row("Téléphone :", "+33 (0)1 88 32 19 55"));
        card.getChildren().add(row("Email :", "contact@cryptovault.com"));
        card.getChildren().add(row("Site web :", "www.cryptovault.fr"));
        return card;
    }

    private VBox infoCardHebergeur() {
        VBox card = cardBase();
        card.getChildren().add(subTitle("Informations hébergeur"));

        card.getChildren().add(row("Hébergeur :", "AlwaysData"));

        Label addr = new Label("Adresse : 91, rue du Faubourg-Saint-Honoré\n75008 Paris, France");
        addr.setWrapText(true);
        addr.setStyle("-fx-text-fill: #333333;");
        card.getChildren().add(addr);

        card.getChildren().add(row("Téléphone :", "+33 1 84 16 23 40"));
        card.getChildren().add(row("Site web :", "www.alwaysdata.com"));
        return card;
    }

    private VBox warningBox() {
        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: #ffe5e5;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12;" +
                        "-fx-border-color: #f2bcbc;" +
                        "-fx-border-radius: 8;"
        );

        Text t = subTitle("⚠ Interdictions strictes");

        Label strong = new Label(
                "Toute reproduction, représentation, modification, publication ou adaptation, totale ou partielle, " +
                        "est strictement interdite sauf autorisation écrite préalable de CryptoVault Inc."
        );
        strong.setWrapText(true);
        strong.setStyle("-fx-text-fill: #980b0b; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label normal = basicLabel(
                "Toute exploitation non autorisée de l’application ou de l’un quelconque des éléments qu’elle contient " +
                        "pourra être considérée comme constitutive de contrefaçon et poursuivie conformément aux articles " +
                        "L.335-2 et suivants du Code de la propriété intellectuelle."
        );

        box.getChildren().addAll(t, strong, normal);
        return box;
    }

    private VBox rgpdCard() {
        VBox card = cardBase();

        card.getChildren().add(subTitle("Responsable du traitement"));
        card.getChildren().add(basicLabel(
                "CryptoVault Inc est responsable du traitement des données personnelles collectées et traitées " +
                        "dans le cadre de l’utilisation de l’application."
        ));

        card.getChildren().add(subTitle("Données collectées"));
        card.getChildren().add(basicLabel(
                "Dans le cadre du coffre-fort numérique, les catégories de données susceptibles d’être traitées incluent notamment :"
        ));

        Label list = basicLabel(
                "✓ Données d’identification des utilisateurs (email, rôle)\n" +
                        "✓ Données de connexion et d’utilisation (logs techniques)\n" +
                        "✓ Métadonnées et historique liés au stockage, au partage et aux téléchargements\n"
        );

        card.getChildren().add(list);
        return card;
    }

    private HBox row(String label, String value) {
        HBox row = new HBox(8);

        Label l1 = new Label(label);
        l1.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

        Label l2 = new Label(value);
        l2.setStyle("-fx-text-fill: #333333;");

        row.getChildren().addAll(l1, l2);
        return row;
    }

    // =========================
    // Triggers
    // =========================

    private void triggerReturn() {
        if (onReturn != null) onReturn.run();
    }

    private void triggerClose() {
        if (onClose != null) onClose.run();
    }

    // =========================
    // Public API
    // =========================

    public Node getRoot() {
        return root;
    }

    public void setOnReturn(Runnable onReturn) {
        this.onReturn = onReturn;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    /** Pour mettre à jour le texte "Version..." depuis ton controller */
    public void setVersionText(String text) {
        versionLabel.setText(text == null ? "" : text);
    }

    public Button getReturnButton() {
        return returnButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public Label getVersionLabel() {
        return versionLabel;
    }
}
