package cl.duoc.huertoapi.exception;

// Se lanza cuando se pide un id que no existe → responde 404 Not Found
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
