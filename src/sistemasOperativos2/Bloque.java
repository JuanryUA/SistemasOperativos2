package sistemasOperativos2;

/**
 * Representa un solo bloque de almacenamiento en el Disco (SD).
 */
public class Bloque {

    // Atributos
    private final int id;      // El número de este bloque (ej. 0, 1, 2...)
    private boolean estaLibre; // true si no está en uso, false si está ocupado
    private int siguienteBloque; // Para la asignación encadenada [cite: 14]
                               // Guarda el ID del siguiente bloque del mismo archivo
                               // -1 si es el último bloque del archivo o está libre

    /**
     * Constructor para un nuevo bloque.
     * @param id El número identificador de este bloque.
     */
    public Bloque(int id) {
        this.id = id;
        this.estaLibre = true;   // Nace libre
        this.siguienteBloque = -1; // No apunta a nada
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public boolean isEstaLibre() {
        return estaLibre;
    }

    public void setEstaLibre(boolean estaLibre) {
        this.estaLibre = estaLibre;
    }

    public int getSiguienteBloque() {
        return siguienteBloque;
    }

    public void setSiguienteBloque(int siguienteBloque) {
        this.siguienteBloque = siguienteBloque;
    }
}