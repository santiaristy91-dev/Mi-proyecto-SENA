package com.powergo.model;

/**
 * Representa la entidad Estación dispensadora de baterías en PowerGo.
 */
public class Estacion {
    private int idEstacion;
    private String codigoEstacion;
    private String nombreEstacion;
    private String direccion;
    private int capacidadTotal;
    private int cargadoresDisponibles;
    private String estadoOperativo; // 'operativa', 'mantenimiento', 'fuera_servicio'

    public Estacion() {
    }

    public Estacion(int idEstacion, String codigoEstacion, String nombreEstacion, 
                    String direccion, int capacidadTotal, int cargadoresDisponibles, String estadoOperativo) {
        this.idEstacion = idEstacion;
        this.codigoEstacion = codigoEstacion;
        this.nombreEstacion = nombreEstacion;
        this.direccion = direccion;
        this.capacidadTotal = capacidadTotal;
        this.cargadoresDisponibles = cargadoresDisponibles;
        this.estadoOperativo = estadoOperativo;
    }

    public Estacion(String codigoEstacion, String nombreEstacion, 
                    String direccion, int capacidadTotal, int cargadoresDisponibles, String estadoOperativo) {
        this.codigoEstacion = codigoEstacion;
        this.nombreEstacion = nombreEstacion;
        this.direccion = direccion;
        this.capacidadTotal = capacidadTotal;
        this.cargadoresDisponibles = cargadoresDisponibles;
        this.estadoOperativo = estadoOperativo;
    }

    public int getIdEstacion() {
        return idEstacion;
    }

    public void setIdEstacion(int idEstacion) {
        this.idEstacion = idEstacion;
    }

    public String getCodigoEstacion() {
        return codigoEstacion;
    }

    public void setCodigoEstacion(String codigoEstacion) {
        this.codigoEstacion = codigoEstacion;
    }

    public String getNombreEstacion() {
        return nombreEstacion;
    }

    public void setNombreEstacion(String nombreEstacion) {
        this.nombreEstacion = nombreEstacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    public void setCapacidadTotal(int capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }

    public int getCargadoresDisponibles() {
        return cargadoresDisponibles;
    }

    public void setCargadoresDisponibles(int cargadoresDisponibles) {
        this.cargadoresDisponibles = cargadoresDisponibles;
    }

    public String getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(String estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }

    @Override
    public String toString() {
        return "Estacion [ID=" + idEstacion + ", Codigo=" + codigoEstacion + ", Nombre=" + nombreEstacion + 
               ", Direccion=" + direccion + ", Disponibles=" + cargadoresDisponibles + "/" + capacidadTotal + "]";
    }
}
