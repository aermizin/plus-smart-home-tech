package ru.yandex.practicum.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/inventory/reserve").hasRole("USER")
                        .pathMatchers(HttpMethod.POST, "/api/inventory/reserves").hasRole("USER")
                        .pathMatchers(HttpMethod.POST, "/api/inventory/release").hasRole("USER")
                        .pathMatchers(HttpMethod.POST, "/api/inventory/release/batch").hasRole("USER")

                        .pathMatchers(HttpMethod.GET, "/api/orders/*").hasRole("USER")
                        .pathMatchers(HttpMethod.POST, "/api/orders").hasRole("USER")

                        .pathMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/products/*").hasRole("ADMIN")

                        .pathMatchers("/api/inventory").hasRole("ADMIN")
                        .pathMatchers("/api/categories").hasRole("ADMIN")
                        .anyExchange().denyAll()
                )
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    ReactiveUserDetailsService userDetailsService(AppSecurityProperties props, PasswordEncoder passwordEncoder) {

        List<UserDetails> users = props.getUsers().stream()
                .map(u -> User.builder()
                        .username(u.getUsername())
                        .password(passwordEncoder.encode(u.getPassword()))
                        .roles(u.getRoles().toArray(String[]::new))
                        .build())
                .toList();

        return new MapReactiveUserDetailsService(users);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
