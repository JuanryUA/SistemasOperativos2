/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author verol
 */
public class DMA {
    private boolean running = false;
    private long unidadTiempoMs; 

    public DMA(long unidadTiempoMs) {
        this.unidadTiempoMs = unidadTiempoMs;
    }

    public void ejecutarES(Proceso p, FileSystem filesystem, Runnable callback) {
        new Thread(() -> {
            try {
                System.out.println("[DMA] Entro al DMA");
                System.out.println("[DMA] Transportando datos del proceso " + p.getNombre() + " para crear peticion...");
                FileData fileData = p.getFileData();
                System.out.println("        DMA: Hilo de " + p.getNombre() + " intentando acceder a FileSystem...");
                filesystem.agregarPeticion(fileData);
                
                
                running = true;
                
                while(fileData.isIsProcessed() != true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DMA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                };
                
                
                System.out.println("DMA: Operación E/S completada → genera interrupción al SO (" + p.getNombre() + ")");
                
                if (callback != null) {
                    System.out.println("SALIO DEL DMA");
                    callback.run(); 
                }

            } finally {
                running = false;
            }
        }).start();
    }

    public boolean estaOcupado() {
        return running;
    }

    public void setUnidadTiempoMs(long unidadTiempoMs) {
        this.unidadTiempoMs = unidadTiempoMs;
    }
    
    
}