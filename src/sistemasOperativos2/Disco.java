package sistemasOperativos2;

/**
 * Simula el Disco (SD) como un conjunto de bloques.
 */
public class Disco {

    // Atributos
    private final Bloque[] bloques; // El array de bloques
    private static final int CANTIDAD_BLOQUES = 100; // Tamaño máximo [cite: 34, 35]

    /**
     * Constructor. Inicializa el disco y crea todos los bloques.
     */
    public Disco() {
        this.bloques = new Bloque[CANTIDAD_BLOQUES];
        
        // ¡Importante! Hay que crear cada objeto Bloque individualmente
        for (int i = 0; i < CANTIDAD_BLOQUES; i++) {
            this.bloques[i] = new Bloque(i);
        }
    }

    /**
     * Busca el primer bloque que esté libre en el disco.
     * @return El ID del bloque libre, o -1 si el disco está lleno.
     */
    public int buscarBloqueLibre() {
        for (int i = 0; i < CANTIDAD_BLOQUES; i++) {
            if (this.bloques[i].isEstaLibre()) {
                return this.bloques[i].getId();
            }
        }
        return -1; // No hay bloques libres
    }

    /**
     * Ocupa un bloque específico y le dice cuál es el siguiente.
     * @param id El ID del bloque a ocupar.
     * @param idSiguiente El ID del siguiente bloque (-1 si es el último).
     */
    public void ocuparBloque(int id, int idSiguiente) {
        if (id >= 0 && id < CANTIDAD_BLOQUES) {
            this.bloques[id].setEstaLibre(false);
            this.bloques[id].setSiguienteBloque(idSiguiente);
        }
    }

    /**
     * Libera un bloque específico.
     * @param id El ID del bloque a liberar.
     */
    public void liberarBloque(int id) {
        if (id >= 0 && id < CANTIDAD_BLOQUES) {
            this.bloques[id].setEstaLibre(true);
            this.bloques[id].setSiguienteBloque(-1);
            System.out.println("Disco: Bloque " + id + " liberado.");
        }
    }
    
    // --- Método de Prueba ---
    
    /**
     * (PARA PRUEBAS) Imprime el estado actual del disco en la consola.
     * [L] = Libre, [O] = Ocupado
     */
    public void imprimirEstadoDisco() {
        System.out.println("--- Estado del Disco (SD) ---");
        for (int i = 0; i < CANTIDAD_BLOQUES; i++) {
            // Imprime [L] o [O] y un salto de línea cada 10 bloques
            String estado = this.bloques[i].isEstaLibre() ? "[L]" : "[O]";
            System.out.print(estado);
            
            if ((i + 1) % 10 == 0) {
                System.out.println(); // Salto de línea
            }
        }
        System.out.println("-----------------------------");
    }
    
    /**
     * Devuelve el ID del siguiente bloque en la cadena de un archivo.
     * @param idBloqueActual El bloque que estamos consultando.
     * @return El ID del siguiente bloque, o -1 si es el final.
     */
    public int getSiguienteBloqueDe(int idBloqueActual) {
        if (idBloqueActual >= 0 && idBloqueActual < CANTIDAD_BLOQUES) {
            return this.bloques[idBloqueActual].getSiguienteBloque();
        }
        return -1; // ID de bloque no válido
    }
    
}