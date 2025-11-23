/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en el árbol de directorios
 * Puede ser un directorio o un archivo
 * @author verol
 */
public class DirectoryNode {
    public enum NodeType {
        DIRECTORY, FILE
    }
    
    private String name;
    private NodeType type;
    private DirectoryNode parent;
    private List<DirectoryNode> children;
    private Archivo archivo; // Solo para archivos
    private String fullPath; // Ruta completa desde root
    
    // Constructor para directorios
    public DirectoryNode(String name, DirectoryNode parent) {
        this.name = name;
        this.type = NodeType.DIRECTORY;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.archivo = null;
        updateFullPath();
    }
    
    // Constructor para archivos
    public DirectoryNode(String name, Archivo archivo, DirectoryNode parent) {
        this.name = name;
        this.type = NodeType.FILE;
        this.parent = parent;
        this.children = null;
        this.archivo = archivo;
        updateFullPath();
    }
    
    private void updateFullPath() {
        if (parent == null) {
            this.fullPath = name;
        } else {
            if (parent.getFullPath().equals("/")) {
                this.fullPath = "/" + name;
            } else {
                this.fullPath = parent.getFullPath() + "/" + name;
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        updateFullPath();
    }
    
    public NodeType getType() {
        return type;
    }
    
    public DirectoryNode getParent() {
        return parent;
    }
    
    public List<DirectoryNode> getChildren() {
        return children;
    }
    
    public Archivo getArchivo() {
        return archivo;
    }
    
    public void setArchivo(Archivo archivo) {
        this.archivo = archivo;
    }
    
    public String getFullPath() {
        return fullPath;
    }
    
    // Calcular el tamaño del directorio (suma de todos los archivos)
    public int getSize() {
        if (type == NodeType.FILE) {
            return archivo != null ? archivo.getTamano() : 0;
        } else {
            int totalSize = 0;
            if (children != null) {
                for (DirectoryNode child : children) {
                    totalSize += child.getSize();
                }
            }
            return totalSize;
        }
    }
    
    // Agregar hijo
    public void addChild(DirectoryNode child) {
        if (type == NodeType.DIRECTORY && children != null) {
            children.add(child);
        }
    }
    
    // Eliminar hijo
    public boolean removeChild(DirectoryNode child) {
        if (type == NodeType.DIRECTORY && children != null) {
            return children.remove(child);
        }
        return false;
    }
    
    // Buscar hijo por nombre
    public DirectoryNode findChild(String name) {
        if (type == NodeType.DIRECTORY && children != null) {
            for (DirectoryNode child : children) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }
    
    // Verificar si es directorio
    public boolean isDirectory() {
        return type == NodeType.DIRECTORY;
    }
    
    // Verificar si es archivo
    public boolean isFile() {
        return type == NodeType.FILE;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

