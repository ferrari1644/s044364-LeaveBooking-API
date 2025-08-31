package com.staffs.api.common.security;

import com.staffs.api.common.filters.HeaderObfuscationFilter;
import com.staffs.api.common.rateLimit.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenUtil jwt;
    private final RateLimitingFilter rateLimitingFilter;
    private final HeaderObfuscationFilter headerObfuscationFilter;
    private final SecurityHandlers securityHandlers;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // ✅ JWT best practice
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ preflight (optional)
                .anyRequest().authenticated()
        );

        // H2 console needs this
        http.headers(h -> h.frameOptions(f -> f.disable()));

        // Plug in your custom handlers
        http.exceptionHandling(e -> e
                .authenticationEntryPoint(securityHandlers)
                .accessDeniedHandler(securityHandlers)
        );

        // Filter order: rate limit -> JWT auth -> header scrub
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(headerObfuscationFilter, SecurityContextHolderFilter.class);

        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
