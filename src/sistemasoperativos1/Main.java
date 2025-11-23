/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemasoperativos1;

import CoreV2.CPU;
import CoreV2.Scheduler;
import CoreV2.DMA;
import CoreV2.Disk;
import CoreV2.Proceso;
import CoreV2.MainMemory;
import CoreV2.OperatingSystem;
import CoreV2.*;
import CoreV2.ALgorithmsStrategies.FIFOScheduling;
import CoreV2.ALgorithmsStrategies.HRRNScheduling;
import CoreV2.ALgorithmsStrategies.RRScheduling;
import CoreV2.ALgorithmsStrategies.SPNScheduling;
import CoreV2.ALgorithmsStrategies.SRTScheduling;

import CoreV2.CPU;
import CoreV2.Clock;
import CoreV2.DMA;
import CoreV2.Disk;
import CoreV2.DiskStrategies.FIFODisk;
import CoreV2.MainMemory;
import CoreV2.OperatingSystem;
import CoreV2.Scheduler;
import Interfaces.SimuladorGUIForm;

/**
 *
 * @author verol
 */


public class Main {
     public static void main(String[] args) throws InterruptedException {
        long unidadTiempoMs = 1000; // duración de un tick (0.5s)
        int memoriaTotal = 200;    // tamaño de memoria
//                int memoriaTotal = 8;    // tamaño de memoria
        int bloquesDeDisco = 32; // tamaño para el disco

        MainMemory memory = new MainMemory(memoriaTotal);
        Disk disk = new Disk(bloquesDeDisco);
        
        DMA dma = new DMA(unidadTiempoMs);
        CPU cpu = new CPU(); 
        Scheduler scheduler = new Scheduler(new FIFOScheduling()); 
        Clock clock = new Clock(unidadTiempoMs);
        DiskScheduler diskScheduler = new DiskScheduler(new FIFODisk());
        FileSystem filesystem = new FileSystem(disk, diskScheduler);
        OperatingSystem so = new OperatingSystem(cpu, memory, disk, dma, scheduler, clock, filesystem);
        clock.setSO(so);
        scheduler.setSO(so);
        diskScheduler.setFileSystem(filesystem);

        cpu = new CPU();
        int quantumDefault=0;
        if(scheduler.algoritmoTieneQuantum()){
            quantumDefault = 5;
        }
        so.setCPUQuantum(quantumDefault); 
        
//       SimuladorGUI ventana = new SimuladorGUI(so, cpu, clock);
        
//        so.setGUI(ventana);
//        
//        java.awt.EventQueue.invokeLater(() -> {
//            ventana.setVisible(true);
//        });

        // --- 6. INICIO DE LA INTERFAZ GRÁFICA (GUI) ---
        // Usamos invokeLater para seguridad de hilos en Swing
        java.awt.EventQueue.invokeLater(() -> {
            // Le pasamos las referencias clave para que la GUI pueda pintar y crear procesos
            SimuladorGUIForm interfaz = new SimuladorGUIForm(disk, filesystem, so);
            interfaz.setVisible(true);
        });

        // --- 7. Arrancar la Simulación ---
        System.out.println(">>> INICIANDO SIMULADOR SO <<<");
        clock.startClock();
        
//        // ▼▼▼ AÑADE ESTAS LÍNEAS PARA CREAR PROCESOS ▼▼▼
//        System.out.println("--- Creando P1 ---");
//        so.crearProceso(Proceso.Tipo.IO_BOUND, 0, "archivo 1", 2); // Prioridad 0
////        new Thread(() -> { // <-- ¡Debe estar en un hilo!
////            try {
////                Thread.sleep(1000);
////            } catch (InterruptedException ex) {
////            }finally {
////            }
////        }).start();
//
//        System.out.println("--- Creando P2 ---");
//        so.crearProceso(Proceso.Tipo.IO_BOUND, 0, "archivo 2", 6); 
////        new Thread(() -> { // <-- ¡Debe estar en un hilo!
////            try {
////                Thread.sleep(1000);
////            } catch (InterruptedException ex) {
////            }finally {
////            }
////        }).start();
//        
//        System.out.println("--- Creando P3 ---");
//        so.crearProceso(Proceso.Tipo.IO_BOUND, 0, "archivo 3", 4); 
//        new Thread(() -> { // <-- ¡Debe estar en un hilo!
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//            }finally {
//            }
//        }).start();






//        System.out.println("--- Creando P4 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 4", 1);
//System.out.println("--- Creando P5 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 5", 1);
//System.out.println("--- Creando P6 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 6", 1);
//System.out.println("--- Creando P7 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 7", 1);
//System.out.println("--- Creando P8 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 8", 1);
//System.out.println("--- Creando P9 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 9", 1);
//System.out.println("--- Creando P10 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 10", 1);
//System.out.println("--- Creando P11 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 11", 1);
//System.out.println("--- Creando P12 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 12", 1);
//System.out.println("--- Creando P13 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 13", 1);
//System.out.println("--- Creando P14 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 14", 1);
//System.out.println("--- Creando P15 ---");
//so.crearProceso(Proceso.Tipo.IO_BOUND, 4, "archivo 15", 1);
       

//        so.crearProceso(1, Proceso.Tipo.CPU_BOUND, 20, 20, 2L, 8);      // tamaño 20, 2 ticks de E/S
//        so.crearProceso(2, Proceso.Tipo.IO_BOUND, 5, 5, 10, 1, 3, 10);    // tamaño 30, 5 ticks de E/S
//        so.crearProceso(3,Proceso.Tipo.NORMAL,10, 10, 5L, 7);   // tamaño 10, sin E/S
////        Thread.sleep(10*unidadTiempoMs);
//        so.crearProceso(4,Proceso.Tipo.NORMAL,8, 8, 5L, 7);   // tamaño 10, sin E/S


//        so.asignarProcesoACPU();

//        for (int i = 0; i < 20; i++) {
//            Thread.sleep(unidadTiempoMs);
//            // Revisar si CPU está libre y asignar siguiente
//            so.asignarProcesoACPU();
//
//            // Bloquear aleatoriamente procesos por E/S
//            if (i == 3) {
//                so.bloquearProcesoES(p2); // p2 solicita E/S
//            }
//        }

//        clock.stopClock();
//
//        System.out.println("Simulación completada");
    }
}

