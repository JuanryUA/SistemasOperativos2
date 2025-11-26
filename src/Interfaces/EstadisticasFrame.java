package Interfaces;

import CoreV2.FileSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class EstadisticasFrame extends JFrame {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private FileSystem fileSystem;
    private Timer timer;

    public EstadisticasFrame(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        initComponents();
        
        // Timer para refrescar cada 0.5 segundos
        timer = new Timer(500, e -> actualizarTabla());
        timer.start();
    }
    
    private void initComponents() {
        this.setTitle("Estadísticas de Disco");
        this.setSize(500, 350); // Un poco más ancho para que quepa bien
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // 1. Panel superior con un título bonito
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 240, 240));
        JLabel titleLabel = new JLabel("Métricas de Rendimiento");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(titleLabel);
        this.add(topPanel, BorderLayout.NORTH);

        // 2. Configuración de la Tabla
        String[] columnas = {"Algoritmo", "Tiempo Promedio", "Peticiones Atendidas"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Para que el usuario no pueda editar la tabla
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30); // Filas más altas para que se vea mejor
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(220, 220, 220));
        
        // Centrar el texto de las celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        this.add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    private void actualizarTabla() {
        if (fileSystem != null) {
            // Obtenemos el texto feo original: "FIFO : 3320.71 ms (7 peticiones)..."
            String textoCompleto = fileSystem.obtenerEstadisticasTexto();
            
            // Limpiamos la tabla actual
            tableModel.setRowCount(0);
            
            // Procesamos línea por línea para llenar la tabla
            if (textoCompleto != null && !textoCompleto.isEmpty()) {
                String[] lineas = textoCompleto.split("\n");
                
                for (String linea : lineas) {
                    // Ignoramos líneas vacías o títulos decorativos (===)
                    if (linea.trim().isEmpty() || linea.contains("===")) {
                        continue;
                    }
                    
                    // Intentamos interpretar el formato: "ALGORITMO : TIEMPO ms (NUM peticiones)"
                    // Ejemplo: "FIFO : 3320.71 ms (7 peticiones)"
                    try {
                        if (linea.contains(":")) {
                            String[] partes = linea.split(":");
                            String nombreAlgoritmo = partes[0].trim(); // "FIFO"
                            
                            String resto = partes[1].trim(); // "3320.71 ms (7 peticiones)"
                            
                            // Separar el tiempo del resto
                            String[] datos = resto.split("ms");
                            String tiempo = datos[0].trim() + " ms"; // "3320.71 ms"
                            
                            String peticiones = "";
                            if (datos.length > 1) {
                                peticiones = datos[1].replace("(", "").replace(")", "").trim(); // "7 peticiones"
                            }
                            
                            // Agregamos la fila bonita a la tabla
                            tableModel.addRow(new Object[]{nombreAlgoritmo, tiempo, peticiones});
                        }
                    } catch (Exception e) {
                        // Si alguna línea tiene un formato raro, no la agregamos para no romper el programa
                    }
                }
            }
        }
    }
    
    @Override
    public void dispose() {
        if (timer != null) timer.stop();
        super.dispose();
    }
}