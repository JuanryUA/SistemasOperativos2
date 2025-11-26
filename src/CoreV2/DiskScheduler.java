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
    private int currentHeadPosition;
    
    public DiskScheduler(ISchedullingDiskAlgorithm algoritmoInicial) {
        this.algoritmoDisk = algoritmoInicial;
        this.currentHeadPosition = 0; 
    }
    
    public Petition obtenerSiguientePeticion() {
        algoritmoDisk.setCurrentHeadPosition(this.currentHeadPosition);
        Petition siguiente = algoritmoDisk.obtenerSiguientePeticion();
        if (siguiente != null) {
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
        this.fileSystem=fileSystem;
        this.algoritmoDisk.setColaPeticiones(fileSystem.getColaPeticiones());
    }
    
    public void setAlgoritmoDisk(ISchedullingDiskAlgorithm nuevoDiskAlgorithm){
        this.algoritmoDisk=nuevoDiskAlgorithm;
        if (this.fileSystem != null) {
            this.algoritmoDisk.setColaPeticiones(fileSystem.getColaPeticiones());
        }
        this.algoritmoDisk.setCurrentHeadPosition(this.currentHeadPosition);
    }
    
    public int getCurrentHeadPosition() {
        return currentHeadPosition;
    }
    
    public void setCurrentHeadPosition(int position) {
        this.currentHeadPosition = position;
    }
    
}
    
