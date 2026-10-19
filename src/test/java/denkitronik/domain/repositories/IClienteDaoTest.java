
package denkitronik.domain.repositories;

import denkitronik.domain.entities.Cliente;
import denkitronik.domain.entities.Region;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Integration tests (data slice) — IClienteDao con H2")
class IClienteDaoTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private IClienteDao clienteDao;

    private Region region;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Preparar datos directamente con TestEntityManager.
        region = new Region();
        region.setNombre("Asia");
        em.persist(region);

        cliente = new Cliente();
        cliente.setNombre("Grace");
        cliente.setApellido("Hopper");
        cliente.setEmail("grace@navy.mil");
        cliente.setRegion(region);

        em.persist(cliente);
        em.flush();
    }

    // 1. Query personalizada del profesor
    @Test
    @DisplayName("findAllRegiones — devuelve las regiones persistidas")
    void findAllRegiones_debeRetornarListaDeRegiones() {
        List<Region> regiones = clienteDao.findAllRegiones();

        assertThat(regiones).hasSize(1);
        assertThat(regiones.get(0).getNombre()).isEqualTo("Asia");
    }

    // 2. Buscar por ID
    @Test
    @DisplayName("findById — encuentra el cliente existente")
    void findById_debeRetornarCliente() {
        Optional<Cliente> resultado = clienteDao.findById(cliente.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Grace");
        assertThat(resultado.get().getEmail()).isEqualTo("grace@navy.mil");
    }

    // 3. Listar todos
    @Test
    @DisplayName("findAll — devuelve los clientes persistidos")
    void findAll_debeRetornarListaDeClientes() {
        List<Cliente> clientes = clienteDao.findAll();

        assertThat(clientes).hasSize(1);
        assertThat(clientes.get(0).getApellido()).isEqualTo("Hopper");
    }

    // 4. Guardar un cliente
    @Test
    @DisplayName("save — persiste un cliente nuevo")
    void save_debeGuardarCliente() {
        Cliente nuevo = new Cliente();
        nuevo.setNombre("Ada");
        nuevo.setApellido("Lovelace");
        nuevo.setEmail("ada@example.com");
        nuevo.setRegion(region);

        Cliente guardado = clienteDao.saveAndFlush(nuevo);

        em.clear();

        Cliente encontrado = em.find(Cliente.class, guardado.getId());

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Ada");
    }

    // 5. Paginacion
    @Test
    @DisplayName("findAll(Pageable) — devuelve una pagina de clientes")
    void findAllPaginado_debeRetornarPagina() {
        Page<Cliente> pagina = clienteDao.findAll(PageRequest.of(0, 4));

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getNombre()).isEqualTo("Grace");
    }

    // 6. Eliminacion: ejemplo del profesor
    @Test
    @DisplayName("deleteById — elimina el cliente verificado con em.find")
    void deleteById_debeEliminarElCliente() {
        Long id = cliente.getId();

        clienteDao.deleteById(id);
        em.flush();

        assertThat(em.find(Cliente.class, id)).isNull();
    }
}
