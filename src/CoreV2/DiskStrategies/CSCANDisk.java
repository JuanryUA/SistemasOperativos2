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
        
        // Sort ahead in ascending order, behind in ascending order (for wrap-around)
        Comparator<Petition> comparadorAscendente = Comparator.comparingInt(Petition::getTrack);
        
        ahead.sort(comparadorAscendente);
        behind.sort(comparadorAscendente);
        
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

