package cl.levelup.tienda.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Rol {
    ADMINISTRADOR("administrador"),
    VENDEDOR("vendedor"),
    CLIENTE("cliente");

    private final String valor;

    Rol(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static Rol desde(String texto) {
        for (Rol rol : values()) {
            if (rol.valor.equalsIgnoreCase(texto)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol no válido: " + texto);
    }
}
