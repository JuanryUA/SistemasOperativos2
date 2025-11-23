/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore; // <-- ¡IMPORTANTE!
/**
 *
 * @author verol
 */
public class FileSystem {
    public Cola colaPeticiones;
    private Disk disk;
    private DiskScheduler diskScheduler; 
    private Lista<Archivo> tablaDeArchivos = new Lista<Archivo>();
    private DirectoryNode root; // Raíz del árbol de directorios
    
    // vvv ¡AÑADE ESTE GUARDIA! vvv
    private final Semaphore mutexCola = new Semaphore(1); // 1 = solo 1 hilo puede pasar
    
    // --- GUARDIA #2: "El Obrero" ---
    // Simula que solo hay UN disco físico.
    private volatile boolean estaOcupado = false; // ¡volatile es importante!

    public FileSystem(Disk disk, DiskScheduler diskScheduler) {
        this.disk=disk;
        this.diskScheduler=diskScheduler;
        this.colaPeticiones = new Cola();
        // Inicializar el árbol con root
        this.root = new DirectoryNode("/", null);
    }
    
    public void agregarPeticion(FileData fileData){
        try {
            // vvv ¡AÑADE ESTO! vvv
            // El hilo (P1, P2, P3) pide permiso para usar el disco.
            // P1 entra. P2 y P3 se quedan aquí esperando en fila.
            mutexCola.acquire();
            System.out.println("        FileSystem: OCUPADO por " + fileData.getFileName());

            Petition peticionNueva = new Petition( fileData);
            this.colaPeticiones.add(peticionNueva);


            } catch (InterruptedException ex) {
//            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
Thread.currentThread().interrupt();
        } finally {
            // vvv ¡AÑADE ESTO! vvv
            // P1 termina y suelta el permiso.
            // Ahora P2 (que estaba esperando en 'acquire') puede entrar.
//            System.out.println("        FileSystem: Disco LIBRE.");
            mutexCola.release();
        }
    }
    
    public void getNextPeticion(){
        // 1. Si el disco ya está trabajando (GUARDIA #2 activo), no hagas nada.
        if (estaOcupado) {
            return; 
        }
        
        // 2. Si está libre, ¿hay trabajo? (Revisar CON mutexCola)
        Petition siguiente = null;
        try {
            mutexCola.acquire();
            if (diskScheduler.hayPeticiones()) {
                // ¡FIFO saca la petición!
                siguiente = diskScheduler.obtenerSiguientePeticion(); 
            }
        } catch (InterruptedException e) { /*...*/ } 
        finally {
            mutexCola.release();
        }
        
        // 3. Si sacamos una petición, haz el trabajo
        if (siguiente != null) {
            estaOcupado = true; // <-- Ocupa el disco (Activa GUARDIA #2)
            System.out.println("        FileSystem: OCUPADO. Procesando (FIFO): " + siguiente.getFileName());
            
            // Route to the correct operation based on operation type
            FileData data = siguiente.getFileData();
            switch (data.getOperationType()) {
                case CREATE:
                    this.createFile(siguiente);
                    break;
                case READ:
                    this.readFile(siguiente);
                    break;
                case UPDATE:
                    this.updateFile(siguiente);
                    break;
                case DELETE:
                    this.deleteFile(siguiente);
                    break;
            }
        }
        
//        if (diskScheduler.hayPeticiones()) {
//            Petition siguiente = diskScheduler.obtenerSiguientePeticion();
//            if (siguiente != null) {
//                this.createFile(siguiente);
//            }
//        }
    }

    public Cola getColaPeticiones() {
        return colaPeticiones;
    }

    public void setColaPeticiones(Cola colaPeticiones) {
        this.colaPeticiones = colaPeticiones;
    }
    
    public void createFile(Petition peticionDeLaCola){
        new Thread(() -> { 
            try {
                // --- INICIO DE LA LÓGICA REAL ---
                FileData data = peticionDeLaCola.getFileData();
                String nombre = data.getFileName();
                int tamano = data.getFileSize();
                
                // Obtener la ruta del archivo
                String ruta = data.getRuta() != null ? data.getRuta() : "/";
                
                // 1. Validar si ya existe en esa ruta
                if (buscarArchivo(nombre, ruta) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' ya existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' ya existe en ese directorio.");
                    data.setIsProcessed(true); // ¡Avisa al DMA que "terminó" (con error)!
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return; // Termina el hilo
                }

                // 2. Pedir bloques al disco (¡tu idea!)
                // (Simulamos que esto tarda tiempo)
                Thread.sleep(1000); // Simula el tiempo de búsqueda de bloques
                // Pedimos los bloques al disco (esto sigue igual)
                Lista<Integer> bloquesAsignados = disk.asignarBloques(tamano);
                
                // 3. Validar si se pudo
                if (bloquesAsignados == null) {
                    // No hay espacio
                    System.out.println("FileSystem: Error, no hay espacio para '" + nombre + "'.");
                } else {
                    // Obtener la ruta del archivo
                    String ruta = data.getRuta() != null ? data.getRuta() : "/";
                    
                    // Verificar que el directorio existe
                    DirectoryNode dir = buscarDirectorioPorRuta(ruta);
                    if (dir == null) {
                        System.out.println("FileSystem: Error, el directorio '" + ruta + "' no existe.");
                        data.setErrorMessage("El directorio '" + ruta + "' no existe.");
                        data.setIsProcessed(true);
                        estaOcupado = false;
                        return;
                    }
                    
                    // Verificar que no exista ya un archivo con ese nombre en ese directorio
                    if (dir.findChild(nombre) != null) {
                        System.out.println("FileSystem: Error, el archivo '" + nombre + "' ya existe en '" + ruta + "'.");
                        data.setErrorMessage("El archivo '" + nombre + "' ya existe en ese directorio.");
                        data.setIsProcessed(true);
                        estaOcupado = false;
                        return;
                    }
                    
                    // Creamos el archivo con nombre, tamaño y proceso que lo creó
                    String processName = data.getProcessName();
                    Archivo nuevoArchivo = new Archivo(nombre, tamano, processName, ruta);

                    // Y ahora le asignamos los bloques que nos dio el disco
                    nuevoArchivo.setBloquesAsignados(bloquesAsignados);

                    // Agregar a la tabla de archivos
                    tablaDeArchivos.add(nuevoArchivo);
                    
                    // Agregar al árbol de directorios
                    agregarArchivoAlArbol(nuevoArchivo, ruta);
                    
                    System.out.println("FileSystem: Archivo '" + nombre + "' CREADO con éxito en '" + ruta + "'.");
                    System.out.println("            TAMANO ARCHIVOOOOOL " + nuevoArchivo.getTamano());
                }
                                
            } catch (InterruptedException ex) { /*...*/ } 
            finally {
                // ¡Avisa al DMA que terminaste (con éxito o error)!
                peticionDeLaCola.getFileData().setIsProcessed(true); 
                estaOcupado = false; // <-- Libera el disco
                System.out.println("FileSystem: LIBERADO (por este hilo).");
            }
        }).start();
        
//        new Thread(() -> { // <-- ¡Debe estar en un hilo!
//            try {
//                Thread.sleep(3000); //estooooo reemplaza momentaneamente toda la funcion de crear el archivo, reemplazar luego cuando se tenga la funcionaliadd
//                System.out.println("FileSystem: Archivo '" + peticionDeLaCola.getFileName() + "' CREADO.");
//                peticionDeLaCola.getFileData().setIsProcessed(true);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
//            }finally {
//                estaOcupado = false; // <-- Libera el disco (Desactiva GUARDIA #2)
//                System.out.println("FileSystem: Disco LIBRE.");
//            }
//        }).start();
    }
    
    // Helper para buscar un archivo en nuestra 'tablaDeArchivos' (por nombre y ruta)
    private Archivo buscarArchivo(String nombre, String ruta) {
        System.out.println("TABLA DE ARCHIVO: "+ tablaDeArchivos);
        String rutaBusqueda = ruta != null ? ruta : "/";
        for (int i = 0; i < tablaDeArchivos.size(); i++) {
            Archivo actual = tablaDeArchivos.get(i);
            if (actual.getNombre().equals(nombre) && actual.getRuta().equals(rutaBusqueda)) {
                return actual;
            }
        }
        return null; // No encontrado
    }
    
    // Helper para buscar un archivo solo por nombre (compatibilidad)
    private Archivo buscarArchivo(String nombre) {
        return buscarArchivo(nombre, "/");
    }
    
    public DiskScheduler getDiskScheduler (){
        return this.diskScheduler;
    }
    
    public Lista<Archivo> getTablaDeArchivos() {
        return tablaDeArchivos;
    }
    
    /**
     * Obtiene el nombre del archivo que ocupa un bloque específico
     * @param blockNumber El número del bloque
     * @return El nombre del archivo que ocupa ese bloque, o null si está libre
     */
    public String getFileNameFromBlock(int blockNumber) {
        for (int i = 0; i < tablaDeArchivos.size(); i++) {
            Archivo arch = tablaDeArchivos.get(i);
            Lista<Integer> bloques = arch.getBloquesAsignados();
            for (int j = 0; j < bloques.size(); j++) {
                if (bloques.get(j) == blockNumber) {
                    return arch.getNombre();
                }
            }
        }
        return null; // Bloque libre
    }
    
    public void deleteFile(Petition peticionDeLaCola) {
        new Thread(() -> {
            try {
                FileData data = peticionDeLaCola.getFileData();
                String nombre = data.getFileName();
                String ruta = data.getRuta() != null ? data.getRuta() : "/";
                
                // Buscar el archivo en el árbol
                DirectoryNode archivoNode = buscarArchivoEnArbol(nombre, ruta);
                
                if (archivoNode == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' no existe en ese directorio.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Archivo archivo = archivoNode.getArchivo();
                
                // Simular tiempo de operación
                Thread.sleep(1000);
                
                // Liberar los bloques del archivo
                Lista<Integer> bloquesALiberar = archivo.getBloquesAsignados();
                if (bloquesALiberar != null && bloquesALiberar.size() > 0) {
                    disk.liberarBloques(bloquesALiberar);
                }
                
                // Eliminar el archivo del árbol
                eliminarArchivoDelArbol(nombre, ruta);
                
                // Eliminar el archivo de la tabla
                tablaDeArchivos.remove(archivo);
                System.out.println("FileSystem: Archivo '" + nombre + "' ELIMINADO con éxito de '" + ruta + "'.");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                peticionDeLaCola.getFileData().setIsProcessed(true);
                estaOcupado = false;
                System.out.println("FileSystem: LIBERADO (por este hilo).");
            }
        }).start();
    }
    
    public void readFile(Petition peticionDeLaCola) {
        new Thread(() -> {
            try {
                FileData data = peticionDeLaCola.getFileData();
                String nombre = data.getFileName();
                String ruta = data.getRuta() != null ? data.getRuta() : "/";
                
                // Buscar el archivo en el árbol
                DirectoryNode archivoNode = buscarArchivoEnArbol(nombre, ruta);
                
                if (archivoNode == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' no existe en ese directorio.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Simular tiempo de lectura
                Thread.sleep(1500);
                
                System.out.println("FileSystem: Archivo '" + nombre + "' LEÍDO (solo lectura, sin modificaciones).");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                peticionDeLaCola.getFileData().setIsProcessed(true);
                estaOcupado = false;
                System.out.println("FileSystem: LIBERADO (por este hilo).");
            }
        }).start();
    }
    
    public void updateFile(Petition peticionDeLaCola) {
        new Thread(() -> {
            try {
                FileData data = peticionDeLaCola.getFileData();
                String nombreViejo = data.getFileName();
                String nombreNuevo = data.getNewFileName();
                
                // Validar que el nuevo nombre no esté vacío
                if (nombreNuevo == null || nombreNuevo.isEmpty()) {
                    System.out.println("FileSystem: Error, el nuevo nombre no puede estar vacío.");
                    data.setErrorMessage("El nuevo nombre no puede estar vacío.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                String ruta = data.getRuta() != null ? data.getRuta() : "/";
                
                // Buscar el archivo en el árbol
                DirectoryNode archivoNode = buscarArchivoEnArbol(nombreViejo, ruta);
                
                if (archivoNode == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombreViejo + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombreViejo + "' no existe en ese directorio.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Archivo archivo = archivoNode.getArchivo();
                
                // Validar que el nuevo nombre no exista ya en la misma ruta
                DirectoryNode parentDir = buscarDirectorioPorRuta(ruta);
                if (parentDir != null && parentDir.findChild(nombreNuevo) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombreNuevo + "' ya existe en ese directorio.");
                    data.setErrorMessage("El archivo '" + nombreNuevo + "' ya existe en ese directorio.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Simular tiempo de operación
                Thread.sleep(1000);
                
                // Actualizar el nombre del archivo y el nodo en el árbol
                String ruta = archivo.getRuta();
                DirectoryNode dirNode = buscarDirectorioPorRuta(ruta);
                if (dirNode != null) {
                    DirectoryNode oldFileNode = dirNode.findChild(nombreViejo);
                    if (oldFileNode != null) {
                        dirNode.removeChild(oldFileNode);
                        oldFileNode.setName(nombreNuevo);
                        dirNode.addChild(oldFileNode);
                    }
                }
                archivo.setNombre(nombreNuevo);
                System.out.println("FileSystem: Archivo '" + nombreViejo + "' RENOMBRADO a '" + nombreNuevo + "' con éxito.");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                peticionDeLaCola.getFileData().setIsProcessed(true);
                estaOcupado = false;
                System.out.println("FileSystem: LIBERADO (por este hilo).");
            }
        }).start();
    }
    
    // ========== MÉTODOS PARA GESTIÓN DE DIRECTORIOS ==========
    
    public DirectoryNode getRoot() {
        return root;
    }
    
    /**
     * Busca un directorio por su ruta (ej: "/root/folder1")
     */
    public DirectoryNode buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || ruta.equals("/")) {
            return root;
        }
        
        String[] partes = ruta.split("/");
        DirectoryNode actual = root;
        
        for (String parte : partes) {
            if (parte.isEmpty()) continue; // Saltar partes vacías
            
            DirectoryNode hijo = actual.findChild(parte);
            if (hijo == null || !hijo.isDirectory()) {
                return null; // No encontrado
            }
            actual = hijo;
        }
        
        return actual;
    }
    
    /**
     * Crea un nuevo directorio en la ruta especificada
     */
    public boolean crearDirectorio(String nombre, String rutaPadre) {
        DirectoryNode padre = buscarDirectorioPorRuta(rutaPadre);
        if (padre == null) {
            return false;
        }
        
        // Verificar que no exista ya
        if (padre.findChild(nombre) != null) {
            return false;
        }
        
        DirectoryNode nuevoDir = new DirectoryNode(nombre, padre);
        padre.addChild(nuevoDir);
        return true;
    }
    
    /**
     * Elimina un directorio y todo su contenido
     */
    public boolean eliminarDirectorio(String nombre, String rutaPadre) {
        DirectoryNode padre = buscarDirectorioPorRuta(rutaPadre);
        if (padre == null) {
            return false;
        }
        
        DirectoryNode dir = padre.findChild(nombre);
        if (dir == null || !dir.isDirectory()) {
            return false;
        }
        
        // Eliminar todos los archivos del directorio y subdirectorios
        eliminarRecursivo(dir);
        
        // Eliminar el directorio del padre
        padre.removeChild(dir);
        return true;
    }
    
    /**
     * Elimina recursivamente un directorio y todo su contenido
     */
    private void eliminarRecursivo(DirectoryNode nodo) {
        if (nodo.isDirectory() && nodo.getChildren() != null) {
            // Crear una copia de la lista para evitar problemas de modificación concurrente
            List<DirectoryNode> hijos = new java.util.ArrayList<>(nodo.getChildren());
            for (DirectoryNode hijo : hijos) {
                if (hijo.isFile() && hijo.getArchivo() != null) {
                    // Eliminar archivo de la tabla
                    tablaDeArchivos.remove(hijo.getArchivo());
                    // Liberar bloques
                    Lista<Integer> bloques = hijo.getArchivo().getBloquesAsignados();
                    if (bloques != null && bloques.size() > 0) {
                        disk.liberarBloques(bloques);
                    }
                } else if (hijo.isDirectory()) {
                    eliminarRecursivo(hijo);
                }
            }
        }
    }
    
    /**
     * Busca un archivo en el árbol por nombre y ruta
     */
    public DirectoryNode buscarArchivoEnArbol(String nombre, String ruta) {
        DirectoryNode dir = buscarDirectorioPorRuta(ruta);
        if (dir == null) {
            return null;
        }
        
        DirectoryNode archivoNode = dir.findChild(nombre);
        if (archivoNode != null && archivoNode.isFile()) {
            return archivoNode;
        }
        
        return null;
    }
    
    /**
     * Agrega un archivo al árbol de directorios
     */
    public boolean agregarArchivoAlArbol(Archivo archivo, String ruta) {
        DirectoryNode dir = buscarDirectorioPorRuta(ruta);
        if (dir == null) {
            return false;
        }
        
        // Verificar que no exista ya
        if (dir.findChild(archivo.getNombre()) != null) {
            return false;
        }
        
        DirectoryNode archivoNode = new DirectoryNode(archivo.getNombre(), archivo, dir);
        dir.addChild(archivoNode);
        archivo.setRuta(ruta);
        return true;
    }
    
    /**
     * Elimina un archivo del árbol
     */
    public boolean eliminarArchivoDelArbol(String nombre, String ruta) {
        DirectoryNode dir = buscarDirectorioPorRuta(ruta);
        if (dir == null) {
            return false;
        }
        
        DirectoryNode archivoNode = dir.findChild(nombre);
        if (archivoNode == null || !archivoNode.isFile()) {
            return false;
        }
        
        dir.removeChild(archivoNode);
        return true;
    }
    
    
}
