/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2.DiskStrategies;
import CoreV2.Cola;
import CoreV2.Petition;
import CoreV2.Nodo;

/**
 *
 * @author verol
 */
public class SSTFDisk implements ISchedullingDiskAlgorithm {
    public Cola colaPeticiones = new Cola();
    private SchedulingDiskType type = SchedulingDiskType.SSTF;
    private int currentHeadPosition = 0;
    
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
    public void setCurrentHeadPosition(int position) {
        this.currentHeadPosition = position;
    }
    
    @Override
    public Petition obtenerSiguientePeticion() {
        if (colaPeticiones.isEmpty()) {
            return null;
        }
        
        // Find the request with the shortest seek time
        Nodo actual = colaPeticiones.getFrente();
        Petition closestPeticion = null;
        int minDistance = Integer.MAX_VALUE;
        
        // First pass: find the closest request
        while (actual != null) {
            Petition peticion = actual.getPeticion();
            if (peticion != null) {
                int distance = Math.abs(peticion.getTrack() - currentHeadPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPeticion = peticion;
                }
                // If we find a request at the current position, prioritize it
                if (distance == 0) {
                    closestPeticion = peticion;
                    break;
                }
            }
            actual = actual.getSiguiente();
        }
        
        // Remove the closest petition from the queue
        if (closestPeticion != null) {
            colaPeticiones.remove(closestPeticion);
            return closestPeticion;
        }
        
        return null;
    }
}

