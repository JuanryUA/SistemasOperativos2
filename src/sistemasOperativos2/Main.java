/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sistemasOperativos2;

/**
 *
 * @author juanr
 */
public class Main {

    /**
     * @param args the command line arguments
     */
 
    public static void main(String[] args) {

            System.out.println("--- Iniciando prueba del Sistema de Archivos ---");

            // 1. Crear el sistema
            SistemaArchivos miSistema = new SistemaArchivos();
            Directorio raiz = miSistema.getRaiz();
            Disco disco = miSistema.getMiDisco();

            // 2. Crear archivos y directorios
            miSistema.crearArchivo("boot.ini", 2, raiz); // Ocupa 2 bloques
            miSistema.crearDirectorio("home", raiz);

            Directorio home = (Directorio) raiz.buscarHijo("home");
            miSistema.crearArchivo("mi_documento.txt", 4, home); // Ocupa 4 bloques

            // 3. Ver el estado del disco (debe tener 2 + 4 = 6 bloques [O])
            System.out.println("\n--- Estado del Disco (después de crear) ---");
            disco.imprimirEstadoDisco();

            // 4. Ver la estructura del árbol
            System.out.println("\n--- Árbol de Directorios ---");
            miSistema.imprimirArbol(raiz, "");

            // 5. Eliminar un archivo
            System.out.println("\n--- Eliminando 'boot.ini' ---");
            miSistema.eliminarArchivo("boot.ini", raiz);

            // 6. Ver el estado del disco (debe tener 4 bloques [O])
            System.out.println("\n--- Estado del Disco (después de eliminar) ---");
            disco.imprimirEstadoDisco();

            // 7. Ver la estructura del árbol
            System.out.println("\n--- Árbol de Directorios ---");
            miSistema.imprimirArbol(raiz, "");

            System.out.println("\n--- Prueba de Sistema de Archivos terminada ---");
        }    
}
    

