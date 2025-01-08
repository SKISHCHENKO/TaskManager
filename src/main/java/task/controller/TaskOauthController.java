package task.controller;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth-tasks")
public class TaskOauthController {

    // Пример метода с @Secured
    @Secured("ROLE_READ")
    @GetMapping("/read-only")
    public String readOnlyTasks() {
        return "Доступ только для ROLE_READ";
    }

    // Пример метода с @RolesAllowed
    @RolesAllowed("ROLE_WRITE")
    @PostMapping("/write-only")
    public String writeOnlyTasks() {
        return "Доступ только для ROLE_WRITE";
    }

    // Пример метода с @PreAuthorize
    @PreAuthorize("hasAnyRole('WRITE', 'DELETE')")
    @GetMapping("/write-or-delete")
    public String writeOrDeleteTasks() {
        return "Доступ для ROLE_WRITE или ROLE_DELETE";
    }

    // Пример метода с проверкой пользователя
    @GetMapping("/user-specific")
    public String userSpecificTask(@RequestParam String username) {
        String currentUsername = getCurrentUsername();
        if (currentUsername != null && currentUsername.equals(username)) {
            return "Доступ для пользователя: " + username;
        }
        throw new RuntimeException("Доступ запрещён");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : null;
    }
}