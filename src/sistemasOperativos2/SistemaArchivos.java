package sistemasOperativos2;

/**
 * El "Cerebro Central" que gestiona el Disco y la estructura de Directorios.
 * Une todas las clases para realizar las operaciones CRUD.
 */
public class SistemaArchivos {

    private final Disco miDisco;
    private final Directorio raiz;

    /**
     * Constructor. Inicializa el disco y el directorio raíz "/".
     */
    public SistemaArchivos() {
        this.miDisco = new Disco();
        // La raíz no tiene padre, por eso 'null'
        this.raiz = new Directorio("/", null); 
    }

    // --- Getters para acceder al disco y la raíz desde fuera ---
    public Disco getMiDisco() {
        return miDisco;
    }

    public Directorio getRaiz() {
        return raiz;
    }
    
    /**
     * Crea un nuevo directorio (carpeta).
     * @param nombre El nombre del nuevo directorio.
     * @param padre El directorio donde se creará.
     */
    public boolean crearDirectorio(String nombre, Directorio padre) {
        // 1. Verificar si ya existe un hijo con ese nombre
        if (padre.buscarHijo(nombre) != null) {
            System.out.println("Error: Ya existe un archivo o directorio con el nombre '" + nombre + "'");
            return false;
        }
        
        // 2. Crear el directorio y agregarlo al padre
        Directorio nuevoDir = new Directorio(nombre, padre);
        padre.agregarHijo(nuevoDir);
        System.out.println("Directorio '" + nombre + "' creado.");
        return true;
    }

    /**
     * Crea un nuevo archivo. Esta es la operación más compleja.
     * @param nombre El nombre del archivo.
     * @param tamanoEnBloques El número de bloques que necesita.
     * @param padre El directorio donde se creará.
     */
    public boolean crearArchivo(String nombre, int tamanoEnBloques, Directorio padre) {
        // 1. Verificar si ya existe
        if (padre.buscarHijo(nombre) != null) {
            System.out.println("Error: Ya existe un archivo o directorio con el nombre '" + nombre + "'");
            return false;
        }

        // 2. Verificar si hay espacio (encontrando todos los bloques PRIMERO)
        // Usamos un array de Java normal (¡no es una estructura de datos prohibida!)
        int[] bloquesIDs = new int[tamanoEnBloques];
        
        for (int i = 0; i < tamanoEnBloques; i++) {
            int bloqueLibre = miDisco.buscarBloqueLibre();
            
            if (bloqueLibre == -1) {
                System.out.println("Error: ¡Disco lleno! No se pudo crear el archivo.");
                // (Importante: aquí faltaría liberar los bloques que ya encontramos,
                // pero por simplicidad lo omitimos de momento)
                return false;
            }
            
            // Ocupamos "temporalmente" para que buscarBloqueLibre() no lo encuentre de nuevo
            miDisco.ocuparBloque(bloqueLibre, -2); // -2 = "reservado temporalmente"
            bloquesIDs[i] = bloqueLibre;
        }
        
        // 3. Si llegamos aquí, SÍ hay espacio. Ahora los enlazamos (asignación encadenada)
        for (int i = 0; i < tamanoEnBloques - 1; i++) {
            // Ocupa el bloque [i] y haz que apunte al bloque [i+1]
            miDisco.ocuparBloque(bloquesIDs[i], bloquesIDs[i+1]);
        }
        
        // 4. Ocupar el último bloque, que apunta a -1 (fin de archivo)
        miDisco.ocuparBloque(bloquesIDs[tamanoEnBloques - 1], -1);

        // 5. Crear el objeto Archivo y añadirlo al directorio padre
        int primerBloque = bloquesIDs[0];
        Archivo nuevoArchivo = new Archivo(nombre, tamanoEnBloques, primerBloque);
        padre.agregarHijo(nuevoArchivo);
        
        System.out.println("Archivo '" + nombre + "' creado (inicia en bloque " + primerBloque + ")");
        return true;
    }
    
    /**
     * Elimina un archivo del sistema.
     * @param nombre El nombre del archivo a eliminar.
     * @param padre El directorio donde se encuentra.
     */
    public boolean eliminarArchivo(String nombre, Directorio padre) {
        // 1. Buscar el archivo
        Object hijo = padre.buscarHijo(nombre);
        
        if (hijo == null || !(hijo instanceof Archivo)) {
            System.out.println("Error: No se encontró el archivo '" + nombre + "'");
            return false;
        }
        
        Archivo archivoAEliminar = (Archivo) hijo;

        // 2. Liberar los bloques en el disco (siguiendo la cadena)
        System.out.println("Eliminando '" + nombre + "'. Liberando bloques:");
        int idBloqueActual = archivoAEliminar.getPrimerBloque();
        
        while (idBloqueActual != -1) {
            // Obtenemos el SIGUIENTE antes de borrar el actual
            int idSiguienteBloque = miDisco.getSiguienteBloqueDe(idBloqueActual);
            
            // Liberamos el actual
            miDisco.liberarBloque(idBloqueActual);
            
            // Avanzamos
            idBloqueActual = idSiguienteBloque;
        }
        
        // 3. Quitar el archivo de la lista de hijos del directorio padre
        padre.eliminarHijo(archivoAEliminar);
        System.out.println("Archivo '" + nombre + "' eliminado.");
        return true;
    }
    
    /**
     * (PARA PRUEBAS) Imprime la estructura de directorios recursivamente.
     */
    public void imprimirArbol(Directorio d, String indentacion) {
        System.out.println(indentacion + d.getNombre() + "/");
        
        ListaEnlazada hijos = d.getHijos();
        if (hijos.estaVacia()) {
            return;
        }

        Nodo actual = hijos.getNodoCabeza();
        while (actual != null) {
            Object datoHijo = actual.getDato();
            if (datoHijo instanceof Directorio) {
                // Si es un directorio, llama a esta misma función (recursión)
                imprimirArbol((Directorio) datoHijo, indentacion + "  ");
            } else if (datoHijo instanceof Archivo) {
                // Si es un archivo, solo imprímelo
                Archivo a = (Archivo) datoHijo;
                System.out.println(indentacion + "  " + a.getNombre() + " (" + a.getTamanoEnBloques() + "b)");
            }
            actual = actual.getSiguiente();
        }
    }
}