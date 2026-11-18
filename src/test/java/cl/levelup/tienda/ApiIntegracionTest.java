package cl.levelup.tienda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "test"})
class ApiIntegracionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private ResultActions enviarJson(String metodo, String url, String token, Object cuerpo) throws Exception {
        var peticion = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> throw new IllegalArgumentException(metodo);
        };
        peticion.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(cuerpo));
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(peticion);
    }

    private JsonNode login(String email, String password) throws Exception {
        String json = enviarJson("POST", "/api/auth/login", null, Map.of("email", email, "password", password))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(json);
    }

    private String tokenAdmin() throws Exception {
        return login("admin@levelup.cl", "admin123").get("accessToken").asText();
    }

    private String tokenVendedor() throws Exception {
        return login("vendedor@levelup.cl", "vendedor123").get("accessToken").asText();
    }

    private String tokenCliente() throws Exception {
        return login("cliente@levelup.cl", "cliente123").get("accessToken").asText();
    }

    private String registrarYEntrar(String email) throws Exception {
        enviarJson("POST", "/api/auth/register", null,
                Map.of("nombre", "Cliente Prueba", "email", email, "fecha", "1999-05-20", "password", "clave123"))
                .andExpect(status().isCreated());
        return login(email, "clave123").get("accessToken").asText();
    }

    private int stockDe(long productoId) throws Exception {
        String json = mvc.perform(get("/api/productos/" + productoId)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("stock").asInt();
    }

    private Map<String, Object> compra(long productoId, int cantidad, boolean pagoExitoso) {
        return Map.of(
                "items", List.of(Map.of("productoId", productoId, "cantidad", cantidad)),
                "nombre", "Cliente Prueba",
                "email", "cliente@correo.cl",
                "telefono", "912345678",
                "entrega", "retiro",
                "pagoExitoso", pagoExitoso);
    }

    @Test
    void loginCorrectoEntregaTokensYUsuario() throws Exception {
        JsonNode respuesta = login("admin@levelup.cl", "admin123");
        assertEquals("administrador", respuesta.get("usuario").get("rol").asText());
        assertNotEquals("", respuesta.get("accessToken").asText());
        assertNotEquals("", respuesta.get("refreshToken").asText());
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        enviarJson("POST", "/api/auth/login", null, Map.of("email", "admin@levelup.cl", "password", "mala"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
    }

    @Test
    void endpointProtegidoSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void tokenInventadoDevuelve401() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeListarUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenCliente()))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorPuedeVerPedidosPeroNoCrearProductos() throws Exception {
        String token = tokenVendedor();
        mvc.perform(get("/api/pedidos").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        enviarJson("POST", "/api/productos", token, Map.of("nombre", "Teclado", "categoria", "Accesorios",
                "precio", 10000, "stock", 3)).andExpect(status().isForbidden());
    }

    @Test
    void refreshRenuevaLosTokens() throws Exception {
        String refresh = login("cliente@levelup.cl", "cliente123").get("refreshToken").asText();
        String json = enviarJson("POST", "/api/auth/refresh", null, Map.of("refreshToken", refresh))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String nuevoAccess = mapper.readTree(json).get("accessToken").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + nuevoAccess))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("cliente@levelup.cl"));
    }

    @Test
    void unAccessTokenNoSirveComoRefreshToken() throws Exception {
        String access = tokenCliente();
        enviarJson("POST", "/api/auth/refresh", null, Map.of("refreshToken", access))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unRefreshTokenNoSirveParaAccederARecursos() throws Exception {
        String refresh = login("admin@levelup.cl", "admin123").get("refreshToken").asText();
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroValidaEdadYCorreoRepetido() throws Exception {
        enviarJson("POST", "/api/auth/register", null, Map.of("nombre", "Menor", "email", "menor@correo.cl",
                "fecha", "2020-01-01", "password", "clave123"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.fecha").exists());

        String email = "repetido-" + UUID.randomUUID() + "@correo.cl";
        registrarYEntrar(email);
        enviarJson("POST", "/api/auth/register", null, Map.of("nombre", "Otra Persona", "email", email,
                "fecha", "1999-05-20", "password", "clave123")).andExpect(status().isConflict());
    }

    @Test
    void compraExitosaConDescuentoDuocIvaYDescuentoDeStock() throws Exception {
        String token = registrarYEntrar("ana-" + UUID.randomUUID() + "@duocuc.cl");
        int antes = stockDe(8);

        enviarJson("POST", "/api/pedidos", token, compra(8, 2, true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("Compra exitosa"))
                .andExpect(jsonPath("$.resumen.subtotal").value(99980))
                .andExpect(jsonPath("$.resumen.descuento").value(19996))
                .andExpect(jsonPath("$.resumen.base").value(79984))
                .andExpect(jsonPath("$.resumen.iva").value(15197))
                .andExpect(jsonPath("$.resumen.total").value(95181))
                .andExpect(jsonPath("$.items[0].id").value(8))
                .andExpect(jsonPath("$.entrega.tipo").value("retiro"));

        assertEquals(antes - 2, stockDe(8));
    }

    @Test
    void compraFallidaNoDescuentaStock() throws Exception {
        String token = tokenCliente();
        int antes = stockDe(10);

        enviarJson("POST", "/api/pedidos", token, compra(10, 3, false))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("Compra fallida"))
                .andExpect(jsonPath("$.resumen.descuento").value(0))
                .andExpect(jsonPath("$.resumen.total").value(53514));

        assertEquals(antes, stockDe(10));
    }

    @Test
    void compraConStockInsuficienteDevuelve409() throws Exception {
        enviarJson("POST", "/api/pedidos", tokenCliente(), compra(6, 99, true))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void compraADomicilioExigeDireccionYComuna() throws Exception {
        Map<String, Object> cuerpo = Map.of(
                "items", List.of(Map.of("productoId", 9, "cantidad", 1)),
                "nombre", "Cliente Prueba", "email", "cliente@correo.cl", "telefono", "912345678",
                "entrega", "domicilio", "pagoExitoso", true);
        enviarJson("POST", "/api/pedidos", tokenCliente(), cuerpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.direccion").exists())
                .andExpect(jsonPath("$.errores.comuna").exists());
    }

    @Test
    void vendedorNoPuedeComprar() throws Exception {
        enviarJson("POST", "/api/pedidos", tokenVendedor(), compra(9, 1, true)).andExpect(status().isForbidden());
    }

    @Test
    void clienteNoVeLosPedidosDeOtroPeroElPersonalSi() throws Exception {
        String dueno = registrarYEntrar("dueno-" + UUID.randomUUID() + "@correo.cl");
        String json = enviarJson("POST", "/api/pedidos", dueno, compra(9, 1, false))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(json).get("id").asLong();

        mvc.perform(get("/api/pedidos/" + id).header("Authorization", "Bearer " + dueno))
                .andExpect(status().isOk());
        mvc.perform(get("/api/pedidos/" + id).header("Authorization", "Bearer " + tokenCliente()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/pedidos/" + id).header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/pedidos/999999").header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isNotFound());
    }

    @Test
    void administradorHaceCrudDeProductos() throws Exception {
        String token = tokenAdmin();
        String creado = enviarJson("POST", "/api/productos", token, Map.of("nombre", "Teclado Mecánico",
                "categoria", "Accesorios", "precio", 45990, "stock", 7, "descripcion", "Teclado RGB"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.img").value("/assets/img/logo.jpg"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(creado).get("id").asLong();

        enviarJson("PUT", "/api/productos/" + id, token, Map.of("nombre", "Teclado Mecánico Pro",
                "categoria", "Accesorios", "precio", 49990, "stock", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(49990))
                .andExpect(jsonPath("$.stock").value(4));

        mvc.perform(get("/api/productos/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Teclado Mecánico Pro"));

        mvc.perform(delete("/api/productos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void productoConStockNegativoEsRechazado() throws Exception {
        enviarJson("POST", "/api/productos", tokenAdmin(), Map.of("nombre", "Raro", "categoria", "Accesorios",
                "precio", 1000, "stock", -1))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.stock").exists());
    }

    @Test
    void administradorCambiaRolesPeroNoPuedeEliminarseASiMismo() throws Exception {
        String token = tokenAdmin();
        String email = "rol-" + UUID.randomUUID() + "@correo.cl";
        registrarYEntrar(email);

        String lista = mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long idNuevo = -1;
        long idAdmin = -1;
        for (JsonNode u : mapper.readTree(lista)) {
            if (u.get("email").asText().equals(email)) idNuevo = u.get("id").asLong();
            if (u.get("email").asText().equals("admin@levelup.cl")) idAdmin = u.get("id").asLong();
        }

        enviarJson("PUT", "/api/usuarios/" + idNuevo + "/rol", token, Map.of("rol", "vendedor"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rol").value("vendedor"));
        mvc.perform(delete("/api/usuarios/" + idAdmin).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/usuarios/" + idNuevo).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void swaggerYDocumentacionOpenApiSonPublicos() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/productos']").exists());
    }
}
