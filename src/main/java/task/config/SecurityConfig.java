package task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableGlobalMethodSecurity(
        securedEnabled = true,          // Для @Secured
        jsr250Enabled = true,           // Для @RolesAllowed
        prePostEnabled = true           // Для @PreAuthorize и @PostAuthorize
)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF (можно включить при необходимости)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/home").permitAll()
                        .requestMatchers("/register", "/login", "/resources/**").permitAll() // Доступ к регистрации и статическим файлам
                        .requestMatchers(HttpMethod.GET, "/tasks/**").hasAnyRole("USER", "ADMIN", "GUEST") // Гости могут только GET
                        .requestMatchers(HttpMethod.POST, "/tasks/**").hasAnyRole("USER", "ADMIN") // POST для USER и ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/tasks/**").hasRole("ADMIN") // DELETE только для ADMIN
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
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        // Пользователь с ролью GUEST (ROLE_READ)
        userDetailsManager.createUser(User
                .withUsername("guest")
                .password(passwordEncoder.encode("guest123"))
                .roles("READ") // ROLE_READ
                .build());

        // Пользователь с ролью USER (ROLE_WRITE)
        userDetailsManager.createUser(User
                .withUsername("user")
                .password(passwordEncoder.encode("user123"))
                .roles("WRITE") // ROLE_WRITE
                .build());

        // Пользователь с ролью ADMIN (ROLE_DELETE)
        userDetailsManager.createUser(User
                .withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("DELETE") // ROLE_DELETE
                .build());

        return userDetailsManager;
    }
}