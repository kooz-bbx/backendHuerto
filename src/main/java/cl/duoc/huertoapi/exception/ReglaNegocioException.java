package cl.duoc.huertoapi.exception;

// Se lanza cuando no se cumple una regla del huerto
// (código repetido, cosecha antes de la siembra, sector con cultivos...) → responde 409 Conflict
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
