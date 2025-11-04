package sistemasOperativos2;

/**
 * Representa un directorio (carpeta) en el sistema.
 * Puede contener una lista de Archivos y otros Directorios (subdirectorios).
 */
public class Directorio {

    private String nombre;
    private Directorio padre; // Referencia al directorio padre (null si es la raíz)
    private ListaEnlazada hijos; // ¡Usamos nuestra lista enlazada!

    /**
     * Constructor para un nuevo directorio.
     * @param nombre El nombre del directorio.
     * @param padre El directorio que lo contiene (null para la raíz).
     */
    public Directorio(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.hijos = new ListaEnlazada(); // Un directorio nace con una lista vacía de hijos
    }

    // --- Getters ---

    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Directorio getPadre() {
        return padre;
    }

    public ListaEnlazada getHijos() {
        return hijos;
    }
    
    /**
     * Añade un archivo o subdirectorio a este directorio.
     * @param hijo Puede ser un objeto Archivo o un objeto Directorio.
     */
    public void agregarHijo(Object hijo) {
        this.hijos.agregar(hijo);
    }
    
    /**
     * (PARA PRUEBAS) Busca un hijo (archivo o dir) por su nombre.
     * @param nombre El nombre a buscar.
     * @return El objeto (Archivo o Directorio) si se encuentra, o null.
     */
    public Object buscarHijo(String nombre) {
        if (hijos.estaVacia()) {
            return null;
        }
        
        // Tenemos que "recorrer" la lista enlazada manualmente
        Nodo actual = hijos.getNodoCabeza(); // 
        
        while (actual != null) {
            Object datoHijo = actual.getDato();
            String nombreHijo = "";
            
            // Verificamos de qué tipo es el hijo
            if (datoHijo instanceof Archivo) {
                nombreHijo = ((Archivo) datoHijo).getNombre();
            } else if (datoHijo instanceof Directorio) {
                nombreHijo = ((Directorio) datoHijo).getNombre();
            }
            
            // Comparamos
            if (nombreHijo.equals(nombre)) {
                return datoHijo; // ¡Encontrado!
            }
            
            actual = actual.getSiguiente();
        }
        
        return null; // No se encontró
    }
    
    // Método para facilitar la impresión
    @Override
    public String toString() {
        return "Directorio: " + nombre;
    }
    
    /**
     * Elimina un hijo (archivo o directorio) de la lista de este directorio.
     * @param hijo El objeto (Archivo o Directorio) a eliminar.
     */
    public void eliminarHijo(Object hijo) {
        this.hijos.eliminar(hijo);
    }
    
}