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
public class SPNScheduling implements ISchedulingAlgorithm {
    private Cola colaNuevos = new Cola();
    private Cola colaListos = new Cola();
    private Cola colaBloqueados = new Cola();
    private Cola colaTerminados = new Cola();
    private Cola colaListoSuspendido = new Cola();
    private Cola colaBloqueadoSuspendido = new Cola();  
    private SchedulingType type = SchedulingType.SPN;
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
        if (colaListos.isEmpty()) {
            return null;
        }

        // Buscar el proceso con menor duración estimada (burst)
        Proceso masCorto = null;
        Nodo actual = colaListos.getFrente(); // getter que devuelva el frente
        while (actual != null) {
            Proceso p = actual.getProceso();
            if (masCorto == null || p.getDuracionTotal() < masCorto.getDuracionTotal()) {
                masCorto = p;
            }
            actual = actual.getSiguiente();
        }
//        Proceso masCorto = null;
//        for (Proceso p : colaListos) {
//            if (masCorto == null || p.getDuracionTotal() < masCorto.getDuracionTotal()) {
//                masCorto = p;
//            }
//        }

        // Quitar el proceso seleccionado de la cola
        colaListos.remove(masCorto);
        return masCorto;
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
