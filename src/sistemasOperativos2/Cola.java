package sistemasOperativos2;


/**
 * Implementación de una Cola (Queue) desde cero, usando nuestra ListaEnlazada.
 * Funciona bajo el principio FIFO (First In, First Out).
 */
public class Cola {

    // Atributo
    private ListaEnlazada lista; // Usamos nuestra lista como almacenamiento

    /**
     * Constructor. Inicializa la cola creando una nueva lista interna.
     */
    public Cola() {
        this.lista = new ListaEnlazada();
    }

    /**
     * Verifica si la cola está vacía.
     * @return true si no hay elementos.
     */
    public boolean estaVacia() {
        return this.lista.estaVacia();
    }

    /**
     * Agrega un elemento AL FINAL de la cola (en-colar).
     * @param dato El objeto a agregar.
     */
    public void encolar(Object dato) {
        // Simplemente usamos el 'agregar' de nuestra lista,
        // que ya agrega al final.
        this.lista.agregar(dato);
    }

    /**
     * Saca y devuelve el elemento DEL FRENTE de la cola (desen-colar).
     * @return El objeto que estaba al frente, o null si la cola está vacía.
     */
    public Object desencolar() {
        if (estaVacia()) {
            System.out.println("Error: La cola está vacía.");
            return null;
        }

        // 1. Ver cuál es el dato del frente (la cabeza de la lista)
        Object datoAlFrente = this.verFrente();
        
        // 2. Usar el 'eliminar' de nuestra lista para quitarlo
        this.lista.eliminar(datoAlFrente);
        
        // 3. Devolver el dato que guardamos
        return datoAlFrente;
    }

    /**
     * Devuelve el elemento DEL FRENTE de la cola, pero SIN SACARLO.
     * @return El objeto que está al frente, o null si la cola está vacía.
     */
    public Object verFrente() {
        if (estaVacia()) {
            return null;
        }
        
        // El 'verFrente' de una cola es simplemente ver la 'cabeza' de
        // nuestra lista. Necesitamos un pequeño método en ListaEnlazada
        // para esto.
        
        // --- TEMPORAL: Vamos a agregar getCabeza() a ListaEnlazada ---
        // (Lo haremos en el siguiente paso)
        return this.lista.getDatoCabeza(); 
    }
    
    /**
     * (PARA PRUEBAS) Imprime el contenido de la cola.
     */
    public void imprimirCola() {
        System.out.println("--- Contenido de la Cola (Frente -> Final) ---");
        // La función 'imprimirLista' de nuestra lista ya
        // imprime desde la cabeza (frente) hasta el final.
        this.lista.imprimirLista();
    }
    
    
    
}