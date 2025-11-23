/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;
import CoreV2.Disk;
import CoreV2.FileSystem;
import CoreV2.OperatingSystem;
import CoreV2.Proceso;
import CoreV2.FileData;
import CoreV2.Cola;
import CoreV2.Nodo;
import CoreV2.Petition;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author verol
 */
public class SimuladorGUIForm extends javax.swing.JFrame {

    // --- 1. VARIABLES DEL BACKEND (AÑADIR ESTO) ---
    private Disk disk;
    private FileSystem fileSystem;
    private OperatingSystem so; 
    private Timer timerActualizacion;
    private java.util.Set<String> procesosProcesados = new java.util.HashSet<>(); // Para evitar mostrar popups duplicados

    /**
     * Constructor vacío para el diseñador visual (NetBeans lo usa)
     */
    public SimuladorGUIForm() {
        initComponents();
    }

    // --- 2. CONSTRUCTOR REAL (AÑADIR ESTO) ---
    // Este es el que llamarás desde el Main.java
    public SimuladorGUIForm(Disk disk, FileSystem fileSystem, OperatingSystem so) {
        this.disk = disk;
        this.fileSystem = fileSystem;
        this.so = so; 
        
        initComponents(); // Inicia lo visual
        
        configurarVentana(); // Configuración extra
        iniciarTimer();      // Arranca el refresco automático
    }
    
    // --- 3. MÉTODOS DE CONFIGURACIÓN (AÑADIR ESTO) ---
    private void configurarVentana() {
        this.setTitle("Simulador SO - Gestión de Archivos");
        this.setLocationRelativeTo(null); // Centrar en pantalla
        
        // Inicializar la cuadrícula del panel de disco
        if (miPanelDisco != null && disk != null) {
            miPanelDisco.inicializarCuadricula(disk.getTotalBloques());
        }
        
        // Configurar componentes adicionales
        configurarComponentesCRUD();
    }
    
    private void configurarComponentesCRUD() {
        // Crear y configurar el combo box de operaciones
        comboOperacion = new javax.swing.JComboBox<>(FileData.OperationType.values());
        comboOperacion1.setSelectedItem(FileData.OperationType.CREATE);
        comboOperacion1.addActionListener((ActionEvent e) -> {
            actualizarInputsSegunOperacion();
        });
//        getContentPane().add(comboOperacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 120, -1));
        
        // Crear label para operación
//        jLabelOperacion = new javax.swing.JLabel("Operación:");
//        getContentPane().add(jLabelOperacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        
        // Crear label y campo para nuevo nombre (UPDATE)
        jLabel3 = new javax.swing.JLabel("Nuevo Nombre");
        jLabel3.setVisible(false);
//        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, -1, -1));
        
        txtNuevoNombre = new javax.swing.JTextField();
        txtNuevoNombre.setVisible(false);
//        getContentPane().add(txtNuevoNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 80, -1));
        
        // Crear tabla para cola de procesos
        tablaColaProcesos = new javax.swing.JTable();
        tablaColaProcesos.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"Proceso", "Estado", "Operación", "Archivo"}
        ));
        jScrollPaneCola = new javax.swing.JScrollPane(tablaColaProcesos);
        jScrollPaneCola.setPreferredSize(new java.awt.Dimension(400, 200));
//        getContentPane().add(jScrollPaneCola, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, 400, 200));
        
        jLabelCola = new javax.swing.JLabel("Cola de Procesos:");
//        getContentPane().add(jLabelCola, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, -1, -1));
        
        // Inicializar visibilidad de inputs
        actualizarInputsSegunOperacion();
    }

    private void iniciarTimer() { 
        // Refrescar cada 100ms
        timerActualizacion = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarInterfaz();
            }
        });
        timerActualizacion.start();
    }

    private void refrescarInterfaz() {
        // Actualizar Disco (ahora también necesita FileSystem para mostrar nombres de archivos)
        if (miPanelDisco != null && disk != null) {
            miPanelDisco.actualizarVista(this.disk, this.fileSystem);
        }
        
        // Actualizar Tabla FAT
        if (miPanelTAA != null && fileSystem != null) {
            miPanelTAA.actualizarVista(this.fileSystem);
        }
        
        // Actualizar cola de procesos
        if (tablaColaProcesos1 != null && so != null) {
            actualizarColaProcesos();
        }
        
        // Verificar errores en operaciones completadas
        verificarErroresEnOperaciones();
    }
    
    private void verificarErroresEnOperaciones() {
        if (so == null) {
            return;
        }
        
        // Revisar todas las colas de procesos para encontrar operaciones completadas con errores
        Cola[] colas = {
            so.getColaListos(),
            so.getColaBloqueados(),
            so.getColaTerminados()
        };
        
        for (Cola cola : colas) {
            if (cola != null && !cola.isEmpty()) {
                Nodo actual = cola.getFrente();
                while (actual != null) {
                    Proceso p = actual.getProceso();
                    if (p != null && p.getFileData() != null) {
                        FileData data = p.getFileData();
                        String procesoId = p.getNombre();
                        
                        // Solo verificar operaciones RUD (no CREATE) que estén procesadas
                        if (data.getOperationType() != FileData.OperationType.CREATE && 
                            data.isIsProcessed() && data.hasError()) {
                            String key = procesoId + "_" + data.getOperationType() + "_" + data.getFileName();
                            // Solo mostrar una vez por proceso y operación
                            if (!procesosProcesados.contains(key)) {
                                procesosProcesados.add(key);
                                // Mostrar popup en el hilo de Swing
                                final String errorMsg = data.getErrorMessage();
                                final String opType = data.getOperationType().toString();
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(this, errorMsg, 
                                        "Error en " + opType, 
                                        JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        }
                    }
                    actual = actual.getSiguiente();
                }
            }
        }
        
        // También revisar la cola de peticiones del FileSystem
        if (fileSystem != null && fileSystem.getColaPeticiones() != null) {
            Cola colaPeticiones = fileSystem.getColaPeticiones();
            if (colaPeticiones != null && !colaPeticiones.isEmpty()) {
                Nodo actual = colaPeticiones.getFrente();
                while (actual != null) {
                    Petition peticion = actual.getPeticion();
                    if (peticion != null) {
                        FileData data = peticion.getFileData();
                        String procesoId = data.getProcessName();
                        
                        // Solo verificar operaciones RUD (no CREATE) que estén procesadas
                        if (data.getOperationType() != FileData.OperationType.CREATE && 
                            data.isIsProcessed() && data.hasError()) {
                            String key = procesoId + "_" + data.getOperationType() + "_" + data.getFileName();
                            if (!procesosProcesados.contains(key)) {
                                procesosProcesados.add(key);
                                final String errorMsg = data.getErrorMessage();
                                final String opType = data.getOperationType().toString();
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(this, errorMsg, 
                                        "Error en " + opType, 
                                        JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        }
                    }
                    actual = actual.getSiguiente();
                }
            }
        }
    }
    
    private void actualizarColaProcesos() {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tablaColaProcesos1.getModel();
        modelo.setRowCount(0);
        
        // Obtener todas las colas de procesos
        Cola colaNuevos = so.getColaNuevos();
        Cola colaListos = so.getColaListos();
        Cola colaBloqueados = so.getColaBloqueados();
        Cola colaTerminados = so.getColaTerminados();
        
        // Agregar procesos de cada cola
        agregarProcesosATabla(colaNuevos, "NUEVO", modelo);
        agregarProcesosATabla(colaListos, "LISTO", modelo);
        agregarProcesosATabla(colaBloqueados, "BLOQUEADO", modelo);
        agregarProcesosATabla(colaTerminados, "TERMINADO", modelo);
        
        // Agregar procesos en la cola de peticiones del FileSystem
        if (fileSystem != null && fileSystem.getColaPeticiones() != null) {
            Cola colaPeticiones = fileSystem.getColaPeticiones();
            if (colaPeticiones != null && !colaPeticiones.isEmpty()) {
                Nodo actual = colaPeticiones.getFrente();
                while (actual != null) {
                    Petition peticion = actual.getPeticion();
                    if (peticion != null) {
                        FileData data = peticion.getFileData();
                        String operacion = data.getOperationType().toString();
                        String nombreArchivo = data.getFileName();
                        String proceso = data.getProcessName() != null ? data.getProcessName() : "N/A";
                        modelo.addRow(new Object[]{proceso, "EN COLA", operacion, nombreArchivo});
                    }
                    actual = actual.getSiguiente();
                }
            }
        }
    }
    
    private void agregarProcesosATabla(Cola cola, String estado, javax.swing.table.DefaultTableModel modelo) {
        if (cola != null && !cola.isEmpty()) {
            Nodo actual = cola.getFrente();
            while (actual != null) {
                Proceso p = actual.getProceso();
                if (p != null && p.getFileData() != null) {
                    FileData data = p.getFileData();
                    String operacion = data.getOperationType().toString();
                    String nombreArchivo = data.getFileName();
                    modelo.addRow(new Object[]{p.getNombre(), estado, operacion, nombreArchivo});
                }
                actual = actual.getSiguiente();
            }
        }
    }
    
    private void actualizarInputsSegunOperacion() {
        FileData.OperationType op = (FileData.OperationType) comboOperacion1.getSelectedItem();
        
        // Ocultar todos primero
        jLabel1.setVisible(false);
        jLabel2.setVisible(false);
        labelNuevoNombre.setVisible(false);
        txtCrearNombre.setVisible(false);
        txtCrearTamano.setVisible(false);
        txtNuevoNombre1.setVisible(false);
        
        switch (op) {
            case CREATE:
                jLabel1.setVisible(true);
                jLabel2.setVisible(true);
                jLabel1.setText("Nombre");
                jLabel2.setText("Tamaño");
                txtCrearNombre.setVisible(true);
                txtCrearTamano.setVisible(true);
                txtNuevoNombre1.setVisible(false);
                btnCrear.setText("CREAR");
                break;
            case READ:
                jLabel1.setVisible(true);
                jLabel1.setText("Nombre");
                txtCrearNombre.setVisible(true);
                txtCrearTamano.setVisible(false);
                txtNuevoNombre1.setVisible(false);
                btnCrear.setText("LEER");
                break;
            case UPDATE:
                jLabel1.setVisible(true);
                labelNuevoNombre.setVisible(true);
                jLabel1.setText("Nombre");
                labelNuevoNombre.setText("Nuevo Nombre");
                txtCrearNombre.setVisible(true);
                txtNuevoNombre1.setVisible(true);
                txtCrearTamano.setVisible(false);
                btnCrear.setText("ACTUALIZAR");
                break;
            case DELETE:
                jLabel1.setVisible(true);
                jLabel1.setText("Nombre");
                txtCrearNombre.setVisible(true);
                txtCrearTamano.setVisible(false);
                txtNuevoNombre1.setVisible(false);
                btnCrear.setText("ELIMINAR");
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCrearNombre = new javax.swing.JTextField();
        txtCrearTamano = new javax.swing.JTextField();
        btnCrear = new javax.swing.JButton();
        panelIzq = new javax.swing.JPanel();
        miPanelTAA = new Interfaces.PanelTAAForm();
        miPanelDisco = new Interfaces.PanelDiscoForm();
        jLabel5 = new javax.swing.JLabel();
        jScrollPaneCola1 = new javax.swing.JScrollPane();
        tablaColaProcesos1 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        comboOperacion1 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtNuevoNombre1 = new javax.swing.JTextField();
        labelNuevoNombre = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Nombre");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, -1, -1));

        jLabel2.setText("Tamaño");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 180, -1, -1));
        getContentPane().add(txtCrearNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 130, 110, -1));

        txtCrearTamano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCrearTamanoActionPerformed(evt);
            }
        });
        getContentPane().add(txtCrearTamano, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 180, 110, -1));

        btnCrear.setText("CREAR");
        btnCrear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearActionPerformed(evt);
            }
        });
        getContentPane().add(btnCrear, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 230, 110, 30));

        panelIzq.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        panelIzq.add(miPanelTAA, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 490, 110));
        panelIzq.add(miPanelDisco, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 350, 440, 320));

        jLabel5.setText("Tabla de Asignaciones");
        panelIzq.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, -1, -1));

        tablaColaProcesos1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Proceso", "Operación", "Estado", "Archivo"
            }
        ));
        jScrollPaneCola1.setViewportView(tablaColaProcesos1);

        panelIzq.add(jScrollPaneCola1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 480, 100));

        jLabel6.setText("Visualización de Procesos");
        panelIzq.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        jLabel8.setText("Disco Secundario (SD)");
        panelIzq.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 320, -1, -1));

        getContentPane().add(panelIzq, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 0, 520, 690));

        comboOperacion1.setModel(new javax.swing.DefaultComboBoxModel<>(FileData.OperationType.values()));
        comboOperacion1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboOperacion1ActionPerformed(evt);
            }
        });
        getContentPane().add(comboOperacion1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, 110, 30));

        jLabel4.setText("Operación");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 90, -1, -1));
        getContentPane().add(txtNuevoNombre1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 180, 110, -1));

        labelNuevoNombre.setText("Nuevo Nombre");
        getContentPane().add(labelNuevoNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 20));

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel7.setText("Directorio");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 310, -1, -1));

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel9.setText("Archivo");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Modifica este método que ya tienes (o crea uno nuevo para el botón Crear)
    // Si le das doble clic al botón CREAR en "Design", te llevará aquí.
    
    private void txtCrearTamanoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCrearTamanoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCrearTamanoActionPerformed

    private void btnCrearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearActionPerformed
        try {
            FileData.OperationType op = (FileData.OperationType) comboOperacion1.getSelectedItem();
            String nombre = txtCrearNombre.getText();
            
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese el nombre del archivo.");
                return;
            }
            
            if (so == null) {
                JOptionPane.showMessageDialog(this, "Error: No hay conexión con el SO");
                return;
            }
            
            switch (op) {
                case CREATE:
                    String tamanoStr = txtCrearTamano.getText();
                    if (tamanoStr.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Por favor ingrese el tamaño.");
                        return;
                    }
                    int tamano = Integer.parseInt(tamanoStr);
                    if (tamano <= 0) {
                        JOptionPane.showMessageDialog(this, "El tamaño debe ser mayor a 0.");
                        return;
                    }
                    System.out.println("GUI: Creando proceso para archivo: " + nombre);
                    so.crearProceso(Proceso.Tipo.IO_BOUND, 0, nombre, tamano);
                    txtCrearNombre.setText("");
                    txtCrearTamano.setText("");
                    break;
                    
                case READ:
                    System.out.println("GUI: Creando proceso para leer archivo: " + nombre);
                    so.crearProcesoIO(FileData.OperationType.READ, nombre);
                    txtCrearNombre.setText("");
                    break;
                    
                case UPDATE:
                    String nuevoNombre = txtNuevoNombre1.getText();
                    if (nuevoNombre.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Por favor ingrese el nuevo nombre.");
                        return;
                    }
                    System.out.println("GUI: Creando proceso para actualizar archivo: " + nombre + " -> " + nuevoNombre);
                    so.crearProcesoIO(FileData.OperationType.UPDATE, nombre, nuevoNombre);
                    txtCrearNombre.setText("");
                    txtNuevoNombre1.setText("");
                    break;
                    
                case DELETE:
                    System.out.println("GUI: Creando proceso para eliminar archivo: " + nombre);
                    so.crearProcesoIO(FileData.OperationType.DELETE, nombre);
                    txtCrearNombre.setText("");
                    break;
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El tamaño debe ser un número entero.");
        }
    }//GEN-LAST:event_btnCrearActionPerformed

    private void comboOperacion1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboOperacion1ActionPerformed
            System.out.println("prueba");
    }//GEN-LAST:event_comboOperacion1ActionPerformed
    

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(SimuladorGUIForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(SimuladorGUIForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(SimuladorGUIForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(SimuladorGUIForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new SimuladorGUIForm().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCrear;
    private javax.swing.JComboBox<FileData.OperationType> comboOperacion1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPaneCola1;
    private javax.swing.JLabel labelNuevoNombre;
    private Interfaces.PanelDiscoForm miPanelDisco;
    private Interfaces.PanelTAAForm miPanelTAA;
    private javax.swing.JPanel panelIzq;
    private javax.swing.JTable tablaColaProcesos1;
    private javax.swing.JTextField txtCrearNombre;
    private javax.swing.JTextField txtCrearTamano;
    private javax.swing.JTextField txtNuevoNombre1;
    // End of variables declaration//GEN-END:variables
    
    // Variables adicionales para CRUD
    private javax.swing.JComboBox<FileData.OperationType> comboOperacion;
    private javax.swing.JLabel jLabelOperacion;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField txtNuevoNombre;
    private javax.swing.JTable tablaColaProcesos;
    private javax.swing.JScrollPane jScrollPaneCola;
    private javax.swing.JLabel jLabelCola;
}
