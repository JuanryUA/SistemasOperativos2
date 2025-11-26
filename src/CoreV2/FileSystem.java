/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore; 
import CoreV2.DiskStrategies.ISchedullingDiskAlgorithm.SchedulingDiskType;
import CoreV2.Map;      
import CoreV2.HashMap;  
/**
 *
 * @author verol
 */
public class FileSystem {
    public Cola colaPeticiones;
    private Disk disk;
    private DiskScheduler diskScheduler; 
    private Lista<Archivo> tablaDeArchivos = new Lista<Archivo>();
    private Directorio root; 
    private Map<SchedulingDiskType, Long> tiempoTotalPorPolitica;
    private Map<SchedulingDiskType, Integer> cantidadPorPolitica;
    

    private final Semaphore mutexCola = new Semaphore(1); 
    

    private volatile boolean estaOcupado = false; 

    public FileSystem(Disk disk, DiskScheduler diskScheduler) {
        this.disk=disk;
        this.diskScheduler=diskScheduler;
        this.colaPeticiones = new Cola();
        this.root = new Directorio("root", null);
        
        this.tiempoTotalPorPolitica = new HashMap<>();
        this.cantidadPorPolitica = new HashMap<>();
        
        for (SchedulingDiskType type : SchedulingDiskType.values()) {
            tiempoTotalPorPolitica.put(type, 0L);
            cantidadPorPolitica.put(type, 0);
        }
    }
    
    private void registrarEstadistica(long tiempoInicio) {
        long tiempoFinal = System.currentTimeMillis();
        long duracion = tiempoFinal - tiempoInicio;
        
        SchedulingDiskType tipoActual = diskScheduler.getAlgoritmo().getSchedulingDiskType();
        
        long totalActual = tiempoTotalPorPolitica.getOrDefault(tipoActual, 0L);
        tiempoTotalPorPolitica.put(tipoActual, totalActual + duracion);
        
        int cantidadActual = cantidadPorPolitica.getOrDefault(tipoActual, 0);
        cantidadPorPolitica.put(tipoActual, cantidadActual + 1);
    }
    
    public String obtenerEstadisticasTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TIEMPO PROMEDIO DE EJECUCIÓN (ms) ===\n\n");
        
        for (SchedulingDiskType type : SchedulingDiskType.values()) {
            Long totalObj = tiempoTotalPorPolitica.get(type);
            Integer countObj = cantidadPorPolitica.get(type);
            
            long total = (totalObj != null) ? totalObj : 0L;
            int count = (countObj != null) ? countObj : 0;
            
            double promedio = (count > 0) ? (double) total / count : 0.0;
            
            sb.append(String.format("%-10s: %.2f ms  (%d peticiones)\n", type.name(), promedio, count));
        }
        return sb.toString();
    }
    
    
    public void agregarPeticion(FileData fileData){
        try {

            mutexCola.acquire();
            System.out.println("[FS] El FileSystem ha recibido al DMA del proceso " + fileData.getProcessName());
            System.out.println("        FileSystem: OCUPADO por " + fileData.getFileName());

            Petition peticionNueva = new Petition( fileData);
            this.colaPeticiones.add(peticionNueva);
            System.out.println("[COLA] Peticion de " + fileData.getProcessName() + " (" + fileData.getFileName() + ") entro a la cola.");


            } catch (InterruptedException ex) {
Thread.currentThread().interrupt();
        } finally {
        
            mutexCola.release();
        }
    }
    
    public void getNextPeticion(){
        if (estaOcupado) {
            return; 
        }
        
        Petition siguiente = null;
        try {
            mutexCola.acquire();
            if (diskScheduler.hayPeticiones()) {
                siguiente = diskScheduler.obtenerSiguientePeticion(); 
            }
        } catch (InterruptedException e) { /*...*/ } 
        finally {
            mutexCola.release();
        }
        
        if (siguiente != null) {
            estaOcupado = true; 
            String algorithmName = diskScheduler.getAlgoritmo().getSchedulingDiskType().name();
            System.out.println("        FileSystem: OCUPADO. Procesando (" + algorithmName + "): " + siguiente.getFileName());
            
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
                FileData data = peticionDeLaCola.getFileData();
                String nombre = data.getFileName();
                int tamano = data.getFileSize();
                String ruta = data.getRuta() != null ? data.getRuta() : "root";
                
                Directorio directorioDestino = buscarDirectorioPorRuta(ruta);
                if (directorioDestino == null) {
                    System.out.println("FileSystem: Error, el directorio '" + ruta + "' no existe.");
                    data.setErrorMessage("El directorio '" + ruta + "' no existe.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                if (directorioDestino.buscarArchivo(nombre) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' ya existe en el directorio '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' ya existe en el directorio '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }

                Thread.sleep(1000);
                Lista<Integer> bloquesAsignados = disk.asignarBloques(tamano);
                
                if (bloquesAsignados == null) {
                    System.out.println("FileSystem: Error, no hay espacio para '" + nombre + "'.");
                    data.setErrorMessage("No hay espacio suficiente en el disco.");
                } else {
                    String processName = data.getProcessName();
                    String tipoArchivo = data.getTipoArchivo() != null ? data.getTipoArchivo() : "publico";
                    Archivo nuevoArchivo = new Archivo(nombre, tamano, ruta, processName, tipoArchivo);

                    nuevoArchivo.setBloquesAsignados(bloquesAsignados);

                    tablaDeArchivos.add(nuevoArchivo);
                    directorioDestino.agregarArchivo(nuevoArchivo);
                    
                    System.out.println("FileSystem: Archivo '" + nombre + "' CREADO con éxito en '" + ruta + "' (tipo: " + tipoArchivo + ").");
                    System.out.println("            TAMANO ARCHIVOOOOOL " + nuevoArchivo.getTamano());
                    System.out.println("[FS] Archivo creado con exito");
                }
                                
            } catch (InterruptedException ex) { /*...*/ } 
            finally {
                registrarEstadistica(peticionDeLaCola.getTiempoInicio());
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
    
    private Archivo buscarArchivo(String nombre) {
        System.out.println(         "TABLA DE ARCHIVO: "+ tablaDeArchivos);
        for (int i = 0; i < tablaDeArchivos.size(); i++) {
            Archivo actual = tablaDeArchivos.get(i);
            if (actual.getNombre().equals(nombre)) {
                return actual;
            }
        }
        return null; 
    }
    
    private Archivo buscarArchivoPorRuta(String nombre, String ruta) {
        Directorio dir = buscarDirectorioPorRuta(ruta);
        if (dir != null) {
            return dir.buscarArchivo(nombre);
        }
        return null;
    }
    
    private Directorio buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || ruta.equals("root")) {
            return root;
        }
        
        String path = ruta.startsWith("root/") ? ruta.substring(5) : ruta;
        if (path.isEmpty()) {
            return root;
        }
        
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
    
    public String crearDirectorio(String ruta, String nombreDirectorio) {
        Directorio directorioPadre = buscarDirectorioPorRuta(ruta);
        
        if (directorioPadre == null) {
            String msg = "Error: El directorio padre '" + ruta + "' no existe.";
            System.out.println("FileSystem: " + msg);
            return msg; // <--- Devolvemos el error
        }
        
        if (directorioPadre.existe(nombreDirectorio)) {
            String msg = "Error: Ya existe un archivo o directorio con el nombre '" + nombreDirectorio + "' en '" + ruta + "'.";
            System.out.println("FileSystem: " + msg);
            return msg; // <--- Devolvemos el error
        }
        
        Directorio nuevoDirectorio = new Directorio(nombreDirectorio, directorioPadre);
        directorioPadre.agregarSubdirectorio(nuevoDirectorio);
        System.out.println("FileSystem: Directorio '" + nombreDirectorio + "' creado en '" + ruta + "'.");
        return null; 
    }
    
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
        
        Lista<Archivo> archivosAEliminar = directorioAEliminar.obtenerTodosLosArchivos();
        
        for (int i = 0; i < archivosAEliminar.size(); i++) {
            Archivo arch = archivosAEliminar.get(i);
            Lista<Integer> bloques = arch.getBloquesAsignados();
            if (bloques != null && bloques.size() > 0) {
                disk.liberarBloques(bloques);
            }
            tablaDeArchivos.remove(arch);
        }
        
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
        return null; 
    }
    
    public void deleteFile(Petition peticionDeLaCola) {
        new Thread(() -> {
            try {
                FileData data = peticionDeLaCola.getFileData();
                String nombre = data.getFileName();
                String ruta = data.getRuta() != null ? data.getRuta() : "root";
                String modoUsuario = data.getModoUsuario() != null ? data.getModoUsuario() : "Usuario";
                
                if ("Usuario".equals(modoUsuario)) {
                    System.out.println("FileSystem: Error, el usuario no tiene permisos para eliminar archivos.");
                    data.setErrorMessage("Acceso denegado: Solo el administrador puede eliminar archivos.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Archivo archivo = buscarArchivoPorRuta(nombre, ruta);
                
                if (archivo == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Thread.sleep(1000);
                
                Lista<Integer> bloquesALiberar = archivo.getBloquesAsignados();
                if (bloquesALiberar != null && bloquesALiberar.size() > 0) {
                    disk.liberarBloques(bloquesALiberar);
                }
                
                tablaDeArchivos.remove(archivo);
                Directorio dir = buscarDirectorioPorRuta(ruta);
                if (dir != null) {
                    dir.eliminarArchivo(archivo);
                }
                
                System.out.println("FileSystem: Archivo '" + nombre + "' ELIMINADO con éxito de '" + ruta + "'.");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                registrarEstadistica(peticionDeLaCola.getTiempoInicio());
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
                String modoUsuario = data.getModoUsuario() != null ? data.getModoUsuario() : "Usuario";
                
                Archivo archivo = buscarArchivoPorRuta(nombre, ruta);
                
                if (archivo == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombre + "' no existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                if ("Usuario".equals(modoUsuario) && "privado".equals(archivo.getTipoArchivo())) {
                    System.out.println("FileSystem: Error, el usuario no tiene permisos para leer el archivo privado '" + nombre + "'.");
                    data.setErrorMessage("Acceso denegado: No tiene permisos para leer archivos privados del sistema.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Thread.sleep(1500);
                
                System.out.println("FileSystem: Archivo '" + nombre + "' LEÍDO (solo lectura, sin modificaciones) de '" + ruta + "'.");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                registrarEstadistica(peticionDeLaCola.getTiempoInicio());
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
                String modoUsuario = data.getModoUsuario() != null ? data.getModoUsuario() : "Usuario";
                
                if ("Usuario".equals(modoUsuario)) {
                    System.out.println("FileSystem: Error, el usuario no tiene permisos para actualizar archivos.");
                    data.setErrorMessage("Acceso denegado: Solo el administrador puede actualizar archivos.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                if (nombreNuevo == null || nombreNuevo.isEmpty()) {
                    System.out.println("FileSystem: Error, el nuevo nombre no puede estar vacío.");
                    data.setErrorMessage("El nuevo nombre no puede estar vacío.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Archivo archivo = buscarArchivoPorRuta(nombreViejo, ruta);
                
                if (archivo == null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombreViejo + "' no existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombreViejo + "' no existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Directorio dir = buscarDirectorioPorRuta(ruta);
                if (dir != null && dir.buscarArchivo(nombreNuevo) != null) {
                    System.out.println("FileSystem: Error, el archivo '" + nombreNuevo + "' ya existe en '" + ruta + "'.");
                    data.setErrorMessage("El archivo '" + nombreNuevo + "' ya existe en '" + ruta + "'.");
                    data.setIsProcessed(true);
                    estaOcupado = false;
                    System.out.println("FileSystem: LIBRE (por este hilo).");
                    return;
                }
                
                Thread.sleep(1000);
                
                archivo.setNombre(nombreNuevo);
                System.out.println("FileSystem: Archivo '" + nombreViejo + "' RENOMBRADO a '" + nombreNuevo + "' con éxito en '" + ruta + "'.");
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                registrarEstadistica(peticionDeLaCola.getTiempoInicio());
                peticionDeLaCola.getFileData().setIsProcessed(true);
                estaOcupado = false;
                System.out.println("FileSystem: LIBERADO (por este hilo).");
            }
        }).start();
    }
    
    
}
