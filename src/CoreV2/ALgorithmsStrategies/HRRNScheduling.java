/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2.ALgorithmsStrategies;

import CoreV2.Cola;
import CoreV2.Nodo;
import CoreV2.Proceso;

/**
 *
 * @author verol
 */
public class HRRNScheduling implements ISchedulingAlgorithm {
    private Cola colaNuevos = new Cola();
    private Cola colaListos = new Cola();
    private Cola colaBloqueados = new Cola();
    private Cola colaTerminados = new Cola();
    private Cola colaListoSuspendido = new Cola();
    private Cola colaBloqueadoSuspendido = new Cola();  
    private SchedulingType type = SchedulingType.HRRN;
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
        if (colaListos.isEmpty()) return null;

        Proceso mejor = null;
        double mejorRR = -1;
        
        Nodo actual = colaListos.getFrente();
        while (actual != null) {
            Proceso p = actual.getProceso();
            long tiempoEsperando = p.getTiempoEsperando();
            long duracion = p.getDuracionTotal();
            double rr = ((double) tiempoEsperando + duracion) / duracion;

            if (rr > mejorRR) {
                mejorRR = rr;
                mejor = p;
            }

            actual = actual.getSiguiente();
        }
//        for (Proceso p : colaListos) {
//            long tiempoEsperando = p.getTiempoEsperando(); // en ticks
//            long duracion = p.getDuracionTotal(); // duración total
//            double rr = ((double) tiempoEsperando + duracion) / duracion;
//
//            if (rr > mejorRR) {
//                mejorRR = rr;
//                mejor = p;
//            }
//        }

        // Quitar el proceso elegido de la cola
        colaListos.remove(mejor);
        return mejor;
    }
    
    @Override
    public SchedulingType getSchedulingType() {
        return this.type;
    }


    @Override
    public boolean hayProcesos() {
        return !colaListos.isEmpty();
    }
    
    @Override
    public void setColaNuevos(Cola cola){
        this.colaNuevos = cola;
    }
    
    @Override
    public void setColaListos(Cola cola){
        this.colaListos = cola;
    }
    
    @Override
    public void setColaBloqueados(Cola cola){
        this.colaBloqueados = cola;
    }
    
    @Override
    public void setColaTerminados(Cola cola){
        this.colaTerminados = cola;
    }
    
    @Override
    public void setColaListoSuspendido(Cola cola){
        this.colaListoSuspendido = cola;
    }
    
    @Override
    public void setColaBloqueadoSuspendido(Cola cola){
        this.colaBloqueadoSuspendido = cola;
    }
}