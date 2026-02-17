// 6. ClassEntity (переименовал в ClassEntity, т.к. Class — зарезервированное слово)
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    private String name;
    private String description;
    @Column(name = "display_order")
    private Integer displayOrder;   // null = не задан → сортируем по id или по имени
}