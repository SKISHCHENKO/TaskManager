package task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import task.model.Role;
import task.model.User;
import task.repository.UserRepository;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // Главная страница /home
    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("message", "Welcome to TaskManager!");
        return "home"; // Возвращаем home.html
    }

    // Отображение формы регистрации
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Это страница login.html
    }

    // Обработка регистрации нового пользователя
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        if (user.getRole() == null) {
            user.setRole(Role.USER); // Роль по умолчанию
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Хешируем пароль
        userRepository.save(user);
        return "redirect:/login";
    }

    // Обработка аутентификации пользователя
    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            // Создаем объект аутентификации
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Пытаемся аутентифицировать пользователя
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // Сохраняем аутентификацию в контексте безопасности
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return "redirect:/home"; // Перенаправление на главную страницу после успешной аутентификации
        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password");
            return "login"; // Возвращаем на страницу логина с ошибкой
        }
    }
}