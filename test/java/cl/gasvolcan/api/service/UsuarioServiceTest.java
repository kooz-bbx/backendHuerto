package cl.gasvolcan.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.gasvolcan.api.dto.UsuarioRequest;
import cl.gasvolcan.api.dto.UsuarioResponse;
import cl.gasvolcan.api.exception.ReglaNegocioException;
import cl.gasvolcan.api.exception.SolicitudInvalidaException;
import cl.gasvolcan.api.model.TipoUsuario;
import cl.gasvolcan.api.model.Usuario;
import cl.gasvolcan.api.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Reglas de negocio de los usuarios: RUN y correo únicos, dominios permitidos y contraseña con hash. */
class UsuarioServiceTest {

    private UsuarioRepository repo;
    private PasswordEncoder encoder;
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        repo = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenAnswer(i -> "hash(" + i.getArgument(0) + ")");
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        servicio = new UsuarioService(repo, encoder);
    }

    private static UsuarioRequest pedido(String run, String correo, String clave, TipoUsuario tipo) {
        return new UsuarioRequest(run, "Ana", "Pérez", correo, clave, "Región Metropolitana", "Maipú", "Calle 1", tipo);
    }

    @Test
    @DisplayName("el registro público siempre crea un Cliente, aunque se intente enviar otro rol")
    void registroSiempreCliente() {
        UsuarioResponse r = servicio.crear(pedido("190110222", "ana@gmail.com", "abcd", TipoUsuario.ADMINISTRADOR), TipoUsuario.CLIENTE);
        assertEquals(TipoUsuario.CLIENTE, r.tipoUsuario());
    }

    @Test
    @DisplayName("el administrador puede crear usuarios con otro rol (ej. Repartidor)")
    void adminCreaOtroRol() {
        UsuarioResponse r = servicio.crear(pedido("190110222", "rep@duoc.cl", "abcd", TipoUsuario.REPARTIDOR), null);
        assertEquals(TipoUsuario.REPARTIDOR, r.tipoUsuario());
    }

    @Test
    @DisplayName("la contraseña se guarda con hash, nunca en texto plano")
    void contrasenaConHash() {
        servicio.crear(pedido("190110222", "ana@gmail.com", "abcd", null), TipoUsuario.CLIENTE);
        org.mockito.ArgumentCaptor<Usuario> capturado = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(capturado.capture());
        assertEquals("hash(abcd)", capturado.getValue().getContrasena());
    }

    @Test
    @DisplayName("el correo se guarda en minúsculas")
    void correoEnMinusculas() {
        UsuarioResponse r = servicio.crear(pedido("190110222", "ANA@Gmail.com", "abcd", null), TipoUsuario.CLIENTE);
        assertEquals("ana@gmail.com", r.correo());
    }

    @Test
    @DisplayName("RUN repetido: se rechaza")
    void runRepetido() {
        when(repo.existsById("190110222")).thenReturn(true);
        assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(pedido("190110222", "ana@gmail.com", "abcd", null), TipoUsuario.CLIENTE));
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("correo repetido: se rechaza")
    void correoRepetido() {
        when(repo.existsByCorreo("ana@gmail.com")).thenReturn(true);
        assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(pedido("190110222", "ana@gmail.com", "abcd", null), TipoUsuario.CLIENTE));
    }

    @Test
    @DisplayName("solo se aceptan correos duoc.cl, profesor.duoc.cl y gmail.com")
    void dominioNoPermitido() {
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(pedido("190110222", "ana@hotmail.com", "abcd", null), TipoUsuario.CLIENTE));
    }

    @Test
    @DisplayName("al crear, la contraseña es obligatoria")
    void contrasenaObligatoria() {
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(pedido("190110222", "ana@gmail.com", "", null), TipoUsuario.CLIENTE));
    }

    @Test
    @DisplayName("al editar con contraseña vacía se conserva la anterior")
    void editarConservaContrasena() {
        Usuario existente = new Usuario();
        existente.setRun("190110222");
        existente.setContrasena("hash-antiguo");
        existente.setTipoUsuario(TipoUsuario.CLIENTE);
        when(repo.findById("190110222")).thenReturn(Optional.of(existente));

        servicio.actualizar("190110222", pedido("190110222", "ana@gmail.com", "", null));

        assertEquals("hash-antiguo", existente.getContrasena());
    }

    @Test
    @DisplayName("editar un usuario que no existe da error 404")
    void editarInexistente() {
        when(repo.findById("111111111")).thenReturn(Optional.empty());
        assertThrows(cl.gasvolcan.api.exception.RecursoNoEncontradoException.class,
                () -> servicio.actualizar("111111111", pedido("111111111", "ana@gmail.com", "", null)));
    }
}
