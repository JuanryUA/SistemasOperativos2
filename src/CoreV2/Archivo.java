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
    private String ruta; 
    
    private Lista<Integer> bloquesAsignados; 
    private String processName; 
    private String tipoArchivo; 

    public Archivo(String nombre, int tamano) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = "root"; 
        this.bloquesAsignados = new Lista<>();    
        this.processName = null;
        this.tipoArchivo = "publico"; 
    }
    
    public Archivo(String nombre, int tamano, String processName) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = "root"; 
        this.bloquesAsignados = new Lista<>();    
        this.processName = processName;
        this.tipoArchivo = "publico"; 
    }
    
    public Archivo(String nombre, int tamano, String ruta, String processName) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = ruta != null ? ruta : "root";
        this.bloquesAsignados = new Lista<>();    
        this.processName = processName;
        this.tipoArchivo = "publico"; 
    }
    
    public Archivo(String nombre, int tamano, String ruta, String processName, String tipoArchivo) {
        this.nombre = nombre;
        this.tamano = tamano;
        this.ruta = ruta != null ? ruta : "root";
        this.bloquesAsignados = new Lista<>();    
        this.processName = processName;
        this.tipoArchivo = tipoArchivo != null ? tipoArchivo : "publico";
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getTamano() { return tamano; }
    public void setTamano(int tamano) { this.tamano = tamano; }
    public Lista<Integer> getBloquesAsignados() { return bloquesAsignados; }
    
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
    
    public String getTipoArchivo() {
        return tipoArchivo;
    }
    
    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo != null ? tipoArchivo : "publico";
    }
    
    @Override
    public String toString() {
        return nombre + " (" + tamano + " bloques)";
    }
    
}
