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
    
    // vvv ¡AÑADE ESTE GUARDIA! vvv
    private final Semaphore mutexCola = new Semaphore(1); // 1 = solo 1 hilo puede pasar
    
    // --- GUARDIA #2: "El Obrero" ---
    // Simula que solo hay UN disco físico.
    private volatile boolean estaOcupado = false; // ¡volatile es importante!

    public FileSystem(Disk disk, DiskScheduler diskScheduler) {
        this.disk=disk;
        this.diskScheduler=diskScheduler;
        this.colaPeticiones = new Cola();
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
            this.createFile(siguiente); // Llama al método que duerme
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
                
                // 1. Validar si ya existe
                if (buscarArchivo(nombre) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' ya existe.");
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
                    // Creamos el archivo con nombre, tamaño y proceso que lo creó
                    String processName = data.getProcessName();
                    Archivo nuevoArchivo = new Archivo(nombre, tamano, processName);

                    // Y ahora le asignamos los bloques que nos dio el disco
                    nuevoArchivo.setBloquesAsignados(bloquesAsignados);

                    tablaDeArchivos.add(nuevoArchivo); 
                    System.out.println("FileSystem: Archivo '" + nombre + "' CREADO con éxito.");
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
    
    // Helper para buscar un archivo en nuestra 'tablaDeArchivos'
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
    
    
}
