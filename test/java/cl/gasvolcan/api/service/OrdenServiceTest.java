package cl.gasvolcan.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.gasvolcan.api.dto.*;
import cl.gasvolcan.api.exception.ReglaNegocioException;
import cl.gasvolcan.api.exception.SolicitudInvalidaException;
import cl.gasvolcan.api.model.*;
import cl.gasvolcan.api.repository.*;
import cl.gasvolcan.api.security.SesionActual;
import cl.gasvolcan.api.security.UsuarioActual;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/**
 * Pruebas unitarias de la lógica de negocio de las órdenes (precios, stock, pago simulado,
 * reparto y permisos por rol). Los repositorios son simulados con Mockito: no usan base de datos.
 */
class OrdenServiceTest {

    private OrdenRepository ordenes;
    private ProductoRepository productos;
    private CamionRepository camiones;
    private UsuarioRepository usuarios;
    private SesionActual sesion;
    private OrdenService servicio;
    private Producto gas11;

    private static final UsuarioActual OPERADORA = new UsuarioActual("333333333", "operadora@duoc.cl", "Camila", TipoUsuario.OPERADORA);
    private static final UsuarioActual ENZO = new UsuarioActual("444444444", "repenzo@duoc.cl", "Enzo", TipoUsuario.REPARTIDOR);
    private static final UsuarioActual JAVIERA = new UsuarioActual("555555555", "repjaviera@duoc.cl", "Javiera", TipoUsuario.REPARTIDOR);
    private static final UsuarioActual CLIENTE = new UsuarioActual("190110222", "cliente@gmail.com", "Ana", TipoUsuario.CLIENTE);

    @BeforeEach
    void preparar() {
        ordenes = mock(OrdenRepository.class);
        productos = mock(ProductoRepository.class);
        camiones = mock(CamionRepository.class);
        usuarios = mock(UsuarioRepository.class);
        sesion = mock(SesionActual.class);
        servicio = new OrdenService(ordenes, productos, camiones, usuarios, sesion);

        // guardar devuelve lo mismo que recibe
        when(ordenes.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));
        when(sesion.opcional()).thenReturn(Optional.empty()); // por defecto: visitante (invitado)

        gas11 = producto("CL002", "Cilindro GLP 11 kg", 10000, 10, 10); // precio final: 9.000
        when(productos.findById("CL002")).thenReturn(Optional.of(gas11));
    }

    // ---------------- helpers ----------------

    private static Producto producto(String codigo, String nombre, int precio, int descuento, int stock) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setDescuento(descuento);
        p.setStock(stock);
        p.setCategoriaId("residencial");
        return p;
    }

    private static CompraRequest compra(String metodo, String tarjeta, int cantidad) {
        return new CompraRequest(
                new ClienteDto("Ana", "Pérez", "ana@gmail.com", "+56912345678"),
                new EntregaDto("domicilio", "Calle 1 #123", "", "Región Metropolitana", "Maipú", ""),
                new PagoRequest(metodo, tarjeta),
                List.of(new ItemRequest("CL002", cantidad)));
    }

    private static Orden ordenEn(String numero, EstadoOrden estado) {
        Orden o = new Orden();
        o.setNumero(numero);
        o.setEstado(estado);
        o.setPropietario("invitado");
        o.setRepartidor("Sin asignar");
        return o;
    }

    private static Usuario repartidor(String run, String nombre) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(nombre);
        u.setApellidos("Apellido");
        u.setTipoUsuario(TipoUsuario.REPARTIDOR);
        return u;
    }

    // ---------------- compra ----------------

    @Test
    @DisplayName("compra en efectivo: el servidor calcula el precio con descuento y descuenta el stock")
    void compraEfectivo() {
        OrdenResponse r = servicio.crearCompra(compra("efectivo", null, 2));

        assertEquals(18000, r.total()); // 2 x 9.000 (10% de descuento aplicado en el servidor)
        assertEquals(EstadoOrden.PENDIENTE, r.estado());
        assertFalse(r.pago().pagado()); // el efectivo se cobra al entregar
        assertEquals("invitado", r.propietario());
        assertEquals(8, gas11.getStock());
        assertNull(r.motivoPago());
    }

    @Test
    @DisplayName("compra con sesión iniciada: la orden queda a nombre del usuario")
    void compraConSesion() {
        when(sesion.opcional()).thenReturn(Optional.of(CLIENTE));
        OrdenResponse r = servicio.crearCompra(compra("efectivo", null, 1));
        assertEquals("cliente@gmail.com", r.propietario());
    }

    @Test
    @DisplayName("compra con tarjeta válida: queda pagada")
    void compraTarjetaValida() {
        OrdenResponse r = servicio.crearCompra(compra("tarjeta", "4111 1111 1111 1111", 1));
        assertEquals(EstadoOrden.PENDIENTE, r.estado());
        assertTrue(r.pago().pagado());
        assertEquals(9, gas11.getStock());
    }

    @Test
    @DisplayName("tarjeta terminada en 0000: pago rechazado, estado pago-fallido y el stock no se toca")
    void tarjetaRechazada() {
        OrdenResponse r = servicio.crearCompra(compra("tarjeta", "4111111111110000", 3));
        assertEquals(EstadoOrden.PAGO_FALLIDO, r.estado());
        assertTrue(r.motivoPago().toLowerCase().contains("rechazada"));
        assertEquals(10, gas11.getStock());
    }

    @Test
    @DisplayName("tarjeta con menos de 16 dígitos: pago fallido por número inválido")
    void tarjetaInvalida() {
        OrdenResponse r = servicio.crearCompra(compra("tarjeta", "411111111111111", 1));
        assertEquals(EstadoOrden.PAGO_FALLIDO, r.estado());
        assertTrue(r.motivoPago().toLowerCase().contains("inválido"));
    }

    @Test
    @DisplayName("sin stock suficiente la compra se rechaza")
    void sinStock() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crearCompra(compra("efectivo", null, 11)));
        assertEquals(10, gas11.getStock());
    }

    @Test
    @DisplayName("un producto que no existe es una solicitud inválida")
    void productoInexistente() {
        CompraRequest c = new CompraRequest(
                new ClienteDto("Ana", "Pérez", "ana@gmail.com", ""),
                new EntregaDto("domicilio", "Calle 1", "", "", "", ""),
                new PagoRequest("efectivo", null),
                List.of(new ItemRequest("XX999", 1)));
        assertThrows(SolicitudInvalidaException.class, () -> servicio.crearCompra(c));
    }

    @Test
    @DisplayName("reintentar el pago solo se permite si la orden está en pago-fallido")
    void reintentoSoloSiFallo() {
        when(ordenes.findById("GV-1111")).thenReturn(Optional.of(ordenEn("GV-1111", EstadoOrden.PENDIENTE)));
        ReintentoPagoRequest r = new ReintentoPagoRequest(
                new ClienteDto("Ana", "Pérez", "ana@gmail.com", ""),
                new EntregaDto("domicilio", "Calle 1", "", "", "", ""),
                new PagoRequest("efectivo", null));
        assertThrows(ReglaNegocioException.class, () -> servicio.reintentarPago("GV-1111", r));
    }

    // ---------------- pedidos rápido y telefónico ----------------

    @Test
    @DisplayName("pedido telefónico: normaliza el teléfono a +56 y queda pendiente")
    void pedidoTelefonico() {
        PedidoRapidoRequest p = new PedidoRapidoRequest("Rosa Díaz", "912345678", "Av. Central 123", "Maipú", "CL002", 2, "Casa azul");
        OrdenResponse r = servicio.crearPedidoTelefonico(p);

        assertEquals("+56912345678", r.cliente().telefono());
        assertEquals("telefono", r.origen());
        assertEquals(EstadoOrden.PENDIENTE, r.estado());
        assertEquals(18000, r.total());
        assertEquals(8, gas11.getStock());
    }

    @Test
    @DisplayName("pedido telefónico con teléfono inválido se rechaza")
    void telefonoInvalido() {
        PedidoRapidoRequest p = new PedidoRapidoRequest("Rosa", "98179788988888", "Av. 1", "", "CL002", 1, "");
        assertThrows(SolicitudInvalidaException.class, () -> servicio.crearPedidoTelefonico(p));
        verify(ordenes, never()).save(any());
    }

    // ---------------- reparto ----------------

    @Test
    @DisplayName("la operadora asigna camión y repartidor a un pedido pendiente")
    void asignarOk() {
        when(sesion.requerir()).thenReturn(OPERADORA);
        when(ordenes.findById("GV-2001")).thenReturn(Optional.of(ordenEn("GV-2001", EstadoOrden.PENDIENTE)));
        when(camiones.findById("KLRT-11")).thenReturn(Optional.of(new Camion("KLRT-11", "Hyundai", 60)));
        when(usuarios.findById("444444444")).thenReturn(Optional.of(repartidor("444444444", "Enzo")));
        when(ordenes.findByEstadoIn(any())).thenReturn(List.of());

        OrdenResponse r = servicio.asignar("GV-2001", new AsignacionRequest("KLRT-11", "444444444", " Portón verde "));

        assertEquals(EstadoOrden.ASIGNADO, r.estado());
        assertEquals("KLRT-11", r.camion());
        assertEquals("444444444", r.repartidorRun());
        assertEquals("Camila", r.asignadoPor());
        assertEquals("Portón verde", r.instrucciones());
    }

    @Test
    @DisplayName("un camión que ya va en ruta con otro repartidor no se puede asignar")
    void camionOcupado() {
        when(sesion.requerir()).thenReturn(OPERADORA);
        when(ordenes.findById("GV-2002")).thenReturn(Optional.of(ordenEn("GV-2002", EstadoOrden.PENDIENTE)));
        when(camiones.findById("KLRT-11")).thenReturn(Optional.of(new Camion("KLRT-11", "Hyundai", 60)));
        when(usuarios.findById("555555555")).thenReturn(Optional.of(repartidor("555555555", "Javiera")));
        Orden enRuta = ordenEn("GV-9000", EstadoOrden.EN_CAMINO);
        enRuta.setCamion("KLRT-11");
        enRuta.setRepartidorRun("444444444"); // el camión lo lleva Enzo
        when(ordenes.findByEstadoIn(any())).thenReturn(List.of(enRuta));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.asignar("GV-2002", new AsignacionRequest("KLRT-11", "555555555", "")));
    }

    @Test
    @DisplayName("un repartidor que ya va en ruta en otro camión no puede cambiar de camión")
    void repartidorOcupado() {
        when(sesion.requerir()).thenReturn(OPERADORA);
        when(ordenes.findById("GV-2003")).thenReturn(Optional.of(ordenEn("GV-2003", EstadoOrden.PENDIENTE)));
        when(camiones.findById("PHZX-45")).thenReturn(Optional.of(new Camion("PHZX-45", "Mitsubishi", 80)));
        when(usuarios.findById("444444444")).thenReturn(Optional.of(repartidor("444444444", "Enzo")));
        Orden enRuta = ordenEn("GV-9001", EstadoOrden.ASIGNADO);
        enRuta.setCamion("KLRT-11");
        enRuta.setRepartidorRun("444444444");
        when(ordenes.findByEstadoIn(any())).thenReturn(List.of(enRuta));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.asignar("GV-2003", new AsignacionRequest("PHZX-45", "444444444", "")));
    }

    @Test
    @DisplayName("el mismo repartidor puede llevar varios pedidos en su camión")
    void variosPedidosMismoRepartidor() {
        when(sesion.requerir()).thenReturn(OPERADORA);
        when(ordenes.findById("GV-2004")).thenReturn(Optional.of(ordenEn("GV-2004", EstadoOrden.PENDIENTE)));
        when(camiones.findById("KLRT-11")).thenReturn(Optional.of(new Camion("KLRT-11", "Hyundai", 60)));
        when(usuarios.findById("444444444")).thenReturn(Optional.of(repartidor("444444444", "Enzo")));
        Orden enRuta = ordenEn("GV-9002", EstadoOrden.EN_CAMINO);
        enRuta.setCamion("KLRT-11");
        enRuta.setRepartidorRun("444444444");
        when(ordenes.findByEstadoIn(any())).thenReturn(List.of(enRuta));

        OrdenResponse r = servicio.asignar("GV-2004", new AsignacionRequest("KLRT-11", "444444444", ""));
        assertEquals(EstadoOrden.ASIGNADO, r.estado());
    }

    @Test
    @DisplayName("solo se pueden asignar pedidos pendientes")
    void asignarNoPendiente() {
        when(ordenes.findById("GV-2005")).thenReturn(Optional.of(ordenEn("GV-2005", EstadoOrden.ENTREGADO)));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.asignar("GV-2005", new AsignacionRequest("KLRT-11", "444444444", "")));
    }

    @Test
    @DisplayName("no se puede asignar a alguien que no es repartidor")
    void asignarNoRepartidor() {
        when(ordenes.findById("GV-2006")).thenReturn(Optional.of(ordenEn("GV-2006", EstadoOrden.PENDIENTE)));
        when(camiones.findById("KLRT-11")).thenReturn(Optional.of(new Camion("KLRT-11", "Hyundai", 60)));
        Usuario cliente = new Usuario();
        cliente.setRun("190110222");
        cliente.setTipoUsuario(TipoUsuario.CLIENTE);
        when(usuarios.findById("190110222")).thenReturn(Optional.of(cliente));
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.asignar("GV-2006", new AsignacionRequest("KLRT-11", "190110222", "")));
    }

    // ---------------- cambios de estado y permisos ----------------

    private Orden ordenAsignadaA(String run, EstadoOrden estado) {
        Orden o = ordenEn("GV-3001", estado);
        o.setRepartidorRun(run);
        o.setCamion("KLRT-11");
        when(ordenes.findById("GV-3001")).thenReturn(Optional.of(o));
        return o;
    }

    @Test
    @DisplayName("el repartidor asignado sale a repartir y entrega: al entregar queda pagado y con fecha")
    void repartidorAvanzaPedido() {
        when(sesion.requerir()).thenReturn(ENZO);
        ordenAsignadaA("444444444", EstadoOrden.ASIGNADO);

        assertEquals(EstadoOrden.EN_CAMINO, servicio.cambiarEstado("GV-3001", EstadoOrden.EN_CAMINO).estado());
        OrdenResponse entregada = servicio.cambiarEstado("GV-3001", EstadoOrden.ENTREGADO);
        assertEquals(EstadoOrden.ENTREGADO, entregada.estado());
        assertTrue(entregada.pago().pagado());
        assertNotNull(entregada.fechaEntrega());
    }

    @Test
    @DisplayName("otro repartidor no puede cambiar un pedido que no es suyo")
    void otroRepartidorNoPuede() {
        when(sesion.requerir()).thenReturn(JAVIERA);
        ordenAsignadaA("444444444", EstadoOrden.ASIGNADO);
        assertThrows(AccessDeniedException.class, () -> servicio.cambiarEstado("GV-3001", EstadoOrden.EN_CAMINO));
    }

    @Test
    @DisplayName("no se puede saltar pasos: un pedido pendiente no pasa directo a entregado")
    void saltoInvalido() {
        when(sesion.requerir()).thenReturn(ENZO);
        when(ordenes.findById("GV-3002")).thenReturn(Optional.of(ordenEn("GV-3002", EstadoOrden.PENDIENTE)));
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado("GV-3002", EstadoOrden.ENTREGADO));
    }

    @Test
    @DisplayName("la operadora puede cancelar un pedido; el repartidor no")
    void cancelarSoloOperadora() {
        ordenAsignadaA("444444444", EstadoOrden.ASIGNADO);

        when(sesion.requerir()).thenReturn(ENZO);
        assertThrows(AccessDeniedException.class, () -> servicio.cambiarEstado("GV-3001", EstadoOrden.CANCELADO));

        when(sesion.requerir()).thenReturn(OPERADORA);
        assertEquals(EstadoOrden.CANCELADO, servicio.cambiarEstado("GV-3001", EstadoOrden.CANCELADO).estado());
    }

    // ---------------- consulta de una orden ----------------

    @Test
    @DisplayName("un cliente no puede ver la orden de otra persona; la operadora sí")
    void accesoAUnaOrden() {
        Orden o = ordenEn("GV-4001", EstadoOrden.PENDIENTE);
        o.setPropietario("otro@gmail.com");
        when(ordenes.findById("GV-4001")).thenReturn(Optional.of(o));

        when(sesion.requerir()).thenReturn(CLIENTE);
        assertThrows(AccessDeniedException.class, () -> servicio.obtener("GV-4001"));

        when(sesion.requerir()).thenReturn(OPERADORA);
        assertEquals("GV-4001", servicio.obtener("GV-4001").numero());
    }
}
