package task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import task.model.Task;
import task.model.TaskStatus;
import task.repository.TaskRepository;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Получить все задачи и отображать их в tasks.html
    @GetMapping
    public String getTasks(Model model) {
        List<Task> tasks = taskRepository.findAll();
        model.addAttribute("tasks", tasks);  // Добавляем список задач в модель
        return "tasks";  // Возвращаем имя шаблона tasks.html
    }

    // Создать новую задачу
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody Task task) {
        // При создании задачи указываем статус по умолчанию
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING); // Например, статус "PENDING" по умолчанию
        }
        String username = getCurrentUsername();
        task.setUsername(username); // Устанавливаем имя текущего пользователя
        return taskRepository.save(task);
    }

    // Получить задачу по ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    // Обновить задачу по ID
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        // Обновление всех полей задачи
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());

        // Обновление приоритета
        if (taskDetails.getPriority() != null) {
            task.setPriority(taskDetails.getPriority());
        }

        return taskRepository.save(task);
    }

    // Удалить задачу по ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        String currentUsername = getCurrentUsername();

        // Проверяем, является ли текущий пользователь владельцем задачи или администратором
        if (!task.getUsername().equals(currentUsername) && !isAdmin()) {
            throw new SecurityException("You are not allowed to delete this task");
        }

        taskRepository.delete(task);
    }
    // Показывает задачи текущего пользователя
    @GetMapping("/my-tasks")
    public String getTasksForCurrentUser(Model model) {
        String username = getCurrentUsername();
        List<Task> tasks = taskRepository.findByUsername(username);
        model.addAttribute("tasks", tasks);
        return "tasks";  // Шаблон tasks.html покажет только задачи текущего пользователя
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Вернёт имя текущего пользователя
        }
        return null;
    }
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}