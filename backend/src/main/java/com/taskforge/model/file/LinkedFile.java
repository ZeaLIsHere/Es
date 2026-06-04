package com.taskforge.model.file;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("LINK")
@NoArgsConstructor
public class LinkedFile extends ProjectFile {

    // Polymorphism: external link returns the URL directly
    @Override
    public String getAccessUrl() {
        return getExternalUrl();
    }
}
