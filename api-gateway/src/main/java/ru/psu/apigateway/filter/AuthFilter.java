package ru.psu.apigateway.filter;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.psu.apigateway.util.JwtUtil;

@Component
public class AuthFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    public AuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // Открытые маршруты (регистрация, вход и просмотр витрины товаров) - пропуск без токена
        if (path.startsWith("/api/v1/auth") ||
                (path.startsWith("/api/v1/products") && method == HttpMethod.GET)) {
            return chain.filter(exchange);
        }

        // Для всех остальных запросов требуем заголовок Authorization: Bearer <token>
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            String role = claims.get("role", String.class);
            String userId = String.valueOf(claims.get("userId"));

            // Проверка прав администратора для изменения каталога (POST, PUT, DELETE)
            if (path.startsWith("/api/v1/products") && !"ROLE_ADMIN".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 403 Доступ запрещен
                return exchange.getResponse().setComplete();
            }

            // Пробрас запроса дальше в микросервис, добавив заголовок X-User-Id
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            // Если токен подделан или просрочен
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
