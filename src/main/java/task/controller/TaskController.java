package task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        taskRepository.delete(task);
    }
}