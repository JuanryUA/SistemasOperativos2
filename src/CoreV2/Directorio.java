/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

/**
 *
 * @author verol
 */
public class Directorio {
    private String nombre;
    private String ruta; // Full path like "root/x/y/z"
    private Directorio padre; // Parent directory
    private Lista<Directorio> subdirectorios; // Child directories
    private Lista<Archivo> archivos; // Files in this directory
    
    public Directorio(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.subdirectorios = new Lista<>();
        this.archivos = new Lista<>();
        
        // Calculate full path
        if (padre == null) {
            this.ruta = nombre; // Root directory
        } else {
            if (padre.getRuta().equals("root")) {
                this.ruta = padre.getRuta() + "/" + nombre;
            } else {
                this.ruta = padre.getRuta() + "/" + nombre;
            }
        }
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
        // Update path if name changes
        if (padre == null) {
            this.ruta = nombre;
        } else {
            this.ruta = padre.getRuta() + "/" + nombre;
        }
    }
    
    public String getRuta() {
        return ruta;
    }
    
    public Directorio getPadre() {
        return padre;
    }
    
    public Lista<Directorio> getSubdirectorios() {
        return subdirectorios;
    }
    
    public Lista<Archivo> getArchivos() {
        return archivos;
    }
    
    public void agregarSubdirectorio(Directorio subdirectorio) {
        subdirectorios.add(subdirectorio);
    }
    
    public void eliminarSubdirectorio(Directorio subdirectorio) {
        subdirectorios.remove(subdirectorio);
    }
    
    public void agregarArchivo(Archivo archivo) {
        archivos.add(archivo);
    }
    
    public void eliminarArchivo(Archivo archivo) {
        archivos.remove(archivo);
    }
    
    /**
     * Calculate the total size of this directory (sum of all files)
     */
    public int calcularTamano() {
        int tamano = 0;
        
        // Sum sizes of all files in this directory
        for (int i = 0; i < archivos.size(); i++) {
            tamano += archivos.get(i).getTamano();
        }
        
        // Recursively sum sizes of all subdirectories
        for (int i = 0; i < subdirectorios.size(); i++) {
            tamano += subdirectorios.get(i).calcularTamano();
        }
        
        return tamano;
    }
    
    /**
     * Find a subdirectory by name
     */
    public Directorio buscarSubdirectorio(String nombre) {
        for (int i = 0; i < subdirectorios.size(); i++) {
            Directorio dir = subdirectorios.get(i);
            if (dir.getNombre().equals(nombre)) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * Find a file by name in this directory
     */
    public Archivo buscarArchivo(String nombre) {
        for (int i = 0; i < archivos.size(); i++) {
            Archivo arch = archivos.get(i);
            if (arch.getNombre().equals(nombre)) {
                return arch;
            }
        }
        return null;
    }
    
    /**
     * Check if a file or directory with the given name exists in this directory
     */
    public boolean existe(String nombre) {
        return buscarArchivo(nombre) != null || buscarSubdirectorio(nombre) != null;
    }
    
    /**
     * Delete this directory and all its contents recursively
     */
    public void eliminarRecursivo() {
        // Delete all files
        while (!archivos.isEmpty()) {
            archivos.remove(archivos.get(0));
        }
        
        // Recursively delete all subdirectories
        while (!subdirectorios.isEmpty()) {
            Directorio subdir = subdirectorios.get(0);
            subdir.eliminarRecursivo();
            subdirectorios.remove(subdir);
        }
    }
    
    /**
     * Get all files recursively from this directory and all subdirectories
     */
    public Lista<Archivo> obtenerTodosLosArchivos() {
        Lista<Archivo> todos = new Lista<>();
        
        // Add files from this directory
        for (int i = 0; i < archivos.size(); i++) {
            todos.add(archivos.get(i));
        }
        
        // Recursively add files from subdirectories
        for (int i = 0; i < subdirectorios.size(); i++) {
            Lista<Archivo> archivosSubdir = subdirectorios.get(i).obtenerTodosLosArchivos();
            todos.addAll(archivosSubdir);
        }
        
        return todos;
    }
}

