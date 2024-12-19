package task.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private boolean completed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    // Изменённый конструктор, теперь с параметром для статуса
    public Task(String title, String description, boolean completed, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.status = status;
    }
}