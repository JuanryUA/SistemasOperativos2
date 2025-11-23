/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.Random;

/**
 *
 * @author verol
 */
public class Proceso {
    public enum Tipo { IO_BOUND, CPU_BOUND };
    private final Tipo tipo;
//    public enum Estado { NUEVO, LISTO, EJECUCION, BLOQUEADO, LISTOSUSPENDIDO, BLOQUEADOSUSPENDIDO, TERMINADO };
    public enum Estado { NUEVO, LISTO, EJECUCION, BLOQUEADO, TERMINADO };
    private Estado estado;
    private final int id;
    private final String nombre;
    private int programCounter; // PC: instrucción actual
    private int memoryAddressRegister; // MAR: última dirección de memoria accedida
    private final int instrucciones;  //cant total de instrucciones
    private final long duracionTotal; // en ticks
//    private final long quantum;       // 0 si no tiene quantum
//    private final long tiempoES;      // en ticks, si hace E/S
    private int tamano;               // tamaño de memoria requerido
    private long primerTicEjecucion;           // tic en el cual comenzo el procesp
    private long salidaTicEjecucion;           // tic en el cual salio el procesp

    private long ticInicioEspera = 0;        // tic en que empezó a esperar
    private long ultimoTicEjecucion = 0;     // tic de última ejecución
    private long tiempoEjecutado = 0;        // ticks ya ejecutados IGNORAR
    private long tiempoEsperando = 0;        // ticks total en espera
    private boolean interrumpido = false;
    private int actualInstruccion;   //por la cual va
    private int instruccionesParaES;  //cuantas instrucs tienen que pasar para que agarre ES
                                      // se usa en ejecutarInstruccion de clase CPU. ahi reviso si la instruc actual es igual a esta
    private int ciclosParaCompletarES;  //cuantas instrucs tienen que pasar para que termine ES, le dices al dma SLEEP por tanto tiempo (dma es thread)
                                        // se usa en ejecutarES de la clase DMA.
    private PCB pcb;
    private FileData fileData;

    public Proceso() {
        this.tipo = null;
        this.id = 0;
        this.nombre = null;
        this.instrucciones = 0;
        this.duracionTotal = 0;
    }
    
    private int queuePriority = 0;
    

    public int getActualInstruccion() {
        return actualInstruccion;
    }
    
    public int nextInstruccion() {
        return actualInstruccion++;
    }
    
    public boolean isLastInstruccion() {
        return actualInstruccion >= instrucciones;
    }

    public int getInstruccionesParaES() {
        return instruccionesParaES;
    }

    public int getCiclosParaCompletarES() {
        return ciclosParaCompletarES;
    }
    
    public int getRemainingTime() {
        return this.instrucciones - this.actualInstruccion;
    }

    Random random = new Random();
    private int startAddress = random.nextInt(1000) + 100; // Ejemplo: entre 100 y 1099
    
    public Proceso(int id, String nombre, String fileName, int fileSize) { //constructor simplificado
       
        this.id = id;
        this.nombre = nombre;
        this.tipo = Tipo.IO_BOUND;
        this.instrucciones = 2;
        this.actualInstruccion = 0;
        this.duracionTotal = instrucciones;  //Todas las instrucciones se ejecutan en un único ciclo de instrucción
//        this.quantum = quantum;
//        this.tiempoES = tiempoES;
        this.tamano = this.instrucciones*2;
        this.estado = Estado.NUEVO;
        this.programCounter = this.startAddress;
        this.memoryAddressRegister = this.programCounter * this.instrucciones;
        this.instruccionesParaES = 1;
        this.ciclosParaCompletarES = 3;
        this.pcb = new PCB(id, nombre, this.estado, this.programCounter, this.memoryAddressRegister);
        this.fileData= new FileData(fileName, fileSize, nombre); // Pasar el nombre del proceso
    }
    
    public Proceso(int id, String nombre, FileData fileData) { //constructor con FileData
        this.id = id;
        this.nombre = nombre;
        this.tipo = Tipo.IO_BOUND;
        this.instrucciones = 2;
        this.actualInstruccion = 0;
        this.duracionTotal = instrucciones;
        this.tamano = this.instrucciones*2;
        this.estado = Estado.NUEVO;
        this.programCounter = this.startAddress;
        this.memoryAddressRegister = this.programCounter * this.instrucciones;
        this.instruccionesParaES = 1;
        this.ciclosParaCompletarES = 3;
        this.pcb = new PCB(id, nombre, this.estado, this.programCounter, this.memoryAddressRegister);
        this.fileData = fileData;
    }

    public FileData getFileData() {
        return fileData;
    }
    
    
    public long getPrimerTicEjecucion() {
        return primerTicEjecucion;
    }

    public long getSalidaTicEjecucion() {
        return salidaTicEjecucion;
    }
    
    public void setPrimerTicEjecucion(long primerTicEjecucion) {
        this.primerTicEjecucion = primerTicEjecucion;
    }
    
    public void setSalidaTicEjecucion(long salidaTicEjecucion) {
        this.salidaTicEjecucion = salidaTicEjecucion;
    }
    
    public float getEquidad() {
        return (float)(((float)salidaTicEjecucion-(float)primerTicEjecucion)/(float)instrucciones);
    }
    
    
    public void incrementarPCyMAR() {
        this.programCounter++;
        this.memoryAddressRegister++;
        
        this.setMemoryAddressRegister(memoryAddressRegister);
        this.setProgramCounter(programCounter);
        this.pcb.setPc(this.programCounter);
        this.pcb.setMar(this.memoryAddressRegister);
    }

    public PCB getPcb() {
        return pcb;
    }

    public int getQueuePriority() {
        return queuePriority;
    }
    

    public int getId() { return this.pcb.getId(); }
    public Tipo getTipo() { return tipo; }
    public long getDuracionTotal() { return duracionTotal; }
//    public long getQuantum() { return quantum; }
//    public long getTiempoES() { return tiempoES; }
    public int getTamano() { return tamano; }
//    public void setTamano(int tamano) { this.tamano = tamano; }

    public boolean isInterrumpido() { return interrumpido; }
    public void setInterrumpido(boolean interrumpido) { this.interrumpido = interrumpido; }

    public int getStartAddress() { return startAddress; }
    public void setStartAddress(int address) { this.startAddress = address; }

    public void setTicInicioEspera(long tic) { this.ticInicioEspera = tic; } //tiempo en el que empezo E/S
    public long getTicInicioEspera(){ return this.ticInicioEspera; }
    public long getUltimoTicEjecucion(){ return this.ultimoTicEjecucion; } //cuando se dejo de ejecutar en que tic iba
    public void setUltimoTicEjecucion(long tic) { this.ultimoTicEjecucion = tic; }

    public void actualizarTiempoEjecutado(long ticActual) {
        tiempoEjecutado += (ticActual - ultimoTicEjecucion);
        ultimoTicEjecucion = ticActual;
    }

//    public void actualizarTiempoEsperando(long ticActual) {
//        
//        System.out.println("aja->"+ticActual+"---"+ticInicioEspera);
//        if(ticInicioEspera==0){
//            ticInicioEspera = ticActual;
//        }
//        else{
//            tiempoEsperando += (ticActual - ticInicioEspera);
//            ticInicioEspera = ticActual;
//        }
//    }
    
//    public void actualizarTiempoEsperando(long ticActual) {
//        tiempoEsperando ++;
//        ticInicioEspera = ticActual;
//    }

    public void incrementarTiempoEjecutado() {
        tiempoEjecutado++;
    }

    public long getTiempoEjecutado() { return tiempoEjecutado; }
    public long getTiempoEsperando() { return tiempoEsperando; }

    public void setTiempoEsperando(long tiempoEsperando) {
        this.tiempoEsperando = tiempoEsperando;
    }

    public int getInstrucciones() {
        return instrucciones;
    }
    
    
    

    public boolean estaCompletado() {
//      System.out.println("[Proceso a validar "+this.id+"] "+"Validando completacion --> tiempo ejecutado: "+ tiempoEjecutado+ " duracionTotal: "+ duracionTotal);
        return tiempoEjecutado >= duracionTotal;
    }

    public Estado getEstado() { return this.pcb.getEstado(); }
    public void setEstado(Estado nuevoEstado) { 
        if (nuevoEstado == Proceso.Estado.NUEVO){
            System.out.printf(
            "📄 [%s] Agregado con éxito a la cola de largo plazo%n",
             nombre );
            return;
        }
        Estado anterior = this.pcb.getEstado();
        this.estado = nuevoEstado; 
        this.pcb.setEstado(nuevoEstado);
    System.out.printf(
        "📄 [%s] Estado cambiado: %s → %s | PC=%d | MAR=%d%n",
        nombre, anterior, nuevoEstado, this.pcb.getPc(), this.pcb.getMar()
    );
    }

    public String getNombre() {
        return this.pcb.getNombre();
    }
   
    
    public int getProgramCounter() { return this.pcb.getPc(); }
    public void setProgramCounter(int programCounter) { 
        this.programCounter = programCounter;
        this.pcb.setPc(programCounter);
    }

    public int getMemoryAddressRegister() { return this.pcb.getMar(); }
    public void setMemoryAddressRegister(int mar) { 
        this.memoryAddressRegister = mar; 
        this.pcb.setMar(mar);
    }
    public void penalizar() {
        if (this.getQueuePriority() < 2) {
            this.setQueuePriority(this.getQueuePriority() + 1);
        }
    }

    public void setQueuePriority(int queuePriority) {
        this.queuePriority = queuePriority;
    }

}
