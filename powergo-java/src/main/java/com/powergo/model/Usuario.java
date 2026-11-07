package com.powergo.model;

import java.sql.Timestamp;

/**
 * Representa la entidad Usuario dentro del sistema PowerGo.
 * Cumple con el estándar de JavaBeans (atributos privados, getters/setters y constructor).
 */
public class Usuario {
    private int idUsuario;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String passwordHash;
    private String rol; // 'cliente', 'administrador', 'tecnico'
    private double saldoCuenta;
    private String estado; // 'activo', 'inactivo', 'suspendido'
    private Timestamp fechaRegistro;
    private Timestamp fechaActualizacion;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor completo
    public Usuario(int idUsuario, String nombreCompleto, String email, String telefono, 
                   String passwordHash, String rol, double saldoCuenta, String estado) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.saldoCuenta = saldoCuenta;
        this.estado = estado;
    }

    // Constructor para inserción (sin ID ni timestamps autogenerados)
    public Usuario(String nombreCompleto, String email, String telefono, 
                   String passwordHash, String rol, double saldoCuenta, String estado) {
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.saldoCuenta = saldoCuenta;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public double getSaldoCuenta() {
        return saldoCuenta;
    }

    public void setSaldoCuenta(double saldoCuenta) {
        this.saldoCuenta = saldoCuenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Timestamp getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Timestamp fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "Usuario [ID=" + idUsuario + ", Nombre=" + nombreCompleto + ", Email=" + email + 
               ", Rol=" + rol + ", Saldo=$" + saldoCuenta + ", Estado=" + estado + "]";
    }
}
