/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2.DiskStrategies;
import CoreV2.Cola;
import CoreV2.Petition;
import CoreV2.Nodo;
import CoreV2.Lista; // Tu Lista
import java.util.Comparator;

/**
 *
 * @author verol
 */
public class SCANDisk implements ISchedullingDiskAlgorithm {
    public Cola colaPeticiones = new Cola();
    private SchedulingDiskType type = SchedulingDiskType.SCAN;
    private int currentHeadPosition = 0;
    private boolean movingRight = true; // Direction of head movement
    
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
        
        // Collect all petitions
        Lista<Petition> petitions = new Lista<>();
        Nodo actual = colaPeticiones.getFrente();
        while (actual != null) {
            Petition p = actual.getPeticion();
            if (p != null) {
                petitions.add(p);
            }
            actual = actual.getSiguiente();
        }
        
        if (petitions.isEmpty()) {
            return null;
        }
        
        // Separate petitions into those ahead and behind
        Lista<Petition> ahead = new Lista<>();
        Lista<Petition> behind = new Lista<>();
        Petition atCurrent = null; // Request at current head position
        
        for (int i = 0; i < petitions.size(); i++) {
            Petition p = petitions.get(i);
            if (p.getTrack() == currentHeadPosition) {
                atCurrent = p; // Prioritize request at current position
            } else if (p.getTrack() > currentHeadPosition) {
                ahead.add(p);
            } else {
                behind.add(p);
            }
        }
        
        // If there's a request at current position, serve it first
        if (atCurrent != null) {
            colaPeticiones.remove(atCurrent);
            return atCurrent;
        }
        
        // Sort ahead in ascending order, behind in descending order
        ahead.sort(Comparator.comparingInt(Petition::getTrack));
        behind.sort(Comparator.comparingInt(Petition::getTrack).reversed());
        
        Petition next = null;
        
        if (movingRight) {
            // Moving right: serve requests ahead first
            if (!ahead.isEmpty()) {
                next = ahead.get(0);
            } else {
                // No requests ahead, change direction and serve the highest behind
                movingRight = false;
                if (!behind.isEmpty()) {
                    next = behind.get(0);
                }
            }
        } else {
            // Moving left: serve requests behind first
            if (!behind.isEmpty()) {
                next = behind.get(0);
            } else {
                // No requests behind, change direction and serve the lowest ahead
                movingRight = true;
                if (!ahead.isEmpty()) {
                    next = ahead.get(0);
                }
            }
        }
        
        if (next != null) {
            colaPeticiones.remove(next);
            // Update direction based on next request
            if (next.getTrack() > currentHeadPosition) {
                movingRight = true;
            } else if (next.getTrack() < currentHeadPosition) {
                movingRight = false;
            }
        }
        
        return next;
    }
}

