/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import CoreV2.DiskStrategies.ISchedullingDiskAlgorithm;

/**
 *
 * @author verol
 */
public class DiskScheduler {
    private ISchedullingDiskAlgorithm algoritmoDisk;
    private FileSystem fileSystem;
    private int currentHeadPosition; // Current disk head position (track/cylinder)
    
    public DiskScheduler(ISchedullingDiskAlgorithm algoritmoInicial) {
        this.algoritmoDisk = algoritmoInicial;
        this.currentHeadPosition = 0; // Start at track 0
    }
    
    public Petition obtenerSiguientePeticion() {
        // Update algorithm with current head position before getting next request
        algoritmoDisk.setCurrentHeadPosition(this.currentHeadPosition);
        Petition siguiente = algoritmoDisk.obtenerSiguientePeticion();
        if (siguiente != null) {
            // Update head position to the track of the processed request
            this.currentHeadPosition = siguiente.getTrack();
        }
        return siguiente;
    }
    
    public void getNextPeticion(){
        if (this.hayPeticiones()) {
            Petition siguiente = this.obtenerSiguientePeticion();
            if (siguiente != null) {
                this.fileSystem.createFile(siguiente);
            }
        }
    }

    public boolean hayPeticiones() {
        return algoritmoDisk.hayPeticiones();
    }
    
    public ISchedullingDiskAlgorithm getAlgoritmo() {
        return algoritmoDisk;
    }
    
    
    public void setFileSystem(FileSystem fileSystem){
        // Configura el algoritmo INICIAL con la cola
        this.fileSystem=fileSystem;
        this.algoritmoDisk.setColaPeticiones(fileSystem.getColaPeticiones());
    }
    
    public void setAlgoritmoDisk(ISchedullingDiskAlgorithm nuevoDiskAlgorithm){
        this.algoritmoDisk=nuevoDiskAlgorithm;
        if (this.fileSystem != null) {
            this.algoritmoDisk.setColaPeticiones(fileSystem.getColaPeticiones());
        }
        // Update the algorithm with current head position
        this.algoritmoDisk.setCurrentHeadPosition(this.currentHeadPosition);
    }
    
    public int getCurrentHeadPosition() {
        return currentHeadPosition;
    }
    
    public void setCurrentHeadPosition(int position) {
        this.currentHeadPosition = position;
    }
    
}
    
