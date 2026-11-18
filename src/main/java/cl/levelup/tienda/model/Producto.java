package cl.levelup.tienda.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private int precio;

    @Column(nullable = false)
    private int stock;

    private String img;

    @Column(length = 500)
    private String descripcion;

    public Producto() {
    }

    public Producto(String categoria, String nombre, int precio, int stock, String img, String descripcion) {
        this.categoria = categoria;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.img = img;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
