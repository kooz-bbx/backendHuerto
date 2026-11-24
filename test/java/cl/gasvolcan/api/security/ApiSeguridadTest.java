package cl.gasvolcan.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de seguridad de punta a punta (JWT + roles) con la aplicación completa y una base H2.
 * Usan las cuentas que crea DataInitializer al partir.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSeguridadTest {

    @Autowired
    private MockMvc mvc;

    private String loginCompleto(String correo, String clave) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String accessToken(String correo, String clave) throws Exception {
        return JsonPath.read(loginCompleto(correo, clave), "$.accessToken");
    }

    @Test
    @DisplayName("el catálogo de productos es público")
    void catalogoPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("sin token, una ruta protegida responde 401")
    void sinTokenEs401() throws Exception {
        mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un token inventado responde 401")
    void tokenFalso() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer esto.no.es.un.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login con contraseña incorrecta responde 401")
    void loginIncorrecto() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("el administrador entra a /usuarios con su token")
    void adminEntra() throws Exception {
        String token = accessToken("admin@duoc.cl", "admin123");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("el repartidor no puede ver /usuarios ni todas las órdenes (403), pero sí sus entregas")
    void repartidorRestringido() throws Exception {
        String token = accessToken("repenzo@duoc.cl", "reparte123");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/ordenes").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/ordenes/mis-entregas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("la operadora ve todas las órdenes pero no administra usuarios")
    void operadoraRestringida() throws Exception {
        String token = accessToken("operadora@duoc.cl", "opera123");
        mvc.perform(get("/api/v1/ordenes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("el refresh token renueva la sesión, pero no sirve para llamar a la API")
    void renovacion() throws Exception {
        String respuesta = loginCompleto("admin@duoc.cl", "admin123");
        String refresh = JsonPath.read(respuesta, "$.refreshToken");

        // no sirve como token de acceso
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        // sí sirve para pedir tokens nuevos
        String renovado = mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String nuevoAcceso = JsonPath.read(renovado, "$.accessToken");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + nuevoAcceso))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("un access token no sirve para renovar la sesión")
    void accessNoRenueva() throws Exception {
        String acceso = accessToken("admin@duoc.cl", "admin123");
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + acceso + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
