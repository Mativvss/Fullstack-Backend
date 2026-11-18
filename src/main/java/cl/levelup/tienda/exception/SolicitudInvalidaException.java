package cl.levelup.tienda.exception;

import java.util.Map;

public class SolicitudInvalidaException extends RuntimeException {

    private final Map<String, String> errores;

    public SolicitudInvalidaException(String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.errores = errores;
    }

    public SolicitudInvalidaException(String mensaje) {
        this(mensaje, null);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
