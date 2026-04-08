// 4. Division
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"sections", "criteria"})
@Entity
@Table(name = "divisions")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    private String name;
    private String type;        // realistic_coats, fantasy, ammo и т.д.
    private String description;
    @Column(name = "display_order")
    private Integer displayOrder;   // null = не задан → сортируем по id или по имени

    @Builder.Default
    @OneToMany(mappedBy = "division", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Section> sections = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "division", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<DivisionCriterion> criteria = new ArrayList<>();
}