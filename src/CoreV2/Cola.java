/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

import java.util.function.Consumer;

/**
 *
 * @author verol
 */
public class Cola {
    private Nodo frente;
    private Nodo fin;
    public int size;

    public Cola() {
        frente = null;
        fin = null;
        size = 0;
    }
    
    public int size() {
        return size;
    }
    
    public void add(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (fin != null) {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        if (frente == null) {
            frente = nuevo;
        }
        size++;
    }
    
    public void add(PCB pcb) {
        Nodo nuevo = new Nodo(pcb);
        if (fin != null) {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        if (frente == null) {
            frente = nuevo;
        }
        size++;
    }
    
    public void add(Petition peticion) {
        Nodo nuevo = new Nodo(peticion);
        if (fin != null) {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        if (frente == null) {
            frente = nuevo;
        }
        size++;
    }

    

    public Proceso poll() {
        if (frente == null) return null;
        Proceso p = frente.getProceso();
        frente = frente.getSiguiente();
        if (frente == null) fin = null;
        size--;
        return p;
    }

    public boolean remove(Proceso p) {
        if (frente == null) return false;

        if (frente.getProceso() == p) {
            poll();
//            size--;
            return true;
        }

        Nodo actual = frente;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getProceso() == p) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                if (actual.getSiguiente() == null) fin = actual;
                size--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false; // no se encontró
    }
    
    
    public boolean remove(PCB pcb) {
        if (frente == null) return false;

        if (frente.getProceso().getPcb() == pcb) {
            poll();
            size--;
            return true;
        }

        Nodo actual = frente;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getProceso().getPcb() == pcb) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                if (actual.getSiguiente() == null) fin = actual;
                size--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false; 
    }

    public Proceso peek() {
        return (frente != null) ? frente.getProceso() : null;
    }

    public boolean isEmpty() {
        return frente == null;
    }
    
    public void forEach(Consumer<Proceso> callback) {
        Nodo actual = frente;
        while (actual != null) {
            callback.accept(actual.getProceso());
            actual = actual.getSiguiente();
        }
    }

    public Nodo getFrente() {
        return frente;
    }
    
    public Petition pollPeticion() {
        if (frente == null) return null;
        Petition p = frente.getPeticion();
        frente = frente.getSiguiente();
        if (frente == null) fin = null;
        size--;
        return p;
    }
    
    public boolean remove(Petition peticion) {
        if (frente == null) return false;

        if (frente.getPeticion() == peticion) {
            pollPeticion();
            return true;
        }

        Nodo actual = frente;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getPeticion() == peticion) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                if (actual.getSiguiente() == null) fin = actual;
                size--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false; // no se encontró
    }
    
}

