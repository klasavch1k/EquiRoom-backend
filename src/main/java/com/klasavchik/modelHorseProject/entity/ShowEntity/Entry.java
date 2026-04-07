// 9. Entry
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import com.klasavchik.modelHorseProject.entity.model.Model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"entryMedia"})
@EqualsAndHashCode(exclude = {"entryMedia"})
@Entity
@Table(name = "entries")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;  // ClassEntity

    private String modelName;
    private String authorIfJudge;
    private String notes;
    private Boolean admitted = false;  // допущен ли (проверили и одобрили)
    private Boolean judged = false;    // оценен ли (судья поставил баллы/место)


    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEntry status = StatusEntry.PENDING;

    private String mainPhotoUrl;

    @ElementCollection
    @CollectionTable(name = "entry_additional_photos", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> additionalPhotos = new ArrayList<>();

    private boolean active = true;

    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EntryMedia> entryMedia = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = StatusEntry.PENDING;
    }
}