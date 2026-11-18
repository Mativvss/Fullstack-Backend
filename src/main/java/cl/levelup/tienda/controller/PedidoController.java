package cl.levelup.tienda.controller;

import cl.levelup.tienda.dto.PedidoDto;
import cl.levelup.tienda.dto.PedidoRequest;
import cl.levelup.tienda.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Compras: checkout, listado para personal y detalle")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    @Operation(summary = "Realiza una compra: valida stock, calcula descuento Duoc e IVA y registra el pedido")
    public ResponseEntity<PedidoDto> crear(@Valid @RequestBody PedidoRequest request, Authentication autenticacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidos.crear(autenticacion.getName(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Lista todos los pedidos (administrador y vendedor)")
    public List<PedidoDto> listar() {
        return pedidos.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un pedido (personal o dueño del pedido)")
    public PedidoDto obtener(@PathVariable Long id, Authentication autenticacion) {
        return pedidos.obtener(id, autenticacion);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Elimina un pedido (administrador)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
