package com.taskforge.model.file;

import com.taskforge.interfaces.Accessible;
import com.taskforge.model.User;
import com.taskforge.model.task.BaseTask;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_files")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "file_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class ProjectFile implements Accessible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private BaseTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    // Encapsulation: private storage details exposed only through getAccessUrl()
    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "external_url", length = 1000)
    private String externalUrl;

    // Abstraction: callers never need to know if storage is local or remote
    @Override
    public abstract String getAccessUrl();

    @Override
    public String getDisplayName() {
        return this.name;
    }
}
