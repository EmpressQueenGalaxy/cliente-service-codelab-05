package denkitronik.domain.services;

import denkitronik.domain.entities.Cliente;
import denkitronik.domain.entities.Region;
import denkitronik.domain.exception.ClienteNotFoundException;
import denkitronik.domain.exception.ClienteServiceException;
import denkitronik.domain.repositories.IClienteDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests - ClienteServiceImpl con Mockito")
class ClienteServiceImplTest {

    @Mock
    private IClienteDao clienteDao;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
        region.setId(1L);
        region.setNombre("Sudamerica");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Linus");
        cliente.setApellido("Torvalds");
        cliente.setEmail("linus@kernel.org");
        cliente.setFoto("foto.jpg");
        cliente.setRegion(region);
    }

    // TEST 1: Buscar un cliente existente

    @Test
    @DisplayName("findById - ID existente devuelve cliente")
    void findById_idExistente_debeRetornarCliente() {
        when(clienteDao.findById(1L))
                .thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Linus");

        verify(clienteDao, times(1)).findById(1L);
    }

    // TEST 2: Buscar un cliente inexistente

    @Test
    @DisplayName("findById - ID inexistente lanza excepcion")
    void findById_idInexistente_debeLanzarExcepcion() {
        when(clienteDao.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(999L))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("999");
    }

    // TEST 3: Listar todos los clientes

    @Test
    @DisplayName("findAll - devuelve todos los clientes")
    void findAll_debeRetornarListaDeClientes() {
        when(clienteDao.findAll())
                .thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.findAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre())
                .isEqualTo("Linus");

        verify(clienteDao).findAll();
    }

    // TEST 4: Obtener clientes paginados

    @Test
    @DisplayName("findAll paginado - devuelve una pagina")
    void findAll_paginado_debeRetornarPagina() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Cliente> pagina = new PageImpl<>(
                List.of(cliente), pageable, 1
        );

        when(clienteDao.findAll(pageable))
                .thenReturn(pagina);

        Page<Cliente> resultado = clienteService.findAll(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getNumber()).isZero();

        verify(clienteDao).findAll(pageable);
    }

    // TEST 5: Guardar cliente correctamente

    @Test
    @DisplayName("save - guarda un cliente")
    void save_clienteValido_debeGuardar() {
        when(clienteDao.save(cliente))
                .thenReturn(cliente);

        Cliente resultado = clienteService.save(cliente);

        assertThat(resultado).isSameAs(cliente);
        assertThat(resultado.getEmail())
                .isEqualTo("linus@kernel.org");

        verify(clienteDao).save(cliente);
    }

    // TEST 6: Error al guardar cliente

    @Test
    @DisplayName("save - datos duplicados lanzan excepcion propia")
    void save_emailDuplicado_debeLanzarExcepcion() {
        when(clienteDao.save(cliente))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value"
                ));

        assertThatThrownBy(() -> clienteService.save(cliente))
                .isInstanceOf(ClienteServiceException.class)
                .hasMessageContaining("duplicados");
    }

    // TEST 7: Actualizar cliente existente

    @Test
    @DisplayName("update - actualiza todos los campos")
    void update_idExistente_debeActualizar() {
        Cliente nuevosDatos = new Cliente();
        nuevosDatos.setNombre("Linus Updated");
        nuevosDatos.setApellido("Torvalds");
        nuevosDatos.setEmail("nuevo@kernel.org");
        nuevosDatos.setFoto("nueva-foto.jpg");
        nuevosDatos.setRegion(region);

        when(clienteDao.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(clienteDao.save(any(Cliente.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = clienteService.update(1L, nuevosDatos);

        assertThat(resultado.getNombre())
                .isEqualTo("Linus Updated");
        assertThat(resultado.getEmail())
                .isEqualTo("nuevo@kernel.org");
        assertThat(resultado.getFoto())
                .isEqualTo("nueva-foto.jpg");
        assertThat(resultado.getRegion())
                .isSameAs(region);

        verify(clienteDao).findById(1L);
        verify(clienteDao).save(cliente);
    }

    // TEST 8: Actualizar cliente inexistente

    @Test
    @DisplayName("update - ID inexistente lanza excepcion")
    void update_idInexistente_debeLanzarExcepcion() {
        when(clienteDao.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                clienteService.update(999L, cliente))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("999");

        verify(clienteDao, never())
                .save(any(Cliente.class));
    }

    // TEST 9: Eliminar cliente existente

    @Test
    @DisplayName("delete - elimina cliente existente")
    void delete_idExistente_debeEliminar() {
        when(clienteDao.existsById(1L))
                .thenReturn(true);

        clienteService.delete(1L);

        verify(clienteDao).existsById(1L);
        verify(clienteDao).deleteById(1L);
    }

    // TEST 10: No eliminar cliente inexistente

    @Test
    @DisplayName("delete - ID inexistente no debe eliminar")
    void delete_idInexistente_noDebeEliminar() {
        when(clienteDao.existsById(999L))
                .thenReturn(false);

        assertThatThrownBy(() -> clienteService.delete(999L))
                .isInstanceOf(ClienteNotFoundException.class);

        verify(clienteDao, never())
                .deleteById(any(Long.class));
    }

    // TEST 11: Consultar regiones

    @Test
    @DisplayName("findAllRegiones - devuelve regiones")
    void findAllRegiones_debeRetornarRegiones() {
        when(clienteDao.findAllRegiones())
                .thenReturn(List.of(region));

        List<Region> resultado = clienteService.findAllRegiones();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre())
                .isEqualTo("Sudamerica");

        verify(clienteDao).findAllRegiones();
    }
}

