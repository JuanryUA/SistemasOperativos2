/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import CoreV2.Proceso.Estado;


/**
 *
 * @author verol
 */
public class PCB {
    
    private final int id;
    private final String nombre;
    private Estado estado;
    private int pc;
    private int mar;
            
    
    public PCB(int id, String nombre, Estado estado, int pc, int mar) { 
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.pc = pc; 
        this.mar = mar;
        
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void setMar(int mar) {
        this.mar = mar;
    }

    public Estado getEstado() {
        return estado;
    }

    public int getPc() {
        return pc;
    }

    public int getMar() {
        return mar;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
