package io.github.codexrm.server.infrastructure.security;

import io.github.codexrm.server.infrastructure.security.jwt.AuthEntryPointJwt;
import io.github.codexrm.server.infrastructure.security.jwt.AuthTokenFilter;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import io.github.codexrm.server.infrastructure.security.jwt.AuthEntryPointJwt;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;
  private final AuthEntryPointJwt unauthorizedHandler;
  private final AccessDeniedHandlerImpl accessDeniedHandler;
  private final org.springframework.core.env.Environment environment;

  public WebSecurityConfig(UserDetailsServiceImpl userDetailsService,
                           AuthEntryPointJwt unauthorizedHandler,
                           AccessDeniedHandlerImpl accessDeniedHandler,
                           org.springframework.core.env.Environment environment) {
    this.userDetailsService = userDetailsService;
    this.unauthorizedHandler = unauthorizedHandler;
    this.accessDeniedHandler = accessDeniedHandler;
    this.environment = environment;
  }

  // JWT Filter
  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  // Password encoder
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Authentication provider
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {

    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  // Authentication manager
  @Bean
  public AuthenticationManager authenticationManager(
          AuthenticationConfiguration authConfig) throws Exception {

    return authConfig.getAuthenticationManager();
  }

  // Security configuration
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
            .cors(cors -> {})
            
            .csrf(csrf -> csrf.disable())

            .exceptionHandling(exception ->
                    exception
                            .authenticationEntryPoint(unauthorizedHandler)
                            .accessDeniedHandler(accessDeniedHandler))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> {
              auth.requestMatchers(
                      "/api/auth/signup",
                      "/api/auth/signin",
                      "/api/auth/refresh-token",
                      "/error"
              ).permitAll();

              boolean isProd = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

              if (!isProd) {
                auth.requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll();
              }

              auth.anyRequest().authenticated();
            })

            .authenticationProvider(authenticationProvider());

    http.addFilterBefore(authenticationJwtTokenFilter(),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}