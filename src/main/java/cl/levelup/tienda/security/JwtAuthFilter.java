package cl.levelup.tienda.security;

import cl.levelup.tienda.model.Usuario;
import cl.levelup.tienda.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            Optional<Claims> claims = jwtService.leer(token, JwtService.TIPO_ACCESS);
            claims.flatMap(c -> usuarios.findByEmail(c.getSubject())).ifPresent(this::autenticar);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(Usuario usuario) {
        var autoridad = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
        var autenticacion = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of(autoridad));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }
}
