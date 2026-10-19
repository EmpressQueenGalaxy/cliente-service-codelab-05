
package denkitronik;

import denkitronik.domain.entities.Cliente;
import denkitronik.domain.entities.Region;
import denkitronik.domain.repositories.IClienteDao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:e2edb;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@DisplayName("E2E tests — ClienteService con H2")
class ClienteIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private IClienteDao clienteDao;

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final HttpClient http = HttpClient.newHttpClient();

    private Long regionId;

    private String url() {
        return "http://localhost:" + port
                + "/api/v1/cliente-service/clientes";
    }

    @BeforeEach
    void setUp() {
        clienteDao.deleteAll();

        regionId = transactionTemplate.execute(status -> {
            Region region = new Region();
            region.setNombre("Asia");
            em.persist(region);
            em.flush();
            return region.getId();
        });
    }

    private Cliente guardarCliente() {
        Region region = transactionTemplate.execute(
                status -> em.find(Region.class, regionId)
        );

        Cliente cliente = new Cliente();
        cliente.setNombre("Grace");
        cliente.setApellido("Hopper");
        cliente.setEmail("grace@example.com");
        cliente.setRegion(region);

        return clienteDao.saveAndFlush(cliente);
    }

    private String jsonValido() {
        return """
                {
                    "nombre": "Ada",
                    "apellido": "Lovelace",
                    "email": "ada@example.com",
                    "region": {"id": %d}
                }
                """.formatted(regionId);
    }

    private HttpResponse<String> enviar(
            String metodo, String ruta, String json
    ) throws Exception {

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url() + ruta))
                .header("Accept", "application/json");

        if (json != null) {
            request.header("Content-Type", "application/json");
        }

        switch (metodo) {
            case "POST" -> request.POST(
                    HttpRequest.BodyPublishers.ofString(json)
            );
            case "PUT" -> request.PUT(
                    HttpRequest.BodyPublishers.ofString(json)
            );
            case "DELETE" -> request.DELETE();
            default -> request.GET();
        }

        return http.send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // 1. GET: listar clientes
    @Test
    void listarClientes_debeRetornar200() throws Exception {
        guardarCliente();

        var respuesta = enviar("GET", "", null);

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body()).contains("Grace");
    }

    // 2. GET: buscar cliente existente
    @Test
    void buscarCliente_existente_debeRetornar200() throws Exception {
        Cliente cliente = guardarCliente();

        var respuesta = enviar(
                "GET", "/" + cliente.getId(), null
        );

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body())
                .contains("Grace", "Hopper", "grace@example.com");
    }

    // 3. GET: buscar cliente inexistente
    @Test
    void buscarCliente_inexistente_debeRetornar404() throws Exception {
        var respuesta = enviar("GET", "/999999", null);

        assertThat(respuesta.statusCode()).isEqualTo(404);
    }

    // 4. POST: crear cliente
    @Test
    void crearCliente_valido_debeRetornar201() throws Exception {
        var respuesta = enviar("POST", "", jsonValido());

        assertThat(respuesta.statusCode()).isEqualTo(201);
        assertThat(respuesta.body())
                .contains("Ada", "Lovelace", "ada@example.com");

        assertThat(clienteDao.findAll())
                .extracting(Cliente::getNombre)
                .contains("Ada");
    }

    // 5. POST: email invalido
    @Test
    void crearCliente_emailInvalido_debeRetornar400() throws Exception {
        String json = """
                {
                    "nombre": "Ada",
                    "apellido": "Lovelace",
                    "email": "correo-invalido",
                    "region": {"id": %d}
                }
                """.formatted(regionId);

        var respuesta = enviar("POST", "", json);

        assertThat(respuesta.statusCode()).isEqualTo(400);
        assertThat(clienteDao.findAll()).isEmpty();
    }

    // 6. PUT: actualizar cliente
    @Test
    void actualizarCliente_existente_debeRetornar201()
            throws Exception {

        Cliente cliente = guardarCliente();

        var respuesta = enviar(
                "PUT", "/" + cliente.getId(), jsonValido()
        );

        assertThat(respuesta.statusCode()).isEqualTo(201);

        Cliente actualizado = clienteDao.findById(
                cliente.getId()
        ).orElseThrow();

        assertThat(actualizado.getNombre()).isEqualTo("Ada");
        assertThat(actualizado.getApellido()).isEqualTo("Lovelace");
    }

    // 7. DELETE: eliminar cliente
    @Test
    void eliminarCliente_existente_debeRetornar204()
            throws Exception {

        Cliente cliente = guardarCliente();

        var respuesta = enviar(
                "DELETE", "/" + cliente.getId(), null
        );

        assertThat(respuesta.statusCode()).isEqualTo(204);
        assertThat(clienteDao.existsById(cliente.getId()))
                .isFalse();
    }
}
