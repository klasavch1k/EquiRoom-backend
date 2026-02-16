package com.klasavchik.modelHorseProject.entity.model;

import com.klasavchik.modelHorseProject.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"modelMedia", "rewards"})
@ToString(exclude = {"modelMedia", "rewards"})
@Entity
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; //название лошади
    private String avatar; //аватар лошади
    private String breed; //порода лошади
    private String horseColor; //масть лошади
    private String salesInformation; //информация о продаже

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner; //владелец лошади
    private String modelMasterName; //автор модели
    private String artMasterName; //автор лошади
    private Integer yearOfPainting; //год покраски

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "model")
    @Builder.Default
    private Set<ModelMedia> modelMedia = new HashSet<>(); //фото/видео лошади

    @Builder.Default
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reward> rewards = new HashSet<>();

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

    public void addReward(Reward reward) {
        rewards.add(reward);
        reward.setModel(this);
    }

    public void addMedia(ModelMedia media) {
        modelMedia.add(media);
        media.setModel(this);
    }

}
