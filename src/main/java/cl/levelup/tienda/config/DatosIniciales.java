package cl.levelup.tienda.config;

import cl.levelup.tienda.model.Producto;
import cl.levelup.tienda.model.Rol;
import cl.levelup.tienda.model.Usuario;
import cl.levelup.tienda.repository.ProductoRepository;
import cl.levelup.tienda.repository.UsuarioRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DatosIniciales(ProductoRepository productos, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.productos = productos;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (productos.count() == 0) {
            productos.saveAll(List.of(
                    new Producto("Juegos de Mesa", "Catan", 29990, 10, "/assets/img/catan.jpg",
                            "Juego de estrategia para 3 a 4 jugadores."),
                    new Producto("Juegos de Mesa", "Carcassonne", 24990, 8, "/assets/img/carcassonne.jpg",
                            "Construye ciudades y caminos con losetas."),
                    new Producto("Accesorios", "Controlador Xbox Series X", 59990, 15, "/assets/img/xbox-control.jpg",
                            "Control inalámbrico para Xbox y PC."),
                    new Producto("Accesorios", "Auriculares HyperX Cloud II", 79990, 6, "/assets/img/hyperx-cloud2.jpg",
                            "Audífonos gamer con sonido envolvente 7.1."),
                    new Producto("Consolas", "PlayStation 5", 549990, 5, "/assets/img/ps5.jpg",
                            "Consola de última generación de Sony."),
                    new Producto("Computadores Gamers", "PC Gamer ASUS ROG Strix", 1299990, 3,
                            "/assets/img/pc-rog-strix.jpg", "Computador de alto rendimiento para juegos."),
                    new Producto("Sillas Gamers", "Silla Secretlab Titan", 349990, 4, "/assets/img/silla-secretlab.jpg",
                            "Silla ergonómica para largas sesiones."),
                    new Producto("Mouse y Mousepad", "Mouse Logitech G502", 49990, 12, "/assets/img/mouse-logitech.webp",
                            "Mouse gamer con sensor HERO de alta precisión."),
                    new Producto("Mouse y Mousepad", "Mousepad Razer Goliathus", 29990, 20,
                            "/assets/img/mousepad-razer.jpg", "Mousepad extendido con iluminación Chroma."),
                    new Producto("Poleras Personalizadas", "Polera Level-Up", 14990, 25,
                            "/assets/img/polera-levelup.jpg", "Polera gamer personalizada Level-Up.")));
        }
        if (usuarios.count() == 0) {
            usuarios.saveAll(List.of(
                    new Usuario("Administrador Level-Up", "admin@levelup.cl", encoder.encode("admin123"),
                            Rol.ADMINISTRADOR, null),
                    new Usuario("Vendedor Level-Up", "vendedor@levelup.cl", encoder.encode("vendedor123"),
                            Rol.VENDEDOR, null),
                    new Usuario("Cliente Demo", "cliente@levelup.cl", encoder.encode("cliente123"),
                            Rol.CLIENTE, null)));
        }
    }
}
