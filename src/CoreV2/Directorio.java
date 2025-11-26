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
    private String ruta; 
    private Directorio padre; 
    private Lista<Directorio> subdirectorios; 
    private Lista<Archivo> archivos;
    
    public Directorio(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.subdirectorios = new Lista<>();
        this.archivos = new Lista<>();
        
        if (padre == null) {
            this.ruta = nombre; 
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
    

    public int calcularTamano() {
        int tamano = 0;
        
        for (int i = 0; i < archivos.size(); i++) {
            tamano += archivos.get(i).getTamano();
        }
        
        for (int i = 0; i < subdirectorios.size(); i++) {
            tamano += subdirectorios.get(i).calcularTamano();
        }
        
        return tamano;
    }
    

    public Directorio buscarSubdirectorio(String nombre) {
        for (int i = 0; i < subdirectorios.size(); i++) {
            Directorio dir = subdirectorios.get(i);
            if (dir.getNombre().equals(nombre)) {
                return dir;
            }
        }
        return null;
    }
    

    public Archivo buscarArchivo(String nombre) {
        for (int i = 0; i < archivos.size(); i++) {
            Archivo arch = archivos.get(i);
            if (arch.getNombre().equals(nombre)) {
                return arch;
            }
        }
        return null;
    }
    

    public boolean existe(String nombre) {
        return buscarArchivo(nombre) != null || buscarSubdirectorio(nombre) != null;
    }
    

    public void eliminarRecursivo() {
        while (!archivos.isEmpty()) {
            archivos.remove(archivos.get(0));
        }
        
        while (!subdirectorios.isEmpty()) {
            Directorio subdir = subdirectorios.get(0);
            subdir.eliminarRecursivo();
            subdirectorios.remove(subdir);
        }
    }
    

    public Lista<Archivo> obtenerTodosLosArchivos() {
        Lista<Archivo> todos = new Lista<>();
        
        for (int i = 0; i < archivos.size(); i++) {
            todos.add(archivos.get(i));
        }
        
        for (int i = 0; i < subdirectorios.size(); i++) {
            Lista<Archivo> archivosSubdir = subdirectorios.get(i).obtenerTodosLosArchivos();
            todos.addAll(archivosSubdir);
        }
        
        return todos;
    }
}

