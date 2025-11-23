/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;
import CoreV2.ALgorithmsStrategies.ISchedulingAlgorithm;
//

/**
 *
 * @author verol
 */


public class Scheduler {
    private ISchedulingAlgorithm algoritmo;
    private OperatingSystem so;

    public Scheduler(ISchedulingAlgorithm algoritmoInicial) {  //constructor
        this.algoritmo = algoritmoInicial;
    }

    //public void setAlgoritmo(ISchedulingAlgorithm nuevoAlgoritmo) {  //para pasar el algoritmo sig
    //    this.algoritmo = nuevoAlgoritmo;
    //}
    

//    public void agregarProcesos(Proceso p) {
//        algoritmo.agregarProcesoAListos(p);
//    }

    public Proceso obtenerSiguienteProceso() {
        return algoritmo.obtenerSiguienteProceso();
    }

    public boolean hayProcesos() {
        return algoritmo.hayProcesos();
    }
    
    public boolean algoritmoTieneQuantum(){
        return algoritmo.hasQuantum();
    }
    
    public void setSO(OperatingSystem so){  //para pasarle al scheduler el SO, necesitas cosas del SO en el scheduler
        this.so = so;
        // Configura el algoritmo INICIAL con las colas
        this.algoritmo.setColaNuevos(so.getColaNuevos());
        this.algoritmo.setColaListos(so.getColaListos());
        this.algoritmo.setColaBloqueados(so.getColaBloqueados()); // Añadido Bloqueados
        this.algoritmo.setColaTerminados(so.getColaTerminados()); // Añadido Terminados
//        this.algoritmo.setColaListoSuspendido(so.getColaListoSuspendido()); // Añadido ListoSuspendido
//        this.algoritmo.setColaBloqueadoSuspendido(so.getColaBloqueadoSuspendido()); // Añadido BloqueadoSuspendido
    }
    
    public void setAlgoritmo(ISchedulingAlgorithm nuevoAlgoritmo) {
        this.algoritmo = nuevoAlgoritmo; // <-- Esto ya lo tenías

        if (this.so != null) {
            this.algoritmo.setColaNuevos(so.getColaNuevos());
            this.algoritmo.setColaListos(so.getColaListos());
            this.algoritmo.setColaBloqueados(so.getColaBloqueados());
            this.algoritmo.setColaTerminados(so.getColaTerminados());
//                this.algoritmo.setColaListoSuspendido(so.getColaListoSuspendido());
//                this.algoritmo.setColaBloqueadoSuspendido(so.getColaBloqueadoSuspendido());
        }
        }
    
    public ISchedulingAlgorithm getAlgoritmo() {
        return algoritmo;
    }
   
}

