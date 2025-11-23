/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;
import CoreV2.ALgorithmsStrategies.ISchedulingAlgorithm;

import java.util.concurrent.Semaphore;
//import sistemasoperativos1.SimuladorGUI;
import javax.swing.SwingUtilities;
//import java.util.HashMap;
//import java.util.Map;
import CoreV2.ALgorithmsStrategies.ISchedulingAlgorithm.SchedulingType;
//import java.util.*;


/**
 *
 * @author verol
 */
public class OperatingSystem {
    private String nextProcessName = null;
    private final Scheduler scheduler;
    private final CPU cpu;
    private final MainMemory memory;
    private final Disk disk;
    private final DMA dma;
    private final Clock clock;
    private final FileSystem filesystem;
    public int processCounter;
//    private SimuladorGUI gui;
    
    private Cola colaNuevos = new Cola();
    private Cola colaListos = new Cola();
    private Cola colaBloqueados = new Cola();
    private Cola colaTerminados = new Cola();
//    private Cola colaListoSuspendido = new Cola();
//    private Cola colaBloqueadoSuspendido = new Cola();
    
//    private Queue<Proceso> colaNuevos = new LinkedList<>();
//    private Queue<Proceso> colaListos = new LinkedList<>();
//    private Queue<Proceso> colaBloqueados = new LinkedList<>();
//    private Queue<Proceso> colaTerminados = new LinkedList<>();
//    private Queue<Proceso> colaListoSuspendido = new LinkedList<>();
//    private Queue<Proceso> colaBloqueadoSuspendido = new LinkedList<>();
    private Thread quantumThread;
    private boolean stopQuantumThread;
    

    private final Semaphore mutex = new Semaphore(1); 

    private int stat_procesosTotalesTerminados = 0;
    private int stat_ioBoundTerminados = 0;
    private int stat_cpuBoundTerminados = 0;
    private SchedulingType currentPolicyType;
    private Map<SchedulingType, Integer> terminosPorPolitica;
    private Map<SchedulingType, Long> ciclosPorPolitica;
    private Map<SchedulingType, Integer> terminosIOBoundPorPolitica;
    private Map<SchedulingType, Integer> terminosCPUBoundPorPolitica;
    private final Map<SchedulingType, Long> tiempoEsperaTotalPorPolitica = new HashMap<>();
    private Lista<Float>[] equidadesPorPolitica;


    public OperatingSystem(CPU cpu, MainMemory memory, Disk disk, DMA dma, Scheduler scheduler, Clock clock, FileSystem filesystem) {
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
        this.dma = dma;
        this.scheduler = scheduler; 
//        this.filesystem
//                this.dickscheduler
        this.clock = clock;
        this.filesystem=filesystem;
        this.processCounter = 1;
        this.stopQuantumThread = false;
        
        this.terminosPorPolitica = new HashMap<>();
        this.ciclosPorPolitica = new HashMap<>();
        this.terminosIOBoundPorPolitica = new HashMap<>(); 
        this.terminosCPUBoundPorPolitica = new HashMap<>(); 
        
    equidadesPorPolitica = new Lista[SchedulingType.values().length];
for (int i = 0; i < equidadesPorPolitica.length; i++) {
    equidadesPorPolitica[i] = new Lista<>();
}
        
        for (SchedulingType type : SchedulingType.values()) {
            this.terminosPorPolitica.put(type, 0);
            this.ciclosPorPolitica.put(type, 0L);
            this.terminosIOBoundPorPolitica.put(type, 0); 
            this.terminosCPUBoundPorPolitica.put(type, 0);
    }

           this.currentPolicyType = scheduler.getAlgoritmo().getSchedulingType();
    }
    
//    public void setGUI(SimuladorGUI gui) {
//        this.gui = gui;
//    }
    
    //NO IO Bound
    public void crearProceso(Proceso.Tipo tipo, int instrucciones, int prioridad, String nombreFuturoArchivo, int sizeFuturoArchivo) {
//        System.out.println("entro A CRar");
        String nombre = "P"+processCounter;
        
//        Proceso p = new Proceso(id, tipo, nombre, instrucciones, this.cpu.getQuantumCiclos(),tiempoES, tamano, 0, 0);
        Proceso p = new Proceso(processCounter, nombre, nombreFuturoArchivo, sizeFuturoArchivo);
        p.setEstado(Proceso.Estado.NUEVO);
        p.setPrimerTicEjecucion(clock.getTic());
        moverANuevos(p);
        this.agregarProceso(p);
        processCounter++;
    }
    
    //IO Bound EL QUE SE USA EN ESTE PROYECTO NADA MAS !!!
    public void crearProceso(Proceso.Tipo tipo, int prioridad, String nombreFuturoArchivo, int sizeFuturoArchivo) {
        crearProceso(tipo, prioridad, nombreFuturoArchivo, sizeFuturoArchivo, "root");
    }
    
    //IO Bound with directory path
    public void crearProceso(Proceso.Tipo tipo, int prioridad, String nombreFuturoArchivo, int sizeFuturoArchivo, String ruta) {
//        System.out.println("entro A CRar");
        String nombre = "P"+processCounter;

        Proceso p = new Proceso(processCounter, nombre, nombreFuturoArchivo, sizeFuturoArchivo, ruta);
        p.setEstado(Proceso.Estado.NUEVO);
        p.setPrimerTicEjecucion(clock.getTic());
        
//        logEvent("Proceso " + p.getNombre() + " creado.");
        // ...
//        if (memory.cargarProceso(p)) {
//             logEvent("Proceso " + p.getNombre() + " cargado en memoria principal (LISTO).");
//             // ...
//        } else {
//             logEvent("Memoria llena. Intentando suspender para Proceso " + p.getNombre() + ".");
//             // ...
//        }
        processCounter++;
        moverANuevos(p);
        this.agregarProceso(p);
    }
    
    // Método para crear procesos IO con diferentes operaciones CRUD
    public void crearProcesoIO(FileData.OperationType operationType, String nombreArchivo) {
        crearProcesoIO(operationType, nombreArchivo, "root");
    }
    
    public void crearProcesoIO(FileData.OperationType operationType, String nombreArchivo, String ruta) {
        String nombre = "P" + processCounter;
        // Crear FileData con el tipo de operación correcto
        FileData fileData = new FileData(nombreArchivo, ruta, operationType, nombre);
        Proceso p = new Proceso(processCounter, nombre, fileData);
        p.setEstado(Proceso.Estado.NUEVO);
        p.setPrimerTicEjecucion(clock.getTic());
        processCounter++;
        moverANuevos(p);
        this.agregarProceso(p);
    }
    
    public void crearProcesoIO(FileData.OperationType operationType, String nombreArchivo, String nuevoNombre, String ruta) {
        String nombre = "P" + processCounter;
        // Crear FileData con el tipo de operación correcto
        FileData fileData = new FileData(nombreArchivo, nuevoNombre, ruta, operationType, nombre);
        Proceso p = new Proceso(processCounter, nombre, fileData);
        p.setEstado(Proceso.Estado.NUEVO);
        p.setPrimerTicEjecucion(clock.getTic());
        processCounter++;
        moverANuevos(p);
        this.agregarProceso(p);
    }

    public void agregarProceso(Proceso p) {
        try {
            
//        System.out.println(colaListos.size());
            mutex.acquire();
            if (memory.cargarProceso(p)) {
//                memory.agregarAColaCortoPlazo(p);
                System.out.println("SO: " + p.getNombre() + " cargado en memoria principal");
//             logEvent("Proceso " + p.getNombre() + " cargado en memoria principal (LISTO).");
                colaNuevos.remove(p);
                p.setEstado(Proceso.Estado.LISTO);
                
//        System.out.println("---"+colaListos.size());
                moverAColaListos(p);
//        System.out.println("+++"+colaListos.size());
            } else {
            // ▼▼▼ ESTA ES LA LÓGICA NUEVA ▼▼▼
            // Memoria llena. El proceso ya está en colaNuevos por defecto.
            // No hacemos nada más que informar.
            System.out.println("SO: Memoria llena. " + p.getNombre() + " permanece en cola de Nuevos.");
            // ▲▲▲ FIN DE LA LÓGICA NUEVA ▲▲▲
        }
//            else {
//                p.setEstado(Proceso.Estado.LISTOSUSPENDIDO);
//                colaNuevos.remove(p);
//                moverAListoSuspendidos(p);
//                
//                disk.guardarProceso(p);
//            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

//    public void asignarProcesoACPU() {
//        System.out.println(!cpu.estaOcupado()+" "+ !scheduler.hayProcesos());
//        if (!cpu.estaOcupado() || !scheduler.hayProcesos()) return;
//        
//        Proceso siguiente = scheduler.obtenerSiguienteProceso();
//        if (siguiente != null) {
//            cpu.asignarProceso(siguiente, clock.getTic());
//            siguiente.setEstado(Proceso.Estado.EJECUCION);
//            System.out.println("Proceso a ejecutar según planificador (algoritmo) --> "+siguiente);
//        }
//    }

    



    public void interrumpirProceso(Proceso p) {
        moverAColaListos(p);
    }
    
    /**
 * Intenta cargar procesos de la cola de Nuevos a la memoria (Listos)
 * si hay espacio disponible.
 */
public void intentarCargarProcesosNuevos() {
    // Iteramos de forma segura. Sacamos uno, probamos, y si falla lo volvemos a meter.
    int n = colaNuevos.size();
    if (n == 0) {
        System.out.println("DEBUG: colaNuevos está vacía. Nada que cargar.");
        return;
    }
    
    System.out.println("DEBUG: Revisando " + n + " procesos en colaNuevos.");

    // Usamos un bucle for tradicional porque modificaremos la cola
    for (int i = 0; i < n; i++) {
        Proceso p = colaNuevos.poll(); // Saca el primero
        if (p == null) break; // Seguridad

        if (memory.cargarProceso(p)) {
            // Éxito: Mover de Nuevos a Listos
            p.setEstado(Proceso.Estado.LISTO);
            moverAColaListos(p);
            System.out.println("SO: " + p.getNombre() + " cargado desde Nuevos a Listos.");
        } else {
            // Fracaso: Memoria sigue llena. Devolver a la cola (al final).
            System.out.println("DEBUG: " + p.getNombre() + " no cupo. Devolviendo a la cola.");
            colaNuevos.add(p);
            break;
            // IMPORTANTE: Si el primero que intentamos no cupo,
            // es muy probable que los siguientes (que llegaron después) tampoco.
            // Rompemos el bucle para no revisar innecesariamente.
        }
    }
}

    public void procesoFinalizado(Proceso p) {
        p.setEstado(Proceso.Estado.TERMINADO);
        p.setSalidaTicEjecucion(clock.getTic());
        long tiempoEsperando = (p.getSalidaTicEjecucion() - p.getPrimerTicEjecucion())-p.getInstrucciones();
        p.setTiempoEsperando(tiempoEsperando);
        moverATerminados(p);
        memory.liberarProceso(p);
        System.out.println("DEBUG: P" + p.getId() + " terminó. Revisando cola de nuevos...");
        // ▼▼▼ AÑADIR ESTA LÍNEA AQUÍ ▼▼▼
    intentarCargarProcesosNuevos();
    // ▲▲▲ FIN DE LA LÍNEA AÑADIDA ▲▲▲
//        logEvent("Proceso " + p.getNombre() + " pasa a TERMINADO y se libera memoria."); // <-- Añadir log
        System.out.println("SO: " + p.getNombre() + " finalizado y liberado de memoria");
//        System.out.println("--->----> "+p.getTiempoEsperando());
//        System.out.println("TIC DE INICIO: "+p.getPrimerTicEjecucion());
//        System.out.println("TIC DE FIN: "+p.getSalidaTicEjecucion());
//        System.out.println("TIEMPO ESPERADO: "+ tiempoEsperando);
//        System.out.println("EQUIDAD: "+p.getEquidad());
        this.stat_procesosTotalesTerminados++;
        if (p.getTipo() == Proceso.Tipo.IO_BOUND) {
            this.stat_ioBoundTerminados++;
        } else if (p.getTipo() == Proceso.Tipo.CPU_BOUND) {
            this.stat_cpuBoundTerminados++;
        }
        
        if (currentPolicyType != null) {
            long esperaActual = p.getTiempoEsperando();
            tiempoEsperaTotalPorPolitica.put(
                currentPolicyType,
                tiempoEsperaTotalPorPolitica.getOrDefault(currentPolicyType, 0L) + esperaActual
            );
        }
        
        if (currentPolicyType != null) {
    equidadesPorPolitica[currentPolicyType.ordinal()].add(p.getEquidad());
}

        
        if (currentPolicyType != null) {
            terminosPorPolitica.put(currentPolicyType, terminosPorPolitica.getOrDefault(currentPolicyType, 0) + 1);

            if (p.getTipo() == Proceso.Tipo.IO_BOUND) {
                terminosIOBoundPorPolitica.put(currentPolicyType, terminosIOBoundPorPolitica.getOrDefault(currentPolicyType, 0) + 1);
            } else if (p.getTipo() == Proceso.Tipo.CPU_BOUND) {
                terminosCPUBoundPorPolitica.put(currentPolicyType, terminosCPUBoundPorPolitica.getOrDefault(currentPolicyType, 0) + 1);
            }
        }
        
    }

    public void bloquearProcesoES(Proceso p) {
//        logEvent("Proceso " + p.getNombre() + " pasa a BLOQUEADO por E/S."); // <-- Añadir log
        System.out.println("SO: " + p.getNombre() + " bloqueado por E/S");
        p.setEstado(Proceso.Estado.BLOQUEADO);
        moverABloqueados(p);
        dma.ejecutarES(p, this.filesystem, () -> {
//            logEvent("DMA: E/S completada para Proceso " + p.getNombre() + "."); // <-- Añadir log
            System.out.println("SO: E/S completada para " + p.getNombre());
            if (p.getEstado()==Proceso.Estado.BLOQUEADO){
                this.colaBloqueados.remove(p);
                p.setEstado(Proceso.Estado.LISTO);
//                logEvent("Proceso " + p.getNombre() + " pasa de BLOQUEADO a LISTO."); // <-- Añadir log
                moverAColaListos(p);
            } 
//            else if (p.getEstado()==Proceso.Estado.BLOQUEADOSUSPENDIDO){
//                this.colaBloqueadoSuspendido.remove(p);
//                p.setEstado(Proceso.Estado.LISTOSUSPENDIDO);
////                logEvent("Proceso " + p.getNombre() + " pasa de BLOQUEADOSUSPENDIDO a LISTOSUSPENDIDO."); // <-- Añadir log
//                moverAListoSuspendidos(p);
//            }
////            validacionAgregarAlCPU(siguiente);
        });
    }

    public void setAlgoritmo(ISchedulingAlgorithm algoritmo) {
        // Actualizar la política actual
        this.currentPolicyType = algoritmo.getSchedulingType();
        scheduler.setAlgoritmo(algoritmo);
//        logEvent("SO: Algoritmo de planificación cambiado a " + this.currentPolicyType); // <-- Añadir log
        System.out.println("SO: Algoritmo de planificación cambiado");
    }
    
    public void startQuantumTime(){
        this.quantumThread = new Thread(()->{
            while(this.cpu.getActualQuantumCiclosCounter() > 0 && !stopQuantumThread){
                try {
                    Thread.sleep(clock.getTicTimeMs());
//                    System.out.println(this.cpu.getActualQuantumCiclosCounter());
                    this.cpu.decreaseQuantumCounter();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if(!stopQuantumThread){
                this.cpu.throwInterruptToCPU();
                System.out.println("Interrupcion por quantum de "+ this.cpu.getQuantumCiclos()+ "ms excedido");
            }
            this.cpu.resetQuantumCounter();
            this.stopQuantumThread = false;
        });
        this.quantumThread.start();
    }
    
    public void stopQuantumTime(){
        this.stopQuantumThread = true;
        this.cpu.resetQuantumCounter();
    }
    
    public void validacionAgregarAlCPU(Proceso siguiente){
        if (siguiente != null) {
            cpu.asignarProceso(siguiente, clock.getTic());
            System.out.println("Proceso a ejecutar según planificador (algoritmo) --> "+siguiente.getNombre());
            siguiente.setEstado(Proceso.Estado.EJECUCION);
//            logEvent("Proceso " + siguiente.getNombre() + " pasa a EJECUCIÓN."); // <-- Añadir log
            //System.out.println(this.cpu.getActualQuantumCiclosCounter()); //descomentar solo cuando no quiera funcionar bien el quantum!!!
            if (this.cpu.getQuantumCiclos()>0){            
                startQuantumTime();
            }
        }
    }
    public void notifyTic() {
        try {
            
            if (currentPolicyType != null) {
                ciclosPorPolitica.put(currentPolicyType, ciclosPorPolitica.getOrDefault(currentPolicyType, 0L) + 1);
            }
            
            mutex.acquire();
            if (cpu.getProcesoActual() != null) {
                
                Proceso actual = cpu.getProcesoActual();
            if (actual.getEstado() == Proceso.Estado.EJECUCION) {
                actual.incrementarPCyMAR(); 
            }
                cpu.ejecutarInstruccion( this);
            }
//            colaListos.forEach(p -> p.actualizarTiempoEsperando(clock.getTic()));
//            for (Proceso p : this.colaListos) {
////                System.out.println(this.colaListos.size());
//                p.actualizarTiempoEsperando(clock.getTic());
//            }

            if (!cpu.estaOcupado() && scheduler.hayProcesos()) {
                Proceso siguiente = scheduler.obtenerSiguienteProceso();
                if (siguiente != null) {
//                  Proceso siguiente = scheduler.obtenerSiguienteProceso();
//                    if (siguiente != null) {
//                        cpu.asignarProceso(siguiente, clock.getTic());
//                        siguiente.setEstado(Proceso.Estado.EJECUCION);
//                        System.out.println("Proceso a ejecutar según planificador (algoritmo) --> "+siguiente);
//                    }
//                    logEvent("Planificador selecciona Proceso " + siguiente.getNombre() + "."); // <-- Añadir log
                    validacionAgregarAlCPU(siguiente);
                }
            }
            
//            this.filesystem.getDiskScheduler().getNextPeticion();
            this.filesystem.getNextPeticion();
//            verificarMoverListosSuspendidosAListos();
            
//            if (this.gui != null) {
//                SwingUtilities.invokeLater(() -> {
//                    gui.actualizarGUI();
//                });
//            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }
    
//    public void verificarMoverListosSuspendidosAListos(){
//        int cantidadDeListosSuspendidos = this.colaListoSuspendido.size();
//        for (int i = 0; i<cantidadDeListosSuspendidos; i++){
//            Proceso pActual = this.colaListoSuspendido.poll(); //aqui ya se hace remove con poll
//            boolean procesoCargadoEnMainMemory = this.memory.cargarProceso(pActual);
//            if (procesoCargadoEnMainMemory){
//                this.colaListos.add(pActual);
//                pActual.setEstado(Proceso.Estado.LISTO);
//                disk.sacarProcesoDisco(pActual);
////                logEvent("SO: Proceso " + pActual.getNombre() + " reactivado (LISTOSUSPENDIDO -> LISTO)."); // <-- Añadir log
//            }else {
//                this.colaListoSuspendido.add(pActual);
//                
//            }
//        }
//    }
//    // 🔹 Verifica si hay falta de memoria y suspende procesos si es necesario
//    public void verificarYSuspenderProcesos(Proceso p) {
//        if (!memory.findAvailableBlock(p)) {
//            Proceso candidato = null;
//            
//            boolean keepVerifying = true;
//            
//            while (keepVerifying){
//                if (!colaBloqueados.isEmpty()) {
//                candidato = colaBloqueados.poll(); // Saca el primero bloqueado
//                } 
//                else {
//                    candidato = null;
//                    colaNuevos.remove(p);
//                    p.setEstado(Proceso.Estado.LISTOSUSPENDIDO);
//                    moverAListoSuspendidos(p);
//                    disk.guardarProceso(p);
//                    keepVerifying = false;
//                    break;
//                }
//
//                if (candidato != null) {
//                    this.memory.liberarProceso(candidato);
//                    suspenderProceso(candidato);
////                    System.out.println("Proceso " + candidato.getNombre() + " suspendido por falta de memoria.");
//                    if (memory.hayEspacioDisponible() && this.memory.findAvailableBlock(p)){
//                        this.memory.cargarProceso(p);
//                        p.setEstado(Proceso.Estado.LISTO);
//                        this.moverAColaListos(p);
//                        keepVerifying = false;
//                        break;
//                    }
//                    candidato = null; 
//                }
//            }
//            
//        }
//    }

//    public void suspenderProceso(Proceso p) {
////        colaBloqueados.remove(p); //ya se hace POLL de candidato
//        memory.liberarProceso(p);
//        p.setEstado(Proceso.Estado.BLOQUEADOSUSPENDIDO);
//        moverABloqueadoSuspendidos(p);
//        disk.guardarProceso(p);
////        logEvent("SO: Proceso " + p.getNombre() + " suspendido (BLOQUEADOSUSPENDIDO) por falta de memoria."); // <-- Añadir log
//        System.out.println("SO: " + p.getNombre() + " suspendido (bloqueado/suspendido) para liberar memoria.");
//    }
    
//    public void reactivarProcesosBloqueadoSuspendidos() {
//        if (!colaBloqueadoSuspendido.isEmpty()) {
//            Proceso p = colaBloqueadoSuspendido.peek();
//            if (memory.cargarProceso(p)) {
//                colaBloqueadoSuspendido.remove(p);
//                p.setEstado(Proceso.Estado.BLOQUEADO);
//                moverABloqueados(p);
//                System.out.println("SO: " + p.getNombre() + " reactivado y cargado en memoria en lista de Bloqueados.");
//            }
//        }
//    }

    public Cola getColaNuevos() {
        return colaNuevos;
    }

    public Cola getColaListos() {
        return colaListos;
    }

    public Cola getColaBloqueados() {
        return colaBloqueados;
    }

    public Cola getColaTerminados() {
        return colaTerminados;
    }

//    public Cola getColaListoSuspendido() {
//        return colaListoSuspendido;
//    }
//
//    public Cola getColaBloqueadoSuspendido() {
//        return colaBloqueadoSuspendido;
//    }
    
    // ----- Métodos de transición ----- No se cambian estados porque se cambian en los metodos
    public void moverANuevos(Proceso p) {
         colaNuevos.add(p);
        
    }
    
    public void moverAColaListos(Proceso p) {
//        scheduler.agregarProcesos(p);
        colaListos.add(p);
//        System.out.println(colaListos.size());
//        logEvent("Proceso " + p.getNombre() + " movido a Cola de Listos."); // <-- Añade/Verifica este log
        System.out.println("SO: " + p.getNombre() + " pasa a la cola de listos");
//                System.out.println("!!!!!!tamanoooo de proceso = " +p.getTamano());

    }

    public void moverABloqueados(Proceso p) {
        colaBloqueados.add(p);
    }

//    public void moverAListoSuspendidos(Proceso p) {
//        colaListoSuspendido.add(p);
//    }
//
//    public void moverABloqueadoSuspendidos(Proceso p) {
//        colaBloqueadoSuspendido.add(p);
//    }

    public void moverATerminados(Proceso p) {
        colaTerminados.add(p);
    }
    
    public void setCPUQuantum(int quantum){
        this.cpu.setQuantumCiclos(quantum);
    }

    public CPU getCpu() {
        return this.cpu;
    }

    public Clock getClock() {
        return this.clock;
    }
    
    public int getStatProcesosTotalesTerminados() {
    return this.stat_procesosTotalesTerminados;
    }

    public int getStatIoBoundTerminados() {
        return this.stat_ioBoundTerminados;
    }

    public int getStatCpuBoundTerminados() {
        return this.stat_cpuBoundTerminados;
    }
    
    public int getProcessCounter() {
        return this.processCounter;
    }

    public void setDuracionCiclo(long nuevoTiempoMs) {
        this.clock.setTicTimeMs(nuevoTiempoMs);
        this.dma.setUnidadTiempoMs(nuevoTiempoMs);
    }
    
//    public void logEvent(String message) {
//        if (this.gui != null && this.gui.getAreaLog() != null) {
//            String logMessage = String.format("[Tick %d] %s%n", clock.getTic(), message);
//
//            SwingUtilities.invokeLater(() -> {
//                gui.getAreaLog().append(logMessage);
//                gui.getAreaLog().setCaretPosition(gui.getAreaLog().getDocument().getLength());
//            });
//        } else {
//            System.out.printf("[Tick %d] %s%n", clock.getTic(), message);
//        }   
//    }
    
    public void setNextProcessName(String name) {
        this.nextProcessName = name;
    }
    
    public int getTerminadosPorPolitica(SchedulingType tipo) {
        return this.terminosPorPolitica.getOrDefault(tipo, 0);
    }

    public long getCiclosPorPolitica(SchedulingType tipo) {
        return this.ciclosPorPolitica.getOrDefault(tipo, 0L);
    }
    
    public int getTerminadosIOBoundPorPolitica(SchedulingType tipo) {
        return this.terminosIOBoundPorPolitica.getOrDefault(tipo, 0);
    }

    public int getTerminadosCPUBoundPorPolitica(SchedulingType tipo) {
        return this.terminosCPUBoundPorPolitica.getOrDefault(tipo, 0);
    }
    
    public double getTiempoEsperaPromedioPorPolitica(SchedulingType tipo) {
        long totalEspera = tiempoEsperaTotalPorPolitica.getOrDefault(tipo, 0L);
        int terminados = terminosPorPolitica.getOrDefault(tipo, 0);
        if (terminados == 0) return 0.0; // evitar división por cero
        return (double) totalEspera / terminados;
    }
    
    public double getEquidadPromedioPorPolitica(SchedulingType tipo) {
    Lista<Float> lista = equidadesPorPolitica[tipo.ordinal()];
    if (lista.isEmpty()) return 0.0;

    double suma = 0;
    for (int i = 0; i < lista.size(); i++) {
        suma += lista.get(i);
    }

    return suma / lista.size();
}


}




