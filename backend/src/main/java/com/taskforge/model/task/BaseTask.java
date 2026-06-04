package com.taskforge.model.task;

import com.taskforge.interfaces.Scorable;
import com.taskforge.model.Project;
import com.taskforge.model.User;
import com.taskforge.model.file.ProjectFile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "task_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseTask implements Scorable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
    private LocalDateTime deadline;

    // "overdue" field → Lombok generates isOverdue() correctly (avoiding isIsOverdue() bug)
    @Column(name = "is_overdue", nullable = false)
    private boolean overdue = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    // Encapsulation: score is private, computed internally via calculateScore()
    private double score = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Encapsulation: assignee mutated only through reassign()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectFile> files = new ArrayList<>();

    // Encapsulation: controlled mutation methods
    public void complete() {
        this.status = TaskStatus.DONE;
        this.completedAt = LocalDateTime.now();
        this.score = calculateScore();
        this.overdue = false;
    }

    public void reassign(User newAssignee) {
        this.assignee = newAssignee;
    }

    public void markOverdue() {
        if (this.deadline.isBefore(LocalDateTime.now()) && this.status != TaskStatus.DONE) {
            this.overdue = true;
        }
    }

    // Abstraction: subclasses define exactly how score is calculated
    @Override
    public abstract double calculateScore();

    public enum TaskStatus {
        TODO, IN_PROGRESS, REVIEW, DONE
    }

    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3);

        private final int weight;

        Priority(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }
}
