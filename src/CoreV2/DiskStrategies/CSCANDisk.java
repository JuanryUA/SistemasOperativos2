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
    private static final int MAX_TRACK = 199; 
    
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
        
        Lista<Petition> ahead = new Lista<>();
        Lista<Petition> behind = new Lista<>();
        Petition atCurrent = null; 
        
        for (int i = 0; i < petitions.size(); i++) {
            Petition p = petitions.get(i);
            if (p.getTrack() == currentHeadPosition) {
                atCurrent = p; 
            } else if (p.getTrack() > currentHeadPosition) {
                ahead.add(p);
            } else {
                behind.add(p);
            }
        }
        
        if (atCurrent != null) {
            colaPeticiones.remove(atCurrent);
            return atCurrent;
        }
        
        Comparator<Petition> comparadorAscendente = Comparator.comparingInt(Petition::getTrack);
        
        ahead.sort(comparadorAscendente);
        behind.sort(comparadorAscendente);
        
        Petition next = null;
        
        if (!ahead.isEmpty()) {
            next = ahead.get(0);
        } else if (!behind.isEmpty()) {
            next = behind.get(0);
        }
        
        if (next != null) {
            colaPeticiones.remove(next);
        }
        
        return next;
    }
}

