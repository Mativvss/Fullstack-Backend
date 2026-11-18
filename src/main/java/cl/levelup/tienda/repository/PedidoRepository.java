package cl.levelup.tienda.repository;

import cl.levelup.tienda.model.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByIdAsc();

    Optional<Pedido> findTopByOrderByIdDesc();
}
