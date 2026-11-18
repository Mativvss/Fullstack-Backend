package cl.levelup.tienda.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String usuarioEmail;

    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String entregaTipo;
    private String entregaDireccion;
    private String entregaComuna;

    private int subtotal;
    private int descuento;
    private int base;
    private int iva;
    private int total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoItem> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }
    public String getEntregaTipo() { return entregaTipo; }
    public void setEntregaTipo(String entregaTipo) { this.entregaTipo = entregaTipo; }
    public String getEntregaDireccion() { return entregaDireccion; }
    public void setEntregaDireccion(String entregaDireccion) { this.entregaDireccion = entregaDireccion; }
    public String getEntregaComuna() { return entregaComuna; }
    public void setEntregaComuna(String entregaComuna) { this.entregaComuna = entregaComuna; }
    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public int getDescuento() { return descuento; }
    public void setDescuento(int descuento) { this.descuento = descuento; }
    public int getBase() { return base; }
    public void setBase(int base) { this.base = base; }
    public int getIva() { return iva; }
    public void setIva(int iva) { this.iva = iva; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<PedidoItem> getItems() { return items; }

    public void agregarItem(PedidoItem item) {
        item.setPedido(this);
        items.add(item);
    }
}
