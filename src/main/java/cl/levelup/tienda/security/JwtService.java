package cl.levelup.tienda.security;

import cl.levelup.tienda.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final Duration duracionAccess;
    private final Duration duracionRefresh;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.access-minutos}") long accessMinutos,
                      @Value("${app.jwt.refresh-dias}") long refreshDias) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionAccess = Duration.ofMinutes(accessMinutos);
        this.duracionRefresh = Duration.ofDays(refreshDias);
    }

    public String generarAccessToken(Usuario usuario) {
        return generar(usuario, TIPO_ACCESS, duracionAccess);
    }

    public String generarRefreshToken(Usuario usuario) {
        return generar(usuario, TIPO_REFRESH, duracionRefresh);
    }

    public Optional<Claims> leer(String token, String tipoEsperado) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            if (!tipoEsperado.equals(claims.get("tipo", String.class))) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String generar(Usuario usuario, String tipo, Duration duracion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("rol", usuario.getRol().getValor())
                .claim("tipo", tipo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .id(UUID.randomUUID().toString())
                .signWith(clave)
                .compact();
    }
}
