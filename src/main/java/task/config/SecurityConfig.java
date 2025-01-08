package task.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    // DataSource бин, если он еще не настроен в вашем проекте.
    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        // Настройка подключения к базе данных
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/task_db"); // Укажите URL вашей базы данных
        hikariConfig.setUsername("postgres"); // Ваше имя пользователя
        hikariConfig.setPassword("postgres"); // Ваш пароль

        // Опциональные настройки
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setMaximumPoolSize(10); // Максимальное количество соединений в пуле
        hikariConfig.setMinimumIdle(5); // Минимальное количество соединений в пуле
        hikariConfig.setIdleTimeout(30000); // Время простоя соединения в миллисекундах

        return new HikariDataSource(hikariConfig);
    }

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

    // Настроим userDetailsManager для загрузки пользователей и ролей
    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        // Запрос для загрузки пользователей
        manager.setUsersByUsernameQuery("SELECT login AS username, password, true AS enabled FROM users WHERE login = ?");
        // Запрос для загрузки ролей пользователя
        manager.setAuthoritiesByUsernameQuery("SELECT login AS username, CONCAT('ROLE_', role) AS authority FROM users WHERE login = ?");

        return manager;
    }

    // Настроим AuthenticationManager для аутентификации
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsManager(dataSource()))
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    // Настроим PasswordEncoder для шифрования паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}