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
    
    public DiskScheduler(ISchedullingDiskAlgorithm algoritmoInicial) {
        this.algoritmoDisk = algoritmoInicial;
    }
    
    public Petition obtenerSiguientePeticion() {
        return algoritmoDisk.obtenerSiguientePeticion();
        
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
    }
    
}
    
