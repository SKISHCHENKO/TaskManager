package task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Отображение формы регистрации
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Это страница login.html, которая находится в /src/main/resources/templates
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
}