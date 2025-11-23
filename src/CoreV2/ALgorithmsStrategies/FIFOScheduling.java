/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2.ALgorithmsStrategies;
import CoreV2.Cola;
import CoreV2.Proceso;


/**
 *
 * @author verol
 */
public class FIFOScheduling implements ISchedulingAlgorithm {
    private Cola colaNuevos = new Cola();
    private Cola colaListos = new Cola();
    private Cola colaBloqueados = new Cola();
    private Cola colaTerminados = new Cola();
    private Cola colaListoSuspendido = new Cola();
    private Cola colaBloqueadoSuspendido = new Cola();
    private SchedulingType type = SchedulingType.FCFS;
    private boolean hasQuantum = false;

    @Override
    public boolean hasQuantum() {
        return hasQuantum;
    }

    
    @Override
        public void agregarProcesoAListos(Proceso p) {
        colaListos.add(p);
    }

    @Override
    public Proceso obtenerSiguienteProceso() {
        return colaListos.poll(); // FIFO: primero en entrar, primero en salir
    }
    
    @Override
    public SchedulingType getSchedulingType() {
        return this.type;
    }

    @Override
    public boolean hayProcesos() {
        return !colaListos.isEmpty();
    }
    
    public void setColaNuevos(Cola cola){
        this.colaNuevos = cola;
    }
    public void setColaListos(Cola cola){
        this.colaListos = cola;
    }
    public void setColaBloqueados(Cola cola){
        this.colaBloqueados = cola;
    }
    public void setColaTerminados(Cola cola){
        this.colaTerminados = cola;
    }
    public void setColaListoSuspendido(Cola cola){
        this.colaListoSuspendido = cola;
    }
    public void setColaBloqueadoSuspendido(Cola cola){
        this.colaBloqueadoSuspendido = cola;
    }
}