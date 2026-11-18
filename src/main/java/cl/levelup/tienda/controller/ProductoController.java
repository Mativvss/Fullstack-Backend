package cl.levelup.tienda.controller;

import cl.levelup.tienda.dto.ProductoDto;
import cl.levelup.tienda.dto.ProductoRequest;
import cl.levelup.tienda.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo con stock. La lectura es pública; escribir requiere administrador")
public class ProductoController {

    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @GetMapping
    @Operation(summary = "Lista todos los productos")
    public List<ProductoDto> listar() {
        return productos.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id")
    public ProductoDto obtener(@PathVariable Long id) {
        return productos.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crea un producto (administrador)")
    public ResponseEntity<ProductoDto> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productos.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualiza un producto, incluido su stock (administrador)")
    public ProductoDto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productos.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Elimina un producto (administrador)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
