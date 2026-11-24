package cl.gasvolcan.api.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Flujo de estados del pedido: pendiente -> asignado -> en-camino -> entregado (y cancelado). */
class EstadoOrdenTest {

    @Test
    @DisplayName("desde pendiente solo se puede cancelar (la operadora debe asignar primero)")
    void pendienteSoloCancela() {
        assertEquals(List.of(EstadoOrden.CANCELADO), EstadoOrden.PENDIENTE.siguientes());
    }

    @Test
    @DisplayName("el flujo normal es asignado -> en camino -> entregado")
    void flujoNormal() {
        assertEquals(List.of(EstadoOrden.EN_CAMINO, EstadoOrden.CANCELADO), EstadoOrden.ASIGNADO.siguientes());
        assertEquals(List.of(EstadoOrden.ENTREGADO, EstadoOrden.CANCELADO), EstadoOrden.EN_CAMINO.siguientes());
    }

    @Test
    @DisplayName("los estados finales no tienen siguiente")
    void estadosFinales() {
        assertTrue(EstadoOrden.ENTREGADO.siguientes().isEmpty());
        assertTrue(EstadoOrden.CANCELADO.siguientes().isEmpty());
        assertTrue(EstadoOrden.PAGO_FALLIDO.siguientes().isEmpty());
    }

    @Test
    @DisplayName("solo asignado y en-camino son pedidos activos (ocupan camión y repartidor)")
    void activos() {
        assertTrue(EstadoOrden.ASIGNADO.esActivo());
        assertTrue(EstadoOrden.EN_CAMINO.esActivo());
        assertFalse(EstadoOrden.PENDIENTE.esActivo());
        assertFalse(EstadoOrden.ENTREGADO.esActivo());
    }

    @Test
    @DisplayName("el texto del JSON se convierte al estado (con guion o con nombre del enum)")
    void desdeTexto() {
        assertEquals(EstadoOrden.EN_CAMINO, EstadoOrden.desdeTexto("en-camino"));
        assertEquals(EstadoOrden.EN_CAMINO, EstadoOrden.desdeTexto("EN_CAMINO"));
        assertThrows(IllegalArgumentException.class, () -> EstadoOrden.desdeTexto("volando"));
    }

    @Test
    @DisplayName("los 4 perfiles del caso viajan con su nombre en español")
    void tiposDeUsuario() {
        assertEquals("Operadora", TipoUsuario.OPERADORA.getEtiqueta());
        assertEquals(TipoUsuario.REPARTIDOR, TipoUsuario.desdeTexto("Repartidor"));
        assertEquals(4, TipoUsuario.values().length);
    }
}
