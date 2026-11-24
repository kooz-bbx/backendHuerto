package cl.gasvolcan.api.repository;

import static org.junit.jupiter.api.Assertions.*;

import cl.gasvolcan.api.model.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Prueba de integración con una base H2 en memoria: guarda órdenes reales con sus ítems
 * y comprueba las consultas que usa el backend (no necesita MySQL).
 */
@DataJpaTest
class OrdenPersistenciaTest {

    @Autowired
    private OrdenRepository ordenes;
    @Autowired
    private ProductoRepository productos;

    private Producto guardarProducto() {
        Producto p = new Producto();
        p.setCodigo("CL002");
        p.setCategoriaId("residencial");
        p.setNombre("Cilindro GLP 11 kg");
        p.setPrecio(12000);
        p.setStock(50);
        return productos.save(p);
    }

    private Orden orden(String numero, String propietario, EstadoOrden estado, LocalDateTime fecha, Producto p) {
        Orden o = new Orden();
        o.setNumero(numero);
        o.setFecha(fecha);
        o.setOrigen("checkout");
        o.setPropietario(propietario);
        o.setClienteNombre("Ana");
        o.setEntregaTipo("domicilio");
        o.setEntregaCalle("Calle 1");
        o.setPagoMetodo("efectivo");
        o.setPagado(false);
        o.setEstado(estado);
        o.setRepartidor("Sin asignar");
        o.agregarItem(p, 2);
        o.setTotal(p.precioFinal() * 2);
        return ordenes.save(o);
    }

    @Test
    @DisplayName("una orden se guarda junto con sus ítems y se puede leer de nuevo")
    void guardaOrdenConItems() {
        Producto p = guardarProducto();
        orden("GV-1001", "ana@gmail.com", EstadoOrden.PENDIENTE, LocalDateTime.now(), p);

        Orden leida = ordenes.findById("GV-1001").orElseThrow();
        assertEquals(1, leida.getItems().size());
        assertEquals("CL002", leida.getItems().get(0).getCodigo());
        assertEquals(24000, leida.getTotal());
    }

    @Test
    @DisplayName("el seguimiento del cliente trae solo sus órdenes, la más reciente primero")
    void ordenesDelPropietario() {
        Producto p = guardarProducto();
        LocalDateTime hoy = LocalDateTime.now();
        orden("GV-1001", "ana@gmail.com", EstadoOrden.PENDIENTE, hoy.minusDays(2), p);
        orden("GV-1002", "ana@gmail.com", EstadoOrden.PENDIENTE, hoy, p);
        orden("GV-1003", "otro@gmail.com", EstadoOrden.PENDIENTE, hoy, p);

        List<Orden> mias = ordenes.findByPropietarioOrderByFechaDesc("ana@gmail.com");
        assertEquals(List.of("GV-1002", "GV-1001"), mias.stream().map(Orden::getNumero).toList());
    }

    @Test
    @DisplayName("el repartidor ve solo los pedidos que le asignaron")
    void ordenesDelRepartidor() {
        Producto p = guardarProducto();
        Orden o = orden("GV-2001", "ana@gmail.com", EstadoOrden.ASIGNADO, LocalDateTime.now(), p);
        o.setRepartidorRun("444444444");
        ordenes.save(o);
        orden("GV-2002", "ana@gmail.com", EstadoOrden.PENDIENTE, LocalDateTime.now(), p);

        List<Orden> suyas = ordenes.findByRepartidorRunOrderByFechaDesc("444444444");
        assertEquals(1, suyas.size());
        assertEquals("GV-2001", suyas.get(0).getNumero());
    }

    @Test
    @DisplayName("la búsqueda de pedidos en ruta (para la regla camión/repartidor) filtra por estado")
    void ordenesActivas() {
        Producto p = guardarProducto();
        orden("GV-3001", "ana@gmail.com", EstadoOrden.ASIGNADO, LocalDateTime.now(), p);
        orden("GV-3002", "ana@gmail.com", EstadoOrden.EN_CAMINO, LocalDateTime.now(), p);
        orden("GV-3003", "ana@gmail.com", EstadoOrden.ENTREGADO, LocalDateTime.now(), p);

        List<Orden> activas = ordenes.findByEstadoIn(List.of(EstadoOrden.ASIGNADO, EstadoOrden.EN_CAMINO));
        assertEquals(2, activas.size());
    }

    @Test
    @DisplayName("se detecta si una categoría tiene productos (no se puede eliminar)")
    void categoriaConProductos() {
        guardarProducto();
        assertTrue(productos.existsByCategoriaId("residencial"));
        assertFalse(productos.existsByCategoriaId("comercial"));
    }
}
