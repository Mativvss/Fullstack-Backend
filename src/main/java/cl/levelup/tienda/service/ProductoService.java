package cl.levelup.tienda.service;

import cl.levelup.tienda.dto.ProductoDto;
import cl.levelup.tienda.dto.ProductoRequest;
import cl.levelup.tienda.exception.RecursoNoEncontradoException;
import cl.levelup.tienda.model.Producto;
import cl.levelup.tienda.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private static final String IMAGEN_POR_DEFECTO = "/assets/img/logo.jpg";

    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listar() {
        return productos.findAll().stream().map(ProductoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(Long id) {
        return ProductoDto.de(buscar(id));
    }

    @Transactional
    public ProductoDto crear(ProductoRequest request) {
        Producto producto = new Producto();
        copiar(request, producto);
        return ProductoDto.de(productos.save(producto));
    }

    @Transactional
    public ProductoDto actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        copiar(request, producto);
        return ProductoDto.de(productos.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        productos.delete(buscar(id));
    }

    private Producto buscar(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    private void copiar(ProductoRequest request, Producto producto) {
        producto.setNombre(request.nombre().trim());
        producto.setCategoria(request.categoria().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setImg(request.img() == null || request.img().isBlank() ? IMAGEN_POR_DEFECTO : request.img());
        producto.setDescripcion(request.descripcion());
    }
}
