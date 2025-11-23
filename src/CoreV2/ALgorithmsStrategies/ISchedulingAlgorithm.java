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

public interface ISchedulingAlgorithm {
    public enum SchedulingType { FCFS, ROUNDROBIN, SPN, SRT, HRRN, FEEDBACK};
    
    void agregarProcesoAListos(Proceso p);
    Proceso obtenerSiguienteProceso();
    SchedulingType getSchedulingType();
    boolean hayProcesos();
    void setColaNuevos(Cola cola);
    void setColaListos(Cola cola);
    void setColaBloqueados(Cola cola);
    void setColaTerminados(Cola cola);
    void setColaListoSuspendido(Cola cola);
    void setColaBloqueadoSuspendido(Cola cola);
    boolean hasQuantum();
}