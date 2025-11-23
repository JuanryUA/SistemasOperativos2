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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class PanelDiscoForm extends javax.swing.JPanel {

    // --- VARIABLES PROPIAS ---
    private List<JLabel> bloquesVisuales;
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
        this.bloquesVisuales = new ArrayList<>();
        
        // Limpiamos por si acaso
        panelCuadricula.removeAll();
        
        // Calculamos filas y columnas (8 columnas fijas)
        int columnas = 8;
        int filas = (int) Math.ceil((double) totalBloques / columnas);
        
        // Le decimos al panel interno que se comporte como una cuadrícula
        panelCuadricula.setLayout(new GridLayout(filas, columnas, 3, 3));

        // Creamos los cuadritos (Labels)
        for (int i = 0; i < totalBloques; i++) {
            JLabel bloque = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            bloque.setOpaque(true);
            bloque.setBackground(COLOR_LIBRE);
            bloque.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            bloque.setPreferredSize(new Dimension(35, 35)); // Tamaño del cuadrito
            
            bloquesVisuales.add(bloque); // Guardar en lista lógica
            panelCuadricula.add(bloque); // Añadir al panel visual
        }
        
        // Refrescar el panel para que aparezcan
        panelCuadricula.revalidate();
        panelCuadricula.repaint();
    }

    /**
     * Este método actualiza los colores rojo/verde según el Disk real
     * y muestra el nombre del archivo en los bloques ocupados
     */
    public void actualizarVista(Disk disk, FileSystem fileSystem) {
        if (bloquesVisuales == null) return;
        
        for (int i = 0; i < totalBloques; i++) {
            // Preguntamos al disco real
            boolean libre = disk.esBloqueLibre(i);
            JLabel bloqueVisual = bloquesVisuales.get(i);
            
            if (libre) {
                bloqueVisual.setBackground(COLOR_LIBRE);
                bloqueVisual.setText(String.valueOf(i)); // Mostrar número de bloque
                bloqueVisual.setToolTipText("Bloque " + i + " - Libre");
                bloqueVisual.setForeground(Color.BLACK);
                bloqueVisual.setFont(new Font(bloqueVisual.getFont().getName(), Font.BOLD, 12));
            } else {
                bloqueVisual.setBackground(COLOR_OCUPADO);
                // Obtener el nombre del archivo que ocupa este bloque
                String fileName = fileSystem != null ? fileSystem.getFileNameFromBlock(i) : null;
                if (fileName != null) {
                    // Mostrar nombre del archivo (truncado si es muy largo)
//                    String displayText = String.valueOf(i)+". \n"+(fileName.length() > 8 ? fileName.substring(0, 6) + ".." : fileName);
                    // 1. Procesamos el nombre del archivo
                    String nombreProcesado = (fileName.length() > 8 ? fileName.substring(0, 6) + ".." : fileName);

                    // 2. Construimos el String usando HTML y <br>
                    String displayText = "<html><center>" + i + "<br>" + nombreProcesado + "</center></html>";                    bloqueVisual.setText(displayText);
                    bloqueVisual.setToolTipText("Bloque " + i + " - Archivo: " + fileName);
                    // Ajustar fuente para que quepa mejor
                    bloqueVisual.setForeground(Color.WHITE);

                    // 3. Negrita (Bold)
                    // Usamos deriveFont para mantener la fuente actual y solo cambiar el estilo

                    // 4. Centrado horizontal del componente
                    bloqueVisual.setHorizontalAlignment(SwingConstants.CENTER);
                    bloqueVisual.setFont(new Font(bloqueVisual.getFont().getName(), Font.BOLD, 8));
                } else {
                    bloqueVisual.setText(String.valueOf(i));
                    bloqueVisual.setToolTipText("Bloque " + i + " - Ocupado");
                }
            }
        }
        // Repintar cambios
        panelCuadricula.repaint();
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con código existente
     */
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
