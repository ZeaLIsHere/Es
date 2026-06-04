package com.taskforge.model.task;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SIMPLE")
@NoArgsConstructor
public class SimpleTask extends BaseTask {

    // Polymorphism: SimpleTask scores by priority weight + on-time bonus
    @Override
    public double calculateScore() {
        double base = getPriority().getWeight();
        boolean onTime = getCompletedAt() != null && getCompletedAt().isBefore(getDeadline());
        return onTime ? base + 1 : base;
    }
}
