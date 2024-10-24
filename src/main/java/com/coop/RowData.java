package com.coop;

import javafx.beans.property.SimpleStringProperty;

public class RowData {
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty apellido;
    private final SimpleStringProperty dispositivo;
    private final SimpleStringProperty horaEntrada;
    private final SimpleStringProperty horaSalida;
    private final SimpleStringProperty tiempoTranscurrido;

    public RowData(String nombre, String apellido, String dispositivo, String horaEntrada, String horaSalida, String tiempoTranscurrido) {
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.dispositivo = new SimpleStringProperty(dispositivo);
        this.horaEntrada = new SimpleStringProperty(horaEntrada);
        this.horaSalida = new SimpleStringProperty(horaSalida);
        this.tiempoTranscurrido = new SimpleStringProperty(tiempoTranscurrido);
    }

    public String getNombre() {
        return nombre.get();
    }

    public String getApellido() {
        return apellido.get();
    }

    public String getDispositivo() {
        return dispositivo.get();
    }

    public String getHoraEntrada() {
        return horaEntrada.get();
    }

    public String getHoraSalida() {
        return horaSalida.get();
    }

    public String getTiempoTranscurrido() {
        return tiempoTranscurrido.get();
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada.set(horaEntrada);
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida.set(horaSalida);
    }

    public void setTiempoTranscurrido(String tiempoTranscurrido) {
        this.tiempoTranscurrido.set(tiempoTranscurrido);
    }
}

