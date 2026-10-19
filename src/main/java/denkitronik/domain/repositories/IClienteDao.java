
package denkitronik.domain.repositories;

import denkitronik.domain.entities.Cliente;
import denkitronik.domain.entities.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface IClienteDao extends JpaRepository<Cliente, Long> {

    // JpaRepository ya proporciona findAll(Pageable pageable).

    @Query("from Region")
    List<Region> findAllRegiones();
}
