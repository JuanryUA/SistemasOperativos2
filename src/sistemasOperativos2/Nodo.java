    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasOperativos2;

/**
 *
 * @author juanr
 */
public class Nodo {

    // Atributos
    private Object dato;      // El dato que queremos guardar (puede ser cualquier cosa)
    private Nodo siguiente; // La "flecha" que apunta al siguiente nodo

    /**
     * Constructor para crear un nuevo nodo.
     * @param dato El objeto (archivo, proceso, etc.) que se almacenará.
     */
    public Nodo(Object dato) {
        this.dato = dato;
        this.siguiente = null; // Por defecto, no apunta a nada
    }

    // --- Métodos Getters y Setters ---
    // (Nos permiten leer y modificar los atributos privados)

    public Object getDato() {
        return dato;
    }

    public void setDato(Object dato) {
        this.dato = dato;
    }

    public Nodo getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Nodo siguiente) {
        this.siguiente = siguiente;
    }
}
