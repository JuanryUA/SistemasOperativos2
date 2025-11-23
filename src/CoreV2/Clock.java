/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

/**
 *
 * @author verol
 */
public class Clock {
    private volatile long ticGlobal = 0;
    private volatile long ticTimeMs;
    private boolean running = false;
    private Thread thread;
    public OperatingSystem so;
    public CPU cpu;

    public Clock(long ticTimeMs) {
        this.ticTimeMs = ticTimeMs;

    }
    
    public void setTicTimeMs(long nuevoTiempoMs) {
        if (nuevoTiempoMs > 0) {
            this.ticTimeMs = nuevoTiempoMs;
            System.out.println("Clock: Duración del tick cambiada a " + nuevoTiempoMs + " ms");
        } else {
            System.out.println("Clock: Intento de poner duración inválida: " + nuevoTiempoMs);
        }
    }

    public long getTicTimeMs() {
        return ticTimeMs;
    }

    public long getTic() {
        return ticGlobal;
    }

    public void notifySO() {
        this.so.notifyTic();
    }

    public void startClock() {
        running = true;
        thread = new Thread(() -> {
            while (running) {
                try {
                    long currentTicTime = this.ticTimeMs; 
                    if (currentTicTime > 0) { // Evita sleep(0)
                       Thread.sleep(currentTicTime); 
                    } else {
                        Thread.sleep(10); 
                    }

                    ticGlobal++;
                    notifySO();
                    System.out.println("Tic Global: " + ticGlobal);
//                    System.out.println("tictimems: "+ currentTicTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
       thread.start();
    }

    public void stopClock() {
        running = false;
    }
    
    public void setSO(OperatingSystem so){
        this.so = so;
    }
    
    public void setCPU(CPU cpu){
        this.cpu = cpu;
    }
}

