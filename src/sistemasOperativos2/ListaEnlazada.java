/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasOperativos2;

/**
 *
 * @author juanr
 */
public class ListaEnlazada {

    // Atributo
    private Nodo cabeza; // Apunta al PRIMER nodo de la lista. Si es null, la lista está vacía.

    /**
     * Constructor. Inicializa la lista como vacía.
     */
    public ListaEnlazada() {
        this.cabeza = null;
    }

    /**
     * Método para saber si la lista no tiene elementos.
     * @return true si la cabeza es null, false de lo contrario.
     */
    public boolean estaVacia() {
        return this.cabeza == null;
    }

    /**
     * Agrega un nuevo dato AL FINAL de la lista.
     * @param dato El objeto a agregar.
     */
    public void agregar(Object dato) {
        // 1. Crear el nuevo nodo (la "caja")
        Nodo nuevoNodo = new Nodo(dato);

        // 2. Verificar si la lista está vacía
        if (estaVacia()) {
            // Si está vacía, el nuevo nodo es ahora la cabeza
            this.cabeza = nuevoNodo;
        } else {
            // 3. Si no está vacía, hay que recorrer la lista hasta el final
            Nodo actual = this.cabeza;
            while (actual.getSiguiente() != null) {
                // Moverse al siguiente nodo
                actual = actual.getSiguiente();
            }
            
            // 4. Cuando 'actual' es el ÚLTIMO nodo, enganchamos el nuevo
            actual.setSiguiente(nuevoNodo);
        }
    }

    /**
     * (PARA PRUEBAS) Imprime el contenido de la lista en la consola.
     */
    public void imprimirLista() {
        if (estaVacia()) {
            System.out.println("La lista está vacía.");
            return;
        }

        Nodo actual = this.cabeza;
        int contador = 0;
        System.out.println("--- Contenido de la Lista ---");
        while (actual != null) {
            // Imprimimos el dato que está DENTRO del nodo
            System.out.println("Nodo " + contador + ": " + actual.getDato().toString());
            actual = actual.getSiguiente(); // Avanzamos al siguiente
            contador++;
        }
        System.out.println("-----------------------------");
    }
    
    public boolean buscar(Object dato) {
        if (estaVacia()) {
            return false;
        }

        Nodo actual = this.cabeza;
        while (actual != null) {
            // Comparamos el dato del nodo actual con el dato buscado
            if (actual.getDato().equals(dato)) {
                return true; // ¡Lo encontramos!
            }
            actual = actual.getSiguiente(); // Avanzamos al siguiente
        }

        return false; // No se encontró después de recorrer toda la lista
    }

    /**
     * Elimina la PRIMERA aparición de un dato en la lista.
     * @param dato El objeto que se desea eliminar.
     */
    public void eliminar(Object dato) {
        if (estaVacia()) {
            // No hay nada que eliminar
            return;
        }

        // Caso 1: El nodo a eliminar es la cabeza
        if (this.cabeza.getDato().equals(dato)) {
            this.cabeza = this.cabeza.getSiguiente();
            return;
        }

        // Caso 2: El nodo a eliminar está en medio o al final
        Nodo anterior = this.cabeza;
        Nodo actual = this.cabeza.getSiguiente();

        while (actual != null) {
            if (actual.getDato().equals(dato)) {
                // "Saltamos" el nodo actual, conectando el anterior con el siguiente
                anterior.setSiguiente(actual.getSiguiente());
                return; // Terminamos
            }
            // Avanzamos los punteros
            anterior = actual;
            actual = actual.getSiguiente();
        }
        
        // Si el bucle termina, significa que no se encontró el dato (pasado la cabeza)
        System.out.println("Dato '" + dato + "' no encontrado para eliminar.");
    }
    
    /**
     * (Añadido para la Clase Cola)
     * Devuelve el dato del primer nodo (cabeza) sin eliminarlo.
     * @return El objeto en la cabeza, o null si la lista está vacía.
     */
    public Object getDatoCabeza() {
        if (estaVacia()) {
            return null;
        }
        return this.cabeza.getDato();
    }
    
    /**
     * (Añadido para la Clase Directorio)
     * Devuelve el nodo cabeza completo.
     * @return El objeto Nodo de la cabeza, o null si la lista está vacía.
     */
    public Nodo getNodoCabeza() {
        return this.cabeza;
    }
    
}
