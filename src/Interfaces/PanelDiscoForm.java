/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;

/**
 *
 * @author verol
 */
import CoreV2.Disk;
import CoreV2.FileSystem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
//import java.util.ArrayList;
//import java.util.List;
import CoreV2.Lista;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PanelDiscoForm extends javax.swing.JPanel {

    // --- VARIABLES PROPIAS ---
    private Lista<JLabel> bloquesVisuales;
    private int totalBloques;
    
    // Colores para los estados
    private final Color COLOR_LIBRE = new Color(144, 238, 144); // Verde
    private final Color COLOR_OCUPADO = new Color(255, 102, 102); // Rojo

    public PanelDiscoForm() {
        initComponents(); // Esto inicia los componentes visuales (el panelCuadricula)
    }

    /**
     * Este método lo llamaremos desde el Main o la Ventana Principal
     * para generar los cuadritos dinámicamente.
     */
    public void inicializarCuadricula(int totalBloques) {
        this.totalBloques = totalBloques;
        
        this.bloquesVisuales = new Lista<>();
        
        // 1. Configuración del panel contenedor
        panelCuadricula.removeAll();
        panelCuadricula.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        // Calculamos filas (8 columnas fijas)
        int columnas = 8;
        int filas = (int) Math.ceil((double) totalBloques / columnas);
        
        // 2. Panel interno
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(filas, columnas, 3, 3)); // Espacio de 3px entre bloques
        gridPanel.setOpaque(false);

        // Creamos los cuadritos
        for (int i = 0; i < totalBloques; i++) {
            JLabel bloque = new JLabel("", SwingConstants.CENTER); // Texto vacio al inicio
            bloque.setOpaque(true);
            bloque.setBackground(COLOR_LIBRE);
            bloque.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            
            // --- CAMBIO SOLICITADO: 35 de ancho x 50 de alto ---
            bloque.setPreferredSize(new Dimension(35, 45)); 
            
            // Fuente base pequeña
            bloque.setFont(new Font("SansSerif", Font.PLAIN, 10)); 
            
            bloquesVisuales.add(bloque); 
            gridPanel.add(bloque); 
        }
        
        panelCuadricula.add(gridPanel);
        panelCuadricula.revalidate();
        panelCuadricula.repaint();
    }

    /**
     * Actualiza la vista mostrando ID arriba y Nombre abajo
     */
    public void actualizarVista(Disk disk, FileSystem fileSystem) {
        if (bloquesVisuales == null) return;
        
        for (int i = 0; i < totalBloques; i++) {
            boolean libre = disk.esBloqueLibre(i);
            JLabel bloqueVisual = bloquesVisuales.get(i);
            
            if (libre) {
                bloqueVisual.setBackground(COLOR_LIBRE);
                bloqueVisual.setForeground(Color.BLACK);
                // Solo mostramos el número centrado
                bloqueVisual.setText(String.valueOf(i));
                bloqueVisual.setToolTipText("Bloque " + i + " - Libre");
                bloqueVisual.setFont(new Font("SansSerif", Font.PLAIN, 10));
            } else {
                bloqueVisual.setBackground(COLOR_OCUPADO);
                bloqueVisual.setForeground(Color.WHITE);
                
                String fileName = fileSystem != null ? fileSystem.getFileNameFromBlock(i) : null;
                
                if (fileName != null) {
                    // Recortamos el nombre si es muy largo para que quepa en 35px de ancho
                    // (Aprox caben 4 o 5 letras antes de romperse)
                    String nombreMostrar = (fileName.length() > 5 ? fileName.substring(0, 3) + ".." : fileName);
                    
                    // --- AQUI ESTA LA MAGIA DEL HTML ---
                    // <center> para centrar todo
                    // i + "<br>" pone el ID y un salto de linea
                    // nombreMostrar pone el archivo abajo
                    String htmlText = "<html><center>" + i + "<br>" + nombreMostrar + "</center></html>";
                    
                    bloqueVisual.setText(htmlText);
                    bloqueVisual.setToolTipText("Bloque " + i + " - Archivo: " + fileName); // Nombre completo al pasar mouse
                    
                    // Letra un poco más chica y negrita para que se lea bien en blanco
                    bloqueVisual.setFont(new Font("SansSerif", Font.BOLD, 9)); 
                } else {
                    // Si está ocupado pero no sabemos el nombre (raro), solo mostramos ID
                    bloqueVisual.setText(String.valueOf(i));
                    bloqueVisual.setToolTipText("Bloque " + i + " - Ocupado");
                }
            }
        }
        panelCuadricula.repaint();
    }
    
    public void actualizarVista(Disk disk) {
        actualizarVista(disk, null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCuadricula = new javax.swing.JPanel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelCuadricula.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        add(panelCuadricula, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 300));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelCuadricula;
    // End of variables declaration//GEN-END:variables
}
