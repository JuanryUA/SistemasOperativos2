/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;
import java.util.Comparator;
import java.util.concurrent.Semaphore;


/**
 *
 * @author verol
 */

// Representa un bloque libre dentro de la memoria
class MemoryBlock {
    public int start;  // posición inicial en la memoria
    public int size;   // tamaño del bloque

    public MemoryBlock(int start, int size) {
        this.start = start;
        this.size = size;
    }
}

public class MainMemory {
    private final int capacidad;                      // tamaño total de memoria
    private final Lista<MemoryBlock> bloquesLibres;    // lista de bloques libres
    private final Lista<Proceso> procesosEnMemoria;   // procesos actualmente en memoria

//    private final Queue<Proceso> colaCortoPlazo = new LinkedList<>();
//    private final Queue<Proceso> colaMedianoPlazo = new LinkedList<>();
//    private final Queue<Proceso> colaLargoPlazo = new LinkedList<>();


    private final Semaphore mutex = new Semaphore(1); // protege acceso concurrente

    public MainMemory(int capacidad) {
        this.capacidad = capacidad;
        this.bloquesLibres = new Lista<>();
        this.bloquesLibres.add(new MemoryBlock(0, capacidad)); // todo libre al inicio
        this.procesosEnMemoria = new Lista<>();
    }

    public boolean cargarProceso(Proceso p) {
        try {
            mutex.acquire();
            // ▼▼▼ ¡AÑADE ESTAS LÍNEAS! ▼▼▼
        if (bloquesLibres.isEmpty()) { 
            System.out.println("Memoria: No hay bloques libres.");
            return false;
        }
        // ▲▲▲ ¡FIN DE LA SOLUCIÓN! ▲▲▲
            System.out.println("Memoria capacidad: "+ bloquesLibres.get(0).size);
            for (int i = 0; i < bloquesLibres.size(); i++) {
                MemoryBlock block = bloquesLibres.get(i);

                if (block.size >= p.getTamano()) {
                    p.setStartAddress(block.start);
                    procesosEnMemoria.add(p);

                    if (block.size == p.getTamano()) {
                        bloquesLibres.remove(block);
                    } else {
                        block.start += p.getTamano();
                        block.size -= p.getTamano();
                    }
                    System.out.println("Memoria: " + p.getNombre() + " cargado en memoria principal");
                    return true;
                }
            }
          //  System.out.println("No pude cargar proceso");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        } 
   }
    
    public boolean hayEspacioDisponible() {
        System.out.println("Consultando bloques libres: "+bloquesLibres.size());
        for (int i = 0; i < bloquesLibres.size(); i++) {
            MemoryBlock block = bloquesLibres.get(i);
            if (block.size > 0) return true;
        }
        return false;
    }


    public void liberarProceso(Proceso p) {
        try {
            mutex.acquire();
            procesosEnMemoria.remove(p);
            bloquesLibres.add(new MemoryBlock(p.getStartAddress(), p.getTamano()));
            fusionarBloquesLibres();
//            System.out.println("Memoria liberada: "+p.getTamano());
//            System.out.println("Bloques libres: "+bloquesLibres.get(0).size);

//            System.out.println("Bloques libres: "+bloquesLibres.get(0).size+" "+bloquesLibres.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Memoria no liberada: "+p.getTamano());

        } finally {
            mutex.release();
        }
    }
    
    public void sortByStart(Lista<MemoryBlock> lista) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                MemoryBlock current = lista.get(j);
                MemoryBlock next = lista.get(j + 1);
                if (current.start > next.start) {
                    // Intercambiar
                    lista.set(j, next);
                    lista.set(j + 1, current);
                }
            }
        }
    }


    private void fusionarBloquesLibres() {
        sortByStart(bloquesLibres);
        Lista<MemoryBlock> fusionados = new Lista<>();
        MemoryBlock prev = null;

        for (int i = 0; i < bloquesLibres.size(); i++) {
            MemoryBlock b = bloquesLibres.get(i);

            if (prev == null) {
                prev = b;
            } else {
                if (prev.start + prev.size == b.start) {
                    prev.size += b.size;
                } else {
                    fusionados.add(prev);
                    prev = b;
                }
            }
        }

        if (prev != null) fusionados.add(prev);

        bloquesLibres.clear();
        bloquesLibres.addAll(fusionados);
    }
    
    public boolean findAvailableBlock(Proceso p) {
        for (int i = 0; i < bloquesLibres.size(); i++) {
            MemoryBlock block = bloquesLibres.get(i);
            if (block.size >= p.getTamano()) return true;
        }
        return false;

    }

   
    
//    public void agregarAColaCortoPlazo(Proceso p) {
//        try {
//            mutex.acquire();
//            colaCortoPlazo.add(p);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } finally {
//            mutex.release();
//        }
//    }
//
//    public Proceso sacarDeColaCortoPlazo() {
//        try {
//            mutex.acquire();
//            return colaCortoPlazo.poll();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return null;
//        } finally {
//            mutex.release();
//        }
//    }
//
//    public boolean hayProcesosCortoPlazo() {
//        try {
//            mutex.acquire();
//            return !colaCortoPlazo.isEmpty();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return false;
//        } finally {
//            mutex.release();
//        }
//    }
//
//    public void agregarAColaMedianoPlazo(Proceso p) {
//        try {
//            mutex.acquire();
//            colaMedianoPlazo.add(p);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } finally {
//            mutex.release();
//        }
//    }
//
//    public Proceso sacarDeColaMedianoPlazo() {
//        try {
//            mutex.acquire();
//            return colaMedianoPlazo.poll();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return null;
//        } finally {
//            mutex.release();
//        }
//    }
//
//    public boolean hayProcesosMedianoPlazo() {
//        try {
//            mutex.acquire();
//            return !colaMedianoPlazo.isEmpty();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return false;
//        } finally {
//            mutex.release();
//        }
//    }
//
//    public List<Proceso> getProcesosCortoPlazo() {
//        return new ArrayList<>(colaCortoPlazo);
//    }
//
//    public List<Proceso> getProcesosMedianoPlazo() {
//        return new ArrayList<>(colaMedianoPlazo);
//    }
}


