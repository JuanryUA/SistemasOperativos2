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
public class Lista<T> {
    private class Nodo {
        T dato;
        Nodo siguiente;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo cabeza;
    private int size;

    public Lista() {
        cabeza = null;
        size = 0;
    }

    public void add(T elemento) {
        Nodo nuevo = new Nodo(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
        size++;
    }

    public boolean remove(T elemento) {
        if (cabeza == null) return false;

        if (cabeza.dato.equals(elemento)) {
            cabeza = cabeza.siguiente;
            size--;
            return true;
        }

        Nodo actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.equals(elemento)) {
                actual.siguiente = actual.siguiente.siguiente;
                size--;
                return true;
            }
            actual = actual.siguiente;
        }

        return false;
    }

    public T get(int index) {
        if (index < 0 || index >= size) throw new RuntimeException("IndexOutOfBounds");
        Nodo actual = cabeza;
        for (int i = 0; i < index; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        cabeza = null;
        size = 0;
    }

    public void forEach(Accion<T> accion) {
        Nodo actual = cabeza;
        while (actual != null) {
            accion.ejecutar(actual.dato);
            actual = actual.siguiente;
        }
    }

    public interface Accion<T> {
        void ejecutar(T elemento);
    }
    
    public void set(int index, T elemento) {
        if (index < 0 || index >= size) throw new RuntimeException("IndexOutOfBounds");
        Nodo actual = cabeza;
        for (int i = 0; i < index; i++) {
            actual = actual.siguiente;
        }
        actual.dato = elemento;
    }
    
    public void addAll(Lista<T> otraLista) {
        if (otraLista == null || otraLista.isEmpty()) return;

        Nodo actualOtra = otraLista.cabeza;
        while (actualOtra != null) {
            this.add(actualOtra.dato);
            actualOtra = actualOtra.siguiente;
        }
    }
}

