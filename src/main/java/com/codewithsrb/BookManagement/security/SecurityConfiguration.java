package com.codewithsrb.BookManagement.security;


import com.codewithsrb.BookManagement.filter.CustomizeAuthorizationFilter;
import com.codewithsrb.BookManagement.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

/**
 * Configuration class which is providing the spring security authentication and authorization
 * for any http request. It uses the User details service for validating the user from the database.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final PasswordEncoder passwordEncoder;
    private final CustomizeAuthorizationFilter customizeAuthorizationFilter;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public SecurityConfiguration(AccessDeniedHandlerImpl accessDeniedHandler, AuthenticationEntryPointImpl authenticationEntryPoint, PasswordEncoder passwordEncoder, CustomizeAuthorizationFilter customizeAuthorizationFilter, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.passwordEncoder = passwordEncoder;
        this.customizeAuthorizationFilter = customizeAuthorizationFilter;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return userDetailsServiceImpl;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        return security.csrf(csrf -> csrf.ignoringRequestMatchers(toH2Console()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/books/register", "/books/login").permitAll()
                        .requestMatchers(toH2Console()).permitAll()
                        .requestMatchers("/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(customizer -> customizer.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(customizeAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
