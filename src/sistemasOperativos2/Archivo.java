package sistemasOperativos2;

public class Archivo {

    private String nombre;
    private int tamanoEnBloques;
    private int primerBloque; 

    /**
     * @param nombre El nombre del archivo (ej. "texto.txt")
     * @param tamanoEnBloques El número de bloques que ocupa.
     * @param primerBloque El ID del primer bloque asignado en el disco.
     */
    public Archivo(String nombre, int tamanoEnBloques, int primerBloque) {
        this.nombre = nombre;
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
    }

    // --- Getters y Setters ---

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }
    
    // Método para facilitar la impresión
    @Override
    public String toString() {
        return "Archivo: " + nombre + " (" + tamanoEnBloques + " bloques)";
    }
}