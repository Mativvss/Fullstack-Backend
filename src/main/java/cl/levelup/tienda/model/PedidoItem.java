package cl.levelup.tienda.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pedido_items")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String nombre;

    private int precio;
    private int cantidad;

    public PedidoItem() {
    }

    public PedidoItem(Long productoId, String nombre, int precio, int cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public int getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
}
