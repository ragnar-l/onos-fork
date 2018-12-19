package org.onosproject.drivers.fujitsu;

public class AlturaMxpPuertos
{
    // Atributos de la clase AlturaMxpPuertos
    private int puerto;
    private int vecino;
    private int puerto_vecino;

    // Métodos de la clase AlturaMxpPuertos

    public int getPuerto() {
        return this.puerto;
    }

    public int getVecino() {
        return this.vecino;
    }

    public int getPuertoVecino() {
        return this.puerto_vecino;
    }

    public void setPuerto(int n) {
        this.puerto = n;
    }

    public void setVecino(int n) {
        this.vecino = n;
    }

    public void setPuertoVecino(int n) {
        this.puerto_vecino = n;
    }

}
