package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Autoriser l'accès public à /verify-email pour que le clic sur le lien n'ouvre pas la popup d'auth
        http
            // disable http basic auth (browser native login popup) and keep a form login instead
            .httpBasic(httpBasic -> httpBasic.disable())
            // optionally disable CSRF for simplicity on this endpoint (GETs are safe, but if you have JS POSTs later re-enable carefully)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // allow anonymous GET requests to the verify-email endpoint so clicking the link verifies without prompt
                .requestMatchers(HttpMethod.GET, "/verify-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/verify-email/**").permitAll()
                // allow favicon
                .requestMatchers("/favicon.ico").permitAll()
                // allow static resources
                .requestMatchers("/css/**", "/js/**", "/assets/**").permitAll()
                // all other requests require authentication
                .anyRequest().authenticated()
            )
            // convert 401 responses into a redirect to the login page (prevents browser basic auth popup)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
            // enable a normal form login (you can customize loginPage if you have a custom page)
            .formLogin(form -> form
                .permitAll()
            );

        return http.build();
    }
}
