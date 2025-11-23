/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.concurrent.Semaphore;

/**
 *
 * @author verol
 */

class DiskBlock {
    public int id;
    public boolean estaLibre;
    // public Archivo archivo; // (Opcional, para saber qué archivo está aquí)

    public DiskBlock(int id) {
        this.id = id;
        this.estaLibre = true;
    }
} 

public class Disk {
    private final Semaphore mutex = new Semaphore(1); 
    
    private final Lista<DiskBlock> bloques; // El disco físico
    private int bloquesTotales;
    private int bloquesDisponibles;
    
    // --- ¡NUEVO CONSTRUCTOR! ---
    public Disk(int cantidadBloques) {
        this.bloquesTotales = cantidadBloques;
        this.bloquesDisponibles = cantidadBloques;
        this.bloques = new Lista<>();
        for (int i = 0; i < cantidadBloques; i++) {
            bloques.add(new DiskBlock(i)); // Crea los bloques, 0, 1, 2...
        }
    }
    
    
    public Lista<Integer> asignarBloques(int cantidadNecesaria) {
        try {
            mutex.acquire(); // Protege la lista de bloques
            
            // 1. Validar si hay espacio (¡como dijiste!)
            if (cantidadNecesaria > this.bloquesDisponibles) {
                System.out.println("Disk: ¡Error! No hay espacio. Necesita " + cantidadNecesaria + 
                                   ", disponibles " + this.bloquesDisponibles);
                return null; // No hay espacio
            }

            Lista<Integer> bloquesAsignados = new Lista<>();
            int encontrados = 0;
            
            // 2. Buscar los bloques libres (no contiguos)
            for (int i = 0; i < bloques.size(); i++) {
                if (encontrados == cantidadNecesaria) {
                    break; // Ya tenemos suficientes
                }
                
                DiskBlock bloque = bloques.get(i);
                if (bloque.estaLibre) {
                    bloque.estaLibre = false;
                    bloquesAsignados.add(bloque.id);
                    encontrados++;
                }
            }

            // 3. Actualizar contador y retornar la lista de IDs
            this.bloquesDisponibles -= cantidadNecesaria;
            System.out.println("Disk: Asignados " + cantidadNecesaria + " bloques. Disponibles: " + this.bloquesDisponibles);
            return bloquesAsignados;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }
    
    
    public void liberarBloques(Lista<Integer> bloquesALiberar) {
        try {
            mutex.acquire();
            
            for (int i = 0; i < bloquesALiberar.size(); i++) {
                int idBloque = bloquesALiberar.get(i);
                // (Aquí podrías hacer bloques.get(idBloque) si sabes que el ID es el índice)
                // Por seguridad, lo buscamos:
                for(int j = 0; j < bloques.size(); j++) {
                    if (bloques.get(j).id == idBloque) {
                        bloques.get(j).estaLibre = true;
                        break;
                    }
                }
            }
            
            this.bloquesDisponibles += bloquesALiberar.size();
            System.out.println("Disk: Liberados " + bloquesALiberar.size() + " bloques. Disponibles: " + this.bloquesDisponibles);

        } catch (InterruptedException e) { /*...*/ } 
        finally {
            mutex.release();
        }
    }
    
    // --- MÉTODOS PARA LA INTERFAZ GRÁFICA ---
    public boolean esBloqueLibre(int index) {
        try {
            mutex.acquire(); // Importante: usar el semáforo para leer
            if (index < 0 || index >= bloques.size()) {
                 return false; // O manejar error
            }
            // Accedemos al bloque usando tu Lista genérica
            DiskBlock b = bloques.get(index); 
            return b.estaLibre;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }
    
    public int getTotalBloques() {
        return this.bloquesTotales;
    }
    
    
    
    
//    private final Semaphore mutex = new Semaphore(1); 
//
//    public void guardarProceso(Proceso p) {
//        try {
//            mutex.acquire();
////            colaLargoPlazo.add(p);
//            System.out.println("Disco: " + p.getNombre() + " guardado en disco");
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } finally {
//            mutex.release();
//        }
//    }
//    
//    public void sacarProcesoDisco(Proceso p){
//        try {
//            mutex.acquire();
//            System.out.println("Disco: " + p.getNombre() + " sacado de disco");
//        } catch (InterruptedException ex) {
//        }finally{
//        mutex.release();}
//    }
    

//    public Proceso cargarProceso() {
//        try {
//            mutex.acquire();
//            Proceso p = colaLargoPlazo.poll();
//            if (p != null) {
//                System.out.println("Disk: Proceso " + p.getId() + " cargado desde disco");
//            }
//            return p;
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return null;
//        } finally {
//            mutex.release();
//        }
//    }


//    public boolean hayProcesosLongTerm() {
//        try {
//            mutex.acquire();
//            return !colaLargoPlazo.isEmpty();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return false;
//        } finally {
//            mutex.release();
//        }
//    }
}

