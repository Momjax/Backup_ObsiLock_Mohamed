package com.coffrefort.client.util;

import javafx.stage.FileChooser;

import java.util.List;

//classe utilitaires pour les opérations sur les fichiers
public class FileUtils {

    public static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg","png", "webp", "pdf", "doc", "docx", "xlsx"
    );


    /**
     * Extrait l'extension d'un nom de fichier (sans le point)
     *
     * @param fileName Nom du fichier (ex: "document.pdf")
     * @return Extension en minuscules (ex: "pdf") ou chaîne vide si aucune extension
     *
     * @example
     * FileUtils.getFileExtension("photo.jpg") → "jpg"
     * FileUtils.getFileExtension("archive.tar.gz") → "gz"
     * FileUtils.getFileExtension("fichier") → ""
     */
    public static String getFileExtension(String fileName){
        if(fileName == null || fileName.isEmpty()){
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        if(lastDot == -1 || lastDot == fileName.length() - 1 ){
            return ""; //=> pad d'extension
        }

        return fileName.substring(lastDot + 1).toLowerCase();
    }


    /**
     * Vérifie si une extension est autorisée pour l'upload
     *
     * @param extension Extension à vérifier (avec ou sans point)
     * @return true si l'extension est autorisée, false sinon
     *
     * @example
     * FileUtils.isExtensionAllowed("pdf") → true
     * FileUtils.isExtensionAllowed(".jpg") → true
     * FileUtils.isExtensionAllowed("exe") → false
     */
    public static boolean isExtensionAllowed(String extension){
        if(extension == null || extension.isEmpty()){
            return false;
        }

        // Supprimer le point si présent
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;

        return ALLOWED_EXTENSIONS.contains(ext.toLowerCase());
    }

    /**
     * Retourne la liste des extensions autorisées
     *
     * @return Liste non modifiable des extensions autorisées
     */
    public static List<String> getAllowedExtensions(){
        return ALLOWED_EXTENSIONS;
    }

    /**
     * Retourne une chaîne formatée des extensions autorisées
     *
     * @return "jpg, jpeg, png, webp, pdf, doc, docx, xlsx"
     */
    public static String getAllowedExtensionString(){
        return String.join(".", ALLOWED_EXTENSIONS);
    }

    /** à vérifier => il y a un FileEntry et dans le MainController
     * Convertit une taille en octets en format lisible (B, KB, MB, GB) pour l’affichage
     * (fr: o, ko, mo, go avec 3.4 Mo -exemple)
     * il renvoie "anglais" avec virgule
     * @param bytes
     * @return
             */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }


    /**
     * Supprime l'extension d'un nom de fichier
     *
     * @param fileName Nom du fichier (ex: "document.pdf")
     * @return Nom sans extension (ex: "document")
     *
     * @example
     * FileUtils.removeExtension("photo.jpg") → "photo"
     * FileUtils.removeExtension("archive.tar.gz") → "archive.tar"
     */
    public static String removeExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return fileName;
        }

        return fileName.substring(0, lastDot);
    }

    public static void configureFileChooserFilter(FileChooser chooser, String fileName){
        if(chooser == null){
            throw new IllegalStateException("Filechooser ne peut pas être null");
        }

        //extraire l'extension du fichier
        String extension = FileUtils.getFileExtension(fileName);

        //définir les filtres extension
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
        FileChooser.ExtensionFilter imagesFilter = new FileChooser.ExtensionFilter("Images (*.jpg, *.jpeg, *.png, *.webp)", "*.jpg", "*.jpeg", "*.png", "*.webp");
        FileChooser.ExtensionFilter wordFilter = new FileChooser.ExtensionFilter("Documents Word (*.doc, *.docx)", "*.doc", "*.docx");
        FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tous les fichiers (*.*)", "*.*");

        //ajouter tous les filters
        chooser.getExtensionFilters().addAll(pdfFilter, imagesFilter, wordFilter, excelFilter, allFilter);

        //Sélectionner automatiquement le bon filtre d'extension
        FileChooser.ExtensionFilter defaultFilter = allFilter;

        switch(extension.toLowerCase()) {
            case "pdf":
                defaultFilter = pdfFilter;
                break;
            case "jpg":
            case "jpeg":
            case "png":
            case "webp":
                defaultFilter = imagesFilter;
                break;
            case "doc":
            case "docx":
                defaultFilter = wordFilter;
                break;
            case  "xlsx":
                defaultFilter = excelFilter;
                break;
        }

        chooser.setSelectedExtensionFilter(defaultFilter);
    }


}

