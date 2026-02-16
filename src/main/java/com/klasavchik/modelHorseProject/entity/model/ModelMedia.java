package com.klasavchik.modelHorseProject.entity.model;

import com.klasavchik.modelHorseProject.entity.settings.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
public class ModelMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
