package com.taskforge.model.task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("MILESTONE")
@Getter
@Setter
@NoArgsConstructor
public class MilestoneTask extends BaseTask {

    @OneToMany(mappedBy = "milestoneTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubTask> subTasks = new ArrayList<>();

    // Polymorphism: MilestoneTask scores by completion ratio of sub-tasks × priority × 2
    @Override
    public double calculateScore() {
        if (subTasks == null || subTasks.isEmpty()) {
            return getPriority().getWeight();
        }
        long done = subTasks.stream().filter(SubTask::isDone).count();
        double completionRatio = (double) done / subTasks.size();
        return getPriority().getWeight() * 2.0 * completionRatio;
    }
}
