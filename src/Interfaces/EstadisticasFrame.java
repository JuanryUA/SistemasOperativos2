package Interfaces;

import CoreV2.FileSystem;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

public class EstadisticasFrame extends JFrame {
    
    private JTextArea textArea;
    private FileSystem fileSystem;
    private Timer timer;

    public EstadisticasFrame(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        initComponents();
        
        // Timer para refrescar cada 0.5 segundos
        timer = new Timer(500, e -> actualizarTexto());
        timer.start();
    }
    
    private void initComponents() {
        this.setTitle("Estadísticas de Disco");
        this.setSize(400, 300);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        this.add(new JScrollPane(textArea), BorderLayout.CENTER);
    }
    
    private void actualizarTexto() {
        if (fileSystem != null) {
            // Llama al método del FileSystem que usa TUS mapas
            textArea.setText(fileSystem.obtenerEstadisticasTexto());
        }
    }
    
    @Override
    public void dispose() {
        if (timer != null) timer.stop();
        super.dispose();
    }
}