/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2.DiskStrategies;
import CoreV2.Cola;
import CoreV2.Petition;

/**
 *
 * @author verol
 */
public class FIFODisk implements ISchedullingDiskAlgorithm  {
    public Cola colaPeticiones = new Cola();;
    private SchedulingDiskType type = SchedulingDiskType.FIFO;
    
    @Override
    public SchedulingDiskType getSchedulingDiskType() {
        return this.type;
    }
    
    @Override
    public boolean hayPeticiones() {
        return !colaPeticiones.isEmpty();
    }
    
    @Override
    public void setColaPeticiones(Cola cola){
        this.colaPeticiones = cola;
    }
    
    @Override
    public Petition obtenerSiguientePeticion() {
        return colaPeticiones.pollPeticion(); // FIFO: primero en entrar, primero en salir
    }
}
