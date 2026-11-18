package cl.levelup.tienda.service;

import cl.levelup.tienda.dto.PedidoDto;
import cl.levelup.tienda.dto.PedidoRequest;
import cl.levelup.tienda.exception.ConflictoException;
import cl.levelup.tienda.exception.RecursoNoEncontradoException;
import cl.levelup.tienda.exception.SolicitudInvalidaException;
import cl.levelup.tienda.model.Pedido;
import cl.levelup.tienda.model.PedidoItem;
import cl.levelup.tienda.model.Producto;
import cl.levelup.tienda.model.Usuario;
import cl.levelup.tienda.repository.PedidoRepository;
import cl.levelup.tienda.repository.ProductoRepository;
import cl.levelup.tienda.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    public static final String COMPRA_EXITOSA = "Compra exitosa";
    public static final String COMPRA_FALLIDA = "Compra fallida";

    private static final double IVA = 0.19;
    private static final double DESCUENTO_DUOC = 0.2;
    private static final long PRIMER_ID = 1001L;

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    @Transactional
    public PedidoDto crear(String emailUsuario, PedidoRequest request) {
        Usuario usuario = usuarios.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        validarDatos(request);

        Map<Long, Integer> pedidoPorProducto = request.items().stream()
                .collect(Collectors.toMap(PedidoRequest.ItemRequest::productoId, PedidoRequest.ItemRequest::cantidad,
                        Integer::sum, LinkedHashMap::new));
        Map<Long, Producto> existentes = productos.buscarParaActualizar(pedidoPorProducto.keySet()).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        boolean pagado = Boolean.TRUE.equals(request.pagoExitoso());
        Pedido pedido = new Pedido();
        int subtotal = 0;
        for (Map.Entry<Long, Integer> linea : pedidoPorProducto.entrySet()) {
            Producto producto = existentes.get(linea.getKey());
            if (producto == null) {
                throw new ConflictoException("El producto con id " + linea.getKey() + " no existe.");
            }
            if (linea.getValue() > producto.getStock()) {
                throw new ConflictoException("Stock insuficiente de " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ").");
            }
            subtotal += producto.getPrecio() * linea.getValue();
            pedido.agregarItem(new PedidoItem(producto.getId(), producto.getNombre(), producto.getPrecio(),
                    linea.getValue()));
        }

        int descuento = esCorreoDuoc(usuario.getEmail()) ? (int) Math.round(subtotal * DESCUENTO_DUOC) : 0;
        int base = subtotal - descuento;
        int iva = (int) Math.round(base * IVA);

        pedido.setId(siguienteId());
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(pagado ? COMPRA_EXITOSA : COMPRA_FALLIDA);
        pedido.setUsuarioEmail(usuario.getEmail());
        pedido.setClienteNombre(request.nombre().trim());
        pedido.setClienteEmail(request.email().trim());
        pedido.setClienteTelefono(request.telefono().trim());
        pedido.setEntregaTipo(request.entrega());
        boolean domicilio = "domicilio".equals(request.entrega());
        pedido.setEntregaDireccion(domicilio ? request.direccion().trim() : null);
        pedido.setEntregaComuna(domicilio ? request.comuna().trim() : null);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setBase(base);
        pedido.setIva(iva);
        pedido.setTotal(base + iva);

        if (pagado) {
            pedidoPorProducto.forEach((id, cantidad) -> {
                Producto producto = existentes.get(id);
                producto.setStock(producto.getStock() - cantidad);
            });
            productos.saveAll(existentes.values());
        }
        return PedidoDto.de(pedidos.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoDto> listar() {
        return pedidos.findAllByOrderByIdAsc().stream().map(PedidoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoDto obtener(Long id, Authentication autenticacion) {
        Pedido pedido = pedidos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
        boolean personal = autenticacion.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_VENDEDOR"));
        if (!personal && !pedido.getUsuarioEmail().equals(autenticacion.getName())) {
            throw new AccessDeniedException("El pedido pertenece a otro usuario");
        }
        return PedidoDto.de(pedido);
    }

    @Transactional
    public void eliminar(Long id) {
        Pedido pedido = pedidos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
        pedidos.delete(pedido);
    }

    private long siguienteId() {
        return pedidos.findTopByOrderByIdDesc().map(p -> p.getId() + 1).orElse(PRIMER_ID);
    }

    private boolean esCorreoDuoc(String email) {
        return email.endsWith("@duoc.cl") || email.endsWith("@duocuc.cl");
    }

    private void validarDatos(PedidoRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        String telefono = request.telefono().replaceAll("[\\s+]", "");
        if (telefono.length() == 11 && telefono.startsWith("56")) {
            telefono = telefono.substring(2);
        }
        if (!telefono.matches("\\d{9}")) {
            errores.put("telefono", "Ingresa un teléfono de 9 dígitos (ej: 912345678).");
        }
        if ("domicilio".equals(request.entrega())) {
            if (request.direccion() == null || request.direccion().trim().length() < 5) {
                errores.put("direccion", "Ingresa la dirección de entrega.");
            }
            if (request.comuna() == null || request.comuna().trim().length() < 2) {
                errores.put("comuna", "Ingresa la comuna.");
            }
        }
        if (!errores.isEmpty()) {
            throw new SolicitudInvalidaException("Datos inválidos", errores);
        }
    }
}
