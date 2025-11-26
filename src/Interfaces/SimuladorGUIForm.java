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
import CoreV2.Lista;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import CoreV2.Directorio;
import CoreV2.Archivo;
import CoreV2.DiskStrategies.ISchedullingDiskAlgorithm;
import CoreV2.DiskStrategies.FIFODisk;
import CoreV2.DiskStrategies.SSTFDisk;
import CoreV2.DiskStrategies.SCANDisk;
import CoreV2.DiskStrategies.CSCANDisk;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
    private javax.swing.JTree jTreeDirectorio; // JTree for directory structure
    private String modoUsuario = "Administrador"; // Modo actual: "Administrador" o "Usuario"
    private javax.swing.JScrollPane jScrollPaneTree; // Scroll pane for JTree
    private javax.swing.JTextField txtRutaDirectorio; // Directory path input
    private javax.swing.JButton btnCrearDirectorio; // Button to create directory
    private javax.swing.JButton btnEliminarDirectorio; // Button to delete directory
    private javax.swing.JLabel labelInfoNombre; // Label to display selected item name
    private javax.swing.JLabel labelInfoTamano; // Label to display selected item size
    private javax.swing.JPanel panelInfo; // Panel to display file/directory info

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
      
        java.io.PrintStream originalOut = System.out;
        
       
        java.io.PrintStream printStream = new java.io.PrintStream(new CustomOutputStream(txtLog, lblTic, originalOut));       
        
        System.setOut(printStream); // Redirige la salida estándar (System.out.println)
        System.setErr(printStream); // Opcional: Redirige también los errores (System.err.println)
        
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
        configurarJTree();
        configurarDiskSchedulerCombo();
        actualizarVisibilidadSegunModo(); // Inicializar visibilidad según modo
    }
    
    private void actualizarVisibilidadSegunModo() {
        boolean esAdmin = "Administrador".equals(modoUsuario);
        
        // Botones de directorio solo visibles para admin
        btnCrearDirectorio1.setVisible(esAdmin);
        btnEliminarDirectorio1.setVisible(esAdmin);
        jLabel11.setVisible(esAdmin); // Label "Directorio"
        
        // ComboBox de política de planificación solo visible para admin
        diskSchedulingCombo1.setVisible(esAdmin);
        jLabel12.setVisible(esAdmin); // Label "Política de Planificación"
        
        // Filtrar opciones del combo de operaciones según el modo
        FileData.OperationType[] opcionesAdmin = {
            FileData.OperationType.CREATE,
            FileData.OperationType.READ,
            FileData.OperationType.UPDATE,
            FileData.OperationType.DELETE
        };
        FileData.OperationType[] opcionesUsuario = {
            FileData.OperationType.CREATE,
            FileData.OperationType.READ
        };
        
        FileData.OperationType seleccionado = (FileData.OperationType) comboOperacion1.getSelectedItem();
        comboOperacion1.setModel(new javax.swing.DefaultComboBoxModel<>(
            esAdmin ? opcionesAdmin : opcionesUsuario
        ));
        
        // Si estaba en UPDATE o DELETE y cambiamos a modo Usuario, cambiar a CREATE
        if (!esAdmin && (seleccionado == FileData.OperationType.UPDATE || 
                         seleccionado == FileData.OperationType.DELETE)) {
            comboOperacion1.setSelectedItem(FileData.OperationType.CREATE);
        } else if (esAdmin && seleccionado != null) {
            // Restaurar selección si es admin
            comboOperacion1.setSelectedItem(seleccionado);
        }
        
        // Actualizar inputs según la operación seleccionada
        actualizarInputsSegunOperacion();
    }
    
    private void configurarJTree() {
        // Create JTree
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        jTreeDirectorio = new javax.swing.JTree(rootNode);
        jTreeDirectorio.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTreeDirectorio.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                actualizarInfoSeleccionada();
            }
        });
        
        jScrollPaneTree = new javax.swing.JScrollPane(jTreeDirectorio);
        jScrollPaneTree.setPreferredSize(new java.awt.Dimension(300, 400));
        
        // Create directory path input
        txtRutaDirectorio = new javax.swing.JTextField();
        txtRutaDirectorio1.setText("root");
        txtRutaDirectorio1.setPreferredSize(new java.awt.Dimension(200, 25));
        
        // Create directory buttons

        btnEliminarDirectorio = new javax.swing.JButton("Eliminar Directorio");
        btnEliminarDirectorio1.addActionListener((ActionEvent e) -> {
            eliminarDirectorio();
        });
        
        // Create info panel
        panelInfo = new javax.swing.JPanel();
        panelInfo.setLayout(new java.awt.FlowLayout());
        labelInfoNombre = new javax.swing.JLabel("Nombre: -");
        labelInfoTamano = new javax.swing.JLabel("Tamaño: -");
        panelInfo.add(labelInfoNombre);
        panelInfo.add(labelInfoTamano);
        
        // Add components to GUI (using absolute layout)
        getContentPane().add(jScrollPaneTree, new org.netbeans.lib.awtextra.AbsoluteConstraints(265, 120, 280, 300));
//        getContentPane().add(new javax.swing.JLabel("Ruta:"), new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 450, -1, -1));
//        getContentPane().add(txtRutaDirectorio, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 450, 200, 25));
//        getContentPane().add(btnCrearDirectorio, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 480, 120, 30));
//        getContentPane().add(btnEliminarDirectorio, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 480, 130, 30));
        getContentPane().add(panelInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(243, 425, 300, 50));
        getContentPane().add(new javax.swing.JLabel("<html><span style='font-size:10px; font-family:Tahoma'>Estructura de Directorios</span></html>"), new org.netbeans.lib.awtextra.AbsoluteConstraints(268, 83, -1, -1));
    }
    
    private void crearDirectorio() {
        if (fileSystem == null) {
            JOptionPane.showMessageDialog(this, "Error: No hay conexion con el FileSystem");
            return;
        }
        
        String rutaPadre = txtRutaDirectorio1.getText().trim();
        if (rutaPadre.isEmpty()) {
            rutaPadre = "root";
        }
        
        String nombreDirectorio = JOptionPane.showInputDialog(this, "Ingrese el nombre del directorio:", "Crear Directorio", JOptionPane.QUESTION_MESSAGE);
        if (nombreDirectorio != null && !nombreDirectorio.trim().isEmpty()) {
            String error = fileSystem.crearDirectorio(rutaPadre, nombreDirectorio.trim());

                if (error != null) {
                    // ¡Si nos devolvió texto, es un error! Mostramos el Pop-up.
                    JOptionPane.showMessageDialog(this, error, "Error al crear directorio", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Si devolvió null, fue un éxito. Actualizamos el árbol.
                    actualizarJTree();
                }
        }
    }
    
    private void eliminarDirectorio() {
        if (fileSystem == null) {
            JOptionPane.showMessageDialog(this, "Error: No hay conexion con el FileSystem");
            return;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeDirectorio.getLastSelectedPathComponent();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un directorio para eliminar.");
            return;
        }
        
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof String) {
            String nombre = (String) userObject;
            if (nombre.equals("root")) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar el directorio root.");
                return;
            }
            
            // Get parent path
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            String rutaPadre = "root";
            if (parentNode != null && parentNode.getUserObject() instanceof String) {
                String parentName = (String) parentNode.getUserObject();
                if (!parentName.equals("root")) {
                    // Build path from root
                    java.util.List<String> pathParts = new java.util.ArrayList<>();
                    DefaultMutableTreeNode current = parentNode;
                    while (current != null && current.getUserObject() instanceof String) {
                        String part = (String) current.getUserObject();
                        if (!part.equals("root")) {
                            pathParts.add(0, part);
                        }
                        current = (DefaultMutableTreeNode) current.getParent();
                    }
                    if (!pathParts.isEmpty()) {
                        rutaPadre = "root/" + String.join("/", pathParts);
                    }
                }
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar el directorio '" + nombre + "' y todo su contenido?", 
                "Confirmar eliminación", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                fileSystem.eliminarDirectorio(rutaPadre, nombre);
                actualizarJTree();
            }
        }
    }
    
    private void actualizarInfoSeleccionada() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeDirectorio.getLastSelectedPathComponent();
        if (selectedNode == null) {
            labelInfoNombre.setText("Nombre: -");
            labelInfoTamano.setText("Tamaño: -");
            return;
        }
        
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof String) {
            // It's a directory
            String nombre = (String) userObject;
            labelInfoNombre.setText("Nombre: " + nombre);
            
            // Calculate directory size
            if (fileSystem != null) {
                String ruta = construirRuta(selectedNode);
                CoreV2.Directorio dir = buscarDirectorioPorRuta(ruta);
                if (dir != null) {
                    int tamano = dir.calcularTamano();
                    labelInfoTamano.setText("Tamaño: " + tamano + " bloques");
                } else {
                    labelInfoTamano.setText("Tamaño: -");
                }
            }
        } else if (userObject instanceof Archivo) {
            // It's a file
            Archivo arch = (Archivo) userObject;
            labelInfoNombre.setText("Nombre: " + arch.getNombre());
            labelInfoTamano.setText("Tamaño: " + arch.getTamano() + " bloques");
        }
    }
    
    private String construirRuta(DefaultMutableTreeNode node) {
        java.util.List<String> pathParts = new java.util.ArrayList<>();
        DefaultMutableTreeNode current = node;
        while (current != null && current.getUserObject() instanceof String) {
            String part = (String) current.getUserObject();
            pathParts.add(0, part);
            current = (DefaultMutableTreeNode) current.getParent();
        }
        return String.join("/", pathParts);
    }
    
    private CoreV2.Directorio buscarDirectorioPorRuta(String ruta) {
        if (fileSystem == null) return null;
        // Use reflection or add a public method to FileSystem
        // For now, we'll navigate manually
        if (ruta.equals("root")) {
            return fileSystem.getRoot();
        }
        
        String path = ruta.startsWith("root/") ? ruta.substring(5) : ruta;
        if (path.isEmpty()) {
            return fileSystem.getRoot();
        }
        
        String[] partes = path.split("/");
        CoreV2.Directorio actual = fileSystem.getRoot();
        
        for (String parte : partes) {
            if (parte.isEmpty()) continue;
            CoreV2.Directorio siguiente = actual.buscarSubdirectorio(parte);
            if (siguiente == null) {
                return null;
            }
            actual = siguiente;
        }
        
        return actual;
    }
    
    private void actualizarJTree() {
        if (fileSystem == null) return;
        
        // Save current selection path
        javax.swing.tree.TreePath selectedPath = jTreeDirectorio.getSelectionPath();
        Object selectedObject = null;
        if (selectedPath != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (selectedNode != null) {
                selectedObject = selectedNode.getUserObject();
            }
        }
        
        // Save expanded state
        java.util.Set<Object> expandedObjects = new java.util.HashSet<>();
        for (int i = 0; i < jTreeDirectorio.getRowCount(); i++) {
            javax.swing.tree.TreePath path = jTreeDirectorio.getPathForRow(i);
            if (jTreeDirectorio.isExpanded(path)) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node != null) {
                    expandedObjects.add(node.getUserObject());
                }
            }
        }
        
        // Rebuild tree
        DefaultMutableTreeNode rootNode = construirArbolDirectorio(fileSystem.getRoot());
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        jTreeDirectorio.setModel(model);
        
        // Restore expanded state
        restoreExpandedState(jTreeDirectorio, rootNode, expandedObjects);
        
        // Restore selection
        if (selectedObject != null) {
            restoreSelection(jTreeDirectorio, rootNode, selectedObject);
            // Update info panel after restoring selection
            actualizarInfoSeleccionada();
        } else {
            // Expand root by default if nothing was selected
            for (int i = 0; i < jTreeDirectorio.getRowCount(); i++) {
                jTreeDirectorio.expandRow(i);
            }
        }
    }
    
    private void restoreExpandedState(javax.swing.JTree tree, DefaultMutableTreeNode node, java.util.Set<Object> expandedObjects) {
        if (node == null) return;
        
        Object userObject = node.getUserObject();
        if (expandedObjects.contains(userObject)) {
            javax.swing.tree.TreePath path = new javax.swing.tree.TreePath(node.getPath());
            tree.expandPath(path);
        }
        
        // Recursively check children
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            restoreExpandedState(tree, child, expandedObjects);
        }
    }
    
    private boolean restoreSelection(javax.swing.JTree tree, DefaultMutableTreeNode node, Object targetObject) {
        if (node == null) return false;
        
        Object userObject = node.getUserObject();
        boolean found = false;
        
        // For String (directories), compare by value
        if (userObject instanceof String && targetObject instanceof String) {
            if (userObject.equals(targetObject)) {
                found = true;
            }
        }
        // For Archivo objects, compare by name and path
        else if (userObject instanceof Archivo && targetObject instanceof Archivo) {
            Archivo arch1 = (Archivo) userObject;
            Archivo arch2 = (Archivo) targetObject;
            if (arch1.getNombre().equals(arch2.getNombre()) && 
                arch1.getRuta().equals(arch2.getRuta())) {
                found = true;
            }
        }
        // For other cases, use equals
        else if (userObject != null && userObject.equals(targetObject)) {
            found = true;
        }
        
        if (found) {
            javax.swing.tree.TreePath path = new javax.swing.tree.TreePath(node.getPath());
            // Expand all parent nodes
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            while (parent != null) {
                javax.swing.tree.TreePath parentPath = new javax.swing.tree.TreePath(parent.getPath());
                tree.expandPath(parentPath);
                parent = (DefaultMutableTreeNode) parent.getParent();
            }
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            return true;
        }
        
        // Recursively search children
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (restoreSelection(tree, child, targetObject)) {
                return true;
            }
        }
        
        return false;
    }
    
    private DefaultMutableTreeNode construirArbolDirectorio(CoreV2.Directorio directorio) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(directorio.getNombre());
        boolean esAdmin = "Administrador".equals(modoUsuario);
        
        // Add subdirectories
        Lista<Directorio> subdirs = directorio.getSubdirectorios();
        for (int i = 0; i < subdirs.size(); i++) {
            Directorio subdir = subdirs.get(i);
            DefaultMutableTreeNode subdirNode = construirArbolDirectorio(subdir);
            node.add(subdirNode);
        }
        
        // Add files (filtrar según modo de usuario)
        Lista<Archivo> archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.size(); i++) {
            Archivo arch = archivos.get(i);
            // Solo mostrar archivos públicos si es modo Usuario, o todos si es Admin
            if (esAdmin || "publico".equals(arch.getTipoArchivo())) {
                DefaultMutableTreeNode archNode = new DefaultMutableTreeNode(arch);
                node.add(archNode);
            }
        }
        
        return node;
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
    
    private void configurarDiskSchedulerCombo() {
        // Configure existing diskSchedulingCombo combobox
        if (diskSchedulingCombo1 == null) {
            return; // ComboBox doesn't exist yet
        }
        
        // Set model with disk scheduling algorithm types
        diskSchedulingCombo1.setModel(new javax.swing.DefaultComboBoxModel<>(ISchedullingDiskAlgorithm.SchedulingDiskType.values()));
        
        // Set initial value based on current algorithm or default to FIFO
        ISchedullingDiskAlgorithm.SchedulingDiskType initialType = ISchedullingDiskAlgorithm.SchedulingDiskType.FIFO;
        if (fileSystem != null && fileSystem.getDiskScheduler() != null) {
            ISchedullingDiskAlgorithm algoritmoActual = fileSystem.getDiskScheduler().getAlgoritmo();
            if (algoritmoActual != null) {
                initialType = algoritmoActual.getSchedulingDiskType();
            }
        }
        diskSchedulingCombo1.setSelectedItem(initialType);
        
        // Add action listener to change algorithm when selected
        diskSchedulingCombo1.addActionListener((ActionEvent e) -> {
            cambiarAlgoritmoDisk();
        });
    }
    
    private void cambiarAlgoritmoDisk() {
        if (fileSystem == null || fileSystem.getDiskScheduler() == null || diskSchedulingCombo1 == null) {
            return;
        }
        
        ISchedullingDiskAlgorithm.SchedulingDiskType selected = 
            (ISchedullingDiskAlgorithm.SchedulingDiskType) diskSchedulingCombo1.getSelectedItem();
        
        if (selected == null) {
            return;
        }
        
        ISchedullingDiskAlgorithm nuevoAlgoritmo = null;
        
        switch (selected) {
            case FIFO:
                nuevoAlgoritmo = new FIFODisk();
                break;
            case SSTF:
                nuevoAlgoritmo = new SSTFDisk();
                break;
            case SCAN:
                nuevoAlgoritmo = new SCANDisk();
                break;
            case C_SCAN:
                nuevoAlgoritmo = new CSCANDisk();
                break;
        }
        
        if (nuevoAlgoritmo != null) {
            fileSystem.getDiskScheduler().setAlgoritmoDisk(nuevoAlgoritmo);
            //System.out.println("Algoritmo de disco cambiado a: " + selected.name());
            System.out.println("[FS] Politica de Disco cambiada a: " + selected.name());
        }
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
        
        // Actualizar JTree
        if (jTreeDirectorio != null && fileSystem != null) {
            actualizarJTree();
        }
        
        // Actualizar combobox de algoritmo de disco para reflejar el algoritmo actual
        if (diskSchedulingCombo1 != null && fileSystem != null && fileSystem.getDiskScheduler() != null) {
            ISchedullingDiskAlgorithm algoritmoActual = fileSystem.getDiskScheduler().getAlgoritmo();
            if (algoritmoActual != null) {
                ISchedullingDiskAlgorithm.SchedulingDiskType tipoActual = algoritmoActual.getSchedulingDiskType();
                // Solo actualizar si es diferente para evitar eventos infinitos
                Object selected = diskSchedulingCombo1.getSelectedItem();
                if (selected == null || !selected.equals(tipoActual)) {
                    // Temporarily remove listener to avoid triggering change event
                    java.awt.event.ActionListener[] listeners = diskSchedulingCombo1.getActionListeners();
                    for (java.awt.event.ActionListener listener : listeners) {
                        diskSchedulingCombo1.removeActionListener(listener);
                    }
                    diskSchedulingCombo1.setSelectedItem(tipoActual);
                    // Re-add listeners
                    for (java.awt.event.ActionListener listener : listeners) {
                        diskSchedulingCombo1.addActionListener(listener);
                    }
                }
            }
        }
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
                        
                        //VERO TE MODIFIQUE ESTO
                        //if (data.getOperationType() != FileData.OperationType.CREATE && 
                        //    data.isIsProcessed() && data.hasError()) {
                        if (data.isIsProcessed() && data.hasError()) {                       
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
                        
                        //VERO TE MODIFIQUE ESTO 
                        //if (data.getOperationType() != FileData.OperationType.CREATE && 
                        //    data.isIsProcessed() && data.hasError()) {
                        if (data.isIsProcessed() && data.hasError()) {
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
    
    /**
     * Valida si se puede leer un archivo (existe y tiene permisos)
     * @param nombre Nombre del archivo
     * @param ruta Ruta del directorio
     * @param modoUsuario Modo de usuario ("Administrador" o "Usuario")
     * @return null si puede leer, mensaje de error si no puede
     */
    private String validarLecturaArchivo(String nombre, String ruta, String modoUsuario) {
        if (fileSystem == null) {
            return "Error: No hay conexión con el FileSystem";
        }
        
        // Buscar el directorio
        CoreV2.Directorio directorio = buscarDirectorioPorRuta(ruta);
        if (directorio == null) {
            return "El directorio '" + ruta + "' no existe.";
        }
        
        // Buscar el archivo en el directorio
        CoreV2.Archivo archivo = directorio.buscarArchivo(nombre);
        if (archivo == null) {
            return "El archivo '" + nombre + "' no existe en '" + ruta + "'.";
        }
        
        // Validar permisos: Usuario solo puede leer archivos públicos
        if ("Usuario".equals(modoUsuario) && "privado".equals(archivo.getTipoArchivo())) {
            return "Acceso denegado: No tiene permisos para leer archivos privados del sistema.";
        }
        
        // Si pasa todas las validaciones, puede leer
        return null;
    }
    
    private void mostrarPopupLeyendo() {
        // Ejecutar en el hilo de Swing para evitar bloqueos
        SwingUtilities.invokeLater(() -> {
            // Crear un JOptionPane personalizado pero no modal
            JOptionPane optionPane = new JOptionPane(
                "Leyendo archivo...",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null, // null para usar el icono por defecto
                new Object[]{}, // Sin botones
                null
            );
            
            // Crear el diálogo desde el JOptionPane
            JDialog dialog = optionPane.createDialog(this, "");
            dialog.setModal(false); // No bloquear la interfaz
            dialog.setAlwaysOnTop(true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            
            // Mostrar el diálogo
            dialog.setVisible(true);
            
            // Cerrar el diálogo después de un pequeño delay (800ms)
            Timer timer = new Timer(800, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
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
        lblTic = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtRutaDirectorio1 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        btnCrearDirectorio1 = new javax.swing.JButton();
        btnEliminarDirectorio1 = new javax.swing.JButton();
        diskSchedulingCombo1 = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        btnCargarTXT = new javax.swing.JButton();
        comboModoUsuario = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Nombre");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, -1, -1));

        jLabel2.setText("Tamaño");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 150, -1, -1));
        getContentPane().add(txtCrearNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 110, -1));

        txtCrearTamano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCrearTamanoActionPerformed(evt);
            }
        });
        getContentPane().add(txtCrearTamano, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 150, 110, -1));

        btnCrear.setText("CREAR");
        btnCrear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearActionPerformed(evt);
            }
        });
        getContentPane().add(btnCrear, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 230, 140, 20));

        panelIzq.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        panelIzq.add(miPanelTAA, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 490, 110));
        panelIzq.add(miPanelDisco, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 470, 360, 240));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Tabla de Asignaciones");
        panelIzq.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, -1, -1));

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

        panelIzq.add(jScrollPaneCola1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 480, 100));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Visualización de Procesos");
        panelIzq.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, -1));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Disco Secundario (SD)");
        panelIzq.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 440, -1, -1));

        getContentPane().add(panelIzq, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 0, 520, 690));

        comboOperacion1.setModel(new javax.swing.DefaultComboBoxModel<>(FileData.OperationType.values()));
        comboOperacion1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboOperacion1ActionPerformed(evt);
            }
        });
        getContentPane().add(comboOperacion1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 110, 20));

        jLabel4.setText("Operación");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, -1));
        getContentPane().add(txtNuevoNombre1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 150, 110, -1));

        labelNuevoNombre.setText("Nuevo Nombre");
        getContentPane().add(labelNuevoNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, -1, 20));

        lblTic.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblTic.setText("Tic: 0");
        getContentPane().add(lblTic, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 500, -1, -1));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel9.setText("Archivo");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, -1, -1));

        txtRutaDirectorio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRutaDirectorio1ActionPerformed(evt);
            }
        });
        getContentPane().add(txtRutaDirectorio1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 190, 110, -1));

        jLabel10.setText("Ruta");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 190, 30, 20));

        btnCrearDirectorio1.setText("Crear Directorio");
        btnCrearDirectorio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearDirectorio1ActionPerformed(evt);
            }
        });
        getContentPane().add(btnCrearDirectorio1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 140, 20));

        btnEliminarDirectorio1.setText("Eliminar Directorio");
        getContentPane().add(btnEliminarDirectorio1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 410, 140, -1));

        diskSchedulingCombo1.setModel(new javax.swing.DefaultComboBoxModel<>(ISchedullingDiskAlgorithm.SchedulingDiskType.values()));
        diskSchedulingCombo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diskSchedulingCombo1ActionPerformed(evt);
            }
        });
        getContentPane().add(diskSchedulingCombo1, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 30, 100, 20));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setText("Directorio");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 340, 80, -1));

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, 400, 110));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setText("Política");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, -1, 20));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setText("Log");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 500, -1, -1));

        btnCargarTXT.setText("Cargar TXT");
        btnCargarTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarTXTActionPerformed(evt);
            }
        });
        getContentPane().add(btnCargarTXT, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 270, 140, 20));

        comboModoUsuario.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Usuario" }));
        comboModoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboModoUsuarioActionPerformed(evt);
            }
        });
        getContentPane().add(comboModoUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 30, 120, -1));

        jButton1.setText("Ver Estadisticas");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 570, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Modo");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 30, -1, -1));

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
            
            // Get directory path from input
            String ruta = txtRutaDirectorio1.getText().trim();
            if (ruta.isEmpty()) {
                ruta = "root";
            }
            
            // Determinar tipo de archivo según el modo
            String tipoArchivo = "Administrador".equals(modoUsuario) ? "privado" : "publico";
            
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
                    System.out.println("GUI: Creando proceso para archivo: " + nombre + " en " + ruta + " (tipo: " + tipoArchivo + ")");
                    so.crearProceso(Proceso.Tipo.IO_BOUND, 0, nombre, tamano, ruta, tipoArchivo, modoUsuario);
                    txtCrearNombre.setText("");
                    txtCrearTamano.setText("");
                    break;
                    
                case READ:
                    // Primero validar si el archivo existe y tiene permisos
                    String errorValidacion = validarLecturaArchivo(nombre, ruta, modoUsuario);
                    if (errorValidacion != null) {
                        // Si hay error, mostrar mensaje sin popup de "Leyendo"
                        JOptionPane.showMessageDialog(this, errorValidacion, "Error al leer archivo", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Si pasa la validación, mostrar popup y crear el proceso
                    mostrarPopupLeyendo();
                    System.out.println("GUI: Creando proceso para leer archivo: " + nombre + " en " + ruta);
                    so.crearProcesoIO(FileData.OperationType.READ, nombre, ruta, tipoArchivo, modoUsuario);
                    txtCrearNombre.setText("");
                    break;
                    
                case UPDATE:
                    String nuevoNombre = txtNuevoNombre1.getText();
                    if (nuevoNombre.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Por favor ingrese el nuevo nombre.");
                        return;
                    }
                    System.out.println("GUI: Creando proceso para actualizar archivo: " + nombre + " -> " + nuevoNombre + " en " + ruta);
                    so.crearProcesoIO(FileData.OperationType.UPDATE, nombre, nuevoNombre, ruta, tipoArchivo, modoUsuario);
                    txtCrearNombre.setText("");
                    txtNuevoNombre1.setText("");
                    break;
                    
                case DELETE:
                    System.out.println("GUI: Creando proceso para eliminar archivo: " + nombre + " en " + ruta);
                    so.crearProcesoIO(FileData.OperationType.DELETE, nombre, ruta, tipoArchivo, modoUsuario);
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

    private void txtRutaDirectorio1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRutaDirectorio1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRutaDirectorio1ActionPerformed

    private void btnCrearDirectorio1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearDirectorio1ActionPerformed
        crearDirectorio();
    }//GEN-LAST:event_btnCrearDirectorio1ActionPerformed

    private void diskSchedulingCombo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diskSchedulingCombo1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_diskSchedulingCombo1ActionPerformed

    private void btnCargarTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarTXTActionPerformed
        // TODO add your handling code here:
        cargarPeticionesDesdeTXT();
    }//GEN-LAST:event_btnCargarTXTActionPerformed
    
    private void comboModoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboModoUsuarioActionPerformed
        String modoSeleccionado = (String) comboModoUsuario.getSelectedItem();
        modoUsuario = modoSeleccionado;
        actualizarVisibilidadSegunModo();
        // Actualizar JTree para reflejar el filtrado de archivos según el modo
        if (jTreeDirectorio != null && fileSystem != null) {
            actualizarJTree();
        }
    }//GEN-LAST:event_comboModoUsuarioActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if (fileSystem != null) {
            EstadisticasFrame frame = new EstadisticasFrame(fileSystem);
            frame.setVisible(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
// --- CLASE INTERNA MEJORADA PARA FILTRAR SALIDA ---
// --- CLASE INTERNA PARA REDIRIGIR LA CONSOLA (VERSIÓN FINAL 3 PARÁMETROS) ---
    private class CustomOutputStream extends java.io.OutputStream {
        private javax.swing.JTextArea textArea;
        private javax.swing.JLabel labelTic;
        private java.io.PrintStream console; // <-- ¡Esto es lo que faltaba en la definición!
        private StringBuilder sb = new StringBuilder();

        // Constructor actualizado para recibir 3 parámetros
        public CustomOutputStream(javax.swing.JTextArea textArea, javax.swing.JLabel labelTic, java.io.PrintStream console) {
            this.textArea = textArea;
            this.labelTic = labelTic;
            this.console = console; 
        }

        @Override
                public void write(int b) {
                    // 1. Escribir en la consola de NetBeans (SIEMPRE)
                    if (console != null) {
                        console.write(b);
                    }

                    // 2. Acumular para la GUI
                    sb.append((char) b);

                    if (b == '\n') {
                        final String texto = sb.toString();
                        sb.setLength(0); 

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            // --- FILTRO DE MENSAJES PARA LA GUI ---

                            // A. El reloj va a su etiqueta
                            if (texto.contains("Tic Global:")) {
                                labelTic.setText(texto.trim()); 
                            } 
                            // B. Solo mostramos en el cuadro lo que el usuario pidió
                            else if (texto.contains("[DMA]") || texto.contains("[FS]") || texto.contains("[COLA]")) {
                                textArea.append(texto);
                            }
                            // C. Todo lo demás (Debug, CPU, etc.) se ignora en la GUI (pero sale en consola)
                        });
                    }
                }
    }
    
    private void cargarPeticionesDesdeTXT() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar script de pruebas");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Texto (*.txt)", "txt"));

            int selection = fileChooser.showOpenDialog(this);

            if (selection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    int count = 0;

                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty() || line.startsWith("#")) continue;

                        String[] parts = line.split(",");
                        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();

                        if (parts.length < 2) continue;

                        String operacion = parts[0].toUpperCase();
                        String nombre = parts[1];

                        // Valores por defecto
                        String ruta = "root";
                        String modo = "Usuario"; 
                        String tipoArchivo = "publico";

                        switch (operacion) {
                            case "CREATE":
                                if (parts.length >= 3) {
                                    try {
                                        int tamano = Integer.parseInt(parts[2]);
                                        if (parts.length >= 4) ruta = parts[3];
                                        if (parts.length >= 5) modo = parts[4]; // Leemos el modo

                                        // Si es Admin, el archivo nace privado. Si es Usuario, público.
                                        tipoArchivo = "Administrador".equalsIgnoreCase(modo) ? "privado" : "publico";

                                        System.out.println("[TXT] CREATE: " + nombre + " (" + tamano + "kb) en " + ruta + " [" + modo + "]");
                                        // Usamos el constructor completo que incluye tipo y modo
                                        so.crearProceso(Proceso.Tipo.IO_BOUND, 0, nombre, tamano, ruta, tipoArchivo, modo);
                                        count++;
                                    } catch (NumberFormatException e) {
                                        System.out.println("[TXT] Error formato número: " + line);
                                    }
                                }
                                break;

                            case "READ":
                                if (parts.length >= 3) ruta = parts[2];
                                if (parts.length >= 4) modo = parts[3]; // Leemos el modo

                                // El tipoArchivo aquí no importa tanto para leer, pero lo definimos por coherencia
                                tipoArchivo = "Administrador".equalsIgnoreCase(modo) ? "privado" : "publico";

                                System.out.println("[TXT] READ: " + nombre + " en " + ruta + " [" + modo + "]");
                                so.crearProcesoIO(FileData.OperationType.READ, nombre, ruta, tipoArchivo, modo);
                                count++;
                                break;

                            case "UPDATE":
                                if (parts.length >= 3) {
                                    String nuevoNombre = parts[2];
                                    if (parts.length >= 4) ruta = parts[3];
                                    if (parts.length >= 5) modo = parts[4]; // Leemos el modo

                                    tipoArchivo = "Administrador".equalsIgnoreCase(modo) ? "privado" : "publico";

                                    System.out.println("[TXT] UPDATE: " + nombre + " -> " + nuevoNombre + " en " + ruta + " [" + modo + "]");
                                    so.crearProcesoIO(FileData.OperationType.UPDATE, nombre, nuevoNombre, ruta, tipoArchivo, modo);
                                    count++;
                                }
                                break;

                            case "DELETE":
                                if (parts.length >= 3) ruta = parts[2];
                                if (parts.length >= 4) modo = parts[3]; // Leemos el modo

                                tipoArchivo = "Administrador".equalsIgnoreCase(modo) ? "privado" : "publico";

                                System.out.println("[TXT] DELETE: " + nombre + " en " + ruta + " [" + modo + "]");
                                so.crearProcesoIO(FileData.OperationType.DELETE, nombre, ruta, tipoArchivo, modo);
                                count++;
                                break;
                        }
                        // Pequeña pausa para que no entren todos en el mismo milisegundo exacto
                        // Thread.sleep(20); 
                    }

                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Se cargaron " + count + " procesos exitosamente.", 
                        "Carga Completa", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    System.out.println("Error leyendo TXT: " + e.getMessage());
                }
            }
        }
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
    private javax.swing.JButton btnCargarTXT;
    private javax.swing.JButton btnCrear;
    private javax.swing.JButton btnCrearDirectorio1;
    private javax.swing.JButton btnEliminarDirectorio1;
    private javax.swing.JComboBox<String> comboModoUsuario;
    private javax.swing.JComboBox<FileData.OperationType> comboOperacion1;
    private javax.swing.JComboBox<ISchedullingDiskAlgorithm.SchedulingDiskType> diskSchedulingCombo1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneCola1;
    private javax.swing.JLabel labelNuevoNombre;
    private javax.swing.JLabel lblTic;
    private Interfaces.PanelDiscoForm miPanelDisco;
    private Interfaces.PanelTAAForm miPanelTAA;
    private javax.swing.JPanel panelIzq;
    private javax.swing.JTable tablaColaProcesos1;
    private javax.swing.JTextField txtCrearNombre;
    private javax.swing.JTextField txtCrearTamano;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtNuevoNombre1;
    private javax.swing.JTextField txtRutaDirectorio1;
    // End of variables declaration//GEN-END:variables
    
    // Variables adicionales para CRUD
    private javax.swing.JComboBox<FileData.OperationType> comboOperacion;
    private javax.swing.JLabel jLabelOperacion;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField txtNuevoNombre;
    private javax.swing.JTable tablaColaProcesos;
    private javax.swing.JScrollPane jScrollPaneCola;
    private javax.swing.JLabel jLabelCola;
    // diskSchedulingCombo should be declared in the form (GEN-BEGIN:variables section)
    // If it doesn't exist yet, add it to the form file or declare it here:
    private javax.swing.JComboBox<ISchedullingDiskAlgorithm.SchedulingDiskType> diskSchedulingCombo;
}
