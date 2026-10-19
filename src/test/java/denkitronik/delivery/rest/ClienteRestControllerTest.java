package denkitronik.delivery.rest;

import denkitronik.domain.services.IClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import denkitronik.domain.entities.Cliente;
import denkitronik.domain.entities.Region;
import denkitronik.domain.exception.ClienteNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClienteRestController.class)
class ClienteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IClienteService clienteService;

    private static final String URL = "/api/v1/cliente-service/clientes";

    private Cliente crearCliente() {
        Region region = new Region();
        region.setId(1L);
        region.setNombre("Suramerica");

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Laura");
        cliente.setApellido("Perez");
        cliente.setEmail("laura@example.com");
        cliente.setRegion(region);

        return cliente;
    }

    private String jsonCliente() {
        return """
            {
                "nombre": "Laura",
                "apellido": "Perez",
                "email": "laura@example.com",
                "region": {"id": 1}
            }
            """;
    }

    @Test
    void listarClientes_debeRetornar200() throws Exception {
        when(clienteService.findAll())
                .thenReturn(List.of(crearCliente()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laura"));
    }

    @Test
    void buscarCliente_existente_debeRetornar200() throws Exception {
        when(clienteService.findById(1L))
                .thenReturn(crearCliente());

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laura"));
    }

    @Test
    void buscarCliente_inexistente_debeRetornar404() throws Exception {
        when(clienteService.findById(999L))
                .thenThrow(new ClienteNotFoundException(999L));

        mockMvc.perform(get(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }


    // 4. POST: crear cliente con datos validos -> 201
    @Test
    void crearCliente_valido_debeRetornar201() throws Exception {
        when(clienteService.save(any(Cliente.class)))
                .thenReturn(crearCliente());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laura"));

        verify(clienteService).save(any(Cliente.class));
    }

    // 5. POST: nombre vacio -> 400
    @Test
    void crearCliente_nombreVacio_debeRetornar400() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "apellido": "Perez",
                    "email": "laura@example.com",
                    "region": {"id": 1}
                }
                """;

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verify(clienteService, never()).save(any(Cliente.class));
    }

    // 6. POST: email invalido -> 400
    @Test
    void crearCliente_emailInvalido_debeRetornar400() throws Exception {
        String json = """
                {
                    "nombre": "Laura",
                    "apellido": "Perez",
                    "email": "correo-invalido",
                    "region": {"id": 1}
                }
                """;

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verify(clienteService, never()).save(any(Cliente.class));
    }

    // 7. PUT: actualizar cliente existente -> 201
    @Test
    void actualizarCliente_existente_debeRetornar201() throws Exception {
        Cliente actualizado = crearCliente();
        actualizado.setApellido("Gomez");

        when(clienteService.update(eq(1L), any(Cliente.class)))
                .thenReturn(actualizado);

        mockMvc.perform(put(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apellido").value("Gomez"));

        verify(clienteService)
                .update(eq(1L), any(Cliente.class));
    }

    // 8. PUT: actualizar cliente inexistente -> 404
    @Test
    void actualizarCliente_inexistente_debeRetornar404() throws Exception {
        when(clienteService.update(eq(999L), any(Cliente.class)))
                .thenThrow(new ClienteNotFoundException(999L));

        mockMvc.perform(put(URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isNotFound());
    }

    // 9. DELETE: eliminar cliente existente -> 204
    @Test
    void eliminarCliente_existente_debeRetornar204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());

        verify(clienteService).delete(1L);
    }

    // 10. DELETE: eliminar cliente inexistente -> 404
    @Test
    void eliminarCliente_inexistente_debeRetornar404() throws Exception {
        doThrow(new ClienteNotFoundException(999L))
                .when(clienteService).delete(999L);

        mockMvc.perform(delete(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // 11. GET: listar clientes paginados -> 200
    @Test
    void listarClientesPaginado_debeRetornar200() throws Exception {
        var pagina = new PageImpl<>(
                List.of(crearCliente()),
                PageRequest.of(0, 4),
                1
        );

        when(clienteService.findAll(any(Pageable.class)))
                .thenReturn(pagina);

        mockMvc.perform(get(URL + "/page/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre")
                        .value("Laura"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clienteService).findAll(PageRequest.of(0, 4));
    }





}