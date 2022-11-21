package com.example.meganv2;

public class Respuestas {
    private String cuestion;
    private String respuesta;;


    public Respuestas(String cuestion, String respuesta) {
        this.cuestion = cuestion;
        this.respuesta = respuesta;
    }


    public String getCuestion() {
        return cuestion;
    }

    public String getRespuesta() {
        return respuesta;
    }


}
