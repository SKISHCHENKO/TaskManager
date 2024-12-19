package task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import task.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/resources/**").permitAll() // Доступ к регистрации и статическим файлам
                        .requestMatchers(HttpMethod.GET, "/api/tasks").hasAnyRole("USER", "ADMIN", "GUEST") // Гости могут только GET
                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyRole("USER", "ADMIN") // POST для USER и ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks").hasRole("ADMIN") // DELETE только для ADMIN
                        .anyRequest().authenticated() // Остальные запросы требуют аутентификации
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/tasks", true)  // Перенаправление на /tasks после успешного логина
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") // Куда перенаправить после выхода
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}