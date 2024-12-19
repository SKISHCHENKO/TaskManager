package task.service;

import task.model.Task;
import task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null); // Найдём задачу по ID
    }

    public Task createTask(Task task) {
        return taskRepository.save(task); // Сохраняем новую задачу
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id); // Удаляем задачу по ID
    }
}