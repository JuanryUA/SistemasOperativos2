/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2.DiskStrategies;
import CoreV2.Cola;
import CoreV2.Petition;
import CoreV2.Nodo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author verol
 */
public class CSCANDisk implements ISchedullingDiskAlgorithm {
    public Cola colaPeticiones = new Cola();
    private SchedulingDiskType type = SchedulingDiskType.C_SCAN;
    private int currentHeadPosition = 0;
    private static final int MAX_TRACK = 199; // Maximum track number
    
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
        ArrayList<Petition> petitions = new ArrayList<>();
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
        ArrayList<Petition> ahead = new ArrayList<>();
        ArrayList<Petition> behind = new ArrayList<>();
        Petition atCurrent = null; // Request at current head position
        
        for (Petition p : petitions) {
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
        
        // Sort ahead in ascending order, behind in ascending order (for wrap-around)
        Collections.sort(ahead, Comparator.comparingInt(Petition::getTrack));
        Collections.sort(behind, Comparator.comparingInt(Petition::getTrack));
        
        Petition next = null;
        
        // C-SCAN: always move in one direction (right/up)
        // Serve requests ahead first, then wrap to the beginning
        if (!ahead.isEmpty()) {
            next = ahead.get(0);
        } else if (!behind.isEmpty()) {
            // Wrap around: go to the beginning (lowest track)
            next = behind.get(0);
        }
        
        if (next != null) {
            colaPeticiones.remove(next);
        }
        
        return next;
    }
}

