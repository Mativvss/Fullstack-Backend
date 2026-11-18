package cl.levelup.tienda.dto;

import cl.levelup.tienda.model.Pedido;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record PedidoDto(Long id, LocalDateTime fecha, String estado, String usuarioEmail, Cliente cliente,
                        Entrega entrega, List<Item> items, Resumen resumen) {

    public record Cliente(String nombre, String email, String telefono) {
    }

    public record Entrega(String tipo, String direccion, String comuna) {
    }

    public record Item(Long id, String nombre, int precio, int cantidad) {
    }

    public record Resumen(int subtotal, int descuento, int base, int iva, int total) {
    }

    public static PedidoDto de(Pedido p) {
        List<Item> items = p.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProductoId()))
                .map(i -> new Item(i.getProductoId(), i.getNombre(), i.getPrecio(), i.getCantidad()))
                .toList();
        return new PedidoDto(
                p.getId(), p.getFecha(), p.getEstado(), p.getUsuarioEmail(),
                new Cliente(p.getClienteNombre(), p.getClienteEmail(), p.getClienteTelefono()),
                new Entrega(p.getEntregaTipo(), p.getEntregaDireccion(), p.getEntregaComuna()),
                items,
                new Resumen(p.getSubtotal(), p.getDescuento(), p.getBase(), p.getIva(), p.getTotal()));
    }
}
