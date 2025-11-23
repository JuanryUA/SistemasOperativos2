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

public interface ISchedullingDiskAlgorithm {
    public enum SchedulingDiskType { FIFO };
    
    Petition obtenerSiguientePeticion();
    SchedulingDiskType getSchedulingDiskType();
    boolean hayPeticiones();
    void setColaPeticiones(Cola cola);
}