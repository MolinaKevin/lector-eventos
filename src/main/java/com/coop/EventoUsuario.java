package com.coop;

public class EventoUsuario {
    private String usuario;
    private String horaEntrada;
    private String horaSalida;
    private String dispositivo;

    public EventoUsuario(String usuario, String horaEntrada, String horaSalida, String dispositivo) {
        this.usuario = usuario;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.dispositivo = dispositivo;
    }

    // Getters y Setters
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    @Override
    public String toString() {
        return "Usuario: " + usuario + ", Entrada: " + horaEntrada + ", Salida: " + horaSalida + ", Dispositivo: " + dispositivo;
    }
}

