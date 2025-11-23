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
public class Archivo {
    private String nombre;
    private int tamano; 
    private String ruta; // Directory path like "root/x/y/z"
    
    // Tu idea de la lista de bloques. ¡Perfecta!
    // Guardamos los IDs de los bloques que ocupa.
    private Lista<Integer> bloquesAsignados; 
    private String processName; // Nombre del proceso que creó este archivo

    public Archivo(String nombre, int tamano) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = "root"; // Default to root
        this.bloquesAsignados = new Lista<>();    
        this.processName = null;
    }
    
    public Archivo(String nombre, int tamano, String processName) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = "root"; // Default to root
        this.bloquesAsignados = new Lista<>();    
        this.processName = processName;
    }
    
    public Archivo(String nombre, int tamano, String ruta, String processName) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = ruta != null ? ruta : "root";
        this.bloquesAsignados = new Lista<>();    
        this.processName = processName;
    }

    // --- Getters y Setters ---
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getTamano() { return tamano; }
    public void setTamano(int tamano) { this.tamano = tamano; }
    public Lista<Integer> getBloquesAsignados() { return bloquesAsignados; }
    
    // 2. NECESITAS este método para guardar los bloques después
    public void setBloquesAsignados(Lista<Integer> bloquesAsignados) {
        this.bloquesAsignados = bloquesAsignados;
    }
    
    public String getProcessName() {
        return processName;
    }
    
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    public String getRuta() {
        return ruta;
    }
    
    public void setRuta(String ruta) {
        this.ruta = ruta != null ? ruta : "root";
    }
    
    @Override
    public String toString() {
        return nombre + " (" + tamano + " bloques)";
    }
    
}
