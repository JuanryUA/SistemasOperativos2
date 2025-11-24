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
    private Directorio root; // Root directory
    
    // vvv ¡AÑADE ESTE GUARDIA! vvv
    private final Semaphore mutexCola = new Semaphore(1); // 1 = solo 1 hilo puede pasar
    
    // --- GUARDIA #2: "El Obrero" ---
    // Simula que solo hay UN disco físico.
    private volatile boolean estaOcupado = false; // ¡volatile es importante!

    public FileSystem(Disk disk, DiskScheduler diskScheduler) {
        this.disk=disk;
        this.diskScheduler=diskScheduler;
        this.colaPeticiones = new Cola();
        this.root = new Directorio("root", null); // Initialize root directory
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
            String algorithmName = diskScheduler.getAlgoritmo().getSchedulingDiskType().name();
            System.out.println("        FileSystem: OCUPADO. Procesando (" + algorithmName + "): " + siguiente.getFileName());
            
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
                String ruta = data.getRuta() != null ? data.getRuta() : "root";
                
                // 1. Find the target directory
                Directorio directorioDestino = buscarDirectorioPorRuta(ruta);
                if (directorioDestino == null) {
                    System.out.println("FileSystem: Error, el directorio '" + ruta + "' no existe.");
                    data.setErrorMessage("El directorio '" + ruta + "' no existe.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // 2. Validar si ya existe en el mismo directorio
                if (directorioDestino.buscarArchivo(nombre) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' ya existe en el directorio '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' ya existe en el directorio '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }

                // 3. Pedir bloques al disco (¡tu idea!)
                // (Simulamos que esto tarda tiempo)
                Thread.sleep(1000); // Simula el tiempo de búsqueda de bloques
                // Pedimos los bloques al disco (esto sigue igual)
                Lista<Integer> bloquesAsignados = disk.asignarBloques(tamano);
                
                // 4. Validar si se pudo
                if (bloquesAsignados == null) {
                    // No hay espacio
                    System.out.println("FileSystem: Error, no hay espacio para '" + nombre + "'.");
                    data.setErrorMessage("No hay espacio suficiente en el disco.");
                } else {
                    // Creamos el archivo con nombre, tamaño, ruta y proceso que lo creó
                    String processName = data.getProcessName();
                    Archivo nuevoArchivo = new Archivo(nombre, tamano, ruta, processName);

                    // Y ahora le asignamos los bloques que nos dio el disco
                    nuevoArchivo.setBloquesAsignados(bloquesAsignados);

                    // Add to both the global list and the directory
                    tablaDeArchivos.add(nuevoArchivo);
                    directorioDestino.agregarArchivo(nuevoArchivo);
                    
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
    
    // Helper para buscar un archivo en nuestra 'tablaDeArchivos' (global search)
    private Archivo buscarArchivo(String nombre) {
        System.out.println(         "TABLA DE ARCHIVO: "+ tablaDeArchivos);
        for (int i = 0; i < tablaDeArchivos.size(); i++) {
            Archivo actual = tablaDeArchivos.get(i);
            if (actual.getNombre().equals(nombre)) {
                return actual;
            }
        }
        return null; // No encontrado
    }
    
    // Helper para buscar un archivo por nombre y ruta
    private Archivo buscarArchivoPorRuta(String nombre, String ruta) {
        Directorio dir = buscarDirectorioPorRuta(ruta);
        if (dir != null) {
            return dir.buscarArchivo(nombre);
        }
        return null;
    }
    
    // Helper para buscar un directorio por ruta (e.g., "root/x/y/z")
    private Directorio buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || ruta.equals("root")) {
            return root;
        }
        
        // Remove "root/" prefix if present
        String path = ruta.startsWith("root/") ? ruta.substring(5) : ruta;
        if (path.isEmpty()) {
            return root;
        }
        
        // Split path and navigate
        String[] partes = path.split("/");
        Directorio actual = root;
        
        for (String parte : partes) {
            if (parte.isEmpty()) continue;
            Directorio siguiente = actual.buscarSubdirectorio(parte);
            if (siguiente == null) {
                return null; // Path doesn't exist
            }
            actual = siguiente;
        }
        
        return actual;
    }
    
    // Create a directory at the specified path
    public void crearDirectorio(String ruta, String nombreDirectorio) {
        Directorio directorioPadre = buscarDirectorioPorRuta(ruta);
        if (directorioPadre == null) {
            System.out.println("FileSystem: Error, el directorio padre '" + ruta + "' no existe.");
            return;
        }
        
        // Check if directory or file with same name already exists
        if (directorioPadre.existe(nombreDirectorio)) {
            System.out.println("FileSystem: Error, ya existe un archivo o directorio con el nombre '" + nombreDirectorio + "' en '" + ruta + "'.");
            return;
        }
        
        Directorio nuevoDirectorio = new Directorio(nombreDirectorio, directorioPadre);
        directorioPadre.agregarSubdirectorio(nuevoDirectorio);
        System.out.println("FileSystem: Directorio '" + nombreDirectorio + "' creado en '" + ruta + "'.");
    }
    
    // Delete a directory and all its contents
    public void eliminarDirectorio(String ruta, String nombreDirectorio) {
        Directorio directorioPadre = buscarDirectorioPorRuta(ruta);
        if (directorioPadre == null) {
            System.out.println("FileSystem: Error, el directorio padre '" + ruta + "' no existe.");
            return;
        }
        
        Directorio directorioAEliminar = directorioPadre.buscarSubdirectorio(nombreDirectorio);
        if (directorioAEliminar == null) {
            System.out.println("FileSystem: Error, el directorio '" + nombreDirectorio + "' no existe en '" + ruta + "'.");
            return;
        }
        
        // Get all files in this directory and subdirectories
        Lista<Archivo> archivosAEliminar = directorioAEliminar.obtenerTodosLosArchivos();
        
        // Free blocks for all files
        for (int i = 0; i < archivosAEliminar.size(); i++) {
            Archivo arch = archivosAEliminar.get(i);
            Lista<Integer> bloques = arch.getBloquesAsignados();
            if (bloques != null && bloques.size() > 0) {
                disk.liberarBloques(bloques);
            }
            // Remove from global list
            tablaDeArchivos.remove(arch);
        }
        
        // Recursively delete directory
        directorioAEliminar.eliminarRecursivo();
        directorioPadre.eliminarSubdirectorio(directorioAEliminar);
        
        System.out.println("FileSystem: Directorio '" + nombreDirectorio + "' y todo su contenido eliminado.");
    }
    
    public DiskScheduler getDiskScheduler (){
        return this.diskScheduler;
    }
    
    public Lista<Archivo> getTablaDeArchivos() {
        return tablaDeArchivos;
    }
    
    public Directorio getRoot() {
        return root;
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
                String ruta = data.getRuta() != null ? data.getRuta() : "root";
                
                // Buscar el archivo en el directorio especificado
                Archivo archivo = buscarArchivoPorRuta(nombre, ruta);
                
                if (archivo == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Simular tiempo de operación
                Thread.sleep(1000);
                
                // Liberar los bloques del archivo
                Lista<Integer> bloquesALiberar = archivo.getBloquesAsignados();
                if (bloquesALiberar != null && bloquesALiberar.size() > 0) {
                    disk.liberarBloques(bloquesALiberar);
                }
                
                // Eliminar el archivo de la tabla y del directorio
                tablaDeArchivos.remove(archivo);
                Directorio dir = buscarDirectorioPorRuta(ruta);
                if (dir != null) {
                    dir.eliminarArchivo(archivo);
                }
                
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
                String ruta = data.getRuta() != null ? data.getRuta() : "root";
                
                // Buscar el archivo en el directorio especificado
                Archivo archivo = buscarArchivoPorRuta(nombre, ruta);
                
                if (archivo == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Simular tiempo de lectura
                Thread.sleep(1500);
                
                System.out.println("FileSystem: Archivo '" + nombre + "' LEÍDO (solo lectura, sin modificaciones) de '" + ruta + "'.");
                
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
                String ruta = data.getRuta() != null ? data.getRuta() : "root";
                
                // Validar que el nuevo nombre no esté vacío
                if (nombreNuevo == null || nombreNuevo.isEmpty()) {
                    System.out.println("FileSystem: Error, el nuevo nombre no puede estar vacío.");
                    data.setErrorMessage("El nuevo nombre no puede estar vacío.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Buscar el archivo en el directorio especificado
                Archivo archivo = buscarArchivoPorRuta(nombreViejo, ruta);
                
                if (archivo == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombreViejo + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombreViejo + "' no existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Validar que el nuevo nombre no exista ya en el mismo directorio
                Directorio dir = buscarDirectorioPorRuta(ruta);
                if (dir != null && dir.buscarArchivo(nombreNuevo) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombreNuevo + "' ya existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombreNuevo + "' ya existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                // Simular tiempo de operación
                Thread.sleep(1000);
                
                // Actualizar el nombre del archivo
                archivo.setNombre(nombreNuevo);
                System.out.println("FileSystem: Archivo '" + nombreViejo + "' RENOMBRADO a '" + nombreNuevo + "' con éxito en '" + ruta + "'.");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                peticionDeLaCola.getFileData().setIsProcessed(true);
                estaOcupado = false;
                System.out.println("FileSystem: LIBERADO (por este hilo).");
            }
        }).start();
    }
    
    
}
