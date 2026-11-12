package com.metroruta.config;

import com.metroruta.view.VistaLogin;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SeguridadConfig {

    // vaadin ya sabe proteger sus vistas, solo le decimos cual es la pantalla de login
    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(reglas -> reglas.requestMatchers("/styles/metro.css", "/mapa/metro-map.js").permitAll());
        http.with(VaadinSecurityConfigurer.vaadin(), configurador -> configurador.loginView(VistaLogin.class));
        return http.build();
    }

    // las claves se cifran y se comparan con BCrypt
    @Bean
    PasswordEncoder codificador() {
        return new BCryptPasswordEncoder();
    }
}
