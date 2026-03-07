package com.coffrefort.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un dossier (noeud) contenant éventuellement des fichiers et des sous-dossiers.
 */
public class NodeItem {

    //propriétés
    public Integer id;
    private String name;
    private List<NodeItem> children = new ArrayList<>();
    private NodeType type;

    // pour compatibilité avec l'ancien code (MainView)
    private final List<FileEntry> files = new ArrayList<>();

    //enum pour le type de noeud
    public enum NodeType {
        FOLDER,
        FILE
    }

    //public NodeItem() {}

    //méthodes
    public NodeItem(Integer id, String name, NodeType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static NodeItem folder(int id, String name, NodeType type) {
        return new NodeItem(id, name, type);
    }

    //NodeItem de type FOLDER => surcharge sans type
    public static NodeItem folder(int id, String name) {
        return new NodeItem(id, name, NodeItem.NodeType.FOLDER);
    }

    public NodeItem addChild(NodeItem child) {
        this.children.add(child);
        return this;
    }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name){
        this.name = name;
    }
    public void setType(NodeType type) {
        this.type = type;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public List<NodeItem> getChildren() { return children; }
    public NodeType getType(){
        return type;
    }




    @Override
    public String toString() {
        return name != null ? name : "sans nom";
    }


    // === compatibilité avec le code d'exemple (MainView) ===
    public NodeItem withFiles(List<FileEntry> list) {
        this.files.clear();
        if (list != null) {
            this.files.addAll(list);
        }
        return this;
    }

    public List<FileEntry> getFiles() {
        return files;
    }

}
