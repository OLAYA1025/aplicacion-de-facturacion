package com.facturacion.negocio.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.NegocioRepository;
import com.facturacion.negocio.repository.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UsuarioRepository usuarioRepository,
                                   NegocioRepository negocioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token == null || token.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtService.parseToken(token);
            String username = claims.getSubject();
            Long usuarioId = jwtService.extractLongClaim(claims, "uid");
            String rolClaim = claims.get("rol", String.class);
            if (username == null || usuarioId == null || rolClaim == null || rolClaim.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            Optional<Usuario> usuarioOpt;
            if (Rol.SUPER_ADMIN.name().equals(rolClaim)) {
                usuarioOpt = usuarioRepository.findByIdAndRol(usuarioId, Rol.SUPER_ADMIN);
            } else {
                Long negocioId = jwtService.extractLongClaim(claims, "negocioId");
                if (negocioId == null || !negocioRepository.existsByIdAndActivoTrue(negocioId)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                TenantContext.setNegocioId(negocioId);
                usuarioOpt = usuarioRepository.findByIdAndNegocioId(usuarioId, negocioId);
            }
            if (usuarioOpt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            if (!usuario.isActivo()
                    || !username.equals(usuario.getUsername())
                    || !usuario.getRol().name().equals(rolClaim)) {
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rolClaim));
            if ("DEV".equals(rolClaim)) {
                // Permite que DEV pase cualquier verificacion por rol.
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
                authorities.add(new SimpleGrantedAuthority("ROLE_CAJERO"));
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            // token invalido: continuar sin autenticacion
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                return token;
            }
        }

        if (!isPedidosSseRequest(request)) {
            return null;
        }

        String queryToken = request.getParameter("access_token");
        if (queryToken == null) {
            return null;
        }

        String token = queryToken.trim();
        return token.isEmpty() ? null : token;
    }

    private boolean isPedidosSseRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }

        return "/pedidos/stream".equals(requestUri);
    }
}
