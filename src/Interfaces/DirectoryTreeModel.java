/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;

import CoreV2.DirectoryNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * Modelo personalizado para el JTree de directorios
 * @author verol
 */
public class DirectoryTreeModel extends DefaultTreeModel {
    
    public DirectoryTreeModel(DirectoryNode root) {
        super(new DirectoryTreeNode(root));
    }
    
    /**
     * Wrapper para DirectoryNode que implementa TreeNode
     */
    private static class DirectoryTreeNode implements TreeNode {
        private DirectoryNode node;
        
        public DirectoryTreeNode(DirectoryNode node) {
            this.node = node;
        }
        
        @Override
        public TreeNode getChildAt(int childIndex) {
            if (node.isDirectory() && node.getChildren() != null && childIndex < node.getChildren().size()) {
                return new DirectoryTreeNode(node.getChildren().get(childIndex));
            }
            return null;
        }
        
        @Override
        public int getChildCount() {
            if (node.isDirectory() && node.getChildren() != null) {
                return node.getChildren().size();
            }
            return 0;
        }
        
        @Override
        public TreeNode getParent() {
            if (node.getParent() != null) {
                return new DirectoryTreeNode(node.getParent());
            }
            return null;
        }
        
        @Override
        public int getIndex(TreeNode node) {
            if (this.node.isDirectory() && this.node.getChildren() != null) {
                DirectoryNode targetNode = ((DirectoryTreeNode) node).node;
                return this.node.getChildren().indexOf(targetNode);
            }
            return -1;
        }
        
        @Override
        public boolean getAllowsChildren() {
            return node.isDirectory();
        }
        
        @Override
        public boolean isLeaf() {
            return node.isFile();
        }
        
        @Override
        public java.util.Enumeration<? extends TreeNode> children() {
            if (node.isDirectory() && node.getChildren() != null) {
                java.util.Vector<TreeNode> children = new java.util.Vector<>();
                for (DirectoryNode child : node.getChildren()) {
                    children.add(new DirectoryTreeNode(child));
                }
                return children.elements();
            }
            return null;
        }
        
        public DirectoryNode getNode() {
            return node;
        }
        
        @Override
        public String toString() {
            return node.toString();
        }
    }
    
    /**
     * Obtiene el DirectoryNode desde un TreeNode
     */
    public DirectoryNode getDirectoryNode(Object node) {
        if (node instanceof DirectoryTreeNode) {
            return ((DirectoryTreeNode) node).getNode();
        }
        return null;
    }
    
    /**
     * Actualiza el árbol después de cambios
     */
    public void refresh() {
        reload();
    }
}

