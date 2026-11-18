package cl.levelup.tienda.service;

import cl.levelup.tienda.dto.AuthResponse;
import cl.levelup.tienda.dto.LoginRequest;
import cl.levelup.tienda.dto.RegistroRequest;
import cl.levelup.tienda.dto.UsuarioDto;
import cl.levelup.tienda.exception.ConflictoException;
import cl.levelup.tienda.exception.CredencialesInvalidasException;
import cl.levelup.tienda.exception.SolicitudInvalidaException;
import cl.levelup.tienda.model.Rol;
import cl.levelup.tienda.model.Usuario;
import cl.levelup.tienda.repository.UsuarioRepository;
import cl.levelup.tienda.security.JwtService;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final int EDAD_MINIMA = 18;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByEmail(normalizar(request.email()))
                .filter(u -> encoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos."));
        return emitirTokens(usuario);
    }

    public UsuarioDto registrar(RegistroRequest request) {
        if (Period.between(request.fecha(), LocalDate.now()).getYears() < EDAD_MINIMA) {
            throw new SolicitudInvalidaException("Datos inválidos", Map.of("fecha", "Debes tener 18 años o más."));
        }
        String email = normalizar(request.email());
        if (usuarios.existsByEmail(email)) {
            throw new ConflictoException("Este correo ya está registrado.");
        }
        Usuario nuevo = new Usuario(request.nombre().trim(), email, encoder.encode(request.password()),
                Rol.CLIENTE, request.fecha());
        return UsuarioDto.de(usuarios.save(nuevo));
    }

    public AuthResponse renovar(String refreshToken) {
        Usuario usuario = jwtService.leer(refreshToken, JwtService.TIPO_REFRESH)
                .flatMap(claims -> usuarios.findByEmail(claims.getSubject()))
                .orElseThrow(() -> new CredencialesInvalidasException("La sesión expiró, inicia sesión nuevamente."));
        return emitirTokens(usuario);
    }

    public UsuarioDto usuarioActual(String email) {
        return usuarios.findByEmail(email).map(UsuarioDto::de)
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario no encontrado."));
    }

    private AuthResponse emitirTokens(Usuario usuario) {
        return new AuthResponse(jwtService.generarAccessToken(usuario), jwtService.generarRefreshToken(usuario),
                UsuarioDto.de(usuario));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
