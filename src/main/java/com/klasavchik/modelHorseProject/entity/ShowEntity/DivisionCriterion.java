package com.klasavchik.modelHorseProject.entity.ShowEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "division_criteria",
        uniqueConstraints = @UniqueConstraint(columnNames = {"division_id", "position"}))
public class DivisionCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(nullable = false)
    private Integer position;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
