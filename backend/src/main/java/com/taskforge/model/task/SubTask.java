package com.taskforge.model.task;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "milestone_subtasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDone = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_task_id", nullable = false)
    private MilestoneTask milestoneTask;
}
